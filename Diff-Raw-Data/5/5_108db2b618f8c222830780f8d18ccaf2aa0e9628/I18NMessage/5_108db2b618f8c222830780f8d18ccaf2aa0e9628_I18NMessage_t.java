 /*
  * This file is part of Common Implementation + API for MineQuest, This provides the common classes for the MineQuest official implementation of the API..
  * Common Implementation + API for MineQuest is licensed under GNU General Public License v3.
  * Copyright (C) The MineQuest Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.common.util;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.text.MessageFormat;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.logging.Level;
 
 import com.theminequest.api.Managers;
 import com.theminequest.api.util.PropertiesFile;
 import com.theminequest.common.Common;
 
 // See http://docs.oracle.com/javase/tutorial/i18n/format/index.html
 public class I18NMessage {
 	
 	private static final String LOCATION = Managers.getPlatform().getResourceDirectory().getAbsolutePath() + File.separator + "locales";
	private static final String CUSTOM = I18NMessage.LOCATION + File.separator + "custom.properties";
 	private static final Locale LOCALE = new Locale(Managers.getPlatform().getConfigurationFile().getString("locale", "en_US"));
 	
 	public static Locale getLocale() {
 		return I18NMessage.LOCALE;
 	}
 	
 	public static String tr(String translate, Object... input) {
 		return getLocale().tr(translate, input);
 	}
 	
 	public static String _(String translate, Object... input) {
 		return getLocale()._(translate, input);
 	}
 	
 	public static final class Locale {
 		
 		private java.util.Locale locale;
 		private PropertiesFile localeprops;
 		private PropertiesFile customprops;
 		
 		private Locale(String localename) {
 			if (localename.contains("_")) {
 				String[] splitlocale = localename.split("_");
 				locale = new java.util.Locale(splitlocale[0], splitlocale[1]);
 			} else {
 				locale = new java.util.Locale(localename);
 			}
 			
 			File localedir = new File(I18NMessage.LOCATION);
 			if (!localedir.exists() || !localedir.isDirectory()) {
 				if (localedir.exists())
 					localedir.delete();
 				localedir.mkdirs();
 			}
 			
			File localefile = new File(I18NMessage.LOCATION + File.separator + localename + ".properties");
 			if (!localefile.exists())
 				copyFromJar(localefile, localename);
 			else {
 				localeprops = new PropertiesFile(localefile.getAbsolutePath());
 				if (!localeprops.getString("lastversion", "0").equals(Common.getCommon().getVersion())) {
 					localeprops = null;
 					copyFromJar(localefile, localename);
 				}
 			}
 			
 			localeprops = new PropertiesFile(localefile);
 			localeprops.setString("lastversion", Common.getCommon().getVersion());
 			customprops = new PropertiesFile(I18NMessage.CUSTOM);
 			if (!customprops.getString("lastversion", Common.getCommon().getVersion()).equals(Common.getCommon().getVersion()))
 				Managers.log(Level.WARNING, "[i18n] Custom translations may be out of date!");
 			customprops.setString("lastversion", Common.getCommon().getVersion());
 		}
 		
 		/**
 		 * Use MessageFormat to translate the string
 		 * in a Gettext-like way (that is,
 		 * the string will be translated on the fly.)
 		 * 
 		 * @param translate String to translate
 		 * @return Translated string
 		 */
 		public String tr(String translate, Object... input) {
 			String touse = localeprops.getChatString(translate);
 			if (customprops.containsKey(translate))
 				touse = customprops.getChatString(translate);
 			
 			MessageFormat format = new MessageFormat("");
 			format.setLocale(locale);
 			
 			format.applyPattern(touse);
 			return format.format(input);			
 		}
 		
 		/**
 		 * Shortcut for Gettext-like translation.
 		 * 
 		 * @param translate
 		 * @return Translated String
 		 */
 		public String _(String translate, Object... input) {
 			return tr(translate, input);
 		}
 		
 		private void copyFromJar(File localefile, String localename) {
 			try (JarFile file = new JarFile(Managers.getPlatform().getJarFile())) {
 				
 				JarEntry jarentry = file.getJarEntry("i18n/" + localename + ".properties");
 				if (jarentry == null) {
 					Managers.log(Level.SEVERE, "[i18n] Can't find locale " + localename + "; using en_US");
 					jarentry = file.getJarEntry("i18n/en_US.properties");
 				}
 				copyCloseStream(file.getInputStream(jarentry), new FileOutputStream(localefile));
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		
 		private void copyCloseStream(InputStream in, OutputStream out) throws IOException {
 			try {
 				byte[] buffer = new byte[4096];
 				int n = 0;
 				while (-1 != (n = in.read(buffer))) {
 					out.write(buffer, 0, n);
 				}
 			} finally {
 				in.close();
 				out.close();
 			}
 		}
 		
 	}
 	
 }
