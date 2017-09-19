package locidnet.com.marvarid.pattern.MControlllObserver

/**
 * Created by Sarvar on 19.09.2017.
 */
class MusicSubject() : Subject {

    val list:ArrayList<Observer> = ArrayList()

    override fun subscribe(observer: Observer) {
        list.add(observer)
    }

    override fun unsubscribe(observer: Observer) {

        val id = list.indexOf(observer)

       if (id >= 0){
           list.removeAt(id)
       }
    }

    override fun notifyObserver() {


    }

    fun update(){
        notifyObserver()
    }

}