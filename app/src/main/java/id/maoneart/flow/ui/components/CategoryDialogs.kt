package id.maoneart.flow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maoneart.flow.model.AppInfo
import id.maoneart.flow.model.CategoryItem
import id.maoneart.flow.ui.theme.FlowDarkBackground
import id.maoneart.flow.ui.theme.FlowElectricBlue
import id.maoneart.flow.ui.theme.FlowRed
import id.maoneart.flow.ui.theme.FlowTextPrimary
import id.maoneart.flow.ui.theme.FlowTextSecondary

/**
 * Dialog to create a new user-defined custom category.
 */
@Composable
fun CreateCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FlowDarkBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Buat Kategori Baru",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = FlowTextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Masukkan nama kategori untuk mengelompokkan aplikasi Anda.",
                    fontSize = 14.sp,
                    color = FlowTextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x22FFFFFF))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    if (categoryName.isEmpty()) {
                        Text(
                            text = "Contoh: Belajar, Kerja, Crypto...",
                            fontSize = 14.sp,
                            color = Color(0x66FFFFFF)
                        )
                    }
                    BasicTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        textStyle = TextStyle(
                            color = FlowTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(FlowElectricBlue),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onConfirm(categoryName.trim())
                    }
                },
                enabled = categoryName.isNotBlank()
            ) {
                Text(
                    text = "Simpan",
                    color = if (categoryName.isNotBlank()) FlowElectricBlue else FlowTextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Batal", color = FlowTextSecondary)
            }
        }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
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

            // Categories List (exclude 'all')
            val selectableCategories = availableCategories.filter { it.id != "all" }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                items(
                    items = selectableCategories,
                    key = { it.id }
                ) { category ->
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

/**
 * Confirmation dialog to delete a custom user category.
 */
@Composable
fun DeleteCategoryDialog(
    category: CategoryItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FlowDarkBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Hapus Kategori",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = FlowTextPrimary
            )
        },
        text = {
            Text(
                text = "Yakin ingin menghapus kategori \"${category.title}\"? Aplikasi di dalamnya akan kembali ke kategori aslinya.",
                fontSize = 14.sp,
                color = FlowTextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Hapus", color = FlowRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Batal", color = FlowTextSecondary)
            }
        }
    )
}
