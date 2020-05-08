 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
import myorg.domain.VirtualHost;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 
 public abstract class Acquisition extends Acquisition_Base {
 
    public Acquisition() {
	super();
	setExpenditureTrackingSystem(VirtualHost.getVirtualHostForThread().getExpenditureTrackingSystem());
    }

     @Override
     public boolean isConnectedToCurrentHost() {
 	return getExpenditureTrackingSystem() == ExpenditureTrackingSystem.getInstance();
     }
 
 }
