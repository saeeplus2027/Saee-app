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
    private static final String HOME_URL = "https://saeeplus2027.github.io/Saee-plus2027/#home";
    private static final String MAPS_PACKAGE = "com.google.android.apps.maps";
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
                return handleNavigation(request.getUrl().toString());
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleNavigation(url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        android.webkit.WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    Toast.makeText(MainActivity.this,
                            "تعذّر تحميل سعي بلس. تحقق من اتصال الإنترنت وحاول مجددًا.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        setContentView(webView);
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(HOME_URL);
        }
    }

    private boolean handleNavigation(String url) {
        if (url == null) return false;

        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        if (scheme == null) return false;

        if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
            String host = uri.getHost();
            if (host != null && (host.equalsIgnoreCase("saeeplus2027.github.io")
                    || host.toLowerCase().endsWith(".github.io"))) {
                return false;
            }
            if (isGoogleMapsUrl(uri)) {
                openMaps(uri);
                return true;
            }
            return false;
        }

        if ("intent".equalsIgnoreCase(scheme)) {
            handleIntentUrl(url);
            return true;
        }

        if ("geo".equalsIgnoreCase(scheme) || "google.navigation".equalsIgnoreCase(scheme)) {
            openExternal(uri);
            return true;
        }

        if (!"about".equalsIgnoreCase(scheme) && !"javascript".equalsIgnoreCase(scheme)) {
            openExternal(uri);
            return true;
        }
        return false;
    }

    private boolean isGoogleMapsUrl(Uri uri) {
        String host = uri.getHost();
        String path = uri.getPath();
        if (host == null) return false;
        String normalizedHost = host.toLowerCase(java.util.Locale.ROOT);
        // Google Maps share links are sometimes shortened to maps.app.goo.gl.
        if (normalizedHost.equals("maps.app.goo.gl") || normalizedHost.equals("goo.gl")) {
            return true;
        }
        return path != null
                && (normalizedHost.equals("google.com")
                    || normalizedHost.equals("www.google.com")
                    || normalizedHost.equals("maps.google.com")
                    || normalizedHost.equals("maps.google.co.uk")
                    || normalizedHost.startsWith("maps.google."))
                && (path.startsWith("/maps") || path.startsWith("/dir"));
    }

    private void openMaps(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage(MAPS_PACKAGE);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            openExternal(uri);
        }
    }

    private void handleIntentUrl(String url) {
        try {
            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            try {
                startActivity(intent);
                return;
            } catch (ActivityNotFoundException ignored) {
                // Try the browser fallback or the HTTPS URL below.
            }

            String fallback = intent.getStringExtra("browser_fallback_url");
            if (fallback != null && !fallback.isEmpty()) {
                openExternal(Uri.parse(fallback));
                return;
            }

            Uri data = intent.getData();
            if (data != null) {
                String dataScheme = data.getScheme();
                if ("https".equalsIgnoreCase(dataScheme) || "http".equalsIgnoreCase(dataScheme)) {
                    if (isGoogleMapsUrl(data)) openMaps(data);
                    else openExternal(data);
                    return;
                }
            }
            showMessage("تعذّر فتح الخرائط. تأكد من تثبيت Google Maps.");
        } catch (Exception e) {
            showMessage("تعذّر فتح رابط الخريطة.");
        }
    }

    private void openExternal(Uri uri) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException e) {
            showMessage("لا يوجد تطبيق مناسب لفتح هذا الرابط.");
        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}
