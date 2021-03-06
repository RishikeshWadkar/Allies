package com.rishikeshwadkar.socialapp.activities
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.dao.ChatDao
import com.rishikeshwadkar.socialapp.data.dao.NotificationsDao
import com.rishikeshwadkar.socialapp.data.viewmodels.MyViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*


class MainActivity : AppCompatActivity(), androidx.appcompat.widget.Toolbar.OnMenuItemClickListener {

    lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    private var tempBool: Boolean = true
    private val notificationsDao = NotificationsDao()
    private val chatDao = ChatDao()
    private val mViewModel: MyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        navController = Navigation.findNavController(this, R.id.navHost)
        bottomNavigationView.setupWithNavController(navController)

        toolbar2.setOnMenuItemClickListener(this)
        val notificationBtn = toolbar2.menu.findItem(R.id.notificationFragment)
        toolbar2.main_logout_btn.visibility = View.INVISIBLE


        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if(destination.id == R.id.notificationsViewPagerFragment ||
                destination.id == R.id.notificationFragment ||
                destination.id == R.id.sentFriendRequestFragment){
                handler.removeCallbacks(runnable)
                notificationBtn.setIcon( R.drawable.ic_notification_selected)
                tempBool = false
            }
            else{
                notificationBtn.setIcon(R.drawable.ic_no_notification_unselected)
                tempBool = true
                handler.post(runnable)
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

            if (destination.id == R.id.myProfileFragment){
                toolbar2.main_logout_btn.visibility = View.VISIBLE
                toolbar2.main_logout_btn.setOnClickListener {
                    handler.removeCallbacks(runnable)
                    chatHandler.removeCallbacks(chatRunnable)
                    mViewModel.setUpMaterialDialogCommon(this, "Sign Out", "Are you sure?", "Sign out", "Signing out")
                }
            }
            else{
                toolbar2.main_logout_btn.visibility = View.INVISIBLE
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

    private val handler: Handler = Handler()
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            setNotificationDot()
            handler.postDelayed(this, 100)
        }
    }

    private val chatHandler: Handler = Handler()
    private val chatRunnable: Runnable = object : Runnable {
        override fun run() {
            setChatDot()
            chatHandler.postDelayed(this, 100)
        }
    }

    override fun onStart() {
        super.onStart()
        handler.postDelayed(runnable, 100)
        chatHandler.postDelayed(chatRunnable, 100)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(runnable)
        chatHandler.removeCallbacks(chatRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        chatHandler.removeCallbacks(chatRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
        chatHandler.removeCallbacks(chatRunnable)
    }

    private fun setChatDot() {
        chatDao.chatMetadataCollection.whereEqualTo("latestChat.to", Firebase.auth.currentUser!!.uid)
                .whereEqualTo("latestChat.seen", false)
                .get().addOnSuccessListener {
                    val badge = bottomNavigationView.getOrCreateBadge(R.id.chatViewPagerFragment)
                    var tNum = 0
                    for (document in it){
                        tNum++
                        badge.isVisible = true
                        badge.number = tNum
                    }
                    if (it.isEmpty){
                        badge.number = 0
                        badge.isVisible = false
                    }
                }
    }

    private fun setNotificationDot() {
        notificationsDao.notificationsCollection.whereEqualTo("to", Firebase.auth.currentUser!!.uid)
                .whereEqualTo("seen", false)
                .get().addOnSuccessListener {
                    for (document in it){
                        val notificationBtn = toolbar2.menu.findItem(R.id.notificationFragment)
                        notificationBtn.setIcon(R.drawable.ic_notification_unread)
                        break
                    }
                    if (it.isEmpty){
                        val notificationBtn = toolbar2.menu.findItem(R.id.notificationFragment)
                        notificationBtn.setIcon(R.drawable.ic_no_notification_unselected)
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