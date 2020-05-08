 /**
  * Copyright (C) 2012 Emil Edholm, Emil Johansson, Johan Andersson, Johan Gustafsson
  * 
  * This file is part of dat255-bearded-octo-lama
  *
  *  dat255-bearded-octo-lama is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  dat255-bearded-octo-lama is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with dat255-bearded-octo-lama.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package it.chalmers.dat255_bearded_octo_lama.activities.notifications;
 
 import it.chalmers.dat255_bearded_octo_lama.R;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.widget.TextView;
 
 
 public class TextNotification extends NotificationDecorator {
 	
 	private TextView currentTimeView, currentDateView;
 	private final Activity activity;
 	/**
 	 * @param decoratedNotification is an Alarm that is decorated with different notifications
 	 * @param act is the activity that launches the notifications and contains the clock
 	 */
 	public TextNotification(Notification decoratedNotification, Activity act) {
 		super(decoratedNotification);
 		this.activity = act;
 		
 	}
 	
 	@Override
 	public void start() {
 		super.start();
 		currentTimeView = (TextView) activity.findViewById(R.id.currentTime);
 	    currentDateView = (TextView) activity.findViewById(R.id.currentDate);
 		setClock();
 	}
 	
 	@Override
	public void stop() { 
	super.stop();	
	/* No need to do anything */ }
 	
 	private void setClock() {
 		//TODO: Do a cleaner and better version of this.
 		String currentTimeString = new SimpleDateFormat("HH:mm").format(new Date());
 		String currentDateString = DateFormat.getDateInstance().format(new Date());
 		
 		currentTimeView.setText(currentTimeString);
 		currentDateView.setText(currentDateString);
 	}
 	
 }
