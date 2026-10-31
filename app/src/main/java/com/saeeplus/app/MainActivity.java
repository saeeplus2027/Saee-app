```java
package com.saeeplus.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String HOME_URL =
            "https://saeeplus2027.github.io/Saee-plus2027/#home";

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance()
                .setAcceptThirdPartyCookies(webView, false);

        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request) {

                return handleUrl(request.getUrl().toString());
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view, String url) {

                return handleUrl(url);
            }

            private boolean handleUrl(String url) {

                if (url == null) {
                    return false;
                }

                Uri uri = Uri.parse(url);
                String scheme = uri.getScheme();

                // إبقاء موقع سعي بلس داخل التطبيق.
                if ("https".equalsIgnoreCase(scheme)
                        || "http".equalsIgnoreCase(scheme)) {

                    String host = uri.getHost();

                    if (host != null &&
                            (host.equalsIgnoreCase(
                                    "saeeplus2027.github.io")
                            || host.toLowerCase().endsWith(".github.io"))) {
                        return false;
                    }

                    // فتح روابط Google Maps خارج WebView.
                    if (host != null &&
                            (host.equalsIgnoreCase("google.com")
                            || host.equalsIgnoreCase("www.google.com")
                            || host.equalsIgnoreCase("maps.google.com"))
                            && uri.getPath() != null
                            && uri.getPath().startsWith("/maps")) {

                        openMaps(uri);
                        return true;
                    }

                    return false;
                }

                // معالجة روابط Android intent://.
                if ("intent".equalsIgnoreCase(scheme)) {
                    openIntentUrl(url);
                    return true;
                }

                // معالجة روابط الخرائط المباشرة.
                if ("geo".equalsIgnoreCase(scheme)
                        || "google.navigation".equalsIgnoreCase(scheme)) {

                    openExternalApp(uri);
                    return true;
                }

                // فتح الروابط الخارجية مثل الهاتف والبريد.
                if (scheme != null
                        && !"about".equalsIgnoreCase(scheme)
                        && !"javascript".equalsIgnoreCase(scheme)) {

                    openExternalApp(uri);
                    return true;
                }

                return false;
            }

            private void openMaps(Uri uri) {

                try {
                    Intent intent = new Intent(
                            Intent.ACTION_VIEW, uri);

                    intent.setPackage(
                            "com.google.android.apps.maps");

                    startActivity(intent);

                } catch (ActivityNotFoundException e) {

                    try {
                        Intent browserIntent = new Intent(
                                Intent.ACTION_VIEW, uri);

                        startActivity(browserIntent);

                    } catch (ActivityNotFoundException ex) {
                        showMessage("تعذّر فتح الخريطة.");
                    }
                }
            }

            private void openIntentUrl(String url) {

                try {
                    Intent intent = Intent.parseUri(
                            url, Intent.URI_INTENT_SCHEME);

                    try {
                        startActivity(intent);

                    } catch (ActivityNotFoundException e) {

                        String fallback =
                                intent.getStringExtra(
                                        "browser_fallback_url");

                        if (fallback != null
                                && !fallback.isEmpty()) {

                            startActivity(new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(fallback)));

                        } else {

                            Uri data = intent.getData();

                            if (data != null
                                    && ("https".equalsIgnoreCase(
                                            data.getScheme())
                                    || "http".equalsIgnoreCase(
                                            data.getScheme()))) {

                                openMaps(data);

                            } else {
                                showMessage(
                                        "تعذّر فتح الخرائط. تأكد من تثبيت Google Maps.");
                            }
                        }
                    }

                } catch (Exception e) {
                    showMessage("تعذّر فتح رابط الخريطة.");
                }
            }

            private void openExternalApp(Uri uri) {

                try {
                    Intent intent = new Intent(
                            Intent.ACTION_VIEW, uri);

                    startActivity(intent);

                } catch (ActivityNotFoundException e) {
                    showMessage(
                            "لا يوجد تطبيق مناسب لفتح هذا الرابط.");
                }
            }

            private void showMessage(String message) {
                Toast.makeText(
                        MainActivity.this,
                        message,
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        setContentView(webView);

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(HOME_URL);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) {
            webView.saveState(outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }

        super.onDestroy();
    }
}
```

public class MainActivity extends Activity {
    private static final String HOME_URL = "https://saeeplus2027.github.io/Saee-plus2027/#home";
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(0);
        webView = new WebView(this);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setMediaPlaybackRequiresUserGesture(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String host = request.getUrl().getHost();
                if (host != null && (host.equals("saeeplus2027.github.io") || host.endsWith(".github.io"))) {
                    return false;
                }
                return false; // Keep normal website navigation available inside the app.
            }
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    Toast.makeText(MainActivity.this, "تعذّر تحميل سعي بلس. تحقق من اتصال الإنترنت وحاول مجددًا.", Toast.LENGTH_LONG).show();
                }
            }
        });
        setContentView(webView);
        if (savedInstanceState != null) webView.restoreState(savedInstanceState);
        else webView.loadUrl(HOME_URL);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) { webView.stopLoading(); webView.destroy(); }
        super.onDestroy();
    }
}
