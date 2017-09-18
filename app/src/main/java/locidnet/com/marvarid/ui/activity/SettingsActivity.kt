package locidnet.com.marvarid.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_settings.*
import org.json.JSONObject
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.di.DaggerMVPComponent
import locidnet.com.marvarid.di.modules.ErrorConnModule
import locidnet.com.marvarid.di.modules.MVPModule
import locidnet.com.marvarid.di.modules.PresenterModule
import locidnet.com.marvarid.model.ResponseData
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.mvp.Presenter
import locidnet.com.marvarid.mvp.Viewer
import locidnet.com.marvarid.pattern.builder.ErrorConnection
import locidnet.com.marvarid.pattern.builder.SessionOut
import locidnet.com.marvarid.pattern.signInUpBridge.SimpleoAuth
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Functions
import locidnet.com.marvarid.resources.utils.Prefs
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.fragment.MailFormFragment
import locidnet.com.marvarid.ui.fragment.PhoneFormFragment
import locidnet.com.marvarid.ui.fragment.YesNoFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Created by Sarvar on 10.08.2017.
 */
class SettingsActivity : BaseActivity() ,Viewer {


    val userData = Base.get.prefs.getUser()
    val sex = listOf(Base.get.resources.getString(R.string.unknown),Base.get.resources.getString(R.string.male),Base.get.resources.getString(R.string.female))
    var changed = false
    val map = hashMapOf(0 to "N", 1 to "F", 2 to "M")
    val model                 = Model()
    var changePhoneDialog: PhoneFormFragment? = null
    var changeMailDialog: MailFormFragment? = null
    @Inject
    lateinit var presenter:Presenter

    @Inject
    lateinit var errorConn: ErrorConnection

    override fun getLayout(): Int = R.layout.activity_settings

    override fun initView() {
        log.d("userdata: ${userData}")
        DaggerMVPComponent
                .builder()
                .mVPModule(MVPModule(this, Model(),this))
                .presenterModule(PresenterModule())
                .errorConnModule(ErrorConnModule(this,false))
                .build()
                .inject(this)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setTitle(resources.getString(R.string.settings))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {

            onBackPressed()

        }

        /*FIRST LAST NAME AND USERNAME*/
        name.setText("${userData.first_name} ${userData.last_name}")
        username.setText(userData.userName)
        name.addTextChangedListener(textwatcher)
        username.addTextChangedListener(textwatcher)
        /*FIRST LAST NAME AND USERNAME*/

        /*PHONE AND MAIL*/
        phone.text = if (!userData.userPhone.isNullOrEmpty()) userData.userPhone else resources.getString(R.string.addPhone)
        mail.text  = if (!userData.userMail.isNullOrEmpty()) userData.userMail else resources.getString(R.string.addMail)
        phone.setOnClickListener{
            changePhoneDialog = PhoneFormFragment.instance()
            changePhoneDialog!!.setDialogClickListener(object : PhoneFormFragment.DialogClickListener{
                override fun click(whichButton: Int) {
                    log.d("$whichButton")
                    if (whichButton == PhoneFormFragment.GET_SMS){

                            if (Functions.clearEdit(changePhoneDialog!!.phone).length != 9){

                                changePhoneDialog!!.phone.error = resources.getString(R.string.error_incorrect_phone)


                            }else{

                                val js = JSONObject()
                                js.put("user_id",userData.userId)
                                js.put("session",userData.session)
                                val phoneStr = "998${Functions.clearEdit(changePhoneDialog!!.phone)}"
                                js.put("phone",phoneStr)
                                changePhoneDialog!!.setVisibility(true)

                                presenter.requestAndResponse(js, Http.CMDS.CHANGE_PHONE_NUMBER)
                            }



                    }else{
                        if (changePhoneDialog!!.smsCode.text.toString().trim().length == 6){

                            val js = JSONObject()
                            js.put("user_id",userData.userId)
                            js.put("session",userData.session)
                            val phoneStr = "998${Functions.clearEdit(changePhoneDialog!!.phone)}"
                            js.put("phone",phoneStr)
                            js.put("code",changePhoneDialog!!.smsCode.text.toString().trim())
                            changePhoneDialog!!.setVisibility(true)
                            presenter.requestAndResponse(js, Http.CMDS.ACCEPT_CHANGE_PHONE)


                        }else{
                            changePhoneDialog!!.smsCode.error = resources.getString(R.string.sms_code_error)

                        }

                    }
                }

            })

            changePhoneDialog!!.show(supportFragmentManager,YesNoFragment.TAG)
        }
        mail.setOnClickListener{


            changeMailDialog = MailFormFragment.instance()
            changeMailDialog!!.setDialogClickListener(object : MailFormFragment.DialogClickListener{
                override fun click(whichButton: Int) {

                    if (whichButton == PhoneFormFragment.GET_SMS){

                        if (!Const.VALID_EMAIL_ADDRESS_REGEX.matcher(changeMailDialog!!.mail.text.toString()).find()){
                            changeMailDialog!!.mail.error = resources.getString(R.string.error_incorrect_mail)
                        }else{
                            val js = JSONObject()
                            js.put("user_id",userData.userId)
                            js.put("session",userData.session)
                            val phoneStr = changeMailDialog!!.mail.text.toString()
                            js.put("mail",phoneStr)
                            changeMailDialog!!.setVisibility(true)

                            presenter.requestAndResponse(js, Http.CMDS.CHANGE_MAIL)
                        }



                    }else{

                        if (changeMailDialog!!.smsCode.text.toString().trim().length == 6){

                            val js = JSONObject()
                            js.put("user_id",userData.userId)
                            js.put("session",userData.session)
                            js.put("mail",changeMailDialog!!.mail)
                            js.put("code",changeMailDialog!!.smsCode.text.toString().trim())
                            changeMailDialog!!.setVisibility(true)
                            presenter.requestAndResponse(js, Http.CMDS.ACCEPT_MAIL)


                        }else{
                            changePhoneDialog!!.smsCode.error = resources.getString(R.string.sms_code_error)

                        }


                    }
                }

            })

            changeMailDialog!!.show(supportFragmentManager,YesNoFragment.TAG)
        }
        /*PHONE AND MAIL*/


        /*GENDER*/
        val genderAdapter = ArrayAdapter<String>(this,R.layout.white_textview,sex)
        genderAdapter.setDropDownViewResource(R.layout.white_textview_adapter)
        gender.adapter = genderAdapter
        gender.setSelection(if (userData.gender == "N") 0
                            else if(userData.gender == "F") 1
                            else 2)
        /*GENDER*/
        switchCloseAccount.isChecked = if(Base.get.prefs.getUser().close == 1 ) true else false
        switchCloseAccount.setOnCheckedChangeListener{view, isChecked ->
            val js = JSONObject()
            js.put("session",Base.get.prefs.getUser().session)
            js.put("user_id",Base.get.prefs.getUser().userId)
            model.responseCall(Http.getRequestData(js, Http.CMDS.CLOSE_PROFIL))
                    .enqueue(object :Callback<ResponseData>{
                        override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {
                            log.d("close profil $response")
                            try{
                               if (response!!.body()!!.res == "0"){
                                   val user = Base.get.prefs.getUser()
                                   log.d("closed :${user.close}")

                                   user.close = if(user.close == 1) 0 else 1
                                   Base.get.prefs.setUser(user)
                               }
                           }catch (e:Exception){
                                switchCloseAccount.isChecked = if(Base.get.prefs.getUser().close == 1 ) true else false
                           }

                        }

                        override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
                            log.d("close profil fail $t")
                            switchCloseAccount.isChecked = if(Base.get.prefs.getUser().close == 1 ) true else false

                        }

                    })
         }
        /*GENDER*/

        /*QUIT*/
        quitLay.setOnClickListener {
                val dialog = YesNoFragment.instance()
                        dialog.setDialogClickListener(object : YesNoFragment.DialogClickListener{
                            override fun click(whichButton: Int) {

                                if (whichButton == YesNoFragment.NO){
                                    dialog.dismiss()
                                }else{
                                    dialog.dismiss()

                                    MainActivity.start          = 0
                                    MainActivity.end            = 20
                                    MainActivity.startFeed      = 0
                                    MainActivity.endFeed        = 20
                                    MainActivity.startFollowers = 0
                                    MainActivity.endFollowers   = 20
                                    MainActivity.startFollowing = 0
                                    MainActivity.endFollowing   = 20
                                    MainActivity.MY_POSTS_STATUS = MainActivity.NEED_UPDATE
                                    MainActivity.FEED_STATUS = MainActivity.NEED_UPDATE
                                    MainActivity.COMMENT_POST_UPDATE = 0
                                    MainActivity.COMMENT_COUNT = 0
                                    val sesion = SessionOut.Builder(this@SettingsActivity)
                                            .setErrorCode(96)
                                            .build()
                                    sesion.out()
                                }

                            }

                        })
                dialog.show(supportFragmentManager,YesNoFragment.TAG)

        }
        /*QUIT*/

    }


    override fun activityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode,resultCode,data)

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_save,menu)
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {



      if (changed || map.get(gender.selectedItemPosition) != Base.get.prefs.getUser().gender){



          errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
              override fun connected() {
                  log.d("connected")

                  val pattern  = Pattern.compile(SimpleoAuth.REGEXP.loginAndPasswordRegExp)

                      if (username.text.toString().isEmpty() || !pattern.matcher(username.text.toString()).matches()){
                          onFailure("1",resources.getString(R.string.error_symbol))
                      }else{
                          send();



                      }




              }

              override fun disconnected() {
                  log.d("disconnected")

                    Toast.makeText(this@SettingsActivity,resources.getString(R.string.internet_conn_error),Toast.LENGTH_SHORT).show()
              }

          })

      }
        return true
    }


    fun send(){
        val jsObject = JSONObject()
        jsObject.put("user_id",Base.get.prefs.getUser().userId)
        jsObject.put("session",Base.get.prefs.getUser().session)
        jsObject.put("username",username.text.toString())
        jsObject.put("name",name.text.trim().toString())
        jsObject.put("gender", map.get(gender.selectedItemPosition))


        presenter.requestAndResponse(jsObject, Http.CMDS.CHANGE_USER_SETTINGS)
    }

    override fun initProgress() {

    }

    override fun showProgress() {
    }

    override fun hideProgress() {
    }

    override fun onSuccess(from: String, result: String) {


        when(from){
            Http.CMDS.CHANGE_PHONE_NUMBER -> {
                val response = JSONObject(result)
                log.d("from change phone number -> $response")
                changePhoneDialog!!.setVisibility(false)
                changePhoneDialog!!.setSms("231233")
            }
            Http.CMDS.ACCEPT_CHANGE_PHONE -> {
                changePhoneDialog!!.setVisibility(false)
                userData.userPhone = Functions.clearEdit(changePhoneDialog!!.phone)
                Prefs.Builder().setUser(userData)
                phone.text = Prefs.Builder().getUser().userPhone
                changePhoneDialog!!.dismiss()
            }

            Http.CMDS.CHANGE_MAIL -> {
                val response = JSONObject(result)
                log.d("from change phone number -> $response")
                changeMailDialog!!.setVisibility(false)
                changeMailDialog!!.setSms("231233")
            }
            Http.CMDS.ACCEPT_MAIL -> {
                changeMailDialog!!.setVisibility(false)
                userData.userMail = changeMailDialog!!.mail.text.toString()
                Prefs.Builder().setUser(userData)
                mail.text = Prefs.Builder().getUser().userMail
                changeMailDialog!!.dismiss()

            }


            Http.CMDS.CHANGE_USER_SETTINGS -> {
                val user = Base.get.prefs.getUser()
                user.first_name = name.text.toString()
                user.gender = map.get(gender.selectedItemPosition)!!
                user.userName = username.text.toString()

                Base.get.prefs.setUser(user)

                Toast.makeText(Base.get.context,Base.get.context.resources.getString(R.string.saved),Toast.LENGTH_SHORT).show()
            }


        }


    }

    override fun onFailure(from: String, message: String, erroCode: String) {
       if(from == Http.CMDS.CHANGE_PHONE_NUMBER){
           changePhoneDialog!!.dismiss()
       }

        when(from){
            Http.CMDS.CHANGE_PHONE_NUMBER ->{
                changePhoneDialog!!.setVisibility(false)
            }
            Http.CMDS.ACCEPT_CHANGE_PHONE -> {
                changePhoneDialog!!.setVisibility(false)

            }
            Http.CMDS.CHANGE_MAIL -> {
                changeMailDialog!!.setVisibility(false)

            }
            Http.CMDS.ACCEPT_MAIL -> {
                changeMailDialog!!.setVisibility(false)

            }
            Http.CMDS.CHANGE_USER_SETTINGS -> {
                changePhoneDialog!!.setVisibility(false)

            }

        }
        Toast.makeText(Base.get.context,message,Toast.LENGTH_SHORT).show()
    }


    val textwatcher = object : TextWatcher{
        override fun afterTextChanged(s: Editable?) {
            changed = true
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


}