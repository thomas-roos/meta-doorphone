#!/bin/sh

set -euxo pipefail

if [ -e /dev/mmcblk0p1 ]; then
    mkdir -p /tmp/mmcblk0p1
    mount /dev/mmcblk0p1 /tmp/mmcblk0p1
    if [ -f /tmp/mmcblk0p1/wpa_supplicant.conf ]; then
        echo "wpa_supplicant.conf found"
        mv /tmp/mmcblk0p1/wpa_supplicant.conf /etc/wpa_supplicant/wpa_supplicant-wlan0.conf
        chmod 600 /etc/wpa_supplicant/wpa_supplicant-wlan0.conf
        sync
    fi
    if [ -f /tmp/mmcblk0p1/wlan.network ]; then
        echo "wlan.network found"
        mv /tmp/mmcblk0p1/wlan.network  /etc/systemd/network/
        chmod 644 /etc/systemd/network/wlan.network
        sync
    fi
    umount /tmp/mmcblk0p1
    sync
fi
