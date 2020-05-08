 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
import pt.ist.expenditureTrackingSystem.applicationTier.Authenticate.User;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 
 public class FixInvoice extends ReceiveInvoice {
 
     @Override
     protected boolean isAccessible(RegularAcquisitionProcess process) {
 	return userHasRole(RoleType.ACQUISITION_CENTRAL);
     }
 
     @Override
     protected boolean isAvailable(RegularAcquisitionProcess process) {
	Person currentOwner = process.getCurrentOwner();
	User user = getUser();
	return process.getAcquisitionProcessState().isInvoiceReceived()
		&& (currentOwner == null || (user != null && user.getPerson() == currentOwner));
     }
 
     @Override
     protected void process(RegularAcquisitionProcess process, Object... objects) {
 	processInvoiceData(process, objects);
     }
 
 }
