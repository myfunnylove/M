package locidnet.com.marvarid.ui.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v7.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.resources.utils.Prefs
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.MainActivity
import java.util.*




class PushNotification :FirebaseMessagingService() {


    override fun onMessageReceived(msg: RemoteMessage?) {
        super.onMessageReceived(msg)
        MainActivity.NOTIF_STATUS = MainActivity.NEED_UPDATE

        if(Prefs.Builder().isALlowNotif()){
            try{
                log.d("Push keldi: -> ${msg!!.data}")
                val intent =Intent(this,MainActivity::class.java)
                intent.putExtra("Push",1)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val builder = NotificationCompat.Builder(this)
                val pending = PendingIntent.getActivity(this,0,intent,0)

                builder.setSmallIcon(getNotificationIcon(builder))

                        .setContentIntent(pending)
                        .setTicker(Base.get.resources.getString(R.string.app_name))
                        .setContentText(msg.data.get("body"))
                        .setContentTitle(msg.data.get("title"))
                        .setGroup("marvarid")
                        .setAutoCancel(true)



                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    builder.setLargeIcon(BitmapFactory.decodeResource(Base.get.resources, R.mipmap.ic_launcher))

                }



                val m = (Date().time / 1000L % Integer.MAX_VALUE).toInt()
                val notificationManager = applicationContext
                        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


                builder.getNotification().flags = builder.getNotification().flags or Notification.FLAG_AUTO_CANCEL
                notificationManager.notify(Base.get.resources.getString(R.string.app_name),m, builder.build())
            }catch (e:Exception){
                log.d("push kelishda error $e")
            }
        }

        Prefs.Builder().setNotifCount(Prefs.Builder().getNotifCount() + 1)

        sendBroadcast(Intent(MainActivity.notificationTag))
    }


    private fun getNotificationIcon(builder: NotificationCompat.Builder): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val color = 0xA40344
            builder.color = color
            return R.mipmap.ic_launcher

        } else {
            return R.mipmap.ic_launcher
        }
    }
}