FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://asound.conf"

do_install:append() {
    install -d ${D}${sysconfdir}
    install -m 0644 ${UNPACKDIR}/asound.conf ${D}${sysconfdir}/asound.conf
}
