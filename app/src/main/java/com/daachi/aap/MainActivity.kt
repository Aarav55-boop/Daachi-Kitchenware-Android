
package com.daachi.aap

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
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

    private val allowedExternalSchemes = setOf(
        "https",
        "http",
        "mailto",
        "tel"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupSystemBars()

        // Never expose WebView debugging in this app.
        WebView.setWebContentsDebuggingEnabled(false)

        webView = WebView(this).apply {
            setBackgroundColor(Color.WHITE)
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        // Android 11+ system-bar protection.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

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
        }

        setContentView(
            webView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            webView.requestApplyInsets()
        }

        configureWebView()

        // Only the packaged HTML entry point is loaded.
        webView.loadUrl("file:///android_asset/index.html")
    }

    private fun setupSystemBars() {

        window.clearFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        window.addFlags(
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        )

        window.statusBarColor = brandRed
        window.navigationBarColor = Color.BLACK

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            window.setDecorFitsSystemWindows(false)

            window.insetsController?.setSystemBarsAppearance(
                0,
                android.view.WindowInsetsController
                    .APPEARANCE_LIGHT_STATUS_BARS
            )

        } else {

            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = 0
        }
    }

    private fun configureWebView() {

        webView.settings.apply {

            // Required for the local Daachi HTML app.
            javaScriptEnabled = true
            domStorageEnabled = true

            cacheMode = WebSettings.LOAD_DEFAULT

            // Never allow HTTP content inside HTTPS pages.
            mixedContentMode =
                WebSettings.MIXED_CONTENT_NEVER_ALLOW

            // Prevent JavaScript from accessing local files.
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false

            // Keep access to packaged assets for index.html
            // and camel-white.png.
            allowFileAccess = true

            // Disable access to content-provider URLs.
            allowContentAccess = false

            // Disable unnecessary pop-up windows.
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = false

            // Reduce unnecessary WebView exposure.
            setGeolocationEnabled(false)
        }

        // Keep Android Safe Browsing enabled
        // on supported devices.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView.settings.safeBrowsingEnabled = true
        }

        webView.webChromeClient = WebChromeClient()

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {

                val uri = request?.url ?: return true
                val scheme = uri.scheme?.lowercase()

                // Block untrusted local and unsupported URLs.
                if (scheme !in allowedExternalSchemes) {
                    return true
                }

                // Only allow HTTPS website content
                // to load inside the Daachi iframe.
                if (!request.isForMainFrame) {

                    return scheme != "https"
                }

                // Top-level links open in an external app.
                return openExternalLink(uri)
            }
        }
    }

    private fun openExternalLink(uri: Uri): Boolean {

        val scheme = uri.scheme?.lowercase()

        if (scheme !in allowedExternalSchemes) {
            return true
        }

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
