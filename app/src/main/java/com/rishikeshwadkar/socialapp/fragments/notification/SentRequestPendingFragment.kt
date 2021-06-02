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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.adapter.NotificationsAdapter
import com.rishikeshwadkar.socialapp.data.dao.NotificationsDao
import com.rishikeshwadkar.socialapp.data.viewmodels.MyViewModel
import com.rishikeshwadkar.socialapp.data.models.Notification
import kotlinx.android.synthetic.main.fragment_sent_request_pending.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class SentRequestPendingFragment : Fragment(), NotificationsAdapter.NotificationListener {

    private val notificationsDao = NotificationsDao()
    private val mViewModel: MyViewModel by viewModels()
    lateinit var adapter: NotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sent_request_pending, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val query: Query = notificationsDao.notificationsCollection
                .whereEqualTo("type", "Request")
                .whereEqualTo("from", Firebase.auth.currentUser!!.uid)

        val options = FirestoreRecyclerOptions
                .Builder<Notification>()
                .setQuery(query,Notification::class.java).build()
        adapter = NotificationsAdapter(options, this)
        sent_request_recycler_view.adapter = adapter
        sent_request_recycler_view.layoutManager = LinearLayoutManager(context)
        adapter.startListening()
    }

    override fun onNotificationButtonClickListener(notificationId: String, position: Int) {
        val mThis = this
        CoroutineScope(Dispatchers.IO).launch {
            val notification: Notification = notificationsDao.getNotificationById(notificationId)
                    .await().toObject(Notification::class.java)!!
            withContext(Dispatchers.Main){
                if (mThis.isVisible){
                    mViewModel.setUpMaterialDialogRemoveRequest(
                        requireContext(),
                        Firebase.auth.currentUser!!.uid,
                        notification.to,
                        adapter,
                        position,
                        "Remove",
                        "Are you sure?",
                        "Remove",
                        "Removing"
                    )
                }
            }
        }
    }

    override fun onProfileClickListener(fromUid: String, toUid: String) {
        val action = NotificationsViewPagerFragmentDirections.actionNotificationsViewPagerFragmentToUserProfileFragment(toUid)
        Navigation.findNavController(requireView()).navigate(action)
    }

    override fun onNotificationClickListener(notificationId: String) {
        // Do nothing
    }

    override fun onStart() {
        super.onStart()
        setupRecyclerView()
    }
}