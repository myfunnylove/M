package locidnet.com.marvarid.ui.fragment

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.FeedAdapter
import locidnet.com.marvarid.adapter.PostAudioGridAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseFragment
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.musicplayer.MusicController
import locidnet.com.marvarid.musicplayer.MusicService
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.connectors.GoNext
import locidnet.com.marvarid.connectors.MusicPlayerListener
import locidnet.com.marvarid.model.*
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.pattern.builder.EmptyContainer
import locidnet.com.marvarid.resources.customviews.loadmorerecyclerview.EndlessRecyclerViewScrollListener
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.MainActivity
import locidnet.com.marvarid.ui.activity.PlaylistActivity
import kotlin.properties.Delegates

class MyProfileFragment : BaseFragment() , View.OnClickListener, AdapterClicker, MusicController.MediaPlayerControl, MusicPlayerListener {





    var postView               by Delegates.notNull<RecyclerView>()
    var progressLay            by Delegates.notNull<ViewGroup>()
    var swipeRefreshLayout     by Delegates.notNull<SwipeRefreshLayout>()

    var user                          = Base.get.prefs.getUser()
    var oldpostList: PostList?         = null
    var postAdapter: FeedAdapter?      = null

    var connectActivity: GoNext?       = null
    var postUser: PostUser?            = null
    val model                         = Model()
    var manager: LinearLayoutManager?  = null
    var expanded                      = false

    lateinit var emptyContainer:EmptyContainer
    var scroll: EndlessRecyclerViewScrollListener? = null
    companion object {
        var TAG:String   = "ProfileFragment"
        val FOLLOW       = Base.get.resources.getString(R.string.follow)
        val UN_FOLLOW    = Base.get.resources.getString(R.string.unfollow)
        val REQUEST      = Base.get.resources.getString(R.string.request)
        val SETTINGS     = Base.get.resources.getString(R.string.settings)
        val F_TYPE       = "fType"
        var FOLLOW_TYPE  = ""

        fun newInstance(data: Bundle): MyProfileFragment {


            val newsFragment = MyProfileFragment()

            newsFragment.arguments = data
            return newsFragment

        }
        var FOLLOWERS                     = "0"
        var FOLLOWING                     = "0"

        var cachedSongAdapters:HashMap<Int, PostAudioGridAdapter>? = null
        var playedSongPosition  = -1
    }

    fun connect(connActivity: GoNext){
        connectActivity = connActivity

    }

    override fun getFragmentView(): Int = R.layout.fragment_profil_page

    override fun init() {
        Const.TAG = "ProfileFragment"

        FOLLOW_TYPE = arguments.getString(F_TYPE)


        log.d("init profil fragment")


        progressLay    = rootView.findViewById(R.id.progressLay)    as ViewGroup
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout)    as SwipeRefreshLayout

        postView     = rootView.findViewById(R.id.postList)         as RecyclerView


        emptyContainer = EmptyContainer.Builder()
                .setIcon(R.drawable.account_light)
                .setText(R.string.error_empty_universal)
                .initLayoutForFragment(rootView)

                .build()


        manager = LinearLayoutManager(Base.get)
        postView.layoutManager = manager
        postView.setHasFixedSize(true)
        scroll = object : EndlessRecyclerViewScrollListener(manager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                log.d("PROFIL POSTLARI OXIRIGA KELDI ${manager!!.findLastVisibleItemPosition()}")
                if (postAdapter != null && postAdapter!!.feeds.posts.size >= 20){
                    MainActivity.start = (postAdapter!!.feeds.posts.size - 1)
                    MainActivity.end = 20
                    connectActivity!!.goNext(Const.REFRESH_PROFILE_FEED,"")
                }


            }

            override fun onScrolled(view: RecyclerView?, dx: Int, dy: Int) {
                var lastVisibleItemPosition = 0

                val totalItemCount = mLayoutManager.itemCount
                swipeRefreshLayout.setEnabled(mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);

                    lastVisibleItemPosition = (mLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()


                // If the total item count is zero and the previous isn't, assume the
                // list is invalidated and should be reset back to initial state
                if (totalItemCount < previousTotalItemCount) {
                    this.currentPage = this.startingPageIndex
                    this.previousTotalItemCount = totalItemCount
                    if (totalItemCount == 0) {
                        this.loading = true
                    }
                }
                // If it’s still loading, we check to see if the dataset count has
                // changed, if so we conclude it has finished loading and update the current page
                // number and total item count.
                if (loading && totalItemCount > previousTotalItemCount) {
                    loading = false
                    previousTotalItemCount = totalItemCount
                }

                // If it isn’t currently loading, we check to see if we have breached
                // the visibleThreshold and need to reload more data.
                // If we do need to reload some more data, we execute onLoadMore to fetch the data.
                // threshold should reflect how many total columns there are too

                if (!loading && lastVisibleItemPosition + visibleThreshold > totalItemCount) {
                    currentPage++
                    Log.d("APPLICATION_DEMO", "currentPage" + currentPage)
                    onLoadMore(currentPage, totalItemCount, view)
                    loading = true
                }
            }


        }


        postView.addOnScrollListener(scroll)

        swipeRefreshLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                if (postAdapter != null){
                    MainActivity.start = 0
                    MainActivity.end = 20
                    connectActivity!!.goNext(Const.REFRESH_PROFILE_FEED,"")
                }else{
                    swipeRefreshLayout.isRefreshing = false
                }
            }

        })


    }




    override fun onClick(view: View?) {

        if (connectActivity != null) {

            when (view!!.id) {

                R.id.postsLay -> {
                    connectActivity!!.goNext(Const.TO_POSTS,"")
                }
                R.id.followersLay -> {
                    connectActivity!!.goNext(Const.TO_FOLLOWERS,"")

                }
                R.id.followingLay -> {
                    connectActivity!!.goNext(Const.TO_FOLLOWING,"")

                }

                R.id.avatar -> {

                    connectActivity!!.goNext(Const.CHANGE_AVATAR,"")

                }


            }
        }
    }

    fun setAvatar(path:String){

       postAdapter!!.updateProfilPhoto(path)

    }




    fun failedGetList(error:String = ""){
        progressLay.visibility = View.GONE

        swipeRefreshLayout.isRefreshing = false

        log.e("ProfileFragment => method => failedGetList errorCode => $error")
                if (postAdapter != null && postAdapter!!.feeds.posts.size != 0){
                    log.e("list bor lekin xatolik shundo ozini qoldiramiz")
                    emptyContainer.hide()
                    postView.visibility       = View.VISIBLE


                }else{
                    log.e("list null yoki list bom bosh")
                    val emptyPost = ArrayList<Posts>()
                    emptyPost.add(Posts("-1", Quote("", "", ""), ArrayList<Audio>(), ArrayList<Image>(), "0", "0", "", "", PostUser("", "", "http")))
                    val postList: PostList = PostList(emptyPost, "0", "0", "0")
                    postAdapter = FeedAdapter(activity, postList, this, this, true, FOLLOW_TYPE, PostUser("", "", "http"))
                    postView.adapter = postAdapter

//                    val connectErrorIcon = VectorDrawableCompat.create(Base.get.resources, R.drawable.network_error, errorImg.context.theme)
//                    val defaultErrorIcon = VectorDrawableCompat.create(Base.get.resources, R.drawable.account_light,          errorImg.context.theme)
//                    if (error == ""){
//                        errorImg.setImageDrawable(defaultErrorIcon)
//                    }else{
//                        errorImg.setImageDrawable(connectErrorIcon)
//
//                    }
//                    errorText.text = error
//                    emptyContainer.visibility = View.VISIBLE
//                    postView.visibility = View.GONE
                }


    }

    fun initFF(postList: PostList){
        FOLLOWERS = postList.followers
        FOLLOWING = postList.following

//        followers.text = if(FOLLOWERS == "") "0" else FOLLOWERS
//        following.text = if(FOLLOWING == "") "0" else FOLLOWING
    }

    fun swapPosts(postList: PostList){


        log.d("ProfileFragment => method swapPosts => onSuccess")
        log.d("ProfileFragment => method swapPosts => postSize: ${postList.posts.size} posts: ${postList.posts}")


        try {
            swipeRefreshLayout.isRefreshing = false

            scroll!!.resetState()
            emptyContainer.hide()

            progressLay.visibility    = View.GONE

            postView.visibility = View.VISIBLE
            FOLLOWERS = postList.followers
            FOLLOWING = postList.following

            var photo ="http"
            if((MainActivity.end == 20 && MainActivity.start == 0) && postAdapter != null){

               photo = postAdapter!!.feeds.posts.get(0).user.photo

            }else{
                try{
                    photo = if (arguments!!.getString("photo").startsWith("http")) arguments.getString("photo") else Http.BASE_URL +arguments.getString("photo")
                }catch (e:Exception){

                }
            }
            var postUser: PostUser = PostUser(arguments.getString("userId"), arguments.getString("username"), photo)

            postList.posts.forEach { post ->


                post.user = postUser

            }

            if (postAdapter == null){
                log.d("birinchi marta postla yuklandi size: ${postList.posts.size}")
                if(FeedFragment.cachedSongAdapters == null) FeedFragment.cachedSongAdapters = HashMap()

                if (postList.posts.get(0).id != "-1") postList.posts.add(0,postList.posts.get(0))
                postAdapter = FeedAdapter(activity, postList, this, this, true, FOLLOW_TYPE, postUser)

                postView.adapter = postAdapter
            }else if (postList.posts.size == 1 && (MainActivity.endFeed == 1 && MainActivity.startFeed == 0)){
                log.d("post qoshildi postni birinchi elementi update qilinadi")
                MainActivity.start = postAdapter!!.feeds.posts.size

                MainActivity.end = 20
                postAdapter!!.swapFirstItem(postList)
                postView.smoothScrollBy(0,postView.getChildAt(0).height * postAdapter!!.feeds.posts.size)

            }else if ((MainActivity.end == 20 && MainActivity.start == 0) && postAdapter != null){
                log.d("postni boshidan update qisin  F type -> $FOLLOW_TYPE")
                if(FeedFragment.cachedSongAdapters == null) FeedFragment.cachedSongAdapters = HashMap()


                if (postList.posts.get(0).id != "-1") postList.posts.add(0,postList.posts.get(0))

                postAdapter = FeedAdapter(activity, postList, this, this, true, FOLLOW_TYPE, postUser)
                postView.adapter = postAdapter
            }else if((MainActivity.end == 20 && MainActivity.start != 0) && postAdapter != null){
                log.d("postni oxirgi 20 ta elementi keldi")
                postAdapter!!.swapLast20Item(postList)

            }




        }catch (e:Exception){
            log.e("ProfileFragment => swapPosts => $e")
            failedGetList()

        }

    }

    override fun click(position: Int) {


        when(position){
            Const.CHANGE_AVATAR ->  connectActivity!!.goNext(Const.CHANGE_AVATAR,"")

            Const.TO_FOLLOWING -> connectActivity!!.goNext(Const.TO_FOLLOWING,"")
            Const.TO_FOLLOWERS -> connectActivity!!.goNext(Const.TO_FOLLOWERS,"")
        }
    }

    override fun data(data: String) {



    }

    fun closeLoadMore() {
        if (oldpostList == null || oldpostList!!.posts.size <= 0){
            postView.visibility = View.GONE
            emptyContainer.show()

        }
        log.d("hide load more")
        scroll!!.resetState()

//        postView.setPullLoadMoreCompleted()
    }

    internal enum class State {
        EXPANDED,
        COLLAPSED,
        IDLE
    }

    /*
    *
    *
    * MUSIC PLAYER
    *
    * */


    //song list variables
    private var songList: ArrayList<Audio>? = null

    //service
    private var musicSrv: MusicService? = null
    private var playIntent: Intent? = null
    //binding
    private var musicBound = false

    //controller
    private var controller: MusicController? = null

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

            playIntent = Intent(activity, MusicService::class.java)
            activity.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            activity.startService(playIntent)

        }
    }

    override fun canPause(): Boolean = true

    override fun canSeekBackward(): Boolean = true

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
        if(controller != null) controller!!.setLoading(false);
        LocalBroadcastManager.getInstance(activity).registerReceiver(musicReceiver, IntentFilter(MusicService.ACTION_PLAY_TOGGLE))

    }

    override fun seekTo(pos: Int) {
        musicSrv!!.seek(pos)
    }

    override fun start() {
        musicSrv!!.go()
        LocalBroadcastManager.getInstance(activity).registerReceiver(musicReceiver, IntentFilter(MusicService.ACTION_PLAY_TOGGLE))

    }
    override fun goPlayList() {
        startActivity(Intent(activity, PlaylistActivity::class.java))
    }

    private fun setController() {
        if (controller == null){
            controller = MusicController(activity,false)
            //set previous and next button listeners
            controller!!.setPrevNextListeners({ playNext() }, { playPrev() },{ goPlayList() })
            //set and show
            controller!!.setMediaPlayer(this)
            controller!!.setAnchorView(rootView.findViewById(R.id.listFeed))
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
        try {

            if (FeedFragment.cachedSongAdapters != null) {
                FeedFragment.cachedSongAdapters!!.get(FeedFragment.playedSongPosition)!!.notifyDataSetChanged()
            }
        } catch (e: Exception) {

        }

    }

    override fun onPause() {
        super.onPause()
        paused = true

    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(activity).registerReceiver(musicReceiver, IntentFilter(MusicService.ACTION_PLAY_TOGGLE))
        if (paused) {
            setController()
            paused = false
        }
    }


    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden && musicSrv!!.isPng){
            postAdapter!!.notifyDataSetChanged()
        }
        super.onHiddenChanged(hidden)


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



    override fun onStop() {
       if (controller != null){
           controller!!.hide()
       }
        super.onStop()
    }

    override fun onDestroy() {
        activity.stopService(playIntent)
        musicSrv = null
        super.onDestroy()
    }

    override fun playClick(listSong: ArrayList<Audio>, position: Int) {
        if (musicSrv != null){

            if(musicSrv!!.isPng){

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
            Toast.makeText(Base.get, Base.get.resources.getString(R.string.error_something), Toast.LENGTH_SHORT).show()
        }
    }

    fun createProgressForAvatar(status: Int) {
        postAdapter!!.swapPhotoProgress(status)
    }

}