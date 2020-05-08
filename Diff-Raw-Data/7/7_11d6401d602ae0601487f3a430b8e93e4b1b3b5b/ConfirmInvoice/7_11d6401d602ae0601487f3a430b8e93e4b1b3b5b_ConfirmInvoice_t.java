 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 
 public class ConfirmInvoice extends WorkflowActivity<RegularAcquisitionProcess, ActivityInformation<RegularAcquisitionProcess>> {
 
     @Override
     public boolean isActive(RegularAcquisitionProcess process, User user) {
 	Person person = user.getExpenditurePerson();
	return isUserProcessOwner(process, user) && person != null && process.isActive() && !process.isInvoiceReceived()
		&& !process.getUnconfirmedInvoices(person).isEmpty() && process.isResponsibleForUnit(person);
     }
 
     @Override
     protected void process(ActivityInformation<RegularAcquisitionProcess> activityInformation) {
 	activityInformation.getProcess().confirmInvoiceBy(UserView.getCurrentUser().getExpenditurePerson());
     }
 
     @Override
     public String getLocalizedName() {
 	return BundleUtil.getStringFromResourceBundle(getUsedBundle(), "label." + getClass().getName());
     }
 
     @Override
     public String getUsedBundle() {
 	return "resources/AcquisitionResources";
     }
 
 }
