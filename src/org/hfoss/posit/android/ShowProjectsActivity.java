/*
 * File: ShowProjectsActivity.java
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.hfoss.posit.android.utilities.Utils;
import org.hfoss.posit.android.web.Communicator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;

/**
 * This activity shows a list of all the projects on the server that the phone is registered with,
 * and allows the user to pick one from the list.  When the user picks one, the phone automatically
 * syncs with the server to get all the finds from that project
 * 
 *
 */
public class ShowProjectsActivity extends ListActivity implements OnClickListener{

	private static final String TAG = "ShowProjectsActivity";
	private static final int CONFIRM_PROJECT_CHANGE = 0;
	static final int NEW_PROJECT = 1;
	private int mClickedPosition = 0;

	private ArrayList<HashMap<String, Object>> projectList;
	private RadioGroup mRadio;	

	/**
	 * Called when the activity is first started.  Shows a list of 
	 * radio buttons, each representing
	 * a different project on the server.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_proj);
		Button addProjectButton = (Button)findViewById(R.id.idAddProjButton);
		addProjectButton.setOnClickListener(this);

		showProjects();
		

	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
	//	tryToRegister();
	}

	private void showProjects() {
		if (!Utils.isConnected(this)) {
			reportNetworkError("No Network connection ... exiting");
			return;
		} 
		Communicator comm = new Communicator(this);
		try{
			projectList = comm.getProjects();
		} catch(Exception e){
			Log.i(TAG, "Communicator error " + e.getMessage());
			e.printStackTrace();
			this.reportNetworkError(e.getMessage());
			finish();
		}
		if (projectList != null) {
			Iterator<HashMap<String, Object>> it = projectList.iterator();
			ArrayList<String> projList = new ArrayList<String>();
			for(int i = 0; it.hasNext(); i++) {
				HashMap<String,Object> next = it.next();
				projList.add((String)(next.get("name")));
			}
			setListAdapter(new ArrayAdapter<String>(this,
			          android.R.layout.simple_list_item_1, projList));
			
		} else {
			this.reportNetworkError("Null project list returned.\nMake sure your server is reachable.");
		}
	} 
	

	
	/**
	 * Reports as much information as it can about the error.
	 * @param str
	 */
	private void reportNetworkError(String str) {
		Log.i(TAG, "Registration Failed: " + str);
		Utils.showToast(this, "Registration Failed: " + str);
		finish();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == NEW_PROJECT)
			showProjects();
	}

	public void onListItemClick(ListView lv, View v, int position, long idFull){
		mClickedPosition = position;
		ArrayAdapter mAdapter = (ArrayAdapter) this.getListAdapter();
		String projectId = (String) projectList.get(mClickedPosition).get("id");
		int id  = Integer.parseInt(projectId);
		String projectName = (String) projectList.get(mClickedPosition).get("name");
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		int currentProjectId = sp.getInt("PROJECT_ID",0);
		
		if (id == currentProjectId){
			finish();
			return;
		}
		
		Editor editor = sp.edit();

		editor.putInt("PROJECT_ID", id);
		editor.putString("PROJECT_NAME", projectName);
		editor.commit();

		showDialog(CONFIRM_PROJECT_CHANGE);
	}
	/**
	 * Called when the user clicks on a project in the list.  Sets the project id in the shared
	 * preferences so it can be remembered when the application is closed
	 */
	public void onClick(View v) {
		Intent i = new Intent(this, NewProjectActivity.class);;
		switch (v.getId()) {

		case R.id.idAddProjButton:
			startActivityForResult(i,NEW_PROJECT);

			break;

			
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 * Confirms with the user that they have changed their project and automatically syncs with the server
	 * to get all the project finds
	 */
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case CONFIRM_PROJECT_CHANGE:
			return new AlertDialog.Builder(this)
			.setIcon(R.drawable.icon)
			.setTitle("You have changed your project to: " 
					+ (String) projectList.get(mClickedPosition).get("name"))
					.setPositiveButton(R.string.alert_dialog_ok, 
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
									ShowProjectsActivity.this);
							
							boolean syncIsOn = sp.getBoolean("SYNC_ON_OFF", true);
							if (syncIsOn) {
								Intent intent = new Intent(ShowProjectsActivity.this, SyncActivity.class);
								intent.setAction(Intent.ACTION_SYNC);
								startActivity(intent);
							}
							finish();
						}
					}).create();
		default:
			return null;
		}
	}



}