/*
 * File: NewProjectActivity.java
 * 
 * Copyright (C) 2010 The Humanitarian FOSS Project (http://www.hfoss.org)
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

import org.hfoss.posit.android.utilities.Utils;
import org.hfoss.posit.android.web.Communicator;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Creates a new project and registers it on the server. 
 * @author rmorelli
 *
 */
public class NewProjectActivity extends Activity implements OnClickListener{

	private Button mCreateProject;
	private static final String TAG = "NewProjectActivity";
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_project);
		mCreateProject = (Button) this.findViewById(R.id.createProject);
		mCreateProject.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.createProject:
			String projectName = (((TextView) findViewById(R.id.projectName)).getText()).toString();
			String projectDescription = (((TextView) findViewById(R.id.projectDescription)).getText()).toString(); 
			if(projectName.equals("")){
				Utils.showToast(this, "Please enter a name for your project");
				break;
			}
			SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(this);
			String authkey = prefManager.getString("AUTHKEY",null);
			String server = prefManager.getString("SERVER_ADDRESS", null);
			Communicator com = new Communicator(this);
			String response = com.createProject(server, projectName, projectDescription, authkey);
			Log.i(TAG,response);
			if(response.contains("success")){
				
				Utils.showToast(this, response);
				setResult(ShowProjectsActivity.NEW_PROJECT);
				finish();
			}
			else
				Utils.showToast(this, response);
			break;
		}
	}

}