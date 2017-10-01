package locidnet.com.marvarid.resources.utils;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import locidnet.com.marvarid.R;
import locidnet.com.marvarid.base.Base;


public class Toaster {



    public static void errror(String errorText){
        Toast toast = Toast.makeText(Base.Companion.getGet(),errorText,Toast.LENGTH_SHORT);
        toast.setView(View.inflate(Base.Companion.getGet(),R.layout.res_custom_toast_error,null));

        TextView textView = toast.getView().findViewById(R.id.txtMessage);
        textView.setText(errorText);
        textView.setCompoundDrawablesWithIntrinsicBounds(Base.Companion.getGet().getResources().getDrawable(R.drawable.close_),null,null,null);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }

    public static void errror(int errorText){
        Toast toast = Toast.makeText(Base.Companion.getGet(),Base.Companion.getGet().getResources().getString(errorText),Toast.LENGTH_SHORT);
        toast.setView(View.inflate(Base.Companion.getGet(),R.layout.res_custom_toast_error,null));

        TextView textView = toast.getView().findViewById(R.id.txtMessage);
        textView.setText(errorText);
        textView.setCompoundDrawablesWithIntrinsicBounds(Base.Companion.getGet().getResources().getDrawable(R.drawable.close_),null,null,null);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }
    public static void info(String errorText){
        Toast toast = Toast.makeText(Base.Companion.getGet(),errorText,Toast.LENGTH_SHORT);
        toast.setView(View.inflate(Base.Companion.getGet(),R.layout.res_custom_toast_error,null));

        TextView textView = toast.getView().findViewById(R.id.txtMessage);
        textView.setCompoundDrawablesWithIntrinsicBounds(Base.Companion.getGet().getResources().getDrawable(R.drawable.info_),null,null,null);

        textView.setBackgroundDrawable(Base.Companion.getGet().getResources().getDrawable(R.drawable.button_bg_info));
        textView.setText(errorText);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();

    }
}
