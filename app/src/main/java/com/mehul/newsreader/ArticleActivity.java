package com.mehul.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ArticleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        WebView webView = (WebView) findViewById(R.id.webView);

        //Customise settings for webView and to view web content in app instead of default browser
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        //create intent so we can get the intent/request to to his page from the MainActivity
        Intent intent = getIntent();

        //load the HTML for the article content onto the webview from the MainActivity via intent
        webView.loadData(intent.getStringExtra("content"), "text-html", "UTF-8");
    }
}