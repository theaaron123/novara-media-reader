package com.aaronbaker.novaramediareader;

import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import static com.aaronbaker.novaramediareader.ArticleListFragment.NOVARAMEDIA_COM_ABOUT;

public class ArticleFullscreenFragment extends Fragment {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private static final String LARGE_TEXT_KEY = "Large Text";
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

            Activity activity = getActivity();
            if (activity != null
                    && activity.getWindow() != null) {
                activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            }
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private View mContentView;
    private WebView mArticleBody;
    private TextView mTitleView;
    private ImageView mImageView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private boolean mLargeText;
    private SharedPreferences mPrefs;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_article_fullscreen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVisible = true;

        mContentView = view.findViewById(R.id.fullscreen_content);
        mArticleBody = (WebView) view.findViewById(R.id.fullscreen_content);
        mTitleView = (TextView) view.findViewById(R.id.title_content);
        mImageView = (ImageView) view.findViewById(R.id.article_image);

        mArticleBody.getSettings().setJavaScriptEnabled(true);
        mArticleBody.setWebChromeClient(new FullscreenVideoChrome());

        Configuration configuration = getResources().getConfiguration();
        if ((configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                && WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(mArticleBody.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
        }

        if (SDK_INT >= 32) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                try {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(mArticleBody.getSettings(), true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {

            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {

                int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    WebSettingsCompat.setForceDark(mArticleBody.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
                }
            }
        }


        mPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        mLargeText = mPrefs.getBoolean(LARGE_TEXT_KEY, false);

        populateUI(this.getArguments());
        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
    }

    public void populateUI(Bundle b) {
        if (b != null) {
            Glide.with(this).load(b.getString(ArticleListFragment.IMAGE))
                    .placeholder(R.drawable.ic_nm_logo)
                    .dontAnimate()
                    .into(mImageView);
            mTitleView.setText(b.getString(ArticleListFragment.TITLE));
            if (b.getString(ArticleListFragment.BODY) == null) {
                retrieveArticleFromWeb(b.getString(ArticleListFragment.PERMALINK));
            } else {
                retrieveArticleFromBody(b.getString(ArticleListFragment.BODY), b.getString(ArticleListFragment.PERMALINK));
            }
        }
        if (mLargeText) {
            mArticleBody.getSettings().setTextZoom(145);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putBoolean(LARGE_TEXT_KEY, mLargeText);
        ed.apply();
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            // Clear the systemUiVisibility flag
            getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        }
        show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContentView = null;
    }

    public void retrieveArticleFromWeb(final String permalink) {
        RequestQueue queue = ApplicationController.getInstance().getRequestQueue();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, permalink,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        Log.d("On response", "article id: " + permalink);
                        Document document = Jsoup.parse(response);
                        Element mainBody = document.body();
                        if (mainBody.getElementById("single-articles-copy") != null) {
                            mArticleBody.loadDataWithBaseURL(permalink, rewriteHTMLHeader(mainBody.getElementById("single-articles-copy").html()), "text/html", "UTF-8", permalink);
                        } else if (mainBody.getElementById("post") != null) {
                            mArticleBody.loadDataWithBaseURL(permalink, rewriteHTMLHeader(parseVideo(document)), "text/html", "UTF-8", permalink);
                        } else {
                            mArticleBody.loadUrl(permalink);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity().getApplicationContext(), "Cannot refresh feed", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
    }

    private String parseVideo(Document document) {
        if (document.select("p").size() >= 1) {
            return document.select("p").first().toString() + document.select("p").get(1).toString() + document.select("iframe").first().toString();
        }
        return null;
    }

    public void retrieveArticleFromBody(final String body, String permalink) {
        Document document = Jsoup.parse(body);
        Element mainBody = document.body();
        if (mainBody.getElementById("single-articles-copy") != null) {
            mArticleBody.loadDataWithBaseURL(permalink, mainBody.getElementById("single-articles-copy").html(), "text/html", "UTF-8", permalink);
        } else {
            mArticleBody.loadDataWithBaseURL(permalink, body, "text/html", "UTF-8", permalink);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (menu.size() == 0) {
            getActivity().getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        }
        MenuItem item = menu.findItem(R.id.action_font);
        item.setChecked(mLargeText);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_share) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Novara Media link:");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, mArticleBody.getUrl());
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
            return true;
        } else if (itemId == R.id.action_font) {
            if (mArticleBody != null) {
                WebSettings mArticleBodySettings = mArticleBody.getSettings();
                if (mArticleBodySettings.getTextZoom() == 100) {
                    mArticleBodySettings.setTextZoom(145);
                    item.setChecked(true);
                    mLargeText = true;
                } else {
                    mArticleBodySettings.setTextZoom(100);
                    item.setChecked(false);
                    mLargeText = false;
                }
                return true;
            }

            retrieveArticleFromWeb(NOVARAMEDIA_COM_ABOUT);
            return true;
        } else if (itemId == R.id.action_contact) {
            retrieveArticleFromWeb(NOVARAMEDIA_COM_ABOUT);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Nullable
    private ActionBar getSupportActionBar() {
        ActionBar actionBar = null;
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            actionBar = activity.getSupportActionBar();
        }
        return actionBar;
    }

    private String rewriteHTMLHeader(String body) {
        String head = "<head><style>img{max-width: 100%; width:auto; height: auto;}  iframe{\n" +
                "            max-width: 100%;\n" +
                "            height: auto;} \n" +
                "</style></head>";
        return "<html>" + head + "<body>" + body + "</body></html>";
    }

    private class FullscreenVideoChrome extends WebChromeClient {

        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        FullscreenVideoChrome() {
        }

        public Bitmap getDefaultVideoPoster() {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView() {
            ((FrameLayout) getActivity().getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getActivity().getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            getActivity().setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
        }

        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback) {
            if (this.mCustomView != null) {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getActivity().getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getActivity().getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout) getActivity().getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getActivity().getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }
}
