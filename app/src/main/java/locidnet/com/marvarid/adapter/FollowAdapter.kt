package locidnet.com.marvarid.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder

import org.json.JSONObject
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.model.ResponseData
import locidnet.com.marvarid.model.Users
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.resources.adapterAnim.AnimateViewHolder
import locidnet.com.marvarid.resources.utils.Functions
import locidnet.com.marvarid.resources.utils.JS
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.MainActivity
import locidnet.com.marvarid.ui.fragment.FFFFragment
import locidnet.com.marvarid.ui.fragment.ProfileFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates


class FollowAdapter(context:Context,
                    follows:ArrayList<Users>,
                    adapterClicker: AdapterClicker, private val which: Int = 0) : RecyclerView.Adapter<FollowAdapter.Holder>() {

    var ctx      = context
    var users    = follows
    var inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var clicker  = adapterClicker
    val model    = Model()

    var animationsLocked     = false
    private var lastAnimatedPosition = -1
    private var delayEnterAnimation  = true

    override fun getItemCount(): Int = users.size


    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): Holder =
            Holder(inflater.inflate(R.layout.res_follower_list_item,p0,false))

    override fun onBindViewHolder(h: Holder?, @SuppressLint("RecyclerView") p1: Int) {

        val user = users[p1]

        h!!.itemView.runEnterAnimation(p1)
        log.d("$user")
        h.login.text = user.username
        h.login.typeface = Typeface.createFromAsset(Base.get.context.assets,"font/regular.ttf")
        h.name.visibility = View.GONE




        h.img.post{

            h.img.controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(
                            ImageRequestBuilder.newBuilderWithSource(Uri.parse(Functions.checkImageUrl(user.photo150)))
                                    .setResizeOptions(ResizeOptions(100,100))
                                    .build())
                    .setOldController(h.img.controller)
                    .setAutoPlayAnimations(true)

                    .build()

        }

       if (which == 0 && user.userId != Base.get.prefs.getUser().userId){
           if(user.close == 1 && user.follow == 0 && user.request == 0){

               log.d("${user.userId} -> ${user.username}ga zapros tashalgan")
               h.follow.tag  = ProfileFragment.FOLLOW
               h.follow.text = ProfileFragment.FOLLOW
               h.follow.setTextColor(ctx.resources.getColor(R.color.white))

               h.follow.setBackgroundDrawable(ctx.resources.getDrawable(R.drawable.button_accent_select))

           }else if(user.close == 1 && user.follow == 0 && user.request == 1){

               h.follow.tag  = ProfileFragment.REQUEST
               h.follow.text = ProfileFragment.REQUEST
               h.follow.setTextColor(ctx.resources.getColor(R.color.white))

               h.follow.setBackgroundDrawable(ctx.resources.getDrawable(R.drawable.button_accent_select))

           }else if (user.close == 1 && user.follow == 1 && user.request == 0){

               h.follow.tag  = ProfileFragment.UN_FOLLOW
               h.follow.text = ProfileFragment.UN_FOLLOW
               h.follow.setTextColor(ctx.resources.getColor(R.color.headerTextColor))

               h.follow.setBackgroundDrawable(ctx.resources.getDrawable(R.drawable.button_accent_deselect))
           }else if (user.close == 0 && user.follow == 0 && user.request == 1){

               h.follow.tag  = ProfileFragment.FOLLOW
               h.follow.text = ProfileFragment.FOLLOW
               h.follow.setTextColor(ctx.resources.getColor(R.color.white))

               h.follow.setBackgroundDrawable(ctx.resources.getDrawable(R.drawable.button_accent_select))


           }else if (user.close == 0 && user.follow == 1 && user.request == 0){

               h.follow.tag  = ProfileFragment.UN_FOLLOW
               h.follow.text = ProfileFragment.UN_FOLLOW
               h.follow.setTextColor(ctx.resources.getColor(R.color.headerTextColor))

               h.follow.setBackgroundDrawable(ctx.resources.getDrawable(R.drawable.button_accent_deselect))

           }else{
               h.follow.tag  = ProfileFragment.FOLLOW
               h.follow.text = ProfileFragment.FOLLOW
               h.follow.setTextColor(ctx.resources.getColor(R.color.white))

               h.follow.setBackgroundDrawable(ctx.resources.getDrawable(R.drawable.button_accent_select))

           }


       }else{
           log.d("${user.userId} -> ${user.username} bir xil ")

           h.follow.visibility = View.GONE

       }
        h.follow.setOnClickListener {

            val reqObj =  JS.get()

//            reqObj.put("user_id",profile.userId)
//            reqObj.put("session",profile.session)
            reqObj.put("user",   users[p1].userId)

            log.d("request data $reqObj")
            if (h.follow.tag == ProfileFragment.FOLLOW){
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

                                                users[p1].follow  = 0
                                                users[p1].request = 1
                                                swapItems(users,p1)

                                            }else if (req.optString("request") == "0"){
                                                users[p1].follow  = 1
                                                users[p1].request = 0
                                                swapItems(users,p1)
                                                if (FFFFragment.followersCount != -1) FFFFragment.followersCount++
                                                MainActivity.FEED_STATUS = MainActivity.NEED_UPDATE
                                                MainActivity.MY_POSTS_STATUS = MainActivity.ONLY_USER_INFO

                                            }

                                        }catch (e : Exception){

                                        }



                                }else{
                                    Toast.makeText(Base.get, Base.get.resources.getString(R.string.internet_conn_error), Toast.LENGTH_SHORT).show()

                                }


                            }

                        })
            }else if (h.follow.tag == ProfileFragment.REQUEST || h.follow.tag == ProfileFragment.UN_FOLLOW){
                model.responseCall(Http.getRequestData(reqObj, Http.CMDS.UN_FOLLOW))
                        .enqueue(object : Callback<ResponseData>{
                            override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
                                log.d("follow on fail $t")
                            }

                            override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {
                                if (response!!.isSuccessful){
                                    log.d("follow on response $response")
                                    log.d("follow on response ${response.body()!!.res}")
                                    log.d("follow on response ${Http.getResponseData(response.body()!!.prms)}")
                                    log.d("follow on text     ${h.follow.text}")


                                        users[p1].request = 0
                                        users[p1].follow  = 0
                                        swapItems(users,p1)
                                        if (FFFFragment.followersCount != -1) FFFFragment.followersCount--
                                        MainActivity.MY_POSTS_STATUS = MainActivity.ONLY_USER_INFO
                                        MainActivity.FEED_STATUS = MainActivity.NEED_UPDATE

                                }else{
                                    Toast.makeText(Base.get, Base.get.resources.getString(R.string.internet_conn_error), Toast.LENGTH_SHORT).show()

                                }


                            }

                        })
            }

        }
        h.container.setOnClickListener {
                clicker.click(p1)
        }
    }
    fun swapItems(list:ArrayList<Users>,position:Int){

        users = list

        notifyItemChanged(position)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) , AnimateViewHolder {

        var img by Delegates.notNull<SimpleDraweeView>()
        var name by Delegates.notNull<TextView>()
        var login by Delegates.notNull<TextView>()
        var follow by Delegates.notNull<Button>()
        var container by Delegates.notNull<ViewGroup>()

        init {
            img = itemView.findViewById<SimpleDraweeView>(R.id.img)
            img.hierarchy = Functions.getAvatarHierarchy()

            name = itemView.findViewById<TextView>(R.id.name)
            login = itemView.findViewById<TextView>(R.id.login)
            follow = itemView.findViewById<Button>(R.id.follow)
            container = itemView.findViewById<ViewGroup>(R.id.container)

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

    fun View.runEnterAnimation(position:Int){

        //if (animationsLocked) return
        if (position > lastAnimatedPosition){

            lastAnimatedPosition = position

            this.translationY = 100f

            this.alpha        = 0f
            this.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setStartDelay(if (delayEnterAnimation) (20 * position).toLong() else 0)
                    .setInterpolator(DecelerateInterpolator(2f))
                    .setDuration(300)
                    .setListener(object : AnimatorListenerAdapter(){
                        override fun onAnimationEnd(animation: Animator?) {
                            if (translationY == 100f) translationY = 0f
                            animationsLocked = true
                        }
                    }).start()
        }

    }

}