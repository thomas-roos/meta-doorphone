FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://0001-change-default-video-size-to-720p.patch"

# linphone.inc adds its extra packages with
#
#     PACKAGES += " ${PN}c ${PN}-common ${PN}-tester ${PN}-utils ${PN}-rings"
#
# which lands them AFTER ${PN} in PACKAGES. Packaging walks PACKAGES in order and
# oe-core's default FILES:${PN} contains "${bindir}/*", so the main linphone
# package claims /usr/bin/linphonec and /usr/bin/linphonecsh before the linphonec
# package can. linphonec then ends up empty (FILES_INFO:linphonec = {}), no ipk
# is written, and do_rootfs fails with "nothing provides linphonec needed by
# doorphone" - because doorphone.bb RDEPENDS on linphonec by name.
#
# Move ${PN}c ahead of ${PN} so it claims its files first. Done by moving the
# existing entry rather than prepending a second one, to avoid a duplicate in
# PACKAGES.
python () {
    pkgs = (d.getVar('PACKAGES') or '').split()
    pn = d.getVar('PN')
    c = pn + 'c'
    if c in pkgs and pn in pkgs and pkgs.index(c) > pkgs.index(pn):
        pkgs.remove(c)
        pkgs.insert(pkgs.index(pn), c)
        d.setVar('PACKAGES', ' '.join(pkgs))
}

# linphone.bb already carries INSANE_SKIP:${PN} += "buildpaths", which is why
# this never surfaced while the main package was (wrongly) shipping
# /usr/bin/linphonec. Now that the binary is packaged where it belongs, the same
# exemption has to follow it.
#
# The offending strings are belle-sip's __FILE__ paths, pulled in through its
# logging macros - they name belle-sip's own workdir, not anything linphone or
# this layer controls, so there is nothing to fix here beyond acknowledging it.
INSANE_SKIP:${PN}c += "buildpaths"
