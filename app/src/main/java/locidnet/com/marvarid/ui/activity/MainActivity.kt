package locidnet.com.marvarid.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.*
import android.support.design.widget.TabLayout
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.widget.AppCompatImageView
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_main.*
import locidnet.com.marvarid.BuildConfig
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.ProfileFeedAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.connectors.GoNext
import locidnet.com.marvarid.connectors.MusicPlayerListener
import locidnet.com.marvarid.connectors.ProfileMusicController
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
import locidnet.com.marvarid.pattern.builder.SessionOut
import locidnet.com.marvarid.player.PlayerService
import locidnet.com.marvarid.resources.utils.*
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.ui.activity.publish.PublishSongActivity
import locidnet.com.marvarid.ui.activity.publish.PublishUniversalActivity
import locidnet.com.marvarid.ui.fragment.*
import me.iwf.photopicker.PhotoPicker
import okhttp3.MultipartBody
import org.json.JSONObject
import java.io.File
import javax.inject.Inject


class MainActivity : BaseActivity(), GoNext, Viewer ,MusicPlayerListener {


    private var profilFragment:       MyProfileFragment?    = null
    private var searchFragment:       SearchFragment?       = null
    private var notificationFragment: NotificationFragment? = null
    private var feedFragment:         FeedFragment?         = null
    var manager:              FragmentManager?      = null
    private var transaction:          FragmentTransaction?  = null
    var lastFragment:         Int                   = 0


    private var notifView:View? = null
    var notifBadge:TextView? = null
    var notifIcon:AppCompatImageView? = null

    private var profilView:View? = null
    var profilBadge:AppCompatImageView? = null
    var profilIcon:AppCompatImageView? = null
    var musicSrv:PlayerService? = null
    internal var playerServiceBinder: PlayerService.PlayerServiceBinder? = null
    internal var mediaController: MediaControllerCompat? = null
//    private var controller: MusicController? = null

    @Inject
    lateinit var presenter:Presenter


    @Inject
    lateinit var errorConn: ErrorConnection

    var user = Base.get.prefs.getUser()
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    /*
    *
    *
    * */





    companion object MyPostOffset {

        var start          = 0
        var end            = 20
        var startFeed      = 0
        var endFeed        = 20
        var startNotif     = 0
        var endNotif       = 20
        var startFollowers = 0
        var endFollowers   = 20
        var startFollowing = 0
        var endFollowing   = 20
        var startSearch    = 0
        var endSearch      = 20

        var MY_POSTS_STATUS = "-1"
        var RECOMMEND_POST  = "-1"
        var FEED_STATUS     = "1"

        val FIRST_TIME     = "0"
        val NEED_UPDATE    = "1"
        val AFTER_UPDATE   = "2"
        val ONLY_USER_INFO = "3"

        var COMMENT_POST_UPDATE = 0
        var COMMENT_COUNT       = 0

        var tablayoutHeight = 0
        var musicSubject:MusicSubject? = null
        val notificationTag = "locidnet.com.marvarid.ui.activity.NOTIFICATION"

    }

    override fun getLayout(): Int = R.layout.activity_main

    override fun initView() {
        Const.TAG = "MainActivity"

        startIntroAnimation()
//        Prefs.Builder().setNotifCount(10)
        DaggerMVPComponent
                .builder()
                .mVPModule(MVPModule(this, Model(),this))
                .presenterModule(PresenterModule())
                .errorConnModule(ErrorConnModule(this,true))
                .build()
                .inject(this)

        musicSubject = MusicSubject()

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        setPager()
        tablayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    tablayout.viewTreeObserver.removeGlobalOnLayoutListener(this)
                } else {
                    tablayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
                tablayoutHeight = tablayout.measuredHeight

            }

        })
//        setController()
        sendDataForPush()
        registerReceiver(notificationReceiver,IntentFilter(notificationTag))

        bindService(Intent(this, PlayerService::class.java), object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                playerServiceBinder = service as PlayerService.PlayerServiceBinder
                musicSrv = service.service
                musicSrv!!.setActivity(this@MainActivity)
                musicBound = true
                try {
                    mediaController = MediaControllerCompat(this@MainActivity, playerServiceBinder!!.mediaSessionToken)
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
                musicBound = false
            }
        }, Context.BIND_AUTO_CREATE)

    }

    @SuppressLint("InflateParams")
    fun setPager() {
        initFragments()
        setFragment(Const.FEED_FR)


//        if (feedFragment != null && feedFragment!!.feedAdapter == null){
//            feedFragment!!.showProgress()
//        }

        errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
            override fun connected() {
                log.d("connected")
                val reqObj = JS.get()

                reqObj.put("start",   startFeed)
                reqObj.put("end",     endFeed)

                presenter.requestAndResponse(reqObj, Http.CMDS.FEED)

            }

            override fun disconnected() {
                log.d("disconnected")


            }

        })


        tablayout.addTab(tablayout.newTab().setIcon(R.drawable.feed_select))
        tablayout.addTab(tablayout.newTab().setIcon(R.drawable.search))
        val view: View = layoutInflater.inflate(R.layout.res_upload_view, null)

        tablayout.addTab(tablayout.newTab().setCustomView(view))
        notifView  = layoutInflater.inflate(R.layout.res_main_tab_notif_view, null)
        notifBadge = notifView!!.findViewById<TextView>(R.id.badge)
        notifIcon = notifView!!.findViewById<AppCompatImageView>(R.id.icon)
        if (Prefs.Builder().getNotifCount() > 0){
            notifBadge!!.visibility = View.VISIBLE
            notifBadge!!.text = "${Prefs.Builder().getNotifCount()}"
        }else{
            notifBadge!!.visibility = View.GONE

        }

        tablayout.addTab(tablayout.newTab().setCustomView(notifView))

        profilView  = layoutInflater.inflate(R.layout.res_main_tab_profil_view, null)
        profilBadge = profilView!!.findViewById<AppCompatImageView>(R.id.badge)
        profilIcon = profilView!!.findViewById<AppCompatImageView>(R.id.icon)

        tablayout.addTab(tablayout.newTab().setCustomView(profilView))



        tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {

            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
                log.d("Unselected -> ${p0!!.position}")
                if (p0.position == Const.NOTIF_FR) notifIcon!!.setImageDrawable(VectorDrawableCompat.create(resources,R.drawable.notification,theme))
                else if (p0.position == Const.PROFIL_FR) profilIcon!!.setImageDrawable(VectorDrawableCompat.create(resources,R.drawable.account,theme))
                if (p0.position != Const.UPLOAD_FR && p0.position != Const.NOTIF_FR) p0.setIcon(Const.unselectedTabs[p0.position]!!)
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {

                hideSoftKeyboard()

                when (p0!!.position) {

                    Const.PROFIL_FR -> {
                        errorConn.hideErrorLayout()
                        errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                            override fun connected() {
                                if (MY_POSTS_STATUS != AFTER_UPDATE) {

                                    val reqObj = JS.get()
                                    reqObj.put("user", user.userId)


                                    log.d("tab select send data for user info data: $reqObj")
                                    presenter.requestAndResponse(reqObj, Http.CMDS.USER_INFO)

                                }
                            }
                                override fun disconnected() {
                                    errorConn.hideErrorLayout()

                                    onFailure(Http.CMDS.USER_INFO,resources.getString(R.string.internet_conn_error),"")
                                }


                            })
                        lastFragment = p0.position
                        profilBadge!!.visibility = View.GONE
                        profilIcon!!.setImageDrawable(VectorDrawableCompat.create(resources,R.drawable.account_select,theme))
                        setFragment(p0.position)




                    }

                    Const.NOTIF_FR -> {
                        errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                            override fun connected() {
                                val reqObj = JS.get()
                                reqObj.put("start",   startNotif)
                                reqObj.put("end",     endNotif)

                                log.d("feed page select $reqObj")
                                presenter.requestAndResponse(reqObj, Http.CMDS.GET_NOTIF_LIST)



                            }

                            override fun disconnected() {

                            }

                        })


                    lastFragment = p0.position
                            Prefs.Builder().setNotifCount(0)

                            notifBadge!!.text = ""
                            notifBadge!!.visibility = View.GONE
                            notifIcon!!.setImageDrawable(VectorDrawableCompat.create(resources,R.drawable.notification_select,theme))
                            setFragment(p0.position)
                    }

                    Const.SEARCH_FR -> {

                        if(RECOMMEND_POST != AFTER_UPDATE){
                            errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                                override fun connected() {
                                    val reqObj = JS.get()

                                    log.d("feed page select $reqObj")
                                    presenter.requestAndResponse(reqObj, Http.CMDS.GET_15_POSTS)



                                }

                                override fun disconnected() {

                                }

                            })
                        }
                        lastFragment = p0.position

                        p0.setIcon(Const.selectedTabs[p0.position]!!)
                        setFragment(p0.position)
                    }

                    Const.FEED_FR -> {
                        log.i("feed page select")

                        if (FEED_STATUS != AFTER_UPDATE){



                            errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                                override fun connected() {
                                    val reqObj = JS.get()
                                    reqObj.put("start",   startFeed)
                                    reqObj.put("end",     endFeed)

                                    log.d("feed page select $reqObj")
                                    presenter.requestAndResponse(reqObj, Http.CMDS.FEED)



                                }

                                override fun disconnected() {

                                }

                            })

                        }
                        lastFragment = p0.position

                        p0.setIcon(Const.selectedTabs[p0.position]!!)
                        setFragment(p0.position)
                    }
                    Const.UPLOAD_FR -> {
                        goNext(Const.PICK_UNIVERSAL, "")
                    }
                    else -> {
                        lastFragment = p0.position
                        p0.setIcon(Const.selectedTabs[p0.position]!!)
                        setFragment(p0.position)
                    }
                }

            }

        })

    }


    override fun goNext(to: Int, data: String) {

        var intent: Intent? = null
        when (to) {
            Const.TO_POSTS -> {

            }
            Const.TO_FOLLOWERS -> {


                intent = Intent(this, FollowActivity().javaClass)
                intent.putExtra(FollowActivity.TYPE, FollowActivity.FOLLOWERS)
                intent.putExtra("user_id",user.userId)

            }
            Const.TO_FOLLOWING -> {

                intent = Intent(this, FollowActivity().javaClass)
                intent.putExtra(FollowActivity.TYPE, FollowActivity.FOLLOWING)
                intent.putExtra("user_id",user.userId)
            }


            Const.PICK_IMAGE -> {
                log.d("Pick Image ga o'tish")

                PhotoPicker.builder()
                        .setPhotoCount(1)
                        .start(this, Const.PICK_IMAGE)
            }
            Const.CHANGE_AVATAR -> {
                log.i("Pick Image ga o'tish")

                PhotoPicker.builder()
                        .setPhotoCount(1)

                        .start(this, Const.CHANGE_AVATAR)
            }



            Const.PICK_AUDIO -> {
                intent = Intent(this, PublishSongActivity().javaClass)

            }

            Const.PICK_UNIVERSAL -> {

                intent = Intent(this, PublishUniversalActivity().javaClass)
            }


            Const.SEARCH_USER -> {



                errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                    override fun connected() {
                        val reqObj = JS.get()
                        reqObj.put("start",   startSearch)
                        reqObj.put("end",     endSearch)
                        reqObj.put("user",    data)
                        presenter.requestAndResponse(reqObj, Http.CMDS.SEARCH_USER)




                    }

                    override fun disconnected() {
                        hideSoftKeyboard()
                    }

                })
            }

            Const.FOLLOW -> {
                errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                    override fun connected() {
                        presenter.requestAndResponse(JSONObject(data), Http.CMDS.FOLLOW)





                    }

                    override fun disconnected() {

                    }

                })

            }

            Const.PROFIL_PAGE -> {
                tablayout.getTabAt(Const.PROFIL_FR)!!.select()

            }
            Const.REFRESH_FEED -> {

                errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                    override fun connected() {


                        val reqObj = JS.get()
                        reqObj.put("start",   startFeed)
                        reqObj.put("end",     endFeed)


                        presenter.requestAndResponse(reqObj, Http.CMDS.FEED)


                    }

                    override fun disconnected() {

                    }

                })
            }

            Const.REFRESH_NOTIFICATION -> {

                errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                    override fun connected() {


                        val reqObj =  JS.get()
                        reqObj.put("start",   startNotif)
                        reqObj.put("end",     endNotif)


                        presenter.requestAndResponse(reqObj, Http.CMDS.GET_NOTIF_LIST)


                    }

                    override fun disconnected() {

                    }

                })
            }


            Const.REFRESH_PROFILE_FEED ->{


                errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                    override fun connected() {
                        val reqObj = JS.get()
                        reqObj.put("user", user.userId)
                        reqObj.put("start",   start)
                        reqObj.put("end",     end)
                        log.d("send data for user info data: $reqObj")
                        presenter.requestAndResponse(reqObj, Http.CMDS.USER_INFO)





                    }

                    override fun disconnected() {

                    }

                })
            }

            Const.GET_15_POST -> {
                errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                    override fun connected() {
                        val reqObj = JS.get()

                        log.d("feed page select $reqObj")
                        presenter.requestAndResponse(reqObj, Http.CMDS.GET_15_POSTS)



                    }

                    override fun disconnected() {

                    }

                })
            }
            else -> {

            }
        }

        if (intent != null) startActivityForResult(intent, Const.FROM_MAIN_ACTIVITY)
    }

    override fun donGo(why: String) {
       // Toast.makeText(Base.get.applicationContext, why, Toast.LENGTH_SHORT).show()
    }


    private fun initFragments() {


        val bundle = Bundle()
        bundle.putString("photo",     user.profilPhoto)
        bundle.putString("username",  user.userName)
        bundle.putString("firstName", user.first_name)
        bundle.putString("user_id",   user.userId)
        bundle.putString(ProfileFragment.F_TYPE, ProfileFragment.SETTINGS)
        profilFragment = MyProfileFragment.newInstance(bundle)
        profilFragment!!.connectAudioPlayer(this)
        profilFragment!!.setProfileMusicController(object : ProfileMusicController {
            override fun pressPlay() {
                if (mediaController != null){
                    mediaController!!.transportControls.play()

                }



                if (PlayerService.PLAY_STATUS == PlayerService.PLAYING)
                    profilFragment!!.postAdapter!!.updateMusicController(ProfileFeedAdapter.PAUSE)
                else
                    profilFragment!!.postAdapter!!.updateMusicController(ProfileFeedAdapter.PLAY)
            }

            override fun pressNext() {
                if (mediaController != null){
                    mediaController!!.transportControls.skipToNext()

                }
            }

        })
        profilFragment!!.connect(this)
        searchFragment = SearchFragment.newInstance()
        searchFragment!!.connect(this)

        notificationFragment = NotificationFragment.newInstance()
        notificationFragment!!.connect(this)

        feedFragment = FeedFragment.newInstance()
        feedFragment!!.connectAudioPlayer(this)
        feedFragment!!.connect(this)


    }

    @SuppressLint("CommitTransaction")
    fun setFragment(id: Int = Const.FEED_FR) {

        if (manager == null) manager = supportFragmentManager

        transaction = manager!!.beginTransaction()
//        val fr1 = manager!!.findFragmentByTag(FeedFragment.TAG)
//        val fr2 = manager!!.findFragmentByTag(SearchFragment.TAG)
//        val fr3 = manager!!.findFragmentByTag(NotificationFragment.TAG)
//        val fr4 = manager!!.findFragmentByTag(ProfileFragment.TAG)
        when (id) {
            Const.FEED_FR -> {

                if (searchFragment!!.isAdded && !searchFragment!!.isHidden) transaction!!.hide(searchFragment)
                if (notificationFragment!!.isAdded && !notificationFragment!!.isHidden) transaction!!.hide(notificationFragment)
                if (profilFragment!!.isAdded && !profilFragment!!.isHidden) transaction!!.hide(profilFragment)

                if (feedFragment!!.isAdded) {
                    if (feedFragment!!.isHidden)
                        transaction!!.show(feedFragment)
                } else
                    transaction!!.add(R.id.pager, feedFragment, FeedFragment.TAG)


            }

            Const.SEARCH_FR -> {

                if (feedFragment!!.isAdded && !feedFragment!!.isHidden) transaction!!.hide(feedFragment)
                if (notificationFragment!!.isAdded && !notificationFragment!!.isHidden) transaction!!.hide(notificationFragment)
                if (profilFragment!!.isAdded && !profilFragment!!.isHidden) transaction!!.hide(profilFragment)

                if (searchFragment!!.isAdded) {
                    if (searchFragment!!.isHidden)
                        transaction!!.show(searchFragment)
                } else
                    transaction!!.add(R.id.pager, searchFragment, SearchFragment.TAG)
            }

            Const.NOTIF_FR -> {

                if (feedFragment!!.isAdded && !feedFragment!!.isHidden) transaction!!.hide(feedFragment)
                if (searchFragment!!.isAdded && !searchFragment!!.isHidden) transaction!!.hide(searchFragment)
                if (profilFragment!!.isAdded && !profilFragment!!.isHidden) transaction!!.hide(profilFragment)

                if (notificationFragment!!.isAdded) {
                    if (notificationFragment!!.isHidden)
                        transaction!!.show(notificationFragment)
                } else
                    transaction!!.add(R.id.pager, notificationFragment, NotificationFragment.TAG)


            }
            Const.PROFIL_FR -> {

                if (feedFragment!!.isAdded && !feedFragment!!.isHidden) transaction!!.hide(feedFragment)
                if (searchFragment!!.isAdded && !searchFragment!!.isHidden) transaction!!.hide(searchFragment)
                if (notificationFragment!!.isAdded && !notificationFragment!!.isHidden) transaction!!.hide(notificationFragment)


                log.d("${profilFragment!!.isAdded}")
                if (profilFragment!!.isAdded) {
                    if (profilFragment!!.isHidden)
                        transaction!!.show(profilFragment)
                } else
                    transaction!!.add(R.id.pager, profilFragment, ProfileFragment.TAG)


            }


        }

        transaction!!.commit()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        log.d("MainActivity -> OnactivityResult: req:$requestCode res: $resultCode intent: ${data != null}")
        var photos: List<String>? = null
        if (musicSrv != null)  musicSrv!!.setActivity(this@MainActivity)


        if (requestCode == Const.SESSION_OUT || resultCode == Const.SESSION_OUT){
            setResult(Const.SESSION_OUT)
            finish()
        }else {
            when (resultCode) {
                Const.PICK_CROP_IMAGE ->{
                    if (data != null) {
                            log.d("data null emas")
                        val resultUri: Uri = UCrop.getOutput(data)!!
                        resultUri.path.uploadAvatar()
                        }

                }

                Activity.RESULT_OK -> {


                    when (requestCode) {
//                    Const.PICK_IMAGE -> {
//                        if (data != null) {
//                            photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS)
//
//                        }
//
//                        if (photos != null) {
//
//                            val intent = Intent(this, PublishImageActivity().javaClass);
//                            intent.putExtra(Const.PUBLISH_IMAGE, photos.get(0))
//                            startActivityForResult(intent, Const.FROM_MAIN_ACTIVITY)
//                        }
//                    }
                        Const.CHANGE_AVATAR -> {
                            if (data != null) {
                                photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS)

                            }
                            if (photos != null) {
                                log.d("picked photo ${photos[0]}")

                                photos[0].uploadAvatar()
                            }
                        }
                        Const.GO_COMMENT_ACTIVITY -> {
                            if (feedFragment != null && !feedFragment!!.isHidden) {
                                try {

//                               feedFragment!!.feedAdapter!!.feeds.posts.get(COMMENT_POST_UPDATE).comments = COMMENT_COUNT.toString()
//                               feedFragment!!.feedAdapter!!.notifyItemChanged(COMMENT_POST_UPDATE)
                                } catch (e: Exception) {
                                }

                            }
                        }
                    }
                }

                Const.PICK_UNIVERSAL -> {
                    log.d("after posting go feed $MY_POSTS_STATUS")
                    FEED_STATUS = NEED_UPDATE
                    if (feedFragment!!.feedAdapter != null) {
                        MyPostOffset.startFeed = 0
                        MyPostOffset.endFeed = 1
                    }

                    MY_POSTS_STATUS = NEED_UPDATE

                    val tab = tablayout.getTabAt(0)
                    tab!!.setIcon(Const.selectedTabs[0]!!)
                    tab.select()

                }

                Const.QUIT -> {
                    val sesion = SessionOut.Builder(this@MainActivity)
                            .setErrorCode(96)
                            .build()
                    sesion.out()
                }

                else -> {
                    log.d("lastfragment -> $lastFragment")
                    log.d("profil followers count -> ${FFFFragment.followersCount}")
                    log.d("my post status $MY_POSTS_STATUS")

                    if (requestCode == Const.CHANGE_AVATAR) {

                        try {
                            profilFragment!!.createProgressForAvatar(ProfileFeedAdapter.CANCEL_PROGRESS)

                        } catch (e: Exception) {
                        }

                    }

                    if (lastFragment == 4 && profilFragment != null && MY_POSTS_STATUS != AFTER_UPDATE) {

//                    MY_POSTS_STATUS = ONLY_USER_INFO

                        val reqObj = JS.get()
                        reqObj.put("user", user.userId)

                        log.d("send data for user info data: $reqObj")
                        presenter.requestAndResponse(reqObj, Http.CMDS.USER_INFO)


                    }
                    val tab = tablayout.getTabAt(lastFragment)
                    tab!!.setIcon(Const.selectedTabs[lastFragment]!!)
                    tab.select()

                    setFragment(lastFragment)

                }
            }
        }
    }
    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {

        log.d("onbackpressed")


        if (doubleBackToExitPressedOnce) {
            MyPostOffset.startNotif     = 0
            MyPostOffset.endNotif       = 20
            MyPostOffset.start          = 0
            MyPostOffset.end            = 20
            MyPostOffset.startFeed      = 0
            MyPostOffset.endFeed        = 20
            MyPostOffset.startFollowers = 0
            MyPostOffset.endFollowers   = 20
            MyPostOffset.startFollowing = 0
            MyPostOffset.endFollowing   = 20
            MY_POSTS_STATUS             = NEED_UPDATE
            FEED_STATUS                 = NEED_UPDATE
            COMMENT_POST_UPDATE         = 0
            COMMENT_COUNT               = 0
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, resources.getString(R.string.exit_to_press_again), Toast.LENGTH_SHORT).show()

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun initProgress() {

    }

    override fun showProgress() {
    }

    override fun hideProgress() {
    }

    override fun onSuccess(from: String, result: String) {

        log.d("success from: $from result: $result")
        when (from) {

            Http.CMDS.USER_INFO -> {
                val userInfo = Gson().fromJson(result,UserInfo::class.java)
                val fType = ProfileFragment.SETTINGS
                log.i("profil page select $MY_POSTS_STATUS")
                profilFragment!!.initHeader(userInfo,fType)


                if (MY_POSTS_STATUS != ONLY_USER_INFO || !profilFragment!!.initBody) {


//                    errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
//                        override fun connected() {



                          if (Functions.isNetworkAvailable(Base.get.context)){

                              val reqObj = JS.get()
                              reqObj.put("user",    user.userId)
                              reqObj.put("start",   start)
                              reqObj.put("end",     end)
                              presenter.requestAndResponse(reqObj, Http.CMDS.MY_POSTS)

                          }


//                        }
//
//                        override fun disconnected() {
//
//                        }
//
//                    })

                }else{
                    profilFragment!!.updateUserInfo(userInfo,fType)

                }
            }

            Http.CMDS.MY_POSTS -> {
                log.d("my post status $MY_POSTS_STATUS")
                try{
                    val postList: PostList = Gson().fromJson(result, PostList::class.java)

                    if (postList.posts.size > 0 || start == 0){
                        MY_POSTS_STATUS = AFTER_UPDATE
                        profilFragment!!.initBody(postList)
                    }else {
                        profilFragment!!.swipeRefreshLayout.isRefreshing = false
                    }

                    if (postList.posts.size > 0){

                    }

                }catch (e:Exception){



                }
            }



            Http.CMDS.CHANGE_AVATAR -> {

                user = Base.get.prefs.getUser()
                try{
                    log.d("from change avatar get user photo ${user.profilPhoto}")
                profilFragment!!.createProgressForAvatar(ProfileFeedAdapter.HIDE_PROGRESS)
                }catch (e:Exception){}
                profilFragment!!.setAvatar(user.profilPhoto)
            }



            Http.CMDS.FEED -> {

                try{

                    val postList: PostList = Gson().fromJson(result, PostList::class.java)
                   feedFragment!!.hideProgress()
                    if (postList.posts.size > 0 || startFeed == 0){
                       FEED_STATUS = AFTER_UPDATE
                       feedFragment!!.swapPosts(postList)
                   }else{
                       feedFragment!!.failedGetList()

                   }

                }catch (e:Exception){
                    feedFragment!!.failedGetList()

                }

            }

            Http.CMDS.GET_FOLLOWERS -> {

            }

            Http.CMDS.GET_NOTIF_LIST ->{


                val pushList:PushList = Gson().fromJson(result,PushList::class.java)

                if(pushList.pushes.size > 0){
                    notificationFragment!!.swapPushes(pushList)
                }else{
                    notificationFragment!!.onFail("")

                }
            }

            Http.CMDS.GET_15_POSTS -> {
                try{
                    log.d("Get RECOMMENDED POSTS")
                    val posts: RecommededPosts = Gson().fromJson(result, RecommededPosts::class.java)
                    searchFragment!!.hideProgress()
                    if (posts.posts.size > 0){
                        RECOMMEND_POST = AFTER_UPDATE
                        searchFragment!!.swapPosts(posts.posts)
                    }else{
                        searchFragment!!.failedGetList()

                    }

                }catch (e:Exception){
                    searchFragment!!.failedGetList()

                }

            }
        }
    }

    override fun onFailure(from: String, message: String, erroCode: String) {

        log.d("error from: $from message: $message")
       if (from != Http.CMDS.SEARCH_USER) Toaster.errror(message)



        when(from){
            Http.CMDS.FEED        -> Handler().postDelayed({feedFragment!!.failedGetList(message)},1500)

            Http.CMDS.USER_INFO    -> Handler().postDelayed({
                var userInfoForCache = Prefs.getUserInfo()
                if (userInfoForCache != null)
                    profilFragment!!.initHeader(userInfoForCache,ProfileFragment.SETTINGS)
                else
                    profilFragment!!.showOnlyHeader()

                userInfoForCache = null

            },1500)

        }
    }

    private fun String.uploadAvatar() {
        try{
            profilFragment!!.createProgressForAvatar(ProfileFeedAdapter.SHOW_PROGRESS)
        }catch (e:Exception){}
        val reqFile = ProgressRequestBody(File(this), object : ProgressRequestBody.UploadCallbacks {

            override fun onProgressUpdate(percentage: Int) {


            }

            override fun onError() {
                log.d("onerror")

            }

            override fun onFinish() {
                log.d("onfinish")
            }

        }, ProgressRequestBody.IMAGE_ALL)
        val body = MultipartBody.Part.createFormData("upload", File(this).name, reqFile)
        presenter.uploadAvatar(body)

    }


    private fun startIntroAnimation(){

        val actionbarsize = Functions.DPtoPX(56f,Base.get)
        tablayout.translationY = actionbarsize.toFloat()


        tablayout.animate()
                .translationY(0f)
                .setDuration(300).startDelay = 300
    }
    fun hideSoftKeyboard() {
        val inputMethodManager = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager.isActive) {
            if (this.currentFocus != null) {
                inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
            }
        }
    }





    override fun playClick(listSong: ArrayList<Audio>, position: Int){
        PlayerService.songs = listSong
        PlayerService.songPosn = position
        log.d("PLAYCLICKED")
        if (mediaController != null) {

            if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PLAYING &&
                    PlayerService.PLAYING_SONG_URL == listSong[position].middlePath) {
                if (musicSrv != null) musicSrv!!.pressPauseFromControl = 1
                mediaController!!.transportControls.pause()

            } else if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PAUSED &&
                    PlayerService.PLAYING_SONG_URL == listSong[position].middlePath) {
                if(tablayout.selectedTabPosition != Const.PROFIL_FR) profilBadge!!.visibility = View.VISIBLE
                showLoading()

                if (musicSrv != null) musicSrv!!.pressPauseFromControl = -1
                mediaController!!.transportControls.play()

            } else if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PLAYING &&
                    PlayerService.PLAYING_SONG_URL != listSong[position].middlePath) {
                if(tablayout.selectedTabPosition != Const.PROFIL_FR) profilBadge!!.visibility = View.VISIBLE
                showLoading()

                if (musicSrv != null) musicSrv!!.pressPauseFromControl = -1
                mediaController!!.transportControls.play()

            } else if (musicSrv!!.currentState == PlaybackStateCompat.STATE_PAUSED &&
                    PlayerService.PLAYING_SONG_URL != listSong[position].middlePath) {
                if(tablayout.selectedTabPosition != Const.PROFIL_FR) profilBadge!!.visibility = View.VISIBLE
                showLoading()

                mediaController!!.transportControls.play()
                if (musicSrv != null) musicSrv!!.pressPauseFromControl = -1

            }else {
                if(tablayout.selectedTabPosition != Const.PROFIL_FR) profilBadge!!.visibility = View.VISIBLE
                showLoading()
                mediaController!!.transportControls.play()
                if (musicSrv != null) musicSrv!!.pressPauseFromControl = -1

            }

        }else{
            if (musicSrv != null) musicSrv!!.pressPauseFromControl = 1

            Toast.makeText(Base.get,Base.get.resources.getString(R.string.error_something),Toast.LENGTH_SHORT).show()
        }
    }




    private var musicBound = false




    override fun hideLoading(){
        log.d("hide loading")
//        loading.visibility = View.GONE
    }
    override fun showLoading(){
//        loading.visibility = View.VISIBLE
    }

    // get notification countq
    val notificationReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent?) {

            try{
                if (tablayout.selectedTabPosition != Const.NOTIF_FR){
                    notifBadge!!.visibility = View.VISIBLE
                    notifBadge!!.setText("${Prefs.Builder().getNotifCount()}")
                }

            }catch (e:Exception){}
        }

    }



    // send data for push
    private fun sendDataForPush() = try {

        var token = Prefs.Builder().getTokenId()
        log.d("Firebase da token bormi -> " + if (token.isEmpty()) "yo'q" else "bor -> $token ")


        if (token.isEmpty()) {
            log.d("Firebase da token yo'q -> ")

            token = FirebaseInstanceId.getInstance().token!!
            Prefs.Builder().setTokenId(token)
            log.d("Firebase da token olindi -> " + Prefs.Builder().getTokenId())

            //                Http.sendDataForPush();


        }

        val tokenJs = JS.get()

        tokenJs.put("token",Prefs.Builder().getTokenId())
        tokenJs.put("device","android")
        tokenJs.put("app","marvarid")
        try{
            tokenJs.put("version",BuildConfig.VERSION_NAME)
        }catch (e:Exception){
            tokenJs.put("version","none")
        }

        presenter.requestAndResponse(tokenJs,Http.CMDS.SET_TOKEN_DATA)
    } catch (e: Exception) {
        e.printStackTrace()
        log.d("Firebase da tokenni olishda exception-> " + e.toString())


    }

    override fun onDestroy() {
        super.onDestroy()
         start          = 0
         end            = 20
         startFeed      = 0
         endFeed        = 20
         startNotif     = 0
         endNotif       = 20
         startFollowers = 0
         endFollowers   = 20
         startFollowing = 0
         endFollowing   = 20
         startSearch    = 0
         endSearch      = 20

        MY_POSTS_STATUS = "-1"
         RECOMMEND_POST  = "-1"
         FEED_STATUS     = "1"



         COMMENT_POST_UPDATE = 0
         COMMENT_COUNT       = 0

         tablayoutHeight = 0
        unregisterReceiver(notificationReceiver)
    }
}




