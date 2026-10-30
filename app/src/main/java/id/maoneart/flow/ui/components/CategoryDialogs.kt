package id.maoneart.flow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.maoneart.flow.model.AppInfo
import id.maoneart.flow.model.CategoryItem
import id.maoneart.flow.ui.theme.FlowDarkBackground
import id.maoneart.flow.ui.theme.FlowElectricBlue
import id.maoneart.flow.ui.theme.FlowRed
import id.maoneart.flow.ui.theme.FlowTextPrimary
import id.maoneart.flow.ui.theme.FlowTextSecondary

/**
 * MaoneArt Glassmorphism Dialog to create a new category.
 * Features 2-column symmetric action buttons for guaranteed easy touch.
 */
@Composable
fun CreateCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xF016181F))
                .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Folder,
                        contentDescription = null,
                        tint = FlowElectricBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Buat Kategori Baru",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlowTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Ketik nama kategori untuk mengelompokkan aplikasi Anda.",
                    fontSize = 13.sp,
                    color = FlowTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x22FFFFFF))
                        .border(1.dp, if (categoryName.isNotEmpty()) FlowElectricBlue else Color(0x22FFFFFF), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    if (categoryName.isEmpty()) {
                        Text(
                            text = "Misal: Belajar, Kerja, Crypto...",
                            fontSize = 14.sp,
                            color = Color(0x66FFFFFF)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = categoryName,
                            onValueChange = { categoryName = it },
                            textStyle = TextStyle(
                                color = FlowTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(FlowElectricBlue),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (categoryName.isNotBlank()) {
                                        onConfirm(categoryName.trim())
                                    }
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                        )

                        if (categoryName.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear",
                                tint = FlowTextSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { categoryName = "" }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Symmetric 2-column Action Buttons (MaoneArt Standard)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tombol Batal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x22FFFFFF))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Batal",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FlowTextPrimary
                        )
                    }

                    // Tombol Simpan
                    val canSave = categoryName.isNotBlank()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (canSave) FlowElectricBlue else Color(0x3300E5FF))
                            .clickable(
                                enabled = canSave,
                                onClick = {
                                    if (canSave) {
                                        onConfirm(categoryName.trim())
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Simpan",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canSave) Color.Black else FlowTextSecondary
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Throwable) {}
    }
}

/**
 * Confirmation dialog to delete or hide a category.
 * Features 2-column symmetric action buttons.
 */
@Composable
fun DeleteCategoryDialog(
    category: CategoryItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xF016181F))
                .border(1.dp, Color(0x33FF5252), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = FlowRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Hapus Kategori",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlowTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Hapus kategori \"${category.title}\" dari daftar tampilan? Aplikasi di dalamnya tidak akan terhapus.",
                    fontSize = 14.sp,
                    color = FlowTextSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Symmetric 2-column Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tombol Batal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x22FFFFFF))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Batal",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FlowTextPrimary
                        )
                    }

                    // Tombol Hapus
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(FlowRed)
                            .clickable(onClick = onConfirm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Hapus",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bottom sheet to assign an application to a specific category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectCategoryBottomSheet(
    app: AppInfo,
    availableCategories: List<CategoryItem>,
    onCategorySelected: (String) -> Unit,
    onCreateNewCategory: () -> Unit,
    onResetToDefault: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FlowDarkBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = FlowElectricBlue,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Atur Kategori",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlowTextPrimary
                    )
                    Text(
                        text = app.label,
                        fontSize = 13.sp,
                        color = FlowTextSecondary
                    )
                }
            }

            // Categories List (exclude 'all' and deduplicate strictly)
            val selectableCategories = availableCategories
                .filter { it.id != "all" }
                .distinctBy { it.id }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
            ) {
                itemsIndexed(
                    items = selectableCategories,
                    key = { index, category -> "select_cat_${category.id}_$index" }
                ) { _, category ->
                    val isCurrent = app.categoryId == category.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrent) Color(0x1F00E5FF) else Color.Transparent)
                            .clickable {
                                onCategorySelected(category.id)
                                onDismiss()
                            }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.icon,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = category.title,
                            fontSize = 15.sp,
                            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isCurrent) FlowElectricBlue else FlowTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Terpilih",
                                tint = FlowElectricBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action: Create New Category
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onDismiss()
                        onCreateNewCategory()
                    }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = FlowElectricBlue,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Buat Kategori Baru...",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = FlowElectricBlue
                )
            }

            // Action: Reset to Auto-detected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onResetToDefault()
                        onDismiss()
                    }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "↺",
                    fontSize = 18.sp,
                    color = FlowTextSecondary,
                    modifier = Modifier.padding(start = 2.dp, end = 12.dp)
                )
                Text(
                    text = "Kembalikan ke Kategori Bawaan",
                    fontSize = 14.sp,
                    color = FlowTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
