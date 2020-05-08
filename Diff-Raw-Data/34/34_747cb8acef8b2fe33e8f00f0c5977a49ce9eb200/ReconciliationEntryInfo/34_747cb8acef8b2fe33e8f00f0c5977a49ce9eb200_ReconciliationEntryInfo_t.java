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
 
 import net.sf.jmoney.model2.EntryInfo;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.ExtensionPropertySet;
 import net.sf.jmoney.model2.IExtensionObjectConstructors;
 import net.sf.jmoney.model2.IPropertyControl;
 import net.sf.jmoney.model2.IPropertyControlFactory;
 import net.sf.jmoney.model2.IPropertySetInfo;
 import net.sf.jmoney.model2.IValues;
 import net.sf.jmoney.model2.PropertyControlFactory;
 import net.sf.jmoney.model2.PropertySet;
 import net.sf.jmoney.model2.ScalarPropertyAccessor;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * Provides the metadata for the extra properties added to each
  * entry by this plug-in.
  * 
  * @author Nigel Westbury
  */
 public class ReconciliationEntryInfo implements IPropertySetInfo {
 
 	private static ExtensionPropertySet<ReconciliationEntry> propertySet = PropertySet.addExtensionPropertySet(ReconciliationEntry.class, EntryInfo.getPropertySet(), new IExtensionObjectConstructors<ReconciliationEntry>() {
 
 		public ReconciliationEntry construct(ExtendableObject extendedObject) {
 			return new ReconciliationEntry(extendedObject);
 		}
 
 		public ReconciliationEntry construct(ExtendableObject extendedObject, IValues values) {
 			return new ReconciliationEntry(
 					extendedObject, 
 					values.getScalarValue(getStatusAccessor()),
 					values.getScalarValue(getStatementAccessor()),
 					values.getScalarValue(getUniqueIdAccessor())
 			);
 		}
 	});
 	
 	private static ScalarPropertyAccessor<Integer> statusAccessor = null;
 	private static ScalarPropertyAccessor<BankStatement> statementAccessor = null;
 	private static ScalarPropertyAccessor<String> uniqueIdAccessor = null;
 	
 	public PropertySet registerProperties() {
 		class NonEditableTextControlFactory extends PropertyControlFactory<String> {
 			
 			public IPropertyControl createPropertyControl(Composite parent, final ScalarPropertyAccessor<String> propertyAccessor) {
 				
 				// Property is not editable
 		        final Label control = new Label(parent, SWT.NONE);
		        return new IPropertyControl<ExtendableObject>() {
 
 					public Control getControl() {
 						return control;
 					}
 
 					public void load(ExtendableObject object) {
 						String text = object.getPropertyValue(propertyAccessor);
 						if (text == null) {
 							control.setText("");
 						} else {
 							control.setText(text);
 						}
 					}
 
 					public void save() {
 						/*
 						 * The property is not editable so there is nothing
 						 * to do here.
 						 */
 					}
 		        };
 			}
 
 			@Override
 			public String formatValueForMessage(ExtendableObject extendableObject, ScalarPropertyAccessor<? extends String> propertyAccessor) {
 				String value = extendableObject.getPropertyValue(propertyAccessor);
 				return (value == null) ? "<blank>" : value;
 			}
 
 			@Override
 			public String formatValueForTable(ExtendableObject extendableObject, ScalarPropertyAccessor<? extends String> propertyAccessor) {
 				String value = extendableObject.getPropertyValue(propertyAccessor);
 				return (value == null) ? "" : value;
 			}
 
 			public String getDefaultValue() {
 				return null;
 			}
 
 			public boolean isEditable() {
 				return true;
 			}
 		}
 
 		IPropertyControlFactory<BankStatement> statementControlFactory = new PropertyControlFactory<BankStatement>() {
 			public IPropertyControl createPropertyControl(Composite parent, final ScalarPropertyAccessor<BankStatement> propertyAccessor) {
 		        final Text control = new Text(parent, SWT.NONE);
		        return new IPropertyControl<ExtendableObject>() {
 
 		        	private ExtendableObject object;
 		        	
 					public Control getControl() {
 						return control;
 					}
 
 					public void load(ExtendableObject object) {
 						this.object = object;
 						BankStatement statement = object.getPropertyValue(propertyAccessor);
 						if (statement == null) {
 							control.setText("");
 						} else {
 							control.setText(statement.toString());
 						}
 					}
 
 					public void save() {
 						// TODO: make this more robust.
 						// And the control is better if a Combo.
 						String text = control.getText();
 						BankStatement value;
 						if (text.length() == 0) {
 							value = null;
 						} else {
 							value = new BankStatement(control.getText());
 						}
 						object.setPropertyValue(propertyAccessor, value);
 					}
 		        };
 			}
 
 			@Override
 			public String formatValueForMessage(ExtendableObject extendableObject, ScalarPropertyAccessor<? extends BankStatement> propertyAccessor) {
 				BankStatement statement = extendableObject.getPropertyValue(propertyAccessor);
 				if (statement == null) {
 					return "unreconciled";
 				} else {
 					return statement.toString();
 				}
 			}
 
 			@Override
 			public String formatValueForTable(ExtendableObject extendableObject, ScalarPropertyAccessor<? extends BankStatement> propertyAccessor) {
 				BankStatement statement = extendableObject.getPropertyValue(propertyAccessor);
 				if (statement == null) {
 					return "unreconciled";
 				} else {
 					return statement.toString();
 				}
 			}
 
 			public BankStatement getDefaultValue() {
 				// By default, not on any statement (unreconciled)
 				return null;
 			}
 
 			public boolean isEditable() {
 				return true;
 			}
 		};
 		
 		// TODO: correct localized text:
 		statusAccessor    = propertySet.addProperty("status", ReconciliationPlugin.getResourceString("Entry.statusShort"), Integer.class, 1, 30, new StatusControlFactory(), null);
 		statementAccessor = propertySet.addProperty("statement", ReconciliationPlugin.getResourceString("Entry.statementShort"), BankStatement.class, 1, 80, statementControlFactory, null);
 		uniqueIdAccessor  = propertySet.addProperty("uniqueId", ReconciliationPlugin.getResourceString("Entry.uniqueIdShort"), String.class, 1, 80, new NonEditableTextControlFactory(), null);
 		
 		return propertySet;
 	}
 
 	/**
 	 * @return
 	 */
 	public static ExtensionPropertySet<ReconciliationEntry> getPropertySet() {
 		return propertySet;
 	}
 
 	/**
 	 * @return
 	 */
 	public static ScalarPropertyAccessor<Integer> getStatusAccessor() {
 		return statusAccessor;
 	}
 
 	/**
 	 * @return
 	 */
 	public static ScalarPropertyAccessor<BankStatement> getStatementAccessor() {
 		return statementAccessor;
 	}	
 
 	/**
 	 * @return
 	 */
 	public static ScalarPropertyAccessor<String> getUniqueIdAccessor() {
 		return uniqueIdAccessor;
 	}
 }
