 package pt.ist.expenditureTrackingSystem.domain.acquisitions.search.predicates;
 
 import java.util.List;
 import java.util.Set;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcessStateType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.SearchPaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 
 public class SimplifiedAcquisitionPredicate extends SearchPredicate {
 
     @Override
     public boolean evaluate(PaymentProcess process, SearchPaymentProcess searchBean) {
 	final AcquisitionRequest acquisitionRequest = process.getRequest();
 	return matchesSearchCriteria(acquisitionRequest, searchBean)
 		&& (acquisitionRequest.getProcess().isAvailableForCurrentUser() || process.isTakenByCurrentUser());
     }
 
     private boolean matchesSearchCriteria(final AcquisitionRequest acquisitionRequest, SearchPaymentProcess searchBean) {
 	final Person person = acquisitionRequest.getRequester();
 	final List<Supplier> suppliers = acquisitionRequest.getSuppliers();
 	final String identification = acquisitionRequest.getAcquisitionProcessId();
 	final String acquisitionProposalId = acquisitionRequest.getAcquisitionProposalDocumentId();
 	final String acquisitionRequestDocumentID = acquisitionRequest.hasPurchaseOrderDocument() ? acquisitionRequest
 		.getAcquisitionRequestDocumentID() : null;
 	final AcquisitionProcessStateType type = acquisitionRequest.getAcquisitionProcess().getAcquisitionProcessStateType();
 	final Set<AccountingUnit> accountingUnits = acquisitionRequest.getAccountingUnits();
 	return matchCriteria(searchBean.getProcessId(), identification)
 		&& matchCriteria(searchBean.getRequestingPerson(), person)
 		&& (matchCriteria(searchBean.getRequestingUnit(), acquisitionRequest.getFinancersSet()) || matchCriteria(
 			searchBean.getRequestingUnit(), acquisitionRequest.getRequestingUnit()))
 		&& matchCriteria(searchBean.getSupplier(), suppliers)
 		&& matchCriteria(searchBean.getProposalId(), acquisitionProposalId)
 		&& matchCriteria(searchBean.getHasAvailableAndAccessibleActivityForUser(), acquisitionRequest)
 		&& matchCriteria(searchBean.getAcquisitionProcessStateType(), type)
 		&& matchCriteria(searchBean.getAccountingUnit(), accountingUnits)
 		&& matchCriteria(searchBean.getRequestDocumentId(), acquisitionRequestDocumentID)
 		&& matchShowOnlyCriteris(acquisitionRequest, searchBean);
 
     }
 
     protected boolean matchCriteria(AcquisitionProcessStateType acquisitionProcessStateType, AcquisitionProcessStateType type) {
 	return acquisitionProcessStateType == null || acquisitionProcessStateType.equals(type);
     }
 
     private boolean matchCriteria(final Boolean hasAvailableAndAccessibleActivityForUser,
 	    final AcquisitionRequest acquisitionRequest) {
 	return hasAvailableAndAccessibleActivityForUser == null || !hasAvailableAndAccessibleActivityForUser.booleanValue()
 		|| isPersonAbleToExecuteActivities(acquisitionRequest.getAcquisitionProcess());
     }
 
     private boolean isPersonAbleToExecuteActivities(final AcquisitionProcess acquisitionProcess) {
 	if (acquisitionProcess instanceof RegularAcquisitionProcess) {
 	    return ((RegularAcquisitionProcess) acquisitionProcess).isPersonAbleToExecuteActivities();
 	}
 	return false;
     }
 
     private boolean matchShowOnlyCriteris(final AcquisitionRequest acquisitionRequest, final SearchPaymentProcess searchBean) {
 	if (searchBean.getShowOnlyAcquisitionsExcludedFromSupplierLimit().booleanValue()
 		&& !acquisitionRequest.getProcess().getSkipSupplierFundAllocation().booleanValue()) {
 	    return false;
 	}
	if (searchBean.getShowOnlyAcquisitionsWithAdditionalCosts().booleanValue() && acquisitionRequest.hasAdditionalCosts()) {
	    return false;
 	}
 	return true;
     }
 }
