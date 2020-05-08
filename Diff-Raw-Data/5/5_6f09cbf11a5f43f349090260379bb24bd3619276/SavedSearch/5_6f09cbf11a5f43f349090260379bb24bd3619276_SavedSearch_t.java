 package pt.ist.expenditureTrackingSystem.domain;
 
 import myorg.domain.exceptions.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.SearchPaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.fenixWebFramework.services.Service;
 
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
 	setSearchProcessValues(searchBean.getSearchProcess());
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
 
     public SearchPaymentProcess getSearch() {
 	return new SearchPaymentProcess(this);
     }
 
     @Service
     public void delete() {
 	removeTakenBy();
 	removeYear();
 	removePayingUnit();
 	removePerson();
 	removeUnit();
 	removeRequestor();
 	removeAccountingUnit();
 	removeSupplier();
	SavedSearch ownProcessesSearch = MyOwnProcessesSearch.getOwnProcessesSearch();
	for (Person person : getPeople()) {
	    person.setDefaultSearch(ownProcessesSearch);
	}
 	removeExpenditureTrackingSystemForSystemSearch();
 	removeExpenditureTrackingSystem();
 	deleteDomainObject();
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
 
     @Override
     public Boolean getShowPriorityOnly() {
 	Boolean value = super.getShowPriorityOnly();
 	return value != null ? value : Boolean.FALSE;
     }
 }
