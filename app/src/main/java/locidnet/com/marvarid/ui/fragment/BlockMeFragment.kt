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


    override fun getFragmentView(): Int = R.layout.user_profil_header





    override fun init() {
        rootView.findViewById<View>(R.id.follow).visibility = View.GONE
        rootView.findViewById<View>(R.id.progressUpdateAvatar).visibility = View.GONE
        rootView.findViewById<ViewGroup>(R.id.postsLay).visibility = View.GONE
        rootView.findViewById<View>(R.id.playlist).visibility = View.GONE
        rootView.findViewById<ViewGroup>(R.id.closedProfilLay).visibility = View.VISIBLE
        val text = rootView.findViewById<TextView>(R.id.emptyText)

        text.text = Functions.getString(R.string.profil_blocked_me_title)

        val avatar = rootView.findViewById<AppCompatImageView>(R.id.avatar)

        var photo = ""

        log.d("photo is ${arguments.getString("photo")}")

        if (!arguments.getString("photo").isNullOrEmpty())
            photo = arguments.getString("photo")



        Glide.with(this)
                .load(photo)
                .apply(RequestOptions()
                        .circleCrop()
                        .fallback(ColorDrawable(Color.BLACK))
                        .error(VectorDrawableCompat.create(resources,R.drawable.account,activity.theme))
                        .placeholder(ColorDrawable(Color.GRAY)))
                .into(avatar)
    }
}