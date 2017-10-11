package locidnet.com.marvarid.rest

import org.json.JSONObject
import locidnet.com.marvarid.pattern.cryptDecorator.AppCrypt
import locidnet.com.marvarid.pattern.cryptDecorator.B64DecoderCryptDecorator
import locidnet.com.marvarid.pattern.cryptDecorator.B64EncoderCryptDecorator
import locidnet.com.marvarid.pattern.signatureDecorator.AppSign
import locidnet.com.marvarid.pattern.signatureDecorator.MD5SignDecorator
import locidnet.com.marvarid.pattern.signatureDecorator.UpperSignDecorator
import locidnet.com.marvarid.resources.utils.log


/**
 * Created by Michaelan on 6/15/2017.
 */
object Http {

    val PRMS             = "prms"
    val CMD              = "cmd"
    val BASE_URL         = "http://api.maydon.net/new/"

    object CMDS{

        val TELEFONNI_JONATISH         = "1"
        val SMSNI_JONATISH             = "2"
        val LOGIN_YOQLIGINI_TEKSHIRISH = "3"
        val ROYXATDAN_OTISH            = "4"
        val LOGIN_PAYTI                = "5"
        val FB_ORQALI_LOGIN            = "6"
        val VK_ORQALI_LOGIN            = "7"
        val FB_VA_VK_ORQALI_REG        = "8"
        val POST                       = "9"
        val MY_POSTS                   = "10"
        val CHANGE_AVATAR              = "00"
        val FOLLOW                     = "13"
        val UN_FOLLOW                  = "20"
        val USER_INFO                  = "15"
        val SEARCH_USER                = "17"
        val LIKE_BOSISH                = "11"
        val BLOCK_USER                 = "21"
        val FEED                       = "22"
        val GET_FOLLOWERS              = "14"
        val GET_FOLLOWING              = "16"
        val WRITE_COMMENT              = "23"
        val GET_COMMENT_LIST           = "24"
        val DELETE_POST                = "25"
        val CHANGE_POST                = "26"
        val CHANGE_USER_SETTINGS       = "27"
        val CHANGE_PHONE_NUMBER        = "28"
        val ACCEPT_CHANGE_PHONE        = "29"
        val CHANGE_MAIL                = "34"
        val ACCEPT_MAIL                = "35"
        val CHANGE_PASSWORD            = "30"
        val CLOSE_PROFIL               = "31"
        val ADD_SONG_TO_PLAYLIST       = "32"
        val GET_PLAYLIST               = "33"
        val SET_TOKEN_DATA             = "36"
        val GET_NOTIF_LIST             = "37"
        val GET_LAST_COMMENTS          = "38"
        val GET_FULL_POST              = "39"
        val ALLOW_DISMISS              = "19"
        val FORGOT_PHONE               = "42"
        val FORGOT_SMS                 = "43"
        val FORGOT_PASS                = "44"
        val GET_15_POSTS               = "45"
        val COMPLAINTS                 = "46"
        val REPORT_BUG                 = "47"

    }


    fun getRequestData(obj: JSONObject, cmd:String):String{

//        val crypt = MCrypt()
//        val prm = MCrypt.bytesToHex(MCrypt().encrypt(obj.toString()))

        val jsObj = JSONObject()
        val prm = B64EncoderCryptDecorator(AppCrypt(obj.toString())).getPrm()
        jsObj.put(PRMS, prm)
        jsObj.put(CMD,cmd)
        jsObj.put("lang","ru")
        jsObj.put("sign",UpperSignDecorator(
                MD5SignDecorator(
                        AppSign(prm))).getSignature())
        jsObj.put("time","${System.currentTimeMillis()}")
        log.d("ready data $jsObj")
        return jsObj.toString()
    }

    fun getResponseData(prm:String):String = B64DecoderCryptDecorator(AppCrypt(prm)).getPrm()



}