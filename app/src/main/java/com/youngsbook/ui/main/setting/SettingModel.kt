package com.youngsbook.ui.main.setting

import java.time.LocalDate

data class SettingModel(
    val READER_NAME : String,
    val READ_DATE : String,
    val REVIEW_NO : Int,
    val STAR_RATING : Float,
    val BOOK_NAME : String,
    val READER_ID : String,
    val REVIEW : String,
    val GOAL_READ_DATE : String,
    val READ_COMPLETE : String
)