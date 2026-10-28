package com.mobiletaplay.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mobiletaplay.app.data.Tower

/** B/新需求：封面四角信息 + 全名 + 作者 + 4 行简介 */
@Composable
fun TowerCard(
    t: Tower,
    fav: Boolean,
    onClick: () -> Unit,
    onFav: () -> Unit,
) {
    Card(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
    ) {
        Column {
            Box(Modifier.height(160.dp)) {
                AsyncImage(
                    model = t.image,
                    contentDescription = t.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                            )
                        )
                )
                Text(
                    "★ ${t.rate ?: "—"}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                )
                IconButton(
                    onClick = onFav,
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "收藏",
                        tint = if (fav) Color(0xFFF0B429) else Color.White.copy(alpha = 0.85f),
                    )
                }
                Text(
                    "${t.floors ?: "?"}层",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                )
                Text(
                    "${t.people ?: "0"}游玩",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                )
            }
            Column(Modifier.padding(12.dp)) {
                Text(
                    t.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    t.author,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                val desc = plainText(t.description ?: t.text ?: "")
                if (desc.isNotEmpty()) {
                    Text(
                        desc,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

internal fun plainText(html: String?): String {
    if (html.isNullOrBlank()) return ""
    return html
        .replace(Regex("<style[\\s\\S]*?</style>"), "")
        .replace(Regex("<script[\\s\\S]*?</script>"), "")
        .replace(Regex("<br\\s*/?>"), "\n")
        .replace(Regex("</p>"), "\n")
        .replace(Regex("<[^>]+>"), "")
        .replace("&nbsp;", " ")
        .trim()
}
