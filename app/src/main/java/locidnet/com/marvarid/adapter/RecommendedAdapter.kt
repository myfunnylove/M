package locidnet.com.marvarid.adapter

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import locidnet.com.marvarid.R
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.model.Color
import locidnet.com.marvarid.model.RecPost
import locidnet.com.marvarid.model.RecommededPosts
import locidnet.com.marvarid.rest.Http
import kotlin.properties.Delegates

class RecommendedAdapter(clicker: AdapterClicker, ctx: Context, list: ArrayList<RecPost>) : RecyclerView.Adapter<RecommendedAdapter.Adapter>() {
    val adapterClicker = clicker
    val context = ctx
    val posts = list
    val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    val options: RequestOptions? = RequestOptions()
            .centerCrop()
            .fallback(VectorDrawableCompat.create(ctx.resources, R.drawable.account, ctx.theme))

            .error(VectorDrawableCompat.create(ctx.resources, R.drawable.account, ctx.theme))

    override fun onBindViewHolder(h: Adapter?, i: Int) {

        val post = posts.get(i)

        Glide.with(context)
                .load(Http.BASE_URL + post.photo)
                .apply(options!!)
                .into(h!!.photo)
        h.photo.setOnClickListener {
            adapterClicker.click(h.adapterPosition)
        }
    }

    override fun getItemCount(): Int = posts.size

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): Adapter =
            Adapter(inflater.inflate(R.layout.res_recommended_item, p0, false))

    class Adapter(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var photo = itemView.findViewById<AppCompatImageView>(R.id.photo)
    }
}