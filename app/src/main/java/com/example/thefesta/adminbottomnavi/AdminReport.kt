package com.example.thefesta.adminbottomnavi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.thefesta.R



/**
 * A simple [Fragment] subclass.
 * Use the [AdminReport.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminReport : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_report, container, false)
    }

}