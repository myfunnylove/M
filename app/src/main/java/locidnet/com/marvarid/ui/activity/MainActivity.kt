package locidnet.com.marvarid.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import locidnet.com.marvarid.BuildConfig
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.ProfileFeedAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.connectors.GoNext
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
import locidnet.com.marvarid.pattern.MControlObserver.MusicSubject
import locidnet.com.marvarid.pattern.builder.ErrorConnection
import locidnet.com.marvarid.pattern.builder.SessionOut
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


class MainActivity : BaseActivity(), GoNext, Viewer ,MusicController.MediaPlayerControl, MusicPlayerListener {


    var profilFragment:       MyProfileFragment?    = null
    var searchFragment:       SearchFragment?       = null
    var notificationFragment: NotificationFragment? = null
    var feedFragment:         FeedFragment?         = null
    var manager:              FragmentManager?      = null
    var transaction:          FragmentTransaction?  = null
    var lastFragment:         Int                   = 0

    private var controller: MusicController? = null

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
        var getFirst       = 1
        var startFollowers = 0
        var endFollowers   = 20
        var startFollowing = 0
        var endFollowing   = 20
        var startSearch    = 0
        var endSearch      = 20

        var MY_POSTS_STATUS = "-1"

        val FIRST_TIME   = "0"
        val NEED_UPDATE  = "1"
        val AFTER_UPDATE = "2"
        val ONLY_USER_INFO = "3"

        var FEED_STATUS = NEED_UPDATE

        var COMMENT_POST_UPDATE = 0
        var COMMENT_COUNT       = 0

        var tablayoutHeight = 0
        lateinit var musicSubject:MusicSubject

    }

    override fun getLayout(): Int {

        return R.layout.activity_main
    }

    override fun initView() {
        Const.TAG = "MainActivity"

        startIntroAnimation()

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
        tablayout.getViewTreeObserver().addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    tablayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    tablayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                tablayoutHeight = tablayout.measuredHeight

            }

        })
        setController()
        sendDataForPush()

    }

    fun setPager(): Unit {
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
        tablayout.addTab(tablayout.newTab().setIcon(R.drawable.notification))
        tablayout.addTab(tablayout.newTab().setIcon(R.drawable.account))



        tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {

            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
                log.d("Unselected -> ${p0!!.position}")

                if (p0.position != Const.UPLOAD_FR) p0.setIcon(Const.unselectedTabs.get(p0.position)!!)
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {

                hideSoftKeyboard()

                when (p0!!.position) {

                    Const.PROFIL_FR -> {
                        if (MY_POSTS_STATUS != AFTER_UPDATE) {

                            val reqObj = JS.get()
                            reqObj.put("user", user.userId)


                            log.d("send data for user info data: ${reqObj}")
                            presenter.requestAndResponse(reqObj, Http.CMDS.USER_INFO)

                        }
                        lastFragment = p0.position
                        p0.setIcon(Const.selectedTabs.get(p0.position)!!)
                        setFragment(p0.position)




                    }

                    Const.NOTIF_FR -> {
                        errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                            override fun connected() {
                                val reqObj = JS.get()
                                reqObj.put("start",   startNotif)
                                reqObj.put("end",     endNotif)

                                log.d("feed page select $reqObj")
                                presenter!!.requestAndResponse(reqObj, Http.CMDS.GET_NOTIF_LIST)



                            }

                            override fun disconnected() {

                            }

                        })


                    lastFragment = p0.position

                            p0.setIcon(Const.selectedTabs.get(p0.position)!!)
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
                                    presenter!!.requestAndResponse(reqObj, Http.CMDS.FEED)



                                }

                                override fun disconnected() {

                                }

                            })

                        }
                        lastFragment = p0.position

                        p0.setIcon(Const.selectedTabs.get(p0.position)!!)
                        setFragment(p0.position)
                    }
                    Const.UPLOAD_FR -> {
                        goNext(Const.PICK_UNIVERSAL, "")
                    }
                    else -> {
                        lastFragment = p0.position
                        p0.setIcon(Const.selectedTabs.get(p0.position)!!)
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
                        presenter!!.requestAndResponse(reqObj, Http.CMDS.SEARCH_USER)




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

                        log.d("send data for user info data: ${reqObj}")
                        presenter.requestAndResponse(reqObj, Http.CMDS.USER_INFO)





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


    fun initFragments() {

        var bundle = Bundle()
        bundle.putString("photo",     user.profilPhoto)
        bundle.putString("username",  user.userName)
        bundle.putString("firstName", user.first_name)
        bundle.putString("user_id",    user.userId)
        bundle.putString(ProfileFragment.F_TYPE, ProfileFragment.SETTINGS)
        profilFragment = MyProfileFragment.newInstance(bundle)
        profilFragment!!.connectAudioPlayer(this)

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
        log.d("MainActivity -> OnactivityResult: req:${requestCode} res: ${resultCode} intent: ${if (data != null) true else false}")
        if (requestCode == Const.SESSION_OUT || resultCode == Const.SESSION_OUT){
            setResult(Const.SESSION_OUT)
            finish()
        }else {
            when (resultCode) {
                Activity.RESULT_OK -> {

                    var photos: List<String>? = null

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
                                log.d("picked photo ${photos.get(0)}")

                                photos.get(0).uploadAvatar()
                            }
                        }
                        Const.GO_COMMENT_ACTIVITY -> {
                            if (feedFragment != null && !feedFragment!!.isHidden) {
                                try {

                                    //TODO COMMENTLANI SONINI OLIB KELISH
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
                    tab!!.setIcon(Const.selectedTabs.get(0)!!)
                    tab.select()

                }

                Const.QUIT -> {
                    val sesion = SessionOut.Builder(this@MainActivity)
                            .setErrorCode(96)
                            .build()
                    sesion.out()
                }

                else -> {

                    log.d("lastfragment -> ${lastFragment}")
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

                        log.d("send data for user info data: ${reqObj}")
                        presenter.requestAndResponse(reqObj, Http.CMDS.USER_INFO)


                    }
                    val tab = tablayout.getTabAt(lastFragment)
                    tab!!.setIcon(Const.selectedTabs.get(lastFragment)!!)
                    tab.select()

                    setFragment(lastFragment)

                }
            }
        }
    }
    var doubleBackToExitPressedOnce = false
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
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, resources.getString(R.string.exit_to_press_again), Toast.LENGTH_SHORT).show();

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


                    errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                        override fun connected() {



                            val reqObj = JS.get()
                            reqObj.put("user",    user.userId)
                            reqObj.put("start",   start)
                            reqObj.put("end",     end)
                            presenter.requestAndResponse(reqObj, Http.CMDS.MY_POSTS)


                        }

                        override fun disconnected() {

                        }

                    })

                }else{
                    profilFragment!!.updateUserInfo(userInfo,fType)

                }
            }

            Http.CMDS.MY_POSTS -> {
                log.d("my post status $MY_POSTS_STATUS")
                try{
                    val postList: PostList = Gson().fromJson(result, PostList::class.java)

                    if (postList.posts.size > 0){
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
                profilFragment!!.createProgressForAvatar(ProfileFeedAdapter.HIDE_PROGRESS);
                }catch (e:Exception){}
                profilFragment!!.setAvatar(user.profilPhoto)
            }

            Http.CMDS.SEARCH_USER -> {

                val follow = Gson().fromJson<Follow>(result, Follow::class.java)
                if(follow.users.size > 0){
                    searchFragment!!.swapList(follow.users)

                }else {
                    searchFragment!!.failedGetList("empty")
                }
            }

            Http.CMDS.FEED -> {

                try{

                    val postList: PostList = Gson().fromJson(result, PostList::class.java)
                   feedFragment!!.hideProgress()
                    if (postList.posts.size > 0){
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
        }
    }

    override fun onFailure(from: String, message: String, erroCode: String) {

        log.d("error from: $from message: $message")
       if (from != Http.CMDS.SEARCH_USER) Toaster.errror(message)



        when(from){
            Http.CMDS.FEED        -> Handler().postDelayed({feedFragment!!.failedGetList(message)},1500)

//            Http.CMDS.MY_POSTS    -> Handler().postDelayed({profilFragment!!.failedGetList(message)},1500)
            Http.CMDS.SEARCH_USER -> Handler().postDelayed({searchFragment!!.failedGetList(message)},1500)
        }
    }

    private fun String.uploadAvatar() {
        try{
            profilFragment!!.createProgressForAvatar(ProfileFeedAdapter.SHOW_PROGRESS);
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


    fun startIntroAnimation(){

        val actionbarsize = Functions.DPtoPX(56f,Base.get)
        tablayout.translationY = actionbarsize.toFloat()


        tablayout.animate()
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(300)
    }
    fun hideSoftKeyboard() {
        val inputMethodManager = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager.isActive) {
            if (this.currentFocus != null) {
                inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
            }
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
            controller!!.setAnchorView(findViewById(R.id.pager))
            controller!!.setEnabled(true)
        }
    }

    private fun playNext() {

        musicSrv!!.playNext()

        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        musicSubject.playMeause("")

    }

    private fun playPrev() {
        musicSrv!!.playPrev()
        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        musicSubject.playMeause("")

    }

    override fun playClick(listSong: ArrayList<Audio>, position: Int){
        if (musicSrv != null){
            log.d("PLAYIN SONG ${musicSrv!!.isPng}")

            if(musicSrv!!.isPng){
                log.d("PLAYIN SONG in fragment  2 -> ${listSong.get(position).middlePath == MusicService.PLAYING_SONG_URL}")
                if (MusicService.PLAYING_SONG_URL == listSong.get(position).middlePath){
                    musicSrv!!.pausePlayer()
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
                    musicSrv!!.go()
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
            Toast.makeText(Base.get,Base.get.resources.getString(R.string.error_something),Toast.LENGTH_SHORT).show()
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


        musicSubject.playMeause("")


    }

    override fun seekTo(pos: Int) {
        musicSrv!!.seek(pos)
    }

    override fun start() {
        musicSrv!!.go()
        musicSubject.playMeause("")
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


    private fun sendDataForPush() {
        try {

            var token = Prefs.Builder().getTokenId()
            log.d("Firebase da token bormi -> " + if (token!!.isEmpty()) "yo'q" else "bor -> $token ")


            if (token!!.isEmpty()) {
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

    }
}




