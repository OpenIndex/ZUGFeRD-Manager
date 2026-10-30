#!/bin/sh
#
# Application launcher inside the Flatpak sandbox.
#
# Uses the alternative launcher script, that preloads "libcef.so".
# see share/linux/launcher.sh
#

exec /app/lib/zugferd-manager/bin/ZUGFeRD-Manager.sh "$@"
