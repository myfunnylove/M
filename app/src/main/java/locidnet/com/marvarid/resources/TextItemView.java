package locidnet.com.marvarid.resources;

/**
 * Created by myfunnylove on 04.11.2017.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import locidnet.com.marvarid.R;
import locidnet.com.marvarid.base.Base;


/**
 * @author Michael Spitsin
 * @since 2016-09-06
 */
public class TextItemView extends View  {
    private static final Drawable LOADING_PLACEHOLDER = new ColorDrawable(0x00000000);

    private static TextPaint titlePaint;
    private static TextPaint descriptionPaint;

    private Drawable iconDrawable;
    private StaticLayout titleLayout;
//    private StaticLayout descriptionLayout;

    private final int iconSize = (int) dp(56);
    private final int iconMargin = (int) dp(8);
    private final int verticalPadding = (int) dp(4);

    {
        LOADING_PLACEHOLDER.setBounds(iconMargin, iconMargin, iconMargin + iconSize, iconMargin + iconSize);

        int textXOffset = 2 * iconMargin + iconSize;
        LayoutCache.INSTANCE.changeWidth(Base.Companion.getSCREEN_SIZE().x - textXOffset);
    }

    public TextItemView(Context context) {
        super(context);
        init();
    }

    public TextItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TextItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int textHeight = titleLayout.getHeight();
        int viewHeight = 2 * verticalPadding + Math.max(iconDrawable.getBounds().height(), textHeight);
        setMeasuredDimension(width, viewHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        iconDrawable.draw(canvas);
        canvas.save();
        canvas.translate(2 * iconMargin + iconSize, verticalPadding);
        titleLayout.draw(canvas);
        canvas.translate(0, titleLayout.getHeight());
//        descriptionLayout.draw(canvas);
        canvas.restore();
    }

    @Override
    protected boolean verifyDrawable(Drawable drawable) {
        return iconDrawable == drawable || super.verifyDrawable(drawable);
    }


    public void setText(String text) {
        iconDrawable = LOADING_PLACEHOLDER;

        titleLayout = LayoutCache.INSTANCE.titleLayoutFor(text);
//        descriptionLayout = LayoutCache.INSTANCE.descriptionLayoutFor(text);

        requestLayout();
        invalidate();
    }

    private void init() {
        if (titlePaint == null) {
            titlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            titlePaint.setColor(getContext().getResources().getColor(R.color.normalTextColor));
            titlePaint.setTextSize(sp(22));
        }

        if (descriptionPaint == null) {
            descriptionPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            descriptionPaint.setColor(getContext().getResources().getColor(R.color.normalTextColor));
            descriptionPaint.setTextSize(sp(18));
        }
    }

    private float sp(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    private float dp(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private enum LayoutCache {
        INSTANCE;

        private int width;
        private final LruCache<CharSequence, StaticLayout> titleCache = new LruCache<CharSequence, StaticLayout>(100) {
            @Override
            protected StaticLayout create(CharSequence key) {
                CharSequence truncatedTitle = TextUtils.ellipsize(key, titlePaint, width, TextUtils.TruncateAt.END);
                return new StaticLayout(truncatedTitle, titlePaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 1, true);
            }
        };
        private final LruCache<CharSequence, StaticLayout> descriptionCache = new LruCache<CharSequence, StaticLayout>(100) {
            @Override
            protected StaticLayout create(CharSequence key) {
                return new StaticLayout(key, descriptionPaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 1, true);
            }
        };

        public void changeWidth(int newWidth) {
            if (width != newWidth) {
                width = newWidth;
                titleCache.evictAll();
                descriptionCache.evictAll();
            }
        }

        public StaticLayout titleLayoutFor(CharSequence text) {
            return titleCache.get(text);
        }

        public StaticLayout descriptionLayoutFor(CharSequence text) {
            return descriptionCache.get(text);
        }
    }
}
