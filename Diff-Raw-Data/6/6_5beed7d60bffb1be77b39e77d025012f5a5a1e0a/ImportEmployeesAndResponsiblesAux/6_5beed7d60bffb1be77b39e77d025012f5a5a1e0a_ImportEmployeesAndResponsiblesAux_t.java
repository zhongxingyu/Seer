 /*
  * @(#)ImportEmployeesAndResponsiblesAux.java
  *
  * Copyright 2011 Instituto Superior Tecnico
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
 package module.organizationIst.domain.task;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import module.organization.domain.Accountability;
 import module.organization.domain.AccountabilityType;
 import module.organization.domain.OrganizationalModel;
 import module.organization.domain.Party;
 import module.organization.domain.Person;
 import module.organizationIst.domain.IstAccountabilityType;
 import myorg.domain.MyOrg;
 import myorg.domain.User;
 import net.sourceforge.fenixedu.domain.RemotePerson;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 
 /**
  * 
  * @author João Antunes
  * @author Luis Cruz
  * 
  */
 public class ImportEmployeesAndResponsiblesAux {
 
     public static void executeTask() {
 	final User user = User.findByUsername("ist24439");
 	final Person somePerson = user.getPerson();
 	final RemotePerson someRemotePerson = somePerson.getRemotePerson();
 
 	final String allTeacherInformation = someRemotePerson.readAllTeacherInformation();
 	updateInformation(IstAccountabilityType.TEACHING_PERSONNEL, allTeacherInformation, true);
 	final String allResearcherInformation = someRemotePerson.readAllResearcherInformation();
 	updateInformation(IstAccountabilityType.RESEARCH_PERSONNEL, allResearcherInformation, true);
 	final String allEmployeeInformation = someRemotePerson.readAllEmployeeInformation();
 	updateInformation(IstAccountabilityType.PERSONNEL, allEmployeeInformation, true);
 	final String allGrantOwnerInformation = someRemotePerson.readAllGrantOwnerInformation();
 	updateInformation(IstAccountabilityType.GRANT_OWNER_PERSONNEL, allGrantOwnerInformation, true);
 	final String allExternalResearcherInformation = someRemotePerson.readAllExternalResearcherInformation();
 	updateInformation(IstAccountabilityType.EXTERNAL_RESEARCH_PERSONNEL, allExternalResearcherInformation, false);
     }
 
     private static void updateInformation(final IstAccountabilityType istAccountabilityType, final String allInformation,
 	    final boolean updateEmploymentInfo) {
 	final Set<String> usernames = new HashSet<String>();
 
 	final AccountabilityType accountabilityType = istAccountabilityType.readAccountabilityType();
 	final LocalDate now = new LocalDate();
 	for (int i = 0; i < allInformation.length(); ) {
 	    final int sep1 = allInformation.indexOf(':', i);
 	    final int sep2 = allInformation.indexOf(':', sep1 + 1);
 	    final int sep3 = allInformation.indexOf(':', sep2 + 1);
 	    final int sep4 = allInformation.indexOf('|', sep3 + 1);
 
 	    if (sep1 > i && sep2 > sep1 && sep3 > sep2 && sep4 > sep3) {
 		final String username = allInformation.substring(i, sep1);
 		usernames.add(username);
 		final String costCenterCode = allInformation.substring(sep2 + 1, sep3);
 		final String institution = allInformation.substring(sep3 + 1, sep4);
 
 		updateInformation(now, accountabilityType, username, costCenterCode, allInformation, institution, updateEmploymentInfo);
 		i = sep4 + 1;
 	    } else {
 		i++;
 	    }
 	}
 
 	for (final User user : MyOrg.getInstance().getUserSet()) {
 	    final String username = user.getUsername();
 	    if (!usernames.contains(username)) {
 		closeAccountabilities(user, accountabilityType, now);
 	    }
 	}
     }
 
     private static void closeAccountabilities(final User user, final AccountabilityType accountabilityType, final LocalDate now) {
 	if (user.hasPerson()) {
 	    final Person person = user.getPerson();
 	    for (final Accountability accountability : person.getParentAccountabilitiesSet()) {
 		if (accountability.getAccountabilityType() == accountabilityType && accountability.isActive(now)) {
 		    accountability.editDates(accountability.getBeginDate(), now);
 		}
 	    }
 	}
     }
 
     private static void updateInformation(final LocalDate now, final AccountabilityType accountabilityType, 
 	    final String username, final String costCenterCode, final String allInformation,
 	    final String employer, final boolean updateEmploymentInfo) {
 	final User user = User.findByUsername(username);
 	if (user != null) {
 	    final Person person = user.getPerson();
 	    if (person != null) {
 		final Unit unit = CostCenter.findUnitByCostCenter(costCenterCode);
 		if (unit != null) {
 		    updateInformation(now, accountabilityType, person, unit.getUnit(), allInformation, username);
 		} else {
 		    System.out.println("Did not find cost center with code: " + costCenterCode);
 		}
 		if (updateEmploymentInfo) {
 		    updateEmployerInformation(person, now, employer);
 		}
 	    } else {
 		System.out.println("User with username: " + username + " has no person");
 	    }
 	} else {
 	    System.out.println("Did not find user with username: " + username);
 	}
     }
 
     private static void updateEmployerInformation(final Person person, final LocalDate now, final String employer) {
 	if (employer != null) {
 	    final AccountabilityType accountabilityType = IstAccountabilityType.EMPLOYMENT.readAccountabilityType();
 	    for (final Accountability accountability : person.getParentAccountabilitiesSet()) {
 		if (accountability.getAccountabilityType() == accountabilityType && accountability.isActive(now)) {
 		    final Party parent = accountability.getParent();
 		    if (parent.isUnit()) {
 			final module.organization.domain.Unit unit = (module.organization.domain.Unit) parent;
 			final String acronym = unit.getAcronym();
 			if (employer.equals(acronym)) {
 			    return;
 			} else {
 			    accountability.editDates(accountability.getBeginDate(), now);
 			}
 		    }
 		}
 	    }
 	    final module.organization.domain.Unit unit = findUnit(employer);
	    if (unit != null) {
		unit.addChild(person, accountabilityType, now, null);
	    } else {
		System.out.println("No unit with name: " + employer + " was found!");
	    }
 	}
     }
 
     private static module.organization.domain.Unit findUnit(final String employer) {
 	for (final OrganizationalModel organizationalModel : MyOrg.getInstance().getOrganizationalModelsSet()) {
 	    for (final Party party : organizationalModel.getPartiesSet()) {
 		if (party.isUnit()) {
 		    final module.organization.domain.Unit unit = (module.organization.domain.Unit) party;
 		    final String acronym = unit.getAcronym();
 		    if (employer.equals(acronym)) {
 			return unit;
 		    }
 		}
 	    }
 	}
 	return null;
     }
 
     private static void updateInformation(final LocalDate now, final AccountabilityType accountabilityType,
 	    final Person person, final module.organization.domain.Unit unit, final String allInformation, final String username) {
 	for (final Accountability accountability : person.getParentAccountabilitiesSet()) {
 	    if (accountability.getAccountabilityType() == accountabilityType && accountability.isActive(now)) {
 		if (accountability.getParent() == unit) {
 		    return;
 		} else {
 		    final Unit expenditureUnit = ((module.organization.domain.Unit) accountability.getParent()).getExpenditureUnit();
 		    if (expenditureUnit instanceof CostCenter) {
 			final CostCenter center = (CostCenter) expenditureUnit;
 			final String checkString = username + ':' + getAccountabilityTypeString(accountabilityType) + ':' + center.getCostCenter();
 			if (allInformation.indexOf(checkString) < 0) {
 			    accountability.editDates(accountability.getBeginDate(), now);
 			}
 		    } else {
 			accountability.editDates(accountability.getBeginDate(), now);
 		    }
 		}
 	    }
 	}
 	unit.addChild(person, accountabilityType, now, null);
     }
 
     private static String getAccountabilityTypeString(final AccountabilityType accountabilityType) {
 	if (accountabilityType == IstAccountabilityType.TEACHING_PERSONNEL.readAccountabilityType()) {
 	    return "TEACHER";
 	}
 	if (accountabilityType == IstAccountabilityType.RESEARCH_PERSONNEL.readAccountabilityType()) {
 	    return "RESEARCHER";
 	}
 	if (accountabilityType == IstAccountabilityType.PERSONNEL.readAccountabilityType()) {
 	    return "EMPLOYEE";
 	}
 	if (accountabilityType == IstAccountabilityType.GRANT_OWNER_PERSONNEL.readAccountabilityType()) {
 	    return "GRANT_OWNER";
 	}
 	if (accountabilityType == IstAccountabilityType.EXTERNAL_RESEARCH_PERSONNEL.readAccountabilityType()) {
 	    return "RESEARCHER";
 	}
 	return null;
     }
 
 }
