package locidnet.com.marvarid.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.google.gson.Gson
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.mvp.Viewer
import kotlinx.android.synthetic.main.activity_comment.*
import org.json.JSONObject
import locidnet.com.marvarid.adapter.CommentAdapter
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.model.Comments
import locidnet.com.marvarid.mvp.Presenter
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.connectors.SignalListener
import locidnet.com.marvarid.di.DaggerMVPComponent
import locidnet.com.marvarid.di.modules.ErrorConnModule
import locidnet.com.marvarid.di.modules.MVPModule
import locidnet.com.marvarid.di.modules.PresenterModule
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.pattern.builder.ErrorConnection
import locidnet.com.marvarid.resources.customviews.loadmorerecyclerview.EndlessRecyclerViewScrollListener
import locidnet.com.marvarid.resources.utils.*
import java.util.*
import javax.inject.Inject


/**
 * Created by Michaelan on 7/10/2017.
 */
class CommentActivity :BaseActivity(),Viewer,AdapterClicker{


    var postId    = -1
    @Inject
    lateinit var presenter:Presenter

    @Inject
    lateinit var errorConn: ErrorConnection

    val user      = Base.get.prefs.getUser()
    var scroll: EndlessRecyclerViewScrollListener? = null
    var manager:LinearLayoutManager? = null
 //   var commentList:   ArrayList<Comment>? = null
    var commentAdapter:CommentAdapter?     = null
    var drawingStartLocation               = 0
    companion object {

        var start = 0
        var end   = 10

        val LOCATION = "location"
    }

    override fun getLayout(): Int = R.layout.activity_comment

    override fun initView() {
        DaggerMVPComponent
                .builder()
                .mVPModule(MVPModule(this, Model(),this))
                .presenterModule(PresenterModule())
                .errorConnModule(ErrorConnModule(this,true))

                .build()
                .inject(this)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = resources.getString(R.string.headerComment)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {

            onBackPressed()

        }
        Const.TAG = "CommentActivity"
        drawingStartLocation = intent.getIntExtra(LOCATION,0)

        contentRoot.viewTreeObserver.addOnPreDrawListener (object : ViewTreeObserver.OnPreDrawListener{
            override fun onPreDraw(): Boolean {
                contentRoot.viewTreeObserver.removeOnPreDrawListener(this)
                startIntroAnimation()
                return true
            }

        })

        postId = intent.getIntExtra("postId",-1)
        log.d("postId $postId")


        if (postId != -1){



            errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                override fun connected() {
                    /*send data for get comment list*/
                    val obj = JS.get()
                    obj.put("post_id",   postId)
                    obj.put("start",   0)
                    obj.put("end",    end)
                    obj.put("order",  "DESC")

                    presenter.requestAndResponse(obj, Http.CMDS.GET_COMMENT_LIST)
                }

                override fun disconnected() {
                }

            })
        }else{

            list.visibility           = View.GONE
            emptyContainer.visibility = View.VISIBLE
        }

        sendComment.setOnClickListener {
            if(commentText.text.isNotEmpty()){

                errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                    override fun connected() {
                        send()

                    }

                    override fun disconnected() {
                    }

                })
            }else{
                Toaster.info(resources.getString(R.string.error_empty_comment))

            }

        }

        commentText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
        commentText.setOnEditorActionListener(object : OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    if(commentText.text.isNotEmpty()){

                        errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                            override fun connected() {
                                send()

                            }

                            override fun disconnected() {
                            }

                        })

                    }else{
                        Toaster.info(resources.getString(R.string.error_empty_comment))

                    }
                    return true
                }

                return false
            }

        })
        manager = LinearLayoutManager(this)

        list.layoutManager = manager
        list.setHasFixedSize(true)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {


        menuInflater.inflate(R.menu.menu_refresh,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
            override fun connected() {
                /*send data for get comment list*/
                start = 0
                val obj =  JS.get()
                obj.put("post_id",   postId)
                obj.put("start",   start)
                obj.put("end",    end)
                obj.put("order",  "DESC")


                presenter.requestAndResponse(obj, Http.CMDS.GET_COMMENT_LIST)
            }

            override fun disconnected() {
            }

        })

        return true
    }
    override fun initProgress() {
        emptyContainer.visibility = View.GONE
    }

    override fun click(position: Int) {
    }

    override fun showProgress() {
    }

    override fun data(data: String) {
    }

    override fun hideProgress() {
        emptyContainer.visibility = View.GONE

    }

    override fun onSuccess(from: String, result: String) {

        log.d("cmd: $from -> result $result")

        if (from == Http.CMDS.WRITE_COMMENT){

            errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                override fun connected() {
                    commentText.setText("")


                    if (commentAdapter != null && commentAdapter!!.comments.size > 0){
                        val obj =  JS.get()
                        obj.put("post_id",postId)
                        start = commentAdapter!!.comments.size
                        obj.put("comm",    commentAdapter!!.comments.get(commentAdapter!!.comments.size - 1).commentId)
                        obj.put("order",  "ASC")

                        presenter.requestAndResponse(obj, Http.CMDS.GET_LAST_COMMENTS)
                    }else{
                        val obj =  JS.get()
                        obj.put("post_id",postId)

                        obj.put("start",  0)
                        obj.put("end",    end)
                        obj.put("order",  "DESC")

                        presenter.requestAndResponse(obj, Http.CMDS.GET_COMMENT_LIST)
                    }
                }

                override fun disconnected() {
                }

            })
        }else if (from == Http.CMDS.GET_COMMENT_LIST){
            val comment = Gson().fromJson<Comments>(result,Comments::class.java)
            list.visibility           = View.VISIBLE
            emptyContainer.visibility = View.GONE


            Collections.reverse(comment.comments)

            if (commentAdapter == null){
                log.d("COMMENT ADAPTER NULLGA TENG")


                commentAdapter = CommentAdapter(this,comment.comments,this)
                commentAdapter!!.setAdapterClicker(object : SignalListener{
                    override fun turnOn() {
                        if (commentAdapter != null && commentAdapter!!.comments.size >= 10){


                            errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                                override fun connected() {
                                    val obj =  JS.get()
                                    obj.put("post_id",postId)
                                    start = commentAdapter!!.comments.size
                                    obj.put("start",  start)
                                    obj.put("end",    end)
                                    obj.put("order",  "DESC")

                                    presenter.requestAndResponse(obj, Http.CMDS.GET_COMMENT_LIST)
                                }

                                override fun disconnected() {
                                }

                            })
                        }
                    }

                })
                list.adapter = commentAdapter

                try{
//                    list.smoothScrollBy(0,list.getChildAt(0).height * commentAdapter!!.comments.size)
                    manager!!.scrollToPosition(commentAdapter!!.comments.size - 1)
                }catch (e:Exception){}


            }else if(start == 0 && end == 10){



                commentAdapter = CommentAdapter(this,comment.comments,this)
                commentAdapter!!.setAdapterClicker(object : SignalListener{
                    override fun turnOn() {
                        if (commentAdapter != null && commentAdapter!!.comments.size >= 10){


                            errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                                override fun connected() {
                                    val obj =  JS.get()
                                    obj.put("post_id",postId)
                                    start = commentAdapter!!.comments.size
                                    obj.put("start",  start)
                                    obj.put("end",    end)
                                    obj.put("order",  "DESC")

                                    presenter.requestAndResponse(obj, Http.CMDS.GET_COMMENT_LIST)
                                }

                                override fun disconnected() {
                                }

                            })
                        }
                    }

                })
                list.adapter = commentAdapter

                try{
//                    list.smoothScrollBy(0,list.getChildAt(0).height * commentAdapter!!.comments.size)
                    manager!!.scrollToPosition(commentAdapter!!.comments.size - 1)
                }catch (e:Exception){}
            }else{
                log.d("COMMENT ADAPTER NULL EMAS")

                commentAdapter!!.animationsLocked = false
                commentAdapter!!.delayEnterAnimation = false
//                Collections.reverse(comment.comments)


                commentAdapter!!.swapToTop(comment.comments)

            }
        }else if(from == Http.CMDS.GET_LAST_COMMENTS){

            val comment = Gson().fromJson<Comments>(result,Comments::class.java)
            list.visibility           = View.VISIBLE
            emptyContainer.visibility = View.GONE
            emptyContainer.visibility = View.GONE


            Collections.reverse(comment.comments)

            log.d("COMMENT ADAPTER NULL EMAS")

            commentAdapter!!.animationsLocked = false
            commentAdapter!!.delayEnterAnimation = false
            Collections.reverse(comment.comments)

            commentAdapter!!.swapLast(comment.comments)

            try{
                list.smoothScrollBy(0,list.getChildAt(0).height * commentAdapter!!.comments.size)
                manager!!.scrollToPosition(commentAdapter!!.comments.size - 1)

            }catch (e:Exception){}
        }

    }

    override fun onFailure(from: String, message: String, erroCode: String) {

        log.d("fail from $from -> message $message")
        if(commentAdapter == null) {
            emptyContainer.visibility = View.VISIBLE
            list.visibility           = View.GONE
        }

    }

    fun send(){
        val obj =  JS.get()
        obj.put("post_id",postId)
        obj.put("comm",commentText.text.toString())


        commentText.text.clear()
        presenter.requestAndResponse(obj, Http.CMDS.WRITE_COMMENT)
    }

    override fun onBackPressed() {
        start = 0
        end = 10
        commentText.hideKeyboard()
        Functions.hideSoftKeyboard(this)
        commentAdapter = null
        contentRoot.animate()
                .translationY(Functions.getScreenHeight(this).toFloat())
                .setDuration(200)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }})
                .start()





    }


    fun startIntroAnimation(){


        contentRoot.scaleY  = 0.1f
        contentRoot.pivotY  = drawingStartLocation.toFloat()
        commentBoxLay.translationY = 100f

        contentRoot.animate()
                .scaleY(1f)
                .setDuration(200)
                .setInterpolator(AccelerateInterpolator())
                .setListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                            //TODO update comment list
                            commentBoxLay.animate().translationY(0f)
                                    .setInterpolator(DecelerateInterpolator())
                                    .setDuration(200)
                                    .start()
                    }
                })
                .start()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Const.SESSION_OUT || resultCode == Const.SESSION_OUT){
            setResult(Const.SESSION_OUT)
            finish()
        }
    }
}

