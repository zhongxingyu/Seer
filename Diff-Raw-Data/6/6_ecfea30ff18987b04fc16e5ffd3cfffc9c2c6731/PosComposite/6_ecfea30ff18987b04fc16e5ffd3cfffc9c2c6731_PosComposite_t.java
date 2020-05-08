 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
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
 
 package gda.simplescan;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
 
 import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
 import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
 import uk.ac.gda.richbeans.event.ValueEvent;
 import uk.ac.gda.richbeans.event.ValueListener;
 
 public class PosComposite extends Composite {
 
 	private ComboWrapper scannableName;
 	SimpleScan bean;
 	private Text text;
 
 	public PosComposite(Composite parent, int style, Object editingBean) {
 		super(parent, style);
 
 		bean = (SimpleScan) editingBean;
 
 		Group grpPos = new Group(this, SWT.NONE);
 		grpPos.setBounds(0, 0, 232, 174);
 		GridData gd_grpScannable = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
 		gd_grpScannable.widthHint = 597;
 		grpPos.setLayoutData(gd_grpScannable);
 		grpPos.setText("Pos");
 		grpPos.setLayout(new GridLayout(1, false));
 
 		Composite posComposite = new Composite(grpPos, SWT.NONE);
 		GridData gd_posComposite = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
 		gd_posComposite.widthHint = 218;
 		posComposite.setLayoutData(gd_posComposite);
 		GridLayout gl_posComposite = new GridLayout(2, false);
 		gl_posComposite.horizontalSpacing = 3;
 		posComposite.setLayout(gl_posComposite);
 
 		Label lblScannable = new Label(posComposite, SWT.NONE);
 		lblScannable.setText("Scannable");
 
 		createScannables(posComposite);
 		
 		
 		
 		Composite composite_2 = new Composite(grpPos, SWT.NONE);
 		GridData gd_composite_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
 		gd_composite_2.widthHint = 218;
 		composite_2.setLayoutData(gd_composite_2);
 		composite_2.setLayout(new GridLayout(3, false));
 		
 				Label lblTo = new Label(composite_2, SWT.NONE);
 				lblTo.setText("Demand");
 				ScaleBox textTo = new ScaleBox(composite_2, SWT.NONE);
 				((GridData) textTo.getControl().getLayoutData()).widthHint = 75;
 				textTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 				new Label(textTo, SWT.NONE);
 				
 				Label lblIdle = new Label(composite_2, SWT.NONE);
 				lblIdle.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
 				lblIdle.setText("Idle");
 				
 						Label lblReadback = new Label(composite_2, SWT.NONE);
 						lblReadback.setText("Readback");
 						Label lblReadbackVal = new Label(composite_2, SWT.NONE);
 						lblReadbackVal.setText("2.335mm");
 						
 						Button btnStop = new Button(composite_2, SWT.NONE);
 						btnStop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 						btnStop.setText("Stop");
 						
 						Label lblIncrement = new Label(composite_2, SWT.NONE);
 						lblIncrement.setText("Increment");
 						
 						text = new Text(composite_2, SWT.BORDER);
 						
 						Composite composite = new Composite(composite_2, SWT.NONE);
 						GridLayout gl_composite = new GridLayout(2, false);
 						gl_composite.marginHeight = 0;
 						gl_composite.horizontalSpacing = 0;
 						gl_composite.marginWidth = 0;
 						composite.setLayout(gl_composite);
 						
 						Button btnNewButton = new Button(composite, SWT.NONE);
 						btnNewButton.setText("-");
 						GridData gd_btnNewButton = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
 						gd_btnNewButton.widthHint = 30;
 						btnNewButton.setLayoutData(gd_btnNewButton);
 						btnNewButton.setFont(SWTResourceManager.getFont("Sans", 12, SWT.BOLD));
 						
 						Button btnNewButton_1 = new Button(composite, SWT.NONE);
 						btnNewButton_1.setText("+");
 						GridData gd_btnNewButton_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
 						gd_btnNewButton_1.widthHint = 30;
 						btnNewButton_1.setLayoutData(gd_btnNewButton_1);
 						btnNewButton_1.setFont(SWTResourceManager.getFont("Sans", 12, SWT.BOLD));
 	}
 
 	public void createScannables(Composite comp) {
 		this.scannableName = new ComboWrapper(comp, SWT.NONE);
 		scannableName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		scannableName.addValueListener(new ValueListener() {
 			@Override
 			public void valueChangePerformed(ValueEvent e) {
 				try {
 					// setMotorLimits(bean.getScannableName(), fromPos);
 					// setMotorLimits(bean.getScannableName(), toPos);
 				} catch (Exception e1) {
 				}
 			}
 
 			@Override
 			public String getValueListenerName() {
 				return null;
 			}
 		});
 	}
 }
