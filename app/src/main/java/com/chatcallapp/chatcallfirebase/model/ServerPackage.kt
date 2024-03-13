package com.chatcallapp.chatcallfirebase.model

import com.google.gson.annotations.SerializedName

class ServerPackage {
    @SerializedName("package_id")
    var packageId: String = ""

    @SerializedName("price")
    var price: String = ""

    @SerializedName("point")
    var point: Int = 0

    @SerializedName("production_id")
    var productionId: String = ""

    constructor(
        packageId: String = "",
        price: String = "",
        point: Int = 0,
        productId: String = ""
    ) {
        this.packageId = packageId
        this.point = point
        this.price = price
        this.productionId = productId
    }
}

