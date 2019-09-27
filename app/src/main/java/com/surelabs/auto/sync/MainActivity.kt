package com.surelabs.auto.sync

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    /*jangan lupa, ada beberapa permission yang ditambahkan di AndroidManifest.xml ya*/

    var isRunning = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //lansung panggil ID-nya aja

        startServices.setOnClickListener {
            if (isRunning) {
                ContextCompat.startForegroundService(
                    this@MainActivity,
                    Intent(this@MainActivity, SyncServices::class.java)
                )
                isRunning = false
            } else {
                stopService(Intent(this@MainActivity, SyncServices::class.java))
                isRunning = true
            }
        }

        /*
        * kalo mau langsung auto start
        * panggil aja
        * ContextCompat.startForegroundService(
                this@MainActivity,
                Intent(this@MainActivity, SyncServices::class.java)
            )
        * didalam onCreate() atau onStart()
        * */
    }
}
