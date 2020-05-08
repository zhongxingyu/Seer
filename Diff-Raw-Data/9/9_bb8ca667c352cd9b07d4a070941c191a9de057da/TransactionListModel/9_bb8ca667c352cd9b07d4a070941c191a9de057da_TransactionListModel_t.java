  /*
   * Copyright  2012 Mufasa developer unit
   *
   * This file is part of Mufasa Budget.
   *
   *	Mufasa Budget is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as published by
   * the Free Software Foundation, either version 3 of the License, or
   * (at your option) any later version.
   *
   * Mufasa Budget is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   * GNU General Public License for more details.
   *
   * You should have received a copy of the GNU General Public License
   * along with Mufasa Budget.  If not, see <http://www.gnu.org/licenses/>.
   */
 package it.chalmers.mufasa.android_budget_app.model;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A class representing a transaction list.
  * @author slurpo
  *
  */
 public class TransactionListModel {
 	private Account account;
	private List<Transaction> transactionHistory;
 	private PropertyChangeSupport pcs;
 
 	public TransactionListModel() {
 		transactionHistory = new ArrayList<Transaction>();
 
 		pcs = new PropertyChangeSupport(this);
 	}
 
 	/**
 	 * Returns a list of transactions.
 	 */
 	public List<Transaction> getTransactionHistory() {
 		return transactionHistory;
 	}
 
 	/**
 	 * Replaces the transaction history with the given one.
 	 */
 	public void updateTransactionHistory(List<Transaction> transactions) {
 		transactionHistory.clear();
 		transactionHistory.addAll(transactions);
 		pcs.firePropertyChange("Transaction Updated", null, null);
 
 	}
 
 	public void setAccount(Account account) {
 		this.account = account;
 	}
 
 	public Account getAccount() {
 		return account;
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener l) {
 		pcs.addPropertyChangeListener(l);
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener l) {
 		pcs.removePropertyChangeListener(l);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((account == null) ? 0 : account.hashCode());
 		result = prime * result + ((pcs == null) ? 0 : pcs.hashCode());
 		result = prime
 				* result
 				+ ((transactionHistory == null) ? 0 : transactionHistory
 						.hashCode());
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		TransactionListModel other = (TransactionListModel) obj;
 		if (account == null) {
 			if (other.account != null) {
 				return false;
 			}
 		} else if (!account.equals(other.account)) {
 			return false;
 		}
 		if (pcs == null) {
 			if (other.pcs != null) {
 				return false;
 			}
 		} else if (!pcs.equals(other.pcs)) {
 			return false;
 		}
 		if (transactionHistory == null) {
 			if (other.transactionHistory != null) {
 				return false;
 			}
 		} else if (!transactionHistory.equals(other.transactionHistory)) {
 			return false;
 		}
 		return true;
 	}
 
 }
