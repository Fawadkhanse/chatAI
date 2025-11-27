package com.example.chatai.presentation.components

import com.example.chatai.ui.theme.PurpleGrey80

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.chatai.R





@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (isError) MaterialTheme.colorScheme.error
                        else PurpleGrey80
                    )
                }
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = if (passwordVisible)
                                painterResource(R.drawable.visibility)
                            else
                                painterResource(R.drawable.visibility_off),
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = if (isError) MaterialTheme.colorScheme.error
                            else PurpleGrey80
                        )
                    }
                }
            } else if (trailingIcon != null) {
                {
                    IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = if (isError) MaterialTheme.colorScheme.error
                            else PurpleGrey80
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation()
            else
                VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            enabled = enabled,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurpleGrey80,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                focusedLabelColor = PurpleGrey80,
                cursorColor = PurpleGrey80,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error
            )
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}