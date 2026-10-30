package id.maoneart.flow.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maoneart.flow.model.CategoryItem
import id.maoneart.flow.ui.theme.FlowElectricBlue
import id.maoneart.flow.ui.theme.FlowTextPrimary
import id.maoneart.flow.ui.theme.FlowTextSecondary
import id.maoneart.flow.ui.util.rememberFlowHaptics

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryChipBar(
    categories: List<CategoryItem>,
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit,
    onAddCategoryClick: () -> Unit,
    onCategoryLongClick: (CategoryItem) -> Unit,
    hapticsEnabled: Boolean = true,
    chipListState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val haptics = rememberFlowHaptics()

    // Immediately anchor scroll to LEFT (index 0, "Semua") whenever category is "all"
    LaunchedEffect(selectedCategoryId) {
        if (selectedCategoryId == "all") {
            try {
                chipListState.scrollToItem(0)
            } catch (_: Throwable) {}
        } else {
            val index = categories.indexOfFirst { it.id == selectedCategoryId }
            if (index >= 0) {
                try {
                    chipListState.animateScrollToItem(index)
                } catch (_: Throwable) {}
            }
        }
    }

    LazyRow(
        state = chipListState,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(
            items = categories,
            key = { index, category -> "chip_${category.id}_$index" }
        ) { _, category ->
            val isSelected = category.id == selectedCategoryId

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) FlowElectricBlue.copy(alpha = 0.22f) else Color(0x1AFFFFFF),
                animationSpec = tween(durationMillis = 150),
                label = "bgColor"
            )

            val borderColor by animateColorAsState(
                targetValue = if (isSelected) FlowElectricBlue else Color(0x22FFFFFF),
                animationSpec = tween(durationMillis = 150),
                label = "borderColor"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) FlowTextPrimary else FlowTextSecondary,
                animationSpec = tween(durationMillis = 150),
                label = "textColor"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .combinedClickable(
                        onClick = {
                            if (hapticsEnabled) {
                                haptics.click(view)
                            }
                            onCategorySelected(category.id)
                        },
                        onLongClick = if (category.id != "all") {
                            {
                                if (hapticsEnabled) {
                                    haptics.heavyClick(view)
                                }
                                onCategoryLongClick(category)
                            }
                        } else null
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.icon,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )

                    Text(
                        text = category.title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = textColor
                    )

                    if (category.appCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) FlowElectricBlue.copy(alpha = 0.35f)
                                    else Color(0x1FFFFFFF)
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${category.appCount}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White else FlowTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Add Category Button
        item(key = "add_category_button") {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x1A00E5FF))
                    .border(1.dp, FlowElectricBlue.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .combinedClickable(
                        onClick = {
                            if (hapticsEnabled) {
                                haptics.click(view)
                            }
                            onAddCategoryClick()
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Tambah Kategori",
                        tint = FlowElectricBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Kategori",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = FlowElectricBlue
                    )
                }
            }
        }
    }
}
