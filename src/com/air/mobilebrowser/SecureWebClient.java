/********************************************************************************
* Educational Online Test Delivery System
* Copyright (c) 2015 American Institutes for Research
*
* Distributed under the AIR Open Source License, Version 1.0
* See accompanying file AIR-License-1_0.txt or at
* http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
*********************************************************************************/
package com.air.mobilebrowser;

import java.lang.ref.WeakReference;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Custom client for intercepting url requests and forwarding custom messages.
 */
public class SecureWebClient extends WebViewClient {
	//The URL Scheme we look for to intercept a custom message for the app.
	private static final String MSG_URL_SCHEME				= ":##airMobile_msgsnd##";
	
	private WeakReference<BrowserActivity> contextRef = null;

	public SecureWebClient(BrowserActivity activity) {
		contextRef = new WeakReference<BrowserActivity>(activity);
	}

	@Override
	public boolean shouldOverrideUrlLoading(final WebView view, String url) {
		final BrowserActivity context = contextRef.get();

		if (context != null) {
			context.logMessage("Received URL", url);
		
			final String[] parts = url.split(MSG_URL_SCHEME);
	
			if (parts.length >= 1 && url.contains(MSG_URL_SCHEME)) {
				// Delay the response to allow the method to return immediately.
				context.getHandler().postDelayed(new Runnable() {
					@Override
					public void run() {
						context.handleJSRequest(parts[0], parts.length > 1 ? parts[1] : null);
					}
				}, 1);
	
				return true;
			} else {
				// let the default webview handle loading the page
				return false;
			}
		}
		else {
			return false;
		}
	}

	public void onReceivedError(WebView view, int errorCode, String description, final String failingUrl) {
		final BrowserActivity context = contextRef.get();

		if (context != null) {
			view.loadUrl("about:none");
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.alert_http404_title).setMessage(
					R.string.alert_http404_message);
			builder.setPositiveButton(R.string.alert_http404_reload,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
	
							context.getHandler().post(new Runnable() {
								@Override
								public void run() {
									context.mWebView.loadUrl(failingUrl);
								}
							});
						}
					});
			builder.setNegativeButton(R.string.alert_btn_exit,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
	
							context.getHandler().post(new Runnable() {
								@Override
								public void run() {
									context.cleanup();
								}
							});
						}
					});
			builder.show();
		}
	}
}
