 package org.pillarone.riskanalytics.domain.utils;
 
 import org.joda.time.*;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope;
 
 /**
  * Utility class for date, time and period calculations that are either
  * domain-specific, or generic but not already provided by org.joda.time classes.
  *
  * ben (dot) ginsberg (at) intuitive-collaboration (dot) com
  */
 public class DateTimeUtilities {
 
     /**
      * Converts a string of the form YYYY-MM-DD (an ISO-2014 date or ISO-8601 calendar date) to a Joda DateTime
      * @param date
      * @return
      */
     public static DateTime convertToDateTime(String date) {
         try {
             if (date.length() == 0) return null;
             return new DateTime((new SimpleDateFormat("yyyy-MM-dd")).parse(date));
         }
         catch (ParseException ex) {
             throw new RuntimeException(ex.getMessage(), ex);
         }
     }
 
     /**
      * Maps a date $t$ to a period $p \in \mathbf{Z}$ relative to a start
      * date $t_0$ and an interval length $d$ (for now, always one year).
      * Conceptually, period $p = \text{floor}(\frac{t-t_0}{d})$,
      * where $t, t_0 \& d$ have continuous time units.
      * However, although periods have equal calendrical duration, the linear time
      * duration of periods vary due to, e.g., leap years or variable month lengths.
      *
      * Periods may start anywhere within the calendar year, but they <b>must</b> last one year
      * (i.e. up to the same date on each following year, or each preceding year for negative periods).
      * 
      * Usage://todo(bgi)
      *   DateTime startOfPrevPeriod = new DateTime(2009,3,1,0,0,0,0);
      *   DateTime startOfThisPeriod = new DateTime(2010,3,1,0,0,0,0);
      *   DateTime startOfNextPeriod = new DateTime(2011,3,1,0,0,0,0);
      *   DateTime lastPointInPrevPeriod = new DateTime(2010,2,28,23,59,59,999);
      *   DateTime lastPointInThisPeriod = new DateTime(2011,2,28,23,59,59,999);
      *   DateTime lastPointInNextPeriod = new DateTime(2012,2,29,23,59,59,999);
      *   int
      *   period = DateTimeUtilities.mapDateToPeriod(lastPointInNextPeriod, startOfThisPeriod) // 1
      *   period = DateTimeUtilities.mapDateToPeriod(lastPointInPrevPeriod, startOfThisPeriod) // -1
      *
      * // todo(bgi): treat non-year periods, e.g. quarterly, monthly (rename this method to mapDateToCalendarYear)
      *  @param date
      *  @param startDate
      *  @return
      */
     public static int mapDateToPeriod(DateTime date, DateTime startDate) {
         Years years = Years.yearsBetween(startDate, date); // (today, yesterday) -> 'P0Y' (rounded integer!)
         int signedYearDiff = years.getYears();
         /**
          *  Count fractional years before startDate as whole years;
          *  i.e., compensate yearsBetween's unsigned time-difference rounding.
          *
          *  The implementation below is analagous to trying to compute the function floor() when we only have the
          *  (even/symmetric) function int(); however, the analogy overlooks the nonlinearity of calendrical periods
          */
         int startYear = startDate.getYear();
         if (date.isBefore(startDate)) {
             if (startYear > date.getYear()) {
                 /**
                  * Copy the date-to-map ('date') to the same calendar year as 'startDate' (i.e. within one period), to see
                  * whether it lies within, or before, the implied time period (the year-long interval starting at startDate)
                  */
                 MutableDateTime movedDate = new MutableDateTime(date);
                 movedDate.setYear(startYear);
                 if (movedDate.isAfter(startDate)) signedYearDiff--;
             }
             else {
                 /**
                  * Both dates are in the same calendar year, and we already know that date < startDate,
                  * so we know we need to apply the compensation
                  */
                 signedYearDiff--;
             }
         }
         return signedYearDiff;
     }
 
     /**
      * Maps a date $t$ to a fraction of period $f \in [0,1)$ representing elapsed time since start of period.
      * Currently only supports periods beginning at the beginning of a given year.
      * @param date
      * @return
      */
     public static double mapDateToFractionOfPeriod(DateTime date) {
         return ((date.dayOfYear().get() - 1) / (date.year().isLeap() ? 366d : 365d));
     }
 
     public static Period simulationPeriodLength(PeriodScope periodScope) {
         return simulationPeriodLength(periodScope.getCurrentPeriodStartDate(), periodScope.getNextPeriodStartDate());
     }
 
     public static Period simulationPeriodLength(DateTime beginOfPeriod, DateTime endOfPeriod) {
         return new Period(beginOfPeriod, endOfPeriod);
     }
 
     public static int simulationPeriod(DateTime simulationStart, DateTime endOfFirstPeriod, DateTime date) {
         Period periodLength = simulationPeriodLength(simulationStart, endOfFirstPeriod);
         return simulationPeriod(simulationStart, periodLength, date);
     }
 
     public static int simulationPeriod(DateTime simulationStart, Period periodLength, DateTime date) {
         if (periodLength.getMonths() > 0) {
             double months = new Period(simulationStart, date, PeriodType.months()).getMonths() / (double) periodLength.getMonths();
             return months < 0 ? (int) Math.floor(months) : (int) months;
         }
         else if (periodLength.getMonths() == 0 && periodLength.getYears() > 0) {
             return new Period(simulationStart, date).getYears();
         }
         throw new IllegalArgumentException("No rule implemented for " + simulationStart + ", " + periodLength + ", " + date);
     }
 
     public static double dateAsDouble(DateTime simulationStart, DateTime endOfFirstPeriod, DateTime date) {
         Period periodLength = simulationPeriodLength(simulationStart, endOfFirstPeriod);
         int period = simulationPeriod(simulationStart, periodLength, date);
         DateTime beginOfPeriodForDate = new DateTime(simulationStart);
         for (int i = 0; i < period; i++) {
            beginOfPeriodForDate = beginOfPeriodForDate.plus(periodLength);
         }
        double periodLengthInDays = Days.daysBetween(simulationStart, endOfFirstPeriod).getDays();
        return periodLengthInDays == 0 ? 0 : period + Days.daysBetween(beginOfPeriodForDate, date).getDays() / periodLengthInDays;
     }
 
     public static DateTime getDate(DateTime beginOfPeriod, DateTime endOfPeriod, int periodOffset, double fractionOfPeriod) {
         Period periodLength = simulationPeriodLength(beginOfPeriod, endOfPeriod);
         DateTime shiftedDate = new DateTime(beginOfPeriod).plusDays((int) (fractionOfPeriod * Days.daysBetween(beginOfPeriod, endOfPeriod).getDays()));
         int sign = periodOffset >= 0 ? 1 : -1;
         periodOffset = sign * periodOffset;
         Period shift = new Period();
         while (periodOffset > 0) {
             shift = shift.plus(periodLength);
             periodOffset--;
         }
         if (sign == 1) {
             shiftedDate = shiftedDate.plus(shift);
         }
         else {
             shiftedDate = shiftedDate.minus(shift);
         }
         return shiftedDate;
     }
 }
