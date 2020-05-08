 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericAcquisitionProcessActivity;
 
 public class RemoveFundAllocation extends GenericAcquisitionProcessActivity {
 
     @Override
     protected boolean isAccessible(RegularAcquisitionProcess process) {
 	return process.isAccountingEmployee();
     }
 
     @Override
     protected boolean isAvailable(RegularAcquisitionProcess process) {
 	return (checkActiveConditions(process) || checkCanceledConditions(process))
 		&& process.getAcquisitionRequest().hasAnyFundAllocationId(getUser().getPerson());
     }
 
     private boolean checkActiveConditions(RegularAcquisitionProcess process) {
 	return super.isAvailable(process) && process.getAcquisitionProcessState().isInAllocatedToUnitState();
     }
 
     private boolean checkCanceledConditions(RegularAcquisitionProcess process) {
 	return super.isAvailable(process) && process.getAcquisitionProcessState().isCanceled();
     }
 
     @Override
     protected void process(RegularAcquisitionProcess process, Object... objects) {
 	process.getAcquisitionRequest().resetFundAllocationId(getUser().getPerson());
 	if (!process.getAcquisitionProcessState().isCanceled()) {
 	    process.allocateFundsToSupplier();
 	} else {
 	    RemoveFundAllocationExpirationDate removeFundAllocationExpirationDate = new RemoveFundAllocationExpirationDate();
	    if (!process.hasAnyAllocatedFunds() && removeFundAllocationExpirationDate.isActive(process)) {
 		removeFundAllocationExpirationDate.execute(process);
 	    }
 	}
     }
 }
