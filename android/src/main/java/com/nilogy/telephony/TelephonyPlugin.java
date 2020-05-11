package com.nilogy.telephony;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.net.Uri;
import android.Manifest;
import android.telephony.TelephonyManager;

import android.telephony.SubscriptionManager;

import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.telephony.SmsManager;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.content.ComponentName;

/** TelephonyPlugin */
public class TelephonyPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native
  /// Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine
  /// and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Activity activity;

  private static final int REQUEST_CALL_PHONE_PERMISSION = 123;
  private static final int REQUEST_SMS_PERMISSION = 124;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "telephony");
    channel.setMethodCallHandler(this);
  }

  // This static function is optional and equivalent to onAttachedToEngine. It
  // supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new
  // Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith
  // to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith
  // will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both
  // be defined
  // in the same class.
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "telephony");
    channel.setMethodCallHandler(new TelephonyPlugin());
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    this.activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    this.activity = null;
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

    if (call.method.equals("callNumber")) {
      String number = call.argument("number");
      callNumber(number);
      result.success(true);
    } else if (call.method.equals("endCall")) {
      endCall();
      result.success(true);
    } else if (call.method.equals("sendSMSMessage")) {
      String number = call.argument("number");
      String message = call.argument("message");
      sendSMSMessage(number, message);
      result.success(true);
    } else if (call.method.equals("sendUSSD")) {
      String ussd = call.argument("ussd");
      sendUSSD(ussd);
      result.success(true);
    } else {
      result.notImplemented();
    }

  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  protected void sendSMSMessage(String number, String message) {

    boolean hasPermission = activity
        .checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;

    if (!hasPermission) {
      requestSMSPermission();
      return;
    }

    SmsManager smsManager = SmsManager.getDefault();
    smsManager.sendTextMessage(number, null, message, null, null);

  }

  private void callNumber(String num) {
    boolean hasPermission = activity
        .checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;

    if (!hasPermission) {
      requestCallPermission();
      return;
    }

    String number = "tel:" + num.trim();
    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
    final int simSlotIndex = 0; // Second sim slot

    // https://stackoverflow.com/questions/13231962/call-from-second-sim
    intent.putExtra("simSlot", simSlotIndex);
    intent.putExtra("com.android.phone.extra.slot", simSlotIndex); // For sim 1


    try {

      final Method getSubIdMethod = SubscriptionManager.class.getDeclaredMethod("getSubId", int.class);
      getSubIdMethod.setAccessible(true);
      final long subIdForSlot = ((long[]) getSubIdMethod.invoke(SubscriptionManager.class, simSlotIndex))[0];

      final ComponentName componentName = new ComponentName("com.android.phone",
          "com.android.services.telephony.TelephonyConnectionService");
      final PhoneAccountHandle phoneAccountHandle = new PhoneAccountHandle(componentName, String.valueOf(subIdForSlot));
      intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandle);
    } catch (Exception e) {
      e.printStackTrace();
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    activity.startActivity(intent);
  }

  private boolean endCall() {

    boolean hasPermission = activity
        .checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;

    if (!hasPermission) {
      requestCallPermission();
      return false;
    }

    TelecomManager tm = (TelecomManager) activity.getSystemService(Context.TELECOM_SERVICE);

    if (tm != null) {
        boolean success = tm.endCall();
        return success;
    }
    return false;
  }

  private void sendUSSD(String ussd) {

    boolean hasPermission = activity
        .checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;

    if (!hasPermission) {
      requestCallPermission();
      return;
    }

    String uriString = "";

    if (!ussd.startsWith("tel:"))
      uriString += "tel:";

    for (char c : ussd.toCharArray()) {

      if (c == '#')
        uriString += Uri.encode("#");
      else
        uriString += c;
    }

    Uri uri = Uri.parse(uriString);

    Intent callIntent = new Intent(Intent.ACTION_CALL, uri);
    activity.startActivity(callIntent);

  }

  private void requestCallPermission() {
    activity.requestPermissions(new String[] { Manifest.permission.CALL_PHONE }, REQUEST_CALL_PHONE_PERMISSION);
  }

  private void requestSMSPermission() {
    activity.requestPermissions(new String[] { Manifest.permission.SEND_SMS }, REQUEST_SMS_PERMISSION);
  }
}