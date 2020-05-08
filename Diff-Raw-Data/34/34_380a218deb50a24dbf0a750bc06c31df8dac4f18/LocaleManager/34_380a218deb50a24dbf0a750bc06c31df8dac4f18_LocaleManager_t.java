 /*
  *  This file is part of Cotopaxi.
  *
  *  Cotopaxi is free software: you can redistribute it and/or modify
  *  it under the terms of the Lesser GNU General Public License as published
  *  by the Free Software Foundation, either version 3 of the License, or
  *  any later version.
  *
  *  Cotopaxi is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  Lesser GNU General Public License for more details.
  *
  *  You should have received a copy of the Lesser GNU General Public License
  *  along with Cotopaxi. If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.cotopaxi.i18n;
 
 import static br.octahedron.cotopaxi.CotopaxiProperty.CHARSET;
 import static br.octahedron.cotopaxi.CotopaxiProperty.I18N_BASE_FILE;
 import static br.octahedron.cotopaxi.CotopaxiProperty.I18N_FOLDER;
 import static br.octahedron.cotopaxi.CotopaxiProperty.I18N_SUPPORTED_LOCALES;
 import static br.octahedron.cotopaxi.CotopaxiProperty.getProperty;
 import static java.util.Arrays.asList;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.Locale;
 import java.util.PropertyResourceBundle;
 import java.util.ResourceBundle;
 
import br.octahedron.cotopaxi.CotopaxiProperty;
 import br.octahedron.cotopaxi.controller.ControllerDescriptor;
 import br.octahedron.util.Log;
 
 /**
  * This entity is responsible by check and load Locale properties. It uses {@link ResourceBundle}
  * features to manage the Resource files.
  * 
  * {@link LocaleManager} lookup i18n files using the following strategy:
  * 
  * <strong>Choosing locale:</strong> It gets the request acceptable locales, sorted by preference
  * order, and search for the first locale that matches one of the acceptable locales. If no locale
  * is found, it used the default locale, currently "en".
  * 
  * <strong>Loading resources file:</strong> For each controller, it tries to load three different
  * files:
  * 
  * <ul>
  * <li>controllerClass.controllerName_<em>locale</em></li>
  * <li>controllerClass_<em>locale</em></li>
  * <li>master_<em>locale</em></li>
  * </ul>
  * 
  * Where <em>controllerClass</em> is the complete controller class name, as defined at the
  * Configuration file, <em>controllerName</em> is the full controller name, it means, "HTTP method"
  * + name, and finally, master stands for the {@link CotopaxiProperty#I18N_BASE_FILE}.
  * 
  * E.g.: If the controller being executed is br.octahedron.example, and the handler method is
  * <em>getHelloWorld()</em> it will (try) load files:
  * 
  * <ul>
  * <li>br.octahedron.example.getHelloWorld_locale</li>
  * <li>br.octahedron.example_locale</li>
  * <li>master_locale</li>
  * </ul>
  * 
  * The master/base file name can be changed by defining the I18N_BASE_FILE property at configuration
  * file. The default value is <strong>master</strong>.
  * 
  * For the <em>locale</em> part the strategy it uses is:
  * 
  * <ul>
  * <li>baseName_<em>language</em>_<em>country</em></li>
  * <li>baseName_<em>language</em></li>
  * <ul>
  * 
  * When try to recover a value from {@link LocaleMap} it will try to get value from the resources
  * files in the same order files was load.
  * 
  * If no file be found, it will return an empty locale map.l
  * 
  * @author Danilo Queiroz - daniloqueiroz@octahedron.com.br
  */
 public class LocaleManager {
 
 	private static final Log logger = new Log(LocaleManager.class);
 	private static final String BASE_RESOURCE = getProperty(I18N_BASE_FILE);
 	private static final String BASE_FOLDER = getProperty(I18N_FOLDER);
 
 	private static Collection<Locale> supportedLocales = new LinkedHashSet<Locale>();
 	private ClassLoader loader = this.getClass().getClassLoader();
 
 	static {
 		/*
 		 * Loads the supported locales from configuration file.
 		 */
 		String[] locales = getProperty(I18N_SUPPORTED_LOCALES).split(",");
 		for (String l : locales) {
 			Locale lc = null;
 			l = l.trim();
 			int ll = l.length();
 			if (ll == 2) {
 				lc = new Locale(l);
 			} else if (ll == 5 && l.charAt(2) == '_') {
 				String[] ls = l.split("_");
 				lc = new Locale(ls[0], ls[1]);
 			}
 
 			if (lc != null) {
 				logger.debug("Adding locale %s to supported locales list", lc.toString());
 				supportedLocales.add(lc);
 			} else {
 				logger.warning("%s isn't a valid locale, please check configuration file.", l);
 			}
 		}
 	}

 	/**
 	 * @param desc
 	 * @param locales
 	 * @return
 	 */
 	public LocaleMap getLocaleMap(ControllerDescriptor desc, Locale locale) {
 		return this.getLocaleMap(desc, asList(locale));
 	}
 
 	/**
 	 * @param desc
 	 * @param locales
 	 * @return
 	 */
 	public LocaleMap getLocaleMap(ControllerDescriptor desc, Collection<Locale> locales) {
 		Locale lc = this.findLocale(locales);
		logger.debug("Loading i18n files for locale %s", lc);
 		LocaleMap map = new LocaleMap(lc);
		String[] names = { BASE_RESOURCE, desc.getControllerClass(), desc.getControllerClass() + "." + desc.getFullControllerName() };
 		for (String name : names) {
 			try {
 				map.addResourceBundle(this.getResource(name, lc));
 			} catch (Exception ex) {
 				logger.tdebug("Error loading resouce bundle", ex);
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Try to gets the {@link ResourceBundle} with given name and locale. It lookup for
 	 * name_lang_country and name_lang files at the base folder.
 	 * 
 	 * @return The loaded {@link ResourceBundle} or <code>null</code> if unable to load
 	 */
 	private ResourceBundle getResource(String name, Locale lc) throws IOException {
 		String lang = lc.getLanguage();
 		String country = lc.getCountry();
 		String langName = this.getBaseName(name) + "_" + lang;
 
 		ResourceBundle result = null;
 		if (!country.isEmpty()) {
 			result = this.tryLoad(langName + "_" + country);
 		}
 		if (result == null) {
 			result = this.tryLoad(langName);
 		}
 
 		return result;
 	}
 
 	/**
 	 * Try to load the given resource. It tries to load from file system, using {@link File}, and if
 	 * not successful, it tries to load from {@link ClassLoader} as a resource.
 	 * 
 	 * @param resourcePath
 	 *            The path for the resource
	 * @return The loaded resource bundle, or <code>null</code> if it's not able to load.
 	 */
 	private ResourceBundle tryLoad(String resourcePath) throws IOException {
 		InputStreamReader in = null;
 		try {
 			File f = new File(resourcePath);
 			if (f.exists()) {
 				in = new InputStreamReader(new FileInputStream(f), getProperty(CHARSET));
 			} else {
				InputStream inStream = this.loader.getResourceAsStream(resourcePath);
				if (inStream != null) {
					in = new InputStreamReader(inStream, getProperty(CHARSET));
				} else {
					return null;
				}
 			}
 			return new PropertyResourceBundle(in);
 		} finally {
 			if (in != null) {
 				in.close();
 			}
 		}
 	}
 
 	/**
 	 * Resolves which locale should be used to load the {@link LocaleMap}.
 	 * 
 	 * Given the request's locales, in preference order, it chooses the first supported one.
 	 * 
 	 * @param locales
 	 *            The request acceptable locales.
 	 * @return The prefered locale supported by both request and application to be used.
 	 */
 	private Locale findLocale(Collection<Locale> locales) {
		if (supportedLocales.size() != 1) {
			for (Locale lc : locales) {
				if (supportedLocales.contains(lc)) {
					return lc;
				}
 			}
 		}
		return supportedLocales.iterator().next();
 	}
 
 	/**
 	 * @return the full base name for a resource, relative to it absolute path.
 	 */
 	private String getBaseName(String baseName) {
 		if (!BASE_FOLDER.endsWith("/")) {
 			return BASE_FOLDER + "/" + baseName;
 		} else {
 			return BASE_FOLDER + baseName;
 		}
 	}
 
 }
