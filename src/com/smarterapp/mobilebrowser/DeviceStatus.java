//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.NetworkInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.smarterapp.mobilebrowser.NetworkReceiver.NetworkStatusHandler;
import com.smarterapp.mobilebrowser.softkeyboard.KeyboardUtil;

/**
 * Data wrapper that holds information about the current device.
 * This class will also optionally listen for changes in some status
 * features such as network connectivity state.
 *
 */
public class DeviceStatus implements NetworkStatusHandler, OnPrimaryClipChangedListener
{ 
	// - JSON Keys - //
	public static final String MODEL_KEY = "model";
	public static final String MANUFACTURER_KEY = "manufacturer";
	public static final String OS_KEY = "operatingSystem";
	public static final String OS_VERSION_KEY = "operatingSystemVersion";
	public static final String ROOT_ACCESS_KEY = "rootAccess";
	public static final String API_VERSION_KEY = "apiVersion";
	public static final String INSTALLED_PKGS_KEY = "installedPackages";
	public static final String RUNNING_PRCS_KEY = "runningProcesses";
	public static final String TTS_KEY = "textToSpeech";
	public static final String CONNECTIVITY_KEY = "connectivity";
	public static final String DEFAULT_URL_KEY = "defaultURL";
	public static final String ORIENTATION_KEY = "orientation";
	public static final String ORIENTATION_LOCK_KEY = "lockedOrientation";
	public static final String SCREEN_RESOLUTION_KEY = "screenResolution";
	public static final String KEYBOARD_KEY = "keyboard";
	public static final String MICMUTED_KEY = "muted";
	public static final String MAC_ADDRESS_KEY = "macAddress";
	public static final String IP_ADDRESS_KEY = "ipAddress";
	public static final String START_TIME_KEY = "startTime";
	public static final String DEFAULT_TTS_LANGUAGE_KEY = "defaultLanguage";
	public static final String TTS_LANGUAGES_KEY = "availableTTSLanguages";
	public static final String TTS_ENGINE_STATUS = "ttsEngineStatus";
	public static final String TTS_ENGINE_STATUS_IDLE = "idle";
	public static final String TTS_ENGINE_STATUS_PAUSED = "paused";
	public static final String TTS_ENGINE_STATUS_PLAYING = "playing";
	public static final String TTS_ENGINE_STATUS_UNAVAILABLE = "unavailable";
	
	protected static final String CONNECTIVITY_CONNECTED = "connected";
	protected static final String CONNECTIVITY_DISCONNECTED = "disconnected";
	
	private static final String defaultMacAddress = "02:00:00:00:00:00";
    private static final String fileAddressMac = "/sys/class/net/wlan0/address"; 
	
	public interface DeviceStatusChangeHandler
	{
		public void onConnectivityChanged(DeviceStatus deviceStatus);
		public void onTTSChanged(DeviceStatus deviceStatus);
		public void onClipboardChanged(String clipText);
	}

	private WeakReference<DeviceStatusChangeHandler> mHandlerRef;
	private WeakReference<Context> mContextRef;
	private NetworkReceiver mNetStatusReceiver;

	public String model;
	public String operatingSystem;
	public String operatingSystemVersion;
	public float apiVersion;
	public ArrayList<String> runningProcesses;
	public ArrayList<String> installedPackages;
	public boolean ttsEnabled;
	public String ttsEngineStatus;
	public boolean rootAccess;
	public String connectivity;
	public String orientation;
	public String lockedOrientation;
	public String resolution;
	public String keyboard;
	public boolean micMuted;
	public String macAddress;
	public String ipAddress;
	public String startTime;
	public String defaultTTSLanguage;
	public JSONArray ttsLanguages;
	public boolean ttsPauseResumeEnabled = true;
	
	/**
	 * Construct a new DeviceStatus object using the specified
	 * context. Note* the context must not be null.
	 * @param context the context to initialize with.
	 */
	public DeviceStatus(Context context, DeviceStatusChangeHandler handler)
	{
		mContextRef = new WeakReference<Context>(context);
		mHandlerRef = new WeakReference<DeviceStatus.DeviceStatusChangeHandler>(handler);
		mNetStatusReceiver = new NetworkReceiver(this);

		//Calling these has the side effect of setting the ivars
		packages();
		processes();

		this.model = android.os.Build.MODEL;
		this.operatingSystem = "Android";
		this.operatingSystemVersion = android.os.Build.VERSION.RELEASE + " " + android.os.Build.VERSION.INCREMENTAL;
		this.rootAccess = RootUtility.isDeviceRooted();
		this.apiVersion = 3.0f;
		this.ttsEnabled = this.runningProcesses.contains("com.google.android.tts");
		this.connectivity = NetworkUtil.haveInternetConnection(context) ? "connected" : "disconnected";
		this.lockedOrientation = "none";
		this.micMuted = ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE)).isMicrophoneMute();
		this.ipAddress = NetworkUtil.getIPAddress();
		this.defaultTTSLanguage = new Locale("en").getISO3Language();
		
		TextToSpeech tts = new TextToSpeech(context, null);
		Locale[] allLocales = Locale.getAvailableLocales();
		JSONArray available = new JSONArray();
		ArrayList<String> langs = new ArrayList<String>();
		
		int maxnumLang = 30; // set a maximum number of language we want to include
		
		for(Locale locale : allLocales)
		{
			if(tts.isLanguageAvailable(locale) == TextToSpeech.LANG_AVAILABLE);
			{
				String isoLanguage = locale.getISO3Language();
				if(!langs.contains(isoLanguage))
				{
					langs.add(isoLanguage);
					
					try 
					{
						String langCode = locale.getLanguage();
						// always include English, Spanish and Hawaiian regardless of the language limit
						if (available.length() < maxnumLang 
							|| langCode.equals(new Locale("en").getLanguage()) 
							|| langCode.equals(new Locale("es").getLanguage())
							|| langCode.equals(new Locale("haw").getLanguage())) {
							JSONObject voice = new JSONObject();
							voice.put("lang", langCode);
							voice.put("id", langCode);
							voice.put("name", locale.getDisplayName());
							available.put(voice);
						}
					} 
					catch (JSONException e) 
					{
						e.printStackTrace();
					}
				}
				
			}
		}
		
		this.ttsLanguages = available;
		tts.shutdown();
		
		WifiManager wifiMan = (WifiManager) context.getSystemService(
                Context.WIFI_SERVICE);
		WifiInfo wifiInf = wifiMan.getConnectionInfo();
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP) {
			this.macAddress = recupAdresseMAC(wifiMan);
		} else {
			this.macAddress = wifiInf.getMacAddress();
		}
		
		this.startTime = getStartTime();
		
		DisplayMetrics dm = new DisplayMetrics();
		
		WindowManager mg = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mg.getDefaultDisplay().getMetrics(dm);

		final int height = dm.heightPixels;
		final int width = dm.widthPixels;
		this.resolution = "{" + width + "," + height + "}";
		
		int orientation = context.getResources().getConfiguration().orientation;
		
		if(orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			this.orientation = "landscape";
		}
		else if(orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			this.orientation = "portrait";
		}
		else
		{
			this.orientation = "none";
		}
		
		this.keyboard = KeyboardUtil.getKeyboardPackage(context.getContentResolver());
		
		this.ttsEngineStatus = TTS_ENGINE_STATUS_UNAVAILABLE;
	}
	
	@Override
	public void onPrimaryClipChanged() 
	{
		Context context = mContextRef.get();
		
		if(context != null)
		{
			ClipboardManager clipboardManager = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
			Item item = clipboardManager.getPrimaryClip().getItemAt(0);
			
			String text = item.coerceToText(context).toString().trim();
			
			if(!TextUtils.isEmpty(text))
			{
				DeviceStatusChangeHandler handler = mHandlerRef.get();

				if(handler != null)
				{
					handler.onClipboardChanged(text);
				}
			}
		}
	}

	/** Generate a JSON object for the device info. */
	public JSONObject toJSONObject()
	{
		JSONObject json = new JSONObject();

		try 
		{
			json.put(MODEL_KEY, this.model);
			json.put(MANUFACTURER_KEY, android.os.Build.MANUFACTURER);
			json.put(OS_KEY, this.operatingSystem);
			json.put(OS_VERSION_KEY, this.operatingSystemVersion);
			// json.put(ROOT_ACCESS_KEY, this.rootAccess);
			// json.put(API_VERSION_KEY, this.apiVersion);
			// json.put(INSTALLED_PKGS_KEY, new JSONArray(this.installedPackages));
			json.put(RUNNING_PRCS_KEY, new JSONArray(this.runningProcesses));
			json.put(TTS_KEY, this.ttsEnabled);
			json.put(TTS_ENGINE_STATUS, this.ttsEngineStatus);
			json.put(CONNECTIVITY_KEY, this.connectivity);
			json.put(DEFAULT_URL_KEY, getDefaultURL());
			// json.put(SCREEN_RESOLUTION_KEY, this.resolution);
			// json.put(ORIENTATION_KEY, this.orientation);
			// json.put(ORIENTATION_LOCK_KEY, this.lockedOrientation);
			json.put(KEYBOARD_KEY, this.keyboard);
			// json.put(MICMUTED_KEY, this.micMuted);
			json.put(MAC_ADDRESS_KEY, this.macAddress);
			// json.put(IP_ADDRESS_KEY, this.ipAddress);
			json.put(START_TIME_KEY, this.startTime);
			json.put(DEFAULT_TTS_LANGUAGE_KEY, this.defaultTTSLanguage);
			json.put(TTS_LANGUAGES_KEY, this.ttsLanguages);

		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}

		return json;
	}

	public String toJSONString()
	{
		return this.toJSONObject().toString();
	}

	/**
	 * Register the DeviceStatus receivers to listen for changes using
	 * the context passed.
	 * Make sure to call unregisterReceivers when done.
	 * @param context the context to register the receivers with.
	 */
	public void registerReceivers(Context context)
	{
		mNetStatusReceiver.register(context);
		ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		clipboardManager.addPrimaryClipChangedListener(this);
	}

	/**
	 * Unregister the DeviceStatus receivers from the context specified.
	 * @param context the context to unregister the receivers.
	 */
	public void unregisterReceivers(Context context)
	{
		mNetStatusReceiver.unregister(context);
		ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		clipboardManager.removePrimaryClipChangedListener(this);
	}

	/** Retrieves the list of running process names from the ActivityManager. */
	public ArrayList<String> processes()
	{
		ArrayList<String> results = new ArrayList<String>();

		Context context = mContextRef.get();

		if(context != null)
		{
			ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			ArrayList<RunningAppProcessInfo> processes = (ArrayList<RunningAppProcessInfo>) am.getRunningAppProcesses();


			for(RunningAppProcessInfo info : processes)
			{
				results.add(info.processName);
			}
		}

		this.runningProcesses = results;

		return results;
	}

	/** Retrieves the list of installed package names from the PackageManager. */
	public ArrayList<String> packages()
	{
		ArrayList<String> results = new ArrayList<String>();

		Context context = mContextRef.get();

		if(context != null)
		{ 
			PackageManager packageManager = context.getPackageManager();
			ArrayList<PackageInfo> appList = (ArrayList<PackageInfo>) packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);

			for(PackageInfo pack : appList)
			{
				results.add(pack.packageName);
			}
		}

		this.installedPackages = results;

		return results;
	}

	/**
	 * Check if the processes 'com.google.android.tts' is currently
	 * running. 
	 * @return true if the tts process is running, false otherwise.
	 */
	public boolean updateTTSStatus()
	{
		this.ttsEnabled = this.runningProcesses.contains("com.google.android.tts");
		
		return this.ttsEnabled;
	}

	/* NetworkStatusHandler */

	@Override
	public void onNetworkStatusChanged(NetworkInfo netInfo) 
	{	
		this.connectivity = netInfo.isConnected() ? CONNECTIVITY_CONNECTED : CONNECTIVITY_DISCONNECTED;

		DeviceStatusChangeHandler handler = mHandlerRef.get();

		if(handler != null)
		{
			handler.onConnectivityChanged(this);
		}
	}

	public String getDefaultURL()
	{
		Context context = mContextRef.get();
		String url = null;

		if(context != null)
		{
			String preferenceName = context.getString(R.string.pref_default_url);
			String defaultValue = context.getString(R.string.url_default_location);
			
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			url = preferences.getString(preferenceName, defaultValue);
		}

		return url;
	}

	public void setDefaultURL(String url)
	{
		Context context = mContextRef.get();

		if(context != null)
		{
			String preferenceName = context.getString(R.string.pref_default_url);
			
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = preferences.edit();

			if(url == null)
			{
				editor.remove(preferenceName);
			}
			else
			{
				editor.putString(preferenceName, url);
			}

			editor.apply();
		}
	}

	public String getStartTime() {
		Date start = new Date();
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		
		return formatter.format(start);
	}
	
	private static String recupAdresseMAC(WifiManager wifiMan) {
        WifiInfo wifiInf = wifiMan.getConnectionInfo();

        if(wifiInf.getMacAddress().equals(defaultMacAddress)){
            String ret = null;
            try {
                ret= getAdressMacByInterface();
                if (ret != null){
                    return ret;
                } else {
                    ret = getAddressMacByFile(wifiMan);
                    return ret;
                }
            } catch (IOException e) {
            } catch (Exception e) {
            }
        } else{
            return wifiInf.getMacAddress();
        }
        return defaultMacAddress;
    }

	private static String getAdressMacByInterface(){
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (nif.getName().equalsIgnoreCase("wlan0")) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return "";
                    }

                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02X:",b));
                    }

                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            }

        } catch (Exception e) {
        }
        return null;
    }

	private static String getAddressMacByFile(WifiManager wifiMan) throws Exception {
        String ret;
        int wifiState = wifiMan.getWifiState();

        wifiMan.setWifiEnabled(true);
        File fl = new File(fileAddressMac);
        FileInputStream fin = new FileInputStream(fl);
        StringBuilder builder = new StringBuilder();
        int ch;
        while((ch = fin.read()) != -1){
        	builder.append((char)ch);
        }

        ret = builder.toString();
        fin.close();

        boolean enabled = WifiManager.WIFI_STATE_ENABLED == wifiState;
        wifiMan.setWifiEnabled(enabled);
        return ret;
    }
}