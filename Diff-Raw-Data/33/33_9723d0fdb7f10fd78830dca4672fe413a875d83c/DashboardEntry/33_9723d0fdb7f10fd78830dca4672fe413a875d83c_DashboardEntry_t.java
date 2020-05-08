 package org.chai.kevin.dashboard;
 
 import java.util.NoSuchElementException;
 
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.chai.kevin.Translatable;
 import org.chai.kevin.Organisation;
 import org.hibernate.annotations.Cascade;
 import org.hisp.dhis.period.Period;
 
 @Entity(name="DashboardEntry")
 @Table(name="dhsst_dashboard_entry")
 @Inheritance(strategy=InheritanceType.JOINED)
 public abstract class DashboardEntry extends Translatable {
 
 	private Integer id;
 	private DashboardObjectiveEntry parent;
 	private Boolean root = false;
 	
 	@Id
 	@GeneratedValue
 	public Integer getId() {
 		return id;
 	}
 	public void setId(Integer id) {
 		this.id = id;
 	}
 	
	@OneToOne(targetEntity=DashboardObjectiveEntry.class, mappedBy="entry")
	@Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
 	public DashboardObjectiveEntry getParent() {
 		return parent;
 	}
 	public void setParent(DashboardObjectiveEntry parent) {
 		this.parent = parent;
 	}
 	
 	@Basic
 	public Boolean getRoot() {
 		return root;
 	}
 	public void setRoot(Boolean root) {
 		this.root = root;
 	}
 	
 	@Transient
 	public abstract boolean hasChildren();
 	@Transient
 	public abstract boolean isTarget();
 	
 	public abstract DashboardPercentage getValue(PercentageCalculator calculator, Organisation organisation, Period period);
 	public abstract Explanation getExplanation(ExplanationCalculator calculator, Organisation organisation, Period period) throws NoSuchElementException;
 
 }
