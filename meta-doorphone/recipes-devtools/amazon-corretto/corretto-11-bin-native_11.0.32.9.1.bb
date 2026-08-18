SUMMARY = "Amazon Corretto 11 - Native"

# meta-bc ships its own corretto-11-bin-native, but it hardcodes ${PV} into a
# require of a meta-aws recipe file:
#
#   require ${COREBASE}/../meta-aws/.../corretto-11-bin_${PV}.bb
#
# so the two layers must agree on the exact Corretto version. meta-bc is pinned
# at 11.0.29.7.1 while meta-aws has moved to 11.0.32.9.1, which makes meta-bc's
# copy fail to parse ("Could not include required file") and halts the build.
#
# This is the same recipe against the version meta-aws actually ships, and
# meta-bc's copy is BBMASKed in conf/layer.conf. antlr3-native DEPENDS on
# "corretto-11-bin-native" by name, so it resolves here instead; antlr3 is
# needed by belr, which the doorphone's linphone stack needs.
#
# Delete this (and the BBMASK) once meta-bc tracks meta-aws's Corretto version.

require ${COREBASE}/../meta-aws/recipes-devtools/amazon-corretto/corretto-11-bin_${PV}.bb

inherit native

# Disable ptest for native
SRC_URI:remove = "file://run-ptest"
RDEPENDS:${PN}-ptest = ""
