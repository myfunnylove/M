package locidnet.com.marvarid.ui.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import locidnet.com.marvarid.R
import locidnet.com.marvarid.model.DialogFragmentModel

/**
 * Created by macbookpro on 06.09.17.
 */
class YesNoFragment : DialogFragment() {

    var listener: DialogClickListener? = null

    companion object {
        var mInstance: YesNoFragment? = null
        val NO = 1
        val YES = 2
        val TAG = "yesnofragment"
        fun instance(dialogFragmentModel: DialogFragmentModel) : YesNoFragment {

            if (mInstance == null) mInstance = YesNoFragment()

            val bundle = Bundle()
            bundle.putString("title",dialogFragmentModel.title)
            bundle.putString("no",dialogFragmentModel.no)
            bundle.putString("yes",dialogFragmentModel.yes)
            mInstance!!.arguments = bundle

            return mInstance!!
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_dialog,container,false)

        val titleView = view.findViewById(R.id.title) as TextView
        val no = view.findViewById(R.id.no) as TextView
        val yes = view.findViewById(R.id.yes) as TextView

        try{
            titleView.text = arguments.getString("title")
            no.text = arguments.getString("no")
            yes.text = arguments.getString("yes")

        }catch (e:Exception){

        }
        no.findViewById(R.id.no).setOnClickListener {
            listener!!.click(NO)
        }

        yes.findViewById(R.id.yes).setOnClickListener {
            listener!!.click(YES)
        }

        return view
    }

    fun setDialogClickListener(dialogClickListener: DialogClickListener){
        listener = dialogClickListener
    }

    interface DialogClickListener{
        fun click(whichButton:Int)
    }
}