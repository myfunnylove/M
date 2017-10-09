package locidnet.com.marvarid.resources.utils

import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.model.Color
import locidnet.com.marvarid.model.Complaints
import java.util.regex.Pattern

/**
 *
 * Created by Michaelan on 5/19/2017.
 *
 */

object Const {


    var TAG:      String ="DEMO_APP"
    val FEED_FR:  Int    = 0
    val SEARCH_FR:Int    = 1
    val UPLOAD_FR:Int    = 2
    val NOTIF_FR: Int    = 3
    val PROFIL_FR:Int    = 4
    val SIGN_PHONE_OR_MAIL_FR:Int = 5


    /*
    *
    * Feed TYPE
    *
    * */

    val FEED_IMAGE   = 1
    val FEED_QUOTE   = 2
    val FEED_AUDIO   = 3
    val FEED_FAILED  = -1


    val TO_VALUE = "VALUE_IS?"
    val SONG_PICKED = "PICKED_SONG"
    val PUBLISH_IMAGE = "publish_image"



    val TO_POSTS = -1
    val TO_FOLLOWERS = -2
    val TO_FOLLOWING = -3

    val PICK_IMAGE                   = 4
    val PICK_AUDIO                   = 5
    val PICK_QUOTE                   = 6
    val PICK_UNIVERSAL               = 7
    val CHANGE_AVATAR                = 8
    val PICK_CROP_IMAGE              = 9
    val SEARCH_USER                  = 10
    val FOLLOW                       = 11
    val PROFIL_PAGE                  = 12
    val REFRESH_FEED                 = 13
    val REFRESH_PROFILE_FEED         = 14
    val GO_COMMENT_ACTIVITY          = 15
    val SESSION_OUT                  = 16
    val PROFIL_PAGE_OTHER            = 17
    val GO_PLAY_LIST                 = 18
    val FROM_SEARCH_TO_PROFIL        = 19
    val REFRESH_NOTIFICATION         = 20
    val PUSH_LIST                    = 21
    val TO_FAIL                      = -1
    val QUIT: Int                    = 22
    val FORGOT_PASS                  = 23
    val GET_15_POST                  = 24
    val GO_SETTINGS                  = 25


    val FROM_MAIN_ACTIVITY = 100


    val colorPalette = hashMapOf(
            0   to Color(R.color.material_light_black,""),
            5   to Color(R.color.material_red_300,""),
            6   to Color(R.color.material_red_400,""),
            7   to Color(R.color.material_red_500,""),
            8   to Color(R.color.material_red_600,""),
            9   to Color(R.color.material_red_700,""),
            10  to Color(R.color.material_red_800,""),
            11  to Color(R.color.material_red_900,""),
            13  to Color(R.color.material_red_accent_200,""),
            14  to Color(R.color.material_red_accent_400,""),
            15  to Color(R.color.material_red_accent_700,""),
            18  to Color(R.color.material_pink_200,""),
            19  to Color(R.color.material_pink_300,""),
            20  to Color(R.color.material_pink_400,""),
            21  to Color(R.color.material_pink_500,""),
            22  to Color(R.color.material_pink_600,""),
            23  to Color(R.color.material_pink_700,""),
            24  to Color(R.color.material_pink_800,""),
            25  to Color(R.color.material_pink_900,""),
            26  to Color(R.color.material_pink_accent_100,""),
            27  to Color(R.color.material_pink_accent_200,""),
            28  to Color(R.color.material_pink_accent_400,""),
            29  to Color(R.color.material_pink_accent_700,""),
            33  to Color(R.color.material_purple_300,""),
            34  to Color(R.color.material_purple_400,""),
            35  to Color(R.color.material_purple_500,""),
            36  to Color(R.color.material_purple_600,""),
            37  to Color(R.color.material_purple_700,""),
            38  to Color(R.color.material_purple_800,""),
            39  to Color(R.color.material_purple_900,""),
            40  to Color(R.color.material_purple_accent_100,""),
            41  to Color(R.color.material_purple_accent_200,""),
            42  to Color(R.color.material_purple_accent_400,""),
            43  to Color(R.color.material_purple_accent_700,""),
            47  to Color(R.color.material_deep_purple_300,""),
            48  to Color(R.color.material_deep_purple_400,""),
            49  to Color(R.color.material_deep_purple_500,""),
            50  to Color(R.color.material_deep_purple_600,""),
            51  to Color(R.color.material_deep_purple_700,""),
            52  to Color(R.color.material_deep_purple_800,""),
            53  to Color(R.color.material_deep_purple_900,""),
            54  to Color(R.color.material_deep_purple_accent_100,""),
            55  to Color(R.color.material_deep_purple_accent_200,""),
            56  to Color(R.color.material_deep_purple_accent_400,""),
            57  to Color(R.color.material_deep_purple_accent_700,""),
            61  to Color(R.color.material_indigo_300,""),
            62  to Color(R.color.material_indigo_400,""),
            63  to Color(R.color.material_indigo_500,""),
            64  to Color(R.color.material_indigo_600,""),
            65  to Color(R.color.material_indigo_700,""),
            66  to Color(R.color.material_indigo_800,""),
            67  to Color(R.color.material_indigo_900,""),
            68  to Color(R.color.material_indigo_accent_100,""),
            69  to Color(R.color.material_indigo_accent_200,""),
            70  to Color(R.color.material_indigo_accent_400,""),
            71  to Color(R.color.material_indigo_accent_700,""),
            75  to Color(R.color.material_blue_300,""),
            76  to Color(R.color.material_blue_400,""),
            77  to Color(R.color.material_blue_500,""),
            78  to Color(R.color.material_blue_600,""),
            79  to Color(R.color.material_blue_700,""),
            80  to Color(R.color.material_blue_800,""),
            81  to Color(R.color.material_blue_900,""),
            82  to Color(R.color.material_blue_accent_100,""),
            83  to Color(R.color.material_blue_accent_200,""),
            84  to Color(R.color.material_blue_accent_400,""),
            85  to Color(R.color.material_blue_accent_700,""),
            89  to Color(R.color.material_light_blue_300,""),
            90  to Color(R.color.material_light_blue_400,""),
            91  to Color(R.color.material_light_blue_500,""),
            92  to Color(R.color.material_light_blue_600,""),
            93  to Color(R.color.material_light_blue_700,""),
            94  to Color(R.color.material_light_blue_800,""),
            95  to Color(R.color.material_light_blue_900,""),
            96  to Color(R.color.material_light_blue_accent_100,""),
            97  to Color(R.color.material_light_blue_accent_200,""),
            98  to Color(R.color.material_light_blue_accent_400,""),
            99  to Color(R.color.material_light_blue_accent_700,""),
            103 to Color(R.color.material_cyan_300,""),
            104 to Color(R.color.material_cyan_400,""),
            105 to Color(R.color.material_cyan_500,""),
            106 to Color(R.color.material_cyan_600,""),
            107 to Color(R.color.material_cyan_700,""),
            108 to Color(R.color.material_cyan_800,""),
            109 to Color(R.color.material_cyan_900,""),
            113 to Color(R.color.material_cyan_accent_700,""),
            118 to Color(R.color.material_teal_400,""),
            119 to Color(R.color.material_teal_500,""),
            120 to Color(R.color.material_teal_600,""),
            121 to Color(R.color.material_teal_700,""),
            122 to Color(R.color.material_teal_800,""),
            123 to Color(R.color.material_teal_900,""),
            126 to Color(R.color.material_teal_accent_400,""),
            127 to Color(R.color.material_teal_accent_700,""),
            131 to Color(R.color.material_green_300,""),
            132 to Color(R.color.material_green_400,""),
            133 to Color(R.color.material_green_500,""),
            134 to Color(R.color.material_green_600,""),
            135 to Color(R.color.material_green_700,""),
            136 to Color(R.color.material_green_800,""),
            137 to Color(R.color.material_green_900,""),
            139 to Color(R.color.material_green_accent_200,""),
            140 to Color(R.color.material_green_accent_400,""),
            141 to Color(R.color.material_green_accent_700,""),
            145 to Color(R.color.material_light_green_300,""),
            146 to Color(R.color.material_light_green_400,""),
            147 to Color(R.color.material_light_green_500,""),
            148 to Color(R.color.material_light_green_600,""),
            149 to Color(R.color.material_light_green_700,""),
            150 to Color(R.color.material_light_green_800,""),
            151 to Color(R.color.material_light_green_900,""),
            154 to Color(R.color.material_light_green_accent_400,""),
            155 to Color(R.color.material_light_green_accent_700,""),
            165 to Color(R.color.material_lime_900,""),


            202 to Color(R.color.material_orange_400,""),
            203 to Color(R.color.material_orange_500,""),
            204 to Color(R.color.material_orange_600,""),
            205 to Color(R.color.material_orange_700,""),
            206 to Color(R.color.material_orange_800,""),
            207 to Color(R.color.material_orange_900,""),
            209 to Color(R.color.material_orange_accent_200,""),
            210 to Color(R.color.material_orange_accent_400,""),
            211 to Color(R.color.material_orange_accent_700,""),
            217 to Color(R.color.material_deep_orange_500,""),
            218 to Color(R.color.material_deep_orange_600,""),
            219 to Color(R.color.material_deep_orange_700,""),
            220 to Color(R.color.material_deep_orange_800,""),
            221 to Color(R.color.material_deep_orange_900,""),
            223 to Color(R.color.material_deep_orange_accent_200,""),
            224 to Color(R.color.material_deep_orange_accent_400,""),
            225 to Color(R.color.material_deep_orange_accent_700,""),
            230 to Color(R.color.material_brown_400,""),
            231 to Color(R.color.material_brown_500,""),
            232 to Color(R.color.material_brown_600,""),
            233 to Color(R.color.material_brown_700,""),
            234 to Color(R.color.material_brown_800,""),
            235 to Color(R.color.material_brown_900,""),
            242 to Color(R.color.material_grey_600,""),
            243 to Color(R.color.material_grey_700,""),
            244 to Color(R.color.material_grey_800,""),
            245 to Color(R.color.material_grey_900,""),
            250 to Color(R.color.material_blue_grey_400,""),
            251 to Color(R.color.material_blue_grey_500,""),
            252 to Color(R.color.material_blue_grey_600,""),
            253 to Color(R.color.material_blue_grey_700,""),
            254 to Color(R.color.material_blue_grey_800,""),
            255 to Color(R.color.material_blue_grey_900,""))





    /*
    *
    * TEST QUOTE SETTINGS
    *
    *
    * */

    val TEXT_SIZE_DEFAULT = 16f
    val TEXT_SIZE_17 = 19f
    val TEXT_SIZE_22 = 22f


    val unselectedTabs = hashMapOf<Int,Int>(
            FEED_FR to R.drawable.feed,
            SEARCH_FR to R.drawable.search,
            NOTIF_FR to R.drawable.notification,
            PROFIL_FR to R.drawable.account
    )

    val selectedTabs = hashMapOf<Int,Int>(
            FEED_FR to R.drawable.feed_select,
            SEARCH_FR to R.drawable.search_select,
            NOTIF_FR to R.drawable.notification_select,
            PROFIL_FR to R.drawable.account_select
    )


    /*PUSH TITLES*/
    object Push{
        val LIKE = 1
        val COMMENT = 2
        val FOLLOW = 3
        val REQUESTED = 4
        val OTHER = 5
    }


    val VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)
    val ONLY_DIGITS = Pattern.compile("[0-9]{9}")

    val complaints:ArrayList<Complaints> = arrayListOf(
            Complaints(Base.get.resources.getString(R.string.complaintSpam),1),
            Complaints(Base.get.resources.getString(R.string.complaintAbuse),2),
            Complaints(Base.get.resources.getString(R.string.compaintAdult),3),
            Complaints(Base.get.resources.getString(R.string.complaintDrug),4),
            Complaints(Base.get.resources.getString(R.string.complaintChild),5),
            Complaints(Base.get.resources.getString(R.string.complaintViolence),6),
            Complaints(Base.get.resources.getString(R.string.complaintSuicide),7)
    )


    object IMAGE{
        val LOW = "images/300/"
        val MEDIUM = "images/640/"
        val ORIGINAL  = "images/orig/"
    }

    object AUDIO{
        val LOW = "audios/low/"
        val MEDIUM = "audios/middle/"
        val ORIGINAL  = "audios/high/"
    }
}