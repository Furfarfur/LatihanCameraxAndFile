package com.zufar.latihancameraxandfile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.node.CanFocusChecker.right
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberImagePainter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LatihanTakeImage()
        }
    }
}

@Composable
fun LatihanTakeImage()
{

    val context = LocalContext.current
    val file = context.createImageFile()
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        context.packageName + ".provider", file
    )

    var capturedImageUri by remember {
        mutableStateOf<Uri>(Uri.EMPTY)
    }
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
   val bitmap = remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()){
            capturedImageUri = uri
        }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()){uri: Uri? ->
        imageUri = uri
    }


    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        if (it)
        {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            cameraLauncher.launch(uri)
        }
        else
        {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }


    Column(
        Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {

        imageUri?.let {
            if (Build.VERSION.SDK_INT < 28){
                bitmap.value = MediaStore.Images
                    .Media.getBitmap(context.contentResolver, it)
            }else{
                val source = ImageDecoder.createSource(context.contentResolver, it)
                bitmap.value = ImageDecoder.decodeBitmap(source)
            }

            bitmap.value?.let {btm ->
                Image(
                    bitmap = btm.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(400.dp)
                        .padding(20.dp)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
            modifier = Modifier.padding(10.dp),
                onClick = {
                    val permissionCheckResult =
                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)

                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED)
                    {
                        cameraLauncher.launch(uri)
                    }
                    else
                    {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) {
                Text(text = "Capture Image")
            }

            Button(
//            modifier = Modifier.padding(top = 50.dp),
                onClick = { launcher.launch("image/*")}
            ) {
                Text(text = "Pick Image")
            }

        }
//        Button(
////            modifier = Modifier.padding(top = 50.dp),
//            onClick = {
//                val permissionCheckResult =
//                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
//
//                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED)
//                {
//                    cameraLauncher.launch(uri)
//                }
//                else
//                {
//                    permissionLauncher.launch(Manifest.permission.CAMERA)
//                }
//            }) {
//            Text(text = "Capture Image")
//        }
    }


    if (capturedImageUri.path?.isNotEmpty() == true)
    {
        Image(
            modifier = Modifier
                .padding(16.dp, 8.dp),
            painter = rememberImagePainter(capturedImageUri),
            contentDescription = null
        )
    }
    else
    {
        Image(
            modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 90.dp),
            painter = painterResource(id = R.drawable.welcome),
            contentDescription = null
        )
    }

}

fun Context.createImageFile(): File {
    val timeStamp = java.text.SimpleDateFormat("yyyy_MM_dd_HH:mm:ss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        externalCacheDir
    )

    return image
}