package locidnet.com.marvarid.resources;

/**
 * Created by myfunnylove on 04.11.2017.
 */

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeHierarchy;
import com.facebook.drawee.view.DraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

public class Loader {
    public void m1191a(String str, ImageView imageView, int i) {
        load(str, imageView, i, GenericDraweeHierarchyBuilder.DEFAULT_FADE_DURATION, null, null);
    }

    public void load(String str, ImageView imageView, int i, ControllerListener<ImageInfo> controllerListener) {
        load(str, imageView, i, GenericDraweeHierarchyBuilder.DEFAULT_FADE_DURATION, controllerListener, null);
    }

    public void m1194a(String str, ImageView imageView, int i, ControllerListener<ImageInfo> controllerListener, ScaleType scaleType) {
        load(str, imageView, i, GenericDraweeHierarchyBuilder.DEFAULT_FADE_DURATION, controllerListener, scaleType);
    }

    public void m1192a(String str, ImageView imageView, int i, int i2, int i3) {
        load(str, imageView, i, GenericDraweeHierarchyBuilder.DEFAULT_FADE_DURATION, null, null);
    }

    public void m1196b(String str, ImageView imageView, int i, int i2, int i3) {
        load(str, imageView, i, 0, null, null);
    }

    public void m1195b(String str, ImageView imageView, int i) {
        load(str, imageView, i, 0, null, null);
    }

    public static Loader init() {

        return new Loader();
    }

    private void load(String str, ImageView imageView, int i, int i2, ControllerListener<ImageInfo> controllerListener, ScaleType scaleType) {
        if (imageView instanceof DraweeView) {
            try {
                DraweeView draweeView = (DraweeView) imageView;
                DraweeHierarchy hierarchy = draweeView.getHierarchy();
                if (draweeView.getHierarchy() instanceof GenericDraweeHierarchy) {
                    if (i > 0) {
                        ((GenericDraweeHierarchy) hierarchy).setPlaceholderImage(i);
                    }
                    ((GenericDraweeHierarchy) hierarchy).setFadeDuration(i2);
                    if (str != null && str.startsWith("/")) {
                        str = "file://" + str;
                    }
                    if (scaleType != null) {
                        ((GenericDraweeHierarchy) hierarchy).setActualImageScaleType(ScaleType.FIT_CENTER);
                    }
                }
//                Uri decodeImageC = TextUtils.isEmpty(str) ? null : ImageLoader.decodeImageC(4, str, ImageLoader.f941a, draweeView, 2);
                if (controllerListener != null) {
//                    draweeView.setController(((PipelineDraweeControllerBuilder) ((PipelineDraweeControllerBuilder) Fresco.newDraweeControllerBuilder().setUri(decodeImageC).setOldController(draweeView.getController())).setControllerListener(controllerListener)).build());
                    return;
                } else {
//                    draweeView.setImageURI(decodeImageC);
                    return;
                }
            } catch (Throwable th) {
                return;
            }
        }
    }
}
