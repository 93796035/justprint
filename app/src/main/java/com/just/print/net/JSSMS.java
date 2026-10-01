package com.just.print.net;
import android.telephony.gsm.SmsManager;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

public class JSSMS extends Activity {

    // ---Sends an SMS message to another device.---
    public void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        try {
            int pendingFlags = Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0;
            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
                    SENT), pendingFlags);

            PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(DELIVERED), pendingFlags);


            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
        }catch(Exception e){
            System.out.println("..........");
        }
    }
}
