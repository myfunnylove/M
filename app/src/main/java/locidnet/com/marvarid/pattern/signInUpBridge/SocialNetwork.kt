package locidnet.com.marvarid.pattern.signInUpBridge

import android.content.Intent

/**
 * Created by Sarvar on 28.08.2017.
 */
interface SocialNetwork {

    fun register()

    fun login()
    fun result(requestCode: Int, resultCode: Int, data: Intent?):Boolean

    fun cancel()
}