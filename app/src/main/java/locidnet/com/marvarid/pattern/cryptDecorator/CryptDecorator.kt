package locidnet.com.marvarid.pattern.cryptDecorator

/**
 * Created by microlab on 12.09.2017.
 */
abstract class  CryptDecorator :Crypt() {

    abstract override fun getPrm():String
}