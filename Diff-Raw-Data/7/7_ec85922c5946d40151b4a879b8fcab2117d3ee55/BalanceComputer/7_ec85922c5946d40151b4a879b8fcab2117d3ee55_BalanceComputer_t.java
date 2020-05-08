 package com.aurelia.loaning.util;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
import org.apache.commons.lang3.StringUtils;

 import com.aurelia.loaning.domain.AbstractLoan;
 import com.aurelia.loaning.domain.LoanType;
 import com.aurelia.loaning.domain.MoneyLoan;
 import com.aurelia.loaning.service.FxTopExchangeRateTask;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 
 public class BalanceComputer {
 
 	public static Double computeBalance(List<AbstractLoan> loans) {
 		Double balance = 0d;
 		MoneyLoan moneyLoan;
 		for (AbstractLoan loan : loans) {
 			if (loan instanceof MoneyLoan) {
 				moneyLoan = (MoneyLoan) loan;
 
 				if (LoanType.MONEY_BORROWING.equals(moneyLoan.getType())) {
 					balance -= moneyLoan.getAmount();
 				} else if (LoanType.MONEY_LOAN.equals(moneyLoan.getType())) {
 					balance += moneyLoan.getAmount();
 				} else {
 					// TODO : error
 				}
 			}
 		}
		String formattedBalance = new DecimalFormat("#0.00").format(balance);
		formattedBalance = StringUtils.replace(formattedBalance, ",", ".");
		return Double.valueOf(formattedBalance);
 	}
 
 	public static Double computeBalanceInCurrency(List<AbstractLoan> loans, String currency) throws ExecutionException,
 			InterruptedException {
 		// filter out non money loans
 		Collection<AbstractLoan> moneyLoans = Collections2.filter(loans, new Predicate<AbstractLoan>() {
 			public boolean apply(AbstractLoan loan) {
 				return loan instanceof MoneyLoan;
 			}
 		});
 		return computeBalance(convertLoansIntoCurrency(new ArrayList<AbstractLoan>(moneyLoans), currency));
 	}
 
 	public static String displayBalance(Double balance, String currency, String filter) {
 		if (balance < 0) {
 			return "I owe " + -balance + " " + currency + " to " + filter;
 		}
 		return filter + " owes me " + balance + " " + currency;
 	}
 
 	public static List<AbstractLoan> convertLoansIntoCurrency(List<AbstractLoan> loans, String currency)
 			throws ExecutionException, InterruptedException {
 		ExecutorService executor = Executors.newFixedThreadPool(loans.size());
 
 		List<FxTopExchangeRateTask> tasks = new ArrayList<FxTopExchangeRateTask>();
 
 		for (AbstractLoan loan : loans) {
 			tasks.add(new FxTopExchangeRateTask(((MoneyLoan) loan).getCurrency(), currency, (MoneyLoan) loan));
 		}
 		List<AbstractLoan> convertedLoans = new ArrayList<AbstractLoan>();
 
 		List<Future<Double>> futures;
 		try {
 			futures = executor.invokeAll(tasks);
 
 			Iterator<FxTopExchangeRateTask> iterator = tasks.iterator();
 
 			for (Future<Double> future : futures) {
 				FxTopExchangeRateTask task = iterator.next();
 				try {
 					MoneyLoan copiedLoan = (MoneyLoan) ((MoneyLoan) task.getLoan()).clone();
 					copiedLoan.setAmount(copiedLoan.getAmount() * future.get());
 					convertedLoans.add(copiedLoan);
 				} catch (CloneNotSupportedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		} catch (InterruptedException e1) {
 			throw e1;
 		} catch (ExecutionException e) {
 			throw e;
 		}
 
 		return convertedLoans;
 	}
 }
