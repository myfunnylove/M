package locidnet.com.marvarid.ui.activity

import android.content.*
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_comment.*
import org.json.JSONObject
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.PostAudioGridAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.musicplayer.MusicController
import locidnet.com.marvarid.musicplayer.MusicService
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
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Toaster
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.ui.fragment.FeedFragment
import javax.inject.Inject

/**
 * Created by Sarvar on 09.09.2017.
 */
class PlaylistActivity : BaseActivity(),Viewer , MusicController.MediaPlayerControl, MusicPlayerListener,MusicControlObserver {



    var drawingStartLocation               = 0
    var user  = Base.get.prefs.getUser()
    val model = Model()
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
    @Inject
    lateinit var presenter:Presenter

    @Inject
    lateinit var errorConn: ErrorConnection

    lateinit var adapter:PostAudioGridAdapter
    lateinit var emptyContainer: EmptyContainer

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
        }
        adapter =  PostAudioGridAdapter(this,features.audios,this,model,true)
        try{
            setController()
            controller!!.show()
        }catch (e:Exception){}
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
        MainActivity.musicSubject.subscribe(this)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setTitle(resources.getString(R.string.my_playlist))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {

            onBackPressed()

        }
        emptyContainer = EmptyContainer.Builder()
                .setIcon(R.drawable.comment_white)
                .setText(R.string.error_empty_playlist)
                .initLayoutForActivity(this)
                .build()


        list.layoutManager = LinearLayoutManager(this)
        list.setHasFixedSize(true)


        errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
            override fun connected() {
                log.d("connected")

                val js = JSONObject()
                js.put("user_id", user.userId)
                js.put("session", user.session)
                presenter.requestAndResponse(js, Http.CMDS.GET_PLAYLIST)

            }

            override fun disconnected() {
                log.d("disconnected")


            }

        })
    }

    override fun activityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
        try{

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


            adapter.notifyDataSetChanged()



            FeedFragment.playedSongPosition = position
        }catch (e :Exception){

        }
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
            if (adapter != null && adapter.audios != null && adapter.audios.size > 0){
                playClick(adapter.audios,0)
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
            controller!!.setPrevNextListeners(View.OnClickListener { playNext() }, View.OnClickListener { playPrev() })
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

                    adapter.notifyDataSetChanged()

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
     adapter.notifyDataSetChanged()
    }
}