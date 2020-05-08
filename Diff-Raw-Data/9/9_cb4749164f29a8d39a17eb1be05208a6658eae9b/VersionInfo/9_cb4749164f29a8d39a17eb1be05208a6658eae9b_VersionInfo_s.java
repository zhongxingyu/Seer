 package ru.socionicasys.analyst.service;
 
 /**
  * Информация о имени/версии/билде.
  */
public class VersionInfo {
 	private static final String APPLICATION_NAME = "Информационный анализ";
 	private static final String VERSION = VersionInfo.class.getPackage().getSpecificationVersion();
 
 	private VersionInfo() {
 	}
 
 	/**
 	 * @return Имя приложения
 	 */
 	public static String getApplicationName() {
 		return APPLICATION_NAME;
 	}
 
 	/**
 	 * @return Версия приложения
 	 */
 	public static String getVersion() {
 		return VERSION;
 	}
 }
