package com.notHuman.androidlauncher

import android.content.Context

object PinnedAppManager {
    private const val PREF_NAME = "home_apps"
    private const val KEY_ORDER = "app_order_list"

    // Add an app to the list
    fun pinApp(context: Context, packageName: String) {
        val currentList = getPinnedApps(context).toMutableList()
        if (!currentList.contains(packageName)) {
            currentList.add(packageName)
            saveAppList(context, currentList)
        }
    }

    // THIS IS THE MISSING FUNCTION
    fun removeApp(context: Context, packageName: String) {
        val currentList = getPinnedApps(context).toMutableList()
        if (currentList.remove(packageName)) {
            saveAppList(context, currentList)
        }
    }

    // Helper to save the list order
    fun saveAppList(context: Context, apps: List<String>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val serialized = apps.joinToString(",")
        prefs.edit().putString(KEY_ORDER, serialized).apply()
    }

    // Helper to get the list
    fun getPinnedApps(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val serialized = prefs.getString(KEY_ORDER, "") ?: ""

        if (serialized.isEmpty()) return emptyList()

        return serialized.split(",").filter { it.isNotEmpty() }
    }
}