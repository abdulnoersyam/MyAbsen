package com.example.myabsen

import SuccessClockInScreen
import SuccessClockOutScreen
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myabsen.data.di.Injection
import com.example.myabsen.ui.navigation.Screen
import com.example.myabsen.ui.screen.about.AboutScreen
import com.example.myabsen.ui.screen.camera.CameraViewModel
import com.example.myabsen.ui.screen.camera.FaceDetectionTakePhotoScreenContent
import com.example.myabsen.ui.screen.home.HomeScreen
import com.example.myabsen.ui.screen.home.HomeViewModel
import com.example.myabsen.ui.screen.profile.ProfileScreen
import com.example.myabsen.ui.screen.profile.ProfileViewModel
import com.example.myabsen.ui.theme.blue
import com.example.myabsen.ui.theme.fontFamily
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    requiredPermissions: Array<String> = arrayOf(
        android.Manifest.permission.CAMERA,
    )
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val userRepository = Injection.provideRepository()
    val profileViewModel = ProfileViewModel(userRepository)
    val homeViewModel = HomeViewModel(userRepository)
    val cameraViewModel = CameraViewModel(userRepository)

    var showPermissionNeeded by remember { mutableStateOf(false) }
    var startFaceDetection by rememberSaveable { mutableStateOf(FaceDetectionFeature.OFF) }
    val context = LocalContext.current

    // Retrieve click count and last date from SharedPreferences
    val sharedPrefs = context.getSharedPreferences("camera_prefs", Context.MODE_PRIVATE)
    var cameraClickCount by rememberSaveable {
        mutableStateOf(
            sharedPrefs.getInt(
                "camera_click_count",
                0
            )
        )
    }
    val lastDate = sharedPrefs.getString("last_date", null)
    var showMaxClickDialog by remember { mutableStateOf(false) }

    // Get current date
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID")).format(Date())

    // Check if the date has changed
    if (lastDate != currentDate) {
        cameraClickCount = 0
        sharedPrefs.edit().putInt("camera_click_count", cameraClickCount).apply()
        sharedPrefs.edit().putString("last_date", currentDate).apply()
    }

    val requestPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        showPermissionNeeded = result.any { !it.value }
        if (!showPermissionNeeded) {
            startFaceDetection = FaceDetectionFeature.TAKE_PHOTO
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.Initial.route
                && currentRoute != Screen.About.route
                && currentRoute != Screen.Camera.route
                && currentRoute != Screen.SuccessClockIn.route
                && currentRoute != Screen.SuccessClockOut.route
            ) {
                BottomBar(
                    modifier = modifier,
                    navController = navController,
                    onFaceDetectionClick = {
                        if (cameraClickCount < 2) {
                            val notPermittedList = requiredPermissions.filter { permission ->
                                ActivityCompat.checkSelfPermission(
                                    context,
                                    permission
                                ) != PackageManager.PERMISSION_GRANTED
                            }
                            if (notPermittedList.isNotEmpty()) {
                                requestPermission.launch(notPermittedList.toTypedArray())
                            } else {
                                startFaceDetection = FaceDetectionFeature.TAKE_PHOTO
                            }
                            cameraClickCount++

                            // Save click count to SharedPreferences
                            sharedPrefs.edit().putInt("camera_click_count", cameraClickCount)
                                .apply()
                        } else {
                            showMaxClickDialog = true
                        }
                    }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = profileViewModel,
                    homeViewModel = homeViewModel,
                    cameraViewModel = cameraViewModel
                )
            }
            composable(Screen.Camera.route) {
                FaceDetectionTakePhotoScreenContent(
                    onBackClick = {
                        navController.navigateUp()
                    },
                    onSuccessClockIn = {
                        navController.navigate(Screen.SuccessClockIn.route)
                    },
                    onSuccessClockOut = {
                        navController.navigate(Screen.SuccessClockOut.route)
                    },
                    cameraViewModel = cameraViewModel,
                    viewModel = profileViewModel
                )
            }
            composable(Screen.SuccessClockIn.route) {
                SuccessClockInScreen(
                    homeScreen = {
                        navController.navigate(Screen.Home.route)
                    },
                )
            }
            composable(Screen.SuccessClockOut.route) {
                SuccessClockOutScreen(
                    homeScreen = {
                        navController.navigate(Screen.Home.route)
                    },
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogoutClick = {
                        cameraClickCount = 0
                        sharedPrefs.edit().putInt("camera_click_count", cameraClickCount).apply()
                        navController.navigate(Screen.Initial.route)
                    },
                    onAboutClick = {
                        navController.navigate(Screen.About.route)
                    },
                    viewModel = profileViewModel
                )
            }
            composable(Screen.Initial.route) {
                MyAbsenApp()
            }
            composable(Screen.About.route) {
                AboutScreen(
                    onBackClick = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }

    if (startFaceDetection == FaceDetectionFeature.TAKE_PHOTO && !showPermissionNeeded) {
        navController.navigate(Screen.Camera.route)
        startFaceDetection = FaceDetectionFeature.OFF
    }

    if (showMaxClickDialog) {
        AlertDialog.Builder(context).apply {
            setTitle("Tidak Bisa Absen Lagi")
            setMessage("Anda telah mencapai batas maksimum untuk mengambil foto.")
            setPositiveButton("Oke") { dialog, _ ->
                dialog.dismiss()
                showMaxClickDialog = false
            }
            create()
            show()
        }
    }
}

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onFaceDetectionClick: () -> Unit
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            modifier = Modifier
                .clickable {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Profile.route) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                }
                .padding(vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(30.dp),
                painter = painterResource(
                    id = if (currentRoute == Screen.Home.route) R.drawable.home_fill else R.drawable.home
                ),
                contentDescription = null
            )
            Text(
                text = "Home",
                fontFamily = fontFamily,
                fontWeight = if (currentRoute == Screen.Home.route) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.sp,
                color = blue
            )
        }

        Box(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(10.dp))
                .background(blue)
        ) {
            IconButton(onClick = onFaceDetectionClick) {
                Image(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(45.dp)
                        .fillMaxSize(),
                    painter = painterResource(id = R.drawable.camera),
                    contentDescription = null
                )
            }
        }

        Column(
            modifier = Modifier
                .clickable {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Home.route) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop
                    }
                }
                .padding(vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(30.dp),
                painter = painterResource(
                    id = if (currentRoute == Screen.Profile.route) R.drawable.profile_fill else R.drawable.profile
                ),
                contentDescription = null
            )
            Text(
                text = "Account",
                fontFamily = fontFamily,
                fontWeight = if (currentRoute == Screen.Profile.route) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.sp,
                color = blue
            )
        }
    }
}

enum class FaceDetectionFeature {
    OFF, TAKE_PHOTO
}
