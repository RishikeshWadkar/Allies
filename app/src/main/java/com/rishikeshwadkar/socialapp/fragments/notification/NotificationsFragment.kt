package com.rishikeshwadkar.socialapp.fragments.notification

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.adapter.NotificationsAdapter
import com.rishikeshwadkar.socialapp.data.dao.NotificationsDao
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.Notification
import com.rishikeshwadkar.socialapp.data.models.User
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class NotificationsFragment : Fragment(), NotificationsAdapter.NotificationListener {

    val notificationsDao = NotificationsDao()
    private val userDao = UserDao()
    lateinit var adapter: NotificationsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val mThis = this
        GlobalScope.launch(Dispatchers.IO) {
            val query: Query = notificationsDao.notificationsCollection.whereEqualTo("uid", Firebase.auth.currentUser!!.uid)
                    .orderBy("currentTime", Query.Direction.DESCENDING)
            val recyclerOptions = FirestoreRecyclerOptions.Builder<Notification>().setQuery(query,Notification::class.java).build()
            withContext(Dispatchers.Main){
                adapter = NotificationsAdapter(recyclerOptions, mThis)
                notifications_recycler_view.adapter = adapter
                notifications_recycler_view.layoutManager = LinearLayoutManager(context)
                adapter.startListening()
            }
        }
    }

    override fun onNotificationButtonClickListener(notificationId: String) {
        Log.d("noti", "clicked")
        GlobalScope.launch(Dispatchers.IO) {
            val notification: Notification = notificationsDao.getNotificationById(notificationId).await().toObject(Notification::class.java)!!
            val user: User = userDao.getUserById(notification.uid).await().toObject(User::class.java)!!
            withContext(Dispatchers.Main){
                if (user.userAllies.contains(notification.likerUid)){

                }
                else if (user.userRequests.contains(notification.likerUid)){
                    userDao.addToAllies(user.uid, notification.likerUid)
                }
                else {
                    userDao.addRequest(user.uid, notification.likerUid)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        //adapter.stopListening()
    }
}