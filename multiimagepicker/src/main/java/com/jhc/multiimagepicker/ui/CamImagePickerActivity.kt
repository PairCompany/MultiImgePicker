package com.jhc.multiimagepicker.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import androidx.activity.viewModels
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.jhc.multiimagepicker.builder.MultiImagePicker
import com.jhc.multiimagepicker.common.Const
import com.jhc.multiimagepicker.common.Sound
import com.jhc.multiimagepicker.common.Toast
import com.jhc.multiimagepicker.common.toast
import com.jhc.multiimagepicker.databinding.ActivityCamImagesPickerBinding
import com.jhc.multiimagepicker.viewmodel.CamImagesPickerViewModel
import com.jhc.multiimagepicker.R

internal class CamImagePickerActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCamImagesPickerBinding.inflate(layoutInflater) }

    val viewModel: CamImagesPickerViewModel by viewModels { CamImagesPickerViewModel.Factory(builder) }

    val builder by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableExtra(Const.EXTRA_BUILDER, MultiImagePicker.Builder::class.java)
                ?: MultiImagePicker.Builder()
        else
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<MultiImagePicker.Builder>(Const.EXTRA_BUILDER)
                ?: MultiImagePicker.Builder()
    }

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private var camera: Camera? = null

    private val imageCapture = ImageCapture.Builder().build()

    private val sound = Sound()

    private fun initOrientation() {
        this.requestedOrientation =
            if (builder.orientationFix) {
                if (builder.orientationVertical) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            binding.apply {
                activity = this@CamImagePickerActivity
                viewModel = this@CamImagePickerActivity.viewModel
                lifecycleOwner = this@CamImagePickerActivity
            }.root
        )

        Toast.setApplication(this.application)

        isLensFacingBack = builder.isLensFacingBack

        cameraProviderFuture = ProcessCameraProvider.getInstance(this).apply {
            addListener(Runnable {
                val cameraProvider = cameraProviderFuture.get()
                val lensFacing =
                    if (builder.isLensFacingBack) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
                camera =
                    bindPreview(cameraProvider = cameraProvider, lensFacing = lensFacing).apply {
                        cameraControl.enableTorch(viewModel.isTorchOn.value ?: false)
                    }


                initOrientation()
            }, ContextCompat.getMainExecutor(this@CamImagePickerActivity))
        }

        viewModel.isTorchOn.observe(this) {
            binding.btnFlash.setImageResource(
                if (it) R.drawable.ic_flashlight_on else R.drawable.ic_flashlight_off
            )

            camera?.cameraControl?.enableTorch(it)
        }

        this.onBackPressedDispatcher.addCallback {
            viewModel.clearImages()
            setResult(Activity.RESULT_CANCELED)
            this@CamImagePickerActivity.finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if(checkPermission()) binding.tvPermission.visibility = View.GONE
    }

    private var isLensFacingBack = true
    private fun bindPreview(
        cameraProvider: ProcessCameraProvider,
        previewView: PreviewView = binding.camView,
        imageCapture: ImageCapture = this.imageCapture,
        lensFacing: Int? = null
    ): Camera {
        val preview: Preview = Preview.Builder()
            .build()

        val mLensFacing = lensFacing
            ?: if (this.isLensFacingBack) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(mLensFacing)
            .build()

        isLensFacingBack = mLensFacing == CameraSelector.LENS_FACING_BACK

        preview.setSurfaceProvider(previewView.surfaceProvider)

        cameraProvider.unbindAll()
        return cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            imageCapture,
            preview
        )
    }

    /**
     *  set color statusBar, navigationBar
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.statusBarColor = Color.BLACK
            window.navigationBarColor = Color.BLACK
        }
    }

    private var isCapturing = false
    fun cameraCaptured() {
        if(!checkPermission()){
            requestPermission()
            return
        }

        if (viewModel.checkMaxCount(builder.maxCount)) {
            if (isCapturing) return
            else isCapturing = true

            val file = File(
                builder.filePath ?: this.cacheDir,
                builder.fileName(viewModel.imagesSize.value ?: 0)
            )
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(error: ImageCaptureException) {
                        isCapturing = false
                    }

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        isCapturing = false
                        outputFileResults.savedUri?.let {
                            sound.playShutter()
                            binding.camView.startAnimation(
                                AlphaAnimation(1f, 0f).apply { duration = 50 }
                            )
                            viewModel.addImage(it)
                        }
                    }
                }
            )
        } else {
            toast(
                builder.textOverMax?.replace("%d", builder.maxCount.toString())
                    ?: getString(R.string.toast_over_max, builder.maxCount)
            )
        }
    }

    fun cameraSwitch() {
        bindPreview(cameraProviderFuture.get())
    }

    fun torchSwitch() {
        viewModel.isTorchOn.value = !(viewModel.isTorchOn.value ?: false)
    }

    fun confirmClick() {
        if (!viewModel.checkMinCount(builder.minCount)) {
            toast(
                builder.textUnderMin?.replace("%d", builder.minCount.toString())
                    ?: getString(R.string.toast_under_min, builder.minCount)
            )
        } else {
            val data = Intent().apply {
                putParcelableArrayListExtra(
                    Const.EXTRA_SELECTED_URIS, viewModel.images.value?.let { it1 -> ArrayList(it1) }
                )
            }
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    private fun checkPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    fun requestPermission(){
        TedPermission.create()
            .setRationaleTitle(builder.getText(builder.textPermissionRequest, R.string.permission_request))
            .setRationaleMessage(builder.getText(builder.textPermissionRequestMsg, R.string.permission_request_msg))
            .setDeniedTitle(builder.getText(builder.textPermissionDenied, R.string.permission_denied))
            .setDeniedMessage(builder.getText(builder.textPermissionDeniedMsg, R.string.permission_denied_msg))
            .setPermissions(Manifest.permission.CAMERA)
            .setPermissionListener(
                object : PermissionListener {
                    override fun onPermissionGranted() {
                        binding.tvPermission.visibility = View.GONE
                    }

                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                        binding.tvPermission.visibility = View.VISIBLE
                        toast(R.string.permission_denied)
                    }
                }
            )
            .check()
    }

}