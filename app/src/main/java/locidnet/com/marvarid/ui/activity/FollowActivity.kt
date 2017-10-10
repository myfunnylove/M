package locidnet.com.marvarid.ui.activity

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
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
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.mvp.Presenter
import locidnet.com.marvarid.mvp.Viewer
import locidnet.com.marvarid.pattern.MControlObserver.MusicSubject
import locidnet.com.marvarid.pattern.builder.ErrorConnection
import locidnet.com.marvarid.player.PlayerService
import locidnet.com.marvarid.resources.utils.*
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.ui.fragment.*
import org.json.JSONObject
import javax.inject.Inject

class FollowActivity : BaseActivity(),
        GoNext,Viewer ,

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
    var afterrefresh                    = false
    var musicSrv:PlayerService? = null
    internal var playerServiceBinder: PlayerService.PlayerServiceBinder? = null
    internal var mediaController: MediaControllerCompat? = null
   // var jsUserData:JSONObject? = null
//    private var controller: MusicController? = null
    var userInfo:UserInfo? = null
    var bundle:Bundle? = null
    private var playIntent: Intent? = null
    private var musicBound = false
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
            supportActionBar!!.title = intent.extras.getString("username")
//            setController()

        }
        else if (intent.getIntExtra(TYPE,-1) == FOLLOWING){

            supportActionBar!!.title = resources.getString(R.string.following)

        }else if (intent.getIntExtra(TYPE,-1) == FOLLOWERS){
            supportActionBar!!.title = resources.getString(R.string.followers)

        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {

            onBackPressed()

        }

        if (intent.getIntExtra(TYPE,-1) == PROFIL_T ){
            val reqObj =  JS.get()
            reqObj.put("user",   intent.extras.getString("user_id"))


            log.d("send data for user info data: ${reqObj}")
            presenter.requestAndResponse(reqObj,Http.CMDS.USER_INFO)


            bindService(Intent(this, PlayerService::class.java), object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    playerServiceBinder = service as PlayerService.PlayerServiceBinder
                    musicSrv = service.service
                    musicSrv!!.setActivity(this@FollowActivity)

                    musicBound = true
                    try {

                        mediaController = MediaControllerCompat(this@FollowActivity, playerServiceBinder!!.getMediaSessionToken())
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


        val blockJS =  JS.get()
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
                    val obj = JS.get()
                    obj.put("user",   userID)
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
                val fType = Functions.selectFollowType(userInfo!!)

                if (fType == ProfileFragment.FOLLOW || fType == ProfileFragment.UN_FOLLOW){
                    errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                        override fun connected() {
                            log.d("connected")
                            val reqObj =JS.get()
                            reqObj.put("user",    userID)
                            reqObj.put("start",   "0")
                            reqObj.put("end",     end)
                            afterrefresh = true

                            presenter.requestAndResponse(reqObj, Http.CMDS.MY_POSTS)
                        }

                        override fun disconnected() {
                            log.d("disconnected")


                        }

                    })
                }else{
                    errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                        override fun connected() {
                            val reqObj = JS.get()
                            reqObj.put("user",   intent.extras.getString("user_id"))


                            log.d("send data for user info data: ${reqObj}")
                            presenter.requestAndResponse(reqObj,Http.CMDS.USER_INFO)
                        }

                        override fun disconnected() {
                            log.d("disconnected")


                        }

                    })
                }
            }

            Const.TO_FOLLOWERS -> showFragment(FOLLOWERS)

            Const.TO_FOLLOWING -> showFragment(FOLLOWING)

            Const.PROFIL_PAGE -> {
                errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                    override fun connected() {
                        log.d("connected")
                        val reqObj =  JS.get()
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
                    }else {
                        profilFragment!!.swipeRefreshLayout!!.isRefreshing = false
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
                val fType = if (intent.extras.getString(ProfileFragment.F_TYPE) != null && intent.extras.getString(ProfileFragment.F_TYPE) == ProfileFragment.SETTINGS)
                                 ProfileFragment.SETTINGS
                            else Functions.selectFollowType(userInfo!!)

                    log.d("fType $fType result of userinfo $otherUser ")

                    if (fType != "-1") {
                        if (profilFragment != null && profilFragment!!.isVisible){
                            afterrefresh = false

                            profilFragment!!.initHeader(userInfo!!,fType)
                        }else{
                            showFragment(PROFIL_T)

                        }
                    }else{
                        afterrefresh = false
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
       if (manager != null){

           if (manager!!.backStackEntryCount == 1){


               this.finish()
           }else if(Prefs.Builder().getUser().session != ""){

               super.onBackPressed()
           }else{
               setResult(Const.SESSION_OUT)
               this.finish()
           }
       }else{
           super.onBackPressed()
       }
    }






    override fun playClick(listSong: ArrayList<Audio>, position: Int){
        PlayerService.songs = listSong
        PlayerService.songPosn = position
        log.d("PLAYCLICKED")
        if (mediaController != null) {

            if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PLAYING &&
                    PlayerService.PLAYING_SONG_URL == listSong.get(position).middlePath) {

                mediaController!!.getTransportControls().pause()

            } else if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PAUSED &&
                    PlayerService.PLAYING_SONG_URL == listSong.get(position).middlePath) {
                showLoading()

                mediaController!!.getTransportControls().play()

            } else if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PLAYING &&
                    PlayerService.PLAYING_SONG_URL != listSong.get(position).middlePath) {
                showLoading()

                mediaController!!.getTransportControls().play()

            } else if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PAUSED &&
                    PlayerService.PLAYING_SONG_URL != listSong.get(position).middlePath) {
                showLoading()

                mediaController!!.getTransportControls().play()

            }else {
                showLoading()

                mediaController!!.getTransportControls().play()

            }
        windowManager
        }else{
            Toast.makeText(Base.get,Base.get.resources.getString(R.string.error_something), Toast.LENGTH_SHORT).show()
        }
    }




    val musicConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as PlayerService.PlayerServiceBinder
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



    override fun turnOn() {
        val otherUser = userInfo!!.user

        val fType = if (intent.extras.getString(ProfileFragment.F_TYPE) != null && intent.extras.getString(ProfileFragment.F_TYPE) == ProfileFragment.SETTINGS)
            ProfileFragment.SETTINGS
        else Functions.selectFollowType(userInfo!!)
        bundle!!.putString(ProfileFragment.F_TYPE,fType)
        intent.putExtras(bundle)
        profilFragment!!.initHeader(userInfo!!,fType)

        if(fType != ProfileFragment.CLOSE && fType != ProfileFragment.REQUEST){

            errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                override fun connected() {
                    log.d("connected")
                    val reqObj =  JS.get()
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

    override fun hideLoading(){
//        loading.visibility = View.GONE
    }
    override fun showLoading(){
//        loading.visibility = View.VISIBLE
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Const.SESSION_OUT || resultCode == Const.SESSION_OUT){
            setResult(Const.SESSION_OUT)
            finish()
        }
    }
}