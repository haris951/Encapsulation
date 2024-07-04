package com.example.encapsulation

class BankAccount(private var balance : Double) {

    //Getter method for balance
    fun getBalance():Double{
        return balance
    }
    //Method to deposit money
    fun deposit(amount:Double){
        if (amount>0){
            balance += amount
        }
    }
    //Method to withdraw money
    fun withdraw(amount: Double){
        if (amount>0 && amount<=balance){
            balance -= amount
        }
    }
}