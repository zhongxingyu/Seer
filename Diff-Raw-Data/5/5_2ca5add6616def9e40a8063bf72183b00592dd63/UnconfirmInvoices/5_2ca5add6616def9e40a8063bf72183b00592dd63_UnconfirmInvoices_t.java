 package pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 
 public class UnconfirmInvoices extends WorkflowActivity<RefundProcess, ActivityInformation<RefundProcess>> {
 
     @Override
     public boolean isActive(RefundProcess process, User user) {
 	Person person = user.getExpenditurePerson();
 	return isUserProcessOwner(process, user)
 		&& process.isRealValueFullyAttributedToUnits()
 		&& ((process.isAccountingEmployee(person) && !process.hasProjectsAsPayingUnits()) || (process
 			.isProjectAccountingEmployee(person) && process.hasProjectsAsPayingUnits()))
 		&& !process.getRequest().getConfirmedInvoices().isEmpty()
		&& ((process.hasProjectsAsPayingUnits() && !process.getRequest()
			.hasAllocatedFundsPermanentlyForAnyProjectFinancer()) || (!process.hasProjectsAsPayingUnits() && !process
			.getRequest().hasAnyEffectiveFundAllocationId()));
//		&& (!process.getRequest().hasAllocatedFundsPermanentlyForAnyProjectFinancer());
     }
 
     @Override
     protected void process(ActivityInformation<RefundProcess> activityInformation) {
 	activityInformation.getProcess().unconfirmInvoicesByPerson(Person.getLoggedPerson());
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
