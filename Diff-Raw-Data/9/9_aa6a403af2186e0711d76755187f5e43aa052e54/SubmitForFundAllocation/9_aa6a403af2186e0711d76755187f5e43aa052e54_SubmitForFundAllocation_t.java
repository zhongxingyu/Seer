 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericAcquisitionProcessActivity;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 
 public class SubmitForFundAllocation extends GenericAcquisitionProcessActivity {
 
     @Override
     protected boolean isAccessible(RegularAcquisitionProcess process) {
 	final Person loggedPerson = getLoggedPerson();
 	return loggedPerson != null
 		&& process.isResponsibleForUnit(loggedPerson)
 		&& !process.getAcquisitionRequest().hasBeenApprovedBy(loggedPerson);
     }
 
     @Override
     protected boolean isAvailable(RegularAcquisitionProcess process) {
 	return super.isAvailable(process) && process.isPendingApproval();
     }
 
     @Override
     protected void process(RegularAcquisitionProcess process, Object... objects) {
 	Person person = (Person) objects[0];
 	process.getAcquisitionRequest().submittedForFundsAllocation(person);
	if (process.getAcquisitionRequest().isSubmittedForFundsAllocationByAllResponsibles()) {
	    process.submitForFundAllocation();
	    //	    if (!process.getSkipSupplierFundAllocation()) {
 //		process.submitForFundAllocation();
 //		new FundAllocationExpirationDate().execute(process, new Object[] {});
 //	    }
 //	    else {
 //		process.skipFundAllocation();
 //	    }
	}
     }
 
 }
