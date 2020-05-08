 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 
 public abstract class Acquisition extends Acquisition_Base {
 
     @Override
     public boolean isConnectedToCurrentHost() {
 	return getExpenditureTrackingSystem() == ExpenditureTrackingSystem.getInstance();
     }
 
 }
