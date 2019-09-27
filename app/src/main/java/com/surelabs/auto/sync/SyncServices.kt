package com.surelabs.auto.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import java.util.*


//jangan lupa didaftarkan dulu ke manifest yaa
class SyncServices : Service() {
    private var interval = 1L
    private var mTimer: Timer? = null
    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mTimer = Timer()
        // bakal ngulang ngecek koneksi dalam waktu 3 detik
        mTimer?.scheduleAtFixedRate(ConnectionCheck(), 0L, interval.times(3000))

        /*
        * START_STICK, START_NOT_STICK
        *
        * https://stackoverflow.com/questions/9093271/start-sticky-and-start-not-sticky
        *
        * masih ada beberapa yang lainnya
        * */

        Toast.makeText(this@SyncServices, "Services Started", Toast.LENGTH_SHORT).show()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this@SyncServices, "Services Destroyed", Toast.LENGTH_SHORT).show()
    }

    /*
      fungsi untuk cek,, ada koneksi internet atau nggak. Di Android ada 2 tipe pengecekan
      yang pertama ngecek kalo konek ke jaringan tapi nggak ada koneksi internet
      yang kedua ngecek koneksi plus ngecek ada koneksi internet atau nggak
    *
    * */

    private fun isOnline(): Boolean? {
        try {
            //ini process nge-ping via background process
            val pingProcess = Runtime.getRuntime().exec("ping -c 1 www.google.com")
            val isConnected = pingProcess.waitFor()

            //kembaliannya 0 kalo dia connect
            return isConnected == 0
        } catch (e: Exception) {
            //cetak errornya
            e.printStackTrace()
        }

        //ini kalo dia nggak connect
        return false
    }

    inner class ConnectionCheck : TimerTask() {
        override fun run() {
            //variable buat nampung status pengecekan koneksi tadi
            val online = isOnline()
            if (online == true) {
                // yang ini kalo dia konek ke internet
                // contoh ini, dibakal update UI servicenya.
                // Analoginya gini, inget aplikasi BBM kan, ketika kita tarik notifikasi android
                // BBM bakal kasih tau status koneksi intenet kita, connected atau disconnected
                // nah contoh ini bakal update UI dari servicesnya


                /*
                * dikasus lain, menurut logic ku, dibagian ini dia bakal synchronized data lokal
                * ke server. Disini juga kayaknya terjadi proses update status sync untuk
                * masing-masing data yang ada di sqlite
                *
                * bisa di sesuaikan dengan kondisi dan proses bisnis
                * */


                // dikasih cabang dulu, soalnya pengaturan notifikasi di android Oreo 8.0+
                // atau API 26+ agak berbeda
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startMyOwnForeground("Connected Gaes")
                } else {
                    startForeground(
                        NOTIF_ID,
                        defaultNotification(this@SyncServices, "Connected Gaes")
                    )
                }
            } else {
                // yang ini kalo nggak connect
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startMyOwnForeground("Yah, disconnect Gaes")
                } else {
                    startForeground(
                        NOTIF_ID,
                        defaultNotification(this@SyncServices, "Yah, disconnect Gaes")
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground(title: String?) {
        // yang ini baru buat channel
        val NOTIFICATION_CHANNEL_ID = "com.surelabs.auto.syn"
        val channelName = "Network Monitoring Service"
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_MIN
        )
        // yang ini untuk LED notification
        notificationChannel.lightColor = Color.GREEN

        // yang ini munculin di lockscreen atau nggak
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManager.createNotificationChannel(notificationChannel)

        //yang ini kita buat notifikasinya
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(NOTIF_ID, notification)
    }


    // yang ini kumpulan dari function dan variable static
    companion object {
        const val NOTIF_ID = 101
        fun defaultNotification(context: Context, title: String): Notification? {
            val notification = Notification.Builder(context)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setAutoCancel(true)
                .build()

            return notification
        }
    }
}