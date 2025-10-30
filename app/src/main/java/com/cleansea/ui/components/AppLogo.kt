package com.cleansea.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cleansea.R // Убедитесь, что у вас есть файл R для ресурсов

@Composable
fun AppLogo() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Если у вас есть изображение логотипа в drawable, используйте его:
        // Например, если у вас есть файл logo.png в res/drawable
        // Image(
        //     painter = painterResource(id = R.drawable.logo),
        //     contentDescription = "App Logo",
        //     modifier = Modifier
        //         .size(120.dp)
        //         .clip(CircleShape) // Пример: если вы хотите круглое лого
        // )

        // В качестве простой заглушки или если у вас текстовый логотип:
        Text(
            text = "CleanSea",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}