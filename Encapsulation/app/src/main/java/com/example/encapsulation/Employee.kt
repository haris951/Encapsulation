package com.example.encapsulation


class Employee(private var name:String,private var position: String,private var salary:Double) {
      fun getName():String{
          return name
      }
      fun getPosition():String{
          return position
      }
      fun getSalary():Double{
          return salary
      }
      fun setName(newName:String){
          name=newName
      }
      fun setPosition(newPosition: String){
          position=newPosition
      }
      fun setSalary(newSalary:Double){
          if (newSalary>=0) {
              salary=newSalary
          }
          else{
              throw IllegalArgumentException("Salary can only be set to a positive value")
          }

          }
      }
