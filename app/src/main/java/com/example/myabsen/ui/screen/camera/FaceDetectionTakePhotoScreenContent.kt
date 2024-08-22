package com.example.myabsen.ui.screen.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import com.example.myabsen.R
import com.example.myabsen.data.local.pref.AbsenModel
import com.example.myabsen.data.local.pref.UserModel
import com.example.myabsen.ui.common.ResultState
import com.example.myabsen.ui.screen.profile.ProfileViewModel
import com.example.myabsen.ui.theme.blue
import com.google.mlkit.vision.face.Face
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.ceil



@Composable
fun FaceDetectionTakePhotoScreenContent(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onSuccessClockIn: () -> Unit,
    onSuccessClockOut: () -> Unit,
    cameraViewModel: CameraViewModel,
    viewModel: ProfileViewModel
) {
    val density = LocalDensity.current
    val dpToPx = remember { with(density) { 1.dp.toPx() } }

    var faces by remember { mutableStateOf(FacesData(emptyList())) }
    var imageRect by remember { mutableStateOf(Rect()) }
    var imageCaptured by remember { mutableStateOf(false) }

    val onFaceDetected = { facesList: List<Face?>, rect: Rect ->
        faces = FacesData(facesList)
        imageRect = rect
    }


    FaceDetectionUiContent(
        modifier = modifier,
        faces = { faces },
        imageRect = { imageRect },
        dpToPx = dpToPx,
        onFaceDetected = onFaceDetected,
        imageCaptured = { imageCaptured },
        setImageCaptured = { imageCaptured = it },
        onSuccessClockIn = onSuccessClockIn,
        onSuccessClockOut = onSuccessClockOut,
        cameraViewModel = cameraViewModel,
        viewModel = viewModel,
    )

    if (!imageCaptured) {
        BackButton(onBackClick = onBackClick)
    }
}

@Composable
private fun FaceDetectionUiContent(
    modifier: Modifier = Modifier,
    faces: () -> FacesData,
    imageRect: () -> Rect,
    dpToPx: Float,
    onFaceDetected: (List<Face?>, Rect) -> Unit,
    imageCaptured: () -> Boolean,
    setImageCaptured: (Boolean) -> Unit,
    onSuccessClockIn: () -> Unit,
    onSuccessClockOut: () -> Unit,
    cameraViewModel: CameraViewModel,
    viewModel: ProfileViewModel
) {
    val absen by cameraViewModel.absen.collectAsState()
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val executor = ContextCompat.getMainExecutor(context)
    val userModel by viewModel.userModel.collectAsState()

    LaunchedEffect(absen) {
        cameraViewModel.getAbsenSession()
    }

    LaunchedEffect(userModel) {
        viewModel.getUserSessionGuruDanKaryawan()
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetResolution(android.util.Size(1080, 1920)) // Sesuaikan dengan resolusi layar
            .build()
    }

    val onImageCapturedCallback = object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            super.onCaptureSuccess(image)
            println("IMAGE: ${image.imageInfo}, ${image.width}, ${image.height}, ${image.cropRect}")

            // Create a bitmap from the image proxy
            val originalBitmap = image.toBitmap()

            // Create a matrix for the manipulation
            val matrix = Matrix().apply {
                postRotate(image.imageInfo.rotationDegrees.toFloat())
                postScale(
                    -1f,
                    1f,
                    originalBitmap.width / 2f,
                    originalBitmap.height / 2f
                ) // Flip horizontally
            }

            // Create a new bitmap using the matrix to ensure correct orientation
            val rotatedBitmap = Bitmap.createBitmap(
                originalBitmap,
                0,
                0,
                originalBitmap.width,
                originalBitmap.height,
                matrix,
                true
            )

            // Update the bitmap state
            bitmap = rotatedBitmap

            // Signal that the image has been captured
            setImageCaptured(true)

            // Close the image proxy
            image.close()
        }

        override fun onError(exception: ImageCaptureException) {
            println("onError: ${exception.imageCaptureError}, ${exception.message}")
        }
    }

    if (imageCaptured()) {
        userModel?.let {
            DisplayFullScreenPhoto(
                bitmap = { bitmap },
                onRetake = {
                    setImageCaptured(false)
                    bitmap = null
                },
                onSend = {
                    bitmap?.let {
                        val imageFile = it.toFile(context)
                        val absenStatus = getAbsenStatus(context)

                        if (absenStatus == null) {
                            // User belum absen hari ini, maka ini adalah absenMasuk
                            val status = if (isPastTargetTime(6, 45)) "0" else "1"
                            saveAbsenStatus(context, "absenMasuk")
                            cameraViewModel.addAbsen(
                                absenMasuk = getCurrentTime(),
                                absenKeluar = "",
                                tanggal = getCurrentDate(),
                                status = status,
                                nip = userModel?.nip.toString(),
                                fotoMasuk = createMultipartBody(imageFile, "foto_masuk"),
                                fotoKeluar = createMultipartBody(imageFile, "foto_keluar")
                            )
                            onSuccessClockIn()
                        } else if (absenStatus == "absenMasuk") {
                            absen?.let {
                                // User sudah absenMasuk, maka ini adalah absenKeluar
                                saveAbsenStatus(context, "absenKeluar")
                                cameraViewModel.updateAbsen(
                                    absenKeluar = getCurrentTime(),
                                    foto_keluar = createMultipartBody(imageFile, "foto_keluar"),
                                    idAbsen = it
                                )
                            }
                            onSuccessClockOut()
                        } else if (absenStatus == "absenKeluar") {
                            val status = if (isPastTargetTime(6, 45)) "0" else "1"
                            saveAbsenStatus(context, "absenMasuk")
                            cameraViewModel.addAbsen(
                                absenMasuk = getCurrentTime(),
                                absenKeluar = "",
                                tanggal = getCurrentDate(),
                                status = status,
                                nip = userModel?.nip.toString(),
                                fotoMasuk = createMultipartBody(imageFile, "foto_masuk"),
                                fotoKeluar = createMultipartBody(imageFile, "foto_keluar")
                            )
                            onSuccessClockIn()
                        }
                    }
                },
                context = context,
                cameraViewModel = cameraViewModel,
                userModel = it,
                onSuccessClockIn = onSuccessClockIn,
                onSuccessClockOut = onSuccessClockOut,
            )
        }
    } else {
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { context ->
                val imageAnalysis = ImageAnalysis.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setImageQueueDepth(10)
                    .build()
                    .apply {
                        setAnalyzer(
                            executor, FaceAnalyzer(
                                onFaceDetected = onFaceDetected
                            )
                        )
                    }

                val previewView = PreviewView(context)
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()

                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener(
                    {
                        val cameraProvider = cameraProviderFuture.get()
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis,
                            imageCapture,
                        )
                    }, executor
                )
                previewView.implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                previewView
            },
        )

        if (faces().faces.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Text(
                    text = "Tidak ada wajah",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp),
                    color = blue,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        } else {
            DrawBoxes(
                dpToPx = dpToPx,
                imageRect = imageRect,
                faces = faces
            )

            TakePhotoButton(onClick = {
                imageCapture.takePicture(
                    executor,
                    onImageCapturedCallback
                )
            })
        }
    }
}

// Helper functions to convert bitmap to file and create MultipartBody.Part
private fun Bitmap.toFile(context: Context): File {
    val fileName =
        "${System.currentTimeMillis()}.jpg"
    val file = File(context.cacheDir, fileName)
    file.outputStream().use {
        this.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }
    return file
}

private fun createMultipartBody(file: File?, name: String): MultipartBody.Part {
    val requestFile = file?.asRequestBody("image/jpeg".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(name, file?.name, requestFile!!)
}


@Composable
fun DisplayFullScreenPhoto(
    bitmap: () -> Bitmap?,
    onRetake: () -> Unit,
    onSend: () -> Unit,
    context: Context,
    cameraViewModel: CameraViewModel,
    userModel: UserModel,
    onSuccessClockIn: () -> Unit,
    onSuccessClockOut: () -> Unit,

    ) {
    val absen by cameraViewModel.absen.collectAsState()

    LaunchedEffect(absen) {
        cameraViewModel.getAbsenSession()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        bitmap()?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.Black)
                    .padding(horizontal = 25.dp, vertical = 30.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = onRetake, modifier = Modifier.size(60.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.cancel),
                        contentDescription = "",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                IconButton(onClick = {
                    val absenStatus = getAbsenStatus(context)
                    val imageFile = bitmap()?.toFile(context)

                    if (absenStatus == null) {
                        // User belum absen hari ini, maka ini adalah absenMasuk
                            val status = if (isPastTargetTime(6, 45)) "0" else "1"
                        saveAbsenStatus(context, "absenMasuk")
                        cameraViewModel.addAbsen(
                            absenMasuk = getCurrentTime(),
                            absenKeluar = "",
                            tanggal = getCurrentDate(),
                            status = status,
                            nip = userModel.nip.toString(),
                            fotoMasuk = createMultipartBody(imageFile, "foto_masuk"),
                            fotoKeluar = createMultipartBody(imageFile, "foto_keluar")
                        )
                        onSuccessClockIn()
                    } else if (absenStatus == "absenMasuk") {
                        absen?.let {
                            // User sudah absenMasuk, maka ini adalah absenKeluar
                            saveAbsenStatus(context, "absenKeluar")
                            cameraViewModel.updateAbsen(
                                absenKeluar = getCurrentTime(),
                                foto_keluar = createMultipartBody(imageFile, "foto_keluar"),
                                idAbsen = it
                            )
                        }
                        onSuccessClockOut()
                    } else if (absenStatus == "absenKeluar") {
                        // User belum absen hari ini, maka ini adalah absenMasuk
                        val status = if (isPastTargetTime(6, 45)) "0" else "1"
                        saveAbsenStatus(context, "absenMasuk")
                        cameraViewModel.addAbsen(
                            absenMasuk = getCurrentTime(),
                            absenKeluar = "",
                            tanggal = getCurrentDate(),
                            status = status,
                            nip = userModel.nip.toString(),
                            fotoMasuk = createMultipartBody(imageFile, "foto_masuk"),
                            fotoKeluar = createMultipartBody(imageFile, "foto_keluar")
                        )
                        onSuccessClockIn()
                    }
                }, modifier = Modifier.size(60.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.valid),
                        contentDescription = "",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun TakePhotoButton(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Black)
                .padding(horizontal = 25.dp, vertical = 30.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(70.dp)
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(id = R.drawable.take),
                    contentDescription = "",
                )
            }
        }
    }
}

@Composable
private fun DrawBoxes(
    dpToPx: Float,
    imageRect: () -> Rect,
    faces: () -> FacesData
) {
    val borderSize by remember { mutableFloatStateOf(dpToPx * 2) }

    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val imageRectWidth by remember(imageRect()) { derivedStateOf { imageRect().width().toFloat() } }
    val imageRectHeight by remember(imageRect()) {
        derivedStateOf { imageRect().height().toFloat() }
    }
    val scaleX by remember(canvasSize.width, imageRectHeight) {
        derivedStateOf { canvasSize.width / imageRectHeight }
    }
    val scaleY by remember(canvasSize.height, imageRectWidth) {
        derivedStateOf { canvasSize.height / imageRectWidth }
    }
    val scale by remember(scaleX, scaleY) { mutableFloatStateOf(scaleX.coerceAtLeast(scaleY)) }
    val offsetX by remember(scale) {
        mutableFloatStateOf((canvasSize.width - ceil(imageRectHeight * scale)) / 2.0f)
    }
    val offsetY by remember(scale) {
        mutableFloatStateOf((canvasSize.height - ceil(imageRectWidth * scale)) / 2.0f)
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                canvasSize = it.size.toSize()
            }
    ) {
        faces().faces.forEach {
            it?.let { face ->
                val rect = calculateRect(
                    scale = scale,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    boundingBox = face.boundingBox,
                )
                drawRect(
                    color = Color.White,
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.right, rect.bottom),
                    style = Stroke(borderSize)
                )
            }
        }
    }
}

private fun calculateRect(
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    boundingBox: Rect,
): RectF = RectF().apply {
    left = boundingBox.left * scale + offsetX
    top = boundingBox.top * scale + offsetY
    right = boundingBox.width() * scale
    bottom = boundingBox.height() * scale
}

@Composable
private fun BackButton(onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        MyIconButton(
            iconId = R.drawable.baseline_arrow_back_24,
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}

@Composable
private fun MyIconButton(
    @DrawableRes iconId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = { onClick() },
        modifier = modifier.padding(16.dp)
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = null
        )
    }
}

data class FacesData(val faces: List<Face?>)

private val resolutionSelector = ResolutionSelector.Builder()
    .setResolutionStrategy(
        ResolutionStrategy(
            android.util.Size(480, 360),
            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER
        )
    )
    .build()

private fun ImageProxy.toBitmap(): Bitmap {
    val planeProxy = planes[0]
    val buffer: ByteBuffer = planeProxy.buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    close() // Close the image proxy to free up resources
    return bitmap
}

// Fungsi untuk menyimpan status absen ke SharedPreferences
fun saveAbsenStatus(context: Context, absenType: String) {
    val sharedPreferences = context.getSharedPreferences("absen_prefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val currentDate = getCurrentDate()

    editor.putString("absen_type", absenType)
    editor.putString("absen_date", currentDate)
    editor.apply()
}

// Fungsi untuk memeriksa status absen dari SharedPreferences
fun getAbsenStatus(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("absen_prefs", Context.MODE_PRIVATE)
    val currentDate = getCurrentDate()
    val savedDate = sharedPreferences.getString("absen_date", null)

    return if (currentDate == savedDate) {
        sharedPreferences.getString("absen_type", null)
    } else {
        null
    }
}

// Fungsi untuk mendapatkan tanggal saat ini
private fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

// Fungsi untuk mendapatkan waktu saat ini
private fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
}

fun isPastTargetTime(targetHour: Int, targetMinute: Int): Boolean {
        val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"))
        val targetTime = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta")).apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
        }
        return now.after(targetTime)
    }
