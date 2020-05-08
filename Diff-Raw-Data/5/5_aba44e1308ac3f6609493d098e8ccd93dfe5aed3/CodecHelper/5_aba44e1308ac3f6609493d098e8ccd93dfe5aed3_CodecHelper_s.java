 /**
  * Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.feed.ddf.historical.provider;
 
 import static com.barchart.feed.ddf.historical.enums.DDF_QueryOrder.ASCENDING;
 import static com.barchart.feed.ddf.historical.enums.DDF_QueryType.*;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import com.barchart.feed.api.model.meta.Instrument;
 import com.barchart.feed.api.model.meta.id.VendorID;
 import com.barchart.feed.ddf.historical.api.DDF_Query;
 import com.barchart.feed.ddf.historical.enums.DDF_QueryEodType;
 import com.barchart.feed.ddf.historical.enums.DDF_QueryEodVolume;
 import com.barchart.feed.ddf.historical.enums.DDF_QueryOrder;
 import com.barchart.feed.ddf.settings.api.DDF_Settings;
 import com.barchart.util.common.ascii.ASCII;
 
 final class CodecHelper {
 
 	private CodecHelper() {
 	}
 
 	//
 
 	static final String KEYWORD_ERROR = "Error";
 
 	//
 
 	final static void checkNull(final Object object, final String message) {
 		if (object == null) {
 			throw new IllegalArgumentException(message);
 		}
 	}
 
 	/**
 	 * http://ds01.ddfplus.com/historical/queryticks.ashx?username=USER&password
 	 * = PASS&symbol=GOOG&start=20100601090000&end=201006020900000
 	 *
 	 */
 
 	final static String urlQuery(final DDF_Settings settings,
 			final DDF_Query<?> query) {
 
 		final StringBuilder text = new StringBuilder(256);
 
 		final CharSequence server = ConstHistorical.historicalServer(settings);
 
 		final CharSequence queryPage = query.type.queryPage;
 
 		final CharSequence username = settings.getAuthUser();
 		final CharSequence password = settings.getAuthPass();
 
 		final Instrument instrument = query.instrument;
 		final CharSequence symbol = instrument.vendorSymbols().get(VendorID.BARCHART_HISTORICAL);
 
 		final DateTimeZone timeZone = DateTimeZone.forID(instrument.timeZoneName());
 		final CharSequence start = requestTime(query.timeStart, timeZone);
 		final CharSequence end = requestTime(query.timeEnd, timeZone);
 
 		final CharSequence maxRecords =
 				query.maxRecords <= 0 ? "" : "" + query.maxRecords;
 
 		final DDF_QueryOrder resultOrder = query.resultOrder;
 		final CharSequence order =
 				resultOrder == null ? ASCENDING.code : resultOrder.code;
 
 		final CharSequence interval =
 				query.groupBy <= 1 ? "1" : "" + query.groupBy;
 
 		final DDF_QueryEodType eodType = query.eodType;
 		final CharSequence data = eodType == null ? "" : eodType.code;
 
 		final DDF_QueryEodVolume eodVolume = query.eodVolume;
 		final CharSequence volume = eodVolume == null ? "" : eodVolume.code;
 
 		text.append(server);
 
 		if (server.charAt(server.length() - 1) != '/') {
 			text.append("/");
 		}
 
 		text.append(queryPage);
 
 		text.append("?");
 
 		text.append("username=");
 		text.append(username);
 
 		text.append("&");
 
 		text.append("password=");
 		text.append(password);
 
 		text.append("&");
 
 		text.append("symbol=");
 		text.append(symbol);
 
 		text.append("&");
 
 		text.append("start=");
 		text.append(start);
 
 		text.append("&");
 
 		text.append("end=");
 		text.append(end);
 
 		text.append("&");
 
 		text.append("maxrecords=");
 		text.append(maxRecords);
 
 		text.append("&");
 
 		text.append("order=");
 		text.append(order);
 
		text.append("&");

		text.append("order=");
		text.append(order);

 		//
 
 		if (query.type.isIn(MINUTES, MINUTES_NEARBY, MINUTES_FORM_T,
 				MINUTES_TREND)) {
 
 			text.append("&");
 
 			text.append("interval=");
 			text.append(interval);
 
 		}
 
 		if (query.type.isIn(TICKS_FORM_T)) {
 
 			text.append("&");
 
 			text.append("sessionfilter=%2Bt");
 			// text.append(sessionFilter);
 		}
 
 		//
 
 		if (query.type.isIn(END_OF_DAY)) {
 
 			text.append("&");
 
 			text.append("data=");
 			text.append(data);
 
 			// if (query.instrument.get(DDF_InstrumentField.DDF_EXCHANGE).kind
 			// == DDF_ExchangeKind.FUTURE) {
 			// }
 
 			// equities now support sum
 
 			text.append("&");
 
 			text.append("volume=");
 			text.append(volume);
 
 			text.append("&");
 
 			text.append("backadjust=");
 			text.append(query.backadjust ? "1" : "0");
 
 			text.append("&");
 
 			text.append("daystoexpiration=");
 			text.append(query.daystoexpiration);
 
 		}
 
 		if (query.type.isIn(TICKS_TREND, MINUTES_TREND, END_OF_DAY_TREND)) {
 
 			text.append("&");
 
 			text.append("trend=");
 			text.append("y");
 
 		}
 
 		return text.toString();
 
 	}
 
 	/**
 	 *
 	 * start: this parameter should be set to the desired start date/time for 
 	 * the query (the result
 	 * set will include records back to, and including, this value). If not set,
 	 * the value will default to the beginning of the day specified
 	 * in the end parameter, if end is specified, or
 	 * to the beginning of the current day
 	 * , if end is not specified. The value should conform to the format 
 	 *
 	 * yyyymmdd[hhmm[ss]]
 	 *
 	 * , where fields in brackets are optional. Any optional fields that
 	 *  are not explicitly set will default to 0 (i.e. 20090203 will default to 
 	 * 20090203000000 or February 3, 2009 at 00:00:00).
 	 *
 	 * Note: all times are in Eastern Time for equities and Central Time for 
 	 * everything else. 
 	 *
 	 */
 
 	static final DateTimeFormatter QUERY_TIME = //
 			DateTimeFormat.forPattern("yyyyMMddHHmmss");
 
 	static final String requestTime(final DateTime dateTime,
 			final DateTimeZone timeZone) {
 		if (dateTime == null || timeZone == null) {
 			return "";
 		}
 		final DateTime queryTime = dateTime.withZone(timeZone);
 		return QUERY_TIME.print(queryTime);
 	}
 
 	static final boolean isFuture(final Instrument instrument) {
 		return instrument.CFICode().charAt(0) == 'F';
 	}
 
 	final static String[] splitCSV(final String string) {
 		return string.split(ASCII.STRING_COMMA);
 	}
 
 	/**
 	 * YYYY­MM­DD HH:MM:SS.FFF,TRADING_DAY,SESSION_CODE,PRICE,SIZE
 	 */
 
 	static final DateTimeFormatter RESULT_TIME_TICKS = //
 			DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
 
 	static long decodeTicksTime(final String string, final Instrument instrument) {
 		return decodeTime(string, instrument, RESULT_TIME_TICKS);
 	}
 
 	static String encodeTicksTime(final long millisUTC,
 			final Instrument instrument) {
 		return encodeTime(millisUTC, instrument, RESULT_TIME_TICKS);
 	}
 
 	/**
 	 * YYYY­-MM­-DD HH:MM,TRADING_DAY,OPEN,HIGH,LOW,CLOSE,VOLUME
 	 */
 
 	static final DateTimeFormatter RESULT_TIME_MINS = //
 			DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
 
 	static long decodeMinsTime(final String string, final Instrument instrument) {
 		return decodeTime(string, instrument, RESULT_TIME_MINS);
 	}
 
 	static String encodeMinsTime(final long millisUTC,
 			final Instrument instrument) {
 		return encodeTime(millisUTC, instrument, RESULT_TIME_MINS);
 	}
 
 	/**
 	 * SYMBOL,YYYY­MM­DD,OPEN,HIGH,LOW,CLOSE,VOLUME[,OPENINTEREST]
 	 */
 	static final DateTimeFormatter RESULT_TIME_EOD = //
 			DateTimeFormat.forPattern("yyyy-MM-dd");
 
 	static long decodeEodTime(final String string, final Instrument instrument) {
 		return decodeTime(string, instrument, RESULT_TIME_EOD);
 	}
 
 	static String encodeEodTime(final long millisUTC,
 			final Instrument instrument) {
 		return encodeTime(millisUTC, instrument, RESULT_TIME_EOD);
 	}
 
 	// /
 
 	static final int decodeInt(final String string) {
 		if (isEmpty(string)) {
 			return 0;
 		}
 		return Integer.parseInt(string);
 	}
 
 	static final String encodeInt(final int value) {
 		return "" + value;
 	}
 
 	static final long decodeLong(final String string) {
 		if (isEmpty(string)) {
 			return 0;
 		}
 		return Long.parseLong(string);
 	}
 
 	static final String encodeLong(final long value) {
 		return "" + value;
 	}
 
 	static byte decodeByte(final String string) {
 		return (byte) string.charAt(0);
 	}
 
 	static long decodeMantissa(final String string, final int exponent) {
 
 		final String[] parts = string.split("\\.");
 
 		long whole;
 		int part;
 		final int spaces;
 
 
 		switch (parts.length) {
 			default:
 			case 0:
 				return 0;
 			case 1:
 				whole = decodeLong(parts[0]);
 				part = 0;
 				spaces = 0;
 				break;
 			case 2:
 				whole = decodeLong(parts[0]);
 				part = decodeInt(parts[1]);
 				spaces = parts[1].length();
 				break;
 		}
 
 		int thisExp;
 
 		thisExp = -spaces;
 
 		while (thisExp < 0) {
 			whole *= 10;
 			thisExp++;
 		}
 
 		/* Handle negative values */
 		if (whole < 0 || (whole == 0 && string.startsWith("-"))) {
 			part = part * -1;
 		}
 
 		long mantissa = whole + part;
 
 		final int instExp = exponent;
 
 		thisExp = -spaces;
 
 		if (instExp == thisExp) {
 			return mantissa;
 		}
 
 		while (thisExp < instExp) {
 			mantissa /= 10;
 			thisExp++;
 		}
 
 		while (thisExp > instExp) {
 			mantissa *= 10;
 			thisExp--;
 		}
 
 		return mantissa;
 
 	}
 
 	final static String encodeMantissaXXX(final long mantissa,
 			final int exponent) {
 
 		long whole = mantissa;
 
 		int e1 = exponent;
 
 		while (e1 > 0) {
 			whole *= 10;
 			e1--;
 		}
 
 		while (e1 < 0) {
 			whole /= 10;
 			e1++;
 		}
 
 		long part = 0;
 
 		int e2 = exponent;
 
 		if (e2 >= 0) {
 			part = 0;
 		} else {
 			int denom = 1;
 			while (e2 < 0) {
 				denom *= 10;
 				e2++;
 			}
 			part = mantissa % denom;
 		}
 
 		if (part == 0) {
 			return "" + whole;
 		} else {
 			// XXX
 			return "" + whole + "." + part;
 		}
 
 	}
 
 	final static String encodeMantissa(final long mantissa, final int exponent) {
 
 		if (mantissa == 0) {
 			return "0";
 		}
 
 		if (exponent >= 0) {
 			int e = exponent;
 			long m = mantissa;
 			while (e > 0) {
 				m *= 10;
 				e--;
 			}
 			return "" + m;
 		}
 
 		final String string = "" + mantissa;
 
 		final int split = string.length() + exponent;
 
 		final String whole = string.substring(0, split);
 		final String part = string.substring(split);
 
 		return whole + "." + part;
 
 	}
 
 	static long decodeTime(final String string, final Instrument instrument,
 			final DateTimeFormatter format) {
 		final DateTimeZone zone = DateTimeZone.forID(instrument.timeZoneName());
 		return format.withZone(zone).parseMillis(string);
 	}
 
 	static String encodeTime(final long millisUTC, final Instrument instrument,
 			final DateTimeFormatter format) {
 		final DateTimeZone zone = DateTimeZone.forID(instrument.timeZoneName());
 		return format.withZone(zone).print(millisUTC);
 	}
 
 	static final boolean isEmpty(final String string) {
 		if (string == null || string.length() == 0) {
 			return true;
 		}
 		return false;
 	}
 
 	static String encodeInstrument(final Instrument instrument,
 			final long millisUTC) {
 		if (instrument == null) {
 			return "";
 		}
 		// if (instrument.getSymbol().getType() == DDF_SymbolType.FUTURE) {
 		// final DateTime dateTime = new DateTime(millisUTC, DateTimeZone.UTC);
 		// final int year = dateTime.getYearOfCentury();
 		// final DDF_SymbolFuture symbolDDF = (DDF_SymbolFuture) instrument
 		// .getSymbol();
 		// return symbolDDF.getGroup() + symbolDDF.getMonth().code + year;
 		// }
 		return instrument.symbol();
 	}
 
 }
