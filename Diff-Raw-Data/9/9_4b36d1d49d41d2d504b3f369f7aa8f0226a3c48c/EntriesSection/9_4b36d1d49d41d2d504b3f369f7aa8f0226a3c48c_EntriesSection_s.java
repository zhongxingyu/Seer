 /*
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2004 Johann Gyger <jgyger@users.sf.net>
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
  */
 package net.sf.jmoney.stocks.pages;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import net.sf.jmoney.entrytable.BalanceColumn;
 import net.sf.jmoney.entrytable.BaseEntryRowControl;
 import net.sf.jmoney.entrytable.Block;
 import net.sf.jmoney.entrytable.CellBlock;
 import net.sf.jmoney.entrytable.CellFocusListener;
 import net.sf.jmoney.entrytable.DebitAndCreditColumns;
 import net.sf.jmoney.entrytable.DeleteTransactionHandler;
 import net.sf.jmoney.entrytable.DuplicateTransactionHandler;
 import net.sf.jmoney.entrytable.EntriesTable;
 import net.sf.jmoney.entrytable.EntryData;
 import net.sf.jmoney.entrytable.EntryRowControl;
 import net.sf.jmoney.entrytable.HorizontalBlock;
 import net.sf.jmoney.entrytable.ICellControl2;
 import net.sf.jmoney.entrytable.IEntriesContent;
 import net.sf.jmoney.entrytable.IRowProvider;
 import net.sf.jmoney.entrytable.ISplitEntryContainer;
 import net.sf.jmoney.entrytable.IndividualBlock;
 import net.sf.jmoney.entrytable.NewTransactionHandler;
 import net.sf.jmoney.entrytable.OpenTransactionDialogHandler;
 import net.sf.jmoney.entrytable.OtherEntriesBlock;
 import net.sf.jmoney.entrytable.PropertyBlock;
 import net.sf.jmoney.entrytable.RowControl;
 import net.sf.jmoney.entrytable.RowSelectionTracker;
 import net.sf.jmoney.entrytable.SingleOtherEntryPropertyBlock;
 import net.sf.jmoney.entrytable.StackBlock;
 import net.sf.jmoney.entrytable.StackControl;
 import net.sf.jmoney.entrytable.VerticalBlock;
 import net.sf.jmoney.fields.IAmountFormatter;
 import net.sf.jmoney.isolation.TransactionManager;
 import net.sf.jmoney.model2.DataManager;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.EntryInfo;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.IPropertyControl;
 import net.sf.jmoney.model2.ListPropertyAccessor;
 import net.sf.jmoney.model2.ScalarPropertyAccessor;
 import net.sf.jmoney.model2.SessionChangeAdapter;
 import net.sf.jmoney.model2.SessionChangeListener;
 import net.sf.jmoney.model2.Transaction;
 import net.sf.jmoney.model2.TransactionInfo;
 import net.sf.jmoney.resources.Messages;
 import net.sf.jmoney.stocks.model.Stock;
 import net.sf.jmoney.stocks.model.StockAccount;
 import net.sf.jmoney.stocks.model.StockControl;
 import net.sf.jmoney.stocks.model.StockEntry;
 import net.sf.jmoney.stocks.model.StockEntryInfo;
 import net.sf.jmoney.stocks.pages.StockEntryRowControl.TransactionType;
 
 import org.eclipse.core.commands.IHandler;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.SectionPart;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.handlers.IHandlerService;
 
 /**
  * TODO
  * 
  * @author Johann Gyger
  */
 public class EntriesSection extends SectionPart implements IEntriesContent {
 
 	private StockAccount account;
 
     private EntriesTable<StockEntryData> fEntriesControl;
     
     private Block<StockEntryData, StockEntryRowControl> rootBlock;
     
     public EntriesSection(Composite parent, final StockAccount account, FormToolkit toolkit, IHandlerService handlerService) {
         super(parent, toolkit, ExpandableComposite.TITLE_BAR);
         getSection().setText("All Entries");
         this.account = account;
     	
 		/*
 		 * Setup the layout structure of the header and rows.
 		 */
 		IndividualBlock<EntryData, RowControl> transactionDateColumn = PropertyBlock.createTransactionColumn(TransactionInfo.getDateAccessor());
 
 		IndividualBlock<StockEntryData, StockEntryRowControl> actionColumn = new IndividualBlock<StockEntryData, StockEntryRowControl>("Action", 50, 1) {
 
 			@Override
 			public IPropertyControl<StockEntryData> createCellControl(Composite parent, RowControl rowControl, final StockEntryRowControl coordinator) {
 				final CCombo control = new CCombo(parent, SWT.NONE);
 				control.add("buy");
 				control.add("sell");
 				control.add("dividend");
 				control.add("transfer");
 				control.add("custom");
 				
 				control.addSelectionListener(new SelectionAdapter(){
 					@Override
 					public void widgetSelected(SelectionEvent e) {
 						int index = control.getSelectionIndex();
 						switch (index) {
 						case 0:
 							coordinator.getUncommittedEntryData().forceTransactionToBuy();
 							break;
 						case 1:
 							coordinator.getUncommittedEntryData().forceTransactionToSell();
 							break;
 						case 2:
 							coordinator.getUncommittedEntryData().forceTransactionToDividend();
 							break;
 						case 3:
 							coordinator.getUncommittedEntryData().forceTransactionToTransfer();
 							break;
 						case 4:
 							coordinator.getUncommittedEntryData().forceTransactionToCustom();
 							break;
 						}
 						
 						coordinator.fireTransactionTypeChange();
 					}
 				});
 				
 				ICellControl2<StockEntryData> cellControl = new ICellControl2<StockEntryData>() {
 
 					public Control getControl() {
 						return control;
 					}
 
 					public void load(StockEntryData data) {
 						if (data.getTransactionType() == null) {
 							control.deselectAll();
 							control.setText("");
 						} else {
 							switch (data.getTransactionType()) {
 							case Buy:
 								control.select(0);
 								break;
 							case Sell:
 								control.select(1);
 								break;
 							case Dividend:
 								control.select(2);
 								break;
 							case Transfer:
 								control.select(3);
 								break;
 							case Other:
 								control.select(4);
 								break;
 							default:
 								throw new RuntimeException("bad case");
 							}
 						}
 					}
 
 					public void save() {
 						// TODO Auto-generated method stub
 						
 					}
 
 					public void setSelected() {
 						control.setBackground(RowControl.selectedCellColor);
 					}
 
 					public void setUnselected() {
 						control.setBackground(null);
 					}
 				};
 				
 				FocusListener controlFocusListener = new CellFocusListener<RowControl>(rowControl, cellControl);
 
 				/*
 				 * The control may in fact be a composite control, in which case the
 				 * composite control itself will never get the focus. Only the child
 				 * controls will get the focus, so we add the listener recursively
 				 * to all child controls.
 				 */
 				control.addFocusListener(controlFocusListener);
 
 				return cellControl;
 			}
 		};  
 
 		IndividualBlock<StockEntryData, StockEntryRowControl> shareNameColumn = new IndividualBlock<StockEntryData, StockEntryRowControl>("Stock", 50, 1) {
 
 			@Override
 			public IPropertyControl<StockEntryData> createCellControl(Composite parent, RowControl rowControl, final StockEntryRowControl coordinator) {
 				final StockControl<Stock> control = new StockControl<Stock>(parent, null, Stock.class);
 				
 				ICellControl2<StockEntryData> cellControl = new ICellControl2<StockEntryData>() {
 					private StockEntryData data;
 					
 					public Control getControl() {
 						return control;
 					}
 
 					public void load(StockEntryData data) {
 						this.data = data;
 						
 						/*
 						 * We have to find the appropriate entry in the transaction that contains
 						 * the stock.
 						 * 
 						 * - If this is a purchase or sale, then the stock will be set as the commodity
 						 * for one of the entries.  We find this entry.
 						 * - If this is a dividend payment then the stock will be set as an additional
						 * field in the dividend category. 
 						 */
 						Stock stock;
 						if (data.isPurchaseOrSale()) {
 							Entry entry = data.getPurchaseOrSaleEntry();
 							stock = (Stock)entry.getCommodity();
 						} else if (data.isDividend()) {
 							Entry entry = data.getDividendEntry();
 							stock = entry.getPropertyValue(StockEntryInfo.getStockAccessor());
 						} else {
 							stock = null;
 							control.setEnabled(false);
 						}
 						
 				        control.setSession(data.getEntry().getSession(), Stock.class);
 						
 						control.setStock(stock);
 					}
 
 					public void save() {
 						Stock stock = control.getStock();
 					
 						if (data.isPurchaseOrSale()) {
 							Entry entry = data.getPurchaseOrSaleEntry();
 							StockEntry stockEntry = entry.getExtension(StockEntryInfo.getPropertySet(), true);
 							stockEntry.setStock(stock);
 						} else if (data.isDividend()) {
 							Entry entry = data.getDividendEntry();
 							entry.setPropertyValue(StockEntryInfo.getStockAccessor(), stock);
 						}
 					}
 
 					public void setSelected() {
 						control.setBackground(RowControl.selectedCellColor);
 					}
 
 					public void setUnselected() {
 						control.setBackground(null);
 					}
 				};
 
 				FocusListener controlFocusListener = new CellFocusListener<RowControl>(rowControl, cellControl);
 
 				/*
 				 * The control may in fact be a composite control, in which case the
 				 * composite control itself will never get the focus. Only the child
 				 * controls will get the focus, so we add the listener recursively
 				 * to all child controls.
 				 */
 				addFocusListenerRecursively(cellControl.getControl(), controlFocusListener);
 
 				
 				coordinator.addTransactionTypeChangeListener(new ITransactionTypeChangeListener() {
 
 					public void transactionTypeChanged() {
 						/*
 						 * If the user changes the transaction type, the stock control remains
 						 * the same as it was in the previous transaction type.
 						 * 
 						 * For example, suppose an entry is a purchase of stock in Foo company.
 						 * The user changes the entry to a dividend.  The entry will then
 						 * be a dividend from stock in Foo company.  The user changes the stock
 						 * to Bar company.  Then the user changes the transaction type back
 						 * to a purchase.  The entry will now show a purchase of stock in Bar
 						 * company.
 						 */
 						Stock stock = control.getStock();
 						if (coordinator.getUncommittedEntryData().isPurchaseOrSale()) {
 							Entry entry = coordinator.getUncommittedEntryData().getPurchaseOrSaleEntry();
 							entry.setPropertyValue(StockEntryInfo.getStockAccessor(), stock);
 							control.setEnabled(true);
 						} else if (coordinator.getUncommittedEntryData().isDividend()) {
 							Entry entry = coordinator.getUncommittedEntryData().getDividendEntry();
 							entry.setPropertyValue(StockEntryInfo.getStockAccessor(), stock);
 							control.setEnabled(true);
 						} else {
 							stock = null;
 							control.setEnabled(false);
 						}
 					}
 				});
 				
 				return cellControl;
 			}
 
 			private void addFocusListenerRecursively(Control control, FocusListener listener) {
 				control.addFocusListener(listener);
 				if (control instanceof Composite) {
 					for (Control child: ((Composite)control).getChildren()) {
 						addFocusListenerRecursively(child, listener);
 					}
 				}
 			}
 		};  
 
 		IndividualBlock<StockEntryData, StockEntryRowControl> priceColumn = new IndividualBlock<StockEntryData, StockEntryRowControl>("Price", 60, 1) {
 
 			@Override
 			public IPropertyControl<StockEntryData> createCellControl(Composite parent, RowControl rowControl, final StockEntryRowControl coordinator) {
 				final Text control = new Text(parent, SWT.RIGHT);
 				
 				ICellControl2<StockEntryData> cellControl = new ICellControl2<StockEntryData>() {
 
 					public Control getControl() {
 						return control;
 					}
 
 					public void load(StockEntryData data) {
 						assert(data.isPurchaseOrSale());
 						// TODO:This is a bit funny.  We are passed the data object but
 						// the co-ordinator is keeping track of the share price?
 						setControlValue(coordinator.getSharePrice());
 						
 						// Listen for changes in the stock price
 						coordinator.addStockPriceChangeListener(new IPropertyChangeListener<BigDecimal>() {
 							public void propertyChanged(BigDecimal newValue) {
 								setControlValue(newValue);
 							}
 						});
 					}
 
 					private void setControlValue(BigDecimal sharePrice) {
 						if (sharePrice != null) {
 							long lPrice = sharePrice.movePointRight(4).longValue(); 
 							control.setText(account.getPriceFormatter().format(lPrice));
 						} else {
 							control.setText("");
 						}
 					}
 
 					public void save() {
 						long amount = account.getPriceFormatter().parse(control.getText());
 						
 						/*
 						 * The share price is a calculated amount so is not
 						 * stored in any property. However, we do tell the row
 						 * control because it needs to save the value so it can
 						 * be checked for consistency when the transaction is
 						 * saved and also because other values may need to be
 						 * adjusted as a result of the new share price.
 						 */
 						coordinator.sharePriceChanged(new BigDecimal(amount).movePointLeft(4));
 					}
 
 					public void setSelected() {
 						control.setBackground(RowControl.selectedCellColor);
 					}
 
 					public void setUnselected() {
 						control.setBackground(null);
 					}
 				};
 				
 				FocusListener controlFocusListener = new CellFocusListener<RowControl>(rowControl, cellControl);
 				control.addFocusListener(controlFocusListener);
 				
 				return cellControl;
 				
 			}
 		};  
 
 		IndividualBlock<StockEntryData, StockEntryRowControl> shareNumberColumn = new IndividualBlock<StockEntryData, StockEntryRowControl>("Quantity", EntryInfo.getAmountAccessor().getMinimumWidth(), EntryInfo.getAmountAccessor().getWeight()) {
 
 			@Override
 			public IPropertyControl<StockEntryData> createCellControl(Composite parent, RowControl rowControl, final StockEntryRowControl coordinator) {
 				final Text control = new Text(parent, SWT.RIGHT);
 				
 				ICellControl2<StockEntryData> cellControl = new ICellControl2<StockEntryData>() {
 
 					private StockEntryData data;
 					
 					public Control getControl() {
 						return control;
 					}
 
 					public void load(StockEntryData data) {
 						this.data = data;
 
 						IAmountFormatter formatter = getFormatter();
 						
 						long quantity = data.getPurchaseOrSaleEntry().getAmount();
 						if (data.getTransactionType() == TransactionType.Sell) {
 							quantity = -quantity;
 						}
 						control.setText(formatter.format(quantity));
 					}
 
 					private IAmountFormatter getFormatter() {
 						IAmountFormatter formatter = data.getPurchaseOrSaleEntry().getCommodity();
 						if (formatter == null) {
 							/*
 							 * The user has not yet selected the stock. As the
 							 * way the quantity of a stock is formatted may
 							 * potentially depend on the stock, we do not know
 							 * exactly how to format and parse the quantity.
 							 * However in practice it is unlikely to differ
 							 * between different stock in the same account so we
 							 * use a default formatter from the account.
 							 */
 							formatter = account.getQuantityFormatter();
 						}
 						return formatter;
 					}
 
 					public void save() {
 						IAmountFormatter formatter = getFormatter();
 						long quantity = formatter.parse(control.getText());
 						if (data.getTransactionType() == TransactionType.Sell) {
 							quantity = -quantity;
 						}
 						
 						Entry entry = data.getPurchaseOrSaleEntry();
 						entry.setAmount(quantity);
 						
 						coordinator.quantityChanged();
 					}
 
 					public void setSelected() {
 						control.setBackground(RowControl.selectedCellColor);
 					}
 
 					public void setUnselected() {
 						control.setBackground(null);
 					}
 				};
 				
 				FocusListener controlFocusListener = new CellFocusListener<RowControl>(rowControl, cellControl);
 				control.addFocusListener(controlFocusListener);
 				
 				return cellControl;
 			}
 		};  
 
 		
 		final IndividualBlock<StockEntryData, RowControl> withholdingTaxColumn = new PropertyBlock<StockEntryData, RowControl>(EntryInfo.getAmountAccessor(), "withholdingTax", "Withholding Tax") {
 			@Override
 			public ExtendableObject getObjectContainingProperty(StockEntryData data) {
 				return data.getWithholdingTaxEntry();
 			}
 		};		
 
 		List<Block<? super StockEntryData, ? super StockEntryRowControl>> expenseColumns = new ArrayList<Block<? super StockEntryData, ? super StockEntryRowControl>>();
 		
 		if (account.getCommissionAccount() != null) {
 			IndividualBlock<StockEntryData, StockEntryRowControl> commissionColumn = new PropertyBlock<StockEntryData, StockEntryRowControl>(EntryInfo.getAmountAccessor(), "commission", "Commission") {
 				@Override
 				public ExtendableObject getObjectContainingProperty(StockEntryData data) {
 					return data.getCommissionEntry();
 				}
 
 				@Override
 				public void fireUserChange(StockEntryRowControl rowControl) {
 					rowControl.commissionChanged();
 				}
 			};		
 			expenseColumns.add(commissionColumn);
 		}
 		
 		if (account.getTax1Name() != null) {
 			IndividualBlock<StockEntryData, StockEntryRowControl> tax1Column = new PropertyBlock<StockEntryData, StockEntryRowControl>(EntryInfo.getAmountAccessor(), "tax1", account.getTax1Name()) {
 				@Override
 				public ExtendableObject getObjectContainingProperty(StockEntryData data) {
 					return data.getTax1Entry();
 				}
 				
 				@Override
 				public void fireUserChange(StockEntryRowControl rowControl) {
 					rowControl.tax1Changed();
 				}
 
 			};
 			expenseColumns.add(tax1Column);
 		}
 		
 		if (account.getTax2Name() != null) {
 			IndividualBlock<StockEntryData, StockEntryRowControl> tax2Column = new PropertyBlock<StockEntryData, StockEntryRowControl>(EntryInfo.getAmountAccessor(), "tax2", account.getTax2Name()) {
 				@Override
 				public ExtendableObject getObjectContainingProperty(StockEntryData data) {
 					return data.getTax2Entry();
 				}
 				
 				@Override
 				public void fireUserChange(StockEntryRowControl rowControl) {
 					rowControl.tax2Changed();
 				}
 
 			};
 			expenseColumns.add(tax2Column);
 		}
 		
 		final Block<StockEntryData, StockEntryRowControl> purchaseOrSaleInfoColumn = new VerticalBlock<StockEntryData, StockEntryRowControl>(
 				// TEMP
 				new VerticalBlock<StockEntryData, StockEntryRowControl>(
 						priceColumn,
 						shareNumberColumn
 				),
 				new HorizontalBlock<StockEntryData, StockEntryRowControl>(
 						expenseColumns
 				)
 		);
 
 		final IndividualBlock<StockEntryData, RowControl> transferAccountColumn = new PropertyBlock<StockEntryData, RowControl>(EntryInfo.getAccountAccessor(), "transferAccount", "Transfer Account") {
 			@Override
 			public ExtendableObject getObjectContainingProperty(StockEntryData data) {
 				return data.getTransferEntry();
 			}
 		};		
 
 		final Block<EntryData, BaseEntryRowControl> customTransactionColumn = new OtherEntriesBlock(
 				new HorizontalBlock<Entry, ISplitEntryContainer>(
 						new SingleOtherEntryPropertyBlock(EntryInfo.getAccountAccessor()),
 						new SingleOtherEntryPropertyBlock(EntryInfo.getMemoAccessor(), Messages.EntriesSection_EntryDescription),
 						new SingleOtherEntryPropertyBlock(EntryInfo.getAmountAccessor())
 				)
 		);
 		
 		CellBlock<EntryData, BaseEntryRowControl> debitColumnManager = DebitAndCreditColumns.createDebitColumn(account.getCurrency());
 		CellBlock<EntryData, BaseEntryRowControl> creditColumnManager = DebitAndCreditColumns.createCreditColumn(account.getCurrency());
     	CellBlock<EntryData, BaseEntryRowControl> balanceColumnManager = new BalanceColumn(account.getCurrency());
 		
 		RowSelectionTracker<EntryRowControl> rowTracker = new RowSelectionTracker<EntryRowControl>();
 
 		rootBlock = new HorizontalBlock<StockEntryData, StockEntryRowControl>(
 				transactionDateColumn,
 				new VerticalBlock<StockEntryData, StockEntryRowControl>(
 						new HorizontalBlock<StockEntryData, StockEntryRowControl>(
 								actionColumn,
 								shareNameColumn
 						),
 						PropertyBlock.createEntryColumn(EntryInfo.getMemoAccessor())
 				),
 				new StackBlock<StockEntryData, StockEntryRowControl>(
 						withholdingTaxColumn,
 						purchaseOrSaleInfoColumn,
 						transferAccountColumn,
 						customTransactionColumn
 				) {
 
 					@Override
 					protected Block<? super StockEntryData, ? super StockEntryRowControl> getTopBlock(StockEntryData data) {
 						if (data.getTransactionType() == null) {
 							return null;
 						} else {
 							switch (data.getTransactionType()) {
 							case Buy:
 							case Sell:
 								return purchaseOrSaleInfoColumn;
 							case Dividend:
 								return withholdingTaxColumn;
 							case Transfer:
 								return transferAccountColumn;
 							case Other:
 								return customTransactionColumn;
 							default:
 								throw new RuntimeException("bad case");
 							}
 						}
 					}
 					
 				    @Override	
 					public IPropertyControl<StockEntryData> createCellControl(Composite parent, final RowControl rowControl, final StockEntryRowControl coordinator) {
 						final StackControl<StockEntryData, StockEntryRowControl> control = new StackControl<StockEntryData, StockEntryRowControl>(parent, rowControl, coordinator, this);
 						
 						coordinator.addTransactionTypeChangeListener(new ITransactionTypeChangeListener() {
 
 							public void transactionTypeChanged() {
 								Block<? super StockEntryData, ? super StockEntryRowControl> topBlock = getTopBlock(coordinator.getUncommittedEntryData());
 								
 								// Set this block in the control
 								control.setTopBlock(topBlock);
 
 								/*
 								 * This stack layout has a size this is the
 								 * preferred size of the top control, ignoring
 								 * all the other controls. Therefore changing
 								 * the top control may change the size of the
 								 * row.
 								 */
 								// TODO: It is a bit funny using the coordinator here
 								// This needs to be cleaned up.
 								fEntriesControl.table.refreshSize(coordinator);
 								
 								/*
 								 * The above method will re-size the height of the row
 								 * to its preferred height, but it won't layout the child
 								 * controls if the preferred height did not change.
 								 * We therefore force a layout in order to bring the new
 								 * top control to the top and layout its child controls.
 								 */
 								rowControl.layout(true);
 							}
 						});
 						
 						return control;
 				    }
 
 					@Override
 					public SessionChangeListener createListener(
 							final StockEntryData entryData,
 							final StackControl<StockEntryData, StockEntryRowControl> stackControl) {
 						return 	new SessionChangeAdapter() {
 							@Override
 							public void objectChanged(ExtendableObject changedObject,
 									ScalarPropertyAccessor changedProperty, Object oldValue,
 									Object newValue) {
 								// TODO Auto-generated method stub
 								
 							}
 
 							@Override
 							public void objectCreated(ExtendableObject newObject) {
 								// TODO Auto-generated method stub
 								
 							}
 
 							@Override
 							public void objectDestroyed(ExtendableObject deletedObject) {
 								// TODO Auto-generated method stub
 								
 							}
 
 							@Override
 							public void objectInserted(ExtendableObject newObject) {
 								// TODO Auto-generated method stub
 								
 							}
 
 							@Override
 							public void objectMoved(ExtendableObject movedObject,
 									ExtendableObject originalParent, ExtendableObject newParent,
 									ListPropertyAccessor originalParentListProperty,
 									ListPropertyAccessor newParentListProperty) {
 								// TODO Auto-generated method stub
 								
 							}
 
 							@Override
 							public void objectRemoved(ExtendableObject deletedObject) {
 								// TODO Auto-generated method stub
 								
 							}
 
 							@Override
 							public void performRefresh() {
 								// TODO Auto-generated method stub
 								
 							}
 						};
 					}
 
 					@Override
 					protected DataManager getDataManager(StockEntryData data) {
 						return data.getEntry().getDataManager();
 					}
 				},
 				debitColumnManager,
 				creditColumnManager,
 				balanceColumnManager
 		);
 
 		// Create the table control.
 	    IRowProvider<StockEntryData> rowProvider = new StockRowProvider(rootBlock);
 		fEntriesControl = new EntriesTable<StockEntryData>(getSection(), rootBlock, this, rowProvider, account.getSession(), transactionDateColumn, rowTracker) {
 			@Override
 			protected StockEntryData createEntryRowInput(Entry entry) {
 				return new StockEntryData(entry, session.getDataManager());
 			}
 
 			@Override
 			protected StockEntryData createNewEntryRowInput() {
 				return new StockEntryData(null, session.getDataManager());
 			}
 		}; 
 			
 		// Activate the handlers
 		IHandler handler = new NewTransactionHandler(rowTracker, fEntriesControl);
 		handlerService.activateHandler("net.sf.jmoney.newTransaction", handler);		
 
 		handler = new DeleteTransactionHandler(rowTracker);
 		handlerService.activateHandler("net.sf.jmoney.deleteTransaction", handler);		
 
 		handler = new DuplicateTransactionHandler(rowTracker, fEntriesControl);
 		handlerService.activateHandler("net.sf.jmoney.duplicateTransaction", handler);		
 
 		handler = new OpenTransactionDialogHandler(rowTracker);
 		handlerService.activateHandler("net.sf.jmoney.transactionDetails", handler);		
 
         getSection().setClient(fEntriesControl);
         toolkit.paintBordersFor(fEntriesControl);
         refresh();
     }
 
     public void refreshEntryList() {
     	fEntriesControl.refreshEntryList();
     }
 
     /**
      * @return the entries to be shown in the table, unsorted
      */
 	public Collection<Entry> getEntries() {
 		/*
 		 * We want only cash entries, not stock entries.  This is providing
 		 * content for a table of entries that show the running balance.
 		 * A stock entry or an entry in a currency other than the currency
 		 * of the account should not be returned.
 		 */
 		Collection<Entry> entries = new ArrayList<Entry>();
 		for (Entry entry : account.getEntries()) {
 			if (entry.getCommodity() == account.getCurrency()) {
 				entries.add(entry);
 			}
 		}
 		
 		return entries;
 	}
 
 	public boolean isEntryInTable(Entry entry) {
 		/*
 		 * Entry must be in right account AND be in the currency of the account.
 		 * The account will contain stock entries and these should not appear
 		 * as top level entries in the table.
 		 */
 		return account == entry.getAccount()
 			&& entry.getCommodity() == account.getCurrency();
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.jmoney.pages.entries.IEntriesContent#filterEntry(net.sf.jmoney.pages.entries.EntriesTable.DisplayableTransaction)
 	 */
 	public boolean filterEntry(EntryData data) {
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.jmoney.pages.entries.IEntriesContent#getStartBalance()
 	 */
 	public long getStartBalance() {
         return 0; 
         // ???? account.getStartBalance();
 	}
 
 	public Entry createNewEntry(Transaction newTransaction) {
 		/*
 		 * For stock entries, we create a single entry only.
 		 * The other entries are created as appropriate when a
 		 * transaction type is selected.
 		 */
 		Entry entryInTransaction = newTransaction.createEntry();
 
 		// It is assumed that the entry is in a data manager that is a direct
 		// child of the data manager that contains the account.
 		TransactionManager tm = (TransactionManager)entryInTransaction.getDataManager();
 		entryInTransaction.setAccount(tm.getCopyInTransaction(account));
 
 		return entryInTransaction;
 	}
 }
