 package se.chalmers.dat255.sleepfighter.utils;
 
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.List;
 import java.util.Locale;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeConstants;
 import org.joda.time.LocalDate;
 import org.joda.time.MutableDateTime;
 import org.joda.time.Period;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.SFApplication;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.AlarmTimestamp;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.text.SpannableString;
 import android.text.style.ForegroundColorSpan;
 import android.util.Log;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Strings;
 
 /**
  * String/text specific date utility methods.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Sep 19, 2013
  */
 public class DateTextUtils {
 	private final static String TAG = DateTextUtils.class.getSimpleName();
 
 	private static final int ENABLED_DAYS_INDICE_LENGTH = 2;
 
 	/** Joiner for time, the separator is ":". */
 	public static final Joiner TIME_JOINER = Joiner.on( ':' ).skipNulls();
 
 	/**
 	 * Joins time parts together according to {@link #TIME_JOINER}.<br/>
 	 * All the parts are padded with 1 leading zero before joining.
 	 *
 	 * @param parts the time parts to join together.
 	 * @return the time parts joined together.
 	 */
 	public static final String joinTime( Integer... parts ) {
 		String[] paddedParts = new String[parts.length];
 		for ( int i = 0; i < parts.length; ++i ) {
 			paddedParts[i] = Strings.padStart( Integer.toString( parts[i] ), 2, '0' );
 		}
 
 		return TIME_JOINER.join( paddedParts );
 	}
 
 	/**
 	 * Builds and returns text for the "exact" time an alarm occurs as opposed to the period left for it to occur.<br/>
 	 * In English, 12:00 today would become "Today 12:00", tomorrow would be come "Tomorrow 12:00", and on Monday it would become
 	 *
 	 * @param res resources bundle
 	 * @param now current time in Unix epoch timestamp.
 	 * @param ats an AlarmTimestamp: the information about the alarm & its timestamp.
 	 * @param locale the locale to use for weekdays.
 	 * @return the built time-to string.
 	 */
 	public static final String getTime( Resources res, long now, AlarmTimestamp ats, Locale locale ) {
 		String noActive = res.getStringArray( R.array.earliest_time_formats)[0];
 		String[] formats = res.getStringArray( R.array.exact_time_formats );
 		return getTime( formats, noActive, now, ats, locale );
 	}
 
 	/**
 	 * Builds and returns text for the "exact" time an alarm occurs as opposed to the period left for it to occur.<br/>
 	 * In English, 12:00 today would become "Today 12:00", tomorrow would be come "Tomorrow 12:00", and on Monday it would become
 	 *
 	 * @param formats the formats to use, e.g: [Today %1$s, Tomorrow %1$s, %2$s %1$s].
 	 * @param noActive if no alarm was active, this is used.
 	 * @param now current time in Unix epoch timestamp.
 	 * @param ats an AlarmTimestamp: the information about the alarm & its timestamp.
 	 * @param locale the locale to use for weekdays.
 	 * @return the built time-to string.
 	 */
 	public static final String getTime( String[] formats, String noActive, long now, AlarmTimestamp ats, Locale locale ) {
		Log.d( TAG, ats.toString() );
 		if ( ats == AlarmTimestamp.INVALID ) {
 			return noActive;
 		} else {
 			if ( ats.getMillis() < now ) {
 				throw new RuntimeException( "Time given is before now." );
 			}
 
 			// Prepare replacements.
 			Alarm alarm = ats.getAlarm();
 			String timeReplacement = joinTime( alarm.getHour(), alarm.getMinute() );
 
 			// Calculate start of tomorrow.
 			DateTime nowTime = new DateTime( now );
 			DateTime time = new DateTime( ats.getMillis() );
 			LocalDate tomorrow = new LocalDate( nowTime ).plusDays( 1 );
 			DateTime startOfTomorrow = tomorrow.toDateTimeAtStartOfDay( nowTime.getZone() );
 
 			if ( time.isBefore( startOfTomorrow ) ) {
 				// Alarm is today.
 				Log.d( TAG, "today" );
 				return String.format( formats[0], timeReplacement );
 			}
 
 			// Calculate start of the day after tomorrow.
 			LocalDate afterTomorrow = tomorrow.plusDays( 1 );
 			DateTime startOfAfterTomorrow = afterTomorrow.toDateTimeAtStartOfDay( nowTime.getZone() );
 
 			if ( time.isBefore( startOfAfterTomorrow ) ) {
 				Log.d( TAG, "tomorrow" );
 				// Alarm is tomorrow.
 				return String.format( formats[1], timeReplacement );
 			}
 
 			// Alarm is after tomorrow.
 			Log.d( TAG, "after tomorrow" );
 			String weekday = new DateTime( ats.getMillis() ).dayOfWeek().getAsText( locale );
 			return String.format( formats[2], timeReplacement, weekday );
 		}
 	}
 
 	/**
 	 * Builds and returns text for the time to an alarm given resources, current time, and AlarmTimestamp when given a resources bundle.
 	 *
 	 * @param res Android resources.
 	 * @param now current time in unix epoch timestamp.
 	 * @param ats an AlarmTimestamp: the information about the alarm & its timestamp.
 	 * @return the built time-to string.
 	 */
 	public static final String getTimeToText( Resources res, long now, AlarmTimestamp ats ) {
 		Period diff = null;
 		
 		// ats is invalid when all the alarms have been turned off. INVALID has the value null. 
 		// therefore, we must do a nullcheck, otherwise we get an exception. 
 		if(ats != AlarmTimestamp.INVALID) {
 			diff = new Period( now, ats.getMillis() );
 		}
 		
 		String[] formats = res.getStringArray( R.array.earliest_time_formats);
 		String[] partFormats =  res.getStringArray( R.array.earliest_time_formats_parts );
 
 		return getTimeToText( formats, partFormats, diff, ats );
 	}
 
 	/**
 	 * Builds and returns earliest text when given a resources bundle.
 	 *
 	 * @param formats an array of strings containing formats for [no-alarm-active, < minute, xx-yy-zz, xx-yy, xx]
 	 * 		where xx, yy, zz can be either day, hours, minutes (non-respectively).
 	 * @param partsFormat an array of strings containing part formats for [day, month, hour].
 	 * @param diff difference between now and ats.
 	 * @param ats an AlarmTimestamp: the information about the alarm & its timestamp.
 	 * @return the built time-to string.
 	 */
 	public static final String getTimeToText( String[] formats, String[] partFormats, Period diff, AlarmTimestamp ats ) {
 		String earliestText;
 
 		// Not real? = we don't have any alarms active.
 		if ( ats == AlarmTimestamp.INVALID ) {
 			earliestText = formats[0];
 		} else {
 			int[] diffVal = { Math.abs( diff.getDays() ), Math.abs( diff.getHours() ), Math.abs( diff.getMinutes() ) };
 
 			// What fields are set?
 			BitSet setFields = new BitSet(3);
 			setFields.set( 0, diffVal[0] != 0 );
 			setFields.set( 1, diffVal[1] != 0 );
 			setFields.set( 2, diffVal[2] != 0 );
 			int cardinality = setFields.cardinality();
 
 			earliestText = formats[cardinality + 1];
 
 			if ( cardinality > 0 ) {
 				List<String> args = new ArrayList<String>(3);
 
 				for (int i = setFields.nextSetBit(0); i >= 0; i = setFields.nextSetBit(i + 1) ) {
 					args.add( partFormats[i] );
 				}
 
 				// Finally format everything.
 				earliestText = String.format( earliestText, args.toArray() );
 
 				earliestText = String.format( earliestText, diffVal[0], diffVal[1], diffVal[2] );
 			} else {
 				// only seconds remains until it goes off.
 				earliestText = formats[1];
 			}
 		}
 
 		return earliestText;
 	}
 
 	/**
 	 * Returns an array of strings with weekday names.
 	 *
 	 * @param indiceLength how long each string should be.
 	 * @param locale the desired locale.
 	 * @return the array of strings.
 	 */
 	public static final String[] getWeekdayNames( int indiceLength, Locale locale ) {
 		DateTimeFormatter fmt = DateTimeFormat.forPattern( Strings.repeat( "E", indiceLength ) ).withLocale( locale );
 
 		MutableDateTime time = new MutableDateTime();
 		time.setDayOfWeek( DateTimeConstants.MONDAY );
 
 		String[] names = new String[DateTimeConstants.DAYS_PER_WEEK];
 
 		for ( int day = 0; day < DateTimeConstants.DAYS_PER_WEEK; day++ ) {
 			String name = fmt.print( time );
 
 			if ( name.length() > indiceLength ) {
 				name = name.substring( 0, indiceLength );
 			}
 
 			names[day] = name;
 
 			time.addDays( 1 );
 		}
 
 		return names;
 	}
 
 	/**
 	 * Returns a SpannableString of the enabled days in an alarm,<br/>
 	 * where the days are differently colored if enabled/disabled.
 	 *
 	 * @param alarm the alarm to make text for.
 	 * @return the string.
 	 */
 	public static final SpannableString makeEnabledDaysText( final Alarm alarm ) {
 		// Compute weekday names & join.
 		String[] names = DateTextUtils.getWeekdayNames( ENABLED_DAYS_INDICE_LENGTH, Locale.getDefault() );
 
 		SpannableString text = new SpannableString( StringUtils.WS_JOINER.join( names ) );
 
 		// Create spans for enabled days.
 		boolean[] enabledDays = alarm.getEnabledDays();
 		if ( enabledDays.length != names.length || names.length != 7 ) {
 			throw new AssertionError("A week has 7 days, wrong array lengths!");
 		}
 
 		int enabledColor = Color.WHITE;
 		int disabledColor = SFApplication.get().getResources().getColor( R.color.nearly_background_text );
 
 		int start = 0;
 		for ( int i = 0; i < enabledDays.length; i++ ) {
 			boolean enabled = enabledDays[i];
 			int length = names[i].length();
 
 			int color = enabled ? enabledColor : disabledColor;
 			text.setSpan( new ForegroundColorSpan( color ), start, start + length, 0 );
 
 			start += length + 1;
 		}
 
 		return text;
 	}
 
 	/**
 	 * Construction forbidden.
 	 */
 	private DateTextUtils() {
 	}
 }
