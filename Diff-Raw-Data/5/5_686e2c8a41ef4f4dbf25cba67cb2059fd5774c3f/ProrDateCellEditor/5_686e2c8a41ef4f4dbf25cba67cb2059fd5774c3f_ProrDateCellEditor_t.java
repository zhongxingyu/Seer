 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.pror.editor.agilegrid;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.ICellEditorValidator;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
 import org.eclipse.rmf.reqif10.common.util.ReqIF10Util;
 import org.eclipse.rmf.reqif10.pror.util.ProrUtil;
 
 public class ProrDateCellEditor extends ProrCellEditor {
 
 	public ProrDateCellEditor(AgileGrid agileGrid, EditingDomain editingDomain,
 			SpecElementWithAttributes parent,
 			Object affectedObject) {
 		super(agileGrid, editingDomain, parent, affectedObject);
 		this.setValidator(new ICellEditorValidator() {
 			public String isValid(Object value) {
 				if (value == null) {
 					return null;
 				}
 				try {
 					ProrDateCellEditor.this.stingToCalendar(value.toString());
 				} catch (ParseException e) {
 					return "Required Format: "
 							+ DateFormat.getDateInstance().format(new Date());
 				} catch (DatatypeConfigurationException e) {
 					return "Parsing Problem: " + e.toString();
 				}
 				return null;
 			}
 		});
 	}
 	
 	@Override
 	protected Object doGetValue() {
 		try {
 			XMLGregorianCalendar value = stingToCalendar(text.getText());
 			ProrUtil.setTheValue(attributeValue, value, parent, affectedObject,
 					editingDomain);
 		} catch (ParseException e) {
			e.printStackTrace();
 			// No action necessary, we simply restore the old value.
 		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
 			// No action necessary, we simply restore the old value.
 		}
 		return attributeValue;
 	}
 
 	@Override
 	protected void doSetValue(Object value) {
 		if (value instanceof AttributeValue) {
 			attributeValue = (AttributeValue) value;
 
 			Object cal = ReqIF10Util.getTheValue(attributeValue);
 			if (cal instanceof XMLGregorianCalendar) {
 				text.setText(DateFormat.getDateInstance().format(
 						((XMLGregorianCalendar) cal).toGregorianCalendar()
 								.getTime()));
 			}
 		}
 	}
 
 	/**
 	 * Helper that converts String to {@link XMLGregorianCalendar}. We use this
 	 * for Validation and for setting.
 	 */
 	private XMLGregorianCalendar stingToCalendar(String value)
 			throws ParseException, DatatypeConfigurationException {
 		Date date = DateFormat.getDateInstance().parse(value.toString());
 		GregorianCalendar cal = new GregorianCalendar();
 		cal.setTime(date);
 		return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
 	}
 }
