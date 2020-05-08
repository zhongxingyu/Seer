 /*
  * @(#)Party.java
  *
  * Copyright 2009 Instituto Superior Tecnico
  * Founding Authors: João Figueiredo, Luis Cruz, Paulo Abrantes, Susana Fernandes
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Organization Module for the MyOrg web application.
  *
  *   The Organization Module is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU Lesser General Public License as published
  *   by the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.*
  *
  *   The Organization Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Organization Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package module.organization.domain;
 
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.TreeSet;
 
 import module.organization.domain.predicates.PartyPredicate;
 import module.organization.domain.predicates.PartyResultCollection;
 import module.organization.domain.predicates.PartyPredicate.PartyByAccountabilityType;
 import module.organization.domain.predicates.PartyPredicate.PartyByClassType;
 import module.organization.domain.predicates.PartyPredicate.PartyByPartyType;
 import module.organization.domain.predicates.PartyPredicate.TruePartyPredicate;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.MyOrg;
 import myorg.domain.RoleType;
 import myorg.domain.User;
 import myorg.domain.exceptions.DomainException;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.fenixWebFramework.services.Service;
 import pt.utl.ist.fenix.tools.util.i18n.Language;
 
 abstract public class Party extends Party_Base {
 
     static public final Comparator<Party> COMPARATOR_BY_NAME = new Comparator<Party>() {
 	@Override
 	public int compare(Party o1, Party o2) {
 	    int res = o1.getPartyName().compareTo(o2.getPartyName());
 	    return res != 0 ? res : o1.getExternalId().compareTo(o2.getExternalId());
 	}
     };
 
     static public final Comparator<Party> COMPARATOR_BY_TYPE_AND_NAME = new Comparator<Party>() {
 	@Override
 	public int compare(Party o1, Party o2) {
 	    int result = o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
 	    return result == 0 ? COMPARATOR_BY_NAME.compare(o1, o2) : result;
 	}
     };
 
     protected Party() {
 	super();
 	setMyOrg(MyOrg.getInstance());
     }
 
     protected void check(final Object obj, final String message) {
 	if (obj == null) {
 	    throw new DomainException(message);
 	}
     }
 
     public Collection<Party> getParents() {
 	return getParents(new TruePartyPredicate());
     }
 
     public Collection<Party> getParents(final AccountabilityType type) {
 	return getParents(new PartyByAccountabilityType(type));
     }
 
     public Collection<Party> getParents(final Collection<AccountabilityType> types) {
 	return getParents(new PartyByAccountabilityType(types));
     }
 
     public Collection<Party> getParents(final PartyType type) {
 	return getParents(new PartyByPartyType(type));
     }
 
     public Collection<Unit> getParentUnits() {
 	return getParents(new PartyByClassType(Unit.class));
     }
 
     public Collection<Unit> getParentUnits(final AccountabilityType type) {
 	return getParents(new PartyByAccountabilityType(Unit.class, type));
     }
 
     public Collection<Unit> getParentUnits(final PartyType type) {
 	return getParents(new PartyByPartyType(Unit.class, type));
     }
 
     @SuppressWarnings("unchecked")
     protected <T extends Party> Collection<T> getParents(final PartyPredicate predicate) {
 	final Collection<Party> result = new LinkedList<Party>();
 	for (final Accountability accountability : getParentAccountabilities()) {
 	    if (predicate.eval(accountability.getParent(), accountability)) {
 		result.add(accountability.getParent());
 	    }
 	}
 	return (List<T>) result;
     }
 
     public Collection<Accountability> getParentAccountabilities(final Collection<AccountabilityType> types) {
 	return getParentAccountabilities(new PartyByAccountabilityType(types));
     }
 
     public Collection<Accountability> getParentAccountabilities(final AccountabilityType... types) {
 	return getParentAccountabilities(new PartyByAccountabilityType(types));
     }
 
     @SuppressWarnings("unchecked")
     protected <T extends Accountability> Collection<T> getParentAccountabilities(final PartyPredicate predicate) {
 	final Collection<Accountability> result = new LinkedList<Accountability>();
 	for (final Accountability accountability : getParentAccountabilities()) {
 	    if (predicate.eval(accountability.getParent(), accountability)) {
 		result.add(accountability);
 	    }
 	}
 	return (List<T>) result;
     }
 
     public Collection<Party> getChildren() {
 	return getChildren(new TruePartyPredicate());
     }
 
     public Collection<Party> getChildren(final AccountabilityType type) {
 	return getChildren(new PartyByAccountabilityType(type));
     }
 
     public Collection<Party> getChildren(final Collection<AccountabilityType> types) {
 	return getChildren(new PartyByAccountabilityType(types));
     }
 
     public Collection<Party> getChildren(final PartyType type) {
 	return getChildren(new PartyByPartyType(type));
     }
 
     public Collection<Unit> getChildUnits() {
 	return getChildren(new PartyByClassType(Unit.class));
     }
 
     public Collection<Unit> getChildUnits(final AccountabilityType type) {
 	return getChildren(new PartyByAccountabilityType(Unit.class, type));
     }
 
     public Collection<Unit> getChildUnits(final PartyType type) {
 	return getChildren(new PartyByPartyType(Unit.class, type));
     }
 
     public Collection<Person> getChildPersons() {
 	return getChildren(new PartyByClassType(Person.class));
     }
 
     public Collection<Person> getChildPersons(final AccountabilityType type) {
 	return getChildren(new PartyByAccountabilityType(Person.class, type));
     }
 
     public Collection<Person> getChildPersons(final PartyType type) {
 	return getChildren(new PartyByPartyType(Person.class, type));
     }
 
     @SuppressWarnings("unchecked")
     public <T extends Party> Collection<T> getChildren(final PartyPredicate predicate) {
 	final Collection<Party> result = new LinkedList<Party>();
 	for (final Accountability accountability : getChildAccountabilities()) {
 	    if (predicate.eval(accountability.getChild(), accountability)) {
 		result.add(accountability.getChild());
 	    }
 	}
 	return (List<T>) result;
     }
 
     public Collection<Accountability> getChildrenAccountabilities(final Collection<AccountabilityType> types) {
 	return getChildrenAccountabilities(new PartyByAccountabilityType(types));
     }
 
     public Collection<Accountability> getChildrenAccountabilities(final AccountabilityType... types) {
 	return getChildrenAccountabilities(new PartyByAccountabilityType(types));
     }
 
     public Collection<Accountability> getChildrenAccountabilities(final Class<? extends Party> clazz,
 	    final Collection<AccountabilityType> types) {
 	return getChildrenAccountabilities(new PartyByAccountabilityType(clazz, types));
     }
 
     @SuppressWarnings("unchecked")
     protected <T extends Accountability> Collection<T> getChildrenAccountabilities(final PartyPredicate predicate) {
 	final Collection<Accountability> result = new LinkedList<Accountability>();
 	for (final Accountability accountability : getChildAccountabilities()) {
 	    if (predicate.eval(accountability.getChild(), accountability)) {
 		result.add(accountability);
 	    }
 	}
 	return (List<T>) result;
     }
 
     public Collection<Party> getAncestors() {
 	final PartyResultCollection result = new PartyResultCollection(new TruePartyPredicate());
 	getAncestors(result);
 	return result.getResult();
     }
 
     public Collection<Party> getAncestors(final AccountabilityType type) {
 	final PartyResultCollection result = new PartyResultCollection(new PartyByAccountabilityType(type));
 	getAncestors(result);
 	return result.getResult();
     }
 
     public Collection<Party> getAncestors(final PartyType type) {
 	final PartyResultCollection result = new PartyResultCollection(new PartyByPartyType(type));
 	getAncestors(result);
 	return result.getResult();
     }
 
     public Collection<Unit> getAncestorUnits() {
 	final PartyResultCollection result = new PartyResultCollection(new PartyByClassType(Unit.class));
 	getAncestors(result);
 	return result.getResult();
     }
 
     public Collection<Unit> getAncestorUnits(final AccountabilityType type) {
 	final PartyResultCollection result = new PartyResultCollection(new PartyByAccountabilityType(Unit.class, type));
 	getAncestors(result);
 	return result.getResult();
     }
 
     public Collection<Unit> getAncestorUnits(final PartyType type) {
 	final PartyResultCollection result = new PartyResultCollection(new PartyByPartyType(Unit.class, type));
 	getAncestors(result);
 	return result.getResult();
     }
 
     protected void getAncestors(final PartyResultCollection result) {
 	for (final Accountability accountability : getParentAccountabilities()) {
 	    result.conditionalAddParty(accountability.getParent(), accountability);
 	    accountability.getParent().getAncestors(result);
 	}
     }
 
     public boolean ancestorsInclude(final Party party, final AccountabilityType type) {
 	for (final Accountability accountability : getParentAccountabilities()) {
 	    if (accountability.hasAccountabilityType(type)) {
 		if (accountability.getParent().equals(party)) {
 		    return true;
 		}
 		if (accountability.getParent().ancestorsInclude(party, type)) {
 		    return true;
 		}
 	    }
 	}
 
 	return false;
     }
 
     public Collection<Party> getDescendents() {
 	final PartyResultCollection result = new PartyResultCollection(new TruePartyPredicate());
 	getDescendents(result);
 	return result.getResult();
     }
 
     public Collection<Party> getDescendents(final AccountabilityType type) {
 	final PartyResultCollection result = new PartyResultCollection(new PartyByAccountabilityType(type));
 	getDescendents(result);
 	return result.getResult();
     }
 
     public Collection<Party> getDescendents(final PartyType type) {
 	final PartyResultCollection result = new PartyResultCollection(new PartyByPartyType(type));
 	getDescendents(result);
 	return result.getResult();
     }
 
     public Collection<Unit> getDescendentUnits() {
 	final PartyResultCollection result = new PartyResultCollection(new PartyByClassType(Unit.class));
 	getDescendents(result);
 	return result.getResult();
     }
 
     public Collection<Unit> getDescendentUnits(final AccountabilityType type) {
 	final PartyResultCollection result = new PartyResultCollection(new PartyByAccountabilityType(Unit.class, type));
 	getDescendents(result);
 	return result.getResult();
     }
 
     public Collection<Unit> getDescendentUnits(final PartyType type) {
 	final PartyResultCollection result = new PartyResultCollection(new PartyByPartyType(Unit.class, type));
 	getDescendents(result);
 	return result.getResult();
     }
 
     public Collection<Person> getDescendentPersons() {
 	final PartyResultCollection result = new PartyResultCollection(new PartyByClassType(Person.class));
 	getDescendents(result);
 	return result.getResult();
     }
 
     public Collection<Person> getDescendentPersons(final AccountabilityType type) {
 	final PartyResultCollection result = new PartyResultCollection(new PartyByAccountabilityType(Person.class, type));
 	getDescendents(result);
 	return result.getResult();
     }
 
     public Collection<Person> getDescendentPersons(final PartyType type) {
 	final PartyResultCollection result = new PartyResultCollection(new PartyByPartyType(Person.class, type));
 	getDescendents(result);
 	return result.getResult();
     }
 
     protected void getDescendents(final PartyResultCollection result) {
 	for (final Accountability accountability : getChildAccountabilities()) {
 	    result.conditionalAddParty(accountability.getChild(), accountability);
 	    accountability.getChild().getDescendents(result);
 	}
     }
 
     public Collection<Party> getSiblings() {
 	final Collection<Party> result = new LinkedList<Party>();
 	for (final Accountability accountability : getParentAccountabilities()) {
 	    result.addAll(accountability.getParent().getChildren());
 	}
 	result.remove(this);
 	return result;
     }
 
     public boolean isUnit() {
 	return false;
     }
 
     public boolean isPerson() {
 	return false;
     }
 
     @Service
     public void delete() {
 	canDelete();
 	disconnect();
 	deleteDomainObject();
     }
 
     protected void canDelete() {
 	if (hasAnyChildAccountabilities()) {
 	    throw new DomainException("error.Party.delete.has.child.accountabilities");
 	}
     }
 
     protected void disconnect() {
 	while (hasAnyParentAccountabilities()) {
 	    getParentAccountabilities().get(0).delete();
 	}
 	getPartyTypes().clear();
 	removeMyOrg();
     }
 
     @Service
     public Accountability addParent(final Party parent, final AccountabilityType type, final LocalDate begin, final LocalDate end) {
 	return Accountability.create(parent, this, type, begin, end);
     }
 
     @Service
     public Accountability addChild(final Party child, final AccountabilityType type, final LocalDate begin, final LocalDate end) {
 	for (final Accountability accountability : getChildAccountabilitiesSet()) {
 	    if (accountability.getChild() == child && accountability.getAccountabilityType() == type
 		    && accountability.intersects(begin, end)) {
 		if (begin == null
 			|| (begin != null && accountability.getBeginDate() != null && begin.isBefore(accountability
 				.getBeginDate()))) {
 		    accountability.setBeginDate(begin);
 		}
 		if (end == null
 			|| (end != null && accountability.getEndDate() != null && end.isAfter(accountability.getEndDate()))) {
 		    accountability.setEndDate(end);
 		}
 		return accountability;
 	    }
 	}
 
 	return Accountability.create(this, child, type, begin, end);
     }
 
     @Service
     public void removeParent(final Accountability accountability) {
 	if (hasParentAccountabilities(accountability)) {
 	    if (isUnit() && getParentAccountabilitiesCount() == 1) {
 		throw new DomainException("error.Party.cannot.remove.parent.accountability");
 	    }
 	    accountability.delete();
 	}
     }
 
     @Service
     public void editPartyTypes(final List<PartyType> partyTypes) {
 	getPartyTypes().retainAll(partyTypes);
 	getPartyTypes().addAll(partyTypes);
 
 	if (getPartyTypesSet().isEmpty()) {
 	    throw new DomainException("error.Party.must.have.at.least.one.party.type");
 	}
 	if (!accountabilitiesStillValid()) {
 	    throw new DomainException("error.Party.invalid.party.types.accountability.rules.are.not.correct");
 	}
 
     }
 
     protected boolean accountabilitiesStillValid() {
 	for (final Accountability accountability : getParentAccountabilitiesSet()) {
 	    if (!accountability.isValid()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public String getPartyNameWithSuffixType() {
 	return ResourceBundle.getBundle("resources.OrganizationResources", Language.getLocale()).getString(
 		"label." + getClass().getSimpleName().toLowerCase())
 		+ " - " + getPartyName().getContent();
     }
 
     public Set<OrganizationalModel> getAllOrganizationModels() {
 	final Set<OrganizationalModel> organizationModels = new TreeSet<OrganizationalModel>(
 		OrganizationalModel.COMPARATORY_BY_NAME);
 	addAllOrganizationModels(organizationModels, new HashSet<Party>());
 	return organizationModels;
     }
 
     public void addAllOrganizationModels(final Set<OrganizationalModel> organizationModels, final Set<Party> processed) {
 	if (!processed.contains(this)) {
 	    processed.add(this);
 	    organizationModels.addAll(getOrganizationalModelsSet());
 	    for (final Accountability accountability : getParentAccountabilitiesSet()) {
 		final Party party = accountability.getParent();
 		party.addAllOrganizationModels(organizationModels, processed);
 	    }
 	}
     }
 
     public void setPartyTypes(final List<PartyType> partyTypes) {
 	getPartyTypesSet().clear();
 	getPartyTypesSet().addAll(partyTypes);
     }
 
     public boolean isAuthorizedToManage() {
 	final User user = UserView.getCurrentUser();
 	return user == null || user.hasRoleType(RoleType.MANAGER);
     }
 
    public boolean hasChildAccountabilityIncludingAncestry(final List<AccountabilityType> accountabilityTypes, final Party party) {
 	return hasChildAccountabilityIncludingAncestry(new HashSet<Party>(), accountabilityTypes, party);
     }
 
     private boolean hasChildAccountabilityIncludingAncestry(final Set<Party> processed,
	    final List<AccountabilityType> accountabilityTypes, final Party party) {
 	if (!processed.contains(this)) {
 	    processed.add(this);
 	    for (final Party child : getChildren(accountabilityTypes)) {
 		if (child == party) {
 		    return true;
 		}
 	    }
 	    for (final Party parent : getParents(accountabilityTypes)) {
 		if (parent.hasChildAccountabilityIncludingAncestry(processed, accountabilityTypes, party)) {
 		    return true;
 		}
 	    }
 	}
 	return false;
     }
 
     public static Party findPartyByPartyTypeAndAcronymForAccountabilityTypeLink(final Set<Party> parties,
 	    final AccountabilityType accountabilityType, final PartyType partyType, final String acronym) {
 	for (final Party party : parties) {
 	    final Party result = party.findPartyByPartyTypeAndAcronymForAccountabilityTypeLink(accountabilityType, partyType,
 		    acronym);
 	    if (result != null) {
 		return result;
 	    }
 	}
 	return null;
     }
 
     protected Party findPartyByPartyTypeAndAcronymForAccountabilityTypeLink(final AccountabilityType accountabilityType,
 	    final PartyType partyType, final String acronym) {
 	return null;
     }
 
     public String getPresentationName() {
 	return getPartyName().getContent();
     }
 
     public boolean isTop() {
 	return false;
     }
 
     public boolean hasActiveAncestry(final AccountabilityType accountabilityType, final LocalDate when) {
 	return isTop() || hasParentWithActiveAncestry(accountabilityType, when);
     }
 
     public boolean hasDirectActiveAncestry(final AccountabilityType accountabilityType, final LocalDate when) {
 	return !getParents(new PartyPredicate() {
 
 	    @Override
 	    public boolean eval(Party party, Accountability accountability) {
 		return accountability.getAccountabilityType() == accountabilityType && accountability.isActive(when);
 	    }
 
 	}).isEmpty();
     }
 
     private boolean hasParentWithActiveAncestry(final AccountabilityType accountabilityType, final LocalDate when) {
 	for (final Accountability accountability : getParentAccountabilitiesSet()) {
 	    if (accountability.getAccountabilityType() == accountabilityType && accountability.isActive(when)) {
 		final Party parent = accountability.getParent();
 		return parent.hasActiveAncestry(accountabilityType, when);
 	    }
 	}
 	return false;
     }
 
 }
