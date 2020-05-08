 /*
  * @(#)Accountability.java
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
 
 import java.util.Comparator;
 
 import jvstm.cps.ConsistencyPredicate;
 import module.organization.domain.util.OrganizationConsistencyException;
 import myorg.domain.MyOrg;
 import myorg.domain.exceptions.DomainException;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.fenixWebFramework.services.Service;
 
 public class Accountability extends Accountability_Base {
 
     protected static final String LOCAL_DATE_FORMAT = "yyyy-MM-dd";
 
     public static final Comparator<Accountability> COMPARATOR_BY_PARENT_PARTY_NAMES = new Comparator<Accountability>() {
 
 	@Override
 	public int compare(final Accountability o1, final Accountability o2) {
 	    final int c = Party.COMPARATOR_BY_NAME.compare(o1.getParent(), o2.getParent());
 	    return c == 0 ? o1.getExternalId().compareTo(o2.getExternalId()) : c;
 	}
 
     };
 
     public static final Comparator<Accountability> COMPARATOR_BY_CHILD_PARTY_NAMES = new Comparator<Accountability>() {
 
 	@Override
 	public int compare(final Accountability o1, final Accountability o2) {
 	    final int c = Party.COMPARATOR_BY_NAME.compare(o1.getChild(), o2.getChild());
 	    return c == 0 ? o1.getExternalId().compareTo(o2.getExternalId()) : c;
 	}
 
     };
 
     protected Accountability() {
 	super();
 	setMyOrg(MyOrg.getInstance());
 	setBeginDate(new LocalDate());
     }
 
     protected Accountability(final Party parent, final Party child, final AccountabilityType type, final LocalDate begin,
 	    final LocalDate end) {
 	this();
 
 	check(parent, "error.Accountability.invalid.parent");
 	check(child, "error.Accountability.invalid.child");
 	check(type, "error.Accountability.invalid.type");
 	check(begin, "error.Accountability.invalid.begin");
 	checkDates(parent, begin, end);
 
 	canCreate(parent, child, type);
 
 	setParent(parent);
 	setChild(child);
 	setAccountabilityType(type);
 	setBeginDate(begin);
 	setEndDate(end);
     }
 
     protected void checkDates(final Party parent, final LocalDate begin, final LocalDate end) {
 	if (begin != null && end != null && begin.isAfter(end)) {
 	    throw new DomainException("error.Accountability.begin.is.after.end");
 	}
 	checkBeginFromOldestParentAccountability(parent, begin);
     }
 
     private void checkBeginFromOldestParentAccountability(final Party parent, final LocalDate begin) throws DomainException {
 	Accountability oldest = null;
 	for (final Accountability accountability : parent.getParentAccountabilitiesSet()) {
 	    if (oldest == null || accountability.getBeginDate().isBefore(oldest.getBeginDate())) {
 		oldest = accountability;
 	    }
 	}
 
 	if (oldest != null && begin.isBefore(oldest.getBeginDate())) {
 	    final String[] args = new String[] { oldest.getChild().getPartyName().getContent(),
 		    oldest.getBeginDate().toString("dd/MM/yyyy") };
 	    throw new DomainException("error.Accountability.begin.starts.before.oldest.parent.begin", args);
 	}
     }
 
     protected void check(final Object obj, final String message) {
 	if (obj == null) {
 	    throw new DomainException(message);
 	}
     }
 
     protected void canCreate(final Party parent, final Party child, final AccountabilityType type) {
 	if (parent.equals(child)) {
 	    throw new DomainException("error.Accountability.parent.equals.child");
 	}
 	if (parent.ancestorsInclude(child, type)) {
 	    throw new DomainException("error.Accountability.parent.ancestors.include.child.with.type");
 	}
 	if (!type.isValid(parent, child)) {
 	    throw new DomainException("error.Accountability.type.doesnot.allow.parent.child");
 	}
     }
 
     public boolean isValid() {
 	return hasParent() && hasChild() && getAccountabilityType().isValid(getParent(), getChild());
     }
 
     @ConsistencyPredicate(OrganizationConsistencyException.class)
     protected boolean checkDateInterval() {
 	return hasBeginDate() && (!hasEndDate() || !getBeginDate().isAfter(getEndDate()));
     }
 
     public boolean isActive(final LocalDate date) {
 	return contains(date);
     }
 
     public boolean isActiveNow() {
 	final LocalDate now = new LocalDate();
 	return isActive(now);
     }
 
     public boolean contains(final LocalDate date) {
 	return !getBeginDate().isAfter(date) && (!hasEndDate() || getEndDate().isAfter(date));
     }
 
     public boolean contains(final LocalDate begin, final LocalDate end) {
 	check(begin, "error.Accountability.intercepts.invalid.begin");
 	return (end == null || !getBeginDate().isAfter(end)) && (!hasEndDate() || !begin.isAfter(getEndDate()));
     }
 
     private boolean hasBeginDate() {
 	return getBeginDate() != null;
     }
 
     private boolean hasEndDate() {
 	return getEndDate() != null;
     }
 
     public boolean hasAccountabilityType(AccountabilityType type) {
 	return getAccountabilityType().equals(type);
     }
 
     public String getDetailsString() {
 	final StringBuilder stringBuilder = new StringBuilder();
 	stringBuilder.append(getAccountabilityType().getName().getContent());
 	stringBuilder.append(": ");
 	if (getBeginDate() != null) {
 	    stringBuilder.append(getBeginDate().toString(LOCAL_DATE_FORMAT));
 	}
 	stringBuilder.append(" - ");
 	if (getEndDate() != null) {
 	    stringBuilder.append(getEndDate().toString(LOCAL_DATE_FORMAT));
 	}
 	return stringBuilder.toString();
     }
 
     @Service
     void delete() {
 	removeParent();
 	removeChild();
 	removeAccountabilityType();
 	//	removeAccountabilityImportRegister();
 	removeMyOrg();
 	deleteDomainObject();
     }
 
     static Accountability create(final Party parent, final Party child, final AccountabilityType type, final LocalDate begin,
 	    final LocalDate end) {
	return parent.isAuthorizedToManage() ? new Accountability(parent, child, type, begin, end)
		: new UnconfirmedAccountability(parent, child, type, begin, end);
     }
 
     @Service
     public void editDates(final LocalDate begin, final LocalDate end) {
 	check(begin, "error.Accountability.invalid.begin");
 	checkDates(getParent(), begin, end);
 	setBeginDate(begin);
 	setEndDate(end);
     }
 
     public boolean intersects(final LocalDate begin, final LocalDate end) {
 	return !isAfter(getBeginDate(), end) && !isAfter(begin, getEndDate());
     }
 
     private static boolean isAfter(final LocalDate localDate1, final LocalDate localDate2) {
 	return localDate1 != null && localDate2 != null && localDate2.isBefore(localDate1);
     }
 
 }
