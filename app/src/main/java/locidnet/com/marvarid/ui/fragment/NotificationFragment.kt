package locidnet.com.marvarid.ui.fragment

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_settings.*
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.PushAdapter
import locidnet.com.marvarid.base.BaseFragment
import locidnet.com.marvarid.connectors.GoNext
import locidnet.com.marvarid.model.Action
import locidnet.com.marvarid.model.Push
import locidnet.com.marvarid.model.PushList
import locidnet.com.marvarid.model.User
import locidnet.com.marvarid.mvp.Viewer
import locidnet.com.marvarid.pattern.builder.EmptyContainer
import locidnet.com.marvarid.resources.adapterAnim.ScaleInAnimationAdapter
import locidnet.com.marvarid.resources.adapterAnim.ScaleInBottomAnimator
import locidnet.com.marvarid.resources.customviews.loadmorerecyclerview.EndlessRecyclerViewScrollListener
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Prefs
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.MainActivity
import kotlin.properties.Delegates

/**
 * Created by Michaelan on 5/19/2017.
 */
class NotificationFragment : BaseFragment(){

    companion object {
        var TAG:String  = "NotificationFragment"

        fun newInstance(): NotificationFragment {


            val newsFragment = NotificationFragment()
            val args = Bundle()

            newsFragment.arguments = args
            return newsFragment
        }
    }
    var connectActivity:GoNext?     = null

    var emptyContainer:EmptyContainer? = null
    var list:RecyclerView? = null

    var adapter:PushAdapter? = null

    var progressLay            by Delegates.notNull<ViewGroup>()
    var swipeRefreshLayout     by Delegates.notNull<SwipeRefreshLayout>()
    var scroll: EndlessRecyclerViewScrollListener?         = null
    lateinit var user:User
    lateinit var manager:LinearLayoutManager

    fun connect(connActivity: GoNext){
        connectActivity   = connActivity

    }
    override fun getFragmentView(): Int = R.layout.fragment_notification

    override fun init() {
        progressLay           = rootView.findViewById<ViewGroup>(R.id.progressLay)

        swipeRefreshLayout    = rootView.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        list  = rootView.findViewById<RecyclerView>(R.id.list)
        manager = LinearLayoutManager(activity)
        list!!.layoutManager = manager
        list!!.setHasFixedSize(true)
        list!!.itemAnimator = ScaleInBottomAnimator()

        scroll = object : EndlessRecyclerViewScrollListener(manager) {
            override fun onScrolled(view: RecyclerView?, dx: Int, dy: Int) {
                var lastVisibleItemPosition = 0
                val totalItemCount = mLayoutManager.itemCount

                lastVisibleItemPosition = (mLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                swipeRefreshLayout.isEnabled = mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0


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
                    onLoadMore(currentPage, totalItemCount, view)
                    loading = true
                }
            }

            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                if (adapter != null && adapter!!.itemCount >= 20){
                    log.d("on more $page $totalItemsCount ")
                    MainActivity.startNotif = adapter!!.itemCount
                    MainActivity.endNotif = 20

                    log.d("FeedFragment => method onload more => startfrom: ${MainActivity.startNotif}")

                    connectActivity!!.goNext(Const.REFRESH_NOTIFICATION,"")
                }
            }


        }
        list!!.addOnScrollListener(scroll)
        swipeRefreshLayout.isEnabled  = false
        swipeRefreshLayout.setOnRefreshListener {
            MainActivity.startNotif = 0
            MainActivity.endNotif = 20

            log.d("FeedFragment => method onload more => startfrom: ${MainActivity.startNotif}")

            connectActivity!!.goNext(Const.REFRESH_NOTIFICATION,"")
        }

        emptyContainer = EmptyContainer.Builder()
                .setIcon(R.drawable.notification_light)
                .setText(R.string.error_empty_universal)
                .initLayoutForFragment(rootView)

                .build()

        emptyContainer!!.hide()

    }

    fun swapPushes(pushList: PushList) {
        scroll!!.resetState()
        emptyContainer!!.hide()

        progressLay.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = false
        list!!.visibility = View.VISIBLE
        if (adapter == null || (MainActivity.startNotif == 0 && MainActivity.endNotif == 20)){
            adapter = PushAdapter(activity,pushList.pushes)
            var slideAdapter: ScaleInAnimationAdapter? = ScaleInAnimationAdapter(adapter)


            slideAdapter!!.setFirstOnly(true)

            slideAdapter.setInterpolator(OvershootInterpolator())
            slideAdapter.setDuration(500)
            list!!.adapter = slideAdapter
        }else {
            adapter!!.swapItems(pushList)
        }
    }

    fun onFail(error:String){
        progressLay.visibility = View.GONE
        list!!.adapter = null
        list!!.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = false
        emptyContainer!!.show()
    }

}