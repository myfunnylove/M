package locidnet.com.marvarid.PlayListRoom;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import locidnet.com.marvarid.model.Audio;

/**
 * Created by myfunnylove on 12.10.17.
 */
@Database(entities = {Audio.class},version = 1)
public abstract class AppDB extends RoomDatabase{

    public abstract PlayListDAO playListDAO();
}
