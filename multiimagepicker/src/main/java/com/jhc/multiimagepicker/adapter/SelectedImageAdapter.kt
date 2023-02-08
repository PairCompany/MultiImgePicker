package com.jhc.multiimagepicker.adapter

import android.content.res.Configuration
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.jhc.multiimagepicker.R
import com.jhc.multiimagepicker.common.Animations
import com.jhc.multiimagepicker.databinding.ItemSelectedImageBinding
import com.jhc.multiimagepicker.extenstion.deleteFile

class UrisAdapter() : ListAdapter<Uri, UrisAdapter.ViewHolder>(DiffUtilCallback()) {

    var imgDelete: (Uri?) -> Unit = {}

    private var recyclerView: RecyclerView? = null
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCurrentListChanged(
        previousList: MutableList<Uri>,
        currentList: MutableList<Uri>
    ) {
        super.onCurrentListChanged(previousList, currentList)

        if((currentList.size == 0 && previousList.size == 1) || (previousList.size == 0 && currentList.size == 1)){
            recyclerView?.let { recyclerView ->
                val isVertical = recyclerView.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                val rvLength = recyclerView.resources.getDimensionPixelSize(R.dimen.selected_images_recyclerview_length)

                if(previousList.size == 0) Animations.slideShow(recyclerView, isVertical, rvLength)
                else Animations.slideHide(recyclerView, isVertical, rvLength)
            }
        } else if(currentList.size - previousList.size == 1){
            recyclerView?.smoothScrollToPosition(0)
        }
    }

    inner class ViewHolder(var binding: ItemSelectedImageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri?) {
            Glide.with(binding.root)
                .load(uri)
                .transform(CenterCrop())
                .into(binding.imageView)

            binding.btnCancel.setOnClickListener {
                imgDelete(uri)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemSelectedImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).apply {}
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        //code to show recyclerViews childView when animate slide up
        holder.itemView.layoutParams.height = holder.itemView.resources.getDimensionPixelSize(R.dimen.selected_image_length)
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<Uri>(){
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean = oldItem == newItem

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean = oldItem == newItem
    }

}

@BindingAdapter("images")
fun RecyclerView?.setImages(
    images: MutableLiveData<MutableList<Uri>>
) {
    this ?: return

    val adapter = (adapter as? UrisAdapter) ?: UrisAdapter()
        .apply {
            this.imgDelete = {
                images.value = images.value?.apply { remove(it) }
                submitList(images.value?.toMutableList())
                it?.deleteFile()
            }
            adapter = this
        }

    adapter.submitList(images.value?.toMutableList())
}