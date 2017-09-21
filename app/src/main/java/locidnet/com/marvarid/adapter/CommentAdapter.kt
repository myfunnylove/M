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
import locidnet.com.marvarid.model.Comment
import locidnet.com.marvarid.resources.customviews.CircleImageView
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Prefs
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.FollowActivity
import locidnet.com.marvarid.ui.fragment.ProfileFragment
import org.json.JSONObject


class CommentAdapter(context:Context,list:ArrayList<Comment>,clicker:AdapterClicker) : RecyclerView.Adapter<CommentAdapter.Holder>() {

    val ctx= context
    var comments = list
    val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val clicker = clicker

    var animationsLocked = false
    var lastAnimatedPosition = -1
    var delayEnterAnimation = true
    override fun onBindViewHolder(h: Holder?, i: Int) {

        h!!.itemView.runEnterAnimation(i)

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
            if (comment.userId != Prefs.Builder().getUser().userId){



//                if(comment.close == 1 && comment.follow == 0 && comment.request == 0){
//
//                    bundle.putString(ProfileFragment.F_TYPE,ProfileFragment.CLOSE)
//                    js.put(ProfileFragment.F_TYPE,ProfileFragment.CLOSE)
//
//                }else if(comment.close == 1 && comment.follow == 0 && comment.request == 1){
//
//                    bundle.putString(ProfileFragment.F_TYPE,ProfileFragment.REQUEST)
//                    js.put(ProfileFragment.F_TYPE,ProfileFragment.REQUEST)
//
//                }else if (comment.close == 1 && comment.follow == 1 && comment.request == 0){
//
//                    bundle.putString(ProfileFragment.F_TYPE,ProfileFragment.UN_FOLLOW)
//                    js.put(ProfileFragment.F_TYPE,ProfileFragment.UN_FOLLOW)
//
//                }else if (comment.close == 0 && comment.follow == 0 && comment.request == 1){
//
//                    bundle.putString(ProfileFragment.F_TYPE,ProfileFragment.FOLLOW)
//                    js.put(ProfileFragment.F_TYPE,ProfileFragment.FOLLOW)
//
//
//                }else if (comment.close == 0 && comment.follow == 1 && comment.request == 0){
//
//                    bundle.putString(ProfileFragment.F_TYPE,ProfileFragment.UN_FOLLOW)
//                    js.put(ProfileFragment.F_TYPE,ProfileFragment.UN_FOLLOW)
//
//                }else{
//                    bundle.putString(ProfileFragment.F_TYPE,ProfileFragment.FOLLOW)
//                    js.put(ProfileFragment.F_TYPE,ProfileFragment.FOLLOW)
//
//                }

                val go = Intent(ctx, FollowActivity::class.java)
//                if (comment.blockMe == "0")
//                    go.putExtra(FollowActivity.TYPE, FollowActivity.PROFIL_T)
//                else
//                    go.putExtra(FollowActivity.TYPE, FollowActivity.BLOCKED_ME)
//                go.putExtra("close",comment.close)
//                go.putExtras(bundle)
//                js.put("close",comment.close)
                ctx.startActivity(go)


//            connectActivity!!.goNext(Const.PROFIL_PAGE_OTHER,js.toString())

            }else{
                val go = Intent(ctx, FollowActivity::class.java)
                bundle.putString(ProfileFragment.F_TYPE, ProfileFragment.SETTINGS)


                go.putExtra(FollowActivity.TYPE, FollowActivity.PROFIL_T)

//                go.putExtra("close",comment.close)
//                go.putExtra("blockMe",comment.blockMe)
//                go.putExtra("blockIt",comment.blockIt)

                go.putExtras(bundle)
//                js.put("close",comment.close)
                ctx.startActivity(go)
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): Holder {
        return Holder(inflater.inflate(R.layout.res_comment_box,p0,false))
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

    fun  swapList(list:ArrayList<Comment>){
        comments = list
        notifyDataSetChanged()
    }

    fun swapLast(list: ArrayList<Comment>) {
        val lastItemPostition = (comments.size + 1)
        comments.addAll(list)
        notifyItemRangeInserted(lastItemPostition,list.size)
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
