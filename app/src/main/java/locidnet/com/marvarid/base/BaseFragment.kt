package locidnet.com.marvarid.base

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlin.properties.Delegates


abstract class BaseFragment : Fragment(){

    var rootView by Delegates.notNull<View>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        rootView = inflater!!.inflate(getFragmentView(),container,false)

        init()
        return rootView

    }


    abstract fun getFragmentView():Int

    abstract fun init()

}