package locidnet.com.marvarid.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import org.json.JSONObject
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.model.ResponseData
import locidnet.com.marvarid.model.Users
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.resources.customviews.CircleImageView
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.MainActivity
import locidnet.com.marvarid.ui.fragment.FFFFragment
import locidnet.com.marvarid.ui.fragment.ProfileFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates

/**
 * Created by Michaelan on 5/19/2017.
 */
class FollowAdapter(context:Context,
                    follows:ArrayList<Users>,
                    adapterClicker: AdapterClicker,which:Int  = 0) : RecyclerView.Adapter<FollowAdapter.Holder>() {

    var ctx      = context
    var users    = follows
    var inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var clicker  = adapterClicker
    var profile  = Base.get.prefs.getUser()
    val model    = Model()
    val which    = which

    var animationsLocked     = false
    var lastAnimatedPosition = -1
    var delayEnterAnimation  = true

    override fun getItemCount(): Int {
        return users.size
    }


    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): Holder {
        return Holder(inflater.inflate(R.layout.res_follower_list_item,p0,false))
    }

    override fun onBindViewHolder(h: Holder?, @SuppressLint("RecyclerView") p1: Int) {

        val user = users.get(p1)

        h!!.itemView.runEnterAnimation(p1)
        log.d("$user")
        h.login.text = user.username
        h.login.typeface = Typeface.createFromAsset(Base.get.context.assets,"font/Quicksand-Regular.otf")
        h.name.visibility = View.GONE

        var photo = ""
        try{
            photo = if (user.photo150.startsWith("http")) user.photo150 else Http.BASE_URL+user.photo150
        }catch (e:Exception){
            photo = "http"
        }

        Picasso.with(ctx)
                .load(photo)
                .error(VectorDrawableCompat.create(Base.get.resources, R.drawable.account_select,null))
                .into(h.img)

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
           log.d("${user.userId} -> ${user.username}bir xil ")

           h.follow.visibility = View.GONE

       }
        h.follow.setOnClickListener {

            val reqObj = JSONObject()

            reqObj.put("user_id",profile.userId)
            reqObj.put("session",profile.session)
            reqObj.put("user",   users.get(p1).userId)

            log.d("request data $reqObj")

            model.responseCall(Http.getRequestData(reqObj, Http.CMDS.FOLLOW))
                    .enqueue(object : Callback<ResponseData>{
                        override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
                            log.d("follow on fail $t")
                        }

                        override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {
                            if (response!!.isSuccessful){
                                log.d("follow on response $response")
                                log.d("follow on response ${response.body()!!.res}")
                                log.d("follow on response ${Http.getResponseData(response.body()!!.prms)}")
                                log.d("follow on text     ${h.follow.text.toString()}")

                                if ((h.follow.tag == ProfileFragment.UN_FOLLOW || h.follow.tag == ProfileFragment.REQUEST) && response.body()!!.res == "0"){

                                    users.get(p1).request = 0
                                    users.get(p1).follow  = 0
                                    swapItems(users,p1)
                                    MainActivity.MY_POSTS_STATUS = MainActivity.FIRST_TIME
                                    if (FFFFragment.followersCount != -1) FFFFragment.followersCount--
                                }else if (h.follow.tag == ProfileFragment.FOLLOW && response.body()!!.res == "0"){

                                    try{

                                        val req = JSONObject(Http.getResponseData(response.body()!!.prms))
                                        if (req.optString("request") == "1"){

                                            users.get(p1).follow  = 0
                                            users.get(p1).request = 1
                                            swapItems(users,p1)

                                        }else if (req.optString("request") == "0"){
                                            users.get(p1).follow  = 1
                                            users.get(p1).request = 0
                                            swapItems(users,p1)
                                            if (FFFFragment.followersCount != -1) FFFFragment.followersCount++

                                        }
                                        MainActivity.MY_POSTS_STATUS = MainActivity.FIRST_TIME

                                    }catch (e : Exception){

                                    }


                                }
                            }else{
                                Toast.makeText(Base.get, Base.get.resources.getString(R.string.internet_conn_error), Toast.LENGTH_SHORT).show()

                            }


                        }

                    })
        }
        h.container.setOnClickListener {
                clicker.click(p1)
        }
    }
    fun swapItems(list:ArrayList<Users>,position:Int){

        users = list

        notifyItemChanged(position)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var img by Delegates.notNull<CircleImageView>()
        var name by Delegates.notNull<TextView>()
        var login by Delegates.notNull<TextView>()
        var follow by Delegates.notNull<Button>()
        var container by Delegates.notNull<ViewGroup>()

        init {
            img = itemView.findViewById(R.id.img) as CircleImageView
            name = itemView.findViewById(R.id.name) as TextView
            login = itemView.findViewById(R.id.login) as TextView
            follow = itemView.findViewById(R.id.follow) as Button
            container = itemView.findViewById(R.id.container) as ViewGroup

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
                            animationsLocked = true;
                        }
                    }).start()
        }

    }
}