//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.AssetManager;

import com.smarterapp.mobilebrowser.DeviceStatus.DeviceStatusChangeHandler;
import com.smarterapp.mobilebrowser.jscmds.AIRRecorderClearFiles;
import com.smarterapp.mobilebrowser.jscmds.AIRRecorderGetFile;
import com.smarterapp.mobilebrowser.jscmds.AIRRecorderGetFileList;
import com.smarterapp.mobilebrowser.jscmds.AIRRecorderInitialize;
import com.smarterapp.mobilebrowser.jscmds.AIRRecorderPausePlayback;
import com.smarterapp.mobilebrowser.jscmds.AIRRecorderResumePlayback;
import com.smarterapp.mobilebrowser.jscmds.AIRRecorderStartCapture;
import com.smarterapp.mobilebrowser.jscmds.AIRRecorderStartPlayback;
import com.smarterapp.mobilebrowser.jscmds.AIRRecorderStopCapture;
import com.smarterapp.mobilebrowser.jscmds.AIRRecorderStopPlayback;
import com.smarterapp.mobilebrowser.jscmds.CheckSpeakStatus;
import com.smarterapp.mobilebrowser.jscmds.ClearWebCache;
import com.smarterapp.mobilebrowser.jscmds.ClearWebCookies;
import com.smarterapp.mobilebrowser.jscmds.ExitApplication;
import com.smarterapp.mobilebrowser.jscmds.GetProcesses;
import com.smarterapp.mobilebrowser.jscmds.Initialize;
import com.smarterapp.mobilebrowser.jscmds.JSCmd;
import com.smarterapp.mobilebrowser.jscmds.JSCmdResponse;
import com.smarterapp.mobilebrowser.jscmds.JSKeys;
import com.smarterapp.mobilebrowser.jscmds.JSNTVCmds;
import com.smarterapp.mobilebrowser.jscmds.JavascriptLog;
import com.smarterapp.mobilebrowser.jscmds.OrientationLock;
import com.smarterapp.mobilebrowser.jscmds.PauseSpeaking;
import com.smarterapp.mobilebrowser.jscmds.RestartApplication;
import com.smarterapp.mobilebrowser.jscmds.ResumeSpeaking;
import com.smarterapp.mobilebrowser.jscmds.SetDefaultURL;
import com.smarterapp.mobilebrowser.jscmds.SetMicMuted;
import com.smarterapp.mobilebrowser.jscmds.SpeakText;
import com.smarterapp.mobilebrowser.jscmds.StopSpeaking;
import com.smarterapp.mobilebrowser.jscmds.TTSStatusRequest;
import com.smarterapp.mobilebrowser.softkeyboard.KeyboardUtil;

/**
 * Main activity component of the Secure Browser. Manages
 * the user interaction with the browser from a high level and
 * delegates work in response to requests from the web.
 */
@SuppressLint("SetJavaScriptEnabled")
public class BrowserActivity extends Activity implements DeviceStatusChangeHandler, OnInitListener, OnUtteranceCompletedListener
{
	private static final String TAG = "BroswerActivity";

	//Logging Tag
	protected static final String _TAG_ = "AIR_SEC";	

	/* Our WebView instance */
	protected AIRWebView mWebView;

	/* Wrapper for device feature status */
	protected DeviceStatus mDeviceStatus;

	/* Flag indicating we received a new intent to start the activity */
	protected boolean mNewIntent = false;
	
	/* Flag indicating the browser has been previously pushed to background */
	protected boolean mBeenBackgrounded = false;

	/* Receiver for intents from service */
	private SBReceiver mSBReceiver;
	/* Flag indicating if already changing keyboard */
	protected boolean mIsSelectingKeyboard = false;

	/* Flag to indicate if debug console is visible */
	private boolean mIsDebugEnabled = false;

	/* Handler */
	private Handler mHandler;
	/* Reference to system Audio Manager */
	private AudioManager mAudioManager;
	/* Map of JS command to implementation */
	private HashMap<String, JSCmd> mJSCommands;
	private TTSPlayer ttsPlayer;
	
	private String jsContent = "";
	
	private ActionMode mActionMode = null;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		ttsPlayer = new TTSPlayer(this);
		
		// Allow for out-of-band additions to thread queue (mainly for cleanup)
		mHandler = new Handler();

		// Prevents user from taking screenshots. 
		getWindow().setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Load layout
		mWebView = new AIRWebView(this, this);
		mWebView.requestFocus(View.FOCUS_DOWN);
        
		setContentView(R.layout.activity_browser);
		FrameLayout layout = (FrameLayout)findViewById(R.id.sec_webview);
		layout.addView(((AIRWebView)mWebView).getLayout());
		
		// Initialize device monitoring 
		mDeviceStatus = new DeviceStatus(this, this);
		mDeviceStatus.registerReceivers(this);

		// By default, lock to landscape
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		mDeviceStatus.lockedOrientation = "landscape";

		// Configure the webview 
		configureWebView(mWebView);

		// Configure Debug Console
		if (mIsDebugEnabled) {
			findViewById(R.id.slidingDrawer1).setVisibility(View.VISIBLE);
			findViewById(R.id.addressBarWrapper).setVisibility(View.VISIBLE);
			
			findViewById(R.id.goButton).setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {

					String url = ((EditText)findViewById(R.id.address_bar)).getText().toString();
					mWebView.loadUrl(url);
				}
			});
		}

		// Load the content 
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

		// load JS file content on browser startup
		loadJSCode(mWebView);
		
		String url = preferences.getString("pref_default_url", getString(R.string.url_default_location));

		// For testing, uncomment the following line to use a custom url:

		// Check for Internet connectivity
		if (mDeviceStatus.connectivity == DeviceStatus.CONNECTIVITY_CONNECTED) {
			mWebView.loadUrl(url);
		}
		else {
			mWebView.loadUrl("about:none");
		}
		
		// Register BroadcastListener for service intents
		mSBReceiver = new SBReceiver(this);
		LocalBroadcastManager.getInstance(super.getApplicationContext()).registerReceiver(mSBReceiver, new IntentFilter(super.getResources().getString(R.string.intent_black_logtag)));
		LocalBroadcastManager.getInstance(super.getApplicationContext()).registerReceiver(mSBReceiver, new IntentFilter(super.getResources().getString(R.string.intent_micmutechanged)));
		LocalBroadcastManager.getInstance(super.getApplicationContext()).registerReceiver(mSBReceiver, new IntentFilter(super.getResources().getString(R.string.intent_keyboardchange)));
		// add receiver for bluetooth keyboard connection/disconnection events
		LocalBroadcastManager.getInstance(super.getApplicationContext()).registerReceiver(mSBReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
		LocalBroadcastManager.getInstance(super.getApplicationContext()).registerReceiver(mSBReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

		IntentFilter testFilter = new IntentFilter(Intent.CATEGORY_HOME);

		super.getApplicationContext().registerReceiver(mSBReceiver, testFilter);

		// Get AudioManger
		mAudioManager = (AudioManager)super.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

		// Configure JS Command Processing
		configureJSCmdHandling();

		// Begin monitoring for focus change. 
		startService(new Intent(getApplicationContext(), ActivityWatchService.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		//- Remove exit functionality, exit will be via javascript only.
		menu.add(0, 0, 0, "Exit")
		.setIcon(android.R.drawable.ic_menu_close_clear_cancel)
		.setAlphabeticShortcut(SearchManager.MENU_KEY);

		/* Disabled Settings - Uncomment if needed (otherwise this will be removed later, along with the settings code) */
		/*
		menu.add(0, 1, 0, R.string.menu_settings)
		.setIcon(android.R.drawable.ic_menu_preferences)
		.setIntent(new Intent(android.provider.Settings.ACTION_SETTINGS));
		 */

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) 
		{

		case 0:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.alert_exit_title).setMessage(R.string.alert_exit_message);
			builder.setPositiveButton(R.string.alert_btn_exit, new OnClickListener() 
			{
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();

					// Get out of the dialog instance/execution and then perform cleanup
					mHandler.post(new Runnable(){
						@Override
						public void run() {
							cleanup();
						}
					});
				}
			});
			builder.setNegativeButton(R.string.alert_btn_cancel, new OnClickListener() 
			{
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();
				}
			});
			builder.show();
			return true;
		case 1:
			startActivity(new Intent(this, PreferencesActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed()
	{
		//Do nothing - prevents backing out of the activity.
	}
	
    @Override
	public void onActionModeStarted(ActionMode mode) {
	    if (mActionMode == null) {
	        mActionMode = mode;
	        Menu menu = mode.getMenu();
	        // Remove the default menu items (select all, copy, paste, search)
	        menu.clear();
	    }
	    super.onActionModeStarted(mode);
	}

	@Override
	public void onActionModeFinished(ActionMode mode) {
	    mActionMode = null;
	    super.onActionModeFinished(mode);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		//Update the device status with the new orientation, and notify
		int orientation = newConfig.orientation;
		String orientationString = "none";

		if(orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			orientationString = "portrait";
		}
		else if(orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			orientationString = "landscape";
		}
		else if(orientation == Configuration.ORIENTATION_UNDEFINED)
		{
			orientationString = "none";
		}

		this.mDeviceStatus.orientation = orientationString;

		super.onConfigurationChanged(newConfig);

		handleOrientationChange();
	}

	@Override
	protected void onNewIntent(Intent intent) 
	{
		super.onNewIntent(intent);
		mNewIntent = true;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		mWebView.onResume();
		
		if(!validateHomePackage())
		{
			Toast.makeText(this, "Please select Secure Browser as default to continue", Toast.LENGTH_LONG).show();
			startActivity(new Intent(this, StartActivity.class));
			cleanup();
		}

		//If we are not resuming due to a new intent, notify that we have been backgrounded.
		// for Android 5.0 (Lollipop) or later versions, use a different flag to check if the browser has been backgrounded
		if(!mNewIntent || ((android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) && mBeenBackgrounded))
		{
			reportReturnFromBackground();
		}
		
		mNewIntent = false;
		mBeenBackgrounded = false;
	}
	
	public String getJSContent()
	{
		return jsContent;
	}

	public void reportReturnFromBackground()
	{
		JSONObject jsonObj = new JSONObject();

		try 
		{
			jsonObj.put(JSKeys.EVENT, JSKeys.EVENT_RETURN_FROM_BACKGROUND);
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}

		executeAIRMobileFunction(JSNTVCmds.NTV_ON_EVENT_DISPATCHED, jsonObj.toString());
	}
	
	public void reportEnterBackground()
	{
		JSONObject jsonObj = new JSONObject();

		try 
		{
			jsonObj.put(JSKeys.EVENT, JSKeys.EVENT_ENTER_BACKGROUND);
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}

		executeAIRMobileFunction(JSNTVCmds.NTV_ON_EVENT_DISPATCHED, jsonObj.toString());
	}
	
	@Override
	public void onPause()
	{
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		ComponentName topActivity = tasks.get(0).topActivity;
		String packageName = topActivity.getPackageName();

		if (!packageName.equals(super.getPackageName()) && !packageName.equals("android")) {
			mBeenBackgrounded = true;
			reportEnterBackground();
		}
		mWebView.onPause();
		super.onPause();
	}

	public void onStop() {
		mBeenBackgrounded = true;
		reportEnterBackground();
		super.onStop();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		LocalBroadcastManager.getInstance(super.getApplicationContext()).unregisterReceiver(mSBReceiver);
		mDeviceStatus.unregisterReceivers(this);

		ttsPlayer.destroy();

		mWebView.loadUrl("about:blank");
		
		clearHome();
	}

	public WebView getWebView()
	{
		return mWebView;
	}
	
	private void loadJSCode(AIRWebView webView) {
		
		String jscontent = "";
        try {
        	// load JS content from the specific JS file
        	AssetManager am = getAssets();
            InputStream is = am.open("SecureBrowserAndroid.js");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String line;
            while (( line = br.readLine()) != null) {
                jscontent += line;
            }
            is.close();
            // store JS content
            jsContent = jscontent;
        }
        catch(Exception e){
        }
        // run the JS content
        webView.evaluateJavascript(jsContent, null);
	}
	
	/** 
	 * Configure a webview to use the activity as
	 * its client, and update its settings with 
	 * the desired parameters for the activity.
	 * @param webView the webview to configure.
	 */
	private void configureWebView(WebView webView)
	{
		webView.clearCache(true);
		webView.setWebViewClient(new SecureWebClient(this));

		WebSettings settings = mWebView.getSettings();
		settings.setBuiltInZoomControls(true);
		settings.setJavaScriptEnabled(true);
		//		settings.setPluginState(PluginState.ON_DEMAND);
		settings.setPluginState(PluginState.ON);
		settings.setAllowFileAccess(true);
		settings.setAllowContentAccess(true);
		settings.setSaveFormData(false);
		settings.setSavePassword(false);
		webView.clearFormData();
settings.setDomStorageEnabled(true);
		String defaultUserAgent = settings.getUserAgentString();

		StringBuilder sb = new StringBuilder();
		sb.append(defaultUserAgent);
		sb.append(" SmarterAppMobileSecureBrowser/2.0");
		sb.append(" OS/"); sb.append(mDeviceStatus.operatingSystem);
		sb.append(" Version/"); sb.append(mDeviceStatus.operatingSystemVersion);
		sb.append(" Model/"); sb.append(mDeviceStatus.model);
		
		settings.setUserAgentString(sb.toString());
	}

	/**
	 * Configure JS Command Handling
	 */
	private void configureJSCmdHandling() {
		mJSCommands = new HashMap<String,JSCmd>();
		addCommand(new CheckSpeakStatus(this, mDeviceStatus));
		addCommand(new GetProcesses(this, mDeviceStatus));
		addCommand(new Initialize(this, mDeviceStatus));
		addCommand(new OrientationLock(this, mDeviceStatus));
		addCommand(new SetDefaultURL(this, mDeviceStatus));
		addCommand(new SetMicMuted(this, mDeviceStatus, mAudioManager));
		addCommand(new SpeakText(this, mDeviceStatus, ttsPlayer));
		addCommand(new PauseSpeaking(this, mDeviceStatus, ttsPlayer));
		addCommand(new ResumeSpeaking(this, mDeviceStatus, ttsPlayer));
		addCommand(new StopSpeaking(this, mDeviceStatus, ttsPlayer));
		addCommand(new TTSStatusRequest(this, mDeviceStatus));
		addCommand(new ExitApplication(this, mDeviceStatus));
		addCommand(new RestartApplication(this, mDeviceStatus));
		addCommand(new JavascriptLog(this, mDeviceStatus));
		addCommand(new ClearWebCache(this, mDeviceStatus));
		addCommand(new ClearWebCookies(this, mDeviceStatus));
		
		addCommand(new AIRRecorderInitialize(this, mDeviceStatus));
		addCommand(new AIRRecorderStartCapture(this, mDeviceStatus));
		addCommand(new AIRRecorderStopCapture(this, mDeviceStatus));
		
		addCommand(new AIRRecorderStartPlayback(this, mDeviceStatus));
		addCommand(new AIRRecorderStopPlayback(this, mDeviceStatus));
		addCommand(new AIRRecorderResumePlayback(this, mDeviceStatus));
		addCommand(new AIRRecorderPausePlayback(this, mDeviceStatus));
		
		addCommand(new AIRRecorderGetFileList(this, mDeviceStatus));
		addCommand(new AIRRecorderGetFile(this, mDeviceStatus));
		addCommand(new AIRRecorderClearFiles(this, mDeviceStatus));
	}

	@SuppressLint("DefaultLocale")
	private void addCommand(JSCmd command) {
		mJSCommands.put(command.getCmd().toLowerCase(Locale.US), command);
	}

	/**
	 * Clear the setting for default home.
	 */
	private void clearHome()
	{
		PackageManager pm = getPackageManager();

		ComponentName fauxHomeComponent = new ComponentName(getApplicationContext(), FauxHome.class);
		ComponentName homeComponent = new ComponentName(getApplicationContext(), BrowserActivity.class);

		pm.setComponentEnabledSetting(fauxHomeComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		pm.setComponentEnabledSetting(homeComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		pm.setComponentEnabledSetting(fauxHomeComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		pm.setComponentEnabledSetting(homeComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
	}

	/**
	 * Cleanup everything that was changed.
	 */
	public void cleanup() {
		stopService(new Intent(getApplicationContext(), ActivityWatchService.class));
		//clear home and exit
		PackageManager pm = getPackageManager();
		ComponentName fauxHomeComponent = new ComponentName(getApplicationContext(), FauxHome.class);
		ComponentName homeComponent = new ComponentName(getApplicationContext(), BrowserActivity.class);

		pm.setComponentEnabledSetting(fauxHomeComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		pm.setComponentEnabledSetting(homeComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		
		mAudioManager.setMicrophoneMute(OrigSettings.getInstance().isMicMuted());
		
		String curKeyboard = KeyboardUtil.getKeyboardPackage(BrowserActivity.this.getContentResolver());

		if (!curKeyboard.equals(OrigSettings.getInstance().getKeyboard())) {

			KeyboardUtil.showInputChangedExitDialog(this, new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {

					finish();

				}
			});
		}
		else
		{
			finish();
		}
	}
	
	
	

	/**
	 * Determine if the application is set as the default
	 * home.
	 * @return true if the default home package is the secure browser, false
	 * otherwise.
	 */
	public boolean validateHomePackage()
	{
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);

		PackageManager pm = getPackageManager();
		final ResolveInfo mInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
		String homePackage = mInfo.activityInfo.packageName;
		return homePackage.equals(getPackageName());
	}

	private void handleOrientationChange()
	{
		final String ORIENTATION = "orientation";

		JSONObject jsonToSend = new JSONObject();

		try
		{
			jsonToSend.put(ORIENTATION, mDeviceStatus.orientation);
		}
		catch(JSONException e) { /* */ }

		executeAIRMobileFunction(JSNTVCmds.NTV_ON_ORIENTATION_CHANGE, jsonToSend.toString());
	}

	private void handleUnsupportedRequest(JSONObject parameters)
	{
		String requestIdentifier = null;

		try
		{
			requestIdentifier = parameters.getString(JSKeys.REQUEST_IDENTIFIER);

		} 
		catch(JSONException e) { /* */ }

		JSONObject jsonToSend = new JSONObject();

		try 
		{
			if(requestIdentifier != null)
			{
				jsonToSend.put(JSKeys.REQUEST_IDENTIFIER, requestIdentifier);
			}

			executeAIRMobileFunction(JSNTVCmds.NTV_ON_UNSUPPORTED_REQUEST, jsonToSend.toString());
		} 
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Execute an AIRMobile javascript function. 
	 * eg. calling executeAIRMobileFunction("someFunction", "someParam");
	 * Executes AIRMobile.someFunction('someParam'); in the webview, and
	 * logs the javascript to the console.
	 * @param functionName the name of the AIRMobile function.
	 * @param parameter the parameters to pass in the function.
	 */
	public void executeAIRMobileFunction(String functionName, String parameter)
	{
		String javascript = null;

		if(parameter != null)
		{
			javascript = "SecureBrowser." + functionName + "('" + parameter + "');";
		}
		else
		{
			javascript = "SecureBrowser." + functionName + "();";
		}

		logMessage("Executing Javascript", javascript);
		
		mWebView.evaluateJavascript(javascript, null);
	}

	/** Logs an onscreen message for debugging. */
	public void logMessage(final TextView consoleView, String message, String value, int color)
	{
		if(mIsDebugEnabled && consoleView != null)
		{
			consoleView.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					ViewParent parent = consoleView.getParent();
					final ScrollView scroll = (ScrollView)parent;


					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {

							scroll.smoothScrollTo(0, consoleView.getMeasuredHeight()+10);

						}
					}, 0);

				}
			});
			Editable editable = consoleView.getEditableText();

			SpannableString str = null;

			if(editable == null)
			{
				editable = new SpannableStringBuilder();
				str = new SpannableString(message + ": " + value);
				str.setSpan(new ForegroundColorSpan(color), message.length()+2, message.length()+2+value.length(), 0);
			}
			else
			{
				str = new SpannableString("\n" + message + ": " + value);
				str.setSpan(new ForegroundColorSpan(color), message.length()+2, message.length()+3+value.length(), 0);
			}

			editable.append(str);

			consoleView.setText(editable, TextView.BufferType.EDITABLE);

			ViewParent parent = consoleView.getParent();
			if(parent instanceof ScrollView)
			{
				final ScrollView scroll = (ScrollView)parent;


				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {

						scroll.smoothScrollTo(0, consoleView.getMeasuredHeight()+10);

					}
				}, 1000);
			}
		}
	}

	/**
	 * 
	 * @param message
	 * @param value
	 */
	public void logMessage(String message, String value)
	{
		if (mIsDebugEnabled) {
			TextView consoleView = (TextView)findViewById(R.id.air_console_view);
			logMessage(consoleView, message, value, Color.BLUE);
		}
	}

	/**
	 * Handle a request sent from the web page to do something.
	 * @param request the name of the request
	 * @param parameter optional parameter for the request when applicable
	 */
	protected void handleJSRequest(String request, String parameters)
	{
		Log.i(_TAG_, _TAG_ + " Received " + request);
		logMessage("Received URL", request);
		JSONObject jsonParameters = null;

		try 
		{
			if (parameters != null) {
				jsonParameters = new JSONObject(URLDecoder.decode(parameters, "UTF-8"));
			}
			else {
				jsonParameters = new JSONObject();
			}
		} 
		catch (JSONException e) { /* Do nothing, we may not be getting actual parameters. */} 
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		/* Delegate the work */
		if (mJSCommands.containsKey(request.toLowerCase(Locale.US))) {
			try {
				JSCmdResponse response = mJSCommands.get(request.toLowerCase(Locale.US)).handle(jsonParameters);
				if (response != null) {
					executeAIRMobileFunction(response.getNTVCmd(), response.getParameters());
				}
			} catch (JSONException e) {
				Log.e(_TAG_, _TAG_ + " Error:", e);
			}
		}
		else
		{
			handleUnsupportedRequest(jsonParameters);
			Log.i(_TAG_, "Unknown JS Request " + request);
		}
	}


	/* - DeviceStatusChangeHandler Methods - */

	@Override
	public void onConnectivityChanged(DeviceStatus deviceStatus) 
	{
		JSONObject jsonObj = new JSONObject();

		try 
		{
			jsonObj.put(JSKeys.STATUS, deviceStatus.connectivity);
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}

		executeAIRMobileFunction(JSNTVCmds.NTV_ON_CONNECTIVITY_CHANGE, jsonObj.toString());

		if (deviceStatus.connectivity == DeviceStatus.CONNECTIVITY_DISCONNECTED) {
			handleNoInternet();
		}
	}

	private void handleNoInternet() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.alert_exit_title).setMessage(R.string.alert_nointernet_message);
		builder.setPositiveButton(R.string.alert_btn_exit, new OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				dialog.dismiss();

				getHandler().post(new Runnable(){
					@Override
					public void run() {
						cleanup();
					}
				});
			}
		});
		builder.show();	
	}

	@Override
	public void onTTSChanged(DeviceStatus deviceStatus) 
	{

	}

	@Override
	public void onClipboardChanged(String clipText)
	{
		JSONObject jsonToSend = new JSONObject();
		
		try 
		{
			jsonToSend.put("contents", clipText);
			jsonToSend.put(JSKeys.EVENT, JSKeys.EVENT_CLIPBOARD_CHANGED);
			executeAIRMobileFunction(JSNTVCmds.NTV_ON_EVENT_DISPATCHED, jsonToSend.toString());
		} 
		catch(JSONException e) { /* */ }
		
	}
	
	public void onMicChanged() {
		boolean micMute = mAudioManager.isMicrophoneMute();
		if (micMute != mDeviceStatus.micMuted) {
			JSONObject jsonToSend = new JSONObject();

			// Make sure device status matches the actual
			mDeviceStatus.micMuted = mAudioManager.isMicrophoneMute();

			try
			{
				jsonToSend.put(DeviceStatus.MICMUTED_KEY, mDeviceStatus.micMuted);			
				executeAIRMobileFunction(JSNTVCmds.NTV_ON_MIC_MUTE_CHANGED, jsonToSend.toString());
			}
			catch(JSONException e) { /* */ }
		}
	}

	public void onKeyboardChanged() {
		mDeviceStatus.keyboard = KeyboardUtil.getKeyboardPackage(super.getContentResolver());

		JSONObject jsonObj = new JSONObject();

		try 
		{
			jsonObj.put(JSKeys.KEYBOARD, mDeviceStatus.keyboard);
			jsonObj.put(JSKeys.EVENT, JSKeys.EVENT_KEYBOARD_CHANGED);
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}

		executeAIRMobileFunction(JSNTVCmds.NTV_ON_EVENT_DISPATCHED, jsonObj.toString());
	}

	public void onBlackLogTag() {
		executeAIRMobileFunction(JSNTVCmds.NTV_MINI_APP_LAUNCHED, null);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			
			int result = ttsPlayer.getTextToSpeech().setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e(TAG, "Language data missing or not supported");
				mDeviceStatus.ttsEngineStatus = DeviceStatus.TTS_ENGINE_STATUS_UNAVAILABLE;
			}
			else {
				mDeviceStatus.ttsEngineStatus = DeviceStatus.TTS_ENGINE_STATUS_IDLE;
			}
		}
		else {
			Log.e(TAG, "Unable to initialize TextToSpeech (status: " + status + ")");
			mDeviceStatus.ttsEngineStatus = DeviceStatus.TTS_ENGINE_STATUS_UNAVAILABLE;
		}

		// Notification of completion comes in on different thread, currently
		// UI updates necessary in executeAIRMobileFunction
		super.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				JSONObject jsonToSend = new JSONObject();

				try 
				{
					jsonToSend.put(JSKeys.TTS_ENABLED, mDeviceStatus.ttsEnabled);
					jsonToSend.put(JSKeys.TTS_ENGINE_STATUS, mDeviceStatus.ttsEngineStatus);
					executeAIRMobileFunction(JSNTVCmds.NTV_ON_TTS_ENABLED, jsonToSend.toString());
				} 
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onUtteranceCompleted(String requestIdentifier) {
		final JSONObject jsonToSend = new JSONObject();

		try
		{
			jsonToSend.put("enabled", mDeviceStatus.ttsEnabled);

			mDeviceStatus.ttsEngineStatus = DeviceStatus.TTS_ENGINE_STATUS_IDLE;
			jsonToSend.put(JSKeys.TTS_ENGINE_STATUS, mDeviceStatus.ttsEngineStatus);

			// Notification of completion comes in on different thread, currently
			// UI updates necessary in executeAIRMobileFunction
			super.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					executeAIRMobileFunction(JSNTVCmds.NTV_ON_TTS_ENABLED, jsonToSend.toString());
				}});

		}
		catch(JSONException e) { /* */ }		
	}
	
	public void onTTSSynchronized(final String progressStr) {
		super.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				executeAIRMobileFunction("onTTSSynchronized", progressStr);
			}});
	}

	/**
	 * @return the mHandler
	 */
	public Handler getHandler() {
		return mHandler;
	}

	public boolean isDebugEnabled() {
		return mIsDebugEnabled;
	}
	

}
