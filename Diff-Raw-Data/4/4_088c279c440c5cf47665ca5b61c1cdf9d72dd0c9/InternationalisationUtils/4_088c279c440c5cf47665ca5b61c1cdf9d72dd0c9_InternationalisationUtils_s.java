 /*
  * FrontlineSMS <http://www.frontlinesms.com>
  * Copyright 2007, 2008 kiwanja
  * 
  * This file is part of FrontlineSMS.
  * 
  * FrontlineSMS is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or (at
  * your option) any later version.
  * 
  * FrontlineSMS is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.frontlinesms.ui.i18n;
  
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_FAILED;
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_OUTBOX;
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_PENDING;
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_RETRYING;
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_SENT;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Currency;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 
 import net.frontlinesms.FrontlineSMSConstants;
 import net.frontlinesms.FrontlineUtils;
 import net.frontlinesms.data.domain.Email;
 import net.frontlinesms.resources.ResourceUtils;
 import net.frontlinesms.ui.FrontlineUI;
 
 import org.apache.log4j.Logger;
 
 import thinlet.Thinlet;
 
 /**
  * Utilities for helping internationalise text etc.
  * @author Alex
  * 
  * TODO always use UTF-8 with no exceptions.  All Unicode characters, and therefore all characters, can be encoded as UTF-8
  */
 public class InternationalisationUtils {
 	
 //> STATIC PROPERTIES
 	/** Name of the directory containing the languages files.  This is located within the config directory. */
 	private static final String LANGUAGES_DIRECTORY_NAME = "languages";
 	/** The filename of the default language bundle. */
 	public static final String DEFAULT_LANGUAGE_BUNDLE_FILENAME = "frontlineSMS.properties";
 	/** The path to the default language bundle on the classpath. */
 	public static final String DEFAULT_LANGUAGE_BUNDLE_PATH = "/resources/languages/" + DEFAULT_LANGUAGE_BUNDLE_FILENAME;
 	/** Logging object for this class */
 	private static Logger LOG = FrontlineUtils.getLogger(InternationalisationUtils.class);
 	
 //> GENERAL i18n HELP METHODS
 	/** The default characterset, UTF-8.  This must be available for every JVM. */
 	public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
 	
 //>
 	/**
 	 * Return an internationalised message for this key. 
 	 * <br> This method tries to get the string for the current bundle and if it does not exist, it looks into
 	 * the default bundle (English GB). 
 	 * @param key
 	 * @return the internationalised text, or the english text if no internationalised text could be found
 	 */
 	public static String getI18NString(String key) {
 		if(FrontlineUI.currentResourceBundle != null) {
 			try {
 				return FrontlineUI.currentResourceBundle.getValue(key);
 			} catch(MissingResourceException ex) {}
 		}
 		return Thinlet.DEFAULT_ENGLISH_BUNDLE.get(key);
 	}
 	
 	/**
 	 * Return the list of internationalised message for this prefix. 
 	 * <br> This method tries to get the strings from the current bundle, and if it does not exist, it looks into
 	 * the default bundle
 	 * @param key
 	 * @return the list internationalised text, or an empty list if no internationalised text could be found
 	 */
 	public static List<String> getI18nStrings(String key) {
 		if(FrontlineUI.currentResourceBundle != null) {
 			try {
 				return FrontlineUI.currentResourceBundle.getValues(key);
 			} catch(MissingResourceException ex) {}
 		}
 		return LanguageBundle.getValues(Thinlet.DEFAULT_ENGLISH_BUNDLE, key);
 	}
 
 	/**
 	 * Return an internationalised message for this key.  This calls {@link #getI18NString(String)}
 	 * and then replaces any instance of {@link FrontlineSMSConstants#ARG_VALUE} with @param argValues
 	 * 
 	 * @param key
 	 * @param argValues
 	 * @return an internationalised string with any substitution variables converted
 	 */
 	public static String getI18NString(String key, String... argValues) {
 		String string = getI18NString(key);
 		
 		if(argValues != null) {
 			// Iterate backwards through the replacements and replace the arguments with the new values.  Need
 			// to iterate backwards so e.g. %10 is replaced before %1
 			for (int i = argValues.length-1; i >= 0; --i) {
 				String arg = argValues[i];
 				if(arg != null) {
 					if(LOG.isDebugEnabled()) LOG.debug("Subbing " + arg + " as " + (FrontlineSMSConstants.ARG_VALUE + i) + " into: " + string);
 					string = string.replace(FrontlineSMSConstants.ARG_VALUE + i, arg);
 				}
 			}
 		}
 		return string;
 	}
 
 	/**
 	 * Return an internationalised message for this key.  This converts the integer to a {@link String} and then
 	 * calls {@link #getI18NString(String, String...)} with this argument.
 	 * @param key
 	 * @param intValue
 	 * @return the internationalised string with the supplied integer embedded at the appropriate place
 	 */
 	public static String getI18NString(String key, int intValue) {
 		return getI18NString(key, Integer.toString(intValue));
 	}
 
 	/**
 	 * Parses a string representation of an amount of currency to an integer.  This will handle
 	 * cases where the currency symbol has not been included in the <code>currencyString</code> 
 	 * @param currencyString
 	 * @return the currency amount represented by the supplied string
 	 * @throws ParseException
 	 */
 	public static final double parseCurrency(String currencyString) throws ParseException {
 		NumberFormat currencyFormat = InternationalisationUtils.getCurrencyFormat();
 		String currencySymbol = InternationalisationUtils.getCurrencySymbol();
 		if(!currencyString.contains(currencySymbol)) {
 			if(InternationalisationUtils.isCurrencySymbolPrefix()) currencyString = currencySymbol + currencyString;
 			else if(InternationalisationUtils.isCurrencySymbolSuffix()) currencyString += currencySymbol;
 			/* else allow the parse exception to be thrown! */ 
 		}
 		return currencyFormat.parse(currencyString).doubleValue();
 	}
 
 	/**
 	 * Checks if the currency currency symbol is a suffix to the formatted currency value string.
 	 * @return <code>true</code> if the currency symbol should be placed after the value; <code>false</code> otherwise.
 	 */
 	public static boolean isCurrencySymbolSuffix() {
 		String testString = InternationalisationUtils.formatCurrency(12.34);
 		String currencySymbol = InternationalisationUtils.getCurrencySymbol();
 		int symbolPosition = testString.indexOf(currencySymbol);
 		return symbolPosition == testString.length()-currencySymbol.length();
 	}
 
 	/**
 	 * Checks if the currency currency symbol is a prefix to the formatted currency value string.
 	 * @return <code>true</code> if the currency symbol should be placed before the currency value; <code>false</code> otherwise.
 	 */
 	public static boolean isCurrencySymbolPrefix() {
 		String testString = InternationalisationUtils.formatCurrency(12.34);
 		int symbolPosition = testString.indexOf(InternationalisationUtils.getCurrencySymbol());
 		return symbolPosition == 0;
 	}
 
 	/**
 	 * @param value 
 	 * @return a formatted currency string
 	 * @see #formatCurrency(double, boolean)
 	 */
 	public static final String formatCurrency(double value) {
 		return InternationalisationUtils.formatCurrency(value, true);
 	}
 
 	/**
 	 * Format an integer into a decimal string for use as a currency value.
 	 * @param value 
 	 * @param showSymbol 
 	 * @return a formatted currency string
 	 */
 	public static final String formatCurrency(double value, boolean showSymbol) {
 		String formatted = InternationalisationUtils.getCurrencyFormat().format(value);
 		if(!showSymbol) {
 			formatted = formatted.replace(InternationalisationUtils.getCurrencySymbol(), "");
 		}
 		return formatted;
 	}
 
 	/** @return decimal separator to be used with the currect currency */
 	public static final char getDecimalSeparator() {
 		return ((DecimalFormat)InternationalisationUtils.getCurrencyFormat()).getDecimalFormatSymbols().getDecimalSeparator();
 	}
 
 	/** @return symbol used to represent the current currency */
 	public static final String getCurrencySymbol() {
 		return ((DecimalFormat)InternationalisationUtils.getCurrencyFormat()).getDecimalFormatSymbols().getCurrencySymbol();
 	}
 
 	/** @return the localised currency format specified in the language bundle */
 	private static final NumberFormat getCurrencyFormat() {
 		NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
 		try {
 			currencyFormat.setCurrency(Currency.getInstance(getI18NString(FrontlineSMSConstants.COMMON_CURRENCY)));
 		} catch(IllegalArgumentException ex) {
 			LOG.warn("Currency not supported: " + getI18NString(FrontlineSMSConstants.COMMON_CURRENCY), ex);
 		}
 		return currencyFormat;
 	}
 	
 //> LANGUAGE BUNDLE LOADING METHODS
 	/**
 	 * Loads the default, english {@link LanguageBundle} from the classpath
 	 * @return the default English {@link LanguageBundle}
 	 * @throws IOException If there was a problem loading the default language bundle.  // TODO this should probably throw a runtimeexception of some sort
 	 */
 	public static final LanguageBundle getDefaultLanguageBundle() throws IOException {
 		return ClasspathLanguageBundle.create(DEFAULT_LANGUAGE_BUNDLE_PATH);
 	}
 	
 	/** @return {@link InputStream} to the default translation file on the classpath. */
 	public static InputStream getDefaultLanguageBundleInputStream() {
 		return ClasspathLanguageBundle.class.getResourceAsStream(DEFAULT_LANGUAGE_BUNDLE_PATH);
 	}
 	
 	/**
 	 * Loads a {@link LanguageBundle} from a file.  All files are encoded with UTF-8.
 	 * TODO change this to use {@link Currency}, and put the ISO 4217 currency code in the l10n file.
 	 * @param file
 	 * @return The loaded bundle, or NULL if the bundle could not be loaded.
 	 */
 	public static final FileLanguageBundle getLanguageBundle(File file) {
 		try {
 			FileLanguageBundle bundle = FileLanguageBundle.create(file);
 			LOG.info("Successfully loaded language bundle from file: " + file.getName());
 			LOG.info("Bundle reports filename as: " + bundle.getFile().getAbsolutePath());
 			LOG.info("Language Name : " + bundle.getLanguageName());
 			LOG.info("Language Code : " + bundle.getLanguageCode());
 			LOG.info("Country       : " + bundle.getCountry());
 			LOG.info("Right-To-Left : " + bundle.isRightToLeft());
 			return bundle;
 		} catch(Exception ex) {
 			LOG.error("Problem reading language file: " + file.getName(), ex);
 			return null;
 		}
 	}
 	
 	/**
 	 * @param identifier ID used when logging problems while loading the text resource
 	 * @param inputStream
 	 * @return map containing map of key-value pairs of text resources
 	 * @throws IOException
 	 */
 	public static final Map<String, String> loadTextResources(String identifier, InputStream inputStream) throws IOException {
 		HashMap<String, String> i18nStrings = new HashMap<String, String>();
 		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, CHARSET_UTF8));
 		String line;
 		while((line = in.readLine()) != null) {
 			line = line.trim();
 			if(line.length() > 0 && line.charAt(0) != '#') {
 				int splitChar =  line.indexOf('=');
 				if(splitChar <= 0) {
 					// there's no "key=value" pair on this line, but it does have text on it.  That's
 					// not strictly legal, so we'll log a warning and carry on.
 					LOG.warn("Bad line in language file '" + identifier + "': '" + line + "'");
 				} else {
 					String key = line.substring(0, splitChar).trim();					
 					if(i18nStrings.containsKey(key)) {
 						// This key has already been read from the language file.  Ignore the new value.
 						LOG.warn("Duplicate key in language file '': ''");
 					} else {
 						String value = line.substring(splitChar + 1).trim();
						i18nStrings.put(key, value);
 					}
 				}
 			}
 		}
 		return i18nStrings;
 	}
 
 	/**
 	 * Loads all language bundles from within and without the JAR
 	 * @return all language bundles from within and without the JAR
 	 */
 	public static Collection<FileLanguageBundle> getLanguageBundles() {
 		ArrayList<FileLanguageBundle> bundles = new ArrayList<FileLanguageBundle>();
 		File langDir = new File(getLanguageDirectoryPath());
 		if(!langDir.exists() || !langDir.isDirectory()) throw new IllegalArgumentException("Could not find resources directory: " + langDir.getAbsolutePath());
 		
 		for (File file : langDir.listFiles()) {
 			FileLanguageBundle bungle = getLanguageBundle(file);
 			if(bungle != null) {
 				bundles.add(bungle);
 			}
 		}
 		return bundles;
 	}
 	
 	/** @return path of the directory in which language bundles are located. */
 	private static final String getLanguageDirectoryPath() {
 		return ResourceUtils.getConfigDirectoryPath() + LANGUAGES_DIRECTORY_NAME + File.separatorChar;
 	}
 	
 	/** @return path of the directory in which language bundles are located. */
 	public static final File getLanguageDirectory() {
 		return new File(ResourceUtils.getConfigDirectoryPath(), LANGUAGES_DIRECTORY_NAME);
 	}
 
 //> DATE FORMAT GETTERS
 	/**
 	 * N.B. This {@link DateFormat} may be used for parsing user-entered data.
 	 * @return date format for displaying and entering year (4 digits), month and day.
 	 */
 	public static DateFormat getDateFormat() {
 		return new SimpleDateFormat(getI18NString(FrontlineSMSConstants.DATEFORMAT_YMD));
 	}
 
 	/**
 	 * This is not used for parsing user-entered data. 
 	 * @return date format for displaying date and time.#
 	 */
 	public static DateFormat getDatetimeFormat() {
 		return new SimpleDateFormat(getI18NString(FrontlineSMSConstants.DATEFORMAT_YMD_HMS));
 	}
 
 	/**
 	 * TODO what is this method used for?  This value seems completely nonsensical - why wouldn't you just use the timestamp itself?  When do you ever need the date as an actual string?
 	 * @return current time as a formatted date string
 	 */
 	public static String getDefaultStartDate() {
 		return getDateFormat().format(new Date());
 	}
 
 	/**
 	 * Parse the supplied {@link String} into a {@link Date}.
 	 * This method assumes that the supplied date is in the same format as {@link #getDateFormat()}.
 	 * @param date A date {@link String} formatted with {@link #getDateFormat()}
 	 * @return a java {@link Date} object describing the supplied date
 	 * @throws ParseException
 	 */
 	public static Date parseDate(String date) throws ParseException {
 		return getDateFormat().parse(date);
 	}
 
 	/**
 	 * <p>Merges the source map into the destination.  Values in the destination take precedence - they will not be
 	 * overridden if the same key occurs in both destination and source.</p>
 	 * <p>If a <code>null</code> source is provided, this method does nothing; if a <code>null</code> destination is
 	 * provided, a {@link NullPointerException} will be thrown.
 	 * @param destination 
 	 * @param source 
 	 */
 	public static void mergeMaps(Map<String, String> destination, Map<String, String> source) {
 		assert(destination!=null): "You must provide a destination map to merge into.";
 		
 		// If there is nothing to merge, just return.
 		if(source == null) return;
 		
 		for(String key : source.keySet()) {
 			if(destination.get(key) != null) {
 				// key already present in language bundle - ignoring
 			} else {
 				// this key does not appear in the language bundle, so add it with the value from the map
 				destination.put(key, source.get(key));
 			}
 		}
 	}
 
 	/**
 	 * Get the status of a {@link Email} as a {@link String}.
 	 * @param email
 	 * @return {@link String} representation of the status.
 	 */
 	public static final String getEmailStatusAsString(Email email) {
 		switch(email.getStatus()) {
 		case OUTBOX:
 			return getI18NString(COMMON_OUTBOX);
 		case PENDING:
 			return getI18NString(COMMON_PENDING);
 		case SENT:
 			return getI18NString(COMMON_SENT);
 		case RETRYING:
 			return getI18NString(COMMON_RETRYING);
 		case FAILED:
 			return getI18NString(COMMON_FAILED);
 		default:
 			return "(unknown)";
 		}
 	}
 }
