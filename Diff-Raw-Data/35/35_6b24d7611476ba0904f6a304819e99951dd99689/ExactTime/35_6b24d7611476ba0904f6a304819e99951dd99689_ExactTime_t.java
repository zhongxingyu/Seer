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
 package se.toxbee.sleepfighter.model.time;
 
 import org.joda.time.DateTimeConstants;
 import org.joda.time.MutableDateTime;
 
 import se.toxbee.sleepfighter.utils.model.Codifiable;
 
 /**
  * <p>ExactTime is a time where the timestamp is actually inexact but
  * the time in a day when it occurs is set.
  * In other words: the day may vary, but the time in the day is exact.</p>
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Nov 16, 2013
  */
 public class ExactTime extends AlarmTime implements Codifiable {
 	public static class Factory implements Codifiable.Factory {
 		@Override
 		public ExactTime produce( Integer key ) {
 			return new ExactTime( key );
 		}
 	}
 
 	public static final int MAX_WEEK_LENGTH = DateTimeConstants.DAYS_PER_WEEK;
 	public static final int MAX_WEEK_INDEX = MAX_WEEK_LENGTH - 1;
 
 	/**
 	 * Copy constructor.
 	 *
 	 * @param rhs the time to copy from.
 	 */
 	public ExactTime( AlarmTime rhs ) {
 		this( rhs.hour, rhs.minute, rhs.second );
 	}
 
 	/**
 	 * Constructs time with h (hour), m (minute), s = 0.
 	 * Times are wrapped.
 	 *
 	 * @param h hour.
 	 * @param m minute.
 	 */
 	public ExactTime( int h, int m ) {
 		super( h, m, 0 );
 	}
 
 	/**
 	 * Constructs time with h (hour), m (minute), s (second).
 	 * Times are wrapped.
 	 *
 	 * @param h hour.
 	 * @param m minute.
 	 * @param s second.
 	 */
 	public ExactTime( int h, int m, int s ) {
 		super( h, m, s );
 	}
 
 	/**
 	 * Constructs time from a packed integer with bitmask format: HHHHHMMMMMMSSSSSS,
 	 * occupying a total of 16 bits.
 	 */
 	public ExactTime( int i ) {
 		this( i >> 12, (i >> 6) & 0x3F, i & 0x3F );
 	}
 
 	/**
 	 * Packs the time to an integer with format: HHHHHMMMMMMSSSSSS.
 	 *
 	 * @return the integer.
 	 */
 	@Override
 	public int toCode() {
 		return (this.hour << 12) | (this.minute << 6) | this.second;
 	}
 
 	/**
 	 * <p>Uses {@link #toCode()}</p>
 	 *
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int hashCode() {
 		// Perfect uniform hashcode.
 		return this.toCode();
 	}
 
 	@Override
 	protected boolean equalsImpl( AlarmTime t ) {
 		return this.hashCode() == t.hashCode();
 	}
 
 	@Override
	public long scheduledTimestamp( long now, Object... inject ) {
 		// Fetch enabledDays from injected parameters, expected at index 0.
 		boolean[] enabledDays = (boolean[]) inject[0];
 
 		/*
 		 * Compute a UNIX epoch timestamp after "now",
 		 * offset to the time of this {@link AlarmTime}
 		 * and the first in enabledDays to be true.
 		 */
 		MutableDateTime t = new MutableDateTime( now );
 		t.setHourOfDay( this.hour );
 		t.setMinuteOfHour( this.minute );
 		t.setSecondOfMinute( this.second );
 
 		// Move to next day if it happened before now.
 		if ( t.isBefore( now ) ) {
 			t.addDays( 1 );
 		}
 
 		// Offset for weekdays
 		int offset = 0;
 
 		// First weekday to check (0-6), getDayOfWeek returns (1-7)
 		int weekday = t.getDayOfWeek() - 1;
 
 		// Find the weekday the alarm should run, should at most run seven times
 		for ( int i = 0; i < 7; ++i ) {
 			// Wrap to first weekday
 			if ( weekday > MAX_WEEK_INDEX ) {
 				weekday = 0;
 			}
 
 			if ( enabledDays[weekday] ) {
 				// We've found the closest day the alarm is enabled for
 				offset = i;
 				break;
 			}
 
 			++weekday;
 			++offset;
 		}
 
 		if ( offset > 0 ) {
 			t.addDays( offset );
 		}
 
 		return t.getMillis();
 	}
 
 	@Override
 	public void refresh() {
 		// Nothing to refresh.
 	}
 }
