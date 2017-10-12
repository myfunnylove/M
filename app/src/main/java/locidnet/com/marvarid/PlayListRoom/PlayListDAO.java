package locidnet.com.marvarid.PlayListRoom;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.ArrayList;
import java.util.List;

import locidnet.com.marvarid.model.Audio;

/**
 * Created by myfunnylove on 12.10.17.
 */
@Dao
public interface PlayListDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAudios(List<Audio> audios);

    @Query("SELECT * FROM audio")
    List<Audio> getAllAudios();

    @Delete
    void deleteAudio(Audio audio);


}
