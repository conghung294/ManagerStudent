package com.example.studentmanager.ui

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var studentDao: StudentDao
    private lateinit var studentListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private var students: List<Student> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = AppDatabase.getDatabase(this)
        studentDao = db.studentDao()

        studentListView = findViewById(R.id.studentListView)
        loadStudents()

        registerForContextMenu(studentListView)

        studentListView.setOnItemClickListener { _, _, position, _ ->
            val selectedStudent = students[position]
            openEditStudent(selectedStudent)
        }
    }

    private fun loadStudents() {
        CoroutineScope(Dispatchers.IO).launch {
            students = studentDao.getAllStudents()
            val studentNames = students.map { "${it.name} (${it.studentId})" }

            runOnUiThread {
                adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, studentNames)
                studentListView.adapter = adapter
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_new -> {
                openAddStudent()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openAddStudent() {
        val intent = Intent(this, AddStudentActivity::class.java)
        startActivity(intent)
    }

    private fun openEditStudent(student: Student) {
        val intent = Intent(this, EditStudentActivity::class.java)
        intent.putExtra("student", student)
        startActivity(intent)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val selectedStudent = students[info.position]

        return when (item.itemId) {
            R.id.edit -> {
                openEditStudent(selectedStudent)
                true
            }
            R.id.remove -> {
                removeStudent(selectedStudent)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun removeStudent(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Remove Student")
            .setMessage("Are you sure you want to remove ${student.name}?")
            .setPositiveButton("Yes") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    studentDao.deleteStudent(student)
                    loadStudents()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}
