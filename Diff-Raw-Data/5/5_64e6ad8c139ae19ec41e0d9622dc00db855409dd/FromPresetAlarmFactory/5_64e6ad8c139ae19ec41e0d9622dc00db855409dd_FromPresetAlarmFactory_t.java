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
 package se.chalmers.dat255.sleepfighter.factory;
 
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 
 /**
  * FromPresetAlarmFactory creates an Alarm from a preset alarm object.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Oct 4, 2013
  */
 public class FromPresetAlarmFactory implements AlarmFactory {
 	private Alarm preset;
 
 	/**
 	 * Constructs a FromPresetAlarmFactory given a preset.
 	 *
 	 * @param preset the preset to use.
 	 */
 	public FromPresetAlarmFactory( Alarm preset ) {
 		if ( !preset.isPresetAlarm() ) {
 			throw new IllegalArgumentException( "Alarm provided is not a preset." );
 		}
 
 		this.preset = preset;
 	}
 
 	@Override
 	public Alarm createAlarm() {
		Alarm alarm = new Alarm( this.preset );
		alarm.setIsPresetAlarm( false );

		return alarm;
 	}
 
 	/**
 	 * Returns the preset if need be.
 	 *
 	 * @return the preset.
 	 */
 	public Alarm getPreset() {
 		return this.preset;
 	}
 }
