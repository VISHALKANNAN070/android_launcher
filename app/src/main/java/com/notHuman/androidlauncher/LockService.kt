package com.notHuman.androidlauncher

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class LockService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "LOCK_NOW" -> {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            }
            "OPEN_NOTIFICATIONS" -> {
                performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            }
        }
        return START_STICKY
    }
}