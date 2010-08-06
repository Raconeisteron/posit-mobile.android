/*
 * File: PositMain.java
 * 
 * Copyright (C) 2009 The Humanitarian FOSS Project (http://www.hfoss.org)
 * 
 * This file is part of POSIT, Portable Open Search and Identification Tool.
 *
 * POSIT is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published 
 * by the Free Software Foundation; either version 3.0 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU LGPL along with this program; 
 * if not visit http://www.gnu.org/licenses/lgpl.html.
 * 
 */
package org.hfoss.posit.android;

import org.hfoss.posit.android.adhoc.RWGConstants;
import org.hfoss.posit.android.adhoc.RWGService;
import org.hfoss.posit.android.utilities.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

/**
 * Implements the main activity and the main screen for the POSIT application.
 */
public class PositMain extends Activity implements OnClickListener,
		RWGConstants {

	private static final int CONFIRM_EXIT = 0;
	private static final String TAG = "PositMain";
	private static final int LOGIN_ACTIVITY = 1;
	public static final int LOGIN_CANCELED = 3;
	public static final int LOGIN_SUCCESSFUL = 4;
	private static final int REGISTRATION_CANCELLED = 5;
	private static final int REGISTRATION_ACTIVITY = 11;
	private SharedPreferences sp;
	// public static AdhocClient mAdhocClient;
	public static WifiManager wifiManager;
	public RWGService rwgService;
	public Intent rwg;

	NotificationManager mNotificationManager;

	/**
	 * Called when the activity is first created. Sets the UI layout, adds the
	 * buttons, checks whether the phone is registered with a POSIT server.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO has to be enabled only if SharedPreference says so or a similar
		// version of that
		// rwgService =new RWGService();

		sp = PreferenceManager.getDefaultSharedPreferences(this);

		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		if (!sp.getBoolean("tutorialComplete", false)) {
			Intent i = new Intent(this, TutorialActivity.class);
			startActivity(i);
		} else {
			if (!checkPhoneRegistrationAndInitialSync()) {
				Intent i = new Intent(this, RegisterActivity.class);
				startActivityForResult(i, REGISTRATION_ACTIVITY);
			} else {
				setContentView(R.layout.main);

				final ImageButton addFindButton = (ImageButton) findViewById(R.id.addFindButton);
				if (addFindButton != null)
					addFindButton.setOnClickListener(this);

				final ImageButton listFindButton = (ImageButton) findViewById(R.id.listFindButton);
				if (listFindButton != null) {
					// Log.i(TAG, listFindButton.getText() + "");
					listFindButton.setOnClickListener(this);
				}
			}
			sp = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = sp.edit();

			editor.putString("PROJECT_NAME", "");
			Log.i(TAG, "onCreate(), Preferences= " + sp.getAll().toString());

			// NOTE: If the shared preferences get left in a state with the
			// Tracker's not set to IDLE,
			// it will be impossible to start the Tracker. To do so, use the
			// statements here to reset
			// Tracker State to IDLE.
			// This is an area that could use a better algorithm based on
			// Android's
			// life cycle.
			// editor.putInt(BackgroundTrackerActivity.SHARED_STATE,
			// BackgroundTrackerActivity.IDLE);
			// editor.commit();

			// If this is the first run on this device, let the user register
			// the
			// phone.

			Utils.showToast(this, "Current Project: "
					+ sp.getString("PROJECT_NAME", ""));

			/*      ******* POLICY: RWG should not be running at start up */

			// if (RWGService.isRunning()) {
			// Log.i(TAG, "RWG running");
			// Utils.showToast(this, "RWG running");
			// }
		}

	}

	public void OnResume() {
		super.onResume();
		if (!checkPhoneRegistrationAndInitialSync()) {
			Intent i = new Intent(this, RegisterActivity.class);
			startActivityForResult(i, REGISTRATION_ACTIVITY);
		} else {
			setContentView(R.layout.main);

			final ImageButton addFindButton = (ImageButton) findViewById(R.id.addFindButton);
			if (addFindButton != null)
				addFindButton.setOnClickListener(this);

			final ImageButton listFindButton = (ImageButton) findViewById(R.id.listFindButton);
			if (listFindButton != null) {
				// Log.i(TAG, listFindButton.getText() + "");
				listFindButton.setOnClickListener(this);
			}
		}
	}

	public void onRestart() {
		super.onRestart();
		if (!checkPhoneRegistrationAndInitialSync()) {
			Intent i = new Intent(this, RegisterActivity.class);
			startActivityForResult(i, REGISTRATION_ACTIVITY);
		} else {
			setContentView(R.layout.main);

			final ImageButton addFindButton = (ImageButton) findViewById(R.id.addFindButton);
			if (addFindButton != null)
				addFindButton.setOnClickListener(this);

			final ImageButton listFindButton = (ImageButton) findViewById(R.id.listFindButton);
			if (listFindButton != null) {
				// Log.i(TAG, listFindButton.getText() + "");
				listFindButton.setOnClickListener(this);
			}
		}
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (!checkPhoneRegistrationAndInitialSync()) {
			Intent i = new Intent(this, RegisterActivity.class);
			startActivityForResult(i, REGISTRATION_ACTIVITY);
		} else {
			setContentView(R.layout.main);

			final ImageButton addFindButton = (ImageButton) findViewById(R.id.addFindButton);
			if (addFindButton != null)
				addFindButton.setOnClickListener(this);

			final ImageButton listFindButton = (ImageButton) findViewById(R.id.listFindButton);
			if (listFindButton != null) {
				// Log.i(TAG, listFindButton.getText() + "");
				listFindButton.setOnClickListener(this);
			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case LOGIN_ACTIVITY:
			if (resultCode == LOGIN_CANCELED)
				finish();
			else if (resultCode == LOGIN_SUCCESSFUL) {
				Intent intent = new Intent(this, ShowProjectsActivity.class);
				startActivity(intent);
			}
			break;
		case REGISTRATION_CANCELLED:
			finish();
			break;
		case REGISTRATION_ACTIVITY:
			if (resultCode == RegisterActivity.BACK_BUTTON)
				finish();
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}

	}

	/**
	 * Handles clicks on PositMain's buttons.
	 */
	public void onClick(View view) {
		// Make sure the user has chosen a project before trying to add finds
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (sp.getString("PROJECT_NAME", "").equals("")) {
			Utils.showToast(this, "To get started, you must choose a project.");
			Intent i = new Intent(this, ShowProjectsActivity.class);
			startActivity(i);
		} else {
			Intent intent = new Intent();

			switch (view.getId()) {
			case R.id.addFindButton:
				intent.setClass(this, FindActivity.class);
				intent.setAction(Intent.ACTION_INSERT);
				startActivity(intent);
				break;
			case R.id.listFindButton:
				intent.setClass(this, ListFindsActivity.class);
				startActivity(intent);
				break;
			// case R.id.sahanaSMS:
			// intent.setClass(this, SahanaSMSActivity.class);
			// startActivity(intent);
			// break;
			}
		}
	}

	/**
	 * Creates the menu options for the PositMain screen. Menu items are
	 * inflated from a resource file.
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.positmain_menu, menu);
		return true;
	}

	/**
	 * Updates the RWG Start/End menus based on whether RWG is running or not.
	 * 
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		int trackerState = sp
				.getInt(BackgroundTrackerActivity.SHARED_STATE, -1);
		/*
		 * TODO should be more like
		 * "is RWG running and is RWG enabled in the settings" /* if
		 * (RWGService.isRunning()) {
		 * menu.findItem(R.id.rwg_start).setEnabled(false);
		 * menu.findItem(R.id.rwg_end).setEnabled(true); } else {
		 * menu.findItem(R.id.rwg_start).setEnabled(true);
		 * menu.findItem(R.id.rwg_end).setEnabled(false); }
		 */

		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Manages the selection of menu items.
	 * 
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings_menu_item:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case R.id.about_menu_item:
			startActivity(new Intent(this, AboutActivity.class));
			break;
		case R.id.tutorial_menu_item:
			startActivity(new Intent(this, TutorialActivity.class));
			break;
		case R.id.projects_menu_item:
			startActivity(new Intent(this, ShowProjectsActivity.class));
			break;
		case R.id.track_menu_item:
			startActivity(new Intent(this, BackgroundTrackerActivity.class));
			break;
		case R.id.rwg_start:
			wifiManager = (WifiManager) this
					.getSystemService(Context.WIFI_SERVICE);
			// mAdhocClient = new AdhocClient(this);
			rwg = new Intent(this, RWGService.class);
			// rwgService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			RWGService.setActivity(this);

			startService(rwg);
			break;
		case R.id.rwg_end:
			if (RWGService.isRunning() && rwg != null) // Kill RWG if already
				// running
				stopService(rwg);
			try {
				rwgService.killProcessRunning("./rwgexec");
			} catch (Exception e) {
				Log.e(TAG, e.getClass().toString(), e);
			}
			mNotificationManager.cancel(Utils.ADHOC_ON_ID);
			Utils.showToast(this, "RWG Service Stopped");
			break;
		}
		return true;
	}

	/**
	 * Returns True if the phone has an authentication key and False if it does
	 * not
	 */
	private boolean checkPhoneRegistrationAndInitialSync() {
		String AUTH_KEY = sp.getString("AUTHKEY", "");
		if (AUTH_KEY.equals("") || AUTH_KEY.equals(null)) {
			return false;
		} else
			return true;
	}

	/**
	 * Intercepts the back key (KEYCODE_BACK) and displays a confirmation dialog
	 * when the user tries to exit POSIT.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// REMOVED: To allow navigating back to Tracker
		// if(keyCode==KeyEvent.KEYCODE_BACK){
		// showDialog(confirm_exit);
		// return true;
		// }
		// Log.i("code", keyCode+"");
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	/**
	 * Creates a dialog to confirm that the user wants to exit POSIT.
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case CONFIRM_EXIT:
			return new AlertDialog.Builder(this).setIcon(
					R.drawable.alert_dialog_icon).setTitle(R.string.exit)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// User clicked OK so do some stuff
									finish();
								}
							}).setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* User clicked Cancel so do nothing */
								}
							}).create();

		default:
			return null;
		}
	}

	/**
	 * Makes sure RWG is stopped before exiting the Activity
	 * 
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		if (RWGService.isRunning() && rwg != null) {// Kill RWG if already
			// running
			stopService(rwg);

			try {
				rwgService.killProcessRunning("./rwgexec");
				Utils.showToast(this, "RWG Service Stopped");
			} catch (Exception e) {
				Log.e(TAG, e.getClass().toString(), e);
			}

			mNotificationManager.cancel(Utils.ADHOC_ON_ID);
		}
		super.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Intent svc = new Intent(this, SyncService.class);
		// stopService(svc);
	}
}