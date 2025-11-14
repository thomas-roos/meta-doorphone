FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://linphonerc_config"


do_install:append () {
	install -d ${D}/home/root/
	install -m 0644 ${UNPACKDIR}/linphonerc_config ${D}/home/root/.linphonerc
}

FILES:${PN}c += " /home/root/.linphonerc"
