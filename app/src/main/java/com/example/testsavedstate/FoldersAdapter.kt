package com.example.testsavedstate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FoldersAdapter(private var folders: List<Folder>, var mOnViewHolderClickListener: OnViewHolderClickListener) :
    RecyclerView.Adapter<FoldersAdapter.FolderViewHolder>() {

    class FolderViewHolder(
        itemView: View,
        var onViewHolderClickListener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init{
            itemView.setOnClickListener(this)
        }
        var fileImage: ImageView = itemView.findViewById(R.id.fileTypeImage)
        var fileName: TextView = itemView.findViewById(R.id.fileName)
        var fileSize: TextView = itemView.findViewById(R.id.fileSize)
        var fileLastModifiedDate: TextView = itemView.findViewById(R.id.fileDate)
        override fun onClick(v: View?) {
            onViewHolderClickListener.onViewHolderClick(adapterPosition)
        }
    }

    override fun getItemCount(): Int = folders.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        FolderViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.folder,
                parent,
                false
            ), mOnViewHolderClickListener
        )

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        holder.fileImage.setImageResource(folder.typeImage)
        holder.fileName.text = folder.name
        holder.fileSize.text = folder.size.toString()
        holder.fileLastModifiedDate.text = folder.lastModifiedDate


    }

}
