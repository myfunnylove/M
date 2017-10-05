package locidnet.com.marvarid.pattern.signatureDecorator;

import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import locidnet.com.marvarid.resources.utils.log;

/**
 * Created by myfunnylove on 05.10.17.
 */

public class MD5SignDecorator extends SignDecorator {

    AppSignature appSignature;

    public MD5SignDecorator(AppSignature appSignature) {
        this.appSignature = appSignature;
    }

    @NotNull
    @Override
    public String getSignature() {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(appSignature.getSignature().getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                StringBuilder h = new StringBuilder(Integer.toHexString(0xFF & aMessageDigest));
                while (h.length() < 2)
                    h.insert(0, "0");
                hexString.append(h);
            }
            log.INSTANCE.d("sign 2 step :" + hexString.toString());

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
