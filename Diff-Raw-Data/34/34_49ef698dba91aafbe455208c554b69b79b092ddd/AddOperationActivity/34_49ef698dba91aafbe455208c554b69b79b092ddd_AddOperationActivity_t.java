 package es.udc.santiago.view.cashflows;
 
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnDismissListener;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TableRow;
 import android.widget.Toast;
 
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 
 import es.udc.santiago.R;
 import es.udc.santiago.model.backend.DatabaseHelper;
 import es.udc.santiago.model.exceptions.DuplicateEntryException;
 import es.udc.santiago.model.exceptions.EntryNotFoundException;
 import es.udc.santiago.model.facade.CashFlow;
 import es.udc.santiago.model.facade.CashFlowService;
 import es.udc.santiago.model.facade.Category;
 import es.udc.santiago.model.facade.CategoryService;
 import es.udc.santiago.model.facade.MovementType;
 import es.udc.santiago.model.facade.Period;
 
 /**
  * From this activity users will be able to insert expenses or incomes.
  * 
  * @author Santiago Munín González
  * 
  */
 public class AddOperationActivity extends OrmLiteBaseActivity<DatabaseHelper> {
 	private static final String TAG = "Add operation";
 	private static final int DATE_DIALOG_ID = 0;
 	private static final int END_DATE_DIALOG_ID = 1;
 	private static final int NEW_CATEGORY_DIALOG_ID = 2;
 	private int newCategoryPosition = -1;
 	private String newCategory = "";
 	private int dateYear;
 	private int dateMonth;
 	private int dateDay;
 	private int endDateYear = -1;
 	private int endDateMonth = -1;
 	private int endDateDay = -1;
 	private OnDismissListener newCategoryAdded;
 	private DatePickerDialog.OnDateSetListener dateListener;
 	private DatePickerDialog.OnDateSetListener endDateListener;
 	private List<Category> categoryList;
 	private Spinner category;
 	private EditText concept;
 	private Spinner movementType;
 	private Spinner period;
 	private EditText amount;
 	private Button button;
 	private CategoryService catServ;
 	private CashFlowService cashServ;
 	private Button dateButton;
 	private Button endDateButton;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add_operation);
 		newCategoryAdded = new OnDismissListener() {
 			@Override
 			public void onDismiss(DialogInterface dialog) {
 				fillCategorySpinner();
 			}
 		};
 		dateListener = new DatePickerDialog.OnDateSetListener() {
 			@Override
 			public void onDateSet(DatePicker view, int year, int monthOfYear,
 					int dayOfMonth) {
 				dateYear = year;
 				dateMonth = monthOfYear;
 				dateDay = dayOfMonth;
 				Calendar d = new GregorianCalendar(year, monthOfYear,
 						dayOfMonth);
 				dateButton.setText(DateFormat.getDateInstance().format(
 						d.getTime()));
 			}
 		};
 		endDateListener = new DatePickerDialog.OnDateSetListener() {
 			@Override
 			public void onDateSet(DatePicker view, int year, int monthOfYear,
 					int dayOfMonth) {
 				endDateYear = year;
 				endDateMonth = monthOfYear;
 				endDateDay = dayOfMonth;
 				Calendar endDate = new GregorianCalendar(year, monthOfYear,
 						dayOfMonth);
 				endDateButton.setText(DateFormat.getDateInstance().format(
 						endDate.getTime()));
 			}
 		};
 		try {
 			catServ = new CategoryService(getHelper());
 			cashServ = new CashFlowService(getHelper());
 		} catch (SQLException e) {
 			// TODO
 		}
 		initializeViews();
 
 	}
 
 	/**
 	 * Builds the new category's dialog.
 	 * 
 	 * @return Dialog instance.
 	 */
 	private Dialog getNewCategoryDialog() {
 		final Dialog dialog = new Dialog(this);
 
 		dialog.setContentView(R.layout.add_category_dialog);
 		dialog.setTitle(getString(R.string.new_category_dialog));
 		((Button) dialog.findViewById(R.id.dialog_add_cat_button))
 				.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						String categoryName = ((EditText) dialog
 								.findViewById(R.id.dialog_category_name))
 								.getText().toString();
 						if (categoryName.length() > 0) {
 							try {
 								Category c = new Category(-1, categoryName);
 								Long id = catServ.add(c);
 								categoryList.add(catServ.get(id));
 								newCategory = categoryName;
 								dialog.dismiss();
 							} catch (DuplicateEntryException e) {
 								Toast.makeText(
 										getApplicationContext(),
 										getString(R.string.error_cat_alreadyExists),
 										Toast.LENGTH_SHORT).show();
 							} catch (EntryNotFoundException e) {
 								// Can't reach here
 							}
 						}
 					}
 				});
 		dialog.setOnDismissListener(newCategoryAdded);
 		return dialog;
 	}
 
 	/**
 	 * Date picker's dialogs
 	 */
 	protected Dialog onCreateDialog(int id) {
 		Calendar d = GregorianCalendar.getInstance();
 
 		switch (id) {
 		case DATE_DIALOG_ID:
 			return new DatePickerDialog(this, dateListener,
 					d.get(Calendar.YEAR), d.get(Calendar.MONTH),
 					d.get(Calendar.DATE));
 
 		case END_DATE_DIALOG_ID:
 			return new DatePickerDialog(this, endDateListener,
 					d.get(Calendar.YEAR), d.get(Calendar.MONTH),
 					d.get(Calendar.DATE));
 
 		case NEW_CATEGORY_DIALOG_ID:
 			return getNewCategoryDialog();
 		}
 		return null;
 	}
 
 	private void fillCategorySpinner() {
 		List<String> data = new ArrayList<String>();
 		categoryList = catServ.getAll();
 		newCategoryPosition = -1;
 		int i = 0;
 		if (categoryList.size() > 0) {
			Collections.sort(categoryList, new Comparator<Category>() {
				@Override
				public int compare(Category lhs, Category rhs) {
					return lhs.getName().compareToIgnoreCase(rhs.getName());
				}
			});
 			for (Category c : categoryList) {
 				String name = c.getName();
 				data.add(name);
 				// If there is a new category, it will be selected.
 				if (newCategory.equals(name)) {
 					newCategoryPosition = i;
 				}
 				i++;
 			}
 		} else {
 			data.add("");
 		}
 		data.add(getString(R.string.new_category));
 		category.setAdapter(new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, data));
 		if (newCategoryPosition >= 0) {
 			category.setSelection(newCategoryPosition, true);
 		} else {
 			category.setSelection(0);
 		}
 	}
 
 	/**
 	 * Initializes views.
 	 */
 	private void initializeViews() {
 		category = (Spinner) findViewById(R.id.addOp_catSpinner);
 		fillCategorySpinner();
 		category.setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				if (arg0.getSelectedItem().toString()
 						.equals(getString(R.string.new_category))) {
 					showDialog(NEW_CATEGORY_DIALOG_ID);
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 			}
 		});
 		concept = (EditText) findViewById(R.id.addOp_conceptEntry);
 		movementType = (Spinner) findViewById(R.id.addOp_movTypeSpinner);
 		List<String> data = new ArrayList<String>();
 		data = new ArrayList<String>();
 		data.add(getString(R.string.spend));
 		data.add(getString(R.string.income));
 		movementType.setAdapter(new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, data));
 		period = (Spinner) findViewById(R.id.addOp_periodSpinner);
 		data = new ArrayList<String>();
 		data.add(getString(R.string.once));
 		data.add(getString(R.string.monthly));
 		data.add(getString(R.string.yearly));
 		period.setAdapter(new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, data));
 		period.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				switch (arg0.getSelectedItemPosition()) {
 				case 0:
 					((TableRow) findViewById(R.id.addOp_endDateRow))
 							.setVisibility(View.INVISIBLE);
 					break;
 				default:
 					((TableRow) findViewById(R.id.addOp_endDateRow))
 							.setVisibility(View.VISIBLE);
 					break;
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 			}
 		});
 
 		amount = (EditText) findViewById(R.id.addOp_amountEntry);
 		dateYear = Calendar.getInstance().get(Calendar.YEAR);
 		dateMonth = Calendar.getInstance().get(Calendar.MONTH);
 		dateDay = Calendar.getInstance().get(Calendar.DATE);
 		dateButton = (Button) findViewById(R.id.addOp_dateButton);
 		dateButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showDialog(DATE_DIALOG_ID);
 
 			}
 		});
 		endDateButton = (Button) findViewById(R.id.addOp_endDateButton);
 		endDateButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showDialog(END_DATE_DIALOG_ID);
 			}
 		});
 		button = (Button) findViewById(R.id.addOp_button);
 		button.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				if (checkRequiredFieldsFilled()) {
 					try {
 						Period p = Period.getFromCode(period
 								.getSelectedItemPosition());
 						MovementType mov = MovementType
 								.getFromCode(movementType
 										.getSelectedItemPosition());
 						// TODO check spinners
 						Category c;
						if (category.getSelectedItemPosition() < 0 || categoryList.size()==0) {
 							c = null;
 						} else {
 							c = categoryList.get(category
 									.getSelectedItemPosition());
 						}
 
 						if (c == null) {
 							Toast.makeText(getApplicationContext(),
 									R.string.not_category_selected,
 									Toast.LENGTH_SHORT).show();
 							return;
 						}
 
 						Calendar date = new GregorianCalendar(dateYear,
 								dateMonth, dateDay);
 						Calendar endDate = (p != Period.ONCE) ? new GregorianCalendar(
 								endDateYear, endDateMonth, endDateDay) : null;
 						if (endDate != null) {
 							if (date.after(endDate)) {
 								Toast.makeText(getApplicationContext(),
 										R.string.endDate_before_date,
 										Toast.LENGTH_SHORT).show();
 								return;
 							}
 						}
 						CashFlow cf;
						try {
 						if (endDate != null) {
 							cf = new CashFlow(-1, concept.getText().toString(),
 									Float.valueOf(amount.getText().toString()),
 									c, date.getTime(), endDate.getTime(), p,
 									mov);
 						} else {
 							cf = new CashFlow(-1, concept.getText().toString(),
 									Float.valueOf(amount.getText().toString()),
 									c, date.getTime(), null, p, mov);
 						}
 						cashServ.add(cf);
 						Log.i(TAG, "Added cashflow");
						} catch(NumberFormatException nf) {
							Toast.makeText(getApplicationContext(), R.string.bad_amount,
									Toast.LENGTH_SHORT).show();
							return;
						}

						
 						Toast.makeText(getApplicationContext(), R.string.added,
 								Toast.LENGTH_SHORT).show();
 					} catch (DuplicateEntryException e) {
 					}
 				} else {
 					Toast.makeText(getApplicationContext(),
 							R.string.error_fieldsNotFilled, Toast.LENGTH_SHORT)
 							.show();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Checks if required entries are correct and contain any value.
 	 * 
 	 * @return boolean.
 	 */
 	private boolean checkRequiredFieldsFilled() {
 		if (category == null || movementType == null || period == null
 				|| amount == null) {
 			return false;
 		}
 		return true;
 	}
 }
