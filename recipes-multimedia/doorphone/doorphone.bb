DESCRIPTION = "doorphone"

SRCREV = "${AUTOREV}"

LICENSE = "CLOSED"
S = "${WORKDIR}/sources"

SRC_URI = "file://doorphone.py \
           file://doorphone \
           file://linphonerc_config"

RDEPENDS:${PN} = "python3-core"

# linphonec not available in scarthgap, needs separate recipe
DEPENDS = "alsa-utils \
           htop \
           python3-pip \
           espeak"

inherit update-rc.d
INITSCRIPT_NAME = "doorphone"
INITSCRIPT_PARAMS = "defaults"

do_install() {
    install -m 0755 -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/doorphone.py ${D}${bindir}
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/doorphone ${D}${sysconfdir}/init.d/
    install -d ${D}/home/root/
    install ${WORKDIR}/linphonerc_config ${D}/home/root/.linphonerc
}

FILES:${PN} = "/*"
