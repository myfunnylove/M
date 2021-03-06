package locidnet.com.marvarid.resources.customviews.imageuploadmask;

/**
 * Created by Michaelan on 6/23/2017.
 */

import android.content.Context;
import android.graphics.Path;
import android.util.AttributeSet;

public class RoundRectMask extends ShapeMask {

    private Path mClipPath = new Path();

    public RoundRectMask(Context context) {
        this(context, null);
    }

    public RoundRectMask(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundRectMask(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected Path getClipPath() {
        mClipPath.reset();
        mClipPath.addRoundRect(mMaskOvalRect, mCornerRadius, mCornerRadius, Path.Direction.CW);
        return mClipPath;
    }
}
