package com.benju.voicenotes;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

@SuppressLint("SimpleDateFormat")
public class NotesListActivity extends SherlockActivity {
	
	private GridView mGridNotes;
	private SeekBar mSeekBar;
	private ImageButton mPlayButton;
	private TextView currentTime;
	private TextView maxTime;
	
	private SearchView searchView;
	private MediaPlayer mPlayer = null;
	boolean mStartPlaying = true;
		
	public static ArrayList<HashMap<String, String>> NotesArrayList = new ArrayList<HashMap<String, String>>();
	private File root;
	private VoiceNotesAdapter notesAdapter;
	
	private Handler seekHandler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_listnotes);
		getSupportActionBar().setLogo(R.drawable.ic_launcher);
		
		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
		TextView emptyTextView = (TextView)findViewById(R.id.textViewEmpty);
		emptyTextView.setTypeface(tf, Typeface.ITALIC);
		
		int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
		if(titleId == 0)
		    titleId = com.actionbarsherlock.R.id.abs__action_bar_title;
		TextView title = (TextView) findViewById(titleId);
		title.setTypeface(tf);
		
		mGridNotes = (GridView)findViewById(R.id.ListViewNotes);
		mGridNotes.setVerticalSpacing(10);
		mGridNotes.setHorizontalSpacing(10);
		mGridNotes.setOnItemClickListener(listClickListener);
		LinearLayout empty = (LinearLayout)findViewById(R.id.emptyView);
		mGridNotes.setEmptyView(empty);
		
		registerForContextMenu(mGridNotes);
				
		mPlayButton = (ImageButton)findViewById(R.id.imageButtonPlayPauseNote);
		mPlayButton.setOnClickListener(playButtonClickListener);
		
		mSeekBar = (SeekBar)findViewById(R.id.seekBarPlay);
		mSeekBar.setOnSeekBarChangeListener(seekbarListener);
		
		currentTime = (TextView)findViewById(R.id.textViewCurrentTime);
		maxTime = (TextView)findViewById(R.id.textViewTimeMax);
		
		getNotesList();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getNotesList();
	}

	private void getNotesList() {
		NotesArrayList.clear();
		
		root = new File(Environment.getExternalStorageDirectory().toString(), "VoiceNotes/");
		if (root.exists() == false)
			root.mkdirs();
		getFilesList(root);
	}
	
	@SuppressLint("SimpleDateFormat")
	public void getFilesList(File dir) {
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				getFilesList(file);
			} else {
				if (file.getName().contains(".3gp"))
				{
					String name = file.getName().replace(".3gp", "");//-.3gp
					String path = file.getPath();
					
					// date
					Date lastModDate = new Date(file.lastModified());
					DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
					String date = dateFormat.format(lastModDate);
					
					// duration
					String duration = getString(R.string.nulltime);
					MediaPlayer mp = MediaPlayer.create(this, Uri.parse(path));
					if(mp != null) {
						int millis = mp.getDuration();					
						SimpleDateFormat formatDuration = new SimpleDateFormat("HH:mm:ss");
						formatDuration.setTimeZone(TimeZone.getTimeZone("GMT+0"));
						duration = formatDuration.format(new Date(millis));
						mp.release();
					}
					
					HashMap<String, String> map;
					map = new HashMap<String, String>();
					map.put("name", "" + name);
					map.put("path", "" + path);
					map.put("date", "" + date);
					map.put("duration", "" + duration);
					
					NotesArrayList.add(map);
				}
			}
		}
		notesAdapter = new VoiceNotesAdapter(this, NotesArrayList);
		mGridNotes.setAdapter(notesAdapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//record
		menu.add(0, 0, 0, R.string.action_record)
				.setIcon(R.drawable.ic_action_mic)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		// search
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			searchView = new SearchView(getSupportActionBar().getThemedContext());
	        searchView.setQueryHint(getString(R.string.action_search));
	        searchView.setOnQueryTextListener(QueryListener);
	        
			menu.add(0, 1, 0, R.string.action_search)
					.setIcon(R.drawable.ic_action_search)
					.setActionView(searchView)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		}
		menu.add(0, 2, 0, R.string.action_refresh).setIcon(R.drawable.ic_action_reload);// refresh		
		menu.add(0, 3, 0, R.string.action_info).setIcon(R.drawable.ic_action_info);// about
		menu.add(0, 4, 0, R.string.action_delete_all).setIcon(R.drawable.ic_action_cancel);// delete all
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			Intent recordIntent = new Intent(getApplicationContext(), RecordActivity.class);
			startActivity(recordIntent);
			overridePendingTransition(R.anim.blow_up_enter, R.anim.blow_up_exit);
			return true;
			
		case 1:	
			return true;
			
		case 2:
			getNotesList();
			return true;
			
		case 3: // info
			Intent AboutActivity = new Intent(getApplicationContext(), AboutActivity.class);
			startActivity(AboutActivity);
			overridePendingTransition(R.anim.blow_up_enter, R.anim.blow_up_exit);
			return true;
			
		case 4:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					getString(R.string.dialogDeleteAll))
					.setCancelable(false)
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int id) {
							File dir = new File(Environment.getExternalStorageDirectory().toString(), "VoiceNotes/");
							File[] paths = dir.listFiles();
							for (File path : paths) {
								path.delete();
							}
							getNotesList();
						}
					})
					.setNegativeButton(R.string.no,	new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int id) {
							dialog.cancel();
						}
					});
			AlertDialog DeleteAlert = builder.create();
			DeleteAlert.show();
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public OnItemClickListener listClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			String path = NotesArrayList.get(position).get("path");			
			onPlay(mStartPlaying, path);
			if (mStartPlaying) { // afficher pause
				mPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_playback_pause));
				mStartPlaying = false;
			} else { // afficher play
				mPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_playback_play));
				mStartPlaying = true;
			}
		}
	};
	
	public OnClickListener playButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mPlayer != null) {
				if (mPlayer.isPlaying()) {
					mPlayer.pause();
					mPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_playback_play));
				} else {
					mPlayer.start();
					mPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_playback_pause));
				}
			}
		}
	};
	
	public OnSeekBarChangeListener seekbarListener = new OnSeekBarChangeListener() {		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if(mPlayer != null) {
				mPlayer.seekTo(seekBar.getProgress());
			}
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		}
	};
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
		menu.setHeaderTitle("");//nom note?
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		final String fileName = NotesArrayList.get((int) info.id).get("name");
		final String path = NotesArrayList.get((int) info.id).get("path");
		
		switch (item.getItemId()) {
		case R.id.play:
			onPlay(mStartPlaying, path);
			return true;
			
		case R.id.share:			
			Intent sharingIntent = new Intent(Intent.ACTION_SEND);
			sharingIntent.setType("audio/*");
			sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///" + path));
			startActivity(Intent.createChooser(sharingIntent, getString(R.string.share)));
			return true;
			
		case R.id.edit: //renommer, modifier nom
			final AlertDialog.Builder EditAlert = new AlertDialog.Builder(this);
			EditAlert.setTitle(getString(R.string.edit));
			EditAlert.setMessage(getString(R.string.dialog_text));
			EditAlert.setIcon(R.drawable.ic_action_volume_up);
			
			final EditText input = new EditText(this);
			input.setHint(getString(R.string.new_note));
			EditAlert.setView(input);

			// Ok
			EditAlert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,	int whichButton) {
					final String value = input.getText().toString();
					if(value != null) {
						if (path != null) {					
							for (int i = 0; i < NotesArrayList.size(); i++) {
								if (value.equalsIgnoreCase(NotesArrayList.get(i).get("name"))) {// already exists
									replaceDialog(path, value);
									return;
								}
								renameFile(path, value);
							}
						}
					}
					getNotesList();
				}
			});
			// Cancel
			EditAlert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,	int whichButton) {
					dialog.cancel();
				}
			});
			EditAlert.show();
			return true;
			
		case R.id.delete:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					getString(R.string.dialogDelete) + " \"" + fileName	+ "\" ?")
					.setCancelable(false)
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int id) {
							File dir = new File(Environment.getExternalStorageDirectory().toString(), "VoiceNotes/");
							deleteFile(dir, fileName);
							getNotesList();
						}
					})
					.setNegativeButton(R.string.no,	new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int id) {
							dialog.cancel();
						}
					});
			AlertDialog DeleteAlert = builder.create();
			DeleteAlert.show();
			return true;
			
		case R.id.ringtone:			
			File noteFile = new File(path);			
			ContentValues values = new ContentValues();
			values.put(MediaStore.MediaColumns.DATA, path);
			values.put(MediaStore.MediaColumns.TITLE, fileName);
			values.put(MediaStore.MediaColumns.SIZE, noteFile.length());
			values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/3gp");
			values.put(MediaStore.Audio.Media.ARTIST, "VoiceNotes");
			values.put(MediaStore.Audio.Media.IS_RINGTONE, true);

			Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);
			Uri newUri = getContentResolver().insert(uri, values);

			RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, newUri);
			return true;
			
		default:
			return true;
		}
	}
	
	private void onPlay(boolean start, String filename) {
		if (start) {
			startPlaying(filename);
		} else {
			stopPlaying();
		}
	}
	
	private void startPlaying(String filename) {
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(filename);
			mPlayer.prepare();
			mPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
	            public void onCompletion(MediaPlayer mp) {
	                stopPlaying();
	            }
	        });
			mPlayer.start();
			mSeekBar.setMax(mPlayer.getDuration());
			
			SimpleDateFormat formatDuration = new SimpleDateFormat("HH:mm:ss");
			formatDuration.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			String duration = formatDuration.format(new Date(mPlayer.getDuration()));
			
			maxTime.setText(duration);
			seekUpdater();
		} catch (IOException e) {
			//"prepare() failed"
		}
	}
	
	private void stopPlaying() {
		if (mPlayer != null) {
			mPlayButton.setImageDrawable(getResources().getDrawable(
					R.drawable.ic_action_playback_play));
			mSeekBar.setProgress(0);
			mStartPlaying = true;
		}
	}
	
	void deleteFile(File dir, String fileName) {
		File[] files = dir.listFiles();
		fileName += ".3gp";
		
		for (File file : files) {
			if (file.isDirectory()) {
				deleteFile(file, fileName);
			} else {
				if (file.getName().contentEquals(fileName) && file != null) {
					file.delete();
				}
			}
		}
		
		Toast.makeText(this, getString(R.string.deleting) + " " + fileName, Toast.LENGTH_SHORT).show();
	}
	
	public void replaceDialog(final String path, final String value) {
		AlertDialog.Builder builderReplace = new AlertDialog.Builder(this);
		builderReplace.setMessage("\"" + value + "\" " + getString(R.string.replace))
				.setCancelable(false)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								renameFile(path, value);
								getNotesList();
							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,	int id) {
								dialog.cancel();
							}
						});
		AlertDialog ReplaceAlert = builderReplace.create();
		ReplaceAlert.show();
	}
	
	public void renameFile(String path, String value) {
		File noteFile = new File(path);
		String newFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		newFileName += "/VoiceNotes/" + value + ".3gp";
		File newName = new File(newFileName);
		noteFile.renameTo(newName);
	}
	
	public SearchView.OnQueryTextListener QueryListener = new SearchView.OnQueryTextListener() {
		@Override
		public boolean onQueryTextSubmit(String newText) {
			if(notesAdapter != null) {
				notesAdapter.getFilter().filter(newText);
				searchView.clearFocus();
			}
			return true;
		}

		@Override
		public boolean onQueryTextChange(String newText) {
			if(notesAdapter != null) {
				notesAdapter.getFilter().filter(newText);
			}
			return true;
		}
	};

	public void seekUpdater() {
		if (mPlayer != null) {
			mSeekBar.setProgress(mPlayer.getCurrentPosition());
			seekHandler.postDelayed(runnableSeek, 100);

			SimpleDateFormat formatDuration = new SimpleDateFormat("HH:mm:ss");
			formatDuration.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			String duration = formatDuration.format(new Date(mPlayer.getCurrentPosition()));

			currentTime.setText(duration);
		}
	}
    
    Runnable runnableSeek = new Runnable() {
		@Override
		public void run() {
			seekUpdater();
		}
	};  
}