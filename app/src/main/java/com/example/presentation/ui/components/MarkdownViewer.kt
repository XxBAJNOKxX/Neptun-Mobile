package com.example.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A native Jetpack Compose Markdown Viewer for changelogs and release notes.
 * Supports:
 * - H1, H2, H3, H4 headers with distinct typography
 * - Unordered bullet lists (*, -, +) and nested lists
 * - Ordered numeric lists (1., 2., etc.)
 * - Task list checkboxes (- [ ] / - [x])
 * - Multi-line code blocks and inline `code` spans
 * - Blockquotes with accent vertical bar
 * - Horizontal rules (---, ***)
 * - Bold (**text**), Italic (*text*), Strikethrough (~~text~~), and Links ([text](url))
 */
@Composable
fun MarkdownViewer(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    linkColor: Color = MaterialTheme.colorScheme.primary,
    baseFontSize: TextUnit = 13.sp,
    itemSpacing: Dp = 4.dp
) {
    if (markdown.isBlank()) return

    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    Spacer(modifier = Modifier.height(if (index == 0) 0.dp else 8.dp))
                    Text(
                        text = rememberInlineMarkdown(block.content, linkColor),
                        style = when (block.level) {
                            1 -> MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = (baseFontSize.value + 6).sp
                            )
                            2 -> MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = (baseFontSize.value + 4).sp
                            )
                            3 -> MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = (baseFontSize.value + 2).sp
                            )
                            else -> MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = (baseFontSize.value + 1).sp
                            )
                        },
                        color = when (block.level) {
                            1, 2 -> MaterialTheme.colorScheme.primary
                            else -> textColor
                        }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = (block.indentLevel * 12).dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 7.dp, end = 8.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = rememberInlineMarkdown(block.content, linkColor),
                            fontSize = baseFontSize,
                            lineHeight = (baseFontSize.value * 1.45).sp,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = (block.indentLevel * 12).dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            fontSize = baseFontSize,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(22.dp)
                        )
                        Text(
                            text = rememberInlineMarkdown(block.content, linkColor),
                            fontSize = baseFontSize,
                            lineHeight = (baseFontSize.value * 1.45).sp,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.TaskItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = (block.indentLevel * 12).dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (block.isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = null,
                            tint = if (block.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = rememberInlineMarkdown(block.content, linkColor),
                            fontSize = baseFontSize,
                            lineHeight = (baseFontSize.value * 1.45).sp,
                            color = if (block.isChecked) textColor.copy(alpha = 0.7f) else textColor,
                            textDecoration = if (block.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.CodeBlock -> {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = block.code,
                            fontFamily = FontFamily.Monospace,
                            fontSize = (baseFontSize.value - 1).sp,
                            lineHeight = (baseFontSize.value * 1.4).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                is MarkdownBlock.BlockQuote -> {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = rememberInlineMarkdown(block.content, linkColor),
                            fontSize = baseFontSize,
                            fontStyle = FontStyle.Italic,
                            lineHeight = (baseFontSize.value * 1.45).sp,
                            color = textColor.copy(alpha = 0.85f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }

                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = rememberInlineMarkdown(block.content, linkColor),
                        fontSize = baseFontSize,
                        lineHeight = (baseFontSize.value * 1.45).sp,
                        color = textColor,
                        modifier = Modifier.padding(vertical = (itemSpacing.value / 2).dp)
                    )
                }
            }
        }
    }
}

private sealed class MarkdownBlock {
    data class Header(val level: Int, val content: String) : MarkdownBlock()
    data class Paragraph(val content: String) : MarkdownBlock()
    data class BulletItem(val indentLevel: Int, val content: String) : MarkdownBlock()
    data class NumberedItem(val indentLevel: Int, val number: String, val content: String) : MarkdownBlock()
    data class TaskItem(val indentLevel: Int, val isChecked: Boolean, val content: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class BlockQuote(val content: String) : MarkdownBlock()
    object Divider : MarkdownBlock()
}

private fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val lines = markdown.lines()
    val blocks = mutableListOf<MarkdownBlock>()

    var inCodeBlock = false
    val currentCodeLines = mutableListOf<String>()
    var codeLanguage = ""

    var i = 0
    while (i < lines.size) {
        val rawLine = lines[i]
        val trimmed = rawLine.trim()

        // Code block toggle (```)
        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                blocks.add(MarkdownBlock.CodeBlock(codeLanguage, currentCodeLines.joinToString("\n")))
                currentCodeLines.clear()
                inCodeBlock = false
            } else {
                inCodeBlock = true
                codeLanguage = trimmed.removePrefix("```").trim()
            }
            i++
            continue
        }

        if (inCodeBlock) {
            currentCodeLines.add(rawLine)
            i++
            continue
        }

        // Empty line
        if (trimmed.isEmpty()) {
            i++
            continue
        }

        // Horizontal rule
        if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
            blocks.add(MarkdownBlock.Divider)
            i++
            continue
        }

        // Headers
        if (trimmed.startsWith("#")) {
            val hashCount = trimmed.takeWhile { it == '#' }.length
            if (hashCount in 1..6 && trimmed.getOrNull(hashCount) == ' ') {
                val headerContent = trimmed.substring(hashCount).trim()
                blocks.add(MarkdownBlock.Header(hashCount, headerContent))
                i++
                continue
            }
        }

        // Blockquotes
        if (trimmed.startsWith(">")) {
            val quoteContent = trimmed.removePrefix(">").trim()
            blocks.add(MarkdownBlock.BlockQuote(quoteContent))
            i++
            continue
        }

        // Indent level
        val leadingSpaces = rawLine.takeWhile { it == ' ' }.length
        val indentLevel = (leadingSpaces / 2).coerceAtMost(3)

        // Task items (- [ ] / - [x])
        val taskRegex = Regex("""^[-*+]\s+\[([ xX])\]\s+(.*)$""")
        val taskMatch = taskRegex.find(trimmed)
        if (taskMatch != null) {
            val isChecked = taskMatch.groupValues[1].equals("x", ignoreCase = true)
            val content = taskMatch.groupValues[2]
            blocks.add(MarkdownBlock.TaskItem(indentLevel, isChecked, content))
            i++
            continue
        }

        // Bullet lists (*, -, +)
        if (trimmed.startsWith("* ") || trimmed.startsWith("- ") || trimmed.startsWith("+ ")) {
            val content = trimmed.substring(2).trim()
            blocks.add(MarkdownBlock.BulletItem(indentLevel, content))
            i++
            continue
        }

        // Numbered lists (1. , 2. )
        val numberedRegex = Regex("""^(\d+)\.\s+(.*)$""")
        val numMatch = numberedRegex.find(trimmed)
        if (numMatch != null) {
            val number = numMatch.groupValues[1]
            val content = numMatch.groupValues[2]
            blocks.add(MarkdownBlock.NumberedItem(indentLevel, number, content))
            i++
            continue
        }

        // Regular paragraph
        blocks.add(MarkdownBlock.Paragraph(trimmed))
        i++
    }

    if (inCodeBlock && currentCodeLines.isNotEmpty()) {
        blocks.add(MarkdownBlock.CodeBlock(codeLanguage, currentCodeLines.joinToString("\n")))
    }

    return blocks
}

/**
 * Parses inline markdown: **bold**, *italic*, `code`, ~~strike~~, and [links](url) into AnnotatedString.
 */
@Composable
private fun rememberInlineMarkdown(
    text: String,
    linkColor: Color
): AnnotatedString {
    return remember(text, linkColor) {
        val builder = AnnotatedString.Builder()
        var index = 0
        val length = text.length

        while (index < length) {
            // Check for Links: [title](url)
            if (text[index] == '[') {
                val closeBracket = text.indexOf(']', index)
                if (closeBracket != -1 && closeBracket + 1 < length && text[closeBracket + 1] == '(') {
                    val closeParen = text.indexOf(')', closeBracket + 1)
                    if (closeParen != -1) {
                        val linkTitle = text.substring(index + 1, closeBracket)
                        val url = text.substring(closeBracket + 2, closeParen)
                        val startPos = builder.length
                        builder.append(linkTitle)
                        try {
                            builder.addLink(LinkAnnotation.Url(url), startPos, builder.length)
                            builder.addStyle(
                                SpanStyle(
                                    color = linkColor,
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                startPos,
                                builder.length
                            )
                        } catch (_: Exception) {
                            builder.addStyle(
                                SpanStyle(
                                    color = linkColor,
                                    textDecoration = TextDecoration.Underline
                                ),
                                startPos,
                                builder.length
                            )
                        }
                        index = closeParen + 1
                        continue
                    }
                }
            }

            // Inline code: `code`
            if (text[index] == '`') {
                val nextBacktick = text.indexOf('`', index + 1)
                if (nextBacktick != -1) {
                    val codeContent = text.substring(index + 1, nextBacktick)
                    val startPos = builder.length
                    builder.append(codeContent)
                    builder.addStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            background = Color(0x22888888)
                        ),
                        startPos,
                        builder.length
                    )
                    index = nextBacktick + 1
                    continue
                }
            }

            // Bold & Italic: ***text***
            if (index + 2 < length && text.substring(index, index + 3) == "***") {
                val nextTriple = text.indexOf("***", index + 3)
                if (nextTriple != -1) {
                    val boldItalicContent = text.substring(index + 3, nextTriple)
                    val startPos = builder.length
                    builder.append(boldItalicContent)
                    builder.addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
                        startPos,
                        builder.length
                    )
                    index = nextTriple + 3
                    continue
                }
            }

            // Bold: **text** or __text__
            if ((index + 1 < length && text.substring(index, index + 2) == "**") ||
                (index + 1 < length && text.substring(index, index + 2) == "__")
            ) {
                val delimiter = text.substring(index, index + 2)
                val nextDouble = text.indexOf(delimiter, index + 2)
                if (nextDouble != -1) {
                    val boldContent = text.substring(index + 2, nextDouble)
                    val startPos = builder.length
                    builder.append(boldContent)
                    builder.addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        startPos,
                        builder.length
                    )
                    index = nextDouble + 2
                    continue
                }
            }

            // Strikethrough: ~~text~~
            if (index + 1 < length && text.substring(index, index + 2) == "~~") {
                val nextTildes = text.indexOf("~~", index + 2)
                if (nextTildes != -1) {
                    val strikeContent = text.substring(index + 2, nextTildes)
                    val startPos = builder.length
                    builder.append(strikeContent)
                    builder.addStyle(
                        SpanStyle(textDecoration = TextDecoration.LineThrough),
                        startPos,
                        builder.length
                    )
                    index = nextTildes + 2
                    continue
                }
            }

            // Italic: *text* or _text_
            if (text[index] == '*' || (text[index] == '_' && (index == 0 || text[index - 1] == ' '))) {
                val delimiter = text[index]
                val nextSingle = text.indexOf(delimiter, index + 1)
                if (nextSingle != -1 && nextSingle > index + 1) {
                    val italicContent = text.substring(index + 1, nextSingle)
                    val startPos = builder.length
                    builder.append(italicContent)
                    builder.addStyle(
                        SpanStyle(fontStyle = FontStyle.Italic),
                        startPos,
                        builder.length
                    )
                    index = nextSingle + 1
                    continue
                }
            }

            // Default character
            builder.append(text[index])
            index++
        }

        builder.toAnnotatedString()
    }
}
