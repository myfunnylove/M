package locidnet.com.marvarid.ui.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.AppCompatImageView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_login_and_password.*
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.base.BaseFragment
import locidnet.com.marvarid.resources.utils.Functions
import locidnet.com.marvarid.resources.utils.log
import locidnet.com.marvarid.rest.Http

/**
 * Created by myfunnylove on 21.09.17.
 */
class BlockMeFragment : BaseFragment() {


    lateinit var emptyText:TextView


    companion object {
        var TAG: String = "BlockMeFragment"

        fun newInstance(bundle: Bundle): BlockMeFragment {

            val newsFragment = BlockMeFragment()
            val args = bundle

            newsFragment.arguments = args
            return newsFragment

        }
    }


    override fun getFragmentView(): Int = R.layout.empty_profile





    override fun init() {

        rootView.findViewById<TextView>(R.id.emptyText).setText(arguments.getString("text"))

        val avatar = rootView.findViewById<AppCompatImageView>(R.id.avatar)

        var photo = ""

        log.d("photo is ${arguments.getString("photo")}")




    }


}

