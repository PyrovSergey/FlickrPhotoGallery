package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class PhotoPageFragment extends VisibleFragment {
  private static final String ARG_URI = "photo_page_url";

  private Uri uri;
  private WebView webView;
  private ProgressBar progressBar;

  public static PhotoPageFragment newInstanced(Uri uri) {
    Bundle args = new Bundle();
    args.putParcelable(ARG_URI, uri);
    PhotoPageFragment fragment = new PhotoPageFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    uri = getArguments().getParcelable(ARG_URI);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_photo_page, container, false);
    progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
    progressBar.setMax(100);
    webView = (WebView) view.findViewById(R.id.web_view);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.setWebChromeClient(
        new WebChromeClient() {
          public void onProgressChanged(WebView webView, int newProgress) {
            if (newProgress == 100) {
              progressBar.setVisibility(View.GONE);
            } else {
              progressBar.setVisibility(View.VISIBLE);
              progressBar.setProgress(newProgress);
            }
          }

          public void onReceivedTitle(WebView webView, String title) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.getSupportActionBar().setSubtitle(title);
          }
        });
    webView.setWebViewClient(new WebViewClient());
    webView.loadUrl(uri.toString());
    return view;
  }

}
