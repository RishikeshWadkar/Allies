package com.rishikeshwadkar.socialapp.fragments.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.adapter.ChatVPAdapter
import kotlinx.android.synthetic.main.fragment_chat_view_pager.*

class ChatViewPagerFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_view_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter: ChatVPAdapter = ChatVPAdapter(childFragmentManager, lifecycle)
        adapter.addFragment(ChatChatAlliesListFragment(), "Chat")
        adapter.addFragment(ChatUsersListFragment(), "Users")
        chat_view_pager_2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        chat_view_pager_2.adapter = adapter

        TabLayoutMediator(chat_tab_layout, chat_view_pager_2){ tab, position ->
            tab.text = adapter.fmTitle[position]
        }.attach()


    }
}