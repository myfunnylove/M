package locidnet.com.marvarid.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ProgressBar
import locidnet.com.marvarid.R
import locidnet.com.marvarid.resources.utils.Toaster

/**
 * Created by macbookpro on 06.09.17.
 */
class ChangePassFragment : DialogFragment() {

    var listener: DialogClickListener? = null
    companion object {
        var mInstance: ChangePassFragment? = null

        val TAG = "ChangePassFragment"
        fun instance() : ChangePassFragment {

            if (mInstance == null) mInstance = ChangePassFragment()

            return mInstance!!
        }

        fun instance(bundle: Bundle) : ChangePassFragment {

            if (mInstance == null) mInstance = ChangePassFragment()
            mInstance!!.arguments = bundle
            return mInstance!!
        }
    }

    lateinit var oldPass: TextInputEditText
    lateinit var newPass: TextInputEditText
    lateinit var newPassAgain: TextInputEditText
    lateinit var change: Button
    lateinit var progress: ProgressBar
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_dialog_change_password,container,false)

        oldPass = view.findViewById<TextInputEditText>(R.id.oldPass)
        newPass = view.findViewById<TextInputEditText>(R.id.newPass)
        newPassAgain = view.findViewById<TextInputEditText>(R.id.newsPassAgain)
        change = view.findViewById<Button>(R.id.change)




        progress = view.findViewById<ProgressBar>(R.id.progress)

        change.setOnClickListener {

            if (oldPass.text.isEmpty() ||
                    newPass.text.isEmpty() ||
                    newPassAgain.text.isEmpty() ||
                    oldPass.text.toString().length < 6 ||
                    newPass.text.toString().length < 6 ||
                    newPassAgain.text.toString().length < 6){
                Toaster.errror(R.string.password_field_less_5)
            }else if (!newPass.text.toString().equals(newPassAgain.text.toString())){
                Toaster.errror(R.string.password_doesnot_match)

            }else{
                listener!!.click(1)
            }

        }

        return view
    }

    fun setDialogClickListener(dialogClickListener: DialogClickListener){
        listener = dialogClickListener
    }

    interface DialogClickListener{
        fun click(whichButton:Int)
    }



    fun setVisibility(setVisible:Boolean) = if(setVisible) progress.visibility = View.VISIBLE
                                           else progress.visibility = View.GONE

    fun reset() {
       oldPass.text.clear()
        newPass.text.clear()
        newPassAgain.text.clear()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        reset()
        super.onDismiss(dialog)
    }
}