package com.rishikeshwadkar.socialapp.fragments.authentication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.airbnb.lottie.Lottie
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory.fromJson
import com.rishikeshwadkar.socialapp.R
import kotlinx.android.synthetic.main.fragment_choose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChooseFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_choose, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlobalScope.launch(Dispatchers.IO) {
            lottieAnimationView2.setAnimation("home_animation.json")
            //lottieAnimationView2.playAnimation()
        }


        welcome_login_btn.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_chooseFragment_to_signinFragment)
        }

        welcome_signup_btn.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_chooseFragment_to_signUpFragment)
        }
    }
}