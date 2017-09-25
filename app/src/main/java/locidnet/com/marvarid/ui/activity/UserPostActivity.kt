package locidnet.com.marvarid.ui.activity

import android.content.*
import android.os.IBinder
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
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
import locidnet.com.marvarid.adapter.MyFeedAdapter
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
import locidnet.com.marvarid.musicplayer.MusicController
import locidnet.com.marvarid.musicplayer.MusicService
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.mvp.Presenter
import locidnet.com.marvarid.mvp.Viewer
import locidnet.com.marvarid.pattern.MControlObserver.MusicControlObserver
import locidnet.com.marvarid.pattern.builder.EmptyContainer
import locidnet.com.marvarid.pattern.builder.ErrorConnection
import locidnet.com.marvarid.resources.customviews.CustomManager
import locidnet.com.marvarid.resources.utils.*
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.ui.fragment.FeedFragment
import org.json.JSONObject
import org.ocpsoft.prettytime.PrettyTime
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

/**
 * Created by myfunnylove on 24.09.17.
 */
class UserPostActivity : BaseActivity() ,Viewer , MusicController.MediaPlayerControl, MusicPlayerListener, MusicControlObserver {


    override fun getLayout(): Int = R.layout.activity_post

    @Inject
    lateinit var presenter:Presenter


    @Inject
    lateinit var errorConn: ErrorConnection
    lateinit var emptyContainer:EmptyContainer

    lateinit var postContainer:ViewGroup
    var postId = -1

    lateinit var user:User

    val model                 = Model()
    private var musicSrv: MusicService? = null
    private var songList: ArrayList<Audio>? = null

    //service
    private var playIntent: Intent? = null
    //binding
    private var musicBound = false

    //controller
    private var controller: MusicController? = null

    //activity and playback pause flags
    private var paused = false
    var playbackPaused = false

    /*
    *
    *  POST ELEMENTS
    *
    * */

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
    val like                  = R.drawable.like_select
    val unLike                = R.drawable.like
    val likeAnimations             = HashMap<RecyclerView.ViewHolder, AnimatorSet>()
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
        supportActionBar!!.setTitle(resources.getString(R.string.notifications))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {

            onBackPressed()

        }

        initViews()
        emptyContainer = EmptyContainer.Builder()
                .setIcon(R.drawable.feed_light)
                .setText(R.string.error_empty_post)
                .initLayoutForActivity(this)
                .build()
        emptyContainer.hide()
        postContainer = findViewById(R.id.postContainer) as ViewGroup
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

    }



    override fun initProgress() {
    }

    override fun showProgress() {
        progressLay.visibility = View.VISIBLE

    }

    override fun hideProgress() {
        progressLay.visibility = View.GONE

    }

    override fun onSuccess(from: String, result: String) {

       try{
           postContainer.visibility = View.VISIBLE


           val post: Posts = Gson().fromJson(result,Posts::class.java)

           val icon: VectorDrawableCompat?
           if (post.like == "0")
               icon = VectorDrawableCompat.create(Base.get.resources, unLike, likeIcon.context.theme)
           else
               icon = VectorDrawableCompat.create(Base.get.resources, like, likeIcon.context.theme)

           likeIcon.setImageDrawable(icon)

           likeCount.setCurrentText(post.likes)
            supportActionBar!!.setTitle(post.user.username)

           val currentLikesCount  = post.likes.toInt()
           if (true){
               likeCount.setText(currentLikesCount.toString())
           }else{
               likeCount.setCurrentText(currentLikesCount.toString())
           }


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

               quote.setTextColor(ContextCompat.getColor(Base.get, Const.colorPalette.get(post.quote.textColor.toInt())!!.drawable))

           } catch (e: Exception) {

           }
           /*
           *
           * INIT IMAGES
           *
           * */
           if (post.images.size > 0) {


               images.visibility = View.VISIBLE

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

               val manager = CustomManager(this, span)
               val adapter = PostPhotoGridAdapter(this, post.images)

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
                   likeIcon.setImageDrawable(VectorDrawableCompat.create(Base.get.resources, like, likeIcon.context.theme));
               } else {
                   post.likes = (post.likes.toInt() - 1).toString()

                   post.like = "0"
                   likeIcon.setImageDrawable(VectorDrawableCompat.create(Base.get.resources, unLike, likeIcon.context.theme));

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


    }

    override fun onFailure(from: String, message: String, erroCode: String) {



        postContainer.visibility =View.GONE
        emptyContainer.show()
    }


    fun initViews(){
        images       = findViewById(R.id.images)       as RecyclerView
        audios       = findViewById(R.id.audios)       as RecyclerView
        avatar       = findViewById(R.id.avatar)       as AppCompatImageView
        name         = findViewById(R.id.name)         as TextView
        quote        = findViewById(R.id.commentText)  as TextView
        quoteEdit    = findViewById(R.id.commentEditText)  as EditText
        likeCount    = findViewById(R.id.likeCount)    as TextSwitcher
        commentCount = findViewById(R.id.commentCount) as TextView
        time         = findViewById(R.id.time)         as TextView
        username     = findViewById(R.id.username)     as TextView
        likeIcon     = findViewById(R.id.likeIcon)     as AppCompatImageView
        popup        = findViewById(R.id.popup)        as AppCompatImageView
        likeLay      = findViewById(R.id.likeLay)      as LinearLayout
        commentLay   = findViewById(R.id.commentLay)   as LinearLayout
        topContainer = findViewById(R.id.topContainer) as ViewGroup
        sendChange   = findViewById(R.id.sendChangedQuote) as AppCompatImageButton

        images

    }

    val musicConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder
            //get service
            musicSrv = binder.service
            //pass list
//            musicSrv!!.setList(songList)
            musicBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }

    override fun playClick(listSong: ArrayList<Audio>, position: Int) {
//        try{

            if (musicSrv != null){
                log.d("PLAYIN SONG ${musicSrv!!.isPng}")
                if(musicSrv!!.isPng){

                    if (MusicService.PLAYING_SONG_URL == listSong.get(position).middlePath){
                        pause()
                    }else{
                        if(controller == null) setController()
                        controller!!.setLoading(true);

                        musicSrv!!.setList(listSong)
                        musicSrv!!.setSong(position)
                        musicSrv!!.playSong()

                        log.d("playbak is paused $playbackPaused")
                        if (playbackPaused){
                            setController()
                            playbackPaused = false
                        }
//                        controller!!.show()
                    }
                }else{
                    if(controller == null) setController()
                    controller!!.setLoading(false);

                    if(MusicService.PLAY_STATUS == MusicService.PAUSED && MusicService.PLAYING_SONG_URL == listSong.get(position).middlePath){
                        start()
                    }else{

                        if(controller == null) setController()
                        controller!!.setLoading(true);

                        musicSrv!!.setList(listSong)
                        musicSrv!!.setSong(position)
                        musicSrv!!.playSong()
                        log.d("playbak is paused $playbackPaused")
                        if (playbackPaused){
                            setController()
                            playbackPaused = false
                        }
                    }
//                    controller!!.show()

                }


            }else{
                Toast.makeText(Base.get,Base.get.resources.getString(R.string.error_something), Toast.LENGTH_SHORT).show()
            }


            adapter!!.notifyDataSetChanged()



            FeedFragment.playedSongPosition = position
//        }catch (e :Exception){
//            log.d("$e")
//            Toast.makeText(Base.get,Base.get.resources.getString(R.string.error_something), Toast.LENGTH_SHORT).show()
//
//        }
    }


    override fun onStart() {
        super.onStart()
        if (playIntent == null) {

            playIntent = Intent(this, MusicService::class.java)
            this.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            this.startService(playIntent)

        }
    }

    override fun canPause(): Boolean {
        return true
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getAudioSessionId(): Int {
        return 0
    }

    override fun getBufferPercentage(): Int {
        return 0
    }

    override fun getCurrentPosition(): Int {
        if (musicSrv != null && musicBound && musicSrv!!.isPng())
            return musicSrv!!.getPosn()
        else
            return 0
    }

    override fun getDuration(): Int {
        if (musicSrv != null && musicBound && musicSrv!!.isPng())
            return musicSrv!!.getDur()
        else
            return 0
    }

    override fun isPlaying(): Boolean {
        if (musicSrv != null && musicBound)
            return musicSrv!!.isPng()
        return false
    }

    override fun pause() {
        playbackPaused = true
        musicSrv!!.pausePlayer()
        if(controller != null) controller!!.setLoading(false);


        MainActivity.musicSubject.playMeause("")
    }

    override fun seekTo(pos: Int) {
        musicSrv!!.seek(pos)

    }

    override fun start() {
        if(musicSrv != null && musicSrv!!.songs != null) {
            if (musicSrv!!.songs.size > 0) {
                musicSrv!!.go()
                MainActivity.musicSubject.playMeause("")
            }
        }else{
            if (adapter != null && adapter!!.audios != null && adapter!!.audios.size > 0){
                playClick(adapter!!.audios,0)
                MainActivity.musicSubject.playMeause("")

            }
        }
    }
    override fun goPlayList() {

    }

    private fun setController() {
        if (controller == null){
            controller = MusicController(this,true)
            //set previous and next button listeners
            controller!!.setPrevNextListeners({ playNext() }, { playPrev() })
            //set and show
            controller!!.setMediaPlayer(this)
            controller!!.setAnchorView(findViewById(R.id.playlistRoot))
            controller!!.setEnabled(true)
        }
    }

    private fun playNext() {
        musicSrv!!.playNext()

        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        MainActivity.musicSubject.playMeause("")

    }

    private fun playPrev() {
        musicSrv!!.playNext()

        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        MainActivity.musicSubject.playMeause("")

    }

    override fun onPause() {
        super.onPause()
        paused = true

    }

    override fun onResume() {
        super.onResume()
        log.d("onresume")
        LocalBroadcastManager.getInstance(this).registerReceiver(musicReceiver, IntentFilter(MusicService.ACTION_PLAY_TOGGLE))
        if (paused) {
            setController()
            paused = false
        }
    }

    val musicReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (MusicService.CONTROL_PRESSED != -1){
                try {

                    adapter!!.notifyDataSetChanged()

                    MusicService.CONTROL_PRESSED = -1
                } catch (e: Exception) {

                }
            }
            if(controller != null) controller!!.show(0)
        }

    }

    override fun onStop() {
        if (controller != null){
            controller!!.hide()
        }
        super.onStop()
    }

    override fun onDestroy() {
        stopService(playIntent)
        musicSrv = null
        MainActivity.musicSubject.unsubscribe(this)
        super.onDestroy()
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