package me.iwf.photopicker.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.R;
import me.iwf.photopicker.utils.AndroidLifecycleUtils;

/**
 * Created by donglua on 15/6/21.
 */
public class PhotoPagerAdapter extends PagerAdapter {

  private List<String> paths = new ArrayList<>();
  private RequestManager mGlide;

  public PhotoPagerAdapter(RequestManager glide, List<String> paths) {
    this.paths = paths;
    this.mGlide = glide;
  }

  @Override public Object instantiateItem(ViewGroup container, int position) {
    final Context context = container.getContext();
    View itemView = LayoutInflater.from(context)
        .inflate(R.layout.__picker_picker_item_pager, container, false);

    final ImageView imageView = itemView.findViewById(R.id.iv_pager);

    final String path = paths.get(position);
    final Uri uri;
    if (path.startsWith("http")) {
      uri = Uri.parse(path);
    } else {
      uri = Uri.fromFile(new File(path));
    }

    boolean canLoadImage = AndroidLifecycleUtils.canLoadImage(context);
    if (canLoadImage) {
      RequestOptions options = new RequestOptions();
      options.dontAnimate();
      options.override(800, 800);
      options.placeholder(R.drawable.__picker_ic_photo_black_48dp);
      options.error(R.drawable.__picker_ic_broken_image_black_48dp);
        options.dontTransform();
      mGlide.load(uri)
              .thumbnail(0.1f)
             .apply(options)
              .into(imageView);
    }

    imageView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
//        if (context instanceof Activity) {
//          if (!((Activity) context).isFinishing()) {
//            ((Activity) context).onBackPressed();
//          }
//        }

        String IMAGE_NAME = "preview.jpg";
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setFreeStyleCropEnabled(false);
        options.setHideBottomControls(false);
          options.setToolbarColor(ContextCompat.getColor(context,R.color.__picker_colorPrimary));
          options.setStatusBarColor(ContextCompat.getColor(context,R.color.__picker_asbestos));
          options.setActiveWidgetColor(ContextCompat.getColor(context,R.color.__picker_colorPrimary));
          options.setToolbarWidgetColor(ContextCompat.getColor(context,R.color.__picker_sky));
          options.setRootViewBackgroundColor(ContextCompat.getColor(context,R.color.__picker_colorPrimary));
          UCrop.of(uri,Uri.fromFile(new File(context.getCacheDir(),IMAGE_NAME)))
          .useSourceImageAspectRatio()
          .withOptions(options)
          .start((Activity)context);

      }
    });

    container.addView(itemView);

    return itemView;
  }


  @Override public int getCount() {
    return paths.size();
  }


  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }


  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView((View) object);
//    Glide.with(context).clear((View) object);
  }

  @Override
  public int getItemPosition (Object object) { return POSITION_NONE; }

}
