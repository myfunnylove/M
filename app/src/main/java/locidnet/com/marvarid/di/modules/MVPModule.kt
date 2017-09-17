package locidnet.com.marvarid.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.di.scopes.MVPScope
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.mvp.Viewer
import javax.inject.Singleton

/**
 * Created by Sarvar on 22.08.2017.
 */
@Module
class MVPModule(private val viewer: Viewer,private val model: Model,private val context:BaseActivity) {

    @Provides
    fun model() : Model = model

    @Provides
    fun view()  : Viewer = viewer

    @Provides
    fun context() :BaseActivity = context
}