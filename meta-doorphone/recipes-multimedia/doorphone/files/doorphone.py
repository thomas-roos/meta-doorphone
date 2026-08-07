#!/usr/bin/python3

import gpiod
import os
import re
import subprocess
import time

# The PBX moved from the dev PC (mother, Asterisk 18) to the Raspberry Pi that
# also runs Home Assistant (Asterisk 22).
#
# Use the Fritz!Box hostname rather than a literal address: the Pi's wired
# interface is on DHCP (systemd-networkd's stock 80-wired.network, no static
# config anywhere), so 192.168.0.192 is only a lease and could change. The
# Fritz!Box registers "ha.fritz.box" for it and is also the doorphone's DNS
# server, so the name always tracks the lease.
PBX_HOST = "ha.fritz.box"
SIP_USER = "1104"
SIP_PASS = "1104"

def button_callback():
    print("Button was pushed!")
    os.system(f"linphonecsh dial sip:1234@{PBX_HOST}")

print("Starting linphonecsh daemon...")
os.system("linphonecsh init -V -d 6 -l /var/log/linphone.log")
time.sleep(2)

print("Configuring network...")
os.system('linphonecsh generic "ipv6 disable"')

print("Configuring codecs...")
# G.722 everywhere, by policy - one wideband codec for every client, so
# negotiation is deterministic and nothing depends on who answers first.
#
# G.722 rather than opus specifically because Home Assistant's SIP client
# (hass-sip) speaks only PCMU/PCMA/G.722, and Asterisk cannot bridge it to an
# opus-only leg: 22.8.2 ships no codec_opus at all (--with-opus only gates
# res_format_attr_opus for pass-through), so there is no transcoder. HA carries
# no audio - the Pi has no microphone or speaker - but it must still be able to
# negotiate *something* to accept the INVITE, or the leg goes straight to Down
# and no sip_incoming_call event fires. Measured: opus-only gave "format
# (opus|vp8)" and no notification; G.722 gave "(g722|vp8)" and Ringing.
#
# PCMU/PCMA are kept only as fallbacks for anything that cannot do G.722.
# ("codec" is the audio list in linphonec; video codecs are "vcodec" and are
# unaffected, so the VP8 camera stream still works.)
#
# NOTE linphonec's "codec enable" takes an INDEX, not a name - "codec enable
# g722" (or "opus") is silently ignored, which is why this used to run on opus
# regardless of what the script said. Look the indices up at runtime instead of
# hardcoding them, since they shift with the built-in codec list.
WANTED_CODECS = ("G722", "PCMU", "PCMA")


def configure_codecs():
    os.system('linphonecsh generic "codec disable all"')
    listing = subprocess.run(
        ["linphonecsh", "generic", "codec list"],
        capture_output=True, text=True, timeout=15,
    ).stdout
    # lines look like:  " 6: G722 (8000) disabled"
    by_name = {}
    for line in listing.splitlines():
        m = re.match(r"\s*(\d+):\s+(\S+)\s", line)
        if m:
            by_name.setdefault(m.group(2).upper(), m.group(1))
    for name in WANTED_CODECS:
        idx = by_name.get(name)
        if idx is None:
            print(f"  codec {name} not offered by this linphone build, skipping")
            continue
        os.system(f'linphonecsh generic "codec enable {idx}"')
        print(f"  enabled {name} (index {idx})")


configure_codecs()

print("Configuring audio...")
os.system('linphonecsh generic "soundcard use 0"')
# Echo cancellation: delay=200ms (audio path delay), tail_len=150ms (echo duration), framesize=128 samples
os.system('linphonecsh generic "ec on 200 150 128"')
# Echo limiter: additional echo suppression
os.system('linphonecsh generic "el on"')

print("Configuring video...")
os.system('linphonecsh generic "camera on"')
os.system('linphonecsh generic "pwindow hide"')

print("Registering SIP account...")
os.system(f"linphonecsh register --host {PBX_HOST} --username {SIP_USER} --password {SIP_PASS}")
time.sleep(2)
os.system("linphonecsh status register")

# Setup GPIO after linphone is ready
print("Setting up GPIO22 (Pin 15)...")
request = gpiod.request_lines(
    "/dev/gpiochip0",
    consumer="doorphone",
    config={
        22: gpiod.LineSettings(
            direction=gpiod.line.Direction.INPUT,
            edge_detection=gpiod.line.Edge.RISING,
            bias=gpiod.line.Bias.PULL_DOWN
        )
    }
)
print("GPIO22 configured successfully")

print("Doorphone ready. Press button or Ctrl+C to quit")

try:
    while True:
        print(".", end="", flush=True)
        
        if request.wait_edge_events(timeout=1.0):
            print("\nGPIO event detected!")
            # Read all events but only process once
            events = request.read_edge_events()
            print(f"Received {len(events)} event(s), processing first one")
            
            button_callback()
            
            # Clear any additional events that occurred during callback
            time.sleep(0.1)
            if request.wait_edge_events(timeout=0):
                request.read_edge_events()
            
            # Wait 5 seconds before accepting new events
            time.sleep(5)
            
except KeyboardInterrupt:
    print("\nExiting...")
finally:
    request.release()
    os.system("linphonecsh hangup")
    os.system("linphonecsh unregister")
    os.system("linphonecsh exit")
