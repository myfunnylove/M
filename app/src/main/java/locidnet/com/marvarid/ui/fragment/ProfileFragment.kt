package locidnet.com.marvarid.ui.fragment

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import com.bumptech.glide.Glide
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.PostAudioGridAdapter
import locidnet.com.marvarid.adapter.ProfileFeedAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseFragment
import locidnet.com.marvarid.connectors.*
import locidnet.com.marvarid.rest.Http
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
import locidnet.com.marvarid.resources.utils.Functions
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.FollowActivity
import locidnet.com.marvarid.ui.activity.MainActivity
import kotlin.properties.Delegates


class ProfileFragment : BaseFragment() , View.OnClickListener,AdapterClicker,MusicPlayerListener,MusicControlObserver{



    var postView:RecyclerView? = null
    var progressLay:ViewGroup? = null
    var swipeRefreshLayout:SwipeRefreshLayout? = null

    var user:User?                          = Base.get.prefs.getUser()
    var oldpostList:PostList?         = null
    var postAdapter: ProfileFeedAdapter?      = null

    var connectActivity:GoNext?       = null
    var model:Model?                         = Model()
    var manager:LinearLayoutManagerWithSmoothScroller?  = null
    var expanded                      = false
    var userInfo:UserInfo?= null
    var initBody                      = false
    var scroll:EndlessRecyclerViewScrollListener? = null
    lateinit var emptyContainer: EmptyContainer

    companion object {
        var TAG:String   = "ProfileFragment"
        val FOLLOW       = Base.get.resources.getString(R.string.follow)
        val UN_FOLLOW    = Base.get.resources.getString(R.string.unfollow)
        val REQUEST      = Base.get.resources.getString(R.string.request)
        val SETTINGS     = Base.get.resources.getString(R.string.settings)
        val CLOSE        = Base.get.resources.getString(R.string.closedAccaunt)
        val F_TYPE       = "fType"
        var FOLLOW_TYPE  = ""
        val EMPTY_POSTS  = Base.get.resources.getString(R.string.error_empty_feed)

        fun newInstance(data:Bundle): ProfileFragment {


            val newsFragment = ProfileFragment()

            newsFragment.arguments = data
            return newsFragment

        }
        var FOLLOWERS                     = "0"
        var FOLLOWING                     = "0"
        var POST_COUNT                    = "0"
        var cachedSongAdapters:HashMap<Int, PostAudioGridAdapter>? = null
        var playedSongPosition  = -1


    }

    fun connect(connActivity: GoNext){
        connectActivity = connActivity

    }
    var connectAudioList: MusicPlayerListener? = null

    fun connectAudioPlayer(connAudioList: MusicPlayerListener){
        connectAudioList = connAudioList
    }

    var signalListener: SignalListener? = null

    fun signal(signal: SignalListener){
        signalListener = signal

    }

    override fun getFragmentView(): Int = R.layout.fragment_profil_page

    override fun init() {
        Const.TAG = "ProfileFragment"
        MainActivity.musicSubject!!.subscribe(this)
//        FOLLOW_TYPE = arguments.getString(F_TYPE)


        log.d("init profil fragment")


        progressLay    = rootView.findViewById<ViewGroup>(R.id.progressLay)
        swipeRefreshLayout = rootView.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        postView     = rootView.findViewById<RecyclerView>(R.id.postList)



        emptyContainer = EmptyContainer.Builder()
                                        .setIcon(R.drawable.account_light)
                                        .setText(R.string.error_empty_universal)
                                        .initLayoutForFragment(rootView)

                                        .build()

        manager = LinearLayoutManagerWithSmoothScroller(Base.get)
        postView!!.layoutManager = manager
        postView!!.setHasFixedSize(true)
        postView!!.itemAnimator = ScaleInBottomAnimator()
        scroll = object : EndlessRecyclerViewScrollListener(manager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                log.d("PROFIL POSTLARI OXIRIGA KELDI ${manager!!.findLastVisibleItemPosition()}")
                if (postAdapter != null && postAdapter!!.feeds.posts.size >= 20){
                    FollowActivity.start = (postAdapter!!.feeds.posts.size - 1)
                    FollowActivity.end   = 20
                    connectActivity!!.goNext(Const.PROFIL_PAGE,FollowActivity.start.toString())
                }


            }

            override fun onScrolled(view: RecyclerView?, dx: Int, dy: Int) {
                var lastVisibleItemPosition = 0

                val totalItemCount = mLayoutManager.itemCount
                swipeRefreshLayout!!.isEnabled = mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0

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


        postView!!.addOnScrollListener(scroll)

        swipeRefreshLayout!!.setOnRefreshListener(object :SwipeRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                if (postAdapter != null ){
                    FollowActivity.start = 0
                    FollowActivity.end   = 20
                    connectActivity!!.goNext(Const.REFRESH_PROFILE_FEED,"")
                }else{
                    swipeRefreshLayout!!.isRefreshing = false
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






    fun initHeader(userInfo:UserInfo,fType:String){
        log.d("ProfileFragment => initheader $fType")

        this.userInfo = userInfo
        FOLLOWERS  = if(!userInfo.user.count.followersCount.isNullOrEmpty()) userInfo.user.count.followersCount else "0"
        FOLLOWING  = if(!userInfo.user.count.followingCount.isNullOrEmpty()) userInfo.user.count.followingCount else "0"
        POST_COUNT = if(!userInfo.user.count.postCount.isNullOrEmpty()) userInfo.user.count.postCount else "0"

        FOLLOW_TYPE = if (fType == ProfileFragment.CLOSE ) ProfileFragment.FOLLOW else fType
        progressLay!!.visibility = View.GONE
        emptyContainer.hide()
        postView!!.visibility       = View.VISIBLE






        val emptyPost = ArrayList<Posts>()
        emptyPost.add(Posts("-1", Quote("","",""),ArrayList<Audio>(),ArrayList<Image>(),"0","0","","", PostUser("","","")))


        val postList = PostList(emptyPost)
        val isClose = fType == ProfileFragment.REQUEST || fType == ProfileFragment.CLOSE
        if (postAdapter == null){
            postAdapter = ProfileFeedAdapter(activity,postList,this,this,null,userInfo,true,FOLLOW_TYPE,isClose)
            var slideAdapter: ScaleInAnimationAdapter? = ScaleInAnimationAdapter(postAdapter)
            slideAdapter!!.setDuration(500)
            slideAdapter.setFirstOnly(false)

            postView!!.visibility = View.VISIBLE
            postView!!.adapter = slideAdapter
            slideAdapter = null
        }else{
            postAdapter!!.updateFirstItem(userInfo)
        }

        swipeRefreshLayout!!.isRefreshing = false

    }

    fun initBody(postList: PostList){

        log.d("ProfileFragment => method swapPosts => onSuccess")
        log.d("ProfileFragment => method swapPosts => postSize: ${postList.posts.size} posts: ${postList.posts}")


        try {
            initBody = true

            swipeRefreshLayout!!.isRefreshing = false

            log.d("in body user info $userInfo")
            val postUser = PostUser(userInfo!!.user.info.user_id,

                                    userInfo!!.user.info.username,
                                    if (!userInfo!!.user.info.photoOrg.isNullOrEmpty()) userInfo!!.user.info.photoOrg else "")
            postList.posts.forEach { item ->
                item.user = postUser
            }


            scroll!!.resetState()
            emptyContainer.hide()

            progressLay!!.visibility    = View.GONE

            postView!!.visibility = View.VISIBLE


            var photo ="http"

            if (postAdapter == null){
                if(ProfileFragment.cachedSongAdapters == null) ProfileFragment.cachedSongAdapters = HashMap()

                log.d("birinchi marta postla yuklandi size: ${postList.posts.size}")

                if (postList.posts.get(0).id != "-1") postList.posts.add(0,postList.posts.get(0))
                postAdapter = ProfileFeedAdapter(activity,postList,this,this,null,userInfo,true, FOLLOW_TYPE)
                var slideAdapter: ScaleInAnimationAdapter? = ScaleInAnimationAdapter(postAdapter)


                slideAdapter!!.setFirstOnly(false)

                slideAdapter.setInterpolator(OvershootInterpolator())
                slideAdapter.setDuration(500)
                postView!!.adapter = slideAdapter
                slideAdapter = null

            }

            else if ((FollowActivity.end == 20 && FollowActivity.start == 0) && postAdapter != null){
                if(ProfileFragment.cachedSongAdapters == null) ProfileFragment.cachedSongAdapters = HashMap()


                if (postList.posts.get(0).id != "-1") postList.posts.add(0,postList.posts.get(0))

                postAdapter = ProfileFeedAdapter(activity,postList,this,this,null,userInfo,true, FOLLOW_TYPE)
                var slideAdapter: ScaleInAnimationAdapter? = ScaleInAnimationAdapter(postAdapter)


                slideAdapter!!.setFirstOnly(false)

                slideAdapter.setInterpolator(OvershootInterpolator())
                slideAdapter.setDuration(500)
                postView!!.adapter = slideAdapter
                slideAdapter = null
            }else if((FollowActivity.end == 20 && FollowActivity.start != 0) && postAdapter != null){
                log.d("postni oxirgi 20 ta elementi keldi")
                postAdapter!!.swapLast20Item(postList)

            }


            swipeRefreshLayout!!.isRefreshing = false
            swipeRefreshLayout!!.isEnabled = true


        }catch (e:Exception){
            log.e("ProfileFragment => swapPosts => $e")

        }
    }


    override fun click(position: Int) {


        when(position){
            Const.CHANGE_AVATAR ->  connectActivity!!.goNext( Const.CHANGE_AVATAR ,"")

            Const.TO_FOLLOWING -> connectActivity!!.goNext( Const.TO_FOLLOWING  ,"")
            Const.TO_FOLLOWERS -> connectActivity!!.goNext( Const.TO_FOLLOWERS  ,"")
        }
    }

    override fun data(data: String) {



    }
    override fun playClick(listSong: ArrayList<Audio>, position: Int) {
        if (postAdapter != null){
            var key = -1
            for (i in postAdapter!!.feeds.posts.indices) {
                if (postAdapter!!.feeds.posts.get(i).audios == MyProfileFragment.listSong){
                    key = i
                }
            }

            if (key != -1){
                try{
                    if (playedSongPosition != -1) cachedSongAdapters!!.get(playedSongPosition)!!.notifyDataSetChanged()
                    cachedSongAdapters!!.get(key)!!.notifyDataSetChanged()
                    playedSongPosition = key
                }catch (e:Exception){}
            }

        }
        connectAudioList!!.playClick(listSong,position)
    }



    override fun onDestroy() {
        MainActivity.musicSubject!!.unsubscribe(this)
        log.d("profileFragmentDestroy")
        log.d("free memory before ${Runtime.getRuntime().freeMemory()}")
        postAdapter!!.feeds.posts.clear()
        postAdapter!!.FOLLOW_TYPE = null
        postAdapter!!.activity    = null
        postAdapter!!.clicker     = null
        postAdapter!!.inflater    = null
        postAdapter!!.ctx         = null
        postAdapter!!.model       = null
        postAdapter!!.player      = null
        postAdapter!!.profileControl = null
        connectAudioList          = null
        connectActivity           = null
        postView                  = null
        progressLay               = null
        swipeRefreshLayout        = null

        user                      = null
        oldpostList               = null

        model                     = null
        manager                   = null
        userInfo                  = null
        postAdapter               = null

        log.d("free memory after ${Runtime.getRuntime().freeMemory()}")

        super.onDestroy()

    }

    override fun playPause(id: String) {
        try {
//

            if (ProfileFragment.cachedSongAdapters != null) {
                ProfileFragment.cachedSongAdapters!!.get(ProfileFragment.playedSongPosition)!!.notifyDataSetChanged()
            }
            log.d("play button pressed")
            if (PlayerService.PLAY_STATUS == PlayerService.PLAYING)
                postAdapter!!.updateMusicController(ProfileFeedAdapter.PAUSE)
            else
                postAdapter!!.updateMusicController(ProfileFeedAdapter.PLAY)

        } catch (e: Exception) {

        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (signalListener != null) signalListener!!.turnOn()

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && postAdapter != null){
            var key = -1
            for (i in postAdapter!!.feeds.posts.indices) {
                if (postAdapter!!.feeds.posts.get(i).audios == MyProfileFragment.listSong || postAdapter!!.feeds.posts.get(i).audios == FeedFragment.listSong){
                    key = i
                }
            }

            if (key != -1){
                try{
                    if (ProfileFragment.playedSongPosition != -1) ProfileFragment.cachedSongAdapters!!.get(MyProfileFragment.playedSongPosition)!!.notifyDataSetChanged()

                    ProfileFragment.cachedSongAdapters!!.get(key)!!.notifyDataSetChanged()
                    ProfileFragment.playedSongPosition = key
                }catch (e:Exception){

                }
            }

        }

    }

    override fun onLowMemory() {
        super.onLowMemory()
        Glide.with(this).onLowMemory()
    }
}