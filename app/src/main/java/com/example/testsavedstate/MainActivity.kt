package com.example.testsavedstate

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.ListFragment
import java.io.File


class MainActivity : AppCompatActivity(),OnAdapterChangeListener, OnFileChangedListener {
    companion object {
        val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        const val PERMISSION_COUNT = 1
        const val REQUEST_PERMISSION = 1234
    }

    private var path: Bundle?=null
    private var file: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.addOnBackStackChangedListener {
            var i=supportFragmentManager.backStackEntryCount-1
            while(i>=0){
               Log.i(supportFragmentManager.getBackStackEntryAt(i).name,"$i")
                i--
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isPermissionDenied()) {
            requestPermissions(permissions, REQUEST_PERMISSION)
            return
        }

        val fragmentMaster = ListFolder(this,this)
        val bundle = Bundle()
        bundle.putString("path", Environment.getExternalStorageDirectory().path)
        fragmentMaster.arguments = bundle
        supportFragmentManager.beginTransaction()
            .add(R.id.masterContainer, fragmentMaster, "master").commit()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        supportFragmentManager.executePendingTransactions()
        super.onConfigurationChanged(newConfig)
        setContentView(R.layout.activity_main)
        Log.i("Config Change", " ")


//        var fragmentById: Fragment? = supportFragmentManager.findFragmentById(R.id.masterContainer)

//        if (fragmentById != null) {
//            if (fragmentById is ListFolder) {
//                supportFragmentManager.beginTransaction()
//                    .remove(fragmentById).commit()
//            } else if (fragmentById is SingleFile) {
//                file= fragmentById.arguments
//            }
//
//        }
//
//        supportFragmentManager.executePendingTransactions()
//        fragmentById =
//            supportFragmentManager.findFragmentById(R.id.detailContainer)
//        if (fragmentById != null) {
//            file = fragmentById.arguments
//            supportFragmentManager.beginTransaction()
//                .remove(fragmentById).commit()
//        }


        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            if (File(path).parentFile != Environment.getExternalStorageDirectory())
//                path = File(path).parent!!
            val fragmentMaster = ListFolder(this,this)
            fragmentMaster.arguments = path

            supportFragmentManager.beginTransaction()
                .replace(R.id.masterContainer, fragmentMaster)
                .commit()



            file?.let {
                val fragment = SingleFile()
                fragment.arguments = file
                supportFragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, fragment)
                    .addToBackStack("detail1")
                    .commit()
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {


            val fragmentMaster = ListFolder(this,this)
            fragmentMaster.arguments = path

            supportFragmentManager.beginTransaction()
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

    private fun isPermissionDenied(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var p = 0
            while (p < PERMISSION_COUNT) {
                if (checkSelfPermission(permissions[p]) != PackageManager.PERMISSION_GRANTED)
                    return true
                p++
            }
        }
        return false
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && grantResults.isNotEmpty()) {
            if (isPermissionDenied()) {
                (this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
                recreate()
            } else
                onResume()
        }
    }

    override fun onBackPressed() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT && file != null) {

                supportFragmentManager.popBackStack(0, POP_BACK_STACK_INCLUSIVE)
                file = null

        } else {
            if(file!=null){
                file=null
            }
            super.onBackPressed()
        }

    }

    override fun onAdapterChangeListener(currentPath: String) {
        val bundle=Bundle()
        bundle.putString("path",currentPath)
        path=bundle
    }

    override fun onFileChanged(currentFile: Bundle?) {
        file=currentFile
    }
}
