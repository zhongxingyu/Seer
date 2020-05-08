 package fr.mvanbesien.calendars.calendars;
 
 import java.util.Calendar;
 
 import fr.mvanbesien.calendars.dates.DarianDate;
 
 /**
  * Displays a Mars Darian Calendar Date.
  * 
  * @author mvanbesien
  * 
  */
 public class DarianCalendar {
 
 	/**
 	 * Days difference with Unix Epoch
 	 */
 	private static final float UNIX_EPOCH = 719527;
 
 	/**
 	 * Ration between days length on mars and on earth
 	 */
 	private static final float DAY_RATIO = 1.027491251f;
 
 	/**
 	 * Epoch since beginning of Darian calendar.
 	 */
 	private static final float DARIAN_EPOCH = 587744.77817f;
 
 	/**
 	 * @return Darian Date corresponding to now.
 	 */
 	public static DarianDate getDate() {
 		return getDate(Calendar.getInstance());
 	}
 
 	/**
 	 * @return Darian Date corresponding to calendar passed as parameter..
 	 */
 	public static DarianDate getDate(Calendar calendar) {
 
 		// Computation of Sols
 		double elapsedDays = new Double(calendar.getTimeInMillis()) / 86400000 + UNIX_EPOCH;
 		double elapsedSols = (elapsedDays - DARIAN_EPOCH) / DAY_RATIO;
 
 		// First decomposition, based on 500yr basis
 		int halfMilleniums = (int) (elapsedSols / 334296);
 		int halfMilleniumReminder = (int) (elapsedSols - halfMilleniums * 334296);
 
 		// Second decomposition, based on 100yr basis
 		int centuries = halfMilleniumReminder != 0 ? (halfMilleniumReminder - 1) / 66859 : 0;
 		int centuryReminder = halfMilleniumReminder - (centuries != 0 ? centuries * 66859 + 1 : 0);
 
 		// Third decomposition, based on 10yr basis
		int decadeLeapDayValue = centuries == 0 ? 1 : 0;
 		int decades = (centuryReminder + decadeLeapDayValue) / 6686;
 		int decadeReminder = centuryReminder - (decades != 0 ? decades * 6686 - decadeLeapDayValue : 0);
 
 		// Fourth decomposition, based on 2yr basis
 		int twoYrsLeapDayValue = centuries != 0 && decades == 0 ? 0 : 1;
 		int twoYrsPeriod = (decadeReminder - twoYrsLeapDayValue) / 1337;
 		int twoYrsPeriodReminder = decadeReminder - (twoYrsPeriod != 0 ? twoYrsPeriod * 1337 + twoYrsLeapDayValue : 0);
 
 		// Fifth and last decomposition, based on yearly basis
 		int yearLeapDayValue = twoYrsPeriod == 0 && (decades != 0 || (decades == 0 && centuries == 0)) ? 0 : 1;
 		int year = (twoYrsPeriodReminder + yearLeapDayValue) / 669;
 		int yearReminder = twoYrsPeriodReminder - (year != 0 ? 669 - yearLeapDayValue : 0);
 
 		// Now, we put all together to compute the date...
 		int years = 500 * halfMilleniums + 100 * centuries + 10 * decades + 2 * twoYrsPeriod + year;
 
 		int quarter = yearReminder / 167 < 4 ? yearReminder / 167 : 3;
 		int solInQuarter = yearReminder - 167 * quarter;
 		int monthInQuarter = solInQuarter / 28;
 
 		int month = monthInQuarter + 6 * quarter;
 		int sol = yearReminder - monthInQuarter * 28 - quarter + 1;
 		DarianDate date = new DarianDate();
 		date.setOrdinaryDate(0, sol, month, years);
 		
 		return date;
 	}

 }
