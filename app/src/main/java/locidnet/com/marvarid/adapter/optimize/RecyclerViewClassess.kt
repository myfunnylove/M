package locidnet.com.marvarid.adapter.optimize

import android.content.Intent
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.MyFeedAdapter
import locidnet.com.marvarid.adapter.ProfileFeedAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.connectors.MusicPlayerListener
import locidnet.com.marvarid.model.*
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.resources.customviews.CustomManager
import locidnet.com.marvarid.resources.hashtag.HashTagHelper
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.JS
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.resources.zoomimageview.adapter.ViewHolder
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.ui.activity.CommentActivity
import locidnet.com.marvarid.ui.activity.MainActivity
import locidnet.com.marvarid.ui.activity.SearchActivity
import locidnet.com.marvarid.ui.activity.SearchByTagActivity
import locidnet.com.marvarid.ui.dialogs.ComplaintsFragment
import locidnet.com.marvarid.ui.fragment.FeedFragment
import locidnet.com.marvarid.ui.fragment.MyProfileFragment
import locidnet.com.marvarid.ui.fragment.ProfileFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference

/**
 * Created by myfunnylove on 05.11.2017.
 */
class HashtagGenerator(context: FragmentActivity) : HashTagHelper.OnHashTagClickListener{
    val ctx: WeakReference<FragmentActivity> = WeakReference<FragmentActivity>(context)
    override fun onHashTagClicked(hashTag: String?) {

        if (ctx.get() != null){
            var intent: Intent? = Intent(ctx.get()!!, SearchByTagActivity::class.java)
            intent!!.putExtra("tag",hashTag!!)
            ctx.get()!!.startActivity(intent)
            intent = null
        }
    }
    override fun onLoginClicked(login: String?) {

        var intent: Intent? = Intent(ctx.get()!!, SearchActivity::class.java)
        intent!!.putExtra("login",login!!)
        ctx.get()!!.startActivity(intent)
        intent = null
    }

}



class SendChangePost(val feeds: PostList, holder: MyFeedAdapter.Holder, val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) : Callback<ResponseData> {
    val h: WeakReference<MyFeedAdapter.Holder> = WeakReference<MyFeedAdapter.Holder>(holder)
    override fun onResponse(p0: Call<ResponseData>?, response: Response<ResponseData>?) {
        if (h.get()!=null){
            try{
                log.d("result change quote success $response")
                log.d("result change quote success ${response!!.body()}")
                log.d("result after changed ${feeds.posts[MyFeedAdapter.changeId]}")
                if (response.body()!!.res == "0"){
                    feeds.posts[MyFeedAdapter.changeId].quote.text = h.get()!!.quoteEdit.text.toString()
                    val newChange = MyFeedAdapter.changeId
                    MyFeedAdapter.changeId = -1
                    adapter.notifyItemChanged(newChange)
                    MainActivity.MY_POSTS_STATUS = MainActivity.NEED_UPDATE

                }
            }catch (e :Exception){

            }
        }

    }

    override fun onFailure(p0: Call<ResponseData>?, p1: Throwable?) {

        log.d("result change quote failer $p1")
    }
}

class SendChangePostProfile(val feeds: PostList, holder: ProfileFeedAdapter.Holder, val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) : Callback<ResponseData> {
    val h: WeakReference<ProfileFeedAdapter.Holder> = WeakReference<ProfileFeedAdapter.Holder>(holder)
    override fun onResponse(p0: Call<ResponseData>?, response: Response<ResponseData>?) {
        if (h.get()!=null){
            try{
                log.d("result change quote success $response")
                log.d("result change quote success ${response!!.body()}")
                log.d("result after changed ${feeds.posts[MyFeedAdapter.changeId]}")
                if (response.body()!!.res == "0"){
                    feeds.posts[ProfileFeedAdapter.changeId].quote.text = h.get()!!.quoteEdit.text.toString()
                    val newChange = ProfileFeedAdapter.changeId
                    ProfileFeedAdapter.changeId = -1
                    adapter.notifyItemChanged(newChange)
                    MainActivity.MY_POSTS_STATUS = MainActivity.NEED_UPDATE

                }
            }catch (e :Exception){

            }
        }

    }

    override fun onFailure(p0: Call<ResponseData>?, p1: Throwable?) {

        log.d("result change quote failer $p1")
    }
}


class SpanClass(val post: Posts, m: CustomManager) : GridLayoutManager.SpanSizeLookup(){
    val manager: WeakReference<CustomManager> = WeakReference<CustomManager>(m)
    override fun getSpanSize(i: Int): Int {
        return if (i == 0) {
            if (post.images.size == 2)
                1
            else
                (manager.get()!!.spanCount)
        } else 1
    }

}


class LikeBosish : Callback<ResponseData> {
    override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
        log.d("follow on fail $t")
    }

    override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {


    }
}

class Jaloba : Callback<ResponseData> {
    override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
        log.e("complaint fail $t")

    }

    override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {
        log.d("complaint success ${response!!.body()}")

        Toast.makeText(Base.get, Base.get.resources.getString(R.string.thank_data_sent), Toast.LENGTH_SHORT).show()


    }
}

class MusicPlayer(p: MusicPlayerListener, val i:Int) : MusicPlayerListener {
    val player: WeakReference<MusicPlayerListener> = WeakReference<MusicPlayerListener>(p)
    override fun playClick(listSong: ArrayList<Audio>, position: Int) {
        player.get()!!.playClick(listSong,position)


        if (FeedFragment.playedSongPosition != -1 ){
            log.d("position $i => ${FeedFragment.playedSongPosition} $position")

            try{
                FeedFragment.cachedSongAdapters!![FeedFragment.playedSongPosition]!!.notifyDataSetChanged()
            }catch (e:Exception){}

            FeedFragment.cachedSongAdapters!![i]!!.notifyDataSetChanged()

        }else{
            FeedFragment.cachedSongAdapters!![i]!!.notifyDataSetChanged()

        }

        FeedFragment.playedSongPosition = i
    }

}

class ProfileMusicPlayer(p: MusicPlayerListener, val i:Int,val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,val userInfo: UserInfo?) : MusicPlayerListener{
    val player: WeakReference<MusicPlayerListener> = WeakReference<MusicPlayerListener>(p)
    var myProfil = Base.get.prefs.getUser()

    override fun playClick(listSong: ArrayList<Audio>, position: Int) {
        player.get()!!.playClick(listSong, position)
        if (myProfil.userId == userInfo!!.user.info.user_id) {
            log.d("played song position ${MyProfileFragment.playedSongPosition != -1}")
            if (MyProfileFragment.playedSongPosition != -1) {
                log.d("position $i => ${MyProfileFragment.playedSongPosition} $position")

                try {
                    MyProfileFragment.cachedSongAdapters!!.get(MyProfileFragment.playedSongPosition)!!.notifyDataSetChanged()
                } catch (e: Exception) {
                    log.d("position $e")

                }
                MyProfileFragment.cachedSongAdapters!!.get(i)!!.notifyDataSetChanged()

            } else {
                log.d("position $i => ${MyProfileFragment.cachedSongAdapters!!.get(i)} $position")
                MyProfileFragment.cachedSongAdapters!!.get(i)!!.notifyDataSetChanged()

            }
            adapter.notifyItemChanged(0)
            MyProfileFragment.playedSongPosition = i
        }else{
            if (ProfileFragment.playedSongPosition != -1) {
                log.d("position $i => ${ProfileFragment.playedSongPosition} $position")

                try {
                    ProfileFragment.cachedSongAdapters!!.get(ProfileFragment.playedSongPosition)!!.notifyDataSetChanged()
                } catch (e: Exception) {
                }
                ProfileFragment.cachedSongAdapters!!.get(i)!!.notifyDataSetChanged()

            } else {
                log.d("position $i => ${ProfileFragment.cachedSongAdapters!!.get(i)} $position")
                ProfileFragment.cachedSongAdapters!!.get(i)!!.notifyDataSetChanged()

            }
//                            notifyItemChanged(0)
            ProfileFragment.playedSongPosition = i
        }
    }

}

class LikeListenClass(val feeds: PostList, val h: MyFeedAdapter.Holder, val model: Model) : View.OnClickListener {
    private val like                  = R.drawable.like_select
    private val unLike                = R.drawable.like

    val holder: WeakReference<MyFeedAdapter.Holder> = WeakReference<MyFeedAdapter.Holder>(h)
    override fun onClick(p0: View?) {

        if (holder.get() != null){
            val post = feeds.posts.get(holder.get()!!.adapterPosition)
            if (feeds.posts[holder.get()!!.adapterPosition].like == "0") {

                feeds.posts[holder.get()!!.adapterPosition].like = "1"
                feeds.posts[holder.get()!!.adapterPosition].likes = (feeds.posts[holder.get()!!.adapterPosition].likes.toInt() + 1).toString()
                holder.get()!!.likeIcon.setImageDrawable(VectorDrawableCompat.create(Base.get.resources, like, holder.get()!!.likeIcon.context.theme))
            } else {
                feeds.posts[holder.get()!!.adapterPosition].likes = (feeds.posts[holder.get()!!.adapterPosition].likes.toInt() - 1).toString()

                feeds.posts[holder.get()!!.adapterPosition].like = "0"
                holder.get()!!.likeIcon.setImageDrawable(VectorDrawableCompat.create(Base.get.resources, unLike, holder.get()!!.likeIcon.context.theme))

            }





            holder.get()!!.updateLikesCounter(true)




            val reqObj = JS.get()

            reqObj.put("post_id", post.id)

            log.d("request data $reqObj")

            model.responseCall(Http.getRequestData(reqObj, Http.CMDS.LIKE_BOSISH))
                    .enqueue(LikeBosish())
        }
    }

    private fun MyFeedAdapter.Holder.updateLikesCounter(animated:Boolean){
        val currentLikesCount  = feeds.posts[this.adapterPosition].likes.toInt()
        if (animated){
            this.likeCount.setText(currentLikesCount.toString())
        }else{
            this.likeCount.setCurrentText(currentLikesCount.toString())
        }


    }

}


class LikeListenClassProfile(val feeds: PostList, val h: ProfileFeedAdapter.Holder, val model: Model) : View.OnClickListener {
    private val like                  = R.drawable.like_select
    private val unLike                = R.drawable.like

    val holder: WeakReference<ProfileFeedAdapter.Holder> = WeakReference<ProfileFeedAdapter.Holder>(h)
    override fun onClick(p0: View?) {

        if (holder.get() != null){
            val post = feeds.posts.get(holder.get()!!.adapterPosition)
            if (feeds.posts[holder.get()!!.adapterPosition].like == "0") {

                feeds.posts[holder.get()!!.adapterPosition].like = "1"
                feeds.posts[holder.get()!!.adapterPosition].likes = (feeds.posts[holder.get()!!.adapterPosition].likes.toInt() + 1).toString()
                holder.get()!!.likeIcon.setImageDrawable(VectorDrawableCompat.create(Base.get.resources, like, holder.get()!!.likeIcon.context.theme))
            } else {
                feeds.posts[holder.get()!!.adapterPosition].likes = (feeds.posts[holder.get()!!.adapterPosition].likes.toInt() - 1).toString()

                feeds.posts[holder.get()!!.adapterPosition].like = "0"
                holder.get()!!.likeIcon.setImageDrawable(VectorDrawableCompat.create(Base.get.resources, unLike, holder.get()!!.likeIcon.context.theme))

            }





            holder.get()!!.updateLikesCounter(true)




            val reqObj = JS.get()

            reqObj.put("post_id", post.id)

            log.d("request data $reqObj")

            model.responseCall(Http.getRequestData(reqObj, Http.CMDS.LIKE_BOSISH))
                    .enqueue(LikeBosish())
        }
    }

    private fun MyFeedAdapter.Holder.updateLikesCounter(animated:Boolean){
        val currentLikesCount  = feeds.posts[this.adapterPosition].likes.toInt()
        if (animated){
            this.likeCount.setText(currentLikesCount.toString())
        }else{
            this.likeCount.setCurrentText(currentLikesCount.toString())
        }


    }
    private fun ProfileFeedAdapter.Holder.updateLikesCounter(animated:Boolean){
        val currentLikesCount  = feeds.posts[this.adapterPosition].likes.toInt()
        if (animated){
            this.likeCount.setText(currentLikesCount.toString())
        }else{
            this.likeCount.setCurrentText(currentLikesCount.toString())
        }


    }
}


class CommentClassProfileAdapter(ctx: FragmentActivity, val feeds: PostList, h: ProfileFeedAdapter.Holder) : View.OnClickListener {
    val holder: WeakReference<ProfileFeedAdapter.Holder> = WeakReference<ProfileFeedAdapter.Holder>(h)
    val activity: WeakReference<FragmentActivity> = WeakReference<FragmentActivity>(ctx)
    override fun onClick(p0: View?) {
        if (activity.get() != null && holder.get() != null) {
            val post = feeds.posts.get(holder.get()!!.adapterPosition)
            val goCommentActivity = Intent(activity.get()!!, CommentActivity::class.java)

            goCommentActivity.putExtra("postId", post.id.toInt())
            goCommentActivity.putExtra("postUsername", post.user.username)
            goCommentActivity.putExtra("postUserPhoto", post.user.photo)
            goCommentActivity.putExtra("postQuoteText", post.quote.text)
            goCommentActivity.putExtra("postQuoteColor", post.quote.textColor)
            goCommentActivity.putExtra("postQuoteSize", post.quote.textSize)

            val startingLocation = IntArray(2)
            holder.get()!!.commentLay.getLocationOnScreen(startingLocation)
            goCommentActivity.putExtra(CommentActivity.LOCATION, startingLocation[1])
            MainActivity.COMMENT_POST_UPDATE = holder.get()!!.adapterPosition
            activity.get()!!.startActivityForResult(goCommentActivity, Const.GO_COMMENT_ACTIVITY)
            activity.get()!!.overridePendingTransition(0, 0)
        }
    }
}
class CommentClass(ctx: FragmentActivity, val feeds: PostList, h: MyFeedAdapter.Holder) : View.OnClickListener {
    val holder: WeakReference<MyFeedAdapter.Holder> = WeakReference<MyFeedAdapter.Holder>(h)
    val activity: WeakReference<FragmentActivity> = WeakReference<FragmentActivity>(ctx)
    override fun onClick(p0: View?) {
        if (activity.get() != null && holder.get() != null) {
            val post = feeds.posts.get(holder.get()!!.adapterPosition)
            val goCommentActivity = Intent(activity.get()!!, CommentActivity::class.java)

            goCommentActivity.putExtra("postId", post.id.toInt())
            goCommentActivity.putExtra("postUsername", post.user.username)
            goCommentActivity.putExtra("postUserPhoto", post.user.photo)
            goCommentActivity.putExtra("postQuoteText", post.quote.text)
            goCommentActivity.putExtra("postQuoteColor", post.quote.textColor)
            goCommentActivity.putExtra("postQuoteSize", post.quote.textSize)

            val startingLocation = IntArray(2)
            holder.get()!!.commentLay.getLocationOnScreen(startingLocation)
            goCommentActivity.putExtra(CommentActivity.LOCATION, startingLocation[1])
            MainActivity.COMMENT_POST_UPDATE = holder.get()!!.adapterPosition
            activity.get()!!.startActivityForResult(goCommentActivity, Const.GO_COMMENT_ACTIVITY)
            activity.get()!!.overridePendingTransition(0, 0)
        }
    }
}


class PopupClass(context: FragmentActivity, h: MyFeedAdapter.Holder, val feeds: PostList, val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>, val model: Model) : View.OnClickListener{
    val ctx: WeakReference<FragmentActivity> = WeakReference<FragmentActivity>(context)
    val holder: WeakReference<MyFeedAdapter.Holder> = WeakReference<MyFeedAdapter.Holder>(h)
    var user                  = Base.get.prefs.getUser()

    override fun onClick(p0: View?) {
        val popup = PopupMenu(ctx.get()!!, holder.get()!!.popup)
        val post = feeds.posts.get(holder.get()!!.adapterPosition)
        if (user.userId != post.user.userId) {

            popup.inflate(R.menu.menu_feed)
        } else {
            popup.inflate(R.menu.menu_own_feed)

        }
        popup.show()
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete -> {
                    val reqObj = JS.get()

                    reqObj.put("post_id", post.id)

                    log.d("request data for delete post $reqObj")

                    model.responseCall(Http.getRequestData(reqObj, Http.CMDS.DELETE_POST))
                            .enqueue(DeletePost(holder.get()!!,feeds,adapter))
                }

                R.id.change -> {

                    if (MyFeedAdapter.changeId == -1) {
                        MyFeedAdapter.changeId = holder.get()!!.adapterPosition
                        adapter.notifyItemChanged(holder.get()!!.adapterPosition)
                    }
                }

                R.id.report -> {

                    val dialog = ComplaintsFragment.instance()

                    dialog.setDialogClickListener(object : ComplaintsFragment.DialogClickListener {
                        override fun click(whichButton: Int) {
                            val js = JS.get()
                            js.put("type", whichButton)
                            js.put("post", post.id)

                            model.responseCall(Http.getRequestData(js, Http.CMDS.COMPLAINTS))
                                    .enqueue(Jaloba())
                            dialog.dismiss()
                        }
                    })
                    dialog.show(ctx.get()!!.supportFragmentManager, "TAG")

                }
            }
            false
        }
    }
    class DeletePost(h: MyFeedAdapter.Holder, val feeds: PostList, val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>): Callback<ResponseData> {
        val holder: WeakReference<MyFeedAdapter.Holder> = WeakReference<MyFeedAdapter.Holder>(h)

        override fun onResponse(p0: Call<ResponseData>?, p1: Response<ResponseData>?) {
            try {


                feeds.posts.removeAt(holder.get()!!.adapterPosition)
                MainActivity.FEED_STATUS = MainActivity.NEED_UPDATE
                MainActivity.MY_POSTS_STATUS = MainActivity.NEED_UPDATE
                MainActivity.startFeed = 0
                MainActivity.endFeed = 10
                MainActivity.start = 0
                MainActivity.end = 10
                adapter.notifyItemRemoved(holder.get()!!.adapterPosition)
                adapter.notifyItemRangeChanged(holder.get()!!.adapterPosition, feeds.posts.size)
                adapter.notifyItemChanged(0)

                log.d("onresponse from delete post $p1")
            } catch (e: Exception) {

            }
        }

        override fun onFailure(p0: Call<ResponseData>?, p1: Throwable?) {
            log.d("onfail from delete post $p1")
        }
    }
}
class PopupClassProfile(context: FragmentActivity, h: ProfileFeedAdapter.Holder, val feeds: PostList, val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>, val model: Model) : View.OnClickListener{
    val ctx: WeakReference<FragmentActivity> = WeakReference<FragmentActivity>(context)
    val holder: WeakReference<ProfileFeedAdapter.Holder> = WeakReference<ProfileFeedAdapter.Holder>(h)
    var user                  = Base.get.prefs.getUser()

    override fun onClick(p0: View?) {
        val popup = PopupMenu(ctx.get()!!, holder.get()!!.popup)
        val post = feeds.posts.get(holder.get()!!.adapterPosition)
        if (user.userId != post.user.userId) {

            popup.inflate(R.menu.menu_feed)
        } else {
            popup.inflate(R.menu.menu_own_feed)

        }
        popup.show()
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete -> {
                    val reqObj = JS.get()

                    reqObj.put("post_id", post.id)

                    log.d("request data for delete post $reqObj")

                    model.responseCall(Http.getRequestData(reqObj, Http.CMDS.DELETE_POST))
                            .enqueue(DeletePost(holder.get()!!,feeds,adapter))
                }

                R.id.change -> {

                    if (MyFeedAdapter.changeId == -1) {
                        MyFeedAdapter.changeId = holder.get()!!.adapterPosition
                        adapter.notifyItemChanged(holder.get()!!.adapterPosition)
                    }
                }

                R.id.report -> {

                    val dialog = ComplaintsFragment.instance()

                    dialog.setDialogClickListener(object : ComplaintsFragment.DialogClickListener {
                        override fun click(whichButton: Int) {
                            val js = JS.get()
                            js.put("type", whichButton)
                            js.put("post", post.id)

                            model.responseCall(Http.getRequestData(js, Http.CMDS.COMPLAINTS))
                                    .enqueue(Jaloba())
                            dialog.dismiss()
                        }
                    })
                    dialog.show(ctx.get()!!.supportFragmentManager, "TAG")

                }
            }
            false
        }
    }
    class DeletePost(h: ProfileFeedAdapter.Holder, val feeds: PostList, val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>): Callback<ResponseData> {
        val holder: WeakReference<ProfileFeedAdapter.Holder> = WeakReference<ProfileFeedAdapter.Holder>(h)

        override fun onResponse(p0: Call<ResponseData>?, p1: Response<ResponseData>?) {
            try {


                feeds.posts.removeAt(holder.get()!!.adapterPosition)
                MainActivity.FEED_STATUS = MainActivity.NEED_UPDATE
                MainActivity.MY_POSTS_STATUS = MainActivity.NEED_UPDATE
                MainActivity.startFeed = 0
                MainActivity.endFeed = 10
                MainActivity.start = 0
                MainActivity.end = 10
                adapter.notifyItemRemoved(holder.get()!!.adapterPosition)
                adapter.notifyItemRangeChanged(holder.get()!!.adapterPosition, feeds.posts.size)
                adapter.notifyItemChanged(0)

                log.d("onresponse from delete post $p1")
            } catch (e: Exception) {

            }
        }

        override fun onFailure(p0: Call<ResponseData>?, p1: Throwable?) {
            log.d("onfail from delete post $p1")
        }
    }
}