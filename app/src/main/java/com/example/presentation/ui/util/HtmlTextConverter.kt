package com.example.presentation.ui.util

import android.graphics.Typeface
import android.text.Html
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

/**
 * Preprocesses raw HTML from Neptun messages into clean HTML.
 */
fun preprocessNeptunHtml(rawHtml: String): String {
    if (rawHtml.isBlank()) return ""

    var html = rawHtml.trim()

    // Handle double-encoded HTML entities if present (e.g., &lt;p&gt; -> <p>)
    if ((html.contains("&lt;p") || html.contains("&lt;div") || html.contains("&lt;br")) && !html.contains("<p") && !html.contains("<div")) {
        try {
            html = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
        } catch (_: Exception) {}
    }

    // Convert table tags into structured line breaks
    html = html.replace(Regex("(?i)<table[^>]*>"), "<br>")
        .replace(Regex("(?i)</table>"), "<br>")
        .replace(Regex("(?i)<tr[^>]*>"), "<br>")
        .replace(Regex("(?i)</tr>"), "")
        .replace(Regex("(?i)<td[^>]*>"), "  ")
        .replace(Regex("(?i)</td>"), "  ")

    // Clean up excessive empty paragraphs or line breaks
    html = html.replace(Regex("(?i)(<br\\s*/?>\\s*){3,}"), "<br><br>")
        .replace(Regex("(?i)(<p>&nbsp;</p>\\s*)+"), "<p></p>")

    return html
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
                    // Avoid invisible text if color matches background
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

        // Trim excessive trailing newlines
        builder.toAnnotatedString()
    }
}
