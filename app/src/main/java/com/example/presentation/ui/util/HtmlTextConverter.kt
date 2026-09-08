package com.example.presentation.ui.util

import android.graphics.Typeface
import android.text.Html
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Represents structured blocks extracted from message HTML.
 */
sealed class HtmlBlock {
    data class Text(val htmlText: String) : HtmlBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : HtmlBlock()
}

/**
 * Parses raw HTML into blocks of text and structured tables.
 */
fun parseHtmlBlocks(rawHtml: String): List<HtmlBlock> {
    if (rawHtml.isBlank()) return emptyList()

    var html = rawHtml.trim()

    // Handle double-encoded HTML entities if present (e.g., &lt;table&gt; -> <table>)
    if ((html.contains("&lt;table") || html.contains("&lt;p") || html.contains("&lt;div")) &&
        !html.contains("<table") && !html.contains("<p")) {
        try {
            html = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
        } catch (_: Exception) {}
    }

    val blocks = mutableListOf<HtmlBlock>()
    val tableRegex = Regex("(?is)<table[^>]*>(.*?)</table>")
    var lastIndex = 0

    tableRegex.findAll(html).forEach { matchResult ->
        val textBefore = html.substring(lastIndex, matchResult.range.first).trim()
        if (textBefore.isNotBlank()) {
            blocks.add(HtmlBlock.Text(textBefore))
        }

        val tableInnerHtml = matchResult.groupValues[1]
        val parsedTable = parseSingleTable(tableInnerHtml)
        if (parsedTable != null && (parsedTable.headers.isNotEmpty() || parsedTable.rows.isNotEmpty())) {
            blocks.add(parsedTable)
        } else {
            blocks.add(HtmlBlock.Text(matchResult.value))
        }

        lastIndex = matchResult.range.last + 1
    }

    val remainingText = html.substring(lastIndex).trim()
    if (remainingText.isNotBlank()) {
        blocks.add(HtmlBlock.Text(remainingText))
    }

    return blocks
}

private fun parseSingleTable(tableHtml: String): HtmlBlock.Table? {
    val trRegex = Regex("(?is)<tr[^>]*>(.*?)</tr>")
    val thRegex = Regex("(?is)<th[^>]*>(.*?)</th>")
    val tdRegex = Regex("(?is)<td[^>]*>(.*?)</td>")

    val trMatches = trRegex.findAll(tableHtml).toList()
    if (trMatches.isEmpty()) return null

    val rawHeaders = mutableListOf<String>()
    val rawRows = mutableListOf<List<String>>()

    for (tr in trMatches) {
        val rowHtml = tr.groupValues[1]
        val ths = thRegex.findAll(rowHtml).map { cleanCellHtml(it.groupValues[1]) }.toList()
        val tds = tdRegex.findAll(rowHtml).map { cleanCellHtml(it.groupValues[1]) }.toList()

        if (ths.isNotEmpty()) {
            rawHeaders.addAll(ths)
        } else if (tds.isNotEmpty()) {
            rawRows.add(tds)
        }
    }

    var finalHeaders = rawHeaders.toList()
    var finalRows = rawRows.toList()

    // Fallback: if no <th> was specified, use the first <td> row as header if there are multiple rows
    if (finalHeaders.isEmpty() && finalRows.size > 1) {
        finalHeaders = finalRows.first()
        finalRows = finalRows.drop(1)
    }

    if (finalHeaders.isEmpty() && finalRows.isEmpty()) return null

    return HtmlBlock.Table(finalHeaders, finalRows)
}

private fun cleanCellHtml(rawCell: String): String {
    return rawCell
        .replace(Regex("(?i)<br\\s*/?>"), " ")
        .replace(Regex("""\s+"""), " ")
        .trim()
}

/**
 * Preprocesses text HTML from Neptun messages into clean HTML with proper newlines.
 */
fun preprocessNeptunHtml(rawHtml: String): String {
    if (rawHtml.isBlank()) return ""

    var html = rawHtml.trim()

    // Handle double-encoded HTML entities if present
    if ((html.contains("&lt;p") || html.contains("&lt;div") || html.contains("&lt;br")) &&
        !html.contains("<p") && !html.contains("<div")) {
        try {
            html = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
        } catch (_: Exception) {}
    }

    // Convert block HTML tags to explicit newlines before passing to Html.fromHtml
    html = html
        .replace(Regex("(?i)<p[^>]*>"), "\n\n")
        .replace(Regex("(?i)</p>"), "")
        .replace(Regex("(?i)<div[^>]*>"), "\n")
        .replace(Regex("(?i)</div>"), "")
        .replace(Regex("(?i)<br\\s*/?>"), "\n")
        .replace(Regex("(?i)<li[^>]*>"), "\n• ")
        .replace(Regex("(?i)</li>"), "")
        .replace(Regex("(?i)<ul[^>]*>"), "\n")
        .replace(Regex("(?i)</ul>"), "\n")
        .replace(Regex("(?i)<ol[^>]*>"), "\n")
        .replace(Regex("(?i)</ol>"), "\n")

    // Limit consecutive newlines to max 2
    html = html.replace(Regex("""\n{3,}"""), "\n\n")

    return html.trim()
}

/**
 * Renders a structured HTML Table as a Material 3 Compose Card with horizontal scrolling and auto column width.
 */
@Composable
fun HtmlTableView(
    table: HtmlBlock.Table,
    modifier: Modifier = Modifier
) {
    if (table.headers.isEmpty() && table.rows.isEmpty()) return

    val totalCols = maxOf(
        table.headers.size,
        table.rows.maxOfOrNull { it.size } ?: 0
    )
    if (totalCols == 0) return

    // Calculate column widths based on cell text length
    val colWidths = List(totalCols) { colIdx ->
        val headerLen = table.headers.getOrNull(colIdx)?.replace(Regex("<[^>]+>"), "")?.length ?: 0
        val maxRowLen = table.rows.maxOfOrNull {
            it.getOrNull(colIdx)?.replace(Regex("<[^>]+>"), "")?.length ?: 0
        } ?: 0
        val maxLen = maxOf(headerLen, maxRowLen)
        when {
            maxLen <= 8 -> 100.dp
            maxLen <= 16 -> 140.dp
            maxLen <= 25 -> 180.dp
            else -> 230.dp
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            Column {
                // Table Headers
                if (table.headers.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (colIdx in 0 until totalCols) {
                            val headerHtml = table.headers.getOrNull(colIdx) ?: ""
                            Text(
                                text = rememberHtmlAnnotatedString(headerHtml),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .width(colWidths[colIdx])
                                    .padding(horizontal = 8.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                }

                // Table Data Rows
                table.rows.forEachIndexed { rowIndex, row ->
                    val rowBg = if (rowIndex % 2 == 0) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    }

                    Row(
                        modifier = Modifier
                            .background(rowBg)
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (colIdx in 0 until totalCols) {
                            val cellHtml = row.getOrNull(colIdx) ?: ""
                            Text(
                                text = rememberHtmlAnnotatedString(cellHtml),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .width(colWidths[colIdx])
                                    .padding(horizontal = 8.dp)
                            )
                        }
                    }

                    if (rowIndex < table.rows.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Parses HTML string into an AnnotatedString preserving formatting, colors, bold, italic, links, etc.
 */
@Composable
fun rememberHtmlAnnotatedString(
    htmlString: String,
    linkColor: Color = MaterialTheme.colorScheme.primary
): AnnotatedString {
    return remember(htmlString, linkColor) {
        if (htmlString.isBlank()) return@remember AnnotatedString("")

        val cleanHtml = preprocessNeptunHtml(htmlString)
        val spanned: Spanned = try {
            Html.fromHtml(cleanHtml, Html.FROM_HTML_MODE_COMPACT)
        } catch (_: Exception) {
            return@remember AnnotatedString(cleanHtml)
        }

        val text = spanned.toString()
        val builder = AnnotatedString.Builder(text)

        val spans = spanned.getSpans(0, spanned.length, Any::class.java)
        for (span in spans) {
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            if (start < 0 || end <= start || start >= text.length || end > text.length) continue

            when (span) {
                is StyleSpan -> {
                    when (span.style) {
                        Typeface.BOLD -> builder.addStyle(
                            SpanStyle(fontWeight = FontWeight.Bold),
                            start,
                            end
                        )
                        Typeface.ITALIC -> builder.addStyle(
                            SpanStyle(fontStyle = FontStyle.Italic),
                            start,
                            end
                        )
                        Typeface.BOLD_ITALIC -> builder.addStyle(
                            SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
                            start,
                            end
                        )
                    }
                }
                is UnderlineSpan -> builder.addStyle(
                    SpanStyle(textDecoration = TextDecoration.Underline),
                    start,
                    end
                )
                is StrikethroughSpan -> builder.addStyle(
                    SpanStyle(textDecoration = TextDecoration.LineThrough),
                    start,
                    end
                )
                is ForegroundColorSpan -> {
                    val color = Color(span.foregroundColor)
                    builder.addStyle(SpanStyle(color = color), start, end)
                }
                is BackgroundColorSpan -> {
                    val color = Color(span.backgroundColor).copy(alpha = 0.25f)
                    builder.addStyle(SpanStyle(background = color), start, end)
                }
                is TypefaceSpan -> {
                    if (span.family?.equals("monospace", ignoreCase = true) == true) {
                        builder.addStyle(SpanStyle(fontFamily = FontFamily.Monospace), start, end)
                    }
                }
                is RelativeSizeSpan -> {
                    builder.addStyle(SpanStyle(fontSize = (14 * span.sizeChange).sp), start, end)
                }
                is AbsoluteSizeSpan -> {
                    builder.addStyle(SpanStyle(fontSize = span.size.sp), start, end)
                }
                is URLSpan -> {
                    val url = span.url
                    if (!url.isNullOrBlank()) {
                        try {
                            builder.addLink(LinkAnnotation.Url(url), start, end)
                            builder.addStyle(
                                SpanStyle(
                                    color = linkColor,
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                start,
                                end
                            )
                        } catch (_: Exception) {
                            builder.addStyle(
                                SpanStyle(
                                    color = linkColor,
                                    textDecoration = TextDecoration.Underline
                                ),
                                start,
                                end
                            )
                        }
                    }
                }
            }
        }

        builder.toAnnotatedString()
    }
}

