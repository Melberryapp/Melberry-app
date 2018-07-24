package com.melberry.view.onboarding

import android.arch.lifecycle.Observer
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*

import com.melberry.R
import com.melberry.databinding.FragmentFeaturesBinding
import com.melberry.utils.OnSwipeTouchListener
import com.melberry.viewModel.FeaturesViewModel

class FeaturesFragment : Fragment() {

    var listener: OnFeaturesFragmentInteractionListener? = null

    // Bug fix of crash
    //      Fatal Exception: java.lang.IllegalStateException
    //      Can not perform this action after onSaveInstanceState
    override fun onSaveInstanceState(outState: Bundle?) {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Data binding
        val featuresViewModel = FeaturesViewModel(this.activity?.application!!)
        val binding = DataBindingUtil.inflate<FragmentFeaturesBinding>(inflater, R.layout.fragment_features, container, false)
        // Binding connecting with ViewModel
        binding?.let {
            it.viewModel = featuresViewModel
            it.setLifecycleOwner(this)
        }

        // When info that next Button is pressed, inform an activity to perform an action
        // t == 2 -> to not inform during first time it happens, in VM there is changed 1 to 2 to get make a change -> todotask in VM
        featuresViewModel.notifyActivity.observe(this, Observer { t -> if (t == 2) listener?.onFeaturesFragmentInteraction() })

        val view = binding.root
        view.isFocusableInTouchMode = true
        view.requestFocus()

        // Back pressed button to perform back action
        view.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event?.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                featuresViewModel.onBackPressed()
                return@OnKeyListener true
            }
            false
        })

        // Swipe to perform back action
        view.setOnTouchListener(object : OnSwipeTouchListener(this@FeaturesFragment.context) {
            override fun onSwipeLeft() {
                featuresViewModel.onContinuePressed()
            }

            override fun onSwipeRight() {
                featuresViewModel.onBackPressed()
            }
        })
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FeaturesFragment.OnFeaturesFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFeaturesFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    // Interface to communicate with the Activity
    interface OnFeaturesFragmentInteractionListener {
        fun onFeaturesFragmentInteraction()
    }
}
