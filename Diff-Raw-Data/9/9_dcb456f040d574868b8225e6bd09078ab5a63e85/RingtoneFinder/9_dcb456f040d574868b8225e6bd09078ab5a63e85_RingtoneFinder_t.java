 /**
  * Copyright (C) 2012 Emil Edholm, Emil Johansson, Johan Andersson, Johan Gustafsson
<<<<<<< HEAD
  * 
=======
 *
>>>>>>> ea91234303ed22065e0dd3d88803576708d4ef3f
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
 package it.chalmers.dat255_bearded_octo_lama.utilities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 
 /**
  * Class giving utilities to handle Ringtones on the device.
  * 
  */
 public class RingtoneFinder {
 
 	/**	Provides a list of the devices possible ringtones (including alarm and notification sounds)
 	 * 
 	 * @param currentActivity - Current activity to carry out resource gathering
 	 * @return - list of ringtones.
 	 */
 	public static List<Ringtone> getRingtones(Activity currentActivity){	
 		//TODO: Cleaner implementation with activity
 		RingtoneManager rm = new RingtoneManager(currentActivity);
 		List<Ringtone> tones = new ArrayList<Ringtone>();
 		Cursor c = rm.getCursor();
 		c.moveToFirst();
 		Ringtone tone;
 		while(!c.isAfterLast()){
 			tone = rm.getRingtone(c.getPosition());
 			//Adding ringtone at current cursor position
 			if(!tones.contains(tone)){
 				tones.add(rm.getRingtone(c.getPosition()));
 			}
 			c.moveToNext();
 		}
 		return tones;
 	}
 	
 	/**
 	 * Takes a ringtone and returns it's URI.
 	 * @param currentActivity - Activity to carry out the resource gathering.
 	 * @param tone - Ringtone to find URI of.
 	 * @return Uri - Uri of ringtone if found, otherwise null.
 	 */
 	public static Uri findRingtoneUri(Activity currentActivity, Ringtone tone){
 		RingtoneManager rm = new RingtoneManager(currentActivity);
 		Cursor c = rm.getCursor();
 		c.moveToFirst();
 		
 		Ringtone current;
 		while(!c.isAfterLast()){
 			current = rm.getRingtone(c.getPosition());
 			if(current.getTitle(currentActivity).equals(
 					(tone).getTitle(currentActivity))){
 				return rm.getRingtoneUri(c.getPosition());
 			}
 			c.moveToNext();
 		}
 		return null;
 	}
 	
 }
