package locidnet.com.marvarid.adapter

import android.content.Context
import android.support.v7.widget.AppCompatRadioButton
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import locidnet.com.marvarid.R
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.model.Song
import locidnet.com.marvarid.resources.utils.log
import java.text.DecimalFormat
import kotlin.properties.Delegates


class SongAdapter(clicker:AdapterClicker, ctx:Context, list:ArrayList<Song>) : RecyclerView.Adapter<SongAdapter.Adapter>() {
    private val adapterClicker = clicker
    val context = ctx
    private var songs = list
    val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onBindViewHolder(h: Adapter?, p1: Int) {
        val song = songs[p1]
        log.d("${song.songId} ${song.songTitle} ${song.songArtist} ${song.songDuration} ${song.songSize}")


        h!!.songArtist.text = song.songArtist
        h  .songName.text = song.songTitle


        h.songCheck.isChecked = song.selected
        h.songDuration.text = "${song.songDuration.formateMilliSeccond()} | "
        h.songSize.text = song.songSize.getSize()
        h.container.setOnClickListener {
                    selecterSong(p1,!h.songCheck.isChecked)
                    adapterClicker.click(p1)
        }


//        h.songCheck.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
//            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
//                    selecterSong(p1,!isChecked)
//                    adapterClicker.click(p1)
//            }
//
//        })
    }

    override fun getItemCount(): Int = songs.size

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): Adapter =
            Adapter(inflater.inflate(R.layout.res_song_item,p0,false))

    class Adapter(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var songName     by Delegates.notNull<TextView>()
        var songArtist   by Delegates.notNull<TextView>()
        var songSize     by Delegates.notNull<TextView>()
        var songDuration by Delegates.notNull<TextView>()
        var container    by Delegates.notNull<ViewGroup>()
        var songCheck    by Delegates.notNull<AppCompatRadioButton>()
        init {
            songName = itemView.findViewById<TextView>(R.id.songName)
            songArtist = itemView.findViewById<TextView>(R.id.songArtist)
            songSize = itemView.findViewById<TextView>(R.id.songSize)
            songDuration = itemView.findViewById<TextView>(R.id.songDuration)
            songArtist = itemView.findViewById<TextView>(R.id.songArtist)
            songCheck = itemView.findViewById<AppCompatRadioButton>(R.id.songCheck)
            container = itemView.findViewById<ViewGroup>(R.id.container)

        }
    }

    private fun Long.getSize():String{
        val df = DecimalFormat("0.00")

        val sizeKb = 1024.0f
        val sizeMo = sizeKb * sizeKb
        val sizeGo = sizeMo * sizeKb
        val sizeTerra = sizeGo * sizeKb



        return when {
            this < sizeMo -> df.format(this / sizeKb) + " Kb"
            this < sizeGo -> df.format(this / sizeMo) + " Mb"
            this < sizeTerra -> df.format(this / sizeGo) + " Gb"
            else -> ""
        }

    }


    private fun Long.formateMilliSeccond(): String {
        var finalTimerString = ""
        val secondsString:String

        // Convert total duration into time
        val hours = (this / (1000 * 60 * 60)).toInt()
        val minutes = (this % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (this % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()

        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours.toString() + ":"
        }

        // Prepending 0 to seconds if it is one digit
        secondsString = if (seconds < 10) {
            "0" + seconds
        } else {
            "" + seconds
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString


        return finalTimerString
    }


    private fun selecterSong(pos:Int, isCheck:Boolean){
        val res:ArrayList<Song> = ArrayList()
        for (i in songs.indices){

            val song:Song = songs[i]
                song.selected = false

            res.add(song)

        }

        res[pos].selected = isCheck
        songs = res

        this.notifyDataSetChanged()
    }

}