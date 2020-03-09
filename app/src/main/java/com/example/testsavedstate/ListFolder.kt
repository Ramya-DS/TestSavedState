package com.example.testsavedstate


import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat

/**
 * A simple [Fragment] subclass.
 */
class ListFolder() : Fragment(), OnViewHolderClickListener {

    companion object {
        var formatter = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        val document = arrayListOf("doc", "txt", "docx", "pdf", "html", "ppt", "xlxs", "mhtml")
        val audio = arrayListOf("mp3", "wav")
        val video = arrayListOf("mp4", "avi", "mkv")
        val image = arrayListOf("png", "jpeg", "gif", "jpg")
    }

    private lateinit var rootView: View
    private lateinit var recyclerView: RecyclerView
    var path: File=Environment.getExternalStorageDirectory()
    var folders= ArrayList<Folder>()

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
        val bundle: Bundle = arguments!!
        path=File(bundle.getString("path")!!)
    }

    private fun checkFolders(): Boolean {
        if (path.exists()) {
            if (path.isDirectory && path.listFiles() != null) {
                folders = directoryToFolder(path.listFiles()!!)
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
        path = File(folders[position].path)

        if (checkFolders()) {
            recyclerView.adapter = FoldersAdapter(folders, this)
        } else {
            path = path.parentFile!!
            if (activity!!.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.masterContainer, SingleFile(folders[position]))
                    .addToBackStack("detail3").commit()

            } else {
                activity!!.supportFragmentManager.beginTransaction().replace(
                    R.id.detailContainer,
                    SingleFile(folders[position])
                ).addToBackStack("detail4").commit()
            }
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                if(currentFile!=null)
//                {
//                    activity!!.supportFragmentManager.popBackStack(0,POP_BACK_STACK_INCLUSIVE)
//                    currentFile =null
//                    return
//                }
                if (path.parentFile != null) {
                    path = path.parentFile!!
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
