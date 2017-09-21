package locidnet.com.marvarid.ui.fragment

import android.os.Bundle
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.AppCompatImageView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.BaseFragment
import locidnet.com.marvarid.connectors.GoNext
import locidnet.com.marvarid.resources.utils.Const
import locidnet.com.marvarid.resources.utils.log
import kotlin.properties.Delegates

/**
 * Created by Michaelan on 5/19/2017.
 */
class UploadFragment()  : BaseFragment(){




    override fun getFragmentView(): Int {
      return R.layout.fragment_upload
    }

    override fun init() {


    }
    companion object {
        fun newInstance(): UploadFragment {
            val newsFragment = UploadFragment()
            val args = Bundle()

            newsFragment.arguments = args
            return newsFragment
        }
    }
    var connectActivity:GoNext?     = null
    fun connect(connActivity: GoNext){
        connectActivity   = connActivity

    }
}