#!/usr/bin/python3

import gpiod
import os
import time

def button_callback():
    print("Button was pushed!")
    os.system("linphonecsh hangup")
    time.sleep(1.0)
    os.system("linphonecsh dial 1234")

# Setup GPIO using gpiod v2 API
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

print("Starting linphonecsh daemon...")
os.system("linphonecsh init -V -c /var/lib/linphone/.linphonerc -d 6 -l /var/log/linphone.log")
time.sleep(5)
print("Configuring network...")
os.system('''linphonecsh generic "ipv6 disable" ''')
print("Configuring audio codecs...")
os.system('''linphonecsh generic "codec enable opus/48000/1" ''')
os.system('''linphonecsh generic "codec enable opus/48000/2" ''')
print("Configuring audio...")
os.system('''linphonecsh generic "soundcard show" ''')
os.system('''linphonecsh generic "ec on 200 150 128" ''')
os.system('''linphonecsh generic "el on" ''')
os.system('''linphonecsh generic "ec show" ''')
print("Configuring video...")
os.system('''linphonecsh generic "camera on" ''')
print("Registering SIP account...")
os.system("linphonecsh register --host 192.168.0.22 --username 1104 --password 1104")
time.sleep(2)

print("Doorphone ready. Press button or Ctrl+C to quit")

try:
    while True:
        print(".", end="", flush=True)
        if request.wait_edge_events(timeout=1.0):
            print("\nGPIO event detected!")
            events = request.read_edge_events()
            for event in events:
                print(f"Event type: {event.event_type}")
                button_callback()
                time.sleep(5)  # Debounce - wait 5 seconds before accepting next press
except KeyboardInterrupt:
    print("\nExiting...")
finally:
    request.release()
    os.system("linphonecsh hangup")
    os.system("linphonecsh exit")