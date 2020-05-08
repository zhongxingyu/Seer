 package com.cs371m.notesync;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class NotesAdapter extends ArrayAdapter<Note> {
 
 	private static class ViewHolder {
 		TextView course;
 		TextView topic;
 		TextView date;
 	}
 
 	public NotesAdapter(Context context) {
 		super(context, R.layout.note_item);
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		// Get the data item for this position
 		Note note = getItem(position);    
 		// Check if an existing view is being reused, otherwise inflate the view
 		ViewHolder viewHolder; // view lookup cache stored in tag
 		if (convertView == null) {
 			viewHolder = new ViewHolder();
 			LayoutInflater inflater = LayoutInflater.from(getContext());
 			convertView = inflater.inflate(R.layout.note_item, null);
 			viewHolder.course = (TextView) convertView.findViewById(R.id.noteCourse);
 			viewHolder.topic = (TextView) convertView.findViewById(R.id.noteTopic);		
 			viewHolder.date = (TextView) convertView.findViewById(R.id.noteDate);
 			convertView.setTag(viewHolder);
 		} else {
 			viewHolder = (ViewHolder) convertView.getTag();
 		}
 		// Populate the data into the template view using the data object
		viewHolder.course.setText(note.course);
		viewHolder.topic.setText(note.topic);
 		viewHolder.date.setText(note.date);
 		// Return the completed view to render on screen
 		return convertView;
 	}
 }
