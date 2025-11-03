package com.example.smsforwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "SmsReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            for (message in messages) {
                val sender = message.displayOriginatingAddress ?: "未知"
                val content = message.messageBody ?: ""
                val timestamp = message.timestampMillis
                
                Log.d(TAG, "收到短信 - 发件人: $sender, 内容: $content")
                
                // 转发到服务
                val serviceIntent = Intent(context, SmsForwarderService::class.java).apply {
                    action = SmsForwarderService.ACTION_FORWARD_SMS
                    putExtra("sender", sender)
                    putExtra("content", content)
                    putExtra("timestamp", timestamp)
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}

