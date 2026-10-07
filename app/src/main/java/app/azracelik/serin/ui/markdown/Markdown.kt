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
import app.azracelik.serin.ui.theme.SerinPurple
import app.azracelik.serin.ui.theme.SerinType

private val LinkStyles = TextLinkStyles(SpanStyle(color = SerinPurple, textDecoration = TextDecoration.Underline))

@Composable
fun Markdown(blocks: List<MdBlock>, modifier: Modifier = Modifier) {
    Column(modifier) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is MdBlock.Heading -> {
                    if (index > 0) Spacer(Modifier.height(28.dp))
                    Text(
                        block.spans.toAnnotatedString(),
                        style = if (block.level <= 2) SerinType.PostHeading else SerinType.PostSubheading,
                    )
                    Spacer(Modifier.height(10.dp))
                }
                is MdBlock.Paragraph -> {
                    Text(block.spans.toAnnotatedString(), style = SerinType.PostBody)
                    Spacer(Modifier.height(16.dp))
                }
                is MdBlock.ListBlock -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        block.items.forEachIndexed { itemIndex, item ->
                            Row {
                                Box(Modifier.width(24.dp)) {
                                    if (block.ordered) {
                                        Text("${itemIndex + 1}.", style = SerinType.PostBody.copy(color = SerinPurple))
                                    } else {
                                        // Satır yüksekliği 26sp; nokta ilk satırın ortasına hizalanır.
                                        Box(
                                            Modifier
                                                .padding(top = 10.dp, start = 2.dp)
                                                .size(6.dp)
                                                .background(SerinPurple, CircleShape),
                                        )
                                    }
                                }
                                Text(item.toAnnotatedString(), style = SerinType.PostBody)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun List<MdSpan>.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    for (span in this@toAnnotatedString) {
        val style = SpanStyle(
            fontWeight = if (span.bold) FontWeight.Bold else null,
            fontStyle = if (span.italic) FontStyle.Italic else null,
        )
        if (span.url != null) {
            withLink(LinkAnnotation.Url(span.url, LinkStyles)) { withStyle(style) { append(span.text) } }
        } else {
            withStyle(style) { append(span.text) }
        }
    }
}
