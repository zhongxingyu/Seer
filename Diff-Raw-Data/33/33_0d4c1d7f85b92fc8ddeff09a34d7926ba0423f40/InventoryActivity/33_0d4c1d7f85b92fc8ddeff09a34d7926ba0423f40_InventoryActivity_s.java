 package t4069.inventory;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.DialogInterface.OnShowListener;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class InventoryActivity extends Activity {
	private static final int SUP_DELIM = 0x01;
 	private static final String SAVE_FILE = "inventory_storage";
 	protected static final String ADD_NEW_CATEGORY = "Add a new category";
 	private Button addButton, removeButton, filterButton, resetFilterButton,
 			signOutButton, viewButton, editButton;
 	private String filterName = "", filterNumber = "", filterManufacturer = "",
 			filterCategory = "";
 	private int filterQuantity = -1;
 	private ListView partView;
 	private int selectedPartPosition = -1;
 	private Dialog filter, add, signOut, category;
 	private static final String TAG = "FRC Inventory";
 	private ArrayList<CharSequence> categoryList = new ArrayList<CharSequence>();
 	private ArrayList<Part> parts = new ArrayList<Part>();
 	private OnClickListener buttonListener = new OnClickListener() {
 		public void onClick(View v) {
 			switch (v.getId()) {
 			case R.id.button1: {
 				add.show();
 				break;
 			}
 			case R.id.button2: {
 				if (selectedPartPosition == -1)
 					return;
 				view();
 				break;
 			}
 			case R.id.button3: {
 				signOut();
 				break;
 			}
 			case R.id.button4: {
 				remove();
 				break;
 			}
 			case R.id.button5: {
 				View filter_layout = getLayoutInflater().inflate(
 						R.layout.filter_dialog, null);
 				filter(filter_layout);
 				break;
 			}
 			case R.id.button6: {
 				resetFilter();
 				break;
 			}
 			case R.id.button7: {
 				if (selectedPartPosition == -1
 						|| parts.get(selectedPartPosition) == null)
 					return;
 				edit(parts.get(selectedPartPosition));
 				break;
 			}
 			}
 		}
 	};

 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		categoryList.add("None");
		categoryList.add(ADD_NEW_CATEGORY);
 		setContentView(R.layout.activity_main);
 		partView = (ListView) findViewById(R.id.listView1);
 		partView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				selectedPartPosition = arg2;
 			}
 		});
 		refreshParts();
 		addButton = (Button) findViewById(R.id.button1);
 		removeButton = (Button) findViewById(R.id.button4);
 		viewButton = (Button) findViewById(R.id.button2);
 		signOutButton = (Button) findViewById(R.id.button3);
 		filterButton = (Button) findViewById(R.id.button5);
 		resetFilterButton = (Button) findViewById(R.id.button6);
 		editButton = (Button) findViewById(R.id.button7);
 		makeDialogs();
 		addButton.setOnClickListener(buttonListener);
 		removeButton.setOnClickListener(buttonListener);
 		viewButton.setOnClickListener(buttonListener);
 		signOutButton.setOnClickListener(buttonListener);
 		filterButton.setOnClickListener(buttonListener);
 		resetFilterButton.setOnClickListener(buttonListener);
 		editButton.setOnClickListener(buttonListener);
 		loadPreferences();
 	}
 
 	protected void signOut() {
 		Dialog signOut = new Dialog(this);
 		signOut.setTitle("Sign Out");
 	}
 
 	protected void view() {
 		final Dialog view = new Dialog(this);
 		View viewLayout = getLayoutInflater().inflate(R.layout.part_view, null);
 		view.setTitle("View Part");
 		view.setContentView(viewLayout);
 		final Part viewPart = (Part) parts.get(selectedPartPosition);
 		final TextView nameField = (TextView) viewLayout
 				.findViewById(R.id.nameViewView);
 		final TextView numberField = (TextView) viewLayout
 				.findViewById(R.id.numberViewView);
 		final TextView manufacturerField = (TextView) viewLayout
 				.findViewById(R.id.manufacturerViewView);
 		final TextView categoryField = (TextView) viewLayout
 				.findViewById(R.id.categoryViewView);
 		final TextView quantityField = (TextView) viewLayout
 				.findViewById(R.id.quantityViewView);
 		final Button editButton = (Button) viewLayout
 				.findViewById(R.id.buttonEditView);
 		nameField.setText(viewPart.name);
 		numberField.setText(viewPart.number);
 		manufacturerField.setText(viewPart.manufacturer);
 		categoryField.setText(viewPart.category);
 		quantityField.setText(viewPart.quantity);
 		editButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				edit(viewPart);
 				view.cancel();
 			}
 		});
 		view.show();
 	}
 
 	private void loadPreferences() {
 		try {
 			loadData();
 		} catch (IOException e) {
 			Toast.makeText(this, "Loading error!", Toast.LENGTH_LONG).show();
 			categoryList = new ArrayList<CharSequence>();
			categoryList.add(ADD_NEW_CATEGORY);
			categoryList.add("None");
 			parts = new ArrayList<Part>();
 		}
 		refreshParts();
 	}
 
 	private void makeDialogs() {
 		LayoutInflater inflater = getLayoutInflater();
 		View category_layout = inflater.inflate(R.layout.category_dialog, null);
 		makeCategoryDialog(category_layout);
 		View add_layout = inflater.inflate(R.layout.add_dialog, null);
 		makeAddDialog(add_layout);
 	}
 
 	private static final String DO_NOT_FILTER = "Not filtered";
 
 	private void filter(View filter_layout) {
 		filter = new Dialog(this);
 		filter.setTitle("Inventory Filter");
 		filter.setContentView(filter_layout);
 		((Button) filter_layout.findViewById(R.id.closeButtonFilter))
 				.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						filter.cancel();
 					}
 				});
 		final EditText filterNameField = (EditText) filter_layout
 				.findViewById(R.id.nameFieldFilter);
 		final EditText filterNumberField = (EditText) filter_layout
 				.findViewById(R.id.numberFieldFilter);
 		final EditText filterManufacturerField = (EditText) filter_layout
 				.findViewById(R.id.manufacturerFieldFilter);
 		final EditText filterQuantityField = (EditText) filter_layout
 				.findViewById(R.id.quantityFieldFilter);
 		final Spinner categorySpinnerFilter = (Spinner) filter_layout
 				.findViewById(R.id.categorySpinnerFilter);
 		ArrayList<CharSequence> cats = new ArrayList<CharSequence>();
 		cats.add(DO_NOT_FILTER);
 		cats.addAll(categoryList);
 		cats.remove(ADD_NEW_CATEGORY);
 		categorySpinnerFilter.setAdapter(new ArrayAdapter<CharSequence>(this,
 				android.R.layout.simple_expandable_list_item_1, cats));
 		Button filterButton = (Button) filter_layout
 				.findViewById(R.id.saveButtonFilter);
 		filterButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				filterName = filterNameField.getText().toString();
 				filterNumber = filterNumberField.getText().toString();
 				filterManufacturer = filterManufacturerField.getText()
 						.toString();
 				filterCategory = (String) (categorySpinnerFilter
 						.getSelectedItem().equals(DO_NOT_FILTER) ? ""
 						: categorySpinnerFilter.getSelectedItem());
 				filterQuantity = parseInt(filterQuantityField.getText()
 						.toString());
 
 				filter.cancel();
 				refreshParts();
 			}
 		});
 		filter.show();
 	}
 
 	private ArrayList<CharSequence> getModifiableCategories() {
 		ArrayList<CharSequence> mCategories = (ArrayList<CharSequence>) categoryList
 				.clone();
 		mCategories.remove("None");
 		mCategories.remove(ADD_NEW_CATEGORY);
 		return mCategories;
 	}
 
 	String selectedCategory;

 	private void makeCategoryDialog(View category_layout) {
 		category = new Dialog(this);
 		category.setTitle("Category");
 		category.setContentView(category_layout);
 		final EditText categoryNameField = (EditText) category_layout
 				.findViewById(R.id.categoryNameField);
 		final ListView categoryListView = (ListView) category_layout
 				.findViewById(R.id.categoryListView);
 		final Button addButton = (Button) category_layout
 				.findViewById(R.id.addCategoryButton);
 		final Button deleteButton = (Button) category_layout
 				.findViewById(R.id.deleteCategoryButton);
 		category.setOnShowListener(new OnShowListener(){
 			public void onShow(DialogInterface dialog) {
 				categoryListView.setAdapter(new ArrayAdapter<CharSequence>(getApplicationContext(),
 						android.R.layout.simple_expandable_list_item_1,
 						getModifiableCategories()));
 			}
 		});
 		categoryListView.requestFocus();
 		categoryListView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				selectedCategory = (String) arg0.getItemAtPosition(arg2);
 			}
 		});
 		((Button) category_layout.findViewById(R.id.cancelCategoryButton))
 				.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						category.cancel();
 					}
 				});
 		addButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				final String name = categoryNameField.getText().toString();
				categoryList.add(name);
 				categoryListView.setAdapter(new ArrayAdapter<CharSequence>(
 						getApplicationContext(),
 						android.R.layout.simple_expandable_list_item_1,
 						getModifiableCategories()));
 			}
 		});
 		deleteButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				categoryListView.setAdapter(new ArrayAdapter<CharSequence>(
 						getApplicationContext(),
 						android.R.layout.simple_expandable_list_item_1,
 						getModifiableCategories()));
 
 				if (selectedCategory == null) {
 					Toast.makeText(getApplicationContext(),
 							"No category selected!", Toast.LENGTH_SHORT).show();
 					return;
 				}
 				for (int i = 0; i < categoryList.size(); i++) {
 					if (selectedCategory.equals(categoryList.get(i))) {
 						categoryList.remove(i);
 						break;
 					}
 				}
 				categoryListView.setAdapter(new ArrayAdapter<CharSequence>(
 						getApplicationContext(),
 						android.R.layout.simple_expandable_list_item_1,
 						getModifiableCategories()));
 			}
 		});
 	}
 
 	private void makeAddDialog(View add_layout) {
 		add = new Dialog(this);
 		add.setTitle("Add Part");
 		add.setContentView(add_layout);
 		final Spinner addCategories = (Spinner) add_layout
 				.findViewById(R.id.spinner1);
 		addCategories.setAdapter((new ArrayAdapter<CharSequence>(
 				getApplicationContext(),
 				android.R.layout.simple_expandable_list_item_1, categoryList)));
 		category.setOnCancelListener(new OnCancelListener() {
 			public void onCancel(DialogInterface dialog) {
 				addCategories.setAdapter((new ArrayAdapter<CharSequence>(
 						getApplicationContext(),
 						android.R.layout.simple_expandable_list_item_1,
 						categoryList)));
 				for (int i = 0; i < categoryList.size(); i++) {
 				if (addCategories.getItemAtPosition(i).equals("None")) {
 					addCategories.setSelection(i);
 				}}
 			}
 		});
 		final Button m_addButton = (Button) add_layout
 				.findViewById(R.id.addDialogButton1);
 		m_addButton.setText("Add");
 		final Button m_cancelButton = (Button) add_layout
 				.findViewById(R.id.addDialogButton2);
 		final EditText nameField = (EditText) add_layout
 				.findViewById(R.id.editText1);
 		final EditText partField = (EditText) add_layout
 				.findViewById(R.id.editText2);
 		final EditText manufacturerField = (EditText) add_layout
 				.findViewById(R.id.editText3);
 		final EditText quantityField = (EditText) add_layout
 				.findViewById(R.id.editText4);
 		m_addButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
				if (nameField.getText().toString() == null
						&& partField.getText().toString() == null) {
 					Toast.makeText(getApplicationContext(), "No name or ID!",
 							Toast.LENGTH_SHORT).show();
 					return;
 				}
 				String name = "";
 				String manufacturer = "";
 				String partNum = "";
 				String category;
 				name = nameField.getText().toString();
 				manufacturer = manufacturerField.getText().toString();
 				partNum = partField.getText().toString();
 				category = addCategories.getSelectedItem().toString();
 				if (parts.contains(new Part(name, partNum, manufacturer,
 						category))) {
 					Toast.makeText(getApplicationContext(),
 							"Part already exists!", Toast.LENGTH_SHORT).show();
 					refreshParts();
 					return;
 				}
 				Part newPart = new Part(name, partNum, manufacturer, category);
 				int quantity = parseInt(quantityField.getText().toString());
 				newPart.quantity = (quantity == Integer.MIN_VALUE ? 0
 						: quantity);
 				parts.add(newPart);
 				refreshParts();
 				add.dismiss();
 			}
 		});
 		m_cancelButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				add.dismiss();
 			}
 		});
 		addCategories.setOnItemSelectedListener(new OnItemSelectedListener() {
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				String categoryname = (String) addCategories.getSelectedItem();
 				if (categoryname.equals(ADD_NEW_CATEGORY)) {
 					addCategories.setSelection(0);
 					category.show();
 				}
 			}
 
 			public void onNothingSelected(AdapterView<?> arg0) {
 			}
 
 		});
 	}
 
 	protected void remove() {
 		if (selectedPartPosition == -1) {
 			Toast.makeText(this, "No part selected!", Toast.LENGTH_SHORT)
 					.show();
 			return;
 		}
 		Log.d(TAG, partView.getAdapter().getItem(selectedPartPosition)
 				.getClass().toString());
 		Part removePart = (Part) parts.get(selectedPartPosition);
 		parts.remove(removePart);
 		refreshParts();
 	}
 
 	protected void resetFilter() {
 		filterName = "";
 		filterCategory = "";
 		filterNumber = "";
 		filterManufacturer = "";
 		filterQuantity = -1;
 		refreshParts();
 	}
 
 	protected void filter() {
 		filter.show();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	final String[] keys = new String[] { "Name", "Number" };
 
 	protected void refreshParts() {/*
 									 * List<Map<String, String>> data = new
 									 * ArrayList<Map<String, String>>(); for
 									 * (Part part : parts) { Map<String, String>
 									 * datum = new HashMap<String,
 									 * String>(keys.length); datum.put(keys[0],
 									 * part.name); datum.put(keys[1],
 									 * part.number); data.add(datum); }
 									 */
 		SimpleAdapter viewAdapter = filter(parts);
 		selectedPartPosition = -1;
 		partView.setAdapter(viewAdapter);
 		/*
 		 * if (filterName.equals("") && filterCategory.equals("")&&
 		 * filterNumber.equals("") && filterManufacturer.equals("") &&
 		 * filterQuantity == -1) { viewAdapter = new
 		 * SimpleAdapter(getApplicationContext(), data,
 		 * android.R.layout.simple_list_item_2, keys, new int[] {
 		 * android.R.id.text1, android.R.id.text2 }); } else viewAdapter =
 		 * filter(parts); partView.setAdapter(viewAdapter); selectedPartPosition
 		 * = -1;
 		 */
 	}
 	public void onResume() {
 		super.onResume();
 		try {loadData();} catch (Exception e) {}
 	}
 
 	@SuppressWarnings({ "unchecked", "unused" })
 	private SimpleAdapter filter(ArrayList<Part> list) {
 		ArrayList<Part> parts = (ArrayList<Part>) this.parts.clone();
 		boolean numberFiltered = filterNumber != null;
 		boolean manufacturerFiltered = filterManufacturer != null, categoryFiltered = filterCategory != null;
 		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
 		for (Part part : parts) {
 			if ((filterName.length() != 0 && !part.name
 					.equalsIgnoreCase(filterName))
 					|| (filterNumber.length() != 0 && !part.number
 							.equalsIgnoreCase(filterNumber))
 					|| (filterManufacturer.length() != 0 && !part.manufacturer
 							.equalsIgnoreCase(filterManufacturer))
 					|| (filterCategory.length() != 0 && !part.category
 							.equalsIgnoreCase(filterCategory))
 					|| (filterQuantity != -1 && filterQuantity != part.quantity)) {
 				parts.remove(part);
 				continue;
 			}
 			Map<String, String> datum = new HashMap<String, String>(keys.length);
 			datum.put(keys[0], part.name);
 			datum.put(keys[1], part.number);
 			data.add(datum);
 		}
 
 		SimpleAdapter viewAdapter = new SimpleAdapter(getApplicationContext(),
 				data, android.R.layout.simple_list_item_2, keys, new int[] {
 						android.R.id.text1, android.R.id.text2 });
 		return viewAdapter;
 	}
 
 	private void saveData() throws IOException {
 		Log.i(TAG, getFilesDir().toString());
 		getApplicationContext().deleteFile(SAVE_FILE);
 		FileOutputStream saveFile = getApplicationContext().openFileOutput(SAVE_FILE, MODE_WORLD_READABLE);
 		OutputStreamWriter writer = new OutputStreamWriter(saveFile);
 		String finalStr = "";
 		writer.write(SUP_DELIM);
 		for (CharSequence cat : categoryList) {
 			if (!cat.equals(ADD_NEW_CATEGORY) && !cat.equals("None")) {
 				writer.write(cat.toString());
 				writer.write(',');
 			}
 		}
 		writer.write(SUP_DELIM);
 		for (Part p : parts) {
 			writer.write(p.name);
 			writer.write(',');
 			writer.write(p.number);
 			writer.write(',');
 			writer.write(p.manufacturer);
 			writer.write(',');
 			writer.write(p.category);
 			writer.write(',');
 			writer.write(((Integer)p.quantity).toString());
 			writer.write('\n');
 		}
 		writer.write(SUP_DELIM);
 		writer.flush();
 		writer.close();
 	}
 
 	private void loadData() throws IOException {
 		FileInputStream loadFile = getApplicationContext().openFileInput(SAVE_FILE);
 		InputStreamReader reader = new InputStreamReader(loadFile);
 		char[] chars = new char[4096];
 		if (reader.read() != SUP_DELIM) {
 			throw new IOException("Part database not valid!");
 		}
 		reader.read(chars);
 		int offset = 0;
 		char ch;
 		String cat = "";
 		while ((ch = chars[offset]) != SUP_DELIM) {
 			if (ch == ',') {
				categoryList.add(cat);
 				cat = "";
 			} else {
 				cat += ch;
 			}
 			offset++;
 		}
 		System.arraycopy(chars, offset, chars, 0, chars.length - offset - 1);
 		offset = 0;
 		final int NAME = 0, NUM = 1, MAN = 2, CAT = 3, QUANT = 4;
 		int q = 0;
 		String name = "", number = "", maker = "", category = "", quant = "";
 		while ((ch = chars[offset]) != SUP_DELIM) {
 			if (ch == ',') {
 				q++;
 			}
 			if (ch == '\n') {
 				parts.add(new Part(name, number, maker, category,
 						parseInt(quant)));
 				q = 0;
 				name = "";
 				number = "";
 				maker = "";
 				category = "";
 				quant = "";
 			}
 			switch (q) {
 			case NAME:
 				name += ch;
 				break;
 			case NUM:
 				number += ch;
 				break;
 			case MAN:
 				maker += ch;
 				break;
 			case CAT:
 				category += ch;
 				break;
 			case QUANT:
 				quant += ch;
 				break;
 			default:
 				throw new IOException("Part database not valid!");
 			}
 		}
 	}
 
 	public void onPause() {
 		super.onPause();
 		try {
 			saveData();
 		} catch (IOException e) {
 			Toast.makeText(this,"Saving failed!", Toast.LENGTH_LONG).show();
 			e.printStackTrace();
 		}
 	}
 
 	private void edit(final Part viewPart) {
 		final Dialog editDialog = new Dialog(this);
 		View layout = getLayoutInflater().inflate(R.layout.add_dialog, null);
 		editDialog.setContentView(layout);
 		editDialog.setTitle("Edit");
 		final Button m_saveButton = (Button) layout
 				.findViewById(R.id.addDialogButton1);
 		final Button m_cancelButton = (Button) layout
 				.findViewById(R.id.addDialogButton2);
 		m_saveButton.setText("Save");
 		final EditText nameField = (EditText) layout
 				.findViewById(R.id.editText1);
 		final EditText numberField = (EditText) layout
 				.findViewById(R.id.editText2);
 		final EditText manufacturerField = (EditText) layout
 				.findViewById(R.id.editText3);
 		final EditText quantityField = (EditText) layout
 				.findViewById(R.id.editText4);
 		nameField.setText(viewPart.name);
 		numberField.setText(viewPart.number);
 		manufacturerField.setText(viewPart.manufacturer);
 		quantityField.setText(((Integer) viewPart.quantity).toString());
 		final Spinner addCategories = (Spinner) layout
 				.findViewById(R.id.spinner1);
 		addCategories.setAdapter((new ArrayAdapter<CharSequence>(
 				getApplicationContext(),
 				android.R.layout.simple_expandable_list_item_1, categoryList)));
 		for (int i = 0; i < categoryList.size(); i++) {
 			CharSequence category = categoryList.get(i);
 			if (category.equals(viewPart.category)) {
 				addCategories.setSelection(i);
 			}
 		}
 		m_saveButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				int quantity = parseInt(quantityField.getText().toString());
 				parts.remove(viewPart);
 				Part addPart = new Part(nameField.getText().toString(),
 						numberField.getText().toString(), manufacturerField
 								.getText().toString(), (String) addCategories
 								.getSelectedItem());
 				if (quantity != Integer.MIN_VALUE)
 					addPart.quantity = quantity;
 				parts.add(addPart);
 				refreshParts();
 				editDialog.cancel();
 			}
 		});
 		m_cancelButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				editDialog.cancel();
 			}
 		});
 		editDialog.show();
 	}
 
 	protected int parseInt(String string) {
 		try {
 			if (string.length() == 0)
 				throw new RuntimeException();
 			return Integer.parseInt(string.trim());
 		} catch (Exception e) {
 			return Integer.MIN_VALUE;
 		}
 	}
 
 	class Part {
 		int quantity, signedOut;
 		String name;
 		String number;
 		String manufacturer;
 		String category;
 
 		public Part(String name, String number, String manufacturer,
 				String category) {
 			this.name = name;
 			this.number = number;
 			this.manufacturer = manufacturer;
 			setStr("category", category);
 			quantity = 0;
 		}
 
 		public Part(String name, String number, String manufacturer,
 				String category, int quantity) {
 			this(name, number, manufacturer, category);
 			this.quantity = quantity;
 		}
 
 		public boolean equals(Object other) {
 			if (other == this)
 				return true;
 			if (!(other instanceof Part))
 				return false;
 			Part otherPart = (Part) other;
 			if (otherPart.name.equals(name) && otherPart.number.equals(number)) {
 				return true;
 			}
 			return false;
 		}
 
 		public void setStr(String prop, String val) {
 			if (prop.equalsIgnoreCase("name"))
 				name = val;
 			else if (prop.equalsIgnoreCase("number"))
 				number = val;
 			else if (prop.equalsIgnoreCase("manufacturer"))
 				manufacturer = val;
 			else if (prop.equalsIgnoreCase("quantity")) {
 				try {
 					if (val.length() != 0)
 						quantity = Integer.parseInt(val);
 				} catch (Exception e) {
 				}
 			} else if (prop.equalsIgnoreCase("category")) {
 				String category = new String(val);
 				for (CharSequence cat : categoryList) {
 					if (category.equals(cat))
 						this.category = (String) cat;
 				}
 			} else
 				throw new IllegalArgumentException("Property " + prop
 						+ " doesn't exist!");
 		}
 
 	}
 
 }
