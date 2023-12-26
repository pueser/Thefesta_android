package com.example.thefesta.member

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ScrollView
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.example.thefesta.MainActivity
import com.example.thefesta.R


class AgreementPage : Fragment() {

    private lateinit var checkAllAgreements: CheckBox
    private lateinit var checkServiceAgreement1: CheckBox
    private lateinit var checkServiceAgreement2: CheckBox
    private lateinit var scrollView1: ScrollView
    private lateinit var scrollView2: ScrollView
    private lateinit var agreementBtn: Button
    private lateinit var cancelBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_agreement_page, container, false)

        // Initialize views
        checkAllAgreements = view.findViewById(R.id.checkAllAgreements)
        checkServiceAgreement1 = view.findViewById(R.id.checkServiceAgreement1)
        checkServiceAgreement2 = view.findViewById(R.id.checkServiceAgreement2)
        scrollView1 = view.findViewById(R.id.scrollView1)
        scrollView2 = view.findViewById(R.id.scrollView2)
        agreementBtn = view.findViewById(R.id.agreementBtn)
        cancelBtn = view.findViewById(R.id.cancelBtn)

        checkAllAgreements.setOnCheckedChangeListener { _, isChecked ->

            if (checkAllAgreements.isChecked) {
                checkServiceAgreement1.isChecked = true
                checkServiceAgreement2.isChecked = true
            }

            if (checkServiceAgreement1.isChecked && checkServiceAgreement2.isChecked) {
                if (!checkAllAgreements.isChecked) {
                    checkServiceAgreement1.isChecked = false
                    checkServiceAgreement2.isChecked = false
                }
            } else if (!checkServiceAgreement1.isChecked && checkServiceAgreement2.isChecked) {
                if (!checkAllAgreements.isChecked) {
                    checkServiceAgreement1.isChecked = false
                }
            } else if (checkServiceAgreement1.isChecked && !checkServiceAgreement2.isChecked) {
                if (!checkAllAgreements.isChecked) {
                    checkServiceAgreement2.isChecked = false
                }
            }

            if (!checkServiceAgreement1.isChecked || !checkServiceAgreement2.isChecked) {
                checkAllAgreements.isChecked = false
            } else {
                checkAllAgreements.isChecked = isChecked
            }
        }

        val checkBoxChangeListener = View.OnClickListener {
            checkAllAgreements.isChecked =
                checkServiceAgreement1.isChecked && checkServiceAgreement2.isChecked
        }

        checkServiceAgreement1.setOnClickListener(checkBoxChangeListener)
        checkServiceAgreement2.setOnClickListener(checkBoxChangeListener)

        agreementBtn.setOnClickListener {
            if (checkAllAgreements.isChecked) {
                val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
                transaction.replace(R.id.container, Join())
                transaction.addToBackStack(null)
                transaction.commit()
            } else {
                Toast.makeText(requireContext(), "이용약관에 모두 동의해야 합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        cancelBtn.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        return view
    }
}