SUMMARY = "A doorphone image with A/B updates"
HOMEPAGE = "https://github.com/aws4embeddedlinux/meta-aws-demos"

LICENSE = "MIT"

# this needs to be done before installing the (dynamic) packagegroups
inherit core-image

IMAGE_INSTALL += "\
    ${CORE_IMAGE_EXTRA_INSTALL} \
    packagegroup-base \
    packagegroup-core-boot \
    "

### rauc ###
CORE_IMAGE_EXTRA_INSTALL:append = " rauc-grow-data-part"

# only adding if device is rpi, as others might have a different partition layout
IMAGE_INSTALL:append:rpi = " config-init"

# this will allow kernel updates with rauc
IMAGE_INSTALL:append = " kernel-image kernel-modules"

### tmux ###
IMAGE_INSTALL:append = " tmux"
GLIBC_GENERATE_LOCALES = "en_US.UTF-8 UTF-8"
IMAGE_INSTALL:append = " glibc-utils localedef "
IMAGE_INSTALL:append = " ssh openssh-sshd openssh-sftp openssh-scp"

### misc ###
IMAGE_INSTALL:append = " sudo"

# this will disable root password - be warned!
EXTRA_IMAGE_FEATURES ?= "allow-empty-password allow-root-login empty-root-password"

### license compliance ###
COPY_LIC_MANIFEST = "1"

COPY_LIC_DIRS = "1"

IMAGE_FEATURES += "read-only-rootfs"

# this should be equal to sdimage-doorphone-ab_partition.wks.in file,
# for rauc bundle generation wic file is not used!
ROOTFS_POSTPROCESS_COMMAND += "rootfs_user_fstab"

rootfs_user_fstab () {

# Set hostname
echo "doorphone" > ${IMAGE_ROOTFS}/${sysconfdir}/hostname

# overwrite the default fstab, adding customization for this image
cat << EOF > ${IMAGE_ROOTFS}/${sysconfdir}/fstab
/dev/root            /                    auto       defaults              1  1
proc                 /proc                proc       defaults              0  0
devpts               /dev/pts             devpts     mode=0620,ptmxmode=0666,gid=5      0  0
tmpfs                /run                 tmpfs      mode=0755,nodev,nosuid,strictatime 0  0
tmpfs                /var/volatile        tmpfs      defaults              0  0
LABEL=boot  /boot   vfat    defaults         0       0
LABEL=data     /data     ext4    x-systemd.growfs        0       0
/data/etc/wpa_supplicant             /etc/wpa_supplicant             none    bind            0       0
/data/etc/systemd/network            /etc/systemd/network            none    bind            0       0
/data/etc/systemd/system            /etc/systemd/system            none    bind            0       0
/data/var/lib/alsa      /var/lib/alsa      none    bind            0       0
EOF

install -d -m 0755 ${IMAGE_ROOTFS}/data

# copy those directories that should be present at the data partition to /data and just
# leave them empty as a mount point for the bind mount

install -d ${IMAGE_ROOTFS}/data/etc/wpa_supplicant

install -d ${IMAGE_ROOTFS}/data/etc/systemd/network
if [ -n "$(ls -A ${IMAGE_ROOTFS}/etc/systemd/network 2>/dev/null)" ]; then
    mv -f ${IMAGE_ROOTFS}/etc/systemd/network/* ${IMAGE_ROOTFS}/data/etc/systemd/network
fi

install -d ${IMAGE_ROOTFS}/data/etc/systemd/system
if [ -n "$(ls -A ${IMAGE_ROOTFS}/etc/systemd/system 2>/dev/null)" ]; then
    mv -f ${IMAGE_ROOTFS}/etc/systemd/system/* ${IMAGE_ROOTFS}/data/etc/systemd/system
fi

# decided to do here instead of a bbappend of wpa:supplicant
install -d ${IMAGE_ROOTFS}/${sysconfdir}/systemd/system/multi-user.target.wants/
ln -sf /${libdir}/systemd/system/wpa_supplicant@.service ${IMAGE_ROOTFS}/${sysconfdir}/systemd/system/multi-user.target.wants/wpa_supplicant@wlan0.service

# enable systemd-time-wait-sync as this is important to have a correct clock
ln -sf /${libdir}/systemd/system/systemd-time-wait-sync.service ${IMAGE_ROOTFS}/${sysconfdir}/systemd/system/multi-user.target.wants/

mkdir -p ${IMAGE_ROOTFS}/data/var/lib/alsa
if [ -n "$(ls -A ${IMAGE_ROOTFS}/var/lib/alsa 2>/dev/null)" ]; then
    mv -f ${IMAGE_ROOTFS}/var/lib/alsa ${IMAGE_ROOTFS}/data/var/lib/
    install -d ${IMAGE_ROOTFS}/var/lib/alsa
fi
}

# Optimizations for RAUC adaptive method 'block-hash-index'
# rootfs image size must to be 4K-aligned
IMAGE_ROOTFS_ALIGNMENT = "4"

# ext4 block size should be set to 4K and use a fixed directory hash seed to
# reduce the image delta size (keep oe-core's 4K bytes-per-inode)
EXTRA_IMAGECMD:ext4 = "-i 4096 -b 4096 -E hash_seed=86ca73ff-7379-40bd-a098-fcb03a6e719d"

### doorphone
IMAGE_INSTALL:append = " doorphone"