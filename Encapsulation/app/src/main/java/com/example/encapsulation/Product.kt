package com.example.encapsulation

class Product(private var name:String, private var price:Double) {
    fun getName():String{
        return name
    }
    fun getPrice():Double{
        return price
    }
    fun setName(newName: String) {
        name = newName
    }
    fun setPrice(newPrice:Double){
        if(newPrice>=0){
            price=newPrice
        }
        else {
            throw IllegalArgumentException("Price must be positive")
        }
    }

}