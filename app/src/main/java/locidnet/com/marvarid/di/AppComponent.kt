package locidnet.com.marvarid.di

import dagger.Component
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.di.modules.ApiModule
import locidnet.com.marvarid.di.modules.ContextModule
import locidnet.com.marvarid.di.modules.NetworkModule
import javax.inject.Singleton

/**
 * Created by Sarvar on 22.08.2017.
 */
@Singleton
@Component(modules = arrayOf(ContextModule::class,NetworkModule::class,ApiModule::class))
interface AppComponent {

    fun inject (base: Base)


}