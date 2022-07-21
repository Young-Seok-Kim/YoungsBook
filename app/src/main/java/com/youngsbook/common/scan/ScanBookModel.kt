package com.youngsbook.common.scan

import com.youngsbook.common.YoungsFunction
import org.json.JSONObject

class ScanBookModel {
    var title: String = ""
    var link: String = ""
    var image: String = ""
    var author: String = ""
    var price: String = ""
    var discount: String = ""
    var publisher: String = ""
    var pubdate: String = ""
    var isbn: String = ""
    var description: String = ""

    init {
    }

    constructor(jsonObject: JSONObject) {
        init(jsonObject)
    }

    fun init(json: JSONObject) {
        title = YoungsFunction.getStringFromJson(json,"title","").toString()
        link = YoungsFunction.getStringFromJson(json,"link","").toString()
        image = YoungsFunction.getStringFromJson(json,"image","").toString()
        author = YoungsFunction.getStringFromJson(json,"author","").toString()
        price = YoungsFunction.getStringFromJson(json,"price","").toString()
        discount = YoungsFunction.getStringFromJson(json,"discount","").toString()
        publisher = YoungsFunction.getStringFromJson(json,"publisher","").toString()
        pubdate = YoungsFunction.getStringFromJson(json,"pubdate","").toString()
        isbn = YoungsFunction.getStringFromJson(json,"isbn","").toString()
        description = YoungsFunction.getStringFromJson(json,"description","").toString()
    }
}