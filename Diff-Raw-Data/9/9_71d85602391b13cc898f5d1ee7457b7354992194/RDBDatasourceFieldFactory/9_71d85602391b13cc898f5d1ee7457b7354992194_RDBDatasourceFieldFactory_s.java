 package com.pace.settings.ui;
 
 import com.vaadin.data.Item;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Field;
 
 /**
  * RDB Datasource Field Factory
  * 
  * @author JMilliron
  *
  */
 public class RDBDatasourceFieldFactory extends PaceSettingsDefaultFieldFactory {
 
 	
 	private static final long serialVersionUID = 2885762876258153110L;
 	
 	public static final String NAME = "name";
 	public static final String HIBERNATE_PROPERTY_LIST = "hibernatePropertyList";
 		
 	private KeyValueTable hibernatePropertiesTable = new KeyValueTable("Property Key", "Property Value");
 	
 	public RDBDatasourceFieldFactory() {
 		
 		formOrderList.add(NAME);
 		formOrderList.add(HIBERNATE_PROPERTY_LIST);
 		
		captionMap.put(NAME, "Name");
 		captionMap.put(HIBERNATE_PROPERTY_LIST, "Properties");
 		
 		
 			
 	}
 	
 	@Override
 	public Field createField(Item item, Object propertyId, Component uiContext) {
 		
 		Field field = super.createField(item, propertyId, uiContext);
 	
 		if ( propertyId.equals(NAME)) {
 			
 		/*	field.setHeight("3em");
 			field.setWidth("95%");*/
 				
 			field.setReadOnly(true);
 			
 		} else if ( propertyId.equals(HIBERNATE_PROPERTY_LIST)) {
 			
 			
 			hibernatePropertiesTable.setCaption(captionMap.get(HIBERNATE_PROPERTY_LIST));
 			
 			return hibernatePropertiesTable;
 			
 		}
 		
 		
 		return field;
 	}
 
 }
