package locidnet.com.marvarid.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.google.gson.Gson
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.FollowAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.di.DaggerMVPComponent
import locidnet.com.marvarid.di.modules.ErrorConnModule
import locidnet.com.marvarid.di.modules.MVPModule
import locidnet.com.marvarid.di.modules.PresenterModule
import locidnet.com.marvarid.model.Follow
import locidnet.com.marvarid.model.User
import locidnet.com.marvarid.model.Users
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.mvp.Presenter
import locidnet.com.marvarid.mvp.Viewer
import locidnet.com.marvarid.pattern.builder.EmptyContainer
import locidnet.com.marvarid.pattern.builder.ErrorConnection
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.JS
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.ui.fragment.ProfileFragment
import javax.inject.Inject
import kotlin.properties.Delegates

/**
 * Created by myfunnylove on 01.10.17.
 */
class SearchActivity :BaseActivity() ,Viewer, AdapterClicker{



    @Inject
    lateinit var presenter: Presenter

    @Inject
    lateinit var errorConn: ErrorConnection


    companion object  {
        var choosedUserId = ""

    }

    var chooseUserFstatus = ""
    //    var searchResult:TextView by Delegates.notNull<TextView>()
    var list: RecyclerView     by Delegates.notNull<RecyclerView>()
    var toolbar:Toolbar? = null
    val pattern = "^[\\p{L}0-9]*$"

    var usersList:ArrayList<Users>? = null
    var adapter:FollowAdapter?      = null
    var searchView:SearchView?      = null
    var progress:ProgressBar?       = null
    val user: User = Base.get.prefs.getUser()
    lateinit var emptyContainer: EmptyContainer

    override fun getLayout(): Int = R.layout.activity_search


    override fun initView() {

        list           = findViewById<RecyclerView>(R.id.list)
        toolbar        = findViewById<Toolbar>(R.id.toolbar)
        searchView     = findViewById<SearchView>(R.id.searchView)
        progress     = findViewById<ProgressBar>(R.id.progress)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        initSearchView()
        DaggerMVPComponent
                .builder()
                .mVPModule(MVPModule(this, Model(),this))
                .presenterModule(PresenterModule())
                .errorConnModule(ErrorConnModule(this,true))
                .build()
                .inject(this)


        list.layoutManager = LinearLayoutManager(this)
        list.setHasFixedSize(true)



        emptyContainer = EmptyContainer.Builder()
                .setIcon(R.drawable.search_light)
                .setText(R.string.search)
                .initLayoutForActivity(this)
                .build()

    }

    private fun initSearchView() {
        val searchEditText = searchView!!.findViewById<EditText>(android.support.v7.appcompat.R.id.search_src_text)
            searchEditText.setTextColor(getResources().getColor(R.color.normalTextColor));
            searchEditText.setHintTextColor(getResources().getColor(R.color.normalTextColor));
        searchView!!.isIconified = false
        searchView!!.setOnCloseListener {
            this@SearchActivity.finish()
            false
        }
        searchView!!.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(s: String?): Boolean {


                return false;
            }

            override fun onQueryTextChange(s: String?): Boolean {
                if(s!!.toString().length >= 3){
                    val res  =s.toString().isOkString()
                    if (res == ""){
                        val letter = s.toString().replace(pattern,"")
                        try{
                            errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                                override fun connected() {
                                    val reqObj = JS.get()
                                    reqObj.put("start", MainActivity.startSearch)
                                    reqObj.put("end", MainActivity.endSearch)
                                    reqObj.put("user",    letter)
                                    presenter.requestAndResponse(reqObj, Http.CMDS.SEARCH_USER)




                                }

                                override fun disconnected() {
                                    hideSoftKeyboard()
                                }

                            })
                        }catch (e:Throwable){}
                    }else{
                        //TODO ERROR STRING

                    }
                }else{
                    if(adapter != null){
                        adapter!!.users.clear()
                        adapter!!.notifyDataSetChanged()
                    }
                }
                return true
            }

        })
    }


    fun swapList(users:ArrayList<Users>){

        if (users.size > 0){
            emptyContainer.hide()

            usersList                 = users
            adapter                   = FollowAdapter(Base.get,users,this,1)
            list.visibility           = View.VISIBLE
            list.adapter              = adapter


        }else{
            emptyContainer.show()



        }

    }

    fun failedGetList(error:String = ""){

//        progressLay.visibility = View.GONE
        log.e("SearchFragment => method => failedGetList errorCode => $error")
        if (adapter != null && adapter!!.users.size != 0){
            log.e("list bor lekin xatolik shundo ozini qoldiramiz")


            emptyContainer.hide()
            adapter!!.users.clear()
            adapter!!.notifyDataSetChanged()
            list.visibility = View.VISIBLE


        }else{
            log.e("list null yoki list bom bosh")

            emptyContainer.show()

            list.visibility = View.GONE
        }

    }
    override fun data(data: String) {
    }
    override fun click(position: Int) {

        val user = adapter!!.users.get(position)
        log.d("result from search user -> ${user}")

        if (user.userId != this.user.userId){
            val type = user.setStatusUserFactory()

            log.d("user type $type")

            choosedUserId = user.userId
            chooseUserFstatus = type

            val go = Intent(this,FollowActivity::class.java)
            val bundle = Bundle()
            bundle.putString("username",user.username)
            bundle.putString("photo",   if (user.photo150.isNullOrEmpty()) "" else user.photo150)
            bundle.putString("user_id",  user.userId)
//            bundle.putString("blockMe",user.blockMe)
//            bundle.putString("blockIt",user.blockIt)
            bundle.putString(ProfileFragment.F_TYPE,type)
            log.d("result from search user -> ${bundle}")

//           if (user.blockMe == "0")
            go.putExtra(FollowActivity.TYPE,FollowActivity.PROFIL_T)
//            else
//               go.putExtra(FollowActivity.TYPE,FollowActivity.BLOCKED_ME)

            go.putExtras(bundle)
            startActivityForResult(go,Const.FROM_SEARCH_TO_PROFIL)
        }else{
            val type = user.setStatusUserFactory()

            log.d("user type $type")

            choosedUserId = user.userId
            chooseUserFstatus = type

            val go = Intent(this,FollowActivity::class.java)
            val bundle = Bundle()
            bundle.putString("username",user.username)
            bundle.putString("photo",   if (user.photo150.isNullOrEmpty()) "" else user.photo150)
            bundle.putString("user_id",  user.userId)
//            bundle.putString("blockMe",user.blockMe)
//            bundle.putString("blockIt",user.blockIt)
            bundle.putString(ProfileFragment.F_TYPE,ProfileFragment.SETTINGS)
            log.d("result from search user -> ${bundle}")

//           if (user.blockMe == "0")
            go.putExtra(FollowActivity.TYPE,FollowActivity.PROFIL_T)
//            else
//               go.putExtra(FollowActivity.TYPE,FollowActivity.BLOCKED_ME)

            go.putExtras(bundle)
            startActivityForResult(go,Const.FROM_SEARCH_TO_PROFIL)

        }

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        log.d("${requestCode} $resultCode")
        if (requestCode == Const.FROM_SEARCH_TO_PROFIL){


            try{
                adapter!!.users.forEach { user ->

                    if (user.userId == choosedUserId) run {

                        user.setStatusFactory(chooseUserFstatus)

                    }


                }
            }catch (e:Exception){}

            adapter!!.notifyDataSetChanged()
            chooseUserFstatus = ""
            choosedUserId = ""
        }

    }


    override fun onResume() {
        super.onResume()
        log.d("onresume")

    }



    override fun onStart() {
        super.onStart()
        log.d("onresume")

    }
    fun View.showKeyboard() {
        this.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    fun View.hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }


    fun Users.setStatusFactory(type:String):Users{

        when(type){
            ProfileFragment.FOLLOW ->{
                this.follow = 0
                this.request = 0
            }

            ProfileFragment.REQUEST ->{
                this.follow = 0
                this.request = 1

            }

            ProfileFragment.UN_FOLLOW ->{
                this.follow = 1
                this.request = 0

            }
        }

        return this
    }


    fun Users.setStatusUserFactory():String{
        log.d("search select user $this")
        val user = this
        var type = ""



        if(user.close == 1 && user.follow == 0 && user.request == 0){
            type =  ProfileFragment.CLOSE
        }else if(user.close == 1 && user.follow == 0 && user.request == 1){
            type =  ProfileFragment.REQUEST

        }else if (user.close == 1 && user.follow == 1 && user.request == 0){
            type =  ProfileFragment.UN_FOLLOW

        }else if (user.close == 0 && user.follow == 0 && user.request == 1){
            type =  ProfileFragment.FOLLOW

        }else if (user.close == 0 && user.follow == 1 && user.request == 0){
            type =  ProfileFragment.UN_FOLLOW

        }else{
            type =  ProfileFragment.FOLLOW

        }

//        if (user.close == 1 && user.follow == 0 && user.request == 0) return ProfileFragment.CLOSE
//
//        if (user.follow == 0 && user.request == 0){
//
//            log.d("${user.user_id} -> ${user.username}ga follow qilinmagan")
//
//        }else if (user.follow == 1 && user.request == 0){
//
//            log.d("${user.user_id} -> ${user.username}ga follow qilingan")
//            type =  ProfileFragment.UN_FOLLOW
//        }else if (user.follow == 0 && user.request == 1){
//
//            log.d("${user.user_id} -> ${user.username}ga zapros tashalgan")
//            type =  ProfileFragment.REQUEST
//
//        }else {
//            log.d("${user.user_id} -> ${user.username}da xato holat ")
//            type =  ProfileFragment.FOLLOW
//
//
//        }
        return type
    }
    fun String.isOkString():String{

        var result = ""


        for (i in 0..this.length - 1) {
            if (Character.UnicodeBlock.of(this.get(i)) == Character.UnicodeBlock.CYRILLIC) {
                result = Base.get.resources.getString(R.string.error_cyrillic_letters)
            }
        }



        return result
    }
    fun hideSoftKeyboard() {
        val inputMethodManager = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager.isActive) {
            if (this.currentFocus != null) {
                inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
            }
        }
    }

    override fun initProgress() {
    }

    override fun showProgress() {
        progress!!.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progress!!.visibility = View.GONE

    }

    override fun onSuccess(from: String, result: String) {

        when(from){
            Http.CMDS.SEARCH_USER -> {

                val follow = Gson().fromJson<Follow>(result, Follow::class.java)
                if(follow.users.size > 0){
                    swapList(follow.users)

                }else {
                    failedGetList("empty")
                }
            }
        }
    }

    override fun onFailure(from: String, message: String, erroCode: String) {

        Handler().postDelayed({failedGetList(message)},1500)
    }
}