 package com.oschrenk.humangeo.io;
 
 import java.util.Scanner;
 import java.util.regex.MatchResult;
 
 /**
  * Formats decimal formatted geographic coordinates to a degree measurement and
  * parses degree measurements to decimal format.
  * 
  * An examplary output would be: <code>52°15′16″E</code>, whereas the degree
  * symbol is <code>\u00b0</code>, the minute symbol <code>\u2032</code> and the
  * seconds symbol (its only one character) is <code>\u2033</code>. These
  * characters are the correct unicode representation.
  * 
  * But as these symbols are often hard to input and seldom to be found in the
 * real world,is the parser is very lenient and accepts many variants as the
 * seperator symbols.
  * 
  * The parser also accepts fixed width degree measurements.
  * 
  * @author Oliver Schrenk <oliver.schrenk@gmail.com>
  */
 public class GeographicCoordinates {
 
 	/** The Constant DEGREE. */
 	private static final char DEGREE = '\u00b0'; // °
 
 	/** The Constant MASCULINE_ORDINAL_INDICATOR. */
 	private static final char MASCULINE_ORDINAL_INDICATOR = '\u00ba'; // º
 
 	/** The Constant RING_ABOVE. */
 	private static final char RING_ABOVE = '\u02DA'; // ˚
 
 	/** The Constant SUPERSCRIPT_ZERO. */
 	private static final char SUPERSCRIPT_ZERO = '\u2070'; // ⁰
 
 	/** The Constant RING_OPERATOR. */
 	private static final char RING_OPERATOR = '\u2218'; // ∘
 
 	/** The Constant ARCDEGREE. */
 	public static final char ARCDEGREE = DEGREE;
 
 	/** The Constant ARCDEGREE_SYMBOLS. */
 	static final char[] ARCDEGREE_SYMBOLS = { DEGREE, MASCULINE_ORDINAL_INDICATOR, RING_ABOVE, SUPERSCRIPT_ZERO, RING_OPERATOR };
 
 	/** The ARCDEGRE e_ symbol s_ regex. */
 	static String ARCDEGREE_SYMBOLS_REGEX;
 
 	static {
 		StringBuffer sb = new StringBuffer();
 		sb.append("[");
 		for (int i = 0; i < ARCDEGREE_SYMBOLS.length - 1; i++) {
 			sb.append(ARCDEGREE_SYMBOLS[i]);
 		}
 		sb.append(ARCDEGREE_SYMBOLS[ARCDEGREE_SYMBOLS.length - 1]);
 		sb.append("]");
 		ARCDEGREE_SYMBOLS_REGEX = sb.toString();
 	}
 
 	/** The Constant PRIME. */
 	private static final char PRIME = '\u2032'; // ′
 
 	/** The Constant APOSTROPHE. */
 	private static final char APOSTROPHE = '\''; // '
 
 	/** The Constant VERTICAL_TYPEWITER_APOSTROPHE. */
 	private static final char VERTICAL_TYPEWITER_APOSTROPHE = 27; // '
 
 	/** The Constant PUNCTUATION_APOSTROPHE. */
 	private static final char PUNCTUATION_APOSTROPHE = '\u2019';
 
 	/** The Constant LETTER_APOSTROPHE. */
 	private static final char LETTER_APOSTROPHE = '\u02bc';
 
 	/** The Constant ARMENIAN_APOSTROPHE. */
 	private static final char ARMENIAN_APOSTROPHE = '\u055A';
 
 	/** The Constant GRAVE_ACCENT. */
 	private static final char GRAVE_ACCENT = '\u0060'; // `
 
 	/** The Constant ACUTE_ACCENT. */
 	private static final char ACUTE_ACCENT = '\u00B4'; // ´
 
 	/** The Constant MODIFIER_LETTER_PRIME. */
 	private static final char MODIFIER_LETTER_PRIME = '\u02b9'; // ʹ
 
 	/** The Constant SINGLE_QUOTATION_OPENING. */
 	private static final char SINGLE_QUOTATION_OPENING = '\u2018'; // ‘ 
 
 	/** The Constant SINGLE_QUOTATION_CLOSING. */
 	private static final char SINGLE_QUOTATION_CLOSING = '\u2019'; // ’ 
 
 	/** The Constant OKINA. */
 	private static final char OKINA = '\u02BB'; // ʻ
 
 	/** The Constant ARCMINUTE. */
 	public static final char ARCMINUTE = PRIME;
 
 	/** The Constant ARCMINUTES_SYMBOLS. */
 	static final char[] ARCMINUTES_SYMBOLS = {
 			APOSTROPHE, VERTICAL_TYPEWITER_APOSTROPHE, PUNCTUATION_APOSTROPHE, LETTER_APOSTROPHE,
 			ARMENIAN_APOSTROPHE, GRAVE_ACCENT, ACUTE_ACCENT, PRIME, MODIFIER_LETTER_PRIME, SINGLE_QUOTATION_OPENING,
 			SINGLE_QUOTATION_CLOSING, OKINA };
 
 	/** The ARCMINUTE s_ symbol s_ regex. */
 	public static String ARCMINUTES_SYMBOLS_REGEX;
 
 	static {
 		StringBuffer sb = new StringBuffer();
 		sb.append("[");
 		for (int i = 0; i < ARCMINUTES_SYMBOLS.length - 1; i++) {
 			if (ARCMINUTES_SYMBOLS[i] == VERTICAL_TYPEWITER_APOSTROPHE) {
 				//sb.append("\\\\");
 			}
 			sb.append(ARCMINUTES_SYMBOLS[i]);
 		}
 		sb.append(ARCMINUTES_SYMBOLS[ARCMINUTES_SYMBOLS.length - 1]);
 		sb.append("]");
 		ARCMINUTES_SYMBOLS_REGEX = sb.toString();
 	}
 
 	// arc seconds symbols
 
 	/** The Constant DOUBLE_PRIME. */
 	private static final char DOUBLE_PRIME = '\u2033'; // ″
 
 	/** The Constant QUOTATION_MARK. */
 	private static final char QUOTATION_MARK = '\u0022'; // "
 
 	/** The Constant DOUBLE_OPENING_QUOTATION_MARK. */
 	private static final char DOUBLE_OPENING_QUOTATION_MARK = '\u201C'; // “ 
 
 	/** The Constant DOUBLE_CLOSING_QUOTATION_MARK. */
 	private static final char DOUBLE_CLOSING_QUOTATION_MARK = '\u201D'; // ” 
 
 	/** The Constant DITO_MARK. */
 	private static final char DITO_MARK = '\u3003'; // 〃
 
 	/** The Constant ARCSECOND. */
 	public static final char ARCSECOND = DOUBLE_PRIME;
 
 	/** The Constant ARCSECONDS_SYMBOLS. */
 	static final char[] ARCSECONDS_SYMBOLS = {
 			QUOTATION_MARK, DOUBLE_PRIME, DOUBLE_OPENING_QUOTATION_MARK,
 			DOUBLE_CLOSING_QUOTATION_MARK, DITO_MARK };
 
 	/** The ARCSECOND s_ symbol s_ regex. */
 	public static String ARCSECONDS_SYMBOLS_REGEX;
 
 	static {
 		StringBuffer sb = new StringBuffer();
 		sb.append("(");
 
 		sb.append("[");
 		for (int i = 0; i < ARCSECONDS_SYMBOLS.length - 1; i++) {
 			sb.append(ARCSECONDS_SYMBOLS[i]);
 		}
 		sb.append(ARCSECONDS_SYMBOLS[ARCSECONDS_SYMBOLS.length - 1]);
 		sb.append("]");
 
 		sb.append("|");
 
 		sb.append("[");
 		for (int i = 0; i < ARCMINUTES_SYMBOLS.length - 1; i++) {
 			sb.append(ARCMINUTES_SYMBOLS[i]);
 		}
 		sb.append(ARCMINUTES_SYMBOLS[ARCMINUTES_SYMBOLS.length - 1]);
 		sb.append("]");
 		sb.append("{2}");
 
 		sb.append(")");
 		ARCSECONDS_SYMBOLS_REGEX = sb.toString();
 	}
 
 	/** The Constant REGEX. */
 	public static final String REGEX = "(\\d{1,3})" + GeographicCoordinates.ARCDEGREE_SYMBOLS_REGEX + "\\s?(\\d{1,2})"
 			+ GeographicCoordinates.ARCMINUTES_SYMBOLS_REGEX + "\\s?(\\d{1,2})" + GeographicCoordinates.ARCSECONDS_SYMBOLS_REGEX + "\\s?([NSEW])";
 
 	/**
 	 * Parses the.
 	 * 
 	 * @param source
 	 *            the source
 	 * @return the double
 	 */
 	public double parse(String source) {
 		if (source == null)
 			return Double.NaN;
 
 		source = source.replaceAll("\\s+", "");
 
 		if (source.length() == 0)
 			return Double.NaN;
 
 		if (source.length() == 8)
 			return fixedLength(source);
 
 		Scanner scanner = new Scanner(source);
 		scanner.findInLine(REGEX);
 
 		MatchResult result;
 		try {
 			result = scanner.match();
 		} catch (IllegalStateException e) {
 			throw new IllegalArgumentException("Not a valid Arcdegree found.", e);
 		}
 
 		double d = getDecimal(result.group(5), result.group(1), result.group(2), result.group(3));
 
 		scanner.close();
 
 		return d;
 
 	}
 
 	/**
 	 * Fixed length.
 	 * 
 	 * @param source
 	 *            the source
 	 * @return the double
 	 */
 	private double fixedLength(String source) {
 		return getDecimal(source.substring(7), source.substring(0, 3), source.substring(3, 5), source.substring(5, 7));
 	}
 
 	/**
 	 * Gets the decimal.
 	 * 
 	 * @param directionString
 	 *            the direction string
 	 * @param degreesString
 	 *            the degrees string
 	 * @param minutesString
 	 *            the minutes string
 	 * @param secondsString
 	 *            the seconds string
 	 * @return the decimal
 	 */
 	private double getDecimal(String directionString, String degreesString, String minutesString, String secondsString) {
 		int signum = 1;
 		final double degrees = Integer.parseInt(degreesString);
 		final double minutes = Integer.parseInt(minutesString);
 		final double seconds = Integer.parseInt(secondsString);
 		final String direction = directionString;
 
 		if (direction.equalsIgnoreCase("S") || (direction.equalsIgnoreCase("W"))) {
 			signum = -1;
 		} else if (direction.equalsIgnoreCase("N") || (direction.equalsIgnoreCase("E"))) {
 			signum = 1;
 		} else {
 			throw new IllegalArgumentException("Unknown direction.");
 		}
 
 		return getDecimal(signum, degrees, minutes, seconds);
 	}
 
 	/**
 	 * Gets the decimal.
 	 * 
 	 * @param signum
 	 *            the signum
 	 * @param degrees
 	 *            the degrees
 	 * @param minutes
 	 *            the minutes
 	 * @param seconds
 	 *            the seconds
 	 * @return the decimal
 	 */
 	private double getDecimal(int signum, double degrees, double minutes, double seconds) {
 		if (degrees > 180) {
 			throw new IllegalArgumentException("Over 180 degrees.");
 		}
 
 		if (minutes > 60) {
 			throw new IllegalArgumentException("Over 60 minutes.");
 		}
 
 		if (seconds > 60) {
 			throw new IllegalArgumentException("Over 60 seconds.");
 		}
 
 		return signum * (degrees + minutes / 60 + seconds / 3600);
 	}
 
 	/**
 	 * Format longitude with orientation.
 	 * 
 	 * @param longitude
 	 *            the longitude
 	 * @return degree formatted latitude
 	 * @throws IllegalArgumentException
 	 *             when the latitude is under -180 or over +180
 	 */
 	public String formatLongitude(double longitude) {
 		if (Math.abs(longitude) > 180)
 			throw new IllegalArgumentException(String.format("Longitude over +180%s or under -180%s", ARCDEGREE));
 		StringBuilder sb = new StringBuilder();
 		sb.append(getFormat(longitude));
 		if (longitude < 0)
 			sb.append("S");
 		sb.append("N");
 		return sb.toString();
 	}
 
 	/**
 	 * Formats latitude with orientation.
 	 * .
 	 * 
 	 * @param latitude
 	 *            the latitude
 	 * @return degree formatted latitude
 	 * @throws IllegalArgumentException
 	 *             when the latitude is under -90 or over +90
 	 */
 	public String formatLatitude(double latitude) {
 		if (Math.abs(latitude) > 90)
 			throw new IllegalArgumentException(String.format("Longitude over +90%s or under -90%s", ARCDEGREE));
 
 		StringBuilder sb = new StringBuilder();
 		sb.append(getFormat(latitude));
 		if (latitude < 0)
 			sb.append("W");
 		sb.append("E");
 		return sb.toString();
 	}
 
 	/**
 	 * Formats a decimal number in degree formatted format but doesn't add
 	 * orientation.
 	 *
 	 * @param d decimal formatted
 	 * @return the string
 	 */
 	public String format(double d) {
 		return getFormat(d);
 	}
 
 	/**
 	 * Gets the format.
 	 * 
 	 * @param d
 	 *            the d
 	 * @return the format
 	 */
 	private String getFormat(double d) {
 		int degrees = (int) d;
 		int minutes = (int) ((d - degrees) * 60);
 		int seconds = (int) ((d - degrees - minutes / 60d) * 3600);
 		StringBuilder sb = new StringBuilder();
 		sb.append(degrees);
 		sb.append(ARCDEGREE);
 		sb.append(minutes);
 		sb.append(ARCMINUTE);
 		sb.append(seconds);
 		sb.append(ARCSECOND);
 		return sb.toString();
 	}
 
 }
