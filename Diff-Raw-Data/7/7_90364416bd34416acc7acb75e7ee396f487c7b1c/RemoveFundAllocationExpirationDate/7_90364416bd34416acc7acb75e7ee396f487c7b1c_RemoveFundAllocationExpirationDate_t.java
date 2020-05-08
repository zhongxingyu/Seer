 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Financer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericAcquisitionProcessActivity;
 
 public class RemoveFundAllocationExpirationDate extends GenericAcquisitionProcessActivity {
 
     @Override
     protected boolean isAccessible(RegularAcquisitionProcess process) {
	return (process.isAccountingEmployee() && !hasAnyAssociatedProject(process)) || process.isProjectAccountingEmployee()
 		|| userHasRole(RoleType.ACQUISITION_CENTRAL);
     }
 
     @Override
     protected boolean isAvailable(RegularAcquisitionProcess process) {
 	return (checkActiveConditions(process) || checkCanceledConditions(process))
 		&& !process.hasAnyAllocatedFunds()
 		&& ((!process.getSkipSupplierFundAllocation() && process.getFundAllocationExpirationDate() != null) || (process
 			.getSkipSupplierFundAllocation() && process.isPendingFundAllocation()));
     }
 
     private boolean checkActiveConditions(RegularAcquisitionProcess process) {
 	return super.isAvailable(process) && process.getAcquisitionProcessState().isInAllocatedToSupplierState();
     }
 
     private boolean checkCanceledConditions(RegularAcquisitionProcess process) {
 	return super.isAvailable(process) && process.getAcquisitionProcessState().isCanceled();
     }
 
     private boolean hasAnyAssociatedProject(final RegularAcquisitionProcess process) {
 	for (final Financer financer : process.getAcquisitionRequest().getFinancersSet()) {
 	    if (financer.isProjectFinancer()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     @Override
     protected void process(RegularAcquisitionProcess process, Object... objects) {
	process.removeFundAllocationExpirationDate();
	process.getRequest().unSubmitForFundsAllocation();
 	process.submitForApproval();
     }
 
 }
