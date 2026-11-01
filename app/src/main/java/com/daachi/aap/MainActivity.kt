
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

    // Official Daachi brand color
    private val brandRed = Color.rgb(225, 7, 22)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupSystemBars()

        webView = WebView(this).apply {
            setBackgroundColor(Color.WHITE)
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        // Keep the HTML content clear of the phone's
        // status bar, camera cutout and navigation bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            webView.setOnApplyWindowInsetsListener { view, insets ->

                val systemBars = insets.getInsets(
                    WindowInsets.Type.systemBars() or
                    WindowInsets.Type.displayCutout()
                )

                view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
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

        // This is the HTML file Android actually loads.
        webView.loadUrl("file:///android_asset/index.html")
    }

    private fun setupSystemBars() {

        // Remove fullscreen mode.
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        window.addFlags(
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        )

        // Set Daachi red status bar.
        window.statusBarColor = brandRed
        window.navigationBarColor = Color.BLACK

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            // Edge-to-edge is handled with real system insets.
            window.setDecorFitsSystemWindows(false)

            window.insetsController?.setSystemBarsAppearance(
                0,
                android.view.WindowInsetsController
                    .APPEARANCE_LIGHT_STATUS_BARS
            )

        } else {

            // Older Android: system handles bar positioning.
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = 0
        }
    }

    private fun configureWebView() {

        webView.settings.apply {

            javaScriptEnabled = true
            domStorageEnabled = true

            cacheMode = WebSettings.LOAD_DEFAULT

            mixedContentMode =
                WebSettings.MIXED_CONTENT_NEVER_ALLOW

            setSupportMultipleWindows(false)

            javaScriptCanOpenWindowsAutomatically = false
        }

        webView.webChromeClient = WebChromeClient()

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {

                val uri = request?.url ?: return false
                val scheme = uri.scheme?.lowercase()

                // Website pages inside the iframe.
                if (scheme == "http" || scheme == "https") {

                    if (!request.isForMainFrame) {
                        return false
                    }

                    // External top-level links open
                    // in the phone's browser.
                    return openExternalLink(uri)
                }

                // Keep local app HTML inside WebView.
                if (scheme == "file") {
                    return false
                }

                // Phone, email and other external links.
                return openExternalLink(uri)
            }
        }
    }

    private fun openExternalLink(uri: Uri): Boolean {

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
