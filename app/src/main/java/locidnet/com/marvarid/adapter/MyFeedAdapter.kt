package locidnet.com.marvarid.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.google.gson.Gson
import com.nineoldandroids.animation.AnimatorSet
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import org.json.JSONObject
import locidnet.com.marvarid.R
import locidnet.com.marvarid.R.string.post
import locidnet.com.marvarid.adapter.optimize.*
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
import locidnet.com.marvarid.ui.activity.SearchActivity
import locidnet.com.marvarid.ui.activity.SearchByTagActivity
import locidnet.com.marvarid.ui.dialogs.ComplaintsFragment
import locidnet.com.marvarid.ui.fragment.*
import org.ocpsoft.prettytime.PrettyTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
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

    val model                 = Model()
    private val pOrF                  = profilOrFeed
    var user                  = Base.get.prefs.getUser()
    private var lastAnimationPosition = -1
    var activity:FragmentActivity?    = context
    val player                = musicPlayerListener
    private val TYPE_POST             = 0
    private val TYPE_AD               = 1
    val viewPool  = RecyclerView.RecycledViewPool()
    var cachedTexts:ArrayList<String> = ArrayList()
    companion object {
        var changeId              = -1
        private var disableAnimation      = false

        val ANIMATED_ITEM_COUNT        = 0
        val likeAnimations             = HashMap<RecyclerView.ViewHolder,AnimatorSet>()
    }




    override fun getItemCount(): Int = feeds.posts.size


    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int =
            if(feeds.posts[position].type == "ad") TYPE_AD else TYPE_POST

    override fun onCreateViewHolder(p0: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val holder = Holder(inflater.inflate(R.layout.res_feed_block_image, p0!!, false))
        holder.images.recycledViewPool = viewPool
        holder.audios.recycledViewPool = viewPool
        holder.likeLay.setOnClickListener(LikeListenClass(feeds,holder,model))
        holder.sendChange.setOnClickListener {

            val quote:Quote = feeds.posts.get(holder.adapterPosition).quote
            quote.text = holder.quoteEdit.text.toString()

            val js =  JS.get()
            js.put("post_id",feeds.posts.get(holder.adapterPosition).id)
            js.put("quote", JSONObject(Gson().toJson(quote)))
//                  js.put("user_id", profile.userId )
//                  js.put("session", profile.session)
            log.d ("changequote send data $js")

            model.responseCall(Http.getRequestData(js, Http.CMDS.CHANGE_POST)).enqueue(SendChangePost(feeds,holder,this))
        }


        holder.commentLay.setOnClickListener(CommentClass(ctx,feeds,holder))
        holder.avatar.setOnClickListener{
            if (!pOrF) clicker.click(holder.adapterPosition)

        }
        holder.topContainer.setOnClickListener {

            if (!pOrF) clicker.click(holder.adapterPosition)

        }


        holder.popup.setOnClickListener(PopupClass(ctx,holder,feeds,this,model))
        return holder
    }




    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, @SuppressLint("RecyclerView") i: Int) {

        val type = getItemViewType(i)
        if (type == TYPE_POST){

            val h = holder as Holder
            val post = feeds.posts[i]
            log.e("=============== posts count => ${feeds.posts.size}")

            log.wtf("=============== start => ")

            log.wtf("post id:       ${post.id}")
//            log.wtf("post audios:   ${post.audios}")
            log.wtf("post images:   ${post.images}")
            log.wtf("post quote:    ${post.quote}")
            log.wtf("post comments: ${post.comments}")
            log.wtf("post like:     ${post.like}")
            log.wtf("post likes:    ${post.likes}")
            log.wtf("post time:     ${post.time}")
            log.wtf("post user:     ${post.user}")
            log.wtf("=============== end ; ")


            val icon = if (post.like == "0")
                VectorDrawableCompat.create(Base.get.resources, R.drawable.like, h.likeIcon.context.theme)
            else
                VectorDrawableCompat.create(Base.get.resources, R.drawable.like_select, h.likeIcon.context.theme)

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

            }else{

                h.quote.visibility     = View.VISIBLE
                h.quoteEdit.visibility = View.GONE
                h.quoteEdit.clearComposingText()
                h.sendChange.visibility = View.GONE
                h.quote.text           = post.quote.text
                if (cachedTexts.indexOf(post.quote.text) == -1) {

                    log.d("MY OPTIMIZATION nocached")
                    cachedTexts.add(post.quote.text)
                    h.initHashtag(ctx)
                }

            }


            h.showAvatar(Functions.checkImageUrl(post.user.photo))




            if (h.quote.tag == null || h.quote.tag != post.id) {

                h.quote.tag = post.id

                h.username.text = post.user.username

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





                    h.initPhotoAdapter(post,ctx)

                } else {
                    h.images.visibility = View.GONE
                }

//                if (post.audios.size > 0) {
//                    h.initAudioAdapter(post,player,model,ctx)
//
//                } else {
                    h.line.visibility = View.GONE
                    h.audios.visibility = View.GONE
//                }









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
        var avatar        by Delegates.notNull<SimpleDraweeView>()
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
            avatar       = itemView.findViewById<SimpleDraweeView>(R.id.avatar)
            avatar.hierarchy = Functions.getAvatarHierarchy()
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


        fun showAvatar(url:String?){
            avatar.post{

                avatar.controller = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(
                                ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                                        .setResizeOptions(ResizeOptions(100,100))
                                        .build())
                        .setOldController(avatar.controller)
                        .setAutoPlayAnimations(true)

                        .build()

            }
        }

        fun initPhotoAdapter(post:Posts,ctx:FragmentActivity) {
            images.visibility = View.VISIBLE
            val span: Int = if ((post.images.size > 1)) {
                if (post.images.size == 2) {
                    2
                } else {
                    (post.images.size - 1)
                }
            } else {
                1
            }

            val manager = CustomManager(Base.get, span)
            val adapter = PostPhotoGridAdapter(ctx, post.images)

            manager.spanSizeLookup = SpanClass(post,manager)

            images.layoutManager = manager
            images.setHasFixedSize(true)
            images.adapter = adapter
        }

        fun initAudioAdapter(post: Posts,player:MusicPlayerListener,model:Model,ctx:FragmentActivity) {
            audios.visibility = View.VISIBLE

            val span = 1


            val manager = CustomManager(Base.get, span)
//            post.audios.forEach {
//                audio ->
//                audio.middlePath = audio.middlePath.replace(Const.AUDIO.MEDIUM, Prefs.Builder().audioRes())
//
//            }
//            val adapter = PostAudioGridAdapter(ctx, post.audios,MusicPlayer(player,adapterPosition),model)
//            if (FeedFragment.cachedSongAdapters != null){
//                FeedFragment.cachedSongAdapters!!.put(adapterPosition,adapter)
//            }else{
//                FeedFragment.cachedSongAdapters = HashMap()
//                FeedFragment.cachedSongAdapters!!.put(adapterPosition,adapter)
//            }


            audios.layoutManager = manager
            audios.setHasFixedSize(true)
//            audios.adapter = adapter
        }


        var hashTag:HashTagHelper? = null
        fun initHashtag(ctx: FragmentActivity) {

           if (quote.getmTv().text.contains("#") || quote.getmTv().text.contains("@")){
               Observable.just(quote.getmTv())
                       .subscribeOn(Schedulers.computation())
                       .observeOn(AndroidSchedulers.mainThread())
                       .subscribeWith(object :Observer<TextView>{
                           override fun onNext(t: TextView) {
                               hashTag = HashTagHelper.Creator.create(Base.get.resources.getColor(R.color.hashtag),HashtagGenerator(ctx))
                               hashTag!!.handle(quote.getmTv())
                           }

                           override fun onComplete() {
                           }

                           override fun onError(e: Throwable) {
                           }

                           override fun onSubscribe(d: Disposable) {
                           }

                       })
           }



        }
    }







class HashtagGenerator(context:FragmentActivity) : HashTagHelper.OnHashTagClickListener{
        val ctx:WeakReference<FragmentActivity> = WeakReference<FragmentActivity>(context)
        override fun onHashTagClicked(hashTag: String?) {

          if (ctx.get() != null){
              var intent:Intent? = Intent(ctx.get()!!,SearchByTagActivity::class.java)
              intent!!.putExtra("tag",hashTag!!)
              ctx.get()!!.startActivity(intent)
              intent = null
          }
        }
        override fun onLoginClicked(login: String?) {

            var intent:Intent? = Intent(ctx.get()!!,SearchActivity::class.java)
            intent!!.putExtra("login",login!!)
            ctx.get()!!.startActivity(intent)
            intent = null
        }

    }




}



