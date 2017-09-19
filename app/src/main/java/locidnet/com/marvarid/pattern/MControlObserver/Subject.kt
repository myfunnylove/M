package locidnet.com.marvarid.pattern.MControlObserver

/**
 * Created by Sarvar on 19.09.2017.
 */
interface Subject {

    fun subscribe(observer: MusicControlObserver)
    fun unsubscribe(observer: MusicControlObserver)
    fun playControlUpdate()

}