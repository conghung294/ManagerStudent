package com.example.studentmanager.database

import androidx.room.*

@Dao
interface StudentDao {
    @Query("SELECT * FROM students")
    fun getAllStudents(): List<Student>

    @Insert
    suspend fun addStudent(student: Student)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)
}
