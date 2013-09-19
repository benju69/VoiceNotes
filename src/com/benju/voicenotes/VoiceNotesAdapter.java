package com.benju.voicenotes;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class VoiceNotesAdapter extends ArrayAdapter<HashMap<String, String>> /*BaseAdapter*/ implements Filterable {

	private Context mContext;
	private ArrayList<HashMap<String, String>> VoiceNotesList;
	private ArrayList<HashMap<String, String>> filteredVoiceNotesList;
	private LayoutInflater mInflater;
	private Typeface tf;
	
	VoiceNotesFilter filter;

	public VoiceNotesAdapter(Context c, ArrayList<HashMap<String, String>> notesList) {
		super(c, 0);
		mContext = c;
		
		this.VoiceNotesList = new ArrayList<HashMap<String,String>>();
		VoiceNotesList.addAll(notesList);
		
		this.filteredVoiceNotesList = new ArrayList<HashMap<String,String>>();
		filteredVoiceNotesList.addAll(VoiceNotesList);
		
		mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		tf = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
		
		getFilter();
	}

	@Override
	public int getCount() {
		if(filteredVoiceNotesList != null) return filteredVoiceNotesList.size();
		return 0;
	}

	@Override
	public HashMap<String, String> getItem(int position) {
		if(filteredVoiceNotesList != null) return filteredVoiceNotesList.get(position);
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View MyView;
		if (convertView == null) // if it's not recycled, initialize some attributes
		{
			MyView = new View(mContext);
			MyView = mInflater.inflate(R.layout.adapter_note_list, null);
		} else {
			MyView = (View) convertView;
		}

		TextView titleView = (TextView) MyView.findViewById(R.id.titleTextView);
		TextView dateView = (TextView) MyView.findViewById(R.id.dateTextView);
		TextView timeView = (TextView) MyView.findViewById(R.id.timeTextView);
		
		HashMap<String, String> item = filteredVoiceNotesList.get(position);
		
		if (titleView != null) {
			String titre = item.get("name");
			titleView.setText("" + titre);
			titleView.setTypeface(tf);
		}

		if (dateView != null) {
			String date = item.get("date");
			dateView.setText("" + date);
		}
		
		if (timeView != null) {
			String time = item.get("duration");
			timeView.setText(time);
		}
		
		Animation animation = null;
		animation = new TranslateAnimation(-100, 0, 0, 0);
		MyView.clearAnimation();
		animation.setRepeatCount(0);
		animation.setRepeatMode(Animation.REVERSE);
		animation.setDuration(500);
		animation.setFillBefore(true);
		MyView.startAnimation(animation);
		animation = null;

		return MyView;
	}

	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new VoiceNotesFilter();
		}
		return filter;
	}
	
	private class VoiceNotesFilter extends Filter {
		@SuppressLint("DefaultLocale")
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			constraint = constraint.toString().toLowerCase();
			FilterResults result = new FilterResults();

			if (constraint != null && constraint.toString().length() > 0) {
				ArrayList<HashMap<String, String>> filteredItems = new ArrayList<HashMap<String, String>>();
				for (int i = 0; i < VoiceNotesList.size(); i++) {
					String name = VoiceNotesList.get(i).get("name");
					if (name.toLowerCase().contains(constraint)) {
						filteredItems.add(VoiceNotesList.get(i));
					}					
					String date = VoiceNotesList.get(i).get("date");//search by date
					if (date.toLowerCase().contains(constraint)) {
						filteredItems.add(VoiceNotesList.get(i));
					}
				}
				result.count = filteredItems.size();
				result.values = filteredItems;
			} else {
				synchronized (this) {
					result.values = VoiceNotesList;
					result.count = VoiceNotesList.size();
				}
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {			
			filteredVoiceNotesList = (ArrayList<HashMap<String, String>>)results.values;
			notifyDataSetChanged();
            clear();
            for(int i = 0, l = filteredVoiceNotesList.size(); i < l; ++i)
                add(filteredVoiceNotesList.get(i));
            notifyDataSetInvalidated();
		}
		
	}
}