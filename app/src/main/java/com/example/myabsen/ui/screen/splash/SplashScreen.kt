package com.example.myabsen.ui.screen.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myabsen.R
import com.example.myabsen.ui.theme.MyAbsenTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onTimeout: () -> Unit
) {
    Image(
        painter = painterResource(id = R.drawable.logo_fix),
        contentDescription = null,
        modifier = modifier
            .width(200.dp)
            .height(200.dp)
    )

    LaunchedEffect(true) {
        delay(3000)
        onTimeout()
    }
}

@Preview(device = Devices.PIXEL_4, showBackground = true)
@Composable
fun SplashScreenView() {
    MyAbsenTheme {
        SplashScreen(onTimeout = { })
    }
}