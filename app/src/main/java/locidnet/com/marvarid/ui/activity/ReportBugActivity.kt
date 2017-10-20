package locidnet.com.marvarid.ui.activity

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_report_bug.*
import locidnet.com.marvarid.BuildConfig
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.di.DaggerMVPComponent
import locidnet.com.marvarid.di.modules.ErrorConnModule
import locidnet.com.marvarid.di.modules.MVPModule
import locidnet.com.marvarid.di.modules.PresenterModule
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.mvp.Presenter
import locidnet.com.marvarid.mvp.Viewer
import locidnet.com.marvarid.resources.utils.Functions
import locidnet.com.marvarid.resources.utils.JS
import locidnet.com.marvarid.resources.utils.Toaster
import locidnet.com.marvarid.rest.Http
import javax.inject.Inject

class ReportBugActivity : BaseActivity(),Viewer {


    @Inject
    lateinit var presenter: Presenter

    override fun getLayout(): Int = R.layout.activity_report_bug

    override fun initView() {
        DaggerMVPComponent
                .builder()
                .mVPModule(MVPModule(this, Model(),this))
                .presenterModule(PresenterModule())
                .errorConnModule(ErrorConnModule(this,true))
                .build()
                .inject(this)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = resources.getString(R.string.feedBack)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_send,menu)

        return true

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (subject.text.toString().isEmpty() || message.text.toString().isEmpty()){
            Toaster.info(resources.getString(R.string.error_empty_quote))
        }else{
            val js = JS.get()
            js.put("title",subject.text.toString())
            js.put("text", message.text.toString())
            js.put("app",  BuildConfig.VERSION_NAME)
            js.put("device",Functions.getDeviceName())
            presenter.requestAndResponse(js,Http.CMDS.REPORT_BUG)

        }
        return true
    }

    override fun initProgress() {
    }

    override fun showProgress() {
        progress.visibility = View.VISIBLE

    }

    override fun hideProgress() {
        progress.visibility = View.GONE

    }

    override fun onSuccess(from: String, result: String) {
        subject.text.clear()
        message.text.clear()
        Toast.makeText(this,resources.getString(R.string.thank_data_sent),Toast.LENGTH_SHORT).show()

    }

    override fun onFailure(from: String, message: String, erroCode: String) {
        Toaster.errror(message)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out)

    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.ondestroy()

    }
}