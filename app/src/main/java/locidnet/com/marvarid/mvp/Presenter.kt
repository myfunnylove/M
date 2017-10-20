package locidnet.com.marvarid.mvp

import android.support.v7.widget.AppCompatEditText
import com.google.gson.Gson
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.model.Audio
import locidnet.com.marvarid.model.Features
import locidnet.com.marvarid.model.ResponseData
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.model.UserInfo
import locidnet.com.marvarid.pattern.builder.SessionOut
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.JS
import locidnet.com.marvarid.resources.utils.Prefs
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.MainActivity
import org.reactivestreams.Subscription
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit


class Presenter(viewer: Viewer, modeler:Model,context:BaseActivity) :IPresenter {

    val view:Viewer = viewer

    val model:Model = modeler
    val context = context
    var subscription:Disposable? =null
    override fun requestAndResponse(data:JSONObject, cmd:String){


        log.v("REQUEST =========>")
        log.v("JSON DATA :")
        log.e(data.toString())
        log.v("CMD: ")
        log.e(cmd)
        log.v("REQUEST =========;")


        view.initProgress()
        view.showProgress()
        subscription = Observable.just(model.response(Http.getRequestData(data,cmd)))

                           .subscribeOn(Schedulers.io())
                            .flatMap({res -> res})
                            .flatMap({
                             res ->

                                if(res.res == "0" && (cmd == Http.CMDS.LOGIN_PAYTI || cmd == Http.CMDS.FB_ORQALI_LOGIN || cmd == Http.CMDS.VK_ORQALI_LOGIN )){

                                 val response = JSONObject(Http.getResponseData(res.prms))

                                 val user = Base.get.prefs.getUser()

                                 user.userId  = response.optString("user_id")
                                 user.session = response.optString("session")
                                    Base.get.prefs.setUser(user)

                                 val reqObj = JS.get()
                                     reqObj.put("user",   user.userId)


                                     log.d("http zapros $cmd resultat: $res")
                                     log.d("send data for user info data: ${reqObj}")
                                        model.response(Http.getRequestData(reqObj, Http.CMDS.USER_INFO))
                                    }else{
                                        Observable.just(res)
                                    }
                         })
                         .flatMap({infoUser ->
                             log.d("flatmap ishladi: RES: ${infoUser.res} IN PRM: ${Http.getResponseData(infoUser.prms)}")
                             if (infoUser.res == "0" && (cmd == Http.CMDS.LOGIN_PAYTI || cmd == Http.CMDS.FB_ORQALI_LOGIN || cmd == Http.CMDS.VK_ORQALI_LOGIN )){



                                 val user = Base.get.prefs.getUser()

                                 val userInfo     = Gson().fromJson<UserInfo>(Http.getResponseData(infoUser.prms),UserInfo::class.java)
                                 user.userName    = userInfo.user.info.username
                                 user.profilPhoto = if (userInfo.user.info.photoOrg.isNullOrEmpty()) "empty" else userInfo.user.info.photoOrg
                                 try{
                                     user.close       =  userInfo.user.info.close

                                 }catch (e:IllegalArgumentException){
                                     user.close = 0
                                 }
                                 user.userPhone   = if (userInfo.user.info.phone.isNullOrEmpty()) "" else userInfo.user.info.phone
                                 user.userMail   = if (userInfo.user.info.mail.isNullOrEmpty()) "" else userInfo.user.info.mail
                                 user.first_name  = if (userInfo.user.info.name.isNullOrEmpty()) "" else userInfo.user.info.name
                                 user.gender      = if (userInfo.user.info.gender.isNullOrEmpty()) "" else userInfo.user.info.gender
                                 Base.get.prefs.setUser(user)
                                 Observable.just(infoUser)
                             }else{

                             Observable.just(infoUser)

                            }
                         })

                         .doOnNext { onNext -> if (cmd == Http.CMDS.GET_PLAYLIST){
                             log.d("Playlistni joylash")

                             var result:String? = Http.getResponseData(onNext.prms)
                             var features:Features? = Gson().fromJson(result, Features::class.java)
                             Base.get.appDb.playListDAO().insertAudios(features!!.audios)
                             log.d("Playlistni joylashdan kelgan rezultat ")
                             result = null
                             features = null

                         }

                         }
                         .onErrorResumeNext{
                             throwable:Throwable ->
                             if(cmd == Http.CMDS.GET_PLAYLIST){
                                 var resultList:List<Audio>? = Base.get.appDb.playListDAO().allAudios
                                 log.d("xato tufayli localdan oberdi ${resultList == null} ")
                                 if (resultList != null) {
                                     var features:Features? = Features(ArrayList(resultList))


                                     Observable.just(ResponseData("707","ok",Gson().toJson(features!!)))
                                 }else
                                     Observable.error(throwable)
                             }else
                                 Observable.error(throwable)


                         }

                           .observeOn(AndroidSchedulers.mainThread())

                           .subscribe({
                                response ->
                               try{
                                   log.d("CMD : ${cmd} \n RES: ${response.res} \n IN PRM ${Http.getResponseData(response.prms)}")
                               }catch(e:Exception){
                                   log.d("CND :${cmd} Exception: $e")
                               }



                               view.hideProgress()


                                when(response.res){
                                    "707"  -> view.onSuccess(cmd, response.prms)
                                    "0"    -> view.onSuccess(cmd, Http.getResponseData(response.prms))
                                    "1996" -> view.onFailure("",Base.get.resources.getString(R.string.error_no_type))
                                    "404"  ->  view.onFailure(cmd,Base.get.resources.getString(R.string.internet_conn_error))
                                    "96"   -> {
                                        val sesion = SessionOut.Builder(context)
                                                .setErrorCode(96)
                                                .build()
                                        sesion.out()
                                    }
                                     else  -> {
                                         if (response.message != "null")
                                              view.onFailure(cmd,response.message)
                                         else
                                              view.onFailure("",Base.get.resources.getString(R.string.error_something))
                                     }

                                }

                             },{
                                    fail ->
                                    log.d("RESPONSE FAILER =========>")
                                    log.e(fail.toString())
                                                  view.hideProgress()

                                   when (fail) {
                                       is SocketTimeoutException ->  view.onFailure(cmd,Base.get.resources.getString(R.string.internet_conn_error))
                                       is UnknownHostException ->    view.onFailure(cmd,Base.get.resources.getString(R.string.internet_conn_error))
                                       is HttpException ->           view.onFailure(cmd,Base.get.resources.getString(R.string.internet_conn_error))
                                       else -> {
                                           view.onFailure(cmd,Base.get.resources.getString(R.string.error_something))
                                       }
                                   }

                             })


    }


    override fun ondestroy() {
        if (subscription!= null && !subscription!!.isDisposed)subscription!!.dispose()
    }

    fun filterLogin(editText :AppCompatEditText){
        RxTextView.textChangeEvents(editText)
                .delay(3, TimeUnit.MILLISECONDS)
                .filter { beforeText -> beforeText.text().toString().length >= 6 }
                .map { filteredText ->

                    val obj = JSONObject()
                    obj.put("username",filteredText.text().toString())


                 }
                .flatMap { data ->
                    log.d("Data : befor decrypt -> $data")
                    model.response(Http.getRequestData(data, Http.CMDS.LOGIN_YOQLIGINI_TEKSHIRISH)) }
                .cache()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ( {

                    response ->
                    log.d("Data : succes from -> ${Http.CMDS.LOGIN_YOQLIGINI_TEKSHIRISH} $response")

                    if (response.res == "0") view.onSuccess(Http.CMDS.LOGIN_YOQLIGINI_TEKSHIRISH,"" )
                        else view.onFailure(Http.CMDS.LOGIN_YOQLIGINI_TEKSHIRISH,Base.get.resources.getString(R.string.error_something))

                },{
                    fail ->
                    log.d("Data : fail from -> ${Http.CMDS.LOGIN_YOQLIGINI_TEKSHIRISH}")

                    when (fail) {
                        is UnknownHostException ->{
                            view.onFailure(Http.CMDS.LOGIN_YOQLIGINI_TEKSHIRISH,Base.get.resources.getString(R.string.internet_conn_error))

                        }
                        else -> {
                            view.onFailure(Http.CMDS.LOGIN_YOQLIGINI_TEKSHIRISH,Base.get.resources.getString(R.string.error_something))
                        }
                        }


                } )
    }




    fun uploadPhoto(body: MultipartBody.Part){
        val user = Base.get.prefs.getUser()
        log.d("upload file ketvotti: ${body.body()!!}")
        val name = RequestBody.create(MediaType.parse("text/plain"),"image/*")
        subscription = Observable.just(model.uploadPhoto(body,name))
                .subscribeOn(Schedulers.io())
                .flatMap({res -> res})
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    result ->log.d("${result}")
                    if (result.res == "0"){
                        view.onSuccess("${Const.PICK_IMAGE}", Http.getResponseData(result.prms))
                    }
                },{
                    err -> log.d(err.toString())
                })
    }

    fun uploadAvatar(body: MultipartBody.Part){
        val user = Base.get.prefs.getUser()
        log.d("upload file ketvotti: ${body.body()!!}")
        val name = RequestBody.create(MediaType.parse("text/plain"),"image/*")
      subscription = Observable.just(model.uploadAvatar(body,name,"avatar",user.userId,user.session))
                .subscribeOn(Schedulers.io())
                .flatMap({res ->
                    log.d("$res")
                    res})
                .filter { filt -> filt.res == "0" }
                .flatMap {

                    val reqObj = JS.get()
                    reqObj.put("user",   user.userId)

                    model.response(Http.getRequestData(reqObj, Http.CMDS.USER_INFO))
                 }

                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    result ->
                    log.d("FROM UPLOAD AVATAR")
                    log.d("$result")
                    log.d(Http.getResponseData(result.prms))
                    if (result.res == "0"){

                        val userInfo     = Gson().fromJson<UserInfo>(Http.getResponseData(result.prms),UserInfo::class.java)
                        user.userName    = userInfo.user.info.username
                        user.profilPhoto = userInfo.user.info.photoOrg
                        Base.get.prefs.setUser(user)
                        MainActivity.MY_POSTS_STATUS = MainActivity.NEED_UPDATE
                        view.onSuccess(Http.CMDS.CHANGE_AVATAR, Http.getResponseData(result.prms))
                    }
                },{
                    err -> log.d(err.toString())
                })
    }


}