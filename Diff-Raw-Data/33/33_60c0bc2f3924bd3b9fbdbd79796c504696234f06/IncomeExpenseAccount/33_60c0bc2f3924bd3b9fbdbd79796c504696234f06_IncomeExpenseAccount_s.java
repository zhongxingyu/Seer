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
 
 package net.sf.jmoney.model2;
 
 import java.util.Map;
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.fields.IncomeExpenseAccountInfo;
 
 /**
  * An implementation of the IncomeExpenseAccount interface
  */
 public class IncomeExpenseAccount extends Account {
 
 	/**
 	 * True if the entries in this account can be in any account,
 	 * false if all the entries must be in the same currency
 	 * (in which case the currency property must be set).
 	 */
 	private boolean multiCurrency = true;
 	
 	/**
 	 * The currency in which all entries in this account are denominated.
 	 * This property is not applicable if multiCurrency is true.
 	 */
 	private Currency currency = null;
 	
 	private String fullAccountName = null;
 
 	public IncomeExpenseAccount(
 			IObjectKey objectKey, 
 			Map extensions, 
 			IObjectKey parent,
 			String name,
 			IListManager subAccounts,
 			boolean multiCurrency,
 			IObjectKey currencyKey) {
 		super(objectKey, extensions, parent, name, subAccounts);
 		this.multiCurrency = multiCurrency;
 		if (currencyKey == null) {
 			this.currency = null;
 		} else {
 			this.currency = (Currency)currencyKey.getObject();
 		}
 	}
 
 	public IncomeExpenseAccount(
 			IObjectKey objectKey, 
 			Map extensions, 
 			IObjectKey parent,
 			IListManager subAccounts) {
 		super(objectKey, extensions, parent, null, subAccounts);
 	}
 
 	protected String getExtendablePropertySetId() {
 		return "net.sf.jmoney.category";
 	}
 	
 	public String getFullAccountName() {
 		if (fullAccountName == null) {
 			fullAccountName = name;
 			Account ancestorCategory = getParent();
 			while (ancestorCategory != null) {
 				fullAccountName = ancestorCategory.getName() + ":" + fullAccountName;
 				ancestorCategory = ancestorCategory.getParent();
 			}
 		}
 		return fullAccountName;
 	}
 
 	// TODO: use debugger to see if this version is called
 	// when the Method object references the version in the
 	// abstract base class.  If not then this is broke.
 	public void setName(String name) {
 		super.setName(name);
 		fullAccountName = null;
 	}
 
 	public boolean isMultiCurrency() {
 		return multiCurrency;
 	}
 	
 	public Currency getCurrency() {
 		return currency;
 	}
 	
 	public void setMultiCurrency(boolean multiCurrency) {
 		this.multiCurrency = multiCurrency;
 	}
 	
 	public void setCurrency(Currency currency) {
 		this.currency = currency;
 	}
 	
 	/**
 	 * @return Commodity represented by the amount in the given entry
 	 */
 	public Commodity getCommodity(Entry entry) {
 		// Income and expense accounts may be either single currency or
 		// multiple currency.
 		if (!multiCurrency) {
 			return currency;
 		} else {
 			// This is a multi-currency account, so we look to the entry.
 			return entry.getIncomeExpenseCurrency();
 		}
 	}
 
 	/**
 	 * This method is required by the JMoney framework.
 	 * 
 	 * @param name
 	 * @param extensionProperties
 	 * @return
 	 */
     public IncomeExpenseAccount createSubAccount() {
     	final IncomeExpenseAccount newSubAccount = (IncomeExpenseAccount)subAccounts.createNewElement(
 				this, 
 				IncomeExpenseAccountInfo.getPropertySet());
 
 		processObjectAddition(IncomeExpenseAccountInfo.getSubAccountAccessor(), newSubAccount);
 		
 		// In addition to the generic object creation event, we also fire an event
 		// specifically for account creation.  The accountAdded event is superfluous 
 		// and it may be simpler if we removed it, so that listeners receive the generic
 		// objectAdded event only.
 		getSession().fireEvent(
 				new ISessionChangeFirer() {
 					public void fire(SessionChangeListener listener) {
 						listener.accountAdded(newSubAccount);
 					}
 				});
 		
 		return newSubAccount;
     }
     
 	/**
 	 * This version is required by the JMoney framework.
 	 */
 	boolean deleteSubAccount(final IncomeExpenseAccount subAccount) {
 		boolean found = subAccounts.remove(subAccount);
 		if (found) {
 			processObjectDeletion(IncomeExpenseAccountInfo.getSubAccountAccessor(), subAccount);
 
 			// In addition to the generic object deletion event, we also fire an event
 			// specifically for account deletion.  The accountDeleted event is superfluous 
 			// and it may be simpler if we removed it, so that listeners receive the generic
 			// objectDeleted event only.
 			getSession().fireEvent(
 					new ISessionChangeFirer() {
 						public void fire(SessionChangeListener listener) {
 							listener.accountDeleted(subAccount);
 						}
 					});
 		}
 		return found;
 	}
 	
     static public  Object [] getDefaultProperties() {
		return new Object [] { "new category" };
 	}
 }
