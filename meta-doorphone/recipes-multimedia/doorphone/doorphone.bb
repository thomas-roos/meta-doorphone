DESCRIPTION = "doorphone"

LICENSE = "CLOSED"

SRC_URI = "file://doorphone.py \
           file://doorphone.service \
           file://linphonerc_config"

RDEPENDS:${PN} = "python3-core linphone linphonec rpi-gpio alsa-utils python3-gpiod"

# linphonec not available in scarthgap, needs separate recipe
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
    install -d ${D}/home/root/
    install ${UNPACKDIR}/linphonerc_config ${D}/home/root/.linphonerc
}

FILES:${PN} = "/*"
