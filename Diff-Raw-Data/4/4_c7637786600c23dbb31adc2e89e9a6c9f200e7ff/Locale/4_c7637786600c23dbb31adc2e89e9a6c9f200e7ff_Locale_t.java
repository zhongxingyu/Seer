 /*
 Ziah_'s Client
 Copyright (C) 2013  Ziah Jyothi
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see [http://www.gnu.org/licenses/].
 */
 
 package com.oneofthesevenbillion.ziah.ZiahsClient;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 
 import net.minecraft.src.Minecraft;
 import net.minecraft.src.ResourceLocation;
 
 public class Locale {
     private static Map<String, Map<String, String>> languageTranslations = new HashMap<String, Map<String, String>>();
     private static String defaultLang = "en_US";
 
     public static void loadLanguages(List<ResourceLocation> langFiles) {
         for (ResourceLocation langFile : langFiles) {
         	try {
 				InputStream langIn = Minecraft.getMinecraft().func_110442_L().func_110536_a(langFile).func_110527_b();
 				String language = langFile.func_110623_a().substring(langFile.func_110623_a().lastIndexOf("/") + 1, langFile.func_110623_a().lastIndexOf("."));
 				Locale.loadLanguage(language, langIn);
 			} catch (IOException e) {
 				ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "Failed to load language file: " + langFile.func_110623_a() + "!", e);
 			}
         }
     }
 
 	public static void loadLanguages(Map<String, InputStream> mlangFiles) {
 		for (Entry<String, InputStream> lang : mlangFiles.entrySet()) {
 		    Locale.loadLanguage(lang.getKey(), lang.getValue());
 		}
 	}
 
 	public static void loadLanguage(String language, InputStream langIn) {
        Map<String, String> translations = Locale.languageTranslations.containsKey(language) ? Locale.languageTranslations.get(language) : new HashMap<String, String>();
         BufferedReader in = new BufferedReader(new InputStreamReader(langIn));
 
         while (true) {
             try {
                 String line = in.readLine();
                 if (line == null) break;
                 if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                     String[] ldata = line.split("=", 2);
                     if (ldata[0].length() > 0 && ldata[1].length() > 0) {
                         translations.put(ldata[0], ldata[1]);
                     }
                 }
             } catch (IOException e) {
                 ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "Failed to load part of language: " + language + "!", e);
             }
         }
 
         if (!translations.isEmpty()) Locale.languageTranslations.put(language, translations);
 	}
 
     public static String localize(String unlocalizedString) {
         String language = Minecraft.getMinecraft().gameSettings.language;
         if (!Locale.languageTranslations.containsKey(language)) {
             if (Locale.languageTranslations.containsKey(Locale.defaultLang)) {
                 language = Locale.defaultLang;
             }else{
                 return unlocalizedString;
             }
         }
         if (!Locale.languageTranslations.get(language).containsKey(unlocalizedString)) {
        	System.out.println(Locale.languageTranslations.get(Locale.defaultLang));
             if (!Locale.languageTranslations.get(Locale.defaultLang).containsKey(unlocalizedString)) {
                 return unlocalizedString;
             }else{
                 return Locale.languageTranslations.get(Locale.defaultLang).get(unlocalizedString);
             }
         }
         return Locale.languageTranslations.get(language).get(unlocalizedString);
     }
 }
