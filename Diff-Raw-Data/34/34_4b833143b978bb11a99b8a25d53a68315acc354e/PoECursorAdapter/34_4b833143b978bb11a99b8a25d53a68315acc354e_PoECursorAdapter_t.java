 package com.tywholland.poeevents;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Paint;
 import android.net.Uri;
 import android.support.v4.widget.CursorAdapter;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PoECursorAdapter extends CursorAdapter {
 	public PoECursorAdapter(Context context, Cursor cursor, int flags) {
 		super(context, cursor, flags);
 	}
 
 	@Override
 	public void bindView(View view, final Context context, final Cursor cursor) {
 		// Get data
 		final String eventName = cursor.getString(cursor
 				.getColumnIndexOrThrow(PoEEvent.TAG_EVENT_NAME));
 		final String normalName = cursor.getString(cursor
 				.getColumnIndexOrThrow(PoEEvent.TAG_NORMAL_NAME));
 		final String eventShortName = cursor.getString(cursor
 				.getColumnIndexOrThrow(PoEEvent.TAG_CLASSIFICATION));
 		final String startTime = cursor.getString(cursor
 				.getColumnIndexOrThrow(PoEEvent.TAG_START_TIME));
 		final String endTime = cursor.getString(cursor
 				.getColumnIndexOrThrow(PoEEvent.TAG_END_TIME));
 		final String regTime = cursor.getString(cursor
 				.getColumnIndexOrThrow(PoEEvent.TAG_REGISTER_TIME));
 		final int alert = cursor.getInt(cursor
 				.getColumnIndexOrThrow(PoEEvent.TAG_ALERT));
		
 		// Get views
 		TextView eventNameView = (TextView) view.findViewById(R.id.event_name);
 		TextView eventShortNameView = (TextView) view
 				.findViewById(R.id.event_short_name);
 		TextView startTimeView = (TextView) view
 				.findViewById(R.id.event_start_time);
 		TextView endTimeView = (TextView) view
 				.findViewById(R.id.event_end_time);
 		final CheckBox checkBox = (CheckBox) view
 				.findViewById(R.id.event_checkbox);
 
 		// Set views
 		eventNameView.setText(normalName);
 		eventNameView.setPaintFlags(eventNameView.getPaintFlags()
 				| Paint.UNDERLINE_TEXT_FLAG);
 		if (TextUtils.isEmpty(eventShortName)) {
 			eventShortNameView.setVisibility(View.GONE);
 		} else {
 			eventShortNameView.setText(eventShortName);
 		}
 		checkBox.setChecked(alert == 1);
 
 		// Convert times to local
 		startTimeView.setText(PoEUtil.getLocalTimeString(startTime, context));
 		endTimeView.setText(PoEUtil.getLocalTimeString(endTime, context));
 
 		// Add forum link on click
 		final String webLink = cursor.getString(cursor
 				.getColumnIndexOrThrow(PoEEvent.TAG_WEB_LINK));
 		if (!TextUtils.isEmpty(webLink)) {
 			final Intent forumLinkIntent = new Intent(Intent.ACTION_VIEW,
 					Uri.parse(webLink));
 			eventNameView.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					context.startActivity(forumLinkIntent);
 				}
 			});
 		} else {
 			eventNameView.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Toast.makeText(context, R.string.no_forum_link,
 							Toast.LENGTH_SHORT).show();
 				}
 			});
 		}
 		view.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				checkBox.toggle();
 				handleEventAlertToggle(eventName, normalName, startTime,
 						webLink, regTime, checkBox.isChecked(), context);
 			}
 		});
 		checkBox.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				handleEventAlertToggle(eventName, normalName, startTime,
 						webLink, regTime, ((CheckBox) v).isChecked(), context);
 			}
 		});
 	}
 
 	private void handleEventAlertToggle(final String eventName,
 			String normalName, String startTime, String webLink,
 			String regTime, final boolean enabled, final Context context) {
 		// Update database
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				new PoEEventsDataSource(context).updateAlertOnEvent(eventName,
 						enabled);
 			}
 		}).start();
 		// Set or cancel notification alarm
 		Intent service = new Intent(context, EventAlarmService.class);
 		service.putExtra(EventAlarmService.NAME_EXTRA, normalName);
 		service.putExtra(EventAlarmService.START_TIME_EXTRA, startTime);
 		service.putExtra(EventAlarmService.WEB_LINK_EXTRA, webLink);
 		if (enabled) {
 			// Create alarm
 			service.setAction(EventAlarmService.CREATE_SINGLE);
 			service.putExtra(EventAlarmService.REG_TIME_EXTRA, regTime);
 		} else {
 			// Cancel alarm
 			service.setAction(EventAlarmService.CANCEL_SINGLE);
 
 		}
 		context.startService(service);
 	}
 
 	@Override
 	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
 		return LayoutInflater.from(context).inflate(R.layout.event_listitem,
 				viewGroup, false);
 	}
 }
