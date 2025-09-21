package ai.screentalk.screen.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import ai.screentalk.common.Logger
import ai.screentalk.screen.ScreenContextBuilder

class ScreenReaderService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val packageName = event.packageName?.toString()
        val className = event.className?.toString()
        if (!packageName.isNullOrEmpty()) {
            ScreenContextBuilder.updateApp(packageName, className)
        }

        val source = event.source
        val root = rootInActiveWindow ?: source ?: return
        val textFragments = mutableListOf<String>()
        collectNodeText(root, textFragments)
        val focused = findFocusedText(root)
        val mergedText = textFragments.joinToString(separator = " \u2022 ")
        ScreenContextBuilder.updateAccessibility(mergedText, focused, packageName)
        if (root !== source) {
            root.recycle()
        }
        source?.recycle()
    }

    override fun onInterrupt() {
        Logger.d("Accessibility service interrupted")
    }

    private fun collectNodeText(node: AccessibilityNodeInfo?, accumulator: MutableList<String>) {
        node ?: return
        val label = buildString {
            node.text?.toString()?.takeIf { it.isNotBlank() }?.let { append(it) }
            node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let {
                if (isNotEmpty()) append(' ')
                append(it)
            }
        }.trim()
        if (label.isNotEmpty()) {
            accumulator += label
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            collectNodeText(child, accumulator)
            child?.recycle()
        }
    }

    private fun findFocusedText(node: AccessibilityNodeInfo?): String? {
        node ?: return null
        val focused = node.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?: node.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)
        val text = focused?.text?.toString()?.takeIf { it.isNotBlank() }
            ?: focused?.contentDescription?.toString()?.takeIf { it.isNotBlank() }
        focused?.recycle()
        return text
    }
}
