package locidnet.com.marvarid.resources.utils

import com.orhanobut.logger.Logger

/**
 * Created by Michaelan on 5/26/2017.
 */
object log {
    val show = true;
    fun d(msg:String){
        if(show) Logger.d("${Const.TAG } => $msg")
    }

    fun e(msg:String){
        if(show) Logger.e("${Const.TAG } => $msg")
    }
    fun i(msg:String){
        if(show) Logger.i("${Const.TAG } => $msg")
    }
    fun v(msg:String){
        if(show) Logger.i("${Const.TAG } => $msg")
    }

    fun wtf(msg:String){
        if(show) Logger.wtf("${Const.TAG } => $msg")


    }

    fun json(js:String){
        if(show)  Logger.json(js)
    }


}