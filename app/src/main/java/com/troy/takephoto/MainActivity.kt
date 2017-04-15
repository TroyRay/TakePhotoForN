package com.troy.takephoto

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import java.io.File

class MainActivity : AppCompatActivity() {
    val CAMERA_CODE = 1001

    var toFile:File? = null
    var mTakePhotoUri:Uri? = null

    var mBtnTakePhoto:Button? = null
    var mImageView:ImageView? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mBtnTakePhoto = findViewById(R.id.btn_take_photo) as Button?
        mImageView = findViewById(R.id.image) as ImageView?
        mBtnTakePhoto?.setOnClickListener {
            checkPermission()
        }
    }

    fun  checkPermission(){
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        takePhoto()
                    }
                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {}
                }).check()
    }

    fun takePhoto(){
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val takePhotoFolder = File(Environment.getExternalStorageDirectory(), "/DCIM/camera/") //保存拍照图片的文件夹
        if (!takePhotoFolder.exists()) takePhotoFolder.mkdirs()
        toFile = File(takePhotoFolder,System.currentTimeMillis().toString()+".jpg")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mTakePhotoUri = FileProvider.getUriForFile(this, "com.troy.takephoto.fileProvider", toFile)
            captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        else {
            mTakePhotoUri = Uri.fromFile(toFile)
        }
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT,mTakePhotoUri)
        startActivityForResult(captureIntent,CAMERA_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == CAMERA_CODE){
            galleryAddPhoto(toFile)
            val path = toFile?.absolutePath
            Toast.makeText(this,"照片路径："+path,Toast.LENGTH_SHORT).show()
            if (File(path).exists())
                Picasso.with(this)
                        .load("file://"+path)
                        .error(R.mipmap.ic_launcher)
                        .into(mImageView)
        }
    }

    /**
     * 扫描图片
     */
    fun galleryAddPhoto(file:File?){
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(file)
        mediaScanIntent.setData(contentUri)
        sendBroadcast(mediaScanIntent)
    }

}
