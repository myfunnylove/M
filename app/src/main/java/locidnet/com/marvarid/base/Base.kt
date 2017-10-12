package locidnet.com.marvarid.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.support.multidex.MultiDex
import android.support.v7.app.AppCompatDelegate
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.drawee.backends.pipeline.Fresco
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKAccessTokenTracker
import com.vk.sdk.VKSdk
import locidnet.com.marvarid.R
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.LoginActivity
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import locidnet.com.marvarid.PlayListRoom.AppDB
import locidnet.com.marvarid.rest.API
import locidnet.com.marvarid.di.DaggerAppComponent
import locidnet.com.marvarid.di.modules.ApiModule
import locidnet.com.marvarid.di.modules.ContextModule
import locidnet.com.marvarid.di.modules.NetworkModule
import locidnet.com.marvarid.di.modules.OrmModule
import locidnet.com.marvarid.resources.utils.Prefs
import java.io.File
import javax.inject.Inject



class Base : Application (){


    @Inject
    lateinit var APIClient: API

    @Inject
    lateinit var context:Context

    @Inject
    lateinit var prefs:Prefs

    @Inject
    lateinit var appDb:AppDB

    private val vkAccessTokenTracker = object : VKAccessTokenTracker(){
        override fun onVKAccessTokenChanged(oldToken: VKAccessToken?, newToken: VKAccessToken?) {
            if (newToken == null) {
                log.d("new token is null")
                goLoginActivity()
            }
        }

    }





    private fun goLoginActivity() {
        val intent = Intent(this,LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        startActivity(intent)
    }

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)

        get = this
        DaggerAppComponent
                .builder()
                .apiModule(ApiModule())
                .contextModule(ContextModule(this))
                .networkModule(NetworkModule())
                .ormModule(OrmModule())

                .build()
                .inject(this)




        val conf = ImageLoaderConfiguration.Builder(context)
                .build()


        vkAccessTokenTracker.startTracking()
        VKSdk.initialize(context)

        FacebookSdk.sdkInitialize(context)

        AppEventsLogger.activateApp(this)


        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)


        Logger.addLogAdapter(AndroidLogAdapter())
        val config = ImagePipelineConfig.newBuilder(context)
                .setProgressiveJpegConfig(SimpleProgressiveJpegConfig())
                .setResizeAndRotateEnabledForNetwork(true)
                .setDownsampleEnabled(true)
                .build()
        Fresco.initialize(context, config)

        CalligraphyConfig.initDefault(
                CalligraphyConfig.Builder()
                        .setDefaultFontPath("font/regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build())

    }



    companion object {


        @SuppressLint("StaticFieldLeak")
        lateinit var get: Base
            private set
    }


}