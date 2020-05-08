 package com.kg6.schlafdoedel.custom;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.kg6.schlafdoedel.event.Event;
 import com.kg6.schlafdoedel.event.EventScheduler;
import com.kg6.schlafdoedelmaster.R;
 
 public class RecentActivitiesPanel extends LinearLayout {
 	private final int VIEW_DATE_COLUMN_WIDTH = 100;
 	private final int VIEW_DISMISS_COLUMN_WIDTH = 160;
 	private final int VIEW_COLUMN_MARGIN = 20;
 	
 	private final Activity CONTEXT;
 	
 	private EventScheduler eventScheduler;
 	
 	private List<StatusEntry> statusEntryList;
 	private StatusEntry defaultStatusEntry;
 
 	public RecentActivitiesPanel(Activity context) {
 		super(context);
 		
 		CONTEXT = context;
 		
 		this.eventScheduler = null;
 		
 		this.statusEntryList = new ArrayList<StatusEntry>();
 		this.defaultStatusEntry = generateDefaultStatusEntry();
 		
 		setOrientation(LinearLayout.VERTICAL);
 	}
 	
 	public void setEventScheduler(EventScheduler eventScheduler) {
 		this.eventScheduler = eventScheduler;
 	}
 	
 	public void addStatusText(final String text) {
 		CONTEXT.runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				addStatusEntry(new StatusEntry(text));
 			}
 			
 		});
 	}
 	
 	public void addStatusText(final Event event) {
 		CONTEXT.runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				addStatusEntry(new StatusEntry(event.getTitle(), event));
 			}
 			
 		});
 	}
 	
 	public void removeStatusText(final Event event) {
 		CONTEXT.runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				StatusEntry statusEntry = null;
 				
 				for(int i = statusEntryList.size() - 1; i >= 0; i--) {
 					StatusEntry entry = statusEntryList.get(i);
 					
 					if(entry.hasEvent() && event.equals(entry.event)) {
 						statusEntry = entry;
 						break;
 					}
 				}
 				
 				if(statusEntry != null) {
 					removeStatusEntry(statusEntry);
 				}
 			}
 			
 		});
 	}
 	
 	private void addStatusEntry(StatusEntry entry) {
 		this.statusEntryList.add(entry);
 		
 		//Remove the default entry
 		if(this.defaultStatusEntry.hasView()) {
 			removeView(this.defaultStatusEntry.view);
 		}
 		
 		//Update the status view
 		updateStatusView();
 	}
 	
 	private void removeStatusEntry(StatusEntry entry) {
 		if(entry.hasView()) {
 			removeView(entry.view);
 		}
 		
 		//Remove the entry
 		this.statusEntryList.remove(entry);
 		
 		//Show the default entry if necessary
 		if(this.statusEntryList.size() == 0 && this.defaultStatusEntry.hasView()) {
 			addView(this.defaultStatusEntry.view, getStatusEntryParams());
 		}
 	}
 	
 	private void updateStatusView() {
 		for(int i = 0; i < this.statusEntryList.size(); i++) {
 			StatusEntry entry = this.statusEntryList.get(i);
 			
 			if(!entry.hasView()) {
 				entry.view = generateEntry(entry);
 
 				addView(entry.view, getStatusEntryParams());
 			}
 		}
 		
 		//Scroll down automatically
 		ScrollView scrollView = (ScrollView)CONTEXT.findViewById(R.id.statusPanelScrollView);
 		
 		if(scrollView != null) {
 			scrollView.scrollTo(0, scrollView.getBottom());
 		}
 	}
 	
 	private LinearLayout generateEntry(final StatusEntry entry) {
 		OnClickListener dismissListener = new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				removeStatusEntry(entry);
 				
 				if(entry.hasEvent() && eventScheduler != null) {
 					eventScheduler.dismissEvent(entry.event);
 				}
 			}
 		};
 		
 		LinearLayout entryLayout = new LinearLayout(CONTEXT);
 		entryLayout.setOrientation(LinearLayout.HORIZONTAL);
 		
 		//date
 		TextView entryDateView = new TextView(CONTEXT);
 		entryDateView.setText(Util.GetTimeOfDatePrintableFormat(new Date(System.currentTimeMillis())));
 		
 		entryLayout.addView(entryDateView, new LayoutParams(VIEW_DATE_COLUMN_WIDTH, LayoutParams.WRAP_CONTENT));
 		
 		//description
 		TextView entryDescriptionView = new TextView(CONTEXT);
 		entryDescriptionView.setText(entry.title);
 		
 		LayoutParams entryDescriptionViewParams = new LayoutParams(Util.GetDeviceWidth(CONTEXT) - VIEW_DATE_COLUMN_WIDTH - VIEW_DISMISS_COLUMN_WIDTH - 2 * VIEW_COLUMN_MARGIN, LayoutParams.WRAP_CONTENT);
 		
 		entryLayout.addView(entryDescriptionView, entryDescriptionViewParams);
 		
 		//dismiss button
 		Button dismissButton = new Button(CONTEXT);
 		dismissButton.setText("Dismiss");
 		dismissButton.setOnClickListener(dismissListener);
 		
 		entryLayout.addView(dismissButton, new LayoutParams(VIEW_DISMISS_COLUMN_WIDTH, LayoutParams.WRAP_CONTENT));
 		
 		return entryLayout;
 	}
 	
 	private LayoutParams getStatusEntryParams() {
 		LayoutParams entryParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		
 		entryParams.topMargin = 10;
 		entryParams.rightMargin = 10;
 		entryParams.leftMargin = 10;
 		
 		return entryParams;
 	}
 	
 	private StatusEntry generateDefaultStatusEntry() {
 		StatusEntry statusEntry = new StatusEntry("No recent activities");
 		
 		LinearLayout entryLayout = new LinearLayout(CONTEXT);
 		entryLayout.setOrientation(LinearLayout.HORIZONTAL);
 		
 		statusEntry.view = entryLayout;
 		
 		//description
 		TextView entryDescriptionView = new TextView(CONTEXT);
 		entryDescriptionView.setText(statusEntry.title);
 		
 		entryLayout.addView(entryDescriptionView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 		
 		return statusEntry;
 	}
 	
 	private class StatusEntry {
 		private Event event;
 		private String title;
 		
 		private LinearLayout view = null;
 		
 		public StatusEntry(String title) {
 			this.title = title;
 			this.event = null;
 		}
 		
 		public StatusEntry(String title, Event event) {
 			this.title = title;
 			this.event = event;
 		}
 		
 		public boolean hasEvent() {
 			return this.event != null;
 		}
 		
 		public boolean hasView() {
 			return this.view != null;
 		}
 	}
 }
