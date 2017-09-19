package locidnet.com.marvarid.ui.service

import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v7.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.resources.utils.Prefs
import locidnet.com.marvarid.resources.utils.log
import java.util.*

/**
 * Created by Sarvar on 18.09.2017.
 */
class PushNotification :FirebaseMessagingService() {


    override fun onMessageReceived(msg: RemoteMessage?) {
        super.onMessageReceived(msg)
        if(Prefs.Builder().isALlowNotif()){
            try{

                log.d("Push keldi: -> ${msg!!.data}")

                val builder = NotificationCompat.Builder(this)
                builder.setSmallIcon(getNotificationIcon(builder))
                        .setTicker(Base.get.resources.getString(R.string.app_name))
                        .setContentText(msg.data.get("body"))
                        .setContentTitle(msg.data.get("title"))
                        .setGroup("marvarid")



                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    builder.setLargeIcon(BitmapFactory.decodeResource(Base.get.resources, R.mipmap.ic_launcher))

                }



                val m = (Date().time / 1000L % Integer.MAX_VALUE).toInt()
                val notificationManager = applicationContext
                        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager



                notificationManager.notify(Base.get.resources.getString(R.string.app_name),m, builder.build())
            }catch (e:Exception){
                log.d("push kelishda error $e")
            }
        }
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