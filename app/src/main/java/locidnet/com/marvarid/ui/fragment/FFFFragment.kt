package locidnet.com.marvarid.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.FollowAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseFragment
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.connectors.GoNext
import locidnet.com.marvarid.model.User
import locidnet.com.marvarid.model.Users
import locidnet.com.marvarid.pattern.builder.EmptyContainer
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Functions
import locidnet.com.marvarid.resources.utils.Prefs
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.FollowActivity
import kotlin.properties.Delegates

/**
 *
 * Created by Michaelan on 7/5/2017.
 *
 */


class FFFFragment :BaseFragment() ,AdapterClicker{



    var search: EditText       by Delegates.notNull<EditText>()
//  var searchResult: TextView by Delegates.notNull<TextView>()
    var list: RecyclerView     by Delegates.notNull<RecyclerView>()

    var searchLay              by Delegates.notNull<LinearLayout>()


    var headerText = ""
    //var progressLay    by Delegates.notNull<ViewGroup>()
    val pattern = "^[\\p{L}0-9]*$"

    var usersList:ArrayList<Users>?  = null
    var adapter: FollowAdapter?      = null
    val user: User = Base.get.prefs.getUser()
    var changePosition               = -1
    lateinit var emptyContainer:EmptyContainer

    companion object {
        var TAG:String  = "FFFFragment"

        fun newInstance(bundle: Bundle): FFFFragment {
            followersCount = -1

            val newsFragment = FFFFragment()
            val args =bundle

            newsFragment.arguments = args
            return newsFragment

        }
        var followersCount = -1


        var OZGARGAN_USERNI_IDSI  = -1
        var QAYSI_HOLATGA_OZGARDI = ProfileFragment.FOLLOW
    }

    var connectActivity: GoNext?     = null
    fun connect(connActivity: GoNext){
        connectActivity = connActivity

    }
    override fun getFragmentView(): Int {
        return R.layout.fragment_search
    }

    override fun init() {
        Const.TAG = "FFFFragment"
        OZGARGAN_USERNI_IDSI = -1
        QAYSI_HOLATGA_OZGARDI = ProfileFragment.FOLLOW

        list           = rootView.findViewById(R.id.list)           as RecyclerView
        search         = rootView.findViewById(R.id.search)         as EditText
        searchLay      = rootView.findViewById(R.id.searchLay)      as LinearLayout

        emptyContainer = EmptyContainer.Builder()
                .setIcon(R.drawable.account_light)
                .setText(R.string.error_empty_universal)
                .initLayoutForFragment(rootView)

                .build()
        searchLay.visibility = View.GONE
        list.layoutManager = LinearLayoutManager(activity)
        list.setHasFixedSize(true)
        headerText = arguments.getString("header","")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
        val drawableCompat = VectorDrawableCompat.create(activity.resources,R.drawable.search,search.context.theme)
        search.setCompoundDrawablesWithIntrinsicBounds(drawableCompat,null,null,null)
//        }else{

//        }

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(s!!.toString().length >= 3){

                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })


    }

    fun swapList(users:ArrayList<Users>){

        log.d("${users}")
        if (users.size > 0){
            emptyContainer.hide()
            usersList                 = users
            adapter                   = FollowAdapter(Base.get,users,this)
            list.adapter              = adapter


        }else{
            emptyContainer.show()



        }

    }
    fun failedGetList(error:String = ""){

//        progressLay.visibility = View.GONE
        log.e("FeedFragment => method => failedGetList errorCode => $error")
        if (adapter != null && adapter!!.users.size != 0){
            log.e("list bor lekin xatolik shundo ozini qoldiramiz")


            emptyContainer.hide()

            list.visibility = View.VISIBLE


        }else{
            log.e("list null yoki list bom bosh")

            emptyContainer.show()

            list.visibility = View.GONE
        }

    }
    override fun click(position: Int) {
        val user = adapter!!.users.get(position)
        val bundle = Bundle()
        val js = JSONObject()
        log.d("fffFragment $user")
        bundle.putString("username",user.username)
        bundle.putString("photo",   user.photo150)
        bundle.putString("user_id",  user.userId)

//        bundle.putString("blockMe",user.blockMe)
//        bundle.putString("blockIt",user.blockIt)
        js.put("username",user.username)
        js.put("photo",   user.photo150)
        js.put("user_id",  user.userId)
        if (user.userId != this.user.userId){



//            log.d("user clicked $user")
            if(user.close == 1 && user.follow == 0 && user.request == 0){

                bundle.putString(ProfileFragment.F_TYPE,ProfileFragment.CLOSE)
                js.put(ProfileFragment.F_TYPE,ProfileFragment.CLOSE)

            }else if(user.close == 1 && user.follow == 0 && user.request == 1){

                bundle.putString(ProfileFragment.F_TYPE,ProfileFragment.REQUEST)
                js.put(ProfileFragment.F_TYPE,ProfileFragment.REQUEST)

            }else if (user.close == 1 && user.follow == 1 && user.request == 0){

                bundle.putString(ProfileFragment.F_TYPE,ProfileFragment.UN_FOLLOW)
                js.put(ProfileFragment.F_TYPE,ProfileFragment.UN_FOLLOW)

            }else if (user.close == 0 && user.follow == 0 && user.request == 1){

                bundle.putString(ProfileFragment.F_TYPE,ProfileFragment.FOLLOW)
                js.put(ProfileFragment.F_TYPE,ProfileFragment.FOLLOW)


            }else if (user.close == 0 && user.follow == 1 && user.request == 0){

                bundle.putString(ProfileFragment.F_TYPE,ProfileFragment.UN_FOLLOW)
                js.put(ProfileFragment.F_TYPE,ProfileFragment.UN_FOLLOW)

            }else{
                bundle.putString(ProfileFragment.F_TYPE,ProfileFragment.FOLLOW)
                js.put(ProfileFragment.F_TYPE,ProfileFragment.FOLLOW)
            }

            val go = Intent(activity, FollowActivity::class.java)

//            if (user.blockMe == "0")
                go.putExtra(FollowActivity.TYPE, FollowActivity.PROFIL_T)
//            else
//                go.putExtra(FollowActivity.TYPE, FollowActivity.BLOCKED_ME)

//            go.putExtra("close",user.close)
//            go.putExtra("blockMe",user.blockMe)
//            go.putExtra("blockIt",user.blockIt)
            go.putExtras(bundle)
//            js.put("close",user.close)
            startActivityForResult(go,Const.TO_FAIL)


//            connectActivity!!.goNext(Const.PROFIL_PAGE_OTHER,js.toString())

        }else{
            val go = Intent(activity, FollowActivity::class.java)
            bundle.putString(ProfileFragment.F_TYPE,ProfileFragment.SETTINGS)

            go.putExtra(FollowActivity.TYPE, FollowActivity.PROFIL_T)
            go.putExtra("close",user.close)

            go.putExtras(bundle)
            js.put("close",user.close)

            startActivityForResult(go,Const.TO_FAIL)
        }

    }

    override fun data(data: String) {

    }

    override fun onResume() {
        super.onResume()
        log.d("Onresume $OZGARGAN_USERNI_IDSI $QAYSI_HOLATGA_OZGARDI")

         if (adapter != null && headerText == Base.get.getString(R.string.following)) followersCount = adapter!!.users.filter { user -> user.follow == 1 }.size
        if (OZGARGAN_USERNI_IDSI != -1 && adapter != null){
            when(QAYSI_HOLATGA_OZGARDI){
                ProfileFragment.REQUEST ->{
                    adapter!!.users.forEach { user -> if (user.userId == OZGARGAN_USERNI_IDSI.toString()){
                        log.d("Onresume 1 $OZGARGAN_USERNI_IDSI $QAYSI_HOLATGA_OZGARDI")

                        user.follow = 0
                        user.request = 1
                    }
                        log.d("Onresume 4 $OZGARGAN_USERNI_IDSI $QAYSI_HOLATGA_OZGARDI")

                    }
                    adapter!!.notifyDataSetChanged()
                }

                ProfileFragment.FOLLOW ->{
                    adapter!!.users.forEach { user -> if (user.userId == OZGARGAN_USERNI_IDSI.toString()){
                        log.d("Onresume 2 $OZGARGAN_USERNI_IDSI $QAYSI_HOLATGA_OZGARDI")

                        user.follow = 0
                        user.request = 0
                    }
                        log.d("Onresume 5 $OZGARGAN_USERNI_IDSI $QAYSI_HOLATGA_OZGARDI ${user.userId}")

                    }
                    adapter!!.notifyDataSetChanged()
                }

                ProfileFragment.UN_FOLLOW ->{
                    adapter!!.users.forEach { user -> if (user.userId == OZGARGAN_USERNI_IDSI.toString()){
                        log.d("Onresume 3 $OZGARGAN_USERNI_IDSI $QAYSI_HOLATGA_OZGARDI")

                        user.follow = 1
                        user.request =0
                    }
                        log.d("Onresume 6 $OZGARGAN_USERNI_IDSI $QAYSI_HOLATGA_OZGARDI")

                    }
                    adapter!!.notifyDataSetChanged()
                }
            }
        }
    }
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        log.d("onHiddenChanged ")
        if(hidden){
            search.hideKeyboard()

        }else{
            search.showKeyboard()

        }
    }

    fun View.showKeyboard() {
        this.requestFocus()
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    fun View.hideKeyboard() {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }
}