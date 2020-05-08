 package pt.ist.expenditureTrackingSystem.domain.processes;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import module.workflow.domain.WorkflowLog;
 import module.workflow.domain.WorkflowProcessComment;
 import module.workflow.domain.WorkflowSystem;
 import module.workflow.util.ProcessEvaluator;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 
 import org.apache.commons.collections.Predicate;
 
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcessYear;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.ProcessesThatAreAuthorizedByUserPredicate;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 
 public abstract class GenericProcess extends GenericProcess_Base {
 
     public GenericProcess() {
 	super();
     }
 
     public static <T extends GenericProcess> Set<T> getAllProcesses(Class<T> processClass, PaymentProcessYear year) {
 	return year != null ? filter(processClass, null, year.getPaymentProcess()) : filter(processClass, null, WorkflowSystem
 		.getInstance().getProcessesSet());
     }
 
     public static void evaluateAllProcess(final Class processClass, final ProcessEvaluator<GenericProcess> processEvaluator,
 	    final PaymentProcessYear year) {
 	if (year == null) {
 	    evaluate(processClass, (ProcessEvaluator) processEvaluator, WorkflowSystem.getInstance().getProcessesSet());
 	} else {
 	    evaluate(processClass, (ProcessEvaluator) processEvaluator, year.getPaymentProcess());
 	}
     }
 
     public static <T extends GenericProcess> Set<T> getAllProcess(Class<T> processClass, Predicate predicate,
 	    PaymentProcessYear year) {
 	return year != null ? filter(processClass, predicate, year.getPaymentProcess()) : filter(processClass, predicate,
 		WorkflowSystem.getInstance().getProcessesSet());
     }
 
     public static void evaluateProcessesWithResponsible(final Class processClass, final Person person,
 	    final PaymentProcessYear year, final ProcessEvaluator<GenericProcess> processEvaluator) {
 	if (person == null) {
 	    return;
 	}
 
 	final Class clazz = processClass == null ? PaymentProcess.class : processClass;
 	final ProcessesThatAreAuthorizedByUserPredicate predicate = new ProcessesThatAreAuthorizedByUserPredicate(person);
 
 	final ProcessEvaluator<GenericProcess> genericProcessEvaluator = new ProcessEvaluator<GenericProcess>() {
	    private final Set<GenericProcess> processed = new HashSet<GenericProcess>();
 
 	    {
 		next = processEvaluator;
 	    }
 
 	    @Override
 	    public void evaluate(GenericProcess process) {
 		if (!processed.contains(process) && clazz.isAssignableFrom(process.getClass()) && predicate.evaluate(process)) {
 		    processed.add(process);
 		    super.evaluate(process);
 		}
 	    }
 	};
 
 	// final Set<PaymentProcess> processes = new HashSet<PaymentProcess>();
 	// final Set<Unit> units = new HashSet<Unit>();
 	for (final Authorization authorization : person.getAuthorizations()) {
 	    final Unit unit = authorization.getUnit();
 	    unit.evaluateAllProcesses(genericProcessEvaluator, year);
 	    // units.add(unit);
 	    // units.addAll(unit.getAllSubUnits());
 	}
 
 	// for (final Unit unit : units) {
 	// processes.addAll(unit.getProcesses(year));
 	// }
 
 	// final Class clazz = processClass == null ? PaymentProcess.class :
 	// processClass;
 	// final ProcessesThatAreAuthorizedByUserPredicate predicate = new
 	// ProcessesThatAreAuthorizedByUserPredicate(person);
 	// for (final PaymentProcess paymentProcess : processes) {
 	// if (clazz.isAssignableFrom(paymentProcess.getClass()) &&
 	// predicate.evaluate(paymentProcess)) {
 	// processEvaluator.evaluate(paymentProcess);
 	// }
 	// }
     }
 
     public static <T extends PaymentProcess> Set<T> getProcessesWithResponsible(Class<T> processClass, final Person person,
 	    PaymentProcessYear year) {
 	if (person == null) {
 	    return Collections.emptySet();
 	}
 
 	Set<PaymentProcess> processes = new HashSet<PaymentProcess>();
 	Set<Unit> units = new HashSet<Unit>();
 	for (Authorization authorization : person.getAuthorizations()) {
 	    Unit unit = authorization.getUnit();
 	    units.add(unit);
 	    units.addAll(unit.getAllSubUnits());
 	}
 
 	for (Unit unit : units) {
 	    processes.addAll(unit.getProcesses(year));
 	}
 	return (Set<T>) GenericProcess.filter(processClass != null ? processClass : PaymentProcess.class, null, processes);
     }
 
     public static void evaluateProcessesForPerson(final Class processClass, final Person person, final PaymentProcessYear year,
 	    final boolean userAwarness, final ProcessEvaluator<GenericProcess> processEvaluator) {
 	if (person.hasAnyAuthorizations()) {
 	    GenericProcess.evaluateProcessesWithResponsible(processClass, person, year, new ProcessEvaluator<GenericProcess>() {
 		{
 		    next = processEvaluator;
 		}
 
 		@Override
 		public void evaluate(final GenericProcess process) {
 		    if (process.hasAnyAvailableActivity(userAwarness)) {
 			super.evaluate(process);
 		    }
 		}
 
 	    });
 	} else {
 	    final User user = person.getUser();
 	    GenericProcess.evaluateAllProcess(processClass, new ProcessEvaluator<GenericProcess>() {
 		{
 		    next = processEvaluator;
 		}
 
 		@Override
 		public void evaluate(final GenericProcess process) {
 		    if (process.hasAnyAvailableActivity(user, userAwarness)) {
 			super.evaluate(process);
 		    }
 		}
 
 	    }, year);
 	}
     }
 
     public static <T extends PaymentProcess> Set<T> getProcessesForPerson(Class<T> processClass, final Person person,
 	    PaymentProcessYear year, final boolean userAwarness) {
 
 	Set<T> processes = null;
 	final User user = person.getUser();
 	if (person.hasAnyValidAuthorization()
 		&& !(person.hasRoleType(RoleType.ACQUISITION_CENTRAL) || person.hasRoleType(RoleType.ACQUISITION_CENTRAL_MANAGER))) {
 	    processes = new HashSet<T>();
 	    for (T process : GenericProcess.getProcessesWithResponsible(processClass, person, year)) {
 		if (process.hasAnyAvailableActivity(user, userAwarness)) {
 		    processes.add(process);
 		}
 	    }
 	} else {
 	    processes = GenericProcess.getAllProcess(processClass, new Predicate() {
 
 		@Override
 		public boolean evaluate(Object arg0) {
 		    GenericProcess process = (GenericProcess) arg0;
 		    return process.hasAnyAvailableActivity(user, userAwarness);
 		}
 
 	    }, year);
 	}
 
 	return processes;
     }
 
     public static boolean isCreateNewProcessAvailable() {
 	final User user = UserView.getCurrentUser();
 	return user != null;
     }
 
     @Override
     public boolean isSystemAbleToNotifyUser(User user) {
 	return user.getExpenditurePerson().getEmail() != null;
     }
 
     protected Person getLoggedPerson() {
 	return Person.getLoggedPerson();
     }
 
    @Override
     public List<WorkflowProcessComment> getUnreadCommentsForCurrentUser() {
 	return getUnreadCommentsForPerson(Person.getLoggedPerson());
     }
 
     public List<WorkflowProcessComment> getUnreadCommentsForPerson(Person person) {
 	User user = person.getUser();
 	List<WorkflowProcessComment> comments = new ArrayList<WorkflowProcessComment>();
 	for (WorkflowProcessComment comment : getComments()) {
 	    if (comment.isUnreadBy(user)) {
 		comments.add(comment);
 	    }
 	}
 	return comments;
     }
 
     public boolean hasActivitiesFromUser(Person person) {
 	User user = person.getUser();
 	for (WorkflowLog log : getExecutionLogs()) {
 	    if (log.getActivityExecutor() == user) {
 		return true;
 	    }
 	}
 	return false;
     }
 
 }
