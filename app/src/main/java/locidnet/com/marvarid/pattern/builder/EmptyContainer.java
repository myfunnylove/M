package locidnet.com.marvarid.pattern.builder;

import android.app.Activity;
import android.content.Context;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import locidnet.com.marvarid.R;

/**
 * Created by Sarvar on 12.09.2017.
 */

public class EmptyContainer {

    private final ViewGroup container;

    public void show(){
        container.setVisibility(View.VISIBLE);
    }

    public void hide(){
        container.setVisibility(View.GONE);
    }
    public static class Builder{

        private int icon;
        private int text;
        private AppCompatImageView imageView;
        private TextView textView;
        private ViewGroup container;



        public Builder setIcon(int icon) {
            this.icon = icon;
            return this;
        }

        public Builder setText(int text) {
            this.text = text;
            return this;

        }
        public Builder initLayoutForActivity(AppCompatActivity activity){
//            textView = activity.findViewById(R.id.emptyText);
//            textView.setText(activity.getResources().getString(text));
            imageView = activity.findViewById(R.id.emptyIcon);
            imageView.setImageDrawable(VectorDrawableCompat.create(activity.getResources(),icon,activity.getTheme()));
            container = activity.findViewById(R.id.emptyContainer);
            return this;
        }

        public Builder initLayoutForFragment(View view){
//            textView = view.findViewById(R.id.emptyText);
//            textView.setText(view.getResources().getString(text));
            imageView = view.findViewById(R.id.emptyIcon);
            imageView.setImageDrawable(VectorDrawableCompat.create(view.getResources(),icon,null));
            container = view.findViewById(R.id.emptyContainer);
            return this;
        }

        public EmptyContainer build(){
            return new EmptyContainer(this);
        }

    }

    private EmptyContainer(Builder builder) {
        container = builder.container;
    }
}
