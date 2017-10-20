package locidnet.com.marvarid.ui.activity

import android.content.Intent
import android.view.View
import android.widget.Toast
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.mvp.Viewer
import kotlinx.android.synthetic.main.activity_sign.*
import org.json.JSONObject
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.di.DaggerMVPComponent
import locidnet.com.marvarid.di.modules.ErrorConnModule
import locidnet.com.marvarid.di.modules.MVPModule
import locidnet.com.marvarid.di.modules.PresenterModule
import locidnet.com.marvarid.model.User
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.mvp.Presenter
import locidnet.com.marvarid.pattern.builder.ErrorConnection
import locidnet.com.marvarid.resources.utils.*
import javax.inject.Inject

class SignActivity : BaseActivity() ,Viewer{


    var signMode   = -1
    val PHONE_MODE = 77
    val MAIL_MODE  = 129
    val SMS_MODE   = 3

    @Inject
    lateinit var presenter:Presenter
    @Inject
    lateinit var errorConn: ErrorConnection

    var phoneStr:String      = ""
    var smsStr:  String      = ""

    override fun initProgress() {

        progressLay.visibility = View.VISIBLE
        disableAllElements()
    }



    override fun showProgress() {

    }


    override fun hideProgress() {
        progressLay.visibility = View.GONE
        enableAllElements()
    }

    override fun onSuccess(from:String,result: String) {

        if(from == Http.CMDS.TELEFONNI_JONATISH){

            val testUser = Prefs.Builder().getUser()
            testUser.phoneOrMail
            selectMail.isEnabled  = false
            selectPhone.isEnabled = false
            smsCode.visibility = View.VISIBLE
            sendAgain.visibility = View.VISIBLE
            sendAgain.setOnClickListener {
                if(signMode == PHONE_MODE){
                    if (phone.isValid){
                        val sendObject = JSONObject()
                        phoneStr = phone.phoneNumber!!
                        sendObject.put("phone",phoneStr)

                        presenter.requestAndResponse(sendObject, Http.CMDS.TELEFONNI_JONATISH)
                    }else
                        Toaster.errror(R.string.error_incorrect_phone)
                }else{
                    if (!Const.VALID_EMAIL_ADDRESS_REGEX.matcher(mail.text.toString()).find()){

                        mail.error = resources.getString(R.string.error_incorrect_mail)
                    }else{
                        val sendObject = JSONObject()
                        phoneStr = mail.text.toString()
                        sendObject.put("phone",phoneStr)

                        presenter.requestAndResponse(sendObject, Http.CMDS.TELEFONNI_JONATISH)
                    }
                }
            }
            signMode = SMS_MODE
            signUp.text = resources.getString(R.string.Sign_up)

        }else if(from == Http.CMDS.SMSNI_JONATISH){

            phone.isEnabled    = false
            mail.isEnabled     = false
            selectMail.isEnabled  = false
            selectPhone.isEnabled = false
            val phone = if(Const.ONLY_DIGITS.matcher(phoneStr).find()) phoneStr else ""
            val mail  = if(Const.VALID_EMAIL_ADDRESS_REGEX.matcher(phoneStr).find()) phoneStr else ""

            val user = User("","","","","","N",phoneStr,smsStr,"","","",signMode,
                    phone,mail)

            Base.get.prefs.setUser(user)
            startActivity(Intent(this,LoginAndPassActivity().javaClass))
            this.finish()
        }

    }



    override fun onFailure(from: String, message: String, erroCode: String) {

        Toaster.errror(message)


    }


    override fun initView() {
        Const.TAG = "SignActivity"

        DaggerMVPComponent
                .builder()
                .mVPModule(MVPModule(this, Model(),this))
                .presenterModule(PresenterModule())
                .errorConnModule(ErrorConnModule(this,false))
                .build()
                .inject(this)
        signMode = PHONE_MODE
        signUp.text = resources.getString(R.string.get_sms)

        phone.setDefaultCountry("uz")
        selectPhone.setOnClickListener {
            signMode = PHONE_MODE


            selectPhone.setBackgroundDrawable(resources.getDrawable(R.drawable.sign_edittext_block_top_enabled))
            selectMail.setBackgroundDrawable(resources.getDrawable(R.drawable.sign_edittext_block_top_disabled))
            phone.visibility = View.VISIBLE
            mail.visibility = View.GONE
        }

        selectMail.setOnClickListener {
            signMode = MAIL_MODE
            selectPhone.setBackgroundDrawable(resources.getDrawable(R.drawable.sign_edittext_block_top_disabled))
            selectMail.setBackgroundDrawable(resources.getDrawable(R.drawable.sign_edittext_block_top_enabled))

            phone.visibility = View.GONE
            mail.visibility = View.VISIBLE
        }

        signUp.setOnClickListener{




            errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                override fun connected() {

                    if(signMode == PHONE_MODE){


                        if (phone.isValid){
                            val sendObject = JSONObject()
                            phoneStr = phone.phoneNumber!!
                            sendObject.put("phone",phoneStr)

                            presenter.requestAndResponse(sendObject, Http.CMDS.TELEFONNI_JONATISH)
                        }else
                           Toaster.errror(R.string.error_incorrect_phone)


                    }else if(signMode == SMS_MODE){

                        if (smsCode.text.toString().length != 6){
                            smsCode.error = resources.getString(R.string.sms_code_error)
                        }else{
                            val sendObject = JSONObject()

                            smsStr = smsCode.text.toString()

                            sendObject.put("phone",phoneStr)
                            sendObject.put("sms",smsStr)


                            presenter.requestAndResponse(sendObject, Http.CMDS.SMSNI_JONATISH)
                        }
                    }else{
                        if (!Const.VALID_EMAIL_ADDRESS_REGEX.matcher(mail.text.toString()).find()){

                            mail.error = resources.getString(R.string.error_incorrect_mail)
                        }else{
                            val sendObject = JSONObject()
                            phoneStr = mail.text.toString()
                            sendObject.put("phone",phoneStr)

                            presenter.requestAndResponse(sendObject, Http.CMDS.TELEFONNI_JONATISH)
                        }
                    }


                }

                override fun disconnected() {
                    Toast.makeText(this@SignActivity,resources.getString(R.string.internet_conn_error), Toast.LENGTH_SHORT).show()

                }

            })

        }


    }



    override fun getLayout(): Int = R.layout.activity_sign

    private fun disableAllElements() {

        phone.isEnabled = false
//        phone.error = ""

        smsCode.isEnabled = false
//        smsCode.error = ""

        mail.isEnabled = false
//        mail.error = ""

        selectPhone.isEnabled = false
        selectMail.isEnabled = false
        signUp.isEnabled = false
    }
    private fun enableAllElements() {

        phone.isEnabled = true
//        phone.error = ""
        smsCode.isEnabled = true
//        smsCode.error = ""

        mail.isEnabled = true
//        mail.error = ""
        selectPhone.isEnabled = true
        selectMail.isEnabled = true
        signUp.isEnabled = true

    }
    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this,LoginActivity().javaClass))
        this.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Const.SESSION_OUT || resultCode == Const.SESSION_OUT){
            setResult(Const.SESSION_OUT)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.ondestroy()

    }
}