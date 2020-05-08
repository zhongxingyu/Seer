 package module.mission.domain;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import module.organization.domain.Party;
 import myorg.applicationTier.Authenticate;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 
 import org.jfree.data.time.Month;
 import org.joda.time.LocalDate;
 
 import pt.ist.emailNotifier.domain.Email;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.Role;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.fenixframework.plugins.remote.domain.exception.RemoteException;
 import pt.utl.ist.fenix.tools.util.i18n.Language;
 
 public class EmailDigesterUtil {
     
     public static void executeTask() {
 	Language.setLocale(Language.getDefaultLocale());
 	for (Person person : getPeopleToProcess()) {
 
 	    	final User user = person.getUser();
 	    	if (user.hasPerson() && user.hasExpenditurePerson()) {
 	    	    final UserView userView = Authenticate.authenticate(user);
 	    	    pt.ist.fenixWebFramework.security.UserView.setUser(userView);
 
 	    	    try {
 	    		final MissionYear missionYear = MissionYear.getCurrentYear();
 	    		final LocalDate today = new LocalDate();
	    		final MissionYear previousYear = today.getMonthOfYear() == Month.JANUARY ? MissionYear.findMissionYear(today.getYear() - 1) : null;
 
 	    		final int takenByUser = missionYear.getTaken().size() + (previousYear == null ? 0 : previousYear.getTaken().size());
 	    		final int pendingApprovalCount = missionYear.getPendingAproval().size() + (previousYear == null ? 0 : previousYear.getPendingAproval().size());
 	    		final int pendingAuthorizationCount = missionYear.getPendingAuthorization().size() + (previousYear == null ? 0 : previousYear.getPendingAuthorization().size());
 	    		final int pendingFundAllocationCount = missionYear.getPendingFundAllocation().size() + (previousYear == null ? 0 : previousYear.getPendingFundAllocation().size());
 	    		final int pendingProcessingCount = missionYear.getPendingProcessingPersonelInformation().size() + (previousYear == null ? 0 : previousYear.getPendingProcessingPersonelInformation().size());
 	    		final int totalPending = takenByUser + pendingApprovalCount + pendingAuthorizationCount + pendingFundAllocationCount + pendingProcessingCount;
 
 	    		if (totalPending > 0) {
 	    		    try {
 	    			final String email = person.getEmail();
 	    			if (email != null) {
 	    			    final StringBuilder body = new StringBuilder("Caro utilizador, possui processos de missão pendentes nas aplicações centrais do IST, em http://dot.ist.utl.pt/.\n");
 	    			    if (takenByUser > 0) {
 	    				body.append("\n\tPendentes de Libertação\t");
 	    				body.append(takenByUser);
 	    			    }
 	    			    if (pendingApprovalCount > 0) {
 	    				body.append("\n\tPendentes de Aprovação\t");
 	    				body.append(pendingApprovalCount);
 	    			    }
 	    			    if (pendingAuthorizationCount > 0) {
 	    				body.append("\n\tPendentes de Autorização\t");
 	    				body.append(pendingAuthorizationCount);
 	    			    }
 	    			    if (pendingFundAllocationCount > 0) {
 	    				body.append("\n\tPendentes de Cabimentação\t");
 	    				body.append(pendingFundAllocationCount);
 	    			    }
 	    			    if (pendingProcessingCount > 0) {
 	    				body.append("\n\tPendentes de Processamento por Mim\t");
 	    				body.append(pendingProcessingCount);
 	    			    }
 	    			    body.append("\n\n\tTotal de Processos de Missão Pendentes\t");
 	    			    body.append(totalPending);
 
 	    			    if (takenByUser > 0) {
 	    				body.append("\n\n\n\tPor favor, proceda à libertação dos processos em \"acesso exclusivo\", após concluir as tarefas que nele tem para realizar.\t");
 	    				body.append(takenByUser);
 	    			    }
 
 	    			    body.append("\n\n---\n");
 	    			    body.append("Esta mensagem foi enviada por meio das Aplicações Centrais do IST.\n");
 
 	    			    final Collection<String> toAddress = Collections.singleton(email);
 	    			    final Collection<String> bccAddress = Collections.singleton("luis.cruz@ist.utl.pt");
 	    			    new Email("Aplicações Centrais do IST", "noreply@ist.utl.pt", new String[] {}, toAddress, Collections.EMPTY_LIST,
 	    				bccAddress, "Processos Pendentes - Missões", body.toString());
 	    			}
 	    		    } catch (final RemoteException ex) {
 	    			System.out.println("Unable to lookup email address for: " + person.getUsername());
 	    			// skip this person... keep going to next.
 	    		    }
 	    		}
 	    	    } finally {
 	    		pt.ist.fenixWebFramework.security.UserView.setUser(null);
 	    	    }
 	    	}
 	}
     }
 
     private static Collection<Person> getPeopleToProcess() {
 	final Set<Person> people = new HashSet<Person>();
 	final LocalDate today = new LocalDate();
 	final ExpenditureTrackingSystem instance = ExpenditureTrackingSystem.getInstance();
 	for (final Authorization authorization : instance.getAuthorizationsSet()) {
 	    if (authorization.isValidFor(today)) {
 		final Person person = authorization.getPerson();
 		if (person.getOptions().getReceiveNotificationsByEmail()) {
 		    people.add(person);
 		}
 	    }
 	}
 	for (final RoleType roleType : RoleType.values()) {
 	    addPeopleWithRole(people, roleType);
 	}
 	for (final AccountingUnit accountingUnit : instance.getAccountingUnitsSet()) {
 	    addPeople(people, accountingUnit.getPeopleSet());
 	    addPeople(people, accountingUnit.getProjectAccountantsSet());
 	    addPeople(people, accountingUnit.getResponsiblePeopleSet());
 	    addPeople(people, accountingUnit.getResponsibleProjectAccountantsSet());
 	    addPeople(people, accountingUnit.getTreasuryMembersSet());
 	}
 	final MissionYear missionYear = MissionYear.getCurrentYear();
 	addRequestorsAndResponsibles(people, missionYear);
 	if (today.getMonthOfYear() == Month.JANUARY) {
	    final MissionYear previousYear = MissionYear.findMissionYear(today.getYear() - 1);
 	    addRequestorsAndResponsibles(people, previousYear);
 	}
 	return people;
     }
 
     private static void addRequestorsAndResponsibles(final Set<Person> people, final MissionYear missionYear) {
 	for (final MissionProcess missionProcess : missionYear.getMissionProcessSet()) {
 	    final Mission mission = missionProcess.getMission();
 	    final module.organization.domain.Person requestingPerson = mission.getRequestingPerson();
 	    if (requestingPerson != null && requestingPerson.getUser().hasExpenditurePerson()) {
 		people.add(requestingPerson.getUser().getExpenditurePerson());
 	    }
 	    final Party missionResponsible = mission.getMissionResponsible();
 	    if (missionResponsible != null && missionResponsible.isPerson()) {
 		final module.organization.domain.Person missionPerson = (module.organization.domain.Person) missionResponsible;
 		if (missionPerson.hasUser()) {
 		    final User user = missionPerson.getUser();
 		    if (user != null && user.hasExpenditurePerson()) {
 			final Person person = user.getExpenditurePerson();
 			if (person.getOptions().getReceiveNotificationsByEmail()) {
 			    people.add(person);
 			}
 		    }
 		}
 	    }
 	}
     }
 
     private static void addPeopleWithRole(final Set<Person> people, final RoleType roleType) {
 	final Role role = Role.getRole(roleType);
 	addPeople(people, role.getPersonSet());
     }
 
     private static void addPeople(final Set<Person> people, Collection<Person> unverified) {
 	for (final Person person : unverified) {
 	    if (person.getOptions().getReceiveNotificationsByEmail()) {
 		people.add(person);
 	    }
 	}
     }
 
 }
