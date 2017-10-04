package locidnet.com.marvarid.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_new_password.*
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
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Toaster
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.rest.Http
import org.json.JSONObject
import javax.inject.Inject

/**
 * Created by Michaelan on 6/16/2017.
 */
class NewPasswordActivity : BaseActivity(), Viewer {

    @Inject
    lateinit var presenter: Presenter
    @Inject
    lateinit var errorConn: ErrorConnection

    var isLoginFree = false
    var pass = ""
    var passAgain = ""
    var from = -1

    override fun getLayout(): Int = R.layout.activity_new_password

    override fun initView() {
        Const.TAG = "NewPasswordACtivity"

        from = intent.getIntExtra("from",from)


        DaggerMVPComponent.builder()
                .mVPModule(MVPModule(this, Model(), this))
                .presenterModule(PresenterModule())
                .errorConnModule(ErrorConnModule(this, false))
                .build()
                .inject(this)





        send.setOnClickListener {

            errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener {
                override fun connected() {
                    log.d("connected")


                    pass = newpass.text.toString()
                    passAgain =    newsPassAgain.text.toString()

                    if (!pass.equals(passAgain)){
                        Toast.makeText(this@NewPasswordActivity, resources.getString(R.string.password_doesnot_match), Toast.LENGTH_SHORT).show()

                    }
                  else if(pass.length < 6 || passAgain.length < 6){

                        Toast.makeText(this@NewPasswordActivity, resources.getString(R.string.password_field_less_5), Toast.LENGTH_SHORT).show()

                    }else{

                        val obj = JSONObject()
                        obj.put("user",intent.getStringExtra("userId"))
                        obj.put("token",intent.getStringExtra("token"))
                        obj.put("password",pass)
                        presenter.requestAndResponse(obj, Http.CMDS.FORGOT_PASS)

                  }

                }

                override fun disconnected() {
                    log.d("disconnected")

                    Toast.makeText(this@NewPasswordActivity, resources.getString(R.string.internet_conn_error), Toast.LENGTH_SHORT).show()

                }

            })



        }
    }

    override fun initProgress() {
        progressLay.visibility = View.VISIBLE
    }

    override fun showProgress() {
    }

    override fun hideProgress() {
        progressLay.visibility = View.GONE
    }

    override fun onSuccess(from: String, result: String) {


            setResult(Activity.RESULT_OK)
            this.finish()

    }

    override fun onFailure(from: String, message: String, erroCode: String) {

//            errorText.visibility = View.VISIBLE
//            errorText.text = message
            Toaster.errror(message)

//            Handler().postDelayed({
//                errorText.text = ""
//                errorText.visibility = View.GONE
//            },3000)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Const.SESSION_OUT || resultCode == Const.SESSION_OUT){
            setResult(Const.SESSION_OUT)
            finish()
        }
    }
    fun AppCompatEditText.setLoginResult(drawable:Int = 0){
        if(drawable != 0){
            val drawableCompat = VectorDrawableCompat.create(resources, drawable, this.context.theme)
            this.setCompoundDrawablesWithIntrinsicBounds(null,null,drawableCompat,null)
        }else{
            this.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null)

        }
    }






    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, LoginActivity().javaClass))
        this.finish()
    }
}