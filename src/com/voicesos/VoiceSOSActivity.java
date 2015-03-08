package com.voicesos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class VoiceSOSActivity extends Activity{

	private static final String BROADCAST_ACTION = "com.voicesos.BROADCAST";
	private ToggleButton toggleButton;
//	private TextView returnedText;
	private Intent mServiceIntent;
	private String phoneNumber="";
	private String message="help me";
	private String voiceSOS="help";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		load();
		toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);
//		returnedText = (TextView) findViewById(R.id.textView1);
        // The filter's action is BROADCAST_ACTION
        IntentFilter mStatusIntentFilter = new IntentFilter(
                BROADCAST_ACTION);
        // Adds a data filter for the HTTP scheme
        mStatusIntentFilter.addDataScheme("http");

//        // Instantiates a new DownloadStateReceiver
//        DownloadStateReceiver mDownloadStateReceiver =
//                new DownloadStateReceiver();
//        // Registers the DownloadStateReceiver and its intent filters
//        LocalBroadcastManager.getInstance(this).registerReceiver(
//                mDownloadStateReceiver,
//                mStatusIntentFilter);
//
		mServiceIntent = new Intent(this, VoiceRecognitionService.class);

		toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					Toast.makeText(getApplicationContext(),"ready to start service: "+phoneNumber, Toast.LENGTH_LONG).show();
					if(phoneNumber.isEmpty()){
						Toast.makeText(getApplicationContext(),"Phone number cannot be blank, please select a valid phone number", Toast.LENGTH_LONG).show();
						return;
					}
					mServiceIntent.putExtra("phoneNumber", phoneNumber);
					mServiceIntent.putExtra("message",message);
					mServiceIntent.putExtra("voiceSOS",voiceSOS);
					getApplicationContext().startService(mServiceIntent);	
				} else {
					getApplicationContext().stopService(mServiceIntent);
				}				
			}
		});		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent i = new Intent(getApplicationContext(), SetupActivity.class);
			i.putExtra("phoneNumber", phoneNumber);
			i.putExtra("message",message);
			i.putExtra("voiceSOS",voiceSOS);
			startActivityForResult(i, 100); // 100 is some code to identify the returning result
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
    protected void onActivityResult(int requestCode,
                                     int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 100){
         // Storing result in a variable called myvar
         // get("option") 'option' is the key value result data
        	message = data.getStringExtra("message");
        	phoneNumber = data.getStringExtra("phoneNumber");
        	voiceSOS = data.getStringExtra("voiceSOS");
    		save();
        }
    }	

	private void save() {
	    SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = sharedPreferences.edit();
	    editor.putString("message", message);
	    editor.putString("phoneNumber", phoneNumber);
	    editor.putString("voiceSOS", voiceSOS);
	    editor.commit();
	}

	private void load() { 
	    SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
	    message = sharedPreferences.getString("message","help");
	    phoneNumber = sharedPreferences.getString("phoneNumber","");
	    voiceSOS = sharedPreferences.getString("voiceSOS","help");
	}	
	
	// Broadcast receiver for receiving status updates from the IntentService
//	private class DownloadStateReceiver extends BroadcastReceiver
//	{
//	    // Prevents instantiation
//	    private DownloadStateReceiver() {
//	    }
//	    // Called when the BroadcastReceiver gets an Intent it's registered to receive
//	    @Override
//	    public void onReceive(Context context, Intent intent) {
//	    	if(intent.getAction().equals(BROADCAST_ACTION)){
//	    		String data = intent.getDataString();
//	    		if(data != null && !data.isEmpty())
//	    		{
//	    			returnedText.setText(intent.getDataString());
//	    		}
//	    	}
//	    }
//	}

}
