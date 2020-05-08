 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericAcquisitionProcessActivity;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess.ProcessClassification;
 
 public class ChangeProcessClassification extends GenericAcquisitionProcessActivity {
 
     @Override
     protected boolean isAccessible(RegularAcquisitionProcess process) {
	return process.getAcquisitionProcessState().isInGenesis() && process instanceof SimplifiedProcedureProcess;
     }
 
     @Override
     protected void process(RegularAcquisitionProcess process, Object... objects) {
 	ProcessClassification classification = (ProcessClassification) objects[0];
 	((SimplifiedProcedureProcess) process).setProcessClassification(classification);
 
     }
 
 }
