 package com.kg6.schlafdoedel.custom;
 
 import java.io.File;
 import java.util.Calendar;
 
 import kankan.wheel.widget.OnWheelChangedListener;
 import kankan.wheel.widget.WheelView;
 import kankan.wheel.widget.adapters.NumericWheelAdapter;
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.graphics.Color;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.kg6.schlafdoedel.Configuration;
 import com.kg6.schlafdoedel.event.Event;
 import com.kg6.schlafdoedel.event.EventScheduler;
 import com.kg6.schlafdoedel.event.EventSource;
 import com.kg6.schlafdoedel.event.EventSource.SourceType;
import com.kg6.schlafdoedelmaster.R;
 
 public class EventDefinitionDialog extends Dialog {
 	private final int ROW_HEADER_WIDTH = 180;
 	private final int SELECT_BUTTON_WIDTH = 150;
 	private final int WHEEL_WIDTH = 100;
 	
 	private final Activity CONTEXT;
 	private final EventScheduler EVENT_SCHEDULER;
 	
 	private EditText musicSourceTextBox;
 	private EditText imageSourceTextBox;
 	
 	public EventDefinitionDialog(Activity context, EventScheduler eventScheduler) {
 		super(context, android.R.style.Theme_Black);
 		
 		CONTEXT = context;
 		EVENT_SCHEDULER = eventScheduler;
 		
 		this.musicSourceTextBox = null;
 		this.imageSourceTextBox = null;
 
 		initializeControls();
 	}
 	
 	private void initializeControls() {
 		ScrollView scrollView = new ScrollView(getContext());
 		addContentView(scrollView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 		
 		LinearLayout contentContainerLayout = new LinearLayout(getContext());
 		contentContainerLayout.setOrientation(LinearLayout.VERTICAL);
 		
 		scrollView.addView(contentContainerLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 		
 		LinearLayout contentLayout = new LinearLayout(getContext());
 		contentLayout.setOrientation(LinearLayout.VERTICAL);
 		
 		LayoutParams contentLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 		contentLayoutParams.topMargin = 10;
 		contentLayoutParams.rightMargin = 10;
 		contentLayoutParams.bottomMargin = 10;
 		contentLayoutParams.leftMargin = 10;
 		
 		contentContainerLayout.addView(contentLayout, contentLayoutParams);
 		
 		//header
 		TextView headerTextView = new TextView(CONTEXT);
 		headerTextView.setText("Add a new event");
 		headerTextView.setTextSize(18);
 		headerTextView.setTextColor(Color.WHITE);
 		
 		contentLayout.addView(headerTextView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 		
 		//title
 		final EditText titleTextBox = new EditText(CONTEXT);
 		titleTextBox.setSingleLine();
 		titleTextBox.setText(getDefaultEventTitle());
 		
 		addRow(contentLayout, "Event title", titleTextBox);
 		
 		//start
 		final int[] expectedWakeupTime = getNextWakeUpTime();
 		
 		LinearLayout startWheelLayout = new LinearLayout(CONTEXT);
 		startWheelLayout.setOrientation(LinearLayout.HORIZONTAL);
 		
 		final WheelView startWheelHours = new WheelView(CONTEXT);
 		startWheelHours.setViewAdapter(new NumericWheelAdapter(CONTEXT, 0, 23));
 		startWheelHours.setCurrentItem(expectedWakeupTime[0]);
 		
 		startWheelLayout.addView(startWheelHours, new LayoutParams(WHEEL_WIDTH, LayoutParams.WRAP_CONTENT));
 		
 		final WheelView startWheelMinutes = new WheelView(CONTEXT);
 		startWheelMinutes.setViewAdapter(new NumericWheelAdapter(CONTEXT, 0, 59, "%02d"));
 		startWheelHours.setCurrentItem(expectedWakeupTime[1]);
 		startWheelMinutes.setCyclic(true);
 		
 		startWheelLayout.addView(startWheelMinutes, new LayoutParams(WHEEL_WIDTH, LayoutParams.WRAP_CONTENT));
 		
 		addRow(contentLayout, "Wake up start time", startWheelLayout);
 		
 		//end
 		LinearLayout endWheelLayout = new LinearLayout(CONTEXT);
 		endWheelLayout.setOrientation(LinearLayout.HORIZONTAL);
 		
 		final WheelView endWheelHours = new WheelView(CONTEXT);
 		endWheelHours.setViewAdapter(new NumericWheelAdapter(CONTEXT, 0, 23));
 		endWheelHours.setCurrentItem((expectedWakeupTime[0] + 1) % 24);
 		
 		endWheelLayout.addView(endWheelHours, new LayoutParams(WHEEL_WIDTH, LayoutParams.WRAP_CONTENT));
 		
 		final WheelView endWheelMinutes = new WheelView(CONTEXT);
 		endWheelMinutes.setViewAdapter(new NumericWheelAdapter(CONTEXT, 0, 59, "%02d"));
 		endWheelMinutes.setCurrentItem(expectedWakeupTime[1]);
 		endWheelMinutes.setCyclic(true);
 		
 		endWheelLayout.addView(endWheelMinutes, new LayoutParams(WHEEL_WIDTH, LayoutParams.WRAP_CONTENT));
 		
 		addRow(contentLayout, "Wake up end time", endWheelLayout);
 		
 		//repetition
 		final boolean[] expectedRepetition = getExpectedRepetition();
 		
 		final CheckBox[] repetitionCheckBoxes = new CheckBox[Util.GetNumberOfWeekdays()];
 		
 		LinearLayout repetitionLayout = new LinearLayout(CONTEXT);
 		repetitionLayout.setOrientation(LinearLayout.HORIZONTAL);
 		
 		LinearLayout repetitionLeftLayout = new LinearLayout(CONTEXT);
 		repetitionLeftLayout.setOrientation(LinearLayout.VERTICAL);
 		repetitionLayout.addView(repetitionLeftLayout);
 		
 		for(int i = 1; i <= repetitionCheckBoxes.length; i++) {
 			final int index = i % repetitionCheckBoxes.length;
 			
 			CheckBox checkBox = new CheckBox(CONTEXT);
 			checkBox.setText(Util.getWeekdayPrintname(index));
 			checkBox.setChecked(expectedRepetition[index]);
 			
 			repetitionLeftLayout.addView(checkBox);
 			
 			repetitionCheckBoxes[index] = checkBox;
 		}
 		
 		LinearLayout repetitionRightLayout = new LinearLayout(CONTEXT);
 		repetitionRightLayout.setOrientation(LinearLayout.VERTICAL);
 		repetitionLayout.addView(repetitionRightLayout);
 		
 		LinearLayout allWorkdaysLayout = new LinearLayout(CONTEXT);
 		allWorkdaysLayout.setOrientation(LinearLayout.HORIZONTAL);
 		repetitionRightLayout.addView(allWorkdaysLayout);
 		
 		final ImageButton allWorkdaysBracketButton = new ImageButton(CONTEXT);		
 		allWorkdaysBracketButton.setBackground(CONTEXT.getResources().getDrawable(R.drawable.bracket));
 		
 		LayoutParams allWorkdaysBracketButtonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		allWorkdaysBracketButtonParams.topMargin = 20;
 		allWorkdaysBracketButtonParams.leftMargin = 50;
 		
 		allWorkdaysLayout.addView(allWorkdaysBracketButton, allWorkdaysBracketButtonParams);
 		
 		final Button allWorkdaysButton = new Button(CONTEXT);
 		allWorkdaysButton.setText("All workdays");
 		
 		allWorkdaysButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				for(int i = 1; i <= 5; i++) {
 					repetitionCheckBoxes[i].setChecked(true);
 				}
 			}
 		});
 		
 		LayoutParams allWorkdaysButtonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		allWorkdaysButtonParams.topMargin = 130;
 		allWorkdaysButtonParams.leftMargin = 30;
 		
 		allWorkdaysLayout.addView(allWorkdaysButton, allWorkdaysButtonParams);
 		
 		addRow(contentLayout, "Repetition", repetitionLayout);
 		
 		//music event type
 		LinearLayout musicSourceLayout = new LinearLayout(CONTEXT);
 		musicSourceLayout.setOrientation(LinearLayout.HORIZONTAL);
 		
 		this.musicSourceTextBox = new EditText(CONTEXT);
 		this.musicSourceTextBox.setSingleLine();
 		
 		musicSourceLayout.addView(this.musicSourceTextBox, new LayoutParams(Util.GetDeviceWidth(CONTEXT) - ROW_HEADER_WIDTH - SELECT_BUTTON_WIDTH - 10, LayoutParams.WRAP_CONTENT));
 		
 		final Button musicSourceButton = new Button(CONTEXT);
 		musicSourceButton.setText("Select");
 		
 		musicSourceButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
 			    intent.setType("*/*"); 
 			    intent.addCategory(Intent.CATEGORY_OPENABLE);
 
 			    try {
 			        CONTEXT.startActivityForResult(Intent.createChooser(intent, "Select a music source"), Configuration.FILE_CHOOSER_MUSIC_RESPONSE_CODE);
 			    } catch (android.content.ActivityNotFoundException ex) {
 			        Toast.makeText(CONTEXT, "You need to install a file manager in order to use this functionality.", Toast.LENGTH_SHORT).show();
 			    }
 			}
 		});
 		
 		musicSourceLayout.addView(musicSourceButton, new LayoutParams(SELECT_BUTTON_WIDTH, LayoutParams.WRAP_CONTENT));
 		
 		addRow(contentLayout, "Play music from", musicSourceLayout);
 		
 		//image event type
 		LinearLayout imageSourceLayout = new LinearLayout(CONTEXT);
 		imageSourceLayout.setOrientation(LinearLayout.HORIZONTAL);
 		
 		this.imageSourceTextBox = new EditText(CONTEXT);
 		this.imageSourceTextBox.setSingleLine();
 		
 		imageSourceLayout.addView(this.imageSourceTextBox, new LayoutParams(Util.GetDeviceWidth(CONTEXT) - ROW_HEADER_WIDTH - SELECT_BUTTON_WIDTH - 10, LayoutParams.WRAP_CONTENT));
 		
 		final Button imageSourceButton = new Button(CONTEXT);
 		imageSourceButton.setText("Select");
 		
 		imageSourceButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
 			    intent.setType("*/*");
 			    intent.addCategory(Intent.CATEGORY_OPENABLE);
 
 			    try {
 			        CONTEXT.startActivityForResult(Intent.createChooser(intent, "Select a music source"), Configuration.FILE_CHOOSER_IMAGE_RESPONSE_CODE);
 			    } catch (android.content.ActivityNotFoundException ex) {
 			        Toast.makeText(CONTEXT, "You need to install a file manager in order to use this functionality.", Toast.LENGTH_SHORT).show();
 			    }
 			}
 		});
 		
 		imageSourceLayout.addView(imageSourceButton, new LayoutParams(SELECT_BUTTON_WIDTH, LayoutParams.WRAP_CONTENT));
 		
 		addRow(contentLayout, "Show image from", imageSourceLayout);
 		
 		//add button
 		Button addButton = new Button(CONTEXT);
 		addButton.setText("Add event");
 		
 		addButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				String eventTitle = titleTextBox.getText().toString();
 				String eventMusicSource = musicSourceTextBox.getText().toString();
 				String eventImageSource = imageSourceTextBox.getText().toString();
 				
 				if(eventTitle.trim().length() == 0) {
 					eventTitle = getDefaultEventTitle();
 				}
 				
 				if(!new File(eventMusicSource).exists()) {
 					eventMusicSource = "";
 				}
 				
 				if(!new File(eventImageSource).exists()) {
 					eventImageSource = "";
 				}
 				
 				long start = Util.GetMillisecondsOfDay(startWheelHours.getCurrentItem(), startWheelMinutes.getCurrentItem(), 0);
 				long end = Util.GetMillisecondsOfDay(endWheelHours.getCurrentItem(), endWheelMinutes.getCurrentItem(), 0);
 				
 				if(end < start) {
 					end = start;
 				}
 				
 				//Create the event
 				Event event = new Event(eventTitle, start, end);
 				
 				//Add repetition entries
 				for(int i = 0; i < repetitionCheckBoxes.length; i++) {
 					event.setRepetition(i, repetitionCheckBoxes[i].isChecked());
 				}
 				
 				//Add event sources
 				if(eventMusicSource.length() > 0) {
 					event.addEventSource(new EventSource(SourceType.Music, eventMusicSource));
 				}
 				
 				if(eventMusicSource.length() > 0) {
 					event.addEventSource(new EventSource(SourceType.Image, eventImageSource));
 				}
 				
 				//Add the event to the scheduler
 				EVENT_SCHEDULER.addEvent(event);
 				
 				dismiss();
 			}
 		});
 		
 		contentLayout.addView(addButton, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 		
 		//Make sure that the end time is always > than the start time
 		OnWheelChangedListener wheelChangeListener = new OnWheelChangedListener() {
 			public void onChanged(WheelView wheel, int oldValue, int newValue) {
 				final int startHour = startWheelHours.getCurrentItem();
 				final int startMinute = startWheelMinutes.getCurrentItem();
 				
 				final int endHour = endWheelHours.getCurrentItem();
 				final int endMinute = endWheelMinutes.getCurrentItem();
 				
 				if(startWheelHours.getCurrentItem() > endWheelHours.getCurrentItem()) {
 					endWheelHours.setCurrentItem(startHour);
 				}
 				
 				if(startMinute > endMinute && startHour == endHour) {
 					endWheelMinutes.setCurrentItem(startMinute);
 				}
 			}
 		};
 		
 		startWheelHours.addChangingListener(wheelChangeListener);
 		startWheelMinutes.addChangingListener(wheelChangeListener);
 		endWheelHours.addChangingListener(wheelChangeListener);
 		endWheelMinutes.addChangingListener(wheelChangeListener);
 	}
 	
 	public void setMusicSource(String source) {
 		this.musicSourceTextBox.setText(source);
 	}
 	
 	public void setImageSource(String source) {
 		this.imageSourceTextBox.setText(source);
 	}
 	
 	private void addRow(LinearLayout container, String text, View control) {
 		TextView textView = new TextView(CONTEXT);
 		textView.setText(text);
 		
 		LinearLayout rowLayout = new LinearLayout(CONTEXT);
 		rowLayout.setOrientation(LinearLayout.HORIZONTAL);
 		
 		rowLayout.addView(textView, new LayoutParams(ROW_HEADER_WIDTH, LayoutParams.WRAP_CONTENT));
 		rowLayout.addView(control, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 		
 		LayoutParams entryLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 		entryLayout.topMargin = 10;
 		entryLayout.bottomMargin = 10;
 		
 		container.addView(rowLayout, entryLayout);
 	}
 	
 	private String getDefaultEventTitle() {
 		return "New event " + Event.GetNextEventId();
 	}
 
 	private int[] getNextWakeUpTime() {
 		Calendar calendar = Calendar.getInstance();
 		
 		//TODO: Access the kalendar to get next events
 		
 		return new int[] {
 			calendar.get(Calendar.HOUR_OF_DAY),
 			calendar.get(Calendar.MINUTE),
 		};
 	}
 	
 	private boolean[] getExpectedRepetition() {
 		//TODO: Access the kalendar to get next events
 		
 		boolean[] expectedRepetition = new boolean[Util.GetNumberOfWeekdays()];
 		
 		for(int i = 0; i < expectedRepetition.length; i++) {
 			expectedRepetition[i] = true;
 		}
 		
 		return expectedRepetition;
 	}
 }
