package com.youngsbook.common.customlayout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.youngsbook.R
import com.youngsbook.databinding.CustomlayoutTestBinding

class CustomlayoutEmoji (context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private var binding: CustomlayoutTestBinding = CustomlayoutTestBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        // attrs.xml에서 View의 속성 목록을 가져온다.
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomlayoutEmoji)

        val emoji = typedArray.getString(R.styleable.CustomlayoutEmoji_emoji)
        binding.tvEmojiButton.text = emoji

        val title = typedArray.getString(R.styleable.CustomlayoutEmoji_title)
        binding.tvTitleButton.text = title

        val count = typedArray.getInt(R.styleable.CustomlayoutEmoji_count, 0)
        binding.tvCountButton.text = count.toString()

        // 데이터를 캐싱해두어 가비지컬렉션에서 제외시키도록 하는 함수
        // typedArray 사용 후 호출해야하나, 커스텀뷰가 반복 사용되는 것이 아니라면 호출하지 않아도 무방하다.
        typedArray.recycle()
    }

    /*
    * XML에서 사용할때는 아래코드 사용
    *
    * <com.youngsbook.common.customlayout.CustomlayoutEmoji
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                app:count="2"
                app:emoji="👍"
                app:title="test"/>
    * */

    /*
    이미지를 받는 뷰라면 아래 코드를 사용
        val iconImageResource = typedArray.getResourceId(R.styleable.IconButton_icon, R.drawable.ic_drop)
        binding.ivIcon.setImageResource(iconImageResource)
    */
}