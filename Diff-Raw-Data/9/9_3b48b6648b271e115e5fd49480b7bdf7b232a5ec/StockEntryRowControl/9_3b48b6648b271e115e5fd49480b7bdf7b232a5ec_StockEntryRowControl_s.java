 package net.sf.jmoney.stocks.pages;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import net.sf.jmoney.entrytable.BaseEntryRowControl;
 import net.sf.jmoney.entrytable.Block;
 import net.sf.jmoney.entrytable.EntryData;
 import net.sf.jmoney.entrytable.FocusCellTracker;
 import net.sf.jmoney.entrytable.RowSelectionTracker;
 import net.sf.jmoney.entrytable.VirtualRowTable;
 import net.sf.jmoney.isolation.TransactionManager;
 import net.sf.jmoney.model2.Currency;
 import net.sf.jmoney.model2.CurrencyAccount;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.Transaction.EntryCollection;
 import net.sf.jmoney.stocks.Stock;
 import net.sf.jmoney.stocks.StockAccount;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.swt.widgets.Composite;
 
public class StockEntryRowControl extends BaseEntryRowControl<StockEntryData> {
 
 	public enum TransactionType {
 		Buy,
 		Sell,
 		Dividend,
 		Transfer,
 		Other
 	}
 
 	private ArrayList<ITransactionTypeChangeListener> transactionTypeChangeListeners = new ArrayList<ITransactionTypeChangeListener>();
 
 	public StockEntryRowControl(final Composite parent, int style, VirtualRowTable rowTable, Block<StockEntryData, ? super StockEntryRowControl> rootBlock, final RowSelectionTracker selectionTracker, final FocusCellTracker focusCellTracker) {
 		super(parent, style, rowTable, rootBlock);
 		init(this, rootBlock, selectionTracker, focusCellTracker);
 	}
 	
 	/*
 	 * Notify listeners when the transaction type (purchase, sale, dividend etc.)
 	 * changes.
 	 */
 	public void fireTransactionTypeChange() {
 		for (ITransactionTypeChangeListener listener: transactionTypeChangeListeners) {
 			listener.transactionTypeChanged();
 		}
 	}
 	
 	public void addTransactionTypeChangeListener(ITransactionTypeChangeListener listener) {
 		transactionTypeChangeListeners.add(listener);
 	}
 
 	@Override
 	protected StockEntryData createUncommittedEntryData(
 			Entry entryInTransaction, TransactionManager transactionManager) {
 		StockEntryData entryData = new StockEntryData(entryInTransaction, transactionManager);
 		return entryData;
 	}
 
 }
