package locidnet.com.marvarid.base

import android.arch.lifecycle.LifecycleObserver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import locidnet.com.marvarid.R
import locidnet.com.marvarid.mvp.Viewer
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.viewmodel.PublishViewmodel
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import kotlin.properties.Delegates


/**
 * Created by Michaelan on 5/18/2017.
 */
abstract class BaseActivity : AppCompatActivity (){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            setContentView(getLayout())
        initView()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    abstract fun getLayout():Int

    abstract fun initView()




}