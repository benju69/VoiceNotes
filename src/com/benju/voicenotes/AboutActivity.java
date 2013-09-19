package com.benju.voicenotes;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class AboutActivity extends SherlockActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setLogo(R.drawable.ic_launcher);
		
		TextView mText = (TextView) findViewById(R.id.infoTv);		
		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
		mText.setTypeface(tf);
		
		int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
		if(titleId == 0) titleId = com.actionbarsherlock.R.id.abs__action_bar_title;
		TextView title = (TextView) findViewById(titleId);
		title.setTypeface(tf);
		
		Button feedback = (Button)findViewById(R.id.buttonfeed);
		feedback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","begonin@gmail.com", null));
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback)+" "+getString(R.string.app_name));
				startActivity(Intent.createChooser(emailIntent, getString(R.string.feedback)));
			}
		});
		Button rate = (Button)findViewById(R.id.buttonrate);
		rate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String appName = "com.benju.voicenotes";
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+appName)));
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(R.anim.shrink_enter, R.anim.shrink_exit);
	}
}