package com.example.testsavedstate

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.ListFragment
import java.io.File
import java.util.jar.Manifest


class MainActivity : AppCompatActivity(), OnAdapterChangeListener, OnFileChangedListener,
    DetailPaneVisibility {
    companion object {
        val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        const val PERMISSION_COUNT = 1
        const val REQUEST_PERMISSION = 1234
    }

    private var path: Bundle? = Bundle().apply{
        this.putString("path",Environment.getExternalStorageDirectory().path)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestStoragePermission()
            }

        val fragmentMaster = ListFolder(this, this, this)
        val bundle = Bundle()
        bundle.putString("path", Environment.getExternalStorageDirectory().path)
        fragmentMaster.arguments = bundle
        manager.beginTransaction()
            .replace(R.id.masterContainer, fragmentMaster, "master").commit()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        manager.executePendingTransactions()
        super.onConfigurationChanged(newConfig)
        setContentView(R.layout.activity_main)
        Log.i("Config Change", " ")

        manager.popBackStack(0, POP_BACK_STACK_INCLUSIVE)


        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val fragmentMaster = ListFolder(this, this, this)
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

            val fragmentMaster = ListFolder(this, this, this)
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

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Permission is needed")
                    .setMessage("This permission is essential for the working of the application")
                    .setPositiveButton("Ok") { dialogInterface: DialogInterface, i: Int ->
                        requestPermissions(permissions, REQUEST_PERMISSION)
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        dialog.dismiss()
                    }
                    .create().show()

            } else {
                requestPermissions(permissions, REQUEST_PERMISSION)
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && grantResults.isNotEmpty()&& grantResults[0]==PackageManager.PERMISSION_GRANTED) {
            onResume()
        }
        else{
            Toast.makeText(this,"PERMISSION DENIED", Toast.LENGTH_SHORT).show()
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
            Log.i("Visibility", findViewById<View>(R.id.detailContainer).visibility.toString())
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

    override fun DetailVisiblity(flag: Boolean) {
        val view = findViewById<View>(R.id.detailContainer)
        view.visibility = if (flag) View.VISIBLE else View.GONE
    }
}
