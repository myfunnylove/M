package locidnet.com.marvarid.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import locidnet.com.marvarid.resources.adapterAnim.AnimateViewHolder
import locidnet.com.marvarid.resources.customviews.CustomManager
import locidnet.com.marvarid.resources.expandableTextView.ExpandableTextView
import locidnet.com.marvarid.resources.hashtag.HashTagHelper
import locidnet.com.marvarid.resources.utils.*
import locidnet.com.marvarid.ui.activity.CommentActivity
import locidnet.com.marvarid.ui.activity.MainActivity
import locidnet.com.marvarid.ui.activity.SearchByTagActivity
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
                    profilOrFeed:Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {




    var ctx                   = context
    var feeds                 = feedsMap
    var inflater              = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var clicker               = adapterClicker
    private val like                  = R.drawable.like_select
    private val unLike                = R.drawable.like
    val model                 = Model()
    private val pOrF                  = profilOrFeed
    var user                  = Base.get.prefs.getUser()
    private var lastAnimationPosition = -1
    var activity:FragmentActivity?    = context
    private var disableAnimation      = false
    var changeId              = -1
    val player                = musicPlayerListener
    private val TYPE_POST             = 0
    private val TYPE_AD               = 1

    companion object {

        val ANIMATED_ITEM_COUNT        = 0
        val likeAnimations             = HashMap<RecyclerView.ViewHolder,AnimatorSet>()
    }


//    private fun View.runEnterAnimation(position:Int){
//        if(disableAnimation || position < lastAnimationPosition){
//            return
//        }
//
//
//        if (position > ANIMATED_ITEM_COUNT){
//            lastAnimationPosition = position
//            this.translationY =Functions.getScreenHeight(ctx).toFloat()
//            this.animate()
//                    .translationY(0f)
//                    .setInterpolator(DecelerateInterpolator(3f))
//                    .setDuration(700)
//                    .start()
//        }
//    }

    override fun getItemCount(): Int = feeds.posts.size


    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int =
            if(feeds.posts[position].type == "ad") TYPE_AD else TYPE_POST

    override fun onCreateViewHolder(p0: ViewGroup?, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                TYPE_POST -> Holder(inflater.inflate(R.layout.res_feed_block_image, p0!!, false))
                TYPE_AD -> Holder(inflater.inflate(R.layout.res_feed_block_ad, p0!!, false))
                else -> Holder(inflater.inflate(R.layout.res_feed_block_image, p0!!, false))
            }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, @SuppressLint("RecyclerView") i: Int) {

        val type = getItemViewType(i)
        if (type == TYPE_POST){
            holder!!.itemView

            val h = holder as Holder
            val post = feeds.posts[i]
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


            val icon = if (post.like == "0")
                VectorDrawableCompat.create(Base.get.resources, unLike, h.likeIcon.context.theme)
            else
                VectorDrawableCompat.create(Base.get.resources, like, h.likeIcon.context.theme)

            h.likeIcon.setImageDrawable(icon)

            h.likeCount.setCurrentText(post.likes)



            if (likeAnimations.containsKey(h)){
                likeAnimations[h]!!.cancel()
            }

            likeAnimations.remove(h)

//            h.commentCount.text = post.comments


            if(changeId == i){

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
//                  js.put("user_id", profile.userId )
//                  js.put("session", profile.session)
                    log.d ("changequote send data $js")

                    model.responseCall(Http.getRequestData(js, Http.CMDS.CHANGE_POST))
                            .enqueue(object : Callback<ResponseData>{
                                override fun onResponse(p0: Call<ResponseData>?, response: Response<ResponseData>?) {
                                    try{
                                        log.d("result change quote success $response")
                                        log.d("result change quote success ${response!!.body()}")
                                        log.d("result after changed ${feeds.posts[changeId]}")
                                        if (response.body()!!.res == "0"){
                                            feeds.posts[changeId].quote.text = h.quoteEdit.text.toString()
                                            val newChange = changeId
                                            changeId = -1
                                            notifyItemChanged(newChange)
                                            MainActivity.MY_POSTS_STATUS = MainActivity.NEED_UPDATE

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
                h.quoteEdit.visibility = View.GONE
                h.quoteEdit.clearComposingText()
                h.sendChange.visibility = View.GONE
                h.quote.text           = post.quote.text

                val hashTag = HashTagHelper.Creator.create(
                        Base.get.resources.getColor(R.color.material_pink_300),
                        object : HashTagHelper.OnHashTagClickListener{
                            override fun onHashTagClicked(hashTag: String?) {
                                var intent:Intent? = Intent(ctx,SearchByTagActivity::class.java)
                                intent!!.putExtra("tag",hashTag!!)
                                ctx.startActivity(intent)
                                intent = null
                            }

                        })
                hashTag.handle(h.quote.getmTv())
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
                        h.quote.setTextSize(post.quote.textSize.toFloat())
                    } catch (e: Exception) {
                    }
                }
                try {

                    h.quote.setTextColor(ContextCompat.getColor(Base.get, Const.colorPalette[post.quote.textColor.toInt()]!!.drawable))

                } catch (e: Exception) {

                }



                if (post.images.size > 0) {


                    h.images.visibility = View.VISIBLE

                    val span: Int = if ((post.images.size > 1)) {
                        if (post.images.size == 2) {
                            2
                        } else {
                            (post.images.size - 1)
                        }
                    } else {
                        1
                    }

                    val manager = CustomManager(ctx, span)
                    val adapter = PostPhotoGridAdapter(ctx, post.images)

                    manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(i: Int): Int {
                            return if (i == 0) {
                                if (post.images.size == 2)
                                    1
                                else
                                    (manager.spanCount)
                            } else 1
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
                    post.audios.forEach {
                        audio ->
                        audio.middlePath = audio.middlePath.replace(Const.AUDIO.MEDIUM, Prefs.Builder().audioRes())

                    }
                    val adapter = PostAudioGridAdapter(ctx, post.audios,object :MusicPlayerListener{
                        override fun playClick(listSong: ArrayList<Audio>, position: Int) {
                                player.playClick(listSong,position)


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
                    h.line.visibility = View.GONE
                    h.audios.visibility = View.GONE
                }


                h.likeLay.setOnClickListener {
                    if (feeds.posts[i].like == "0") {

                        feeds.posts[i].like = "1"
                        feeds.posts[i].likes = (feeds.posts[i].likes.toInt() + 1).toString()
                        h.likeIcon.setImageDrawable(VectorDrawableCompat.create(Base.get.resources, like, h.likeIcon.context.theme))
                    } else {
                        feeds.posts[i].likes = (feeds.posts[i].likes.toInt() - 1).toString()

                        feeds.posts[i].like = "0"
                        h.likeIcon.setImageDrawable(VectorDrawableCompat.create(Base.get.resources, unLike, h.likeIcon.context.theme))

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
                    goCommentActivity.putExtra("postUsername",post.user.username)
                    goCommentActivity.putExtra("postUserPhoto",post.user.photo)
                    goCommentActivity.putExtra("postQuoteText",post.quote.text)
                    goCommentActivity.putExtra("postQuoteColor",post.quote.textColor)
                    goCommentActivity.putExtra("postQuoteSize",post.quote.textSize)

                    val startingLocation = IntArray(2)
                    h.commentLay.getLocationOnScreen(startingLocation)
                    goCommentActivity.putExtra(CommentActivity.LOCATION, startingLocation[1])
                    if (activity != null){
                        MainActivity.COMMENT_POST_UPDATE = holder.adapterPosition
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


                                            feeds.posts.removeAt(holder.adapterPosition)
                                            MainActivity.FEED_STATUS = MainActivity.NEED_UPDATE
                                            MainActivity.MY_POSTS_STATUS = MainActivity.NEED_UPDATE
                                            MainActivity.startFeed = 0
                                            MainActivity.endFeed = 10
                                            MainActivity.start = 0
                                            MainActivity.end = 10
                                            notifyItemRemoved(holder.adapterPosition)
                                            notifyItemRangeChanged(holder.adapterPosition, feeds.posts.size)
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
                                    changeId = holder.adapterPosition
                                    notifyItemChanged(holder.adapterPosition)
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


    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {

    }




    fun swapFirstItem(postList: PostList){
        disableAnimation = false
        feeds.posts.add(0, postList.posts[0])
        notifyDataSetChanged()
    }
    fun swapLast20Item(postList: PostList){
        disableAnimation = false

        val lastItemPostition = (feeds.posts.size + 1)
        feeds.posts.addAll(postList.posts)
        notifyItemRangeInserted(lastItemPostition,postList.posts.size)
    }
    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView), AnimateViewHolder {


        var images        by Delegates.notNull<RecyclerView>()
        var line          by Delegates.notNull<View>()
        var audios        by Delegates.notNull<RecyclerView>()
        var avatar        by Delegates.notNull<AppCompatImageView>()
        var name          by Delegates.notNull<TextView>()
        var quote         by Delegates.notNull<ExpandableTextView>()
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
            images       = itemView.findViewById<RecyclerView>(R.id.images)
            line         = itemView.findViewById<View>(R.id.line)
            audios       = itemView.findViewById<RecyclerView>(R.id.audios)
            avatar       = itemView.findViewById<AppCompatImageView>(R.id.avatar)
            name         = itemView.findViewById<TextView>(R.id.name)
            quote        = itemView.findViewById<ExpandableTextView>(R.id.expand_text_view)
            quoteEdit    = itemView.findViewById<EditText>(R.id.commentEditText)
            likeCount    = itemView.findViewById<TextSwitcher>(R.id.likeCount)
            commentCount = itemView.findViewById<TextView>(R.id.commentCount)
            time         = itemView.findViewById<TextView>(R.id.time)
            username     = itemView.findViewById<TextView>(R.id.username)
            likeIcon     = itemView.findViewById<AppCompatImageView>(R.id.likeIcon)
            popup        = itemView.findViewById<AppCompatImageView>(R.id.popup)
            likeLay      = itemView.findViewById<LinearLayout>(R.id.likeLay)
            commentLay   = itemView.findViewById<LinearLayout>(R.id.commentLay)
            topContainer = itemView.findViewById<ViewGroup>(R.id.topContainer)
            sendChange   = itemView.findViewById<AppCompatImageButton>(R.id.sendChangedQuote)
        }

        override fun preAnimateAddImpl(holder: RecyclerView.ViewHolder?) {
            ViewCompat.setTranslationY(itemView, -itemView.getHeight() * 0.3f);
            ViewCompat.setAlpha(itemView, 0f);
        }

        override fun preAnimateRemoveImpl(holder: RecyclerView.ViewHolder?) {
        }

        override fun animateAddImpl(holder: RecyclerView.ViewHolder?, listener: ViewPropertyAnimatorListener?) {
            ViewCompat.animate(itemView)
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(300)
                    .setListener(listener)
                    .start();
        }

        override fun animateRemoveImpl(holder: RecyclerView.ViewHolder?, listener: ViewPropertyAnimatorListener?) {
            ViewCompat.animate(itemView)
                    .translationY(-itemView.getHeight() * 0.1f)
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(listener)
                    .start();
        }
    }




    private fun Holder.updateLikesCounter(animated:Boolean){
        val currentLikesCount  = feeds.posts[this.adapterPosition].likes.toInt()
        if (animated){
            this.likeCount.setText(currentLikesCount.toString())
        }else{
            this.likeCount.setCurrentText(currentLikesCount.toString())
        }


    }

}