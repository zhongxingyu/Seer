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
 
 package net.sf.jmoney.reconciliation.reconcilePage;
 
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import net.sf.jmoney.model2.Currency;
 import net.sf.jmoney.model2.CurrencyAccount;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.EntryInfo;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.ScalarPropertyAccessor;
 import net.sf.jmoney.model2.SessionChangeAdapter;
 import net.sf.jmoney.reconciliation.BankStatement;
 import net.sf.jmoney.reconciliation.IReconciliationQueries;
 import net.sf.jmoney.reconciliation.ReconciliationEntryInfo;
 
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.ui.forms.SectionPart;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 
 /**
  * Implementation of the 'Statements' section of the reconciliation
  * page.  This section lists all the statements in the account that
  * have reconciled entries in them.
  * 
  * @author Nigel Westbury
  */
 public class StatementsSection extends SectionPart {
 	
 	protected Table statementTable;
 	
 	protected StatementContentProvider contentProvider;
 	
 	private TableViewerColumn balanceColumn;
 	
 	public StatementsSection(Composite parent, FormToolkit toolkit, CurrencyAccount account) {
 		super(parent, toolkit, Section.DESCRIPTION | Section.TITLE_BAR);		
 		getSection().setText("Statements");
 		getSection().setDescription("Double click a statement to show that statement.");
 
 		final Currency currencyForFormatting = account.getCurrency();
 
 		Composite composite = new Composite(getSection(), SWT.NONE);
 		composite.setLayout(new GridLayout());
 		
 		statementTable = new Table(composite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL);
 		GridData gdTable = new GridData(SWT.FILL, SWT.FILL, true, true);
 		gdTable.heightHint = 100;
 		statementTable.setLayoutData(gdTable);
 		
 		statementTable.setHeaderVisible(true);
 		statementTable.setLinesVisible(true);
 		
 		// Create and setup the TableViewer
 		TableViewer tableViewer = new TableViewer(statementTable);   
 		tableViewer.setUseHashlookup(true);
 		
 		// Add the columns
 		TableViewerColumn statementColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
 		statementColumn.getColumn().setWidth(65);
 		statementColumn.getColumn().setText("Statement");
 		statementColumn.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				StatementDetails statementDetails = (StatementDetails)element;
 				return statementDetails.statement.toLocalizedString();  
 			}
 		});
 
 		balanceColumn = new TableViewerColumn(tableViewer, SWT.RIGHT);
 		balanceColumn.getColumn().setWidth(70);
 		balanceColumn.getColumn().setText("Balance");
 		balanceColumn.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				StatementDetails statementDetails = (StatementDetails)element;
 				return currencyForFormatting.format(statementDetails.getClosingBalance());
 			}
 		});
 		
 		contentProvider = new StatementContentProvider(tableViewer);
 		
 		tableViewer.setContentProvider(contentProvider);
 		tableViewer.setComparator(new StatementViewerComparator());
 		tableViewer.setInput(account);
 
 		/*
 		 * Scroll the statement list to the bottom so that the most recent
		 * statements are shown.
 		 */
		tableViewer.reveal(contentProvider.getLastStatement());
 		
 		getSection().setClient(composite);
 		toolkit.paintBordersFor(composite);
 		refresh();
 	}
 	
 	class StatementContentProvider implements IStructuredContentProvider {
 		/**
 		 * The table viewer to be notified whenever the content changes.
 		 */
 		TableViewer tableViewer;
 		
 		CurrencyAccount account;
 		
 		SortedMap<BankStatement, StatementDetails> statementDetailsMap;
 
 		StatementContentProvider(TableViewer tableViewer) {
 			this.tableViewer = tableViewer;
 		}
 		
 		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
 			account = (CurrencyAccount)newInput;
 			
 			// Build a tree map of the statement totals
 			
 			// We use a tree map in preference to the more efficient
 			// hash map because we can then fetch the results in order.
 			statementDetailsMap = new TreeMap<BankStatement, StatementDetails>();
 			
 			// When this item is disposed, the input may be set to null.
 			// Return an empty list in this case.
 			if (newInput == null) {
 				return;
 			}
 			
 			IReconciliationQueries queries = (IReconciliationQueries)account.getSession().getAdapter(IReconciliationQueries.class);
 			if (queries != null) {
 				// TODO: change this method
 				//return queries.getStatements(fPage.getAccount());
 			} else {
 				// IReconciliationQueries has not been implemented in the datastore.
 				// We must therefore provide our own implementation.
 				
 				// We use a tree map in preference to the more efficient
 				// hash map because we can then fetch the results in order.
 				SortedMap<BankStatement, Long> statementTotals = new TreeMap<BankStatement, Long>();
 				
 				for (Entry entry: account.getEntries()) {
 					BankStatement statement = entry .getPropertyValue(ReconciliationEntryInfo.getStatementAccessor());
 					
 					if (statement != null) {
 						Long statementTotal = statementTotals.get(statement);
 						if (statementTotal == null) {
 							statementTotal = new Long(0);
 						}
 						statementTotals.put(statement, new Long(statementTotal.longValue() + entry.getAmount()));
 					}
 				}
 				
 				long balance = account.getStartBalance();
 				for (Map.Entry<BankStatement, Long> mapEntry: statementTotals.entrySet()) {
 					BankStatement statement = mapEntry.getKey();
 					long totalEntriesOnStatement = (mapEntry.getValue()).longValue();
 					
 					statementDetailsMap.put(
 							statement,
 							new StatementDetails(
 									statement,
 									balance,
 									totalEntriesOnStatement)
 					);
 					balance += totalEntriesOnStatement;
 				}
 			}
 
 			// Listen for changes so we can keep the tree map up to date.
 			account.getDataManager().addChangeListener(new SessionChangeAdapter() {
 				@Override
 				public void objectCreated(ExtendableObject newObject) {
 					if (newObject instanceof Entry) {
 						Entry newEntry = (Entry)newObject;
 						if (account.equals(newEntry.getAccount())) {
 							BankStatement statement = newEntry.getPropertyValue(ReconciliationEntryInfo.getStatementAccessor());
 							adjustStatement(statement, newEntry.getAmount());
 						}
 					}
 				}
 				
 				@Override
 				public void objectDestroyed(ExtendableObject deletedObject) {
 					if (deletedObject instanceof Entry) {
 						Entry deletedEntry = (Entry)deletedObject;
 						if (account.equals(deletedEntry.getAccount())) {
 							BankStatement statement = deletedEntry.getPropertyValue(ReconciliationEntryInfo.getStatementAccessor());
 							adjustStatement(statement, -deletedEntry.getAmount());
 						}
 					}
 				}
 				
 				@Override
 				public void objectChanged(ExtendableObject changedObject, ScalarPropertyAccessor changedProperty, Object oldValue, Object newValue) {
 					if (changedObject instanceof Entry) {
 						Entry entry = (Entry)changedObject;
 						
 						if (changedProperty == EntryInfo.getAccountAccessor()) {
 							if (account.equals(oldValue)) {
 								BankStatement statement = entry.getPropertyValue(ReconciliationEntryInfo.getStatementAccessor());
 								adjustStatement(statement, -entry.getAmount());
 							}
 							if (account.equals(newValue)) {
 								BankStatement statement = entry.getPropertyValue(ReconciliationEntryInfo.getStatementAccessor());
 								adjustStatement(statement, entry.getAmount());
 							}
 						} else {
 							if (account.equals(entry.getAccount())) {
 								if (changedProperty == EntryInfo.getAmountAccessor()) {
 									BankStatement statement = entry.getPropertyValue(ReconciliationEntryInfo.getStatementAccessor());
 									long oldAmount = ((Long)oldValue).longValue();
 									long newAmount = ((Long)newValue).longValue();
 									adjustStatement(statement, newAmount - oldAmount);
 								} else if (changedProperty == ReconciliationEntryInfo.getStatementAccessor()) {
 									adjustStatement((BankStatement)oldValue, -entry.getAmount());
 									adjustStatement((BankStatement)newValue, entry.getAmount());
 								}
 							}
 						}
 					}
 				}
 			}, statementTable);
 		}
 		
 		/**
 		 * @param statement the statement to be adjusted.  This parameter
 		 * 				may be null in which case this method does nothing
 		 * @param amount the amount by which the total for the given statement
 		 * 				is to be adjusted
 		 */
 		void adjustStatement(BankStatement statement, long amount) {
 			if (statement != null) {
 				StatementDetails thisStatementDetails = statementDetailsMap.get(statement);
 				if (thisStatementDetails == null) {
 					
 					long openingBalance;
 					SortedMap<BankStatement, StatementDetails> priorStatements = statementDetailsMap.headMap(statement);
 					if (priorStatements.isEmpty()) {
 						openingBalance = account.getStartBalance();
 					} else {
 						openingBalance = priorStatements.get(priorStatements.lastKey()).getClosingBalance();
 					}
 					
 					thisStatementDetails = new StatementDetails(
 							statement,
 							openingBalance,
 							amount);
 					
 					statementDetailsMap.put(statement, thisStatementDetails);
 
 					// Notify the viewer that we have a new item
 					tableViewer.add(thisStatementDetails);
 				} else {
 					thisStatementDetails.adjustEntriesTotal(amount);
 					
 					// Notify the viewer that an item has changed
 					tableViewer.update(thisStatementDetails, null);
 				}
 				
 				// This total affects all later balances.
 				
 				// Iterate through all later statements updating the
 				// statement details and then notify the viewer of the
 				// update.  Note that tailMap returns a collection that
 				// includes the starting key, so we must be sure not to
 				// process that.
 				SortedMap<BankStatement, StatementDetails> laterStatements = statementDetailsMap.tailMap(statement);
 				for (StatementDetails statementDetails: laterStatements.values()) {
 					if (!statementDetails.statement.equals(statement)) {
 						statementDetails.adjustOpeningBalance(amount);
 						
 						// Notify the viewer that an item has changed
 						tableViewer.update(statementDetails, null);
 					}
 				}
 			}
 		}
 		
 		public void dispose() {
 		}
 		
 		public Object[] getElements(Object parent) {
 			// Return an array of the statements.  These are already ordered.
 			return statementDetailsMap.values().toArray();
 		}
 
 		/**
 		 * @return
 		 */
 		public StatementDetails getLastStatement() {
 			if (this.statementDetailsMap.isEmpty()) {
 				return null;
 			} else {
 				return statementDetailsMap.get(statementDetailsMap.lastKey());
 			}
 		}
 	}
 	
 	/**
 	 * Even though the content provider supplies the statements in order, we
 	 * still need to set a sorter into the table. The reason is that new
 	 * statements will otherwise always be added at the end and it is possible,
 	 * though unlikely, that a user will go back and add earlier statements.
 	 */
 	class StatementViewerComparator extends ViewerComparator {
 		@Override
 	    public int compare(Viewer viewer, Object element1, Object element2) {
 	        StatementDetails statementDetails1 = (StatementDetails)element1;
 	        StatementDetails statementDetails2 = (StatementDetails)element2;
 	        return statementDetails1.compareTo(statementDetails2);
 	    }
 	}
 	
 	/**
 	 * @param show
 	 */
 	public void showBalance(boolean show) {
 		if (show) {
 			balanceColumn.getColumn().setWidth(70);
 		} else {
 			balanceColumn.getColumn().setWidth(0);
 		}
 	}
 	
 	/**
 	 * Returns the last statement in the list of statements for
 	 * the account.  This is used to determine default values when
 	 * the user creates a new statement and also the starting
 	 * balance of any newly created statement.
 	 * 
 	 * @return the last statement, or null if no statements have
 	 * 				yet been created in the account
 	 */
 	public StatementDetails getLastStatement() {
 		return contentProvider.getLastStatement();
 	}
 
 	/**
 	 * Listen for changes in the selected statement.
 	 */
 	public void addSelectionListener(SelectionAdapter listener) {
 		// Pass thru the request to the table control.
 		statementTable.addSelectionListener(listener);
 	}	
 }
