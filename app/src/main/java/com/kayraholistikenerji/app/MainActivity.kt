package com.kayraholistikenerji.app

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast

/**
 * The whole app: show the branded cover for a moment, then hand the site address to
 * the phone's browser and finish.
 *
 * It deliberately has no WebView, no third-party libraries and no permissions, so there
 * is nothing device specific that can break during install or launch.
 */
class MainActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())
    private val openSiteTask = Runnable { openSite() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // A plain static layout: the logo is centred and scales with the screen width.
        setContentView(R.layout.activity_cover)
        handler.postDelayed(openSiteTask, COVER_DURATION_MS)
    }

    override fun onDestroy() {
        handler.removeCallbacks(openSiteTask)
        super.onDestroy()
    }

    private fun openSite() {
        val uri: Uri = Uri.parse(SITE_URL)

        // Primary path: whatever the user has set as the default browser.
        val viewIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (tryStart(viewIntent)) {
            finish()
            return
        }

        // Fallback: explicitly ask known browsers in case the generic intent is not resolved.
        for (packageName in KNOWN_BROWSERS) {
            val explicitIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(packageName)
                addCategory(Intent.CATEGORY_BROWSABLE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (tryStart(explicitIntent)) {
                finish()
                return
            }
        }

        // Last resort: let the system suggest an app store to install a browser.
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URL))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (tryStart(marketIntent)) {
            finish()
            return
        }

        Toast.makeText(
            applicationContext,
            R.string.no_browser,
            Toast.LENGTH_LONG
        ).show()
        finish()
    }

    private fun tryStart(intent: Intent): Boolean {
        return try {
            startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: SecurityException) {
            false
        }
    }

    private companion object {
        const val SITE_URL = "https://kayraholistikenerji.com"
        const val MARKET_URL = "market://search?q=browser"

        // Long enough to read the cover, short enough not to feel slow.
        const val COVER_DURATION_MS = 1100L

        val KNOWN_BROWSERS = listOf(
            "com.android.chrome",
            "com.google.android.apps.chrome",
            "org.mozilla.firefox",
            "com.microsoft.emmx",
            "com.opera.browser",
            "com.opera.mini.native",
            "com.brave.browser",
            "com.sec.android.app.sbrowser",
            "com.mi.globalbrowser",
            "com.UCMobile.intl"
        )
    }
}
