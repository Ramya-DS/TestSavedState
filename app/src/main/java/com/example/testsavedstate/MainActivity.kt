package com.example.testsavedstate

import android.app.PendingIntent.getActivity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE


class MainActivity : AppCompatActivity(), OnAdapterChangeListener, OnFileChangedListener,
    DetailPaneVisibility {
    companion object {
        val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        const val PERMISSION_COUNT = 1
        const val REQUEST_PERMISSION = 1234
    }

    private var path: Bundle? = Bundle().apply {
        this.putString("path", Environment.getExternalStorageDirectory().path)
    }
    private var file: Bundle? = null

    var manager = supportFragmentManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        manager.addOnBackStackChangedListener {
            var i = manager.backStackEntryCount - 1
            while (i >= 0) {
                Log.i(manager.getBackStackEntryAt(i).name, "$i")
                i--
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                val fragmentMaster = ListFolder()
                val bundle = Bundle()
                bundle.putString("path", Environment.getExternalStorageDirectory().path)
                fragmentMaster.arguments = bundle
                manager.beginTransaction()
                    .replace(R.id.masterContainer, fragmentMaster, "master").commit()
            } else
                requestPermissions(permissions, REQUEST_PERMISSION)
        } else {
            val fragmentMaster = ListFolder()
            val bundle = Bundle()
            bundle.putString("path", Environment.getExternalStorageDirectory().path)
            fragmentMaster.arguments = bundle
            manager.beginTransaction()
                .replace(R.id.masterContainer, fragmentMaster, "master").commit()
        }


    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        manager.executePendingTransactions()
        super.onConfigurationChanged(newConfig)
        setContentView(R.layout.activity_main)
        Log.i("Config Change", " ")

        manager.popBackStack(0, POP_BACK_STACK_INCLUSIVE)


        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val fragmentMaster = ListFolder()
            fragmentMaster.arguments = path
            manager.beginTransaction()
                .replace(R.id.masterContainer, fragmentMaster)
                .commit()

            file?.let {
                val fragment = SingleFile()
                fragment.arguments = file
                manager.beginTransaction()
                    .replace(R.id.detailContainer, fragment)
//                    .addToBackStack("detail1")
                    .commit()
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            val fragmentMaster = ListFolder()
            fragmentMaster.arguments = path

            manager.beginTransaction()
                .replace(R.id.masterContainer, fragmentMaster)
                .commit()

            file?.let {
                val fragment = SingleFile()
                fragment.arguments = file
                supportFragmentManager.beginTransaction().replace(
                    R.id.masterContainer, fragment
                ).addToBackStack("detail2").commit()
            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val fragmentMaster = ListFolder()
            val bundle = Bundle()
            bundle.putString("path", Environment.getExternalStorageDirectory().path)
            fragmentMaster.arguments = bundle
            manager.beginTransaction()
                .replace(R.id.masterContainer, fragmentMaster, "master").commit()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Permission is needed")
                .setMessage("This permission is essential for the working of the application")
                .setPositiveButton("Ok") { dialogInterface: DialogInterface, i: Int ->
                    requestPermissions(Companion.permissions, REQUEST_PERMISSION)
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .create().show()
        } else {
//            val myAppSettings = Intent(
//                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                Uri.parse("package:$packageName")
//            )
//            finish()
//            myAppSettings.addCategory(Intent.CATEGORY_DEFAULT)
//            myAppSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            startActivityForResult(myAppSettings, 168)

            val i = Intent()
            i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            i.addCategory(Intent.CATEGORY_DEFAULT)
            i.data = Uri.parse("package:" + getPackageName())
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            startActivity(i)
        }
    }

    override fun onBackPressed() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT && file != null) {

            manager.popBackStack(0, POP_BACK_STACK_INCLUSIVE)
            file = null

        } else {
            if (file != null) {
                file = null
            }
            super.onBackPressed()
        }

    }

    override fun onAdapterChangeListener(currentPath: String) {
        val bundle = Bundle()
        bundle.putString("path", currentPath)
        path = bundle
    }

    override fun onFileChanged(currentFile: Bundle?) {
        file = currentFile
    }

    override fun detailVisiblity(flag: Boolean) {
        val view = findViewById<View>(R.id.detailContainer)
        view.visibility = if (flag) View.VISIBLE else View.GONE
    }
}
