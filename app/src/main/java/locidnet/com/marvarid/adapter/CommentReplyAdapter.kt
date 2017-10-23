package locidnet.com.marvarid.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import com.bumptech.glide.Glide
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.connectors.SignalListener
import locidnet.com.marvarid.model.Comment
import locidnet.com.marvarid.resources.hashtag.HashTagHelper
import locidnet.com.marvarid.resources.utils.*
import locidnet.com.marvarid.ui.activity.FollowActivity
import locidnet.com.marvarid.ui.activity.SearchByTagActivity
import locidnet.com.marvarid.ui.fragment.ProfileFragment
import org.ocpsoft.prettytime.PrettyTime
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class CommentReplyAdapter(context: Context, list: ArrayList<Comment>, val clicker: AdapterClicker) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ctx= context
    var comments = list
    val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    var animationsLocked = false
    var lastAnimatedPosition = -1
    var delayEnterAnimation = true

    val BODY = 1
    val LOAD_MORE= 2
    var loadMore: SignalListener? = null
    var lastCommentSize = list.size
    val prettyTime = PrettyTime()
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    fun setAdapterClicker(adapterClicker: SignalListener){
        loadMore = adapterClicker
    }
    override fun getItemViewType(position: Int): Int {

        if (position == 0 && lastCommentSize >= 10)
            return LOAD_MORE
        else
            return BODY

    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, i: Int) {



            val h = holder as Holder
            h.itemView.runEnterAnimation(i)

            val comment = comments.get(i)

            Glide.with(ctx)
                    .load(Functions.checkImageUrl(comment.avatar))
                    .apply(Functions.getGlideOpts())
                    .into(h.avatar)

            h.comment.text  = comment.comment.replace("\\n","\n")
            val hashTag = HashTagHelper.Creator.create(
                    Base.get.resources.getColor(R.color.hashtag),
                    object : HashTagHelper.OnHashTagClickListener{
                        override fun onHashTagClicked(hashTag: String?) {
                            var intent:Intent? = Intent(ctx, SearchByTagActivity::class.java)
                            intent!!.putExtra("tag",hashTag!!)
                            ctx.startActivity(intent)
                            intent = null
                        }
                        override fun onLoginClicked(login: String?) {
                            Toaster.info(login!!)
                        }

                    })
            hashTag.handle(h.comment)
            h.username.text = comment.username

            val date2 =  if (!comment.date.equals("now"))
                formatter.parse(comment.date) as Date
            else {
                val str = formatter.format(Calendar.getInstance().time)

                formatter.parse(str)
            }

        h.commentDate.text = prettyTime.format(date2)

            h.container.setOnClickListener {

                val bundle = Bundle()
                val js = JS.get()

                bundle.putString("username",comment.username)
                bundle.putString("photo",   comment.avatar)
                bundle.putString("user_id",  comment.userId)
                js.put("username",comment.username)
                js.put("photo",   comment.avatar)

                log.d("user userid ${comment.userId} my userId ${Prefs.getUser().userId}")

                if (comment.userId != Prefs.getUser().userId){


                    val go = Intent(ctx, FollowActivity::class.java)
                    bundle.putString(ProfileFragment.F_TYPE, "")
                    go.putExtra(FollowActivity.TYPE, FollowActivity.PROFIL_T)

                    go.putExtras(bundle)

                    ctx.startActivity(go)



                }else{
                    val go = Intent(ctx, FollowActivity::class.java)
                    bundle.putString(ProfileFragment.F_TYPE, ProfileFragment.SETTINGS)
                    go.putExtras(bundle)


                    go.putExtra(FollowActivity.TYPE, FollowActivity.PROFIL_T)

                    ctx.startActivity(go)
                }

        }

    }

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): RecyclerView.ViewHolder {

           return Holder(inflater.inflate(R.layout.res_comment_reply_item,p0,false))
    }

    override fun getItemCount(): Int = comments.size

    class Holder(view: View) : RecyclerView.ViewHolder(view){
        val avatar    = view.findViewById<AppCompatImageView>(R.id.repliedAvatar)
        val username  = view.findViewById<TextView>(R.id.repliedUsername)
        val container = view.findViewById<ViewGroup>(R.id.repliedContainer)
        val comment   = view.findViewById<TextView>(R.id.repliedComment)
        val commentDate   = view.findViewById<TextView>(R.id.repliedDate)
    }


    class LoadMoreHolder(view: View) : RecyclerView.ViewHolder(view){
        val container = view.findViewById<ViewGroup>(R.id.container)
    }


    fun swapLast(list: ArrayList<Comment>) {
        val lastItemPostition = (comments.size + 1)
        comments.addAll(list)
        notifyItemRangeInserted(lastItemPostition,list.size)
    }

    fun swapToTop(list: ArrayList<Comment>) {
        lastCommentSize = list.size
        comments.addAll(0,list)
        notifyItemRangeInserted(0,list.size)
    }
    fun View.runEnterAnimation(position:Int){

        //if (animationsLocked) return
        if (position > lastAnimatedPosition){

            lastAnimatedPosition = position

            this.translationY = 100f

            this.alpha        = 0f
            this.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setStartDelay(if (delayEnterAnimation) (20 * position).toLong() else 0)
                    .setInterpolator(DecelerateInterpolator(2f))
                    .setDuration(300)
                    .setListener(object : AnimatorListenerAdapter(){
                        override fun onAnimationEnd(animation: Animator?) {
                           if (translationY == 100f) translationY = 0f
                            animationsLocked = true
                        }
                    }).start()
        }

    }

}