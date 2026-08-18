LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

require pjproject.inc

SRC_URI[sha256sum] = "32a5ab5bfbb9752cb6a46627e4c410e61939c8dbbd833ac858473cfbd9fb9d7d"

SRC_URI += "\
    file://arm-arch.patch \
    file://install-perms.patch \
\
    file://0010-Make-sure-that-NOTIFY-tdata-is-set-before-sending-it_new-129fb323a66dd1fd16880fe5ba5e6a57.patch \
    file://0020-log-dropped-packet-in-debug.patch \
"
