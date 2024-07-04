package com.example.encapsulation

class Car(private var make:String,private var model:String,private var mileage:Int) {
    fun getMake():String{
        return make
    }
    fun getModel():String{
        return model
    }
    fun getMileage():Int{
        return mileage
    }
    fun setMake(newMake: String){
        make=newMake
    }
    fun setModel(newModel: String){
        model=newModel
    }
    fun setMileage(newMileage:Int){
        if (newMileage>=0){
            mileage=newMileage
        }
        else{
            throw IllegalArgumentException("Mileage cannot be negative")

        }

    }

}