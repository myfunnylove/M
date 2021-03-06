package locidnet.com.marvarid.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.model.Push
import locidnet.com.marvarid.model.PushList
import locidnet.com.marvarid.model.ResponseData
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.resources.adapterAnim.AnimateViewHolder
import locidnet.com.marvarid.resources.utils.*
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.ui.activity.FollowActivity
import locidnet.com.marvarid.ui.activity.MainActivity
import locidnet.com.marvarid.ui.activity.UserPostActivity
import locidnet.com.marvarid.ui.fragment.ProfileFragment
import org.json.JSONObject
import org.ocpsoft.prettytime.PrettyTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

/*
 *
 *  Created by myfunnylove on 17.09.17.
 *
 */
class PushAdapter(private val ctx: Context, private val list: ArrayList<Push>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    val model = Model()
    val user = Prefs.Builder().getUser()
//    var wrapParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
    private val prettyTime = PrettyTime()
    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int = list[position].type


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {

                Const.Push.LIKE ,
                Const.Push.COMMENT ,
                Const.Push.REPLIED ,
                Const.Push.MENTIONED -> {
                    val comment = Comment(inflater.inflate(R.layout.res_item_push_like, parent, false))
                    comment.avatar.setOnClickListener{

                        val push = list[comment.adapterPosition]

                        if (push.user.userId != user.userId){
                            val type = ProfileFragment.FOLLOW



                            log.d("user type $type")


                            val go = Intent(ctx, FollowActivity::class.java)
                            val bundle = Bundle()
                            bundle.putString("username",push.user.userName)
                            bundle.putString("photo",   if (push.user.userPhoto.isNullOrEmpty()) "" else push.user.userPhoto)
                            bundle.putString("user_id",  push.user.userId)
                            bundle.putString(ProfileFragment.F_TYPE,type)
                            log.d("result from search user -> ${bundle}")

                            go.putExtra(FollowActivity.TYPE,FollowActivity.PROFIL_T)

                            go.putExtras(bundle)
                            ctx.startActivity(go)
                        }
                    }

                     comment
                }


                Const.Push.FOLLOW ,
                Const.Push.REQUESTED ->{
                    val requested = Requested(inflater.inflate(R.layout.res_item_push_requested, parent, false))
                    requested.avatar.setOnClickListener{
                        val push = list[requested.adapterPosition]


                        if (push.user.userId != user.userId){
                            val type = ProfileFragment.FOLLOW



                            log.d("user type $type")


                            val go = Intent(ctx, FollowActivity::class.java)
                            val bundle = Bundle()
                            bundle.putString("username",push.user.userName)
                            bundle.putString("photo",   if (push.user.userPhoto.isNullOrEmpty()) "" else push.user.userPhoto)
                            bundle.putString("user_id",  push.user.userId)
                            bundle.putString(ProfileFragment.F_TYPE,type)
                            log.d("result from search user -> ${bundle}")

                            go.putExtra(FollowActivity.TYPE,FollowActivity.PROFIL_T)

                            go.putExtras(bundle)
                            ctx.startActivity(go)
                        }
                    }

                    requested
                }

                else -> Other(inflater.inflate(R.layout.res_item_push_requested, parent, false))
            }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, @SuppressLint("RecyclerView") position: Int) {

        val push = list[position]

        val date2 = formatter.parse(push.time) as Date
        log.d("onbind ${getItemViewType(position)}")
        when (getItemViewType(position)) {

            Const.Push.LIKE ,

            Const.Push.COMMENT ,

            Const.Push.REPLIED ,

            Const.Push.MENTIONED -> {

                val comment = holder as Comment

                comment.avatar.post{

                    comment.avatar.controller = Fresco.newDraweeControllerBuilder()
                            .setImageRequest(
                                    ImageRequestBuilder.newBuilderWithSource(Uri.parse(Functions.checkImageUrl(push.user.userPhoto)))
                                            .setResizeOptions(ResizeOptions(200,200))

                                            .build())
                            .setOldController(comment.avatar.controller)
                            .setAutoPlayAnimations(true)

                            .build()

                }


                comment.username.text = push.user.userName
                comment.body.text = when(getItemViewType(position)){
                    Const.Push.COMMENT -> Base.get.resources.getString(R.string.pushCommentBody)
                    Const.Push.LIKE -> Base.get.resources.getString(R.string.pushLikeBody)
                    Const.Push.REPLIED -> Base.get.resources.getString(R.string.pushRepliedBody)
                    Const.Push.MENTIONED -> Base.get.resources.getString(R.string.pushMentionedBody)

                    else ->ctx.resources.getString(R.string.pushCommentBody)
                }
                comment.time.text = "${prettyTime.format(date2)}"
                comment.mypost.post {
                    comment.mypost.controller = Fresco.newDraweeControllerBuilder()

                            .setImageRequest(
                                    ImageRequestBuilder.newBuilderWithSource(Uri.parse(Functions.checkImageUrl(push.action.actionPhoto)!!.replace(Const.IMAGE.TITLE, Prefs.Builder().imageRes())))
//                                            .setResizeOptions(ResizeOptions(width,height))
                                            .setCacheChoice(ImageRequest.CacheChoice.DEFAULT)
                                            .build())
                            .setOldController(comment.mypost.controller)
                            .setAutoPlayAnimations(true)
                            .build()

                }



                comment.mypost.setOnClickListener {
                    val data = Intent(ctx,UserPostActivity::class.java)
                    data.putExtra("postId",push.action.actionID.toInt())
                    ctx.startActivity(data)
                }
            }

            Const.Push.REQUESTED -> {

                val requested = holder as Requested

                requested.avatar.post{

                    requested.avatar.controller = Fresco.newDraweeControllerBuilder()
                            .setImageRequest(
                                    ImageRequestBuilder.newBuilderWithSource(Uri.parse(Functions.checkImageUrl(push.user.userPhoto)))
                                            .setResizeOptions(ResizeOptions(200,200))

                                            .build())
                            .setOldController(requested.avatar.controller)
                            .setAutoPlayAnimations(true)

                            .build()

                }



                requested.username.text = push.user.userName

                requested.body.text = ctx.resources.getString(R.string.pushRequestBody)

                requested.time.text ="${prettyTime.format(date2)}"

                requested.accept.text = Functions.getString(R.string.allow)
                requested.dismiss.text = Functions.getString(R.string.ignore)
                val js =  JS.get()
                js.put("user", push.user.userId)
                requested.accept.setOnClickListener {
                    js.put("type","1")

                    model.responseCall(Http.getRequestData(js, Http.CMDS.ALLOW_DISMISS))
                            .enqueue(object : Callback<ResponseData>{
                                override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {
                                    val res = response!!
                                    log.d("onresponse from push request $res")
                                    if (res.body()!!.res == "0") {
                                        list.removeAt(holder.adapterPosition)
                                        notifyItemRemoved(holder.adapterPosition)
                                        MainActivity.MY_POSTS_STATUS = MainActivity.ONLY_USER_INFO

                                    }

                                }

                                override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
                                    log.d("onfail $t")
                                }

                            })
                }
                requested.dismiss.setOnClickListener{
                js.put("type","0")

                    model.responseCall(Http.getRequestData(js, Http.CMDS.ALLOW_DISMISS))
                            .enqueue(object : Callback<ResponseData>{
                                override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {
                                    val res = response!!
                                    log.d("onresponse from push request ${res.body()!!.res}")
                                    if (res.body()!!.res == "0") {
                                        list.removeAt(holder.adapterPosition)
                                        notifyItemRemoved(holder.adapterPosition)

                                    }
                                }

                                override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
                                    log.d("onfail $t")
                                }

                            })

                }

            }

            Const.Push.FOLLOW-> {

                val follow = holder as Requested

                follow.avatar.post{

                    follow.avatar.controller = Fresco.newDraweeControllerBuilder()
                            .setImageRequest(
                                    ImageRequestBuilder.newBuilderWithSource(Uri.parse(Functions.checkImageUrl(push.user.userPhoto)))
                                            .setResizeOptions(ResizeOptions(200,200))

                                            .build())
                            .setOldController(follow.avatar.controller)
                            .setAutoPlayAnimations(true)

                            .build()

                }

//                follow.accept.layoutParams = wrapParams


                follow.username.text = push.user.userName

                follow.body.text = ctx.resources.getString(R.string.pushFollowBody)
                follow.time.text = "${prettyTime.format(date2)}"

                follow.dismiss.visibility = View.GONE
                val params =
                log.d("push:  $push")
                try{
                    when {
                        push.user.actions.requestIt == "1" -> {
                            follow.accept.tag = ProfileFragment.REQUEST
                            follow.accept.text = Functions.getString(R.string.request)
                            follow.accept.setTextColor(ctx.resources.getColor(R.color.white))

                            follow.accept.setBackgroundDrawable(ctx.resources.getDrawable(R.drawable.button_accent_select))
                        }
                        push.user.actions.followIt == "1" -> {
                            follow.accept.tag = ProfileFragment.UN_FOLLOW

                            follow.accept.text = Functions.getString(R.string.unfollow)
                            follow.accept.setTextColor(ctx.resources.getColor(R.color.headerTextColor))

                            follow.accept.setBackgroundDrawable(ctx.resources.getDrawable(R.drawable.button_accent_deselect))
                        }
                        else -> {
                            follow.accept.tag = ProfileFragment.FOLLOW
                            follow.accept.setTextColor(ctx.resources.getColor(R.color.white))

                            follow.accept.setBackgroundDrawable(ctx.resources.getDrawable(R.drawable.button_accent_select))
                            follow.accept.text = Functions.getString(R.string.follow)
                        }
                    }
                }catch (e:Exception){
                    follow.accept.tag = ProfileFragment.FOLLOW

                    follow.accept.text = Functions.getString(R.string.follow)
                }



                follow.accept.setOnClickListener {
                    val reqObj =  JS.get()


                    reqObj.put("user",   push.user.userId)
                    if (follow.accept.tag == ProfileFragment.FOLLOW){
                        model.responseCall(Http.getRequestData(reqObj, Http.CMDS.FOLLOW))
                                .enqueue(object : Callback<ResponseData>{
                                    override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
                                        log.d("follow on fail $t")
                                    }

                                    override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {
                                        if (response!!.isSuccessful){

                                            try{

                                                val req = JSONObject(Http.getResponseData(response.body()!!.prms))
                                                if (req.optString("request") == "1"){

                                                    list[position].user.actions.followIt  = "0"
                                                    list[position].user.actions.requestIt = "1"
                                                    notifyItemChanged(position)

                                                }else if (req.optString("request") == "0"){
                                                    list[position].user.actions.followIt  = "1"
                                                    list[position].user.actions.requestIt = "0"
                                                    notifyItemChanged(position)
                                                    MainActivity.MY_POSTS_STATUS = MainActivity.ONLY_USER_INFO
                                                }

                                            }catch (e : Exception){

                                            }



                                        }else{
                                            Toast.makeText(Base.get, Base.get.resources.getString(R.string.internet_conn_error), Toast.LENGTH_SHORT).show()

                                        }


                                    }

                                })
                    }else if (follow.accept.tag == ProfileFragment.REQUEST || follow.accept.tag == ProfileFragment.UN_FOLLOW){
                        model.responseCall(Http.getRequestData(reqObj, Http.CMDS.UN_FOLLOW))
                                .enqueue(object : Callback<ResponseData>{
                                    override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
                                        log.d("follow on fail $t")
                                    }

                                    override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {
                                        if (response!!.isSuccessful){
                                            list[position].user.actions.followIt  = "0"
                                            list[position].user.actions.requestIt = "0"
                                            notifyItemChanged(position)
                                            MainActivity.MY_POSTS_STATUS = MainActivity.ONLY_USER_INFO

                                        }else{
                                            Toast.makeText(Base.get, Base.get.resources.getString(R.string.internet_conn_error), Toast.LENGTH_SHORT).show()

                                        }

                                    }

                                })
                    }

                }
            }

        }
    }




    class Comment(view: View) : RecyclerView.ViewHolder(view), AnimateViewHolder {
        val container = view.findViewById<ViewGroup>(R.id.container)
        val username = view.findViewById<TextView>(R.id.username)
        val avatar = view.findViewById<SimpleDraweeView>(R.id.avatar)
        val body = view.findViewById<TextView>(R.id.body)
        val time = view.findViewById<TextView>(R.id.time)
        val mypost = view.findViewById<SimpleDraweeView>(R.id.actionPhoto)
        init {
            avatar.hierarchy = Functions.getAvatarHierarchy()
            mypost.hierarchy = Functions.getBackgroundOptions()
        }
        override fun preAnimateAddImpl(holder: RecyclerView.ViewHolder?) {
            ViewCompat.setTranslationY(itemView, -itemView.getHeight() * 0.3f);
            ViewCompat.setAlpha(itemView, 0f);
        }

        override fun preAnimateRemoveImpl(holder: RecyclerView.ViewHolder?) {
        }

        override fun animateAddImpl(holder: RecyclerView.ViewHolder?, listener: ViewPropertyAnimatorListener?) {
            ViewCompat.animate(itemView)
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(300)
                    .setListener(listener)
                    .start();
        }

        override fun animateRemoveImpl(holder: RecyclerView.ViewHolder?, listener: ViewPropertyAnimatorListener?) {
            ViewCompat.animate(itemView)
                    .translationY(-itemView.getHeight() * 0.3f)
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(listener)
                    .start();
        }
    }

    class Requested(view: View) : RecyclerView.ViewHolder(view), AnimateViewHolder {
        val container = view.findViewById<ViewGroup>(R.id.container)
        val avatar = view.findViewById<SimpleDraweeView>(R.id.avatar)
        val username = view.findViewById<TextView>(R.id.username)
        val body = view.findViewById<TextView>(R.id.body)
        val accept = view.findViewById<Button>(R.id.accept)
        val dismiss = view.findViewById<Button>(R.id.dismiss)
        val time = view.findViewById<TextView>(R.id.time)
        init {
            avatar.hierarchy = Functions.getAvatarHierarchy()

        }
        override fun preAnimateAddImpl(holder: RecyclerView.ViewHolder?) {
            ViewCompat.setTranslationY(itemView, -itemView.getHeight() * 0.3f);
            ViewCompat.setAlpha(itemView, 0f);
        }

        override fun preAnimateRemoveImpl(holder: RecyclerView.ViewHolder?) {
        }

        override fun animateAddImpl(holder: RecyclerView.ViewHolder?, listener: ViewPropertyAnimatorListener?) {
            ViewCompat.animate(itemView)
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(300)
                    .setListener(listener)
                    .start();
        }

        override fun animateRemoveImpl(holder: RecyclerView.ViewHolder?, listener: ViewPropertyAnimatorListener?) {
            ViewCompat.animate(itemView)
                    .translationY(-itemView.getHeight() * 0.3f)
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(listener)
                    .start();
        }

    }

    class Other(view: View) : RecyclerView.ViewHolder(view), AnimateViewHolder {
        val container = view.findViewById<ViewGroup>(R.id.container)
        val avatar = view.findViewById<SimpleDraweeView>(R.id.avatar)
        val username = view.findViewById<TextView>(R.id.username)
        val body = view.findViewById<TextView>(R.id.body)
        val accept = view.findViewById<Button>(R.id.accept)
        val dismiss = view.findViewById<Button>(R.id.dismiss)
        val time = view.findViewById<TextView>(R.id.time)

        init {
            avatar.hierarchy = Functions.getAvatarHierarchy()

        }

        override fun preAnimateAddImpl(holder: RecyclerView.ViewHolder?) {
            ViewCompat.setTranslationY(itemView, -itemView.getHeight() * 0.3f);
            ViewCompat.setAlpha(itemView, 0f);
        }

        override fun preAnimateRemoveImpl(holder: RecyclerView.ViewHolder?) {
        }

        override fun animateAddImpl(holder: RecyclerView.ViewHolder?, listener: ViewPropertyAnimatorListener?) {
            ViewCompat.animate(itemView)
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(300)
                    .setListener(listener)
                    .start();
        }

        override fun animateRemoveImpl(holder: RecyclerView.ViewHolder?, listener: ViewPropertyAnimatorListener?) {
            ViewCompat.animate(itemView)
                    .translationY(-itemView.getHeight() * 0.3f)
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(listener)
                    .start();
        }

    }

    fun swapItems(pushList: PushList) {

        list.addAll(pushList.pushes)
        notifyItemRangeInserted(list.size + 1, pushList.pushes.size)

    }

}