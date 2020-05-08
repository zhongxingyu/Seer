 package pt.ist.expenditureTrackingSystem.domain.processes;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.joda.time.DateTime;
 
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.fenixWebFramework.security.UserView;
 
 public abstract class GenericProcess extends GenericProcess_Base {
 
     public GenericProcess() {
 	super();
 	setOjbConcreteClass(getClass().getName());
 	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
     }
 
     public static <T extends GenericProcess> Set<T> getAllProcesses(Class<T> processClass) {
 	Set<T> classes = new HashSet<T>();
 	for (GenericProcess process : ExpenditureTrackingSystem.getInstance().getProcessesSet()) {
 	    if (process.getClass().equals(processClass)) {
 		classes.add((T) process);
 	    }
 	}
 	return classes;
     }
 
     public abstract <T extends GenericProcess> AbstractActivity<T> getActivityByName(String name);
 
     public DateTime getDateFromLastActivity() {
	List<GenericLog> logs = getExecutionLogs();
 	Collections.sort(logs, new Comparator<GenericLog>() {
 
 	    public int compare(GenericLog log1, GenericLog log2) {
 		return -1 * log1.getWhenOperationWasRan().compareTo(log2.getWhenOperationWasRan());
 	    }
 
 	});
 
 	return logs.isEmpty() ? null : logs.get(0).getWhenOperationWasRan();
     }
 
     public static boolean isCreateNewProcessAvailable() {
 	return UserView.getUser() != null;
     }
 
 }
