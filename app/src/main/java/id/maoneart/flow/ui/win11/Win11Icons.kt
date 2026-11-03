package id.maoneart.flow.ui.win11

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import id.maoneart.flow.R

/**
 * Authentic Windows 11 Fluent Design System Icons
 * Uses official 256x256 Fluent assets extracted from Windows 11 binaries.
 */
object Win11Icons {

    /**
     * Iconic 4-Square Windows 11 Logo
     */
    @Composable
    fun WindowsLogo(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_win11_logo),
            contentDescription = "Windows 11 Start",
            modifier = modifier.size(size)
        )
    }

    /**
     * Iconic Windows 11 "This PC" / Computer Desktop Icon
     */
    @Composable
    fun ThisPcIcon(
        modifier: Modifier = Modifier,
        size: Dp = 44.dp
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_win11_this_pc),
            contentDescription = "This PC",
            modifier = modifier.size(size)
        )
    }

    /**
     * Windows 11 Recycle Bin Icon
     */
    @Composable
    fun RecycleBinIcon(
        modifier: Modifier = Modifier,
        size: Dp = 44.dp
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_win11_recycle_bin),
            contentDescription = "Recycle Bin",
            modifier = modifier.size(size)
        )
    }

    /**
     * Windows 11 File Explorer Icon
     */
    @Composable
    fun FileExplorerIcon(
        modifier: Modifier = Modifier,
        size: Dp = 32.dp
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_win11_explorer),
            contentDescription = "File Explorer",
            modifier = modifier.size(size)
        )
    }

    /**
     * Windows 11 Microsoft Edge Browser Icon
     */
    @Composable
    fun EdgeBrowserIcon(
        modifier: Modifier = Modifier,
        size: Dp = 32.dp
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_win11_edge),
            contentDescription = "Microsoft Edge",
            modifier = modifier.size(size)
        )
    }
}
