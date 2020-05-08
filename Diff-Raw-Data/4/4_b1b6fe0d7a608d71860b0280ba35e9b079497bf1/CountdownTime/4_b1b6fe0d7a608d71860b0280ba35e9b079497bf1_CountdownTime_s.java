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
 
 import org.joda.time.MutableDateTime;
 import org.joda.time.Period;
 
 import se.toxbee.sleepfighter.utils.model.CodifiableLong;
 
 import com.google.common.base.Preconditions;
 import com.google.common.primitives.Longs;
 
 /**
  * <p>CountdownTime is a time occurring at an exact UNIX epoch timestamp.<br/>
  * However, the values of {@link #getHour()}, {@link #getMinute()},<br/>
  * {@link #getSecond()} are counting down all the time.</p>
  *
  * <p>To store the time, a long is at least required.</p>
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Nov 16, 2013
  */
 public class CountdownTime extends AlarmTime implements CodifiableLong {
 	public static class Factory implements CodifiableLong.Factory {
 		@Override
 		public CountdownTime produce( Long key ) {
 			return new CountdownTime( key );
 		}
 	}
 
 	private final long timestamp;
 
 	/**
 	 * Copies a CountdownTime, if is null, null is returned.
 	 *
 	 * @param rhs the time to copy.
 	 * @return the copied time.
 	 */
 	public static CountdownTime copy( CountdownTime rhs ) {
 		return rhs == null ? null : new CountdownTime( rhs );
 	}
 
 	/**
 	 * Copy constructor.
 	 *
 	 * @param rhs the time to copy from.
 	 */
 	public CountdownTime( CountdownTime rhs ) {
 		super( rhs );
 		this.timestamp = rhs.timestamp;
 	}
 
 	/**
 	 * Constructs time counting down to h (hour), m (minute), s = 0.
 	 * Times are wrapped.
 	 *
 	 * @param h hour.
 	 * @param m minute.
 	 */
 	public CountdownTime( int h, int m ) {
 		this( h, m, 0 );
 	}
 
 	/**
 	 * Constructs time counting down to h (hour), m (minute), s (second).
 	 * Times are wrapped.
 	 *
 	 * @param h hour.
 	 * @param m minute.
 	 * @param s second.
 	 */
 	public CountdownTime( int h, int m, int s ) {
 		super( h, m, s );
 
 		// Compute the timestamp.
 		this.timestamp = this.computeTimestamp();
 	}
 
 	/**
 	 * Constructs time counting down to the given UNIX epoch timestamp.
 	 * Executes {@link #refresh()} immediately.
 	 *
 	 * @param timestamp the timestamp.
 	 */
 	public CountdownTime( long timestamp ) {
 		super();
 
		Preconditions.checkArgument( timestamp < this.now() );
 		this.timestamp = timestamp;
 
 		this.refresh();
 	}
 
 	/**
 	 * Returns a UNIX epoch timestamp adding this to "now".
 	 *
 	 * @return the timestamp.
 	 */
 	private long computeTimestamp() {
 		MutableDateTime t = new MutableDateTime();
 		t.addHours( this.hour );
 		t.addMinutes( this.minute );
 		t.addSeconds( this.second );
 		return t.getMillis();
 	}
 
 	@Override
 	protected boolean equalsImpl( AlarmTime t ) {
 		return this.timestamp == ((CountdownTime) t).timestamp;
 	}
 
 	@Override
 	public int hashCode() {
 		return Longs.hashCode( this.timestamp );
 	}
 
 	@Override
 	public void refresh() {
 		Period p = new Period( this.timestamp - this.now() );
 
 		this.hour = p.getHours();
 		this.minute = p.getMinutes();
 		this.second = p.getSeconds();
 	}
 
 	private long now() {
 		return System.currentTimeMillis();
 	}
 
 	@Override
 	public long scheduledTimestamp( long now, Object... inject ) {
 		return this.timestamp;
 	}
 
 	@Override
 	public boolean canHappen( Object... inject ) {
 		return true;
 	}
 
 	@Override
 	public long toCodeLong() {
 		return this.timestamp;
 	}

 }
