 /*
  * @(#)SubProject.java
  *
  * Copyright 2009 Instituto Superior Tecnico
  * Founding Authors: Luis Cruz, Nuno Ochoa, Paulo Abrantes
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Expenditure Tracking Module.
  *
  *   The Expenditure Tracking Module is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.
  *
  *   The Expenditure Tracking Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Expenditure Tracking Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package pt.ist.expenditureTrackingSystem.domain.organization;
 
 import module.organization.domain.Party;
 import module.organization.domain.PartyType;
 
 import org.apache.commons.lang.StringUtils;
 
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Financer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.ProjectFinancer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestWithPayment;
 import dml.runtime.RelationAdapter;
 
 /**
  * 
  * @author Luis Cruz
  * @author Paulo Abrantes
  * 
  */
 public class SubProject extends SubProject_Base {
 
     public static class SubProjectPartyTypeListener extends RelationAdapter<Party, PartyType> {
 
 	@Override
 	public void afterAdd(final Party party, final PartyType partyType) {
 	    if (party.isUnit() && partyType != null && partyType == ExpenditureTrackingSystem.getInstance().getSubProjectPartyType()) {
 		new SubProject((module.organization.domain.Unit) party);
 	    }
 	}
 
     }
 
     static {
 	Party.PartyTypeParty.addListener(new SubProjectPartyTypeListener());
     }
 
     public SubProject(final module.organization.domain.Unit unit) {
 	super();
 	setUnit(unit);
     }
 
     public SubProject(final Project parentUnit, final String name) {
 	super();
 	final String acronym = StringUtils.abbreviate(name, 5);
 	createRealUnit(this, parentUnit, ExpenditureTrackingSystem.getInstance().getSubProjectPartyType(), acronym, name);
 
 	// TODO : After this object is refactored to retrieve the name and
 	// parent from the real unit,
 	// the following two lines may be deleted.
 	setName(name);
 	setParentUnit(parentUnit);
     }
 
     @Override
     public void setName(final String name) {
 	super.setName(name);
 	final Project project = (Project) getParentUnit();
 	if (project == null) {
 	    final String acronym = StringUtils.abbreviate(name, 5);
 	    getUnit().setAcronym(acronym);
 	} else {
 	    getUnit().setAcronym(project.getUnit().getAcronym());
 	}
     }
 
     @Override
     public String getPresentationName() {
 	return "(" + getUnit().getAcronym() + ") " + " - " + super.getPresentationName();
     }
 
     @Override
     public void setParentUnit(final Unit parentUnit) {
 	super.setParentUnit(parentUnit);
 	if (parentUnit != null && hasUnit()) {
 	    getUnit().setAcronym(parentUnit.getUnit().getAcronym());
 	}
     }
 
     @Override
     public AccountingUnit getAccountingUnit() {
 	final AccountingUnit accountingUnit = super.getAccountingUnit();
	return accountingUnit == null ? getParentUnit().getAccountingUnit() : accountingUnit;
     }
 
     @Override
     public Financer finance(final RequestWithPayment acquisitionRequest) {
 	return new ProjectFinancer(acquisitionRequest, this);
     }
 /*
     @Override
     public IndexDocument getDocumentToIndex() {
 	IndexDocument document = super.getDocumentToIndex();
 	document.indexField(UnitIndexFields.NUMBER_INDEX, getUnit().getAcronym());
 	return document;
     }
 */
     @Override
     public boolean isAccountingResponsible(final Person person) {
 	final Project project = (Project) getParentUnit();
 	return project.isAccountingResponsible(person);
     }
 
     @Override
     public String getUnitNumber() {
 	final Unit parentUnit = getParentUnit();
 	return parentUnit == null ? null : parentUnit.getUnitNumber();
     }
 
 }
