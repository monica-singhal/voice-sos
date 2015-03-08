package com.voicesos;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1) public class SetupActivity extends Activity {
	private Button mBtSet = null;
	private Button mBtBack = null;
	private EditText mPhoneNumber = null;
	private EditText mMessage = null;
	private EditText mVoiceSOS = null;
	
	private String phoneNumber="";
	private String message="help me";
	private String voiceSOS="help";
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup);
		mBtSet = (Button) (findViewById(R.id.btSet));
		mBtBack = (Button) (findViewById(R.id.btBack));
		mMessage = (EditText) (findViewById(R.id.message));
		mPhoneNumber = (EditText) (findViewById(R.id.phoneNumber));
		mVoiceSOS = (EditText) (findViewById(R.id.voice_sos));
		
		/* Get values from Intent */
        Intent intent = getIntent();
         
        phoneNumber  = intent.getStringExtra("phoneNumber");
        message = intent.getStringExtra("message");
        voiceSOS = intent.getStringExtra("voiceSOS");
        mPhoneNumber.setText(phoneNumber);
        mMessage.setText(message);
        mVoiceSOS.setText(voiceSOS);
		mBtSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				String inputStr = mPhoneNumber.getText().toString();
				if(inputStr.isEmpty()){
					Toast.makeText(getApplicationContext(), "Phone number cannot be blank", Toast.LENGTH_LONG).show();
					return;
				}
				phoneNumber = inputStr;
				inputStr = mMessage.getText().toString();
				if(inputStr.isEmpty()){
					Toast.makeText(getApplicationContext(), "Message field is blank, selecting default: "+message, Toast.LENGTH_LONG).show();
				}
				else
					message = inputStr;
				inputStr = mVoiceSOS.getText().toString();
				if(inputStr.isEmpty()){
					Toast.makeText(getApplicationContext(), "SOS call text is blank, selecting default: "+voiceSOS, Toast.LENGTH_LONG).show();
				}
				else
					voiceSOS = inputStr;
			    i.putExtra("phoneNumber", phoneNumber);
				i.putExtra("message", message);
				i.putExtra("voiceSOS", voiceSOS);
				// Setting resultCode to 100 to identify on old activity
				setResult(100,i);	
				finish();
			}
		});
		mBtBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}


