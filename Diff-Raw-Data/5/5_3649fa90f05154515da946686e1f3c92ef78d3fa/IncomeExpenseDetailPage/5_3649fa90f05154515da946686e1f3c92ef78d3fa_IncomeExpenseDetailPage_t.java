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
 
 package net.sf.jmoney.reports;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 
 import net.sf.jasperreports.engine.JRDataSource;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
 import net.sf.jmoney.IBookkeepingPage;
 import net.sf.jmoney.IBookkeepingPageFactory;
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.VerySimpleDateFormat;
 import net.sf.jmoney.fields.DateControl;
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.Commodity;
 import net.sf.jmoney.model2.Currency;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.IncomeExpenseAccount;
 import net.sf.jmoney.views.NodeEditor;
 import net.sf.jmoney.views.SectionlessPage;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
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
 
 import com.jasperassistant.designer.viewer.ViewerComposite;
 
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
     
 	public static final int THIS_MONTH = 0;
 
 	public static final int THIS_YEAR = 1;
 
 	public static final int LAST_MONTH = 2;
 
 	public static final int LAST_YEAR = 3;
 
 	public static final int CUSTOM = 4;
 
 	public static final String[] periods =
 		{
 			ReportsPlugin.getResourceString("Panel.Report.IncomeExpense.thisMonth"),
 			ReportsPlugin.getResourceString("Panel.Report.IncomeExpense.thisYear"),
 			ReportsPlugin.getResourceString("Panel.Report.IncomeExpense.lastMonth"),
 			ReportsPlugin.getResourceString("Panel.Report.IncomeExpense.lastYear"),
 			ReportsPlugin.getResourceString("Panel.Report.IncomeExpense.custom")
 		};
 
 	private ViewerComposite viewer;
 
 	private Label periodLabel;
 	private Combo periodBox;
 	private Label fromLabel;
 	private DateControl fromField;
 	private Label toLabel;
 	private DateControl toField;
 	private Button subtotalsCheckBox;
 	private Button generateButton;
 
 	/**
 	 * The Shell to use for all message dialogs.
 	 */
 	Shell shell;
 	
 	private Date fromDate;
 	private Date toDate;
 
 	private IncomeExpenseAccount account = null;
 
 	/* (non-Javadoc)
 	 * @see net.sf.jmoney.IBookkeepingPageListener#createPages(java.lang.Object, org.eclipse.swt.widgets.Composite)
 	 */
 	private Composite createContent(IncomeExpenseAccount account, Composite parent) {
 		this.account = account;
 		
 		/**
 		 * topLevelControl is a control with grid layout, 
 		 * onto which all sub-controls should be placed.
 		 */
 		Composite topLevelControl = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		topLevelControl.setLayout(layout);
 		
 		shell = topLevelControl.getShell(); 
 
 		viewer = new ViewerComposite(topLevelControl, SWT.NONE);
 		
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
 		subtotalsCheckBox = new Button(editAreaControl, SWT.CHECK);
 			
 		periodLabel.setText(
 				ReportsPlugin.getResourceString("Panel.Report.IncomeExpense.Period"));
 		for (int i = 0; i < periods.length; i++) {
 			periodBox.add(periods[i]);
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
 				ReportsPlugin.getResourceString("Panel.Report.IncomeExpense.From"));
 		toLabel.setText(ReportsPlugin.getResourceString("Panel.Report.IncomeExpense.To"));
 		generateButton.setText(ReportsPlugin.getResourceString("Panel.Report.Generate"));
 		generateButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				generateReport();
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				generateReport();
 			}
 		});
 		
 		subtotalsCheckBox.setText(
 				ReportsPlugin.getResourceString("Panel.Report.IncomeExpense.ShowSubtotals"));
 		
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
 			public Composite createControl(Object nodeObject, Composite parent, FormToolkit toolkit, IMemento memento) {
 				Composite control = createContent((IncomeExpenseAccount)nodeObject, parent);
 
 				VerySimpleDateFormat dateFormat = new VerySimpleDateFormat(JMoneyPlugin.getDefault().getDateFormat());
 
 				// If a memento is passed, restore the field contents
 				if (memento != null) {
 					Integer periodType = memento.getInteger("period");
 					if (periodType != null) {
 						int periodIndex = periodType.intValue();
 						periodBox.select(periodIndex);
 						if (periodIndex == CUSTOM) {
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
				// TODO: This code might cause an exception.  If the editor was opened
				// but the user never switched to this page in
				// the multi-page editor then the controls will never
				// have been initialized.  When the editor is closed, this
				// method throws an exception.
 				VerySimpleDateFormat dateFormat = new VerySimpleDateFormat(JMoneyPlugin.getDefault().getDateFormat());
 				int period = periodBox.getSelectionIndex();
 				if (period != -1) {
 					memento.putInteger("period", period);
 					if (period == CUSTOM) {
 						memento.putString("fromDate", dateFormat.format(fromField.getDate()));
 						memento.putString("toDate", dateFormat.format(toField.getDate()));
 					}
 				}
 				memento.putString(
 						"subtotals",
 						String.valueOf(subtotalsCheckBox.getSelection()));
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
 
 	private void updateFromAndTo() {
 		int index = periodBox.getSelectionIndex();
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 
 		switch (index) {
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
 		fromField.setEnabled(index == CUSTOM);
 		toField.setDate(toDate);
 		toField.setEnabled(index == CUSTOM);
 	}
 
 	private void generateReport() {
 		VerySimpleDateFormat dateFormat = new VerySimpleDateFormat(JMoneyPlugin.getDefault().getDateFormat());
 		
 		fromDate = fromField.getDate();
 		toDate = toField.getDate();
 		
 		try {
 			String reportFile =
 				subtotalsCheckBox.getSelection()
 					? "resources/ItemizedIncomeExpense.jasper"
 					: "resources/ItemizedIncomeExpense.jasper";
 			URL url = ReportsPlugin.class.getResource(reportFile);
 			if (url == null) {
 				Object [] messageArgs = new Object[] {
 						reportFile
 				};
 				
 				String errorText = new java.text.MessageFormat(
 						"{0} not found.  A manual build of the net.sf.jmoney.reports project may be necessary.", 
 								java.util.Locale.US)
 								.format(messageArgs);
 				
 				JMoneyPlugin.log(new Status(IStatus.ERROR, ReportsPlugin.PLUGIN_ID, IStatus.ERROR, errorText, null));
 	            MessageDialog errorDialog = new MessageDialog(
 	                    shell,
 	                    "JMoney Build Error",
 	                    null, // accept the default window icon
 	                    errorText,
 	                    MessageDialog.ERROR,
 	                    new String[] { IDialogConstants.OK_LABEL }, 0);
 	            errorDialog.open();
 				return;
 			}
 			InputStream is = url.openStream();
 
 			Map<String, String> params = new HashMap<String, String>();
 			params.put(
 				"Title",
 				ReportsPlugin.getResourceString("Report.IncomeExpense.Title"));
 			params.put(
 				"Subtitle",
 				dateFormat.format(fromDate)
 					+ " - "
 					+ dateFormat.format(toDate));
 			params.put("Total", ReportsPlugin.getResourceString("Report.Total"));
 			params.put("Category", ReportsPlugin.getResourceString("Entry.category"));
 			params.put(
 				"Income",
 				ReportsPlugin.getResourceString("Report.IncomeExpense.Income"));
 			params.put(
 				"Expense",
 				ReportsPlugin.getResourceString("Report.IncomeExpense.Expense"));
 			params.put("DateToday", dateFormat.format(new Date()));
 			params.put("Page", ReportsPlugin.getResourceString("Report.Page"));
 
 			Collection items = getItems();
 			if (items.isEmpty()) {
 				MessageDialog dialog =
 					new MessageDialog(
 							shell,
 							ReportsPlugin.getResourceString("Panel.Report.EmptyReport.Title"), 
 							null, // accept the default window icon
 							ReportsPlugin.getResourceString("Panel.Report.EmptyReport.Message"), 
 							MessageDialog.ERROR, 
 							new String[] { IDialogConstants.OK_LABEL }, 0);
 				dialog.open();
 			} else {
 				JRDataSource ds = new JRBeanCollectionDataSource(items);
 				JasperPrint print =
 					JasperFillManager.fillReport(is, params, ds);
 				viewer.getReportViewer().setDocument(print);
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	private Collection getItems() {
         Vector<Item> allItems = new Vector<Item>();
 
         for (Iterator eIt = account.getEntries().iterator(); eIt.hasNext();) {
             Entry e = (Entry) eIt.next();
             if (accept(e)) {
                 Item i = new Item(account, e);
                 allItems.add(i);
             }
         }
 
         Collections.sort(allItems);
         return allItems;
     }
 
 	private boolean accept(Entry e) {
 		return acceptFrom(e.getTransaction().getDate()) && acceptTo(e.getTransaction().getDate());
 	}
 
 	private boolean acceptFrom(Date d) {
 		if (fromDate == null)
 			return true;
 		return (d.after(fromDate) || d.equals(fromDate));
 	}
 
 	private boolean acceptTo(Date d) {
 		if (toDate == null)
 			return true;
 		return (d.before(toDate) || d.equals(toDate));
 	}
 
 	public class Item implements Comparable {
 
 		private Entry entry;
 
 		public Item(Account aCategory, Entry entry) {
 			this.entry = entry;
 		}
 
 		/**
 		 * Get the string that is used to specify the currency in
 		 * the report.
 		 */
 		// TODO: rename this method
 		public String getCurrencyCode() {
 			
 			return ((Currency)entry.getCommodity()).getCode();
 		}
 
 		public String getBaseCategory() {
 			if (entry.getAccount() == null)
 				return ReportsPlugin.getResourceString("Report.IncomeExpense.NoCategory");
                         Account baseCategory = entry.getAccount();
                         while (baseCategory.getParent() != null) {
                             baseCategory = baseCategory.getParent();
                         }
 			return baseCategory.getName();
 		}
 
 		public String getCategory() {
 			return entry.getAccount() == null
 				? ReportsPlugin.getResourceString("Report.IncomeExpense.NoCategory")
 				: entry.getAccount().getFullAccountName();
 		}
 
 		public Long getIncome() {
 			return new Long(entry.getAmount());
 		}
 
 		public String getIncomeString() {
                     return formatAmount(entry.getCommodity(), getIncome());
 		}
 
 		public Long getExpense() {
 			return entry.getAmount() < 0 ? new Long(-entry.getAmount()) : null;
 		}
 
 		public String getExpenseString() {
 			return formatAmount(entry.getCommodity(), getExpense());
 		}
 		
 		public String getDescription() {
 			return entry.getMemo();
 		}
 		
 		private String formatAmount(Commodity commodity, Long amount) {
 			if (amount == null) {
 				return "";
 			} else {
 				return commodity.format(amount.longValue());
 			}
 		}
 		
 		public boolean noCategory() {
 			return entry.getAccount() == null;
 		}
 
 		public int compareTo(Object o) {
 			Item other = (Item) o;
 			if (noCategory() && other.noCategory())
 				return 0;
 			else if (noCategory())
 				return 1;
 			else if (other.noCategory())
 				return -1;
 			else
 				return getCategory().compareTo(other.getCategory());
 		}
 	}
 }
