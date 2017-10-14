package locidnet.com.marvarid.ui.activity

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import locidnet.com.marvarid.R
import kotlinx.android.synthetic.main.activity_sign.*

import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.di.DaggerMVPComponent
import locidnet.com.marvarid.di.modules.ErrorConnModule
import locidnet.com.marvarid.di.modules.MVPModule
import locidnet.com.marvarid.di.modules.PresenterModule
import locidnet.com.marvarid.model.User
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.mvp.Presenter
import locidnet.com.marvarid.mvp.Viewer
import locidnet.com.marvarid.pattern.builder.ErrorConnection
import locidnet.com.marvarid.resources.utils.*
import locidnet.com.marvarid.rest.Http
import org.json.JSONObject
import javax.inject.Inject

class ForgotPasswordActivity : BaseActivity() , Viewer {


    var signMode   = -1
    private val PHONE_MODE = 77
    private val MAIL_MODE  = 129
    private val SMS_MODE   = 3

    @Inject
    lateinit var presenter: Presenter
    @Inject
    lateinit var errorConn: ErrorConnection

    var phoneStr:String?      = null
    var smsStr:  String?      = null
    var userId:  String ?     = null
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
        log.d("from $from result $result")
        if(from == Http.CMDS.FORGOT_PHONE){

            val js= JSONObject(result)
            userId = js.optString("user");
            selectMail.isEnabled  = false
            selectPhone.isEnabled = false
            smsCode.visibility = View.VISIBLE
            signMode = SMS_MODE
            signUp.text = resources.getString(R.string.send)

        }else if(from == Http.CMDS.FORGOT_SMS){

            phone.isEnabled    = false
            mail.isEnabled     = false
            selectMail.isEnabled  = false
            selectPhone.isEnabled = false
            val phone = if(Const.ONLY_DIGITS.matcher(phoneStr).find()) phoneStr else ""
            val mail  = if(Const.VALID_EMAIL_ADDRESS_REGEX.matcher(phoneStr).find()) phoneStr else ""

            val user = User(userId!!, "", "", "", "", "N", phoneStr!!, smsStr!!, "", "", "", signMode,
                    phone!!, mail!!)

            Prefs.setUser(user)
            val intent = Intent(this, NewPasswordActivity().javaClass)
            val js= JSONObject(result)
            intent.putExtra("userId",userId!!)
            intent.putExtra("token",js.optString("token"))

            startActivityForResult(intent,Const.FORGOT_PASS)
        }

    }



    override fun onFailure(from: String, message: String, erroCode: String) {

        Toaster.errror(message)


    }


    override fun initView() {
        Const.TAG = "ForgotPasswordActivity"

        DaggerMVPComponent.builder()
                .mVPModule(MVPModule(this, Model(), this))
                .presenterModule(PresenterModule())
                .errorConnModule(ErrorConnModule(this, false))
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

        smsCode.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if (smsCode.text.toString().length == 6){
                    val sendObject = JS.get()

                    smsStr = smsCode.text.toString()

                    sendObject.put("code",smsStr)
                    sendObject.put("user",userId)

                    presenter.requestAndResponse(sendObject, Http.CMDS.FORGOT_SMS)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
        signUp.setOnClickListener{




            errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener {
                override fun connected() {

                    if(signMode == PHONE_MODE){


                        if (phone.isValid){
                            val sendObject = JS.get()
                            phoneStr = phone.phoneNumber!!
                            sendObject.put("type","1")
                            sendObject.put("input",phoneStr)

                            presenter.requestAndResponse(sendObject, Http.CMDS.FORGOT_PHONE)
                        }else
                            Toaster.errror(R.string.error_incorrect_phone)


                    }else if(signMode == SMS_MODE){

                        if (smsCode.text.toString().length != 6){
                            smsCode.error = resources.getString(R.string.sms_code_error)
                        }else{
                            val sendObject = JS.get()


                            smsStr = smsCode.text.toString()
                            sendObject.put("type",if(signMode == PHONE_MODE) "1" else "2")
                            sendObject.put("input",phoneStr)
                            sendObject.put("code",smsStr)



                            presenter.requestAndResponse(sendObject, Http.CMDS.FORGOT_SMS)
                        }
                    }else{
                        if (!Const.VALID_EMAIL_ADDRESS_REGEX.matcher(mail.text.toString()).find()){

                            mail.error = resources.getString(R.string.error_incorrect_mail)
                        }else{
                            val sendObject = JS.get()

                            phoneStr = mail.text.toString()
                            sendObject.put("type","2")
                            sendObject.put("input",phoneStr)

                            presenter.requestAndResponse(sendObject, Http.CMDS.FORGOT_PHONE)
                        }
                    }


                }

                override fun disconnected() {
                    Toast.makeText(this@ForgotPasswordActivity, resources.getString(R.string.internet_conn_error), Toast.LENGTH_SHORT).show()

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
        startActivity(Intent(this, LoginActivity().javaClass))
        this.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Const.SESSION_OUT || resultCode == Const.SESSION_OUT){
            setResult(Const.SESSION_OUT)
            finish()
        }else if(requestCode == Const.FORGOT_PASS){
            finish()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
         phoneStr      = null
         smsStr      = null
         userId    = null
    }
}