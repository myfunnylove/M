package locidnet.com.marvarid.model

import com.google.gson.annotations.SerializedName

/**
 * Created by macbookpro on 17.09.17.
 */

data class PushList(@SerializedName("list") var pushes:ArrayList<Push>)

data class Push (@SerializedName("type") var type:Int, //  1 - like,2 - comment,3 - follow,4 - request
                 @SerializedName("time") var time:String,
                 @SerializedName("user") var user:PushUser,
                 @SerializedName("actions") var actions:Actions,
                 @SerializedName("action") var action:Action
                 )

data class Actions(@SerializedName("followIt") var followIt:String,
                   @SerializedName("requestIt") var requestIt:String,
                   @SerializedName("blockMe") var blockMe:String)

data class Action(@SerializedName("actionID") var actionID:String,
                   @SerializedName("actionPhoto") var actionPhoto:String
                   )


data class PushUser(@SerializedName("userID") var userId:String,
                @SerializedName("userName") var userName:String,
                @SerializedName("userPhoto") var userPhoto:String)

