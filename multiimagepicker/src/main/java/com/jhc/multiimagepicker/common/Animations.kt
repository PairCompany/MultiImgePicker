package com.jhc.multiimagepicker.common

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

object Animations {

    fun slide(view: View, isVertical: Boolean, currentDimen: Int, newDimen: Int){
        val valueAnimator = ValueAnimator.ofInt(currentDimen, newDimen).apply {
            addUpdateListener {
                if(it.animatedValue as Int == 0) return@addUpdateListener

                if(isVertical) view.layoutParams.height = it.animatedValue as Int
                else view.layoutParams.width = it.animatedValue as Int
                view.requestLayout()
            }
        }

        AnimatorSet().apply {
            interpolator = AccelerateDecelerateInterpolator()
            play(valueAnimator)
        }.start()
    }

    fun slideShow(view: View, isVertical: Boolean, showDimen: Int){
        slide(view, isVertical, 0, showDimen)
    }

    fun slideHide(view: View, isVertical: Boolean, showDimen: Int){
        slide(view, isVertical, showDimen, 0)
    }

}