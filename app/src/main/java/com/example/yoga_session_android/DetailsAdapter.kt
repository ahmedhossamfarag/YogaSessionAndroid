package com.example.yoga_session_android

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.yoga_session_android.lib.Segment

class DetailsAdapter(val context: Context, val details: Array<Segment>) : BaseAdapter() {
    override fun getCount(): Int = details.size

    override fun getItem(position: Int): Any? = details[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        val item = details[position]
        var view = convertView
        var holder: ViewHolder
        if(view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.details_list_item, parent, false)
            holder = ViewHolder(
                view.findViewById(R.id.name_text_view),
                view.findViewById(R.id.duration_text_view),
                view.findViewById(R.id.loop_icon),
                view.findViewById(R.id.loop_count_text_view)
            )
            view.tag = holder
        }else{
            holder = view.tag as ViewHolder
        }
        holder.name.text = item.name
        holder.duration.text = view.context.getString(R.string.duration_seconds, item.durationSec)
        holder.loopIcon.visibility = if (item.loopable) View.VISIBLE else View.INVISIBLE
        holder.loopCount.visibility = holder.loopIcon.visibility
        holder.loopCount.text = item.loopCount.toString()
        return view
    }

    private data class ViewHolder(
        val name: TextView,
        val duration: TextView,
        val loopIcon: ImageView,
        val loopCount: TextView
    )

}