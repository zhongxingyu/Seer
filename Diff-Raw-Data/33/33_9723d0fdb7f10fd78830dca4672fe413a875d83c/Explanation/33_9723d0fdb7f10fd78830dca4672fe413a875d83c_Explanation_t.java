 package org.chai.kevin.dashboard;
 
 import org.hisp.dhis.organisationunit.OrganisationUnit;
 import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
 import org.hisp.dhis.period.Period;
 
 public abstract class Explanation {
 
 	// if this is null, it means no expression is defined for the specified objective and organisation
	// or we are on a target and no percentage is defined
 	private DashboardPercentage percentage;
 	private OrganisationUnitLevel level;
 	
 	public Explanation(DashboardPercentage percentage, OrganisationUnitLevel level) {
 		this.percentage = percentage;
 		this.level = level;
 	}
 
 	public abstract boolean isTarget();
 
 	public DashboardPercentage getAverage() {
 		return percentage;
 	}
 	
 	public DashboardEntry getEntry() {
 		return percentage.getEntry();
 	}
 	
 	public Period getPeriod() {
 		return percentage.getPeriod();
 	}
 	
 	public OrganisationUnit getOrganisation() {
 		return percentage.getOrganisationUnit();
 	}
 	
 	public OrganisationUnitLevel getLevel() {
 		return level;
 	}
 	
 	public boolean isLeaf() {
 		return percentage.getOrganisationUnit().getChildren().size() == 0;
 	}
 	
 }
