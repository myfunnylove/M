package locidnet.com.marvarid.resources.utils

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.model.User

/**
 * Created by Michaelan on 6/18/2017.
 */
object Prefs {

    private var prefs:SharedPreferences? = null

    private val USER = "user"

    fun Builder():Prefs{
        if (prefs == null) prefs = PreferenceManager.getDefaultSharedPreferences(Base.get)

        return Prefs
    }


    @SuppressLint("ApplySharedPref")
    fun setUser(user: User){
        log.d("set user profile $user")

        @SuppressLint("CommitPrefEdits")
        val writer = prefs!!.edit()
        writer.putString(USER,Gson().toJson(user))
        writer.commit()
    }

    fun getUser():User{
        val user = prefs!!.getString(USER,"")
        log.d("get user profile $user")
        if (user != "")
        return Gson().fromJson(user,User::class.java)
        else
            return User("","","","","","N","","","","","",-1,"","")
    }
    fun clearUser(){

        val user = User("","","","","","N","","","","","",-1,"","")
        setUser(user)
    }

    fun setTokenId(token: String) {
        val editor = prefs!!.edit()
        editor.putString("tokenId", token)
        editor.commit()
    }

    fun getTokenId(): String {
        return prefs!!.getString("tokenId", "")
    }

    fun allowNotif(allow : Boolean){
        val editor = prefs!!.edit()
        editor.putBoolean("allowNotification", allow)
        editor.commit()
    }

    fun isALlowNotif(): Boolean {
        return prefs!!.getBoolean("allowNotification", true)
    }

    fun getNotifCount():Int{
        return prefs!!.getInt("countNotif", 0)

    }
    fun setNotifCount(count:Int) {
        synchronized(this@Prefs){
            val editor = prefs!!.edit()
            editor.putInt("countNotif", count)
            editor.commit()
        }
    }

}