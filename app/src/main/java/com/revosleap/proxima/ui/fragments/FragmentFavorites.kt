package com.revosleap.proxima.ui.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.revosleap.proxima.R


/**
 * A simple [Fragment] subclass.
 */
class FragmentFavorites : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the constControls for this fragment
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

}// Required empty public constructor
