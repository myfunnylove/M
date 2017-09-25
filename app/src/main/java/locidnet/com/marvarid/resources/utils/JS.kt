package locidnet.com.marvarid.resources.utils

import org.json.JSONObject

/**
 * Created by Sarvar on 25.09.2017.
 */
object JS {


    fun get():JSONObject{
        val user = Prefs.Builder().getUser()

        val js = JSONObject()
        js.put("user_id",user.userId)
        js.put("session",user.session)
        return js
    }
}