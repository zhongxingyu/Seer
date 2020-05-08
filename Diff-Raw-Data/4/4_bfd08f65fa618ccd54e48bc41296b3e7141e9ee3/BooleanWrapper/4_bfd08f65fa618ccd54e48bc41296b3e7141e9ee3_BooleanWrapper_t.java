 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.richbeans.components.wrappers;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 
 import uk.ac.gda.richbeans.components.FieldComposite;
 import uk.ac.gda.richbeans.event.ValueEvent;
 
 /**
  * @author fcp94556
  * 
  * You have to be a widget (even though not needed) so that RCP developer
  * can deal with using the class. Therefore in inherits from Composite.
  */
 public class BooleanWrapper extends FieldComposite{
 	
 	public enum BOOLEAN_MODE {
 		/**
 		 * Default
 		 */
 		DIRECT,
 		/**
 		 * Shows the value to the user inversely to the data.
 		 */
 		REVERSE
 	}
 
 	private BOOLEAN_MODE booleanMode;
 	private Button       checkBox;
 	private SelectionAdapter selectionListener;
 
 	/**
 	 * @param parent
 	 * @param style
 	 */
 	public BooleanWrapper(final Composite parent, final int style) {
 		super(parent, SWT.NONE);
 		setLayout(new FillLayout());
 		checkBox = new Button(this, style|SWT.CHECK);
 		mainControl = checkBox;
 		this.selectionListener = new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				final ValueEvent evt = new ValueEvent(checkBox,getFieldName());
 				evt.setValue(getValue());
 				eventDelegate.notifyValueListeners(evt);
 			}
 		};
 		checkBox.addSelectionListener(selectionListener);
 
 	}
 	
 	@Override
 	public void dispose() {
 		if (checkBox!=null&&!checkBox.isDisposed()) checkBox.removeSelectionListener(selectionListener);
 		super.dispose();
 	}
 
 	/**
 	 * @param booleanMode
 	 */
 	public void setBooleanMode(final BOOLEAN_MODE booleanMode) {
 		this.booleanMode = booleanMode;
 	}
 
 	@Override
 	public Boolean getValue() {
 		final boolean value = checkBox.getSelection();
 		if (booleanMode==null||booleanMode == BOOLEAN_MODE.DIRECT) return value;
 		return !value;
 	}
 
 	@Override
 	public void setValue(Object value) {
 		if (!(value instanceof Boolean)) throw new RuntimeException("Cannot parse "+value+" to Boolean.");
 		if (booleanMode==null||booleanMode == BOOLEAN_MODE.DIRECT) {
 			checkBox.setSelection(((Boolean)value).booleanValue());
 		} else {
 			checkBox.setSelection(!((Boolean)value).booleanValue());
 		}
 		final ValueEvent evt = new ValueEvent(checkBox,getFieldName());
 		eventDelegate.notifyValueListeners(evt);
 	}
 	
 	/*******************************************************************/
 	/**        This section will be the same for many wrappers.       **/
 	/*******************************************************************/
 	@Override
 	protected void checkSubclass () {
 	}
 
 	/**
 	 * @param string
 	 */
 	public void setText(String string) {
 		checkBox.setText(string);
 	}
 	
 	@Override
 	public void setToolTipText(String string) {
 		super.setToolTipText(string);
 		checkBox.setToolTipText(string);
 	}
 	
 	public Button getButton(){
 		return checkBox;
 	}
 }
 
 	
