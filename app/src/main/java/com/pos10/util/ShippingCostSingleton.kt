package com.pos10.util

object ShippingCostSingleton{
    var isEdit:Boolean =false
    var name:String=""
    var isFreeShipping:String=""
    var id=0
    var shippingcost=""
    var shippingprice=""
    var itemRange=""
    var itemPriceTax:String=""
    var itemRateTax:String=""
    var addTaxToogle:Int=0
    var addTaxValue:String=""

    fun clear(){
        name=""
        isFreeShipping=""
        itemPriceTax =""
        itemRateTax =""
        addTaxValue =""
        addTaxToogle =0
        itemRange=""
    }
}