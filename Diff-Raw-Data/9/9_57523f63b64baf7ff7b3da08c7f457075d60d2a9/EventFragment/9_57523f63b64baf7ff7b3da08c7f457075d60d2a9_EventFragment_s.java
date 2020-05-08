 package com.dev.campus.event;
 
 import com.dev.campus.R;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class EventFragment extends Fragment {
 
 	public static final String ARG_EVENT_KEY = "com.dev.campus.EVENT_KEY";
 	private Event mEvent;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mEvent = (Event) getArguments().get(ARG_EVENT_KEY);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_event, container, false);
 		((TextView) rootView.findViewById(R.id.event_view_title)).setText(mEvent.getTitle());
 		((TextView) rootView.findViewById(R.id.event_view_category)).setText(mEvent.getCategory());
 		((TextView) rootView.findViewById(R.id.event_view_date)).setText(mEvent.getStringDate());
		((TextView) rootView.findViewById(R.id.event_view_details)).setText(Html.fromHtml(mEvent.getDetails()));
 		return rootView;
 	}
 
 	public static EventFragment create(Event event) {
 		EventFragment fragment = new EventFragment();
 		Bundle args = new Bundle();
 		args.putSerializable(ARG_EVENT_KEY, event);
 		fragment.setArguments(args);
 		return fragment;
 	}
 
 	public EventFragment() {
 
 	}
 }
