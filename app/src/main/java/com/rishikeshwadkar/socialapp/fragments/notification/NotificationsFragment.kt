package com.rishikeshwadkar.socialapp.fragments.notification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.adapter.NotificationsAdapter
import com.rishikeshwadkar.socialapp.data.dao.NotificationsDao
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.Notification
import com.rishikeshwadkar.socialapp.data.models.Post
import com.rishikeshwadkar.socialapp.data.models.User
import com.rishikeshwadkar.socialapp.data.viewmodels.MyViewModel
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

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
        CoroutineScope(Dispatchers.IO).launch {
            val query: Query = notificationsDao.notificationsCollection.whereEqualTo("to", Firebase.auth.currentUser!!.uid)
                    .orderBy("currentTime", Query.Direction.DESCENDING)

            val recyclerOptions = FirestoreRecyclerOptions.Builder<Notification>().setQuery(query,Notification::class.java).build()
            withContext(Dispatchers.Main){
                if (mThis.isVisible){
                    adapter = NotificationsAdapter(recyclerOptions, mThis)
                    notifications_recycler_view.adapter = adapter
                    notifications_recycler_view.layoutManager = LinearLayoutManager(context)
                    adapter.startListening()
                }
            }
        }
    }

    override fun onNotificationButtonClickListener(notificationId: String, position: Int) {
        mViewModel.showDialog(requireContext(), "Loading")
        val mThis = this
        CoroutineScope(Dispatchers.IO).launch {
            val notification: Notification = notificationsDao.getNotificationById(notificationId)
                    .await().toObject(Notification::class.java)!!
            val fromUser: User = userDao.getUserById(notification.from)
                    .await().toObject(User::class.java)!!

            withContext(Dispatchers.Main){
                if (mThis.isVisible){
                    userDao.addToAllies(notification.to, notification.from)
                    notificationsDao.removeNotification(notificationId)
                    val snackbar = Snackbar.make(
                        notificationFragmentConstraintLayout,
                        "${fromUser.userDisplayName} is now your Allies",
                        Snackbar.LENGTH_LONG
                    )
                    snackbar.setAction("Action", null)
                    snackbar.show()
                    mViewModel.dismissDialog()
                }
            }
        }
    }

    override fun onProfileClickListener(fromUid: String, toUid: String) {
        val action = NotificationsViewPagerFragmentDirections.actionNotificationsViewPagerFragmentToUserProfileFragment(fromUid)
        Navigation.findNavController(requireView()).navigate(action)
    }

    override fun onNotificationClickListener(notificationId: String) {
        val mThis = this
        CoroutineScope(Dispatchers.IO).launch {
            val notification: Notification = notificationsDao.getNotificationById(notificationId).await()
                .toObject(Notification::class.java)!!
            withContext(Dispatchers.Main){
                if (notification.postId != "" && mThis.isVisible){
                    val action = NotificationsViewPagerFragmentDirections.actionNotificationsViewPagerFragmentToSinglePostFragment(notification.postId)
                    Navigation.findNavController(requireView()).navigate(action)
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        setupRecyclerView()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}