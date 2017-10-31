package locidnet.com.marvarid.ui.activity.publish

import android.app.Activity
import android.content.Intent
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_publish_song.*
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.SongAdapter
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.connectors.SongClicker
import locidnet.com.marvarid.model.Song
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Functions
import java.util.*

/**
 * Created by Michaelan on 5/27/2017.
 */
class PublishSongActivity : BaseActivity(), SongClicker {





    var songList:ArrayList<Song>? = null
    var song:Song? = null
    var adapter:SongAdapter? =null
    override fun getLayout(): Int {
        return R.layout.activity_publish_song
    }

    override fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        initSearchView()

        toolbar.setNavigationOnClickListener {

            onBackPressed()
        }

            songList = Functions.getSongList(this)
//            songList!!.sortedWith(compareBy({it.dataAdded}))
            if (songList!!.size > 0){
                list.layoutManager = LinearLayoutManager(this)
                list.setHasFixedSize(true)
                adapter  =SongAdapter(this,this,songList!!)
                list.adapter = adapter
            }
    }




    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_done,menu)
        return true
    }
    private fun initSearchView() {

        searchView.setTextColor(getResources().getColor(R.color.normalTextColor));
        searchView.setCompoundDrawablesWithIntrinsicBounds(VectorDrawableCompat.create(resources,R.drawable.search_select,theme),null,null,null)
        searchView.compoundDrawablePadding = 6
        searchView.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(newText: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (!newText!!.toString().isEmpty()) {
                    adapter!!
                            .getFilteredResults()
                            .getFilter()
                            .filter(newText)
                    adapter!!.notifyDataSetChanged()
                } else {
                    adapter!!.swapItems(songList)
                }
            }

        })





    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.publish -> {
                if(song != null){
                    val intent  = Intent()
                    intent.putExtra(Const.SONG_PICKED,song)
                    setResult(Activity.RESULT_OK,intent)
                    finish()
                }
            }
        }

        return true
    }

    override fun songClick(song: Song) {
        this.song = song
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Const.SESSION_OUT || resultCode == Const.SESSION_OUT){
            setResult(Const.SESSION_OUT)
            finish()
        }
    }



}