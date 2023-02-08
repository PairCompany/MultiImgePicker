package com.jhc.multiimagepicker.builder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import com.gun0912.tedonactivityresult.TedOnActivityResult
import com.jhc.multiimagepicker.common.Const
import com.jhc.multiimagepicker.ui.CamImagePickerActivity
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.File
import java.util.*

class MultiImagePicker {

    companion object {
        @JvmStatic
        fun with(context: Context) = Builder(context)
    }

    @Parcelize
    class Builder(
        @IgnoredOnParcel
        private var context: Context? = null,
        var showCount: Boolean = true,
        internal var minCount: Int = 0,
        internal var maxCount: Int = Int.MAX_VALUE,
        var textConfirm: String? = null,
        var textUnderMin: String? = null,
        var textOverMax: String? = null,
        var isLensFacingBack: Boolean = true,
        var lensFacingSwitcher: Boolean = true,
        internal var orientationVertical: Boolean = true,
        internal var orientationFix: Boolean = false,
        internal var selectedImages: List<Uri> = listOf(),
        internal var filePath: File? = null,
        internal var fileName: (Int) -> String = { UUID.randomUUID().toString() }
    ) : Parcelable {

        fun showCount(value: Boolean): Builder = apply{ showCount = value }

        fun min(value: Int): Builder = apply { minCount = value }

        fun max(value: Int): Builder = apply { maxCount = value }

        fun getMinMaxText(): String = "$minCount ~ $maxCount"

        /**
         *  @param confirm text of confirm button
         *  @param underMin text of under minimum toast
         *
         *  '%d' will be change to minCount
         *
         *  ex) "under minimum (%d)" -> "under minimum (2)"
         *  @param overMax text of over maximum toast
         *
         *  '%d' will be change to maxCount
         *
         *  ex) "over maximum (%d)" -> "over maximum (2)"
         */
        fun setTexts(
            confirm: String? = null,
            underMin: String? = null,
            overMax: String? = null
        ): Builder = apply {
            textConfirm = confirm
            textUnderMin = underMin
            textOverMax = overMax
        }

        fun isLensFacingBack(value: Boolean): Builder = apply { isLensFacingBack = value }

        fun lensFacingSwitcher(value: Boolean): Builder = apply { lensFacingSwitcher = value }

        fun orientationFix(isFix: Boolean, isVertical: Boolean): Builder = apply {
            orientationFix = isFix
            orientationVertical = isVertical
        }

        fun selectedImages(images: List<Uri>): Builder = apply{
            this.selectedImages = images
        }

        /**
         *  @param filePath file directory to save image default(ContextWrapper.cacheDir)
         *  @param fileName lambda that receive imageIndex(begin 0) and return fileName
         */
        fun filePath(filePath: File, fileName: (Int) -> String): Builder = apply{
            this.filePath = filePath
            this.fileName = fileName
        }

        fun with(context: Context): Builder = apply { this.context = context }

        fun startGetImages(action: (List<Uri>) -> Unit) {

            TedOnActivityResult.with(context)
                .setIntent(
                    Intent(
                        context,
                        CamImagePickerActivity::class.java
                    ).apply { putExtra(Const.EXTRA_BUILDER, this@Builder) }
                )
                .setListener { resultCode, data ->
                    if (resultCode == Activity.RESULT_OK) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            data.getParcelableArrayListExtra(Const.EXTRA_SELECTED_URIS, Uri::class.java)
                        }else {
                            @Suppress("DEPRECATION")
                            data.getParcelableArrayListExtra<Uri>(Const.EXTRA_SELECTED_URIS)
                        }
                            ?.let { action(it) }
                    } else if (resultCode == Activity.RESULT_CANCELED) {

                    }
                }
                .startActivityForResult()

        }

    }

}