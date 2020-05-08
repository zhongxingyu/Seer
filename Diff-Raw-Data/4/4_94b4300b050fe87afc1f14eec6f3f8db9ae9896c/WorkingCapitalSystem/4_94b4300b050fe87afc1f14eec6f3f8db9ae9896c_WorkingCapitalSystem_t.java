 package module.workingCapital.domain;
 
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import module.organization.domain.Accountability;
 import module.organization.domain.AccountabilityType;
 import module.organization.domain.Person;
 import module.organization.domain.Unit;
 import module.workflow.widgets.ProcessListWidget;
 import module.workingCapital.domain.util.WorkingCapitalPendingProcessCounter;
 import myorg.domain.ModuleInitializer;
 import myorg.domain.MyOrg;
 import myorg.domain.User;
 import myorg.domain.VirtualHost;
 import pt.ist.fenixWebFramework.services.Service;
 import dml.runtime.RelationAdapter;
 
 public class WorkingCapitalSystem extends WorkingCapitalSystem_Base implements ModuleInitializer {
 
     public static class VirtualHostMyOrgRelationListener extends RelationAdapter<VirtualHost, MyOrg> {
 
 	@Override
 	public void beforeRemove(VirtualHost vh, MyOrg myorg) {
 	    vh.removeWorkingCapitalSystem();
 	    super.beforeRemove(vh, myorg);
 	}
     }
 
     static {
 	VirtualHost.MyOrgVirtualHost.addListener(new VirtualHostMyOrgRelationListener());
 
 	ProcessListWidget.register(new WorkingCapitalPendingProcessCounter());
     }
 
     @Deprecated
     /**
      * This class is no longer a singleton.
      * Use getInstanceForCurrentHost() instead.
      */
     public static WorkingCapitalSystem getInstance() {
 	return getInstanceForCurrentHost();
     }
 
     public static WorkingCapitalSystem getInstanceForCurrentHost() {
 	final VirtualHost virtualHostForThread = VirtualHost.getVirtualHostForThread();
 	return (virtualHostForThread == null) ? MyOrg.getInstance().getWorkingCapitalSystem() : virtualHostForThread
 		.getWorkingCapitalSystem();
     }
 
     private WorkingCapitalSystem(final VirtualHost virtualHost) {
 	super();
 	//setMyOrg(Myorg.getInstance());
 	virtualHost.setWorkingCapitalSystem(this);
     }
 
     @Service
     public void resetAcquisitionValueLimit() {
 	setAcquisitionValueLimit(null);
     }
 
     @Service
     public static void createSystem(final VirtualHost virtualHost) {
 	if (!virtualHost.hasWorkingCapitalSystem() || virtualHost.getWorkingCapitalSystem().getVirtualHostsCount() > 1) {
 	    new WorkingCapitalSystem(virtualHost);
 	}
     }
 
     public SortedSet<Accountability> getManagementMembers() {
 	final SortedSet<Accountability> accountingMembers = new TreeSet<Accountability>(
 		Accountability.COMPARATOR_BY_CHILD_PARTY_NAMES);
 	if (hasManagementUnit() && hasManagingAccountabilityType()) {
 	    final Unit accountingUnit = getManagementUnit();
 	    final AccountabilityType accountabilityType = getManagingAccountabilityType();
 	    for (final Accountability accountability : accountingUnit.getChildAccountabilitiesSet()) {
 		if (accountability.getAccountabilityType() == accountabilityType && accountability.getChild().isPerson()) {
 		    accountingMembers.add(accountability);
 		}
 	    }
 	}
 	return accountingMembers;
     }
 
     public boolean isManagementMember(final User user) {
 	return getManagementAccountability(user) != null;
     }
 
     public Accountability getManagementAccountability(final User user) {
 	if (hasManagementUnit() && hasManagingAccountabilityType()) {
 	    final Unit managementUnit = getManagementUnit();
 	    final AccountabilityType accountabilityType = getManagingAccountabilityType();
 	    return findAccountability(user, accountabilityType, managementUnit);
 	}
 	return null;
     }
 
     private Accountability findAccountability(final User user, final AccountabilityType accountabilityType, final Unit unit) {
 	for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
	    if (accountability.isValid()
		    && accountability.getAccountabilityType() == accountabilityType
		    && accountability.getChild().isPerson()) {
 		final Person person = (Person) accountability.getChild();
 		if (person.getUser() == user) {
 		    return accountability;
 		}
 	    }
 	}
 	return null;
     }
 
     @Override
     @Service
     public void init(final MyOrg root) {
 	final WorkingCapitalSystem workingCapitalSystem = root.getWorkingCapitalSystem();
 	if (workingCapitalSystem != null) {
 	}
     }
 
     @Service
     public void setForVirtualHost(final VirtualHost virtualHost) {
 	virtualHost.setWorkingCapitalSystem(this);
     }
 
 }
