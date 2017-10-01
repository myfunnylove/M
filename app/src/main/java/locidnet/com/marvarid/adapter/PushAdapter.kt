package locidnet.com.marvarid.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.model.Push
import locidnet.com.marvarid.model.PushList
import locidnet.com.marvarid.model.ResponseData
import locidnet.com.marvarid.mvp.Model
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
    val options               = RequestOptions()
            .centerCrop()
            .fallback(VectorDrawableCompat.create(Base.get.resources,R.drawable.image, Base.get.theme))
            .error(VectorDrawableCompat.create(Base.get.resources,R.drawable.image, Base.get.theme))
            .placeholder(VectorDrawableCompat.create(Base.get.resources,R.drawable.image, Base.get.theme))

    val model = Model()
    val user = Prefs.Builder().getUser()
    val prettyTime = PrettyTime()
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int = list.get(position).type


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {

            Const.Push.LIKE -> return Like(inflater.inflate(R.layout.res_item_push_like, parent, false))

            Const.Push.COMMENT -> return Comment(inflater.inflate(R.layout.res_item_push_like, parent, false))

            Const.Push.FOLLOW -> return Requested(inflater.inflate(R.layout.res_item_push_requested, parent, false))
            Const.Push.REQUESTED -> return Requested(inflater.inflate(R.layout.res_item_push_requested, parent, false))

            else -> return Other(inflater.inflate(R.layout.res_item_push_requested, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        val push = list.get(position)

        val date2 = formatter.parse(push.time) as Date

        when (getItemViewType(position)) {

            Const.Push.LIKE -> {

                val like = holder as Like


                Glide.with(ctx)
                        .load(Functions.checkImageUrl(push.user.userPhoto))
                        .apply(Functions.getGlideOpts())
                        .into(like.avatar)

                like.avatar.setOnClickListener{


                    if (push.user.userId != user.userId){
                        var type = ProfileFragment.FOLLOW



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

                like.username.text = push.user.userName
                like.body.text = ctx.resources.getString(R.string.pushLikeBody)




                like.time.text = "${prettyTime.format(date2)} - "



                Glide.with(ctx)
                        .load(Functions.checkImageUrl(push.action.actionPhoto))
                        .apply(options)
                        .into(like.mypost)


                like.mypost.setOnClickListener {
                    val data = Intent(ctx,UserPostActivity::class.java)
                    data.putExtra("postId",push.action.actionID.toInt())
                    ctx.startActivity(data)
                }
            }

            Const.Push.COMMENT -> {

                val comment = holder as Comment


                Glide.with(ctx)
                        .load(Functions.checkImageUrl(push.user.userPhoto))
                        .apply(Functions.getGlideOpts())
                        .into(comment.avatar)
                comment.avatar.setOnClickListener{


                    if (push.user.userId != user.userId){
                        var type = ProfileFragment.FOLLOW



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
                comment.username.text = push.user.userName
                comment.body.text = ctx.resources.getString(R.string.pushCommentBody)
                comment.time.text = "${prettyTime.format(date2)} - "

                Glide.with(ctx)
                        .load(Functions.checkImageUrl(push.action.actionPhoto))
                        .apply(options)
                        .into(comment.mypost)


                comment.mypost.setOnClickListener {
                    val data = Intent(ctx,UserPostActivity::class.java)
                    data.putExtra("postId",push.action.actionID.toInt())
                    ctx.startActivity(data)
                }
            }

            Const.Push.REQUESTED -> {

                val requested = holder as Requested

                Glide.with(ctx)
                        .load(Functions.checkImageUrl(push.user.userPhoto))
                        .apply(Functions.getGlideOpts())
                        .into(requested.avatar)

                requested.avatar.setOnClickListener{


                    if (push.user.userId != user.userId){
                        var type = ProfileFragment.FOLLOW



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

                requested.username.text = push.user.userName

                requested.body.text = ctx.resources.getString(R.string.pushRequestBody)

                requested.time.text ="${prettyTime.format(date2)} - "

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
                                        list.removeAt(position)
                                        notifyItemRemoved(position)
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
                                        list.removeAt(position)
                                        notifyItemRemoved(position)

                                    }
                                }

                                override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
                                    log.d("onfail $t")
                                }

                            })

                }

            }

            Const.Push.FOLLOW -> {

                val follow = holder as Requested

                Glide.with(ctx)
                        .load(Functions.checkImageUrl(push.user.userPhoto))
                        .apply(Functions.getGlideOpts())
                        .into(follow.avatar)

                follow.avatar.setOnClickListener{

                    if (push.user.userId != user.userId){
                        var type = ProfileFragment.FOLLOW



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

                follow.username.text = push.user.userName

                follow.body.text = ctx.resources.getString(R.string.pushFollowBody)
                follow.time.text = "${prettyTime.format(date2)} - "

                follow.dismiss.visibility = View.GONE

                log.d("push:  ${push}")
                try{
                    if (push.user.actions.requestIt == "1") {
                        follow.accept.tag = ProfileFragment.REQUEST
                        follow.accept.text = Functions.getString(R.string.request)
                    }else if(push.user.actions.followIt == "1") {
                        follow.accept.tag = ProfileFragment.UN_FOLLOW

                        follow.accept.text = Functions.getString(R.string.unfollow)

                    }else {
                        follow.accept.tag = ProfileFragment.FOLLOW

                        follow.accept.text = Functions.getString(R.string.follow)
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

                                                    list.get(position).user.actions.followIt  = "0"
                                                    list.get(position).user.actions.requestIt = "1"
                                                    notifyItemChanged(position)

                                                }else if (req.optString("request") == "0"){
                                                    list.get(position).user.actions.followIt  = "1"
                                                    list.get(position).user.actions.requestIt = "0"
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
                                            list.get(position).user.actions.followIt  = "0"
                                            list.get(position).user.actions.requestIt = "0"
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


    class Like(view: View) : RecyclerView.ViewHolder(view) {

        val container = view.findViewById<ViewGroup>(R.id.container)
        val username = view.findViewById<TextView>(R.id.username)
        val avatar = view.findViewById<AppCompatImageView>(R.id.avatar)
        val body = view.findViewById<TextView>(R.id.body)
        val time = view.findViewById<TextView>(R.id.time)
        val mypost = view.findViewById<AppCompatImageView>(R.id.actionPhoto)

    }

    class Comment(view: View) : RecyclerView.ViewHolder(view) {
        val container = view.findViewById<ViewGroup>(R.id.container)
        val username = view.findViewById<TextView>(R.id.username)
        val avatar = view.findViewById<AppCompatImageView>(R.id.avatar)
        val body = view.findViewById<TextView>(R.id.body)
        val time = view.findViewById<TextView>(R.id.time)
        val mypost = view.findViewById<AppCompatImageView>(R.id.actionPhoto)
    }

    class Requested(view: View) : RecyclerView.ViewHolder(view) {
        val container = view.findViewById<ViewGroup>(R.id.container)
        val avatar = view.findViewById<AppCompatImageView>(R.id.avatar)
        val username = view.findViewById<TextView>(R.id.username)
        val body = view.findViewById<TextView>(R.id.body)
        val accept = view.findViewById<Button>(R.id.accept)
        val dismiss = view.findViewById<Button>(R.id.dismiss)
        val time = view.findViewById<TextView>(R.id.time)

    }

    class Other(view: View) : RecyclerView.ViewHolder(view) {
        val container = view.findViewById<ViewGroup>(R.id.container)
        val avatar = view.findViewById<AppCompatImageView>(R.id.avatar)
        val username = view.findViewById<TextView>(R.id.username)
        val body = view.findViewById<TextView>(R.id.body)
        val accept = view.findViewById<Button>(R.id.accept)
        val dismiss = view.findViewById<Button>(R.id.dismiss)
        val time = view.findViewById<TextView>(R.id.time)

    }

    fun swapItems(pushList: PushList) {

        list.addAll(pushList.pushes)
        notifyItemRangeInserted(list.size + 1, pushList.pushes.size)

    }

}