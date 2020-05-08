 package org.chai.kevin.dashboard;
 
 /* 
  * Copyright (c) 2011, Clinton Health Access Initiative.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the <organization> nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 import grails.plugin.springcache.annotations.Cacheable;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.LocationService;
 import org.chai.kevin.data.Info;
 import org.chai.kevin.data.InfoService;
 import org.chai.kevin.data.Type;
 import org.chai.kevin.location.CalculationEntity;
 import org.chai.kevin.location.DataLocationEntity;
 import org.chai.kevin.location.DataEntityType;
 import org.chai.kevin.location.LocationEntity;
 import org.chai.kevin.location.LocationLevel;
 import org.chai.kevin.reports.ReportObjective;
 import org.chai.kevin.reports.ReportService;
 import org.chai.kevin.value.CalculationValue;
 import org.chai.kevin.value.Value;
 import org.chai.kevin.value.ValueService;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Restrictions;
 import org.hisp.dhis.period.Period;
 import org.springframework.transaction.annotation.Transactional;
 
 public class DashboardService {
 
 //	private Log log = LogFactory.getLog(DashboardService.class);
 	
 	private ReportService reportService;
 	private InfoService infoService;
 	private ValueService valueService;
 	private LocationService locationService;
 	private SessionFactory sessionFactory;
 	private Set<String> skipLevels;
 	
 	private Set<LocationLevel> getSkipLocationLevels() {
 		Set<LocationLevel> levels = new HashSet<LocationLevel>();
 		for (String skipLevel : skipLevels) {
 			levels.add(locationService.findLocationLevelByCode(skipLevel));
 		}
 		return levels;
 	}
 	
 	@Transactional(readOnly = true)
 	@Cacheable("dashboardCache")
 	public Dashboard getDashboard(LocationEntity location, ReportObjective objective, Period period, Set<DataEntityType> types) {
 		
 		List<CalculationEntity> locations = new ArrayList<CalculationEntity>();
 		
		locations.add(location);
 		locations.addAll(location.getChildren(getSkipLocationLevels()));
 		for (DataLocationEntity dataEntity : location.getDataEntities(getSkipLocationLevels())) {
 			if (types.contains(dataEntity.getType())) locations.add(dataEntity);
 		}
 		
 		List<DashboardEntity> dashboardEntities = new ArrayList<DashboardEntity>();
 		
 		dashboardEntities.add(getDashboardObjective(objective));		
 		dashboardEntities.addAll(getDashboardEntities(objective));
 		
 		List<LocationEntity> locationPath = calculateLocationPath(location);
 		List<DashboardObjective> objectivePath = getBreadcrumb(objective);
 		return new Dashboard(locations, dashboardEntities, locationPath, objectivePath,
 				getValues(locations, dashboardEntities, period, types));
 	}
 
 	@Transactional(readOnly = true)
 	@Cacheable("dashboardCache")
 	public Dashboard getCompareDashboard(LocationEntity location, ReportObjective objective, Period period, Set<DataEntityType> groups) {		
 		List<CalculationEntity> locations = new ArrayList<CalculationEntity>();
 		locations.add(location);
 		
 		List<DashboardEntity> dashboardEntities = getDashboardEntities(objective);
 		dashboardEntities.add(getDashboardObjective(objective));
 		
 		return new Dashboard(locations, dashboardEntities, null, null,
 				getValues(locations, dashboardEntities, period, groups));
 	}
 	
 	@Transactional(readOnly = true)
 	public Info<?> getExplanation(CalculationEntity entity, DashboardEntity dashboardEntity, Period period, Set<DataEntityType> groups) {
 		return dashboardEntity.visit(new ExplanationVisitor(groups), entity, period);
 	}
 
 	private class ExplanationVisitor implements DashboardVisitor<Info> {
 
 		private Set<DataEntityType> types;
 		
 		public ExplanationVisitor(Set<DataEntityType> types) {
 			this.types = types;
 		}
 		
 		@Override
 		public Info visitObjective(DashboardObjective objective, CalculationEntity entity, Period period) {
 			DashboardPercentage percentage = objective.visit(new PercentageVisitor(types), entity, period);
 			if (percentage == null) return null;
 			List<DashboardEntity> dashboardEntities = getDashboardEntities(objective.getObjective());
 			Map<DashboardEntity, DashboardPercentage> values = getValues(dashboardEntities, period, entity, types);
 			return new DashboardObjectiveInfo(percentage, values);
 		}
 
 		@Override
 		public Info visitTarget(DashboardTarget target, CalculationEntity entity, Period period) {
 			return infoService.getCalculationInfo(target.getCalculation(), entity, period, types);
 		}
 		
 	}
 	
 	private static Type type = Type.TYPE_NUMBER();
 	
 	private class PercentageVisitor implements DashboardVisitor<DashboardPercentage> {
 		
 		private Set<DataEntityType> types;
 		
 		public PercentageVisitor(Set<DataEntityType> types) {
 			this.types = types;
 		}
 
 		private final Log log = LogFactory.getLog(PercentageVisitor.class);
 		
 		@Override
 		public DashboardPercentage visitObjective(DashboardObjective objective, CalculationEntity entity, Period period) {
 			if (log.isDebugEnabled()) log.debug("visitObjective(objective="+objective+",entity="+entity+",period="+period+")");
 			
 			Integer totalWeight = 0;
 			Double sum = 0.0d;
 
 			List<DashboardEntity> dashboardEntities = getDashboardEntities(objective.getObjective());
 			for (DashboardEntity child : dashboardEntities) {
 				DashboardPercentage childPercentage = child.visit(this, entity, period);
 				if (childPercentage == null) {
 					if (log.isErrorEnabled()) log.error("found null percentage, objective: "+child+", entity: "+entity+", period: "+period);
 					return null;
 				}
 				Integer weight = child.getWeight();
 				if (childPercentage.isValid()) {
 					sum += childPercentage.getGradientValue() * weight;
 					totalWeight += weight;
 				}
 				else {
 					// MISSING_EXPRESSION - we skip it
 					// MISSING_NUMBER - should we count it in as zero ?
 				}
 
 			}
 			// TODO what if sum = 0 and totalWeight = 0 ?
 			Double average = sum/totalWeight;
 			Value value = null;
 			if (average.isNaN() || average.isInfinite()) value = Value.NULL;
 			else value = type.getValue(average);
 			DashboardPercentage percentage = new DashboardPercentage(value, entity, period);
 			
 			if (log.isDebugEnabled()) log.debug("visitObjective()="+percentage);
 			return percentage;
 		}
 
 		@Override
 		public DashboardPercentage visitTarget(DashboardTarget target, CalculationEntity entity, Period period) {
 			if (log.isDebugEnabled()) log.debug("visitTarget(target="+target+",entity="+entity+",period="+period+")");
 			
 			CalculationValue<?> calculationValue = valueService.getCalculationValue(target.getCalculation(), entity, period, types);
 			if (calculationValue == null) return null;
 			DashboardPercentage percentage = new DashboardPercentage(calculationValue.getValue(), entity, period);
 
 			if (log.isDebugEnabled()) log.debug("visitTarget(...)="+percentage);
 			return percentage;
 		}
 	}
 	
 	private Map<CalculationEntity, Map<DashboardEntity, DashboardPercentage>> getValues(List<CalculationEntity> locations, List<DashboardEntity> objectiveEntries, Period period, Set<DataEntityType> types) {
 		Map<CalculationEntity, Map<DashboardEntity, DashboardPercentage>> values = new HashMap<CalculationEntity, Map<DashboardEntity, DashboardPercentage>>();
 
 		for (CalculationEntity entity : locations) {
 			Map<DashboardEntity, DashboardPercentage> locationMap = getValues(objectiveEntries, period, entity, types);
 			values.put(entity, locationMap);
 		}
 		return values;
 	}
 
	private Map<DashboardEntity, DashboardPercentage> getValues(List<DashboardEntity> dashboardEntities, Period period, CalculationEntity location, Set<DataEntityType> types) {
 		Map<DashboardEntity, DashboardPercentage> locationMap = new HashMap<DashboardEntity, DashboardPercentage>();
 		for (DashboardEntity dashboardEntity : dashboardEntities) {
			DashboardPercentage percentage = dashboardEntity.visit(new PercentageVisitor(types), location, period);
 			locationMap.put(dashboardEntity, percentage);
 		}
 		return locationMap;
 	}
 	
 	private List<LocationEntity> calculateLocationPath(LocationEntity entity) {
 		List<LocationEntity> locationPath = new ArrayList<LocationEntity>();
 		LocationEntity parent = entity;
 		while ((parent = parent.getParent()) != null) {
 			locationPath.add(parent);
 		}
 		Collections.reverse(locationPath);
 		return locationPath;
 	}
 	
 	private List<DashboardObjective> getBreadcrumb(ReportObjective objective) {
 		List<DashboardObjective> objectivePath = new ArrayList<DashboardObjective>();
 		while (objective != null) {
 			ReportObjective parent = objective.getParent();
 			if(parent != null) {
 				DashboardObjective dashboardParent = (DashboardObjective) getDashboardObjective(parent);
 				if(dashboardParent != null)	objectivePath.add(dashboardParent);
 			}
 			objective = parent;
 		}
 		Collections.reverse(objectivePath);
 		return objectivePath;
 	}
 	
 	public List<DashboardEntity> getDashboardEntities(ReportObjective objective) {
 		List<DashboardEntity> entities = new ArrayList<DashboardEntity>();
 		if(objective.getChildren() != null) {
 			for (ReportObjective child : objective.getChildren()) {
 				DashboardEntity dashboardEntity = getDashboardObjective(child);
 				if(dashboardEntity != null)	entities.add(dashboardEntity);
 			}
 		}
 		entities.addAll(reportService.getReportTargets(DashboardTarget.class, objective));
 		return entities;
 	}
 	
 	public DashboardEntity getDashboardObjective(ReportObjective objective) {
 		DashboardObjective dashboardObjective = (DashboardObjective) sessionFactory.getCurrentSession()
 				.createCriteria(DashboardObjective.class)
 				.add(Restrictions.eq("objective", objective))
 				.uniqueResult();
 		
 		return dashboardObjective;
 	}
 
 	public void setInfoService(InfoService infoService) {
 		this.infoService = infoService;
 	}
 	
 	public void setValueService(ValueService valueService) {
 		this.valueService = valueService;
 	}
 	
 	public void setLocationService(LocationService locationService) {
 		this.locationService = locationService;
 	}
 	
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 	
 	public void setSkipLevels(Set<String> skipLevels) {
 		this.skipLevels = skipLevels;
 	}
 	
 	public void setReportService(ReportService reportService) {
 		this.reportService = reportService;
 	}
 }
