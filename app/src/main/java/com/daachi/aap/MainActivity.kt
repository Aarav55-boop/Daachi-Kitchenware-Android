
package com.daachi.aap

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
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

        webView = WebView(this)
        setContentView(webView)

        // Enable JavaScript for HTML design and side menu
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT

        // Allow website content inside the app
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

                // Allow website inside iframe.
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

        // Load Daachi HTML design from assets folder
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
