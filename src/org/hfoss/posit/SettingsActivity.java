/*
 * File: SettingsActivity.java
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
package org.hfoss.posit;

import org.hfoss.posit.utilities.Utils;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Offers the user various options on how things should work in POSIT. The user
 * can choose whether or not they want automatic syncing to be on and whether
 * they want notifications about syncing. The user can also register their phone
 * from this screen in case they need to register with a different web server.
 * Lastly, the user can also set their group size should they need to be in ad
 * hoc mode.
 * 
 * 
 */
public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener, OnSharedPreferenceChangeListener {
	private static final String TAG = "SettingsActivity";
	protected static final int BARCODE_READER = 0;
	private String server;
	private Preference serverAddress;
	private Preference project;
	private Preference user;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.posit_preferences);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(this);
		
		server = sp.getString("SERVER_ADDRESS", "");
		String email = sp.getString("EMAIL","");
		String projectName = sp.getString("PROJECT_NAME","");
		
		Preference regUser = this.findPreference("regUser");
		Preference regDevice = this.findPreference("regDevice");
		user = this.findPreference("EMAIL");
		project = this.findPreference("PROJECT_NAME");	
		serverAddress = this.findPreference("SERVER_ADDRESS");
		
		if (server != null && serverAddress != null) {
			serverAddress.setSummary(server); 
			serverAddress.setOnPreferenceClickListener(this);
		}
		if (email != null && user != null){
			user.setSummary(email);
			user.setOnPreferenceClickListener(this);
		}
		if (projectName != null && project != null) {
			project.setSummary(projectName);
			project.setOnPreferenceClickListener(this);
		}
			
		regUser.setOnPreferenceClickListener(this);
		regDevice.setOnPreferenceClickListener(this);
	}
	

	@Override
	public boolean onPreferenceClick(Preference preference) {

		if(preference.getTitle().toString().equals("Add a phone")){
			Intent i = new Intent(this, RegisterPhoneActivity.class);
			startActivity(i);
		}
		if(preference.getTitle().toString().equals("Create an account")){
			Intent intent = new Intent(this, RegisterPhoneActivity.class);
			intent.setClass(this, RegisterPhoneActivity.class);
			intent.putExtra("regUser", true);
			startActivity(intent);
		}
		if(preference.getTitle().toString().equals("Current server")){
			if (preference instanceof EditTextPreference) {
				EditTextPreference textPreference = (EditTextPreference) preference;
				EditText eText =  textPreference.getEditText();
				eText.setText(server);
			}
		}
		if(preference.getTitle().toString().equals("Current user")){
			Intent i = new Intent(this, RegisterPhoneActivity.class);
			startActivity(i);
		}
		if(preference.getTitle().toString().equals("Current project")){
			Intent i = new Intent(this, ShowProjectsActivity.class);
			startActivity(i);
		}
		
		return false;
	}
	
	 public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		 if (key.equals("SERVER_ADDRESS")){
			server = sp.getString("SERVER_ADDRESS", "");
			if (server != null) 
				serverAddress.setSummary(server); 
		 }
		 else if (key.equals("PROJECT_NAME")){
				String projectName = sp.getString("PROJECT_NAME", "");
				if (projectName != null) 
					project.setSummary(projectName); 
		 }
		 else if (key.equals("EMAIL")){
				String email = sp.getString("EMAIL", "");
				if (email != null) 
					user.setSummary(email); 
		 }
	 }

}