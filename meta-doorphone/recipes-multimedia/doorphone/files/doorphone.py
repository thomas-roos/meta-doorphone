#!/usr/bin/python3

import gpiod
import os
import time

def button_callback():
    print("Button was pushed!")
    os.system("linphonecsh dial sip:1234@192.168.0.22")

print("Starting linphonecsh daemon...")
os.system("linphonecsh init -V -d 6 -l /var/log/linphone.log")
time.sleep(2)

print("Configuring network...")
os.system('linphonecsh generic "ipv6 disable"')

print("Configuring codecs...")
os.system('linphonecsh generic "codec disable all"')
os.system('linphonecsh generic "codec enable opus"')

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
os.system("linphonecsh register --host 192.168.0.22 --username 1104 --password 1104")
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
