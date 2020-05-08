 package es.udc.santiago.view.cashflows;
 
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
 import android.content.Intent;
 import android.content.SharedPreferences.Editor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.googlecode.android.widgets.DateSlider.DateSlider;
 import com.googlecode.android.widgets.DateSlider.DefaultDateSlider;
 import com.googlecode.android.widgets.DateSlider.MonthYearDateSlider;
 import com.googlecode.android.widgets.DateSlider.YearDateSlider;
 
 import es.udc.santiago.R;
 import es.udc.santiago.model.facade.CashFlow;
 import es.udc.santiago.model.facade.CashFlowService;
 import es.udc.santiago.model.facade.MovementType;
 import es.udc.santiago.model.facade.Period;
 import es.udc.santiago.model.util.ModelUtilities;
 import es.udc.santiago.view.categories.ManageCategoriesActivity;
 import es.udc.santiago.view.utils.ViewUtils;
 
 public class OverviewActivity extends SherlockFragmentActivity implements
 		com.actionbarsherlock.app.ActionBar.TabListener {
 	private static final String TAG = "OverviewActivity";
 	private static final int DATE_PICKER_DIALOG = 1;
 	private static final int DIALOG_SELECT_CURRENCY = 2;
 	private static final int TOP_CATEGORIES_MAX = 5;
 
 	private Button dayButton;
 	private Calendar day;
 	private Period period;
 
 	private CashFlowService cashService;
 	private TextView incomes, expenses, balance;
 	private List<Entry<String, Float>> top5incomes;
 	private List<Entry<String, Float>> top5expenses;
 	private Map<String, Float> catIncomes = new HashMap<String, Float>();
 	private Map<String, Float> catExpenses = new HashMap<String, Float>();
 	private MovementType top5type;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.overview);
 		try {
 			cashService = new CashFlowService(ModelUtilities.getHelper(this));
 		} catch (SQLException e) {
 		}
 
 		top5incomes = new LinkedList<Map.Entry<String, Float>>();
 		top5expenses = new LinkedList<Map.Entry<String, Float>>();
 		top5type = MovementType.INCOME;
 		day = Calendar.getInstance();
 
 		ActionBar bar = getSupportActionBar();
 		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		Tab tab = bar.newTab().setText(getString(R.string.today))
 				.setTabListener(this);
 
 		bar.addTab(tab);
 		tab = bar.newTab().setText(getString(R.string.monthly))
 				.setTabListener(this);
 
 		bar.addTab(tab);
 		tab = bar.newTab().setText(getString(R.string.yearly))
 				.setTabListener(this);
 
 		bar.addTab(tab);
 
 		period = Period.ONCE;
 
 		dayButton = (Button) findViewById(R.id.daybutton);
 		dayButton.setText(getString(R.string.today));
 		dayButton.setOnClickListener(new OnClickListener() {
 
 			@SuppressWarnings("deprecation")
 			@Override
 			public void onClick(View v) {
 				Log.d(TAG, "Showing dialog");
 				showDialog(DATE_PICKER_DIALOG);
 			}
 		});
 
 		((Spinner) findViewById(R.id.topcat_spinner))
 				.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 					@Override
 					public void onItemSelected(AdapterView<?> arg0, View arg1,
 							int arg2, long arg3) {
 						switch (arg0.getSelectedItemPosition()) {
 						case 0:
 							top5type = MovementType.INCOME;
 							break;
 						case 1:
 							top5type = MovementType.EXPENSE;
 							break;
 						}
 						setTop5Categories(top5type);
 					}
 
 					@Override
 					public void onNothingSelected(AdapterView<?> arg0) {
 					}
 				});
 		incomes = (TextView) findViewById(R.id.incomes_daily);
 		expenses = (TextView) findViewById(R.id.expenses_daily);
 		balance = (TextView) findViewById(R.id.balance_daily);
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		new GetMovementsTask().execute(day);
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.actionbar_menu, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.add_movement) {
 			startActivity(new Intent(this, AddOperationActivity.class));
 		}
 		if (item.getItemId() == R.id.manage_categories) {
 			startActivity(new Intent(this, ManageCategoriesActivity.class));
 		}
 		if (item.getItemId() == R.id.view_all) {
 			Intent i = new Intent(getApplicationContext(),
 					ViewAllMovementsActivity.class);
 			Bundle b = new Bundle();
 			b.putLong("startDayMilliseconds", day.getTimeInMillis());
 			b.putInt("periodCode", period.getCode());
 			i.putExtras(b);
 			startActivity(i);
 		}
 		return true;
 	}
 
 	/**
 	 * Builds a select currency dialog.
 	 * 
 	 * @return Dialog
 	 */
 	private Dialog getSelectCurrencyDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(getString(R.string.select_currency));
 		builder.setItems(R.array.currency,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int item) {
 						Editor prefs = PreferenceManager
 								.getDefaultSharedPreferences(
 										getApplicationContext()).edit();
 						String currencyCode = "";
 						switch (item) {
 						case 0:
 							currencyCode = "EUR";
 							break;
 						case 1:
 							currencyCode = "USD";
 							break;
 						case 2:
 							currencyCode = "JPY";
 							break;
 						case 3:
 							currencyCode = "GBP";
 							break;
 						}
 						prefs.putString("currency", currencyCode);
 						if (prefs.commit()) {
 							Log.i(TAG, "Currency changed to: " + currencyCode);
 							new GetMovementsTask().execute(day);
 						}
 					}
 				});
 		return builder.create();
 	}
 
 	private DateSlider.OnDateSetListener mDateSetListener = new DateSlider.OnDateSetListener() {
 		public void onDateSet(DateSlider view, Calendar selectedDate) {
 			day = selectedDate;
 			dayButton.setText(DateFormat.getDateInstance()
 					.format(day.getTime()));
 			new GetMovementsTask().execute(day);
 		}
 	};
 
 	/**
 	 * Dialogs
 	 */
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DATE_PICKER_DIALOG:
			Dialog d = null;
 			if (period == Period.ONCE) {
				Log.d(TAG, "Default");
				d = new DefaultDateSlider(this, mDateSetListener, day);
 			}
 			if (period == Period.MONTHLY) {
				Log.d(TAG, "Month");
				d = new MonthYearDateSlider(this, mDateSetListener, day);
 			}
 			if (period == Period.YEARLY) {
				Log.d(TAG, "YEar");
				d = new YearDateSlider(this, mDateSetListener, day);
 			}
			// TODO check a better way of doing this (onpreparatedialog)
			d.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					removeDialog(DATE_PICKER_DIALOG);

				}
			});
			return d;
 		case DIALOG_SELECT_CURRENCY:
 			return getSelectCurrencyDialog();
 		}
 
 		return null;
 	}
 
	// TODO onpreparatedialog?
 	/**
 	 * Calculates and displays top categories.
 	 * 
 	 * @param mType
 	 *            Type of movements.
 	 */
 	private void setTop5Categories(MovementType mType) {
 		int i = 0;
 		if (mType == MovementType.INCOME) {
 			top5incomes.clear();
 			catIncomes = ModelUtilities.sortByValue(catIncomes);
 			for (Entry<String, Float> entry : catIncomes.entrySet()) {
 				if (i < TOP_CATEGORIES_MAX) {
 					top5incomes.add(entry);
 					i++;
 				} else {
 					break;
 				}
 			}
 		} else {
 			top5expenses.clear();
 			catExpenses = ModelUtilities.sortByValue(catExpenses);
 			for (Entry<String, Float> entry : catExpenses.entrySet()) {
 				if (i < TOP_CATEGORIES_MAX) {
 					top5expenses.add(entry);
 					i++;
 				} else {
 					break;
 				}
 			}
 		}
 		setTopCategoriesLabels(top5type);
 	}
 
 	/**
 	 * Fills labels of the top categories.
 	 * 
 	 * @param movType
 	 *            Type of movements.
 	 */
 	private void setTopCategoriesLabels(MovementType movType) {
 		List<Entry<String, Float>> topList;
 		if (movType == MovementType.INCOME) {
 			topList = top5incomes;
 		} else {
 			topList = top5expenses;
 		}
 		resetCategoryTop();
 		try {
 			setTopCategoryRow(R.id.topcat1, topList.get(0).getKey(),
 					R.id.topcat1_amount, topList.get(0).getValue());
 			setTopCategoryRow(R.id.topcat2, topList.get(1).getKey(),
 					R.id.topcat2_amount, topList.get(1).getValue());
 			setTopCategoryRow(R.id.topcat3, topList.get(2).getKey(),
 					R.id.topcat3_amount, topList.get(2).getValue());
 			setTopCategoryRow(R.id.topcat4, topList.get(3).getKey(),
 					R.id.topcat4_amount, topList.get(3).getValue());
 			setTopCategoryRow(R.id.topcat5, topList.get(4).getKey(),
 					R.id.topcat5_amount, topList.get(4).getValue());
 		} catch (IndexOutOfBoundsException e) {
 		}
 	}
 
 	/**
 	 * Fills a row of categories' top
 	 * 
 	 * @param textViewId1
 	 *            Id of name TextView.
 	 * @param cat
 	 *            Content of name TextView.
 	 * @param textViewId2
 	 *            Id of amount TextView.
 	 * @param amount
 	 *            Amount.
 	 */
 	private void setTopCategoryRow(int textViewId1, String cat,
 			int textViewId2, Float amount) {
 		TextView label, content;
 		label = (TextView) findViewById(textViewId1);
 		content = (TextView) findViewById(textViewId2);
 		if (amount != null) {
 			label.setText(cat);
 			ViewUtils.printAmount(getApplicationContext(), content, amount,
 					true);
 		} else {
 			label.setText("");
 			content.setText("");
 		}
 	}
 
 	/**
 	 * Resets top's TextViews.
 	 */
 	private void resetCategoryTop() {
 		setTopCategoryRow(R.id.topcat1, "", R.id.topcat1_amount, null);
 		setTopCategoryRow(R.id.topcat2, "", R.id.topcat2_amount, null);
 		setTopCategoryRow(R.id.topcat3, "", R.id.topcat3_amount, null);
 		setTopCategoryRow(R.id.topcat4, "", R.id.topcat4_amount, null);
 		setTopCategoryRow(R.id.topcat5, "", R.id.topcat5_amount, null);
 	}
 
 	/**
 	 * Fetches all movements from database and classify them.
 	 * 
 	 * @author Santiago Munín González.
 	 * 
 	 */
 	private class GetMovementsTask extends
 			AsyncTask<Calendar, Void, List<CashFlow>> {
 
 		@Override
 		protected List<CashFlow> doInBackground(Calendar... params) {
 			if (params.length == 0) {
 				return new ArrayList<CashFlow>();
 			}
 			Calendar day = GregorianCalendar.getInstance();
 			day.setTime(params[0].getTime());
 			Log.i(TAG, "Fetching movements day: " + day.getTime().toString()
 					+ " period: " + period.toString());
 			return cashService.getAllWithFilter(day, period, null, null);
 		}
 
 		@Override
 		protected void onPostExecute(List<CashFlow> result) {
 			super.onPostExecute(result);
 			resetCategoryTop();
 			catExpenses.clear();
 			catIncomes.clear();
 			Map<String, Float> catBalance = new HashMap<String, Float>();
 			float totalExpenses = 0;
 			float totalIncomes = 0;
 			float totalBalance = 0;
 
 			for (CashFlow cashFlow : result) {
 				float amount = cashFlow.getAmount();
 				String catName;
 				if (cashFlow.getCategory() != null) {
 					catName = cashFlow.getCategory().getName();
 				} else {
 					catName = getString(R.string.other);
 				}
 				if (cashFlow.getMovType() == MovementType.EXPENSE) {
 					totalExpenses += amount;
 					// Adds category data
 					if (catBalance.containsKey(catName)) {
 						catBalance.put(catName,
 								-amount + catBalance.get(catName));
 					} else {
 						catBalance.put(catName, -amount);
 					}
 					if (catExpenses.containsKey(catName)) {
 						catExpenses.put(catName, catExpenses.get(catName)
 								- amount);
 					} else {
 						catExpenses.put(catName, -amount);
 					}
 				} else {
 					totalIncomes += amount;
 					if (catBalance.containsKey(catName)) {
 						catBalance.put(catName,
 								amount + catBalance.get(catName));
 					} else {
 						catBalance.put(catName, amount);
 					}
 					if (catIncomes.containsKey(catName)) {
 						catIncomes.put(catName, catIncomes.get(catName)
 								+ amount);
 					} else {
 						catIncomes.put(catName, +amount);
 					}
 				}
 			}
 			ViewUtils.printAmount(getApplicationContext(), incomes,
 					totalIncomes, true);
 			// Don't print a negative amount if totalExpenses = 0
 			if (totalExpenses == 0) {
 				ViewUtils.printAmount(getApplicationContext(), expenses,
 						totalExpenses, true);
 			} else {
 				ViewUtils.printAmount(getApplicationContext(), expenses,
 						-totalExpenses, true);
 			}
 			totalBalance = totalIncomes - totalExpenses;
 			ViewUtils.printAmount(getApplicationContext(), balance,
 					totalBalance, true);
 			setTop5Categories(top5type);
 		}
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		switch (tab.getPosition()) {
 		case 0:
			Log.d(TAG, "Setting period to ONCE");
 			period = Period.ONCE;
 			break;
 		case 1:
			Log.d(TAG, "Setting period to MONTHLY");
 			period = Period.MONTHLY;
 			break;
 		case 2:
			Log.d(TAG, "Setting period to YEARLY");
 			period = Period.YEARLY;
 			break;
 		}
 		new GetMovementsTask().execute(day);
 
 	}
 
 	@Override
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 	}
 
 	@Override
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 	}
 }
