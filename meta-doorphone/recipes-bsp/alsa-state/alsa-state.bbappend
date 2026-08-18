FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://asound.conf"

do_install:append() {
    install -d ${D}${sysconfdir}
    install -m 0644 ${UNPACKDIR}/asound.conf ${D}${sysconfdir}/asound.conf
}

# Ship the mixer state the doorphone actually needs instead of oe-core's stock
# asound.state. FILESEXTRAPATHS above gives this copy precedence, and the
# upstream recipe already installs every file://*.state it finds into
# ${localstatedir}/lib/alsa - so no SRC_URI entry is needed here (adding one
# would just duplicate the upstream fetch).
#
# The captured state keeps the digital path at full scale (Speaker Playback
# Volume 30/30 = 0dB) and the mic sidetone muted. Full scale is deliberate: the
# LM386 amp's noise floor is fixed and sits downstream of its input pot, so the
# best signal-to-noise at the speaker comes from feeding the amp the largest
# clean signal and attenuating in the analog stage, never the reverse.
#
# NOTE doorphone-image.bb moves /var/lib/alsa onto the /data partition at rootfs
# time, so this file only seeds a freshly flashed device; on an already-deployed
# one the persisted state on /data wins and RAUC updates leave it alone.
