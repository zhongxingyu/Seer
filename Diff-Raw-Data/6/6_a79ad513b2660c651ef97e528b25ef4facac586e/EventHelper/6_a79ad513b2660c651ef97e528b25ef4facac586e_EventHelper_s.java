 package app.xzone.storyline.helper;
 
 import java.text.ParseException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import app.xzone.storyline.R;
 import app.xzone.storyline.model.Event;
 import app.xzone.storyline.model.Story;
 import app.xzone.storyline.util.TimeUtil;
 
 public class EventHelper {
 
 	public static Event getEventFromTag(Activity a) {
 		View v = a.findViewById(R.id.titleFieldEvent);
 		Event e = (Event) v.getTag();
 
 		if (e == null)
 			return null;
 		return e;
 	}
 
 	public static void setEventIntoTag(Activity a, Event obj) {
 		View v = a.findViewById(R.id.titleFieldEvent);
 		v.setTag(obj);
 	}
 
 	public static Event buildEvent(Activity a, Event event, Story story) throws ParseException {
 
 		EditText t01 = (EditText) a.findViewById(R.id.titleFieldEvent);
 		EditText t02 = (EditText) a.findViewById(R.id.messageFieldEvent);
 		
 		Spinner sp02 = (Spinner) a.findViewById(R.id.transportSpinnerEvent);
 
		TextView location = (TextView) a.findViewById(R.id.locationEvent);
 		TextView dateEvent = (TextView) a.findViewById(R.id.valueDateEvent);
 		TextView timeEvent = (TextView) a.findViewById(R.id.valueTimeEvent);
 
 		event.setView(event.getView());
 		event.setName(t01.getText().toString());
 		event.setMessage(t02.getText().toString());
 		event.setTransportation(sp02.getSelectedItem().toString());
 		event.setLocname(location.getText().toString());
 		
 		int hour = Integer
 				.parseInt(timeEvent.getText().toString().split(":")[0]);
 		int minute = Integer
 				.parseInt(timeEvent.getText().toString().split(":")[1].split(" ")[0]);
 		event.setStartDate(TimeUtil.toEpochFormat(dateEvent.getText().toString(),
 				hour, minute));
 
 		event.setStory(story);
 
 		if (location.getTag() != null) {
 			double[] data = (double[]) location.getTag();
 			event.setLat((double) data[0]);
 			event.setLng((double) data[1]);
 		}
 
 		return event;
 	}
 
 	public static void buildUISliding(Activity a, Event e) {
 		EditText e01 = (EditText) a.findViewById(R.id.titleFieldEvent);
 		EditText e02 = (EditText) a.findViewById(R.id.messageFieldEvent);
 
 		Spinner sp02 = (Spinner) a.findViewById(R.id.transportSpinnerEvent);
 
 		TextView tv01 = (TextView) a.findViewById(R.id.valueDateEvent);
 		TextView tv02 = (TextView) a.findViewById(R.id.valueTimeEvent);
 		TextView tv03 = (TextView) a.findViewById(R.id.locationEvent);
 
 		// null -> add new event, exist -> update event
 		e01.setTag(e); // store event object into tag event was null
 		if (e == null)
 			return;
 
 		e01.setText(e.getName());
 		e02.setText(e.getMessage());
 
 		String[] items = a.getApplication().getResources()
 				.getStringArray(R.array.categoryItems);
 		List<String> list = Arrays.asList(items);
 
 		items = a.getApplication().getResources()
 				.getStringArray(R.array.tranportItems);
 		list = Arrays.asList(items);
 
 		int pos = list.indexOf(e.getTransportation());
 		sp02.setSelection(pos);
 
 		Date date = TimeUtil.fromEpochFormat(e.getStartDate());
 
 		tv01.setText(TimeUtil.dateFormat(date, "EEE MMM d, yyyy"));
 		tv02.setText(TimeUtil.dateFormat(date, "k:mm a"));

 	}
 
 }
