package locidnet.com.marvarid.rest

import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import locidnet.com.marvarid.model.ResponseData
import locidnet.com.marvarid.resources.utils.Functions
import retrofit2.Call
import retrofit2.http.*

interface API {

    @POST("index.php")
    @FormUrlEncoded
    fun request(@Header("User-Agent") userAgent: String?, @Header("Authorization") headerIdAndSess: String, @Field("data") data: String): Observable<ResponseData>

    @POST("index.php")
    @FormUrlEncoded
    fun requestCall(@Header("User-Agent") userAgent: String?, @Header("Authorization") headerIdAndSess: String, @Field("data") data: String): Call<ResponseData>

    @Multipart
    @POST("image.php")
    fun uploadPhoto(@Header("User-Agent") userAgent: String?,
                    @Header("Authorization") headerIdAndSess: String,
                    @Part file: MultipartBody.Part,
                    @Part("name") name: RequestBody
    ): Observable<ResponseData>

    @Multipart
    @POST("audio.php")
    fun uploadAudio(@Header("User-Agent") userAgent: String?,
                    @Header("Authorization") headerIdAndSess: String,
                    @Part file: MultipartBody.Part,
                    @Part("name") name: RequestBody
    ): Observable<ResponseData>


    @Multipart
    @POST("img_post.php")
    fun uploadPhotoDemo(
            @Header("User-Agent") userAgent: String?,
            @Header("Authorization") headerIdAndSess: String,
            @Part file: MultipartBody.Part,
            @Part("name") name: RequestBody,
            @Query("user_id") userId :String,
            @Query("session") session :String): Call<ResponseData>

    @Multipart
    @POST("audio.php")
    fun uploadAudioDemo(
            @Header("User-Agent") userAgent: String?,
            @Header("Authorization") headerIdAndSess: String,
            @Part file: MultipartBody.Part,
            @Part("name") name: RequestBody,
            @Query("user_id") userId :String,
            @Query("session") session :String): Call<ResponseData>

    @Multipart
    @POST("img_profile.php")
    fun uploadAvatar(
            @Header("User-Agent") userAgent: String?,
            @Header("Authorization") headerIdAndSess: String,
            @Part file: MultipartBody.Part,
            @Part("name") name: RequestBody,
            @Query("profile") profil: String,
            @Query("user_id") userId :String,
            @Query("session") session :String): Observable<ResponseData>
}