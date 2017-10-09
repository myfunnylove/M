package locidnet.com.marvarid.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.model.PhotoUpload
import locidnet.com.marvarid.model.ProgressRequestBody
import locidnet.com.marvarid.model.ResponseData
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.resources.customviews.imageuploadmask.ImageUploadMask
import locidnet.com.marvarid.resources.customviews.imageuploadmask.ShapeMask
import locidnet.com.marvarid.resources.utils.CustomAnim
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.ui.activity.publish.PublishUniversalActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import kotlin.properties.Delegates


class PickedPhotoAdapter(ctx:Context,adapterClicker:AdapterClicker,listPhoto:ArrayList<PhotoUpload>) : RecyclerView.Adapter<PickedPhotoAdapter.Holder>() {


    var context:Context = ctx
    var clicker:AdapterClicker = adapterClicker
    var inflater:LayoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var list = listPhoto
    private var isFirst = true

    val user = Base.get.prefs.getUser()
    val name = RequestBody.create(MediaType.parse("text/plain"),"test_image")!!

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): Holder =
            Holder(inflater.inflate(R.layout.res_photo_item,p0,false))

    override fun onBindViewHolder(h: Holder?, p1: Int) {
       val photo:PhotoUpload = list[p1]

        log.d("$photo")


        h!!.pr!!.setProgress(100f)
        val reqFile = ProgressRequestBody(File(photo.uri.path), object : ProgressRequestBody.UploadCallbacks{

            override fun onProgressUpdate(percentage: Int) {


                if (p1 != -1){
                    list[p1].progress = percentage
                    h.pr!!.setProgress(list[p1].progress.toFloat())
                }

            }

            override fun onError() {
                log.d("onerror")

            }

            override fun onFinish() {
                log.d("onfinish")
            }

        }, ProgressRequestBody.IMAGE_ALL)

        val body = MultipartBody.Part.createFormData("upload", File(photo.uri.path).name,reqFile)

        log.d("file: ${body.body()!!.contentType()}")

        if (p1 == list.size - 1){
            if (isFirst) CustomAnim.setScaleAnimation(h.container,500)
        }

        h.errorImg.visibility = View.GONE


//        if(h.image.tag == null || h.image.tag == photo.uri){

        Glide.with(context)
                .load(photo.uri)
                .apply(RequestOptions().fallback(ColorDrawable(Color.GRAY)))
                .into(h.image)

//            h.image.tag = photo.uri
//        }

        h.container.setOnClickListener {







            if (!photo.loaded ){
                h.errorImg.visibility = View.GONE
                val call: Call<ResponseData> = Model().uploadPhotoDemo(body,name,user.userId,user.session)
                call.uploadAudioByUri(p1,photo.uri.path)
            }


        }









        if (!photo.loaded ){

            if(photo.onFail == 0){
                val call: Call<ResponseData> = Model().uploadPhotoDemo(body,name,user.userId,user.session)
                call.uploadAudioByUri(p1,photo.uri.path)
            }else{
                h.errorImg.visibility = View.VISIBLE
            }
            h.remove.visibility = View.GONE

        }else{
            h.remove.visibility = View.VISIBLE
            h.remove.setOnClickListener {
                list.removeAt(p1)
                PublishUniversalActivity.loadedImagesIds.removeAt(p1)
                notifyItemRemoved(p1)
                notifyDataSetChanged()
            }
            h.pr!!.setProgress(100f)

        }
    }

    fun swapItems(listPhoto:ArrayList<PhotoUpload>){
        list = listPhoto
        notifyItemInserted(list.size)
    }
    class Holder(itemView:View) : RecyclerView.ViewHolder(itemView) {
        var image     by Delegates.notNull<AppCompatImageView>()
        var container by Delegates.notNull<ViewGroup>()
        var errorImg  by Delegates.notNull<AppCompatImageView>()
        var remove    by Delegates.notNull<AppCompatImageView>()

        var pr:ImageUploadMask? = null
        init {
            image = itemView.findViewById<AppCompatImageView>(R.id.image)
            errorImg = itemView.findViewById<AppCompatImageView>(R.id.errorImg)
            remove = itemView.findViewById<AppCompatImageView>(R.id.remove)
            container = itemView.findViewById<ViewGroup>(R.id.container)
            pr = ImageUploadMask.Builder(Base.get)
                    .bind(image)
                    .textColorInt(Color.WHITE)
                    .maskColorInt(Color.argb(90,0,0,0))
                    .textSize(Base.get.resources.getDimension(R.dimen.normalTextSize))
                    .cornerRadius(4f)
                    .direction(ShapeMask.Direction.DTU)
                    .margin(1f)
                    .build()
//            progress.setTextColor(Color.WHITE)
//            progress.setTextSize(16f)
//            progress.setMaskColor(Color.argb(50,0,0,0))
//            progress.setDirection(ShapeMask.Direction.LTR)
//            progress.setCornerRadius(4f)
//            progress.setMargin(1f)

        }
    }


    fun setError(position:Int,path: String){
        isFirst = false

        list.forEach{
            item -> if (item.uri.path == path) {
            log.d("${item.uri.path} this photo cannot upload")
            item.loaded = false
            item.onFail ++
        }
        }
        notifyItemChanged(position)

    }

    fun setProgress(position:Int,model: PhotoUpload){
        log.d("update holder: $position $model")
        isFirst = false
        list[position] = model

        notifyItemChanged(position)
    }

    private fun Call<ResponseData>.uploadAudioByUri (id:Int, path:String){






        PublishUniversalActivity.loading = true
        this.enqueue(object : Callback<ResponseData> {
            override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
                log.d("fail $t")
                PublishUniversalActivity.loading = false

                setError(id,path)

            }

            override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {
                log.d("result ${response!!} ")
                PublishUniversalActivity.loading = false

//                log.d("prm ${Http.getResponseData(response.body()!!.prms)} ")
                try{

                    if (response.body()!!.res == "0")
                    {
                        try{
                            val resObj = JSONObject(Http.getResponseData(response.body()!!.prms))

                            val audioId = resObj.optString("image_id")

                            PublishUniversalActivity.loadedImagesIds.add(audioId)

                            val song = list[id]
                            song.progress = 100
                            song.onFail = 0
                            song.loaded = true


                            setProgress(id,song)
                        }catch (e:Exception){
                            log.d("unzip image_id exception $e")
                        }

                    }else{

                        setError(id,path)

                    }
                }catch (e:Exception){
                    setError(id,path)

                }

            }

        })



    }

}