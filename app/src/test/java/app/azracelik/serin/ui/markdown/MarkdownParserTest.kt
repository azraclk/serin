package app.azracelik.serin.ui.markdown

import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownParserTest {
    @Test
    fun parsesBlocks() {
        val blocks = parseMarkdown(
            """
            Intro line one
            continues here.

            ## Heading

            - first
              wraps
            - second

            1. one
            2. two
            """.trimIndent(),
        )
        assertEquals(
            listOf(
                MdBlock.Paragraph(listOf(MdSpan("Intro line one continues here."))),
                MdBlock.Heading(2, listOf(MdSpan("Heading"))),
                MdBlock.ListBlock(false, listOf(listOf(MdSpan("first wraps")), listOf(MdSpan("second")))),
                MdBlock.ListBlock(true, listOf(listOf(MdSpan("one")), listOf(MdSpan("two")))),
            ),
            blocks,
        )
    }

    @Test
    fun parsesInlineStyles() {
        assertEquals(
            listOf(
                MdSpan("A "),
                MdSpan("bold", bold = true),
                MdSpan(", "),
                MdSpan("italic", italic = true),
                MdSpan(" and "),
                MdSpan("a link", url = "https://example.com/a"),
                MdSpan("."),
            ),
            parseInline("A **bold**, *italic* and [a link](https://example.com/a)."),
        )
    }

    @Test
    fun keepsPlainTextWithoutMarkup() {
        assertEquals(listOf(MdSpan("2 * 3 = 6")), parseInline("2 * 3 = 6"))
    }
}
