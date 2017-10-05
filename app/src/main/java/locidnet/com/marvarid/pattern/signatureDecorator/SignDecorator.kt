package locidnet.com.marvarid.pattern.signatureDecorator

/**
 * Created by myfunnylove on 05.10.17.
 */
abstract class SignDecorator:AppSignature() {

    abstract override fun getSignature():String
}