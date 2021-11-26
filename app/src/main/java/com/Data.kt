package com.youngsbook

class Data {

    private object SingletoneHolder {
        val INSTANCE = Data()

    }

    companion object{
        val instance : Data
            get() = SingletoneHolder.INSTANCE
    }
}