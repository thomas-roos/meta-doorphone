LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

require pjproject.inc

SRC_URI[sha256sum] = "6140f7a97e318caa89c17e8d5468599671c6eed12d64a7c160dac879ba004c68"

SRC_URI += "\
    file://arm-arch.patch \
    file://install-perms.patch \
"
