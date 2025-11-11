#!/usr/bin/python3

import RPi.GPIO as GPIO # Import Raspberry Pi GPIO library
import os
import time

def button_callback(channel):
    print("Button was pushed!")
    os.system("linphonecsh hangup")
#    os.system("linphonecsh dial 1101")
    time.sleep (1.0)
#    os.system("linphonecsh dial 1102")
    os.system("linphonecsh dial 1234")


GPIO.setwarnings(False) # Ignore warning for now
GPIO.setmode(GPIO.BOARD) # Use physical pin numbering
GPIO.setup(10, GPIO.IN, pull_up_down=GPIO.PUD_DOWN) # Set pin 10 to be an input pin and set initial value to be pulled low (off)
GPIO.add_event_detect(10,GPIO.RISING,callback=button_callback, bouncetime=5000) # Setup event on pin 10 rising edge
os.system("linphonecsh init -C -d 6 -l /var/log/linphone.log -c /home/root/.linphonerc --real-early-media && sleep 3")
os.system('''linphonecsh generic "ec on 200 150 128" ''')
os.system('''linphonecsh generic "el on" ''')
os.system("linphonecsh register --host 192.168.0.22 --username 1104   --password 1104")
message = input("Press enter to quit\n\n") # Run until someone presses enter
GPIO.cleanup() # Clean up
os.system("linphonecsh hangup")
os.system("linphonecsh exit")