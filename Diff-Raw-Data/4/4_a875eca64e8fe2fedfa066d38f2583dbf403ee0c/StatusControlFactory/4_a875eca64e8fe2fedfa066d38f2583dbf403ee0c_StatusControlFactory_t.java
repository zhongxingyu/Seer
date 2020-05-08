 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2004 Nigel Westbury <westbury@users.sourceforge.net>
  *
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  */
 
 package net.sf.jmoney.reconciliation;
 
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.IPropertyControl;
 import net.sf.jmoney.model2.IPropertyControlFactory;
 import net.sf.jmoney.model2.PropertyAccessor;
 
 import org.eclipse.swt.widgets.Composite;
 
 /**
  * A control factory to select the uncleared/pending/reconciled status
  * of an entry.
  * 
  * @author Nigel Westbury
  */
 public class StatusControlFactory implements IPropertyControlFactory {
 
 	/**
 	 * Localized descriptions of the reconciled/cleared status
 	 */
 	static String[] statusText = new String[] {
 			ReconciliationPlugin.getResourceString("Entry.uncleared"),
 			ReconciliationPlugin.getResourceString("Entry.reconciling"),
 			ReconciliationPlugin.getResourceString("Entry.cleared"),
 	};
 
 	/**
 	 * Single letter indicators to be used in tables
 	 */
 	static String[] shortStatusText = new String[] {
 			ReconciliationPlugin.getResourceString("Entry.unclearedShort"),
 			ReconciliationPlugin.getResourceString("Entry.reconcilingShort"),
 			ReconciliationPlugin.getResourceString("Entry.clearedShort"),
 	};
 	
     public IPropertyControl createPropertyControl(Composite parent, PropertyAccessor propertyAccessor) {
         return new StatusEditor(parent, propertyAccessor);
     }
 
     public String formatValueForMessage(ExtendableObject extendableObject, PropertyAccessor propertyAccessor) {
         int status = extendableObject.getIntegerPropertyValue(propertyAccessor);
         return statusText[status];
     }
 
     public String formatValueForTable(ExtendableObject extendableObject, PropertyAccessor propertyAccessor) {
         int status = extendableObject.getIntegerPropertyValue(propertyAccessor);
         return shortStatusText[status];
     }
 
 	public boolean isEditable() {
 		return true;
 	}
 }
