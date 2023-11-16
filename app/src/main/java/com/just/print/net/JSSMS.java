package com.just.print.net;
import android.telephony.gsm.SmsManager;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;

public class JSSMS extends Activity {

    // ---Sends an SMS message to another device.---
    public void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        try {
            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
                    SENT), 0);

            PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(DELIVERED), 0);


            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
        }catch(Exception e){
            System.out.println("..........");
        }
    }
}