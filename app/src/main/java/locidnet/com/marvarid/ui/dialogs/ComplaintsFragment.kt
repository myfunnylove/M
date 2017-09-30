package locidnet.com.marvarid.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import locidnet.com.marvarid.R
import locidnet.com.marvarid.base.Base
import locidnet.com.marvarid.resources.utils.Const

/**
 * Created by macbookpro on 06.09.17.
 */
class ComplaintsFragment : DialogFragment() {

    var listener: DialogClickListener? = null
    companion object {
        var mInstance: ComplaintsFragment? = null

        fun instance() : ComplaintsFragment {

            if (mInstance == null) mInstance = ComplaintsFragment()

            val bundle = Bundle()

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

        val view = inflater!!.inflate(R.layout.fragment_dialog_complain,container,false)

        val list = view.findViewById(R.id.list) as RecyclerView
        list.layoutManager = LinearLayoutManager(activity)
        list.setHasFixedSize(true)
        val adapter = ComplainsAdapter()
        adapter.setDialogClickListener(object : DialogClickListener{
            override fun click(whichButton: Int) {
                listener!!.click(whichButton)
            }

        })
        list.adapter = adapter

        return view
    }

    fun setDialogClickListener(dialogClickListener: DialogClickListener){
        listener = dialogClickListener
    }




    interface DialogClickListener{
        fun click(whichButton:Int)
    }


    class ComplainsAdapter : RecyclerView.Adapter<ComplainsAdapter.Holder>() {
        var listener: DialogClickListener? = null

        val inf = Base.get.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        override fun getItemCount(): Int {
            return Const.complaints.size
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): Holder {

            return Holder(inf.inflate(R.layout.res_complaint_item,parent,false))
        }

        override fun onBindViewHolder(holder: Holder?, position: Int) {

            holder!!.text.text = Const.complaints.get(position).type
            holder.text.setOnClickListener{
                listener!!.click(Const.complaints.get(position).id)
            }
        }


        class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val text = itemView.findViewById(R.id.text) as TextView;
        }
        fun setDialogClickListener(dialogClickListener: DialogClickListener){
            listener = dialogClickListener
        }
    }
}