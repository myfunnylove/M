package locidnet.com.marvarid.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import locidnet.com.marvarid.base.BaseFragment
import locidnet.com.marvarid.connectors.GoNext
import locidnet.com.marvarid.ui.fragment.*
import kotlin.properties.Delegates

/**
 * Created by Michaelan on 5/18/2017.
 */
class MainMenuPagerAdapter(fm: FragmentManager?,map:HashMap<Int,BaseFragment>) : FragmentPagerAdapter(fm) {


    var mainFragments by Delegates.notNull<HashMap<Int,BaseFragment>>()
    init {


        mainFragments = HashMap(map)

    }
    override fun getItem(p0: Int): BaseFragment {
        return  mainFragments.get(p0)!!
    }

    override fun getCount(): Int {
        return mainFragments.size
    }



}