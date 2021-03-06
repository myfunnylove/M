package locidnet.com.marvarid.ui.fragment

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import io.reactivex.Observable
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.ProfileFeedAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseFragment
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.connectors.GoNext
import locidnet.com.marvarid.connectors.MusicPlayerListener
import locidnet.com.marvarid.connectors.ProfileMusicController
import locidnet.com.marvarid.model.*
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.pattern.MControlObserver.MusicControlObserver
import locidnet.com.marvarid.pattern.builder.EmptyContainer
import locidnet.com.marvarid.player.PlayerService
import locidnet.com.marvarid.resources.adapterAnim.LinearLayoutManagerWithSmoothScroller
import locidnet.com.marvarid.resources.adapterAnim.ScaleInAnimationAdapter
import locidnet.com.marvarid.resources.adapterAnim.ScaleInBottomAnimator
import locidnet.com.marvarid.resources.adapterAnim.SlideInBottomAnimationAdapter
import locidnet.com.marvarid.resources.customviews.loadmorerecyclerview.EndlessRecyclerViewScrollListener
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Prefs
import locidnet.com.marvarid.resources.utils.Toaster
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.MainActivity
import kotlin.properties.Delegates

class MyProfileFragment : BaseFragment() , View.OnClickListener, AdapterClicker, MusicPlayerListener,MusicControlObserver, ProfileMusicController {



    var postView               by Delegates.notNull<RecyclerView>()
    var progressLay            by Delegates.notNull<ViewGroup>()
    var swipeRefreshLayout     by Delegates.notNull<SwipeRefreshLayout>()

    var user                          = Base.get.prefs.getUser()
    var oldpostList: PostList?         = null
    var postAdapter: ProfileFeedAdapter?      = null

    var connectActivity: GoNext?       = null
    val model                         = Model()
    var manager: LinearLayoutManagerWithSmoothScroller?  = null
    var expanded                      = false
    var initBody                      = false
    var profileControl:ProfileMusicController? = null

    lateinit var emptyContainer:EmptyContainer
    var scroll: EndlessRecyclerViewScrollListener? = null
    lateinit var userInfo:UserInfo
    companion object {
        var TAG:String   = "MyProfileFragment"
        val FOLLOW       = Base.get.resources.getString(R.string.follow)
        val UN_FOLLOW    = Base.get.resources.getString(R.string.unfollow)
        val REQUEST      = Base.get.resources.getString(R.string.request)
        val SETTINGS     = Base.get.resources.getString(R.string.settings)
        val F_TYPE       = "fType"
        var FOLLOW_TYPE  = ""
        var listSong:ArrayList<Audio>? = null

        fun newInstance(data: Bundle): MyProfileFragment {


            val newsFragment = MyProfileFragment()

            newsFragment.arguments = data
            return newsFragment

        }
        var FOLLOWERS                     = "0"
        var FOLLOWING                     = "0"
        var POST_COUNT                    = "0"
//        var cachedSongAdapters:HashMap<Int, PostAudioGridAdapter>? = null
        var playedSongPosition  = -1

    }

    fun connect(connActivity: GoNext){
        connectActivity = connActivity

    }
    var connectAudioList: MusicPlayerListener? = null

    fun connectAudioPlayer(connAudioList: MusicPlayerListener){
        connectAudioList = connAudioList
    }
    override fun getFragmentView(): Int = R.layout.fragment_profil_page

    override fun init() {
        Const.TAG = "MyProfileFragment"

        FOLLOW_TYPE = arguments.getString(F_TYPE)


        log.d("init profil fragment")

        if(MainActivity.musicSubject != null) MainActivity.musicSubject!!.subscribe(this)


        progressLay    = rootView.findViewById<ViewGroup>(R.id.progressLay)
        swipeRefreshLayout = rootView.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        postView     = rootView.findViewById<RecyclerView>(R.id.postList)


        emptyContainer = EmptyContainer.Builder()
                .setIcon(R.drawable.account_light)
                .setText(R.string.error_empty_universal)
                .initLayoutForFragment(rootView)

                .build()


        manager = LinearLayoutManagerWithSmoothScroller(Base.get)
        postView.layoutManager = manager
//        postView.setHasFixedSize(true)
//        postView.itemAnimator = ScaleInBottomAnimator()
        scroll = object : EndlessRecyclerViewScrollListener(manager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                log.d("PROFIL POSTLARI OXIRIGA KELDI ${manager!!.findLastVisibleItemPosition()}")
                if (postAdapter != null && postAdapter!!.feeds.posts.size >= 10){
                    MainActivity.start = (postAdapter!!.feeds.posts.size - 1)
                    MainActivity.end = 10
                    connectActivity!!.goNext(Const.REFRESH_PROFILE_FEED,"")
                }


            }

            override fun onScrolled(view: RecyclerView?, dx: Int, dy: Int) {
                var lastVisibleItemPosition = 0

                val totalItemCount = mLayoutManager.itemCount
                swipeRefreshLayout.isEnabled = mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0

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

                if (!loading && lastVisibleItemPosition + visibleThreshold > totalItemCount - 2) {
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
                    MainActivity.end = 10
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


    fun updateUserInfo(userInfo:UserInfo,fType:String){


        if (postAdapter != null){
            this.userInfo = userInfo
            FOLLOWERS  = userInfo.user.count.followersCount
            FOLLOWING  = userInfo.user.count.followingCount
            POST_COUNT = userInfo.user.count.postCount



            progressLay.visibility = View.GONE
            emptyContainer.hide()
            postView.visibility       = View.VISIBLE



            val emptyPost = ArrayList<Posts>()
            emptyPost.add(Posts("-1", Quote("","",""),ArrayList<Image>(),"0","0","","",PostUser("","","")))


//            val isClose = fType == ProfileFragment.REQUEST || fType == ProfileFragment.CLOSE
                postAdapter!!.userInfo = userInfo
              postAdapter!!.notifyItemChanged(0)


        }else{
            initHeader(userInfo,fType)
        }
    }

    fun setProfileMusicController(profileMusicController: ProfileMusicController){
        this.profileControl = profileMusicController
    }

    fun initHeader(userInfo:UserInfo,fType:String){
        this.userInfo = userInfo
        Prefs.Builder().setUserInfo(this.userInfo)
        FOLLOWERS  = if(!userInfo.user.count.followersCount.isNullOrEmpty()) userInfo.user.count.followersCount else "0"
        FOLLOWING  = if(!userInfo.user.count.followingCount.isNullOrEmpty()) userInfo.user.count.followingCount else "0"
        POST_COUNT = if(!userInfo.user.count.postCount.isNullOrEmpty()) userInfo.user.count.postCount else "0"


        progressLay.visibility = View.GONE
        emptyContainer.hide()
        postView.visibility       = View.VISIBLE



        var photo ="http"
        try{
            photo = if (arguments!!.getString("photo").startsWith("http")) arguments.getString("photo") else Http.BASE_URL+arguments.getString("photo")
        }catch (e:Exception){

        }


        val emptyPost = ArrayList<Posts>()
        emptyPost.add(Posts("-1", Quote("","",""),ArrayList<Image>(),"0","0","","",PostUser("","","")))

        val postList = PostList(emptyPost)

        val isClose = fType == ProfileFragment.REQUEST || fType == ProfileFragment.CLOSE

       if (postAdapter == null){
           postAdapter = ProfileFeedAdapter(activity,postList,this,this,this,userInfo,true,fType,isClose)
//           var slideAdapter: ScaleInAnimationAdapter? = ScaleInAnimationAdapter(postAdapter)
//           slideAdapter!!.setFirstOnly(false)
//           slideAdapter.setDuration(500)
//
//           postView.visibility = View.VISIBLE
           postView.adapter = postAdapter
//           slideAdapter = null
       }else{
           log.d("update first item $userInfo")
           postAdapter!!.updateFirstItem(userInfo)
       }
//        swipeRefreshLayout.isEnabled = false
        swipeRefreshLayout.isRefreshing = false
    }

    fun initBody(postList: PostList){

        log.d("ProfileFragment => method swapPosts => onSuccess")
        log.d("ProfileFragment => method swapPosts => postSize: ${postList.posts.size} posts: ${postList.posts}")


        try {
            initBody = true

            swipeRefreshLayout.isRefreshing = false

            scroll!!.resetState()
            emptyContainer.hide()

            progressLay.visibility    = View.GONE

            postView.visibility = View.VISIBLE
            user = Prefs.Builder().getUser()
            val postUser = PostUser(user.userId,user.userName,if (user.profilPhoto.isNullOrEmpty()) "" else user.profilPhoto)
            postList.posts.forEach { item ->
                item.user = postUser
            }

            var photo ="http"

            if (postAdapter == null){
                log.d("birinchi marta postla yuklandi size: ${postList.posts.size}")
//                if(MyProfileFragment.cachedSongAdapters == null) MyProfileFragment.cachedSongAdapters = HashMap()

                if (postList.posts.get(0).id != "-1") postList.posts.add(0,postList.posts.get(0))
                postAdapter = ProfileFeedAdapter(activity,postList,this,this,this,userInfo,true,FOLLOW_TYPE)
//                var slideAdapter: ScaleInAnimationAdapter? = ScaleInAnimationAdapter(postAdapter)
//
//
//                slideAdapter!!.setFirstOnly(false)
//
//                slideAdapter!!.setInterpolator(OvershootInterpolator())
//                slideAdapter.setDuration(500)
                postView.adapter = postAdapter
//                slideAdapter = null
            }else if (postList.posts.size == 1 && (MainActivity.end == 1 && MainActivity.start == 0)){
                log.d("post qoshildi postni birinchi elementi update qilinadi")
                MainActivity.start = postAdapter!!.feeds.posts.size

                MainActivity.end = 10
                postAdapter!!.swapFirstItem(postList)
                postView.smoothScrollBy(0,postView.getChildAt(0).height * postAdapter!!.feeds.posts.size)

            }

            else if ((MainActivity.end == 10 && MainActivity.start == 0) && postAdapter != null){
                log.d("postni boshidan update qisin  F type -> $FOLLOW_TYPE")
//                if(MyProfileFragment.cachedSongAdapters == null) MyProfileFragment.cachedSongAdapters = HashMap()


                if (postList.posts.get(0).id != "-1") postList.posts.add(0,postList.posts.get(0))

                postAdapter = ProfileFeedAdapter(activity,postList,this,this,this,userInfo,true,FOLLOW_TYPE)
//                var slideAdapter: ScaleInAnimationAdapter? = ScaleInAnimationAdapter(postAdapter)
//
//
//                slideAdapter!!.setInterpolator(OvershootInterpolator())
//                slideAdapter.setDuration(500)
                postView.adapter = postAdapter
//                slideAdapter = null
            }else if((MainActivity.end == 10 && MainActivity.start != 0) && postAdapter != null){
                log.d("postni oxirgi 20 ta elementi keldi")
                postAdapter!!.swapLast20Item(postList)

            }




        }catch (e:Exception){
            log.e("ProfileFragment => swapPosts => $e")

        }
    }

    override fun click(position: Int) {


        when(position){
            Const.CHANGE_AVATAR -> connectActivity!!.goNext(Const.CHANGE_AVATAR,"")

            Const.TO_FOLLOWING  -> connectActivity!!.goNext(Const.TO_FOLLOWING,"")
            Const.TO_FOLLOWERS  -> connectActivity!!.goNext(Const.TO_FOLLOWERS,"")
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



    override fun playClick(songs: ArrayList<Audio>, position: Int) {
        if (postAdapter != null){
            var key = -1
//            for (i in postAdapter!!.feeds.posts.indices) {
//                if (postAdapter!!.feeds.posts.get(i).audios == FeedFragment.listSong){
//                    key = i
//                }
//            }

            if (key != -1){
//                try{
//                    if (playedSongPosition != -1) cachedSongAdapters!!.get(playedSongPosition)!!.notifyDataSetChanged()
//                    cachedSongAdapters!!.get(key)!!.notifyDataSetChanged()
//                    playedSongPosition = key
//                }catch (e:Exception){}
            }

        }
        listSong = songs
        connectAudioList!!.playClick(songs,position)
    }

    fun createProgressForAvatar(status: Int) {
        postAdapter!!.swapPhotoProgress(status)
    }

    override fun playPause(id: String) {
        try {
//

//            if (MyProfileFragment.cachedSongAdapters != null) {
//                MyProfileFragment.cachedSongAdapters!!.get(MyProfileFragment.playedSongPosition)!!.notifyDataSetChanged()
//            }
            log.d("play button pressed")
            if (PlayerService.PLAY_STATUS == PlayerService.PLAYING)
                postAdapter!!.updateMusicController(ProfileFeedAdapter.PAUSE)
            else
                postAdapter!!.updateMusicController(ProfileFeedAdapter.PLAY)

        } catch (e: Exception) {

        }
    }
    override fun pressPlay() {
      profileControl!!.pressPlay()
    }

    override fun pressNext() {
        profileControl!!.pressNext()
    }
    override fun onDestroy() {
        log.d("ondestroy myprofil")
        if (postAdapter != null) postAdapter!!.cachedLists.clear()

        MainActivity.musicSubject!!.unsubscribe(this)
        super.onDestroy()
    }
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && postAdapter != null){

            update()
        }

    }

    fun update(){
        var key = -1


       

//        for (i in postAdapter!!.feeds.posts.indices) {
//
//            if (postAdapter!!.feeds.posts.get(i).audios == FeedFragment.listSong){
//                key = i
//            }
//
//        }



        if (key != -1){

            try{
//                if (playedSongPosition != -1) cachedSongAdapters!!.get(playedSongPosition)!!.notifyDataSetChanged()
//
//                cachedSongAdapters!!.get(key)!!.notifyDataSetChanged()

                playedSongPosition = key
                postAdapter
            }catch (e:Exception){

            }
        }else {

//            for (i in postAdapter!!.feeds.posts.indices) {
//                for(j in postAdapter!!.feeds.posts.get(i).audios.indices){
//                    if (!PlayerService.PLAYING_SONG_URL.isNullOrEmpty() &&
//                            postAdapter!!.feeds.posts.get(i).audios.get(j).middlePath
//                                    ==
//                            PlayerService.PLAYING_SONG_URL
//                            ){
//                     key = i
//                    }
//                }
//
//            }

            if (key != -1){
                try{
//                    if (playedSongPosition != -1) cachedSongAdapters!!.get(playedSongPosition)!!.notifyDataSetChanged()
//
//                    cachedSongAdapters!!.get(key)!!.notifyDataSetChanged()

                    playedSongPosition = key

                }catch (e:Exception){

                }


            }
        }

        if (!PlayerService.PLAYING_SONG_URL.isNullOrEmpty()){
            postAdapter!!.notifyItemChanged(0)
        }
    }

    fun showOnlyHeader() {
        FOLLOWERS  = "0"
        FOLLOWING  = "0"
        POST_COUNT = "0"


        progressLay.visibility = View.GONE
        emptyContainer.hide()
        postView.visibility       = View.VISIBLE



        userInfo = UserInfo(UserData(
                Info(user.profilPhoto,user.userName,user.first_name,user.gender,"","",0,user.userId)
                ,Count("0","0","0"),0,0,"0","0"))

        val emptyPost = ArrayList<Posts>()
        emptyPost.add(Posts("-1", Quote("","",""),ArrayList<Image>(),"0","0","","",PostUser("","","")))

        val postList = PostList(emptyPost)


        if (postAdapter == null){
            postAdapter = ProfileFeedAdapter(activity,postList,this,this,this,userInfo,true,ProfileFragment.SETTINGS,false)
            var slideAdapter: ScaleInAnimationAdapter? = ScaleInAnimationAdapter(postAdapter)
            slideAdapter!!.setFirstOnly(false)
            slideAdapter.setDuration(500)

            postView.visibility = View.VISIBLE
            postView.adapter = slideAdapter
            slideAdapter = null
        }else{
            log.d("update first item $userInfo")
            postAdapter!!.updateFirstItem(userInfo)
        }
//        swipeRefreshLayout.isEnabled = false
        swipeRefreshLayout.isRefreshing = false
    }


}