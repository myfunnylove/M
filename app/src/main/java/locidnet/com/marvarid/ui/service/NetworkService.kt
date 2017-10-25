package locidnet.com.marvarid.ui.service

import android.content.Intent
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.google.firebase.iid.FirebaseInstanceIdService
import locidnet.com.marvarid.resources.utils.log

/**
 * Created by myfunnylove on 25.10.2017.
 */
class NetworkService : JobService() {


    override fun onStartJob(jobParameters: JobParameters): Boolean {
        log.d("onStartJob")
        startService(Intent(this@NetworkService,DeleteToken::class.java))

        return false
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        log.d("onStartonStopJob")
        return true
    }

}