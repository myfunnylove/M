package locidnet.com.marvarid.adapter

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import jp.wasabeef.glide.transformations.internal.Utils

import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.model.Image
import locidnet.com.marvarid.resources.utils.*
import locidnet.com.marvarid.resources.zoomimageview.ImageViewer


class PostPhotoGridAdapter(ctx:Context,list:ArrayList<Image>) : RecyclerView.Adapter<PostPhotoGridAdapter.Holder>() {


    val context = ctx
    val images = list
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var hierarchyBuilder:GenericDraweeHierarchyBuilder? =null
    private var options:RequestOptions? = null
    private var screenSize:Int? = null
    private var h1 = 300
    private var h2 = 400
    private var h3 = 600
    private var h4 = 500
    //var cachedImages:ArrayList<Bitmap>? = null
    init {
        setHasStableIds(true)
        hierarchyBuilder = GenericDraweeHierarchyBuilder.newInstance(Base.get.resources)
                .setFailureImage(VectorDrawableCompat.create(Base.get.resources, R.drawable.image_broken_variant_white, null))
                .setProgressBarImage(VectorDrawableCompat.create(Base.get.resources, R.drawable.image, null))
                .setPlaceholderImage(VectorDrawableCompat.create(Base.get.resources, R.drawable.image, null))

        if (options == null){
         options =    RequestOptions()
                    .fallback(ColorDrawable(Color.GRAY))
                    .error(ColorDrawable(Color.LTGRAY))
        }
      screenSize = JavaCodes.getScreenSize()
    }
    override fun getItemCount(): Int = images.size
    override fun getItemId(position: Int): Long = position.toLong()
    override fun onBindViewHolder(h: Holder?, i: Int) {
        val img = images[i]

        val itemView = h!!.itemView




       val params: GridLayoutManager.LayoutParams = itemView.layoutParams as GridLayoutManager.LayoutParams



       when(screenSize){
           Configuration.SCREENLAYOUT_SIZE_SMALL -> {
               log.d("small size")
               h1 = 300
               h2 = 400

               h3 = 600
               h4 = 500

           }
           Configuration.SCREENLAYOUT_SIZE_NORMAL -> {
               log.d("normal size")

               h1 = 400
               h2 = 500
               h3 = 800
               h4 = 700
           }
           Configuration.SCREENLAYOUT_SIZE_LARGE -> {
               log.d("large size")

               h1 = 500
               h2 = 600
               h3 = 1000
               h4 = 800

           }
           Configuration.SCREENLAYOUT_SIZE_XLARGE -> {
               log.d("xlarge size")

               h1 = 600
               h2 = 700
               h3 = 1200
               h4 = 800

           }
       }
        log.d("screen width width image - ${img.width} ${img.height}")
        if ((images.size > 2 && images.size != 3)&& i >= 1){
            log.d("params: ${h.container.layoutParams.height}")
            val p = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,h1)
            p.height = h1
            h.container.layoutParams = p
            params.height = h1
            options!!.centerCrop()

        }
        else if (images.size == 3 && i >= 1){
            log.d("params: ${h.container.layoutParams.height}")
            val p = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,h2)
            p.height = h2
            h.container.layoutParams = p
            params.height = h2
            options!!.centerCrop()
        }else if(images.size == 2){
            log.d("params: ${h.container.layoutParams.height}")
            val p = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,h2)
            p.height = h2
            h.container.layoutParams = p
            params.height = h2
            options!!.centerCrop()
        }
        else{
            if (img.width == "0" || img.height == "0"){

            options!!.centerCrop()
                val p = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,h3)
                p.height = h3
                h.container.layoutParams = p
                params.height = h3
            }
            else{

//                if (img.width.toInt() <= img.height.toInt()) {
                    options!!.centerCrop()
                    val p = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,h3)
                    p.height = h3
                    h.container.layoutParams = p
                    params.height = h3
//                }else {
//                    log.d("myscreen ${Functions.getScreenHeight()}")
//                    log.d("myscreen w: ${img.width.toInt()} h: ${img.height.toInt()}")
//                    log.d("myscreen result1: ${Functions.getScreenWidth() - (img.width.toInt() - img.height.toInt())}")
//                    log.d("myscreen result2: ${(img.width.toInt() - img.height.toInt())}")
//                    log.d("myscreen result3: ${(Functions.getScreenWidth() - img.height.toInt())}")
//                    log.d("myscreen result4: ${(Functions.getScreenWidth() - img.width.toInt())}")
//
//                    h3 = Functions.getScreenWidth() - (img.width.toInt() )
//                    val p = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, h3)
//                    p.height = h3
//                    h.container.layoutParams = p
//                    params.height = h3
//                }
            }


        }

        itemView.layoutParams = params

        log.d("image after ${Functions.checkImageUrl(img.image)!!.replace(Const.IMAGE.LOW, Prefs.Builder().imageRes())}")

        Glide.with(context)
                .load(Functions.checkImageUrl(img.image)!!.replace(Const.IMAGE.LOW, Prefs.Builder().imageRes()))

                .apply(options!!)
                .into(h.photo)





        h.photo.setOnClickListener {

            ImageViewer.Builder(context,images)
                    .setFormatter(object : ImageViewer.Formatter<Image>{
                        override fun format(t: Image?): String = Http.BASE_URL+t!!.image

                    })
                    .setStartPosition(i)
                    .hideStatusBar(true)
                    .allowZooming(true)
                    .allowSwipeToDismiss(true)
                    .setBackgroundColor(Base.get.resources.getColor(R.color.transparent80))
                    .setCustomDraweeHierarchyBuilder(hierarchyBuilder)
                    .show()


        }
    }

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): Holder =
            Holder(inflater.inflate(R.layout.res_post_photo_item,p0,false))

    class Holder(view: View) : RecyclerView.ViewHolder(view) {

        var container:RelativeLayout = view.findViewById<RelativeLayout>(R.id.container)
        var photo:AppCompatImageView = view.findViewById<AppCompatImageView>(R.id.photo)
    }


}