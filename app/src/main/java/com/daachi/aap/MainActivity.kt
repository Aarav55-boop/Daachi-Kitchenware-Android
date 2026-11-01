
package com.daachi.aap

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make sure app is not in fullscreen mode.
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Red Android status bar.
        window.addFlags(
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        )
        window.statusBarColor = Color.rgb(189, 9, 18)
        window.navigationBarColor = Color.BLACK

        // Keep status bar icons light.
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility and
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()

        webView = WebView(this)

        webView.setBackgroundColor(Color.WHITE)

        // Keep the WebView below Android system bars.
        // Works with edge-to-edge layouts too.
        webView.setOnApplyWindowInsetsListener { view, insets ->

            val statusBars = insets.getInsets(
                android.view.WindowInsets.Type.statusBars()
            )

            val navigationBars = insets.getInsets(
                android.view.WindowInsets.Type.navigationBars()
            )

            view.setPadding(
                0,
                statusBars.top,
                0,
                navigationBars.bottom
            )

            insets
        }

        setContentView(
            webView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Enable JavaScript for HTML design and side menu.
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT

        // Keep HTTPS content secure.
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

                // Allow website content inside iframe.
                // Open external links in phone browser.
                if (scheme == "http" || scheme == "https") {

                    if (!request.isForMainFrame) {
                        return false
                    }

                    return try {
                        startActivity(
                            Intent(Intent.ACTION_VIEW, uri)
                        )
                        true
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            this@MainActivity,
                            "Browser not available",
                            Toast.LENGTH_SHORT
                        ).show()
                        true
                    } catch (_: Exception) {
                        true
                    }
                }

                if (scheme == "file") {
                    return false
                }

                return try {
                    startActivity(
                        Intent(Intent.ACTION_VIEW, uri)
                    )
                    true
                } catch (_: Exception) {
                    true
                }
            }
        }

        // Load the updated HTML from assets folder.
        webView.loadUrl("file:///android_asset/index.html")
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
        webView.destroy()
        super.onDestroy()
    }
}
