package locidnet.com.marvarid.resources;

import android.content.Context;
import android.util.AttributeSet;

import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Created by myfunnylove on 04.11.2017.
 */

public class BaseImageView extends SimpleDraweeView {
    public BaseImageView(Context context) {
        super(context);
    }

    public BaseImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public BaseImageView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }
}