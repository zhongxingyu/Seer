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
 
 package net.sf.jmoney.ameritrade;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.isolation.TransactionManager;
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.Commodity;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.model2.Transaction;
 import net.sf.jmoney.model2.Session.NoAccountFoundException;
 import net.sf.jmoney.model2.Session.SeveralAccountsFoundException;
 import net.sf.jmoney.stocks.model.Stock;
 import net.sf.jmoney.stocks.model.StockAccount;
 import net.sf.jmoney.stocks.model.StockEntry;
 import net.sf.jmoney.stocks.model.StockEntryInfo;
 import net.sf.jmoney.stocks.model.StockInfo;
 
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.IImportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 /**
  * A wizard to import data from a comma-separated file that has been downloaded
  * from Ameritrade.
  * 
  * Currently this wizard if a single page wizard that asks only for the file.
  * This feature is implemented as a wizard because the Eclipse workbench import
  * action requires all import implementations to be wizards.
  */
 public class CsvImportWizard extends Wizard implements IImportWizard {
 	public class MandatoryExchange {
 
 		private Date date;
 		private long originalQuantity;
 		private Stock originalStock;
 		private long newQuantity;
 		private Stock newStock;
 		private Date fractionalSharesDate;
 		private long fractionalSharesAmount;
 
 		/**
 		 * Initial constructor called when first "Mandatory Exchange" row found.
 		 * 
 		 * @param date
 		 * @param quantity
 		 * @param stock
 		 */
 		public MandatoryExchange(Date date, long quantity, Stock stock) {
 			this.date = date;
 			this.originalQuantity = quantity;
 			this.originalStock = stock;
 		}
 
 		/**
 		 * Called when second "Mandatory Exchange" row found.
 		 * 
 		 * @param quantity
 		 * @param stock
 		 */
 		public void setReplacementStock(long quantity, Stock stock) {
 			this.newQuantity = quantity;
 			this.newStock = stock;
 		}
 
 		/**
 		 * Called when an amount indicating it is paid in lieu of fractional
 		 * shares is found.  The transaction can complete if this row is not
 		 * found.
 		 * 
 		 * @param date
 		 * @param total
 		 */
 		public void setCashForFractionalShares(Date date, long total) {
 			this.fractionalSharesDate = date;
 			this.fractionalSharesAmount = total;
 		}
 
 		public Date getDate() {
 			return date;
 		}
 
 		public void createTransaction(Session sessionInTransaction, Account account) {
         	Transaction trans = sessionInTransaction.createTransaction();
         	trans.setDate(date);
         	
         	StockEntry originalSharesEntry = trans.createEntry().getExtension(StockEntryInfo.getPropertySet(), true);
         	originalSharesEntry.setAccount(account);
         	originalSharesEntry.setAmount(-originalQuantity);
         	originalSharesEntry.setStock(originalStock);
         	originalSharesEntry.setStockChange(true);
         	originalSharesEntry.setMemo("mandatory exchange");
         	
         	StockEntry newSharesEntry = trans.createEntry().getExtension(StockEntryInfo.getPropertySet(), true);
         	newSharesEntry.setAccount(account);
         	newSharesEntry.setAmount(newQuantity);
         	newSharesEntry.setStock(newStock);
         	newSharesEntry.setStockChange(true);
         	newSharesEntry.setMemo("mandatory exchange");
         	
         	if (fractionalSharesAmount != 0) {
             	StockEntry fractionalEntry = trans.createEntry().getExtension(StockEntryInfo.getPropertySet(), true);
             	fractionalEntry.setAccount(account);
             	fractionalEntry.setAmount(fractionalSharesAmount);
             	fractionalEntry.setValuta(fractionalSharesDate);
             	fractionalEntry.setStock(newStock);
             	fractionalEntry.setStockChange(false);
             	fractionalEntry.setMemo("cash in lieu of fractional shares");
         	} else {
         		// We must have a currency entry in the account in order to see an entry.
             	StockEntry fractionalEntry = trans.createEntry().getExtension(StockEntryInfo.getPropertySet(), true);
             	fractionalEntry.setAccount(account);
             	fractionalEntry.setAmount(fractionalSharesAmount);
             	fractionalEntry.setMemo("exchange of stock");
         	}
 		}
 	}
 
 	private IWorkbenchWindow window;
 
 	private CsvImportWizardPage mainPage;
 
 	private Session session;
 
 	public CsvImportWizard() {
 		IDialogSettings workbenchSettings = Activator.getDefault().getDialogSettings();
 		IDialogSettings section = workbenchSettings.getSection("CsvImportWizard");//$NON-NLS-1$
 		if (section == null) {
 			section = workbenchSettings.addNewSection("CsvImportWizard");//$NON-NLS-1$
 		}
 		setDialogSettings(section);
 	}
 
 	/**
 	 * We will cache window object in order to be able to provide parent shell
 	 * for the message dialog.
 	 */
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.window = workbench.getActiveWorkbenchWindow();
 
 		session = JMoneyPlugin.getDefault().getSession();
 		
 		// Original JMoney disabled the import menu items when no
 		// session was open. I don't know how to do that in Eclipse,
 		// so we display a message instead.
 		if (session == null) {
 			MessageDialog waitDialog = new MessageDialog(
 					window.getShell(),
 					"Disabled Action Selected",
 					null, // accept the default window icon
 					"You cannot import data into an accounting session unless you have a session open.  You must first open a session or create a new session.",
 					MessageDialog.INFORMATION,
 					new String[] { IDialogConstants.OK_LABEL }, 0);
 			waitDialog.open();
 			return;
 		}
 
 		mainPage = new CsvImportWizardPage(window);
 		addPage(mainPage);
 	}
 
 	@Override
 	public boolean performFinish() {
 		String fileName = mainPage.getFileName();
 		if (fileName != null) {
 			File csvFile = new File(fileName);
 			importFile(csvFile);
 		}
 
 		return true;
 	}
 
 
 	public void importFile(File file) {
 
 		try {
 			/*
 			 * Create a transaction to be used to import the entries.  This allows the entries to
 			 * be more efficiently written to the back-end datastore and it also groups
 			 * the entire import as a single change for undo/redo purposes.
 			 */
 			TransactionManager transactionManager = new TransactionManager(session.getDataManager());
 			Session sessionInTransaction = transactionManager.getSession();
 			
 			
 			
 			// Find the appropriate accounts.
 			// These should probably be in preferences rather than forcing
 			// the account names to these hard coded values.
 			
			StockAccount accountOutside = (StockAccount)session.getAccountByShortName("Ameritrade");
			StockAccount account = transactionManager.getCopyInTransaction(accountOutside);
			
			Account interestAccountOutside = session.getAccountByShortName("Interest - Ameritrade");
			Account interestAccount = transactionManager.getCopyInTransaction(interestAccountOutside);
 
			Account expensesAccountOutside = session.getAccountByShortName("Stock - Expenses (US)");
			Account expensesAccount = transactionManager.getCopyInTransaction(expensesAccountOutside);
 
 			Pattern patternAdr;
 			Pattern patternForeignTax;
 			try {
 				patternAdr = Pattern.compile("ADR FEES \\([A-Z]*\\)");
 				patternForeignTax = Pattern.compile("FOREIGN TAX WITHHELD \\([A-Z]*\\)");
 			} catch (PatternSyntaxException e) {
 				throw new RuntimeException("pattern failed"); 
 			}
 				
         	MandatoryExchange mandatoryExchange = null;
 
         	CSVReader reader = new CSVReader(new FileReader(file));
 			
 			// Pass the header
 			reader.readNext();
 			
 			String [] nextLine = reader.readNext();
 			while (nextLine != null && nextLine.length == 12) {
 				String dateString = nextLine[0];
 				String uniqueId = nextLine[1];
 				String memo = nextLine[2];
 				String quantityString = nextLine[3];
 				String security = nextLine[4];
 				String price = nextLine[5];
 				String commissionString = nextLine[6];
 				String totalString = nextLine[7];
 				String salesFee = nextLine[8];
 				String shortTermRedemptionFee = nextLine[9];
 				String fundRedemptionFee = nextLine[10];
 				String deferredSalesCharge = nextLine[11];
 				
 		        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
 		        Date date = df.parse(dateString);
 		        
 		        Long total = getAmount(totalString);
 		        Long commission = getAmount(commissionString);
 
 		        // Find the security
 		        Stock stock = null;
 		        if (security.length() != 0) {
 		        	for (Commodity commodity : sessionInTransaction.getCommodityCollection()) {
 		        		if (commodity instanceof Stock) {
 		        			Stock eachStock = (Stock)commodity;
 		        			if (security.equals(eachStock.getSymbol())) {
 		        				stock = eachStock;
 		        				break;
 		        			}
 		        		}
 		        	}
 		        	if (stock == null) {
 		        		// Create it.  The name is not available in the import file,
 		        		// so for time being we use the symbol as the name.
 		        		stock = sessionInTransaction.createCommodity(StockInfo.getPropertySet());
 		        		stock.setName(security);
 		        		stock.setSymbol(security);
 		        	}
 		        }		        
 
 		        long totalSalesFee =
 		        	getAmount(salesFee) + getAmount(deferredSalesCharge);
 		        long totalRedemptionFee =
 		        	getAmount(shortTermRedemptionFee) + getAmount(fundRedemptionFee);
 
 
 		        if (mandatoryExchange != null
 		        		&& !memo.startsWith("MANDATORY - EXCHANGE ")
 		        		&& !memo.startsWith("CASH IN LIEU OF FRACTIONAL SHARES ")) {
 		        	mandatoryExchange.createTransaction(sessionInTransaction, account);
 		        	mandatoryExchange = null;
 		        }
 
 
 		        
 		        Matcher matcher;
 		        
 		        if (memo.startsWith("Sold ") || memo.startsWith("Bought ")) {
 			        Long quantity = stock.parse(quantityString);
 			        
 		        	Transaction trans = sessionInTransaction.createTransaction();
 		        	trans.setDate(date);
 		        	
 		        	StockEntry mainEntry = createStockEntry(trans);
 		        	mainEntry.setAccount(account);
 		        	mainEntry.setAmount(total);
 //		        	mainEntry.setUniqueId(uniqueId);  // TODO
 		        	StockEntry saleEntry = createStockEntry(trans);
 		        	saleEntry.setAccount(account);
 		        	
 		        	if (memo.startsWith("Bought ")) {
 		        		saleEntry.setAmount(quantity);
 		        	} else {
 		        		saleEntry.setAmount(-quantity);
 		        	}
 		        	
 		        	saleEntry.setStock(stock);
 		        	saleEntry.setStockChange(true);
 		        	
 		        	if (commission != null) {
 		        		StockEntry commissionEntry = trans.createEntry().getExtension(StockEntryInfo.getPropertySet(), true);
 		        		commissionEntry.setAccount(account.getCommissionAccount());
 		        		commissionEntry.setAmount(commission);
 		        		commissionEntry.setStock(stock);
 		        	}
 		        	
 		        	if (totalSalesFee != 0) {
 		        		StockEntry entry = trans.createEntry().getExtension(StockEntryInfo.getPropertySet(), true);
 		        		entry.setAccount(account.getTax1Account());
 		        		entry.setAmount(totalSalesFee);
 		        		entry.setStock(stock);
 		        	}
 		        	
 		        	if (totalRedemptionFee != 0) {
 		        		StockEntry entry = trans.createEntry().getExtension(StockEntryInfo.getPropertySet(), true);
 		        		entry.setAccount(account.getTax2Account());
 		        		entry.setAmount(totalRedemptionFee);
 		        		entry.setStock(stock);
 		        	}
 		        	
 		        } else if (memo.startsWith("QUALIFIED DIVIDEND ")) {
 		        	Transaction trans = sessionInTransaction.createTransaction();
 		        	trans.setDate(date);
 		        	
 		        	Entry mainEntry = trans.createEntry();
 		        	mainEntry.setAccount(account);
 		        	mainEntry.setMemo("qualified dividend");
 		        	mainEntry.setAmount(total);
 		        	
 		        	StockEntry otherEntry = trans.createEntry().getExtension(StockEntryInfo.getPropertySet(), true);
 		        	otherEntry.setAccount(account.getDividendAccount());
 		        	otherEntry.setMemo("qualified");
 	        		otherEntry.setAmount(-total);
 	        		otherEntry.setStock(stock); 
 		        } else if (memo.equals("W-8 WITHHOLDING") || memo.equals("BACKUP WITHHOLDING (W-9)")) {
 		        	Transaction trans = sessionInTransaction.createTransaction();
 		        	trans.setDate(date);
 		        	
 		        	Entry mainEntry = trans.createEntry();
 		        	mainEntry.setAccount(account);
 		        	mainEntry.setMemo("withholding");
 		        	mainEntry.setAmount(total);
 		        	
 		        	Entry otherEntry = trans.createEntry();
 		        	otherEntry.setAccount(account.getWithholdingTaxAccount());
 		        	if (memo.equals("W-8 WITHHOLDING")) {
 		        		otherEntry.setMemo("W-8");
 		        	} else {
 		        		otherEntry.setMemo("W-9");
 		        	}
 	        		otherEntry.setAmount(-total);
 //		        } else if (memo.startsWith("MANDATORY - NAME CHANGE ")) {
 		        	
 		        } else if (memo.startsWith("MANDATORY - EXCHANGE ")) {
 		        	/*
 		        	 * These usually come in pairs, with the first being the old stock
 		        	 * and the second entry being the new stock.  There is no other way of
 		        	 * knowing which is which, except perhaps by looking to see what we currently
 		        	 * have in the account.
 		        	 */
 			        Long quantity = stock.parse(quantityString);
 
 					if (mandatoryExchange == null) {
 		        		mandatoryExchange = new MandatoryExchange(date, quantity, stock);
 		        	} else {
 		        		if (!mandatoryExchange.getDate().equals(date)) {
 		        			throw new RuntimeException("dates don't match");
 		        		}
 		        		mandatoryExchange.setReplacementStock(quantity, stock);
 		        	}
 		        } else if (memo.startsWith("CASH IN LIEU OF FRACTIONAL SHARES ")) {
 					if (mandatoryExchange == null) {
 						// TODO: error
 					} else {
 						mandatoryExchange.setCashForFractionalShares(date, total);
 						mandatoryExchange.createTransaction(sessionInTransaction, account);
 						mandatoryExchange = null;
 					}
 //		        } else if (memo.startsWith("STOCK SPLIT ")) {
 		        	
 		        } else if (memo.equals("FREE BALANCE INTEREST ADJUSTMENT")) {
 		        	Transaction trans = sessionInTransaction.createTransaction();
 		        	trans.setDate(date);
 		        	
 		        	Entry mainEntry = trans.createEntry();
 		        	mainEntry.setAccount(account);
 		        	mainEntry.setMemo("interest");
 		        	mainEntry.setAmount(total);
 		        	
 		        	Entry otherEntry = trans.createEntry();
 		        	otherEntry.setAccount(interestAccount);
 	        		otherEntry.setAmount(-total);
 //		        } else if (memo.equals("CLIENT REQUESTED ELECTRONIC FUNDING DISBURSEMENT (FUNDS NOW)")) {
 
 //		        } else if (memo.equals("WIRE OUTGOING (ACD WIRE DISBURSEMENTS)")) {
 		        
 //		        } else if (memo.equals("MARGIN INTEREST ADJUSTMENT")) {
 		        	
 		        } else if (memo.startsWith("FOREIGN TAX WITHHELD ")) {
 		        	Transaction trans = sessionInTransaction.createTransaction();
 		        	trans.setDate(date);
 		        	
 		        	Entry mainEntry = trans.createEntry();
 		        	mainEntry.setAccount(account);
 		        	mainEntry.setMemo("Foreign tax withheld");
 		        	mainEntry.setAmount(total);
 		        	
 		        	StockEntry otherEntry = trans.createEntry().getExtension(StockEntryInfo.getPropertySet(), true);
 		        	otherEntry.setAccount(expensesAccount);
 		        	otherEntry.setMemo("Foreign tax withheld???");
 	        		otherEntry.setAmount(-total);
 	        		otherEntry.setStock(stock);
 		        } else if ((matcher = patternAdr.matcher(memo)).matches()) {
 		        	Transaction trans = sessionInTransaction.createTransaction();
 		        	trans.setDate(date);
 		        	
 		        	Entry mainEntry = trans.createEntry();
 		        	mainEntry.setAccount(account);
 		        	mainEntry.setMemo("ADR fee");
 		        	mainEntry.setAmount(total);
 		        	
 		        	StockEntry otherEntry = createStockEntry(trans);
 		        	otherEntry.setAccount(expensesAccount);
 		        	otherEntry.setMemo("ADR fee");
 	        		otherEntry.setAmount(-total);
 	        		otherEntry.setStock(stock);
 		        } else {
 					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Unable to read CSV file", "Entry found with unknown memo: '" + memo + "'.");
 		        }
 				
 		        nextLine = reader.readNext();
 		    }
 			
 			assert (nextLine.length == 1);
 			assert (nextLine[0].equals("***END OF FILE***"));
 
 			/*
 			 * All entries have been imported and all the properties
 			 * have been set and should be in a valid state, so we
 			 * can now commit the imported entries to the datastore.
 			 */
 			String transactionDescription = String.format("Import Ameritrade {0}", file.getName());
 			transactionManager.commit(transactionDescription);									
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e) {
 			// Bad date
 			e.printStackTrace();
 		} catch (SeveralAccountsFoundException e) {
 			// The accounts should not really be hard-coded anyway
 			e.printStackTrace();
 		} catch (NoAccountFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
     public StockEntry createStockEntry(Transaction trans) {
     	return trans.createEntry().getExtension(StockEntryInfo.getPropertySet(), true);
 	}
     
 	long getAmount(String amountString) {
 		if (amountString.length() == 0) {
 			return 0;
 		}
 		
 		String parts [] = amountString.split("\\.");
 		long amount = Long.parseLong(parts[0]) * 100;
 		if (parts.length > 1) {
 			amount += Long.parseLong(parts[1]);
 		}
 		return amount;
 	}
 }
