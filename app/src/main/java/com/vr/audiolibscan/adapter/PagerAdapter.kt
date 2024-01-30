package com.vr.audiolibscan.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.vr.audiolibscan.ui.fragment.MetaFragment
import com.vr.audiolibscan.ui.fragment.ResultFragment

class PagerAdapter(activity: FragmentActivity)
    : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        if(position==0){
            val fragment = ResultFragment()
            return fragment
        }else{
            val fragment = MetaFragment()
            return fragment
        }
    }
    override fun getItemCount(): Int {
        return 2
    }
}