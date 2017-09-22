package locidnet.com.marvarid.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Michaelan on 6/18/2017.
 */
data class User (
        @SerializedName("user_id")
        var userId:String,
        @SerializedName("session")
        var session:String,
        @SerializedName("token")
        var token:String,
        @SerializedName("username")
        var userName:String,
        @SerializedName("password")
        var password:String,
        @SerializedName("gender")
        var gender:String, // nothing = 0, female = 1, male = 2
        @SerializedName("phoneOrMail")
        var phoneOrMail:String,
        @SerializedName("smsCode")
        var smsCode:String,
        @SerializedName("first_name")
        var first_name:String,
        @SerializedName("last_name")
        var last_name:String,
        @SerializedName("profilPhoto")
        var profilPhoto:String = "",
        @SerializedName("signType")
        var signType:Int ,// facebook = 0, vkontakte = 1,sms = 2

        @SerializedName("phone")
        var userPhone:String,
        @SerializedName("mail")
        var userMail:String,
        @SerializedName("close")
        var close:Int = 0 // close 1 open 0

)

data class UserInfo(@SerializedName("user")      var user:UserData)

data class UserData(@SerializedName("info")  var info:Info,
                    @SerializedName("count") var count:Count,
                    @SerializedName("follow") var follow:Int,
                    @SerializedName("request") var request:Int,
                    @SerializedName("block_me") var block_me:String,
                    @SerializedName("block_it") var block_it:String

                    )

data class Info(@SerializedName("photo_org")  var photoOrg:String,
                @SerializedName("username")  var username:String,
                @SerializedName("name")  var name:String,
                @SerializedName("gender")  var gender:String,
                @SerializedName("mail")      var mail:String,
                @SerializedName("phone")   var phone:String,
                @SerializedName("close")     var close:Int,
                @SerializedName("user_id")   var user_id:String
                )

data class Count(@SerializedName("flwrs")  var followersCount:String,
                 @SerializedName("flwngs")  var followingCount:String,
                 @SerializedName("posts")  var postCount:String)