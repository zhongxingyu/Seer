 package pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities;
 
import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 
 public class RefundPerson extends WorkflowActivity<RefundProcess, RefundPersonActivityInformation> {
 
     @Override
     public boolean isActive(RefundProcess process, User user) {
 	Person person = user.getExpenditurePerson();
 	return (person.hasRoleType(RoleType.TREASURY_MANAGER) || process.isTreasuryMember(person))
		&& isUserProcessOwner(process, user) && process.hasFundsAllocatedPermanently() && !process.isPayed();
    }

    @Override
    public ActivityInformation<RefundProcess> getActivityInformation(RefundProcess process) {
	return new RefundPersonActivityInformation(process, this);
     }
 
     @Override
     protected void process(RefundPersonActivityInformation activityInformation) {
 	activityInformation.getProcess().refundPerson(activityInformation.getPaymentReference());
 
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
