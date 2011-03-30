/*
 * File: RegisterPhoneActivity.java
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

import java.util.List;

import org.apache.commons.validator.EmailValidator;
import org.apache.commons.validator.UrlValidator;
import org.hfoss.posit.android.utilities.Utils;
import org.hfoss.posit.android.web.Communicator;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Prompts the user to register their phone if the phone is not registered, or
 * shows the phone's current registration status and allows the user to register
 * their phone again with a different server.
 * 
 * 
 */
public class RegisterPhoneActivity extends Activity implements OnClickListener {

	private static final int LOGIN_BY_BARCODE_READER = 0;
	private static final int LOGIN_BY_EMAIL_PASSWORD = 1;

	private static final String TAG = "RegisterPhoneActivity";
	public boolean isSandbox = false;
	public boolean readerInstalled = false;
//	private Button registerUserButton;
//	private Button editServer;
	private Button registerUsingBarcodeButton;
	private Button registerDeviceButton;
	private SharedPreferences mSharedPrefs;
	private ProgressDialog mProgressDialog;
//	private Dialog mServerDialog;
	private String mServerName;
	
	/**
	 * Called when the Activity is first started. If the phone is not
	 * registered, tells the user so and gives the user instructions on how to
	 * register the phone. If the phone is registered, tells the user the server
	 * address that the phone is registered to in case the user would like to
	 * change it.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
  
		setContentView(R.layout.registerphone);

		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mServerName = mSharedPrefs.getString("SERVER_ADDRESS", "");
		Log.i(TAG,"Server = " + mServerName);
//		if (mServerName != null) 
		((TextView) findViewById(R.id.serverName)).setText(mServerName);
		
//		mServerDialog = new Dialog(this);
//		String server = sp.getString("SERVER_ADDRESS", null);
//		mServerName = sp.getString("SERVER_ADDRESS", "http://turing.cs.trincoll.edu/~pgautam/positweb");

		String email = mSharedPrefs.getString("EMAIL", "");
		((TextView) findViewById(R.id.email)).setText(email);

					
//		if(email != null)
		
		if (isIntentAvailable(this, "com.google.zxing.client.android.SCAN")) {
			readerInstalled = true;
		}
		
		Intent regI = getIntent();
		if(regI.getBooleanExtra("regUser", false)&&!regI.getBooleanExtra("regComplete", false)){
			Intent i = new Intent(this, RegisterUserActivity.class);
//			i.putExtra("server", (((TextView) findViewById(R.id.serverName))
//					.getText()).toString());
			i.putExtra("email", (((TextView) findViewById(R.id.email))
					.getText()).toString());
			regI.putExtra("regComplete", true);
			this.startActivityForResult(i, LOGIN_BY_EMAIL_PASSWORD);
		}

//		registerUserButton = (Button) findViewById(R.id.createaccount);
//		editServer = (Button) findViewById(R.id.editServer);
		registerUsingBarcodeButton = (Button) findViewById(R.id.registerUsingBarcodeButton);
		registerDeviceButton = (Button) findViewById(R.id.registerDeviceButton);

//		editServer.setOnClickListener(this);
		registerUsingBarcodeButton.setOnClickListener(this);
		registerDeviceButton.setOnClickListener(this);
//		registerUserButton.setOnClickListener(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			setResult(PositMain.LOGIN_CANCELED);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}


	/**
	 * This method is used to check whether or not the user has an intent
	 * available before an activitythis is actually started. This is only invoked on
	 * the register view to check whether or not the intent for the barcode
	 * scanner is available. Since the barcode scanner requires a downloadable
	 * dependency, the user will not be allowed to click the "Read Barcode"
	 * button unless the phone is able to do so.
	 * 
	 * @param context
	 * @param action
	 * @return
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
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
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED)
			return;
		if(resultCode == RegisterUserActivity.BACK_BUTTON){
			finish();
			return;
		}
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
						Editor spEditor = mSharedPrefs.edit();


						spEditor.putString("SERVER_ADDRESS", server);
						spEditor.putString("AUTHKEY", authKey);
						spEditor.putInt("PROJECT_ID", 0);
//						spEditor.putString("EMAIL", email);      // Should be in barcode?
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
			break;
		case LOGIN_BY_EMAIL_PASSWORD:
			String email = data.getStringExtra("email");
			String password = data.getStringExtra("password");
//			String serverName = (((TextView) findViewById(R.id.serverName))
//					.getText()).toString();
//			loginUser(serverName, email, password);
			loginUser(mServerName, email, password);  
			break;
		}
	}
/**
 *  Handles when user clicks on one of the buttons: more, register device, register using barcode,
 *   or create account 
 * 
 */
	public void onClick(View v) {
		
		
//		String serverName = (((TextView) findViewById(R.id.serverName))
//				.getText()).toString();
		EmailValidator emV = EmailValidator.getInstance();
		UrlValidator urV = new UrlValidator();

		switch (v.getId()) {
		
//		case R.id.editServer:
//			mServerDialog.setContentView(R.layout.edit_server_dialog);
//			mServerDialog.setTitle("Change Server");
//			TextView text = (TextView) mServerDialog.findViewById(R.id.editServerMessage);
//			text.setText("Please enter the name of the server");
//			EditText eText = (EditText) mServerDialog.findViewById(R.id.newServer);
//			eText.setText("http://");
//			Button blar = (Button) mServerDialog.findViewById(R.id.confirmChange);
//			blar.setOnClickListener(this);
//			mServerDialog.show();
//			break;
//		case R.id.confirmChange:
//			String server = ((EditText) mServerDialog.findViewById(R.id.newServer)).getText().toString();
//			((TextView) this.findViewById(R.id.serverName)).setText(server);
//			Editor editor = sp.edit();
//			editor.putString("SERVER_ADDRESS", server);
//			editor.commit();
//			mServerDialog.dismiss();
//			break;			
		case R.id.registerDeviceButton:
			String password = (((TextView) findViewById(R.id.password))
					.getText()).toString();
			String email = (((TextView) findViewById(R.id.email)).getText())
					.toString();

//			if (urV.isValid(serverName) != true) {
//				Utils.showToast(this, "Please enter a valid server URL");
//			}
			if (password.equals("") || email.equals("")) {
				Utils.showToast(this, "Please fill in all the fields");
				break;
			}
			if (emV.isValid(email) != true) {
				Utils.showToast(this, "Please enter a valid address");
				break;
			}
			
//			loginUser(serverName,email, password);
			loginUser(mServerName,email, password);
			
			
			break;

		case R.id.registerUsingBarcodeButton:
			if (!readerInstalled) {
				Utils.showToast(this,
						"Please install the Zxing Barcode Scanner");
				break;
			}
			if (!Utils.isNetworkAvailable(this)) {
				Utils
						.showToast(this,
								"Registration Error:No Network Available");
				break;
			}
			if (RegisterPhoneActivity.isIntentAvailable(
					RegisterPhoneActivity.this,
					"com.google.zxing.client.android.SCAN")) {

				Intent intent = new Intent(
						"com.google.zxing.client.android.SCAN");
				try {
					startActivityForResult(intent, LOGIN_BY_BARCODE_READER);
				} catch (ActivityNotFoundException e) {
					if (Utils.debug)
						Log.i(TAG, e.toString());
				}
			}

			break;

//		case (R.id.createaccount):
//
////			if (urV.isValid(serverName) != true) {
////				Utils.showToast(this, "Please enter a valid server URL");
////				break;
////			}
//			Intent i = new Intent(this, RegisterUserActivity.class);
//			i.putExtra("server", (((TextView) findViewById(R.id.serverName))
//					.getText()).toString());
//			i.putExtra("email", (((TextView) findViewById(R.id.email))
//					.getText()).toString());
//			this.startActivityForResult(i, CREATE_ACCOUNT);
//			break;

		}

	}

	
	/**
	 * Handles user logging in to the server. It is called when user clicks on 
	 * the register button on RegisterPhoneActivity
	 * @param serverName name of the server user is registering with
	 * @param email email account user is using to register with a given server
	 * @param password password used to register and sign in to a server
	 */
	private void loginUser(String serverName, String email, String password) {
		Communicator com = new Communicator(this);
		TelephonyManager manager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = manager.getDeviceId();

		
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
		if (message[0].equals(""+Constants.AUTHN_OK)){
			mProgressDialog = ProgressDialog.show(this, "Registering device",
					"Please wait.", true, true);
			authKey = message[1];
			Log.i(TAG, "AuthKey "+ authKey +" obtained, registering device");
			String responseString = com.registerDevice(serverName, authKey, imei);
			if (responseString.equals("true")){
				Editor spEditor = mSharedPrefs.edit();
				spEditor.putInt("PROJECT_ID", 0);
				spEditor.putString("SERVER_ADDRESS", serverName);
				spEditor.putString("EMAIL", email);
				spEditor.putString("PASSWORD", password);
				spEditor.putString("AUTHKEY", authKey);
				spEditor.putString("PROJECT_NAME", ""); 
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
}
