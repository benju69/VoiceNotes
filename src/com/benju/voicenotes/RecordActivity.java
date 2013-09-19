package com.benju.voicenotes;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class RecordActivity extends SherlockActivity {
	private static String mFileName = null;
	private ImageButton recordButton;
	private MediaRecorder mRecorder = null;
	private boolean mStartRecording = true;	
	private TextView stateRecordTV;
	private TextView timeRecordTV;	
	private Handler mHandlerTime = new Handler();
	private long startTime = 0;
	
	String format3gp = ".3gp";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		getSupportActionBar().setLogo(R.drawable.ic_launcher);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		recordButton = (ImageButton)findViewById(R.id.RecordButton);
		recordButton.setOnClickListener(clickRecord);
		
		Typeface tf = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Light.ttf");
		
		// State
		stateRecordTV = (TextView)findViewById(R.id.TextViewState);
		stateRecordTV.setTypeface(tf, Typeface.ITALIC);
		
		// Time
		timeRecordTV = (TextView)findViewById(R.id.TextViewTime);
		timeRecordTV.setTypeface(tf);
		
		int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
		if(titleId == 0)
		    titleId = com.actionbarsherlock.R.id.abs__action_bar_title;
		TextView title = (TextView) findViewById(titleId);
		title.setTypeface(tf);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		/*if (mRecorder != null) {
			try {
				mRecorder.stop();
				mRecorder.release();
				mRecorder = null;
			} catch(IllegalStateException e) {
				Toast.makeText(getApplicationContext(), getString(R.string.impossibleRecord), Toast.LENGTH_SHORT).show();
			}			
		}*/
	}
	
	Runnable runnnableTime = new Runnable() {
        @Override
        public void run() {
           long millis = System.currentTimeMillis() - startTime;
           int currentMillis = (int) (millis % 100);
           int seconds = (int) (millis / 1000);
           int minutes = seconds / 60;
           seconds     = seconds % 60;
           timeRecordTV.setText(String.format("%02d:%02d:%02d", minutes, seconds, currentMillis));
           mHandlerTime.postDelayed(this, 1);
        }
    };
	
	OnClickListener clickRecord = new OnClickListener() {
		public void onClick(View v) {
			onRecord(mStartRecording);
			if (mStartRecording) { // STOP RECORDING
				recordButton.setImageDrawable(getResources().getDrawable(R.drawable.stopbig));
				stateRecordTV.setText(getString(R.string.recording));
				
				startTime = System.currentTimeMillis();
				mHandlerTime.postDelayed(runnnableTime, 0);				
			} else { // START RECORDING
				recordButton.setImageDrawable(getResources().getDrawable(R.drawable.recordbig));
				stateRecordTV.setText(getString(R.string.ready));
				
				mHandlerTime.removeCallbacks(runnnableTime);
			}
			mStartRecording = !mStartRecording;
		}
	};
	
	private void onRecord(boolean start) {
		if (start) {
			startRecording();
		} else {
			stopRecording();
		}
	}
	
	@SuppressLint("NewApi")
	private void startRecording() {
		GetFileName();
		
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFile(mFileName);
		
		if (Build.VERSION.SDK_INT >= 8) {
			mRecorder.setAudioSamplingRate(44100);
			mRecorder.setAudioEncodingBitRate(96000);
			
			//mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			//mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		} 
		
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		
		try {
			mRecorder.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			mRecorder.start();
		} catch(IllegalStateException e) {
			Toast.makeText(getApplicationContext(), getString(R.string.impossibleRecord), Toast.LENGTH_SHORT).show();
		}
	}

	private void stopRecording() {
		try {
			if(mRecorder != null) {
				mRecorder.stop();
				mRecorder.release();
				mRecorder = null;
			}
		}
		catch(IllegalStateException e) {
			Toast.makeText(getApplicationContext(), getString(R.string.impossibleRecord), Toast.LENGTH_SHORT).show();
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	public void GetFileName() {
		createDirIfNotExists("/VoiceNotes/");
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		
		Date date = new Date();		
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd-MM-yyyy HH.mm.ss");
		dateFormat.setTimeZone(TimeZone.getDefault());
		String dateNote = dateFormat.format(date);
		
		mFileName += "/VoiceNotes/VoiceNote - " + dateNote + format3gp;
	}
	
	public static boolean createDirIfNotExists(String path) {
	    boolean ret = true;
	    File file = new File(Environment.getExternalStorageDirectory(), path);
	    if (!file.exists()) {
	        if (!file.mkdirs()) {
	            ret = false;
	        }
	    }
	    return ret;
	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, R.string.done).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(getString(R.string.new_note));
			alert.setMessage(getString(R.string.dialog_text));
			alert.setIcon(R.drawable.ic_action_volume_up);
			
			final EditText input = new EditText(this);
			input.setHint(getString(R.string.new_note));
			alert.setView(input);
			
			// Ok
			alert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,	int whichButton) {					
					final String value = input.getText().toString();
					if(value != null) {
						if (mFileName != null) {
							if(!NotesListActivity.NotesArrayList.isEmpty()) {
								for (int i = 0; i < NotesListActivity.NotesArrayList.size(); i++) {
									if (value.equalsIgnoreCase(NotesListActivity.NotesArrayList.get(i).get("name"))) {// already exists
										replaceDialog(mFileName, value);
										return;
									}
									renameFile(mFileName, value);
								}
							} else renameFile(mFileName, value);
						}
					}
					finish();
					overridePendingTransition(R.anim.shrink_enter, R.anim.shrink_exit);
				}
			});
			// Cancel
			alert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,	int whichButton) {
					finish();
					overridePendingTransition(R.anim.shrink_enter, R.anim.shrink_exit);
				}
			});
			alert.show();
			return true;
			
		case android.R.id.home:
			onBackPressed();
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void replaceDialog(final String path, final String value) {
		AlertDialog.Builder builderReplace = new AlertDialog.Builder(this);
		builderReplace.setMessage("\"" + value + "\" " + getString(R.string.replace))
				.setCancelable(false)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								renameFile(path, value);
								finish();
								overridePendingTransition(R.anim.shrink_enter, R.anim.shrink_exit);
							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,	int id) {
								dialog.cancel();
								finish();
								overridePendingTransition(R.anim.shrink_enter, R.anim.shrink_exit);
							}
						});
		AlertDialog ReplaceAlert = builderReplace.create();
		ReplaceAlert.show();
	}
	
	public void renameFile(String path, String value) {
		File noteFile = new File(path);
		String newFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		newFileName += "/VoiceNotes/" + value + format3gp;
		File newName = new File(newFileName);
		noteFile.renameTo(newName);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(R.anim.shrink_enter, R.anim.shrink_exit);
	}
}