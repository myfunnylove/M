package locidnet.com.marvarid.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.*
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

    var search:Toolbar?    = null
    var list:RecyclerView? = null

    var recPosts:ArrayList<RecPost>? = null
    var adapter:FollowAdapter?       = null
    val user:User                    = Base.get.prefs.getUser()




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
        search!!.setOnClickListener {
            startActivity(Intent(activity,SearchActivity::class.java))
            activity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
        }

        list!!.layoutManager = GridLayoutManager(activity,3)
        list!!.setHasFixedSize(true)



    }



    override fun click(position: Int) {
        val intent = Intent(activity,FollowActivity::class.java)
        intent.putExtra("user_id",recPosts!!.get(position).userId)
        intent.putExtra("username",recPosts!!.get(position).username)
        intent.putExtra(FollowActivity.TYPE,FollowActivity.PROFIL_T)
        startActivity(intent)
    }

    override fun data(data: String) {

    }

    fun hideProgress() {

    }

    fun swapPosts(postList: ArrayList<RecPost>) {
        recPosts = postList
        val adapter = RecommendedAdapter(this,Base.get.applicationContext,recPosts!!)

        list!!.adapter = adapter

    }

    fun failedGetList() {
    }


}