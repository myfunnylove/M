package locidnet.com.marvarid.di.modules

import android.arch.persistence.room.Room
import android.content.Context
import android.support.transition.Visibility
import dagger.Module
import dagger.Provides
import locidnet.com.marvarid.PlayListRoom.AppDB

/**
 * Created by myfunnylove on 12.10.17.
 */
@Module(includes = arrayOf(ContextModule::class))
class OrmModule {

    @Provides
    fun roomORM(context:Context):AppDB =
            Room.databaseBuilder(context,AppDB::class.java,"marvaridDb").build()
}