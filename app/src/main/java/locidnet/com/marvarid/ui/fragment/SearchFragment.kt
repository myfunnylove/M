package locidnet.com.marvarid.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.*
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import io.reactivex.Observable
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.FollowAdapter
import locidnet.com.marvarid.adapter.RecommendedAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseFragment
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.connectors.GoNext
import locidnet.com.marvarid.connectors.SignalListener
import locidnet.com.marvarid.model.PostList
import locidnet.com.marvarid.model.RecPost
import locidnet.com.marvarid.model.User
import locidnet.com.marvarid.model.Users
import locidnet.com.marvarid.pattern.builder.EmptyContainer
import locidnet.com.marvarid.resources.adapterAnim.ScaleInAnimationAdapter
import locidnet.com.marvarid.resources.adapterAnim.ScaleInBottomAnimator
import locidnet.com.marvarid.resources.customviews.loadmorerecyclerview.EndlessRecyclerViewScrollListener
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Prefs
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.FollowActivity
import locidnet.com.marvarid.ui.activity.SearchActivity
import locidnet.com.marvarid.ui.activity.UserPostActivity
import kotlin.properties.Delegates

/**
 * Created by Michaelan on 5/19/2017.
 */
class SearchFragment : BaseFragment(), AdapterClicker{

    var search:Toolbar?    = null
    var list:RecyclerView? = null
    var swipe:SwipeRefreshLayout? = null

    var recPosts:ArrayList<RecPost>? = null
    var adapter:FollowAdapter?       = null
    val user:User                    = Base.get.prefs.getUser()
    var scroll:EndlessRecyclerViewScrollListener? = null
    var manager:GridLayoutManager?   = null



    companion object {
        var TAG:String  = "SearchFragment"

        fun newInstance(): SearchFragment {


            val newsFragment = SearchFragment()
            val args = Bundle()

            newsFragment.arguments = args
            return newsFragment

        }


    }

    var connectActivity:GoNext?     = null
    fun connect(connActivity: GoNext){
        connectActivity = connActivity

    }



    override fun getFragmentView(): Int = R.layout.fragment_search

    override fun init() {
        Const.TAG = "SearchFragment"


        list           = rootView.findViewById<RecyclerView>(R.id.list)
        search         = rootView.findViewById<Toolbar>(R.id.toolbar)
        swipe          = rootView.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        search!!.setOnClickListener {
            startActivity(Intent(activity!!,SearchActivity::class.java))
            activity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
        }
        manager = GridLayoutManager(activity,3)
        list!!.layoutManager =  manager
        list!!.setHasFixedSize(true)
        list!!.itemAnimator = ScaleInBottomAnimator()
        scroll = object : EndlessRecyclerViewScrollListener(manager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {



            }

            override fun onScrolled(view: RecyclerView?, dx: Int, dy: Int) {
                var lastVisibleItemPosition = 0

                val totalItemCount = mLayoutManager.itemCount
//                swipe!!.isEnabled = mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0

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


        list!!.addOnScrollListener(scroll)

        swipe!!.setOnRefreshListener(object :SwipeRefreshLayout.OnRefreshListener{
            override fun onRefresh() {

                    connectActivity!!.goNext(Const.GET_15_POST,"")
            }

        })

    }



    override fun click(position: Int) {
        val data = Intent(context, UserPostActivity::class.java)
        data.putExtra("postId",recPosts!!.get(position).postId.toInt())
//        val intent = Intent(activity,FollowActivity::class.java)
//        intent.putExtra("user_id",recPosts!!.get(position).userId)
//        intent.putExtra("username",recPosts!!.get(position).username)
//        intent.putExtra(FollowActivity.TYPE,FollowActivity.PROFIL_T)
        startActivity(data)
    }

    override fun data(data: String) {

    }

    fun hideProgress() {

    }

    fun swapPosts(postList: ArrayList<RecPost>) {
        swipe!!.isRefreshing  = false
        recPosts = postList
        val adapter = RecommendedAdapter(this,Base.get.applicationContext,recPosts!!)
        var slideAdapter: ScaleInAnimationAdapter? = ScaleInAnimationAdapter(adapter)

        slideAdapter!!.setFirstItem(true)
        slideAdapter.setFirstOnly(true)

        slideAdapter.setInterpolator(OvershootInterpolator())
        slideAdapter.setDuration(500)
        list!!.adapter = slideAdapter

    }

    fun failedGetList() {
        swipe!!.isRefreshing  = false

    }


}