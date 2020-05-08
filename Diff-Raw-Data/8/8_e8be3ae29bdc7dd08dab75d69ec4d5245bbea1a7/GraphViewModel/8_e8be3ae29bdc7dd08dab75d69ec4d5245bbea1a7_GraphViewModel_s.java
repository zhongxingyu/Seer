 package it.chalmers.mufasa.android_budget_app.model;
 
 import it.chalmers.mufasa.android_budget_app.model.database.DataAccessor.AccountDay;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import android.content.Context;
 
 public class GraphViewModel {
 
 	Account account;
 	List<BudgetItem> budgetItems;
 
 	PropertyChangeSupport pcs;
 
 	Category currentMainCategory;
 
 	boolean editMode = false;
 
 	public GraphViewModel(Context context) {
 		this.pcs = new PropertyChangeSupport(this);
 		this.account = Account.getInstance(context);
 		this.currentMainCategory = account.getCategory(1);
 	}
 	
 	public List<Double> getAccountBalanceListForGraph(int number) {
 		
		Calendar calendar = new GregorianCalendar();		
		calendar.set(2012, 8, 0);		
 		Date from = calendar.getTime();		
 		calendar.add(Calendar.MONTH, 1);		
 		Date to = calendar.getTime();
 		
 		return getAccountBalanceForEachDay(from, to);
 	}
 
 	/**
 	 * Gets change of account balance each day over a specific time period. Handy for
 	 * graph views.
 	 * 
 	 * @param from
 	 *            from date
 	 * @param to
 	 *            to date
 	 * @return 
 	 */
 	public List<Double> getAccountBalanceForEachDay(Date from, Date to) {
 		
 		List<Double> accountBalances = new ArrayList<Double>();
 		
 		for(AccountDay accountDay : account.getAccountBalanceForEachDay(from)) {
 			if(accountDay.getDay().getTime() >= from.getTime() && accountDay.getDay().getTime() <= to.getTime()) {
 				accountBalances.add(accountDay.getValue());
 			}
 		}
 		
 		return accountBalances;
 	}
 	
 	public Double getActualSpendingsInCategoryOverPeriod(Category category, Date from, Date to) {
 		Double result;
 		
 //TODO		
 //		for(Transaction transaction : account.getTransactions(from, to, category)) {
 //			result += transaction.getAmount();
 //		}
 		
 		return 2000.0;
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		pcs.addPropertyChangeListener(listener);
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		pcs.removePropertyChangeListener(listener);
 	}
 }
