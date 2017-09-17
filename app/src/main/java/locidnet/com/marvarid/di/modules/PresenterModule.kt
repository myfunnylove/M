package locidnet.com.marvarid.di.modules

import dagger.Module
import dagger.Provides
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.di.scopes.MVPScope
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.mvp.Presenter
import locidnet.com.marvarid.mvp.Viewer

/**
 * Created by Sarvar on 22.08.2017.
 */
@Module
class PresenterModule {

    @Provides
    fun presenter(viewer: Viewer,model:Model,context:BaseActivity):Presenter = Presenter(viewer,model,context)
}