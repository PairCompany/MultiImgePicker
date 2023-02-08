package com.jhc.multiimagepicker.extenstion

import android.net.Uri
import java.io.File

fun Uri.deleteFile(): Boolean? =
    this.path?.let {
        File(it).delete()
    }

