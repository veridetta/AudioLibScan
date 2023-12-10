package com.vr.audiolibscan.ui.auth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.vr.audiolibscan.MainActivity
import com.vr.audiolibscan.R
import com.vr.audiolibscan.ui.LoginActivity
import com.vr.audiolibscan.ui.auth.fragment.AddFragment
import com.vr.audiolibscan.ui.auth.fragment.HomeFragment

class AdminActivity : AppCompatActivity() {
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        initView()
        fragmentSetup()
        clickView()
    }

    fun initView(){
        fragmentContainer = findViewById(R.id.fragmentContainer)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
    }

    fun fragmentSetup(){
        val homeFragment = HomeFragment()
        //dapatkan intent dari activity sebelumnya
        val intent = intent
        //dapatkan data dari intent
        val fragment = intent.getStringExtra("fragment")
        if (fragment != null) {
            if (fragment == "home") {
                val homeFragment = HomeFragment()
                replaceFragment(homeFragment)
                bottomNavigationView.selectedItemId = R.id.menu_home
            }else if (fragment == "add") {
                val addFragment = AddFragment()
                replaceFragment(addFragment)
                bottomNavigationView.selectedItemId = R.id.menu_add
            }
        }else{
            replaceFragment(homeFragment)
        }
    }

    fun clickView(){
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    val homeFragment = HomeFragment()
                    replaceFragment(homeFragment)
                    true
                }
                R.id.menu_add -> {
                    val addFragment = AddFragment()
                    replaceFragment(addFragment)
                    true
                }

                R.id.menu_logout -> {
                    // Hapus shared preferences
                    val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()

                    // Arahkan ke MainActivity dengan membersihkan stack aktivitas
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                    true
                }

                else -> false
            }
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}