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
import com.example.testsavedstate.SingleFile.Companion.currentFile
import java.io.File


class MainActivity : AppCompatActivity() {
    companion object {
        val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        const val PERMISSION_COUNT = 1
        const val REQUEST_PERMISSION = 1234
    }

    var path: String = Environment.getExternalStorageDirectory().path

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isPermissionDenied()) {
            requestPermissions(permissions, REQUEST_PERMISSION)
            return
        }

//        supportFragmentManager.addOnBackStackChangedListener {
//            Log.i("back", "stack")
//            var i = supportFragmentManager.backStackEntryCount - 1
//            while (i >= 0) {
//                Log.i("${supportFragmentManager.getBackStackEntryAt(i).name}", "$i")
//                i--
//            }
//        }

        val fragmentMaster=ListFragment()
        val bundle=Bundle()
        bundle.putString("path",path)
        fragmentMaster.arguments=bundle
        supportFragmentManager.beginTransaction().add(R.id.masterContainer, fragmentMaster, "master").commit()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setContentView(R.layout.activity_main)
        Log.i("Config Change", " ")

//        Log.i("count1" ,supportFragmentManager.backStackEntryCount.toString())

        supportFragmentManager.executePendingTransactions()
        var fragmentById: Fragment? =
            supportFragmentManager.findFragmentById(R.id.masterContainer)
        if (fragmentById != null) {
            val bundle=fragmentById.arguments
            path=bundle!!.getString("current file")!!
            supportFragmentManager.beginTransaction()
                .remove(fragmentById).commit()
        }

        supportFragmentManager.executePendingTransactions()
        fragmentById=
            supportFragmentManager.findFragmentById(R.id.detailContainer)
        if (fragmentById != null) {
            supportFragmentManager.beginTransaction()
                .remove(fragmentById).commit()
        }
//        Log.i("count2" ,supportFragmentManager.backStackEntryCount.toString())

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (File(path).parentFile != Environment.getExternalStorageDirectory())
                path = File(path).parent!!
            val fragmentMaster=ListFragment()
            val bundle=Bundle()
            bundle.putString("path",path)
            fragmentMaster.arguments=bundle


            supportFragmentManager.beginTransaction()
                .replace(R.id.masterContainer, fragmentMaster)
                .commit()


            currentFile?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, SingleFile(it))
                    .addToBackStack("detail1")
                    .commit()
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {


            val fragmentMaster=ListFragment()
            val bundle=Bundle()
            bundle.putString("path",path)
            fragmentMaster.arguments=bundle

            supportFragmentManager.beginTransaction()
                .replace(R.id.masterContainer, fragmentMaster)
                .commit()

            if (currentFile != null) {
                supportFragmentManager.beginTransaction().replace(
                    R.id.masterContainer, SingleFile(
                        currentFile!!
                    )
                ).addToBackStack("detail2").commit()
            }
        }
//        Log.i("count3" ,supportFragmentManager.backStackEntryCount.toString())
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
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT && currentFile != null) {

            supportFragmentManager.popBackStack(0, POP_BACK_STACK_INCLUSIVE)
            currentFile = null

        } else {
            super.onBackPressed()
        }

    }
}
