package com.example.myabsen.ui.screen.home

import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myabsen.R
import com.example.myabsen.data.local.pref.AbsenModel
import com.example.myabsen.data.remote.response.GetAbsenGuruDanKaryawanResponse
import com.example.myabsen.data.remote.response.GetAbsenGuruDanKaryawanResponseItem
import com.example.myabsen.data.remote.response.GetProfileGuruDanKaryawanResponse
import com.example.myabsen.data.remote.response.GetWaktuAbsenResponse
import com.example.myabsen.data.remote.response.InsertAbsenGuruDanKaryawanResponse
import com.example.myabsen.ui.common.ResultState
import com.example.myabsen.ui.screen.camera.CameraViewModel
import com.example.myabsen.ui.screen.profile.ProfileViewModel
import com.example.myabsen.ui.theme.MyAbsenTheme
import com.example.myabsen.ui.theme.blue
import com.example.myabsen.ui.theme.divider
import com.example.myabsen.ui.theme.fontFamily
import com.example.myabsen.ui.theme.green
import com.example.myabsen.ui.theme.grey
import com.example.myabsen.ui.theme.md_theme_light_shadow
import com.example.myabsen.ui.theme.red
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

fun getCurrentDateFormatted(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel,
    homeViewModel: HomeViewModel,
    cameraViewModel: CameraViewModel
) {
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val userModel by viewModel.userModel.collectAsState()
    val absen by cameraViewModel.absen.collectAsState()

    // get waktu absen
    val getWaktuAbsenState: ResultState<GetWaktuAbsenResponse> by homeViewModel.getWaktuAbsenState.collectAsState()
    val selectedYear = remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    val selectedMonth = remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }

    val listState = rememberLazyListState()

    LaunchedEffect(userModel) {
        viewModel.getUserSessionGuruDanKaryawan()
        absen?.let {
            homeViewModel.getWaktuAbsen(it.toInt())
        }
    }

    // get data absen fungsi getAbsenGuruDanKaryawan
    userModel?.let {
        LaunchedEffect(selectedYear.value, selectedMonth.value) {
            homeViewModel.getAbsenGuruDanKaryawan(
                it.nip,
                selectedYear.value,
                selectedMonth.value + 1
            )
        }
    }

    LaunchedEffect(absen) {
        cameraViewModel.getAbsenSession()
    }

    val getAbsenResponseState by homeViewModel.absenResponse.collectAsState()
    val addAbsenResponseState: ResultState<AbsenModel> by cameraViewModel.addAbsenResult.collectAsState()

    // Launch effect to clear session if the day has changed
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID"))
        val currentDate = sdf.format(Date())

        while (true) {
            delay(60 * 1000) // Check every minute
            val newDate = sdf.format(Date())
            if (newDate != currentDate) {
                homeViewModel.clearSession()
                break
            }
        }
    }


    Box(modifier = modifier) {
        Column(
            modifier = Modifier.padding(top = 20.dp),
        ) {
            userModel?.let {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 35.dp),
                    text = stringResource(R.string.hello) + it.fullname + "!",
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 26.sp,
                    lineHeight = 35.sp,
                    color = md_theme_light_shadow
                )
            }

            Column(
                modifier = Modifier
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 45.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 11.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = getCurrentDate(),
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = grey
                        )
                        Spacer(modifier = Modifier.height(11.dp))
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth(),
                            thickness = 2.dp,
                            color = divider
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = getCurrentTime(),
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 40.sp,
                            color = md_theme_light_shadow
                        )
                        Text(
                            text = stringResource(R.string.day),
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            color = grey
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 45.dp)
                    .clip(shape = RoundedCornerShape(10.dp))
                    .background(color = blue)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    when (getWaktuAbsenState) {
                        is ResultState.Loading -> {
                            Column(
                                modifier = Modifier,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Image(
                                    modifier = Modifier
                                        .size(50.dp),
                                    painter = painterResource(id = R.drawable.alarm_in),
                                    contentDescription = null
                                )
                                Text(
                                    text = "Check in at",
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(
                                        top = 7.dp,
                                        start = 25.dp,
                                        end = 25.dp
                                    )
                                )
                                // jam absen masuk
                                Text(
                                    text = "00:00",
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            VerticalDivider(
                                color = Color.White,
                                thickness = 2.dp
                            )
                            Column(
                                modifier = Modifier,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Image(
                                    modifier = Modifier.size(50.dp),
                                    painter = painterResource(id = R.drawable.alarm_out),
                                    contentDescription = null
                                )
                                Text(
                                    text = "Check out at",
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(
                                        top = 7.dp,
                                        start = 25.dp,
                                        end = 25.dp
                                    )
                                )
                                // jam absen keluar
                                Text(
                                    text = "00:00",
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            }
                        }

                        is ResultState.Success -> {
                            // get waktu absen
                            val getWaktuAbsenResponse =
                                (getWaktuAbsenState as ResultState.Success).data

                            val currentDate = getCurrentDateFormatted()
                            val absenDate = getWaktuAbsenResponse.absen?.tanggal

                            Column(
                                modifier = Modifier,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Image(
                                    modifier = Modifier
                                        .size(50.dp),
                                    painter = painterResource(id = R.drawable.alarm_in),
                                    contentDescription = null
                                )
                                Text(
                                    text = "Check in at",
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(
                                        top = 7.dp,
                                        start = 25.dp,
                                        end = 25.dp
                                    )
                                )
                                // jam absen masuk (tanggal absen masuk)
                                // jam absen masuk
                                if (absenDate == currentDate) {
                                    getWaktuAbsenResponse.absen?.absenMasuk?.let {
                                        val dateFormat =
                                            SimpleDateFormat("HH:mm:SS", Locale.getDefault())
                                        val date = dateFormat.parse(it)
                                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                        date?.let {
                                            Text(
                                                text = sdf.format(date),
                                                fontFamily = fontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 20.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "00:00",
                                        fontFamily = fontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 20.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            VerticalDivider(
                                color = Color.White,
                                thickness = 2.dp
                            )
                            Column(
                                modifier = Modifier,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Image(
                                    modifier = Modifier.size(50.dp),
                                    painter = painterResource(id = R.drawable.alarm_out),
                                    contentDescription = null
                                )
                                Text(
                                    text = "Check out at",
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(
                                        top = 7.dp,
                                        start = 25.dp,
                                        end = 25.dp
                                    )
                                )
                                // jam absen keluar
                                if (absenDate == currentDate) {
                                    getWaktuAbsenResponse.absen?.absenKeluar?.let {
                                        val dateFormat =
                                            SimpleDateFormat("HH:mm:SS", Locale.getDefault())
                                        val date = dateFormat.parse(it)
                                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                        date?.let {
                                            Text(
                                                text = sdf.format(date),
                                                fontFamily = fontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 20.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "00:00",
                                        fontFamily = fontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 20.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        is ResultState.Error -> {
                            val addProfileGuruDanKaryawanResponse =
                                (addAbsenResponseState as ResultState.Error).errorMessage
                        }

                        else -> {}
                    }
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .clickable { showDatePicker = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = R.drawable.calendar_date),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = "${
                        selectedDate.getDisplayName(
                            Calendar.MONTH,
                            Calendar.LONG,
                            Locale("en", "EN")
                        )
                    } ${selectedDate.get(Calendar.YEAR)}",
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = blue
                )
                Spacer(modifier = Modifier.width(5.dp))
                Image(
                    modifier = Modifier
                        .size(20.dp),
                    painter = painterResource(id = R.drawable.expand_arrow),
                    contentDescription = null
                )
            }
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = blue,
                            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                        )
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = "Date",
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Clock In",
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Clock Out",
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Status",
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
                Divider(color = divider, thickness = 2.dp)

                when (getAbsenResponseState) {
                    ResultState.Loading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is ResultState.Success -> {
                        val getAbsenResponse =
                            // getAbsenResponseState sukses
                            (getAbsenResponseState as ResultState.Success<List<GetAbsenGuruDanKaryawanResponseItem>>).data
                        LazyColumn(
                            state = listState,
                            modifier = Modifier,
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            items(getAbsenResponse.reversed()) { data ->
                                // save session
                                getAbsenResponse.lastOrNull()?.idAbsen?.let { latestIdAbsen ->
                                    cameraViewModel.saveSessionAbsen(latestIdAbsen)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(color = Color.White)
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    val dateFormatTanggal =
                                        SimpleDateFormat("yyyy-mm-dd", Locale.getDefault())
                                    val tanggal = dateFormatTanggal.parse(data.tanggal)
                                    val sdfTanggal = SimpleDateFormat("dd/mm/yyyy", Locale.getDefault())
                                    tanggal?.let {
                                        Text(
                                            text = sdfTanggal.format(it),
                                            fontFamily = fontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = Color.Black,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    val dateFormat =
                                        SimpleDateFormat("HH:mm:SS", Locale.getDefault())
                                    val dateAbsenMasuk = dateFormat.parse(data.absenMasuk)
                                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                    dateAbsenMasuk?.let {
                                        Text(
                                            text = sdf.format(it),
                                            fontFamily = fontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = Color.Black,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    val dateAbsenKeluar = dateFormat.parse(data.absenKeluar)
                                    dateAbsenKeluar?.let {
                                        Text(
                                            text = sdf.format(it),
                                            fontFamily = fontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = Color.Black,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    Text(
                                        text = if (data.status.toString() == "0") "Overdue" else "On Time",
                                        fontFamily = fontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (data.status == "0") red else green,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Divider(color = divider, thickness = 2.dp)

                            }
                        }
                    }

                    is ResultState.Error -> {
                        Log.e(
                            "HomeScreen",
                            "Error: ${(getAbsenResponseState as ResultState.Error).errorMessage}"
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    // Show DatePickerDialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { date ->
                selectedDate.time = date
                selectedYear.value = selectedDate.get(Calendar.YEAR)
                selectedMonth.value = selectedDate.get(Calendar.MONTH)
                showDatePicker = false
            }
        )
    }
}


fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("en", "EN"))
    return sdf.format(Date())
}

fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
}

// fungsi DatePickerDialog
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Date) -> Unit
) {
    val dialog = remember { mutableStateOf(true) }
    if (dialog.value) {
        AlertDialog(
            onDismissRequest = { onDismissRequest() },
            confirmButton = {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val calendar = remember { Calendar.getInstance() }
                    AndroidView(
                        modifier = Modifier.wrapContentSize(),
                        factory = { context ->
                            DatePicker(context).apply {
                                init(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ) { _, year, month, dayOfMonth ->
                                    calendar.set(year, month, dayOfMonth)
                                    onDateSelected(calendar.time)
                                }
                            }
                        }
                    )
                }
            }
        )
    }
}