Intended for use with an Android smartphone mounted to your car dashboard. 
Could be a regular UVC camera (standard webcam interface), but purpose built car backup cameras all have composite-video out, which can then be converted to UVC using an 'Easycap' device. 
The USB video is then connected to the phone via a USB hub or an 'on the go' cable. Both of these also allow the phone to be charged at the same time. 

Compile using Android Studio using ndk version 14b: in the automatically generated local.properties file, add the line:
ndk.dir=<your SDK dir>\\ndk\\android-ndk-r14b

This has only been tested on a Galaxy A53 5G running Android 12. 

Based on the UVC driver developed by saki4510t:
https://github.com/saki4510t/UVCCamera

Also thanks to YoboZorle for his solution to handle camera input when your custom app is not currently being displayed:
https://github.com/YoboZorle/Android-Background-Camera