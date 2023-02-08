package com.jhc.multiimagepicker.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jhc.multiimagepicker.builder.MultiImagePicker
import com.jhc.multiimagepicker.extenstion.deleteFile

internal class CamImagesPickerViewModel(builder: MultiImagePicker.Builder): ViewModel() {

    var images = MutableLiveData(builder.selectedImages.toMutableList())

    val imagesSize = Transformations.map(images){ it.size }

    var isTorchOn = MutableLiveData(false)

    fun addImage(uri: Uri){
        images.value = images.value?.apply { add(0, uri) }?.toMutableList()
    }

    fun clearImages(){
        if(images.value?.isNotEmpty() == true) {
            images.value?.forEach { it.deleteFile() }
            images.value = mutableListOf()
        }
    }

    fun checkMinCount(min: Int?): Boolean =
        (images.value?.size ?: 0) >= (min ?: 0)

    fun checkMaxCount(max: Int?): Boolean =
        (images.value?.size ?: 0) < (max ?: 0)

    @Suppress("UNCHECKED_CAST")
    class Factory(private val builder: MultiImagePicker.Builder): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CamImagesPickerViewModel(builder) as T
        }
    }

}