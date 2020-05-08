 package org.chai.kevin.dashboard;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.MapKey;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.chai.kevin.Organisation;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.Cascade;
 import org.hisp.dhis.period.Period;
 
 @Entity(name="StrategicTarget")
 @Table(name="dhsst_dashboard_target")
 @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
 public class DashboardTarget extends DashboardEntry {
 
 	private Map<String, DashboardCalculation> calculations;
 	
 	public DashboardTarget() {
 		this.calculations = new HashMap<String, DashboardCalculation>();
 	}
 	
	@OneToMany(cascade={CascadeType.ALL}, targetEntity=DashboardCalculation.class)
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
 	@MapKey(name="groupUuid")
 	@JoinColumn
 	public Map<String, DashboardCalculation> getCalculations() {
 		return calculations;
 	}
 	public void setCalculations(Map<String, DashboardCalculation> calculations) {
 		this.calculations = calculations;
 	}
 	
 	@Override
 	public Explanation getExplanation(ExplanationCalculator calculator, Organisation organisation, Period period) {
 		return calculator.explain(this, organisation, period);
 	}	
 
 	@Override
 	public DashboardPercentage getValue(PercentageCalculator calculator, Organisation organisation, Period period) {
 		return calculator.getPercentage(this, organisation, period);
 	}
 	
 	@Override
 	public boolean hasChildren() {
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		return "StrategicTarget [getName()=" + getName() + ", calculations=" + calculations + "]";
 	}
 
 	@Override
 	@Transient
 	public boolean isTarget() {
 		return true;
 	}
 
 }
