 package org.iucn.sis.shared.api.models.fields;
 
 import org.iucn.sis.shared.api.models.Field;
 
 public class UseTradeField extends ProxyField {
 	
 	public static final String PURPOSE_KEY = "purpose";
 	public static final String SOURCE_KEY = "source";
 	public static final String FORM_REMOVED_KEY = "formRemoved";
 	public static final String SUBSISTENCE_KEY = "subsistence";
 	public static final String NATIONAL_KEY = "national";
 	public static final String INTERNATIONAL_KEY = "international";
 	public static final String HARVEST_LEVEL_KEY = "harvestLevel";
 	public static final String UNITS_KEY = "units";
 	public static final String POSSIBLE_THREAT_KEY = "possibleThreat";
 	public static final String JUSTIFICATION_KEY = "justification";
 
 	public UseTradeField(Field field) {
 		super(field);
 	}
 
 	public void setPurpose(Integer value) {
 		setForeignKeyPrimitiveField(PURPOSE_KEY, value);
 	}
 	
 	public Integer getPurpose() {
 		return getForeignKeyPrimitiveField(PURPOSE_KEY, 0);
 	}
 	
 	public void setSource(Integer value) {
 		setForeignKeyPrimitiveField(SOURCE_KEY, value);
 	}
 	
 	public Integer getSource() {
 		return getForeignKeyPrimitiveField(SOURCE_KEY, 0);
 	}
 	
 	public void setFormRemoved(Integer value) {
 		setForeignKeyPrimitiveField(FORM_REMOVED_KEY, value);
 	}
 	
 	public Integer getFormRemoved() {
 		return getForeignKeyPrimitiveField(FORM_REMOVED_KEY, 0);
 	}
 	
 	public void setSubsistence(Boolean value) {
 		setBooleanPrimitiveField(SUBSISTENCE_KEY, value, Boolean.FALSE);
 	}
 	
 	public Boolean getSubsistence() {
 		return getBooleanPrimitiveField(SUBSISTENCE_KEY, Boolean.FALSE);
 	}
 	
 	public void setNational(Boolean value) {
 		setBooleanPrimitiveField(NATIONAL_KEY, value, Boolean.FALSE);
 	}
 	
 	public Boolean getNational() {
 		return getBooleanPrimitiveField(NATIONAL_KEY, Boolean.FALSE);
 	}
 	
 	public void setInternational(Boolean value) {
 		setBooleanPrimitiveField(INTERNATIONAL_KEY, value, Boolean.FALSE);
 	}
 	
 	public Boolean getInternational() {
 		return getBooleanPrimitiveField(INTERNATIONAL_KEY, Boolean.FALSE);
 	}
 	
 	public void setHarvestLevel(String value) {
		setStringPrimitiveField(HARVEST_LEVEL_KEY, value);
 	}
 	
 	public String getHarvestLevel() {
		return getStringPrimitiveField(HARVEST_LEVEL_KEY);
 	}
 	
 	public void setUnits(Integer value) {
 		setForeignKeyPrimitiveField(UNITS_KEY, value);
 	}
 	
 	public Integer getUnits() {
 		return getForeignKeyPrimitiveField(UNITS_KEY, 0);
 	}
 	
 	public void setPossibleThreat(Boolean value) {
 		setBooleanPrimitiveField(POSSIBLE_THREAT_KEY, value, Boolean.FALSE);
 	}
 	
 	public Boolean getPossibleThreat() {
 		return getBooleanPrimitiveField(POSSIBLE_THREAT_KEY, Boolean.FALSE);
 	}
 	
 	public void setJustification(String value) {
 		setTextPrimitiveField(JUSTIFICATION_KEY, value);
 	}
 	
 	public String getJustification() {
 		return getTextPrimitiveField(JUSTIFICATION_KEY);
 	}
 	
 	@Override
 	public void setForeignKeyPrimitiveField(String key, Integer value) {
 		Integer toSave = value;
 		if (toSave.intValue() == 0)
 			toSave = null;
 		
 		super.setForeignKeyPrimitiveField(key, toSave);
 	}
 	
 }
