package com.notHuman.androidlauncher

import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections
import kotlin.math.abs

class HomeAdapter(
    val appList: MutableList<Pair<String, Intent>>,
    private val pm: PackageManager
) : RecyclerView.Adapter<HomeAdapter.Holder>() {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.appName)
        var startX: Float = 0f
        var startY: Float = 0f
        var isLongPress: Boolean = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_app, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val (pkg, intent) = appList[position]

        try {
            holder.name.text = pm.getApplicationInfo(pkg, 0).loadLabel(pm)
        } catch (e: Exception) {
            holder.name.text = pkg
        }

        // Smart Touch Listener
        holder.itemView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    holder.isLongPress = false
                    holder.startX = event.rawX
                    holder.startY = event.rawY
                    false
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (holder.isLongPress) return@setOnTouchListener false

                    val diffX = abs(event.rawX - holder.startX)
                    val diffY = abs(event.rawY - holder.startY)
                    val touchSlop = ViewConfiguration.get(view.context).scaledTouchSlop

                    if (diffX < touchSlop && diffY < touchSlop) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        view.context.startActivity(intent)
                    }
                    false
                }
                else -> false
            }
        }

        // Long Press Menu (Uninstall Removed)
        holder.itemView.setOnLongClickListener { view ->
            holder.isLongPress = true

            val popup = PopupMenu(view.context, view)
            popup.menu.add("App Info")
            popup.menu.add("Remove")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                popup.menu.add("Popup View")
            }

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Popup View" -> {
                        try {
                            val popupIntent = Intent(intent)
                            popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            val options = ActivityOptions.makeBasic()
                            val bounds = Rect(100, 100, 900, 1500)
                            options.launchBounds = bounds
                            view.context.startActivity(popupIntent, options.toBundle())
                        } catch (e: Exception) {
                            Toast.makeText(view.context, "Popup not allowed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    "App Info" -> {
                        val infoIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        infoIntent.data = Uri.parse("package:$pkg")
                        view.context.startActivity(infoIntent)
                    }
                    "Remove" -> {
                        PinnedAppManager.removeApp(view.context, pkg)
                        appList.removeAt(holder.adapterPosition)
                        notifyItemRemoved(holder.adapterPosition)
                        Toast.makeText(view.context, "Removed", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }
            popup.show()
            true
        }
    }

    override fun getItemCount() = appList.size

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(appList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(appList, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    fun onItemDismiss(position: Int) {}
}