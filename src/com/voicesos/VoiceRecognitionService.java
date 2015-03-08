package com.voicesos;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


public class VoiceRecognitionService extends Service implements RecognitionListener{
	private static String LOG_TAG;
	private SpeechRecognizer speech = null;
	private Intent recognizerIntent;
	private CountDownTimer mTimer;
	// Defines a custom Intent action
    public static final String BROADCAST_ACTION =
        "com.voicesos.BROADCAST";
 
    // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_DATA_STATUS =
        "com.voicesos.STATUS";
 
    private String message="help me";
    private String voiceSOS="help";
    private String phoneNumber="";
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
    	VoiceRecognitionService getService() {
            return VoiceRecognitionService.this;
        }
    }

    public VoiceRecognitionService() {
    	this(LOG_TAG);
    }

    public VoiceRecognitionService(String name) {
		super();
		LOG_TAG = name;
	}

	public void close(){
		Log.i(LOG_TAG, "destroy");
		if (speech != null) {
			speech.stopListening();
			speech.destroy();
			speech = null;
		}
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		Log.i(LOG_TAG, "Ready for speech");		
	}

	@Override
	public void onBeginningOfSpeech() {
		Log.i(LOG_TAG, "Beginning of speech");		
	}

	@Override
	public void onRmsChanged(float rmsdB) {
	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		
	}

	@Override
	public void onEndOfSpeech() {
		Log.i(LOG_TAG, "End of speech");		
	}

	@Override
	public void onError(int error) {
		Log.e(LOG_TAG, getErrorText(error));
		if(error ==SpeechRecognizer.ERROR_NO_MATCH)
			reStartListening();
	}

	@Override
	public void onResults(Bundle results) {
        //If the timer is available, cancel it so it doesn't interrupt our result processing
        if(mTimer != null){
            mTimer.cancel();
        }
		Log.i(LOG_TAG, "onResults");
		ArrayList<String> matches = results
				.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		String text = "";
		for (String result : matches)
			text += result + "\n";
		Log.i(LOG_TAG,text);
		processResults(text);
		reStartListening();
	}

	private void processResults(String text){
		if(text == null || text.isEmpty())
			return;
		if(text.contains(voiceSOS))
		{
			SmsManager sms = SmsManager.getDefault();
	        try
	        {
	            sms.sendTextMessage(phoneNumber, getMy10DigitPhoneNumber(), message, null, null);
	        }
	        catch(IllegalArgumentException e)
	        {

	        }
		}
	}
	
	private String getMyPhoneNumber(){
	    TelephonyManager mTelephonyMgr;
	    mTelephonyMgr = (TelephonyManager)
	        getSystemService(Context.TELEPHONY_SERVICE); 
	    return mTelephonyMgr.getLine1Number();
	}

	private String getMy10DigitPhoneNumber(){
	    String s = getMyPhoneNumber();
	    if(s != null && !s.isEmpty())
	    	return s.substring(2);
	    else
	    	return s;
	}	
	private void reStartListening(){
        //Start listening again
        Log.d(LOG_TAG, "onResults: Restart Listening");
        speech.startListening(recognizerIntent);
        //Start a timer in case OnReadyForSpeech is never called back 
        Log.d(LOG_TAG, "onResults: Start a timer");
        if(mTimer == null) {
            mTimer = new CountDownTimer(2000, 500) {
                @Override
                public void onTick(long l) {
                }

                @Override
                public void onFinish() {
                    Log.d(LOG_TAG, "Timer.onFinish: Timer Finished, Restart recognizer");
                    speech.cancel();
                    speech.startListening(recognizerIntent);
                }
            };
        }
        mTimer.start();
	}
	@Override
	public void onPartialResults(Bundle partialResults) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		
	}		
	public static String getErrorText(int errorCode) {
		String message;
		switch (errorCode) {
		case SpeechRecognizer.ERROR_AUDIO:
			message = "Audio recording error";
			break;
		case SpeechRecognizer.ERROR_CLIENT:
			message = "Client side error";
			break;
		case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
			message = "Insufficient permissions";
			break;
		case SpeechRecognizer.ERROR_NETWORK:
			message = "Network error";
			break;
		case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
			message = "Network timeout";
			break;
		case SpeechRecognizer.ERROR_NO_MATCH:
			message = "No match";
			break;
		case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
			message = "RecognitionService busy";
			break;
		case SpeechRecognizer.ERROR_SERVER:
			message = "error from server";
			break;
		case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
			message = "No speech input";
			break;
		default:
			message = "Didn't understand, please try again.";
			break;
		}
		return message;
	}

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
        close();
     }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        phoneNumber  = intent.getStringExtra("phoneNumber");
        message = intent.getStringExtra("message");
        voiceSOS = intent.getStringExtra("voiceSOS");
        if(speech == null)
        	initializeSpeech();
        speech.startListening(recognizerIntent);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate()"); 
        initializeSpeech();
    }	
    
    private void initializeSpeech(){
    	Log.d(LOG_TAG, "initializing speech recognizer");
		speech = SpeechRecognizer.createSpeechRecognizer(this);
		speech.setRecognitionListener(this);
		recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
		 "en-us");
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
		this.getPackageName());
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
		RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }
}
