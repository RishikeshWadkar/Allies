package com.rishikeshwadkar.socialapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_choose.*

class ChooseFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        welcome_login_btn.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_chooseFragment_to_signinFragment)
        }

        welcome_signup_btn.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_chooseFragment_to_signUpFragment)
        }
    }
}