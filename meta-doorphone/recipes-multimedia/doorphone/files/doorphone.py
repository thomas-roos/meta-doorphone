#!/usr/bin/python3

import gpiod
import os
import time
from gpiod.line import Edge

def button_callback():
    print("Button was pushed!")
    os.system("linphonecsh hangup")
    time.sleep(1.0)
    os.system("linphonecsh dial 1234")

# Setup GPIO using gpiod
chip = gpiod.Chip('/dev/gpiochip0')
line = chip.get_line(17)  # GPIO17 (physical pin 11)
line.request(consumer="doorphone", type=gpiod.LINE_REQ_EV_RISING_EDGE, flags=gpiod.LINE_REQ_FLAG_BIAS_PULL_DOWN)

os.system("linphonecsh init -C -d 6 -l /var/log/linphone.log -c /home/root/.linphonerc --real-early-media && sleep 3")
os.system('''linphonecsh generic "ec on 200 150 128" ''')
os.system('''linphonecsh generic "el on" ''')
os.system("linphonecsh register --host 192.168.0.22 --username 1104   --password 1104")

print("Doorphone ready. Press Ctrl+C to quit")

try:
    while True:
        if line.event_wait(sec=1):
            event = line.event_read()
            if event.type == gpiod.LineEvent.RISING_EDGE:
                button_callback()
except KeyboardInterrupt:
    print("\nExiting...")
finally:
    line.release()
    os.system("linphonecsh hangup")
    os.system("linphonecsh exit")