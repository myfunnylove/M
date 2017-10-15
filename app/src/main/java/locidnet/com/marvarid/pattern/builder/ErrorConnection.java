package locidnet.com.marvarid.pattern.builder;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import locidnet.com.marvarid.R;
import locidnet.com.marvarid.base.Base;
import locidnet.com.marvarid.ui.activity.MainActivity;


public class ErrorConnection {

    private final AppCompatActivity activity;
    private final Button tryAgain;
    private final ViewGroup container;
    private ErrorListener errorListener;
    private final boolean showLayout;
    public ErrorConnection checkNetworkConnection(ErrorListener listener){
        this.errorListener = listener;
        if (isConnected()){
            hideErrorLayout();
            errorListener.connected();
        }else {
            if (showLayout) showErrorLayout();
            errorListener.disconnected();

        }

        return this;
    }


    public void showErrorLayout(){
        container.setVisibility(View.VISIBLE);
    }
    public void hideErrorLayout(){
        container.setVisibility(View.GONE);
    }


    public boolean isConnected() {

        NetworkInfo info = getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
    private NetworkInfo getActiveNetworkInfo() {
        return ((ConnectivityManager) Base.Companion.getGet().getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
    }

    public static class Builder {

        private final AppCompatActivity activity;
        private Button tryAgain;
        private ViewGroup container;
        private boolean showLayout = true;
        public Builder(AppCompatActivity activity) {
            this.activity = activity;
        }

        public Builder init(){
            tryAgain = activity.findViewById(R.id.errorButton);
            container = activity.findViewById(R.id.connectAgain);

            return this;
        }

        public Builder show(boolean isShow){
            showLayout = isShow;
            return this;
        }

        public Builder initClickListener(){

//            this.tryAgain.setOnClickListener(listener);
            return this;
        }

        public ErrorConnection build(){
            return new ErrorConnection(this);
        }
    }

    private ErrorConnection(Builder builder) {
        activity   = builder.activity;
        tryAgain   = builder.tryAgain;
        container  = builder.container;
        showLayout = builder.showLayout;
        tryAgain.setOnClickListener(listener);
    }

    public interface ErrorListener{

        void connected();
        void disconnected();
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MainActivity.MyPostOffset.setNOTIF_STATUS(MainActivity.MyPostOffset.getNEED_UPDATE());
            MainActivity.MyPostOffset.setMY_POSTS_STATUS(MainActivity.MyPostOffset.getNEED_UPDATE());
            MainActivity.MyPostOffset.setFEED_STATUS(MainActivity.MyPostOffset.getNEED_UPDATE());
            
            checkNetworkConnection(errorListener);
        }
    };
}
