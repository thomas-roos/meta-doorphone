DESCRIPTION = "A update bundle for doorphone-image"

inherit bundle

RAUC_BUNDLE_DESCRIPTION = "doorphone-bundle"

RAUC_BUNDLE_SLOTS = "rootfs"
RAUC_SLOT_rootfs = "doorphone-image"

RAUC_BUNDLE_COMPATIBLE   ?= "${MACHINE}"
RAUC_BUNDLE_FORMAT ?= "verity"

# those are the certs that are contained in meta-rauc-community/meta-rauc-raspberrypi
# they are intended for demo purpose only
RAUC_KEY_FILE ?= "${THISDIR}/files/development-1.key.pem"
RAUC_CERT_FILE ?= "${THISDIR}/files/development-1.cert.pem"

# uncomment for enabling adaptive update method 'block-hash-index'
RAUC_SLOT_rootfs[fstype] = "ext4"
RAUC_SLOT_rootfs[adaptive] = "block-hash-index"
