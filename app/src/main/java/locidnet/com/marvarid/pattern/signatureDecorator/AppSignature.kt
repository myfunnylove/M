package locidnet.com.marvarid.pattern.signatureDecorator

import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.resources.utils.log

/**
 * Created by myfunnylove on 05.10.17.
 */
abstract open class AppSignature {

    protected var sign:String = ""
    open fun getSignature():String{
        log.d("sign 1 step : ${sign + "+${Base.get.resources.getString(R.string.secretKey)}"}")
      return sign + "+${Base.get.resources.getString(R.string.secretKey)}"
    }
}