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
import com.melberry.databinding.FragmentCitiesBinding
import com.melberry.utils.City
import com.melberry.viewModel.CitiesViewModel

class CitiesFragment : Fragment() {

    private var listener: OnCitiesFragmentInteractionListener? = null

    // Bug fix of crash
    //      Fatal Exception: java.lang.IllegalStateException
    //      Can not perform this action after onSaveInstanceState
    override fun onSaveInstanceState(outState: Bundle?) {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Data binding
        val citiesViewModel = CitiesViewModel(this.activity?.application!!)
        val binding = DataBindingUtil.inflate<FragmentCitiesBinding>(inflater, R.layout.fragment_cities, container, false)

        // Binding connecting with ViewModel
        binding.let {
            it.viewModel = citiesViewModel
            it.setLifecycleOwner(this)
        }

        // After city is clicked, informs an activity
        citiesViewModel.cityClicked.observe(this, Observer { cityName -> if (cityName != City.DEFAULT) listener?.openActivityFromSelectedCity(cityName) })

        // After contact us is clicked, info came from VM, informs an activity
        citiesViewModel.contactUsClicked.observe(this, Observer { listener?.openMailClient() })

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCitiesFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnCitiesFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    // Interface to communicate with the Activity
    interface OnCitiesFragmentInteractionListener {
        fun openActivityFromSelectedCity(city: City?)
        fun openMailClient()
    }
}
