 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.toxbee.sleepfighter.adapter;
 
 import java.util.List;
 
 import se.toxbee.sleepfighter.R;
 import se.toxbee.sleepfighter.android.component.secondpicker.SecondTimePicker;
 import se.toxbee.sleepfighter.android.component.secondpicker.SecondTimePickerDialog;
 import se.toxbee.sleepfighter.model.Alarm;
 import se.toxbee.sleepfighter.model.time.AlarmTime;
 import se.toxbee.sleepfighter.model.time.CountdownTime;
 import se.toxbee.sleepfighter.model.time.ExactTime;
 import se.toxbee.sleepfighter.text.DateTextUtils;
 import se.toxbee.sleepfighter.utils.string.StringUtils;
 import android.app.TimePickerDialog;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 public class AlarmAdapter extends ArrayAdapter<Alarm> {
 	public AlarmAdapter( Context context, List<Alarm> alarms ) {
 		super( context, 0, alarms );
 	}
 
 	private static class ViewHolder {
 		TextView name;
 		View timeContainer;
 		TextView time;
 		TextView seconds;
 		TextView weekdays;
 		CompoundButton activatedSwitch;
 		View activatedBackground;
 
 		public ViewHolder( View cv ) {
 			// Find all views needed.
 			name = (TextView) cv.findViewById( R.id.name_view );
 			timeContainer = cv.findViewById( R.id.time_view_container );
 			time = (TextView) cv.findViewById( R.id.time_view );
 			seconds = (TextView) cv.findViewById( R.id.time_view_seconds );
 			weekdays = (TextView) cv.findViewById( R.id.weekdaysText );
 			activatedSwitch = (CompoundButton) cv.findViewById( R.id.activated );
 			activatedBackground = cv.findViewById( R.id.activated_background );
 		}
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder;
 
 		if (convertView == null) {
 			// A view isn't being recycled, so make a new one from definition
			convertView = LayoutInflater.from( getContext() ).inflate( R.layout.alarm_list_item, parent, false );

 			// Make & store holder.
 			holder = new ViewHolder( convertView );
 			convertView.setTag( holder );
 		} else {
 			// Recycle holder.
 			holder = (ViewHolder) convertView.getTag();
 		}
 
 		// Get alarm associated with the row.
 		final Alarm alarm = this.getItem( position );
 
 		// Setup the view.
 		this.makeTimeTextViewHitboxBigger( holder );
 		this.setupActivatedSwitch( alarm, holder );
 		this.setupTimeText( alarm, holder );
 		this.setupName( alarm, holder );
 		this.setupWeekdays( alarm, holder );
 
 		return convertView;
 	}
 
 	private void makeTimeTextViewHitboxBigger( final ViewHolder holder ) {
 		holder.timeContainer.setOnClickListener( new OnClickListener() {
 			@Override
 			public void onClick( View v ) {
 				holder.time.performClick();
 			}
 		} );
 	}
 
 	public void pickTime( final Alarm alarm ) {
 		if ( alarm.isCountdown() ) {
 			this.pickCountdownTime( alarm );
 		} else {
 			this.pickNormalTime( alarm );
 		}
 	}
 
 	public void pickNormalTime( final Alarm alarm ) {
 		OnTimeSetListener onTimePickerSet = new OnTimeSetListener() {
 			@Override
 			public void onTimeSet(TimePicker view, int h, int m ) {
 				alarm.setTime( new ExactTime( h, m ) );
 			}
 		};
 
 		// TODO possibly use some way that doesn't make the dialog close on rotate
 		AlarmTime time = alarm.getTime();
 		TimePickerDialog tpd = new TimePickerDialog(
 			getContext(), onTimePickerSet,
 			time.getHour(), time.getMinute(),
 			true
 		);
 
 		tpd.show();
 	}
 
 	public void pickCountdownTime( final Alarm alarm ) {
 		SecondTimePickerDialog.OnTimeSetListener onTimePickerSet = new SecondTimePickerDialog.OnTimeSetListener() {
 			@Override
 			public void onTimeSet( SecondTimePicker view, int h, int m, int s ) {
 				alarm.setTime( new CountdownTime( h, m, s ) );
 			}
 		};
 
 		// TODO possibly use some way that doesn't make the dialog close on rotate
 		AlarmTime time = alarm.getTime();
 		time.refresh();
 
 		SecondTimePickerDialog tpd = new SecondTimePickerDialog(
 			getContext(), onTimePickerSet,
 			time.getHour(), time.getMinute(), time.getSecond(),
 			true
 		);
 
 		tpd.show();
 	}
 
 	private void setupTimeText( final Alarm alarm, ViewHolder holder ) {
 		AlarmTime time = alarm.getTime();
 		time.refresh();
 
 		// Set countdown if needed.
 		if ( alarm.isCountdown() ) {
 			holder.seconds.setVisibility( View.VISIBLE );
 			holder.seconds.setText( StringUtils.joinTime( time.getSecond() ) + "\"" );
 		} else {
 			holder.seconds.setVisibility( View.GONE );
 		}
 
 		holder.time.setText( time.getTimeString() );
 		holder.time.setOnClickListener( new OnClickListener() {
 			public void onClick( View v ) {
 				pickTime( alarm );
 			}
 		});
 	}
 
 	private void setupName( final Alarm alarm, ViewHolder holder ) {
 		holder.name.setText( alarm.printName() );
 	}
 
 	private void setupWeekdays( final Alarm alarm, ViewHolder holder ) {
 		holder.weekdays.setText( DateTextUtils.makeEnabledDaysText( alarm ) );
 	}
 
 	private void setupActivatedSwitch( final Alarm alarm, final ViewHolder holder ) {
 		final CompoundButton activated = holder.activatedSwitch;
 
 		// Makes sure that previous alarm for a recycled view won't get changed
 		// when setting initial value
 		activated.setOnCheckedChangeListener( null );
 		activated.setChecked( alarm.isActivated() );
 		activated.setOnCheckedChangeListener( new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) {
 				alarm.setActivated( isChecked );
 			}
 		} );
 
 		// Allow pressing in area next to checkbox/switch to toggle
 		holder.activatedBackground.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				activated.toggle();
 			}
 		});
 	}
 }
