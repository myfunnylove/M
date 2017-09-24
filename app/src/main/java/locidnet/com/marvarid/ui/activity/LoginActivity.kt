package locidnet.com.marvarid.ui.activity

import android.content.Intent
import android.os.Handler
import android.view.View
import com.facebook.CallbackManager
import com.vk.sdk.util.VKUtil
import kotlinx.android.synthetic.main.activity_login.*
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.di.DaggerMVPComponent
import locidnet.com.marvarid.di.modules.ErrorConnModule
import locidnet.com.marvarid.di.modules.MVPModule
import locidnet.com.marvarid.di.modules.PresenterModule
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.mvp.Presenter
import locidnet.com.marvarid.mvp.Viewer
import locidnet.com.marvarid.pattern.builder.ErrorConnection
import locidnet.com.marvarid.pattern.signInUpBridge.*
import locidnet.com.marvarid.resources.utils.*
import locidnet.com.marvarid.rest.Http
import org.json.JSONObject
import javax.inject.Inject


class LoginActivity : BaseActivity(), Viewer {


    //var fbCallbackManager: CallbackManager? = null
    var username = ""
    var password = ""

    lateinit var facebookoAuth:FacebookoAuth
    lateinit var vkAuth:VKoAuth
    lateinit var simpleAuth:SimpleoAuth
    lateinit var signBridge:SignBridgeConnector
    @Inject
    lateinit var presenter:Presenter
    @Inject
    lateinit var errorConn: ErrorConnection


    companion object {
        val FACEBOOK = 64206
        val VKONTAKTE = 10485
    }


    /*
    *
    *
    * PERMISSION CHECK
    *
    *
    * */


    override fun getLayout(): Int {
        return R.layout.activity_login
    }

    override fun initView() {
        Const.TAG = "LoginACtivity"
        log.d("FINGERPRINT: ${VKUtil.getCertificateFingerprint(this, this.getPackageName()).get(0)}")
        if (Base.get.prefs.getUser().session == "") {

            DaggerMVPComponent
                    .builder()
                    .mVPModule(MVPModule(this, Model(),this))
                    .presenterModule(PresenterModule())
                    .errorConnModule(ErrorConnModule(this,false))
                    .build()
                    .inject(this)


            Functions.checkPermissions(this)


            //FACEBOOK OAUTH2 BUILDER INITIALIZE
            facebookoAuth = FacebookoAuth.Builder(CallbackManager.Factory.create(),this,fb)
                                         .build()

            //VKONTAKTE OAUTH2 BUILDER INITIALIZE
            vkAuth = VKoAuth.Builder(this, vk)
                            .build()

            //SIMPLE AUTH BUILDER INITIALIZE
            simpleAuth = SimpleoAuth.Builder(this,sm,login,pass)
                                    .build()

            logIn.setOnClickListener {

                simpleAuth.authorizeWithThisBridge()

            }


            loginVk.setOnClickListener {


                vkAuth.authorizeWithThisBridge()

            }


            loginFb.setOnClickListener {

                facebookoAuth.authorizeWithThisBridge()

            }

            signUp.setOnClickListener {
                startActivity(Intent(this, SignActivity().javaClass))
                this.finish()
            }
        } else {
            startActivity(Intent(this, MainActivity().javaClass))
            this.finish()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Const.SESSION_OUT || resultCode == Const.SESSION_OUT){
            setResult(Const.SESSION_OUT)
            finish()
        }
        if (requestCode == VKONTAKTE) { /* INTEGRATE VIA VKONTAKTE */

            if (!signBridge.getResult(requestCode,resultCode,data)) {
                super.onActivityResult(requestCode, resultCode, data)
            }
        } else if (requestCode == FACEBOOK) { /* INTEGRATE VIA FACEBOOK */

            signBridge.getResult(requestCode,resultCode,data)
        }
    }

    /*
    *
    *  VKONTAKTE CALLBACK
    *
    * */



    private fun goNextActivity(from: Int) {
        val intent = Intent(this, LoginAndPassActivity::class.java)
        intent.putExtra("from", from)
        startActivityForResult(intent,Const.TO_FAIL)
        this.finish()
    }


    override fun initProgress() {
        progressLay.visibility = View.VISIBLE
        errorText.text = ""

        disableAllElements()
    }

    override fun showProgress() {
    }

    override fun hideProgress() {
        progressLay.visibility = View.GONE
        enableAllElements()
    }

    override fun onSuccess(from: String, result: String) {

        if(from  == Http.CMDS.LOGIN_PAYTI) {
            val user = Prefs.Builder().getUser()
            user.password = password
            user.userName = username
        Prefs.Builder().setUser(user)
        }
//        Toast.makeText(this,":OK",Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity().javaClass))
        this.finish()
//        }else if (from == Http.CMDS.FB_ORQALI_LOGIN){
//
//        }
    }

    override fun onFailure(from: String, message: String, erroCode: String) {


        when (from) {

            Http.CMDS.FB_ORQALI_LOGIN -> goNextActivity(FACEBOOK)
            Http.CMDS.VK_ORQALI_LOGIN -> goNextActivity(VKONTAKTE)

            "96" -> {

            }

            else -> {
                Toaster.errror(message)

//                errorText.text = message
//
//                Handler().postDelayed({
//                    errorText.text = ""
//                }, 3000)
            }
        }

    }

    override fun onStop() {
        super.onStop()
        try{
            signBridge.onDestroy()
        }catch (e:Exception){}
    }

    private fun disableAllElements() {


        loginVk.isEnabled       = false
        loginFb.isEnabled       = false
        signUp.isEnabled        = false

    }

    private fun enableAllElements() {


        signUp.isEnabled = true
        loginVk.isEnabled = true
        loginFb.isEnabled = true
    }


    /*
    *
    *
    *   LISTENERS
    *
    *
    * */

    val fb = object : AuthorizeConnector{
        override fun onSuccess(idUser: String, token: String) {

            val sendObj = JSONObject()
            sendObj.put("fb_id", idUser)
            sendObj.put("token", token)

            presenter.requestAndResponse(sendObj, Http.CMDS.FB_ORQALI_LOGIN)
        }


        override fun onFailure(message: String) {

            if(message.isEmpty()) onFailure(Http.CMDS.VK_ORQALI_LOGIN, resources.getString(R.string.error_no_type))
            else onFailure(Http.CMDS.FB_ORQALI_LOGIN, message)

        }

    }

    val vk = object : AuthorizeConnector{

        override fun onSuccess(idUser: String, token: String) {
            val sendObj = JSONObject()
            sendObj.put("vk_id",idUser)
            sendObj.put("token", token)

            presenter.requestAndResponse(sendObj, Http.CMDS.VK_ORQALI_LOGIN)
        }

        override fun onFailure(message: String) {
            if(message.isEmpty()) onFailure(Http.CMDS.VK_ORQALI_LOGIN, resources.getString(R.string.error_no_type))
            else onFailure(Http.CMDS.VK_ORQALI_LOGIN, message)

        }



    }

    val sm = object :AuthorizeConnector{
        override fun onSuccess(idUser: String, token: String) {
            val obj = JSONObject()
            obj.put("username", idUser)
            obj.put("password", token)
            username = idUser
            password = token

            presenter.requestAndResponse(obj, Http.CMDS.LOGIN_PAYTI)
        }

        override fun onFailure(message: String) {
            Toaster.errror(message)

        }

    }

    fun SocialNetwork.authorizeWithThisBridge(){
        errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
            override fun connected() {
                signBridge = SignBridge(this@authorizeWithThisBridge)
                signBridge.initialize().tryAuthorize()
            }

            override fun disconnected() {
                Toaster.errror(resources.getString(R.string.internet_conn_error))

            }

        })

    }

}


