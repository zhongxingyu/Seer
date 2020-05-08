 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericAcquisitionProcessActivity;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess.ProcessClassification;
import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 
 public class ChangeProcessClassification extends GenericAcquisitionProcessActivity {
 
     @Override
     protected boolean isAccessible(RegularAcquisitionProcess process) {
	Person loggedPerson = getLoggedPerson();
	return loggedPerson == process.getRequestor() && process.getAcquisitionProcessState().isInGenesis()
		&& process instanceof SimplifiedProcedureProcess;
     }
 
     @Override
     protected void process(RegularAcquisitionProcess process, Object... objects) {
 	ProcessClassification classification = (ProcessClassification) objects[0];
 	((SimplifiedProcedureProcess) process).setProcessClassification(classification);
 
     }
 
 }
