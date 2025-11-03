package com.example.smsforwarder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    
    private lateinit var etServerIp: EditText
    private lateinit var etServerPort: EditText
    private lateinit var btnConnect: Button
    private lateinit var btnDisconnect: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvLog: TextView
    
    private val PERMISSION_REQUEST_CODE = 100
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        checkPermissions()
        loadSettings()
        updateUI()
    }
    
    private fun initViews() {
        etServerIp = findViewById(R.id.etServerIp)
        etServerPort = findViewById(R.id.etServerPort)
        btnConnect = findViewById(R.id.btnConnect)
        btnDisconnect = findViewById(R.id.btnDisconnect)
        tvStatus = findViewById(R.id.tvStatus)
        tvLog = findViewById(R.id.tvLog)
        
        btnConnect.setOnClickListener {
            connectToServer()
        }
        
        btnDisconnect.setOnClickListener {
            disconnectFromServer()
        }
    }
    
    private fun checkPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.INTERNET
        )
        
        // Android 13及以上需要通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        val needRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (needRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                needRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "需要短信和网络权限才能使用此应用", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun loadSettings() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        etServerIp.setText(prefs.getString("server_ip", "192.168.1.100"))
        etServerPort.setText(prefs.getString("server_port", "8888"))
    }
    
    private fun saveSettings() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        prefs.edit().apply {
            putString("server_ip", etServerIp.text.toString())
            putString("server_port", etServerPort.text.toString())
            apply()
        }
    }
    
    private fun connectToServer() {
        val ip = etServerIp.text.toString().trim()
        val portStr = etServerPort.text.toString().trim()
        
        if (ip.isEmpty()) {
            Toast.makeText(this, "请输入服务器IP地址", Toast.LENGTH_SHORT).show()
            return
        }
        
        val port = portStr.toIntOrNull()
        if (port == null || port !in 1..65535) {
            Toast.makeText(this, "请输入有效的端口号(1-65535)", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 保存设置
        saveSettings()
        
        // 启动服务
        val intent = Intent(this, SmsForwarderService::class.java).apply {
            action = SmsForwarderService.ACTION_START
            putExtra("server_ip", ip)
            putExtra("server_port", port)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        
        Toast.makeText(this, "正在连接服务器...", Toast.LENGTH_SHORT).show()
        updateUI()
    }
    
    private fun disconnectFromServer() {
        val intent = Intent(this, SmsForwarderService::class.java).apply {
            action = SmsForwarderService.ACTION_STOP
        }
        startService(intent)
        
        Toast.makeText(this, "已断开连接", Toast.LENGTH_SHORT).show()
        updateUI()
    }
    
    private fun updateUI() {
        val prefs = getSharedPreferences("service_status", MODE_PRIVATE)
        val isRunning = prefs.getBoolean("is_running", false)
        
        btnConnect.isEnabled = !isRunning
        btnDisconnect.isEnabled = isRunning
        etServerIp.isEnabled = !isRunning
        etServerPort.isEnabled = !isRunning
        
        tvStatus.text = if (isRunning) {
            "状态: 已连接"
        } else {
            "状态: 未连接"
        }
        
        // 显示日志
        val log = prefs.getString("log", "等待短信...")
        tvLog.text = log
    }
    
    override fun onResume() {
        super.onResume()
        updateUI()
    }
}

