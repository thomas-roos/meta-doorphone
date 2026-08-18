DESCRIPTION = "doorphone"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://doorphone.py \
           file://doorphone.service"

S = "${UNPACKDIR}"

# belle-sip ships /usr/share/belr/grammars/sdp_grammar, which belle-sip loads at
# runtime to parse SDP. This used to be satisfied by symlinks into
# /opt/belledonne-communications (linphone-sdk's INSTALL_PATH) created right here
# in do_install, but linphone-sdk no longer builds at all - it DEPENDS on
# yasm-native, which oe-core has dropped - so those symlinks dangled and
# linphonec aborted the moment a call was answered. Depend on the real files
# instead; recipes-bc/belle-sip moves them out of belle-sip's -dev package.
RDEPENDS:${PN} = "python3-core linphone linphonec rpi-gpio alsa-utils python3-gpiod belr belle-sip v4l-utils"

DEPENDS = "alsa-utils \
           htop \
           python3-pip \
           espeak"

inherit systemd
SYSTEMD_SERVICE:${PN} = "doorphone.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_install() {
    install -m 0755 -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/doorphone.py ${D}${bindir}
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/doorphone.service ${D}${systemd_system_unitdir}/
    install -d ${D}/var/lib/linphone
}

FILES:${PN} = "/*"
