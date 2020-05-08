 package org.glom.web.shared.libglom;
 
 import java.io.Serializable;
import java.util.TreeMap;
 
 import org.glom.web.client.StringUtils;
 
 public class Translatable implements Serializable {
 
 	private static final long serialVersionUID = 700462080795724363L;
 
	// We use TreeMap instead of HashTable because GWT does not support HashTable.
	public static class TranslationsMap extends TreeMap<String, String> {
 
 		private static final long serialVersionUID = 1275019181399622213L;
 	};
 
 	private String name = "";
 	private String titleOriginal = "";
 
 	// A map of localeID to title:
 	TranslationsMap translationsMap = new TranslationsMap();
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(final String name) {
 		this.name = name;
 	}
 
 	public String getTitleOriginal() {
 		return titleOriginal;
 	}
 
 	public void setTitleOriginal(final String title) {
 		this.titleOriginal = title;
 	}
 
 	public String getTitle() {
 		return getTitleOriginal();
 	}
 
 	public String getTitle(final String locale) {
 		if (StringUtils.isEmpty(locale)) {
 			return getTitleOriginal();
 		}
 
 		final String title = translationsMap.get(locale);
 		if (title == null) {
 			return "";
 		}
 
 		return title;
 	}
 
 	/**
 	 * @param locale
 	 * @return
 	 */
 	public String getTitleOrName(final String locale) {
 		final String title = getTitle(locale);
 		if (StringUtils.isEmpty(title)) {
 			return getName();
 		}
 
 		return title;
 	};
 
 	/**
 	 * Make sure that getTitle() or getTitleOriginal() returns the specified translation. And discard all translations.
 	 * You should probably only call this on a clone()ed item.
 	 * 
 	 * @param locale
 	 */
 	public void makeTitleOriginal(final String locale) {
 		final String title = getTitle(locale);
 		translationsMap.clear();
 		setTitleOriginal(title);
 	}
 
 	/**
 	 * @param translatedTitle
 	 * @param locale
 	 */
 	public void setTitle(final String title, final String locale) {
 		if (StringUtils.isEmpty(locale)) {
 			setTitleOriginal(title);
 			return;
 		}
 
 		translationsMap.put(locale, title);
 	}
 }
