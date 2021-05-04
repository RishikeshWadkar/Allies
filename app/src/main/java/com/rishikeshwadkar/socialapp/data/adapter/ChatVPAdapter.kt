package com.rishikeshwadkar.socialapp.data.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ChatVPAdapter(fm: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fm, lifecycle) {

    var fmArray: ArrayList<Fragment> = ArrayList()
    var fmTitle: ArrayList<String> = ArrayList()

    fun addFragment(fragment:Fragment, title: String){
        fmArray.add(fragment)
        fmTitle.add(title)
    }

    override fun getItemCount(): Int {
        return fmArray.size
    }

    override fun createFragment(position: Int): Fragment {
        return fmArray[position]
    }
}