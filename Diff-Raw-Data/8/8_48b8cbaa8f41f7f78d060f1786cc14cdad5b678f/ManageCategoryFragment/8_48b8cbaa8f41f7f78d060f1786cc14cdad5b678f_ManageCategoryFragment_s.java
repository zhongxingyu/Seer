 package it.chalmers.mufasa.android_budget_app.activities;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.List;
 
 import it.chalmers.mufasa.android_budget_app.R;
 import it.chalmers.mufasa.android_budget_app.controller.ManageCategoryController;
 import it.chalmers.mufasa.android_budget_app.model.Category;
 import it.chalmers.mufasa.android_budget_app.model.ManageCategoryModel;
 import android.os.Bundle;
 import android.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * version 1
  * 
  * @author FannyM Activity Class to manage categories depending on the user
  *         input.
  */
 
 public class ManageCategoryFragment extends Fragment implements
 		PropertyChangeListener {
 
 	private ManageCategoryController controller;
 	private ManageCategoryModel model;
 	private LayoutInflater inflater;
 	private View view;
 	private View addView;
 	private Button incomeButton;
 	private Button expenseButton;
 	private Button editSaveButton;
 	private Button addButton;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		this.inflater = inflater;
 		this.view = inflater.inflate(R.layout.activity_manage_category,
 				container, false);
 		this.addView = inflater.inflate(
 				R.layout.manage_categories_add_category, container, false);
 		this.model = new ManageCategoryModel(this.getActivity());
 		this.controller = new ManageCategoryController(model);
 		model.addPropertyChangeListener(this);
 
 		this.populateCategoryListView(model.getCategoryList());
 
 		this.setupOnClickListeners();
 
 		return view;
 	}
 
 	private void setupOnClickListeners() {
 		incomeButton = (Button) view
 				.findViewById(R.id.manageCategoryButtonIncome);
 		expenseButton = (Button) view
 				.findViewById(R.id.manageCategoryButtonExpense);
 		editSaveButton = (Button) view
 				.findViewById(R.id.manageCategoryButtonEditSave);
 		addButton = (Button) addView.findViewById(R.id.manageCategoryButtonAdd);
 
 		incomeButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				ManageCategoryFragment.this.toggleIncomeExpense();
 			}
 		});
 
 		expenseButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				ManageCategoryFragment.this.toggleIncomeExpense();
 			}
 		});
 
 		editSaveButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				ManageCategoryFragment.this.toggleEditSave();
 			}
 		});
 
 		addButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				System.out.println("trying to add category");
 				EditText editTextname = (EditText) addView
 						.findViewById(R.id.manageCategoryEditTextAdd);
 				String nameString = editTextname.getText().toString();
 				ManageCategoryFragment.this.addCategory(nameString);
 			}
 		});
 	}
 
 	/**
 	 * a method to add categories to the list view in different ways depending
 	 * on save or edit mode.
 	 * 
 	 * @param list
 	 */
 	private void populateCategoryListView(List<Category> list) {
 		LinearLayout categoryListLayout = (LinearLayout) view
 				.findViewById(R.id.manageCategoryLayout);
 		categoryListLayout.removeAllViews();
 
 		if (model.isEditMode()) {
 			for (Category cat : list) {
 				View v = this.inflater.inflate(R.layout.category_list_row_edit,
 						null);
				v.setTag(cat);
 				EditText categoryEditText = (EditText) v
 						.findViewById(R.id.manageCategoryCategoryEditText);
 				categoryEditText.setText(cat.getName());
 
 				Button removeButton = (Button) v
 						.findViewById(R.id.manageActivityButtonRemove);

 				removeButton.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						if (v.getTag() instanceof Category) {
							Category category = (Category) view.getTag();
 							ManageCategoryFragment.this
 									.removeCategory(category);
 						}
 					}
 				});
 
 				categoryListLayout.addView(v);
 			}
 
 			categoryListLayout.addView(addView);
 		} else {
 			for (Category cat : list) {
 				View v = inflater.inflate(R.layout.category_list_row, null);
 
 				TextView categoryTextView = (TextView) v
 						.findViewById(R.id.manageCategoryCategoryTextView);
 				categoryTextView.setText(cat.getName());
 				categoryListLayout.addView(v);
 			}
 		}
 	}
 
 	/**
 	 * a method to add a category
 	 * 
 	 * @param view
 	 */
 
 	public void addCategory(String name) {
 		controller.addCategory(name, model.getCurrentParentCategory());
 	}
 
 	/**
 	 * a method to remove a category
 	 * 
 	 * @param category
 	 */
 	public void removeCategory(Category category) {
 		controller.removeCategory(category);
 	}
 
 	/**
 	 * a method to switch between income and expenses in the shown list
 	 */
 	public void toggleIncomeExpense() {
 		if (model.getCurrentParentCategory().getId() == 1) {
 			expenseButton.setEnabled(false);
 			incomeButton.setEnabled(true);
 			controller.setCurrentParentCategory(2);
 		} else {
 			incomeButton.setEnabled(false);
 			expenseButton.setEnabled(true);
 			controller.setCurrentParentCategory(1);
 		}
 	}
 
 	/**
 	 * a method to switch between save and edit mode
 	 */
 	public void toggleEditSave() {
 		if (model.isEditMode()) {
 			controller.setEditMode(false);
 
 			editSaveButton.setText(R.string.edit);
 		} else {
 			controller.setEditMode(true);
 			editSaveButton.setText(R.string.save);
 		}
 	}
 
 	/**
 	 * a method that listens for property changes from the model.
 	 */
 	public void propertyChange(PropertyChangeEvent event) {
 		if (event.getPropertyName().equals("added_category")) {
 			populateCategoryListView(model.getCategoryList());
 		} else if (event.getPropertyName().equals("removed_category")) {
 			populateCategoryListView(model.getCategoryList());
 		} else if (event.getPropertyName().equals("edited_category")) {
 			populateCategoryListView(model.getCategoryList());
 		} else if (event.getPropertyName().equals("changed_parent_category")) {
 			populateCategoryListView(model.getCategoryList());
 		} else if (event.getPropertyName().equals("changed_editmode")) {
 			populateCategoryListView(model.getCategoryList());
 		}
 	}
 }
