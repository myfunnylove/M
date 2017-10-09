package locidnet.com.marvarid.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.connectors.MusicPlayerListener
import locidnet.com.marvarid.model.Audio
import locidnet.com.marvarid.model.ResponseData
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.player.PlayerService
import locidnet.com.marvarid.resources.utils.*
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.ui.activity.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLDecoder


class PostAudioGridAdapter(private val context: Context, list: ArrayList<Audio>, private val player: MusicPlayerListener, private val model: Model, private val isPlayList: Boolean = false) : RecyclerView.Adapter<PostAudioGridAdapter.Holder>() {


    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val PLAY = R.drawable.play
    private val PAUSE = R.drawable.pause
    var user = Base.get.prefs.getUser()
    val audios = list
    private val notFeatured = VectorDrawableCompat.create(Base.get.resources, R.drawable.plus, context.theme)
    private val featured = VectorDrawableCompat.create(Base.get.resources, R.drawable.playlist_remove, context.theme)


    private val featureMap = mapOf(0 to notFeatured!!, 1 to featured!!)
    val progress: HashMap<Int, String>? = null
    override fun getItemCount(): Int = audios.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: Holder?, i: Int) {


        val audio = audios[i]


        log.d("audio ${audios[i].middlePath}")

        val playIcon = VectorDrawableCompat.create(Base.get.resources, PLAY, h!!.play.context.theme)

        val pauseIcon = VectorDrawableCompat.create(Base.get.resources, PAUSE, h.play.context.theme)

        log.d("after notify ${audio.isFeatured}")

        if (audio.isFeatured != -1) {
            h.addFavorite.setImageDrawable(featureMap[audio.isFeatured])
            h.addFavorite.tag = featureMap[audio.isFeatured]
        } else {
            h.addFavorite.visibility = View.GONE
        }
        h.play.setImageDrawable(playIcon)

        if (audio.middlePath == PlayerService.PLAYING_SONG_URL && PlayerService.PLAY_STATUS == PlayerService.PLAYING) {
            h.play.tag = PAUSE
            h.play.setImageDrawable(pauseIcon)

        } else {
            h.play.tag = PLAY
            h.play.setImageDrawable(playIcon)

        }

        h.addFavorite.setOnClickListener {

            if (!isPlayList) {

                try {


                    if (h.addFavorite.tag == featureMap[0]) {
                        h.addFavorite.setImageDrawable(featureMap[1])
                        h.addFavorite.tag = featureMap[1]
                        audios[h.adapterPosition].isFeatured = 1
                        log.d("to notify ${h.adapterPosition}")

                        notifyItemChanged(h.adapterPosition)
                        notifyDataSetChanged()
                    } else {
                        h.addFavorite.setImageDrawable(featureMap[0])
                        h.addFavorite.tag = featureMap[0]
                        audios[h.adapterPosition].isFeatured = 0
                        notifyItemChanged(h.adapterPosition)
                        notifyDataSetChanged()

                    }


                } catch (e: Exception) {
                }
            } else {

                try {
                    if (PlayerService.PLAYING_SONG_URL == audio.middlePath && PlayerService.PLAY_STATUS == PlayerService.PLAYING) {
                        Toaster.info(context.resources.getString(R.string.error_cannot_remove))

                    } else {
                        MainActivity.MY_POSTS_STATUS = MainActivity.FIRST_TIME
                        audios.removeAt(h.adapterPosition)
                        notifyItemRemoved(h.adapterPosition)
                        PlayerService.songPosn = 0
                        PlayerService.songs.removeAt(h.adapterPosition)
                    }
                } catch (e: Exception) {

                }

            }

            val reqObj = JS.get()

            reqObj.put("audio", audio.audioId)

            model.responseCall(Http.getRequestData(reqObj, Http.CMDS.ADD_SONG_TO_PLAYLIST))
                    .enqueue(object : Callback<ResponseData> {
                        override fun onFailure(call: Call<ResponseData>?, t: Throwable?) {
                            log.d("from add audio from favorite $t")

                        }

                        override fun onResponse(call: Call<ResponseData>?, response: Response<ResponseData>?) {


                        }

                    })


            notifyItemChanged(h.adapterPosition)

        }

        h.play.setOnClickListener {

            log.d("audio clicked => ${audios[i]}")
            player.playClick(audios, i)
        }



        h.songName.text = if (audio.artist.isNotEmpty()) URLDecoder.decode(audio.artist, "UTF-8")
        else context.resources.getString(R.string.unknown)
        h.title.text = if (audio.title.isNotEmpty()) URLDecoder.decode(audio.title, "UTF-8")
        else context.resources.getString(R.string.unknown)
        h.duration.text = "(${audio.duration})"
    }

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): Holder =
            Holder(inflater.inflate(R.layout.res_post_audio_item, p0, false))

    class Holder(view: View) : RecyclerView.ViewHolder(view) {


        var container: ViewGroup = view.findViewById<ViewGroup>(R.id.container)
        var title: TextView = view.findViewById<TextView>(R.id.title)
        var duration: TextView = view.findViewById<TextView>(R.id.duration)
        var songName: TextView = view.findViewById<TextView>(R.id.songName)
        var play: AppCompatImageView = view.findViewById<AppCompatImageView>(R.id.play)
        var progress: ProgressBar = view.findViewById<ProgressBar>(R.id.progress)
        var addFavorite: AppCompatImageView = view.findViewById<AppCompatImageView>(R.id.addFavorite)

    }


}