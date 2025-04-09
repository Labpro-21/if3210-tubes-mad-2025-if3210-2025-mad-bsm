package com.mad.besokminggu.ui.home

import androidx.recyclerview.widget.DiffUtil
import com.mad.besokminggu.data.model.Song

class UpdateSong(
    private val oldList: List<Song>,
    private val newList: List<Song>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Cek apakah isinya sama persis
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
