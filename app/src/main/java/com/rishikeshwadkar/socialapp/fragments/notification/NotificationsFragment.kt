package com.rishikeshwadkar.socialapp.fragments.notification

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
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
import com.rishikeshwadkar.socialapp.data.viewmodels.MyViewModel
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.FieldPosition

class NotificationsFragment : Fragment(), NotificationsAdapter.NotificationListener {

    private val notificationsDao = NotificationsDao()
    private val userDao = UserDao()
    lateinit var adapter: NotificationsAdapter
    private val mViewModel: MyViewModel by viewModels()

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

            val recyclerOptions = FirestoreRecyclerOptions.Builder<Notification>().setQuery(query,Notification::class.java).build()
            withContext(Dispatchers.Main){
                adapter = NotificationsAdapter(recyclerOptions, mThis)
                notifications_recycler_view.adapter = adapter
                notifications_recycler_view.layoutManager = LinearLayoutManager(context)
                adapter.startListening()
            }
        }
    }

    override fun onNotificationButtonClickListener(notificationId: String, position: Int) {
        Log.d("noti", "clicked")
        val mThis = this
        GlobalScope.launch(Dispatchers.IO) {
            val notification: Notification = notificationsDao.getNotificationById(notificationId).await().toObject(Notification::class.java)!!
            val user: User = userDao.getUserById(notification.uid).await().toObject(User::class.java)!!
            withContext(Dispatchers.Main){
                if (user.userAllies.contains(notification.likerUid)){
                    mViewModel.setupMaterialDialog(requireContext(), user.uid, notification.likerUid, adapter, position)
                }
                else if (user.userRequests.contains(notification.likerUid)){
                    userDao.addToAllies(user.uid, notification.likerUid, adapter, position)
                }
                else if (user.userRequestSent.contains(notification.likerUid)){}
                else {
                    userDao.addRequest(user.uid, notification.likerUid, adapter, position)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        //adapter.stopListening()
    }
}