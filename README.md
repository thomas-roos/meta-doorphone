A meta-layer to build a sip based doorphone

Currently used in a build-farm for the following boards
* Raspberry Pi, all flavors, but most testing on RPi4, RPi3 and RPi0-W 


This layer depends on:

    URI: git://git.yoctoproject.org/poky.git
    branch: master

    URI: git://git.openembedded.org/meta-openembedded
    branch: master

    URI: git://git.yoctoproject.org/meta-yocto.git
    branch: master

    URI: git://git.yoctoproject.org/meta-raspberrypi.git
    branch: master


meta-tro layer maintainer: Thomas Roos <thomas@roosesweb.de>

## Building with bitbake-setup

### Quick Start

1. Clone bitbake:
```bash
git clone https://git.openembedded.org/bitbake
cd bitbake
```

2. Initialize the build environment:
```bash
./bin/bitbake-setup --setting default top-dir-prefix $PWD init \
  /path/to/meta-doorphone/bitbake-setup.conf.json \
  doorphone machine/raspberrypi4-64 distro/poky-altcfg --non-interactive
```

Or directly from GitHub:
```bash
./bin/bitbake-setup --setting default top-dir-prefix $PWD init \
  https://raw.githubusercontent.com/user/meta-doorphone/master/bitbake-setup.conf.json \
  doorphone machine/raspberrypi4-64 distro/poky-altcfg --non-interactive
```

3. Source the build environment:
```bash
. ./bitbake-builds/bitbake-setup-doorphone-distro_poky-altcfg-machine_raspberrypi4-64/build/init-build-env
```

4. Build the image:
```bash
bitbake core-image-minimal
```

### Available Distros

- `distro/poky` - Poky distribution
- `distro/poky-altcfg` - Poky with alternative configuration

The configuration includes CDN sstate mirror acceleration for faster builds.

steps to get started for me (borrored from: <https://jumpnowtek.com/rpi/Raspberry-Pi-4-64bit-Systems-with-Yocto.html>):

bitbake console: 

    bitbake console-image

different console:

    sudo su
    
    export MACHINE=raspberrypi4-64

    export OETMP=/home/tro/yocto/build/tmp


    once: cd /home/tro/yocto/poky/meta-rpi64/scripts/ 
		 sudo ./mk2parts.sh sde
          sudo mkdir /media/card

    /home/tro/yocto/poky/meta-rpi64/scripts/copy_boot.sh sde
    
    /home/tro/yocto/poky/meta-rpi64/scripts/copy_rootfs.sh sde console
    
work:

root pwd: toor

/boot partition edit config.txt
	start_x=1
    disable_camera_led=1

misc notepad:

    ssh root@192.168.0.33    
    linphonec -C -d 6 -l /var/log/linphone.log
    linphone-daemon -C --log /var/log/linphone.log 
    register sip:1104@192.168.0.22 192.168.0.22 1104 1104
    daemon-linphone>call sip:1102@192.168.0.22 


linphonecsh init -C -d 6 -l /var/log/linphone.log
linphonecsh register --host 192.168.0.22 --username 1104   --password 1104
linphonecsh dial 1102 


#!/bin/bash
linphonecsh init
sleep 4

  #VOIP benutzer auf meiner FritzBox
linphonecsh generic "soundcard use files"
linphonecsh dial **613   # Interne Rufnummer der Fritzbox anrufen
sleep 2
linphonecsh generic "speak default hallo"
sleep 8      #Nötig, damit linphone nicht auflegt, bevor die Sprachausgabe zu Ende ist. Bei längeren Texten entsprechend erhöhen.
linphonecsh hangup
linphonecsh exit
sleep 2




vi ~/.linphonerc
vga -> 720p

ec_delay

ec_tail_len

ec_frame_size
ec on

ec show

ec on 200 150 128
ec
ffmpeg -f video4linux2 -i /dev/video0 -s 1920x1080 -ss 0:0:2 -frames 1  image.jpg 


root@rpi4:~# python3
Python 3.8.2 (default, Feb 25 2020, 10:39:28) 
[GCC 9.3.0] on linux
Type "help", "copyright", "credits" or "license" for more information.
>>> import RPi.GPIO


arecord -l


arecord -D hw:0,0 -V stereo -r 44100 -f S16_LE -c 2 /dev/null

arecord -D hw:0,0 -V stereo -r 44100 -f S16_LE -c 2 | aplay 


alsactl store
alsactl restore
