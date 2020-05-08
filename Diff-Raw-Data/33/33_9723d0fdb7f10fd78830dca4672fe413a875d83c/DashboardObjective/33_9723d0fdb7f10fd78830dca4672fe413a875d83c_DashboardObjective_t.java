 package org.chai.kevin.dashboard;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeMap;
 
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.OrderColumn;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.chai.kevin.Translatable;
 import org.chai.kevin.Organisation;
 import org.chai.kevin.dashboard.Explanation;
 import org.chai.kevin.dashboard.ExplanationCalculator;
 import org.chai.kevin.dashboard.PercentageCalculator;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.Sort;
 import org.hibernate.annotations.SortType;
 import org.hisp.dhis.organisationunit.OrganisationUnit;
 import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
 import org.hisp.dhis.period.Period;
 
 @Entity(name="StrategicObjective")
 @Table(name="dhsst_dashboard_objective")
 @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
 public class DashboardObjective extends DashboardEntry {
 
 	private List<DashboardObjectiveEntry> objectiveEntries = new ArrayList<DashboardObjectiveEntry>();
 	
 	@OneToMany(mappedBy="parent", targetEntity=DashboardObjectiveEntry.class)
 	@OrderBy(value="order")
 	public List<DashboardObjectiveEntry> getObjectiveEntries() {
 		return objectiveEntries;
 	}
 	public void setObjectiveEntries(List<DashboardObjectiveEntry> objectiveEntries) {
 		this.objectiveEntries = objectiveEntries;
 	}
 	
 	public void addObjectiveEntry(DashboardObjectiveEntry objectiveEntry) {
 		objectiveEntry.setParent(this);
 		objectiveEntry.getEntry().setParent(objectiveEntry);
 		objectiveEntries.add(objectiveEntry);
 		Collections.sort(objectiveEntries);
 	}
 	
 	@Override
 	public DashboardPercentage getValue(PercentageCalculator calculator, Organisation organisation, Period period) {
 		return calculator.getPercentage(this, organisation, period);
 	}
 	
 	@Override
 	public Explanation getExplanation(ExplanationCalculator calculator, Organisation organisation, Period period) {
 		return calculator.explain(this, organisation, period);
 	}
 	
 	@Override
 	public boolean hasChildren() {
 		return objectiveEntries.size() > 0;
 	}
 	@Override
 	@Transient
 	public boolean isTarget() {
 		return false;
 	}
 	
 }
