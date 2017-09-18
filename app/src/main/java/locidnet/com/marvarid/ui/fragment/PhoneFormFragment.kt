package locidnet.com.marvarid.ui.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.DialogFragment
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ProgressBar
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.resources.utils.Functions
import locidnet.com.marvarid.resources.utils.JavaCodes
import locidnet.com.marvarid.resources.utils.log
import retrofit2.http.GET

/**
 * Created by macbookpro on 06.09.17.
 */
class PhoneFormFragment : DialogFragment() {

    var listener:DialogClickListener? = null
    companion object {
        var mInstance: PhoneFormFragment? = null

        val GET_SMS = 1
        val CHANGE = 2
        val TAG = "yesnofragment"
        fun instance() : PhoneFormFragment {

            if (mInstance == null) mInstance = PhoneFormFragment()

            return mInstance!!
        }

        fun instance(bundle:Bundle) : PhoneFormFragment {

            if (mInstance == null) mInstance = PhoneFormFragment()
            mInstance!!.arguments = bundle
            return mInstance!!
        }
    }

    lateinit var phone: TextInputEditText
    lateinit var smsCode:TextInputEditText
    lateinit var send:Button
    lateinit var progress:ProgressBar
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_dialog_change_phone,container,false)

        send = view.findViewById(R.id.yes) as Button
        send.text = Base.get.resources.getString(R.string.get_sms)
        phone = view.findViewById(R.id.phone) as TextInputEditText

           phone.addTextChangedListener(JavaCodes.EditTelephoneCodeWatcher)
           phone.setKeyListener(Functions.EditCardKey)



        smsCode = view.findViewById(R.id.smsCode) as TextInputEditText
        progress = view.findViewById(R.id.progress) as ProgressBar

        send.findViewById(R.id.yes).setOnClickListener {
            log.d("${send.tag}")
            if (send.tag == CHANGE) {
                listener!!.click(CHANGE)

            }else {
                listener!!.click(GET_SMS)

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

    fun setSms(sms:String) {
        send.tag = CHANGE

        send.text = Base.get.resources.getString(R.string.confirm)
        smsCode.visibility =View.VISIBLE

        smsCode.setText(sms)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        phone.setText("")
        smsCode.visibility =View.GONE

        smsCode.setText("")
        send.tag = GET_SMS
        send.text = Base.get.resources.getString(R.string.get_sms)
        super.onDismiss(dialog)
    }


    fun setVisibility(setVisible:Boolean) = if(setVisible) progress.visibility = View.VISIBLE
                                           else progress.visibility = View.GONE
}