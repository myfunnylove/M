package locidnet.com.marvarid.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.FeedAdapter
import locidnet.com.marvarid.adapter.PostAudioGridAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseFragment
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.connectors.GoNext
import locidnet.com.marvarid.connectors.MusicPlayerListener
import locidnet.com.marvarid.model.Audio
import locidnet.com.marvarid.model.PostList
import locidnet.com.marvarid.pattern.builder.EmptyContainer
import locidnet.com.marvarid.resources.customviews.loadmorerecyclerview.EndlessRecyclerViewScrollListener
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.FollowActivity
import locidnet.com.marvarid.ui.activity.MainActivity
import kotlin.properties.Delegates


/**
 * Created by Michaelan on 5/19/2017.
 */
class FeedFragment : BaseFragment(), AdapterClicker,MusicPlayerListener{



    var feedAdapter:FeedAdapter? = null
    var listFeed               by Delegates.notNull<RecyclerView>()
    var progressLay            by Delegates.notNull<ViewGroup>()
    var swipeRefreshLayout     by Delegates.notNull<SwipeRefreshLayout>()
    //  var refreshLayout  by Delegates.notNull<RecyclerRefreshLayout>()
    val user = Base.get.prefs.getUser()
    var manager:LinearLayoutManager?                      = null
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

        progressLay    = rootView.findViewById(R.id.progressLay)    as ViewGroup

        swipeRefreshLayout    = rootView.findViewById(R.id.swipeRefreshLayout)    as SwipeRefreshLayout

        listFeed       = rootView.findViewById(R.id.listFeed)       as RecyclerView

        emptyContainer = EmptyContainer.Builder()
                                       .setIcon(R.drawable.feed_light)
                                       .setText(R.string.error_empty_feed)
                                       .initLayoutForFragment(rootView)
                                       .build()
        manager = LinearLayoutManager(Base.get)
        listFeed.layoutManager = manager
        listFeed.setHasFixedSize(true)
        scroll = object : EndlessRecyclerViewScrollListener(manager) {
            override fun onScrolled(view: RecyclerView?, dx: Int, dy: Int) {
                var lastVisibleItemPosition = 0
                val totalItemCount = mLayoutManager.itemCount

                lastVisibleItemPosition = (mLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                swipeRefreshLayout.setEnabled(mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);


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

            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                if (feedAdapter != null && feedAdapter!!.feeds.posts.size >= 20){
                    log.d("on more $page $totalItemsCount ")
                    MainActivity.startFeed = feedAdapter!!.feeds.posts.size
                    MainActivity.endFeed = 20

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
                    MainActivity.endFeed   = 20
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
            bundle.putString("userId",  user.userId)
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
                feedAdapter = FeedAdapter(activity,postList,this,this)
                feedAdapter!!.activity = activity!!
                listFeed.adapter = feedAdapter
            }else if (postList.posts.size == 1 && (MainActivity.endFeed == 1 && MainActivity.startFeed == 0)){
                log.d("post qoshildi postni birinchi elementi update qilinadi")
                MainActivity.startFeed = feedAdapter!!.feeds.posts.size

                MainActivity.endFeed = 20
                feedAdapter!!.swapFirstItem(postList)
                listFeed.scrollToPosition(0)
            }else if ((MainActivity.endFeed == 20 && MainActivity.startFeed == 0) && feedAdapter != null){
                log.d("postni boshidan update qisin")
                cachedSongAdapters = HashMap()

                feedAdapter = FeedAdapter(activity,postList,this,this)
                listFeed.adapter = feedAdapter
            }else if((MainActivity.endFeed == 20 && MainActivity.startFeed != 0) && feedAdapter != null){
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




    override fun playClick(listSong: ArrayList<Audio>, position: Int) {

        connectAudioList!!.playClick(listSong,position)
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


}