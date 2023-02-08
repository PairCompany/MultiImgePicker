package com.jhc.multiimagepicker.common

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.StringRes
import com.jhc.multiimagepicker.common.Toast.toastMakeText

object Toast{

    private var app: Application? = null

    private var toast: Toast? = try{ Toast.makeText(app, "", Toast.LENGTH_SHORT) }catch(e: Exception){ null }

    fun setApplication(application: Application?){
        app = application
    }

    fun <T> toastMakeText(msg: T, duration: Int){
        //call by mainThread
        if(Looper.myLooper() == Looper.getMainLooper()){
            toast?.cancel()
            if (msg is String) toast = Toast.makeText(app, msg, duration)
            else if (msg is Int) toast = Toast.makeText(app, msg, duration)
            toast?.show()
        }else{
            Handler(Looper.getMainLooper()).postDelayed(
                Runnable {
                    toast = Toast.makeText(app, "", duration)
                    toastMakeText(msg, duration)
                }, 0
            )
        }

    }

}

fun toast(@StringRes msgResId: Int, duration: Int = Toast.LENGTH_SHORT){
    toastMakeText(msgResId, duration)
}

fun toast(msg: String, duration: Int = Toast.LENGTH_SHORT){
    toastMakeText(msg, duration)
}
