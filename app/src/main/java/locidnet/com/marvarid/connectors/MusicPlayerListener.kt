package locidnet.com.marvarid.connectors

import locidnet.com.marvarid.model.Audio

/**
 * Created by locidnet on 07.08.2017.
 */
interface MusicPlayerListener {


    fun playClick(listSong:ArrayList<Audio>, position:Int)
}