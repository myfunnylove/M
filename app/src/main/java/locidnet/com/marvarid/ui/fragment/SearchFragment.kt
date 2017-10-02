package locidnet.com.marvarid.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import io.reactivex.Observable
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.FollowAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseFragment
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.connectors.GoNext
import locidnet.com.marvarid.connectors.SignalListener
import locidnet.com.marvarid.model.PostList
import locidnet.com.marvarid.model.User
import locidnet.com.marvarid.model.Users
import locidnet.com.marvarid.pattern.builder.EmptyContainer
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Prefs
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.FollowActivity
import locidnet.com.marvarid.ui.activity.SearchActivity
import kotlin.properties.Delegates

/**
 * Created by Michaelan on 5/19/2017.
 */
class SearchFragment : BaseFragment(), AdapterClicker{

    var search:Toolbar       by Delegates.notNull<Toolbar>()
//    var searchResult:TextView by Delegates.notNull<TextView>()
    var list:RecyclerView     by Delegates.notNull<RecyclerView>()

    var progress           by Delegates.notNull<ProgressBar>()
    val pattern = "^[\\p{L}0-9]*$"

    var usersList:ArrayList<Users>? = null
    var adapter:FollowAdapter?      = null
    val user:User                   = Base.get.prefs.getUser()


    var changePosition              = -1


    /*STATIC PROPERTIES*/
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
        progress       = rootView.findViewById<ProgressBar>(R.id.progress)
        search.setOnClickListener {
            startActivity(Intent(activity,SearchActivity::class.java))
            activity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
        }

        list.layoutManager = LinearLayoutManager(activity)
        list.setHasFixedSize(true)



    }



    override fun click(position: Int) {

    }

    override fun data(data: String) {
    }

    fun hideProgress() {
        progress.visibility = View.GONE

    }

    fun swapPosts(postList: PostList) {
        progress.visibility = View.GONE


    }

    fun failedGetList() {
        progress.visibility = View.GONE
    }


}