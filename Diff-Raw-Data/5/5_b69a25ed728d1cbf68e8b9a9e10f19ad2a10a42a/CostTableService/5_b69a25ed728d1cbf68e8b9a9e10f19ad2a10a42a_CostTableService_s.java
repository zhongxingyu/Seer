 package org.chai.kevin.cost;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.ExpressionService;
 import org.chai.kevin.GroupCollection;
 import org.chai.kevin.Organisation;
 import org.chai.kevin.OrganisationService;
 import org.hisp.dhis.common.AbstractNameableObject;
 import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
 import org.hisp.dhis.period.Period;
 
 public class CostTableService {
 
 	private final static Log log = LogFactory.getLog(CostTableService.class);
 	
 	private CostService costService;
 	private OrganisationService organisationService;
 	private ExpressionService expressionService;
 	private String organisationLevel;
 	
 	public CostTable getCostTable(Period period, CostObjective objective, Organisation organisation) {
 		if (objective == null) return new CostTable(period, costService.getYears(), organisation);
 		List<CostTarget> targets = objective.getTargets();
 		
 		return new CostTable(period, objective, targets, costService.getYears(), organisation, getValues(period, targets, organisation));
 	}
 
 	public Explanation getExplanation(Period period, CostTarget target, Organisation organisation) {
 		GroupCollection collection = new GroupCollection(organisationService.getGroupsForExpression());
 		
 		Map<Organisation, Map<Integer, Cost>> explanationMap = new HashMap<Organisation, Map<Integer,Cost>>();
 		organisationService.loadChildren(organisation);
 		
 		for (Organisation child : organisation.getChildren()) {
 			explanationMap.put(child, getCost(target, child, period, collection));
 		}
 		return new Explanation(target, target.getParent(), period, organisation.getChildren(), costService.getYears(), explanationMap);
 	}
 	
 	private boolean appliesToOrganisation(CostTarget target, Organisation organisation, GroupCollection collection) {
 		Set<OrganisationUnitGroup> groups = organisation.getOrganisationUnit().getGroups();
 		for (String groupUuid : CostService.getGroupUuids(target.getGroupUuidString())) {
 			if (groups.contains(collection.getGroupByUuid(groupUuid))) return true;
 		}
 		return false;
 	}
 	
 	private Map<Integer, Cost> getCost(CostTarget target, Organisation organisation, Period period, GroupCollection collection) {
 		organisationService.loadChildren(organisation);
 		
 		if (organisationService.getLevel(organisation) == new Integer(organisationLevel).intValue()) {
 			return getCostForLeafOrganisation(target, organisation, period, collection);
 		}
 		else {
 			Map<Integer, Cost> result = new HashMap<Integer, Cost>();
 
 			for (Integer year : costService.getYears()) {
 				result.put(year, new Cost(target, year, period, organisation));
 			}
 			for (Organisation child : organisation.getChildren()) {
 				Map<Integer, Cost> costs = getCost(target, child, period, collection);
 				
 				for (Integer year : costService.getYears()) {
 					if (costs.containsKey(year)) {
 						result.get(year).addValue(costs.get(year).getValue());
 						if (costs.get(year).isHasMissingValue()) result.get(year).hasMissingValue();
 					}
 				}
 			}
 			return result;
 		}
 	}
 	
 	private Map<Integer, Cost> getCostForLeafOrganisation(CostTarget target, Organisation organisation, Period period, GroupCollection collection) {
 		Map<Integer, Cost> result = new HashMap<Integer, Cost>();
 		if (appliesToOrganisation(target, organisation, collection)) {
 			boolean hasMissingValues = false;
 
 			log.debug("target "+target+" applies to organisation "+organisation);
 			List<Integer> years = costService.getYears();
 			Map<AbstractNameableObject, Object> values = new HashMap<AbstractNameableObject, Object>();
 
 			Double baseCost = (Double)expressionService.getValue(target.getExpression(), period, organisation.getOrganisationUnit(), values);
 			if (ExpressionService.hasNullValues(values.values())) hasMissingValues = true;
 			
 			Double steps = 0d;
 			if (target.isAverage()) {
 				values.clear();
 				Double endCost = (Double)expressionService.getValue(target.getExpressionEnd(), period, organisation.getOrganisationUnit(), values);
 				if (ExpressionService.hasNullValues(values.values())) hasMissingValues = true;
 				steps = (endCost - baseCost)/(years.size()-1);
 			}
 
 			for (Integer year : years) {
 				Double yearCost;
 				if (target.isAverage()) {
 					yearCost = ( baseCost + steps * (year-1) ) * target.getCostRampUp().getYears().get(year).getValue();
 				}
 				else {
 					yearCost = baseCost * target.getCostRampUp().getYears().get(year).getValue();
 				}
 				// TODO add inflation
 				Cost cost = new Cost(yearCost, target, year, period, organisation, hasMissingValues);
 				result.put(year, cost);
 			}
 		}
 		else {
 			log.debug("skipping organisation: "+organisation);
 		}
 		return result;
 	}
 	
 	private Map<CostTarget, Map<Integer, Cost>> getValues(Period period, List<CostTarget> targets, Organisation organisation) {
 		Map<CostTarget, Map<Integer, Cost>> result = new HashMap<CostTarget, Map<Integer,Cost>>();
 		
 		GroupCollection collection = new GroupCollection(organisationService.getGroupsForExpression());
 		for (CostTarget target : targets) {
 			result.put(target, getCost(target, organisation, period, collection));
 		}
 		return result;
 	}
 	
 	public String getOrganisationLevel() {
 		return organisationLevel;
 	}
 	
 	public void setCostService(CostService costService) {
 		this.costService = costService;
 	}
 	
 	public void setExpressionService(ExpressionService expressionService) {
 		this.expressionService = expressionService;
 	}
 	
 	public void setOrganisationService(OrganisationService organisationService) {
 		this.organisationService = organisationService;
 	}
 	
 	public void setOrganisationLevel(String organisationLevel) {
 		this.organisationLevel = organisationLevel;
 	}
 	
 }
