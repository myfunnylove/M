package locidnet.com.marvarid.ui.activity

import android.content.*
import android.os.Handler
import android.os.IBinder
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_playlist.*
import org.json.JSONObject
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
import locidnet.com.marvarid.pattern.MControlObserver.MusicSubject
import locidnet.com.marvarid.pattern.builder.EmptyContainer
import locidnet.com.marvarid.pattern.builder.ErrorConnection
import locidnet.com.marvarid.player.PlayerService
import locidnet.com.marvarid.resources.utils.*
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.ui.fragment.FeedFragment
import javax.inject.Inject

/**
 * Created by Sarvar on 09.09.2017.
 */
class PlaylistActivity : BaseActivity(),Viewer , MusicPlayerListener,MusicControlObserver {



    var drawingStartLocation               = 0
    var user  = Base.get.prefs.getUser()
    val model = Model()

    //service
    private var playIntent: Intent? = null
    //binding
    private var musicBound = false
    var musicSrv:PlayerService? = null
    internal var playerServiceBinder: PlayerService.PlayerServiceBinder? = null
    internal var mediaController: MediaControllerCompat? = null
    //controller

    //activity and playback pause flags
    private var paused = false
    var playbackPaused = false
    @Inject
    lateinit var presenter:Presenter

    @Inject
    lateinit var errorConn: ErrorConnection

    lateinit var adapter:PostAudioGridAdapter
    lateinit var emptyContainer: EmptyContainer
    var songPosition = -1;
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
        MainActivity.musicSubject!!.subscribe(this)

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


        errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
            override fun connected() {
                log.d("connected")

                val js =  JS.get()
                presenter.requestAndResponse(js, Http.CMDS.GET_PLAYLIST)

            }

            override fun disconnected() {
                log.d("disconnected")


            }

        })


        bindService(Intent(this, PlayerService::class.java), object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                playerServiceBinder = service as PlayerService.PlayerServiceBinder
                try {
                    mediaController = MediaControllerCompat(this@PlaylistActivity, playerServiceBinder!!.getMediaSessionToken())
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
                playerServiceBinder = null
                mediaController = null
            }
        }, Context.BIND_AUTO_CREATE)



        playPause.setOnClickListener {
            if (songPosition != -1){
                playClick(adapter.audios,songPosition)
            }else{
                if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PLAYING ) {
                    playPause.setImageDrawable(VectorDrawableCompat.create(resources,R.drawable.notif_play,theme))

                    mediaController!!.getTransportControls().pause()

                } else {
                    playPause.setImageDrawable(VectorDrawableCompat.create(resources,R.drawable.notif_pause,theme))

                  if (PlayerService.songs != null && PlayerService.songs.size > 0){
                      mediaController!!.getTransportControls().play()
                  }else{
                      playClick(adapter.audios,0)

                  }

                }
            }
        }
        prev.setOnClickListener {
            if(musicSrv!!.currentAudio != null){
                songTitle.text = musicSrv!!.currentAudio.title
                artist.text = musicSrv!!.currentAudio.artist
            }
            if (mediaController != null) mediaController!!.transportControls.skipToPrevious()

        }

        next.setOnClickListener{
            if(musicSrv!!.currentAudio != null){
                songTitle.text = musicSrv!!.currentAudio.title
                artist.text = musicSrv!!.currentAudio.artist
            }
            if (mediaController != null) mediaController!!.transportControls.skipToNext()

        }
    }






    override fun playClick(listSong: ArrayList<Audio>, position: Int){
        PlayerService.songs = listSong
        PlayerService.songPosn = position
        log.d("PLAYCLICKED")
        if (mediaController != null) {

            if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PLAYING &&
                    PlayerService.PLAYING_SONG_URL == listSong.get(position).middlePath) {
                playPause.setImageDrawable(VectorDrawableCompat.create(resources,R.drawable.notif_play,theme))

                mediaController!!.getTransportControls().pause()

            } else if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PAUSED &&
                    PlayerService.PLAYING_SONG_URL == listSong.get(position).middlePath) {
                playPause.setImageDrawable(VectorDrawableCompat.create(resources,R.drawable.notif_pause,theme))

                mediaController!!.getTransportControls().play()

            } else if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PLAYING &&
                    PlayerService.PLAYING_SONG_URL != listSong.get(position).middlePath) {
                playPause.setImageDrawable(VectorDrawableCompat.create(resources,R.drawable.notif_pause,theme))

                mediaController!!.getTransportControls().play()

            } else if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PAUSED &&
                    PlayerService.PLAYING_SONG_URL != listSong.get(position).middlePath) {
                playPause.setImageDrawable(VectorDrawableCompat.create(resources,R.drawable.notif_pause,theme))

                mediaController!!.getTransportControls().play()

            }else {
                playPause.setImageDrawable(VectorDrawableCompat.create(resources,R.drawable.notif_pause,theme))

                mediaController!!.getTransportControls().play()

            }

                songTitle.text = listSong.get(position).title
                artist.text = listSong.get(position).artist

        }else{
            Toast.makeText(Base.get,Base.get.resources.getString(R.string.error_something),Toast.LENGTH_SHORT).show()
        }
    }





    val musicConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as PlayerService.PlayerServiceBinder
            musicSrv = binder.service
            musicBound = true

        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }
    override fun onStart() {
        super.onStart()
        if (playIntent == null) {

            playIntent = Intent(this, PlayerService::class.java)
            this.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            this.startService(playIntent)

        }
        updateControl();
    }
    fun updateControl(){
        Handler().postDelayed({
            if(musicSrv != null && musicSrv!!.currentState == PlaybackStateCompat.STATE_PLAYING){
                if(musicSrv!!.currentAudio != null){
                    songTitle.text = musicSrv!!.currentAudio.title
                    artist.text = musicSrv!!.currentAudio.artist
                }

                playPause.setImageDrawable(VectorDrawableCompat.create(resources,R.drawable.notif_pause,theme))
            }else{
                playPause.setImageDrawable(VectorDrawableCompat.create(resources,R.drawable.notif_play,theme))

            }
        },1000)
    }
    override fun playPause(id: String) {
     adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        MainActivity.musicSubject!!.unsubscribe(this)
        songPosition = -1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Const.SESSION_OUT || resultCode == Const.SESSION_OUT){
            setResult(Const.SESSION_OUT)
            finish()
        }
    }
}