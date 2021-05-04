package com.rishikeshwadkar.socialapp.fragments.notification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.adapter.VPAdapter
import kotlinx.android.synthetic.main.fragment_notifications_view_pager.*

class NotificationsViewPagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifications_view_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager2 = notification_view_pager_2
        val tabLayout: TabLayout = notifications_viewpager_tablayout as TabLayout

        val vpAdapter = VPAdapter(childFragmentManager, lifecycle)
        vpAdapter.addFragment(NotificationsFragment(), "Notifications")
        vpAdapter.addFragment(SentRequestPendingFragment(), "Requests")
        viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager2.adapter = vpAdapter

        tabLayout.getTabAt(0)?.orCreateBadge?.number = 1

        if (tabLayout.getTabAt(0)?.badge?.number == 0)
            tabLayout.getTabAt(0)!!.removeBadge()

        TabLayoutMediator(tabLayout, viewPager2){tab, position ->
            tab.text = vpAdapter.fmTitle[position]
        }.attach()
    }
}