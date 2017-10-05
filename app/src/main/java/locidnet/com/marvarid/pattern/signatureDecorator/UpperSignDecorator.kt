package locidnet.com.marvarid.pattern.signatureDecorator

import locidnet.com.marvarid.resources.utils.log

/**
 * Created by myfunnylove on 05.10.17.
 */
class UpperSignDecorator(val appSignature: AppSignature) :SignDecorator() {
    override fun getSignature(): String {
        log.d("sign 3 step : ${appSignature.getSignature().toUpperCase()}")

        return appSignature.getSignature().toUpperCase()
    }
}