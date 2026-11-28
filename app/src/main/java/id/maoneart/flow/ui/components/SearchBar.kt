package id.maoneart.flow.ui.components

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import id.maoneart.flow.ui.theme.FlowDarkSurface
import id.maoneart.flow.ui.theme.FlowElectricBlue
import id.maoneart.flow.ui.theme.FlowTextPrimary
import id.maoneart.flow.ui.theme.FlowTextSecondary
import kotlinx.coroutines.delay

/**
 * Native EditText tailored for Flow Search Bar:
 * - Immune to Compose BasicTextField / IME async StateFlow crashes on rapid backspace.
 * - Consumes excess Backspace / KEYCODE_DEL when already empty so it stays calmly on standby.
 */
private class SafeSearchEditText(context: Context) : EditText(context) {
    var isSyncingFromState: Boolean = false

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DEL && text.isNullOrEmpty()) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DEL && text.isNullOrEmpty()) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val baseIc = super.onCreateInputConnection(outAttrs) ?: return null
        return object : InputConnectionWrapper(baseIc, true) {
            override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
                if (text.isNullOrEmpty() && beforeLength > 0 && afterLength == 0) {
                    return true
                }
                return super.deleteSurroundingText(beforeLength, afterLength)
            }

            override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean {
                if (text.isNullOrEmpty() && beforeLength > 0 && afterLength == 0) {
                    return true
                }
                return super.deleteSurroundingTextInCodePoints(beforeLength, afterLength)
            }

            override fun sendKeyEvent(event: KeyEvent?): Boolean {
                if (event?.keyCode == KeyEvent.KEYCODE_DEL && text.isNullOrEmpty()) {
                    return true
                }
                return super.sendKeyEvent(event)
            }
        }
    }
}

@Composable
fun FlowSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false,
    onCloseClick: (() -> Unit)? = null,
    onSearchAction: () -> Unit = {}
) {
    var editTextRef by remember { mutableStateOf<SafeSearchEditText?>(null) }
    val latestOnQueryChange by rememberUpdatedState(onQueryChange)
    val latestOnSearchAction by rememberUpdatedState(onSearchAction)

    LaunchedEffect(autoFocus, editTextRef) {
        if (autoFocus) {
            delay(50)
            editTextRef?.let { et ->
                try {
                    et.requestFocus()
                    val imm = et.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
                } catch (_: Throwable) {}
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .height(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(FlowDarkSurface),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = FlowTextSecondary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        SafeSearchEditText(ctx).apply {
                            background = null
                            setPadding(0, 0, 0, 0)
                            gravity = Gravity.CENTER_VERTICAL
                            isSingleLine = true
                            maxLines = 1
                            hint = "Cari aplikasi…"
                            setTextColor(FlowTextPrimary.toArgb())
                            setHintTextColor(FlowTextSecondary.toArgb())
                            highlightColor = FlowElectricBlue.copy(alpha = 0.35f).toArgb()
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_FILTER
                            imeOptions = EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                try {
                                    textCursorDrawable?.setTint(FlowElectricBlue.toArgb())
                                } catch (_: Throwable) {}
                            }

                            if (query.isNotEmpty()) {
                                isSyncingFromState = true
                                setText(query)
                                setSelection(query.length)
                                isSyncingFromState = false
                            }

                            addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                                override fun afterTextChanged(s: Editable?) {
                                    if (!isSyncingFromState) {
                                        latestOnQueryChange(s?.toString() ?: "")
                                    }
                                }
                            })

                            setOnEditorActionListener { _, actionId, event ->
                                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                    actionId == EditorInfo.IME_ACTION_DONE ||
                                    (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
                                ) {
                                    latestOnSearchAction()
                                    true
                                } else {
                                    false
                                }
                            }

                            editTextRef = this
                        }
                    },
                    update = { et ->
                        editTextRef = et
                        val currentText = et.text?.toString() ?: ""
                        // Only sync externally when cleared to empty or when field is not actively focused
                        if (query != currentText && (query.isEmpty() || !et.isFocused)) {
                            et.isSyncingFromState = true
                            et.setText(query)
                            et.setSelection(query.length.coerceAtMost(et.text?.length ?: 0))
                            et.isSyncingFromState = false
                        }
                    }
                )
            }

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = {
                        editTextRef?.let { et ->
                            et.isSyncingFromState = true
                            et.setText("")
                            et.isSyncingFromState = false
                        }
                        onQueryChange("")
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Clear",
                        tint = FlowTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else if (onCloseClick != null) {
                IconButton(
                    onClick = {
                        editTextRef?.let { et ->
                            try {
                                et.clearFocus()
                                val imm = et.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                imm?.hideSoftInputFromWindow(et.windowToken, 0)
                            } catch (_: Throwable) {}
                        }
                        onCloseClick()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup",
                        tint = FlowTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
