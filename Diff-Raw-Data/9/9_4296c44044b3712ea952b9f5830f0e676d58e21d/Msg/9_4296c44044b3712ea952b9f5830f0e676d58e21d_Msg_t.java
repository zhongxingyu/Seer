 package com.jcertif.presentation.internationalisation;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import javax.servlet.http.HttpSession;
 
 public class Msg {
 	private static final String BUNDLE_NAME = "i18n.messages"; //$NON-NLS-1$
 
 	private static ThreadLocal<Boolean> ISFRENCH = new ThreadLocal<Boolean>();
 
 	private Msg() {
 	}
 
 	public static String getString(String key, Locale locale) {
 		try {
 			// ResourceBundle caches the bundles, so this is not as inefficient
 			// as it seems.
 			return ResourceBundle.getBundle(BUNDLE_NAME, locale).getString(key);
 		} catch (MissingResourceException e) {
 			return '' + key + '';
 		}
 	}
 
 	public static String get(String key) {
 		try {
 			if (ISFRENCH.get() == null || ISFRENCH.get()) {
 				return ResourceBundle.getBundle(BUNDLE_NAME, Locale.FRENCH)
 						.getString(key);
 			}
 			return ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH)
 					.getString(key);
 
 		} catch (MissingResourceException e) {
 			return '' + key + '';
 		}
 	}
 
 	public static void initI18n(HttpSession session, String browserLanguage) {
 
 		ResourceBundle bundle = null;
 
 		if (browserLanguage == null || browserLanguage.startsWith("fr")) {
 			// Par dfaut c'est le franais
 			bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.FRENCH);
 		} else {
 			bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH);
 		}
 
 		Enumeration<String> enumKeys = bundle.getKeys();
 
 		while (enumKeys.hasMoreElements()) {
 			String key = enumKeys.nextElement();
 			session.setAttribute(key, bundle.getString(key));
 		}
 	}
 
 	public static Map<String, String> getAllProps() {
 		Map<String, String> allProps = new HashMap<String, String>();
		ResourceBundle bundle = null;

		if (ISFRENCH.get() == null || ISFRENCH.get()) {
			bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.FRENCH);
		} else {
			bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH);
		}
 		Enumeration<String> enumKeys = bundle.getKeys();
 
 		while (enumKeys.hasMoreElements()) {
 			String key = enumKeys.nextElement();
 			allProps.put(key, bundle.getString(key));
 		}
 		return allProps;
 	}
 
 	public static void updateLocale(String browserLanguage) {
 		if (browserLanguage == null || browserLanguage.startsWith("fr")) {
 			// Par dfaut c'est le franais
 			ISFRENCH.set(true);
 		} else {
 			ISFRENCH.set(false);
 		}
 
 	}
 }
