package locidnet.com.marvarid.ui.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.resources.utils.Prefs
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.MainActivity
import java.util.*




class PushNotification :FirebaseMessagingService() {

   companion object {
   }
    override fun onMessageReceived(msg: RemoteMessage?) {
        super.onMessageReceived(msg)
        MainActivity.NOTIF_STATUS = MainActivity.NEED_UPDATE

        Prefs.Builder().setNotifCount(Prefs.Builder().getNotifCount(
                if(!msg!!.data.containsKey("type")) "-1" else msg.data.get("type")!!) + 1,
                if(!msg.data.containsKey("type")) "-1" else msg.data.get("type")!!)
        if(Prefs.Builder().isALlowNotif()){
            try{
                log.d("Push keldi: -> ${msg.data}")
                val intent =Intent(this,MainActivity::class.java)
                intent.putExtra("Push",1)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pending = PendingIntent.getActivity(this,0,intent,0)
                val base = NotificationCompat.Builder(this)

                base.setSmallIcon(getNotificationIcon(base))

                        .setContentIntent(pending)
                        .setTicker(Base.get.resources.getString(R.string.app_name))
                        .setContentText(msg.data.get("body"))
                        .setNumber(Prefs.Builder().getNotifCount(
                                if(!msg.data.containsKey("type")) "-1" else msg.data.get("type")!!))
                        .setContentTitle(msg.data.get("title"))
                        .setGroup("marvarid")
                        .setAutoCancel(true)



                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    base.setLargeIcon(BitmapFactory.decodeResource(Base.get.resources, R.mipmap.ic_launcher))

                }



                val m = (Date().time / 1000L % Integer.MAX_VALUE).toInt()
                val notificationManager = applicationContext
                        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


                base.getNotification().flags = base.getNotification().flags or Notification.FLAG_AUTO_CANCEL

                val builder = NotificationCompat.InboxStyle(base)
                        .setBigContentTitle(msg.data.get("body"))
                        .setSummaryText("+${Prefs.Builder().getNotifCount(
                                if(!msg.data.containsKey("type")) "-1" else msg.data.get("type")!!)}")
                        .build()
                notificationManager.notify(Base.get.resources.getString(R.string.app_name), if(!msg.data.containsKey("type")) 303 else msg.data.get("type")!!.toInt(), builder)
            }catch (e:Exception){
                log.d("push kelishda error $e")
            }
        }


        sendBroadcast(Intent(MainActivity.notificationTag))
    }


    private fun getNotificationIcon(builder: NotificationCompat.Builder): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            val color = 0xA40344
//            builder.color = color
            return R.mipmap.ic_launcher

        } else {
            return R.mipmap.ic_launcher
        }
    }
}