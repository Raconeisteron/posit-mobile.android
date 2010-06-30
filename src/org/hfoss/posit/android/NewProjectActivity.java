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