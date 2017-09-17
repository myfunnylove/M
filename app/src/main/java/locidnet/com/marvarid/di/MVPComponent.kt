package locidnet.com.marvarid.di

import dagger.Component
import locidnet.com.marvarid.di.modules.ErrorConnModule
import locidnet.com.marvarid.di.modules.MVPModule
import locidnet.com.marvarid.di.modules.PresenterModule
import locidnet.com.marvarid.di.scopes.MVPScope
import locidnet.com.marvarid.ui.activity.*
import locidnet.com.marvarid.ui.activity.publish.PublishUniversalActivity

/**
 * Created by Sarvar on 22.08.2017.
 */
@MVPScope
@Component(modules = arrayOf(MVPModule::class, PresenterModule::class,ErrorConnModule::class))
interface MVPComponent {

    fun inject(signActivity: SignActivity)
    fun inject(mainActivity: MainActivity)
    fun inject(commentActivity: CommentActivity)
    fun inject(followActivity: FollowActivity)
    fun inject(loginActivity: LoginActivity)
    fun inject(settingsActivity: SettingsActivity)
    fun inject(publishUniversalActivity: PublishUniversalActivity)
    fun inject(loginAndPassActivity: LoginAndPassActivity)
    fun inject(playlistActivity: PlaylistActivity)

}