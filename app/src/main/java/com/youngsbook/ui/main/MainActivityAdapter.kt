package com.youngsbook.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.youngsbook.databinding.RecylerviewBookItemBinding

class MainActivityAdapter: RecyclerView.Adapter<MainActivityAdapter.MyViewHolder>() {

    var datalist = mutableListOf<MainActivityModel>()//리사이클러뷰에서 사용할 데이터 미리 정의 -> 나중에 MainActivity등에서 datalist에 실제 데이터 추가

    inner class MyViewHolder(private val binding: RecylerviewBookItemBinding): RecyclerView.ViewHolder(binding.root) {

        var textView_BookName = binding.textviewBookName
        var textView_BookReview = binding.textviewBookReview
        var ratingBar_Star = binding.ratingBarStar
        val layout = binding.list

        fun bind(reviewList:MainActivityModel){
            //binding.dogPhotoImg.=dogData.dog_img
            binding.textviewBookName.text=reviewList.NAME
            binding.textviewBookReview.text= reviewList.AGE.toString()
        }
    }


    //만들어진 뷰홀더 없을때 뷰홀더(레이아웃) 생성하는 함수
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding= RecylerviewBookItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = datalist.size

    //recyclerview가 viewholder를 가져와 데이터 연결할때 호출
    //적절한 데이터를 가져와서 그 데이터를 사용하여 뷰홀더의 레이아웃 채움
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(datalist[position])
        holder.textView_BookName.text = datalist[position].NAME
        holder.textView_BookReview.text = datalist[position].AGE.toString()

        holder.layout.setOnClickListener() // 해당 행 클릭시 이벤트
        {
//            WriteBookReview().showNow(FragmentManager,"")
//            Toast.makeText(holder.itemView.context,"$position",Toast.LENGTH_SHORT).show()
        }

        if (position.equals(1))
            holder.ratingBar_Star.rating = 3.3F

    }
}