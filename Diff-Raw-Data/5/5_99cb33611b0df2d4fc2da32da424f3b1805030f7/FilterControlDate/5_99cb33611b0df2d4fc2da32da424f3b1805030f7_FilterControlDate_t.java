 /*******************************************************************************
  * Copyright (c) 2014 Formal Mind GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.search.filter.ui;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.eclipse.rmf.reqif10.AttributeDefinitionDate;
 import org.eclipse.rmf.reqif10.search.filter.DateFilter;
 import org.eclipse.rmf.reqif10.search.filter.DateFilter.InternalAttribute;
 import org.eclipse.rmf.reqif10.search.filter.IFilter;
 import org.eclipse.rmf.reqif10.search.filter.IFilter.Operator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.DateTime;
 
 public class FilterControlDate extends FilterControl {
 
 	private DateTime control[];
 
 	public FilterControlDate(FilterPanel parent, DateFilter.InternalAttribute attribute) {
 		super(parent, attribute);
 	}
 
 	public FilterControlDate(FilterPanel parent,
 			AttributeDefinitionDate attribute) {
 		super(parent, attribute);
 	}
 
 	public FilterControlDate(FilterPanel parent, DateFilter template) {
 		super(parent, template);		
 	}
 
 	@Override
 	protected List<Operator> getOperators() {
 		return DateFilter.SUPPORTED_OPERATORS.asList();
 	}
 
 	@Override
 	protected void updateValueControls(boolean initialize) {
 		if (getOperator() == Operator.IS_SET
 				|| getOperator() == Operator.IS_NOT_SET) {
 			showControl(0, false);
 			showControl(1, false);
 		}
 		else if (getOperator() == Operator.BETWEEN) {
 			showControl(0, true);
 			showControl(1, true);
 		} else {
 			showControl(0, true);
 			showControl(1, false);
 		}
 		
 		if (initialize) {
			if (control[0] != null) {
 				GregorianCalendar cal = (GregorianCalendar) templateFilter.getFilterValue1();
 				control[0].setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
 			}
			if (control[1] != null) {
 				GregorianCalendar cal = (GregorianCalendar) templateFilter.getFilterValue2();
 				control[1].setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
 			}
 		}
 
 		layout();
 	}
 
 	private void showControl(int controlId, boolean show) {
 		 if (control == null) control = new DateTime[2];
 		 
 		if (show && control[controlId] == null) {
 			control[controlId] = new DateTime(this, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);
 			GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 			control[controlId].setLayoutData(layoutData);
 		} 
 		if (! show && control[controlId] != null) {
 			control[controlId].dispose();
 			control[controlId] = null;
 		}
 	}
 
 	@Override
 	public IFilter getFilter() {
 		GregorianCalendar value1 = control[0] == null ? null
 				: new GregorianCalendar(control[0].getYear(),
 						control[0].getMonth(), control[0].getDay());
 		GregorianCalendar value2 = control[1] == null ? null
 				: new GregorianCalendar(control[1].getYear(),
 						control[1].getMonth(), control[1].getDay());
 		if (attribute instanceof InternalAttribute) {
 			return new DateFilter(getOperator(), value1, value2,
 					(InternalAttribute) attribute);
 		} else {
 			return new DateFilter(getOperator(), value1, value2,
 					(AttributeDefinitionDate) attribute);
 		}
 	}
 }
