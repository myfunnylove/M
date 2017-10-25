package locidnet.com.marvarid.ui.service

import android.app.IntentService
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.firebase.jobdispatcher.Constraint
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.google.firebase.iid.FirebaseInstanceId
import locidnet.com.marvarid.resources.utils.Prefs
import locidnet.com.marvarid.resources.utils.log
import java.io.IOException

/**
 * Created by myfunnylove on 23.10.2017.
 */
class DeleteToken : IntentService("deleteToken") {
    override fun onHandleIntent(p0: Intent?) {
        Prefs.Builder().setNotifCount(0)
       try{
           FirebaseInstanceId.getInstance().deleteInstanceId()
           Prefs.Builder().setTokenId("")
           log.d("Logout token remove: ${Prefs.Builder().getTokenId()}")
           FirebaseInstanceId.getInstance().getToken()
           log.d("Logout token new token: ${FirebaseInstanceId.getInstance().getToken()}")
       }catch (e:IOException){
           log.d("Logout exception $e")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                val networkJob = applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                val dispatcher  = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))


                dispatcher.mustSchedule(
                         dispatcher.newJobBuilder()
                         .setTag("myjobservice-tag")
                        .setService(NetworkService::class.java)
                        .setConstraints(Constraint.ON_ANY_NETWORK).build())
            }


       }

        stopSelf()
    }
}