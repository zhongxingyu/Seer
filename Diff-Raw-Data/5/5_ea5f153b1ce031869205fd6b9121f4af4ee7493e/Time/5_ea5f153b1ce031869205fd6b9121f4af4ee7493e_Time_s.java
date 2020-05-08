 package com.alexrnl.commons.time;
 
 import java.io.Serializable;
 import java.sql.Date;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.alexrnl.commons.utils.object.AutoCompare;
 import com.alexrnl.commons.utils.object.AutoHashCode;
 import com.alexrnl.commons.utils.object.Field;
 
 /**
  * Class representing a time (hours and minutes).<br />
  * This time has <em>no limit</em>, which means that the number of hours may be anything (negative,
  * more than 23, etc.). However, the number of minutes is kept between 0 and 60.<br />
  * This class is immutable.
  * @author Alex
  */
 public class Time implements Serializable, Comparable<Time>, Cloneable {
 	/** Logger */
 	private static Logger		lg						= Logger.getLogger(Time.class.getName());
 	
 	/** Serial Version UID */
 	private static final long	serialVersionUID		= -8846707527438298774L;
 	
 	/** The separator between hours and minutes */
 	public static final String	TIME_SEPARATOR			= ":";
 	/** The regex pattern that matches any non decimal character */
 	public static final String	NON_DECIMAL_CHARACTER	= "[^0-9]";
 	/** Number of minutes per hour */
 	public static final int		MINUTES_PER_HOURS		= 60;
 	
 	/** The number of hours */
 	private final int			hours;
 	/** The number of minutes */
 	private final int			minutes;
 	
 	/**
 	 * Constructor #1.<br />
 	 * Default constructor, set time to midnight.
 	 */
 	public Time () {
 		this(0);
 	}
 	
 	/**
 	 * Constructor #2.<br />
 	 * Number of minutes will be set to 0.
 	 * @param hours
 	 *        the number of hours.
 	 */
 	public Time (final int hours) {
 		this(hours, 0);
 	}
 	
 	/**
 	 * Constructor #3.<br />
 	 * @param hours
 	 *        the number of hours.
 	 * @param minutes
 	 *        the number of minutes.
 	 */
 	public Time (final int hours, final int minutes) {
 		super();
 		int cHours = hours;
 		int cMinutes = minutes;
 		
 		// Correcting if minutes are below zero
 		while (cMinutes < 0) {
 			--cHours;
 			cMinutes += MINUTES_PER_HOURS;
 		}
 		
 		this.hours = cHours + cMinutes / MINUTES_PER_HOURS;
 		this.minutes = cMinutes % MINUTES_PER_HOURS;
 		
 		if (lg.isLoggable(Level.FINE)) {
 			lg.fine("Created time: " + this.hours + " h, " + this.minutes + " min");
 		}
 	}
 	
 	/**
 	 * Constructor #4.<br />
 	 * @param timeStamp
 	 *        the number of seconds since January 1st, 1970.
 	 */
 	public Time (final long timeStamp) {
 		this(new Date(timeStamp));
 	}
 	
 	/**
 	 * Constructor #5.<br />
 	 * Build the time from the date.
 	 * @param date
 	 *        the date to use.
 	 */
 	public Time (final Date date) {
 		final Calendar cal = Calendar.getInstance(Locale.getDefault());
 		cal.setTime(date);
 		hours = cal.get(Calendar.HOUR_OF_DAY);
 		minutes = cal.get(Calendar.MINUTE);
 	}
 	
 	/**
 	 * Constructor #6.<br />
 	 * Copy constructor, build a copy of the time.
 	 * @param time
 	 *        the time to use.
 	 */
 	public Time (final Time time) {
 		this(time.hours, time.minutes);
 	}
 	
 	/**
 	 * Build a time based on a string.<br />
 	 * The time format must be hours minutes seconds (in that order) separated using any
 	 * non-numerical character.<br />
 	 * @param time
 	 *        the time set.
 	 * @return the time matching the string.
 	 */
 	public static Time get (final String time) {
 		return TimeSec.get(time).getTime();
 	}
 	
 	/**
 	 * Return the current time.
 	 * @return a {@link Time} object matching the current time.
 	 */
 	public static Time getCurrent () {
 		return new Time(System.currentTimeMillis());
 	}
 	
 	/**
 	 * Return the attribute hours.
 	 * @return the attribute hours.
 	 */
 	@Field
 	public int getHours () {
 		return hours;
 	}
 	
 	/**
 	 * Return the attribute minutes.
 	 * @return the attribute minutes.
 	 */
 	@Field
 	public int getMinutes () {
 		return minutes;
 	}
 	
 	/**
 	 * Add the amount of time specified to the current time.<br />
 	 * There is no maximum, so you may reach 25:48.
 	 * @param time
 	 *        the time to add.
 	 * @return the new time.
 	 */
 	public Time add (final Time time) {
 		return new Time(hours + time.hours, minutes + time.minutes);
 	}
 	
 	/**
 	 * Subtract the amount of time specified to the current time.<br />
 	 * There is no minimum, so you may reach -2:48.
 	 * @param time
 	 *        the time to subtract.
 	 * @return the new time.
 	 */
 	public Time sub (final Time time) {
 		return new Time(hours - time.hours, minutes - time.minutes);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Comparable#compareTo(java.lang.Object)
 	 */
 	@Override
 	public int compareTo (final Time o) {
 		if (o == null) {
 			return 1;
 		}
 		if (hours > o.hours) {
 			return 1;
 		} else if (hours < o.hours) {
 			return -1;
 		}
 		if (minutes > o.minutes) {
 			return 1;
 		} else if (minutes < o.minutes) {
 			return -1;
 		}
		assert equals(o);
 		return 0;
 	}
 	
 	/**
 	 * Check if the current time is after the specified time.<br />
 	 * @param time
 	 *        the time used for reference.
 	 * @return <code>true</code> if this time is after the reference time provided.
 	 * @see #compareTo(Time)
 	 */
 	public boolean after (final Time time) {
 		return compareTo(time) > 0;
 	}
 	
 	/**
 	 * Check if the current time is before the specified time.<br />
 	 * @param time
 	 *        the time used for reference.
 	 * @return <code>true</code> if this time is before the reference time provided.
 	 * @see #compareTo(Time)
 	 */
 	public boolean before (final Time time) {
 		return compareTo(time) < 0;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString () {
 		String h = Integer.toString(hours);
 		String m = Integer.toString(minutes);
 		if (h.length() < 2) {
 			h = Integer.valueOf(0) + h;
 		}
 		if (m.length() < 2) {
 			m = Integer.valueOf(0) + m;
 		}
 		return h + TIME_SEPARATOR + m;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode () {
 		return AutoHashCode.getInstance().hashCode(this);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals (final Object obj) {
 		if (!(obj instanceof Time)) {
 			return false;
 		}
 		return AutoCompare.getInstance().compare(this, (Time) obj);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#clone()
 	 */
 	@Override
 	public Time clone () throws CloneNotSupportedException {
 		return new Time(this);
 	}
 	
 }
