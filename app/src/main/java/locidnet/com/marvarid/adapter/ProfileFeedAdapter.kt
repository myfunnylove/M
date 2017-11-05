package locidnet.com.marvarid.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
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
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.postprocessors.IterativeBoxBlurPostProcessor
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.google.gson.Gson
import com.nineoldandroids.animation.AnimatorSet
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import org.json.JSONObject
import locidnet.com.marvarid.R
import locidnet.com.marvarid.R.string.feeds
import locidnet.com.marvarid.adapter.optimize.*
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.connectors.MusicPlayerListener
import locidnet.com.marvarid.connectors.ProfileMusicController
import locidnet.com.marvarid.model.*
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.player.PlayerService
import locidnet.com.marvarid.resources.adapterAnim.AnimateViewHolder
import locidnet.com.marvarid.resources.customviews.CustomManager
import locidnet.com.marvarid.resources.expandableTextView.ExpandableTextView
import locidnet.com.marvarid.resources.hashtag.HashTagHelper
import locidnet.com.marvarid.resources.utils.*
import locidnet.com.marvarid.ui.activity.*
import locidnet.com.marvarid.ui.dialogs.ComplaintsFragment
import locidnet.com.marvarid.ui.fragment.*
import org.ocpsoft.prettytime.PrettyTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.properties.Delegates


class ProfileFeedAdapter(context: FragmentActivity,
                         feedsMap: PostList,

                         adapterClicker: AdapterClicker,
                         musicPlayerListener: MusicPlayerListener,
                         profileController:ProfileMusicController?,
                         var userInfo: UserInfo? = null,
                         profilOrFeed: Boolean = false,
                         followType: String = "",

                         closedProfil: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var ctx: FragmentActivity? = context
    var feeds = feedsMap
    var inflater: LayoutInflater? = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var clicker: AdapterClicker? = adapterClicker
    val like = R.drawable.like_select
    val unLike = R.drawable.like
    var myProfil = Base.get.prefs.getUser()
    var model: Model? = Model()
    val pOrF = profilOrFeed
    var FOLLOW_TYPE: String? = followType
    var user: User? = Base.get.prefs.getUser()
    var lastAnimationPosition = -1
    var itemsCount = 0
    var activity: FragmentActivity? = context
    var disableAnimation = false
    var cachedLists = HashMap<String, String>()
    var player: MusicPlayerListener? = musicPlayerListener
    var closedProfile = closedProfil
    var profileControl: ProfileMusicController? = profileController
    var callback:Call<ResponseData>? = null
    val viewPool  = RecyclerView.RecycledViewPool()
    var cachedTexts:ArrayList<String> = ArrayList()




    companion object {
        var changeId = -1

        val ANIMATED_ITEM_COUNT = 0
        val HEADER = 1
        val BODY = 2
        val ACTION_LIKE_BUTTON_CLICKED = "action_like_button_button"
        val ACTION_LIKE_IMAGE_CLICKED = "action_like_image_button"
        val VIEW_TYPE_DEFAULT = 1
        val VIEW_TYPE_LOADER = 2
        val ACCELERATE_INTERPOLATOR = AccelerateInterpolator()
        val OVERSHOOT_INTERPOLATOR = OvershootInterpolator(4f)
        val likeAnimations = HashMap<RecyclerView.ViewHolder, AnimatorSet>()
        var avatarUpdated = -1
        var SHOW_PROGRESS = 1
        var HIDE_PROGRESS = 0
        var CANCEL_PROGRESS = 2
        val PLAY = R.drawable.notif_play
        val PAUSE = R.drawable.notif_pause
        var playStatus: Int? = -1




    }


    fun View.runEnterAnimation(position: Int) {
        if (disableAnimation || position < lastAnimationPosition) {
            return
        }


        if (position > ANIMATED_ITEM_COUNT) {
            lastAnimationPosition = position
            this.translationY = Functions.getScreenHeight(ctx!!).toFloat()
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

        if (viewType == HEADER) {
            return ProfileHeaderHolder(inflater!!.inflate(R.layout.user_profil_header, p0!!, false))

        } else {
            val holder = Holder(inflater!!.inflate(R.layout.res_feed_block_image, p0!!, false))
            holder.images.recycledViewPool = viewPool
            holder.audios.recycledViewPool = viewPool
            holder.likeLay.setOnClickListener(LikeListenClassProfile(feeds,holder,model!!))
            holder.sendChange.setOnClickListener {

                val quote:Quote = feeds.posts.get(holder.adapterPosition).quote
                quote.text = holder.quoteEdit.text.toString()

                val js =  JS.get()
                js.put("post_id",feeds.posts.get(holder.adapterPosition).id)
                js.put("quote", JSONObject(Gson().toJson(quote)))
//                  js.put("user_id", profile.userId )
//                  js.put("session", profile.session)
                log.d ("changequote send data $js")

                model!!.responseCall(Http.getRequestData(js, Http.CMDS.CHANGE_POST)).enqueue(SendChangePostProfile(feeds,holder,this))
            }


            holder.commentLay.setOnClickListener(CommentClassProfileAdapter(ctx!!,feeds,holder))
            holder.avatar.setOnClickListener{
                if (!pOrF) clicker!!.click(holder.adapterPosition)

            }
            holder.topContainer.setOnClickListener {

                if (!pOrF) clicker!!.click(holder.adapterPosition)

            }


            holder.popup.setOnClickListener(PopupClassProfile(ctx!!,holder,feeds,this,model!!))
            return holder
        }

    }

    override fun getItemViewType(position: Int): Int {

        if (position == 0) return HEADER else return BODY

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, i: Int) {

        val type = getItemViewType(i)
        holder!!.itemView

        if (type == BODY) {
            val h = holder as Holder
            val post = feeds.posts.get(i)
            log.e("=============== posts count => ${feeds.posts.size}")
            log.d("oneni ${post}")

            log.wtf("=============== start => ")

            log.wtf("post id:       ${post.id}")
            log.wtf("post audios:   ${post.audios}")
            log.wtf("post images:   ${post.images}")
            log.wtf("post quote:    ${post.quote}")
            log.wtf("post comments: ${post.comments}")
            log.wtf("post like:     ${post.like}")
            log.wtf("post likes:    ${post.likes}")
            log.wtf("post time:     ${post.time}")
            log.wtf("=============== end ; ")


            val icon: VectorDrawableCompat?
            if (post.like == "0")
                icon = VectorDrawableCompat.create(Base.get.resources, unLike, h.likeIcon.context.theme)
            else
                icon = VectorDrawableCompat.create(Base.get.resources, like, h.likeIcon.context.theme)

            h.likeIcon.setImageDrawable(icon)

            h.likeCount.setCurrentText(post.likes)



            if (likeAnimations.containsKey(h)) {
                likeAnimations.get(h)!!.cancel()
            }

            likeAnimations.remove(h)

//            h.commentCount.text = post.comments


            if (pOrF == true && changeId == i) {

                h.quote.visibility = View.GONE
                h.quoteEdit.visibility = View.VISIBLE
                h.quoteEdit.setText(post.quote.text)
                h.sendChange.visibility = View.VISIBLE

            } else {

                h.quote.visibility = View.VISIBLE
                h.quoteEdit.visibility = View.GONE
                h.quoteEdit.clearComposingText()
                h.sendChange.visibility = View.GONE




                    h.quote.text = post.quote.text

                if (cachedTexts.indexOf(post.quote.text) == -1) {

                    log.d("MY OPTIMIZATION nocached")
                    cachedTexts.add(post.quote.text)
                    h.initHashtag(ctx!!)
                }


            }

            h.showAvatar(Functions.checkImageUrl(post.user.photo))


            if (h.quote.tag == null || h.quote.tag != post.id) {

                h.quote.tag = post.id

                h.username.text = userInfo!!.user.info.username
                //TODO
//                if (!userInfo!!.user.info.name.isNullOrEmpty()){
//                    h.name.visibility = View.VISIBLE
//                    h.name.text = userInfo!!.user.info.name
//
//                }else{
                h.name.visibility = View.GONE

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

                    h.quote.setTextColor(ContextCompat.getColor(Base.get, Const.colorPalette.get(post.quote.textColor.toInt())!!.drawable))

                } catch (e: Exception) {

                }



                if (post.images.size > 0) {


                    h.initPhotoAdapter(post,ctx!!)

                } else {
                    h.images.visibility = View.GONE
                }

                if (post.audios.size > 0) {

                    h.initAudioAdapter(post,player!!,model!!,ctx!!,this,userInfo!!,myProfil)


                } else {
                    h.line.visibility = View.GONE

                    h.audios.visibility = View.GONE
                }





            }

        } else if (type == HEADER) {
            log.d("HEADER $FOLLOW_TYPE")
            val h = holder as ProfileHeaderHolder

            h.follow.tag = FOLLOW_TYPE
            h.follow.text = FOLLOW_TYPE

            /*AGAR CLOSED PROFIL BOSA */
            if (pOrF && closedProfile) h.closedProfilLay.visibility = View.VISIBLE
            else h.closedProfilLay.visibility = View.GONE


            /*AGAR MY PROFIL BOSA PLAYLIST*/
            if (FOLLOW_TYPE == ProfileFragment.SETTINGS) {

                h.playlist.visibility = View.VISIBLE
                h.playlist.setOnClickListener {

                    val goCommentActivity = Intent(ctx, PlaylistActivity::class.java)

                    val startingLocation = IntArray(2)
//                    h.playlist.getLocationOnScreen(startingLocation)
//                    goCommentActivity.putExtra(CommentActivity.LOCATION, startingLocation[1])
                    if (activity != null) {
                        activity!!.startActivityForResult(goCommentActivity, Const.GO_PLAY_LIST)
                        activity!!.overridePendingTransition(0, 0)
                    } else {
                        ctx!!.startActivity(goCommentActivity)
                    }
                }
                log.d("play status $playStatus ${PlayerService.PLAY_STATUS == PlayerService.PLAYING}")
                if (playStatus != -1 || PlayerService.PLAY_STATUS == PlayerService.PLAYING){
                    h.play.visibility = View.VISIBLE
                    h.next.visibility = View.VISIBLE
                    h.play.setOnClickListener {
                        profileControl!!.pressPlay()
                    }
                    h.next.setOnClickListener {
                        profileControl!!.pressNext()
                    }
                    if (PlayerService.PLAY_STATUS == PlayerService.PLAYING) playStatus = R.drawable.notif_pause else playStatus = R.drawable.notif_play
                    val icon = VectorDrawableCompat.create(Base.get.resources, playStatus!!, Base.get.theme)
                    h.play.setImageDrawable(icon)
                }



            } else {
                h.playlist.visibility = View.GONE

            }


            if (avatarUpdated == SHOW_PROGRESS) {
                h.progress.visibility = View.VISIBLE

            } else {
                h.progress.visibility = View.GONE
            }




            h.avatar.post{

                h.avatar.controller = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(
                                ImageRequestBuilder.newBuilderWithSource(Uri.parse(Functions.checkImageUrl(userInfo!!.user.info.photoOrg)))
                                        .setResizeOptions(ResizeOptions(200,200))

                                        .build())
                        .setOldController(h.avatar.controller)
                        .setAutoPlayAnimations(true)

                        .build()

            }

            h.bg.post{
                h.bg.controller = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(
                                ImageRequestBuilder.newBuilderWithSource(Uri.parse(Functions.checkImageUrl(userInfo!!.user.info.photoOrg)!!.replace(Const.IMAGE.TITLE, Prefs.Builder().imageRes())))
//                                            .setResizeOptions(ResizeOptions(width,height))
                                        .setCacheChoice(ImageRequest.CacheChoice.DEFAULT)
                                        .setPostprocessor(IterativeBoxBlurPostProcessor(10))
                                        .build())
                        .setOldController(h.bg.controller)

                        .setAutoPlayAnimations(true)
                        .build()


            }


            h.username.text = userInfo!!.user.info.username
            h.posts.text = userInfo!!.user.count.postCount

            if (!userInfo!!.user.info.name.isNullOrEmpty()) {
                h.firstName.visibility = View.VISIBLE
                h.firstName.text = userInfo!!.user.info.name

            }

            h.followers.text = userInfo!!.user.count.followersCount
            h.following.text = userInfo!!.user.count.followingCount

            if (!closedProfile) {
                h.followersLay.setOnClickListener {
                    clicker!!.click(Const.TO_FOLLOWERS)
                }
                h.followingLay.setOnClickListener {

                    clicker!!.click(Const.TO_FOLLOWING)
                }

                h.avatar.setOnClickListener {
                    clicker!!.click(Const.CHANGE_AVATAR)

                }
            }


            h.follow.setOnClickListener {

                log.d("follow button type ${h.follow.tag}")

                if (h.follow.tag == ProfileFragment.SETTINGS) {
                    val goSettingActivity = Intent(ctx, SettingsActivity::class.java)

                    activity!!.startActivityForResult(goSettingActivity, Const.GO_SETTINGS)
                } else if (h.follow.tag == ProfileFragment.FOLLOW) {

                    val reqObj = JS.get()

                    reqObj.put("user", userInfo!!.user.info.user_id)
                    callback = model!!.responseCall(Http.getRequestData(reqObj, Http.CMDS.FOLLOW))
                    callback!!.enqueue(object : retrofit2.Callback<ResponseData> {
                                override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
                                    log.d("follow on fail $t")
                                }

                                override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {
                                    if (response!!.isSuccessful) {

                                        FFFFragment.OZGARGAN_USERNI_IDSI = userInfo!!.user.info.user_id.toInt()

                                        try {

                                            val req = JSONObject(Http.getResponseData(response.body()!!.prms))
                                            if (req.optString("request") == "1") {


                                                FOLLOW_TYPE = ProfileFragment.REQUEST
                                                FFFFragment.QAYSI_HOLATGA_OZGARDI = ProfileFragment.REQUEST
                                                ProfileFragment.FOLLOW_TYPE = ProfileFragment.REQUEST
                                                if (SearchActivity.choosedUserId.isNotEmpty()) {
                                                    SearchActivity.choosedUserId = userInfo!!.user.info.user_id
                                                }


                                            } else if (req.optString("request") == "0") {

                                                FOLLOW_TYPE = ProfileFragment.UN_FOLLOW

                                                userInfo!!.user.count.followersCount = "${h.followers.text.toString().toInt() + 1}"
                                                FFFFragment.QAYSI_HOLATGA_OZGARDI = ProfileFragment.UN_FOLLOW
                                                ProfileFragment.FOLLOW_TYPE = ProfileFragment.UN_FOLLOW


                                                if (SearchActivity.choosedUserId.isNotEmpty()) {
                                                    SearchActivity.choosedUserId = userInfo!!.user.info.user_id
                                                }
                                            }
                                            notifyItemChanged(0)

                                        } catch (e: Exception) {

                                        }



                                        MainActivity.FEED_STATUS = MainActivity.NEED_UPDATE
                                    } else {

                                        Toast.makeText(Base.get, Base.get.resources.getString(R.string.internet_conn_error), Toast.LENGTH_SHORT).show()

                                    }


                                }

                            })
                } else if (h.follow.tag == ProfileFragment.UN_FOLLOW || h.follow.tag == ProfileFragment.REQUEST) {

                    val reqObj = JS.get()

                    reqObj.put("user", userInfo!!.user.info.user_id)
                    callback = model!!.responseCall(Http.getRequestData(reqObj, Http.CMDS.UN_FOLLOW))
                    callback!!.enqueue(object : retrofit2.Callback<ResponseData> {
                                override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
                                    log.d("follow on fail $t")
                                }

                                override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {
                                    if (response!!.isSuccessful) {

                                        FFFFragment.OZGARGAN_USERNI_IDSI = userInfo!!.user.info.user_id.toInt()



                                        if ((FOLLOW_TYPE == ProfileFragment.UN_FOLLOW || FOLLOW_TYPE == ProfileFragment.REQUEST) && response.body()!!.res == "0") {


                                            if (SearchActivity.choosedUserId.isNotEmpty()) {
                                                SearchActivity.choosedUserId = userInfo!!.user.info.user_id
                                            }

                                            if (h.follow.tag != ProfileFragment.REQUEST) userInfo!!.user.count.followersCount = "${h.followers.text.toString().toInt() - 1}"
                                            FOLLOW_TYPE = ProfileFragment.FOLLOW
                                            FFFFragment.QAYSI_HOLATGA_OZGARDI = ProfileFragment.FOLLOW
                                            ProfileFragment.FOLLOW_TYPE = ProfileFragment.FOLLOW

                                            if (userInfo!!.user.info.close == 1) {
                                                closedProfile = true
                                                val post = feeds.posts.get(0)
                                                feeds.posts.clear()
                                                feeds.posts.add(post)
                                                notifyDataSetChanged()
                                            } else {
                                                notifyItemChanged(0)
                                            }


                                        }

                                        MainActivity.FEED_STATUS = MainActivity.NEED_UPDATE
                                    }


                                }

                            })
                }


            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        if (callback != null) callback!!.cancel()
    }




    fun swapPhotoProgress(status: Int) {
        avatarUpdated = status
        notifyItemChanged(0)
    }

    fun swapFirstItem(postList: PostList) {
        disableAnimation = false
        feeds.posts.add(0, postList.posts.get(0))
        notifyDataSetChanged()
    }

    fun updateFirstItem(userInfo: UserInfo?) {
        disableAnimation = false
        this.userInfo = userInfo
        notifyItemChanged(0)
    }

    fun swapLast20Item(postList: PostList) {
        log.d("in profilefeed $postList")
        disableAnimation = false

        val lastItemPostition = (feeds.posts.size + 1)
        feeds.posts.addAll(postList.posts)
        notifyItemRangeInserted(lastItemPostition, postList.posts.size)
    }


    fun updateMusicController(playS: Int) {
        playStatus = playS
        notifyItemChanged(0)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) , AnimateViewHolder {

        var images by Delegates.notNull<RecyclerView>()

        var line         by Delegates.notNull<View>()
        var audios       by Delegates.notNull<RecyclerView>()

        var avatar       by Delegates.notNull<SimpleDraweeView>()

        var name         by Delegates.notNull<TextView>()
        var quote        by Delegates.notNull<ExpandableTextView>()
        var quoteEdit    by Delegates.notNull<EditText>()
        var likeCount    by Delegates.notNull<TextSwitcher>()
        var commentCount by Delegates.notNull<TextView>()
        var time         by Delegates.notNull<TextView>()
        var username     by Delegates.notNull<TextView>()
        var likeIcon     by Delegates.notNull<AppCompatImageView>()
        var popup        by Delegates.notNull<AppCompatImageView>()
        var likeLay      by Delegates.notNull<LinearLayout>()
        var commentLay   by Delegates.notNull<LinearLayout>()
        var topContainer by Delegates.notNull<ViewGroup>()
        var sendChange   by Delegates.notNull<AppCompatImageButton>()

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
                    .translationY(-itemView.getHeight() * 0.3f)
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

        fun initAudioAdapter(post: Posts,player:MusicPlayerListener,model:Model,ctx:FragmentActivity,recycler:RecyclerView.Adapter<RecyclerView.ViewHolder>,userInfo: UserInfo?,myProfil:User) {
            audios.visibility = View.VISIBLE

            val span = 1


            val manager = CustomManager(Base.get, span)
            post.audios.forEach {
                audio ->
                audio.middlePath = audio.middlePath.replace(Const.AUDIO.MEDIUM, Prefs.Builder().audioRes())

            }
            val adapter = PostAudioGridAdapter(ctx, post.audios, ProfileMusicPlayer(player,adapterPosition,recycler,userInfo),model)

            if (myProfil.userId == userInfo!!.user.info.user_id) {
                if (MyProfileFragment.cachedSongAdapters != null) {
                    MyProfileFragment.cachedSongAdapters!!.put(adapterPosition, adapter)
                } else {
                    MyProfileFragment.cachedSongAdapters = HashMap()
                    MyProfileFragment.cachedSongAdapters!!.put(adapterPosition, adapter)
                }
            }else{
                if (ProfileFragment.cachedSongAdapters != null) {
                    ProfileFragment.cachedSongAdapters!!.put(adapterPosition, adapter)
                } else {
                    ProfileFragment.cachedSongAdapters = HashMap()
                    ProfileFragment.cachedSongAdapters!!.put(adapterPosition, adapter)
                }
            }


            audios.layoutManager = manager
            audios.setHasFixedSize(true)
            audios.adapter = adapter

        }


        var hashTag:HashTagHelper? = null
        fun initHashtag(ctx: FragmentActivity) {

            if (quote.getmTv().text.contains("#") || quote.getmTv().text.contains("@")){
                Observable.just(quote.getmTv())
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : Observer<TextView> {
                            override fun onNext(t: TextView) {
                                hashTag = HashTagHelper.Creator.create(Base.get.resources.getColor(R.color.hashtag), MyFeedAdapter.HashtagGenerator(ctx))
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


    class ProfileHeaderHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {


        val closedProfilLay = rootView.findViewById<LinearLayout>(R.id.closedProfilLay)
        val followersLay = rootView.findViewById<LinearLayout>(R.id.followersLay)
        val followingLay = rootView.findViewById<LinearLayout>(R.id.followingLay)
        val playlist = rootView.findViewById<AppCompatImageView>(R.id.playlist)
        val play = rootView.findViewById<AppCompatImageView>(R.id.play)
        val next = rootView.findViewById<AppCompatImageView>(R.id.next)
        var avatar by Delegates.notNull<SimpleDraweeView>()
        val bg = rootView.findViewById<SimpleDraweeView>(R.id.bg)
        val followers = rootView.findViewById<TextView>(R.id.followers)
        val following = rootView.findViewById<TextView>(R.id.following)
        val username = rootView.findViewById<TextView>(R.id.username)
        val firstName = rootView.findViewById<TextView>(R.id.firstName)
        val posts = rootView.findViewById<TextView>(R.id.posts)
        val follow = rootView.findViewById<Button>(R.id.follow)
        val progress = rootView.findViewById<ProgressBar>(R.id.progressUpdateAvatar)
        init {
            avatar  = rootView.findViewById<SimpleDraweeView>(R.id.avatar)
            avatar.hierarchy = Functions.getAvatarHierarchy()
            bg.hierarchy = Functions.getBackgroundOptions()

        }

    }

    fun updateProfilPhoto(path: String) {
        userInfo!!.user.info.photoOrg = Http.BASE_URL + path
        feeds.posts.forEach { post ->
            post.user.photo = userInfo!!.user.info.photoOrg
        }

        avatarUpdated = HIDE_PROGRESS
        notifyDataSetChanged()


    }

    fun Holder.updateLikesCounter(animated: Boolean) {
        val currentLikesCount = feeds.posts.get(this.adapterPosition).likes.toInt()
        if (animated) {
            this.likeCount.setText(currentLikesCount.toString())
        } else {
            this.likeCount.setCurrentText(currentLikesCount.toString())
        }


    }

    fun updateFollowersCount() {
        userInfo!!.user.count.followingCount = MyProfileFragment.FOLLOWING
        notifyItemChanged(0)
    }


    override fun onViewRecycled(holder: RecyclerView.ViewHolder?) {
        super.onViewRecycled(holder)
        Glide.get(ctx).clearMemory()
    }


}