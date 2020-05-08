 package pt.ist.expenditureTrackingSystem.domain;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.SearchPaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixframework.pstm.Transaction;
 
 public class SavedSearch extends SavedSearch_Base {
 
     protected SavedSearch() {
 	setOjbConcreteClass(this.getClass().getName());
 	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
     }
 
     public SavedSearch(String name, Person person, SearchPaymentProcess searchBean) {
 	this();
 	if (person == null) {
 	    throw new DomainException("message.exception.aPersonIsNeededToSaveTheSearch");
 	}
 	setSearchName(name);
 	setPerson(person);
 	setProcessId(searchBean.getProcessId());
 	Class clazz = searchBean.getSearchClass();
 	setSearchClassName(clazz != null ? clazz.getName() : null);
 	setPendingOperations(searchBean.getHasAvailableAndAccessibleActivityForUser());
 	setShowOnlyResponsabilities(searchBean.getResponsibleUnitSetOnly());
 	setRequestor(searchBean.getRequestingPerson());
 	setAccountingUnit(searchBean.getAccountingUnit());
 	setUnit(searchBean.getRequestingUnit());
 	setPayingUnit(searchBean.getPayingUnit());
 	setRequestDocumentId(searchBean.getRequestDocumentId());
 	setAcquisitionProcessStateType(searchBean.getAcquisitionProcessStateType());
 	setRefundProcessStateType(searchBean.getRefundProcessStateType());
 	setProposalId(searchBean.getProposalId());
 	setRefundeeName(searchBean.getRefundeeName());
 	setShowOnlyAcquisitionsExcludedFromSupplierLimit(searchBean.getShowOnlyAcquisitionsExcludedFromSupplierLimit());
 	setShowOnlyAcquisitionsWithAdditionalCosts(searchBean.getShowOnlyAcquisitionsWithAdditionalCosts());
 	setYear(searchBean.getPaymentProcessYear());
 	setTakenBy(searchBean.getTaker());
 	setShowOnlyWithUnreadComments(searchBean.getShowOnlyWithUnreadComments());
 	setShowPriorityOnly(searchBean.getShowPriorityOnly());
     }
 
     public Class getSearchClass() {
 	if (getSearchClassName() == null) {
 	    return null;
 	}
 	try {
 	    return Class.forName(getSearchClassName());
 	} catch (ClassNotFoundException e) {
 	    throw new DomainException("message.exception.invalidClassInASavedSearch", e);
 	}
 
     }
 
     public SearchPaymentProcess getSearch() {
 	return new SearchPaymentProcess(this);
     }
 
     @Service
     public void delete() {
 	removeAccountingUnit();
 	removeExpenditureTrackingSystem();
 	removePerson();
 	removeRequestor();
 	removeSupplier();
 	removeUnit();
 	removePayingUnit();
 	getPeople().clear();
 	Transaction.deleteObject(this);
     }
 
     public boolean isSearchDefaultForUser(Person person) {
 	return person.getDefaultSearch() == this;
     }
 
     public boolean isSearchDefaultForCurrentUser() {
 	final Person person = Person.getLoggedPerson();
 	return isSearchDefaultForUser(person);
     }
 
     public static SavedSearch getOwnProcessesSearch() {
 	for (SavedSearch search : ExpenditureTrackingSystem.getInstance().getSystemSearches()) {
 	    if (search instanceof MyOwnProcessesSearch) {
 		return search;
 	    }
 	}
 	return null;
     }
 }
