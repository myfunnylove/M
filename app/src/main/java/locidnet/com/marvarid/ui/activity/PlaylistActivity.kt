package locidnet.com.marvarid.ui.activity

import android.content.*
import android.os.Handler
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.PlaybackControlView
import com.google.android.exoplayer2.util.RepeatModeUtil
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_playlist.*
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.PostAudioGridAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.connectors.MusicPlayerListener
import locidnet.com.marvarid.di.DaggerMVPComponent
import locidnet.com.marvarid.di.modules.ErrorConnModule
import locidnet.com.marvarid.di.modules.MVPModule
import locidnet.com.marvarid.di.modules.PresenterModule
import locidnet.com.marvarid.model.Audio
import locidnet.com.marvarid.model.Features
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.mvp.Presenter
import locidnet.com.marvarid.mvp.Viewer
import locidnet.com.marvarid.pattern.MControlObserver.MusicControlObserver
import locidnet.com.marvarid.pattern.builder.EmptyContainer
import locidnet.com.marvarid.pattern.builder.ErrorConnection
import locidnet.com.marvarid.player.PlayerService
import locidnet.com.marvarid.resources.utils.*
import locidnet.com.marvarid.rest.Http
import javax.inject.Inject


class PlaylistActivity : BaseActivity(),Viewer , MusicPlayerListener,MusicControlObserver {


    var user  = Base.get.prefs.getUser()
    val model = Model()

    private var musicBound = false
    var musicSrv:PlayerService? = null
    internal var playerServiceBinder: PlayerService.PlayerServiceBinder? = null
    internal var mediaController: MediaControllerCompat? = null
    //controller

    @Inject
    lateinit var presenter:Presenter

    @Inject
    lateinit var errorConn: ErrorConnection

    lateinit var adapter:PostAudioGridAdapter
    lateinit var emptyContainer: EmptyContainer
    private lateinit var songTitle:TextView
    private lateinit var artist:TextView
    private lateinit var exoPlay:AppCompatImageView
    private lateinit var exoPause:AppCompatImageView
    private lateinit var exoPrev: AppCompatImageView
    private lateinit var exoNext:AppCompatImageView
    private lateinit var exoDuration:TextView
    private lateinit var exoPosition:TextView
    private lateinit var exoProgress: DefaultTimeBar
    private var songPosition = -1
    override fun initProgress() {

    }

    override fun showProgress() {
        progressLay.visibility = View.VISIBLE
        emptyContainer.hide()

    }

    override fun hideProgress() {
        progressLay.visibility = View.GONE
        emptyContainer.hide()

    }

    override fun onSuccess(from: String, result: String) {
        log.d("from $from result $result")

        progressLay.visibility = View.GONE

        val features = Gson().fromJson(result,Features::class.java)
        features.audios.forEach {
            audio -> audio.isFeatured = 1
            audio.middlePath.replace(Const.AUDIO.MEDIUM, Prefs.Builder().audioRes())
        }
        adapter =  PostAudioGridAdapter(this,features.audios,this,model,true)

        list.adapter = adapter
    }

    override fun onFailure(from: String, message: String, erroCode: String) {
        Toaster.errror(message)

        progressLay.visibility = View.GONE
        emptyContainer.show()


    }

    override fun getLayout(): Int  = R.layout.activity_playlist

    override fun initView() {
        Const.TAG = "PlaylistActivity"

        DaggerMVPComponent
                .builder()
                .mVPModule(MVPModule(this, Model(), this))
                .presenterModule(PresenterModule())
                .errorConnModule(ErrorConnModule(this,true))
                .build()
                .inject(this)
        if(MainActivity.musicSubject != null) MainActivity.musicSubject!!.subscribe(this)


        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = resources.getString(R.string.my_playlist)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {

            onBackPressed()

        }
        MainActivity.musicSubject!!.subscribe(this)

        emptyContainer = EmptyContainer.Builder()
                .setIcon(R.drawable.music_select)
                .setText(R.string.error_empty_playlist)
                .initLayoutForActivity(this)
                .build()


        list.layoutManager = LinearLayoutManager(this)
        list.setHasFixedSize(true)


//        errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
//            override fun connected() {
//                log.d("connected")

                val js =  JS.get()
                presenter.requestAndResponse(js, Http.CMDS.GET_PLAYLIST)

//            }
//
//            override fun disconnected() {
//                log.d("disconnected")
//
//
//            }
//
//        })


        bindService(Intent(this, PlayerService::class.java), object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                playerServiceBinder = service as PlayerService.PlayerServiceBinder
                musicSrv = service.service
                player.player = musicSrv?.exoPlayer

                player.show()

                player.setShowMultiWindowTimeBar(true)


                        musicBound = true
                try {
                    mediaController = MediaControllerCompat(this@PlaylistActivity, playerServiceBinder!!.mediaSessionToken)
                    mediaController!!.registerCallback(object : MediaControllerCompat.Callback() {
                        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                            log.d("MEDIACONTROLL $state")
                            if (state == null)
                                return
//                            val playing = state.state == PlaybackStateCompat.STATE_PLAYING
                        }




                    })


                    updateControl()
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

        songTitle = player.rootView!!.findViewById<TextView>(R.id.songTitle);
        artist = player.rootView!!.findViewById<TextView>(R.id.artist);
        exoDuration = player.rootView!!.findViewById<TextView>(R.id.exo_duration);
        exoPosition = player.rootView!!.findViewById<TextView>(R.id.exo_position);
        exoProgress = player.rootView!!.findViewById<DefaultTimeBar>(R.id.exo_progress);
        exoPlay = player.rootView!!.findViewById<AppCompatImageView>(R.id.exo_play);
        exoPause = player.rootView!!.findViewById<AppCompatImageView>(R.id.exo_pause);
        exoPrev = player.rootView!!.findViewById<AppCompatImageView>(R.id.prev);
        exoNext = player.rootView!!.findViewById<AppCompatImageView>(R.id.next);
        exoPlay.setOnClickListener {



                  if (PlayerService.songs != null && PlayerService.songs.size > 0){
                      mediaController!!.transportControls.play()
                  }else{
                          playClick(adapter.audios,0)

                  }



            Handler().postDelayed({
                if(musicSrv!!.currentAudio != null){
                    songTitle.text = if (musicSrv!!.currentAudio.title.isEmpty()) Base.get.resources.getString(R.string.unknown) else musicSrv!!.currentAudio.title

                    artist.text = if (musicSrv!!.currentAudio.artist.isEmpty()) Base.get.resources.getString(R.string.unknown) else musicSrv!!.currentAudio.artist
                }
            },500)
        }

        exoPause.setOnClickListener {
            if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PLAYING ) {

                mediaController!!.getTransportControls().pause()

            }

            Handler().postDelayed({
                if(musicSrv!!.currentAudio != null){
                    songTitle.text = if (musicSrv!!.currentAudio.title.isEmpty()) Base.get.resources.getString(R.string.unknown) else musicSrv!!.currentAudio.title

                    artist.text = if (musicSrv!!.currentAudio.artist.isEmpty()) Base.get.resources.getString(R.string.unknown) else musicSrv!!.currentAudio.artist
                }
            },500)
        }

        exoPrev.setOnClickListener {

            if (mediaController != null) mediaController!!.transportControls.skipToPrevious()
            Handler().postDelayed({
                if (musicSrv!!.currentAudio != null) {

                    songTitle.text = if (musicSrv!!.currentAudio.title.isEmpty()) Base.get.resources.getString(R.string.unknown) else musicSrv!!.currentAudio.title

                    artist.text = if (musicSrv!!.currentAudio.artist.isEmpty()) Base.get.resources.getString(R.string.unknown) else musicSrv!!.currentAudio.artist

                }
            },500)

        }
        exoNext.setOnClickListener{

            if (mediaController != null) mediaController!!.transportControls.skipToNext()

            Handler().postDelayed({
                if(musicSrv!!.currentAudio != null){
                    songTitle.text = if (musicSrv!!.currentAudio.title.isEmpty()) Base.get.resources.getString(R.string.unknown) else musicSrv!!.currentAudio.title

                    artist.text = if (musicSrv!!.currentAudio.artist.isEmpty()) Base.get.resources.getString(R.string.unknown) else musicSrv!!.currentAudio.artist
                }
            },500)
        }

        player.setControlDispatcher(object : PlaybackControlView.ControlDispatcher{
            override fun dispatchSeekTo(player: Player?, windowIndex: Int, positionMs: Long): Boolean {
                player?.seekTo(positionMs)
                return true
            }

            override fun dispatchSetPlayWhenReady(player: Player?, playWhenReady: Boolean): Boolean =
                    true

            override fun dispatchSetRepeatMode(player: Player?, repeatMode: Int): Boolean {
                player!!.repeatMode = Player.REPEAT_MODE_OFF

                return true

            }

        })

        player.repeatToggleModes = RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE
    }






    override fun playClick(listSong: ArrayList<Audio>, position: Int){
        PlayerService.songs = listSong
        PlayerService.songPosn = position
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

            songTitle.text = if (listSong[position].title.isEmpty()) Base.get.resources.getString(R.string.unknown) else listSong[position].title

            artist.text = if (listSong[position].artist.isEmpty()) Base.get.resources.getString(R.string.unknown) else listSong[position].artist

        }else{
            Toast.makeText(Base.get,Base.get.resources.getString(R.string.error_something),Toast.LENGTH_SHORT).show()
        }
    }







    fun updateControl(){
        Handler().postDelayed({
            if(musicSrv != null && musicSrv!!.currentState == PlaybackStateCompat.STATE_PLAYING){
                if(musicSrv!!.currentAudio != null){
                    songTitle.text = if (musicSrv!!.currentAudio.title.isEmpty()) Base.get.resources.getString(R.string.unknown) else musicSrv!!.currentAudio.title

                    artist.text = if (musicSrv!!.currentAudio.artist.isEmpty()) Base.get.resources.getString(R.string.unknown) else musicSrv!!.currentAudio.artist
                }

            }
            else if(musicSrv != null && musicSrv!!.currentState == PlaybackStateCompat.STATE_PAUSED){
                if(musicSrv!!.currentAudio != null){
                    songTitle.text = if (musicSrv!!.currentAudio.title.isEmpty()) Base.get.resources.getString(R.string.unknown) else musicSrv!!.currentAudio.title

                    artist.text = if (musicSrv!!.currentAudio.artist.isEmpty()) Base.get.resources.getString(R.string.unknown) else musicSrv!!.currentAudio.artist
                }

            }
            else{

            }
        },1000)
    }
    override fun playPause(id: String) {
        if(musicSrv!!.currentAudio != null){
            songTitle.text = if (musicSrv!!.currentAudio.title.isEmpty()) Base.get.resources.getString(R.string.unknown) else musicSrv!!.currentAudio.title

            artist.text = if (musicSrv!!.currentAudio.artist.isEmpty()) Base.get.resources.getString(R.string.unknown) else musicSrv!!.currentAudio.artist
        }

        adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        MainActivity.musicSubject!!.unsubscribe(this)
        songPosition = -1
        presenter.ondestroy()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Const.SESSION_OUT || resultCode == Const.SESSION_OUT){
            setResult(Const.SESSION_OUT)
            finish()
        }
    }
}