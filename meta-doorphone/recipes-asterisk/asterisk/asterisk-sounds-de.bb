LICENSE = "CC0-1.0"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=5ca06639fdbd49cd1ff643b084e65f60"

PV = "2.11.19"

SRC_URI = "\
  http://www.asterisksounds.org/sites/asterisksounds.org/files/sounds/de/download/asterisk-sounds-core-de-${PV}.zip;name=core;subdir=${S} \
  http://www.asterisksounds.org/sites/asterisksounds.org/files/sounds/de/download/asterisk-sounds-extra-de-${PV}.zip;name=extra;subdir=${S} \
"
SRC_URI[core.md5sum] = "b775f7ed578662d92fb444c2f76a6088"
SRC_URI[core.sha256sum] = "cbdef15430478270ff67f0f18ca7123630975df8953be3d650531bc90a5a14b6"

SRC_URI[extra.md5sum] = "9b7ef10778bcc85e939d5cd467e807f5"
SRC_URI[extra.sha256sum] = "2a416b1d8d1b661d1aab29b020280f181b880909a06c30769612e96423f39aa7"

DEPENDS = "sox-native"

B = "${WORKDIR}/build"

inherit allarch

do_compile() {
	cd ${S}
	find . -name '*.sln16' | while read f; do
		d=`dirname $f`
		b=${f%%.sln16}
		mkdir -p ${B}/$d
		sox -t raw -e signed-integer -b 16 -c 1 -r 16k "$f" -r 8k -e gsm   ${B}/${b}.gsm
		touch --reference=$f ${B}/${b}.gsm
		#sox -t raw -e signed-integer -b 16 -c 1 -r 16k "$f" -r 8k -e a-law ${B}/${b}.alaw
		#sox -t raw -e signed-integer -b 16 -c 1 -r 16k "$f" -r 8k -e mu-law ${B}/${b}.ulaw
	done
}

do_install() {
	mkdir -p ${D}${datadir}/asterisk/sounds/de
	tar cf - -C ${B} --owner root --group root --mode=a+rX,go-w . | tar xf - -C ${D}${datadir}/asterisk/sounds/de
}

FILES:${PN} = "${datadir}/asterisk/sounds/de"
