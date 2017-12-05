//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser;

import com.smarterapp.mobilebrowser.softkeyboard.IKeyboardSelected;
import com.smarterapp.mobilebrowser.softkeyboard.KeyboardUtil;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * This activity acts as a splash page for initializing the Secure Browser.
 * It's role is to ensure that the Secure Browser is chosen as the users 
 * default home application, as well as ensure that the bundled keyboard is
 * set as the users default input method. It is also responsible for collecting
 * any original user settings that will need to restored upon exit.
 */
public class StartActivity extends Activity implements OnClickListener, IKeyboardSelected 
{
	/** Reference ot continue button */
	private Button continueButton;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		continueButton = (Button) findViewById(R.id.btn_continue);
		continueButton.setOnClickListener(this);
		continueButton.setEnabled(false);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		OrigSettings.initOrigSettings(super.getApplicationContext());
		// Check the current keyboard for proper selection
		String target = getResources().getString(R.string.setting_target_keyboard);
		if (!KeyboardUtil.isCorrectKeyboard(super.getContentResolver(), target)) {
			// Incorrect keyboard selected, change keyboard
			continueButton.setEnabled(false);
			KeyboardUtil.forceKeyboard(this, target, this, true);
		}
		else {
			// Correct keyboard selected, allow user to continue
			continueButton.setEnabled(true);
		}
	}

	@Override
	public void onClick(View view) 
	{
		PackageManager pm = getPackageManager();
		
		//Enable the home component and start.
		ComponentName fauxHomeComponent = new ComponentName(getApplicationContext(), FauxHome.class);
		ComponentName homeComponent = new ComponentName(getApplicationContext(), BrowserActivity.class);
		
		pm.setComponentEnabledSetting(fauxHomeComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		pm.setComponentEnabledSetting(homeComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		
		pm.setComponentEnabledSetting(fauxHomeComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		pm.setComponentEnabledSetting(homeComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

		startActivity(browserIntent());
		
		pm.setComponentEnabledSetting(fauxHomeComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		
		finish();
	}
	
	/**
	 * Create a new browser intent.
	 * @return the browser intent
	 */
	protected Intent browserIntent()
	{
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		
		return intent;
	}
	
	/** Utility for debugging, gets the preffered package. s*/
	protected String getPreffered(Intent i) 
	{
		PackageManager pm = getPackageManager();
		final ResolveInfo mInfo = pm.resolveActivity(i, PackageManager.MATCH_DEFAULT_ONLY);
		return (String) pm.getApplicationLabel(mInfo.activityInfo.applicationInfo);
	}

	@Override
	public void keyboardSelected() {
		// In the case of forceKeyboard, only called when target keyboard selected
		continueButton.setEnabled(true);
	}
}
