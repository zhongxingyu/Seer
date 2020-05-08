 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.filters.table.ui;
 
 import net.sf.okapi.common.ui.abstracteditor.IDialogPage;
 import net.sf.okapi.common.ui.abstracteditor.SWTUtil;
 import net.sf.okapi.filters.table.base.Parameters;
 import net.sf.okapi.lib.extra.filters.CompoundFilterParameters;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Widget;
 
 /**
  * 
  * 
  * @version 0.1, 20.06.2009
  */
 
 public class TableTab extends Composite implements IDialogPage, SelectionListener {
 	
 	private static String NONE_ID = "%%%^^^$$$nonenonenone$$$^^^%%%";
 	
 	private Group grpTableType;
 	private Button btnCSV;
 	private Button btnTSV;
 	private Button btnFWC;
 	private Group grpTableProperties;
 	private Label lblValuesStartAt;
 	private Spinner start;
 	private Spinner cols;
 	private Label lcols;
 	private Group csvOptions;
 	private Button removeQualif;
 	private Button nqualif;
 	private Button trim;
 	private Button allT;
 	private Group extr;
 	private Button header;
 	private Button names;
 	private Button allE;
 	private Button body;
 	private Group csvActions;
 //	private FormData formData_9;
 	private Text custDelim;
 	private Text custQualif;
 	private Combo delim;
 	private Combo qualif;
 	private Label label_2;
 	private Label label_3;
 
 	/**
 	 * Create the composite.
 	 * @param parent
 	 * @param style
 	 */
 	public TableTab(Composite parent, int style) {
 		super(parent, style);
 		setLayout(new GridLayout(3, false));
 		
 		grpTableType = new Group(this, SWT.NONE);
 		grpTableType.setLayout(new GridLayout(1, false));
 		grpTableType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
 		grpTableType.setText("Table type");
 		
 		btnCSV = new Button(grpTableType, SWT.RADIO);
 		btnCSV.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		btnCSV.setData("name", "btnCSV");
 		btnCSV.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				
 				interop(e.widget);
 			}
 		});
 		btnCSV.setText("CSV (Columns, separated by a comma, semicolon, etc.)                  ");
 		
 		btnTSV = new Button(grpTableType, SWT.RADIO);
 		btnTSV.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		btnTSV.setData("name", "btnTSV");
 		btnTSV.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				
 				interop(e.widget);
 			}
 		});
 		btnTSV.setText("TSV (Columns, separated by one or more tabs)");
 		
 		btnFWC = new Button(grpTableType, SWT.RADIO);
 		btnFWC.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		btnFWC.setData("name", "btnFWC");
 		btnFWC.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				
 				interop(e.widget);
 			}
 		});
 		btnFWC.setText("Fixed-width columns");
 		
 		grpTableProperties = new Group(this, SWT.NONE);
 		grpTableProperties.setLayout(new GridLayout(2, false));
 		grpTableProperties.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
 		grpTableProperties.setText("Table properties");
 		
 		lblValuesStartAt = new Label(grpTableProperties, SWT.NONE);
 		lblValuesStartAt.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
 		lblValuesStartAt.setAlignment(SWT.RIGHT);
 		lblValuesStartAt.setText("Values start at line:");
 		
 		start = new Spinner(grpTableProperties, SWT.BORDER);
 		start.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				
 				interop(e.widget);
 			}
 		});
 		start.setMinimum(1);
 		
 		lcols = new Label(grpTableProperties, SWT.NONE);
 		lcols.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
 		lcols.setAlignment(SWT.RIGHT);
 		lcols.setText("Line with column names (0 if none):");
 		
 		cols = new Spinner(grpTableProperties, SWT.BORDER);
 		cols.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				
 				interop(e.widget);
 			}
 		});
 		
 		csvOptions = new Group(this, SWT.NONE);
 		csvOptions.setLayout(new GridLayout(1, false));
 		csvOptions.setText("CSV options");
 		csvOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		{
 			Composite composite = new Composite(csvOptions, SWT.NONE);
 			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 			composite.setLayout(new GridLayout(2, false));
 			{
 				Label label = new Label(composite, SWT.NONE);
 				label.setText("Field delimiter:");
 				label.setAlignment(SWT.RIGHT);
 				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
 			}
 			{
 				delim = new Combo(composite, SWT.READ_ONLY);
 				delim.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						
 						interop(e.widget);
 						if (custDelim.getEnabled()) custDelim.setFocus();
 					}
 				});
 				delim.setItems(new String[] {"Comma (,)", "Semi-colon (;)", "Tab", "Space", "Custom"});
 				delim.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 				delim.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
 				delim.select(0);
 			}
 			Label label = new Label(composite, SWT.NONE);
 			label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
 			{
 				custDelim = new Text(composite, SWT.BORDER);
 				custDelim.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 				custDelim.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
 			}
 			new Label(composite, SWT.NONE);
 			new Label(composite, SWT.NONE);
 			{
 				Label label_1 = new Label(composite, SWT.NONE);
 				label_1.setText("Text qualifier:");
 				label_1.setAlignment(SWT.RIGHT);
 				label_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
 			}
 			{
 				qualif = new Combo(composite, SWT.READ_ONLY);
 				qualif.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						
 						interop(e.widget);
 						if (custQualif.getEnabled()) custQualif.setFocus();
 					}
 				});
 				qualif.setItems(new String[] {"Double-quote (\")", "Apostrophe (')", "None", "Custom"});
 				qualif.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 				qualif.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
 				qualif.select(0);
 			}
 			new Label(composite, SWT.NONE);
 			{
 				custQualif = new Text(composite, SWT.BORDER);
 				custQualif.setData("name", "custQualif");
 				custQualif.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 				custQualif.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
 			}
 		}
 		
 		csvActions = new Group(this, SWT.NONE);
 		csvActions.setLayout(new GridLayout(2, false));
 		csvActions.setText("CSV actions");
 		csvActions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		
 		removeQualif = new Button(csvActions, SWT.CHECK);
 		removeQualif.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
 		removeQualif.setText("Exclude qualifiers from extracted text");
 		
 		trim = new Button(csvActions, SWT.CHECK);
 		trim.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
 		trim.setData("name", "trim");
 		trim.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				
 				interop(e.widget);
 			}
 		});
 		trim.setText("Exclude leading/trailing white spaces from extracted text");
 		
 		label_2 = new Label(csvActions, SWT.NONE);
 		label_2.setData("name", "label_2");
 		label_2.setText("    ");
 		
 		nqualif = new Button(csvActions, SWT.RADIO);
 		nqualif.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
 		nqualif.setText("Only entries without qualifiers    ");
 		new Label(csvActions, SWT.NONE);
 		
 		allT = new Button(csvActions, SWT.RADIO);
 		allT.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
 		allT.setText("All");
 		
 		extr = new Group(this, SWT.NONE);
 		extr.setLayout(new GridLayout(2, false));
 		extr.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		extr.setText("Extraction mode");
 		
 		header = new Button(extr, SWT.CHECK);
 		header.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
 		header.setData("name", "header");
 		header.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				
 				interop(e.widget);
 			}
 		});
 		header.setText("Extract header lines");
 		
 		label_3 = new Label(extr, SWT.NONE);
 		label_3.setData("name", "label_3");
 		label_3.setText("    ");
 		
 		names = new Button(extr, SWT.RADIO);
 		names.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
 		names.setText("Column names only         ");
 		new Label(extr, SWT.NONE);
 		
 		allE = new Button(extr, SWT.RADIO);
 		allE.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
 		allE.setText("All");
 		
 		body = new Button(extr, SWT.CHECK);
 		body.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
 		body.addSelectionListener(this);
 		body.setData("name", "body");
 		body.setText("Extract table data");
 
 	}
 
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents subclassing of SWT components
 	}
 
 	public boolean canClose(boolean isOK) {
 		
 		return true;
 	}
 
 	public void interop(Widget speaker) {
 
 		SWTUtil.setAllEnabled(csvOptions, btnCSV.getSelection());
 		SWTUtil.setAllEnabled(csvActions, btnCSV.getSelection());
 		
 		custDelim.setEnabled(delim.getSelectionIndex() == 4);		
 		
 		custQualif.setEnabled(qualif.getSelectionIndex() == 3);
 		
 		boolean noQualif = qualif.getSelectionIndex() == 2;
 		boolean trimOn = trim.getSelection(); // && csvActions.getEnabled();
 		boolean noHeader = start.getSelection() <= 1;
 		boolean headerOn = header.getSelection();
 		boolean noColNames = cols.getSelection() == 0;
 		
 		//if (noQualif || !csvActions.getEnabled()) removeQualif.setSelection(false);		
 		if (noQualif && csvActions.getEnabled()) removeQualif.setSelection(false);
 		removeQualif.setEnabled(!noQualif && csvActions.getEnabled());
 		
 		//----------------------------
 		if (noQualif) {
 			
 			nqualif.setSelection(false);
 			nqualif.setEnabled(false);
 						
 			allT.setEnabled(false);
 			allT.setSelection(false);
 			
 //			trim.setSelection(false);
 //			trim.setEnabled(false);
 		}
 		else {
 						
 			nqualif.setEnabled(trimOn);
 			allT.setEnabled(trimOn);
 			
 		}
 
 //		trim.setEnabled(csvActions.getEnabled());
 		
 		if (!trimOn) {
 			
 			trim.setSelection(false);
 			nqualif.setSelection(false);
 			allT.setSelection(false);
 			
 			nqualif.setEnabled(false);
 			allT.setEnabled(false);
 		}
 		else {
 			
 			nqualif.setEnabled(!noQualif && csvActions.getEnabled());
 			allT.setEnabled(csvActions.getEnabled());
 			
 			if (!nqualif.getSelection() && !allT.getSelection()) { // Default selection if none selected
 				
 				if (nqualif.getEnabled()) {
 					
 					nqualif.setSelection(true);
 					allT.setSelection(false);
 				}
 				else {
 					
 					nqualif.setSelection(false);
 					allT.setSelection(true);
 				}
 			}
 		}
 		
 		//----------------------------
 		if (noHeader) {
 					
 			names.setSelection(false);
 			names.setEnabled(false);
 			
 			allE.setSelection(false);
 			allE.setEnabled(false);
 		
 			header.setSelection(false);
 			header.setEnabled(false);
 			
 			cols.setSelection(0);
 			cols.setEnabled(false);
 			lcols.setEnabled(false);
 		}
 		else {
 		
 			lcols.setEnabled(true);
 			cols.setEnabled(true);
 			header.setEnabled(true);
 			
 			names.setEnabled(headerOn);
 			allE.setEnabled(headerOn);
 
 			if (!headerOn) {
 				
 				names.setSelection(false);
 				allE.setSelection(false);
 			}
 			else {
 			
 				if (!names.getSelection() && !allE.getSelection()) { // Default selection if none selected
 					
 					names.setSelection(headerOn);
 					allE.setSelection(false);
 				}
 			}
 							
 		}
 		
 		cols.setMaximum(start.getSelection() - 1);
 		
 		if (noColNames) {
 			
 			names.setSelection(false);
 			allE.setSelection(allE.getEnabled());
 			names.setEnabled(false);
 		}
 		else {
 			
 			names.setEnabled(headerOn);
 		}
 		
 		// Make sure main box and children are disabled together
 		SWTUtil.disableIfDisabled(names, header);
 		SWTUtil.disableIfDisabled(allE, header);
 		
 		
 		// Lock Extract table data enabled if no header
 		SWTUtil.selectIfDisabled(body, header);
 		
 		// Do not allow both boxes empty
 		if (body.getEnabled()) {
 			
 			if (! header.getSelection() && ! body.getSelection())
 				body.setSelection(true);
 		}
 	}
 
 	public boolean load(Object data) {
 
 		if (data instanceof CompoundFilterParameters) {
 			
 			CompoundFilterParameters params = (CompoundFilterParameters) data;
 			
 			Class<?> c = params.getParametersClass();
 				
 			if (c == net.sf.okapi.filters.table.csv.Parameters.class) {
 				
 				btnCSV.setSelection(true);
 				btnTSV.setSelection(false);
 				btnFWC.setSelection(false);
 			}
 			else if (c == net.sf.okapi.filters.table.tsv.Parameters.class) {
 				
 				btnCSV.setSelection(false);
 				btnTSV.setSelection(true);
 				btnFWC.setSelection(false);
 			}		
 			else if (c == net.sf.okapi.filters.table.fwc.Parameters.class) {
 				
 				btnCSV.setSelection(false);
 				btnTSV.setSelection(false);
 				btnFWC.setSelection(true);
 			}			
 			else {
 				
 				btnCSV.setSelection(true);
 				btnTSV.setSelection(false);
 				btnFWC.setSelection(false);
 			}
 			
 		} 
 		else {
 			if (data instanceof net.sf.okapi.filters.table.csv.Parameters) {
 			
 				net.sf.okapi.filters.table.csv.Parameters params =
 					(net.sf.okapi.filters.table.csv.Parameters) data;
 				
 				if (params.fieldDelimiter.equals(",")) {
 					delim.select(0);
 					custDelim.setText("");
 				}
 				else if (params.fieldDelimiter.equals(";")) {
 					delim.select(1);
 					custDelim.setText("");
 				}
 				else if (params.fieldDelimiter.equals("\t")) {
 					delim.select(2);
 					custDelim.setText("");
 				} 
 				else if (params.fieldDelimiter.equals(" ")) {
 					delim.select(3);
 					custDelim.setText("");
 				}
 				else {
 					delim.select(4);
 					custDelim.setText(params.fieldDelimiter);
 				}
 				
 				if (params.textQualifier.equals("\"")) {
 					qualif.select(0);
 					custQualif.setText("");
 				}
 				else if (params.textQualifier.equals("\'")) {
 					qualif.select(1);
 					custQualif.setText("");
 				}
 				else if (params.textQualifier.equals(NONE_ID)) {
 					qualif.select(2);
 					custQualif.setText("");
 				} 
 				else {
 					qualif.select(3);
 					custQualif.setText(params.textQualifier);
 				}
 				
 				removeQualif.setSelection(params.removeQualifiers);
 			}
 //			else if (data instanceof net.sf.okapi.filters.table.tsv.Parameters) {
 //				
 //				net.sf.okapi.filters.table.tsv.Parameters params =
 //					(net.sf.okapi.filters.table.tsv.Parameters) data;
 //							
 //			}
 //			else if (data instanceof net.sf.okapi.filters.table.fwc.Parameters) {
 //			
 //				net.sf.okapi.filters.table.fwc.Parameters params =
 //					(net.sf.okapi.filters.table.fwc.Parameters) data;
 //				
 //			}
 			
 			// Common part
 			
 			if (data instanceof net.sf.okapi.filters.table.base.Parameters) {
 				
 				net.sf.okapi.filters.table.base.Parameters params =
 					(net.sf.okapi.filters.table.base.Parameters) data;
 			
 				cols.setSelection(params.columnNamesLineNum);
 				start.setSelection(params.valuesStartLineNum);
 				
 				//-----------------------
 				
 				body.setSelection(params.sendColumnsMode != Parameters.SEND_COLUMNS_NONE);
 				
 				//-----------------------
 				if (params.sendHeaderMode == Parameters.SEND_HEADER_NONE) {
 					
 					header.setSelection(false);
 					names.setSelection(false);
 					allE.setSelection(false);
 				}
 									
 				else if (params.sendHeaderMode == Parameters.SEND_HEADER_COLUMN_NAMES_ONLY) {
 					
 					header.setEnabled(true);
 					header.setSelection(true);
 					names.setSelection(true);
 					allE.setSelection(false);
 				}
 									
 				else if (params.sendHeaderMode == Parameters.SEND_HEADER_ALL) {
 					
 					header.setEnabled(true);
 					header.setSelection(true);
 					names.setSelection(false);
 					allE.setSelection(true);
 				}
 				
 				//-----------------------
 				if (params.trimMode == Parameters.TRIM_NONQUALIFIED_ONLY) {
 					
 					trim.setEnabled(true);
 					trim.setSelection(true);
 					nqualif.setSelection(true);
 					allT.setSelection(false);
 				}
 				
				else if (params.trimMode == Parameters.TRIM_ALL) {
 					
 					trim.setEnabled(true);
 					trim.setSelection(true);
 					nqualif.setSelection(false);
 					allT.setSelection(true);
 				}
 				
				else if (params.trimMode == Parameters.TRIM_NONE) {
 					
 					trim.setSelection(false);
 					nqualif.setSelection(false);
 					allT.setSelection(false);
 				}				
 			}			
 		}
 
 		return true;
 	}
 
 	public boolean save(Object data) {
 
 		if (data instanceof CompoundFilterParameters) {
 			
 			CompoundFilterParameters params = (CompoundFilterParameters) data;
 			
 			if (btnCSV.getSelection())
 				params.setParametersClass(net.sf.okapi.filters.table.csv.Parameters.class);
 
 			else if (btnTSV.getSelection()) 
 				params.setParametersClass(net.sf.okapi.filters.table.tsv.Parameters.class);
 			
 			else if (btnFWC.getSelection()) 
 				params.setParametersClass(net.sf.okapi.filters.table.fwc.Parameters.class);
 			
 			else
 				params.setParametersClass(net.sf.okapi.filters.table.base.Parameters.class);
 		} 		
 		else {
 			if (data instanceof net.sf.okapi.filters.table.csv.Parameters) {
 			
 				net.sf.okapi.filters.table.csv.Parameters params =
 					(net.sf.okapi.filters.table.csv.Parameters) data;
 				
 				switch (delim.getSelectionIndex()) {
 				
 				case 0:
 					params.fieldDelimiter = ",";
 					break;
 					
 				case 1:
 					params.fieldDelimiter = ";";
 					break;
 					
 				case 2:
 					params.fieldDelimiter = "\t";
 					break;
 			
 				case 3:
 					params.fieldDelimiter = " ";
 					break;				
 					
 				case 4:
 					params.fieldDelimiter = custDelim.getText();
 					break;
 				}
 				
 				switch (qualif.getSelectionIndex()) {
 				
 				case 0:
 					params.textQualifier = "\"";
 					break;
 					
 				case 1:
 					params.textQualifier = "\'";
 					break;
 					
 				case 2:					
 					params.textQualifier = NONE_ID;
 					break;
 			
 				case 3:
 					params.textQualifier = custQualif.getText();
 					break;				
 				}
 				
 				params.removeQualifiers = removeQualif.getSelection();				
 			}
 //			else if (data instanceof net.sf.okapi.filters.table.tsv.Parameters) {
 //				
 //				net.sf.okapi.filters.table.tsv.Parameters params =
 //					(net.sf.okapi.filters.table.tsv.Parameters) data;
 //						
 //			}
 //			else if (data instanceof net.sf.okapi.filters.table.fwc.Parameters) {
 //			
 //				net.sf.okapi.filters.table.fwc.Parameters params =
 //					(net.sf.okapi.filters.table.fwc.Parameters) data;
 //				
 //			}
 			
 			// Common part
 			
 			if (data instanceof net.sf.okapi.filters.table.base.Parameters) {
 				
 				net.sf.okapi.filters.table.base.Parameters params =
 					(net.sf.okapi.filters.table.base.Parameters) data;
 			
 				params.columnNamesLineNum = cols.getSelection();
 				params.valuesStartLineNum = start.getSelection();
 				
 				if (header.getSelection() && names.getSelection())
 					params.sendHeaderMode = Parameters.SEND_HEADER_COLUMN_NAMES_ONLY;
 				
 				else if (header.getSelection() && allE.getSelection())
 					params.sendHeaderMode = Parameters.SEND_HEADER_ALL;
 				
 				else
 					params.sendHeaderMode = Parameters.SEND_HEADER_NONE;
 				
 				
 				if (!body.getSelection())
 					params.sendColumnsMode = Parameters.SEND_COLUMNS_NONE;
 				
 				if (trim.getSelection() && nqualif.getSelection())
 					params.trimMode = Parameters.TRIM_NONQUALIFIED_ONLY;
 				
 				else if (trim.getSelection() && allT.getSelection())
 					params.trimMode = Parameters.TRIM_ALL;
 				
 				else
 					params.trimMode = Parameters.TRIM_NONE;
 			}
 		}
 
 		return true;
 	}
 		
 	public void widgetDefaultSelected(SelectionEvent e) {
 	}
 	
 	public void widgetSelected(SelectionEvent e) {
 		
 		interop(e.widget);
 	}
 }
 
