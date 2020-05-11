import 'dart:async';

import 'package:flutter/services.dart';

class Telephony {
  static const MethodChannel _channel =
      const MethodChannel('telephony');





  static Future<bool> callNumber(String number) async {
    if (number == null) {
        return false;
    }
    return await _channel.invokeMethod(
        'callNumber',
        <String, Object>{'number': number},
    );
  }


  static Future<bool> sendUSSD(String ussd) async {
    if (ussd == null) {
        return false;
    }
    return await _channel.invokeMethod(
        'sendUSSD',
        <String, Object>{'ussd': ussd},
    );
  }


  static Future<bool> sendSMSMessage(String number,String message) async {
    if (number == null) {
        return false;
    }    
    if (message == null) {
        return false;
    }

    return await _channel.invokeMethod(
        'sendSMSMessage',
        <String, Object>{'number': number,'message':message},
    );
  }

  static Future<bool> endCall() async {
    return await _channel.invokeMethod('endCall');
  }


}
