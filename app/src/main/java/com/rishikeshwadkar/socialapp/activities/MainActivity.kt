package com.rishikeshwadkar.socialapp.activities
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.rishikeshwadkar.socialapp.R
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(){

    lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        navController = Navigation.findNavController(this, R.id.navHost)
        bottomNavigationView.setupWithNavController(navController)
        //toolbar2.setupWithNavController(navController)

        imageView5.setOnClickListener {
            navController.navigate(R.id.notificationsViewPagerFragment)
        }

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if(destination.id == R.id.notificationsViewPagerFragment){
                imageView5.setImageResource(R.drawable.ic_notification_selected)
            }
            else
                imageView5.setImageResource(R.drawable.ic_no_notification_unselected)
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

}