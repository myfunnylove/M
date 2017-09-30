package locidnet.com.marvarid.adapter

import android.content.Context
import android.content.Intent
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.*
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.nineoldandroids.animation.AnimatorSet

import org.json.JSONObject
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.connectors.MusicPlayerListener
import locidnet.com.marvarid.model.*
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.resources.customviews.CustomManager
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Functions
import locidnet.com.marvarid.resources.utils.JS
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.CommentActivity
import locidnet.com.marvarid.ui.activity.MainActivity
import locidnet.com.marvarid.ui.dialogs.ComplaintsFragment
import locidnet.com.marvarid.ui.fragment.*
import org.ocpsoft.prettytime.PrettyTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.properties.Delegates


class MyFeedAdapter(context: FragmentActivity,
                    feedsMap: PostList,

                    adapterClicker: AdapterClicker,
                    musicPlayerListener: MusicPlayerListener,
                    profilOrFeed:Boolean = false,
                    followType:String = "",
                    val postUser: PostUser? = null,
                    closedProfil:Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var ctx                   = context
    var feeds                 = feedsMap
    var inflater              = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var clicker               = adapterClicker
    val like                  = R.drawable.like_select
    val unLike                = R.drawable.like
    var profile               = Base.get.prefs.getUser()
    val model                 = Model()
    val pOrF                  = profilOrFeed
    var FOLLOW_TYPE           = followType
    var user                  = Base.get.prefs.getUser()
    var lastAnimationPosition = -1
    var itemsCount            = 0
    var activity:FragmentActivity?    = context
    var disableAnimation      = false
    var cachedLists           = HashMap<String,String>()
    var changeId              = -1
    val player                = musicPlayerListener
    var closedProfile         = closedProfil


    companion object {

        val ANIMATED_ITEM_COUNT        = 0
        val HEADER                     = 1
        val BODY                       = 2
        val ACTION_LIKE_BUTTON_CLICKED = "action_like_button_button"
        val ACTION_LIKE_IMAGE_CLICKED  = "action_like_image_button"
        val VIEW_TYPE_DEFAULT          = 1
        val VIEW_TYPE_LOADER           = 2
        val ACCELERATE_INTERPOLATOR    = AccelerateInterpolator()
        val OVERSHOOT_INTERPOLATOR     = OvershootInterpolator(4f)
        val likeAnimations             = HashMap<RecyclerView.ViewHolder,AnimatorSet>()
        var avatarUpdated              = -1
        var SHOW_PROGRESS              = 1
        var HIDE_PROGRESS              = 0
        var CANCEL_PROGRESS            = 2
    }


    fun View.runEnterAnimation(position:Int){
        if(disableAnimation || position < lastAnimationPosition){
            return
        }


        if (position > ANIMATED_ITEM_COUNT){
            lastAnimationPosition = position
            this.translationY =Functions.getScreenHeight(ctx).toFloat()
            this.animate()
                    .translationY(0f)
                    .setInterpolator(DecelerateInterpolator(3f))
                    .setDuration(700)
                    .start()
        }
    }

    override fun getItemCount(): Int = feeds.posts.size


    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(p0: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {

            return  Holder(inflater.inflate(R.layout.res_feed_block_image, p0!!, false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, i: Int) {

        val type = getItemViewType(i)
        holder!!.itemView.runEnterAnimation(i)

            val h = holder as Holder
            val post = feeds.posts.get(i)
            log.e("=============== posts count => ${feeds.posts.size}")

            log.wtf("=============== start => ")

            log.wtf("post id:       ${post.id}")
            log.wtf("post audios:   ${post.audios}")
            log.wtf("post images:   ${post.images}")
            log.wtf("post quote:    ${post.quote}")
            log.wtf("post comments: ${post.comments}")
            log.wtf("post like:     ${post.like}")
            log.wtf("post likes:    ${post.likes}")
            log.wtf("post time:     ${post.time}")
            log.wtf("post user:     ${post.user}")
            log.wtf("=============== end ; ")


            val icon: VectorDrawableCompat?
            if (post.like == "0")
                icon = VectorDrawableCompat.create(Base.get.resources, unLike, h.likeIcon.context.theme)
            else
                icon = VectorDrawableCompat.create(Base.get.resources, like, h.likeIcon.context.theme)

            h.likeIcon.setImageDrawable(icon)

            h.likeCount.setCurrentText(post.likes)



            if (likeAnimations.containsKey(h)){
                likeAnimations.get(h)!!.cancel()
            }

            likeAnimations.remove(h);

//            h.commentCount.text = post.comments


            if(pOrF == true && changeId == i){

                h.quote.visibility      = View.GONE
                h.quoteEdit.visibility  = View.VISIBLE
                h.quoteEdit.setText(post.quote.text)
                h.sendChange.visibility = View.VISIBLE
                h.sendChange.setOnClickListener {

                    val quote:Quote = post.quote
                    quote.text = h.quoteEdit.text.toString()

                    val js =  JS.get()
                    js.put("post_id",post.id)
                    js.put("quote", JSONObject(Gson().toJson(quote)))
//                    js.put("user_id", profile.userId )
//                    js.put("session", profile.session)
                    log.d ("changequote send data $js")

                    model.responseCall(Http.getRequestData(js, Http.CMDS.CHANGE_POST))
                            .enqueue(object : Callback<ResponseData>{
                                override fun onResponse(p0: Call<ResponseData>?, response: Response<ResponseData>?) {
                                    try{
                                        log.d("result change quote success $response")
                                        log.d("result change quote success ${response!!.body()}")
                                        log.d("result after changed ${feeds.posts.get(changeId)}")
                                        if (response.body()!!.res == "0"){
                                            feeds.posts.get(changeId).quote.text = h.quoteEdit.text.toString()
                                            val newChange = changeId
                                            changeId = -1
                                            notifyItemChanged(newChange)
                                        }
                                    }catch (e :Exception){

                                    }

                                }

                                override fun onFailure(p0: Call<ResponseData>?, p1: Throwable?) {

                                    log.d("result change quote failer $p1")
                                }

                            })
                }
            }else{

                h.quote.visibility     = View.VISIBLE
                h.quote.text           = post.quote.text
                h.quoteEdit.visibility = View.GONE
                h.quoteEdit.clearComposingText()
                h.sendChange.visibility = View.GONE

            }


        Glide.with(ctx)
                .load(Functions.checkImageUrl(post.user.photo))
                .apply(Functions.getGlideOpts())
                .into(h.avatar)


        if (h.quote.tag == null || h.quote.tag != post.id) {

                h.quote.tag = post.id

                h.username.text = post.user.username
                //TODO

//                if (!post.user..isNullOrEmpty()){
//                    h.name.visibility = View.VISIBLE
//                    h.name.text = userInfo!!.user.info.name
//
//                }else{
//                    h.name.visibility = View.GONE
//
//                }
                val prettyTime = PrettyTime()
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val date2 = formatter.parse(post.time) as Date



                h.time.text = prettyTime.format(date2)



                if (post.quote.textSize != "") {
                    try {
                        h.quote.textSize = post.quote.textSize.toFloat()
                    } catch (e: Exception) {
                    }
                }
                try {

                    h.quote.setTextColor(ContextCompat.getColor(Base.get, Const.colorPalette.get(post.quote.textColor.toInt())!!.drawable))

                } catch (e: Exception) {

                }



                if (post.images.size > 0) {


                    h.images.visibility = View.VISIBLE

                    var span = (post.images.size - 1)

                    if ((post.images.size > 1)) {
                        if (post.images.size == 2) {
                            span = 2
                        } else {
                            span = (post.images.size - 1)
                        }
                    } else {
                        span = 1
                    }

                    val manager = CustomManager(ctx, span)
                    val adapter = PostPhotoGridAdapter(ctx, post.images)

                    manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(i: Int): Int {
                            if (i == 0) {
                                if (post.images.size == 2)
                                    return 1
                                else
                                    return (manager.spanCount)
                            } else return 1
                        }

                    }

                    h.images.layoutManager = manager
                    h.images.setHasFixedSize(true)
                    h.images.adapter = adapter




                } else {
                    h.images.visibility = View.GONE
                }

                if (post.audios.size > 0) {
                    h.audios.visibility = View.VISIBLE

                    val span = 1


                    val manager = CustomManager(ctx, span)
                    val adapter = PostAudioGridAdapter(ctx, post.audios,object :MusicPlayerListener{
                        override fun playClick(listSong: ArrayList<Audio>, position: Int) {
                            try{
                                player.playClick(listSong,position)


                                if (FeedFragment.playedSongPosition != -1 ){
                                    log.d("position $i => ${FeedFragment.playedSongPosition} $position")

                                    FeedFragment.cachedSongAdapters!!.get(FeedFragment.playedSongPosition)!!.notifyDataSetChanged()

                                    FeedFragment.cachedSongAdapters!!.get(i)!!.notifyDataSetChanged()

                                }else{
                                    log.d("position $i => ${FeedFragment.cachedSongAdapters!!.get(i)} $position")
                                    FeedFragment.cachedSongAdapters!!.get(i)!!.notifyDataSetChanged()

                                }

                                FeedFragment.playedSongPosition = i
                            }catch (e :Exception){
                                log.d("null 1 ${e}")

                            }

                        }

                    },model)
                    if (FeedFragment.cachedSongAdapters != null){
                        FeedFragment.cachedSongAdapters!!.put(i,adapter)
                    }else{
                        FeedFragment.cachedSongAdapters = HashMap()
                        FeedFragment.cachedSongAdapters!!.put(i,adapter)
                    }
//            manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup(){
//                override fun getSpanSize(i: Int): Int {
//                    if (i == 0){
//                        if (post.images.size == 2)
//                            return 1
//                        else
//                            return (manager.spanCount)
//                    } else return 1
//                }
//
//            }

                    h.audios.layoutManager = manager
                    h.audios.setHasFixedSize(true)
                    h.audios.adapter = adapter

                } else {
                    h.audios.visibility = View.GONE
                }


                h.likeLay.setOnClickListener {
                    if (feeds.posts.get(i).like == "0") {

                        feeds.posts.get(i).like = "1"
                        feeds.posts.get(i).likes = (feeds.posts.get(i).likes.toInt() + 1).toString()
                        h.likeIcon.setImageDrawable(VectorDrawableCompat.create(Base.get.resources, like, h.likeIcon.context.theme));
                    } else {
                        feeds.posts.get(i).likes = (feeds.posts.get(i).likes.toInt() - 1).toString()

                        feeds.posts.get(i).like = "0"
                        h.likeIcon.setImageDrawable(VectorDrawableCompat.create(Base.get.resources, unLike, h.likeIcon.context.theme));

                    }


                    disableAnimation = true
//                    notifyDataSetChanged()



                    holder.updateLikesCounter(true)




                    val reqObj = JS.get()

                    reqObj.put("post_id", post.id)

                    log.d("request data $reqObj")

                    model.responseCall(Http.getRequestData(reqObj, Http.CMDS.LIKE_BOSISH))
                            .enqueue(object : retrofit2.Callback<ResponseData> {
                                override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
                                    log.d("follow on fail $t")
                                }

                                override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {
//                                    if (response!!.isSuccessful) {
//                                        log.d("like on response $response")
//                                        log.d("like on response ${response.body()!!.res}")
//                                        log.d("like on response ${Http.getResponseData(response.body()!!.prms)}")
//
//
//                                        try {
//
//                                            val req = JSONObject(Http.getResponseData(response.body()!!.prms))
//                                            if (req.has("likes")) {
//                                                feeds.posts.get(i).likes = req.optString("likes")
//                                                if (feeds.posts.get(i).like == "0") {
//
//                                                    feeds.posts.get(i).like = "1"
//
//                                                } else {
//
//                                                    feeds.posts.get(i).like = "0"
//
//                                                }
//                                                log.d("on refresh ${h.quote.tag == null} ${h.quote.tag != post.id} post ${post.id}")
//                                                disableAnimation = true
//                                                notifyDataSetChanged()
//                                            }
//                                        } catch (e: Exception) {
//
//                                        }
//                                    } else {
//                                        Toast.makeText(Base.get, Base.get.resources.getString(R.string.internet_conn_error), Toast.LENGTH_SHORT).show()
//                                    }

                                }

                            })
                }

                h.commentLay.setOnClickListener {
                    val goCommentActivity = Intent(ctx, CommentActivity::class.java)
                    goCommentActivity.putExtra("postId", post.id.toInt())
                    val startingLocation = IntArray(2)
                    h.commentLay.getLocationOnScreen(startingLocation)
                    goCommentActivity.putExtra(CommentActivity.LOCATION, startingLocation[1])
                    if (activity != null){
                        MainActivity.COMMENT_POST_UPDATE = i
                        activity!!.startActivityForResult(goCommentActivity,Const.GO_COMMENT_ACTIVITY)
                        activity!!.overridePendingTransition(0, 0)
                    }else{
                        ctx.startActivity(goCommentActivity)
                    }
                }
                h.avatar.setOnClickListener{
                    if (!pOrF) clicker.click(i)

                }
                h.topContainer.setOnClickListener {

                    if (!pOrF) clicker.click(i)

                }


                    h.popup.setOnClickListener {
                        val popup = PopupMenu(ctx, h.popup)
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

                                    model.responseCall(Http.getRequestData(reqObj, Http.CMDS.DELETE_POST)).enqueue(object : Callback<ResponseData> {
                                        override fun onResponse(p0: Call<ResponseData>?, p1: Response<ResponseData>?) {
                                            try {


                                                feeds.posts.removeAt(i)
                                                MainActivity.FEED_STATUS = MainActivity.NEED_UPDATE
                                                notifyItemRemoved(i)
                                                notifyItemRangeChanged(i, feeds.posts.size)
                                                notifyItemChanged(0)

                                                log.d("onresponse from delete post $p1")
                                            } catch (e: Exception) {

                                            }
                                        }

                                        override fun onFailure(p0: Call<ResponseData>?, p1: Throwable?) {
                                            log.d("onfail from delete post $p1")
                                        }

                                    })
                                }

                                R.id.change -> {

                                    if (changeId == -1) {
                                        changeId = i
                                        notifyItemChanged(i)
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
                                                    .enqueue(object : retrofit2.Callback<ResponseData> {
                                                        override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
                                                            log.e("complaint fail $t")

                                                        }

                                                        override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {

                                                            log.d("complaint fail ${response!!.body()}")
                                                            Toast.makeText(ctx, ctx.resources.getString(R.string.thank_data_sent), Toast.LENGTH_SHORT).show()
                                                        }

                                                    })
                                            dialog.dismiss()
                                        }
                                    })
                                    dialog.show(activity!!.supportFragmentManager, "TAG")

                                }
                            }
                            false
                        }
                    }

            }


    }


    fun swapPhotoProgress(status:Int){
        avatarUpdated = status
        notifyItemChanged(0)
    }

    fun swapFirstItem(postList: PostList){
        disableAnimation = false
        feeds.posts.add(0,postList.posts.get(0))
        notifyDataSetChanged()
    }
    fun swapLast20Item(postList: PostList){
        disableAnimation = false

        val lastItemPostition = (feeds.posts.size + 1)
        feeds.posts.addAll(postList.posts)
        notifyItemRangeInserted(lastItemPostition,postList.posts.size)
    }
    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        var images        by Delegates.notNull<RecyclerView>()
        var audios        by Delegates.notNull<RecyclerView>()
        var avatar        by Delegates.notNull<AppCompatImageView>()
        var name          by Delegates.notNull<TextView>()
        var quote         by Delegates.notNull<TextView>()
        var quoteEdit     by Delegates.notNull<EditText>()
        var likeCount     by Delegates.notNull<TextSwitcher>()
        var commentCount  by Delegates.notNull<TextView>()
        var time          by Delegates.notNull<TextView>()
        var username      by Delegates.notNull<TextView>()
        var likeIcon      by Delegates.notNull<AppCompatImageView>()
        var popup         by Delegates.notNull<AppCompatImageView>()
        var likeLay       by Delegates.notNull<LinearLayout>()
        var commentLay    by Delegates.notNull<LinearLayout>()
        var topContainer  by Delegates.notNull<ViewGroup>()
        var sendChange    by Delegates.notNull<AppCompatImageButton>()
        init {
            images       = itemView.findViewById(R.id.images)       as RecyclerView
            audios       = itemView.findViewById(R.id.audios)       as RecyclerView
            avatar       = itemView.findViewById(R.id.avatar)       as AppCompatImageView
            name         = itemView.findViewById(R.id.name)         as TextView
            quote        = itemView.findViewById(R.id.commentText)  as TextView
            quoteEdit    = itemView.findViewById(R.id.commentEditText)  as EditText
            likeCount    = itemView.findViewById(R.id.likeCount)    as TextSwitcher
            commentCount = itemView.findViewById(R.id.commentCount) as TextView
            time         = itemView.findViewById(R.id.time)         as TextView
            username     = itemView.findViewById(R.id.username)     as TextView
            likeIcon     = itemView.findViewById(R.id.likeIcon)     as AppCompatImageView
            popup        = itemView.findViewById(R.id.popup)        as AppCompatImageView
            likeLay      = itemView.findViewById(R.id.likeLay)      as LinearLayout
            commentLay   = itemView.findViewById(R.id.commentLay)   as LinearLayout
            topContainer = itemView.findViewById(R.id.topContainer) as ViewGroup
            sendChange   = itemView.findViewById(R.id.sendChangedQuote) as AppCompatImageButton
        }
    }




    fun updateProfilPhoto(path: String) {
        postUser!!.photo = Http.BASE_URL + path
        feeds.posts.forEach { post ->

            post.user = postUser


        }
        avatarUpdated = HIDE_PROGRESS
        notifyDataSetChanged()


    }

    fun Holder.updateLikesCounter(animated:Boolean){
        val currentLikesCount  = feeds.posts.get(this.adapterPosition).likes.toInt()
        if (animated){
            this.likeCount.setText(currentLikesCount.toString())
        }else{
            this.likeCount.setCurrentText(currentLikesCount.toString())
        }


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