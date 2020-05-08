 package se.chalmers.dat255.sleepfighter.utils;
 
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.List;
 import java.util.Locale;
 
 import org.joda.time.DateTimeConstants;
 import org.joda.time.MutableDateTime;
 import org.joda.time.Period;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.AlarmTimestamp;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.text.SpannableString;
 import android.text.style.ForegroundColorSpan;
 
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
 	private static final int ENABLED_DAYS_INDICE_LENGTH = 2;
 
 	/** Joiner for time, the separator is ":". */
 	public static final Joiner TIME_JOINER = Joiner.on( ':' ).skipNulls();
 
 	/**
 	 * Joins time parts together according to {@link #TIME_JOINER}.
 	 *
 	 * @param parts the time parts to join together.
 	 * @return the time parts joined together.
 	 */
 	public static final String joinTime( Integer... parts ) {
 		return TIME_JOINER.join( parts );
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
 		if(ats !=  AlarmTimestamp.INVALID) {
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
 
 	public static final SpannableString makeEnabledDaysText( final Alarm alarm ) {
 		// Compute weekday names & join.
 		String[] names = DateTextUtils.getWeekdayNames( ENABLED_DAYS_INDICE_LENGTH, Locale.getDefault() );
 
 		SpannableString text = new SpannableString( StringUtils.WS_JOINER.join( names ) );
 
 		// Create spans for enabled days.
 		boolean[] enabledDays = alarm.getEnabledDays();
 		if ( enabledDays.length != names.length || names.length != 7 ) {
 			throw new AssertionError("A week has 7 days, wrong array lengths!");
 		}
 
 		int start = 0;
 		for ( int i = 0; i < enabledDays.length; i++ ) {
 			boolean enabled = enabledDays[i];
 			int length = names[i].length();
 
			if ( enabled ) {
				text.setSpan( new ForegroundColorSpan( Color.WHITE ), start, start + length, 0 );
			}
 
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
