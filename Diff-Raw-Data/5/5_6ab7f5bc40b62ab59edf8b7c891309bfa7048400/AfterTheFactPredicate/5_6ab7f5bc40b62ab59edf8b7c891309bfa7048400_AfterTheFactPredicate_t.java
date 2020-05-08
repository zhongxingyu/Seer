 package pt.ist.expenditureTrackingSystem.domain.acquisitions.search;
 
import org.apache.commons.lang.StringUtils;

 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.AcquisitionAfterTheFact;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.AfterTheFactAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.predicates.SearchPredicate;
 
 public class AfterTheFactPredicate extends SearchPredicate {
 
     @Override
     public boolean evaluate(PaymentProcess process, SearchPaymentProcess searchBean) {
 	AcquisitionAfterTheFact request = ((AfterTheFactAcquisitionProcess) process).getAcquisitionAfterTheFact();
 	return request != null && matchesSearchCriteria(request, searchBean)
 		&& (process.isAccessibleToCurrentUser() || process.isTakenByCurrentUser());
     }
 
     private boolean matchesSearchCriteria(AcquisitionAfterTheFact request, SearchPaymentProcess searchBean) {
 
	return !StringUtils.isEmpty(searchBean.getProcessId())
 		&& matchCriteria(searchBean.getProcessId(), request.getAfterTheFactAcquisitionProcess().getProcessNumber());
 
     }
 }
