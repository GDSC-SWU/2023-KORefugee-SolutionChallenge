package com.example.korefugee.Community

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.korefugee.R
import com.example.korefugee.databinding.ActivityTranslateApp2Binding
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*


class TranslateApp : AppCompatActivity() {
    lateinit var binding: ActivityTranslateApp2Binding

    lateinit var way:String

    lateinit var filePath: String
    lateinit var accesstoken:String
    lateinit var refreshtoken:String

    var language_short: String = "en"
    private val REQUEST_PERMISSIONS = 1

    private val timeStamp: String =
        SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityTranslateApp2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra("way")&&intent.hasExtra("accesstoken")&&intent.hasExtra("refreshtoken")) {
            way = intent.getStringExtra("way").toString()
            accesstoken = intent.getStringExtra("accesstoken").toString()
            refreshtoken = intent.getStringExtra("refreshtoken").toString()
        }

        val jsonString = assets.open("lang_list.json").reader().readText()
        val jsonArray = JSONArray(jsonString)

        binding.language.setOnClickListener(View.OnClickListener { v ->
                val popupMenu = PopupMenu(this, v)
                val inflater = popupMenu.menuInflater
                val menu = popupMenu.menu
                inflater.inflate(R.menu.languagelist, menu)
                popupMenu.setOnMenuItemClickListener { item ->

                    var x =item.title.toString()  // 메뉴의 타이틀을 불러와서

                    binding.language.setText(x)    // 카테고리 텍스트뷰에 출력해줌
                    // 그 나라 언어와 연결하기
                    for (index in 0 until jsonArray.length()){

                        val jsonObject = jsonArray.getJSONObject(index)
                        val name = jsonObject.getString("name")
                        val language = jsonObject.getString("language")

                        if(name == x){
                            language_short = language
                        }
                    }
                    false
                }
                popupMenu.show()
            })




        val request_image =
            registerForActivityResult(ActivityResultContracts
                .StartActivityForResult())
            {
                filePath = getRealPathFromURI(it.data!!.data!!).toString()


            }

        val request_pdf =
            registerForActivityResult(ActivityResultContracts
                .StartActivityForResult())
            {
                it.data?.data?.let { uri->
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    filePath = getPath(this, uri).toString()
                    Log.d("aa",filePath)

                }
            }

        if(way == "camera"){
            // 카메라 앱 실행 및 api로 넘기기
            checkPermission()
            val intent = Intent(Intent.ACTION_PICK)
            intent.setType("image/*")
            request_image.launch(intent)

        }
        else if(way == "pdf") {
            checkPermission()
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("application/pdf")
            request_pdf.launch(intent)
        }

        binding.startButton.setOnClickListener {
            val intent = Intent(this, Translateloading::class.java)
            intent.putExtra("way",way)
            intent.putExtra("language_short",language_short)
            intent.putExtra("file",filePath)
            intent.putExtra("accesstoken",accesstoken)
            intent.putExtra("refreshtoken",refreshtoken)
            startActivity(intent)
            finish()
        }


    }

    private fun checkPermission() {
        var permission = mutableMapOf<String, String>()
        permission["storageRead"] = Manifest.permission.READ_EXTERNAL_STORAGE
        permission["storageWrite"] =  Manifest.permission.WRITE_EXTERNAL_STORAGE

        // 현재 권한 상태 검사
        var denied = permission.count { ContextCompat.checkSelfPermission(this, it.value)  == PackageManager.PERMISSION_DENIED }

        // 마시멜로 버전 이후
        if(denied > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permission.values.toTypedArray(), REQUEST_PERMISSIONS)
        }


    }

    private fun getRealPathFromURI(contentURI: Uri): String? {
        val result: String?
        val cursor: Cursor? = contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    fun getPath(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                Log.e("Aaa","error1")
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                Log.e("Aaa","error2")
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                Log.e("Aaa","error3")
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.getContentResolver().query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
}