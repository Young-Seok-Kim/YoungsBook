package com.youngsbook.ui.main

import android.app.FragmentManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.youngsbook.common.Data
import com.youngsbook.common.RecyclerViewAdapter
import com.youngsbook.common.YoungsFunction
import com.youngsbook.databinding.RecylerviewBookItemBinding
import com.youngsbook.ui.BookReview.WriteBookReview

class MainActivityAdapter: RecyclerViewAdapter<MainActivityModel,MainActivityAdapter.MyViewHolder>() {

//    var datalist = mutableListOf<MainActivityModel>()//리사이클러뷰에서 사용할 데이터 미리 정의 -> 나중에 MainActivity등에서 datalist에 실제 데이터 추가

    inner class MyViewHolder(private val binding: RecylerviewBookItemBinding): RecyclerView.ViewHolder(binding.root) {

        var textView_BookName = binding.textviewBookName
        var textView_BookReview = binding.textviewBookReview
        var ratingBar_Star = binding.ratingBarStar
        var textView_readDate = binding.textviewReadDate

    }


    //만들어진 뷰홀더 없을때 뷰홀더(레이아웃) 생성하는 함수
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding= RecylerviewBookItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return MyViewHolder(binding)
    }


    //recyclerview가 viewholder를 가져와 데이터 연결할때 호출
    //적절한 데이터를 가져와서 그 데이터를 사용하여 뷰홀더의 레이아웃 채움
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.textView_BookName.text = MainActivityAdapter.instance._arrayList[position].BOOK_NAME
        holder.textView_BookReview.text = MainActivityAdapter.instance._arrayList[position].REVIEW
        holder.ratingBar_Star.rating = MainActivityAdapter.instance._arrayList[position].STAR_RATING
        holder.textView_readDate.text =MainActivityAdapter.instance._arrayList[position].READ_DATE


    }

    private object SingletonHolder {
        val INSTANCE = MainActivityAdapter()
    }

    companion object {
        val instance: MainActivityAdapter
            get() = SingletonHolder.INSTANCE
    }
}