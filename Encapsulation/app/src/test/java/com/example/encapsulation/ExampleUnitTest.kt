package com.example.encapsulation

import org.junit.Test


class ExampleUnitTest {
    @Test
    fun main() {
        println("---------------------------------------------------")
        println("---------------------------------------------------")
        val account=BankAccount(1000.0)
        println("Initial Balance : ${account.getBalance()}")

        account.deposit(1000.0)
        println("Balance after deposit : ${account.getBalance()}")

        account.withdraw(400.0)
        println("Balance after withdraw : ${account.getBalance()}")

        println("---------------------------------------------------")
        println("---------------------------------------------------")

        // STUDENT
        val student=Student("HARIS","A")
        println("Name and grade is : ${student.getName() }:${student.getGrade()}")

        student.setName("HAMZA")
        student.setGrade("B")
        println("Set name and grade as : ${student.getName()}:${student.getGrade()}")

        println("---------------------------------------------------")
        println("---------------------------------------------------")

        // Product
        val product = Product ("WATCH" , 1500.0)
        println("Product name and price is : ${product.getName()}:${product.getPrice()}")

        product.setName("Glasses")
        product.setPrice(1000.0)
        println("Set the name and price as : ${product.getName()}:${product.getPrice()}")

        println("---------------------------------------------------")
        println("---------------------------------------------------")

        // Car
        val car=Car ("Toyota","Corolla",5000)
        println("Car's Make:${car.getMake()}  Model:${car.getModel()}  Mileage:${car.getMileage()}")

        car.setMake("Suzuki")
        car.setModel("Alto")
        car.setMileage(2000)
        println("Set the car's make:${car.getMake()} Model:${car.getModel()} Mileage:${car.getMileage()}")

        println("---------------------------------------------------")
        println("---------------------------------------------------")

        //Employee
        val employee=Employee("RAHEEL","Android Developer", 50000.0)
        println("Employee's Name:${employee.getName()} Position:${employee.getPosition()} Salary:${employee.getSalary()}")

        employee.setName("ZOHAIB")
        employee.setPosition("Web Developer")
        employee.setSalary(60000.0)
        println("Set the Employee's name as Name:${employee.getName()} Position:${employee.getPosition()} Salary:${employee.getSalary()}")

        println("---------------------------------------------------")
        println("---------------------------------------------------")
    }
}
