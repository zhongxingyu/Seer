 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 
 public class SubmitForConfirmInvoice extends
 	WorkflowActivity<RegularAcquisitionProcess, ActivityInformation<RegularAcquisitionProcess>> {
 
     @Override
     public boolean isActive(RegularAcquisitionProcess process, User user) {
 	return isUserProcessOwner(process, user) && user.getExpenditurePerson().hasRoleType(RoleType.ACQUISITION_CENTRAL)
		&& process.isInvoiceReceived() && process.getRequest().isCurrentTotalRealValueFullyDistributed();
     }
 
     @Override
     protected void process(ActivityInformation<RegularAcquisitionProcess> activityInformation) {
 	activityInformation.getProcess().submitedForInvoiceConfirmation();
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
