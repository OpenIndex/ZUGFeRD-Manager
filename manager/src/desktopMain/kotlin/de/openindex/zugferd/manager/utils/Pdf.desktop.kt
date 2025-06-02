/*
 * Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.openindex.zugferd.manager.utils

import de.openindex.zugferd.manager.APP_LOGGER
import de.openindex.zugferd.manager.APP_TITLE_FULL
import de.openindex.zugferd.manager.APP_VERSION
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.cos.COSArray
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDDocumentCatalog
import org.apache.pdfbox.pdmodel.PDDocumentInformation
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode
import org.apache.pdfbox.pdmodel.common.PDMetadata
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent
import org.apache.xmpbox.xml.DomXmpParser
import org.apache.xmpbox.xml.XmpParsingException
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromPDFA
import org.mustangproject.ZUGFeRD.ZUGFeRDImporter
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Collections
import java.util.GregorianCalendar

actual suspend fun getPdfArchiveVersion(pdfFile: PlatformFile): Int {
    return try {
        getPDFAVersion(pdfFile.file)
    } catch (e: Exception) {
        APP_LOGGER.error("Can't extract PDF/A version!", e)
        -1
    }
}

/**
 * taken from ZUGFeRDExporterFromPDFA
 * @see ZUGFeRDExporterFromPDFA.getPDFAVersion
 */
private fun getPDFAVersion(pdfFile: File): Int {
    val document = Loader.loadPDF(pdfFile)
    val catalog: PDDocumentCatalog = document.getDocumentCatalog()
    val metadata = catalog.metadata

    // the PDF version we could get through the document, but we want the PDF-A version,
    // which is different (and can probably base on different PDF versions)
    if (metadata != null) {
        try {
            val xmpParser = DomXmpParser()
            val xmp = xmpParser.parse(metadata.createInputStream())

            val pdfASchema = xmp.pdfaIdentificationSchema
            if (pdfASchema != null) {
                return pdfASchema.part
            }
        } catch (e: XmpParsingException) {
            APP_LOGGER.error("XmpParsingException", e)
        } finally {
            document.close()
        }
    }
    return 0
}

/**
 * Convert any kind of PDF to PDF/A-3.
 *
 * This won't work in any case. The provided PDF should embed its fonts,
 * should not use external referenced assets and should not contain transparency.
 *
 * inspired by https://github.com/ETDA/PdfAConverter
 */
actual suspend fun convertToPdfArchive(pdfFile: PlatformFile): PlatformFile =
    withContext(Dispatchers.IO) {
        val outputFile = File.createTempFile(
            pdfFile.name.substringBeforeLast("."),
            ".pdf",
        )

        convertToPdfArchive(
            inputFile = pdfFile.file,
            outputFile = outputFile,
        )

        PlatformFile(outputFile)
    }

@Throws(IOException::class)
private fun convertToPdfArchive(
    inputFile: File,
    outputFile: File,
    embedFile: File? = null,
    colorProfile: File? = null,
    xmpTemplate: File? = null,
    pdfVersion: String = "1.7",
) {
    fun makeA3compliant(
        document: PDDocument,
    ): PDDocumentCatalog {
        val cat = document.documentCatalog
        val pdd = document.documentInformation
        val metadata = PDMetadata(document)
        cat.metadata = metadata

        val pdi = PDDocumentInformation()
        pdi.producer = pdd.producer
        pdi.author = pdd.author
        pdi.title = pdd.title
        pdi.subject = pdd.subject
        pdi.keywords = pdd.keywords

        // Set OID
        // pdi.setCustomMetadataValue("OID", "10.2.3.65.5");
        document.documentInformation = pdi

        // XMP Metadata
        val xmpTemplateInput = xmpTemplate?.inputStream()
            ?: {}.javaClass.getResourceAsStream("pdf/xmpTemplate.xml")

        val now = Instant.now()
        val editedBytes = xmpTemplateInput.reader(charset = Charsets.UTF_8).use { reader ->
            reader.readText()
                .replace(
                    "@PDF_PRODUCER@",
                    pdi.producer,
                )
                .replace(
                    "@PDF_VERSION@",
                    pdfVersion,
                )
                .replace(
                    "@XMP_CREATOR_TOOL@",
                    "$APP_TITLE_FULL $APP_VERSION",
                )
                .replace(
                    "@XMP_CREATE_DATE@",
                    DateTimeFormatter.ISO_INSTANT.format(
                        (pdi.creationDate?.toInstant() ?: now)
                            .truncatedTo(ChronoUnit.SECONDS)
                    ),
                )
                .replace(
                    "@XMP_MODIFY_DATE@",
                    DateTimeFormatter.ISO_INSTANT.format(
                        (pdi.modificationDate?.toInstant() ?: now)
                            .truncatedTo(ChronoUnit.SECONDS)
                    ),
                )
                .replace(
                    "@XMP_METADATA_DATE@",
                    DateTimeFormatter.ISO_INSTANT.format(
                        now
                            .truncatedTo(ChronoUnit.SECONDS)
                    ),
                )
                .toByteArray(charset = Charsets.UTF_8)
        }

        metadata.importXMPMetadata(editedBytes)

        return cat
    }

    fun attachFile(
        document: PDDocument,
    ) {
        val file = embedFile!!
        val efTree = PDEmbeddedFilesNameTreeNode()

        val subType = Files.probeContentType(file.toPath())
        val embedFileName = file.name

        // first create the file specification, which holds the embedded file
        val fs = PDComplexFileSpecification()
        fs.file = embedFileName
        val dict = fs.cosObject
        // Relation "Source" for linking with e.g. catalog
        dict.setName("AFRelationship", "Source")

        dict.setString("UF", embedFileName)

        val input: InputStream = FileInputStream(embedFile)

        val ef = PDEmbeddedFile(document, input)

        // set some of the attributes of the embedded file
        ef.modDate = GregorianCalendar.getInstance()
        ef.size = file.length().toInt()
        ef.creationDate = GregorianCalendar()
        fs.embeddedFile = ef
        ef.subtype = subType

        // now add the entry to the embedded file tree and set in the document.
        efTree.names = Collections.singletonMap(embedFileName, fs)

        // attachments are stored as part of the "names" dictionary in the
        val catalog = document.documentCatalog

        val names = PDDocumentNameDictionary(document.documentCatalog)
        names.embeddedFiles = efTree
        catalog.names = names

        val dict2 = catalog.cosObject
        val array = COSArray()
        array.add(fs.cosObject)
        dict2.setItem("AF", array)
    }

    fun addOutputIntent(
        document: PDDocument,
        catalog: PDDocumentCatalog,
    ) {
        if (catalog.outputIntents.isEmpty()) {
            val colorProfileInput = colorProfile?.inputStream()
                ?: {}.javaClass.getResourceAsStream("pdf/sRGB.icm")

            colorProfileInput.use { input ->
                val oi = PDOutputIntent(document, input)
                oi.info = "sRGB IEC61966-2.1"
                oi.outputCondition = "sRGB IEC61966-2.1"
                oi.outputConditionIdentifier = "sRGB IEC61966-2.1"
                @Suppress("HttpUrlsUsage")
                oi.registryName = "http://www.color.org"
                catalog.addOutputIntent(oi)
            }
        }
    }

    Loader.loadPDF(inputFile).use { doc ->
        val cat = makeA3compliant(
            document = doc,
        )

        if (embedFile != null) {
            attachFile(
                document = doc,
            )
        }

        addOutputIntent(
            document = doc,
            catalog = cat,
        )

        doc.version = pdfVersion.toFloat()

        outputFile.outputStream().buffered().use { output ->
            doc.save(output)
        }
    }
}

@Throws(IOException::class)
fun PDDocument.removeEmbeddedFiles(): PDDocument {
    val names = PDDocumentNameDictionary(documentCatalog)
    names.embeddedFiles = PDEmbeddedFilesNameTreeNode()
    documentCatalog.names = names
    return this
}

actual fun getXmlFromPdf(pdf: PlatformFile): String? {
    return try {
        pdf.file
            .inputStream().use { input ->
                ZUGFeRDImporter(input)
            }
            .utF8
            ?.trimToNull()
    } catch (e: Exception) {
        APP_LOGGER.error("PDF read error.", e)
        null
    }
}
