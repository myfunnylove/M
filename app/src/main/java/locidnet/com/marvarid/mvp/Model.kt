package locidnet.com.marvarid.mvp

import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.model.PostList
import locidnet.com.marvarid.model.ResponseData
import locidnet.com.marvarid.pattern.cryptDecorator.AppCrypt
import locidnet.com.marvarid.pattern.cryptDecorator.B64EncoderCryptDecorator
import locidnet.com.marvarid.resources.utils.Functions
import locidnet.com.marvarid.resources.utils.Prefs
import retrofit2.Call

/**
 * Created by Michaelan on 6/15/2017.
 */
class Model {
    val user  = Prefs.Builder().getUser()

    val idAndSess = B64EncoderCryptDecorator(AppCrypt("${user.userId}:${user.session}")).getPrm()

    fun response(request:String) : Observable<ResponseData> = Base.get.APIClient.request(Functions.getDeviceName()!!,idAndSess,request)
    fun responseCall(request:String) : Call<ResponseData> = Base.get.APIClient.requestCall(Functions.getDeviceName()!!,idAndSess,request)

    fun uploadPhoto(body:MultipartBody.Part,name:RequestBody) : Observable<ResponseData> = Base.get.APIClient.uploadPhoto(Functions.getDeviceName()!!,idAndSess,body,name)
    fun uploadAudio(body:MultipartBody.Part,name:RequestBody) : Observable<ResponseData> = Base.get.APIClient.uploadAudio(Functions.getDeviceName()!!,idAndSess,body,name)


    fun uploadPhotoDemo(body:MultipartBody.Part,name:RequestBody,userId:String,session:String) : Call<ResponseData> = Base.get.APIClient.uploadPhotoDemo(Functions.getDeviceName()!!,idAndSess,body,name,userId,session)
    fun uploadAudioDemo(body:MultipartBody.Part,name:RequestBody,userId:String,session:String) : Call<ResponseData> = Base.get.APIClient.uploadAudioDemo(Functions.getDeviceName()!!,idAndSess,body,name,userId,session)
    fun uploadAvatar(body:MultipartBody.Part,name:RequestBody,profile:String,userId:String,session:String) : Observable<ResponseData> = Base.get.APIClient.uploadAvatar(Functions.getDeviceName()!!,idAndSess,body,name,profile,userId,session)



}