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
            1   to Color(R.color.material_red_300,""),
            2   to Color(R.color.material_red_400,""),
            3   to Color(R.color.material_red_500,""),
            4   to Color(R.color.material_red_600,""),
            5   to Color(R.color.material_red_700,""),
            6   to Color(R.color.material_red_800,""),
            7   to Color(R.color.material_red_900,""),
            8   to Color(R.color.material_red_accent_200,""),
            9   to Color(R.color.material_red_accent_400,""),
            10  to Color(R.color.material_red_accent_700,""),
            11  to Color(R.color.material_pink_200,""),
            12  to Color(R.color.material_pink_300,""),
            13  to Color(R.color.material_pink_400,""),
            14  to Color(R.color.material_pink_500,""),
            15  to Color(R.color.material_pink_600,""),
            16  to Color(R.color.material_pink_700,""),
            17  to Color(R.color.material_pink_800,""),
            18  to Color(R.color.material_pink_900,""),
            19  to Color(R.color.material_pink_accent_100,""),
            20  to Color(R.color.material_pink_accent_200,""),
            21  to Color(R.color.material_pink_accent_400,""),
            22  to Color(R.color.material_pink_accent_700,""),
            23  to Color(R.color.material_purple_300,""),
            24  to Color(R.color.material_purple_400,""),
            25  to Color(R.color.material_purple_500,""),
            26  to Color(R.color.material_purple_600,""),
            27  to Color(R.color.material_purple_700,""),
            28  to Color(R.color.material_purple_800,""),
            29  to Color(R.color.material_purple_900,""),
            30  to Color(R.color.material_purple_accent_100,""),
            31  to Color(R.color.material_purple_accent_200,""),
            32  to Color(R.color.material_purple_accent_400,""),
            33  to Color(R.color.material_purple_accent_700,""),
            34  to Color(R.color.material_deep_purple_300,""),
            35  to Color(R.color.material_deep_purple_400,""),
            36  to Color(R.color.material_deep_purple_500,""),
            37  to Color(R.color.material_deep_purple_600,""),
            38  to Color(R.color.material_deep_purple_700,""),
            39  to Color(R.color.material_deep_purple_800,""),
            40  to Color(R.color.material_deep_purple_900,""),
            41  to Color(R.color.material_deep_purple_accent_100,""),
            42  to Color(R.color.material_deep_purple_accent_200,""),
            43  to Color(R.color.material_deep_purple_accent_400,""),
            44  to Color(R.color.material_deep_purple_accent_700,""),
            45  to Color(R.color.material_indigo_300,""),
            46  to Color(R.color.material_indigo_400,""),
            47  to Color(R.color.material_indigo_500,""),
            48  to Color(R.color.material_indigo_600,""),
            49  to Color(R.color.material_indigo_700,""),
            50  to Color(R.color.material_indigo_800,""),
            51  to Color(R.color.material_indigo_900,""),
            52  to Color(R.color.material_indigo_accent_100,""),
            53  to Color(R.color.material_indigo_accent_200,""),
            54  to Color(R.color.material_indigo_accent_400,""),
            55  to Color(R.color.material_indigo_accent_700,""),
            56  to Color(R.color.material_blue_300,""),
            57  to Color(R.color.material_blue_400,""),
            58  to Color(R.color.material_blue_500,""),
            59  to Color(R.color.material_blue_600,""),
            60  to Color(R.color.material_blue_700,""),
            61  to Color(R.color.material_blue_800,""),
            62  to Color(R.color.material_blue_900,""),
            63  to Color(R.color.material_blue_accent_100,""),
            64  to Color(R.color.material_blue_accent_200,""),
            65  to Color(R.color.material_blue_accent_400,""),
            66  to Color(R.color.material_blue_accent_700,""),
            67  to Color(R.color.material_light_blue_300,""),
            68  to Color(R.color.material_light_blue_400,""),
            69  to Color(R.color.material_light_blue_500,""),
            70  to Color(R.color.material_light_blue_600,""),
            71  to Color(R.color.material_light_blue_700,""),
            72  to Color(R.color.material_light_blue_800,""),
            73  to Color(R.color.material_light_blue_900,""),
            74  to Color(R.color.material_light_blue_accent_100,""),
            75  to Color(R.color.material_light_blue_accent_200,""),
            76  to Color(R.color.material_light_blue_accent_400,""),
            77  to Color(R.color.material_light_blue_accent_700,""),
            78  to Color(R.color.material_cyan_300,""),
            79  to Color(R.color.material_cyan_400,""),
            80  to Color(R.color.material_cyan_500,""),
            81  to Color(R.color.material_cyan_600,""),
            82  to Color(R.color.material_cyan_700,""),
            83  to Color(R.color.material_cyan_800,""),
            84  to Color(R.color.material_cyan_900,""),
            85  to Color(R.color.material_cyan_accent_700,""),
            86  to Color(R.color.material_teal_400,""),
            87  to Color(R.color.material_teal_500,""),
            88  to Color(R.color.material_teal_600,""),
            89  to Color(R.color.material_teal_700,""),
            90  to Color(R.color.material_teal_800,""),
            91  to Color(R.color.material_teal_900,""),
            92  to Color(R.color.material_teal_accent_400,""),
            93  to Color(R.color.material_teal_accent_700,""),
            94  to Color(R.color.material_green_300,""),
            95  to Color(R.color.material_green_400,""),
            96  to Color(R.color.material_green_500,""),
            97  to Color(R.color.material_green_600,""),
            98  to Color(R.color.material_green_700,""),
            99  to Color(R.color.material_green_800,""),
            100 to Color(R.color.material_green_900,""),
            101 to Color(R.color.material_green_accent_200,""),
            102 to Color(R.color.material_green_accent_400,""),
            103 to Color(R.color.material_green_accent_700,""),
            104 to Color(R.color.material_light_green_300,""),
            105 to Color(R.color.material_light_green_400,""),
            106 to Color(R.color.material_light_green_500,""),
            107 to Color(R.color.material_light_green_600,""),
            108 to Color(R.color.material_light_green_700,""),
            109 to Color(R.color.material_light_green_800,""),
            110 to Color(R.color.material_light_green_900,""),
            111 to Color(R.color.material_light_green_accent_400,""),
            112 to Color(R.color.material_light_green_accent_700,""),
            113 to Color(R.color.material_lime_900,""),
            114 to Color(R.color.material_orange_400,""),
            115 to Color(R.color.material_orange_500,""),
            116 to Color(R.color.material_orange_600,""),
            117 to Color(R.color.material_orange_700,""),
            118 to Color(R.color.material_orange_800,""),
            119 to Color(R.color.material_orange_900,""),
            120 to Color(R.color.material_orange_accent_200,""),
            121 to Color(R.color.material_orange_accent_400,""),
            122 to Color(R.color.material_orange_accent_700,""),
            123 to Color(R.color.material_deep_orange_500,""),
            124 to Color(R.color.material_deep_orange_600,""),
            125 to Color(R.color.material_deep_orange_700,""),
            126 to Color(R.color.material_deep_orange_800,""),
            127 to Color(R.color.material_deep_orange_900,""),
            128 to Color(R.color.material_deep_orange_accent_200,""),
            129 to Color(R.color.material_deep_orange_accent_400,""),
            130 to Color(R.color.material_deep_orange_accent_700,""),
            131 to Color(R.color.material_brown_400,""),
            132 to Color(R.color.material_brown_500,""),
            133 to Color(R.color.material_brown_600,""),
            134 to Color(R.color.material_brown_700,""),
            135 to Color(R.color.material_brown_800,""),
            136 to Color(R.color.material_brown_900,""),
            137 to Color(R.color.material_grey_600,""),
            138 to Color(R.color.material_grey_700,""),
            139 to Color(R.color.material_grey_800,""),
            140 to Color(R.color.material_grey_900,""),
            141 to Color(R.color.material_blue_grey_400,""),
            142 to Color(R.color.material_blue_grey_500,""),
            143 to Color(R.color.material_blue_grey_600,""),
            144 to Color(R.color.material_blue_grey_700,""),
            145 to Color(R.color.material_blue_grey_800,""),
            146 to Color(R.color.material_blue_grey_900,""))





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