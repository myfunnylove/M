package locidnet.com.marvarid.pattern.MControlllObserver

/**
 * Created by Sarvar on 19.09.2017.
 */
interface Subject {

    fun subscribe(observer: Observer)
    fun unsubscribe(observer: Observer)
    fun notifyObserver()

}