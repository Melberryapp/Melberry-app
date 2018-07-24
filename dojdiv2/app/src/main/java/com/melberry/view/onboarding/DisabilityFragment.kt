package com.melberry.view.onboarding

import android.arch.lifecycle.Observer
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.melberry.R
import com.melberry.databinding.FragmentDisabilityBinding
import com.melberry.viewModel.DisabilityViewModel

class DisabilityFragment : Fragment() {

    private var listener: DisabilityFragment.OnDisabilityFragmentInteractionListener? = null

    // Bug fix of crash
    //      Fatal Exception: java.lang.IllegalStateException
    //      Can not perform this action after onSaveInstanceState
    override fun onSaveInstanceState(outState: Bundle?) {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Data binding
        val disabilityViewModel = DisabilityViewModel(this.activity?.application!!)
        val binding = DataBindingUtil.inflate<FragmentDisabilityBinding>(inflater, R.layout.fragment_disability, container, false)

        // Binding connecting with ViewModel
        binding.let {
            it.viewModel = disabilityViewModel
            it.setLifecycleOwner(this)
        }

        // When info that next Button is pressed, inform an activity to perform an action
        // t == 2 -> to not inform during first time it happens, in VM there is changed 1 to 2 to get make a change -> todotask in VM
        disabilityViewModel.buttonPressed.observe(this, Observer { t -> if (t == 2) listener?.onDisabilityFragmentInteraction() })

        return binding.root
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DisabilityFragment.OnDisabilityFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnDisabilityFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    // Interface to communicate with the Activity
    interface OnDisabilityFragmentInteractionListener {
        fun onDisabilityFragmentInteraction()
    }
}

