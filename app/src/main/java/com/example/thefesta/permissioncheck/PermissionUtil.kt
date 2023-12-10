package com.example.thefesta.permissioncheck

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionUtil(val activity: Activity) {
    companion object {

        //체크해야 할 권한들
        private const val TAG = "permission"
        private const val PERMISSIONS_REQUEST_CODE = 100

        private val REQUIRED_PERMISSIONS = listOf<String>(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    }

    //self check했을 때 실행될 메서드
    fun checkPermissions() {
        if (isPermissionGranted() != PackageManager.PERMISSION_GRANTED) {
            showAlert() // 권한이 없으면 알림창을 띄움
        } else {
            Toast.makeText(activity, "Permissions already granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isPermissionGranted(): Int {
        var count = 0;
        for (permission in REQUIRED_PERMISSIONS) {
            count += ContextCompat.checkSelfPermission(activity, permission)
        }
        Log.d(TAG, "count : ${count}")

        return count
    }

    private  fun deniedPermission(): String {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_DENIED) return permission
        }
        return ""
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(activity)

        builder.setTitle("권한 요청")
        builder.setMessage("작업을 수행하려면 일부 권한이 필요합니다.")
        builder.setPositiveButton("Ok",{dialog, which -> requestPermissions()})
        builder.setNeutralButton("Cancel", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun requestPermissions() {
        val permission = deniedPermission()
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            Toast.makeText(activity, "작업을 수행하기 위해 해당 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS.toTypedArray(), PERMISSIONS_REQUEST_CODE)
        }
    }

}