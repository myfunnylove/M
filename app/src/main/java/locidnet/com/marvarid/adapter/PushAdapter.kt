package locidnet.com.marvarid.adapter

import android.content.Context
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import locidnet.com.marvarid.R
import locidnet.com.marvarid.model.Push
import locidnet.com.marvarid.model.PushList
import locidnet.com.marvarid.resources.customviews.CircleImageView
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Functions
import locidnet.com.marvarid.rest.Http

/**
 * Created by myfunnylove on 17.09.17.
 */
class PushAdapter(private val ctx:Context,private val list:ArrayList<Push> ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int = list.get(position).type



    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        when(viewType){

            Const.Push.LIKE -> return Like(inflater.inflate(R.layout.res_item_push_like,parent,false))

            Const.Push.COMMENT -> return Comment(inflater.inflate(R.layout.res_item_push_like,parent,false))

            Const.Push.FOLLOW -> return Requested(inflater.inflate(R.layout.res_item_push_requested,parent,false))
            Const.Push.REQUESTED -> return Requested(inflater.inflate(R.layout.res_item_push_requested,parent,false))

            else -> return Other(inflater.inflate(R.layout.res_item_push_requested,parent,false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        val push = list.get(position)

        when(getItemViewType(position)){

            Const.Push.LIKE -> {

                val like = holder as Like

                    try{
                        Picasso.with(ctx)
                                .load(push.user.userPhoto)
                                .error(VectorDrawableCompat.create(ctx.resources,R.drawable.account,ctx.theme))
                                .into(like.avatar)
                    }catch (e:Exception){

                    }

                like.username.text = push.user.userName
                like.body.text = ctx.resources.getString(R.string.pushLikeBody)

                Picasso.with(ctx)
                        .load(push.action.actionPhoto)
                        .error(VectorDrawableCompat.create(ctx.resources,R.drawable.image_broken_variant_white,ctx.theme))
                        .into(like.mypost)

                like.mypost.setOnClickListener{
                    Toast.makeText(ctx,"Like pressed ",Toast.LENGTH_SHORT).show()
                }
            }

            Const.Push.COMMENT -> {

                val comment = holder as Comment
                Picasso.with(ctx)
                        .load(push.user.userPhoto)
                        .error(VectorDrawableCompat.create(ctx.resources,R.drawable.account,ctx.theme))
                        .into(comment.avatar)
                comment.username.text = push.user.userName
                comment.body.text = ctx.resources.getString(R.string.pushCommentBody)

                Picasso.with(ctx)
                        .load(Http.BASE_URL +push.action.actionPhoto)
                        .error(VectorDrawableCompat.create(ctx.resources,R.drawable.image_broken_variant_white,ctx.theme))
                        .into(comment.mypost)

                comment.mypost.setOnClickListener{
                    Toast.makeText(ctx,"Comment pressed ",Toast.LENGTH_SHORT).show()
                }
            }

            Const.Push.REQUESTED -> {

                val requested = holder as Requested
                Picasso.with(ctx)
                        .load(Http.BASE_URL + push.action.actionPhoto)
                        .error(VectorDrawableCompat.create(ctx.resources,R.drawable.account,ctx.theme))
                        .into(requested.avatar)

                requested.username.text = push.user.userName

                requested.body.text = ctx.resources.getString(R.string.pushRequestBody)


                requested.action.setText(Functions.getString(R.string.allow))

                requested.action.setOnClickListener{
                    Toast.makeText(ctx,"requested pressed ",Toast.LENGTH_SHORT).show()
                }
            }

            Const.Push.FOLLOW -> {

                val follow = holder as Requested
                Picasso.with(ctx)
                        .load(Http.BASE_URL + push.action.actionPhoto)
                        .error(VectorDrawableCompat.create(ctx.resources,R.drawable.account,ctx.theme))
                        .into(follow.avatar)

                follow.username.text = push.user.userName

                follow.body.text = ctx.resources.getString(R.string.pushFollowBody)

                follow.action.setText(Functions.getString(R.string.allow))


                follow.action.setOnClickListener{
                    Toast.makeText(ctx,"other pressed ",Toast.LENGTH_SHORT).show()
                }
            }

        }
    }


    class Like(view: View) : RecyclerView.ViewHolder(view) {

        val container  = view.findViewById(R.id.container) as ViewGroup
        val avatar = view.findViewById(R.id.avatar) as CircleImageView
        val username = view.findViewById(R.id.username) as TextView
        val body = view.findViewById(R.id.body) as TextView
        val mypost = view.findViewById(R.id.mypost) as ImageView

    }

    class Comment(view: View) : RecyclerView.ViewHolder(view) {
        val container  = view.findViewById(R.id.container) as ViewGroup
        val avatar = view.findViewById(R.id.avatar) as CircleImageView
        val username = view.findViewById(R.id.username) as TextView
        val body = view.findViewById(R.id.body) as TextView
        val mypost = view.findViewById(R.id.mypost) as ImageView
    }

    class Requested(view: View) : RecyclerView.ViewHolder(view) {
        val container  = view.findViewById(R.id.container) as ViewGroup
        val avatar = view.findViewById(R.id.avatar) as CircleImageView
        val username = view.findViewById(R.id.username) as TextView
        val body = view.findViewById(R.id.body) as TextView
        val action = view.findViewById(R.id.action) as Button
    }

    class Other(view: View) : RecyclerView.ViewHolder(view) {
        val container  = view.findViewById(R.id.container) as ViewGroup
        val avatar = view.findViewById(R.id.avatar) as CircleImageView
        val username = view.findViewById(R.id.username) as TextView
        val body = view.findViewById(R.id.body) as TextView
        val action = view.findViewById(R.id.action) as Button
    }

    fun swapItems(pushList: PushList) {

        list.addAll(pushList.pushes)
        notifyItemRangeInserted(list.size + 1,pushList.pushes.size)

    }
}