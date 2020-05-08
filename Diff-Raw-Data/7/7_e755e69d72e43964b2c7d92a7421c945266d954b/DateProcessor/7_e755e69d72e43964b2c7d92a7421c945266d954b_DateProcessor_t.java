 package com.roshka.raf.params;
 
import java.net.HttpURLConnection;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.roshka.raf.exception.RAFException;
 
 public class DateProcessor {
 	
 	private static Map<String, SimpleDateFormat> _formats;
 	
 	static 
 	{
 		_formats = new ConcurrentHashMap<String, SimpleDateFormat>();
 	}
 	
 	private static void addFormat(String parameterName, String format)
 		throws RAFException
 	{
 		if (format == null) {
 			throw new RAFException(RAFException.ERRCODE_INVALID_PARAMETER_DEFINITION, String.format(
 					"Parameter [%s] has an invalid definition. Null formats are not allowed.", parameterName));
 		}
 		try {
 			SimpleDateFormat newDateFormat = new SimpleDateFormat(format);
 			_formats.put(format, newDateFormat);
 		} catch (IllegalArgumentException e) {
 			throw new RAFException(RAFException.ERRCODE_INVALID_PARAMETER_DEFINITION, String.format(
 					"Parameter [%s] has an invalid definition. Date Format [%s] is not valid.", parameterName, format), e);
 		}
 	}
 	
 	private static SimpleDateFormat getDateFormat(String parameterName, String format) 
 		throws RAFException
 	{
 		SimpleDateFormat dateFormat = _formats.get(format);
 		if (dateFormat == null) {
 			DateProcessor.addFormat(parameterName, format);
 			dateFormat = _formats.get(format);
 		}
 		return dateFormat;
 	}
 
 	public static final java.util.Date parseUtilDate(String parameterName, String format, String value)
 		throws RAFException
 	{
 		SimpleDateFormat dateFormat = DateProcessor.getDateFormat(parameterName, format);
 		try {
 			java.util.Date ret = dateFormat.parse(value);
 			return ret;
 		} catch (ParseException e) {
 			throw new RAFException(
					HttpURLConnection.HTTP_BAD_REQUEST,
 					RAFException.ERRCODE_INVALID_PARAMETER_VALUE, 
 					String.format("Value [%s] can't be converted to java.util.Date parameter %s with format [%s]", value, parameterName, format), e
 			);
 		}
 	}
 	
 	public static final java.sql.Date parseSqlDate(String parameterName, String format, String value)
 			throws RAFException
 	{
 		SimpleDateFormat dateFormat = DateProcessor.getDateFormat(parameterName, format);
 		try {
 			java.sql.Date ret = new java.sql.Date(dateFormat.parse(value).getTime());
 			return ret;
 		} catch (ParseException e) {
 			throw new RAFException(
					HttpURLConnection.HTTP_BAD_REQUEST,
 					RAFException.ERRCODE_INVALID_PARAMETER_VALUE,
 					String.format("Value [%s] can't be converted to java.sql.Date parameter %s with format [%s]", value, parameterName, format), e
 			);
 		}
 	}
 	
 	public static final Calendar parseCalendar(String parameterName, String format, String value)
 			throws RAFException
 	{
 		SimpleDateFormat dateFormat = DateProcessor.getDateFormat(parameterName, format);
 		try {
 			Calendar ret = Calendar.getInstance();
 			ret.setTime(dateFormat.parse(value));
 			return ret;
 		} catch (ParseException e) {
 			throw new RAFException(
					HttpURLConnection.HTTP_BAD_REQUEST,
 					RAFException.ERRCODE_INVALID_PARAMETER_VALUE,
 					String.format("Value [%s] can't be converted to java.util.Calendar parameter %s with format [%s]", value, parameterName, format), e
 			);
 		}
 	}
 	
 	
 }
