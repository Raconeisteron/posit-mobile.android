/*
 * File: RegisterActivity.java
 *
 * Copyright (C) 2010 The Humanitarian FOSS Project (http://www.hfoss.org)
 *
 * This file is part of POSIT, Portable Open Search and Identification Tool.
 *
 * POSIT is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as publishe
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

import java.util.List;

import org.apache.commons.validator.EmailValidator;
import org.hfoss.posit.android.utilities.Utils;
import org.hfoss.posit.android.web.Communicator;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Handles both user registration and phone registration. In order to use POSIT
 * effectively a user should have a valid account on a POSIT server. They can create a new
 * account from their phone or by visiting the server (Default = posit.hfoss.org).
 * If they have an existing account, then the phone must be registered with the
 * server.  A properly registered phone receives an authKey.
 *
 * Various preference settings are stored in SharedPreferences and carry over from
 * one use of POSIT to another. These include SERVER_ADDRESS, EMAIL, AUTHKEY, PROJECT.
 *
 */
public class RegisterActivity extends Activity implements OnClickListener{

	private static final String TAG = "RegisterActivity";
	private static final int LOGIN_BY_BARCODE_READER = 0;
	private static final int CONFIRM_EXIT = 1;
	private static final int PROMPT_REGISTRATION = 0;

	public static final String REGISTER_USER = "RegisterUser";
	public static final String REGISTER_PHONE = "RegisterPhone";


	private SharedPreferences mSharedPrefs;
	private ProgressDialog mProgressDialog;

	private Button mRegisterUsingBarcodeButton;
	private Button mRegisterUsingDeviceButton;
	private Button mRegisterButton;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG,"Registration Activity");

		// If the authKey is set, the user is already register, but what if
		// they want to create a new account??
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(!mSharedPrefs.getString("AUTHKEY", "").equals(""))
			finish();
		
		setContentView(R.layout.main_register);

		// Register existing user button
		Button register = (Button) findViewById(R.id.register);
		if (register != null) {
			register.setOnClickListener(this);
		}

		// Create new user button
		Button login = (Button) findViewById(R.id.login);
		if (login != null)
			login.setOnClickListener(this);

		// This activity is started with specific actions by the SettingsActivity
		Intent intent = getIntent();
		String action = intent.getAction();

		if (action != null) {
			if (action.equals(REGISTER_USER))
				createNewUserAccount();
			else if (action.equals(REGISTER_PHONE))
				registerExistingAccount();
		}
		else
			showDialog(PROMPT_REGISTRATION);
	}

	public void onResume(){
		super.onResume();
//		if(!sp.getString("AUTHKEY", "").equals(""))
//			finish();
	}
	
	/**
	 * Handle all button clicks. There are two main buttons that appear on the View
	 * when the Activity is started.  When one of those buttons is clicked, a new
	 * View is displayed with one or more additional buttons.
	 */
	public void onClick(View v) {

		if (!Utils.isNetworkAvailable(this)) {
			Utils.showToast(this,"There's a problem. To register you must be on a network.");
			return;
		}

		Intent intent;

		switch (v.getId()) {

		// Register phone for an existing account
		case R.id.register:
			//registerExistingAccount();
			createNewUserAccount();
		break;

		// Create a new user account
		case R.id.login:
			//createNewUserAccount();
			registerExistingAccount();
			intent = new Intent(this, RegisterPhoneActivity.class);
			//intent.setClass(this, RegisterPhoneActivity.class);
			intent.putExtra("regUser", false);
			startActivity(intent);
			break;

		// Register the phone from the phone by providing valid email and password
		case R.id.registerDeviceButton:
			String password = (((TextView) findViewById(R.id.password)).getText()).toString();
			String email = (((TextView) findViewById(R.id.email)).getText()).toString();
			if (password.equals("") || email.equals("")) {
				Utils.showToast(this, "Please fill in all the fields");
				break;
			}
			EmailValidator emailValidator = EmailValidator.getInstance();
			if (emailValidator.isValid(email) != true) {
				Utils.showToast(this, "Please enter a valid address");
				break;
			}
			loginUser(email, password);
			break;

		// Register the phone by reading a barcode on the server's website (Settings > Register)
		case R.id.registerUsingBarcodeButton:
			if (!isIntentAvailable(this, "com.google.zxing.client.android.SCAN")) {
				Utils.showToast(this,  "Please install the Zxing Barcode Scanner from the Market");
				break;
			}
			intent = new Intent("com.google.zxing.client.android.SCAN");
			try {
				startActivityForResult(intent, LOGIN_BY_BARCODE_READER);
			} catch (ActivityNotFoundException e) {
				if (Utils.debug)
					Log.i(TAG, e.toString());
			}
			break;

		// User clicks the "Login" button in the Create User View
		case (R.id.submitInfo):
			Intent i = getIntent();
			password = (((TextView) findViewById(R.id.password)).getText()).toString();
			String check = (((TextView) findViewById(R.id.passCheck)).getText()).toString();
			email = (((TextView) findViewById(R.id.email)).getText()).toString();
			String lastname = (((TextView) findViewById(R.id.lastName)).getText()).toString();
			String firstname = (((TextView) findViewById(R.id.firstName)).getText()).toString();
			if (password.equals("") || check.equals("") || lastname.equals("")
					|| firstname.equals("") || email.equals("")) {
				Utils.showToast(this,"Please fill in all the fields");
				break;
			}

			EmailValidator emV = EmailValidator.getInstance();
			if (emV.isValid(email) != true) {
				Utils.showToast(this, "Please enter a valid email address");
				break;
			}
			if (!check.equals(password)) {
				Utils.showToast(this,"Your passwords do not match");
				break;
			}

			TelephonyManager manager = (TelephonyManager) this
					.getSystemService(Context.TELEPHONY_SERVICE);
			String imei = manager.getDeviceId();

			Communicator com = new Communicator(this);

			String server = mSharedPrefs.getString("SERVER_ADDRESS", getString(R.string.defaultServer));

			String result = com.registerUser(server, firstname, lastname,
					email, password, check, imei);
			Log.i(TAG, "RegisterUser result = " + result);
			if (result != null) {
				String[] message = result.split(":");
				if (message.length != 2) {
					Utils.showToast(this, "Malformed message");
					break;
				}
				// A new account has successfully been created.
				if (message[0].equals("" + Constants.AUTHN_OK)) {
					Editor editor = mSharedPrefs.edit();
					editor.putString("EMAIL", email);
					editor.commit();

					// The user logs in to register the device.
					loginUser(email, password);

				} else {
					Utils.showToast(this, message[1]);
				}

			}
			mProgressDialog.dismiss();

			// case R.id.sahanaSMS:
			// intent.setClass(this, SahanaSMSActivity.class);
			// startActivity(intent);
			// break;
		}
	}

	private void createNewUserAccount() {
		Log.i(TAG,"Creating new user");
		setContentView(R.layout.registeruser);
		String server = mSharedPrefs.getString("SERVER_ADDRESS", "");
		String email = mSharedPrefs.getString("EMAIL", "");
		((TextView) findViewById(R.id.serverName)).setText(server);
		((TextView) findViewById(R.id.email)).setText(email);
		mRegisterButton = (Button) findViewById(R.id.submitInfo);
		mRegisterButton.setOnClickListener(this);
	}


	/**
	 * Handles server registration by decoding the JSON Object that the barcode
	 * reader gets from the server site containing the server address and the
	 * authentication key. These two pieces of information are stored as shared
	 * preferences. The user is then prompted to choose a project from the
	 * server to work on and sync with.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "Requestcode = " + requestCode + "Result code = " + resultCode);
		super.onActivityResult(requestCode, resultCode, data);
//		if (resultCode == RESULT_CANCELED)
//			return;
//		if(resultCode == RegisterUserActivity.BACK_BUTTON){
//			finish();
//			return;
//		}
		switch (requestCode) {
		case LOGIN_BY_BARCODE_READER:
			String value = data.getStringExtra("SCAN_RESULT");
			Log.i(TAG,"Bar code scanner result " + value);

			// Hack to remove extra escape characters from JSON text.
			StringBuffer sb = new StringBuffer("");
			for (int k = 0; k < value.length(); k++) {
				char ch = value.charAt(k);
				if (ch != '\\') {
					sb.append(ch);
				} else if (value.charAt(k + 1) == '\\') {
					sb.append(ch);
				}
			}
			value = sb.toString(); // Valid JSON encoded string
			// End of Hack

			JSONObject object;
			JSONTokener tokener = new JSONTokener(value);

			try {
				Log.i(TAG, "JSON=" + value);

				object = new JSONObject(value);
				String server = object.getString("server");
				String authKey = object.getString("authKey");
				if (Utils.debug)
					Log.i(TAG, "server= " + server + ", authKey= " + authKey);
				TelephonyManager manager = (TelephonyManager) this
						.getSystemService(Context.TELEPHONY_SERVICE);
				String imei = manager.getDeviceId();
				Communicator communicator = new Communicator(this);
				mProgressDialog = ProgressDialog.show(this, "Registering device",
						"Please wait.", true, true);
				try {
					String registered = communicator.registerDevice(server, authKey, imei);

					if (registered != null) {
						Log.i(TAG, "registered");
						Editor spEditor = mSharedPrefs.edit();

						spEditor.putString("SERVER_ADDRESS", server);
						spEditor.putString("AUTHKEY", authKey);
						spEditor.putInt("PROJECT_ID", 0);
						spEditor.putString("EMAIL", email);      // Should be in barcode?
						spEditor.putString("PASSWORD", password);
						spEditor.putString("PROJECT_NAME", "");
						spEditor.commit();

						Intent intent = new Intent(this, ShowProjectsActivity.class);
						startActivity(intent);
					}
				} catch (NullPointerException e) {
					Utils.showToast(this, "Registration Error");
				}

				mProgressDialog.dismiss();
				int projectId = mSharedPrefs.getInt("PROJECT_ID", 0);
				if (projectId == 0) {
					Intent intent = new Intent(this, ShowProjectsActivity.class);
					startActivity(intent);
				}
				finish();

			} catch (JSONException e) {
				if (Utils.debug)
					Log.e(TAG, e.toString());
			}
			break
		}
	}


	/**
	 * For a user with an existing account on the server, this method will register
	 * the phone, resulting in an authKey being sent to the phone by the server and
	 * saved in SharedPreferences.
	 */
	private void registerExistingAccount() {
		Log.i(TAG,"Creating new user");

		setContentView(R.layout.registerphone);

		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String serverName = mSharedPrefs.getString("SERVER_ADDRESS", "");
		Log.i(TAG,"Server = " + serverName);
//		if (mServerName != null)
		((TextView) findViewById(R.id.serverName)).setText(serverName);

		String email = mSharedPrefs.getString("EMAIL", "");
		((TextView) findViewById(R.id.email)).setText(email);

		mRegisterUsingBarcodeButton = (Button) findViewById(R.id.registerUsingBarcodeButton);
		mRegisterUsingBarcodeButton.setOnClickListener(this);

		mRegisterUsingDeviceButton = (Button) findViewById(R.id.registerDeviceButton);
		mRegisterUsingDeviceButton.setOnClickListener(this);
	}

	/**
	 * Logs the user onto the server, give the user's email and password. If
	 * the user can successfully login, the phone receives an AUTHKEY, which is
	 * saved in SharedPreferences.
	 * Actually, this method makes two HTTP requests to the server, one to login
	 * the user and a second to register the user's phone.
	 * @param email email account user is using to register with a given server
	 * @param password password used to register and sign in to a server
	 */
	private void loginUser(String email, String password) {
		mProgressDialog = ProgressDialog.show(this, "Registering device",
				"Please wait.", true, true);
		String serverName = mSharedPrefs.getString("SERVER_ADDRESS", "");
		Communicator com = new Communicator(this);
		TelephonyManager manager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = manager.getDeviceId();

		// First login the user.
		String result = com.loginUser(serverName, email, password, imei);

		Log.i(TAG, "loginUser result: " + result);
		String authKey;
		if (null==result){
			Utils.showToast(this, "Failed to get authentication key from server.");
			return;
		}
		//TODO this is still little uglyish
		String[] message = result.split(":");
		if (message.length != 2){
			Utils.showToast(this, "Malformed message");
			return;
		}
		// Successfully logged in
		if (message[0].equals(""+Constants.AUTHN_OK)){
			authKey = message[1];
			Log.i(TAG, "AuthKey "+ authKey +" obtained, registering device");

			// Here we register the device
			String responseString = com.registerDevice(serverName, authKey, imei);

			if (responseString.equals("true")){
				Editor spEditor = mSharedPrefs.edit();
				spEditor.putString("AUTHKEY", authKey); // Remember the AUTHKEY
				spEditor.putString("EMAIL", email); // Remember the userID
				spEditor.commit();

				Intent intent = new Intent(this, ShowProjectsActivity.class);
				startActivity(intent);

				Utils.showToast(this, "Successfully logged in.");
				setResult(PositMain.LOGIN_SUCCESSFUL);
				finish();
			}
		}else {
			Utils.showToast(this, message[1]);
			return;
		}
		mProgressDialog.dismiss();
	}

	/**
	 * This method is used to check whether or not the user has an intent
	 * available before an activity is actually started. In this case we
	 * check to see whether the barcode scanner is available. Since the
	 * barcode scanner requires a downloadable dependency, the user cannot
	 * be allowed to click the "Read Barcode" button unless the phone is able
	 * to do so.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

//	/**
//	 * Intercepts the back key (KEYCODE_BACK) and displays a confirmation dialog
//	 * when the user tries to exit POSIT.
//	 */
//	 @Override
//	 public boolean onKeyDown(int keyCode, KeyEvent event) {
//	 	if(keyCode==KeyEvent.KEYCODE_BACK){
//	 		showDialog(CONFIRM_EXIT);
//	 		return true;
//	 	}
//	 	Log.i("code", keyCode+"");
//	 	return super.onKeyDown(keyCode, event);
//	 }
//
//
	/**
	 * Creates a dialog to confirm that the user wants to exit POSIT.
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROMPT_REGISTRATION:
			return new AlertDialog.Builder(this)
				.setIcon(R.drawable.icon)
				.setMessage("To effectively use POSIT " +
							" you should register with a POSIT server. " +
							" Either create an account on " +
							getString(R.string.defaultServer) + " or create a new account.")
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// User clicked OK so do some stuff
						}
					})
					.create();

		default:
			return null;
		}
	}

}
