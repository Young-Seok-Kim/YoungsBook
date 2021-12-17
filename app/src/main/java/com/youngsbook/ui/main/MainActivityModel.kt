package com.youngsbook.ui.main

import java.time.LocalDate

data class MainActivityModel(
    val READER_NAME: String,
    val READ_DATE: String,
    val REVIEW_NO: Int,
    val STAR_RATING: Float,
    val BOOK_NAME: String,
    val READER_ID: String,
    val REVIEW: String
)