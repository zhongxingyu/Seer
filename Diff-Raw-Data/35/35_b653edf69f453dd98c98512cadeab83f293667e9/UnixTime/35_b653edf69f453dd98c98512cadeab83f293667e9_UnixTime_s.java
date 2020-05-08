 /**
  * 
  */
 package com.smmsp.time;
 
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

 /**
  * @author sean
  *
  */
 public class UnixTime implements TimeInstant{
 	
 	public static final int EPOCH_YEAR = 1970;
 	public static final int EPOCH_MONTH = 1;
 	public static final int EPOCH_DAY = 1;
 
 	private GregorianDate _date = new GregorianDate();
 	private int _hours = 0;
 	private int _minutes = 0;
 	private int _seconds = 0;
 	private int _nanos = 0;
 	
 	/**
 	 * Empty constructor - defaults to 1/1/1970
 	 */
 	public UnixTime() {
 		_date = new GregorianDate(EPOCH_YEAR, EPOCH_MONTH, EPOCH_DAY);
 	}
 
 	/**
 	 * @param _years
 	 * @param _months
 	 * @param _days
 	 */
 	public UnixTime(int _years, int _months, int _days) {
 		_date = new GregorianDate(_years, _months, _days);
 	}
 
 	/**
 	 * @param _years
 	 * @param _months
 	 * @param _days
 	 * @param _hours
 	 * @param _minutes
 	 * @param _seconds
 	 */
 	public UnixTime(int _years, int _months, int _days, int _hours,
 			int _minutes, int _seconds) {
 		this(_years, _months, _days);
 		this._hours = _hours;
 		this._minutes = _minutes;
 		this._seconds = _seconds;
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param _years
 	 * @param _months
 	 * @param _days
 	 * @param _hours
 	 * @param _minutes
 	 * @param _seconds
 	 * @param _nanos
 	 */
 	public UnixTime(int _years, int _months, int _days, int _hours,
 			int _minutes, int _seconds, int _nanos) {
 		this(_years, _months, _days);
 		this._hours = _hours;
 		this._minutes = _minutes;
 		this._seconds = _seconds;
 		this._nanos = _nanos;
 	}
 	
 
 	
 	/**
 	 * Returns the unix timestamp represented by this object
 	 * @return
 	 * @throws TimeException
 	 */
 	public long toTimestamp() throws TimeException{
 		int numYearsSinceEpoch = _date.get_years() - EPOCH_YEAR;
 		
 		if(numYearsSinceEpoch < 0){
 			throw new TimeException("Years must be >= " + EPOCH_YEAR);
 		}
 		
 		int daysSinceEpoch = 0;
 		for(int i = 0; i < numYearsSinceEpoch; ++i){
 			if(GregorianDate.isLeapYear(EPOCH_YEAR + i)){
 				daysSinceEpoch += TimeConstants.DAYS_IN_LEAP_YEAR;
 			}else{
 				daysSinceEpoch += TimeConstants.DAYS_IN_YEAR;
 			}
 		}
 		
 		daysSinceEpoch += _date.get_days() - 1;
 		int[] monthDays;
 		if(_date.isLeapYear()){
 			monthDays = TimeConstants.GREGORIAN_DAYS_IN_LEAP_MONTH;
 		}else{
 			monthDays = TimeConstants.GREGORIAN_DAYS_IN_MONTH;
 		}
 		
 		for(int i = 0; i < _date.get_months() - 1; ++i){
 			daysSinceEpoch += monthDays[i];
 		}
 		
 		int hoursInSeconds = _hours * TimeConstants.SECONDS_IN_HOUR;
 		int minutesInSeconds = _minutes * TimeConstants.SECONDS_IN_MINUTE;
 		long daysInSeconds = daysSinceEpoch * TimeConstants.SECONDS_IN_DAY;
 		
 		long seconds = daysInSeconds + hoursInSeconds + minutesInSeconds;
 		seconds += _seconds;
 		
 		return seconds;
 	}
 
 	@Override
 	public GPSTime toGPSTime() {
		throw new NotImplementedException();
 	}
 
 	@Override
 	public UnixTime toUnixTime() {
 		return this;
 	}
 
 }
