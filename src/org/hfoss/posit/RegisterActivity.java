package org.hfoss.posit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RegisterActivity extends Activity implements OnClickListener{

	private SharedPreferences sp;
	public final static int BACK_BUTTON = 12;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main_register);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		if(!sp.getString("AUTHKEY", "").equals(""))
			finish();
		Button login = (Button) findViewById(R.id.login);
		if (login != null)
			login.setOnClickListener(this);

		Button register = (Button) findViewById(R.id.register);
		if (register != null) {
			register.setOnClickListener(this);
		}
	}
	public void onResume(){
		super.onResume();
		if(!sp.getString("AUTHKEY", "").equals(""))
			finish();
	}
	
	public boolean onKeyDown (int keyCode, KeyEvent event){
		switch(keyCode){
			
		case KeyEvent.KEYCODE_BACK:	
			setResult(BACK_BUTTON, new Intent());
			finish();
			break;
		}
		return true;	
	}
	
	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.login:
			intent.setClass(this, RegisterPhoneActivity.class);
			startActivity(intent);
			break;
		case R.id.register:
			intent.setClass(this, RegisterPhoneActivity.class);
			intent.putExtra("regUser", true);
			startActivity(intent);
			break;
			// case R.id.sahanaSMS:
			// intent.setClass(this, SahanaSMSActivity.class);
			// startActivity(intent);
			// break;
		}
		
	}

}
