 package net.sf.jmoney.search;
 
 import java.util.Date;
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.fields.DateControl;
 import net.sf.jmoney.model2.Currency;
 import net.sf.jmoney.search.views.EntrySearch;
 import net.sf.jmoney.search.views.SearchException;
 import net.sf.jmoney.search.views.SearchView;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 public class EntrySearchDialog extends Dialog {
 
 	private IWorkbenchPage workbenchPage;
 	
 	private DateControl startDateControl;
 	private DateControl endDateControl;
 	private Text amountControl;
 	private Text memoControl;
 	
 	protected EntrySearchDialog(Shell parentShell, IWorkbenchPage workbenchPage) {
 		super(parentShell);
 		this.workbenchPage = workbenchPage;
 	}
 
 	@Override
 	protected void buttonPressed(int buttonId) {
 		if (buttonId == IDialogConstants.OK_ID) {
 			/**
 			 * The start date, or null if no start date
 			 */
 			Date startDate = startDateControl.getDate();
 
 			/**
 			 * The end date, or null if no start date
 			 */
 			Date endDate = endDateControl.getDate();
 			
 			/**
 			 * The amount of the entry, or null if no amount restriction
 			 */
 			Long amount;
 	        String amountString = amountControl.getText();
 	        if (amountString.length() == 0) {
 	            amount = null;
 	        } else {
 	        	// TODO: Have a drop down with the currency on which
 	        	// we are searching.
 	        	Currency currency = JMoneyPlugin.getDefault().getSession().getDefaultCurrency(); 
 	            amount = currency.parse(amountString);
 	        }
 
 	    	/**
 	    	 * Text that must appear in the memo, or null if no matching on the memo
 	    	 */
 			String memo = memoControl.getText().isEmpty()
 				? null 
 				: memoControl.getText();
 			
 			IEntrySearch search = new EntrySearch(startDate, endDate, amount, memo);
 
 			try {
 				search.executeSearch();
 				SearchView view = (SearchView)workbenchPage.showView(SearchView.ID, null, IWorkbenchPage.VIEW_ACTIVATE);
 				view.showSearch(search);
 			} catch (PartInitException e) {
 				throw new RuntimeException(e);
 			} catch (SearchException e) {
 				throw new RuntimeException(e);
 			}
 		}

		super.buttonPressed(buttonId);
 	}
 
 	@Override
 	protected void configureShell(Shell shell) {
 		super.configureShell(shell);
 		shell.setText("Search Entries");
 	}
 
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		// create OK and Cancel buttons by default
 		createButton(parent, IDialogConstants.OK_ID,
 				IDialogConstants.OK_LABEL, true);
 		createButton(parent, IDialogConstants.CANCEL_ID,
 				IDialogConstants.CANCEL_LABEL, false);
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		Composite composite = (Composite) super.createDialogArea(parent);
 
 		Label label = new Label(composite, SWT.WRAP);
 		label.setText("Enter the values for which to search entries.  Leave the field blank if you want no restriction on that field.");
 
 		GridData messageData = new GridData();
 		Rectangle rect = getShell().getMonitor().getClientArea();
 		messageData.widthHint = rect.width/4;
 		label.setLayoutData(messageData);
 
 		Composite fieldArea = new Composite(composite, SWT.NONE);
 		fieldArea.setLayout(new GridLayout(2, false));
 		
 		new Label(fieldArea, SWT.NONE).setText("Start Date:");
 		startDateControl = new DateControl(fieldArea);
 		startDateControl.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				boolean isDateValid = (startDateControl.getDate() != null);
 				setErrorMessage(isDateValid ? null : "Date must be in the format " + JMoneyPlugin.getDefault().getDateFormat());
 			}
 		});
 
 		new Label(fieldArea, SWT.NONE).setText("End Date:");
 		endDateControl = new DateControl(fieldArea);
 		endDateControl.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				boolean isDateValid = (endDateControl.getDate() != null);
 				setErrorMessage(isDateValid ? null : "Date must be in the format " + JMoneyPlugin.getDefault().getDateFormat());
 			}
 		});
 
 		new Label(fieldArea, SWT.NONE).setText("Amount:");
     	amountControl = new Text(fieldArea, SWT.TRAIL);
 
 		new Label(fieldArea, SWT.NONE).setText("Memo:");
     	memoControl = new Text(fieldArea, SWT.NONE);
     	memoControl.setLayoutData(new GridData(200, SWT.DEFAULT));
 
 		applyDialogFont(composite);
 		return composite;
 	}
 
 	protected void setErrorMessage(String string) {
 		// TODO:
 		
 	}
 }
