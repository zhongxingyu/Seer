 package com.aurelia.loaning.view;
 
 import android.app.Activity;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 
 import com.aurelia.loaning.R;
 
 public class LoanFromUIBuilder {
 
 	private LoanFromUI loanFromUI;
 
 	public LoanFromUIBuilder(Activity activity) {
 		this.loanFromUI = new LoanFromUI();
 		this.loanFromUI.setRadioGroupFromUI((RadioGroup) activity.findViewById(R.id.radioGroup1));
 		this.loanFromUI.setBorrowingRadio((RadioButton) activity.findViewById(R.id.borrowRadio));
 		this.loanFromUI.setLoanRadio((RadioButton) activity.findViewById(R.id.loanRadio));
 		this.loanFromUI.setDestinationFromUI((EditText) activity.findViewById(R.id.destination));
 		this.loanFromUI.setDescriptionFromUI((EditText) activity.findViewById(R.id.description));
 		this.loanFromUI.setEndDateFromUI((DatePicker) activity.findViewById(R.id.chooseWhen));
 	}
 
 	public LoanFromUI getLoanFromUI() {
 		return loanFromUI;
 	}
 
 }
