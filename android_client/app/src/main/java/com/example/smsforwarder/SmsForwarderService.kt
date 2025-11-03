package com.example.smsforwarder

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.PrintWriter
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*

class SmsForwarderService : Service() {
    
    companion object {
        private const val TAG = "SmsForwarderService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "sms_forwarder_channel"
        
        const val ACTION_START = "action_start"
        const val ACTION_STOP = "action_stop"
        const val ACTION_FORWARD_SMS = "action_forward_sms"
    }
    
    private var serverIp: String = ""
    private var serverPort: Int = 0
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var isConnected = false
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                serverIp = intent.getStringExtra("server_ip") ?: ""
                serverPort = intent.getIntExtra("server_port", 0)
                startForeground(NOTIFICATION_ID, createNotification("正在连接..."))
                connectToServer()
            }
            ACTION_STOP -> {
                disconnect()
                stopSelf()
            }
            ACTION_FORWARD_SMS -> {
                val sender = intent.getStringExtra("sender") ?: ""
                val content = intent.getStringExtra("content") ?: ""
                val timestamp = intent.getLongExtra("timestamp", System.currentTimeMillis())
                forwardSms(sender, content, timestamp)
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "短信转发服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "短信转发到电脑的后台服务"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("短信转发服务")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification(text: String) {
        val notification = createNotification(text)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun connectToServer() {
        serviceScope.launch {
            try {
                Log.d(TAG, "正在连接服务器: $serverIp:$serverPort")
                
                socket = Socket(serverIp, serverPort)
                writer = PrintWriter(socket!!.getOutputStream(), true)
                isConnected = true
                
                withContext(Dispatchers.Main) {
                    updateNotification("已连接到 $serverIp:$serverPort")
                    updateStatus(true, "已连接到服务器")
                }
                
                Log.d(TAG, "连接成功")
                
            } catch (e: Exception) {
                Log.e(TAG, "连接失败: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    updateNotification("连接失败: ${e.message}")
                    updateStatus(false, "连接失败: ${e.message}")
                }
                isConnected = false
                
                // 尝试重连
                delay(5000)
                if (!isConnected) {
                    connectToServer()
                }
            }
        }
    }
    
    private fun forwardSms(sender: String, content: String, timestamp: Long) {
        if (!isConnected || writer == null) {
            Log.w(TAG, "未连接到服务器，无法转发短信")
            return
        }
        
        serviceScope.launch {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val timeStr = dateFormat.format(Date(timestamp))
                
                val json = JSONObject().apply {
                    put("sender", sender)
                    put("content", content)
                    put("timestamp", timeStr)
                }
                
                writer?.println(json.toString())
                
                Log.d(TAG, "短信已转发: $sender - $content")
                
                withContext(Dispatchers.Main) {
                    updateNotification("已转发短信来自: $sender")
                    updateLog("[$timeStr] $sender\n$content\n\n")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "转发短信失败: ${e.message}", e)
                isConnected = false
                // 尝试重连
                connectToServer()
            }
        }
    }
    
    private fun disconnect() {
        isConnected = false
        
        try {
            writer?.close()
            socket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "断开连接时出错: ${e.message}", e)
        }
        
        writer = null
        socket = null
        
        updateStatus(false, "已断开连接")
        serviceScope.cancel()
    }
    
    private fun updateStatus(isRunning: Boolean, message: String) {
        val prefs = getSharedPreferences("service_status", MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_running", isRunning)
            putString("status_message", message)
            apply()
        }
    }
    
    private fun updateLog(log: String) {
        val prefs = getSharedPreferences("service_status", MODE_PRIVATE)
        val existingLog = prefs.getString("log", "") ?: ""
        val newLog = log + existingLog
        
        // 只保留最近10条记录
        val lines = newLog.split("\n\n")
        val trimmedLog = lines.take(10).joinToString("\n\n")
        
        prefs.edit().apply {
            putString("log", trimmedLog)
            apply()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }
}

