 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 
 public class CancelInvoiceConfirmation extends
 	WorkflowActivity<RegularAcquisitionProcess, ActivityInformation<RegularAcquisitionProcess>> {
 
     @Override
     public boolean isActive(RegularAcquisitionProcess process, User user) {
 	Person person = user.getExpenditurePerson();
	return isUserProcessOwner(process, user)
		&& process.isResponsibleForUnit(person)
		&& !process.getConfirmedInvoices(person).isEmpty()
		&& ((process.hasProjectsAsPayingUnits() && !process.getRequest()
			.hasAllocatedFundsPermanentlyForAnyProjectFinancer()) || (!process.hasProjectsAsPayingUnits() && !process
			.getRequest().hasAnyEffectiveFundAllocationId()));
     }
 
     @Override
     protected void process(ActivityInformation<RegularAcquisitionProcess> activityInformation) {
 	activityInformation.getProcess().cancelInvoiceConfirmationBy(UserView.getCurrentUser().getExpenditurePerson());
     }
 
     @Override
     public String getLocalizedName() {
 	return BundleUtil.getStringFromResourceBundle(getUsedBundle(), "label." + getClass().getName());
     }
 
     @Override
     public String getUsedBundle() {
 	return "resources/AcquisitionResources";
     }
 
     @Override
     public boolean isUserAwarenessNeeded(RegularAcquisitionProcess process, User user) {
 	return false;
     }
 }
