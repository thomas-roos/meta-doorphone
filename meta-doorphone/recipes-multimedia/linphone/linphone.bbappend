FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://linphonerc_config"


do_install:append () {
	install -d ${D}/home/root/
	cp ../linphonerc_config ${D}/home/root/.linphonerc
}

FILES:linphonec += "/home/root/.linphonerc"
