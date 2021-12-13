package com.youngsbook.ui.BookReview

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.youngsbook.R
import com.youngsbook.databinding.ActivityMainBinding
import com.youngsbook.databinding.WriteBookReviewBinding

class WriteBookReview : DialogFragment() {
    private lateinit var binding: WriteBookReviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WriteBookReviewBinding.inflate(layoutInflater)

    }

    override fun onResume() {
        super.onResume()
        // 전체화면으로 만드는 코드
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        initButton()

        return binding.root
    }

    private fun initButton() {
        binding.ratingBarStar.setOnRatingBarChangeListener { ratingBar, fl, b ->
            binding.ratingBarStar.rating = fl
        }

        binding.buttonSave.setOnClickListener {
            Toast.makeText(context,"${binding.ratingBarStar.rating}",Toast.LENGTH_SHORT).show()
        }
    }


}