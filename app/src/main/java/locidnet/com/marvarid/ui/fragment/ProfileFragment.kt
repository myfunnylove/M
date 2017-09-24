package locidnet.com.marvarid.ui.fragment

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.ProfileFeedAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseFragment
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.connectors.GoNext
import locidnet.com.marvarid.connectors.MusicPlayerListener
import locidnet.com.marvarid.connectors.SignalListener
import locidnet.com.marvarid.model.*
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.pattern.MControlObserver.MusicControlObserver
import locidnet.com.marvarid.pattern.builder.EmptyContainer
import locidnet.com.marvarid.resources.customviews.loadmorerecyclerview.EndlessRecyclerViewScrollListener
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Functions
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.FollowActivity
import locidnet.com.marvarid.ui.activity.MainActivity
import kotlin.properties.Delegates


class ProfileFragment : BaseFragment() , View.OnClickListener,AdapterClicker,MusicPlayerListener,MusicControlObserver{



    var postView               by Delegates.notNull<RecyclerView>()
    var progressLay            by Delegates.notNull<ViewGroup>()
    var swipeRefreshLayout     by Delegates.notNull<SwipeRefreshLayout>()

    var user                          = Base.get.prefs.getUser()
    var oldpostList:PostList?         = null
    var postAdapter: ProfileFeedAdapter?      = null

    var connectActivity:GoNext?       = null
    val model                         = Model()
    var manager:LinearLayoutManager?  = null
    var expanded                      = false
    var userInfo:UserInfo?= null

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
        MainActivity.musicSubject.subscribe(this)
//        FOLLOW_TYPE = arguments.getString(F_TYPE)


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
                    FollowActivity.start = (postAdapter!!.feeds.posts.size - 1)
                    FollowActivity.end   = 20
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

        swipeRefreshLayout.setOnRefreshListener(object :SwipeRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                if (postAdapter != null ){
                    FollowActivity.start = 0
                    FollowActivity.end   = 20
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






    fun initHeader(userInfo:UserInfo,fType:String){
        log.d("ProfileFragment => initheader")

        this.userInfo = userInfo
        FOLLOWERS  = userInfo.user.count.followersCount
        FOLLOWING  = userInfo.user.count.followersCount
        POST_COUNT = userInfo.user.count.postCount

        FOLLOW_TYPE = if (fType == ProfileFragment.CLOSE ) ProfileFragment.FOLLOW else fType
        progressLay.visibility = View.GONE
        emptyContainer.hide()
        postView.visibility       = View.VISIBLE



        var photo ="http"
        try{
            photo = if (arguments!!.getString("photo").startsWith("http")) arguments.getString("photo") else Http.BASE_URL+arguments.getString("photo")
        }catch (e:Exception){

        }


        val emptyPost = ArrayList<Posts>()
        emptyPost.add(Posts("-1", Quote("","",""),ArrayList<Audio>(),ArrayList<Image>(),"0","0","","", PostUser("","","")))


        val postList = PostList(emptyPost)
        val isClose = fType == ProfileFragment.REQUEST || fType == ProfileFragment.CLOSE

        postAdapter = ProfileFeedAdapter(activity,postList,this,this,userInfo,true,FOLLOW_TYPE,isClose)
        postView.visibility = View.VISIBLE
        postView.adapter = postAdapter
        swipeRefreshLayout.isEnabled = false
        swipeRefreshLayout.isRefreshing = false

    }

    fun initBody(postList: PostList){

        log.d("ProfileFragment => method swapPosts => onSuccess")
        log.d("ProfileFragment => method swapPosts => postSize: ${postList.posts.size} posts: ${postList.posts}")


        try {
            swipeRefreshLayout.isRefreshing = false

            log.d("in body user info $userInfo")
            val postUser = PostUser(userInfo!!.user.info.user_id,

                                    userInfo!!.user.info.username,
                                    if (!userInfo!!.user.info.photoOrg.isNullOrEmpty()) userInfo!!.user.info.photoOrg else "")
            postList.posts.forEach { item ->
                item.user = postUser
            }


            scroll!!.resetState()
            emptyContainer.hide()

            progressLay.visibility    = View.GONE

            postView.visibility = View.VISIBLE


            var photo ="http"

            if (postAdapter == null){

                log.d("birinchi marta postla yuklandi size: ${postList.posts.size}")
                if(FeedFragment.cachedSongAdapters == null) FeedFragment.cachedSongAdapters = HashMap()

                if (postList.posts.get(0).id != "-1") postList.posts.add(0,postList.posts.get(0))
                postAdapter = ProfileFeedAdapter(activity,postList,this,this,userInfo,true, FOLLOW_TYPE)
                postView.adapter = postAdapter

            }

            else if ((FollowActivity.end == 20 && FollowActivity.start == 0) && postAdapter != null){
                if(FeedFragment.cachedSongAdapters == null) FeedFragment.cachedSongAdapters = HashMap()


                if (postList.posts.get(0).id != "-1") postList.posts.add(0,postList.posts.get(0))

                postAdapter = ProfileFeedAdapter(activity,postList,this,this,userInfo,true, FOLLOW_TYPE)
                postView.adapter = postAdapter
            }else if((FollowActivity.end == 20 && FollowActivity.start != 0) && postAdapter != null){
                log.d("postni oxirgi 20 ta elementi keldi")
                postAdapter!!.swapLast20Item(postList)

            }


            swipeRefreshLayout.isRefreshing = false
            swipeRefreshLayout.isEnabled = true


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

        connectAudioList!!.playClick(listSong,position)
    }


    override fun onDestroy() {
        MainActivity.musicSubject.unsubscribe(this)

        super.onDestroy()

    }

    override fun playPause(id: String) {
        try {
//
            log.d("PATTERN OBSERVER CALLED OTHER PROFILE FRAGMENT")

            if (FeedFragment.cachedSongAdapters != null) {
                FeedFragment.cachedSongAdapters!!.get(FeedFragment.playedSongPosition)!!.notifyDataSetChanged()
            }

        } catch (e: Exception) {

        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (signalListener != null) signalListener!!.turnOn()

    }
}