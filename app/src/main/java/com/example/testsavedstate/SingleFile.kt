package com.example.testsavedstate


import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE

/**
 * A simple [Fragment] subclass.
 */
class SingleFile(var files: Folder) : Fragment() {

    companion object {
        const val TAG = "SingleFile"
        var currentFile: Folder? = null
    }

    //    lateinit var currentFile: Folder
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_single_file, container, false)
        root.findViewById<ImageView>(R.id.Image).setImageResource(files.typeImage)
        root.findViewById<TextView>(R.id.Name).text = files.name
        root.findViewById<TextView>(R.id.Size).text = files.size.toString()
        root.findViewById<TextView>(R.id.Date).text = files.lastModifiedDate
        root.findViewById<TextView>(R.id.Path).text = files.path
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        currentFile = files
    }
}