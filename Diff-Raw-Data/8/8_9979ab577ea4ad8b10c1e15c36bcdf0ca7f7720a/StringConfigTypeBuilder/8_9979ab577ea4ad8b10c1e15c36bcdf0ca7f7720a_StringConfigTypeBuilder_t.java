 package org.araqne.log.api;
 
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 public class StringConfigTypeBuilder {
 	Map<Locale, String> names = new HashMap<Locale, String>();
 	Map<Locale, String> descriptions = new HashMap<Locale, String>();
 	Map<Locale, String> defaultValues = new HashMap<Locale, String>();
 	private final String optKey;
 	private final boolean isRequired;
 
 	public StringConfigTypeBuilder(String optKey, String optName, String optDesc, boolean isRequired, String defaultValue) {
 		this.optKey = optKey;
 		this.isRequired = isRequired;
 		this.names.put(Locale.ENGLISH, optName);
 		this.descriptions.put(Locale.ENGLISH, optDesc);
 		this.defaultValues.put(Locale.ENGLISH, defaultValue);
 	}
 
 	public StringConfigTypeBuilder(String optKey, String optName, boolean isRequired) {
 		this(optKey, optName, optName, isRequired);
 	}
 
 	public StringConfigTypeBuilder(String optKey, String optName, boolean isRequired, String defaultValue) {
		this(optKey, optName, optName, isRequired, defaultValue);
 	}
 
 	public StringConfigTypeBuilder(String optKey, String optName, String optDesc, boolean isRequired) {
 		this(optKey, optName, optDesc, isRequired, "");
 	}
 
 	public StringConfigTypeBuilder addLocaleInfo(Locale locale, String name, String desc) {
 		this.names.put(locale, name);
 		this.descriptions.put(locale, name);
 		return this;
 	}
 
 	public AbstractConfigType get() {
		if (isRequired)
			return new StringConfigType(optKey, names, descriptions, isRequired);
		else
			return new StringConfigType(optKey, names, descriptions, isRequired, defaultValues);
 	}
 }
