package locidnet.com.marvarid.pattern.builder;

import android.content.Intent;

import locidnet.com.marvarid.base.BaseActivity;
import locidnet.com.marvarid.resources.utils.Const;
import locidnet.com.marvarid.resources.utils.Prefs;
import locidnet.com.marvarid.ui.activity.LoginActivity;


public class SessionOut {

    private final BaseActivity context;

    private final int errorCode;

    public BaseActivity getContext() {
        return context;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public static class Builder{

        private final BaseActivity context;
        private int errorCode = -1;

        public Builder(BaseActivity context) {
            this.context = context;
        }

        public BaseActivity getContext() {
            return context;
        }

        int getErrorCode() {
            return errorCode;
        }

        public Builder setErrorCode(int errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public SessionOut build(){

            return new SessionOut(this);
        }
    }

    private SessionOut(Builder builder){
        context = builder.getContext();
        errorCode = builder.getErrorCode();

    }

    public void out(){

        Prefs.INSTANCE.Builder()
                      .clearUser();

        context.startActivityForResult(new Intent(context, LoginActivity.class), Const.INSTANCE.getSESSION_OUT());

    }

}
