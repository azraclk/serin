package app.azracelik.serin.ui.adaptive

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WindowSizeTest {
    private fun size(
        width: WidthClass = WidthClass.Compact,
        height: HeightClass = HeightClass.Regular,
        contentWidth: Int = 400,
    ) = WindowSize(width, height, contentWidth = contentWidth.dp, windowHeight = 800.dp)

    @Test
    fun widthClass_followsMaterialBreakpoints() {
        assertEquals(WidthClass.Compact, WidthClass.of(320.dp))
        assertEquals(WidthClass.Compact, WidthClass.of(599.dp))
        assertEquals(WidthClass.Medium, WidthClass.of(600.dp))
        assertEquals(WidthClass.Medium, WidthClass.of(839.dp))
        assertEquals(WidthClass.Expanded, WidthClass.of(840.dp))
        assertEquals(WidthClass.Expanded, WidthClass.of(1280.dp))
    }

    @Test
    fun heightClass_isCompactBelow480() {
        assertEquals(HeightClass.Compact, HeightClass.of(411.dp))
        assertEquals(HeightClass.Compact, HeightClass.of(479.dp))
        assertEquals(HeightClass.Regular, HeightClass.of(480.dp))
        assertEquals(HeightClass.Regular, HeightClass.of(891.dp))
    }

    @Test
    fun rail_isUsedOnWideOrShortWindows() {
        assertFalse(size(WidthClass.Compact, HeightClass.Regular).useRail)
        assertTrue(size(WidthClass.Medium, HeightClass.Regular).useRail)
        assertTrue(size(WidthClass.Expanded, HeightClass.Regular).useRail)
        // Yatay küçük telefon: dar ama alçak.
        assertTrue(size(WidthClass.Compact, HeightClass.Compact).useRail)
    }

    @Test
    fun sidePadding_centersContentOnlyWhenWiderThanMax() {
        // 1000dp pencerede 640dp'lik sütun: kalan 360dp iki yana bölünür.
        assertEquals(180.dp, size(contentWidth = 1000).sidePadding(maxContent = 640.dp, min = 28.dp))
        // Sütundan dar pencerede en az kenar boşluğu kalır.
        assertEquals(28.dp, size(contentWidth = 393).sidePadding(maxContent = 640.dp, min = 28.dp))
        // Tam sınırda boşluk yok, en az değer geçerli.
        assertEquals(28.dp, size(contentWidth = 640).sidePadding(maxContent = 640.dp, min = 28.dp))
    }

    @Test
    fun columns_fitMinCellWidthWithinBounds() {
        val s = size()
        // 393dp telefon: 22dp kenar boşluğu düşülür, 165dp'lik iki kart sığar.
        assertEquals(2, s.columns(available = 371.dp, minCell = 165.dp, spacing = 21.dp, max = 5, min = 2))
        // Çok dar pencerede en az sütun sayısı korunur.
        assertEquals(2, s.columns(available = 200.dp, minCell = 165.dp, spacing = 21.dp, max = 5, min = 2))
        // Orta genişlikte üç sütun.
        assertEquals(3, s.columns(available = 682.dp, minCell = 165.dp, spacing = 21.dp, max = 5, min = 2))
        // Çok geniş pencerede üst sınır geçerli.
        assertEquals(5, s.columns(available = 3000.dp, minCell = 165.dp, spacing = 21.dp, max = 5, min = 2))
    }

    @Test
    fun landscapeShort_followsHeightClassOnly() {
        assertTrue(size(WidthClass.Expanded, HeightClass.Compact).isLandscapeShort)
        assertFalse(size(WidthClass.Expanded, HeightClass.Regular).isLandscapeShort)
    }
}
