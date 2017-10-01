package locidnet.com.marvarid.ui.activity.publish


import android.app.Activity
import android.arch.lifecycle.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.google.gson.Gson
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_publish_universal.*
import locidnet.com.marvarid.R
import locidnet.com.marvarid.adapter.ColorPaletteAdapter
import locidnet.com.marvarid.adapter.PickedPhotoAdapter
import locidnet.com.marvarid.adapter.PickedSongAdapter
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseActivity
import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.di.DaggerMVPComponent
import locidnet.com.marvarid.di.modules.ErrorConnModule
import locidnet.com.marvarid.di.modules.MVPModule
import locidnet.com.marvarid.di.modules.PresenterModule
import locidnet.com.marvarid.model.*
import locidnet.com.marvarid.mvp.Model
import locidnet.com.marvarid.mvp.Presenter
import locidnet.com.marvarid.mvp.Viewer
import locidnet.com.marvarid.pattern.builder.ErrorConnection
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.Functions
import locidnet.com.marvarid.resources.utils.Toaster
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.ui.dialogs.YesNoFragment
import locidnet.com.marvarid.viewmodel.PublishViewmodel
import me.iwf.photopicker.PhotoPicker
import org.json.JSONObject
import java.io.File
import javax.inject.Inject


class PublishUniversalActivity :BaseActivity(),Viewer , LifecycleOwner{


    var listMusic:ArrayList<Song>        = ArrayList()
    var listImage:ArrayList<PhotoUpload> = ArrayList()
    var imageAdapter:PickedPhotoAdapter? = null
    var songAdapter:PickedSongAdapter?   = null


    @Inject
    lateinit var presenter:Presenter


    @Inject
    lateinit var errorConn: ErrorConnection

    lateinit var quitDialog: YesNoFragment

    var visibly       = false
    var TEXT_SIZE     = 16f
    var TEXT_COLOR_ID = 0
    var idImage:Int   = -1
    var idMusic:Int   = -1
    var user:User?    = null
    var publishViewModel:PublishViewmodel? = null

    companion  object IDS{
        var loadedImagesIds:ArrayList<String>  = ArrayList()
        var loadedAudioIds :ArrayList<String>  = ArrayList()
        var loading:Boolean = false

    }
    override fun getLayout(): Int = R.layout.activity_publish_universal

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_publish,menu)
        return true
    }




    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item!!.itemId){
            R.id.publish -> {


                if (!commentText.text.isEmpty()){
                    errorConn.checkNetworkConnection(object : ErrorConnection.ErrorListener{
                        override fun connected() {
                            val logicImage = if (imageAdapter == null) true else imageAdapter!!.list.size == loadedImagesIds.size
                            val logicAudio = if (songAdapter == null)  true else songAdapter!!.list.size == loadedAudioIds.size

                            log.d("notloaded $loading $logicImage $logicAudio")
                            if (loading == false && logicImage && logicAudio){

                                val quote = Quote(commentText.text.toString(), // quote text
                                        "${TEXT_SIZE}", // quote text size
                                        "${TEXT_COLOR_ID}") //quote text color


                                val post = Post(user!!.userId,
                                        user!!.session,
                                        loadedAudioIds,
                                        loadedImagesIds,
                                        quote)
                                val reqJSOBJ = Gson().toJson(post)

                                log.d("send data $reqJSOBJ")

                                presenter.requestAndResponse(JSONObject(reqJSOBJ), Http.CMDS.POST)
                            }else {
                                Toaster.info(resources.getString(R.string.error_not_loaded_yet))
                            }

                        }

                        override fun disconnected() {
                            log.d("disconnected")
                            Toaster.info(resources.getString(R.string.internet_conn_error))

                        }

                    })

                }
                else{
                    Toaster.info(resources.getString(R.string.error_empty_quote))

                }

            }
        }

        return true
    }
    override fun initView() {
        Const.TAG = "PublishUniversalActivity"

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = resources.getString(R.string.post)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {

            onBackPressed()

        }
        toolbar.setNavigationOnClickListener {

            onBackPressed()

        }
        user      = Base.get.prefs.getUser()
        DaggerMVPComponent
                .builder()
                .mVPModule(MVPModule(this, Model(),this))
                .presenterModule(PresenterModule())
                .errorConnModule(ErrorConnModule(this,false))
                .build()
                .inject(this)

        quitDialog = YesNoFragment.instance(
                DialogFragmentModel(Functions.getString(R.string.quitPostPublish),
                        Functions.getString(R.string.yes),
                        Functions.getString(R.string.no))
        )

        quitDialog.setDialogClickListener(object : YesNoFragment.DialogClickListener{
            override fun click(whichButton: Int) {

                if (whichButton == YesNoFragment.NO)
                    finish()
                else
                    quitDialog.dismiss()

            }

        })

        initQuoteSettings()

        var icon = VectorDrawableCompat.create(resources,R.drawable.music,publishMusic.context.theme)

        publishMusic.setIconDrawable(icon!!)
            publishMusic.setOnClickListener {
                commentText.hideKeyboard()

                actionMenu.collapse()

                quoteSettings.visibility = View.GONE
                                 visibly = false

                val intent = Intent(this,PublishSongActivity().javaClass)
                startActivityForResult(intent,Const.PICK_AUDIO)
            }
        icon = VectorDrawableCompat.create(resources,R.drawable.image,publishMusic.context.theme)
        publishImage.setIconDrawable(icon!!)

        publishImage.setOnClickListener {
            commentText.hideKeyboard()

                actionMenu.collapse()


                quoteSettings.visibility = View.GONE
                                 visibly = false

                PhotoPicker.builder()
                           .setPhotoCount(1)

                           .start(this,Const.PICK_IMAGE)

        }
        icon = VectorDrawableCompat.create(resources,R.drawable.quote,editQuote.context.theme)
        editQuote.setIconDrawable(icon!!)

        editQuote.setOnClickListener {
                actionMenu.collapse()


                if (visibly == false)
                {
                    quoteSettings.visibility = View.VISIBLE
                                     visibly = true

                }else{
                    quoteSettings.visibility = View.GONE
                                     visibly = false

                }
            }


        clickContainer.setOnClickListener {
            commentText.showKeyboard()
        }

        actionMenu.setOnFloatingActionsMenuUpdateListener(object : FloatingActionsMenu.OnFloatingActionsMenuUpdateListener{
            override fun onMenuCollapsed() {
                commentText.hideKeyboard()
                quoteSettings.visibility = View.GONE
                visibly = false
            }

            override fun onMenuExpanded() {
                commentText.hideKeyboard()
                quoteSettings.visibility = View.GONE
                visibly = false
            }

        })
    }



    private fun initQuoteSettings() {
        colorList.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        colorList.setHasFixedSize(true)

        colorList.adapter = ColorPaletteAdapter(object  : AdapterClicker{
            override fun data(data: String) {

            }

            override fun click(position: Int) {
                TEXT_COLOR_ID = position
                commentText.setTextColor(ContextCompat.getColor(applicationContext,Const.colorPalette.get(position)!!.drawable))
            }

        },this,Const.colorPalette)

        commentText.textSize = Const.TEXT_SIZE_DEFAULT


        textSize1.setOnClickListener {
            TEXT_SIZE            = Const.TEXT_SIZE_DEFAULT
            commentText.textSize = TEXT_SIZE
        }

        textSize2.setOnClickListener {
            TEXT_SIZE            = Const.TEXT_SIZE_17
            commentText.textSize = TEXT_SIZE
        }

        textSize3.setOnClickListener {
            TEXT_SIZE            = Const.TEXT_SIZE_22
            commentText.textSize = TEXT_SIZE
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        log.d("save ${commentText.text.toString()} $TEXT_SIZE $TEXT_COLOR_ID")
        outState!!.putString("quoteText",commentText.text.toString())
        outState.putFloat("quoteSize",TEXT_SIZE)
        outState.putInt("quoteColor",TEXT_COLOR_ID)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        try{

            TEXT_SIZE =  savedInstanceState!!.getFloat("quoteSize",TEXT_SIZE)
            TEXT_COLOR_ID = savedInstanceState.getInt("quoteColor",TEXT_COLOR_ID)
            commentText.setText(savedInstanceState.getString("quoteText"))
            commentText.textSize = TEXT_SIZE
            commentText.setTextColor(ContextCompat.getColor(applicationContext,
                    Const.colorPalette.get(TEXT_COLOR_ID)!!.drawable))

        }catch (e:Exception){

        }
    }
    var photo:File? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        log.d("MainActivity -> OnactivityResult: req:${requestCode} res: ${resultCode} intent: ${data != null}" )
        if (requestCode == Const.SESSION_OUT || resultCode == Const.SESSION_OUT){
            setResult(Const.SESSION_OUT)
            finish()
        }else
        if (resultCode == Activity.RESULT_OK ){
            log.d("Rasm olishdan resultat keldi: ")
            var photos: List<String>? = null
            if(requestCode == Const.PICK_IMAGE || requestCode == Const.PICK_IMAGE) {

                if (data != null) {

                    photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS)

                }

                if (photos != null) {


                    listImage.add(PhotoUpload(Uri.fromFile(File(photos.get(0)))))
                    idImage = listImage.size - 1
                    if (imageAdapter == null) {

                        imageAdapter = PickedPhotoAdapter(this, object :AdapterClicker{
                            override fun click(position: Int) {
                                idImage = position

                            }

                            override fun data(data: String) {
                                //  uploadPhotoByUri(data,PickedPhotoAdapter.Photo.UPDATE)

                            }

                        }, listImage)

                        viewImage.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
                        viewImage.setHasFixedSize(true)
                        viewImage.adapter = imageAdapter

                    }else{

                        imageAdapter!!.swapItems(listImage)
                    }

                }
            }else if(requestCode == Const.PICK_AUDIO){
                val song:Song = data!!.getParcelableExtra(Const.SONG_PICKED)

//              uploadAudioByUri(song.songPath,PickedSongAdapter.Audio.UPLOAD)


                listMusic.add(song)
                idMusic = listMusic.size - 1
                if (songAdapter == null){

                    songAdapter = PickedSongAdapter(this,object :AdapterClicker{
                        override fun click(position: Int) {
                            idMusic = position

                        }

                        override fun data(data: String) {
//                                    uploadAudioByUri(data,PickedSongAdapter.Audio.UPDATE)

                        }

                    },listMusic)

                    viewMusic.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
                    viewMusic.setHasFixedSize(true)
                    viewMusic.adapter = songAdapter

                }else{
                    songAdapter!!.swapItems(listMusic)

                }
            }
        }else if(resultCode == Const.PICK_CROP_IMAGE && data != null){

            val resultUri:Uri = UCrop.getOutput(data)!!

            listImage.add(PhotoUpload(Uri.fromFile(File(resultUri.path))))
            idImage = listImage.size - 1
            if (imageAdapter == null) {

                imageAdapter = PickedPhotoAdapter(this, object :AdapterClicker{
                    override fun click(position: Int) {

                        idImage = position

                    }

                    override fun data(data: String) {
                        //uploadPhotoByUri(data,PickedPhotoAdapter.Photo.UPDATE)

                    }

                }, listImage)

                viewImage.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
                viewImage.setHasFixedSize(true)
                viewImage.adapter = imageAdapter

            }else{

                imageAdapter!!.swapItems(listImage)

            }

        }
    }

    override fun initProgress() {
        progressLay.visibility = View.VISIBLE

        disableAllElements()
    }

    override fun showProgress() {
    }

    override fun hideProgress() {
        progressLay.visibility = View.GONE
        enableAllElements()
    }


    fun View.showKeyboard() {
        this.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    fun View.hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }
    override fun onSuccess(from: String, result: String) {

        log.d("from $from result: $result")
        loadedImagesIds  = ArrayList()
        loadedAudioIds  = ArrayList()
        Functions.hideSoftKeyboard(this)

        commentText!!.hideKeyboard()
        setResult(Const.PICK_UNIVERSAL)
        this.finish()
    }

    override fun onFailure(from: String, message: String, erroCode: String) {
        log.d("fail from $from result: $message")

    }


    private fun disableAllElements() {

    }

    private fun enableAllElements() {

    }


    override fun onBackPressed() {
        val logicImage = if (imageAdapter == null) true else imageAdapter!!.list.size == loadedImagesIds.size
        val logicAudio = if (songAdapter == null)  true else songAdapter!!.list.size == loadedAudioIds.size

        if(!loading && logicImage && logicAudio){
            clearCache()
            super.onBackPressed()

        }else{
            quitDialog.show(supportFragmentManager, YesNoFragment.TAG)



        }


    }

    override fun onDestroy() {
        clearCache()
        super.onDestroy()
    }


    fun clearCache(){
        imageAdapter    = null
        songAdapter     = null
        loadedImagesIds = ArrayList()
        loadedAudioIds  = ArrayList()
        loading         = false
        commentText.hideKeyboard()
        Functions.hideSoftKeyboard(this)
    }


}