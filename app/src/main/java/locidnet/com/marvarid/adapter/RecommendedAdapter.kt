package locidnet.com.marvarid.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import locidnet.com.marvarid.R
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.model.RecPost
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Functions
import locidnet.com.marvarid.resources.utils.Prefs
import locidnet.com.marvarid.ui.activity.UserPostActivity
import kotlin.properties.Delegates

class RecommendedAdapter(clicker: AdapterClicker, ctx: Context, list: ArrayList<RecPost>) : RecyclerView.Adapter<RecommendedAdapter.Adapter>() {
    private val adapterClicker = clicker
    val context = ctx
    val posts = list
    val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater



    override fun onBindViewHolder(h: Adapter?, i: Int) {

        val post = posts[i]
        h!!.photo.post {
            h.photo.controller = Fresco.newDraweeControllerBuilder()

                    .setImageRequest(
                            ImageRequestBuilder.newBuilderWithSource(Uri.parse(Functions.checkImageUrl(post.photo)!!.replace(Const.IMAGE.MEDIUM, Prefs.Builder().imageRes())))
//                                            .setResizeOptions(ResizeOptions(width,height))
                                    .setCacheChoice(ImageRequest.CacheChoice.DEFAULT)
                                    .build())
                    .setOldController(h.photo.controller)
                    .setAutoPlayAnimations(true)
                    .build()

        }


        h.photo.setOnClickListener {

            adapterClicker.click(h.adapterPosition)
        }
    }

    override fun getItemCount(): Int = posts.size

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): Adapter =
            Adapter(inflater.inflate(R.layout.res_recommended_item, p0, false))

    class Adapter(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var photo by Delegates.notNull<SimpleDraweeView>()
        init {
            photo = itemView.findViewById<SimpleDraweeView>(R.id.photo)!!
            photo.hierarchy = Functions.getPostPhotoHierarchy()

        }
    }
}