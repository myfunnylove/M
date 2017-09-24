package locidnet.com.marvarid.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.model.Push
import locidnet.com.marvarid.model.PushList
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Functions
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.rest.Http
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.*

/*
 *
 *  Created by myfunnylove on 17.09.17.
 *
 */
class PushAdapter(private val ctx: Context, private val list: ArrayList<Push>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val options               = RequestOptions()
            .centerCrop()
            .fallback(VectorDrawableCompat.create(Base.get.resources,R.drawable.image, Base.get.theme))
            .error(VectorDrawableCompat.create(Base.get.resources,R.drawable.image, Base.get.theme))
            .placeholder(VectorDrawableCompat.create(Base.get.resources,R.drawable.image, Base.get.theme))
    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int = list.get(position).type


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {

            Const.Push.LIKE -> return Like(inflater.inflate(R.layout.res_item_push_like, parent, false))

            Const.Push.COMMENT -> return Comment(inflater.inflate(R.layout.res_item_push_like, parent, false))

            Const.Push.FOLLOW -> return Requested(inflater.inflate(R.layout.res_item_push_requested, parent, false))
            Const.Push.REQUESTED -> return Requested(inflater.inflate(R.layout.res_item_push_requested, parent, false))

            else -> return Other(inflater.inflate(R.layout.res_item_push_requested, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        val push = list.get(position)
        val prettyTime = PrettyTime()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date2 = formatter.parse(push.time) as Date

        when (getItemViewType(position)) {

            Const.Push.LIKE -> {

                val like = holder as Like


                Glide.with(ctx)
                        .load(Functions.checkImageUrl(push.user.userPhoto))
                        .apply(Functions.getGlideOpts())
                        .into(like.avatar)

                like.username.text = push.user.userName
                like.body.text = ctx.resources.getString(R.string.pushLikeBody)




                like.time.text = prettyTime.format(date2)



                Glide.with(ctx)
                        .load(Functions.checkImageUrl(push.action.actionPhoto))
                        .apply(options)
                        .into(like.mypost)
                like.mypost.setOnClickListener {
                    Toast.makeText(ctx, "Like pressed ", Toast.LENGTH_SHORT).show()
                }
            }

            Const.Push.COMMENT -> {

                val comment = holder as Comment


                Glide.with(ctx)
                        .load(Functions.checkImageUrl(push.user.userPhoto))
                        .apply(Functions.getGlideOpts())
                        .into(comment.avatar)

                comment.username.text = push.user.userName
                comment.body.text = ctx.resources.getString(R.string.pushCommentBody)
                comment.time.text = prettyTime.format(date2)

                Glide.with(ctx)
                        .load(Functions.checkImageUrl(push.action.actionPhoto))
                        .apply(options)
                        .into(comment.mypost)


                comment.mypost.setOnClickListener {
                    Toast.makeText(ctx, "Comment pressed ", Toast.LENGTH_SHORT).show()
                }
            }

            Const.Push.REQUESTED -> {

                val requested = holder as Requested

                Glide.with(ctx)
                        .load(Functions.checkImageUrl(push.action.actionPhoto))
                        .apply(Functions.getGlideOpts())
                        .into(requested.avatar)
                requested.username.text = push.user.userName

                requested.body.text = ctx.resources.getString(R.string.pushRequestBody)

//                requested.time.text = prettyTime.format(date2)

                requested.accept.setText(Functions.getString(R.string.allow))
                requested.dismiss.setText(Functions.getString(R.string.ignore))
                requested.dismiss.setText(Functions.getString(R.string.allow))

                requested.accept.setOnClickListener {

                }
                requested.dismiss.setOnClickListener{

                }

            }

            Const.Push.FOLLOW -> {

                val follow = holder as Requested

                Glide.with(ctx)
                        .load(Functions.checkImageUrl(push.action.actionPhoto))
                        .apply(Functions.getGlideOpts())
                        .into(follow.avatar)
                follow.username.text = push.user.userName

                follow.body.text = ctx.resources.getString(R.string.pushFollowBody)
//                follow.time.text = prettyTime.format(date2)

                follow.dismiss.visibility = View.GONE

                log.d("push:  ${push}")
                try{
                    if (push.actions.requestIt == "1")
                        follow.accept.setText(Functions.getString(R.string.request))
                    else if(push.actions.followIt == "1")
                        follow.accept.setText(Functions.getString(R.string.unfollow))
                    else
                        follow.accept.setText(Functions.getString(R.string.follow))

                }catch (e:Exception){
                    follow.accept.setText(Functions.getString(R.string.follow))
                }



                follow.accept.setOnClickListener {



                }
            }

        }
    }


    class Like(view: View) : RecyclerView.ViewHolder(view) {

        val container = view.findViewById(R.id.container) as ViewGroup
        val avatar = view.findViewById(R.id.avatar) as AppCompatImageView
        val username = view.findViewById(R.id.username) as TextView
        val body = view.findViewById(R.id.body) as TextView
        val time = view.findViewById(R.id.time) as TextView
        val mypost = view.findViewById(R.id.actionPhoto) as AppCompatImageView

    }

    class Comment(view: View) : RecyclerView.ViewHolder(view) {
        val container = view.findViewById(R.id.container) as ViewGroup
        val avatar = view.findViewById(R.id.avatar) as AppCompatImageView
        val username = view.findViewById(R.id.username) as TextView
        val body = view.findViewById(R.id.body) as TextView
        val time = view.findViewById(R.id.time) as TextView

        val mypost = view.findViewById(R.id.actionPhoto) as AppCompatImageView
    }

    class Requested(view: View) : RecyclerView.ViewHolder(view) {
        val container = view.findViewById(R.id.container) as ViewGroup
        val avatar = view.findViewById(R.id.avatar) as AppCompatImageView
        val username = view.findViewById(R.id.username) as TextView
        val body = view.findViewById(R.id.body) as TextView
        val accept = view.findViewById(R.id.accept) as Button
        val dismiss = view.findViewById(R.id.dismiss) as Button
    }

    class Other(view: View) : RecyclerView.ViewHolder(view) {
        val container = view.findViewById(R.id.container) as ViewGroup
        val avatar = view.findViewById(R.id.avatar) as AppCompatImageView
        val username = view.findViewById(R.id.username) as TextView
        val body = view.findViewById(R.id.body) as TextView
        val accept = view.findViewById(R.id.accept) as Button
        val dismiss = view.findViewById(R.id.dismiss) as Button
    }

    fun swapItems(pushList: PushList) {

        list.addAll(pushList.pushes)
        notifyItemRangeInserted(list.size + 1, pushList.pushes.size)

    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder?) {

//        try{
//            Glide.with(ctx).clear(holder!!.itemView)
//
//
//        }catch (e:Exception){}
        super.onViewRecycled(holder)
    }
}