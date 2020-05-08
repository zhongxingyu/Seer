 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2002 Johann Gyger <johann.gyger@switzerland.org>
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
 
 package net.sf.jmoney.pages.entries;
 
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.Vector;
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.fields.AccountInfo;
 import net.sf.jmoney.fields.CommodityInfo;
 import net.sf.jmoney.fields.EntryInfo;
 import net.sf.jmoney.isolation.TransactionManager;
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.CapitalAccount;
 import net.sf.jmoney.model2.Commodity;
 import net.sf.jmoney.model2.Currency;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.IPropertyControl;
 import net.sf.jmoney.model2.IncomeExpenseAccount;
 import net.sf.jmoney.model2.PropertyAccessor;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.model2.SessionChangeAdapter;
 import net.sf.jmoney.model2.Transaction;
 
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.TreeEditor;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.TraverseEvent;
 import org.eclipse.swt.events.TraverseListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeColumn;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 /**
  * Class that displays a list of entries in a tree. The tree contains one top
  * level row for each entry in the account. The child rows show the other
  * entries in the transaction.
  * <P>
  * It is important to understand how object properties are mapped to cells in
  * the table. There is one top-level row in the tree control for each entry in
  * the list. In general, the other entries in the transaction are child rows.
  * Note that it is possible that one transaction may contain two or more entries
  * in the same account. For example, a user may write a check for a deposit and
  * a check for the balance of a purchase. The user may enter this as a single
  * 'split' transaction containing the two checks and a single entry containing
  * the total purchase amount in an expense category. In this situation, the
  * single transaction will have two top-level rows (one for each check).
  * Expanding one of the rows with show two child entries: the other check and
  * the purchase entry.
  * <P>
  * A simple transaction is a transaction that contains two entries where one
  * entry is in a capital account and the other entry is in an income and expense
  * account. This is a common form of transaction and therefore we make a special
  * effort to display such transactions on a single row.
  * <P>
  * Some of the entry properties apply only to entries in capital accounts, some
  * apply only to entries in income and expense accounts, and some apply to
  * entries in both types of accounts. In a simple transaction, all the
  * properties from both entries have a column. Properties from the transaction
  * are displayed in the top level and also have a column. So, we have a column
  * for all the following:
  * <UL>
  * <LI>Every property in the transaction</LI>
  * <LI>Every property in an entry that may be applicable given the account
  * being listed, with the exception that if all the entries are in the same
  * account (true in most uses of this class) then no column exists for the
  * account property (as such a column would contain the same account in every
  * row and therefore not be of much use)</LI>
  * <LI>Every property in an entry that may be applicable when the entry is in
  * an income and expense account</LI>
  * </UL>
  * 
  * Some properties may be applicable for both entries in the capital account and
  * for entries in income and expense accounts. There will be two columns for
  * such properties. When an entry is being show on its own child row, we have
  * the choice of which of the two columns we use for the property. We chose to
  * show it in the column that would, for a simple transaction, be used to show
  * the property of the entry in the income and expense account. This makes the
  * child rows look more similar and also ensures that a transfer account is
  * shown.
  * <P>
  * Each column is managed by an IEntriesTableProperty object. Each row is
  * managed by an IDisplayableItem object. (Both classes of object are set as the
  * data on the TreeColumn and TreeItem items respectively). These two classes
  * must work together to determine the contents of a cell.
  * <P>
  * The credit, debit, and balance columns are special cases and special
  * implementations of IEntriesTableProperty handle those three columns. The
  * other columns fall into one of the above three categories. The rest of this
  * explanation applies only to the latter class of columns.
  * <P>
  * A request for cell contents (whether for displaying text or for creating a
  * cell editor) goes first to the IEntriesTableProperty object. That object then
  * calls one of the following three methods on the IDisplayableItem row,
  * depending on which class of column it is:
  * <UL>
  * <LI>getTransactionForTransactionFields</LI>
  * <LI>getEntryForAccountFields</LI>
  * <LI>getEntryForOtherFields</LI>
  * </UL>
  * The above three methods all return the object that contains the value
  * associated with the cell (or null if no property value is associated with
  * that cell). As the IEntriesTableProperty object has the property accessor,
  * the property value associated with the cell can be got and set.
  */
 public class EntriesTree extends Composite {
 
 	// The darker purple and green lines for the listed entry in each transaction
 	protected static final Color transactionColor = new Color(Display
 			.getCurrent(), 235, 235, 255);
 
 	protected static final Color alternateTransactionColor = new Color(Display
 			.getCurrent(), 235, 255, 237);
 
 	// The lighter colors for the sub-entry lines
 	protected static final Color entryColor = new Color(Display.getCurrent(),
 			245, 245, 255);
 
 	protected static final Color alternateEntryColor = new Color(Display
 			.getCurrent(), 245, 255, 255);
 
 	protected IEntriesContent entriesContent;
 
 	protected TransactionManager transactionManager;
 	
 	protected Tree fTable;
 
 	private TreeEditor editor;
 	
 	/**
 	 * List of entries to show in the table. Only the top level entries are
 	 * included, the other entries in the transactions, which are shown as child
 	 * items, are not in this list. The elements are not sorted.
 	 * <P>
 	 * Element: DisplayableTransaction
 	 */
 	Vector entries;
 
 	/**
 	 * The column on which the items are currently sorted
 	 */
 	protected TreeColumn sortColumn;
 
 	/**
 	 * true if items are sorted in an ascending order;
 	 * false if items are sorted in a descending order
 	 */
 	protected boolean sortAscending;
 
 	protected IPropertyControl currentCellPropertyControl = null;
 
 	private Entry newEntryRowEntry;
 
 	/**
 	 * The item that was selected before an mouse down or selection
 	 * change event occurs.  We need to know the previously selected
 	 * item for two purposes:
 	 * <OL>
 	 * <LI>Clicking on a cell will create a cell editor only if
 	 *     the user clicked in a row that was already the selected
 	 *     row</LI>
 	 * <LI>When moving the row selection to a row for a different
 	 *     transaction, the transaction for the previously selected
 	 *     row must be validated and committed.</LI>
 	 * </OL>    
 	 */
 	private TreeItem previouslySelectedItem = null;
 
 	/**
 	 * Set of listeners for selection changes
 	 */
 	private Vector selectionListeners = new Vector();
 
 	/**
 	 * a kludgy field used to indicate that the next selection
 	 * event should be ignored.  i.e. the event should not be
 	 * passed on to our listeners.
 	 */
 	protected boolean ignoreSelectionEvent = false;
 
 	public EntriesTree(Composite parent, FormToolkit toolkit,
 			final TransactionManager transactionManager, final IEntriesContent entriesContent, final Session session) {
 		super(parent, SWT.NONE);
 		
 		toolkit.adapt(this, true, false);
 
 		GridLayout layout = new GridLayout(1, false);
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		setLayout(layout);
 		
 		this.transactionManager = transactionManager;
 		this.entriesContent = entriesContent;
 
 		fTable = new Tree(this, SWT.FULL_SELECTION | SWT.BORDER
 				| SWT.H_SCROLL | SWT.SINGLE);
 		
 		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		gridData.heightHint = 100;
 		gridData.widthHint = 100;
 		fTable.setLayoutData(gridData);
 
         // Create the button area
 		Composite buttonArea = toolkit.createComposite(this);
 		
 		// Note that the buttons touch each other and also touch
 		// the table above.  This makes it clearer that the buttons
 		// are tightly associated with the table.
 		RowLayout layoutOfButtons = new RowLayout();
 		layoutOfButtons.fill = false;
 		layoutOfButtons.justify = true;
 		layoutOfButtons.marginTop = 0;
 		layoutOfButtons.spacing = 5;
 		buttonArea.setLayout(layoutOfButtons);
 		
         // Create the 'add transaction' button.
         Button addButton = toolkit.createButton(buttonArea, "New Transaction", SWT.PUSH);
         addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
 				// Clean up any cell editor
 				closeCellEditor();
 
 				if (!checkAndCommitTransaction(null)) {
 					return;
 				}
            		
            		Transaction transaction = session.createTransaction();
            		Entry entry1 = transaction.createEntry();
            		Entry entry2 = transaction.createEntry();
            		entriesContent.setNewEntryProperties(entry1);
            		
            		/*
 				 * We set the currency by default to be the currency of the
 				 * top-level entry.
 				 * 
 				 * The currency of an entry is not applicable if the entry is an
 				 * entry in a currency account or an income and expense account
 				 * that is restricted to a single currency.
 				 * However, we set it anyway so the value is there if the entry
 				 * is set to an account which allows entries in multiple currencies.
 				 * 
 				 * It may be that the currency of the top-level entry is not
 				 * known. This is not possible if entries in a currency account
 				 * are being listed, but may be possible if this entries list
 				 * control is used for more general purposes. In this case, the
 				 * currency is not set and so the user must enter it.
 				 */
     			if (entry1.getCommodity() instanceof Currency) {
         			entry2.setIncomeExpenseCurrency((Currency)entry1.getCommodity());
     			}
     			
            		// Select entry1 in the entries list.
                 setSelection(entry1, entry1);
            }
         });
 
         // Create the 'duplicate transaction' button.
         Button duplicateButton = toolkit.createButton(buttonArea, "Duplicate Transaction", SWT.PUSH);
         duplicateButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
 				// Clean up any cell editor
 				closeCellEditor();
 
 				if (!checkAndCommitTransaction(null)) {
 					return;
 				}
            		
         		Entry selectedEntry = getSelectedEntryInAccount();
         		Entry newSelectedEntry = null;
         		
         		if (selectedEntry != null) {
         			
     				TransactionManager transactionManager = new TransactionManager(session);
     				Session sessionInTrans = transactionManager.getSession();
     				
         			Transaction sourceTransaction = selectedEntry.getTransaction();
         			Transaction newTransaction = sessionInTrans.createTransaction();
         			
         			for (Iterator iter = sourceTransaction.getEntryCollection().iterator(); iter.hasNext(); ) {
         				Entry sourceEntry = (Entry)iter.next();
         				
         				Entry newEntry = newTransaction.createEntry();
         				newEntry.setAccount((Account)transactionManager.getCopyInTransaction(sourceEntry.getAccount()));
         				newEntry.setDescription(sourceEntry.getDescription());
         				newEntry.setMemo(sourceEntry.getMemo());
         				if (sourceEntry.getIncomeExpenseCurrency() != null) {
         					newEntry.setIncomeExpenseCurrency((Currency)transactionManager.getCopyInTransaction(sourceEntry.getIncomeExpenseCurrency()));
         				}
         				newEntry.setAmount(sourceEntry.getAmount());
         				
         				if (sourceEntry.equals(selectedEntry)) {
         					newSelectedEntry = newEntry;
         				}
         			}
     			
         			transactionManager.commit();
         			
         			// Select newSelectedEntry in the entries list.
 //        			setSelection(newSelectedEntry, newSelectedEntry);
         		}
            }
         });
 
         // Create the 'delete transaction' button.
         Button deleteButton = toolkit.createButton(buttonArea, "Delete Transaction", SWT.PUSH);
         deleteButton.addSelectionListener(new SelectionAdapter() {
         	public void widgetSelected(SelectionEvent event) {
 				// Clean up any previous editor control
 				closeCellEditor();
 
         		Entry selectedEntry = getSelectedEntry();
         		if (selectedEntry != null) {
         			// Clear the selection.  This should not be necessary but.....
         			fTable.setSelection(new TreeItem [] {});
         			
         			Transaction transaction = selectedEntry.getTransaction();
         			transaction.getSession().deleteTransaction(transaction);
         			transaction.getSession().registerUndoableChange("Delete Transaction");
         			transactionManager.commit();
         		}
         	}
         });
         
         // Create the 'add split' button.
         Button addEntryButton = toolkit.createButton(buttonArea, "New Split", SWT.PUSH);
         addEntryButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
 				// Clean up any cell editor
 				closeCellEditor();
 
         		Entry selectedEntry = getSelectedEntryInAccount();
         		if (selectedEntry != null) {
         			closeCellEditor();
         			
         			Transaction transaction = selectedEntry.getTransaction();
         			Entry newEntry = transaction.createEntry();
         			
         			long total = 0;
         			Commodity commodity = null;
         			for (Iterator iter = transaction.getEntryCollection().iterator(); iter.hasNext(); ) {
         				Entry entry = (Entry)iter.next();
         				
         				if (entry.getCommodity() != null) {
             				if (commodity == null) {
             					commodity = entry.getCommodity();
             				} else if (!commodity.equals(entry.getCommodity())) {
             					// We have entries with mismatching commodities set.
             					// This means there is an exchange of one commodity
             					// for another so we do not expect the total amount
             					// of all the entries to be zero.  Leave the amount
             					// for this new entry blank (a zero amount).
             					total = 0;
             					break;
             				}
         				}
 
         				total += entry.getAmount();
         			}
         			
         			newEntry.setAmount(-total);
         			
         			// We set the currency by default to be the currency
         			// of the top-level entry.
         			
         			// The currency of an entry is not
         			// applicable if the entry is an entry in a currency account
         			// (because all entries in a currency account must have the
         			// currency of the account).  However, we set it anyway so
         			// the value is there if the entry is set to an income and
         			// expense account (which would cause the currency property
         			// to become applicable).
 
     				// It may be that the currency of the top-level entry is not known.
     				// This is not possible if entries in a currency account
     				// are being listed, but may be possible if this entries list
     				// control is used for more general purposes.  In this case,
         			// the currency is not set and so the user must enter it.
         			if (selectedEntry.getCommodity() instanceof Currency) {
             			newEntry.setIncomeExpenseCurrency((Currency)selectedEntry.getCommodity());
         			}
         			
         			transaction.getSession().registerUndoableChange("New Split");
         			
                		// Select the new entry in the entries list.
                     setSelection(selectedEntry, newEntry);
         		} else {
         			showMessage("You cannot add a new split to an entry until you have selected an entry from the above list.");
         		}
            }
         });
 
         // Create the 'delete split' button.
         Button deleteSplitButton = toolkit.createButton(buttonArea, "Delete Split", SWT.PUSH);
         deleteSplitButton.addSelectionListener(new SelectionAdapter() {
         	public void widgetSelected(SelectionEvent event) {
 				// Clean up any cell editor
 				closeCellEditor();
 
 				if (fTable.getSelectionCount() != 1) {
 					showMessage("No split entry is selected in the entries list");
 					return;
 				}
 
 				IDisplayableItem data = (IDisplayableItem)fTable.getSelection()[0].getData();
 				
 				if (!(data instanceof DisplayableEntry)) {
 					showMessage("No split entry is selected in the entries list");
 					return;
 				}
 
 				DisplayableTransaction dTrans = ((DisplayableEntry)data).transactionData;
 				
 				if (dTrans.otherEntries.size() == 1) {
 					showMessage("You cannot delete the transfer account entry in a double entry transaction.  If you do not intend this transaction to be a transfer then change the category as appropriate.");
 					return;
 				}
 
         		Entry selectedEntryInAccount = getSelectedEntryInAccount();
         		Entry selectedEntry = getSelectedEntry();
         		if (selectedEntry != null 
         				&& selectedEntry != selectedEntryInAccount) {
 
         			// Set the previouslySelectedItem to be the item of the top-level entry,
     				// as this item will no longer be valid after we dispose it.
     				previouslySelectedItem = previouslySelectedItem.getParentItem();
     				fTable.setSelection(new TreeItem [] { previouslySelectedItem });
     				
         			Transaction transaction = selectedEntry.getTransaction();
         			transaction.deleteEntry(selectedEntry);
         			transaction.getSession().registerUndoableChange("Delete Split");
         		}
         	}
         });
         
         // Create the 'details' button.
         Button detailsButton = toolkit.createButton(buttonArea, "Details", SWT.PUSH);
         detailsButton.addSelectionListener(new SelectionAdapter() {
         	public void widgetSelected(SelectionEvent event) {
 				// Clean up any cell editor
 				closeCellEditor();
 
         		Entry selectedEntryInAccount = getSelectedEntryInAccount();
         		if (selectedEntryInAccount != null) {
 				TransactionDialog dialog = new TransactionDialog(
 						getShell(),
 						selectedEntryInAccount,
 						session);
 				dialog.open();
         		} else {
         			// No entry selected.
         		}
         	}
         });
         
 		TableLayout tlayout = new TableLayout();
 
 		int index = 0;
 		for (Iterator iter = entriesContent.getAllEntryDataObjects().iterator(); iter
 				.hasNext();) {
 			IEntriesTableProperty entriesSectionProperty = (IEntriesTableProperty) iter
 					.next();
 			// By default, do not include the column for the currency
 			// of the entry in the category (which applies only when
 			// the category is a multi-currency income/expense category)
 			// and the column for the amount (which applies only when
 			// the currency is different from the entry in the capital 
 			// account)
 			if (!entriesSectionProperty.getId().equals("common2.net.sf.jmoney.entry.incomeExpenseCurrency")
 					&& !entriesSectionProperty.getId().equals("other.net.sf.jmoney.entry.amount")) {
 				addColumn(entriesSectionProperty, index++);
 			}
 		}
 
 		TreeColumn col;
 
 		col = new TreeColumn(fTable, SWT.RIGHT);
 		col.setText("Debit");
 		col.setData("layoutData", new ColumnWeightData(2, 70));
 		col.setData(entriesContent.getDebitColumnManager());
 
 		col = new TreeColumn(fTable, SWT.RIGHT);
 		col.setText("Credit");
 		col.setData("layoutData", new ColumnWeightData(2, 70));
 		col.setData(entriesContent.getCreditColumnManager());
 
 		if (entriesContent.getBalanceColumnManager() != null) {
 			col = new TreeColumn(fTable, SWT.RIGHT);
 			col.setText("Balance");
 			col.setData("layoutData", new ColumnWeightData(2, 70));
 			col.setData(entriesContent.getBalanceColumnManager());
 		}
 
 		fTable.setLayout(tlayout);
 		fTable.setHeaderVisible(true);
 		fTable.setLinesVisible(true);
 
 		// Set sort column to first column (this is always the transaction date)
 		sortColumn = fTable.getColumn(0);
 		sortAscending = true;
 
 		// Fetch and sort the list of top level entries to display.
 		buildEntryList();
 
 		// Add the content.  The table is empty (no items)
 		// before this method is called, so the item fetcher
 		// simply adds news items to the end of the table.
 		setTableItems();
 		
 		// Create the editor.
 		editor = new TreeEditor(fTable);
 		//The editor must have the same size as the cell and must
 		//not be any smaller than 50 pixels.
 		editor.horizontalAlignment = SWT.LEFT;
 		editor.grabHorizontal = true;
 		editor.minimumWidth = 50;
 
 		fTable.addListener(SWT.MenuDetect, new Listener() {
 			public void handleEvent(Event event) {
 				// Although use of this event would be preferable to use of the MouseDown
 				// event, the co-ordinates of the mouse event are not given relative to
 				// the table control and therefore it would be non-trivial to determine
 				// the column to which the pop-up menu applies.  We therefore process the
 				// pop-up menu in the MouseDown event.
 			}
 		});
 
 		fTable.addMouseListener(new MouseAdapter() {
 			public void mouseDown(MouseEvent event) {
 				Point pt = new Point(event.x, event.y);
 				final TreeItem item = fTable.getItem(pt);
 				
 				// User may have clicked on space after the last row (i.e. after the new entry row).
 				// The selection is not changed by SWT when the user does this, so ignore
 				// this mouse down event, leaving the current selection and any
 				// open cell editor.
 				if (item == null) {
 					return;
 				}
 				
 				// Clean up any previous editor control
 				closeCellEditor();
 
 				final int column = getColumn(pt, item);
 
 				// User may have clicked on the expand button, or perhaps somewhere else.
 				if (column == -1) {
 					return;
 				}
 
 				if (event.button == 1) {
 					final IDisplayableItem data = (IDisplayableItem)item.getData();
 
 					if (!checkAndCommitTransaction(data)) {
 						return;
 					}
 					
 					// Go into edit mode only if the user clicked on a field in the
 					// selected row or on the new transaction row.
 					if (item != previouslySelectedItem) {
 						previouslySelectedItem = item;
 						if (data instanceof DisplayableNewEmptyEntry) {
 							// Replace with a real transaction.
 							// Note that all transactions must have at least
 							// two entries.
 			           		Transaction transaction = session.createTransaction();
 			           		Entry entry1 = transaction.createEntry();
 			           		transaction.createEntry();
 			           		
 			           		// TODO: There is so much duplicated stuff here that it
 			           		// would be better if we could get the listener to do this.
 			     
 			           		// When we set the account, the entry will be added
 			           		// to the list through the listener.  However, we want this
 			           		// new entry to be replaced into the new entry item.
 			           		// We do this by setting a reference to this entry in
 			           		// a field.  When the listener is notified of a new entry,
 			           		// it checks this field against the new entry and, if
 			           		// it matches, places the new entry in the new entry row.
 			           		// (The listener then resets the field to null).
 			           		newEntryRowEntry = entry1;
 			           		entriesContent.setNewEntryProperties(entry1);
 			           		JMoneyPlugin.myAssert(newEntryRowEntry == null);
 			           		
 			           		// Get the balance from the previous item.
 			           		long balance;
 			           		if (fTable.getItemCount() == 1) {
 			           			balance = entriesContent.getStartBalance();
 			           		} else {
 			           			balance = 
 			           				((DisplayableTransaction) 
 			           						fTable
 			           						.getItem(fTable.getItemCount() - 2)
 			           						.getData()
 									).balance;
 			           		}
 			           		
 			           		DisplayableTransaction dTrans = new DisplayableTransaction(entry1, balance);
 			           		item.setData(dTrans);
 			           		
 			           		// Although most properties will be blank, some, such
 			           		// as the date, default to non-blank values and these
 			           		// should be shown now.
 			           		updateItem(item);
 			           		
 			           		// Add to our cached list of all transactions in the table
 			           		entries.add(dTrans);
 			           		
 			           		// Add another blank new entry row to replace the one we
 			           		// have just converted to a new transaction.
 			    			TreeItem blankItem = new TreeItem(fTable, SWT.NULL);
 			    			blankItem.setData(new DisplayableNewEmptyEntry());
 			    			blankItem.setBackground(fTable.getItemCount() % 2 == 0 ? alternateTransactionColor
 								: transactionColor);
 						} else {
 							return;
 						}
 					}
 
 					IEntriesTableProperty entryData = (IEntriesTableProperty) fTable.getColumn(column).getData();
 					currentCellPropertyControl = entryData.createAndLoadPropertyControl(fTable, data);
 					if (currentCellPropertyControl != null) {
 						createCellEditor(item, column, currentCellPropertyControl);
 						
 						/*
 						 * If the category property, listen for changes and set
 						 * the currency to be the currency of the listed account
 						 * whenever the category is set to a multi-currency
 						 * category and no currency is set.
 						 */
 						if (entryData.getId().equals("common2.net.sf.jmoney.entry.account")) {
 							currentCellPropertyControl.getControl().addListener(SWT.Selection, new Listener() {
 								public void handleEvent(Event event) {
 									Entry changedEntry = data.getEntryForCommon2Fields();
 									Account account = changedEntry.getAccount();
 									if (account instanceof IncomeExpenseAccount) {
 										IncomeExpenseAccount incomeExpenseAccount = (IncomeExpenseAccount)account;
 										if (incomeExpenseAccount.isMultiCurrency()
 												&& changedEntry.getIncomeExpenseCurrency() == null) {
 											// Find the capital account in this transaction and set
 											// the currency of this income or expense to match the
 											// currency of the capital entry.
 											Commodity defaultCommodity = data.getEntryInAccount().getCommodity();
 											if (defaultCommodity instanceof Currency) {
 												changedEntry.setIncomeExpenseCurrency((Currency)defaultCommodity);
 											}
 										}
 									}
 								}
 							});
 						}
 					}
 				} else {
 					// Mouse button other than number 1 was pressed.
 					// Bring up a pop-up menu.
 					// It would be a more consistent interface if this menu were
 					// linked to the column header.  However, TableColumn has
 					// no setMenu method, nor does the column header respond to
 					// SWT.MenuDetect nor any other event when right clicked.
 					// This code works but does not follow the popup-menu conventions
 					// on even one platform!
 
 					Menu popupMenu = new Menu(fTable.getShell(), SWT.POP_UP);
 
 					MenuItem removeColItem = new MenuItem(popupMenu, SWT.NONE);
 
 					MenuItem shiftColLeftItem = new MenuItem(popupMenu,
 							SWT.NONE);
 
 					MenuItem shiftColRightItem = new MenuItem(popupMenu,
 							SWT.NONE);
 
 					Object[] messageArgs = new Object[] { fTable.getColumn(
 							column).getText() };
 
 					removeColItem.setText(new java.text.MessageFormat(
 							"Remove {0} column", java.util.Locale.US)
 							.format(messageArgs));
 					shiftColLeftItem.setText(new java.text.MessageFormat(
 							"Move {0} column left", java.util.Locale.US)
 							.format(messageArgs));
 					shiftColRightItem.setText(new java.text.MessageFormat(
 							"Move {0} column right", java.util.Locale.US)
 							.format(messageArgs));
 
 					removeColItem.addSelectionListener(new SelectionAdapter() {
 						public void widgetSelected(SelectionEvent e) {
 							fTable.getColumn(column).dispose();
 						}
 					});
 
 					shiftColLeftItem
 							.addSelectionListener(new SelectionAdapter() {
 								public void widgetSelected(SelectionEvent e) {
 									if (column > 1) {
 										IEntriesTableProperty entryData = (IEntriesTableProperty) fTable
 												.getColumn(column).getData();
 										removeColumn(column);
 										addColumn(entryData, column - 1);
 									}
 								}
 							});
 
 					shiftColRightItem
 							.addSelectionListener(new SelectionAdapter() {
 								public void widgetSelected(SelectionEvent e) {
 									if (column < fTable.getColumnCount() - 1) {
 										IEntriesTableProperty entryData = (IEntriesTableProperty) fTable
 												.getColumn(column).getData();
 										removeColumn(column);
 										addColumn(entryData, column + 1);
 									}
 								}
 							});
 
 					new MenuItem(popupMenu, SWT.SEPARATOR);
 					
 					for (Iterator iter = entriesContent
 							.getAllEntryDataObjects().iterator(); iter
 							.hasNext();) {
 						final IEntriesTableProperty entriesSectionProperty = (IEntriesTableProperty) iter
 								.next();
 						boolean found = false;
 						for (int index = 0; index < fTable.getColumnCount(); index++) {
 							IEntriesTableProperty entryData2 = (IEntriesTableProperty) (fTable
 									.getColumn(index).getData());
 							if (entryData2 == entriesSectionProperty) {
 								found = true;
 								break;
 							}
 						}
 
 						if (!found) {
 							Object[] messageArgs2 = new Object[] { entriesSectionProperty
 									.getText() };
 
 							MenuItem addColItem = new MenuItem(popupMenu,
 									SWT.NONE);
 							addColItem.setText(new java.text.MessageFormat(
 									"Add {0} column", java.util.Locale.US)
 									.format(messageArgs2));
 
 							addColItem
 									.addSelectionListener(new SelectionAdapter() {
 										public void widgetSelected(
 												SelectionEvent e) {
 											addColumn(entriesSectionProperty,
 													Math.max(1, column));
 										}
 									});
 						}
 					}
 
 					popupMenu.setVisible(true);
 				}
 			}
 		});
 
 		fTable.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent event) {
     			if (ignoreSelectionEvent) {
         			ignoreSelectionEvent = false;
     			} else {
     				IDisplayableItem data = (IDisplayableItem)event.item.getData();
     				if (!checkAndCommitTransaction(data)) {
     					return;
     				}
                		
     				fireSelectionChanges(data);
     			}
 			}
 
 			public void widgetDefaultSelected(SelectionEvent event) {
 				// If we get here, we know that:
 				// - This is not a selection that must be ignored.  The reason we
 				// know this is that if there was an error on another transaction,
 				// a dialog would have come up in response to the first click, and
 				// that stops this double click event.
 				// - there is no error on another transaction.  In fact, this row
 				// must already be the currently selected row.
    				JMoneyPlugin.myAssert(!ignoreSelectionEvent);
 				JMoneyPlugin.myAssert(previouslySelectedItem == event.item);
 				
 				// Because double clicks may create actions that would remove the
 				// item from the list, we free up the item.
 				closeCellEditor();
 				fTable.setSelection(new TreeItem [] {});
 				previouslySelectedItem = null;
 				
 				IDisplayableItem data = (IDisplayableItem)event.item.getData();
 				if (!checkAndCommitTransaction(data)) {
 					return;
 				}
 				
 				fireRowDefaultSelection(data);
 			}
 		});
 		
 		session.addSessionChangeListener(new SessionChangeAdapter() {
 			public void objectAdded(ExtendableObject newObject) {
 				if (newObject instanceof Entry) {
 					Entry newEntry = (Entry) newObject;
 					// if the entry is in this table, add it
 					if (entriesContent.isEntryInTable(newEntry)) {
 						addEntryInAccount(newEntry);
 					}
 
 					// Even if this entry is not in this table, if one of
 					// the other entries in the transaction is in this table
 					// then the table view will need updating because the split
 					// entry rows will need updating.
 					for (Iterator iter = newEntry.getTransaction()
 							.getEntryCollection().iterator(); iter.hasNext();) {
 						Entry entry = (Entry) iter.next();
 						if (!entry.equals(newEntry)
 								&& entriesContent.isEntryInTable(entry)) {
 							addEntry(entry, newEntry);
 						}
 					}
 				} else if (newObject instanceof Transaction) {
 					Transaction newTransaction = (Transaction) newObject;
 					// Add all entries in the transaction that are to be listed at
 					// the top level in this list.
 					for (Iterator iter = newTransaction.getEntryCollection().iterator(); iter.hasNext();) {
 						Entry entry = (Entry) iter.next();
 						if (entriesContent.isEntryInTable(entry)) {
 							addEntryInAccount(entry);
 						}
 					}
 				}
 			}
 
 			public void objectDeleted(ExtendableObject deletedObject) {
 				if (deletedObject instanceof Entry) {
 					Entry deletedEntry = (Entry) deletedObject;
 					// if the entry is in this table, remove it.
 					if (entriesContent.isEntryInTable(deletedEntry)) {
 						removeEntryInAccount(deletedEntry);
 					}
 
 					// Even if this entry is not in this table, if one of
 					// the other entries in the transaction is in this table
 					// then the table view will need updating because the split
 					// entry rows will need updating.
 					for (Iterator iter = deletedEntry.getTransaction()
 							.getEntryCollection().iterator(); iter.hasNext();) {
 						Entry entry = (Entry) iter.next();
 						if (!entry.equals(deletedEntry)
 								&& entriesContent.isEntryInTable(entry)) {
 							removeEntry(entry, deletedEntry);
 						}
 					}
 				} else if (deletedObject instanceof Transaction) {
 					Transaction deletedTransaction = (Transaction) deletedObject;
 
 					/*
 					 * If <code>previouslySelectedItem</code> is a row in this
 					 * transaction then we must set it to null because the row
 					 * item will no longer be valid. Normally we must be not
 					 * change <code>previouslySelectedItem</code> without
 					 * first validating and committing the transaction in which
 					 * the row item occurs. However, in this situation the
 					 * transaction is being deleted so no validation of the
 					 * transaction is necessary.
 					 */
 					if (previouslySelectedItem != null
 							&& deletedTransaction.equals(((IDisplayableItem) previouslySelectedItem.getData()).getTransaction())) {
 						previouslySelectedItem = null;
 					}
 					
 					for (Iterator iter = deletedTransaction
 							.getEntryCollection().iterator(); iter.hasNext();) {
 						Entry entry = (Entry) iter.next();
 						if (entriesContent.isEntryInTable(entry)) {
 							removeEntryInAccount(entry);
 						}
 					}
 				}
 			}
 
 			public void objectChanged(ExtendableObject extendableObject,
 					PropertyAccessor propertyAccessor, Object oldValue,
 					Object newValue) {
 				if (extendableObject instanceof Entry) {
 					Entry entry = (Entry) extendableObject;
 
 					// If we have been told to ignore this new entry
 					// then we had better do so.
 					if (entry.equals(newEntryRowEntry)) {
 						newEntryRowEntry = null;
 						return;
 					}
 					
 					/*
 					 * A property change may result in a top-level entry no
 					 * longer meeting the requirements to be listed in the
 					 * table. If the entry is not either the selected entry or
 					 * the parent of the selected entry then the changed entry
 					 * is immediately removed from the list.
 					 * 
 					 * However, if the changed entry is the selected top-level
 					 * entry or the parent of the selected child entry then we
 					 * do not remove the entry from the list. It would confuse
 					 * the user if an entry disappeared while the user was
 					 * editing the entry. The entry is instead removed when the
 					 * entry is no longer the selected entry. This may still be
 					 * a little confusing, because the entry would disappear
 					 * when the user selects another entry, but the user is
 					 * forced to commit the changes and should not be so
 					 * surprised to see that the entry is no longer in the table
 					 * once the changes are committed.
 					 * 
 					 * Note that the entry being changed may be the other entry
 					 * in the transaction, whose properties are also diplayed on
 					 * the parent row. As long as properties from the 'other'
 					 * entry never affect whether an entry is listed then this
 					 * code is correct. If, however, properties from the other
 					 * entry or properties from the transaction affect whether
 					 * an entry is listed then this code will need re-visiting.
 					 */
 					if (!entry.equals(getSelectedEntryInAccount())) {
 						boolean wasInTable = entriesContent.isEntryInTable(
 								entry, propertyAccessor, oldValue);
 						boolean isNowInTable = entriesContent.isEntryInTable(
 								entry, propertyAccessor, newValue);
 						if (wasInTable && !isNowInTable) {
 							removeEntryInAccount(entry);
 						} else if (!wasInTable && isNowInTable) {
 							addEntryInAccount(entry);
 						}
 					}
 
 					/*
 					 * Find all rows on which properties of this entry are
 					 * displayed and update those rows. This involves finding
 					 * all entries in the same transaction that are listed in
 					 * the table contents (including this entry itself if this
 					 * entry is a top level entry in the table). TODO: we do not
 					 * need to include this entry itself if it were just added
 					 * or just removed above. This code is not quite perfect but
 					 * probably works.
 					 */
 					Transaction transaction = entry.getTransaction();
 					for (Iterator iter = transaction.getEntryCollection()
 							.iterator(); iter.hasNext();) {
 						Entry entry2 = (Entry) iter.next();
 						if (entriesContent.isEntryInTable(entry2)) {
 							updateEntry(entry2, entry, propertyAccessor,
 									oldValue, newValue);
 						}
 					}
 				}
 
 				// When a transaction property changes, we notify the entries list
 				// control once for each entry in the transaction where the entry is
 				// listed as a top level entry in the table.
 				// (Only rows for top level entries display transaction properties).
 				if (extendableObject instanceof Transaction) {
 					Transaction transaction = (Transaction) extendableObject;
 					
 					for (Iterator iter = transaction.getEntryCollection()
 							.iterator(); iter.hasNext();) {
 						Entry entry = (Entry) iter.next();
 						if (entriesContent.isEntryInTable(entry)) {
 							updateTransaction(entry, propertyAccessor,
 									oldValue, newValue);
 						}
 					}
 				}
 
 				// Account names and currency names affect the data displayed in the
 				// entries list.  These changes are both infrequent, may involve a
 				// change to a lot of entries, and would involve finding all transactions
 				// that contain both an entry with the changed account or currency
 				// and an entry with in the account for this page.  It is therefore
 				// better just to refresh all the entire entries list, but note that
 				// only the text need be refreshed.
 				if (propertyAccessor == AccountInfo.getNameAccessor()
 						|| propertyAccessor == CommodityInfo.getNameAccessor()) {
 					refreshLabels();
 				}
 			}
 		}, this);
 	}
 
 	/**
 	 * If the user has clicked on a row in a different transaction and changes
 	 * have been made to a previously selected transaction then check that the
 	 * previous transaction is valid and complete. If it is, commit it.
 	 * <P>
 	 * Edits may have been made through cell editors, or they may have been made
 	 * using the property controls in the entry edit section, or the user may
 	 * have opened the dialog window showing the full transaction details and
 	 * made edits. We detect any of these changes by asking the transaction
 	 * manager if any uncommitted changes exit.
 	 * <P>
 	 * If there are any changes, then we first check the transaction to be sure
 	 * the data is complete and valid. If the are any problems that prevent
 	 * committing the transaction then the user is told and this the selected
 	 * row is set to the row with the error.
 	 * <P>
 	 * previouslySelectedItem is set to the new selected item if and only if
 	 * this method returns false. If this method returned true then the caller
 	 * must update previouslySelectedItem.
 	 * 
 	 * @param item
 	 *            the item for the row to which the user would like to move
 	 * @return true if it is ok to move to the new row, false if the new row is
 	 *         in a different transaction and there are errors in the previous
 	 *         transaction that must be corrected by the user.
 	 */
 	protected boolean checkAndCommitTransaction(IDisplayableItem newData) {
 		if (previouslySelectedItem == null) {
 			return true;
 		}
 		
 		DisplayableTransaction previousTransData;
 		Object previousData = previouslySelectedItem.getData();
 		if (previousData instanceof DisplayableTransaction) {
 			previousTransData = (DisplayableTransaction)previousData;
 		} else if (previousData instanceof DisplayableEntry) {
 			previousTransData = ((DisplayableEntry)previousData).transactionData;
 		} else {
			/*
			 * We were not on a transaction. This can only happen if we were
			 * previously on the blank 'new transaction' line. There is no
			 * previous transaction to check and commit.
			 */
			return true;
 		}
 		
 		Transaction previousTransaction = previousTransData.getTransaction();
 		
 		DisplayableTransaction newTransData;
 		if (newData instanceof DisplayableTransaction) {
 			newTransData = (DisplayableTransaction)newData;
 		} else if (newData instanceof DisplayableEntry) {
 			newTransData = ((DisplayableEntry)newData).transactionData;
 		} else {
 			// We are not on a transaction (we are probably on the
 			// blank 'new transaction' line.
 			newTransData = null;
 		}
 		
 		if (newTransData != previousTransData) {
 			// We have moved to a different transaction.
 
 			// If changes have been made then check they are valid and ask
 			// the user if the changes should be committed.
 			if (transactionManager.hasChanges()) {
 				// Validate the transacion.
 
 				long totalAmount = 0;
 				Commodity commodity = null;
 				boolean mixedCommodities = false;
 				
 				TreeItem parentItem = (previouslySelectedItem.getData() instanceof DisplayableTransaction)
 				? previouslySelectedItem
 						: previouslySelectedItem.getParentItem();
 
 				class InvalidUserEntryException extends Exception {
 					private static final long serialVersionUID = -8693190447361905525L;
 
 					TreeItem itemWithError = null;
 					
 					public InvalidUserEntryException(String message, TreeItem itemWithError) {
 						super(message);
 						this.itemWithError = itemWithError;
 					}
 
 					public TreeItem getItemWithError() {
 						return itemWithError;
 					}
 				};
 				
 				try {
 					if (previousTransaction.getDate() == null) {
 						throw new InvalidUserEntryException(
 								"The date cannot be blank.",
 								parentItem);
 					} else {
 						for (Iterator iter = previousTransaction.getEntryCollection().iterator(); iter.hasNext(); ) {
 							Entry entry = (Entry)iter.next();
 							if (entry.getAccount() == null) {
 								throw new InvalidUserEntryException(
 										"A category must be selected.",
 										lookupSplitEntry(parentItem, entry));
 							}
 							
 							if (entry.getAccount() instanceof IncomeExpenseAccount) {
 								IncomeExpenseAccount incomeExpenseAccount = (IncomeExpenseAccount)entry.getAccount();
 								if (incomeExpenseAccount.isMultiCurrency()
 										&& entry.getIncomeExpenseCurrency() == null) {
 									throw new InvalidUserEntryException(
 											"A currency must be selected (" + incomeExpenseAccount.getName() + " is a multi-currency category).",
 											lookupSplitEntry(parentItem, entry));
 								}
 							}
 							
 							if (commodity == null) {
 								commodity = entry.getCommodity();
 							} else if (!commodity.equals(entry.getCommodity())) {
 								mixedCommodities = true;
 							}
 							
 							totalAmount += entry.getAmount();
 						}
 					}
 					
 					/*
 					 * If all the entries are in the same currency then the sum of
 					 * the entries in the transaction must add to zero. In a
 					 * transaction with child rows we display an error to the user
 					 * if the sum is not zero. However, in a simple transaction the
 					 * amount of the income and expense is not shown because it
 					 * always matches the amount of the credit or debit. The amounts
 					 * may not match if, for example, the currencies used to differ
 					 * but the user changed the category so that the currencies now
 					 * match. We present the data to the user as tho the other
 					 * amount does not exist, so we should silently correct the
 					 * amount.
 					 */
 					if (totalAmount != 0 && !mixedCommodities) {
 						// TODO: For double entries where both accounts are in the same currency,
 						// should the amount for one account automatically change when the user changes
 						// the amount for the other account?  Currently the user must update both
 						// to keep the transaction balanced and to avoid the following error message.
 						if (previousTransData.hasSplitEntries() || previousTransData.isDoubleEntry()) {
 							throw new InvalidUserEntryException(
 									"The transaction does not balance.  " +
 									"Unless some entries in the transaction are in different currencies, " +
 									"the sum of all the entries in a transaction must add up to zero.",
 									previouslySelectedItem);
 						} else {
 							Entry accountEntry = newTransData.getEntryForAccountFields();
 							Entry otherEntry = newTransData.getEntryForOtherFields();
 							otherEntry.setAmount(-accountEntry.getAmount());
 						}
 					}
 					
 					/*
 					 * Check for zero amounts. A zero amount is
 					 * normally a user error and will not be accepted. However, if
 					 * this is a simple transaction and the currencies are different
 					 * then we prompt the user for the amount of the other entry
 					 * (the income and expense entry). This is very desirable
 					 * because the foreign currency column (being used so little) is
 					 * not displayed by default.
 					 */
 					if (previousTransData.isSimpleEntry()
 							&& previousTransData.getEntryForAccountFields().getAmount() != 0
 							&& previousTransData.getEntryForOtherFields().getAmount() == 0
 							&& previousTransData.getEntryForOtherFields().getCommodity() != previousTransData.getEntryForAccountFields().getCommodity()) {
 						ForeignCurrencyDialog dialog = new ForeignCurrencyDialog(
 								getShell(),
 								previousTransData);
 						dialog.open();
 					} else {
 						for (Iterator iter = previousTransaction.getEntryCollection().iterator(); iter.hasNext(); ) {
 							Entry entry = (Entry)iter.next();
 							if (entry.getAmount() == 0) {
 								throw new InvalidUserEntryException(
 										"A non-zero credit or debit amount must be entered.",
 										lookupSplitEntry(parentItem, entry));
 							}
 						}
 					}
 				} catch (InvalidUserEntryException e) {
         			MessageDialog dialog = new MessageDialog(
         					fTable.getShell(),
         					"Incomplete or invalid data in entry",
         					null, // accept the default window icon
         					e.getLocalizedMessage(),
         					MessageDialog.ERROR,
         					new String[] { IDialogConstants.OK_LABEL }, 0);
 
         			// While waiting for the dialog box input, SWT will process
         			// the selection change event.  We want that event to be ignored,
         			// as we are going to set the selection back to the row with
         			// the error.  We do this by setting on a flag field.
         			ignoreSelectionEvent = true;
         			dialog.open();
         			JMoneyPlugin.myAssert(!ignoreSelectionEvent);
         			
         			// Unfortunately there is no 'doit' field in mouse events.
         			// However, we can simply select the row with the error and that
         			// will cancel out the user's selection.
     				fTable.setSelection( new TreeItem [] { e.getItemWithError() });
     				
     				/*
 					 * SWT does not seem to generate events for the above
 					 * selection change, so fire the event ourselves. (If the
 					 * error was on the previously selected item, then there has
 					 * been no change to the selected item so we don't need to
 					 * fire anything.
 					 */
         			if (e.getItemWithError() != previouslySelectedItem) {
         				fireSelectionChanges((IDisplayableItem)(e.getItemWithError()).getData());
         				previouslySelectedItem = e.getItemWithError();
         			}
         			
         			return false;
 				}
 				
 				// Commit the changes to the transaction
            		transactionManager.commit();
 			}
 		}
 		
 		return true;
 	}
 
 	private void createCellEditor(final TreeItem item, final int column, IPropertyControl propertyControl) {
 		currentCellPropertyControl = propertyControl;
 
 		Control c = currentCellPropertyControl.getControl();
 			editor.setEditor(c, item, column);
 			c.setFocus();
 			
 			if (c instanceof Composite) {
 				Composite composite = (Composite)c;
 
 				// Let's see which control has the focus, and listen
 				// for the traversal events from that control (listening
 				// for traversals from the composite control will not work
 				// because none occur).
 				
 				// This does mean that the user cannot use the tab key to move
 				// between controls within the composite.  For example, if a date
 				// control has focus then the user cannot tab to the button at the
 				// right side of the date control.  Users are more likely to expect
 				// the tab key to move to the next property, so that is the trade off.
 				Control [] childControls = composite.getChildren();
 				for (int i = 0; i < childControls.length; i++) {
 					if (childControls[i].isFocusControl()) {
 						c = childControls[i];
 						break;
 					}
 				}
 			}
 			
 			c.addTraverseListener(new TraverseListener() {
 				public void keyTraversed(TraverseEvent event) {
 					if (event.detail == SWT.TRAVERSE_TAB_NEXT) {
 						// Clean up this editor control
 						closeCellEditor();
 						
 						// Find the next editable property in the row
 						IDisplayableItem data = (IDisplayableItem) item.getData();
 						IPropertyControl nextPropertyControl = null;
 						int nextColumn = column;
 						while (nextPropertyControl == null
 								&& nextColumn + 1 < fTable.getColumnCount()) {
 							nextColumn++;
 							IEntriesTableProperty entryData = (IEntriesTableProperty) fTable.getColumn(nextColumn).getData();
 							nextPropertyControl = entryData.createAndLoadPropertyControl(fTable, data);
 						}
 						
 						// Create the next control
 						if (nextPropertyControl != null) {
 							createCellEditor(item, nextColumn, nextPropertyControl);
 						}
 					} else if (event.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
 						// Clean up this editor control
 						closeCellEditor();
 						
 						// Find the previous editable property in the row
 						IDisplayableItem data = (IDisplayableItem) item.getData();
 						IPropertyControl nextPropertyControl = null;
 						int nextColumn = column;
 						while (nextPropertyControl == null
 								&& nextColumn - 1 >= 0) {
 							nextColumn--;
 							IEntriesTableProperty entryData = (IEntriesTableProperty) fTable.getColumn(nextColumn).getData();
 							nextPropertyControl = entryData.createAndLoadPropertyControl(fTable, data);
 						}
 						
 						// Create the next control
 						if (nextPropertyControl != null) {
 							createCellEditor(item, nextColumn, nextPropertyControl);
 						}
 					}
 				}
 			});
 	}
 
 	/**
 	 * Set the content into the table.  If this is the first time this
 	 * method is called then the table will be empty and a TableItem
 	 * must be created for each row in the table.  If this is a subsequent
 	 * call then TableItem objects will already exist and we re-use them,
 	 * creating extra at the end or disposing excess if necessary. 
 	 */
 	private void setTableItems() {
 		// Sort the entries.
 		Comparator rowComparator = new RowComparator(sortColumn, sortAscending);
 		DisplayableTransaction [] sortedEntries = new DisplayableTransaction [entries.size()];
 		sortedEntries = (DisplayableTransaction [])entries.toArray(sortedEntries);
 		Arrays.sort(sortedEntries, rowComparator); 
 
 		/*
 		 * Alternate the color of the entries to improve the readibility 
 		 */
 		boolean isAlternated = true;
 		long saldo = entriesContent.getStartBalance();
 
 		int itemIndex = 0;
 
 		for (int i = 0; i < sortedEntries.length; i++) {
 			DisplayableTransaction data = sortedEntries[i];
 			
 			isAlternated = !isAlternated;
 
 			// Get the next item, using existing items or creating
 			// items if there is not an item already at this index.
 			TreeItem item;
 			if (itemIndex < fTable.getItemCount()) {
 				item = fTable.getItem(itemIndex++);
 				
 				// Dispose of any child items.
 				TreeItem childItems [] = item.getItems();
 				for (int j = 0; j < childItems.length; j++) {
 					childItems[j].dispose();
 				}
 			} else {
 				itemIndex++;
 				item = new TreeItem(fTable, SWT.NULL);
 			}
 
 			saldo = saldo + data.getEntryForAccountFields().getAmount();
 			data.setBalance(saldo);
 			item.setData(data);
 			updateItem(item);
 
 			item.setBackground(isAlternated ? alternateTransactionColor
 					: transactionColor);
 
 			// Dispose of all children of this item.
 			TreeItem[] childItems = item.getItems();
 			for (int j = 0; j < childItems.length; j++) {
 				childItems[j].dispose();
 			}
 
 			if (!data.isSimpleEntry()) {
 				// Case of an splitted entry. We display the transaction and
 				// account entry on the first line and the other entries of the
 				// transaction on the following ones.
 				Iterator itSubEntries = data.getSplitEntryIterator();
 				while (itSubEntries.hasNext()) {
 					Entry entry2 = (Entry) itSubEntries.next();
 					DisplayableEntry entryData = new DisplayableEntry(entry2,
 							data);
 					TreeItem childItem = new TreeItem(item, 0);
 					childItem.setData(entryData);
 					updateItem(childItem);
 
 					childItem.setBackground(isAlternated ? alternateEntryColor
 							: entryColor);
 				}
 			}
 		}
 
 		// an empty line to have the possibility to enter a new entry
 		TreeItem blankItem;
 		if (itemIndex < fTable.getItemCount()) {
 			blankItem = fTable.getItem(itemIndex++);
 		} else {
 			itemIndex++;
 			blankItem = new TreeItem(fTable, SWT.NULL);
 		}
 		blankItem.setData(new DisplayableNewEmptyEntry());
 
 		isAlternated = !isAlternated;
 		blankItem.setBackground(isAlternated ? alternateTransactionColor
 				: transactionColor);
 
 		// If there were more items in the table than needed,
 		// dispose of the excess.
 		while (itemIndex < fTable.getItemCount()) {
 			fTable.getItem(itemIndex).dispose();
 		}
 	}
 
 	/**
 	 * @param entriesSectionProperty
 	 * @param columnIndex
 	 */
 	private void addColumn(IEntriesTableProperty entriesSectionProperty,
 			int columnIndex) {
 		final TreeColumn col = new TreeColumn(fTable, SWT.NULL, columnIndex);
 		col.setText(entriesSectionProperty.getText());
 
 		col.setData(entriesSectionProperty);
 
 		col.setData("layoutData", new ColumnWeightData(entriesSectionProperty
 				.getWeight(), entriesSectionProperty.getMinimumWidth()));
 
 		// Re-calculate the layout.  SWT does not do this automatically when a
 		// column is added, resulting in the new column being set to a zero width.
 		fTable.layout(true);
 
 		col.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				// Clean up any cell editor
 				closeCellEditor();
 
 				/*
 				 * Before we can sort, validate and commit any changes to the
 				 * currently selected transaction.
 				 * 
 				 * TODO: It would not be too difficult to allow sorting without
 				 * forcing the user the complete the current transaction. To do
 				 * this, we must first make a note of the current row (stored in
 				 * the previouslySelectedItem field), then set
 				 * previouslySelectedItem to null, then do the sort (which may
 				 * destroy the item referenced by previouslySelectedItem), then
 				 * find the item that now contains the uncommitted transaction
 				 * and set previouslySelectedItem to that, also selecting the
 				 * item. We must, however, consider whether this would be a
 				 * benefit. It is possible that the user wants to sort so that
 				 * the user can find similar transactions and decide how to
 				 * complete the new transaction. However, it is also possible
 				 * that the user will get confused because the user is told
 				 * about an incomplete entry too long after the user entered the
 				 * entry.
 				 */
 				if (!checkAndCommitTransaction(null)) {
 					return;
 				}
 				previouslySelectedItem = null;
            		
 				if (col == sortColumn) {
 					sortAscending = !sortAscending;
 					col.setImage(JMoneyPlugin.createImageDescriptor(
 							sortAscending ? "icons/ArrowUp.gif"
 									: "icons/ArrowDown.gif").createImage());
 				} else {
 					sortColumn.setImage(null);
 					sortColumn = col;
 					sortAscending = true;
 					col.setImage(JMoneyPlugin.createImageDescriptor(
 							"icons/ArrowUp.gif").createImage());
 				}
 
 				// If the user was editing a cell when the sort order was changed,
 				// close the edit control.
 				closeCellEditor();
 				
 				setTableItems();
 			}
 		});
 
 		// Update the contents of the column.  This is not necessary during initial
 		// column setup because columns are setup before columns.  When columns are
 		// inserted or moved, however, this is necessary.
 		for (int rowIndex = 0; rowIndex < fTable.getItemCount(); rowIndex++) {
 			TreeItem item = fTable.getItem(rowIndex);
 			IDisplayableItem data = (IDisplayableItem) item.getData();
 			item.setText(columnIndex, entriesSectionProperty
 					.getValueFormattedForTable(data));
 		}
 	}
 
 	/**
 	 * Removes a column from the table.
 	 * 
 	 * @param colIndex
 	 */
 	private void removeColumn(int colIndex) {
 		fTable.getColumn(colIndex).dispose();
 	}
 
 	/**
 	 * 
 	 */
 	protected void closeCellEditor() {
 		Control oldEditor = editor.getEditor();
 		if (oldEditor != null) {
 			// Save the value from the edit control.
 			// TODO: figure out the sequence of events that lead to
 			// currentCellPropertyControl being null and clean this up.
 			if (currentCellPropertyControl != null) {
 				currentCellPropertyControl.save();
 				oldEditor.dispose();
 				currentCellPropertyControl = null;
 			}
 		}
 	}
 
 	/**
 	 * Determine the column in which the mouse was clicked
 	 * @param pt
 	 * @param item
 	 * @return the index of the column
 	 */
 	protected int getColumn(Point pt, TreeItem item) {
 		int i;
 		for (i = 0; i < fTable.getColumnCount(); i++) {
 			Rectangle rect = item.getBounds(i);
 			if (rect.contains(pt)) {
 				break;
 			}
 		}
 
 		if (i == fTable.getColumnCount()) {
 			// No column sucessfully hit.
 			return -1;
 			//throw new RuntimeException("hit test failure");
 		}
 		return i;
 	}
 
 	/**
      * Build the list of entries to be shown in the entries list. This method
      * sets the list into <code>entries</code>. This list contains only the
      * top level entries.
      * <P>
      * The entries are unsorted.
      */
     private void buildEntryList() {
         // Note that the balances are not set at this time. This is done
         // when the data is set into the table. It is just as easy
         // and efficient to do it then and that reduces the effort
         // to keep the balances updated.
         entries = new Vector();
         for (Iterator iter = entriesContent.getEntries().iterator(); iter
                 .hasNext();) {
             Entry accountEntry = (Entry) iter.next();
             DisplayableTransaction data = new DisplayableTransaction(
                     accountEntry, 0);
             if (matchesFilter(data)) {
                 entries.add(data);
             }
         }
     }
 
 	/**
 	 * Filters work at the transaction level, not the entry level.
 	 * Either the entire transaction is displayed, or none of
 	 * the transaction is displayed.  If any entry in a split
 	 * transaction matches the filter then the entire transaction
 	 * is shown.
 	 * 
 	 * @param transData
 	 * @return
 	 */
 	private boolean matchesFilter(DisplayableTransaction transData) {
 		if (entriesContent.filterEntry(transData)) {
 			return true;
 		}
 
 		if (!transData.isSimpleEntry()) {
 			Iterator itSubEntries = transData.getSplitEntryIterator();
 			while (itSubEntries.hasNext()) {
 				Entry entry2 = (Entry) itSubEntries.next();
 				DisplayableEntry entryData = new DisplayableEntry(entry2,
 						transData);
 				if (entriesContent.filterEntry(entryData)) {
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	public Entry getSelectedEntryInAccount() {
 		if (fTable.getSelectionCount() != 1) {
 			return null;
 		}
 
 		Object selectedObject = fTable.getSelection()[0].getData();
 
 		JMoneyPlugin.myAssert(selectedObject != null);
 		if (selectedObject != null) {
 			IDisplayableItem data = (IDisplayableItem) selectedObject;
 			if (selectedObject instanceof DisplayableTransaction) {
 				DisplayableTransaction transactionData = (DisplayableTransaction) data;
 				return transactionData.getEntryForAccountFields();
 			}
 			if (selectedObject instanceof DisplayableEntry) {
 				DisplayableEntry entryData = (DisplayableEntry) data;
 				return entryData.getDisplayableTransaction()
 						.getEntryForAccountFields();
 			}
 		}
 
 		return null;
 	}
 
 	public Entry getSelectedEntry() {
 		if (fTable.getSelectionCount() != 1) {
 			return null;
 		}
 
 		Object selectedObject = fTable.getSelection()[0].getData();
 
 		if (selectedObject != null) {
 			IDisplayableItem data = (IDisplayableItem) selectedObject;
 			if (selectedObject instanceof DisplayableTransaction) {
 				DisplayableTransaction transactionData = (DisplayableTransaction) data;
 				return transactionData.getEntryForAccountFields();
 			}
 			if (selectedObject instanceof DisplayableEntry) {
 				DisplayableEntry entryData = (DisplayableEntry) data;
 				return entryData.getEntryForThisRow();
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Class representing a top level entry in the list.
 	 * <P>
 	 * Note that, despite the name of this class, it is entries and
 	 * not transactions that are listed.  For example, if a transaction
 	 * has two entries in the account then that transaction will appear
 	 * twice in the list.
 	 */	public class DisplayableTransaction implements IDisplayableItem {
 		private Entry entry;
 
 		private long balance;
 
 		/**
 		 * A cache of the entries in this transaction excluding
 		 * the entry itself.
 		 */
 		private Vector otherEntries = new Vector();
 
 		public DisplayableTransaction(Entry entry, long saldo) {
 			this.entry = entry;
 			this.balance = saldo;
 			
 			Iterator itSubEntries = entry.getTransaction().getEntryCollection()
 			.iterator();
 			while (itSubEntries.hasNext()) {
 				Entry entry2 = (Entry) itSubEntries.next();
 				if (!entry2.equals(entry)) {
 					otherEntries.add(entry2);
 				}
 			}
 		}
 
 		/**
 		 * A transaction with split entries is a transaction that
 		 * has entries in three or more accounts (where each account
 		 * may be either a capital account or an income and
 		 * expense category).
 		 */
 		public boolean hasSplitEntries() {
 			return otherEntries.size() > 1;
 		}
 
 		/**
 		 * A double entry transaction is a transaction with two
 		 * entries (a credit entry and a debit entry) and where
 		 * both entries are capital accounts.
 		 * <P>
 		 * A double entry is not a special case as far as the model
 		 * is concerned.  However, it is a special case as far
 		 * as the entries list is concerned because such transactions
 		 * are displayed on two lines.  The reason why the transaction
 		 * must take two lines is that both entries will have
 		 * capital account properties such as the value date
 		 * (valuta) and a memo and thus needs two lines to display.
 		 */
 		public boolean isDoubleEntry() {
 			return otherEntries.size() == 1 
 				&& ((Entry)otherEntries.firstElement()).getAccount() instanceof CapitalAccount;
 		}
 
 		/**
 		 * A simple entry is a transaction that contains two
 		 * entries, one being a capital account and the other
 		 * being an income or expense account.  Most transactions
 		 * are simple entries.
 		 * <P>
 		 * Note that jmoney requires all transactions to have
 		 * at least two entries.  Therefore, with the rare exception
 		 * of a transaction that has two entries both of which
 		 * are income and expense accounts, all transactions will
 		 * be either a split transaction, a double entry, or a
 		 * simple entry.
 		 * <P>
 		 * The user may not have yet selected the category for a
 		 * simple transaction.  Such a transaction is displayed on
 		 * a single line and therefore considered a simple transaction.
 		 * Therefore the test to be used here is that the category is NOT a
 		 * capital account (rather than testing FOR an income and
 		 * expense account. 
 		 */
 		public boolean isSimpleEntry() {
 			return otherEntries.size() == 1 
 			&& !(((Entry)otherEntries.firstElement()).getAccount() instanceof CapitalAccount);
 		}
 		
 		/**
 		 * @return
 		 */
 		public Iterator getSplitEntryIterator() {
 			return otherEntries.iterator();
 		}
 
 		public Transaction getTransactionForTransactionFields() {
 			return entry.getTransaction();
 		}
 
 		public Entry getEntryForAccountFields() {
 			return entry;
 		}
 
 		public Entry getEntryForOtherFields() {
 			/*
 			 * Income and expense account properties are placed
 			 * on the top level row only if there are no child
 			 * rows.
 			 */
 			if (isSimpleEntry()) {
 				return (Entry)otherEntries.firstElement();
 			} else {
 				return null;
 			}
 		}
 
 		public Entry getEntryForCommon1Fields() {
 			return entry;
 		}
 		
 		public Entry getEntryForCommon2Fields() {
 			/*
 			 * If there is only one other entry then the account
 			 * for that entry goes in the top level entry so that
 			 * it can be seen even when the item is collapsed.
 			 * This is so regardless of whether the other entry
 			 * is an income and expense entry or an entry in a
 			 * capital account.
 			 */
 			if (otherEntries.size() == 1) {
 				return (Entry)otherEntries.firstElement();
 			} else {
 				return null;
 			}
 		}
 		
 		public long getAmount() {
 			return entry.getAmount();
 		}
 
 		public Transaction getTransaction() {
 			return entry.getTransaction();
 		}
 
 		public boolean isBalanceAffected() {
 			return true;
 		}
 
 		public long getBalance() {
 			return balance;
 		}
 
 		/**
 		 * @param balance
 		 */
 		public void setBalance(long balance) {
 			this.balance = balance;
 		}
 
 		public Entry getEntryInAccount() {
 			return entry;
 		}
 
 		public Entry getEntryForThisRow() {
 			return entry;
 		}
 	}
 
 	public class DisplayableEntry implements IDisplayableItem {
 
 		private Entry entry;
 
 		private DisplayableTransaction transactionData;
 
 		public DisplayableEntry(Entry entry,
 				DisplayableTransaction transactionData) {
 			this.entry = entry;
 			this.transactionData = transactionData;
 		}
 
 		public DisplayableTransaction getDisplayableTransaction() {
 			return this.transactionData;
 		}
 
 		public Transaction getTransactionForTransactionFields() {
 			return null;
 		}
 
 		public Entry getEntryForAccountFields() {
 			// If this entry has been set to an income and expense account entry
 			// then the capital account fields must be blank and we return null
 			// to indicate this, otherwise the capital account properties are
 			// associated with this entry.
 			if (entry.getAccount() == null
 					|| entry.getAccount() instanceof CapitalAccount) {
 				return entry;
 			} else {
 				return null;
 			}
 		}
 
 		public Entry getEntryForOtherFields() {
 			/*
 			 * If this entry has been set to a capital account entry then the
 			 * income and expense account fields must be blank and we return
 			 * null to indicate this, otherwise the income and expense account
 			 * properties are associated with this entry.
 			 */
 			if (entry.getAccount() == null
 					|| entry.getAccount() instanceof IncomeExpenseAccount) {
 				return entry;
 			} else {
 				return null;
 			}
 		}
 		
 		public Entry getEntryForCommon1Fields() {
 			// If the column contains an entry property that applies to both capital and income and expense
 			// accounts, and (in the simple transaction case) displays the property from the capital account,
 			// then the column is always blank in the child rows (we use common2 in preference).
 			return null;
 		}
 		
 		public Entry getEntryForCommon2Fields() {
 			/*
 			 * If the column contains an entry property that applies to both
 			 * capital and income and expense accounts, then in child rows it
 			 * will always contain the property from the entry. There is a
 			 * single exception. If this is a double entry transaction (a
 			 * transaction representing a transfer between two capital accounts)
 			 * then there will be a child row, but the account for the other
 			 * entry is put in the top level row. This means the account should
 			 * not be put in the child row
 			 */
 			if (getDisplayableTransaction().otherEntries.size() == 1
 					&& ((Entry)getDisplayableTransaction().otherEntries.firstElement()).getAccount() instanceof CapitalAccount) {
 				return null;
 			} else {
 				return entry;
 			}
 		}
 		
 		/* (non-Javadoc)
 		 * @see net.sf.jmoney.ui.internal.pages.account.capital.IDisplayableItem#getAmount()
 		 */
 		public long getAmount() {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		public Transaction getTransaction() {
 			return transactionData.getTransaction();
 		}
 
 		public boolean isBalanceAffected() {
 			// The amounts on the split-entry fields do not affect the
 			// balance shown in the balance column.
 			return false;
 		}
 
 		public long getBalance() {
 			throw new RuntimeException("");
 		}
 
 		public Entry getEntryInAccount() {
 			DisplayableTransaction transactionData = getDisplayableTransaction();
 			return transactionData.getEntryForAccountFields();
 		}
 
 		public Entry getEntryForThisRow() {
 			return entry;
 		}
 	}
 
 	/**
 	 * This class is to be used for new entries which are currently filled in by the
 	 * user, before they are comited and transformed in real entries
 	 * @author Faucheux
 	 */
 	class DisplayableNewEmptyEntry implements IDisplayableItem {
 
 		public DisplayableNewEmptyEntry() {
 			// 	constructor to define a new, empty entry which we be filled in by the user
 		}
 
 		/* (non-Javadoc)
 		 * @see net.sf.jmoney.ui.internal.pages.account.capital.IDisplayableItem#getTransaction()
 		 */
 		public Transaction getTransactionForTransactionFields() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		/* (non-Javadoc)
 		 * @see net.sf.jmoney.ui.internal.pages.account.capital.IDisplayableItem#getEntryForAccountFields()
 		 */
 		public Entry getEntryForAccountFields() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		/* (non-Javadoc)
 		 * @see net.sf.jmoney.ui.internal.pages.account.capital.IDisplayableItem#getEntryForOtherFields()
 		 */
 		public Entry getEntryForOtherFields() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public Entry getEntryForCommon1Fields() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 		
 		public Entry getEntryForCommon2Fields() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 		
 		/* (non-Javadoc)
 		 * @see net.sf.jmoney.ui.internal.pages.account.capital.IDisplayableItem#getAmount()
 		 */
 		public long getAmount() {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		public Transaction getTransaction() {
 			return null;
 		}
 
 		/* (non-Javadoc)
 		 * @see net.sf.jmoney.ui.internal.pages.account.capital.IDisplayableItem#isBalanceAffected()
 		 */
 		public boolean isBalanceAffected() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		/* (non-Javadoc)
 		 * @see net.sf.jmoney.ui.internal.pages.account.capital.IDisplayableItem#getBalance()
 		 */
 		public long getBalance() {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		/* (non-Javadoc)
 		 * @see net.sf.jmoney.pages.entries.IDisplayableItem#getEntryInAccount()
 		 */
 		public Entry getEntryInAccount() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		/* (non-Javadoc)
 		 * @see net.sf.jmoney.pages.entries.IDisplayableItem#getSelectedEntry()
 		 */
 		public Entry getEntryForThisRow() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 	}
 
 	/**
 	 * Refresh the text only.
 	 * This method is used when text may have changed but the items
 	 * in the table have not changed.
 	 */
 	private void refreshLabels() {
 		for (int i = 0; i < fTable.getItemCount(); i++) {
 			TreeItem parentItem = fTable.getItem(i);
 			updateItem(parentItem);
 			
 			// Update the children too
 			for (int j = 0; j < parentItem.getItemCount(); j++) {
 				TreeItem childItem = parentItem.getItem(j);
 				updateItem(childItem);
 			}
 		}
 	}
 
 	/**
 	 * Refresh the list of entries.
 	 */
 	public void refreshEntryList() {
 		buildEntryList();
 		setTableItems();
 	}
 
 	public void dispose() {
 		fTable.dispose();
 	}
 
 	/*
 	 * Note that the transaction may have been inserted from within
 	 * this page (the user pressed the 'new transaction' button in
 	 * this page, or the transaction may have been inserted outside
 	 * this page (for example, the user imported some transactions
 	 * from a QIF file).  This method is responsible for inserting
 	 * the new data into the entries list.  It must not set the
 	 * current selection.  If the transaction is being inserted because
 	 * the user pressed the 'new transaction' button then the action
 	 * code for that button will set the transaction as the selection
 	 * after this method has created the table rows for the transaction.
 	 */
 	public void addEntryInAccount(Entry entry) {
 		DisplayableTransaction dTrans = new DisplayableTransaction(entry, 0);
 
 		// TODO: check that caller ensures this entry is in the entries content provider.
 		
 		// Do not add this entry to our view if a filter is on and the entry does
 		// not match the filter.
 		if (!matchesFilter(dTrans)) {
 			return;
 		}
 
 		entries.add(dTrans);
 
 		// Scan the table to find the correct index to insert this row.
 		// Because rows are likely to be inserted near the bottom of
 		// the table, we scan backwards.
 		Comparator rowComparator = new RowComparator(sortColumn, sortAscending);
 		int parentIndex = 0;
 		long balance = entriesContent.getStartBalance();
 		for (int i = fTable.getItemCount()-1; i >= 0; i--) {
 			TreeItem item = fTable.getItem(i);
 			Object data = item.getData();
 			if (data instanceof DisplayableTransaction) {
 				DisplayableTransaction dTrans2 = (DisplayableTransaction) data;
 				if (rowComparator.compare(dTrans, dTrans2) >= 0) {
 					parentIndex = i + 1;
 					balance = dTrans2.getBalance();
 					break;
 				}
 			}
 		}
 		
 		// Insert the transaction and its entries now.
 		TreeItem parentItem = new TreeItem(fTable, 0, parentIndex);
 		parentItem.setData(dTrans);
 		
 		// Set the column values for this new row (note that the balance
 		// column is not set.  The balance column is always set
 		// later by the same code that updates all the following balances).
 		for (int columnIndex = 0; columnIndex < fTable.getColumnCount(); columnIndex++) {
 			IEntriesTableProperty p = (IEntriesTableProperty)fTable.getColumn(columnIndex).getData();
 			if (!p.getId().equals("balance")) {
 				parentItem.setText(columnIndex, p.getValueFormattedForTable(dTrans));
 			}
 		}
 
 		if (!dTrans.isSimpleEntry()) {
 			/*
 			 * Case of an splitted or double entry. We display the transaction
 			 * on the first line and the entries of the transaction on the
 			 * following ones. However, the transaction line also holds the
 			 * properties for the entry in this account, so display just the
 			 * other entries underneath.
 			 */
 			Iterator itSubEntries = dTrans.getSplitEntryIterator();
 			while (itSubEntries.hasNext()) {
 				Entry entry2 = (Entry) itSubEntries.next();
 				DisplayableEntry entryData = new DisplayableEntry(entry2, dTrans);
 				TreeItem childItem = new TreeItem(parentItem, SWT.NULL);
 				childItem.setData(entryData);
 				updateItem(childItem);
 			}
 		}
 
 		// Recalculate balances from this point onwards.
 		updateBalances(parentIndex, balance);
 
 		// Set colors from this point onwards (colors have switched).
 		boolean isAlternated = ((parentIndex % 2) == 0);
 		updateColors(parentIndex, isAlternated);
 	}
 
 	/**
 	 * Remove a parent entry from the table.
 	 * <P>
 	 * The caller will alread (non-Javadoc)
 	 * @see net.sf.jmoney.pages.entries.IEntriesControl#removeTransaction(net.sf.jmoney.model2.Entry)
 	 */
 	private void removeEntryInAccount(Entry entry) {
 		// TODO: processing is done correctly for entries being added and removed from
 		// the set provided by entriesContent.  However, the filter is not kept correctly.
 		// This is a problem, because the user may edit properties that make the entry
 		// disappear because it no longer passes the filter.  How do we deal with this?
 		
 		// get the index of the row for the transaction
 		int parentIndex = lookupEntryInAccount(entry);
 
 		TreeItem parentItem = fTable.getItem(parentIndex);
 		DisplayableTransaction dTrans = (DisplayableTransaction)parentItem.getData();
 
 		// Remove from our cached set of entries
 		entries.remove(dTrans);
 		
 		// Determine the color of this row.  It is used to switch the color of
 		// all the following rows.
 		boolean isAlternated = (alternateTransactionColor.equals(parentItem
 				.getBackground()));
 
 		
 		// Dispose it
 		parentItem.dispose();
 
 		long balance = dTrans.getBalance() - entry.getAmount();
 
 		// Recalculate balances from this point onwards.
 		updateBalances(parentIndex, balance);
 
 		// Set colors from this point onwards (colors have switched).
 		updateColors(parentIndex, isAlternated);
 	}
 
 	public void updateEntry(Entry entryInAccount, Entry changedEntry,
 			PropertyAccessor changedProperty, Object oldValue, Object newValue) {
 		int parentIndex = lookupEntryInAccount(entryInAccount);
 		JMoneyPlugin.myAssert(parentIndex >= 0);
 		TreeItem parentItem = fTable.getItem(parentIndex);
 		
 		// If there are two entries in the transaction
 		// and the changed entry in not the listed entry
 		// and the changed property was the account property
 		// and the account changed from being a capital account to an income and expense account
 		// or vica versa
 		// then we must add or remove a child item.
 		DisplayableTransaction dTrans = (DisplayableTransaction)parentItem.getData(); 
 		if (dTrans.otherEntries.size() == 1
 				&& !changedEntry.equals(entryInAccount)
 				&& changedProperty == EntryInfo.getAccountAccessor()) {
 			
 			if ((oldValue instanceof CapitalAccount)
 					&& !(newValue instanceof CapitalAccount)) {
 				// Remove the single child item.
 				parentItem.getItem(0).dispose();
 			}
 			if (!(oldValue instanceof CapitalAccount)
 					&& (newValue instanceof CapitalAccount)) {
 				// Add a child item.Remove the single child item.
 				TreeItem childItem = new TreeItem(parentItem, SWT.NULL);
 				childItem.setData(new DisplayableEntry(changedEntry, dTrans));
 
 				Color colorOfNewEntry = parentItem.getBackground().equals(
 						transactionColor) ? entryColor : alternateEntryColor;
 				childItem.setBackground(colorOfNewEntry);
 			}
 		}
 		
 		// If this is a double entry transaction then changing the other entry
 		// may affect the top-level row (the account, for example, is displayed
 		// in the top-level row).
 		updateItem(parentItem);
 
 		// Update the row containing the changed entry.  This may be
 		// the parent row, in which case the parent row is updated
 		// twice, but that is okay.
 		TreeItem item = lookupSplitEntry(parentItem, changedEntry);
 		updateItem(item);
 
 		// If the changed property is the sort property, the entry may
 		// need to be moved in the sort order.  This is difficult to do
 		// properly, as we do not want to mess up the user while the user
 		// is editing the row.  To move a row requires the row item to be
 		// disposed and a new row item inserted at the new position.
 		// The easiest way may be to see if this is the selected row, 
 		// and if it is, move all the rows past this row.
 		// Until this is implemented, we leave the row out of order. 
 
 		// Recalculate balances from this point onwards.
 		if (changedProperty == EntryInfo.getAmountAccessor()
 				&& entryInAccount.equals(changedEntry)) {
 
 			// Determine the balance prior to this entry.  This is most easily done
 			// by deducting the old amount of this entry from the old balance.
 			long balance = dTrans.getBalance() - ((Long) oldValue).longValue();
 
 			updateBalances(parentIndex, balance);
 		}
 	}
 
 	/**
 	 * This method is called when a transaction property is changed.
 	 * This method is called once for each entry in the transaction that
 	 * is a top level listed entry.
 	 */
 	public void updateTransaction(Entry entryInAccount,
 			PropertyAccessor changedProperty, Object oldValue, Object newValue) {
 		int parentIndex = lookupEntryInAccount(entryInAccount);
 		JMoneyPlugin.myAssert(parentIndex >= 0);
 		TreeItem parentItem = fTable.getItem(parentIndex);
 		updateItem(parentItem);
 
 		// If the changed property is the sort property, the entry may
 		// need to be moved in the sort order.  This is difficult to do
 		// properly, as we do not want to mess up the user while the user
 		// is editing the row.  To move a row requires the row item to be
 		// disposed and a new row item inserted at the new position.
 		// The easiest way may be to see if this is the selected row, 
 		// and if it is, move all the rows past this row.
 		// Until this is implemented, we leave the row out of order. 
 	}
 
 	public int lookupEntryInAccount(Entry entryInAccount) {
 		// Find this entry.  We scan backwards on the assuption
 		// that most changes occur at the bottom of the table.
 		for (int i = fTable.getItemCount()-1; i >= 0; i--) {
 			TreeItem item = fTable.getItem(i);
 			Object data = item.getData();
 			if (data instanceof DisplayableTransaction) {
 				DisplayableTransaction dTrans = (DisplayableTransaction) data;
 				if (dTrans.getEntryForAccountFields().equals(entryInAccount)) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 
 	public TreeItem lookupSplitEntry(TreeItem parentItem, Entry entry) {
 		// See if this entry is represented by the top level row
 		DisplayableTransaction dTrans = (DisplayableTransaction)parentItem.getData();
 		if (entry.equals(dTrans.getEntryForAccountFields())
 				|| entry.equals(dTrans.getEntryForOtherFields())) {
 			return parentItem;
 		}
 		
 		for (int i = 0; i < parentItem.getItemCount(); i++) {
 			TreeItem item = parentItem.getItem(i);
 			DisplayableEntry dEntry = (DisplayableEntry)item.getData();
 			if (dEntry.getEntryForThisRow().equals(entry)) {
 				return item;
 			}
 		}
 		
 		throw new RuntimeException("");
 	}
 
 	private void updateItem(TreeItem item) {
 		Object obj = item.getData();
 		if (obj instanceof IDisplayableItem) {
 			for (int index = 0; index < fTable.getColumnCount(); index++) {
 				IEntriesTableProperty entryData = (IEntriesTableProperty) (fTable
 						.getColumn(index).getData());
 				item.setText(index, entryData
 						.getValueFormattedForTable((IDisplayableItem) obj));
 			}
 		}
 	}
 
 	/**
 	 * Adjust the balances.
 	 * 
 	 * @param i the index at which we start looking for DisplayableTransaction
 	 * 				rows.  All DisplayableTransaction rows at this index and
 	 * 				following are adjusted.
 	 * @param balance the balance prior to the first DisplayableTransaction found
 	 * 				at or after the given index
 	 */
 	private void updateBalances(int i, long balance) {
 		if (entriesContent.getBalanceColumnManager() == null) {
 			// No balance column is being displayed
 			return;
 		}
 		
 		// The balance column is always the last column
 		int balanceColumnIndex = fTable.getColumnCount() - 1;
 		
 		for (; i < fTable.getItemCount(); i++) {
 			TreeItem item = fTable.getItem(i);
 			Object data = item.getData();
 			if (data instanceof DisplayableTransaction) {
 				DisplayableTransaction dTrans = (DisplayableTransaction) item
 						.getData();
 
 				balance += dTrans.getEntryForAccountFields().getAmount();
 				dTrans.setBalance(balance);
 
 				item.setText(balanceColumnIndex, entriesContent
 						.getBalanceColumnManager().getValueFormattedForTable(
 								dTrans));
 			}
 		}
 	}
 
 	private void updateColors(int i, boolean isAlternated) {
 		for (; i < fTable.getItemCount(); i++) {
 			TreeItem item = fTable.getItem(i);
 
 			isAlternated = !isAlternated;
 
 			if (isAlternated) {
 				item.setBackground(alternateTransactionColor);
 			} else {
 				item.setBackground(transactionColor);
 			}
 
 			// Process the child items
 			for (int j = 0; j < item.getItemCount(); j++) {
 				TreeItem childItem = item.getItem(j);
 
 				if (isAlternated) {
 					childItem.setBackground(alternateEntryColor);
 				} else {
 					childItem.setBackground(entryColor);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Set the currently selected row.
 	 * <P>
 	 * This method not only updates the selection in the control, but
 	 * also updates the <code>previouslySelectedItem</code> field
 	 * and fires the selection change event that updates the entry properties
 	 * in the entry section below.
 	 */
 	public void setSelection(Entry entryInAccount, Entry entryToSelect) {
 		int index = lookupEntryInAccount(entryInAccount);
 		JMoneyPlugin.myAssert(index >= 0);
 		TreeItem item = lookupSplitEntry(fTable.getItem(index), entryToSelect);
 		JMoneyPlugin.myAssert(item != null);
 		fTable.setSelection(new TreeItem [] {item});
 		
 		// Setting the selection from code does not fire the selection listener.
 		// We must therefore process this ourselves.
 		previouslySelectedItem = item;
 		fireSelectionChanges((IDisplayableItem)item.getData());
 	}
 
 	/**
 	 * Update the view to show the insertion of a new child
 	 * entry to a given top-level entry.
 	 * This includes updating the cached list of entries
 	 * in the DisplayableTransaction object.
 	 */
 	public void addEntry(Entry entryInAccount, Entry newEntry) {
 		int parentIndex = lookupEntryInAccount(entryInAccount);
 		JMoneyPlugin.myAssert(parentIndex >= 0);
 		TreeItem parentItem = fTable.getItem(parentIndex);
 
 		Transaction transaction = entryInAccount.getTransaction();
 		DisplayableTransaction dTrans = (DisplayableTransaction)parentItem.getData();
 		
 		// Update the list of child entries in the
 		// DisplayableTransaction object.
 		dTrans.otherEntries.add(newEntry);
 		
 		Color colorOfNewEntry = parentItem.getBackground().equals(
 				transactionColor) ? entryColor : alternateEntryColor;
 
 		// If there were no DisplayableEntry rows then this was a
 		// simple transaction.  It can no longer be a simple transaction
 		// so add a row for entry that had been combined.  We also need
 		// to update the combined row.
 
 		if (parentItem.getItemCount() == 0) {
 			// The transaction should have three entries,
 			// newEntry, entryInAccount, and one other.
 			// We need to find the other.
 			Entry otherEntry = null;
 			for (Iterator iter = transaction.getEntryCollection().iterator(); iter
 					.hasNext();) {
 				Entry entry = (Entry) iter.next();
 				if (!entry.equals(entryInAccount) && !entry.equals(newEntry)) {
 					if (otherEntry != null) {
 						throw new RuntimeException("internal inconsistency");
 					}
 					otherEntry = entry;
 				}
 			}
 
 			// Create row for entry that was combined.
 			DisplayableEntry dEntry = new DisplayableEntry(otherEntry, dTrans);
 			TreeItem newItem = new TreeItem(parentItem, SWT.NONE);
 			newItem.setData(dEntry);
 			updateItem(newItem);
 			newItem.setBackground(colorOfNewEntry);
 
 			// Update the transaction row.
 			updateItem(parentItem);
 		}
 
 		DisplayableEntry dEntry = new DisplayableEntry(newEntry, dTrans);
 		TreeItem newItem = new TreeItem(parentItem, SWT.NONE);
 		newItem.setData(dEntry);
 		updateItem(newItem);
 		newItem.setBackground(colorOfNewEntry);
 	}
 
 	/**
 	 * Update the view to show the deletion of a child
 	 * entry to a given top-level entry.
 	 * This includes updating the cached list of entries
 	 * in the DisplayableTransaction object.
 	 */
 	public void removeEntry(Entry entryInAccount, Entry deletedEntry) {
 		// get the index of the row for the transaction
 		int transIndex = lookupEntryInAccount(entryInAccount);
 
 		TreeItem parentItem = fTable.getItem(transIndex);
 		DisplayableTransaction dTrans = (DisplayableTransaction) parentItem
 				.getData();
 
 		// Update the list of child entries in the
 		// DisplayableTransaction object.
 		dTrans.otherEntries.remove(deletedEntry);
 		
 		// get the child row item for the entry
 		TreeItem childItem = lookupSplitEntry(parentItem, deletedEntry);
 
 		// Dispose it
 		childItem.dispose();
 
 		// If the transaction is now a simple transaction,
 		// dispose the remaining item, and update the transaction
 		// row.
 		if (dTrans.isSimpleEntry()) {
 			JMoneyPlugin.myAssert(parentItem.getItemCount() == 1);
 			parentItem.getItem(0).dispose();
 			updateItem(parentItem);
 		}
 	}
 
 	/**
 	 * Add a selection listener.  The listener will be notified whenever
 	 * the selected row in the tree changes.
 	 * <P>
 	 * The event will contain the IDisplayableItem object as data.
 	 */
 	public void addSelectionListener(EntryRowSelectionListener tableSelectionListener) {
 		// We do not add the listener directly to the tree control.
 		// Instead we keep our own list of listeners and we add our
 		// own listener to the tree control.  When our own listener is
 		// notified of a change in selection, the listener in turn
 		// notifies the listeners added thru this method.
 		
 		// This approach is necessary because not all selection changes
 		// the tree should be passed on to the listeners added thru this
 		// method.  The selection change may be ignored if, for example,
 		// there is an error in the data on the previous row.  Also,
 		// when a selection change is initiated by the code from within
 		// a listener, SWT does not seem to generate a SelectionListener
 		// event, but we want one generated and by keeping our own list
 		// of listeners, we can do that.
 		
 		selectionListeners.add(tableSelectionListener);
 	}
 
 	/**
 	 * A private utility method for notifying our listeners of
 	 * a selection change event.
 	 * 
 	 * @param event the event to be passed on to the listeners
 	 */
 	private void fireSelectionChanges(IDisplayableItem data) {
 		for (Iterator iter = selectionListeners.iterator(); iter.hasNext(); ) {
 			EntryRowSelectionListener listener = (EntryRowSelectionListener)iter.next();
 			listener.widgetSelected(data);
 		}
 	}
 
 	private void fireRowDefaultSelection(IDisplayableItem data) {
 		for (Iterator iter = selectionListeners.iterator(); iter.hasNext(); ) {
 			EntryRowSelectionListener listener = (EntryRowSelectionListener)iter.next();
 			listener.widgetDefaultSelected(data);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.jmoney.pages.entries.IEntriesControl#getControl()
 	 */
 	public Control getControl() {
 		return fTable;
 	}
 
 	private void showMessage (String message) {
 		MessageDialog dialog = new MessageDialog(
 				fTable.getShell(),
 				"Disabled Action Selected",
 				null, // accept the default window icon
 				message,
 				MessageDialog.WARNING,
 				new String[] { IDialogConstants.OK_LABEL }, 0);
 		dialog.open();
 	}
 	
 	private class RowComparator implements Comparator {
 		private IEntriesTableProperty sortProperty;
 		private boolean ascending;
 		
 		RowComparator(TreeColumn sortColumn, boolean ascending) {
 			this.sortProperty = (IEntriesTableProperty)sortColumn.getData();
 			this.ascending = ascending;
 		}
 		
 		public int compare(Object obj1, Object obj2) {
 			DisplayableTransaction dTrans1 = (DisplayableTransaction) obj1;
 			DisplayableTransaction dTrans2 = (DisplayableTransaction) obj2;
 			int result = sortProperty.compare(dTrans1, dTrans2);
 			return ascending ? result : -result;
 		}
 	}
 }
