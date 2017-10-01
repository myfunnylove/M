package locidnet.com.marvarid.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder

import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.model.Image
import locidnet.com.marvarid.resources.utils.Functions
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.resources.zoomimageview.ImageViewer


/**
 * Created by Michaelan on 6/28/2017.
 */
class PostPhotoGridAdapter(ctx:Context,list:ArrayList<Image>) : RecyclerView.Adapter<PostPhotoGridAdapter.Holder>() {


    val context = ctx
    val images = list
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    val isVertical = true
    var hierarchyBuilder:GenericDraweeHierarchyBuilder? =null
    //var cachedImages:ArrayList<Bitmap>? = null
    init {
        setHasStableIds(true)
        hierarchyBuilder = GenericDraweeHierarchyBuilder.newInstance(Base.get.resources)
                .setFailureImage(VectorDrawableCompat.create(Base.get.resources, R.drawable.image_broken_variant_white, null))
                .setProgressBarImage(VectorDrawableCompat.create(Base.get.resources, R.drawable.image, null))
                .setPlaceholderImage(VectorDrawableCompat.create(Base.get.resources, R.drawable.image, null))

    }
    override fun getItemCount(): Int = images.size
    override fun getItemId(position: Int): Long = position.toLong()
    override fun onBindViewHolder(h: Holder?, i: Int) {
        val img = images.get(i)

      //  var dimenId = -1
        val itemView = h!!.itemView



        //if(i == 0)  dimenId = R.dimen.staggered_child_xlarge else   dimenId = R.dimen.staggered_child_small
//        if (i % 3 == 0)
//            dimenId = R.dimen.staggered_child_medium
//        else if (i % 5 == 0)
//            dimenId = R.dimen.staggered_child_large
//        else if (i % 7 == 0)
//            dimenId = R.dimen.staggered_child_xlarge
//        else
//            dimenId = R.dimen.staggered_child_small


        val span = if (i == 0) (images.size - 1) else 1


     //   val size = Base.get.resources.getDimensionPixelSize(dimenId)

        val params: GridLayoutManager.LayoutParams = itemView.layoutParams as GridLayoutManager.LayoutParams

        //if (isVertical) params.width = size else  params.height = size


        if ((images.size > 2 && images.size != 3)&& i >= 1){
            log.d("params: ${h.container.layoutParams.height}")
            val p = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,200)
            p.height = 200
            h.container.layoutParams = p
            params.height = 200
        }
        else if (images.size == 3 && i >= 1){
            log.d("params: ${h.container.layoutParams.height}")
            val p = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,400)
            p.height = 400
            h.container.layoutParams = p
            params.height = 400
        }
        else{
            val p = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,600)
            p.height = 600
            h.container.layoutParams = p
            params.height = 600
        }

        itemView.layoutParams = params


        Glide.with(context)
                .load(Functions.checkImageUrl(img.image640))
                .apply(RequestOptions().centerCrop()
                                       .fallback(ColorDrawable(Color.GRAY))
                                       .placeholder(ColorDrawable(Color.LTGRAY))
                                       .error(ColorDrawable(Color.LTGRAY)))
                .into(h.photo)


        h.photo.setOnClickListener {

            ImageViewer.Builder(context,images)
                    .setFormatter(object : ImageViewer.Formatter<Image>{
                        override fun format(t: Image?): String {
                            return Http.BASE_URL+t!!.image
                        }

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

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): Holder {

        return Holder(inflater.inflate(R.layout.res_post_photo_item,p0,false))
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {

        var container:RelativeLayout = view.findViewById<RelativeLayout>(R.id.container)
        var photo:AppCompatImageView = view.findViewById<AppCompatImageView>(R.id.photo)
    }


}