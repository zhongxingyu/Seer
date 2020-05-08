 package pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericRefundProcessActivity;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 
 public class CancelRefundProcess extends GenericRefundProcessActivity {
 
     @Override
     protected boolean isAvailable(RefundProcess process) {
 	return super.isAvailable(process)
 		&& (isCurrentUserRequestor(process) || isUserResponsibleForUnit(process) || process.isAccountingEmployee());
     }
 
     @Override
     protected boolean isAccessible(RefundProcess process) {
 	Person loggedPerson = Person.getLoggedPerson();
 	return ((process.isInGenesis() || process.isInAuthorizedState()) && isCurrentUserRequestor(process))
 		|| (process.isPendingApproval() && isUserResponsibleForUnit(process))
 		|| (process.isResponsibleForUnit(loggedPerson, process.getRequest().getTotalValue())
 			&& !process.getRequest().hasBeenAuthorizedBy(loggedPerson) && process.isInAllocatedToUnitState())
		|| ((process.isPendingInvoicesConfirmation() || process.isPendingFundAllocation()) && ((process
			.isAccountingEmployee() && !process.hasProjectsAsPayingUnits()) || (process.isProjectAccountingEmployee() && process
			.hasProjectsAsPayingUnits())));
     }
 
     private boolean isUserResponsibleForUnit(RefundProcess process) {
 	final Person loggedPerson = getLoggedPerson();
 	return loggedPerson != null && process.isResponsibleForAtLeastOnePayingUnit(loggedPerson);
     }
 
     @Override
     protected void process(RefundProcess process, Object... objects) {
 	process.cancel();
     }
 
 }
