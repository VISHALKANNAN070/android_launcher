package com.notHuman.androidlauncher

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val root = findViewById<View>(R.id.root)
        root.setOnApplyWindowInsetsListener { v, insets ->
            v.setPadding(0, insets.systemWindowInsetTop, 0, insets.systemWindowInsetBottom)
            insets
        }

        val searchBar = findViewById<EditText>(R.id.searchBar)
        val recycler = findViewById<RecyclerView>(R.id.appList)
        recycler.layoutManager = LinearLayoutManager(this)

        val pm = packageManager
        val startupIntent = Intent(Intent.ACTION_MAIN)
        startupIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val activities = pm.queryIntentActivities(startupIntent, 0)
        Collections.sort(activities, ResolveInfo.DisplayNameComparator(pm))

        adapter = AppAdapter(activities, pm)
        recycler.adapter = adapter

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
        })
    }
}

class AppAdapter(
    private val fullAppList: List<ResolveInfo>,
    private val pm: PackageManager
) : RecyclerView.Adapter<AppAdapter.Holder>() {

    private var filteredList: List<ResolveInfo> = fullAppList

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.appName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_app, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val resolveInfo = filteredList[position]
        holder.name.text = resolveInfo.loadLabel(pm)

        holder.itemView.setOnClickListener {
            val activityInfo = resolveInfo.activityInfo
            val componentName = ComponentName(activityInfo.packageName, activityInfo.name)

            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.component = componentName
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED

            holder.itemView.context.startActivity(intent)
        }

        val pkg = resolveInfo.activityInfo.packageName
        holder.itemView.setOnLongClickListener { view ->
            val context = view.context
            val popup = PopupMenu(context, view)

            popup.menu.add("Add to Home")
            popup.menu.add("App Info")
            // Uninstall removed

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Add to Home" -> {
                        PinnedAppManager.pinApp(context, pkg)
                        Toast.makeText(context, "Added to Home", Toast.LENGTH_SHORT).show()
                    }
                    "App Info" -> {
                        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = android.net.Uri.parse("package:$pkg")
                        context.startActivity(intent)
                    }
                }
                true
            }
            popup.show()
            true
        }
    }

    override fun getItemCount() = filteredList.size

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            fullAppList
        } else {
            fullAppList.filter {
                it.loadLabel(pm).toString().contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}