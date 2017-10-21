package locidnet.com.marvarid.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_search_by_tag.*
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.MyFeedAdapter
import locidnet.com.marvarid.adapter.PostAudioGridAdapter
import locidnet.com.marvarid.adapter.SearchByTagAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.connectors.GoNext
import locidnet.com.marvarid.connectors.MusicPlayerListener
import locidnet.com.marvarid.di.DaggerMVPComponent
import locidnet.com.marvarid.di.modules.ErrorConnModule
import locidnet.com.marvarid.di.modules.MVPModule
import locidnet.com.marvarid.di.modules.PresenterModule
import locidnet.com.marvarid.model.Audio
import locidnet.com.marvarid.model.PostList
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.mvp.Presenter
import locidnet.com.marvarid.mvp.Viewer
import locidnet.com.marvarid.pattern.MControlObserver.MusicControlObserver
import locidnet.com.marvarid.pattern.builder.EmptyContainer
import locidnet.com.marvarid.pattern.builder.ErrorConnection
import locidnet.com.marvarid.player.PlayerService
import locidnet.com.marvarid.resources.adapterAnim.LinearLayoutManagerWithSmoothScroller
import locidnet.com.marvarid.resources.adapterAnim.ScaleInAnimationAdapter
import locidnet.com.marvarid.resources.adapterAnim.SlideInUpAnimator
import locidnet.com.marvarid.resources.customviews.loadmorerecyclerview.EndlessRecyclerViewScrollListener
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.JS
import locidnet.com.marvarid.resources.utils.Toaster
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.ui.fragment.FeedFragment
import locidnet.com.marvarid.ui.fragment.MyProfileFragment
import locidnet.com.marvarid.ui.fragment.ProfileFragment
import javax.inject.Inject

/**
 * Created by myfunnylove on 20.10.17.
 */
class SearchByTagActivity : BaseActivity(), GoNext,Viewer, MusicPlayerListener, MusicControlObserver, AdapterClicker {



    var musicSrv:PlayerService?                                 = null
    internal var playerServiceBinder: PlayerService.PlayerServiceBinder? = null
    internal var mediaController: MediaControllerCompat?                 = null
    private  var musicBound                                              = false
             var manager: LinearLayoutManagerWithSmoothScroller?         = null
             var scroll: EndlessRecyclerViewScrollListener?              = null

    lateinit var emptyContainer: EmptyContainer
             var feedAdapter: SearchByTagAdapter?                        = null

             var listSong:ArrayList<Audio>?                              = null
    @Inject
    lateinit var presenter: Presenter


    @Inject
    lateinit var errorConn: ErrorConnection

    var user = Base.get.prefs.getUser()

    companion object {
        var cachedSongAdapters:HashMap<Int, PostAudioGridAdapter>?  = null
        var playedSongPosition                                      = -1
        var start          = 0
        var end            = 10
    }

    override fun getLayout(): Int  = R.layout.activity_search_by_tag

    override fun initView() {
        Const.TAG = "SearchByTagActivity"

        DaggerMVPComponent
                .builder()
                .mVPModule(MVPModule(this, Model(),this))
                .presenterModule(PresenterModule())
                .errorConnModule(ErrorConnModule(this,true))
                .build()
                .inject(this)

        emptyContainer = EmptyContainer.Builder()
                .setIcon(R.drawable.feed_light)
                .setText(R.string.error_empty_feed)
                .initLayoutForActivity(this)
                .build()

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = intent.extras.getString("tag")

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {

            onBackPressed()

        }

        MainActivity.musicSubject!!.subscribe(this)

        bindService(Intent(this, PlayerService::class.java), object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                playerServiceBinder = service as PlayerService.PlayerServiceBinder
                musicSrv = service.service
                musicSrv!!.setActivity(this@SearchByTagActivity)
                musicBound = true
                try {
                    mediaController = MediaControllerCompat(this@SearchByTagActivity, playerServiceBinder!!.mediaSessionToken)
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


        manager = LinearLayoutManagerWithSmoothScroller(Base.get)


//        manager!!.startSmoothScroll(smoothScroller)
        list.layoutManager = manager
        list.setHasFixedSize(true)
//        listFeed.smoothScrollToPosition(-10)
        list.itemAnimator = SlideInUpAnimator(OvershootInterpolator(1f))

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
                    start = feedAdapter!!.feeds.posts.size
                    end = 10

                    log.d("SearchByTagActivity => method onload more => startfrom: ${start}")
                    getPostsByTag()
                }
            }

        }
        list.addOnScrollListener(scroll)

        swipeRefreshLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                if (feedAdapter != null){
                    start = 0
                    end   = 10
                    getPostsByTag()
                }else{
                    swipeRefreshLayout.isRefreshing = false
                }
            }

        })


        if(!intent.getStringExtra("tag").isNullOrEmpty()){
          getPostsByTag()
        }
    }



    override fun initProgress() {
    }

    override fun showProgress() {
        progress.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progress.visibility = View.GONE
    }
    private fun getPostsByTag() {
        errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
            override fun connected() {
                val reqObj = JS.get()
                reqObj.put("start",   start)
                reqObj.put("end",     end)
                reqObj.put("tag",     intent.getStringExtra("tag").replace("#",""))

                log.d("search by tag select $reqObj")
                presenter.requestAndResponse(reqObj, Http.CMDS.SEARCH_BY_TAG)



            }

            override fun disconnected() {

            }

        })
    }

    override fun onSuccess(from: String, result: String) {

        try{

            val postList: PostList = Gson().fromJson(result, PostList::class.java)
            try {

                if (postList.posts.size > 0 ){
                    scroll!!.resetState()
                    emptyContainer.hide()

                    swipeRefreshLayout.isRefreshing = false
                    list.visibility = View.VISIBLE

                    if (feedAdapter == null){
                        log.d("birinchi marta postla yuklandi")
                        cachedSongAdapters = HashMap()
                        feedAdapter = SearchByTagAdapter(this,postList,this,this)
                        var slideAdapter: ScaleInAnimationAdapter? =ScaleInAnimationAdapter(feedAdapter)
                        slideAdapter!!.setInterpolator(OvershootInterpolator())
                        slideAdapter.setDuration(500)
                        list.adapter = slideAdapter
                        slideAdapter = null
                    }else if (postList.posts.size == 1 && (end == 1 && start == 0)){
                        log.d("post qoshildi postni birinchi elementi update qilinadi")
                        start = feedAdapter!!.feeds.posts.size

                        end = 10
                        feedAdapter!!.swapFirstItem(postList)
                        list.scrollToPosition(0)
                    }else if ((end == 10 && start== 0) && feedAdapter != null){
                        log.d("postni boshidan update qisin")
                        cachedSongAdapters = HashMap()

                        feedAdapter = SearchByTagAdapter(this,postList,this,this)
                        var slideAdapter:ScaleInAnimationAdapter? =ScaleInAnimationAdapter(feedAdapter)


                        slideAdapter!!.setInterpolator(OvershootInterpolator())
                        slideAdapter.setDuration(500)
                        list.adapter = slideAdapter
                        slideAdapter = null

                    }else if((end == 10 && start != 0) && feedAdapter != null){
                        log.d("postni oxirgi 20 ta elementi keldi")
                        feedAdapter!!.swapLast20Item(postList)

                    }
                }else{
                    if (feedAdapter == null){
                        onFailure("",resources.getString(R.string.error_empty_result),"")
                    }else
                        onFailure("","","")
                }
            }catch (e:Exception){
                log.e("SearchByTag => swapPosts => $e")

                onFailure("",resources.getString(R.string.error_something),"")

            }

        }catch (e:Exception){
            onFailure("","","")


        }
    }

    override fun onFailure(from: String, message: String, erroCode: String) {
        Toaster.errror(message)
        if (feedAdapter != null && feedAdapter!!.feeds.posts.size != 0){
            log.e("list bor lekin xatolik shundo ozini qoldiramiz")


            emptyContainer.hide()
            list.visibility = View.VISIBLE


        }else{
            log.e("list null yoki list bom bosh")

            emptyContainer.show()

            list.visibility = View.GONE
        }
    }
    override fun goNext(to: Int, data: String) {
    }

    override fun donGo(why: String) {
    }

    override fun playClick(listSong: ArrayList<Audio>, position: Int) {
        if (feedAdapter != null){
            var key = -1
            for (i in feedAdapter!!.feeds.posts.indices) {
                if (feedAdapter!!.feeds.posts.get(i).audios == MyProfileFragment.listSong ||
                        feedAdapter!!.feeds.posts.get(i).audios == FeedFragment.listSong){
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
        this.listSong = listSong

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

    override fun playPause(id: String) {
        try {
            log.d("PATTERN OBSERVER CALLED FEEDFRAGMENT")
//

            if (cachedSongAdapters != null) {
                cachedSongAdapters!!.get(playedSongPosition)!!.notifyDataSetChanged()
            }

        } catch (e: Exception) {

        }
    }
    override fun onDestroy() {
        super.onDestroy()
        start = 0
        end = 10
        MainActivity.musicSubject!!.unsubscribe(this)

    }

    override fun click(position: Int) {
        val user = feedAdapter!!.feeds.posts.get(position).user


            val go = Intent(this, FollowActivity::class.java)
            val bundle = Bundle()
            bundle.putString("username",user.username)
            bundle.putString("photo",   user.photo)
            bundle.putString("user_id",  user.userId)
            bundle.putString(ProfileFragment.F_TYPE,if (user.userId != this.user.userId) ProfileFragment.UN_FOLLOW else ProfileFragment.SETTINGS )
            go.putExtra(FollowActivity.TYPE,  FollowActivity.PROFIL_T )
            go.putExtras(bundle)
            startActivityForResult(go,Const.FOLLOW)
    }

    override fun data(data: String) {
    }
}