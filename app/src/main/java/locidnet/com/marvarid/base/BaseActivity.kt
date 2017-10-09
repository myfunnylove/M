package locidnet.com.marvarid.base

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper



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

    open fun hideLoading(){

    }
    open fun showLoading(){

    }


}