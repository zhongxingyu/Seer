 package org.gsoft.openserv.buslogic.payment;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.gsoft.openserv.buslogic.loanevent.LoanEventLogic;
 import org.gsoft.openserv.buslogic.system.SystemSettingsLogic;
 import org.gsoft.openserv.domain.loan.Loan;
 import org.gsoft.openserv.domain.loan.LoanEvent;
 import org.gsoft.openserv.domain.loan.LoanEventType;
 import org.gsoft.openserv.domain.loan.LoanTypeProfile;
 import org.gsoft.openserv.domain.payment.BillingStatement;
 import org.gsoft.openserv.domain.payment.LateFee;
 import org.gsoft.openserv.repositories.loan.LoanTypeProfileRepository;
 import org.gsoft.openserv.repositories.payment.BillingStatementRepository;
 import org.gsoft.openserv.repositories.payment.LateFeeRepository;
 import org.joda.time.DateTime;
 import org.springframework.stereotype.Component;
 
 @Component
 public class LateFeeLogic {
 	@Resource
 	private BillingStatementRepository billingStatementRepository;
 	@Resource
 	private LoanTypeProfileRepository loanTypeProfileRepository;
 	@Resource
 	private SystemSettingsLogic systemSettings;
 	@Resource
 	private LoanEventLogic loanEventLogic;
 	@Resource
 	private LateFeeRepository lateFeeRepository;
 	
 	public void updateLateFees(Loan loan){
 		LoanTypeProfile ltp = loanTypeProfileRepository.findOne(loan.getEffectiveLoanTypeProfileID());
 		List<BillingStatement> bills = billingStatementRepository.findAllBillsForLoan(loan.getLoanID());
 		for(BillingStatement bill:bills){
 			Date dueDateWithLateGrace = new DateTime(bill.getDueDate()).plusDays(ltp.getDaysLateForFee()).toDate();
 			if(((bill.getSatisfiedDate() == null && dueDateWithLateGrace.before(systemSettings.getCurrentSystemDate())) ||
 					(bill.getSatisfiedDate() != null && dueDateWithLateGrace.before(bill.getSatisfiedDate()))) && 
					bill.getLateFee() == null){
 				//assess late fee
 				this.assessLateFee(bill, ltp, loan);
 			}
			else if(bill.getLateFee() != null && (dueDateWithLateGrace.after(systemSettings.getCurrentSystemDate()) || 
 					(bill.getSatisfiedDate() != null && dueDateWithLateGrace.after(bill.getSatisfiedDate())))){
 				//cancel late fee
 				this.cancelLateFee(bill, loan);
 			}
 		}
 	}
 	
 	private void assessLateFee(BillingStatement bill, LoanTypeProfile ltp, Loan loan){
 		//redundant check, but better safe than sorry.
 		if(bill.getLateFee() == null){
 			LateFee lateFee = new LateFee();
 			lateFee = lateFeeRepository.save(lateFee);
 			lateFee.setBillingStatement(bill);
 			bill.setLateFee(lateFee);
 			lateFee.setFeeAmount(ltp.getLateFeeAmount());
 			Date dueDateWithLateGrace = new DateTime(bill.getDueDate()).plusDays(ltp.getDaysLateForFee()).toDate();
 			LoanEvent lateFeeEvent = loanEventLogic.createLoanEvent(loan, LoanEventType.LATE_FEE_ASSESSED, dueDateWithLateGrace, 0, BigDecimal.ZERO, lateFee.getFeeAmount());
 			lateFee.setLoanEvent(lateFeeEvent);
 		}
 	}
 	
 	private void cancelLateFee(BillingStatement bill, Loan loan){
 		if(bill.getLateFee() != null){
 			LateFee lateFee = bill.getLateFee();
 			LoanEvent cancelledLateFeeEvent = loanEventLogic.createLoanEvent(loan, LoanEventType.LATE_FEE_CANCELLED, lateFee.getLoanEvent().getEffectiveDate(), 0, BigDecimal.ZERO, lateFee.getFeeAmount()*-1);
 			lateFee.setCancelledLoanEvent(cancelledLateFeeEvent);
 		}
 	}
 }
