A yocto layer setup to build a sip based doorphone for Rasperry Pi 64 using linphone, usb-soundcard, camera and a button to start the call. Simple, but fully working. Using the new bitbake-setup approach.

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
- **GPIO Pin 11**: Physical button input (RISING edge = button press)
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
│ 11  │  GPIO17  │  GPIO18  │ 12  │ ◄── Connect button here (GPIO17)
│ ... │   ...    │   ...    │ ... │
└─────┴──────────┴──────────┴─────┘

Button Wiring:
  Pin 6 (GND) ──┬──[ Button ]──┬── Pin 11 (GPIO17)
                │              │
                └──────────────┘
                (Normally Open)
```

**Connection:**
- One side of button → Pin 6 (GND)
- Other side of button → Pin 11 (GPIO17)
- Internal pull-down resistor keeps pin LOW when button not pressed
- Button press connects to GND, creating RISING edge when released

**Note:** Using GPIO17 (Pin 11) avoids conflict with UART which is required by U-Boot and RAUC.

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

# Debug / Tuning (optional)
``` bash
/boot/config.txt
    disable_camera_led=1
    # U-Boot requires UART
    enable_uart=1

 linphonec -C -d 6 -l /var/log/linphone.log
    linphone-daemon -C --log /var/log/linphone.log
    register sip:1104@192.168.0.22 192.168.0.22 1104 1104
    daemon-linphone>call sip:1102@192.168.0.22


linphonecsh init -C -d 6 -l /var/log/linphone.log
linphonecsh register --host 192.168.0.22 --username 1104 --password 1104
linphonecsh dial 1102


#!/bin/bash
linphonecsh init
sleep 4

vi ~/.linphonerc
vga -> 720p

ec_delay

ec_tail_len

ec_frame_size
ec on

ec show

ec on 200 150 128

ec

arecord -l

arecord -D hw:0,0 -V stereo -r 44100 -f S16_LE -c 2 /dev/null

arecord -D hw:0,0 -V stereo -r 44100 -f S16_LE -c 2 | aplay

alsactl store

alsactl restore
```