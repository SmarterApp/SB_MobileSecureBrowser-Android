//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser;

import com.smarterapp.mobilebrowser.jscmds.JSNTVCmds;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.os.Handler;

/**
 * Custom @{link WebView} implementation that
 * implements the client and chrome interfaces
 * to provide better support for html5.
 */
@SuppressLint("SetJavaScriptEnabled")
public class AIRWebView extends WebView {

	private Context 							mContext;
	private AIRWebChromeClient					mWebChromeClient;
	private View								mCustomView;
	private FrameLayout							mCustomViewContainer;
	private WebChromeClient.CustomViewCallback 	mCustomViewCallback;
	
	private FrameLayout							mContentView;
	private FrameLayout							mBrowserFrameLayout;
	private FrameLayout							mLayout;
	private BrowserActivity						mBrowserActivity;
	
	private ActionMode.Callback mActionModeCallback;
	
    static final String LOGTAG = "HTML5WebView";
	    
	private void init(Context context) {
		mContext = context;		
		Activity a = (Activity) mContext;
		
		mLayout = new FrameLayout(context);
		
		mBrowserFrameLayout = (FrameLayout) LayoutInflater.from(a).inflate(R.layout.custom_screen, null);
		mContentView = (FrameLayout) mBrowserFrameLayout.findViewById(R.id.main_content);
		mCustomViewContainer = (FrameLayout) mBrowserFrameLayout.findViewById(R.id.fullscreen_custom_content);
		
		mLayout.addView(mBrowserFrameLayout, COVER_SCREEN_PARAMS);

		mWebChromeClient = new AIRWebChromeClient();
	    
		setWebChromeClient(mWebChromeClient);
	    setWebViewClient(new MyWebViewClient());
	       
	    // Configure the webview
	    WebSettings s = getSettings();
	    s.setBuiltInZoomControls(true);
	    s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
	    s.setUseWideViewPort(true);
	    s.setLoadWithOverviewMode(true);
	    s.setSavePassword(true);
	    s.setSaveFormData(true);
	    s.setJavaScriptEnabled(true);
	    
	    // enable navigator.geolocation 
	    s.setGeolocationEnabled(true);
	    s.setGeolocationDatabasePath(context.getFilesDir().getPath() + "/data/com.smarterapp.mobilebrowser/databases/");
	    
	    // enable Web Storage: localStorage, sessionStorage
	    s.setDomStorageEnabled(true);
	   
	    mContentView.addView(this);
	}

	public AIRWebView(Context context) {
		super(context);
		init(context);
	}

	public AIRWebView(Context context, BrowserActivity bActivity) {
		super(context);
		init(context);
		mBrowserActivity = bActivity;
	}
	
	public AIRWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AIRWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public void setBrowserActivity(BrowserActivity bActivity) {
		mBrowserActivity = bActivity;
	}
	
	public FrameLayout getLayout() {
		return mLayout;
	}
	
    public boolean inCustomView() {
		return (mCustomView != null);
	}
    
    public void hideCustomView() {
		mWebChromeClient.onHideCustomView();
	}
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		if ((mCustomView == null) && canGoBack()){
    			return true;
    		}
    	}
    	return super.onKeyDown(keyCode, event);
    }

    @Override
	public ActionMode startActionMode(Callback callback) {
    	// customize the handling of context menu when some text has been selected
    	mBrowserActivity.logMessage("Invoking customized callback", "CustomizedSelectActionModeCallbac");
    	mActionModeCallback = new CustomizedSelectActionModeCallback();
        return super.startActionMode(mActionModeCallback);
	}
    
    private class CustomizedSelectActionModeCallback implements ActionMode.Callback {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        	mBrowserActivity.logMessage("In function", "onCreateActionMode");
            return true;
        }

        // Called each time the action mode is shown.
        // Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        	mBrowserActivity.logMessage("In function", "onPrepareActionMode");
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return true;
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
        	// notify text selection when exiting action mode
        	mBrowserActivity.logMessage("In function", "onDestroyActionMode");
        	mBrowserActivity.executeAIRMobileFunction(JSNTVCmds.NTV_ON_TEXT_SELECTED, "");
        	// fix the Android 4.4 crash issue after pressing on selected text
        	// the solution is to clear the select quickly right after it is confirmed
        	if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.KITKAT) {
        		final Handler handler = new Handler();
        		handler.postDelayed(new Runnable() {
        			@Override
        			public void run() {
        				// clear the selection after 100ms
        				clearFocus();
        		  }
        		}, 100);
        	}
        }
    }
    private class AIRWebChromeClient extends WebChromeClient {
		private Bitmap 		mDefaultVideoPoster;
		private View 		mVideoProgressView;
    	
    	@Override
		public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback)
		{
    		AIRWebView.this.setVisibility(View.GONE);
	        
	        // if a view already exists then immediately terminate the new one
	        if (mCustomView != null) {
	            callback.onCustomViewHidden();
	            return;
	        }
	        
	        mCustomViewContainer.addView(view);
	        mCustomView = view;
	        mCustomViewCallback = callback;
	        mCustomViewContainer.setVisibility(View.VISIBLE);
		}
		
		@Override
		public void onHideCustomView() {
			
			if (mCustomView == null)
				return;	       
			
			// Hide the custom view.
			mCustomView.setVisibility(View.GONE);
			
			// Remove the custom view from its container.
			mCustomViewContainer.removeView(mCustomView);
			mCustomView = null;
			mCustomViewContainer.setVisibility(View.GONE);
			mCustomViewCallback.onCustomViewHidden();
			
			AIRWebView.this.setVisibility(View.VISIBLE);
		}
		
		@Override
		public Bitmap getDefaultVideoPoster() {
				
			if (mDefaultVideoPoster == null) {
				mDefaultVideoPoster = BitmapFactory.decodeResource(
						getResources(), R.drawable.ic_launcher);
		    }
			return mDefaultVideoPoster;
		}
		
		@Override
		public View getVideoLoadingProgressView() {
			
	        if (mVideoProgressView == null) {
	            LayoutInflater inflater = LayoutInflater.from(mContext);
	            mVideoProgressView = inflater.inflate(R.layout.video_loading_progress, null);
	        }
	        return mVideoProgressView; 
		}
    	
    	 @Override
         public void onReceivedTitle(WebView view, String title) {
            ((Activity) mContext).setTitle(title);
         }

         @Override
         public void onProgressChanged(WebView view, int newProgress) {
        	 ((Activity) mContext).getWindow().setFeatureInt(Window.FEATURE_PROGRESS, newProgress*100);
         }
         
         @Override
         public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
             callback.invoke(origin, true, false);
         }
    }
	
	private class MyWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	
	        return false;
	    }
	}
	
	static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
        new FrameLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

}
