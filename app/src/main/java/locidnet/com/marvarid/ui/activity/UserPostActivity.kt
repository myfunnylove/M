package locidnet.com.marvarid.ui.activity

import android.content.*
import android.os.IBinder
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.nineoldandroids.animation.AnimatorSet
import kotlinx.android.synthetic.main.activity_post.*
import locidnet.com.marvarid.R
import locidnet.com.marvarid.R.string.post
import locidnet.com.marvarid.adapter.PostAudioGridAdapter
import locidnet.com.marvarid.adapter.PostPhotoGridAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.connectors.MusicPlayerListener
import locidnet.com.marvarid.di.DaggerMVPComponent
import locidnet.com.marvarid.di.modules.ErrorConnModule
import locidnet.com.marvarid.di.modules.MVPModule
import locidnet.com.marvarid.di.modules.PresenterModule
import locidnet.com.marvarid.model.*
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.mvp.Presenter
import locidnet.com.marvarid.mvp.Viewer
import locidnet.com.marvarid.pattern.MControlObserver.MusicControlObserver
import locidnet.com.marvarid.pattern.builder.EmptyContainer
import locidnet.com.marvarid.pattern.builder.ErrorConnection
import locidnet.com.marvarid.player.PlayerService
import locidnet.com.marvarid.resources.customviews.CustomManager
import locidnet.com.marvarid.resources.utils.*
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.ui.fragment.FeedFragment
import org.ocpsoft.prettytime.PrettyTime
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


class UserPostActivity : BaseActivity() ,Viewer , MusicPlayerListener, MusicControlObserver {


    override fun getLayout(): Int = R.layout.activity_post

    @Inject
    lateinit var presenter:Presenter


    @Inject
    lateinit var errorConn: ErrorConnection
    lateinit var emptyContainer:EmptyContainer

    private lateinit var postContainer:ViewGroup
    var postId = -1

    lateinit var user:User

    val model                 = Model()

    //binding
    private var musicBound = false
    var musicSrv:PlayerService? = null
    internal var playerServiceBinder: PlayerService.PlayerServiceBinder? = null
    internal var mediaController: MediaControllerCompat? = null


    /*
    *
    *  POST ELEMENTS
    *
    * */

    private var images        by Delegates.notNull<RecyclerView>()
    private var audios        by Delegates.notNull<RecyclerView>()
    private var avatar        by Delegates.notNull<AppCompatImageView>()
    private var name          by Delegates.notNull<TextView>()
    private var quote         by Delegates.notNull<TextView>()
    private var quoteEdit     by Delegates.notNull<EditText>()
    private var likeCount     by Delegates.notNull<TextSwitcher>()
    private var commentCount  by Delegates.notNull<TextView>()
    private var time          by Delegates.notNull<TextView>()
    private var username      by Delegates.notNull<TextView>()
    private var likeIcon      by Delegates.notNull<AppCompatImageView>()
    private var popup         by Delegates.notNull<AppCompatImageView>()
    private var likeLay       by Delegates.notNull<LinearLayout>()
    private var commentLay    by Delegates.notNull<LinearLayout>()
    private var topContainer  by Delegates.notNull<ViewGroup>()
    private var sendChange    by Delegates.notNull<AppCompatImageButton>()
    private val like                  = R.drawable.like_select
    private val unLike                = R.drawable.like
    var adapter:PostAudioGridAdapter? = null
    override fun initView() {

        DaggerMVPComponent
                .builder()
                .mVPModule(MVPModule(this, Model(),this))
                .presenterModule(PresenterModule())
                .errorConnModule(ErrorConnModule(this,true))

                .build()
                .inject(this)
        user = Prefs.Builder().getUser()

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = resources.getString(R.string.notifications)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {

            onBackPressed()

        }
        MainActivity.musicSubject!!.subscribe(this)

        initViews()
        emptyContainer = EmptyContainer.Builder()
                .setIcon(R.drawable.feed_light)
                .setText(R.string.error_empty_post)
                .initLayoutForActivity(this)
                .build()
        emptyContainer.hide()
        postContainer = findViewById<ViewGroup>(R.id.postContainer)
        postContainer.visibility = View.GONE

        postId = intent.getIntExtra("postId",-1)
        Const.TAG = "UserPostAcivity"
        if (postId != -1){

            errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                override fun connected() {
                    /*send data for get comment list*/
                    val obj = JS.get()
                    obj.put("post_id",   postId)
//                    obj.put("start",   0)
//                    obj.put("end", CommentActivity.end)
//                    obj.put("order",  "DESC")
//
                    presenter.requestAndResponse(obj, Http.CMDS.GET_FULL_POST)
                }

                override fun disconnected() {

                }

            })
        }else{
            emptyContainer.show()
        }

        bindService(Intent(this, PlayerService::class.java), object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                playerServiceBinder = service as PlayerService.PlayerServiceBinder
                musicSrv = service.service

                musicBound = true
                try {
                    mediaController = MediaControllerCompat(this@UserPostActivity, playerServiceBinder!!.getMediaSessionToken())
                    mediaController!!.registerCallback(object : MediaControllerCompat.Callback() {
                        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                            log.d("MEDIACONTROLL $state")
                            if (state == null)
                                return
//                            val playing = state.state == PlaybackStateCompat.STATE_PLAYING

                        }
                    })
                } catch (e: Throwable) {
                    mediaController = null
                }

            }

            override fun onServiceDisconnected(name: ComponentName) {
                musicBound = false
                playerServiceBinder = null
                mediaController = null
            }
        }, Context.BIND_AUTO_CREATE)
    }



    override fun initProgress() {
    }

    override fun showProgress() {
        progressLay.visibility = View.VISIBLE

    }

    override fun hideProgress() {
        progressLay.visibility = View.GONE

    }

    override fun onSuccess(from: String, result: String) = try{
        postContainer.visibility = View.VISIBLE


        val post: Posts = Gson().fromJson(result,Posts::class.java)

        val icon: VectorDrawableCompat?
        icon = if (post.like == "0")
            VectorDrawableCompat.create(Base.get.resources, unLike, likeIcon.context.theme)
        else
            VectorDrawableCompat.create(Base.get.resources, like, likeIcon.context.theme)

        likeIcon.setImageDrawable(icon)

        likeCount.setCurrentText(post.likes)
        supportActionBar!!.title = post.user.username

        val currentLikesCount  = post.likes.toInt()
        likeCount.setText(currentLikesCount.toString())


//           if (likeAnimations.containsKey()){
//               likeAnimations.get("a")!!.cancel()
//           }
//
//           likeAnimations.remove(1);

        quote.visibility     = View.VISIBLE
        quote.text           = post.quote.text
        quoteEdit.visibility = View.GONE
        quoteEdit.clearComposingText()
        sendChange.visibility = View.GONE

        Glide.with(this)
                .load(Functions.checkImageUrl(post.user.photo))
                .apply(Functions.getGlideOpts())
                .into(avatar)
        quote.tag = post.id

        username.text = post.user.username

        val prettyTime = PrettyTime()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date2 = formatter.parse(post.time) as Date


        time.text = prettyTime.format(date2)


        if (post.quote.textSize != "") {
            try {
                quote.textSize = post.quote.textSize.toFloat()
            } catch (e: Exception) {
            }
        }
        try {

            quote.setTextColor(ContextCompat.getColor(Base.get, Const.colorPalette[post.quote.textColor.toInt()]!!.drawable))

        } catch (e: Exception) {

        }
        /*
        *
        * INIT IMAGES
        *
        * */
        if (post.images.size > 0) {


            images.visibility = View.VISIBLE


            val span = if ((post.images.size > 1)) {
                if (post.images.size == 2) {
                    2
                } else {
                    (post.images.size - 1)
                }
            } else {
                1
            }

            val manager = CustomManager(this, span)
            val adapter = PostPhotoGridAdapter(this, post.images)

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

            images.layoutManager = manager
            images.setHasFixedSize(true)
            images.adapter = adapter




        } else {
            images.visibility = View.GONE
        }



        /*
        *
        * INIT AUDIOS
        *
        * */

        if (post.audios.size > 0) {
            audios.visibility = View.VISIBLE

            val span = 1


            val manager = CustomManager(this, span)
            post.audios.forEach {
                audio ->
                audio.middlePath = audio.middlePath.replace(Const.AUDIO.MEDIUM, Prefs.Builder().audioRes())

            }
            adapter = PostAudioGridAdapter(this, post.audios,this,model)
            if (FeedFragment.cachedSongAdapters != null){
                FeedFragment.cachedSongAdapters!!.put(0,adapter!!)
            }else{
                FeedFragment.cachedSongAdapters = HashMap()
                FeedFragment.cachedSongAdapters!!.put(0,adapter!!)
            }


            audios.layoutManager = manager
            audios.setHasFixedSize(true)
            audios.adapter = adapter

        } else {
            audios.visibility = View.GONE
        }


                likeLay.setOnClickListener {
            if (post.like == "0") {

                post.like = "1"
                post.likes = (post.likes.toInt() + 1).toString()
                likeIcon.setImageDrawable(VectorDrawableCompat.create(Base.get.resources, like, likeIcon.context.theme))
            } else {
                post.likes = (post.likes.toInt() - 1).toString()

                post.like = "0"
                likeIcon.setImageDrawable(VectorDrawableCompat.create(Base.get.resources, unLike, likeIcon.context.theme))

            }


//                    notifyDataSetChanged()






            val reqObj = JS.get()

            reqObj.put("post_id", post.id)

            log.d("request data $reqObj")

            model.responseCall(Http.getRequestData(reqObj, Http.CMDS.LIKE_BOSISH))
                    .enqueue(object : retrofit2.Callback<ResponseData> {
                        override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
                            log.d("follow on fail $t")
                        }

                        override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {


                        }

                    })
        }

        commentLay.setOnClickListener {
            val goCommentActivity = Intent(this, CommentActivity::class.java)
            goCommentActivity.putExtra("postId", post.id.toInt())
            goCommentActivity.putExtra("postUsername",post.user.username)
            goCommentActivity.putExtra("postUserPhoto",post.user.photo)
            goCommentActivity.putExtra("postQuoteText",post.quote.text)
            goCommentActivity.putExtra("postQuoteColor",post.quote.textColor)
            goCommentActivity.putExtra("postQuoteSize",post.quote.textSize)
            val startingLocation = IntArray(2)
            commentLay.getLocationOnScreen(startingLocation)
            goCommentActivity.putExtra(CommentActivity.LOCATION, startingLocation[1])

                MainActivity.COMMENT_POST_UPDATE = 0
                startActivityForResult(goCommentActivity,Const.GO_COMMENT_ACTIVITY)
                overridePendingTransition(0, 0)

        }
//           avatar.setOnClickListener{
//               clicker.click(i)
//
//           }
//           h.topContainer.setOnClickListener {
//
//               if (!pOrF) clicker.click(i)
//
//           }
    }catch (e:Exception){
        onFailure(from,"","")
    }

    override fun onFailure(from: String, message: String, erroCode: String) {



        postContainer.visibility =View.GONE
        emptyContainer.show()
    }


    private fun initViews(){
        images       = findViewById<RecyclerView>(R.id.images)
        audios       = findViewById<RecyclerView>(R.id.audios)
        avatar       = findViewById<AppCompatImageView>(R.id.avatar)
        name         = findViewById<TextView>(R.id.name)
        quote        = findViewById<TextView>(R.id.commentText)
        quoteEdit    = findViewById<EditText>(R.id.commentEditText)
        likeCount    = findViewById<TextSwitcher>(R.id.likeCount)
        commentCount = findViewById<TextView>(R.id.commentCount)
        time         = findViewById<TextView>(R.id.time)
        username     = findViewById<TextView>(R.id.username)
        likeIcon     = findViewById<AppCompatImageView>(R.id.likeIcon)
        popup        = findViewById<AppCompatImageView>(R.id.popup)
        likeLay      = findViewById<LinearLayout>(R.id.likeLay)
        commentLay   = findViewById<LinearLayout>(R.id.commentLay)
        topContainer = findViewById<ViewGroup>(R.id.topContainer)
        sendChange   = findViewById<AppCompatImageButton>(R.id.sendChangedQuote)

        images

    }

    override fun playClick(listSong: ArrayList<Audio>, position: Int){
        PlayerService.songs = listSong
        PlayerService.songPosn = position
        log.d("PLAYCLICKED")
        if (mediaController != null) {

            if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PLAYING &&
                    PlayerService.PLAYING_SONG_URL == listSong[position].middlePath) {

                mediaController!!.transportControls.pause()

            } else if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PAUSED &&
                    PlayerService.PLAYING_SONG_URL == listSong[position].middlePath) {

                mediaController!!.transportControls.play()

            } else if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PLAYING &&
                    PlayerService.PLAYING_SONG_URL != listSong[position].middlePath) {
                mediaController!!.transportControls.play()

            } else if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PAUSED &&
                    PlayerService.PLAYING_SONG_URL != listSong[position].middlePath) {
                mediaController!!.transportControls.play()

            }else {
                mediaController!!.transportControls.play()

            }

        }else{
            Toast.makeText(Base.get,Base.get.resources.getString(R.string.error_something),Toast.LENGTH_SHORT).show()
        }
    }








    override fun onDestroy() {
        super.onDestroy()
        presenter.ondestroy()

        MainActivity.musicSubject!!.unsubscribe(this)

    }
    override fun playPause(id: String) {
        adapter!!.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Const.SESSION_OUT || resultCode == Const.SESSION_OUT){
            setResult(Const.SESSION_OUT)
            finish()
        }
    }
}