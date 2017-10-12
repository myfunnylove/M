package locidnet.com.marvarid.rest;

import locidnet.com.marvarid.base.Base;
import locidnet.com.marvarid.resources.utils.Functions;

/**
 * Created by myfunnylove on 12.10.17.
 */

public class NetM implements OkCacheControl.NetworkMonitor {
    @Override
    public boolean isOnline() {
        return Functions.INSTANCE.isNetworkAvailable(Base.Companion.getGet());
    }
}
