package app.azracelik.serin.ui.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType

@Composable
fun Markdown(blocks: List<MdBlock>, modifier: Modifier = Modifier) {
    val colors = SerinTheme.colors
    val body = SerinType.PostBody.copy(color = colors.textSoft)
    val links = TextLinkStyles(SpanStyle(color = colors.accent, textDecoration = TextDecoration.Underline))
    Column(modifier) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is MdBlock.Heading -> {
                    if (index > 0) Spacer(Modifier.height(28.dp))
                    Text(
                        block.spans.toAnnotatedString(links),
                        style = if (block.level <= 2) SerinType.PostHeading.copy(color = colors.accent) else SerinType.PostSubheading,
                    )
                    Spacer(Modifier.height(10.dp))
                }
                is MdBlock.Paragraph -> {
                    Text(block.spans.toAnnotatedString(links), style = body)
                    Spacer(Modifier.height(16.dp))
                }
                is MdBlock.ListBlock -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        block.items.forEachIndexed { itemIndex, item ->
                            Row {
                                Box(Modifier.width(24.dp)) {
                                    if (block.ordered) {
                                        Text("${itemIndex + 1}.", style = body.copy(color = colors.accent))
                                    } else {
                                        // Satır yüksekliği 26sp; nokta ilk satırın ortasına hizalanır.
                                        Box(
                                            Modifier
                                                .padding(top = 10.dp, start = 2.dp)
                                                .size(6.dp)
                                                .background(colors.accent, CircleShape),
                                        )
                                    }
                                }
                                Text(item.toAnnotatedString(links), style = body)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun List<MdSpan>.toAnnotatedString(links: TextLinkStyles): AnnotatedString = buildAnnotatedString {
    for (span in this@toAnnotatedString) {
        val style = SpanStyle(
            fontWeight = if (span.bold) FontWeight.Bold else null,
            fontStyle = if (span.italic) FontStyle.Italic else null,
        )
        if (span.url != null) {
            withLink(LinkAnnotation.Url(span.url, links)) { withStyle(style) { append(span.text) } }
        } else {
            withStyle(style) { append(span.text) }
        }
    }
}
