# Telephony

A flutter plugin to provide telephony functions

## Installation
Update your pubspec.yaml add to dependency
telephone:
    git: https://github.com/doonfrs/flutter_telephony.git


## Required Permissions
Update your android/app/src/main/AndroidManifest.xml add the foloowing lines (Maybe you don't need to add them all, google for each permission to understand more about it)
    
    <uses-permission android:name="android.permission.MODIFY_PHONE_STATE" />
    <uses-permission android:name="android.permission.CALL_PHONE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.SEND_SMS" />
    <uses-permission android:name="android.permission.READ_SMS" />
    <uses-permission android:name="android.permission.ANSWER_PHONE_CALLS" />

## Only android supported currently

#### Call Number
Telephony.callNumber("+12343");

#### Drop Current active Call
Telephony.endCall();

#### Send SMS
Telephony.sendSMSMessage("+12343","Hi");

#### Send sendUSSD
Telephony.sendUSSD("*123*1*2#");



