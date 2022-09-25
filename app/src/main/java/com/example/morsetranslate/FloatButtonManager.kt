package com.example.morsetranslate

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FloatButtonManager(context: Context) {

    private var mClicked = false
    private val mComtext = context

    private val rotateOpen : Animation by lazy { AnimationUtils.loadAnimation(mComtext, R.anim.rotate_open_anim) }
    private val rotateClose : Animation by lazy { AnimationUtils.loadAnimation(mComtext, R.anim.rotate_close_anim) }
    private val fromBottom : Animation by lazy { AnimationUtils.loadAnimation(mComtext, R.anim.from_bottom_anim) }
    private val toBottom : Animation by lazy { AnimationUtils.loadAnimation(mComtext, R.anim.to_bottom_anim) }

    private lateinit var mFabPlus : FloatingActionButton
    private lateinit var mFabCamera : FloatingActionButton
    private lateinit var mFabGallery : FloatingActionButton

    fun onPlusButtonClicked(fabPlus : FloatingActionButton, fabCamera : FloatingActionButton, fabGallery : FloatingActionButton){
        mFabPlus = fabPlus
        mFabCamera = fabCamera
        mFabGallery = fabGallery

        setVisibility(mClicked)
        setAnimation(mClicked)
        setClickable(mClicked)

        mClicked = !mClicked
    }



    // cambiar visibilidad de los botones
    private fun setVisibility(mClicked: Boolean) {
        if (!mClicked){
            mFabCamera.visibility = View.VISIBLE
            mFabGallery.visibility = View.VISIBLE
        }else{
            mFabCamera.visibility = View.GONE
            mFabGallery.visibility = View.GONE
        }
    }

    // pasar las animaciones al click
    private fun setAnimation(mClicked: Boolean) {
        if (!mClicked){
            mFabCamera.startAnimation(fromBottom)
            mFabGallery.startAnimation(fromBottom)
            mFabPlus.startAnimation(rotateOpen)
        }else{
            mFabCamera.startAnimation(toBottom)
            mFabGallery.startAnimation(toBottom)
            mFabPlus.startAnimation(rotateClose)
        }
    }

    // quita el click cuando los botones no esten visibles
    private fun setClickable(mClicked: Boolean) {
        if (!mClicked){
            mFabCamera.isClickable = true
            mFabGallery.isClickable = true
        }else{
            mFabCamera.isClickable = false
            mFabGallery.isClickable = false
        }
    }
}