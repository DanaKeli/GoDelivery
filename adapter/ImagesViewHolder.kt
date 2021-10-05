package ru.zipper.godelivery.view.adapter

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import ru.zipper.godelivery.databinding.CardViewContactBinding
import ru.zipper.godelivery.databinding.ItemImagesBinding
import ru.zipper.godelivery.model.models.ContactsItem

class ImagesViewHolder(private val binding: ItemImagesBinding):
RecyclerView.ViewHolder(binding.root) {
    var photoView: ImageView = binding.viewImage
    var btnRemove = binding.imageViewDeleteImage
}