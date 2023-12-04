package com.group7.studdibuddi

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.auth.FirebaseAuth
import com.group7.studdibuddi.databinding.ActivityMainBinding
import com.group7.studdibuddi.session.BaseActivity

class MainActivity : BaseActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var PROFILE_BUTTON_ID = 123
    private var LOG_IN_BUTTON_ID = 321
    private lateinit var PROFILE_BUTTON_TITLE: String
    private lateinit var LOG_IN_BUTTON_TITLE: String
    private lateinit var profileButton: MenuItem
    private lateinit var logInButton: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        PROFILE_BUTTON_TITLE = getString(R.string.profile)
        LOG_IN_BUTTON_TITLE = getString(R.string.log_in_button)

        if (!DatabaseUtil.isNetworkAvailable(this)){
            Toast.makeText(this,
                getString(R.string.please_check_your_internet_connection), Toast.LENGTH_SHORT).show()
        }

        // Database set up
        DatabaseUtil.initDatabase()

        binding.appBarMain.fab.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser != null) {
                val intent = Intent(this, PinActivity::class.java)
                startActivity(intent)
            } else {
                val intent: Intent?
                intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_t1, R.id.nav_t2, R.id.nav_t3
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        if (menu != null) {
            profileButton = menu.add(Menu.NONE, PROFILE_BUTTON_ID, Menu.NONE, PROFILE_BUTTON_TITLE)
        }
        if (menu != null) {
            logInButton = menu.add(Menu.NONE, LOG_IN_BUTTON_ID, Menu.NONE, LOG_IN_BUTTON_TITLE)
        }

        profileButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        logInButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)

        //modify toolbar to not include setting button
        menu?.removeItem(R.id.action_settings)

        return true
    }
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (FirebaseAuth.getInstance().currentUser != null) {
            menu.findItem(PROFILE_BUTTON_ID)?.isVisible = true
            menu.findItem(LOG_IN_BUTTON_ID)?.isVisible = false
        } else {
            menu.findItem(PROFILE_BUTTON_ID)?.isVisible = false
            menu.findItem(LOG_IN_BUTTON_ID)?.isVisible = true
        }

        profileButton.setOnMenuItemClickListener {item ->
            when (item.itemId) {
                PROFILE_BUTTON_ID -> {
                    //add another check layer
                    if (FirebaseAuth.getInstance().currentUser != null) {
                        val intent: Intent?
                        intent = Intent(this, UserProfileActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent: Intent?
                        intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    true
                }
                else -> {
                    false
                }
            }
        }

        logInButton.setOnMenuItemClickListener {item ->
            when (item.itemId) {
                LOG_IN_BUTTON_ID -> {
                    //add another check layer
                    if (FirebaseAuth.getInstance().currentUser != null) {
                        val intent: Intent?
                        intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent: Intent?
                        intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    true
                }
                else -> {
                    false
                }
            }
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}