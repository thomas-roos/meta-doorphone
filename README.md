A yocto layer setup to build a sip based doorphone for Rasperry Pi 64 using linphone, usb-soundcard, old (non libcamera ) cam and a button to start the call. Simple, but fully working. Using the new bitbake-setup approach.

## System Architecture

The doorphone connects to an Asterisk PBX server in your local network. When the doorbell button is pressed:

1. Doorphone initiates a SIP call to the Asterisk server (extension 1234)
2. Asterisk rings all registered SIP clients (tablets, phones on WiFi)
3. User answers the call on their device for audio/video communication
4. User presses a DTMF key (e.g., `*` or `#`) on their device to trigger door opener
5. Asterisk server controls the door opener relay (not directly connected to doorphone)

This architecture allows multiple devices to receive doorbell calls and any authorized device can open the door through the Asterisk server.

## Doorphone Application Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    doorphone.py starts                      │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
                    ┌────────────────┐
                    │ Initialize GPIO│
                    │ Pin 10 as INPUT│
                    │ (Pull-down)    │
                    └────────┬───────┘
                             │
                             ▼
                    ┌────────────────────┐
                    │ Setup Event Detect │
                    │ RISING edge on     │
                    │ Pin 10 (button)    │
                    └────────┬───────────┘
                             │
                             ▼
                    ┌────────────────────┐
                    │ Start linphonecsh  │
                    │ - Debug level 6    │
                    │ - Load config      │
                    └────────┬───────────┘
                             │
                             ▼
                    ┌────────────────────┐
                    │ Configure Audio    │
                    │ - Echo cancel ON   │
                    │ - Echo limiter ON  │
                    └────────┬───────────┘
                             │
                             ▼
                    ┌────────────────────┐
                    │ Register SIP       │
                    │ User: 1104         │
                    │ Host: 192.168.0.22 │
                    └────────┬───────────┘
                             │
                ┌────────────┴────────────┐
                │                         │
                ▼                         ▼
    ┌───────────────────┐      ┌──────────────────┐
    │ Wait for button   │      │ Wait for Enter   │
    │ press (GPIO 10)   │      │ key to quit      │
    └─────────┬─────────┘      └────────┬─────────┘
              │                         │
              ▼                         │
    ┌───────────────────┐               │
    │ Button Callback:  │               │
    │ 1. Hangup current │               │
    │ 2. Sleep 1 sec    │               │
    │ 3. Dial 1234      │               │
    └───────────────────┘               │
                                        │
                                        ▼
                               ┌──────────────────┐
                               │ Cleanup & Exit:  │
                               │ - GPIO cleanup   │
                               │ - Hangup call    │
                               │ - Exit linphone  │
                               └──────────────────┘
```

**Key Components:**
- **GPIO Pin 15**: Physical button input (RISING edge = button press)
- **Linphone**: SIP client for VoIP calls
- **Button action**: Hangup → Dial extension 1234
- **SIP registration**: Extension 1104 on local PBX (192.168.0.22)

## Hardware Wiring

**Hardware Requirements:**
- Raspberry Pi (tested on RPi 64-bit)
- Camera module (for video calls)
- USB soundcard (for audio input/output)
- Push button (for doorbell)

**Ring Button Connection:**
```
Raspberry Pi GPIO Header (Physical Pin Layout)
┌─────┬──────────┬──────────┬─────┐
│ Pin │   Name   │   Name   │ Pin │
├─────┼──────────┼──────────┼─────┤
│  1  │  3.3V    │   5V     │  2  │
│  3  │  GPIO2   │   5V     │  4  │
│  5  │  GPIO3   │   GND    │  6  │ ◄── Connect button here (GND)
│  7  │  GPIO4   │  GPIO14  │  8  │
│  9  │  GND     │  GPIO15  │ 10  │
│ 11  │  GPIO17  │  GPIO18  │ 12  │
│ 13  │  GPIO27  │   GND    │ 14  │
│ 15  │  GPIO22  │  GPIO23  │ 16  │ ◄── Connect button here (GPIO22)
│ ... │   ...    │   ...    │ ... │
└─────┴──────────┴──────────┴─────┘

Button Wiring:
  Pin 6 (GND) ──┬──[ Button ]──┬── Pin 15 (GPIO22)
                │              │
                └──────────────┘
                (Normally Open)
```

**Connection:**
- One side of button → Pin 6 (GND)
- Other side of button → Pin 15 (GPIO22)
- Internal pull-down resistor keeps pin LOW when button not pressed
- Button press connects to GND, creating RISING edge when released

**Note:** Using GPIO22 (Pin 15) - safe GPIO that doesn't conflict with UART or system functions.

## Configuration

**Important:** You must configure your specific network and SIP settings before use!

Edit the following files in `meta-doorphone/recipes-multimedia/doorphone/files/`:

1. **doorphone.py** - Update SIP credentials and dial numbers:
   ```python
   # Line 24: SIP server and credentials
   os.system("linphonecsh register --host 192.168.0.22 --username 1104 --password 1104")

   # Line 13: Extension to dial when button pressed
   os.system("linphonecsh dial 1234")
   ```

2. **linphonerc_config** - Update SIP account settings and audio configuration to match your PBX server and network environment.

## Building with bitbake-setup

### Quick Start

1. Clone with submodules:
```bash
git clone --recurse-submodules https://github.com/thomas-roos/meta-doorphone
```

Or if already cloned:
```bash
git submodule update --init --recursive
```

2. Initialize the build environment:
```bash
cd bitbake/bin/ && \
./bitbake-setup --setting default top-dir-prefix $PWD/../../ init \
  $PWD/../../bitbake-setup.conf.json \
  doorphone distro/poky-altcfg core/yocto/sstate-mirror-cdn --non-interactive && \
  cd -
```

3. Source the build environment:
```bash
. ./bitbake-builds/bitbake-setup-doorphone-distro_poky-altcfg/build/init-build-env
```

4. Build the image:
```bash
bitbake doorphone-image
```

Or rauch bundle:

```bash
bitbake doorphone-bundle
```


5. Resulting image:

```bash
./bitbake-builds/bitbake-setup-doorphone-distro_poky-altcfg/build/tmp/deploy/images/raspberrypi-armv8/doorphone-image-raspberrypi-armv8.rootfs.wic.bz2
```

Or rauc bundle:

```bash
./bitbake-builds/bitbake-setup-doorphone-distro_poky-altcfg/build/tmp/deploy/images/raspberrypi-armv8/doorphone-bundle-raspberrypi-armv8.raucb
```

# Mount rw

``` bash
mount -o remount,rw /
```

# Camera settings

``` bash
/boot/config.txt
    disable_camera_led=1

root@doorphone:~# linphonecsh generic "help camera"
'camera on'	: allow sending of local camera video to remote end.
'camera off'	: disable sending of local camera's video to remote end.


linphonecsh generic "webcam list"

root@doorphone:~# linphonecsh generic "webcam list"
0: V4L2: /dev/video0
1: StaticImage: Static picture

root@doorphone:~# linphonecsh generic "webcam use 0"

```

## ALSA volume / debug

```bash
alsamixer

alsactl store
```

## ALSA debug

```bash

arecord -l

linphonecsh generic "soundcard list"

root@doorphone:~# linphonecsh generic "soundcard show"
Ringer device: ALSA Unknown: USB Audio Device
Playback device: ALSA Unknown: USB Audio Device
Capture device: ALSA Unknown: USB Audio Device

# disable onboard soundcard
/boot/config.txt
  dtparam=audio=off


arecord -D hw:0,0 -V stereo -r 44100 -f S16_LE -c 2 | aplay

speaker-test -t wav -c 2

arecord -D hw:0,0 -f S16_LE -r 48000 -c 1 -d 5 | aplay -D plughw:0,0

This records 5 seconds from USB audio (card 1) and plays it back immediately. Speak into the mic and you should hear yourself.

Or test separately:

# Record 5 seconds
arecord -D plughw:0,0 -f S16_LE -r 48000 -c 1 -d 5 /tmp/test.wav
# Play it back
aplay -D plughw:0,0 /tmp/test.wav
```


## General debugging

```bash
journalctl -xefu doorphone

tail -f /var/log/linphone.log

grep "MSV4l2\|video.*size\|vsize" /var/log/linphone.log | tail -30
```



## Ideas

- add rpi watchdog