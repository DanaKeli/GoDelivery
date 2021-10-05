package ru.zipper.godelivery.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.yandex.mapkit.search.ImageListener
import ru.zipper.godelivery.R
import ru.zipper.godelivery.databinding.ItemImagesBinding

class ImagesAdapter(
    private val listPhotos: MutableList<String>?
) : RecyclerView.Adapter<ImagesViewHolder>() {

    lateinit var context: Context

    lateinit var imageListener: ImageClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        context = parent.context
        return ImagesViewHolder(
            ItemImagesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {

        val uri = listPhotos?.get(position)
        val photoView = holder.photoView

        Glide.with(context)
            .load(uri)
            .apply(RequestOptions.overrideOf(80,80))
            .error(R.drawable.arrow)
            .into(photoView)

        holder.btnRemove.setOnClickListener {
            imageListener.remove(position)
        }
    }

    override fun getItemCount(): Int {
        if (listPhotos != null) {
            return listPhotos.size
        }
        return 0
    }
}

