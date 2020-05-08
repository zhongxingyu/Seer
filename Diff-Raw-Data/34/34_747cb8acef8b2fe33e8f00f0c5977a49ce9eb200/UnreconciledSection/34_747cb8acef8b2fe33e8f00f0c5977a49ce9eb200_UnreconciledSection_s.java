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
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Vector;
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.entrytable.BalanceColumn;
 import net.sf.jmoney.entrytable.BaseEntryRowControl;
 import net.sf.jmoney.entrytable.Block;
 import net.sf.jmoney.entrytable.ButtonCellControl;
 import net.sf.jmoney.entrytable.CellBlock;
 import net.sf.jmoney.entrytable.DebitAndCreditColumns;
 import net.sf.jmoney.entrytable.EntriesTable;
 import net.sf.jmoney.entrytable.EntryData;
 import net.sf.jmoney.entrytable.EntryRowControl;
 import net.sf.jmoney.entrytable.HorizontalBlock;
import net.sf.jmoney.entrytable.ICellControl;
 import net.sf.jmoney.entrytable.IEntriesContent;
 import net.sf.jmoney.entrytable.IRowProvider;
 import net.sf.jmoney.entrytable.ISplitEntryContainer;
 import net.sf.jmoney.entrytable.IndividualBlock;
 import net.sf.jmoney.entrytable.OtherEntriesButton;
 import net.sf.jmoney.entrytable.PropertyBlock;
 import net.sf.jmoney.entrytable.ReusableRowProvider;
 import net.sf.jmoney.entrytable.RowControl;
 import net.sf.jmoney.entrytable.RowSelectionTracker;
 import net.sf.jmoney.entrytable.SingleOtherEntryPropertyBlock;
 import net.sf.jmoney.isolation.TransactionManager;
 import net.sf.jmoney.isolation.UncommittedObjectKey;
 import net.sf.jmoney.model2.Currency;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.EntryInfo;
 import net.sf.jmoney.model2.Transaction;
 import net.sf.jmoney.model2.TransactionInfo;
 import net.sf.jmoney.reconciliation.BankStatement;
 import net.sf.jmoney.reconciliation.ReconciliationEntryInfo;
 import net.sf.jmoney.reconciliation.ReconciliationPlugin;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.util.LocalSelectionTransfer;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSource;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.DragSourceListener;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.forms.SectionPart;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 
 /**
  * Class implementing the section containing the unreconciled
  * entries on the account reconciliation page.
  * 
  * @author Nigel Westbury
  */
 public class UnreconciledSection extends SectionPart {
 
 	ReconcilePage fPage;
 
 	EntriesTable fUnreconciledEntriesControl;
 
 	FormToolkit toolkit;
 
 	IEntriesContent unreconciledTableContents = null;
 
 	ArrayList<CellBlock<EntryData, EntryRowControl>> cellList;
 
 	public UnreconciledSection(ReconcilePage page, Composite parent, RowSelectionTracker rowTracker) {
 		super(parent, page.getManagedForm().getToolkit(), Section.TITLE_BAR);
 		getSection().setText("Unreconciled Entries");
 		fPage = page;
 		this.toolkit = page.getManagedForm().getToolkit();
 
 		unreconciledTableContents = new IEntriesContent() {
 			public Collection<Entry> getEntries() {
 				/* The caller always sorts, so there is no point in us returning
 				 * sorted results.  It may be at some point we decide it is more
 				 * efficient to get the database to sort for us, but that would
 				 * only help the first time the results are fetched, it would not
 				 * help on a re-sort.  It also only helps if the database indexes
 				 * on the date.		
 				CurrencyAccount account = fPage.getAccount();
 		        Collection<Entry> accountEntries = 
 		        	account
 						.getSortedEntries(TransactionInfo.getDateAccessor(), false);
 				 */
 				Collection<Entry> accountEntries = fPage.getAccount().getEntries();
 
 				Vector<Entry> requiredEntries = new Vector<Entry>();
 				for (Entry entry: accountEntries) {
 					if (entry.getPropertyValue(ReconciliationEntryInfo.getStatementAccessor()) == null) {
 						requiredEntries.add(entry);
 					}
 				}
 
 				return requiredEntries;
 			}
 
 			public boolean isEntryInTable(Entry entry) {
 				// This entry is to be shown if the account
 				// matches and no statement is set.
 				BankStatement statement = entry.getPropertyValue(ReconciliationEntryInfo.getStatementAccessor());
 				return fPage.getAccount().equals(entry.getAccount())
 				&& statement == null;
 			}
 
 			public boolean filterEntry(EntryData data) {
 				// No filter here, so entries always match
 				return true;
 			}
 
 			public long getStartBalance() {
 				// TODO: figure out how we keep this up to date.
 				// The EntriesTree class has no mechanism for refreshing
 				// the opening balance.  It should have.
 				return 0;
 			}
 
 			public Entry createNewEntry(Transaction newTransaction) {
 				Entry entryInTransaction = newTransaction.createEntry();
 				Entry otherEntry = newTransaction.createEntry();
 
 				setNewEntryProperties(entryInTransaction);
 
 				// TODO: See if this code has any effect, and
 				// should this be here at all?
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
 				if (entryInTransaction.getCommodity() instanceof Currency) {
 					otherEntry.setIncomeExpenseCurrency((Currency)entryInTransaction.getCommodity());
 				}
 				
 				return entryInTransaction;
 			}
 			
 			private void setNewEntryProperties(Entry newEntry) {
 				// It is assumed that the entry is in a data manager that is a direct
 				// child of the data manager that contains the account.
 				TransactionManager tm = (TransactionManager)newEntry.getDataManager();
 				newEntry.setAccount(tm.getCopyInTransaction(fPage.getAccount()));
 			}
 		};
 
 		// Load the 'reconcile' indicator
 		URL installURL = ReconciliationPlugin.getDefault().getBundle().getEntry("/icons/reconcile.gif");
 		final Image reconcileImage = ImageDescriptor.createFromURL(installURL).createImage();
 		parent.addDisposeListener(new DisposeListener(){
 			public void widgetDisposed(DisposeEvent e) {
 				reconcileImage.dispose();
 			}
 		});
 		
 		CellBlock<EntryData, EntryRowControl> reconcileButton = new CellBlock<EntryData, EntryRowControl>(20, 0) {
 			@Override
			public ICellControl<EntryData> createCellControl(Composite parent, RowControl rowControl, final EntryRowControl coordinator) {
 				ButtonCellControl cellControl = new ButtonCellControl(parent, coordinator, reconcileImage, "Reconcile this Entry to the above Statement") {
 					@Override
 					protected void run(EntryRowControl rowControl) {
 						reconcileEntry(rowControl);
 					}
 				};
 
 				// Allow entries in the account to be moved from the unreconciled list
 				final DragSource dragSource = new DragSource(cellControl.getControl(), DND.DROP_MOVE);
 
 				// Provide data using a local reference only (can only drag and drop
 				// within the Java VM)
 				Transfer[] types = new Transfer[] { LocalSelectionTransfer.getTransfer() };
 				dragSource.setTransfer(types);
 
 				dragSource.addDragListener(new DragSourceListener() {
 					public void dragStart(DragSourceEvent event) {
 						Entry uncommittedEntry = coordinator.getUncommittedEntryData().getEntry();
 						UncommittedObjectKey uncommittedKey = (UncommittedObjectKey)uncommittedEntry.getObjectKey();
 						
 						// Allow a drag in all cases except where this entry is a new uncommitted entry.
 						// TODO: What if there are uncommitted changes????
 						// We should probably commit changes first.
 						// We can't drag these because the merge is done by re-parenting.
 						if (uncommittedKey.getCommittedObjectKey() == null) {
 							event.doit = false;
 						}
 					}
 					public void dragSetData(DragSourceEvent event) {
 						// Provide the data of the requested type.
 						if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
 							Entry uncommittedEntry = coordinator.getUncommittedEntryData().getEntry();
 							UncommittedObjectKey uncommittedKey = (UncommittedObjectKey)uncommittedEntry.getObjectKey();
 							Object sourceEntry = uncommittedKey.getCommittedObjectKey().getObject();
 							LocalSelectionTransfer.getTransfer().setSelection(new StructuredSelection(sourceEntry));
 						}
 					}
 					public void dragFinished(DragSourceEvent event) {
 						if (event.detail == DND.DROP_MOVE) {
 							/*
 							 * Having moved the entry, we must delete this one.
 							 * However, this is done as a single operation with
 							 * the merge process (so it all appears as a single
 							 * undoable/redoable operation). Thus we have
 							 * nothing to do here.
 							 */
 						}
 					}
 				});
 
 				fUnreconciledEntriesControl.addDisposeListener(new DisposeListener() {
 					public void widgetDisposed(DisposeEvent e) {
 						dragSource.dispose();
 					}
 				});
 
 				return cellControl;
 			}
 
 			@Override
 			public void createHeaderControls(Composite parent, EntryData entryData) {
 				// All CellBlock implementations must create a control because
 				// the header and rows must match.
 				// Maybe these objects could just point to the header
 				// controls, in which case this would not be necessary.
 				// Note also we use Label, not an empty Composite,
 				// because we don't want a preferred height that is higher
 				// than the labels.
 				new Label(parent, SWT.NONE);
 			}
 		};
 
 		IndividualBlock<EntryData, RowControl> transactionDateColumn = PropertyBlock.createTransactionColumn(TransactionInfo.getDateAccessor());
 		CellBlock<EntryData, BaseEntryRowControl> debitColumnManager = DebitAndCreditColumns.createDebitColumn(fPage.getAccount().getCurrency());
 		CellBlock<EntryData, BaseEntryRowControl> creditColumnManager = DebitAndCreditColumns.createCreditColumn(fPage.getAccount().getCurrency());
 		CellBlock<EntryData, BaseEntryRowControl> balanceColumnManager = new BalanceColumn(fPage.getAccount().getCurrency());
 
 		/*
 		 * Setup the layout structure of the header and rows.
 		 */
 		Block<EntryData, EntryRowControl> rootBlock = new HorizontalBlock<EntryData, EntryRowControl>(
 				reconcileButton,
 				transactionDateColumn,
 				PropertyBlock.createEntryColumn(EntryInfo.getValutaAccessor()),
 				PropertyBlock.createEntryColumn(EntryInfo.getCheckAccessor()),
 				PropertyBlock.createEntryColumn(EntryInfo.getMemoAccessor()),
 				new OtherEntriesButton(
 						new HorizontalBlock<Entry, ISplitEntryContainer>(
 								new SingleOtherEntryPropertyBlock(EntryInfo.getAccountAccessor()),
 								new SingleOtherEntryPropertyBlock(EntryInfo.getMemoAccessor(), JMoneyPlugin.getResourceString("Entry.description")),
 								new SingleOtherEntryPropertyBlock(EntryInfo.getAmountAccessor())
 						)
 				),
 				debitColumnManager,
 				creditColumnManager,
 				balanceColumnManager
 		);
 
 		// Create the table control.
 	    IRowProvider rowProvider = new ReusableRowProvider(rootBlock);
 		fUnreconciledEntriesControl = new EntriesTable<EntryData>(getSection(), toolkit, rootBlock, unreconciledTableContents, rowProvider, fPage.getAccount().getSession(), transactionDateColumn, rowTracker) {
 			@Override
 			protected EntryData createEntryRowInput(Entry entry) {
 				return new EntryData(entry, session.getDataManager());
 			}
 
 			@Override
 			protected EntryData createNewEntryRowInput() {
 				return new EntryData(null, session.getDataManager());
 			}
 		}; 
 
 		getSection().setClient(fUnreconciledEntriesControl);
 		toolkit.paintBordersFor(fUnreconciledEntriesControl);
 		refresh();
 	}
 
 	public void reconcileEntry(EntryRowControl rowControl) {
 		if (fPage.getStatement() != null) {
 			Entry entry = rowControl.getUncommittedTopEntry();
 
 			// TODO: What do we do about the blank entry???
 			
 			/*
 			 * It is possible that the user has made changes to this entry
 			 * that have not yet been committed.  Furthermore, those changes
 			 * may have put the entry into an invalid state that prevents them
 			 * from being committed.
 			 * 
 			 * As validation is done at commit time, we can only set the entry as
 			 * reconciled and then attempt to commit it.
 			 */
 			
 			// The EntriesTree control will always validate and commit
 			// any outstanding changes before firing a default selection
 			// event.  We set the property to put the entry into the
 			// statement and immediately commit the change.
 			if (entry != null) {
 				entry.setPropertyValue(ReconciliationEntryInfo.getStatementAccessor(), fPage.getStatement());
 
 				/*
 				 * We tell the row control to commit its changes. These changes
 				 * include the above change. They may also include prior changes
 				 * made by the user.
 				 * 
 				 * The tables and controls in this editor should all be capable of
 				 * updating themselves correctly when the change is committed. There
 				 * is a listener that is listening for changes to the committed data
 				 * and this listener should ensure all is updated appropriately,
 				 * just as though the change came from outside this view. However,
 				 * we must go through the row control to commit the changes. This
 				 * ensures that the row control knows that its changes are being
 				 * committed and it does not get confused when the listener
 				 * processes the changes.
 				 */
 				rowControl.commitChanges("Reconcile Entry");
 			}
 		} else {
 			MessageDialog.openError(getSection().getShell(), "Action is Not Available", "You must select a statement first before you can reconcile an entry.  The entry will then reconcile to the statement in the upper table.");
 		}
 	}
 }
