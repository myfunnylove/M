package locidnet.com.marvarid.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.MyFeedAdapter
import locidnet.com.marvarid.adapter.PostAudioGridAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseFragment
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.connectors.GoNext
import locidnet.com.marvarid.connectors.MusicPlayerListener
import locidnet.com.marvarid.model.Audio
import locidnet.com.marvarid.model.PostList
import locidnet.com.marvarid.pattern.MControlObserver.MusicControlObserver
import locidnet.com.marvarid.pattern.builder.EmptyContainer
import locidnet.com.marvarid.player.PlayerService
import locidnet.com.marvarid.resources.customviews.loadmorerecyclerview.EndlessRecyclerViewScrollListener
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.FollowActivity
import locidnet.com.marvarid.ui.activity.MainActivity
import kotlin.properties.Delegates
import locidnet.com.marvarid.resources.adapterAnim.*


/**
 * Created by Michaelan on 5/19/2017.
 */
class FeedFragment : BaseFragment(), AdapterClicker,MusicPlayerListener, MusicControlObserver{



    var feedAdapter: MyFeedAdapter? = null
    var listFeed               by Delegates.notNull<RecyclerView>()
    var progressLay            by Delegates.notNull<ViewGroup>()
    var swipeRefreshLayout     by Delegates.notNull<SwipeRefreshLayout>()
    //  var refreshLayout  by Delegates.notNull<RecyclerRefreshLayout>()
    val user = Base.get.prefs.getUser()
    var manager:LinearLayoutManagerWithSmoothScroller?                      = null
    var scroll:EndlessRecyclerViewScrollListener?         = null

    lateinit var emptyContainer:EmptyContainer
    companion object {
        var TAG: String = "FeedFragment"

        fun newInstance(): FeedFragment {


            val newsFragment = FeedFragment()
            val args = Bundle()

            newsFragment.arguments = args
            return newsFragment
        }

        var cachedSongAdapters:HashMap<Int,PostAudioGridAdapter>? = null
        var playedSongPosition  = -1
        var listSong:ArrayList<Audio>? = null

    }

    var connectActivity: GoNext? = null
    fun connect(connActivity: GoNext) {
        connectActivity = connActivity

    }

    var connectAudioList: MusicPlayerListener? = null

    fun connectAudioPlayer(connAudioList: MusicPlayerListener){
        connectAudioList = connAudioList
    }
    override fun getFragmentView(): Int {
        return R.layout.fragment_feed
    }

    override fun init() {
        Const.TAG = "FeedFragment"

        MainActivity.musicSubject!!.subscribe(this)

        progressLay    = rootView.findViewById<ViewGroup>(R.id.progressLay)

        swipeRefreshLayout    = rootView.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        listFeed       = rootView.findViewById<RecyclerView>(R.id.listFeed)

        emptyContainer = EmptyContainer.Builder()
                                       .setIcon(R.drawable.feed_light)
                                       .setText(R.string.error_empty_feed)
                                       .initLayoutForFragment(rootView)
                                       .build()
        manager = LinearLayoutManagerWithSmoothScroller(Base.get)


//        manager!!.startSmoothScroll(smoothScroller)
        listFeed.layoutManager = manager
        listFeed.setHasFixedSize(true)
//        listFeed.smoothScrollToPosition(-10)
        listFeed.itemAnimator = SlideInUpAnimator(OvershootInterpolator(1f))

        scroll = object : EndlessRecyclerViewScrollListener(manager) {
            override fun onScrolled(view: RecyclerView?, dx: Int, dy: Int) {
                var lastVisibleItemPosition = 0
                val totalItemCount = mLayoutManager.itemCount

                lastVisibleItemPosition = (mLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                swipeRefreshLayout.isEnabled = mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0

//                log.d("swipe is ${swipeRefreshLayout.isEnabled}")
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

            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                if (feedAdapter != null && feedAdapter!!.feeds.posts.size >= 10){
                    log.d("on more $page $totalItemsCount ")
                    MainActivity.startFeed = feedAdapter!!.feeds.posts.size
                    MainActivity.endFeed = 10

                    log.d("FeedFragment => method onload more => startfrom: ${MainActivity.startFeed}")

                    connectActivity!!.goNext(Const.REFRESH_FEED,"")
                }
            }

        }
        listFeed.addOnScrollListener(scroll)

        swipeRefreshLayout.setOnRefreshListener(object :SwipeRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                if (feedAdapter != null){
                    MainActivity.startFeed = 0
                    MainActivity.endFeed   = 10
                    connectActivity!!.goNext(Const.REFRESH_FEED,"")
                }else{
                    swipeRefreshLayout.isRefreshing = false
                }
            }

        })

    }

    fun showProgress(){
        log.d("show progress")


        emptyContainer.hide()
        progressLay.visibility = View.VISIBLE
    }
    fun hideProgress(){
        log.d("hide progress")

        progressLay.visibility = View.GONE
    }
    override fun click(position: Int) {
        val user = feedAdapter!!.feeds.posts.get(position).user

        if (user.userId != this.user.userId){

            val go = Intent(activity, FollowActivity::class.java)
            val bundle = Bundle()
            bundle.putString("username",user.username)
            bundle.putString("photo",   user.photo)
            bundle.putString("user_id",  user.userId)
            bundle.putString(ProfileFragment.F_TYPE,ProfileFragment.UN_FOLLOW)
            go.putExtra(FollowActivity.TYPE, FollowActivity.PROFIL_T)
            go.putExtras(bundle)
            startActivityForResult(go,Const.FOLLOW)
        }else{
            connectActivity!!.goNext(Const.PROFIL_PAGE,"")
        }



    }

    override fun data(data: String) {
    }



    fun failedGetList(error:String = ""){

        progressLay.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = false

        log.e("FeedFragment => method => failedGetList errorCode => $error")
        if (feedAdapter != null && feedAdapter!!.feeds.posts.size != 0){
            log.e("list bor lekin xatolik shundo ozini qoldiramiz")


            emptyContainer.hide()
            listFeed.visibility = View.VISIBLE


        }else{
            log.e("list null yoki list bom bosh")

            emptyContainer.show()

            listFeed.visibility = View.GONE
        }

    }

    fun swapPosts(postList: PostList){
        log.d("FeedFragment => method swapPosts => onSuccess")
        log.d("FeedFragment => method swapPosts => postSize: ${postList.posts.size} posts: ${postList.posts}")
        log.d("FeedFragment => method swapPosts => startfrom: ${MainActivity.start}")

        try {

            scroll!!.resetState()
            emptyContainer.hide()

            progressLay.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = false
            listFeed.visibility = View.VISIBLE

            if (feedAdapter == null){
                log.d("birinchi marta postla yuklandi")
                cachedSongAdapters = HashMap()
                feedAdapter = MyFeedAdapter(activity,postList,this,this)
                var slideAdapter:ScaleInAnimationAdapter? =ScaleInAnimationAdapter(feedAdapter)
                slideAdapter!!.setInterpolator(OvershootInterpolator())
                slideAdapter.setDuration(500)
                listFeed.adapter = slideAdapter
                slideAdapter = null
            }else if (postList.posts.size == 1 && (MainActivity.endFeed == 1 && MainActivity.startFeed == 0)){
                log.d("post qoshildi postni birinchi elementi update qilinadi")
                MainActivity.startFeed = feedAdapter!!.feeds.posts.size

                MainActivity.endFeed = 10
                feedAdapter!!.swapFirstItem(postList)
                listFeed.scrollToPosition(0)
            }else if ((MainActivity.endFeed == 10 && MainActivity.startFeed == 0) && feedAdapter != null){
                log.d("postni boshidan update qisin")
                cachedSongAdapters = HashMap()

                feedAdapter = MyFeedAdapter(activity,postList,this,this)
                var slideAdapter:ScaleInAnimationAdapter? =ScaleInAnimationAdapter(feedAdapter)


                slideAdapter!!.setInterpolator(OvershootInterpolator())
                slideAdapter.setDuration(500)
                listFeed.adapter = slideAdapter
                slideAdapter = null

            }else if((MainActivity.endFeed == 10 && MainActivity.startFeed != 0) && feedAdapter != null){
                log.d("postni oxirgi 20 ta elementi keldi")
                feedAdapter!!.swapLast20Item(postList)

            }
        }catch (e:Exception){
            log.e("FeedFragment => swapPosts => $e")
            failedGetList()

        }

    }


    /*
    *
    *
    * MUSIC PLAYER
    *
    * */




    override fun playClick(songs: ArrayList<Audio>, position: Int) {
        if (feedAdapter != null){
            var key = -1
            for (i in feedAdapter!!.feeds.posts.indices) {
                if (feedAdapter!!.feeds.posts.get(i).audios == MyProfileFragment.listSong){
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
        listSong = songs

        connectAudioList!!.playClick(songs,position)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        log.d("onactivity result")
    }


//    override fun onHiddenChanged(hidden: Boolean) {
//        super.onHiddenChanged(hidden)
//        if (!hidden){
//            try {
//
//                if (FeedFragment.cachedSongAdapters != null) {
//                    FeedFragment.cachedSongAdapters!!.get(FeedFragment.playedSongPosition)!!.notifyDataSetChanged()
//                }
//
//            } catch (e: Exception) {
//
//            }
//        }
//    }


    override fun playPause(id: String) {
        try {
            log.d("PATTERN OBSERVER CALLED FEEDFRAGMENT")
//

                if (FeedFragment.cachedSongAdapters != null) {
                    FeedFragment.cachedSongAdapters!!.get(FeedFragment.playedSongPosition)!!.notifyDataSetChanged()
                }

            } catch (e: Exception) {

            }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && feedAdapter != null){
           update()

        }

    }


    fun update(){
        var key = -1
        for (i in feedAdapter!!.feeds.posts.indices) {
            if (feedAdapter!!.feeds.posts.get(i).audios == MyProfileFragment.listSong){
                key = i
            }
        }

        if (key != -1){
            try{
                if (playedSongPosition != -1) cachedSongAdapters!!.get(playedSongPosition)!!.notifyDataSetChanged()
                cachedSongAdapters!!.get(key)!!.notifyDataSetChanged()
                playedSongPosition = key
            }catch (e:Exception){}
        }else{
            for (i in feedAdapter!!.feeds.posts.indices) {
                for(j in feedAdapter!!.feeds.posts.get(i).audios.indices){
                    if (!PlayerService.PLAYING_SONG_URL.isNullOrEmpty() &&
                            feedAdapter!!.feeds.posts.get(i).audios.get(j).middlePath
                                    ==
                                    PlayerService.PLAYING_SONG_URL
                            ){
                        key = i
                    }
                }

            }

            if (key != -1){
                try{
                    if (playedSongPosition != -1) cachedSongAdapters!!.get(playedSongPosition)!!.notifyDataSetChanged()

                    cachedSongAdapters!!.get(key)!!.notifyDataSetChanged()

                    playedSongPosition = key

                }catch (e:Exception){

                }


            }
        }
    }

    override fun onDestroy() {
        log.d("ondestroy feed")
        MainActivity.musicSubject!!.unsubscribe(this)

        super.onDestroy()
    }

}