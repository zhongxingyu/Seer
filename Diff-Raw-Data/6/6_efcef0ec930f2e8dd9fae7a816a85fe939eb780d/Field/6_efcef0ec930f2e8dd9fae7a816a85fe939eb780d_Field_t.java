 package edu.helsinki.sulka.models;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.fasterxml.jackson.annotation.JsonSetter;
 
 @JsonIgnoreProperties(ignoreUnknown=true)
 public class Field {
 	@JsonProperty("field")
 	private String fieldName;
 	
 	@JsonProperty("name")
 	private String name;
 
 	@JsonProperty("description")
 	private String description;
 	
 	public static enum FieldType {
 		VARCHAR,
 		INTEGER,
 		DECIMAL,
 		DATE,
 		ENUMERATION
 	}
 	@JsonProperty("type")
 	private FieldType type;
 
 	public static enum ViewMode {
 		BROWSING,
 		RINGINGS,
 		RECOVERIES
 	};
 	
 	@JsonProperty("modes")
 	private Set<ViewMode> viewModes;
 	
 	@JsonSetter("modes")
 	public void setViewModes(String[] modes) {
 		Set<ViewMode> viewModes = new HashSet<ViewMode>();
 		for (String mode : modes) {
 			try {
 				viewModes.add(ViewMode.valueOf(mode));
 			} catch (IllegalArgumentException e) {
 				// Ignore
 			}
 		}
 		this.viewModes = viewModes;
 	}
 	
 	@JsonIgnoreProperties(ignoreUnknown=true)
 	public static class EnumerationValue {
 		@JsonProperty("description")
 		private String description;
 		
 		@JsonProperty("value")
 		private String value;
 		
 		/*
 		 * Accessors
 		 */
 		public String getDescription() {
 			return description;
 		}
 		
 		public String getValue() {
 			return value;
 		}
 	}
 	@JsonProperty("enumerationValues")
 	private EnumerationValue[] enumerationValues;
 	
 	/*
 	 * Accessors
 	 */
 	public String getFieldName() {
 		return this.fieldName;
 	}
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	public String getDescription() {
 		return this.description;
 	}
 	
 	public FieldType getType() {
 		return type;
 	}
 	
 	public EnumerationValue[] getEnumerationValues() {
 		return enumerationValues;
 	}
 	
 	public Set<ViewMode> getViewModes() {
 		return viewModes;
 	}
 	
	@JsonIgnore
	private Map<String, String> enumerationDescriptionsMap = null;
	
 	/**
 	 * Get map of enumeration descriptions by enumeration values.
 	 * Assumes getType() == FieldType.ENUMERATION
 	 * @return Map of enumeration value descriptions by possible enumeration values.
 	 */
 	public Map<String, String> getEnumerationDescriptionsMap() {
 		if (this.enumerationDescriptionsMap != null) {
 			return this.enumerationDescriptionsMap;
 		}
 		
 		EnumerationValue[] evs = getEnumerationValues();
 		HashMap<String, String> enumerationMap = new HashMap<String, String>(evs.length);
 		for (EnumerationValue ev : evs) {
 			enumerationMap.put(ev.value, ev.description);
 		}
 		
 		return enumerationDescriptionsMap = enumerationMap;
 	}
 	
 	/**
 	 * Assumes getType() == FieldType.ENUMERATION
 	 * @param  enumerationValue Valid enumeration value
 	 * @return Description for enumeration value enumerationValue
 	 */
 	public String getEnumerationDescription(final String enumerationValue) {
 		return getEnumerationDescriptionsMap().get(enumerationValue);
 	}
 	
 	/*
 	 * Hash implementation
 	 */
 	@Override
 	public int hashCode() {
 		return this.getFieldName().hashCode();
 	}
 	
 	@Override
 	public boolean equals(Object other) {
 		if (other instanceof Field) {
 			return this.getFieldName().equals(((Field) other).getFieldName());
 		}
 		return false;
 	}
 }
