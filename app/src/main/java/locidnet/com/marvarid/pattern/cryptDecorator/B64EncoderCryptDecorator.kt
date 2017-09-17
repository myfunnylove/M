package locidnet.com.marvarid.pattern.cryptDecorator

import android.util.Base64

/**
 * Created by Sarvar on 12.09.2017.
 */
class B64EncoderCryptDecorator(crypt: Crypt) : CryptDecorator() {

    var crypto:Crypt = crypt

    override fun getPrm(): String =
            String(Base64.encode(crypto.getPrm().toByteArray(),0)).replace("\n","")

}