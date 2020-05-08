 package com.example.eventf;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.androidquery.AQuery;
 
 public class EventsDetailsFragment extends Fragment {
 
 	private DetailsFragmentDataIf mDataIf;
 	private AQuery detailsAquery;
 	private View detailsView;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 	
 		detailsView = inflater.inflate(R.layout.events_detail_activity,
 				container, false);
 		detailsAquery = new AQuery(getActivity(), detailsView);
 
 		Event event = mDataIf.getEvent();
 		String mHours;
 		String mDate;
 		mHours = EventsTimeDesign.eventsHoursDesign(event.getStartTime(),
 				event.getEndTime(), event.getGmt());
 		mDate = EventsTimeDesign.eventsDateDesign(event.getStartTime());
		
 		detailsAquery.find(R.id.hours).text(mHours);
 		detailsAquery.find(R.id.date).text(mDate);
 		detailsAquery.find(R.id.title).text(event.getTitle());
 		detailsAquery.find(R.id.location).text(event.getlocation());
 
 		if (!(event.getDescription().equals("null"))) {
 			detailsAquery.find(R.id.description).getTextView()
 					.setHint(event.getDescription() + "\n");
 			detailsAquery.find(R.id.background).image(event.getImageUrl());
 		}
 
 		return detailsView;
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		try {
 			mDataIf = (DetailsFragmentDataIf) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString()
 					+ " must implement DetailsFragmentDataIf");
 		}
 	}
 
 	public interface DetailsFragmentDataIf {
 
 		public Event getEvent();
 
 	}
 
 }
