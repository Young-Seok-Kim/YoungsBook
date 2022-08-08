package com.youngsbook.ui.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.youngsbook.common.RecyclerViewAdapter
import com.youngsbook.common.YoungsFunction
import com.youngsbook.databinding.RecyclerviewBookItemBinding
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivityAdapter: RecyclerViewAdapter<MainActivityModel,MainActivityAdapter.MyViewHolder>() {

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    inner class MyViewHolder(private val binding: RecyclerviewBookItemBinding): RecyclerView.ViewHolder(binding.root) {

        var textView_BookName = binding.textviewBookName
        var textView_BookReview = binding.textviewBookReview
        var ratingBar_Star = binding.ratingBarStar
        var textView_readDate = binding.textviewReadDate
        var textView_ReadComplete = binding.textviewReadComplete
    }


    //만들어진 뷰홀더 없을때 뷰홀더(레이아웃) 생성하는 함수
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding= RecyclerviewBookItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return MyViewHolder(binding)
    }

    //recyclerview가 viewholder를 가져와 데이터 연결할때 호출
    //적절한 데이터를 가져와서 그 데이터를 사용하여 뷰홀더의 레이아웃 채움
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.textView_BookName.text = instance._arrayList[position].BOOK_NAME
        holder.textView_BookReview.text = instance._arrayList[position].REVIEW
        holder.ratingBar_Star.rating = instance._arrayList[position].STAR_RATING
        holder.textView_readDate.text = instance._arrayList[position].READ_DATE

        if(instance._arrayList[position].READ_COMPLETE == "1") {
            holder.textView_ReadComplete.text = "완독"
            holder.textView_ReadComplete.visibility = View.VISIBLE
            holder.textView_ReadComplete.setTextColor(Color.GRAY)
        }
        else if((instance._arrayList.get(position).GOAL_READ_DATE?:"").isNotBlank()) {
            val goalReadDate = instance._arrayList.get(position).GOAL_READ_DATE
            val date = SimpleDateFormat("yyyy-MM-dd").parse(goalReadDate)
            val today = Calendar.getInstance()
            val calculateDate = (date.time - today.time.time) / (1000 * 60 * 60 * 24)

            if (calculateDate > 0) {
                holder.textView_ReadComplete.text = "D-${calculateDate}"
                holder.textView_ReadComplete.visibility = View.VISIBLE
            }
        }
        else{
            holder.textView_ReadComplete.visibility = View.GONE
        }

    }

    private object SingletonHolder {
        val INSTANCE = MainActivityAdapter()
    }

    companion object {
        val instance: MainActivityAdapter
            get() = SingletonHolder.INSTANCE
    }
}