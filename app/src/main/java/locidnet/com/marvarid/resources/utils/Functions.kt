package locidnet.com.marvarid.resources.utils

import android.Manifest.*
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.text.*
import android.text.method.NumberKeyListener
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.facebook.drawee.generic.GenericDraweeHierarchy
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.generic.RoundingParams
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.model.Song
import locidnet.com.marvarid.model.UserInfo
import locidnet.com.marvarid.resources.CircleProgressDrawable
import locidnet.com.marvarid.rest.Http
import locidnet.com.marvarid.ui.activity.FollowActivity
import locidnet.com.marvarid.ui.fragment.ProfileFragment
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File


/**
 * Created by Michaelan on 5/26/2017.
 */
object Functions {

    private var screenWidth = 0
    private var screenHeight = 0

    val TAG:String = "Functions-> "
    fun DPtoPX(dp:Float,ctx: Context):Int{
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,ctx.resources.displayMetrics).toInt()
    }
    fun SPtoPX(dp:Float,ctx: Context):Int{
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,dp,ctx.resources.displayMetrics).toInt()
    }
    fun DPtoSP(dp:Float,ctx: Context):Float{
        return ((DPtoPX(dp,ctx)) / SPtoPX(dp,ctx).toFloat()).toFloat()
    }



    fun getSongList(ctx:Context):ArrayList<Song>{
        var songList:ArrayList<Song> = ArrayList()
        var musicCursor: Cursor? = null
        try{
            val contentResolver = ctx.contentResolver
            val musicUri  = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            musicCursor = contentResolver.query(musicUri,null,null,null,null)

            if (musicCursor != null && musicCursor.moveToFirst()){




                songList = ArrayList<Song>()
                val titleColumn:Int = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)

                val idColumn:Int = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val artistColumn:Int = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)

                val sizeColumn:Int = musicCursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val durationColumn:Int = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val path:Int = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                val size = musicCursor.getColumnIndex(MediaStore.Audio.Media.SIZE)

                val length = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val dataAdded = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

                do{

                    log.d("size : ${musicCursor.getLong(size)} length: ${musicCursor.getLong(length)}")
                    songList.add(
                            Song(
                                    musicCursor.getLong(idColumn),
                                    musicCursor.getString(titleColumn),
                                    musicCursor.getString(artistColumn),
                                    musicCursor.getLong(durationColumn),
                                    musicCursor.getLong(sizeColumn),

                                    false,

                                            musicCursor.getString(path),

                                    0,
                                            musicCursor.getLong(dataAdded)


                                    ))
                }while (musicCursor.moveToNext())
            }
        }catch (e:Exception){
                log.d(e.toString())
            return ArrayList()
        }finally {
            musicCursor!!.close()
            return songList
        }
    }


    fun Long.generateSongDuration():String{
        return "${(this % 60000) / 1000}:${this / 60000}"
    }


    fun checkPermissions(ctx : Activity):Boolean{
        val MULTIPLE_PERMISSIONS = 10
        val permissions = arrayOf(
                permission.CAMERA,
//                Manifest.permission.READ_PHONE_STATE,
                permission.READ_EXTERNAL_STORAGE,
                permission.WRITE_EXTERNAL_STORAGE

        )
        var res = 0

        val listPermissionNeeded = ArrayList<String>()

        for (i in permissions){
            res = ContextCompat.checkSelfPermission(Base.get,i)

            if (res != PackageManager.PERMISSION_GRANTED){
                listPermissionNeeded.add(i)
            }
        }

        if (listPermissionNeeded.isNotEmpty())
        {
            val arr:Array<String> = listPermissionNeeded.toTypedArray()
            ActivityCompat.requestPermissions(ctx,arr,MULTIPLE_PERMISSIONS)
            return false
        }else{
            return true
        }
    }



    fun getFile(path:File):MultipartBody.Part?{

        log.d("getFile ${path.absolutePath}")
        val reqFile = RequestBody.create(MediaType.parse("file"),path)


        return MultipartBody.Part.createFormData("upload",path.name,reqFile)
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }


    fun show(str:String){
        Toast.makeText(Base.get,str, Toast.LENGTH_SHORT).show()

    }


    fun getScreenHeight(c: Context): Int {
        if (screenHeight === 0) {
            val wm = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val size = Point()
            display.getSize(size)
            screenHeight = size.y
        }

        return screenHeight
    }

    fun getScreenWidth(c: Context): Int {
        if (screenWidth == 0) {
            val wm = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val size = Point()
            display.getSize(size)
            screenWidth = size.x
        }

        return screenWidth
    }

    fun isAndroid5(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }


    @Throws(JSONException::class)
    fun jsonToBundle(jsonObject: JSONObject): Bundle {
        val bundle = Bundle()
        val iter = jsonObject.keys()
        while (iter.hasNext()) {
            val key = iter.next() as String
            val value = jsonObject.getString(key)
            bundle.putString(key, value)
        }
        return bundle
    }

    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager.isActive) {
            if (activity.currentFocus != null) {
                inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
            }
        }
    }

    var EditCardWatcher: TextWatcher = object : TextWatcher {
        // int len = 0;
        internal var text = ""
        internal var editingBefore = false
        internal var editingOnChanged = false
        internal var editingAfter = false

        override fun afterTextChanged(str: Editable) {
            if (!editingAfter && editingBefore && editingOnChanged) {
                editingAfter = true
                str.replace(0, str.length, text)
                // str.append(text);


                editingBefore = false
                editingOnChanged = false
                editingAfter = false
            }
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                       after: Int) {
            if (!editingBefore) {
                editingBefore = true
                // text = clearText(s.toString());

            }
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                   count: Int) {
            val d = " "
            if (!editingOnChanged && editingBefore) {
                editingOnChanged = true
                text = clearText(s.toString())
                if (text.length > 4 && text.length <= 8) {
                    text = "${text.substring(0, 4)}$d${text.substring(4, text.length)}"
                } else if (text.length > 8 && text.length <= 12) {

                    text =   "${text.substring(0, 4)}$d${text.substring(4, 8)}$d${text.substring(8, text.length)}"
                } else if (text.length > 12 && text.length <= 16) {
                    text = "${text.substring(0, 4)}$d${text.substring(4, 8)}$d${text.substring(8, 12)}$d${text.substring(12, text.length)}"
                }

            }
        }
    }

    fun clearText(s: String): String {
        var s = s
        s = s.replace("-".toRegex(), "")
        s = s.replace(" ".toRegex(), "")
        s = s.trim { it <= ' ' }
        return s
    }

    fun clearText(edit: EditText): String {
        var s = edit.text.toString()
        s = s.replace("-".toRegex(), "")
        s = s.replace(" ".toRegex(), "")
        s = s.trim { it <= ' ' }
        return s
    }

    fun clearEdit(edit: EditText): String {
        var s = edit.text.toString()
        s = s.replace("-".toRegex(), "")
        s = s.replace(" ".toRegex(), "")
        s = s.trim { it <= ' ' }
        return s
    }

    var EditCardKey: NumberKeyListener = object : NumberKeyListener() {

        override fun getInputType(): Int {
            return InputType.TYPE_CLASS_NUMBER
        }

        override fun getAcceptedChars(): CharArray {
            return charArrayOf(' ', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
        }
    }

    fun getString(str:Int):String = Base.get.resources.getString(str)


    fun selectFollowType(userInfo:UserInfo) :String{
        var result = ""
        if(userInfo.user.block_me == "0"){

            if(     userInfo.user.info.close == 1 &&
                    userInfo.user.follow == 0 &&
                    userInfo.user.request == 0){

                result = ProfileFragment.CLOSE

            }else if(userInfo.user.info.close == 1 &&
                     userInfo.user.follow == 0 &&
                     userInfo.user.request == 1){

                result = ProfileFragment.REQUEST

            }else if (userInfo.user.info.close == 1 &&
                      userInfo.user.follow == 1 &&
                      userInfo.user.request == 0){

                result = ProfileFragment.UN_FOLLOW

            }else if (userInfo.user.info.close == 0 &&
                      userInfo.user.follow == 0 &&
                      userInfo.user.request == 1){

                result = ProfileFragment.FOLLOW


            }else if (userInfo.user.info.close == 0 &&
                      userInfo.user.follow == 1 &&
                      userInfo.user.request == 0){

                result = ProfileFragment.UN_FOLLOW

            }else{
                result = ProfileFragment.FOLLOW

            }

        }else{
            result = "-1"
        }

        return result
    }

    @JvmStatic
    var avatar: Drawable? =null

    @JvmStatic
    var default: Drawable? =null

    fun checkImageUrl(photo:String?):String? {

        if (photo == null) return ""

        if (photo.isNullOrEmpty())  return ""

        if (photo.startsWith("http"))
            return photo
        else
            return Http.BASE_URL+photo
    }


    fun getAvatarHierarchy(): GenericDraweeHierarchy {
        return GenericDraweeHierarchyBuilder.newInstance(Base.get.resources)
                .setFailureImage(Base.get.resources.getDrawable(R.drawable.default_profile_photo_circle))
                .setPlaceholderImage(Base.get.resources.getDrawable(R.drawable.default_profile_photo_circle))
                .setRoundingParams(getCicrleOptions())
                .build()

    }


    fun getPostPhotoHierarchy():GenericDraweeHierarchy{
        val  progressBarDrawable =  CircleProgressDrawable()

            progressBarDrawable.setColor(Base.get.resources.getColor(R.color.material_grey_300))
            progressBarDrawable.setBackgroundColor(Base.get.resources.getColor(R.color.material_grey_100))
            progressBarDrawable
                .setRadius(30);
        return GenericDraweeHierarchyBuilder.newInstance(Base.get.resources)
                .setProgressBarImage(progressBarDrawable)

                .setFailureImage(VectorDrawableCompat.create(Base.get.resources, R.drawable.image, null))
                .build()


    }


    fun getBackgroundOptions():GenericDraweeHierarchy{
        val  progressBarDrawable =  CircleProgressDrawable()

        progressBarDrawable.setColor(Base.get.resources.getColor(R.color.material_grey_300))
        progressBarDrawable.setBackgroundColor(Base.get.resources.getColor(R.color.material_grey_100))
        progressBarDrawable
                .setRadius(30);
        return GenericDraweeHierarchyBuilder.newInstance(Base.get.resources)
                .setProgressBarImage(progressBarDrawable)

                .setFailureImage(Base.get.resources.getDrawable(R.drawable.default_profile_photo))
                .build()


    }
    fun getCicrleOptions(): RoundingParams = RoundingParams.asCircle()


    fun getDeviceName(): String? {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else capitalize(manufacturer) + " " + model
    }

    private fun capitalize(str: String): String? {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true

        val phrase = StringBuilder()
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c))
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase.append(c)
        }

        return phrase.toString()
    }

    fun getScreenWidth(): Int = Resources.getSystem().getDisplayMetrics().widthPixels

    fun getScreenHeight(): Int = Resources.getSystem().getDisplayMetrics().heightPixels
    fun getCacheHeader(): String {
        val infoInternet: NetworkInfo?
                = (Base.get.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo

        return  if(infoInternet != null && infoInternet.isConnected)
            "public, max-age=2419200"
        else
            "public, only-if-cached, max-stale=2419200"
    }




}