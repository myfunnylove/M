package locidnet.com.marvarid.model

import com.google.gson.annotations.SerializedName

/**
 * Created by myfunnylove on 03.10.17.
 */
data class RecommededPosts(@SerializedName("posts") var posts:ArrayList<RecPost>)

data class RecPost(@SerializedName("user_id")var userId:String,
                   @SerializedName("photo") var photo:String,
                   @SerializedName("username") var username:String,
                   @SerializedName("post_id") var postId:String
                   )