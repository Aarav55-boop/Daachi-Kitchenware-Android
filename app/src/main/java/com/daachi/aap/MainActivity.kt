
package com.daachi.aap

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var webView: WebView

    private val brandRed = Color.rgb(225, 7, 22)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Do not use fullscreen or immersive mode.
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility and
            (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            ).inv()

        window.addFlags(
            WindowManager.LayoutParams
                .FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        )

        // Daachi brand colors.
        window.statusBarColor = brandRed
        window.navigationBarColor = Color.BLACK

        // Android 15+ may draw behind system bars.
        // Use window insets instead of fixed top margins.
        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false)
        }

        webView = WebView(this)

        webView.setBackgroundColor(Color.WHITE)
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false

        // Handle system bars without guessing their height.
        if (Build.VERSION.SDK_INT >= 30) {

            webView.setOnApplyWindowInsetsListener { view, insets ->

                val bars = insets.getInsets(
                    WindowInsets.Type.systemBars() or
                    WindowInsets.Type.displayCutout()
                )

                view.setPadding(
                    bars.left,
                    bars.top,
                    bars.right,
                    bars.bottom
                )

                insets
            }

        } else {

            // Older Android: let the system position
            // the content below its bars.
            webView.fitsSystemWindows = false
        }

        setContentView(
            webView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        if (Build.VERSION.SDK_INT >= 30) {
            webView.requestApplyInsets()
        }

        // WebView settings.
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT

        webView.settings.mixedContentMode =
            WebSettings.MIXED_CONTENT_NEVER_ALLOW

        webView.settings.setSupportMultipleWindows(false)
        webView.settings.javaScriptCanOpenWindowsAutomatically = false

        webView.webChromeClient = WebChromeClient()

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {

                val uri = request?.url ?: return false
                val scheme = uri.scheme?.lowercase()

                // Let the website load inside its iframe.
                if (scheme == "http" || scheme == "https") {

                    if (!request.isForMainFrame) {
                        return false
                    }

                    // Open top-level external links
                    // in the phone browser.
                    return openExternalLink(uri)
                }

                // Allow the app's local HTML.
                if (scheme == "file") {
                    return false
                }

                // Handle phone, email and other links.
                return openExternalLink(uri)
            }
        }

        // Load the actual Android assets HTML file.
        webView.loadUrl(
            "file:///android_asset/index.html"
        )
    }

    private fun openExternalLink(
        uri: android.net.Uri
    ): Boolean {

        return try {

            val intent = Intent(
                Intent.ACTION_VIEW,
                uri
            )

            startActivity(intent)
            true

        } catch (_: ActivityNotFoundException) {

            Toast.makeText(
                this,
                "No app available to open this link",
                Toast.LENGTH_SHORT
            ).show()

            true

        } catch (_: Exception) {

            Toast.makeText(
                this,
                "Unable to open link",
                Toast.LENGTH_SHORT
            ).show()

            true
        }
    }

    @Deprecated("Use Android back navigation")
    override fun onBackPressed() {

        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {

        webView.stopLoading()

        (webView.parent as? ViewGroup)
            ?.removeView(webView)

        webView.destroy()

        super.onDestroy()
    }
}
