 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
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
 
 package net.sf.okapi.ui.filters.openxml;
 
 import net.sf.okapi.common.ui.UIUtil;
 
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.TabItem;
 import swing2swt.layout.BorderLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Control;
 
 public class UIEditor extends org.eclipse.swt.widgets.Dialog {
 
 	protected Object result;
 	protected Shell shlOfficeFilter;
 	protected Button btnHelp;
 	protected Button btnOk;
 	protected Button btnCancel;
 	protected Button btnTranslateDocumentProperties;
 	protected Button btnTranslateComments;
 	protected Button btnTranslateHeadersAndFooters;
 	protected Button btnTranslateHiddenText;
 	protected Button btnStylesFromDocument;
 	protected Button btnColorsFromDocument;
 	protected Button btnExcludeExcelColumns;
 	protected List listExcludedWordStyles;
 	protected List listExcelColorsToExclude;
 	protected List listExcelSheet1ColumnsToExclude;
 	protected List listExcelSheet2ColumnsToExclude;
 	protected List listExcelSheet3ColumnsToExclude;
 	protected Button btnTranslateNotes;
 	protected Button btnTranslateMasters;
 	protected Composite compositeOKCancel;
 	/**
 	 * Create the dialog.
 	 * @param parent
 	 * @param style
 	 */
 	public UIEditor(Shell parent, int style) {
 		super(parent, style);
 		setText("SWT Dialog");
 	}
 
 	/**
 	 * Open the dialog.
 	 * @return the result
 	 */
 	public Object open() {
 		createContents(); // !!! DWH 6-17-09 do this explicitly, then add listeners
 		shlOfficeFilter.open();
 		shlOfficeFilter.layout();
 		Display display = getParent().getDisplay();
 		while (!shlOfficeFilter.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 		return result;
 	}
 
 	public Object open(net.sf.okapi.ui.filters.openxml.Editor sl) {
 		createContents(); // !!! DWH 6-17-09 do this explicitly, then add listeners
 		btnCancel.addSelectionListener(sl); // DWH 6-17-09 Editor is the listener
 		btnOk.addSelectionListener(sl); // DWH 6-17-09 Editor is the listener
 		btnHelp.addSelectionListener(sl); // DWH 6-17-09 Editor is the listener
 //		ed.btnStylesFromDocument.addSelectionListener(this);
 //		ed.btnColorsFromDocument.addSelectionListener(this);
		sl.setData(); // initialize the data
 		shlOfficeFilter.open();
 		shlOfficeFilter.layout();
 		Display display = getParent().getDisplay();
 		while (!shlOfficeFilter.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Create contents of the dialog.
 	 */
 	protected void createContents() { // DWH 6-17-09 was private
 		shlOfficeFilter = new Shell(getParent(), getStyle());
 		shlOfficeFilter.setLayout(new BorderLayout(0, 0));
 		shlOfficeFilter.setSize(450, 300);
 		shlOfficeFilter.setText("Office 2007 Filter Parameters");
 		{
 			compositeOKCancel = new Composite(shlOfficeFilter, SWT.NONE);
 			FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
 			fillLayout.spacing = 3;
 			fillLayout.marginWidth = 3;
 			fillLayout.marginHeight = 3;
 			compositeOKCancel.setLayout(fillLayout);
 			compositeOKCancel.setLayoutData(BorderLayout.SOUTH);
 			{
 				btnHelp = new Button(compositeOKCancel, SWT.NONE);
 				btnHelp.addSelectionListener(new SelectionAdapter() {
 					public void widgetDefaultSelected(SelectionEvent e) {
 						int iii = 1;
 						iii = iii+2;
 					}
 				});
 				btnHelp.setText("Help");
 			}
 			{
 				Label label = new Label(compositeOKCancel, SWT.NONE);
 			}
 			{
 				btnOk = new Button(compositeOKCancel, SWT.NONE);
 				btnOk.setText("OK");
 			}
 			{
 				btnCancel = new Button(compositeOKCancel, SWT.NONE);
 				btnCancel.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 					}
 				});
 				btnCancel.setText("Cancel");
 			}
 			if ( UIUtil.getPlatformType() == UIUtil.PFTYPE_WIN )
 				compositeOKCancel.setTabList(new Control[]{btnOk, btnCancel, btnHelp});
 			else
 				compositeOKCancel.setTabList(new Control[]{btnCancel, btnOk, btnHelp});
 		}
 		{
 			TabFolder tabFolder = new TabFolder(shlOfficeFilter, SWT.NONE);
 			tabFolder.setLayoutData(BorderLayout.CENTER);
 			{
 				TabItem tbtmGeneralOptions_1 = new TabItem(tabFolder, SWT.NONE);
 				tbtmGeneralOptions_1.setText("General Options");
 				{
 					Composite composite = new Composite(tabFolder, SWT.NONE);
 					RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
 					rowLayout.marginTop = 10;
 					rowLayout.spacing = 10;
 					composite.setLayout(rowLayout);
 					tbtmGeneralOptions_1.setControl(composite);
 					{
 						btnTranslateDocumentProperties = new Button(composite, SWT.CHECK);
 						btnTranslateDocumentProperties.setLayoutData(new RowData(356, 16));
 						btnTranslateDocumentProperties.setSelection(true);
 						btnTranslateDocumentProperties.setText("Translate Document Properties");
 					}
 					{
 						btnTranslateComments = new Button(composite, SWT.CHECK);
 						btnTranslateComments.setLayoutData(new RowData(343, 16));
 						btnTranslateComments.setSelection(true);
 						btnTranslateComments.setText("Translate Comments");
 					}
 				}
 			}
 			{
 				TabItem tbtmWordOptions = new TabItem(tabFolder, SWT.NONE);
 				tbtmWordOptions.setText("Word Options");
 				{
 					Composite composite = new Composite(tabFolder, SWT.NONE);
 					RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
 					rowLayout.marginTop = 10;
 					rowLayout.spacing = 10;
 					composite.setLayout(rowLayout);
 					tbtmWordOptions.setControl(composite);
 					{
 						btnTranslateHeadersAndFooters = new Button(composite, SWT.CHECK);
 						btnTranslateHeadersAndFooters.setLayoutData(new RowData(290, 16));
 						btnTranslateHeadersAndFooters.setSelection(true);
 						btnTranslateHeadersAndFooters.setText("Translate Headers and Footers");
 					}
 					{
 						btnTranslateHiddenText = new Button(composite, SWT.CHECK);
 						btnTranslateHiddenText.setLayoutData(new RowData(291, 16));
 						btnTranslateHiddenText.setSelection(true);
 						btnTranslateHiddenText.setText("Translate Hidden Text");
 					}
 					{
 						Composite compositeWordStyles = new Composite(composite, SWT.NONE);
 						compositeWordStyles.setLayoutData(new RowData(292, 67));
 //						{
 //							btnStylesFromDocument = new Button(compositeWordStyles, SWT.NONE);
 //							btnStylesFromDocument.setBounds(269, 8, 146, 23);
 //							btnStylesFromDocument.setText("Styles from Document ...");
 //						}
 						{
 							Label lblStyles = new Label(compositeWordStyles, SWT.NONE);
 							lblStyles.setBounds(172, 10, 110, 21);
 							lblStyles.setText("Styles to Exclude");
 						}
 						{
 							listExcludedWordStyles = new List(compositeWordStyles, SWT.BORDER | SWT.V_SCROLL);
 							listExcludedWordStyles.setItems(new String[] {"Emphasis", "ExcludeCharacterStyle", "ExcludeParagraphStyle", "Heading1", "Heading2", "Normal", "Title", "Strong", "Subtitle", "tw4winExternal"});
 							listExcludedWordStyles.setBounds(10, 0, 156, 68);
 						}
 					}
 				}
 			}
 			{
 				TabItem tbtmExcelOptions = new TabItem(tabFolder, SWT.NONE);
 				tbtmExcelOptions.setText("Excel Options");
 				{
 					Composite composite = new Composite(tabFolder, SWT.NONE);
 					tbtmExcelOptions.setControl(composite);
 					{
 						Composite compositeExcelColors = new Composite(composite, SWT.NONE);
 						compositeExcelColors.setBounds(0, 0, 459, 45);
 						{
 							Label lblColorsToExclude = new Label(compositeExcelColors, SWT.NONE);
 							lblColorsToExclude.setBounds(161, 10, 288, 21);
 							lblColorsToExclude.setText("Colors to Exclude");
 						}
 //						{
 //							btnColorsFromDocument = new Button(compositeExcelColors, SWT.NONE);
 //							btnColorsFromDocument.setBounds(289, 10, 146, 23);
 //							btnColorsFromDocument.setText("Colors from Document ...");
 //						}
 						{
 							listExcelColorsToExclude = new List(compositeExcelColors, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
 							listExcelColorsToExclude.setItems(new String[] {"black", "blue", "cyan", "green", "magenta", "red", "white", "yellow"});
 							listExcelColorsToExclude.setBounds(10, 0, 135, 45);
 						}
 					}
 					{
 						Composite compositeExcelSheet1 = new Composite(composite, SWT.NONE);
 						compositeExcelSheet1.setBounds(0, 51, 459, 45);
 						{
 							Label lblSheetColumns = new Label(compositeExcelSheet1, SWT.NONE);
 							lblSheetColumns.setBounds(162, 24, 287, 21);
 							lblSheetColumns.setText("Sheet 1 Columns to Exlude");
 						}
 						{
 							listExcelSheet1ColumnsToExclude = new List(compositeExcelSheet1, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
 							listExcelSheet1ColumnsToExclude.setItems(new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "X", "Y", "Z", "AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH", "AI", "AJ", "AK", "AL", "AM", "AN", "AO", "AP", "AQ", "AR", "AS", "AT", "AU", "AV", "AX", "AY", "AZ", "BA", "BB", "BC", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BK", "BL", "BM", "BN", "BO", "BP", "BQ", "BR", "BS", "BT", "BU", "BV", "BX", "BY", "BZ", "CA", "CB", "CC", "CD", "CE", "CF", "CG", "CH", "CI", "CJ", "CK", "CL", "CM", "CN", "CO", "CP", "CQ", "CR", "CS", "CT", "CU", "CV", "CX", "CY", "CZ", "DA", "DB", "DC", "DD", "DE", "DF", "DG", "DH", "DI", "DJ", "DK", "DL", "DM", "DN", "DO", "DP", "DQ", "DR", "DS", "DT", "DU", "DV", "DX", "DY", "DZ", "EA", "EB", "EC", "ED", "EE", "EF", "EG", "EH", "EI", "EJ", "EK", "EL", "EM", "EN", "EO", "EP", "EQ", "ER", "ES", "ET", "EU", "EV", "EX", "EY", "EZ"});
 							listExcelSheet1ColumnsToExclude.setBounds(10, 0, 135, 45);
 						}
 						{
 							btnExcludeExcelColumns = new Button(compositeExcelSheet1, SWT.CHECK);
 							btnExcludeExcelColumns.setBounds(162, 2, 287, 16);
 							btnExcludeExcelColumns.setSelection(true);
 							btnExcludeExcelColumns.setText("Exclude Marked Columns in Each Sheet");
 						}
 					}
 					{
 						Composite compositeExcelSheet2 = new Composite(composite, SWT.NONE);
 						compositeExcelSheet2.setBounds(0, 102, 459, 45);
 						{
 							Label lblSheetColumns_1 = new Label(compositeExcelSheet2, SWT.NONE);
 							lblSheetColumns_1.setText("Sheet 2 Columns to Exlude");
 							lblSheetColumns_1.setBounds(162, 24, 287, 21);
 						}
 						{
 							listExcelSheet2ColumnsToExclude = new List(compositeExcelSheet2, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
 							listExcelSheet2ColumnsToExclude.setItems(new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "X", "Y", "Z", "AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH", "AI", "AJ", "AK", "AL", "AM", "AN", "AO", "AP", "AQ", "AR", "AS", "AT", "AU", "AV", "AX", "AY", "AZ", "BA", "BB", "BC", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BK", "BL", "BM", "BN", "BO", "BP", "BQ", "BR", "BS", "BT", "BU", "BV", "BX", "BY", "BZ", "CA", "CB", "CC", "CD", "CE", "CF", "CG", "CH", "CI", "CJ", "CK", "CL", "CM", "CN", "CO", "CP", "CQ", "CR", "CS", "CT", "CU", "CV", "CX", "CY", "CZ", "DA", "DB", "DC", "DD", "DE", "DF", "DG", "DH", "DI", "DJ", "DK", "DL", "DM", "DN", "DO", "DP", "DQ", "DR", "DS", "DT", "DU", "DV", "DX", "DY", "DZ", "EA", "EB", "EC", "ED", "EE", "EF", "EG", "EH", "EI", "EJ", "EK", "EL", "EM", "EN", "EO", "EP", "EQ", "ER", "ES", "ET", "EU", "EV", "EX", "EY", "EZ"});
 							listExcelSheet2ColumnsToExclude.setBounds(10, 0, 135, 45);
 						}
 					}
 					{
 						Composite compositeExcelSheet3 = new Composite(composite, SWT.NONE);
 						compositeExcelSheet3.setBounds(0, 152, 459, 45);
 						{
 							Label lblSheetColumns_2 = new Label(compositeExcelSheet3, SWT.NONE);
 							lblSheetColumns_2.setText("Sheets 3 (and higher) Columns to Exlude");
 							lblSheetColumns_2.setBounds(162, 24, 287, 21);
 						}
 						{
 							listExcelSheet3ColumnsToExclude = new List(compositeExcelSheet3, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
 							listExcelSheet3ColumnsToExclude.setItems(new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "X", "Y", "Z", "AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH", "AI", "AJ", "AK", "AL", "AM", "AN", "AO", "AP", "AQ", "AR", "AS", "AT", "AU", "AV", "AX", "AY", "AZ", "BA", "BB", "BC", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BK", "BL", "BM", "BN", "BO", "BP", "BQ", "BR", "BS", "BT", "BU", "BV", "BX", "BY", "BZ", "CA", "CB", "CC", "CD", "CE", "CF", "CG", "CH", "CI", "CJ", "CK", "CL", "CM", "CN", "CO", "CP", "CQ", "CR", "CS", "CT", "CU", "CV", "CX", "CY", "CZ", "DA", "DB", "DC", "DD", "DE", "DF", "DG", "DH", "DI", "DJ", "DK", "DL", "DM", "DN", "DO", "DP", "DQ", "DR", "DS", "DT", "DU", "DV", "DX", "DY", "DZ", "EA", "EB", "EC", "ED", "EE", "EF", "EG", "EH", "EI", "EJ", "EK", "EL", "EM", "EN", "EO", "EP", "EQ", "ER", "ES", "ET", "EU", "EV", "EX", "EY", "EZ"});
 							listExcelSheet3ColumnsToExclude.setBounds(10, 0, 135, 45);
 						}
 					}
 				}
 			}
 			{
 				TabItem tbtmPowerpointOptions = new TabItem(tabFolder, SWT.NONE);
 				tbtmPowerpointOptions.setText("Powerpoint Options");
 				{
 					Composite composite = new Composite(tabFolder, SWT.NONE);
 					RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
 					rowLayout.marginTop = 10;
 					rowLayout.spacing = 10;
 					composite.setLayout(rowLayout);
 					tbtmPowerpointOptions.setControl(composite);
 					{
 						btnTranslateNotes = new Button(composite, SWT.CHECK);
 						btnTranslateNotes.setLayoutData(new RowData(346, 16));
 						btnTranslateNotes.setSelection(true);
 						btnTranslateNotes.setText("Translate Notes");
 					}
 					{
 						btnTranslateMasters = new Button(composite, SWT.CHECK);
 						btnTranslateMasters.setLayoutData(new RowData(345, 16));
 						btnTranslateMasters.setSelection(true);
 						btnTranslateMasters.setText("Translate Masters");
 					}
 				}
 			}
 		}
 
 	}
 }
