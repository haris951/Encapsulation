package com.example.encapsulation

class Student(private var name: String, private var grade: String) {

    // Getter method for name
    fun getName(): String {
        return name
    }
    // Getter method for grade
    fun getGrade(): String {
        return grade
    }
    // Method to set the name
    fun setName(newName: String) {
        name = newName
    }
    // Method to set the Grade
    fun setGrade(newGrade: String) {
        validateGrade(newGrade)
        grade = newGrade
    }
    // Method to ensure that grade must be A,B,C,D,F
    private fun validateGrade(grade: String) {
        val validGrades = setOf("A", "B", "C", "D", "F")
        require(grade in validGrades) {
            "Invalid grade: $grade. Grade must be one of $validGrades."
        }
    }
}
