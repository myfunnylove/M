package locidnet.com.marvarid.ui.activity

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.LocalBroadcastManager
import android.view.Menu
import android.view.MenuItem
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_follow.*
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.connectors.GoNext
import locidnet.com.marvarid.connectors.MusicPlayerListener
import locidnet.com.marvarid.connectors.SignalListener
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
import locidnet.com.marvarid.pattern.builder.ErrorConnection
import locidnet.com.marvarid.resources.utils.*
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.ui.fragment.*
import org.json.JSONObject
import javax.inject.Inject

class FollowActivity : BaseActivity(),
        GoNext,Viewer ,
        MusicController.MediaPlayerControl,
        MusicPlayerListener,
        SignalListener{

    companion object {
        val LIST_T     = 1
        val PROFIL_T   = 2
        val FOLLOWERS  = 3
        val FOLLOWING  = 4
        val BLOCKED_ME = 5
        val TYPE      = "type"

        var start     = 0
        var end       = 20

        var SHOW_POST = ""
    }

    var manager:FragmentManager?        = null
    var transaction:FragmentTransaction?= null
    var menu:Menu? = null
    @Inject
    lateinit var presenter:Presenter

    @Inject
    lateinit var errorConn: ErrorConnection

    var user                            = Base.get.prefs.getUser()
    var profilFragment:ProfileFragment? = null
    var followersFragment:FFFFragment?  = null
    var blocMeFragment:BlockMeFragment? = null
    var userID                          = ""
   // var jsUserData:JSONObject? = null
    private var controller: MusicController? = null
    var userInfo:UserInfo? = null
    var bundle:Bundle? = null
    override fun getLayout(): Int = R.layout.activity_follow

    override fun initView() {
        Const.TAG = "FollowActivity"

        DaggerMVPComponent
                .builder()
                .mVPModule(MVPModule(this, Model(),this))
                .presenterModule(PresenterModule())
                .errorConnModule(ErrorConnModule(this,true))
                .build()
                .inject(this)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)

        if (intent.getIntExtra(TYPE,-1) == PROFIL_T || intent.getIntExtra(TYPE,-1) == BLOCKED_ME){
            supportActionBar!!.setTitle(intent.extras.getString("username"))
            setController()

        }
        else if (intent.getIntExtra(TYPE,-1) == FOLLOWING){

            supportActionBar!!.setTitle(resources.getString(R.string.following))

        }else if (intent.getIntExtra(TYPE,-1) == FOLLOWERS){
            supportActionBar!!.setTitle(resources.getString(R.string.followers))

        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {

            onBackPressed()

        }

        if (intent.getIntExtra(TYPE,-1) == PROFIL_T ){
            val reqObj = JSONObject()
            reqObj.put("user_id",user.userId)
            reqObj.put("user",   intent.extras.getString("user_id"))
            reqObj.put("session",user.session)


            log.d("send data for user info data: ${reqObj}")
            presenter.requestAndResponse(reqObj,Http.CMDS.USER_INFO)
        }else{
            showFragment(intent.getIntExtra(TYPE,-1))

        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {


        if(intent.extras.getString("user_id") != user.userId)
        {
            menuInflater.inflate(R.menu.menu_block_user,menu)
            this.menu = menu
            return true

        }else{
            return false
        }





    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {


        val blockJS = JSONObject()
        blockJS.put("user_id",user.userId)
        blockJS.put("session",user.session)
        blockJS.put("user",intent.extras.getString("user_id"))

        presenter.requestAndResponse(blockJS,Http.CMDS.BLOCK_USER)

        return true
    }

    fun showFragment(int:Int){
        log.d("show fragment $int")
        if (manager == null) manager = supportFragmentManager

        transaction = manager!!.beginTransaction()


        if (int == LIST_T){

            val searchFragment = SearchFragment.newInstance()
            searchFragment.connect(this)

            transaction!!.add(R.id.container,searchFragment,SearchFragment.TAG)

        }else if (int == PROFIL_T){

            if (profilFragment == null){
                profilFragment = ProfileFragment.newInstance(intent.extras)
                profilFragment!!.connectAudioPlayer(this)

                profilFragment!!.connect(this)
                profilFragment!!.signal(this)
            }
            userID = intent.extras.getString("user_id")

            log.d("profile type ${intent.extras.getString(ProfileFragment.F_TYPE)}")
            if (followersFragment != null && followersFragment!!.isAdded && !followersFragment!!.isHidden) transaction!!.hide(followersFragment)
            if (profilFragment!!.isAdded) {
                if (profilFragment!!.isHidden)
                    transaction!!.show(profilFragment)
            } else
                transaction!!.add(R.id.container, profilFragment, ProfileFragment.TAG)
            log.d("json chiqdi")
            log.d(intent.extras.toString())










        }else if (int == BLOCKED_ME){

            if (blocMeFragment == null){
                blocMeFragment = BlockMeFragment.newInstance(intent.extras)

            }

            if (followersFragment != null && followersFragment!!.isAdded && !followersFragment!!.isHidden) transaction!!.hide(followersFragment)
            if (profilFragment != null && profilFragment!!.isAdded && !profilFragment!!.isHidden) transaction!!.hide(profilFragment)

            if (blocMeFragment!!.isAdded) {
                if (blocMeFragment!!.isHidden)
                    transaction!!.show(blocMeFragment)
            } else
                transaction!!.add(R.id.container, blocMeFragment, BlockMeFragment.TAG)

        }else {

            userID = intent.extras.getString("user_id")
            val header = Bundle()

            /*GET FOLLOWERS OR FOLLOWING*/
            header.putString("header", if (int == FOLLOWING) Base.get.resources.getString(R.string.following) else Base.get.resources.getString(R.string.followers))

            if (followersFragment == null){
                followersFragment = FFFFragment.newInstance(header)
                followersFragment!!.connect(this)
            }
            if (profilFragment != null && profilFragment!!.isAdded && !profilFragment!!.isHidden) transaction!!.hide(profilFragment)
            if (followersFragment!!.isAdded) {
                if (followersFragment!!.isHidden)
                    transaction!!.show(followersFragment)
            } else
                transaction!!.add(R.id.container, followersFragment, FFFFragment.TAG)

//            transaction!!.add(R.id.container,followersFragment,FFFFragment.TAG)

            errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                override fun connected() {
                    log.d("connected")
                    val obj = JSONObject()
                    obj.put("user_id",user.userId)
                    obj.put("user",   userID)
                    obj.put("session",user.session)
                    obj.put("start",  MainActivity.startFollowing)
                    obj.put("end",    MainActivity.endFollowing)


                    /*GET FOLLOWERS OR FOLLOWING*/
                    presenter.requestAndResponse(obj, if (int == FOLLOWING) Http.CMDS.GET_FOLLOWING else Http.CMDS.GET_FOLLOWERS)
                }

                override fun disconnected() {
                    log.d("disconnected")


                }

            })
        }
            transaction!!.addToBackStack("")
            transaction!!.commit()
    }

    override fun goNext(to: Int, data: String) {
        when(to){
            Const.REFRESH_PROFILE_FEED ->{


                errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                    override fun connected() {
                        log.d("connected")
                        val reqObj = JSONObject()
                        reqObj.put("user_id", user.userId)
                        reqObj.put("session", user.session)
                        reqObj.put("user",    userID)
                        reqObj.put("start",   "0")
                        reqObj.put("end",     end)

                        presenter.requestAndResponse(reqObj, Http.CMDS.MY_POSTS)
                    }

                    override fun disconnected() {
                        log.d("disconnected")


                    }

                })
            }

            Const.TO_FOLLOWERS -> showFragment(FOLLOWERS)

            Const.TO_FOLLOWING -> showFragment(FOLLOWING)

            Const.PROFIL_PAGE -> {
                errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                    override fun connected() {
                        log.d("connected")
                        val reqObj = JSONObject()
                        reqObj.put("user_id", user.userId)
                        reqObj.put("session", user.session)
                        reqObj.put("user",    userID)
                        reqObj.put("start",   data)
                        reqObj.put("end",     end)

                        presenter.requestAndResponse(reqObj, Http.CMDS.MY_POSTS)
                    }

                    override fun disconnected() {
                        log.d("disconnected")


                    }

                })


             }


        }
    }

    override fun donGo(why: String) {
    }

    override fun initProgress() {

    }

    override fun showProgress() {
    }

    override fun hideProgress() {
    }

    override fun onSuccess(from: String, result: String) {

        log.d("Profil postlani olib kelindi $result")

        when(from){
            Http.CMDS.MY_POSTS -> {

                try{
                    val postList: PostList = Gson().fromJson(result, PostList::class.java)


                    if (postList.posts.size > 0){
                        profilFragment!!.initBody(postList)
                    }

                }catch (e:Exception){



                }


            }

            Http.CMDS.GET_FOLLOWERS -> {
                                        val follow = Gson().fromJson<Followers>(result, Followers::class.java)


                                        log.d("followersla olindi -> ${follow.users}")
                                        if(manager!!.findFragmentByTag(FFFFragment.TAG) != null){

                                            followersFragment!!.swapList(follow.users)
                                            FFFFragment.followersCount = MyProfileFragment.FOLLOWING.toInt()
                                            log.d("get followers -> ${FFFFragment.followersCount}")
                                        }
            }
            Http.CMDS.GET_FOLLOWING -> {
                                        val follow = Gson().fromJson<Following>(result, Following::class.java)


                                        log.d("followingla olindi -> ${follow.users}")
                                        if(manager!!.findFragmentByTag(FFFFragment.TAG) != null){

                                            followersFragment!!.swapList(follow.users)
                                            FFFFragment.followersCount = MyProfileFragment.FOLLOWING.toInt()
                                            log.d("get following -> ${FFFFragment.followersCount}")

                                        }
            }

            Http.CMDS.USER_INFO -> {

                userInfo = Gson().fromJson(result, UserInfo::class.java)

                val otherUser = userInfo!!.user
                bundle = Bundle()

                bundle!!.putString("username", otherUser.info.username)
                bundle!!.putString("photo",    otherUser.info.photoOrg)
                bundle!!.putString("user_id",  otherUser.info.user_id)


               if(menu != null){
                   if(otherUser.block_it == "0")
                       this.menu!!.findItem(R.id.blockUser).title = Functions.getString(R.string.blockUser)
                   else
                       this.menu!!.findItem(R.id.blockUser).title = Functions.getString(R.string.unblock)
               }

                log.d("result of userinfo $otherUser")
                val fType = Functions.selectFollowType(userInfo!!)
                if (fType != "-1") {
                    showFragment(PROFIL_T)



                }else{
                    intent.putExtras(bundle)
                    showFragment(BLOCKED_ME)
                }


            }

            Http.CMDS.BLOCK_USER -> {
               try{
                   val blockRes = JSONObject(result)
                   val item = menu!!.findItem(R.id.blockUser)

                   if(blockRes.opt("result") == "block"){
                       item.title = Functions.getString(R.string.unblock)
                   }else{
                       item.title = Functions.getString(R.string.blockUser)

                   }
               }catch (e:Exception){

               }
            }

        }


    }

    override fun onFailure(from: String, message: String, erroCode: String) {

        Toaster.errror(message)

        when(from ){

            Http.CMDS.GET_FOLLOWING     -> Handler().postDelayed({followersFragment!!.failedGetList(message)},1500)
            Http.CMDS.GET_FOLLOWERS     -> Handler().postDelayed({followersFragment!!.failedGetList(message)},1500)
        }
    }

    override fun onBackPressed() {
        if (manager!!.backStackEntryCount == 1){


            this.finish()
        }else if(Prefs.Builder().getUser().session != ""){

            super.onBackPressed()
        }else{
            setResult(Const.SESSION_OUT)
            this.finish()
        }
    }

    override fun activityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Const.SESSION_OUT || resultCode == Const.SESSION_OUT){
            setResult(Const.SESSION_OUT)
            finish()
        }

    }



    /*
   *
   * AUDIO
   *
   * */

    private fun setController() {
        if (controller == null){
            controller = MusicController(this,false)
            //set previous and next button listeners
            controller!!.setPrevNextListeners({ playNext() }, { playPrev() },{ goPlayList() })
            //set and show
            controller!!.setMediaPlayer(this)
            controller!!.setAnchorView(findViewById(R.id.container))
            controller!!.setEnabled(true)
        }
    }

    private fun playNext() {

        musicSrv!!.playNext()

        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        controller!!.show()
        controller!!.setLoading(true);

        try {
            if (FeedFragment.cachedSongAdapters != null) {
                FeedFragment.cachedSongAdapters!!.get(FeedFragment.playedSongPosition)!!.notifyDataSetChanged()
            }
        } catch (e: Exception) {

        }
    }

    private fun playPrev() {
        musicSrv!!.playPrev()
        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        controller!!.show()
        controller!!.setLoading(true);

        try {

            if (FeedFragment.cachedSongAdapters != null) {
                FeedFragment.cachedSongAdapters!!.get(FeedFragment.playedSongPosition)!!.notifyDataSetChanged()
            }
        } catch (e: Exception) {

        }

    }

    override fun playClick(listSong: ArrayList<Audio>, position: Int){
        if (musicSrv != null){
            log.d("PLAYIN SONG ${musicSrv!!.isPng}")

            if(musicSrv!!.isPng){
                log.d("PLAYIN SONG in fragment  2 -> ${listSong.get(position).middlePath == MusicService.PLAYING_SONG_URL}")
                if (MusicService.PLAYING_SONG_URL == listSong.get(position).middlePath){
                    pause()
                }else{
                    if(controller == null)
                    {
                        setController()
                        controller!!.show()
                    }
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

                if(MusicService.PLAY_STATUS == MusicService.PAUSED && MusicService.PLAYING_SONG_URL == listSong.get(position).middlePath){
                    start()
                }else{
                    if(controller == null)
                    {
                        setController()
                        controller!!.show()
                    }
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

//                if (!musicSrv!!.isPng || MusicService.PLAYING_SONG_URL != listSong.get(position).middlePath){
//                     musicSrv!!.setList(listSong)
//                     musicSrv!!.setSong(position)
//                     musicSrv!!.playSong()
//                    log.d("playbak is paused $playbackPaused")
//                    if (playbackPaused){
//                        setController()
//                        playbackPaused = false
//                    }
//                    controller!!.show()
//                }else{
//
//                    pause()
//                }
        }else{
            Toaster.errror(resources.getString(R.string.error_something))

        }
    }

    override fun onStop() {
        if (controller != null){
            controller!!.hide()
        }
        super.onStop()
    }

    val musicReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            if (MusicService.CONTROL_PRESSED != -1){
                try {

                    if (FeedFragment.cachedSongAdapters != null) {

                        FeedFragment.cachedSongAdapters!!.get(FeedFragment.playedSongPosition)!!.notifyDataSetChanged()
                    }
                    MusicService.CONTROL_PRESSED = -1
                } catch (e: Exception) {

                }
            }
            if(controller != null) controller!!.show(0)
        }

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

    override fun onPause() {
        super.onPause()
        paused = true

    }
    //song list variables
    private var songList: ArrayList<Audio>? = null

    //service
    private var musicSrv: MusicService? = null
    private var playIntent: Intent? = null
    //binding
    private var musicBound = false

    //controller
//    private var controller: MusicController? = null

    //activity and playback pause flags
    private var paused = false
    var playbackPaused = false


    //connect to the service
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

    //start and bind the service when the activity starts
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

    override fun canSeekForward(): Boolean = true

    override fun getAudioSessionId(): Int = 0

    override fun getBufferPercentage(): Int = 0

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
        if(controller != null){
            controller!!.show()

            controller!!.setLoading(false)
        }
    }

    override fun seekTo(pos: Int) {
        musicSrv!!.seek(pos)
    }

    override fun start() {
        musicSrv!!.go()
    }

    override fun goPlayList() {

        startActivity(Intent(this,PlaylistActivity::class.java))
    }






    override fun onDestroy() {
        this.stopService(playIntent)
        musicSrv = null
        FeedFragment.cachedSongAdapters = null
        FeedFragment.playedSongPosition = -1
        super.onDestroy()
    }


    override fun turnOn() {
        val otherUser = userInfo!!.user

        val fType = Functions.selectFollowType(userInfo!!)
        bundle!!.putString(ProfileFragment.F_TYPE,fType)
        intent.putExtras(bundle)
        profilFragment!!.initHeader(userInfo!!,fType)
        if(fType != ProfileFragment.CLOSE && fType != ProfileFragment.REQUEST){

            errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                override fun connected() {
                    log.d("connected")
                    val reqObj = JSONObject()
                    reqObj.put("user_id",user.userId)
                    reqObj.put("session",user.session)
                    reqObj.put("user",   otherUser.info.user_id)
                    reqObj.put("start",  start)
                    reqObj.put("end",    end)


                    presenter.requestAndResponse(reqObj, Http.CMDS.MY_POSTS)
                }

                override fun disconnected() {
                    log.d("disconnected")


                }

            })
        }
    }


}