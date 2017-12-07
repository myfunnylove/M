package locidnet.com.marvarid.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 *
 * Created by Michaelan on 6/27/2017.
 *
 **/

data class PostList(@SerializedName("posts")     var posts:ArrayList<Posts>
                    )

data class Posts(      @SerializedName("id")     var id:String,
                       @SerializedName("quote")  var quote:Quote,
//                       @SerializedName("audios") var audios:ArrayList<Audio>,
                       @SerializedName("images") var images:ArrayList<Image>,
                       @SerializedName("like")   var like:String,
                       @SerializedName("likes")  var likes:String,
                       @SerializedName("comments")  var comments:String,
                       @SerializedName("time")   var time:String,
                       @SerializedName("user")   var user:PostUser,
                       @SerializedName("type")   var type:String = "post") // if post will be ad, type should be "ad"



data class Image(@SerializedName("photo_id")     var photoId:String,
                 @SerializedName("post_id")      var postId: String,
                 @SerializedName("image_orig")   var image: String,
                 @SerializedName("width")        var width:String,
                 @SerializedName("height")    var height:String,
                 @SerializedName("resolution") var resolution:Resolution

                 )

data class PostUser(@SerializedName("user_id") var userId:String, @SerializedName("username") var username:String, @SerializedName("photo_150") var photo:String)

data class Resolution(@SerializedName("porig") var orig:String,
                      @SerializedName("p640") var r640:String,
                      @SerializedName("p320") var r320:String)