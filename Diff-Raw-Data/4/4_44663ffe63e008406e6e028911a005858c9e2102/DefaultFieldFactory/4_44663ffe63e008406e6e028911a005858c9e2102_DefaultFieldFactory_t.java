 package com.github.gwateke.ui.form.support;
 
 import java.util.Date;
 import java.util.List;
 import java.util.MissingResourceException;
 
 
 import com.github.gwateke.binding.form.FieldMetadata;
 import com.github.gwateke.context.MessageSource;
 import com.github.gwateke.ui.RadioButtonGroup;
 import com.github.gwateke.ui.form.FieldFactory;
 import com.github.gwateke.ui.form.KeyboardFilter;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Hidden;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.datepicker.client.DateBox;
 
 
 public class DefaultFieldFactory implements FieldFactory {
 
 	private final int MAX_VISIBLE_FIELD_SIZE = 30;/*em*/
 
 	
 	public Widget createWidget(FieldMetadata field, MessageSource messageSource) {
 		final Boolean display = field.getUserMetadata("display");
 		if (display != null && !display) {
 			return new Hidden();
 		}
 
 		final Boolean association = field.getUserMetadata("association");
 		if (association != null && association) {
			Boolean manyToOne = field.getUserMetadata("manyToOne");
			Boolean oneToOne = field.getUserMetadata("oneToOne");
 			if (manyToOne || oneToOne) {
 				return new ListBox();
 			}
 			else {
 				return null;
 			}
 		}
 		
 		final List<?> inList = field.getUserMetadata("inList");
 		if (inList != null) {
 			if (inList.size() == 2) {
 				return createRadioButtonGroup(field, inList, messageSource);
 			}
 			else {
 				return createListBox(field, inList, messageSource);
 			}
 		} 
 		
 		final String propertyType = field.getPropertyType();
 		if (propertyType.equals(String.class.getName())) {
 			final Boolean password = field.getUserMetadata("password");
 			final Integer maxSize = field.getUserMetadata("maxSize");
 			TextBox textBox = (password != null && password) ? new PasswordTextBox() : new TextBox();
 			if (maxSize != null) {
 				textBox.setWidth(Math.min(MAX_VISIBLE_FIELD_SIZE, maxSize) + "em");
 				textBox.setMaxLength(maxSize);
 			}
 			return textBox;
 		}
 		else if (propertyType.equals("int") || propertyType.equals(Integer.class.getName()) ||
 				propertyType.equals("double") || propertyType.equals(Double.class.getName())) {
 			TextBox textBox = new TextBox();
 			textBox.setStylePrimaryName("gwt-TextBox-numeric");
 			KeyboardFilter keyboardFilter = createKeyboardFilter(propertyType);
 			textBox.addKeyDownHandler(keyboardFilter);
 			textBox.addKeyPressHandler(keyboardFilter);
 			return textBox;
 		}
 		else if (propertyType.equals("boolean") || propertyType.equals(Boolean.class.getName())) {
 			return new CheckBox();
 		}
 		else if (propertyType.equals(Date.class.getName())) {
 			DateTimeFormat shortDateFormat = DateTimeFormat.getShortDateFormat();
 			BindableDateBox dateBox = new BindableDateBox();
 			dateBox.setFormat( new DateBox.DefaultFormat( shortDateFormat ) );
 			dateBox.setWidth( shortDateFormat.getPattern().length() + "em" );
 			return dateBox;
 		}
 		else {
 			return null;
 		}
 	}
 
 	
 	/**
 	 * Construye un filtro de teclado según el tipo de datos de un campo.
 	 * TODO cachear para reutilizar
 	 */
 	protected KeyboardFilter createKeyboardFilter(String propertyType) {
 		if (propertyType.equals("int") || propertyType.equals(Integer.class.getName())) {
 			return new DigitKeyboardFilter();
 		}
 		else if (propertyType.equals("double") || propertyType.equals(Double.class.getName())) {
 			return new DecimalDigitKeyboardFilter();
 		}
 		else {
 			return null;
 		}
 	}
 	
 
 	private Widget createListBox(FieldMetadata field, List<?> inList, MessageSource messageSource) {
 		ListBox listBox = new ListBox();
 		final String keyPrefix = field.getUserMetadata("name");
 		for (Object item: inList) {
 			String value = item.toString();
 			try {
 				listBox.addItem( messageSource.getRequiredMessage(keyPrefix + '.' + value), value );
 			}
 			catch (MissingResourceException e) {
 				listBox.addItem(value);
 			}
 		}
 		return listBox;
 	}
 
 
 	
 	private Widget createRadioButtonGroup(FieldMetadata field, List<?> inList, MessageSource messageSource) {
 		final String name = field.getUserMetadata("name");
 		RadioButtonGroup group = new RadioButtonGroup(name);
 		for (Object item: inList) {
 			String value = item.toString();
 			try {
 				group.addItem( messageSource.getRequiredMessage(name + '.' + value), item );
 			}
 			catch (MissingResourceException e) {
 				group.addItem( value, item );
 			}
 		}
 		return group;
 	}
 }
