package com.cs407.locspend

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddSpendingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddSpendingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        val addSpendingButton = view?.findViewById<ImageButton>(R.id.imageButton)
        val categoryView = view?.findViewById<TextView>(R.id.categoryTextView)
        val addSpendingButton2 = view?.findViewById<ImageButton>(R.id.imageButton2)
        val categoryView2 = view?.findViewById<TextView>(R.id.categoryTextView2)
        val addSpendingButton3 = view?.findViewById<ImageButton>(R.id.imageButton3)
        val categoryView3 = view?.findViewById<TextView>(R.id.categoryTextView3)
        val addSpendingButton4 = view?.findViewById<ImageButton>(R.id.imageButton4)
        val categoryView4 = view?.findViewById<TextView>(R.id.categoryTextView4)
        val addSpendingButton5 = view?.findViewById<ImageButton>(R.id.imageButton5)
        val categoryView5 = view?.findViewById<TextView>(R.id.categoryTextView5)
        val addSpendingButton6 = view?.findViewById<ImageButton>(R.id.imageButton6)
        val categoryView6 = view?.findViewById<TextView>(R.id.categoryTextView6)



        if (addSpendingButton != null) {
            val category = categoryView?.text.toString()
            addSpendingButton.setOnClickListener {
                showDialog(category)
            }
        }

        if (addSpendingButton2 != null) {
            val category = categoryView2?.text.toString()
            addSpendingButton2.setOnClickListener {
                showDialog(category)
            }
        }

        if (addSpendingButton3 != null) {
            val category = categoryView3?.text.toString()
            addSpendingButton3.setOnClickListener {
                showDialog(category)
            }
        }

        if (addSpendingButton4 != null) {
            val category = categoryView4?.text.toString()
            addSpendingButton4.setOnClickListener {
                showDialog(category)
            }
        }

        if (addSpendingButton5 != null) {
            val category = categoryView5?.text.toString()
            addSpendingButton5.setOnClickListener {
                showDialog(category)
            }
        }

        if (addSpendingButton6 != null) {
            val category = categoryView6?.text.toString()
            addSpendingButton6.setOnClickListener {
                showDialog(category)
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_spending, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddSpendingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddSpendingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun showDialog(category : String) {
        val input = EditText(requireContext())
        input.hint = "$0.00"
        AlertDialog.Builder(requireContext())
            .setMessage("Amount:")
            .setView(input)
            .setPositiveButton("Ok") {dialog,_ ->
                val userInput = input.text.toString()
                // needs to change in the database...
                //addSpending(category, userInput.toInt())
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") {dialog,_ ->
                dialog.dismiss()
            }

    }
}