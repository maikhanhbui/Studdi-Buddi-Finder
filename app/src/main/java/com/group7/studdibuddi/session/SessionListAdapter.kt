package com.group7.studdibuddi.session

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.group7.studdibuddi.R
import java.text.DecimalFormat

class SessionListAdapter(private val context: Context, private var sessionList: List<Session>) : BaseAdapter(){

    override fun getItem(position: Int): Any {
        return sessionList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return sessionList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.session_item,null)

        val sessionImage: ImageView = view.findViewById(R.id.session_im_view)
        val sessionName: TextView = view.findViewById(R.id.session_name)
        val sessionDescription: TextView = view.findViewById(R.id.session_description)

        //TODO: Set image of the session here
        sessionName.text = sessionList[position].sessionName
        sessionDescription.text = sessionList[position].description

        return view
    }

    fun replace(newSessionList: List<Session>){
        sessionList = newSessionList
    }

}