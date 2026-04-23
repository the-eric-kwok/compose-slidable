package com.erickwok.composeslidable.demoshared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erickwok.composeslidable.SlidableAction
import com.erickwok.composeslidable.SlidableCell

private data class DemoItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val liked: Boolean = false,
    val archived: Boolean = false
)

@Preview
@Composable
fun DemoApp() {
    MaterialTheme {
        Surface(color = Color(0xFFF4F6FB)) {
            SlidableCellDemo()
        }
    }
}

@Composable
private fun SlidableCellDemo() {
    val items = remember {
        mutableStateListOf(
            DemoItem(1, "Nebula Cam", "左滑试试收藏，右滑可归档或删除"),
            DemoItem(2, "Telescope One", "支持 overslide primary action"),
            DemoItem(3, "Night Vision Pro", "当前是 CMP + iOS host demo")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6FB))
            .safeDrawingPadding()
            .padding(top = 20.dp)
    ) {
        Text(
            text = "SlidableCell Demo",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = "左滑可以编辑/收藏，右滑可以归档/删除。删除按钮设为 primary，可继续 overslide 触发。",
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.size(20.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items, key = { it.id }) { item ->
                SlidableCell(
                    startActions = listOf(
                        SlidableAction(
                            icon = Icons.Outlined.Edit,
                            label = "编辑",
                            backgroundColor = Color(0xFF2563EB),
                            onClick = {
                                items.replace(item.id) { current ->
                                    current.copy(subtitle = "刚刚点了编辑，通常这里会进入详情页")
                                }
                            }
                        ),
                        SlidableAction(
                            icon = Icons.Outlined.FavoriteBorder,
                            label = if (item.liked) "已收藏" else "收藏",
                            backgroundColor = Color(0xFFF97316),
                            onClick = {
                                items.replace(item.id) { current ->
                                    current.copy(liked = !current.liked)
                                }
                            }
                        )
                    ),
                    endActions = listOf(
                        SlidableAction(
                            icon = Icons.Outlined.Archive,
                            label = if (item.archived) "已归档" else "归档",
                            backgroundColor = Color(0xFF0F766E),
                            onClick = {
                                items.replace(item.id) { current ->
                                    current.copy(archived = !current.archived)
                                }
                            }
                        ),
                        SlidableAction(
                            icon = Icons.Outlined.Delete,
                            label = "删除",
                            backgroundColor = Color(0xFFDC2626),
                            isPrimary = true,
                            onClick = {
                                items.removeAll { it.id == item.id }
                            }
                        )
                    )
                ) {
                    DemoCard(
                        item = item,
                        onClick = {
                            items.replace(item.id) { current ->
                                current.copy(subtitle = "刚刚点击了 item（id=${current.id}）")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoCard(
    item: DemoItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E7FF))
                    )
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text(
                            text = item.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = item.subtitle,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                if (item.liked || item.archived) {
                    Text(
                        text = buildString {
                            if (item.liked) append("已收藏")
                            if (item.liked && item.archived) append(" · ")
                            if (item.archived) append("已归档")
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF374151)
                    )
                }
            }
        }
    }
}

private fun SnapshotStateList<DemoItem>.replace(
    id: Int,
    transform: (DemoItem) -> DemoItem
) {
    val index = indexOfFirst { it.id == id }
    if (index >= 0) {
        this[index] = transform(this[index])
    }
}
