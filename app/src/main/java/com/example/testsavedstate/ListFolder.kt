package com.example.testsavedstate


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat

/**
 * A simple [Fragment] subclass.
 */
class ListFolder(
    var mOnAdapterChangeListener: OnAdapterChangeListener,
    var mOnFileChangedListener: OnFileChangedListener,var mDetailPaneVisibility: DetailPaneVisibility
) : Fragment(), OnViewHolderClickListener {
    companion object {
        var formatter = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        val document = arrayListOf("doc", "txt", "docx", "pdf", "html", "ppt", "xlxs", "mhtml")
        val audio = arrayListOf("mp3", "wav")
        val video = arrayListOf("mp4", "avi", "mkv")
        val image = arrayListOf("png", "jpeg", "gif", "jpg")
    }

    private lateinit var rootView: View
    private lateinit var recyclerView: RecyclerView
    lateinit var path: String
    var folders = ArrayList<Folder>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_list, container, false)
        recyclerView = rootView.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this.context)


        if (!checkFolders())
            Toast.makeText(this.context, "Doesn't Exists", Toast.LENGTH_SHORT).show()
        else
            recyclerView.adapter = FoldersAdapter(folders, this)

        return rootView
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val bundle: Bundle = it
            path = bundle.getString("path")!!
        }
    }

    private fun checkFolders(): Boolean {
        val checkPath = File(path)
        if (checkPath.exists()) {
            if (checkPath.isDirectory && checkPath.listFiles() != null) {
                folders = directoryToFolder(checkPath.listFiles()!!)
                return true
            }
        } else {
            Toast.makeText(this.context, "Empty", Toast.LENGTH_SHORT).show()
            return false
        }
        return false
    }

    private fun directoryToFolder(listFiles: Array<File>): ArrayList<Folder> {
        val arrayListFolders = ArrayList<Folder>()
        var typeImage: TypeImage
        listFiles.forEach {
            if (it.isDirectory) typeImage = TypeImage.FOLDER
            else {
                it.toString().apply {
                    val type = this.substring(this.lastIndexOf('.') + 1)
                    typeImage = if (type in document)
                        TypeImage.DOCUMENT
                    else if (type in audio)
                        TypeImage.AUDIO
                    else if (type in video)
                        TypeImage.VIDEO
                    else if (type in image)
                        TypeImage.IMAGE
                    else TypeImage.OTHERS
                }
            }
            arrayListFolders.add(
                Folder(
                    it.name,
                    typeImage.toString(),
                    typeImage.resID,
                    calculateSpace(it),
                    formatToDate(it.lastModified()),
                    it.path
                )
            )
        }
        return arrayListFolders
    }

    private fun formatToDate(lastModified: Long) = formatter.format(lastModified)

    private fun calculateSpace(file: File): Long {
        var size = 0L
        if (file.isDirectory) {
            for (i in file.listFiles()!!) {
                size += calculateSpace(i)
            }
        } else {
            return file.length()
        }
        return size
    }

    override fun onViewHolderClick(position: Int) {
        path = folders[position].path


        if (checkFolders()) {
            mOnAdapterChangeListener.onAdapterChangeListener(path)
            recyclerView.adapter = FoldersAdapter(folders, this)
        } else {
            path=File(path).parent!!
            val fragment = SingleFile()
            val bundle = Bundle()
            bundle.putString("name", folders[position].name)
            bundle.putString("type", folders[position].type)
            bundle.putInt("image", folders[position].typeImage)
            bundle.putLong("size", folders[position].size)
            bundle.putString("date", folders[position].lastModifiedDate)
            bundle.putString("path", folders[position].path)

            mOnFileChangedListener.onFileChanged(bundle)

            fragment.arguments = bundle
            if (activity!!.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.masterContainer, fragment)
                    .addToBackStack("detail3").commit()

            } else {
                mDetailPaneVisibility.DetailVisiblity(true)
                activity!!.supportFragmentManager.beginTransaction().replace(
                    R.id.detailContainer,
                    fragment
                )
//                    .addToBackStack("detail4")
                    .commit()
            }
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (activity!!.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    val frag =
                        activity!!.supportFragmentManager.findFragmentById(R.id.detailContainer)
                    frag?.let {

                        activity!!.supportFragmentManager.popBackStack(0, POP_BACK_STACK_INCLUSIVE)
                        mDetailPaneVisibility.DetailVisiblity(false)
                        mOnFileChangedListener.onFileChanged(null)
                    }
                }
                if (File(path).parentFile != null) {
                    path = File(path).parent!!
                    mOnAdapterChangeListener.onAdapterChangeListener(path)
                    folders.clear()
                    recyclerView.adapter = FoldersAdapter(folders, this@ListFolder)
                    if (checkFolders()) {
                        recyclerView.adapter = FoldersAdapter(folders, this@ListFolder)

                    } else activity!!.finish()

                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

}

