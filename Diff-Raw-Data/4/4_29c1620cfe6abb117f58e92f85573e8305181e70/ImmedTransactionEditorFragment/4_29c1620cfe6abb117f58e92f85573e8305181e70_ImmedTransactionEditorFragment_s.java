 /*******************************************************************************
  * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
  * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
  ******************************************************************************/
 package net.alexjf.tmm.fragments;
 
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import net.alexjf.tmm.R;
 import net.alexjf.tmm.activities.CategoryListActivity;
 import net.alexjf.tmm.activities.MoneyNodeListActivity;
 import net.alexjf.tmm.domain.Category;
 import net.alexjf.tmm.domain.ImmediateTransaction;
 import net.alexjf.tmm.domain.MoneyNode;
 import net.alexjf.tmm.exceptions.DatabaseException;
 import net.alexjf.tmm.utils.DrawableResolver;
 import net.alexjf.tmm.utils.Utils;
 import net.alexjf.tmm.views.SelectorButton;
 import net.alexjf.tmm.views.SignToggleButton;
 
 import android.app.Activity;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 import de.congrace.exp4j.Calculable;
 import de.congrace.exp4j.ExpressionBuilder;
 
 public class ImmedTransactionEditorFragment extends Fragment 
     implements OnDateSetListener, OnTimeSetListener {
     private static final String KEY_CURRENTTRANSACTION = "currentTransaction";
     private static final String KEY_SELECTEDCATEGORY = "selectedCategory";
     private static final String KEY_SELECTEDTRANSFERMONEYNODE =
         "selectedTransferMoneyNode";
 
     private static final String TAG_DATEPICKER = "datePicker";
     private static final String TAG_TIMEPICKER = "timePicker";
 
     private static final int REQCODE_CATEGORYCHOOSE = 0;
     private static final int REQCODE_MONEYNODECHOOSE = 1;
 
     private OnImmediateTransactionEditListener listener;
     private Category selectedCategory;
     private MoneyNode selectedTransferMoneyNode;
 
     private ImmediateTransaction transaction;
     private MoneyNode currentMoneyNode;
 
     private TimePickerFragment timePicker;
     private DatePickerFragment datePicker;
 
     private EditText descriptionText;
     private SelectorButton categoryButton;
     private Button executionDateButton;
     private Button executionTimeButton;
     private SignToggleButton valueSignToggle;
     private EditText valueText;
     private TextView currencyTextView;
     private CheckBox transferCheck;
     private LinearLayout transferPanel;
     private SelectorButton transferMoneyNodeButton;
     private Button addButton;
 
     private DateFormat dateFormat;
     private DateFormat timeFormat;
 
     public ImmedTransactionEditorFragment() {
         dateFormat = DateFormat.getDateInstance();
         timeFormat = DateFormat.getTimeInstance();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.fragment_immedtransaction_editor, 
                 container, false);
 
         descriptionText = (EditText) v.findViewById(R.id.description_text);
         categoryButton = (SelectorButton) v.findViewById(R.id.category_button);
         executionDateButton = (Button) v.findViewById(R.id.executionDate_button);
         executionTimeButton = (Button) v.findViewById(R.id.executionTime_button);
         valueSignToggle = (SignToggleButton) v.findViewById(R.id.value_sign);
         valueText = (EditText) v.findViewById(R.id.value_text);
         currencyTextView = (TextView) v.findViewById(R.id.currency_label);
         transferCheck = (CheckBox) v.findViewById(R.id.transfer_check);
         transferPanel = (LinearLayout) v.findViewById(R.id.transfer_panel);
         transferMoneyNodeButton = (SelectorButton) v.findViewById(
                 R.id.transfer_moneynode_button);
         addButton = (Button) v.findViewById(R.id.add_button);
 
         FragmentManager fm = getFragmentManager();
 
         timePicker = (TimePickerFragment) fm.findFragmentByTag(TAG_TIMEPICKER);
         datePicker = (DatePickerFragment) fm.findFragmentByTag(TAG_DATEPICKER);
 
         if (timePicker == null) {
             timePicker = new TimePickerFragment();
         }
 
         if (datePicker == null) {
             datePicker = new DatePickerFragment();
         }
 
         timePicker.setListener(this);
         datePicker.setListener(this);
 
         categoryButton.setOnClickListener(new OnClickListener() {
             public void onClick(View view) {
                 Intent intent = new Intent(view.getContext(), 
                     CategoryListActivity.class);
                 intent.putExtra(CategoryListActivity.KEY_INTENTION, 
                     CategoryListActivity.INTENTION_SELECT);
                 startActivityForResult(intent, REQCODE_CATEGORYCHOOSE);
             }
         });
 
         executionDateButton.setOnClickListener(new OnClickListener() {
             public void onClick(View view) {
                 try {
                     datePicker.setDate(dateFormat.parse(
                             executionDateButton.getText().toString()));
                 } catch (ParseException e) {
                 }
                 datePicker.show(getFragmentManager(), TAG_DATEPICKER);
             }
         });
 
         executionTimeButton.setOnClickListener(new OnClickListener() {
             public void onClick(View view) {
                 try {
                     timePicker.setTime(timeFormat.parse(
                             executionTimeButton.getText().toString()));
                 } catch (ParseException e) {
                 }
                 timePicker.show(getFragmentManager(), TAG_TIMEPICKER);
             }
         });
 
         transferCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
             public void onCheckedChanged(CompoundButton view, boolean checked) {
                 if (checked) {
                     transferPanel.setVisibility(View.VISIBLE);
                 } else {
                     transferPanel.setVisibility(View.GONE);
                 }
             };
         });
 
         transferMoneyNodeButton.setOnClickListener(new OnClickListener() {
             public void onClick(View view) {
                 Intent intent = new Intent(view.getContext(), 
                     MoneyNodeListActivity.class);
                 intent.putExtra(MoneyNodeListActivity.KEY_INTENTION, 
                     MoneyNodeListActivity.INTENTION_SELECT);
 
                 ArrayList<MoneyNode> excludedMoneyNodes = 
                     new ArrayList<MoneyNode>();
                 excludedMoneyNodes.add(currentMoneyNode);
                 intent.putParcelableArrayListExtra(
                     MoneyNodeListActivity.KEY_EXCLUDE,
                     excludedMoneyNodes);
                 startActivityForResult(intent, REQCODE_MONEYNODECHOOSE);
             }
         });
 
         addButton.setOnClickListener(new OnClickListener() {
             public void onClick(View view) {
                 if (!validateInputFields()) {
                     return;
                 }
 
                 String description = descriptionText.getText().toString().trim();
 
                 Date executionDate;
                 Date executionTime;
                 Date executionDateTime;
                 try {
                     executionDate = dateFormat.parse(
                         executionDateButton.getText().toString());
                     executionTime = timeFormat.parse(
                         executionTimeButton.getText().toString());
                 } catch (ParseException e) {
                     executionTime = executionDate = new Date();
                 }
 
                 executionDateTime = Utils.combineDateTime(executionDate, 
                     executionTime);
 
                 BigDecimal value;
                 try {
                     Calculable calc = new ExpressionBuilder(
                         valueText.getText().toString()).build();
                     value = BigDecimal.valueOf(calc.calculate())
                         .setScale(2, BigDecimal.ROUND_HALF_UP);
                 } catch (Exception e) {
                     value = new BigDecimal(0);
                 }
 
                 if (valueSignToggle.isNegative()) {
                     value = value.multiply(BigDecimal.valueOf(-1));
                 }
 
                 // If we are creating a new transaction
                 if (transaction == null) {
                     ImmediateTransaction newTransaction = 
                         new ImmediateTransaction(currentMoneyNode, value, 
                                 description, selectedCategory, executionDateTime);
 
                     // If this new transaction is a transfer
                     if (selectedTransferMoneyNode != null) {
                         ImmediateTransaction otherTransaction = 
                             new ImmediateTransaction(newTransaction,
                                     selectedTransferMoneyNode);
                         newTransaction.setTransferTransaction(otherTransaction);
                         otherTransaction.setTransferTransaction(newTransaction);
                     }
 
                     listener.onImmediateTransactionCreated(newTransaction);
                 } 
                 // If we are updating an existing transaction
                 else {
                     ImmedTransactionEditOldInfo oldInfo = 
                         new ImmedTransactionEditOldInfo(transaction);
                     transaction.setDescription(description);
                     transaction.setCategory(selectedCategory);
                     transaction.setExecutionDate(executionDateTime);
                     transaction.setValue(value);
 
                     if (selectedTransferMoneyNode != null) {
                         // If edited transaction wasn't part of a transfer and
                         // now is, create transfer transaction
                         if (transaction.getTransferTransaction() == null) {
                             ImmediateTransaction otherTransaction = 
                                 new ImmediateTransaction(transaction,
                                         selectedTransferMoneyNode);
                             transaction.setTransferTransaction(
                                     otherTransaction);
                             otherTransaction.setTransferTransaction(
                                     transaction);
                         }
                         // If edited transaction was already part of a 
                         // transfer, update transfer transaction
                         else {
                             ImmediateTransaction otherTransaction =
                                 transaction.getTransferTransaction();
 
                             otherTransaction.setMoneyNode(selectedTransferMoneyNode);
                             otherTransaction.setDescription(description);
                             otherTransaction.setCategory(selectedCategory);
                             otherTransaction.setExecutionDate(executionDateTime);
                             otherTransaction.setValue(
                                     value.multiply(new BigDecimal("-1")));
                         }
                     }
                     // If edited transaction no longer is a transfer but
                     // was a transfer before, we need to remove the opposite
                     // transaction.
                     else if (transaction.getTransferTransaction() != null) {
                         transaction.setTransferTransaction(null);
                     }
                     listener.onImmediateTransactionEdited(transaction, 
                             oldInfo);
                 }
             }
         });
 
         if (savedInstanceState != null) {
             transaction = savedInstanceState.getParcelable(KEY_CURRENTTRANSACTION);
             selectedCategory = savedInstanceState.getParcelable(KEY_SELECTEDCATEGORY);
             selectedTransferMoneyNode = savedInstanceState.getParcelable(
                 KEY_SELECTEDTRANSFERMONEYNODE);
         }
         
         updateTransactionFields();
 
         return v;
     }
 
     @Override
     public void onDateSet(DatePicker view, int year, int month, int day) {
         GregorianCalendar calendar = new GregorianCalendar(year, month, day);
         executionDateButton.setText(dateFormat.format(calendar.getTime()));
     }
 
     @Override
     public void onTimeSet(TimePicker view, int hours, int minutes) {
         GregorianCalendar calendar = new GregorianCalendar(0, 0, 0, hours, minutes);
         executionTimeButton.setText(timeFormat.format(calendar.getTime()));
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         outState.putParcelable(KEY_CURRENTTRANSACTION, transaction);
         outState.putParcelable(KEY_SELECTEDCATEGORY, selectedCategory);
         outState.putParcelable(KEY_SELECTEDTRANSFERMONEYNODE, 
             selectedTransferMoneyNode);
         super.onSaveInstanceState(outState);
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         try {
             listener = (OnImmediateTransactionEditListener) activity;
         } catch (ClassCastException e) {
             throw new ClassCastException(activity.toString() + 
                     " must implement OnImmediateTransactionEditListener");
         }
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode == Activity.RESULT_OK) {
             if (requestCode == REQCODE_CATEGORYCHOOSE) {
                 selectedCategory = (Category) data.getParcelableExtra(
                         Category.KEY_CATEGORY);
                 updateCategoryFields();
             } 
             else if (requestCode == REQCODE_MONEYNODECHOOSE) {
                 selectedTransferMoneyNode = (MoneyNode) 
                     data.getParcelableExtra(MoneyNode.KEY_MONEYNODE);
                 updateTransferFields();
             }
         }
     }
 
     /**
      * @return the transaction
      */
     public ImmediateTransaction getTransaction() {
         return transaction;
     }
 
     /**
      * @param node the transaction to set
      */
     public void setTransaction(ImmediateTransaction trans) {
         ImmediateTransaction prevNode = this.transaction;
         this.transaction = trans;
 
         if (trans != null) {
             setCurrentMoneyNode(trans.getMoneyNode());
         }
 
         if (prevNode != transaction) {
             updateTransactionFields();
         }
     }
 
     /**
      * @return the currentMoneyNode
      */
     public MoneyNode getCurrentMoneyNode() {
         return currentMoneyNode;
     }
 
     /**
      * @param currentMoneyNode the currentMoneyNode to set
      */
     public void setCurrentMoneyNode(MoneyNode currentMoneyNode) {
         this.currentMoneyNode = currentMoneyNode;
     }
 
     private void updateTransactionFields() {
         // If we are adding a new node, reset all fields
         if (transaction == null) {
             descriptionText.setText("");
             executionDateButton.setText(dateFormat.format(new Date()));
             executionTimeButton.setText(timeFormat.format(new Date()));
             valueText.setText("");
             valueSignToggle.setNegative();
             addButton.setText(R.string.add);
         // If we are editing a node, fill fields with current information
         } else {
             try {
                 transaction.load();
                 descriptionText.setText(transaction.getDescription());
                 executionDateButton.setText(dateFormat.format(
                             transaction.getExecutionDate()));
                 executionTimeButton.setText(timeFormat.format(
                             transaction.getExecutionDate()));
                 BigDecimal value = transaction.getValue();
                 valueText.setText(value.abs().toString());
                 valueSignToggle.setToNumberSign(value);
                 addButton.setText(R.string.edit);
             } catch (DatabaseException e) {
                 Log.e("TMM", "Error loading transaction", e);
             }
         }
 
         if (currentMoneyNode != null) {
             currencyTextView.setText(currentMoneyNode.getCurrency());
         }
 
         updateCategoryFields();
         updateTransferFields();
     }
 
     private void updateCategoryFields() {
         if (selectedCategory == null && transaction != null) {
             selectedCategory = transaction.getCategory();
         }
 
         if (selectedCategory == null) {
             categoryButton.setText(R.string.category_nonselected);
             categoryButton.setDrawableId(0);
         } else {
             categoryButton.setText(selectedCategory.getName());
             int drawableId = DrawableResolver.getInstance().getDrawableId(
                     selectedCategory.getIcon());
             categoryButton.setDrawableId(drawableId);
             categoryButton.setError(false);
         }
     }
 
     private void updateTransferFields() {
         if (selectedTransferMoneyNode == null && transaction != null) {
             ImmediateTransaction transferTransaction = 
                 transaction.getTransferTransaction();
 
             if (transferTransaction != null) {
                 try {
                     transferTransaction.load();
                     selectedTransferMoneyNode = transferTransaction.getMoneyNode();
                 } catch (DatabaseException e) {
                     Log.e("TMM", "Unable to load transfer transaction", e);
                 }
             }
         }
 
         if (selectedTransferMoneyNode == null) {
             transferCheck.setChecked(false);
             transferMoneyNodeButton.setText(R.string.moneynode_nonselected);
             transferMoneyNodeButton.setDrawableId(0);
         } else {
             transferCheck.setChecked(true);
             transferMoneyNodeButton.setText(
                     selectedTransferMoneyNode.getName());
             int drawableId = DrawableResolver.getInstance().getDrawableId(
                     selectedTransferMoneyNode.getIcon());
             transferMoneyNodeButton.setDrawableId(drawableId);
             transferMoneyNodeButton.setError(false);
         }
     }
 
     private boolean validateInputFields() {
         boolean error = false;
 
         Resources res = getResources();
         Drawable errorDrawable = 
             res.getDrawable(R.drawable.indicator_input_error);
         errorDrawable.setBounds(0, 0, 
                 errorDrawable.getIntrinsicWidth(), 
                 errorDrawable.getIntrinsicHeight());
         String value = valueText.getText().toString();
 
         if (TextUtils.isEmpty(value)) {
             valueText.setError(
                     res.getString(R.string.error_trans_value_unspecified),
                     errorDrawable);
             error = true;
         } else {
             try {
                 new ExpressionBuilder(value).build();
             } catch (Exception e) {
                 valueText.setError(
                     res.getString(R.string.error_trans_value_invalid),
                     errorDrawable);
                 error = true;
             }
         }
 
         if (selectedCategory == null) {
             categoryButton.setError(true);
             error = true;
         }
 
         if (transferCheck.isChecked() && selectedTransferMoneyNode == null) {
             transferMoneyNodeButton.setError(true);
             error = true;
         }
 
         return !error;
     }
 
     public static class ImmedTransactionEditOldInfo implements Parcelable {
         public static final String KEY_OLDINFO = "oldImmedTransactionInfo";
         private static final DateFormat dateFormat = DateFormat.getDateTimeInstance();
 
         private String description;
         private Category category;
         private Date executionDate;
         private BigDecimal value;
         private ImmediateTransaction transferTransaction;
 
         public ImmedTransactionEditOldInfo(ImmediateTransaction trans) {
             this(trans.getDescription(), trans.getCategory(),
                     trans.getExecutionDate(), trans.getValue(),
                     trans.getTransferTransaction());
         }
 
         public ImmedTransactionEditOldInfo(String description,
                 Category category, Date executionDate, BigDecimal value,
                 ImmediateTransaction transferTransaction) {
             this.description = description;
             this.category = category;
             this.executionDate = executionDate;
             this.value = value;
             this.transferTransaction = transferTransaction;
         }
 
         public String getDescription() {
             return description;
         }
 
         public Category getCategory() {
             return category;
         }
 
         public Date getExecutionDate() {
             return executionDate;
         }
 
         public BigDecimal getValue() {
             return value;
         }
 
         public ImmediateTransaction getTransferTransaction() {
             return transferTransaction;
         }
 
         public void writeToParcel(Parcel out, int flags) {
             out.writeString(description);
             out.writeParcelable(category, flags);
             out.writeString(dateFormat.format(executionDate));
             out.writeString(value.toString());
             out.writeParcelable(transferTransaction, flags);
         }
 
         public int describeContents() {
             return 0;
         }
 
         public static final Parcelable.Creator<ImmedTransactionEditOldInfo> CREATOR =
             new Parcelable.Creator<ImmedTransactionEditOldInfo>() {
                 public ImmedTransactionEditOldInfo createFromParcel(Parcel in) {
                     String description = in.readString();
                     Category category = (Category) in.readParcelable(
                             Category.class.getClassLoader());
                     Date date = null;
                     try {
                         date = dateFormat.parse(in.readString());
                     } catch (ParseException e) {
                         Log.e("TMM", e.getMessage(), e);
                     }
                     BigDecimal value = new BigDecimal(in.readString());
                     ImmediateTransaction transferTransaction =
                         (ImmediateTransaction) in.readParcelable(
                                 ImmediateTransaction.class.getClassLoader());
                     return new ImmedTransactionEditOldInfo(description, 
                             category, date, value, transferTransaction);
                 }
 
                 public ImmedTransactionEditOldInfo[] newArray(int size) {
                     return new ImmedTransactionEditOldInfo[size];
                 }
             };
     }
 
     public interface OnImmediateTransactionEditListener {
         public void onImmediateTransactionCreated(ImmediateTransaction transaction);
         public void onImmediateTransactionEdited(ImmediateTransaction transaction, 
                 ImmedTransactionEditOldInfo oldInfo);
     }
 }
 
