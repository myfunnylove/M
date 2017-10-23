package locidnet.com.marvarid.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
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
import org.json.JSONObject
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CommentAdapter(context:Context, list:ArrayList<Comment>, val clicker: AdapterClicker) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ctx= context
    var comments = list
    val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    var animationsLocked = false
    var lastAnimatedPosition = -1
    var delayEnterAnimation = true

    val BODY = 1
    val LOAD_MORE= 2
    var loadMore:SignalListener? = null
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

        if(getItemViewType(i) == LOAD_MORE){

            val h = holder as LoadMoreHolder
            h.container.setOnClickListener {
                loadMore!!.turnOn()

            }
        }
        else if (getItemViewType(i) == BODY){

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

            val date2 = formatter.parse(comment.date) as Date
            h.commentDate.text = prettyTime.format(date2)
            h.reply.setOnClickListener {
                clicker.click(h.adapterPosition)
            }
            h.container.setOnClickListener {

                val bundle = Bundle()
                val js = JS.get()

                bundle.putString("username",comment.username)
                bundle.putString("photo",   comment.avatar)
                bundle.putString("user_id",  comment.userId)
                js.put("username",comment.username)
                js.put("photo",   comment.avatar)

                log.d("user userid ${comment.userId} my userId ${Prefs.Builder().getUser().userId}")

                if (comment.userId != Prefs.Builder().getUser().userId){


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

            if (comment.isReply){
                h.replyList.visibility = View.VISIBLE
                h.repliedTitle.visibility = View.VISIBLE
                h.replyList.layoutManager = LinearLayoutManager(ctx)
                h.replyList.setHasFixedSize(true)
                Collections.reverse(comment.replies)
                h.replyList.adapter = CommentReplyAdapter(ctx, comment.replies,clicker)
            }else{
                h.replyList.visibility = View.GONE
                h.repliedTitle.visibility = View.GONE
            }
        }

    }

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): RecyclerView.ViewHolder {

        return if (p1 == BODY)
            Holder(inflater.inflate(R.layout.res_comment_box,p0,false))
        else
            LoadMoreHolder(inflater.inflate(R.layout.pull_loadmore_layout,p0,false))
    }

    override fun getItemCount(): Int = comments.size

    class Holder(view: View) :RecyclerView.ViewHolder(view){
        val avatar    = view.findViewById<AppCompatImageView>(R.id.avatar)
        val username  = view.findViewById<TextView>(R.id.username)
        val container = view.findViewById<ViewGroup>(R.id.container)
        val comment   = view.findViewById<TextView>(R.id.comment)
        val commentDate   = view.findViewById<TextView>(R.id.commentDate)
        val reply   = view.findViewById<TextView>(R.id.reply)
        val replyList = view.findViewById<RecyclerView>(R.id.replyList)
        val repliedTitle = view.findViewById<TextView>(R.id.repliedTitle)
    }


    class LoadMoreHolder(view: View) :RecyclerView.ViewHolder(view){
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

    fun swapReplyComment(comm: String?, choosedCommPos: Int) {
        comments.get(choosedCommPos).isReply = true
        val usr = Prefs.Builder().getUser()

        comments.get(choosedCommPos).replies.add(Comment(
                usr.userName,
                usr.userId,
                usr.profilPhoto,
                "0",
                comm!!,
                "now",
                false,
                ArrayList()))
        Collections.reverse(comments.get(choosedCommPos).replies)
        notifyItemChanged(choosedCommPos)

        }



}
