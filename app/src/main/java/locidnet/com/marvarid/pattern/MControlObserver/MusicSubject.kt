package locidnet.com.marvarid.pattern.MControlObserver

import locidnet.com.marvarid.resources.utils.log

class MusicSubject : Subject {

    companion object {
        var list:ArrayList<MusicControlObserver> = ArrayList()
    }

    var id:String = ""
    override fun subscribe(observer: MusicControlObserver) {

        list.add(observer)
    }

    override fun unsubscribe(observer: MusicControlObserver) {
        val id = list.indexOf(observer)
        if (id >= 0){
            list.removeAt(id)
        }
    }

    override fun playControlUpdate() {
        for (item in list){
            log.d(item.toString())
            item.playPause(id)
        }

    }





    fun playMeause(id:String){
        this.id = id
        playControlUpdate()
    }


}