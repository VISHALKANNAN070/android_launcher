package com.notHuman.androidlauncher

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class HomeActivity : AppCompatActivity() {

    private lateinit var gestureDetector: GestureDetector
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.homeRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Drag & Drop Setup (Swipe Flags = 0 to prevent removal)
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(rv: RecyclerView, vh: RecyclerView.ViewHolder): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                val swipeFlags = 0
                return makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val from = vh.adapterPosition
                val to = target.adapterPosition
                adapter.onItemMove(from, to)
                val newOrder = adapter.appList.map { it.first }
                PinnedAppManager.saveAppList(this@HomeActivity, newOrder)
                return true
            }

            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {}
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Gesture Setup
        gestureDetector = GestureDetector(this, SwipeListener())

        val root = findViewById<View>(R.id.root)
        val touchListener = View.OnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }

        root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
        recyclerView.setOnTouchListener(touchListener)
    }

    override fun onResume() {
        super.onResume()
        loadPinnedApps()
    }

    private fun loadPinnedApps() {
        val pinnedPackages = PinnedAppManager.getPinnedApps(this)
        val appList = mutableListOf<Pair<String, Intent>>()
        val pm = packageManager

        for (pkg in pinnedPackages) {
            try {
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    appList.add(Pair(pkg, launchIntent))
                }
            } catch (e: Exception) {}
        }

        adapter = HomeAdapter(appList, pm)
        recyclerView.adapter = adapter
    }

    // --- ACCESSIBILITY HELPERS ---
    private fun lockScreen() {
        if (isAccessibilityEnabled()) {
            val intent = Intent(this, LockService::class.java)
            intent.action = "LOCK_NOW"
            startService(intent)
        } else {
            promptAccessibility()
        }
    }

    private fun openNotifications() {
        if (isAccessibilityEnabled()) {
            val intent = Intent(this, LockService::class.java)
            intent.action = "OPEN_NOTIFICATIONS"
            startService(intent)
        } else {
            promptAccessibility()
        }
    }

    private fun promptAccessibility() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
        Toast.makeText(this, "Enable 'Launcher Lock' service first.", Toast.LENGTH_LONG).show()
    }

    private fun isAccessibilityEnabled(): Boolean {
        val expectedComponentName = ComponentName(this, LockService::class.java)
        val enabledServicesSetting = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName) return true
        }
        return false
    }

    // --- UPDATED GESTURE LISTENER ---
    private inner class SwipeListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 50
        private val SWIPE_VELOCITY_THRESHOLD = 50

        override fun onDown(e: MotionEvent): Boolean = true

        override fun onDoubleTap(e: MotionEvent): Boolean {
            lockScreen()
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, vX: Float, vY: Float): Boolean {
            if (e1 == null) return false
            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            // 1. Vertical Swipe (Top to Bottom)
            if (abs(diffY) > abs(diffX) && abs(diffY) > SWIPE_THRESHOLD && abs(vY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                    // Down Swipe -> Open Notifications
                    openNotifications()
                    return true
                }
            }

            // 2. Horizontal Swipe (Right to Left)
            if (abs(diffX) > abs(diffY) && abs(diffX) > SWIPE_THRESHOLD && abs(vX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX < 0) {
                    // Left Swipe -> Open Drawer
                    startActivity(Intent(this@HomeActivity, MainActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    return true
                }
            }
            return false
        }
    }
}