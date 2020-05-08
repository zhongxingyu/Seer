 // Copyright © 2011 Martin Ueding <dev@martin-ueding.de>
 
 /*
  * This file is part of jscribble.
  *
  * jscribble is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 2 of the License, or (at your option)
  * any later version.
  *
  * jscribble is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * jscribble.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package jscribble.helpers;
 
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 
 /**
  * Localizes strings.
  *
  * @author Martin Ueding <dev@martin-ueding.de>
  */
 public class Localizer {
 	/**
 	 * Bundle containing the l10n into.
 	 */
 	static ResourceBundle bundle;
 
 	/**
 	 * Translate a string.
 	 *
 	 * @param ident English language string.
 	 * @return Native language string.
 	 */
 	public static String get(String ident) {
 		// Try to load the ResourceBundle.
 		if (bundle == null) {
 			try {
 				bundle = ResourceBundle.getBundle("jscribble");
 			}
 			catch (ExceptionInInitializerError e) {
				Logger.log(Localizer.class.getClass().getName(),
 				           "Error: " + e.getMessage());
 				bundle = null;
 			}
 			catch (MissingResourceException e) {
				Logger.log(Localizer.class.getClass().getName(),
 				           "Error: " + e.getMessage());
 				bundle = null;
 			}
 		}
 
 		// Do not translate if there is no translation around.
 		if (bundle == null) {
 			return ident;
 		}
 		else {
 			try {
 				return bundle.getString(ident);
 			}
 			catch (MissingResourceException e) {
				Logger.log(Localizer.class.getClass().getName(),
 				           e.getMessage());
 				return ident;
 			}
 		}
 	}
 }
