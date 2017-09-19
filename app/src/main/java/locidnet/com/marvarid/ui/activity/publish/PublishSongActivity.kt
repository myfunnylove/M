package locidnet.com.marvarid.ui.activity.publish

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_publish_song.*
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.SongAdapter
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.model.Song
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Functions

/**
 * Created by Michaelan on 5/27/2017.
 */
class PublishSongActivity : BaseActivity(),AdapterClicker{
    override fun data(data: String) {

    }


    var songList:ArrayList<Song>? = null
    var songPosition = -1
    override fun getLayout(): Int {
        return R.layout.activity_publish_song
    }

    override fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {

            onBackPressed()
        }

            songList = Functions.getSongList(this)

            if (songList!!.size > 0){
                list.layoutManager = LinearLayoutManager(this)
                list.setHasFixedSize(true)
                list.adapter = SongAdapter(this,this,songList!!)
            }
    }



    override fun click(position: Int) {
            songPosition = position
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_done,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.publish -> {
                if(songPosition != -1){
                        val song = songList!!.get(songPosition)
                    val intent  = Intent()
                    intent.putExtra(Const.SONG_PICKED,song)
                    setResult(Activity.RESULT_OK,intent)
                    finish()
                }
            }
        }

        return true
    }

    override fun activityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }
}