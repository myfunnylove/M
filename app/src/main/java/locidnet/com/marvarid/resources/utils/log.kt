package locidnet.com.marvarid.resources.utils

import com.orhanobut.logger.Logger

/**
 * Created by Michaelan on 5/26/2017.
 */
object log {

    fun d(msg:String){
        Logger.d("${Const.TAG } => $msg")
    }

    fun e(msg:String){
        Logger.e("${Const.TAG } => $msg")
    }
    fun i(msg:String){
        Logger.i("${Const.TAG } => $msg")
    }
    fun v(msg:String){
        Logger.i("${Const.TAG } => $msg")
    }

    fun wtf(msg:String){
        Logger.wtf("${Const.TAG } => $msg")


    }

    fun json(js:String){
        Logger.json(js)
    }


}