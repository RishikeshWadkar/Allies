package com.rishikeshwadkar.socialapp.activities
import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.android.material.tabs.TabLayout
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.fragments.notification.NotificationsFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_notifications_view_pager.*


class MainActivity : AppCompatActivity(), androidx.appcompat.widget.Toolbar.OnMenuItemClickListener {

    lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    private var tempBool: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        navController = Navigation.findNavController(this, R.id.navHost)
        bottomNavigationView.setupWithNavController(navController)

        toolbar2.setOnMenuItemClickListener(this)
        val notificationBtn = toolbar2.menu.findItem(R.id.notificationFragment)


        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if(destination.id == R.id.notificationsViewPagerFragment){
                notificationBtn.setIcon( R.drawable.ic_notification_selected)
                tempBool = false
            }
            else{
                notificationBtn.setIcon(R.drawable.ic_no_notification_unselected)
                tempBool = true
            }

            if (destination.id == R.id.chatWithUserFragment){
                bottomNavigationView.visibility = View.GONE
                toolbar2.visibility = View.GONE
                this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
            else{
                bottomNavigationView.visibility = View.VISIBLE
                toolbar2.visibility = View.VISIBLE
                this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (true) {
                val window: Window = window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.WHITE
                main_activity_layout.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                main_activity_layout.systemUiVisibility = 100;
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
            navController,
            null
        )
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (tempBool)
            navController.navigate(R.id.notificationsViewPagerFragment)
        tempBool = false
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        tempBool = true
    }
}