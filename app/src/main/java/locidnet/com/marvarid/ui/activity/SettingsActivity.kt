package locidnet.com.marvarid.ui.activity

import android.content.Context
import android.content.Intent
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_settings.*
import locidnet.com.marvarid.BuildConfig
import org.json.JSONObject
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.di.DaggerMVPComponent
import locidnet.com.marvarid.di.modules.ErrorConnModule
import locidnet.com.marvarid.di.modules.MVPModule
import locidnet.com.marvarid.di.modules.PresenterModule
import locidnet.com.marvarid.model.DialogFragmentModel
import locidnet.com.marvarid.model.ResponseData
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.mvp.Presenter
import locidnet.com.marvarid.mvp.Viewer
import locidnet.com.marvarid.pattern.builder.ErrorConnection
import locidnet.com.marvarid.pattern.signInUpBridge.SimpleoAuth
import locidnet.com.marvarid.resources.utils.*
import locidnet.com.marvarid.ui.dialogs.ChangePassFragment
import locidnet.com.marvarid.ui.dialogs.MailFormFragment
import locidnet.com.marvarid.ui.dialogs.PhoneFormFragment
import locidnet.com.marvarid.ui.dialogs.YesNoFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern
import javax.inject.Inject


class SettingsActivity : BaseActivity(), Viewer {


    val userData = Base.get.prefs.getUser()
    val sex = listOf(Base.get.resources.getString(R.string.unknown), Base.get.resources.getString(R.string.male), Base.get.resources.getString(R.string.female))
    var changed = false
    val map = hashMapOf(0 to "N", 1 to "F", 2 to "M")
    val model = Model()
    var changePhoneDialog: PhoneFormFragment? = null
    var changeMailDialog: MailFormFragment? = null
    var changePassDialog: ChangePassFragment? = null
    var isLoginFree = false

    @Inject
    lateinit var presenter: Presenter

    @Inject
    lateinit var errorConn: ErrorConnection

    override fun getLayout(): Int = R.layout.activity_settings

    override fun initView() {
        log.d("userdata: $userData")
        DaggerMVPComponent
                .builder()
                .mVPModule(MVPModule(this, Model(), this))
                .presenterModule(PresenterModule())
                .errorConnModule(ErrorConnModule(this, false))
                .build()
                .inject(this)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = resources.getString(R.string.settings)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {

            onBackPressed()

        }

        /*FIRST LAST NAME AND USERNAME*/
        name.setText("${userData.first_name} ${userData.last_name}")
        name.addTextChangedListener(object :TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (username.text.toString() == userData.userName)
                    isLoginFree = true


                changed = true
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })
        username.setText(userData.userName)
        username.addTextChangedListener(textwatcher)
        /*FIRST LAST NAME AND USERNAME*/

        /*PHONE AND MAIL*/
        phone.text = if (!userData.userPhone.isNullOrEmpty()) userData.userPhone else resources.getString(R.string.addPhone)
        mail.text = if (!userData.userMail.isNullOrEmpty()) userData.userMail else resources.getString(R.string.addMail)
        phone.setOnClickListener {
            changePhoneDialog = PhoneFormFragment.instance()
            changePhoneDialog!!.setDialogClickListener(object : PhoneFormFragment.DialogClickListener {
                override fun click(whichButton: Int) {
                    log.d("$whichButton")
                    if (whichButton == PhoneFormFragment.GET_SMS) {

                        if (!changePhoneDialog!!.phone.isValid) {

                            Toaster.errror(resources.getString(R.string.error_incorrect_phone))


                        } else {

                            val js = JS.get()
                            val phoneStr = changePhoneDialog!!.phone.phoneNumber
                            js.put("phone", phoneStr)
                            changePhoneDialog!!.setVisibility(true)

                            presenter.requestAndResponse(js, Http.CMDS.CHANGE_PHONE_NUMBER)
                        }


                    } else {
                        if (changePhoneDialog!!.smsCode.text.toString().trim().length == 6) {

                            val js = JS.get()
                            val phoneStr = changePhoneDialog!!.phone.phoneNumber
                            js.put("phone", phoneStr)
                            js.put("code", changePhoneDialog!!.smsCode.text.toString().trim())
                            changePhoneDialog!!.setVisibility(true)
                            presenter.requestAndResponse(js, Http.CMDS.ACCEPT_CHANGE_PHONE)


                        } else {
                            changePhoneDialog!!.smsCode.error = resources.getString(R.string.sms_code_error)

                        }

                    }
                }

            })

            changePhoneDialog!!.show(supportFragmentManager, YesNoFragment.TAG)
        }
        mail.setOnClickListener {


            changeMailDialog = MailFormFragment.instance()
            changeMailDialog!!.setDialogClickListener(object : MailFormFragment.DialogClickListener {
                override fun click(whichButton: Int) {

                    if (whichButton == PhoneFormFragment.GET_SMS) {

                        if (!Const.VALID_EMAIL_ADDRESS_REGEX.matcher(changeMailDialog!!.mail.text.toString()).find()) {
                            changeMailDialog!!.mail.error = resources.getString(R.string.error_incorrect_mail)
                        } else {
                            val js = JS.get()
                            val phoneStr = changeMailDialog!!.mail.text.toString()
                            js.put("mail", phoneStr)
                            changeMailDialog!!.setVisibility(true)

                            presenter.requestAndResponse(js, Http.CMDS.CHANGE_MAIL)
                        }


                    } else {

                        if (changeMailDialog!!.smsCode.text.toString().trim().length == 6) {

                            val js = JS.get()
                            js.put("mail", changeMailDialog!!.mail.text.toString())
                            js.put("code", changeMailDialog!!.smsCode.text.toString().trim())
                            changeMailDialog!!.setVisibility(true)
                            presenter.requestAndResponse(js, Http.CMDS.ACCEPT_MAIL)


                        } else {
                            changePhoneDialog!!.smsCode.error = resources.getString(R.string.sms_code_error)

                        }


                    }
                }

            })

            changeMailDialog!!.show(supportFragmentManager, YesNoFragment.TAG)
        }
        /*PHONE AND MAIL*/





        /*GENDER*/
        val genderAdapter = ArrayAdapter<String>(this, R.layout.white_textview, sex)
        genderAdapter.setDropDownViewResource(R.layout.white_textview_adapter)
        gender.adapter = genderAdapter
        gender.setSelection(when {
            userData.gender == "N" -> 0
            userData.gender == "F" -> 1
            else -> 2
        })
        /*GENDER*/
        switchCloseAccount.isChecked = Base.get.prefs.getUser().close == 1
        switchCloseAccount.setOnCheckedChangeListener { _, isChecked ->
            val js = JS.get()
            model.responseCall(Http.getRequestData(js, Http.CMDS.CLOSE_PROFIL))
                    .enqueue(object : Callback<ResponseData> {
                        override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {
                            log.d("close profil $response")
                            try {
                                if (response!!.body()!!.res == "0") {
                                    val user = Base.get.prefs.getUser()
                                    log.d("closed :${user.close}")

                                    user.close = if (user.close == 1) 0 else 1
                                    Base.get.prefs.setUser(user)
                                }
                            } catch (e: Exception) {
                                switchCloseAccount.isChecked = Base.get.prefs.getUser().close == 1
                            }

                        }

                        override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
                            log.d("close profil fail $t")
                            switchCloseAccount.isChecked = Base.get.prefs.getUser().close == 1

                        }

                    })
        }
        /*GENDER*/


        /*PASSWORD*/
//        if (userData.password.isNullOrEmpty()) {
//            password.text = resources.getString(R.string.addPassword)
//            password.tag = R.string.addPassword
//        } else {
            password.text = resources.getString(R.string.changePassword)
            password.tag = R.string.changePassword
//        }

        password.setOnClickListener {
            changePassDialog = ChangePassFragment.instance()
            changePassDialog!!.setDialogClickListener(object : ChangePassFragment.DialogClickListener{
                override fun click(whichButton: Int) {
                    changePassDialog!!.setVisibility(true)

                    val js = JS.get()
                    js.put("old_pass",changePassDialog!!.oldPass.text.toString())
                    js.put("new_pass",changePassDialog!!.newPass.text.toString())
                    presenter.requestAndResponse(js,Http.CMDS.CHANGE_PASSWORD)
                }

            })
            changePassDialog!!.show(supportFragmentManager,ChangePassFragment.TAG)
        }




        /*QUIT*/
        quitLay.setOnClickListener {
            val dialog = YesNoFragment.instance(
                    DialogFragmentModel(
                            Functions.getString(R.string.quitTitle),
                            Functions.getString(R.string.no),
                            Functions.getString(R.string.yes)
                    ))
            dialog.setDialogClickListener(object : YesNoFragment.DialogClickListener {
                override fun click(whichButton: Int) {

                    if (whichButton == YesNoFragment.NO) {
                        dialog.dismiss()
                    } else {
                        dialog.dismiss()

                        MainActivity.start = 0
                        MainActivity.end = 20
                        MainActivity.startFeed = 0
                        MainActivity.endFeed = 20
                        MainActivity.startFollowers = 0
                        MainActivity.endFollowers = 20
                        MainActivity.startFollowing = 0
                        MainActivity.endFollowing = 20
                        MainActivity.MY_POSTS_STATUS = MainActivity.NEED_UPDATE
                        MainActivity.FEED_STATUS = MainActivity.NEED_UPDATE
                        MainActivity.COMMENT_POST_UPDATE = 0
                        MainActivity.COMMENT_COUNT = 0
                        setResult(Const.QUIT)
                        finish()
//                        val sesion = SessionOut.Builder(this@SettingsActivity)
//                                .setErrorCode(96)
//                                .build()
//                        sesion.out()
                    }

                }

            })
            dialog.show(supportFragmentManager, YesNoFragment.TAG)

        }
        /*QUIT*/


        /*ALLOW NOTIFICATION*/
        switchNotification.isChecked = Base.get.prefs.isALlowNotif()
        switchNotification.setOnCheckedChangeListener { _, isChecked -> Base.get.prefs.allowNotif(isChecked) }
        /*ALLOW NOTIFICATION*/


        reportBugLay.setOnClickListener {
            startActivity(Intent(this,ReportBugActivity::class.java))
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out)

        }

        when(Prefs.Builder().imageRes()){
            Const.IMAGE.LOW -> lowImage.isChecked = true
            Const.IMAGE.MEDIUM -> mediumImage.isChecked = true
            Const.IMAGE.ORIGINAL -> highImage.isChecked = true
        }

        when(Prefs.Builder().audioRes()){
            Const.AUDIO.LOW -> lowAudio.isChecked = true
            Const.AUDIO.MEDIUM -> mediumAudio.isChecked = true
            Const.AUDIO.ORIGINAL -> highAudio.isChecked = true
        }
        imageResolution.setOnCheckedChangeListener { _, i ->

            when(i){
                R.id.lowImage -> Prefs.Builder().setImageRes(Const.IMAGE.LOW)
                R.id.mediumImage -> Prefs.Builder().setImageRes(Const.IMAGE.MEDIUM)
                R.id.highImage -> Prefs.Builder().setImageRes(Const.IMAGE.ORIGINAL)
            }
        }

        audioResolution.setOnCheckedChangeListener { _, i ->
            when(i){
                R.id.lowAudio -> Prefs.Builder().setAudioRes(Const.AUDIO.LOW)
                R.id.mediumAudio -> Prefs.Builder().setAudioRes(Const.AUDIO.MEDIUM)
                R.id.highAudio -> Prefs.Builder().setAudioRes(Const.AUDIO.ORIGINAL)
            }
        }

        version.text = "${resources.getString(R.string.app_name)} ${BuildConfig.VERSION_NAME}"

        aboutApp.setOnClickListener { startActivity(Intent(this,AboutUsActivity::class.java)) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Const.SESSION_OUT || resultCode == Const.SESSION_OUT) {
            setResult(Const.SESSION_OUT)
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_save, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {


        if (changed || map[gender.selectedItemPosition] != Base.get.prefs.getUser().gender) {

            if (username.text.toString().trim() == userData.userName) isLoginFree = true
            errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener {
                override fun connected() {
                    log.d("connected")

                    val pattern = Pattern.compile(SimpleoAuth.REGEXP.loginAndPasswordRegExp)

                    if (username.text.toString().isEmpty()  || username.text.toString().length < 6) {
                        onFailure("1", resources.getString(R.string.username_field_less_5))
                    } else if(!isLoginFree){
                        onFailure("1", resources.getString(R.string.username_not_free_error))

                    }
                    else if (!pattern.matcher(username.text.toString()).matches()){
                        onFailure("1", resources.getString(R.string.username_error))

                    }
                    else {
                        send()


                    }


                }

                override fun disconnected() {
                    log.d("disconnected")

                    Toast.makeText(this@SettingsActivity, resources.getString(R.string.internet_conn_error), Toast.LENGTH_SHORT).show()
                }

            })

        }
        return true
    }


    fun send() {
        val jsObject = JS.get()
        jsObject.put("username", username.text.toString().trimEnd().trimStart())
        jsObject.put("name", name.text.trim().toString().trimEnd().trimStart())
        jsObject.put("gender", map[gender.selectedItemPosition])


        presenter.requestAndResponse(jsObject, Http.CMDS.CHANGE_USER_SETTINGS)
    }

    override fun initProgress() {

    }

    override fun showProgress() {
    }

    override fun hideProgress() {
    }

    override fun onSuccess(from: String, result: String) {


        when (from) {
            Http.CMDS.CHANGE_PHONE_NUMBER -> {
                val response = JSONObject(result)
                log.d("from change phone number -> $response")
                changePhoneDialog!!.setVisibility(false)
                changePhoneDialog!!.setSms("")
            }
            Http.CMDS.ACCEPT_CHANGE_PHONE -> {
                changePhoneDialog!!.setVisibility(false)
                userData.userPhone = changePhoneDialog!!.phone.phoneNumber
                Prefs.Builder().setUser(userData)
                phone.text = Prefs.Builder().getUser().userPhone
                changePhoneDialog!!.dismiss()
            }

            Http.CMDS.CHANGE_MAIL -> {
                val response = JSONObject(result)
                log.d("from change phone number -> $response")
                changeMailDialog!!.setVisibility(false)
                changeMailDialog!!.setSms("")

            }
            Http.CMDS.ACCEPT_MAIL -> {
                changeMailDialog!!.setVisibility(false)
                userData.userMail = changeMailDialog!!.mail.text.toString()
                Prefs.Builder().setUser(userData)
                mail.text = Prefs.Builder().getUser().userMail
                changeMailDialog!!.dismiss()

            }

            Http.CMDS.CHANGE_PASSWORD ->{
                changePassDialog!!.setVisibility(false)

                changePassDialog!!.reset()
                changePassDialog!!.dismiss()
                Toast.makeText(Base.get.context, Base.get.context.resources.getString(R.string.changed), Toast.LENGTH_SHORT).show()

            }

            Http.CMDS.CHANGE_USER_SETTINGS -> {
                val user = Base.get.prefs.getUser()
                user.first_name = name.text.toString().trimEnd().trimStart()
                user.gender = map[gender.selectedItemPosition]!!
                user.userName = username.text.toString().trimEnd().trimStart()

                Base.get.prefs.setUser(user)
                MainActivity.MY_POSTS_STATUS = MainActivity.NEED_UPDATE
                MainActivity.FEED_STATUS = MainActivity.NEED_UPDATE
                Toast.makeText(Base.get.context, Base.get.context.resources.getString(R.string.saved), Toast.LENGTH_SHORT).show()
            }
            Http.CMDS.LOGIN_YOQLIGINI_TEKSHIRISH -> {
                isLoginFree = true
                username.setLoginResult(R.drawable.check_circle_outline)
            }

        }


    }

    override fun onFailure(from: String, message: String, erroCode: String) {
        if (from == Http.CMDS.CHANGE_PHONE_NUMBER) {
            changePhoneDialog!!.dismiss()
        }

        when (from) {

            Http.CMDS.CHANGE_PASSWORD -> {

                changePassDialog!!.setVisibility(false)
                Toaster.errror(message)

            }

            Http.CMDS.CHANGE_PHONE_NUMBER -> {
                changePhoneDialog!!.setVisibility(false)
                Toaster.errror(message)

            }
            Http.CMDS.ACCEPT_CHANGE_PHONE -> {
                changePhoneDialog!!.setVisibility(false)

                Toaster.errror(message)

            }
            Http.CMDS.CHANGE_MAIL -> {
                changeMailDialog!!.setVisibility(false)
                Toaster.errror(message)

            }
            Http.CMDS.ACCEPT_MAIL -> {
                changeMailDialog!!.setVisibility(false)
                Toaster.errror(message)

            }
            Http.CMDS.CHANGE_USER_SETTINGS -> {
                changePhoneDialog!!.setVisibility(false)
                Toaster.errror(message)

            }

            Http.CMDS.LOGIN_YOQLIGINI_TEKSHIRISH -> {
                if (username.text.toString() != userData.userName){
                    isLoginFree = false
                    username.setLoginResult(R.drawable.close_circle_outline)
                }else{
                    isLoginFree = true
                    username.setLoginResult()

                }
            }
            else -> {
                Toaster.errror(message)

            }
        }

    }


    val textwatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

            changed = true
            if(s!!.toString().length >= 6){
                if(s.toString() != userData.userName) {
                    log.d("login - $s username ${userData.userName}")
                    presenter.filterLogin(username)
                }else{
                    log.d("login + $s username ${userData.userName}")
                    username.setLoginResult()

                    isLoginFree = true
                }
            }
            else{
                username.setLoginResult()
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

    }

    override fun onStop() {
        changed = false
        super.onStop()
    }


    override fun onBackPressed() {
        username.hideKeyboard()
        name.hideKeyboard()

        Functions.hideSoftKeyboard(this)

        super.onBackPressed()
    }

    fun View.showKeyboard() {
        this.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    fun View.hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }
    fun AppCompatEditText.setLoginResult(drawable:Int = 0){
        if(drawable != 0){
            val drawableCompat = VectorDrawableCompat.create(resources,drawable,this.context.theme)
            this.setCompoundDrawablesWithIntrinsicBounds(null,null,drawableCompat,null)
        }else{
            this.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null)

        }
    }


}