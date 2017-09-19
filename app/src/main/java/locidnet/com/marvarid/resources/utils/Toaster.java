package locidnet.com.marvarid.resources.utils;

import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import locidnet.com.marvarid.R;
import locidnet.com.marvarid.base.Base;

/**
 * Created by Sarvar on 19.09.2017.
 */

public class Toaster {



    public static void errror(String errorText){
        Toast toast = Toast.makeText(Base.Companion.getGet(),errorText,Toast.LENGTH_SHORT);
        TextView textView = (TextView) toast.getView().findViewById(android.R.id.message);
        textView.setTextColor(Color.WHITE);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.getView().setBackgroundColor(Color.RED);
        toast.getView().setPadding(16,8,16,8);
        toast.show();
    }
    public static void info(String errorText){
        Toast toast = Toast.makeText(Base.Companion.getGet(),errorText,Toast.LENGTH_SHORT);
        TextView textView = (TextView) toast.getView().findViewById(android.R.id.message);
        textView.setTextColor(Color.WHITE);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.getView().setPadding(16,8,16,8);

        toast.getView().setBackgroundColor(Base.Companion.getGet().getResources().getColor(R.color.orange));
        toast.show();

    }
}
