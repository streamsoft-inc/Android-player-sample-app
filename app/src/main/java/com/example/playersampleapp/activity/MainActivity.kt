package com.example.playersampleapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.playersampleapp.R
import com.example.playersampleapp.server.ConnectController
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private val connectController : ConnectController by inject()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val playButton: Button = findViewById(R.id.playButton)
        val ip:TextView = findViewById(R.id.ip_value)
        val portValue:TextView = findViewById(R.id.port_value)

        connectController.start(this)

        connectController.serverAddress.observe(this) { (host, port) ->
            Toast.makeText(this, "Connected to $host:$port", Toast.LENGTH_LONG).show()
            ip.text = host
            portValue.text = port
        }

        playButton.setOnClickListener {
            val videoUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"


            val intent = Intent(this@MainActivity, FullScreenActivity::class.java)
            intent.putExtra("VIDEO_URL", videoUrl)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectController.stop()
    }
}