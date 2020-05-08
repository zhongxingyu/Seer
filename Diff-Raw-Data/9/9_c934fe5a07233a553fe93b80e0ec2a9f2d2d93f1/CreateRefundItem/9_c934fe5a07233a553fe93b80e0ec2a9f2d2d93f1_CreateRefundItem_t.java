 package pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundProcess;
 
 public class CreateRefundItem extends WorkflowActivity<RefundProcess, CreateRefundItemActivityInformation> {
 
     @Override
     public boolean isActive(RefundProcess process, User user) {
 	return process.getRequestor() == user.getExpenditurePerson() && isUserProcessOwner(process, user)
 		&& process.isInGenesis();
     }
 
     @Override
     protected void process(CreateRefundItemActivityInformation activityInformation) {
	activityInformation.getProcess().getRequest().createRefundItem(activityInformation.getValueEstimation().round(),
 		activityInformation.getCPVReference(), activityInformation.getDescription());
     }
 
     @Override
     public ActivityInformation<RefundProcess> getActivityInformation(RefundProcess process) {
 	return new CreateRefundItemActivityInformation(process, this);
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
