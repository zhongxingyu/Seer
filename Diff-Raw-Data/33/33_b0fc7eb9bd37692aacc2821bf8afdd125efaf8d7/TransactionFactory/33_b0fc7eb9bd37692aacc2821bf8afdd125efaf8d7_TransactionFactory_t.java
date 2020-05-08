 package com.aurelia.loaning.domain;
 
 import org.joda.time.DateTime;
 
 import com.aurelia.loaning.view.LoanFromUI;
 
 public class TransactionFactory {
 
 	public static Transaction createTransaction(LoanFromUI loanFromUI) {
 		Transaction transaction;
 
 		int selectedRadioButtonID = loanFromUI.getRadioGroupFromUI().getCheckedRadioButtonId();
 
 		if (loanFromUI.getLoanRadio().getId() == selectedRadioButtonID) {
 			transaction = new LoanTransaction();
 			transaction.setSource(Transaction.ME);
 			transaction.setDestination(loanFromUI.getDestinationFromUI().getText().toString());
 		} else if (loanFromUI.getBorrowingRadio().getId() == selectedRadioButtonID) {
 			transaction = new BorrowingTransaction();
 			transaction.setSource(loanFromUI.getDestinationFromUI().getText().toString());
 			transaction.setDestination(Transaction.ME);
		} else if (loanFromUI.getOweRadio().getId() == selectedRadioButtonID) {
			transaction = new BorrowingTransaction();
			transaction.setSource(loanFromUI.getDestinationFromUI().getText().toString());
			transaction.setDestination(Transaction.ME);
 		} else {
 			throw new IllegalStateException();
 		}
 
 		transaction.setDescription(loanFromUI.getDescriptionFromUI().getText().toString());

		// needs to add 1 to the month got from the UI. DatePicker months range
		// from 0 to 11 whereas JodaTime months range from 1 to 12.
 		DateTime endDate = new DateTime(loanFromUI.getEndDateFromUI().getYear(), loanFromUI.getEndDateFromUI()
				.getMonth() + 1, loanFromUI.getEndDateFromUI().getDayOfMonth(), 0, 0, 0);
 		transaction.setStarteDate(DateTime.now());
 		transaction.setEndDate(endDate);
 		transaction.setContact(false);
 		transaction.setType("noType");
 
 		return transaction;
 	}
 
 }
