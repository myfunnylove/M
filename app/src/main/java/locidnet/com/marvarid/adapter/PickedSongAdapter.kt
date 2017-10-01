package locidnet.com.marvarid.adapter

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.model.ProgressRequestBody
import locidnet.com.marvarid.model.ResponseData
import locidnet.com.marvarid.model.Song
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


/**
 * Created by Michaelan on 5/30/2017.
 */
class PickedSongAdapter(ctx:Context,adapterClicker:AdapterClicker,listPhoto:ArrayList<Song>) : RecyclerView.Adapter<PickedSongAdapter.Holder>() {


    var context:Context = ctx
    var clicker:AdapterClicker = adapterClicker
    var inflater:LayoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var list = listPhoto
    var isFirst = true
    val user = Base.get.prefs.getUser()

    val name = RequestBody.create(MediaType.parse("text/plain"),"test_audio")

    object Audio{
        val UPLOAD = 0
        val UPDATE = 1
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): Holder {

       return Holder(inflater.inflate(R.layout.res_picked_song_item,p0,false))
    }

    override fun onBindViewHolder(h: Holder?, p1: Int) {


        val song = list.get(p1)


        log.d("$song")


        h!!.pr!!.setProgress(100f)
        val reqFile = ProgressRequestBody(File(song.songPath), object : ProgressRequestBody.UploadCallbacks{

            override fun onProgressUpdate(percentage: Int) {


                if (p1 != -1){
                    list.get(p1).progress = percentage
                    h.pr!!.setProgress(list.get(p1).progress.toFloat())
                }

            }

            override fun onError() {
                log.d("onerror")

            }

            override fun onFinish() {
                log.d("onfinish")
            }

        }, ProgressRequestBody.AUDIO_MP3)

        val body = MultipartBody.Part.createFormData("upload", File(song.songPath).name,reqFile)

         log.d("file: ${body.body()!!.contentType()}")

        if (p1 == list.size - 1){
            if (isFirst) CustomAnim.setScaleAnimation(h.container,500)
        }

        h.errorImg.visibility = View.GONE

        h.songArtist.setText(song.songArtist)
        h.songName.setText(song.songTitle)



        h.container.setOnClickListener {

                if (!song.loaded ){
                    h.errorImg.visibility = View.GONE
                    val call: Call<ResponseData> = Model().uploadAudioDemo(body,name,user.userId,user.session)
                    call.uploadAudioByUri(p1,song.songPath)
                }


        }









        if (!song.loaded ){

                if(song.onFail == 0){
                    val call: Call<ResponseData> = Model().uploadAudioDemo(body,name,user.userId,user.session)
                    call.uploadAudioByUri(p1,song.songPath)
                }else{
                    h.errorImg.visibility = View.VISIBLE
                }

            h.remove.visibility = View.GONE

        }else{
            h.remove.visibility = View.VISIBLE
            h.remove.setOnClickListener {
                list.removeAt(p1)
                PublishUniversalActivity.loadedAudioIds.removeAt(p1)
                notifyItemRemoved(p1)
                notifyDataSetChanged()
            }
            h.pr!!.setProgress(100f)

        }


    }

    fun swapItems(listPhoto:ArrayList<Song>){
        list = listPhoto
        notifyItemInserted(list.size)
    }

    class Holder(itemView:View) : RecyclerView.ViewHolder(itemView) {
        var songName by Delegates.notNull<TextView>()
        var songArtist by Delegates.notNull<TextView>()
        var container by Delegates.notNull<ViewGroup>()
        var progress by Delegates.notNull<ViewGroup>()
        var remove by Delegates.notNull<AppCompatImageView>()
        var errorImg:AppCompatImageView by Delegates.notNull<AppCompatImageView>()
        var pr:ImageUploadMask? = null
        init {

            songName = itemView.findViewById<TextView>(R.id.songName)
            songArtist = itemView.findViewById<TextView>(R.id.songArtist)
            songArtist = itemView.findViewById<TextView>(R.id.songArtist)
            container = itemView.findViewById<ViewGroup>(R.id.container)
            progress = itemView.findViewById<ViewGroup>(R.id.progress)
            errorImg = itemView.findViewById<AppCompatImageView>(R.id.errorImg)
            remove = itemView.findViewById<AppCompatImageView>(R.id.remove)

            pr = ImageUploadMask.Builder(Base.get)
                    .bind(progress)
                    .textColorInt(Color.BLACK)
                    .maskColorInt(Color.argb(90,0,0,0))
                    .textSize(16f)
                    .cornerRadius(4f)
                    .direction(ShapeMask.Direction.DTU)
                    .margin(1f)
                    .build()
        }


    }

    fun setError(position:Int,path: String){
        isFirst = false

        list.forEach{
            item -> if (item.songPath == path) {
            log.d("${item.songPath} this photo cannot upload")
            item.loaded = false
            item.onFail ++
        }
        }
        notifyItemChanged(position)

    }

    fun setProgress(position:Int,model: Song){
        log.d("update holder: $position $model")
        isFirst = false
        list.set(position,model)

        notifyItemChanged(position)
    }

    fun Call<ResponseData>.uploadAudioByUri (id:Int,path:String){

        PublishUniversalActivity.loading = true

        this.enqueue(object : Callback<ResponseData> {
            override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
                log.d("fail $t")
                PublishUniversalActivity.loading = false

                setError(id,path)

            }

            override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {
//                log.d("result ${response!!.body()} ")
//                log.d("prm ${Http.getResponseData(response.body()!!.prms)} ")

                PublishUniversalActivity.loading = false

                try{
                    if (response!!.body()!!.res == "0")
                    {
                            val resObj = JSONObject(Http.getResponseData(response.body()!!.prms))
                            val audioId = resObj.optString("audio_id")


                            PublishUniversalActivity.loadedAudioIds.add(audioId)

                            val song = list.get(id)
                            song.progress = 100
                            song.onFail = 0
                            song.loaded = true


                            setProgress(id,song)

                    }else{

                        setError(id,path)

                    }
                }catch (e:Exception){
                    log.d("unzip image_id exception $e")
                    setError(id,path)

                }

            }

        })



    }

    override fun onViewRecycled(holder: Holder?) {
//        try{
//            Glide.with(context).clear(holder!!.itemView)
//
//        }catch (e:Exception){}
        super.onViewRecycled(holder)
    }
}