package app.azracelik.serin.ui.markdown

/**
 * Blog yazıları için Markdown'ın küçük bir alt kümesi: `##` başlıklar, paragraflar, `-` ve `1.`
 * listeler, satır içinde **kalın**, *italik* ve [bağlantı](https://...).
 */
sealed interface MdBlock {
    data class Heading(val level: Int, val spans: List<MdSpan>) : MdBlock
    data class Paragraph(val spans: List<MdSpan>) : MdBlock
    data class ListBlock(val ordered: Boolean, val items: List<List<MdSpan>>) : MdBlock
}

data class MdSpan(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val url: String? = null,
)

private val HeadingRegex = Regex("""^(#{1,3})\s+(.*)$""")
private val BulletRegex = Regex("""^[-*]\s+(.*)$""")
private val OrderedRegex = Regex("""^\d+[.)]\s+(.*)$""")
private val InlineRegex = Regex("""\*\*(.+?)\*\*|\*(.+?)\*|\[([^\]]+)]\(([^)\s]+)\)""")

fun parseMarkdown(text: String): List<MdBlock> {
    val blocks = mutableListOf<MdBlock>()
    val paragraph = mutableListOf<String>()
    var listOrdered = false
    val listItems = mutableListOf<String>()

    fun flushParagraph() {
        if (paragraph.isNotEmpty()) {
            blocks += MdBlock.Paragraph(parseInline(paragraph.joinToString(" ")))
            paragraph.clear()
        }
    }

    fun flushList() {
        if (listItems.isNotEmpty()) {
            blocks += MdBlock.ListBlock(listOrdered, listItems.map(::parseInline))
            listItems.clear()
        }
    }

    for (raw in text.lines()) {
        val line = raw.trim()
        val heading = HeadingRegex.find(line)
        val bullet = BulletRegex.find(line)
        val ordered = OrderedRegex.find(line)
        when {
            line.isEmpty() -> {
                flushParagraph()
                flushList()
            }
            heading != null -> {
                flushParagraph()
                flushList()
                blocks += MdBlock.Heading(heading.groupValues[1].length, parseInline(heading.groupValues[2]))
            }
            bullet != null || ordered != null -> {
                flushParagraph()
                val isOrdered = bullet == null
                if (listItems.isNotEmpty() && listOrdered != isOrdered) flushList()
                listOrdered = isOrdered
                listItems += (bullet ?: ordered)!!.groupValues[1]
            }
            // Liste maddesinin alt satıra taşan devamı.
            listItems.isNotEmpty() -> listItems[listItems.lastIndex] += " $line"
            else -> paragraph += line
        }
    }
    flushParagraph()
    flushList()
    return blocks
}

fun parseInline(text: String): List<MdSpan> {
    val spans = mutableListOf<MdSpan>()
    var index = 0
    for (match in InlineRegex.findAll(text)) {
        if (match.range.first > index) spans += MdSpan(text.substring(index, match.range.first))
        val (bold, italic, linkText, url) = match.destructured
        spans += when {
            bold.isNotEmpty() -> MdSpan(bold, bold = true)
            italic.isNotEmpty() -> MdSpan(italic, italic = true)
            else -> MdSpan(linkText, url = url)
        }
        index = match.range.last + 1
    }
    if (index < text.length) spans += MdSpan(text.substring(index))
    return spans
}
