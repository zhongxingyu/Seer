 package pt.ist.expenditureTrackingSystem.domain.authorizations;
 
 import java.util.Comparator;
 import java.util.Set;
 
import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Money;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.dto.AuthorizationBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class Authorization extends Authorization_Base {
 
     public static final Comparator<Authorization> COMPARATOR_BY_NAME_AND_DATE = new Comparator<Authorization>() {
 
 	@Override
 	public int compare(final Authorization o1, final Authorization o2) {
 	    final Person p1 = o1.getPerson();
 	    final Person p2 = o2.getPerson();
 	    final int p = p1.getName().compareTo(p2.getName());
 	    if (p == 0) {
 		final int d = o1.getStartDate().compareTo(o2.getStartDate());
 		return d == 0 ? o1.hashCode() - o2.hashCode() : d;
 	    }
 	    return p;
 	}
 
     };
 
     public Authorization() {
 	super();
 	setOjbConcreteClass(getClass().getName());
 	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
 	setStartDate(new LocalDate());
 	setMaxAmount(Money.ZERO);
     }
 
     public Authorization(final Person person, final Unit unit) {
 	this();
 	setPerson(person);
 	setUnit(unit);
 	setCanDelegate(Boolean.FALSE);
 	AuthorizationOperation.CREATE.log(this);
     }
 
     public Authorization(final AuthorizationBean authorizationBean) {
 	this(authorizationBean.getPerson(), authorizationBean.getUnit());
 	setStartDate(authorizationBean.getStartDate());
 	setEndDate(authorizationBean.getEndDate());
 	setCanDelegate(authorizationBean.getCanDelegate());
 	setMaxAmount(authorizationBean.getMaxAmount() != null ? authorizationBean.getMaxAmount() : Money.ZERO);
     }
 
     @Service
     public void changeUnit(final Unit unit) {
 	setUnit(unit);
     }
 
     public void findAcquisitionProcessesPendingAuthorization(final Set<AcquisitionProcess> result, final boolean recurseSubUnits) {
 	final Unit unit = getUnit();
 	unit.findAcquisitionProcessesPendingAuthorization(result, recurseSubUnits);
     }
 
     public boolean isPersonAbleToRevokeDelegatedAuthorization(Person person) {
 	return getPerson() == person
 		|| person.hasRoleType(RoleType.AQUISITIONS_UNIT_MANAGER)
 		|| person.hasRoleType(RoleType.MANAGER);
     }
 
     @Override
     public Boolean getCanDelegate() {
 	return super.getCanDelegate() && isValid();
     }
 
     @Service
     public void revoke() {
 	if (!isCurrentUserAbleToRevoke()) {
 	    throw new DomainException("error.person.not.authorized.to.revoke");
 	}
 	setEndDate(new LocalDate());
 	for (DelegatedAuthorization authorization : getDelegatedAuthorizations()) {
 	    authorization.revoke();
 	}
 	AuthorizationOperation.EDIT.log(this);
     }
 
     @Override
     public void setEndDate(LocalDate endDate) {
 
 	super.setEndDate(endDate);
 
 	if (endDate != null && (super.getEndDate() == null || super.getEndDate().isAfter(endDate))) {
 	    for (Authorization delegatedAuthorization : getDelegatedAuthorizations()) {
 		if (delegatedAuthorization.getEndDate() == null || delegatedAuthorization.getEndDate().isAfter(endDate)) {
 		    delegatedAuthorization.setEndDate(endDate);
 		}
 	    }
 	}
     }
 
     public boolean isValidFor(LocalDate date) {
 	return getEndDate() == null || getEndDate().isAfter(date);
     }
 
     public boolean isValid() {
 	return isValidFor(new LocalDate());
     }
 
    public boolean isValidAndIsCurrentUserResponsible() {
	return isValid() && getUnit().isResponsible(Person.getLoggedPerson());
    }

     public boolean isCurrentUserAbleToRevoke() {
 	final Person loggedPerson = Person.getLoggedPerson();
 	return loggedPerson != null && isValid() && isPersonAbleToRevokeDelegatedAuthorization(loggedPerson);
     }
 
     @Service
     public void delete() {
 	AuthorizationOperation.DELETE.log(this);
 	for (final DelegatedAuthorization delegatedAuthorization : getDelegatedAuthorizationsSet()) {
 	    delegatedAuthorization.delete();
 	}
 	removePerson();
 	removeUnit();
 	removeExpenditureTrackingSystem();
 	deleteDomainObject();
     }
 
     public void logEdit() {
 	AuthorizationOperation.EDIT.log(this);
     }
 
 }
