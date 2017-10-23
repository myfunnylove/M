package locidnet.com.marvarid.resources.utils

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.model.Info
import locidnet.com.marvarid.model.User
import locidnet.com.marvarid.model.UserData
import locidnet.com.marvarid.model.UserInfo

/**
 * Created by Michaelan on 6/18/2017.
 */
object Prefs {

    private var prefs:SharedPreferences? = null

    private val USER = "user"
    private val USER_INFO = "userInfo"

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

    fun getNotifCount(type:String = "-1"):Int{
        return prefs!!.getInt("countNotif$type", 0)

    }
    fun setNotifCount(count:Int,type:String = "-1") {
        synchronized(this@Prefs){
            val editor = prefs!!.edit()
            editor.putInt("countNotif$type", count)
            editor.commit()
        }
    }

    fun imageRes():String{

        return prefs!!.getString("imageRes",Const.IMAGE.MEDIUM)

    }

    fun setImageRes(res:String){
        val editor = prefs!!.edit()
        editor.putString("imageRes", res)
        editor.commit()
    }
    fun audioRes():String{

        return prefs!!.getString("audioRes",Const.AUDIO.MEDIUM)

    }

    fun setAudioRes(res:String){
        val editor = prefs!!.edit()
        editor.putString("audioRes", res)
        editor.commit()
    }

    fun setUserInfo(userInfo: UserInfo) {
        log.d("set user profile $userInfo")

        @SuppressLint("CommitPrefEdits")
        val writer = prefs!!.edit()
        writer.putString(USER_INFO,Gson().toJson(userInfo))
        writer.commit()
    }
    fun getUserInfo():UserInfo?{
        val user = prefs!!.getString(USER_INFO,"")
        log.d("get user profile $user")
        if (user != "")
            return Gson().fromJson(user,UserInfo::class.java)
        else
            return null
    }
}