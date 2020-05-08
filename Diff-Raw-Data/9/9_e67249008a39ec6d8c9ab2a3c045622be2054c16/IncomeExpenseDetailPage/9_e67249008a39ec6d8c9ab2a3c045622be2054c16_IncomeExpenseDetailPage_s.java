 /*
 *
 *  JMoney - A Personal Finance Manager
 *  Copyright (c) 2002 Johann Gyger <johann.gyger@switzerland.org>
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
 
 package net.sf.jmoney.oda.pages;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.Date;
 
 import net.sf.jmoney.IBookkeepingPage;
 import net.sf.jmoney.IBookkeepingPageFactory;
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.VerySimpleDateFormat;
 import net.sf.jmoney.fields.AccountControl;
 import net.sf.jmoney.fields.DateControl;
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.IncomeExpenseAccount;
 import net.sf.jmoney.oda.Activator;
 import net.sf.jmoney.views.NodeEditor;
 import net.sf.jmoney.views.SectionlessPage;
 
 import org.eclipse.birt.core.framework.Platform;
 import org.eclipse.birt.report.engine.api.EngineConfig;
 import org.eclipse.birt.report.engine.api.HTMLRenderOption;
 import org.eclipse.birt.report.engine.api.IRenderOption;
 import org.eclipse.birt.report.engine.api.IReportEngine;
 import org.eclipse.birt.report.engine.api.IReportEngineFactory;
 import org.eclipse.birt.report.engine.api.IReportRunnable;
 import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.osgi.framework.Bundle;
 
 /**
  * This page displays the detailed income and expense items for a
  * particular income and expense account.  Each entry is itemized.
  * <P>
  * This page requires an income and expense account.  It is therefore
  * opened not an a page under reports, but is a page in the editor
  * for the account.  This allows multiple instances of this report
  * to be open in the editor at the same time, which may or may not
  * be a good thing.
  * 
  * @author Nigel Westbury
  */
 public class IncomeExpenseDetailPage implements IBookkeepingPageFactory {
 
     private static final String PAGE_ID = "net.sf.jmoney.reports.incomeAndExpense";
 
     enum DateRanges {
     	THIS_MONTH {
     		@Override protected String getTextKey() { return "Report.IncomeExpense.thisMonth"; }
     	},
     	THIS_YEAR {
     		@Override protected String getTextKey() { return "Report.IncomeExpense.thisYear"; }
     	},
     	LAST_MONTH {
     		@Override protected String getTextKey() { return "Report.IncomeExpense.lastMonth"; }
     	},
     	LAST_YEAR {
     		@Override protected String getTextKey() { return "Report.IncomeExpense.lastYear"; }
     	},
     	CUSTOM {
     		@Override protected String getTextKey() { return "Report.IncomeExpense.custom"; }
     	};
     	
     	protected abstract String getTextKey();
     	
     	public String getText() {
 			return Activator.getResourceString(getTextKey());
     	}
     }
     
 	private Browser viewer;
 
 	private Label periodLabel;
 	Combo periodBox;
 	private Label fromLabel;
 	DateControl fromField;
 	private Label toLabel;
 	DateControl toField;
 	AccountControl<IncomeExpenseAccount> accountField;
 	private Button generateButton;
 
 	/**
 	 * The Shell to use for all message dialogs.
 	 */
 	Shell shell;
 	
 	Date fromDate;
 	Date toDate;
 
 //	private IncomeExpenseAccount account = null;
 
 	/* (non-Javadoc)
 	 * @see net.sf.jmoney.IBookkeepingPageListener#createPages(java.lang.Object, org.eclipse.swt.widgets.Composite)
 	 */
 	Composite createContent(/*IncomeExpenseAccount account, */Composite parent) {
 //		this.account = account;
 		
 		/**
 		 * topLevelControl is a control with grid layout, 
 		 * onto which all sub-controls should be placed.
 		 */
 		Composite topLevelControl = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		topLevelControl.setLayout(layout);
 		
 		shell = topLevelControl.getShell(); 
 
 		viewer = new Browser(topLevelControl, SWT.NONE);
 		
 		GridData gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		viewer.setLayoutData(gridData);
 							
 		// Set up the area at the bottom for the edit controls.
 
 		Composite editAreaControl = new Composite(topLevelControl, SWT.NULL);
 		
 		GridLayout editAreaLayout = new GridLayout();
 		editAreaLayout.numColumns = 7;
 		editAreaControl.setLayout(editAreaLayout);
 		
 		periodLabel = new Label(editAreaControl, 0);
 		periodBox = new Combo(editAreaControl, 0);
 		fromLabel = new Label(editAreaControl, 0);
 		fromField = new DateControl(editAreaControl);
 		toLabel = new Label(editAreaControl, 0);
 		toField = new DateControl(editAreaControl);
 		generateButton = new Button(editAreaControl, 0);
 		accountField = new AccountControl<IncomeExpenseAccount>(editAreaControl, JMoneyPlugin.getDefault().getSession(), IncomeExpenseAccount.class);
 			
 		periodLabel.setText(
 				Activator.getResourceString("Report.IncomeExpense.Period"));
 		for (DateRanges dateRange: DateRanges.values()) {
 			periodBox.add(dateRange.getText());
 		}
 		periodBox.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				updateFromAndTo();
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				updateFromAndTo();
 			}
 		});
 		fromLabel.setText(
 				Activator.getResourceString("Report.IncomeExpense.From"));
 		toLabel.setText(Activator.getResourceString("Report.IncomeExpense.To"));
 		generateButton.setText(Activator.getResourceString("Report.Generate"));
 		generateButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				generateReport();
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				generateReport();
 			}
 		});
 		
 		return topLevelControl;
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.jmoney.IBookkeepingPageListener#createPages(java.lang.Object, org.eclipse.swt.widgets.Composite)
 	 */
 	public IBookkeepingPage createFormPage(NodeEditor editor, IMemento memento) {
 		SectionlessPage formPage = new SectionlessPage(
 				editor,
 				PAGE_ID, 
 				"Income & Expense Report", 
 				"Income & Expense Report") {
 
 			/**
 			 * @param nodeObject this page is displayed only for node objects
 			 * 			that are IncomeExpenseAccount instances.  Therefore
 			 * 			<code>nodeObject</code> will always be an IncomeExpenseAccount instance.
 			 */
 			@Override
 			public Composite createControl(Object nodeObject, Composite parent, FormToolkit toolkit, IMemento memento) {
 				Composite control = createContent(/*(IncomeExpenseAccount)nodeObject, */ parent);
 
 				VerySimpleDateFormat dateFormat = new VerySimpleDateFormat(JMoneyPlugin.getDefault().getDateFormat());
 
 				// If a memento is passed, restore the field contents
 				if (memento != null) {
 					Integer periodIndex = memento.getInteger("period");
 					if (periodIndex != null && periodIndex >= 0 && periodIndex < DateRanges.values().length) {
 						
 						DateRanges period = DateRanges.values()[periodIndex];
 						periodBox.select(periodIndex);
 						if (period == DateRanges.CUSTOM) {
 							String fromDateString = memento.getString("fromDate");
 							if (fromDateString != null)	{
 								fromDate = dateFormat.parse(fromDateString);
 							}
 							String toDateString = memento.getString("toDate");
 							if (toDateString != null)	{
 								toDate = dateFormat.parse(toDateString);
 							}
 						}
 						
 						updateFromAndTo();
 					}
 					
 					// boolean subtotals = new Boolean(memento.getString("subtotals")).booleanValue();
 				}
 				
 				return control;
 			}
 
 			public void saveState(IMemento memento) {
 				VerySimpleDateFormat dateFormat = new VerySimpleDateFormat(JMoneyPlugin.getDefault().getDateFormat());
 				int periodIndex = periodBox.getSelectionIndex();
 				if (periodIndex != -1) {
 					memento.putInteger("period", periodIndex);
 					if (DateRanges.values()[periodIndex] == DateRanges.CUSTOM) {
 						memento.putString("fromDate", dateFormat.format(fromField.getDate()));
 						memento.putString("toDate", dateFormat.format(toField.getDate()));
 					}
 				}
				memento.putString(
						"account",
						accountField.getAccount().getFullAccountName());
 			}
 		};
 
 		try {
 			editor.addPage(formPage);
 		} catch (PartInitException e) {
 			JMoneyPlugin.log(e);
 			// TODO: cleanly leave out this page.
 		}
 		
 		return formPage;
 	}
 
 	void updateFromAndTo() {
 		int index = periodBox.getSelectionIndex();
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 
 		DateRanges period = DateRanges.values()[index]; 
 		switch (period) {
 			case THIS_MONTH :
 				cal.set(Calendar.DAY_OF_MONTH, 1);
 				fromDate = cal.getTime();
 
 				cal.add(Calendar.MONTH, 1);
 				cal.add(Calendar.MILLISECOND, -1);
 				toDate = cal.getTime();
 				break;
 			case THIS_YEAR :
 				cal.set(Calendar.DAY_OF_MONTH, 1);
 				cal.set(Calendar.MONTH, Calendar.JANUARY);
 				fromDate = cal.getTime();
 
 				cal.add(Calendar.YEAR, 1);
 				cal.add(Calendar.MILLISECOND, -1);
 				toDate = cal.getTime();
 				break;
 			case LAST_MONTH :
 				cal.set(Calendar.DAY_OF_MONTH, 1);
 				cal.add(Calendar.MONTH, -1);
 				fromDate = cal.getTime();
 
 				cal.add(Calendar.MONTH, 1);
 				cal.add(Calendar.MILLISECOND, -1);
 				toDate = cal.getTime();
 				break;
 			case LAST_YEAR :
 				cal.set(Calendar.DAY_OF_MONTH, 1);
 				cal.set(Calendar.MONTH, Calendar.JANUARY);
 				cal.add(Calendar.YEAR, -1);
 				fromDate = cal.getTime();
 
 				cal.add(Calendar.YEAR, 1);
 				cal.add(Calendar.MILLISECOND, -1);
 				toDate = cal.getTime();
 				break;
 			case CUSTOM :
 			default :
 				}
 
 		fromField.setDate(fromDate);
 		fromField.setEnabled(period == DateRanges.CUSTOM);
 		toField.setDate(toDate);
 		toField.setEnabled(period == DateRanges.CUSTOM);
 	}
 
 	void generateReport() { 
 		EngineConfig config = new EngineConfig();
 //		Create the report engine
 		IReportEngineFactory factory = 
 			(IReportEngineFactory) Platform.createFactoryObject( 
 					IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
 		IReportEngine engine = factory.createReportEngine(config);
 		IReportRunnable design = null;
 
 		try {
 			Bundle bundle = org.eclipse.core.runtime.Platform.getBundle(Activator.PLUGIN_ID);
 
 			URL url = bundle.getEntry("reports/IncomeAndExpenses.rptdesign");
 
 			String reportFilename = FileLocator.toFileURL(url).getPath();
 			
 			FileInputStream fs = new FileInputStream(reportFilename);
 			design = engine.openReportDesign(fs);
 			
 			IRunAndRenderTask task = engine.createRunAndRenderTask(design);
 
 			IRenderOption options;
 			options = new HTMLRenderOption( );
 			ByteArrayOutputStream bos = new ByteArrayOutputStream();
 
 			options.setOutputStream(bos);
 			options.setOutputFormat("html");
 
 			task.setRenderOption(options);
 
 			// Set the values of the parameters.
 			Account account = accountField.getAccount();
 			if (account == null) {
 				MessageDialog.openError(shell, "Invalid Data", "You must select an account.");
 				return;
 			}
 
 			task.setParameterValue("AccountName", account.getName());
 			task.setParameterValue( "StartDate", new java.sql.Date(fromDate.getTime()));
 			task.setParameterValue( "EndDate", new java.sql.Date(toDate.getTime()));
 
 //			run the report and destroy the engine
 
 			task.run();
 			task.close();
 
 //			set Browser text accordingly
 			viewer.setText(bos.toString());
 			engine.destroy();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
