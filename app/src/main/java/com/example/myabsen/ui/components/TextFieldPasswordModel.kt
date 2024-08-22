package com.example.myabsen.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myabsen.R
import com.example.myabsen.ui.theme.MyAbsenTheme
import com.example.myabsen.ui.theme.fontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldPasswordModel(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val showPassword = rememberSaveable { mutableStateOf(false) }
    val isError = value.isNotEmpty() && value.length < 8

    Column(
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newText -> onValueChange(newText) },
            label = {
                Text(
                    text = label,
                    fontSize = 16.sp
                )
            },
            modifier = modifier,
            shape = RoundedCornerShape(30.dp),
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                cursorColor = Color.Black,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Black,
                disabledBorderColor = Color.Black,
            ),
            trailingIcon = {
                if (showPassword.value) {
                    IconButton(onClick = { showPassword.value = false }) {
                        Icon(
                            painter = painterResource(id = R.drawable.visibility),
                            modifier = Modifier.size(24.dp),
                            contentDescription = stringResource(id = R.string.hide_password)
                        )
                    }
                } else {
                    IconButton(onClick = { showPassword.value = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.visibility_off),
                            modifier = Modifier.size(24.dp),
                            contentDescription = stringResource(id = R.string.show_password)
                        )
                    }
                }
            },
            visualTransformation = if (showPassword.value) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            isError = isError,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isError) {
            Text(
                text = stringResource(R.string.password_error),
                fontFamily = fontFamily,
                fontSize = 14.sp,
                color = Color.Red,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TextFieldPasswordModelPreview() {
    MyAbsenTheme {
        TextFieldPasswordModel(
            label = "Enter your password",
            value = "",
            onValueChange = {}
        )
    }
}