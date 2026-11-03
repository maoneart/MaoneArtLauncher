package id.maoneart.flow.ui.win11

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import id.maoneart.flow.R

/**
 * Authentic Windows 11 Bloom Dark Official 4K Wallpaper
 * Scales seamlessly to both Portrait and Landscape orientations.
 */
@Composable
fun Win11BloomWallpaper(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D14))
    ) {
        Image(
            painter = painterResource(id = R.drawable.win11_bloom_dark),
            contentDescription = "Windows 11 Bloom Dark Wallpaper",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
