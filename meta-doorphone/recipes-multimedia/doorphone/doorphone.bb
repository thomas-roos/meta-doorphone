DESCRIPTION = "doorphone"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://doorphone.py \
           file://doorphone.service \
           file://linphonerc_config"

RDEPENDS:${PN} = "python3-core linphone linphonec rpi-gpio alsa-utils python3-gpiod belr v4l-utils"

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
    install -m 0644 ${UNPACKDIR}/linphonerc_config ${D}/var/lib/linphone/.linphonerc
    
    # Create symlink for belr grammars in standard location
    install -d ${D}${datadir}/belr/grammars
    ln -sf /opt/belledonne-communications/share/belr/grammars/sdp_grammar ${D}${datadir}/belr/grammars/sdp_grammar
    ln -sf /opt/belledonne-communications/share/belr/grammars/vcard_grammar ${D}${datadir}/belr/grammars/vcard_grammar
    ln -sf /opt/belledonne-communications/share/belr/grammars/cpim_grammar ${D}${datadir}/belr/grammars/cpim_grammar
    ln -sf /opt/belledonne-communications/share/belr/grammars/identity_grammar ${D}${datadir}/belr/grammars/identity_grammar
    ln -sf /opt/belledonne-communications/share/belr/grammars/ics_grammar ${D}${datadir}/belr/grammars/ics_grammar
}

FILES:${PN} = "/*"
