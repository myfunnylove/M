package locidnet.com.marvarid.ui.activity

import kotlinx.android.synthetic.main.activity_about_us.*
import locidnet.com.marvarid.BuildConfig
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.BaseActivity

/**
 * Created by myfunnylove on 10.10.17.
 */
class AboutUsActivity : BaseActivity(){
    override fun getLayout(): Int {
    return    R.layout.activity_about_us
    }

    override fun initView() {

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = resources.getString(R.string.aboutApp)

        toolbar.setNavigationOnClickListener {

            onBackPressed()

        }
        version.text = BuildConfig.VERSION_NAME
    }

}