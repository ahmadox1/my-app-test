package ai.screentalk.screen.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import ai.screentalk.common.Logger
import ai.screentalk.screen.ScreenContextBuilder

class ScreenReaderService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val root = rootInActiveWindow ?: return
        val lines = mutableListOf<String>()
        collectText(root, lines)
        root.recycle()
        val focusedNode = event?.source?.let { node ->
            val focusedText = node.text?.toString().orEmpty()
            node.recycle()
            focusedText
        } ?: ""
        val summary = lines.joinToString(separator = " • ") { it.trim() }
        ScreenContextBuilder.updateAccessibility(summary, focusedNode)
        ScreenContextBuilder.updateApp(event?.packageName?.toString(), event?.className?.toString())
    }

    override fun onInterrupt() {
        Logger.w("Accessibility service interrupted")
    }

    private fun collectText(node: AccessibilityNodeInfo?, output: MutableList<String>) {
        if (node == null) return
        val text = node.text?.toString().orEmpty()
        val contentDesc = node.contentDescription?.toString().orEmpty()
        if (text.isNotBlank()) output += text
        if (contentDesc.isNotBlank() && contentDesc != text) output += contentDesc
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectText(child, output)
            child.recycle()
        }
    }
}
