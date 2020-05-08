 /*
  *  Straight - A system to manage financial demands for small and decentralized
  *  organizations.
  *  Copyright (C) 2011  Octahedron 
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.figgo.modules.bank.data;
 
 import java.math.BigDecimal;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TreeSet;
 
 import javax.jdo.Query;
 
 import br.octahedron.cotopaxi.datastore.jdo.GenericDAO;
 import br.octahedron.figgo.modules.bank.TransactionInfoService;
 
 /**
  * A DAO for transactions.
  * 
  * @author Danilo Queiroz
  */
 public class BankTransactionDAO extends GenericDAO<BankTransaction> implements TransactionInfoService {
 
 	public BankTransactionDAO() {
 		super(BankTransaction.class);
 	}
 
 	public Collection<BankTransaction> getLastNTransactions(String accountId, int n) {
 		return this.getAllTransactions(accountId, n);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see br.octahedron.straight.bank.TransactionInfoService#getLastTransactions(java.lang.Long,
 	 * java.lang.Long)
 	 */
 	@Override
 	public Collection<BankTransaction> getLastTransactions(String accountId, Long startDate) {
 		if (startDate == null) {
 			return this.getAllTransactions(accountId, Long.MIN_VALUE);
 		} else {
 			return this.getLastTransactionsFrom(accountId, startDate);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * br.octahedron.straight.bank.TransactionInfoService#getTransactionsByDateRange(java.lang.String
 	 * , java.util.Date, java.util.Date)
 	 */
 	@Override
 	public Collection<BankTransaction> getTransactionsByDateRange(String accountId, Date startDate, Date endDate) {
 		Collection<BankTransaction> creditTransactions = this.getCreditTransactionsByDateRange(accountId, startDate, endDate);
 		Collection<BankTransaction> debitTransactions = this.getDebitTransactionsByDateRange(accountId, startDate, endDate);
 		return this.mergeTransactions(creditTransactions, debitTransactions, Long.MIN_VALUE);
 	}
 
 	@SuppressWarnings("unchecked")
 	protected Collection<BankTransaction> getCreditTransactionsByDateRange(String accountId, Date startDate, Date endDate) {
 		Query query = this.createQuery();
 		query.setFilter("accountDest == :accountId && timestamp >= :startDate && timestamp <= :endDate");
 		return (List<BankTransaction>) query.execute(accountId, startDate.getTime(), endDate.getTime());
 	}
 
 	@SuppressWarnings("unchecked")
 	protected Collection<BankTransaction> getDebitTransactionsByDateRange(String accountId, Date startDate, Date endDate) {
 		Query query = this.createQuery();
 		query.setFilter("accountOrig == :accountId && timestamp >= :startDate && timestamp <= :endDate");
 		return (List<BankTransaction>) query.execute(accountId, startDate.getTime(), endDate.getTime());
 	}
 
 	/**
 	 * Returns an account credit amount specified by a date range
 	 * 
 	 * @param accountId
 	 *            accountId of the account
 	 * @param startDate
 	 *            startDate of the range
 	 * @param endDate
 	 *            endDate of the range
 	 * 
 	 * @return an amount representing the sum of all credit transactions of an account
 	 */
 	public BigDecimal getAmountCreditByDateRange(String accountId, Date startDate, Date endDate) {
 		Collection<BankTransaction> transactions = this.getCreditTransactionsByDateRange(accountId, startDate, endDate);
 		BigDecimal sum = BigDecimal.ZERO;
 		for (BankTransaction transaction : transactions) {
 			if (!transaction.getAccountOrig().equals(SystemAccount.ID)) {
 				sum = sum.add(transaction.getAmount());				
 			}
 		}
 		return sum;
 	}
 
 	/**
 	 * Returns the amount of transactions specified by a date range
 	 * 
 	 * @param startDate
 	 *            startDate of the range
 	 * @param endDate
 	 *            endDate of the range
 	 * 
 	 * @return an amount representing the sum of all transactions made on the date range
 	 */
 	@SuppressWarnings("unchecked")
 	public BigDecimal getAllAmountByDateRange(Date startDate, Date endDate) {
 		Query query = this.createQuery();
 		query.setFilter("timestamp >= :startDate && timestamp <= :endDate");
 		List<BankTransaction> transactions = (List<BankTransaction>) query.execute(startDate.getTime(), endDate.getTime());
 		BigDecimal sum = BigDecimal.ZERO;
 		for (BankTransaction transaction : transactions) {
 			if (!transaction.getAccountOrig().equals(SystemAccount.ID)) {
 				sum = sum.add(transaction.getAmount());
 			}
 		}
 		return sum;
 	}
 
 	/**
 	 * Get last transactions older than the given lastUsedTransaction
 	 */
 	@SuppressWarnings("unchecked")
 	private Collection<BankTransaction> getLastTransactionsFrom(String accountId, Long lastUsedTransaction) {
 		Query query = this.createQuery();
 		query.setFilter("timestamp >= :timestamp && accountOrig == :accId");
 		query.setOrdering("timestamp asc");
 		List<BankTransaction> transactions1 = (List<BankTransaction>) query.execute(lastUsedTransaction, accountId);
 
 		query = this.createQuery();
 		query.setFilter("timestamp >= :timestamp && accountDest == :accId");
 		query.setOrdering("timestamp asc");
 		List<BankTransaction> transactions2 = (List<BankTransaction>) query.execute(lastUsedTransaction, accountId);
 
 		return this.mergeTransactions(transactions1, transactions2, Long.MIN_VALUE);
 	}
 
 	/**
 	 * Get all transactions for an account
 	 */
 	@SuppressWarnings("unchecked")
 	private Collection<BankTransaction> getAllTransactions(String accountId, long count) {
 		Query query = this.createQuery();
 		query.setFilter("accountOrig == :accId");
		query.setOrdering("timestamp asc");
 		if (count > 0) {
 			query.setRange(0, count);
 		}
 		List<BankTransaction> transactions1 = (List<BankTransaction>) query.execute(accountId);
 
 		query = this.createQuery();
 		query.setFilter("accountDest == :accId");
		query.setOrdering("timestamp asc");
 		if (count > 0) {
 			query.setRange(0, count);
 		}
 		List<BankTransaction> transactions2 = (List<BankTransaction>) query.execute(accountId);
 
 		return this.mergeTransactions(transactions1, transactions2, count);
 	}
 
 	/**
 	 * Merges two transactions list ordering transactions by id (lower to higher)
 	 * 
 	 * @param count
 	 * 
 	 * @return a list with transactions from the two lists, ordered by id.
 	 */
 	private Collection<BankTransaction> mergeTransactions(Collection<BankTransaction> transactions1, Collection<BankTransaction> transactions2,
 			long count) {
 		TreeSet<BankTransaction> result = new TreeSet<BankTransaction>(new BankTransactionComparator());
 		result.addAll(transactions1);
 		result.addAll(transactions2);
 		
 		if (count == Long.MIN_VALUE) {
 			return result;
 		} else {
 			List<BankTransaction> other = new LinkedList<BankTransaction>();
 			Iterator<BankTransaction> itr = result.descendingIterator();
 			while (itr.hasNext() && count != 0) {
 				other.add(itr.next());
 				count--;
 			}
 			return other;
 		}
 	}
 
 	/**
 	 * Returns the whole amount injected on the bank by admin.
 	 * 
 	 * @return an amount representing the whole injected amount on the bank
 	 */
 	@SuppressWarnings("unchecked")
 	public BigDecimal getBallast() {
 		Query query = this.createQuery();
 		query.setFilter("accountOrig == :accOrig");
 		List<BankTransaction> transactions = (List<BankTransaction>) query.execute(SystemAccount.ID);
 		BigDecimal sum = BigDecimal.ZERO;
 		for (BankTransaction transaction : transactions) {
 			sum = sum.add(transaction.getAmount());
 		}
 		return sum;
 	}
 
 	/**
 	 * Compares banktransaction by timestamp. It ignores equals transactions, it means, if two
 	 * transactions have the same timestamp it will -1 arbitrarily.
 	 */
 	private class BankTransactionComparator implements Comparator<BankTransaction> {
 		public int compare(BankTransaction o1, BankTransaction o2) {
 			int comp = o1.getTimestamp().compareTo(o2.getTimestamp());
             return (comp != 0) ? comp : o1.getId().compareTo(o2.getId()); 
 		}
 	}
 
 }
