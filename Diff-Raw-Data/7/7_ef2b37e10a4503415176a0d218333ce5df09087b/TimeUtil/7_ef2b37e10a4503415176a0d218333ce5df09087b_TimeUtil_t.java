 package ch.hsr.waktu.services;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import org.apache.log4j.Logger;
 
 import com.trolltech.qt.core.QDate;
 import com.trolltech.qt.core.QDateTime;
 import com.trolltech.qt.core.QTime;
 
 public class TimeUtil {
 	private static Logger logger = Logger.getLogger(TimeUtil.class);
	
 	//TODO: Konvertier-Methoden refactoren gemss DKellers Aussage..
 	public static QDateTime convertGregorianToQDateTime(GregorianCalendar dateTime) {
		QDate date = new QDate(dateTime.get(GregorianCalendar.YEAR), dateTime.get(GregorianCalendar.MONTH)+1, dateTime.get(GregorianCalendar.DAY_OF_MONTH));
 		QTime time = new QTime(dateTime.get(GregorianCalendar.HOUR_OF_DAY), dateTime.get(GregorianCalendar.MINUTE), dateTime.get(GregorianCalendar.SECOND));
 		return new QDateTime(date, time);
 	}
 	
 	public static GregorianCalendar convertQDateTimeToGregorian(QDateTime dateTime) {
 		QDate date = dateTime.date();
 		QTime time = dateTime.time();
		GregorianCalendar gregCal = new GregorianCalendar(date.year(), date.month()-1, date.day(), time.hour(), time.minute(), time.second());
 		logger.info(gregCal.get(Calendar.DAY_OF_MONTH) + "." + gregCal.get(Calendar.MONTH) + "." + gregCal.get(Calendar.YEAR));
 		logger.info(gregCal.get(Calendar.HOUR) + ":" + gregCal.get(Calendar.MINUTE));
 		return gregCal;
 	}
 	
 	public static int calculateTimespanInSeconds(GregorianCalendar timeBefore, GregorianCalendar timeAfter) {
 		return calculateTimespanInSeconds(convertGregorianToQDateTime(timeBefore), convertGregorianToQDateTime(timeAfter));
 	}
 	
 	public static int calculateTimespanInSeconds(QDateTime timeBefore, QDateTime timeAfter) {
 		return timeBefore.secsTo(timeAfter);
 	}
 
 	public static GregorianCalendar convertQDateToGregorian(QDate date) {
 		return new GregorianCalendar(date.year(), date.month(), date.day());
 	}
 	
 }
