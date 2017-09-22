package locidnet.com.marvarid.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import com.bumptech.glide.Glide
import locidnet.com.marvarid.R
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.connectors.SignalListener
import locidnet.com.marvarid.model.Comment
import locidnet.com.marvarid.resources.customviews.CircleImageView
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Prefs
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.FollowActivity
import locidnet.com.marvarid.ui.fragment.ProfileFragment
import org.json.JSONObject


class CommentAdapter(context:Context,list:ArrayList<Comment>,clicker:AdapterClicker) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ctx= context
    var comments = list
    val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val clicker = clicker

    var animationsLocked = false
    var lastAnimatedPosition = -1
    var delayEnterAnimation = true

    val BODY = 1
    val LOAD_MORE= 2
    var loadMore:SignalListener? = null
    var lastCommentSize = list.size
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

            val url = if (!comment.avatar.isNullOrEmpty() && comment.avatar.startsWith("http")) comment.avatar else  Http.BASE_URL+comment.avatar
            Glide.with(ctx)
                    .load(url)
                    .error(R.drawable.account)
                    .into(h.avatar)

            h.comment.text  = comment.comment.replace("\\n","\n")
            h.username.text = comment.username

            h.container.setOnClickListener {

                val bundle = Bundle()
                val js = JSONObject()

                bundle.putString("username",comment.username)
                bundle.putString("photo",   comment.avatar)
                bundle.putString("user_id",  comment.userId)
                js.put("username",comment.username)
                js.put("photo",   comment.avatar)
                js.put("user_id",  comment.userId)

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
        }

    }

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): RecyclerView.ViewHolder {

       if (p1 == BODY)
        return Holder(inflater.inflate(R.layout.res_comment_box,p0,false))
        else
           return LoadMoreHolder(inflater.inflate(R.layout.pull_loadmore_layout,p0,false))
    }

    override fun getItemCount(): Int {
        return comments.size;
    }

    class Holder(view: View) :RecyclerView.ViewHolder(view){
        val avatar    = view.findViewById(R.id.avatar)    as CircleImageView
        val username  = view.findViewById(R.id.username)  as TextView
        val container = view.findViewById(R.id.container) as ViewGroup
        val comment   = view.findViewById(R.id.comment)   as TextView
    }


    class LoadMoreHolder(view: View) :RecyclerView.ViewHolder(view){
        val container = view.findViewById(R.id.container) as ViewGroup
    }

    fun  swapList(list:ArrayList<Comment>){
        comments = list
        notifyDataSetChanged()
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
                            animationsLocked = true;
                        }
                    }).start()
        }

    }


}
