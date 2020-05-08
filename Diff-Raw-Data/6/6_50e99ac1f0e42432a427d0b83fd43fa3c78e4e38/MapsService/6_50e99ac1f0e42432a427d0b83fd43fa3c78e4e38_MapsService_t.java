 package org.chai.kevin.maps;
 
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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.Info;
 import org.chai.kevin.InfoService;
 import org.chai.kevin.Organisation;
 import org.chai.kevin.OrganisationService;
 import org.chai.kevin.ValueService;
 import org.chai.kevin.maps.MapsTarget.MapsTargetType;
 import org.chai.kevin.value.CalculationValue;
 import org.chai.kevin.value.ExpressionValue;
 import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
 import org.hisp.dhis.period.Period;
 import org.springframework.transaction.annotation.Transactional;
 
 @Transactional(readOnly=true)
 public class MapsService {
 
 	private static final Log log = LogFactory.getLog(MapsService.class);
 	
 	private OrganisationService organisationService;
 	private ValueService valueService;
 	private InfoService infoService;
 	
 	public Maps getMap(Period period, Organisation organisation, Integer level, MapsTarget target) {
 		if (log.isDebugEnabled()) log.debug("getMap(period="+period+",organisation="+organisation+",target="+target+")");
 
 		List<Polygon> polygons = new ArrayList<Polygon>();
 		organisationService.loadParent(organisation);
 		organisationService.loadLevel(organisation);
 		
 		List<OrganisationUnitLevel> levels = organisationService.getAllLevels();
 		levels.remove(0);
 		
 		if (levels.isEmpty()) {
 			// TODO throw exception
 		}
 		
 		if (level == null) {
 			List<OrganisationUnitLevel> childLevels = organisationService.getChildren(organisationService.loadLevel(organisation));
 			if (levels.size() > 0) level = childLevels.get(0).getLevel();
 			else level = levels.get(0).getLevel();
 		}
 		
 		// if we ask for an organisation level bigger than the organisation's, we go back to the right level
 		while (level <= organisation.getLevel()) {
 			organisation = organisation.getParent();
 			organisationService.loadParent(organisation);
 		}
 		
 		if (target == null) return new Maps(period, target, organisation, level, polygons, levels);
 		
 		for (Organisation child : organisationService.getChildrenOfLevel(organisation, level)) {
 			organisationService.loadLevel(child);
 			
 			Double value = null;
 			if (target.getType() == MapsTargetType.AGGREGATION) {
 				if (log.isDebugEnabled()) log.debug("getting values for AGGREGATION map with expression: "+target.getExpression());
 				ExpressionValue expressionValue = valueService.getValue(target.getExpression(), child.getOrganisationUnit(), period);
				if (expressionValue != null) {
					if (!expressionValue.getValue().isNull()) {
						value = expressionValue.getValue().getNumberValue().doubleValue();
					}
				}
 
 				if (value != null) {
 					if (target.getMaxValue() != null) {
 						value = value / target.getMaxValue(); 
 					}
 				}
 			}
 			else if (target.getType() == MapsTargetType.AVERAGE) {
 				if (log.isDebugEnabled()) log.debug("getting values for AVERAGE map with calculation: "+target.getCalculation());
 				CalculationValue calculationValue = valueService.getValue(target.getCalculation(), child.getOrganisationUnit(), period);
 				if (calculationValue != null) {
 					if (!calculationValue.getValue().isNull()) {
 						value = calculationValue.getValue().getNumberValue().doubleValue();
 					}
 				}
 			}
 			
 			
 			polygons.add(new Polygon(child, value));
 		}
 
 		return new Maps(period, target, organisation, level, polygons, levels);
 	}
 
 	public MapsExplanation getExplanation(Period period, Organisation organisation, MapsTarget target) {
 		Info info = null;
 		if (target.getType() == MapsTargetType.AGGREGATION) {
 			info = infoService.getInfo(target.getExpression(), organisation, period, target.getMaxValue()); 
 		}
 		else if (target.getType() == MapsTargetType.AVERAGE) {
 			info = infoService.getInfo(target.getCalculation(), organisation, period);
 		}
 		
 		return new MapsExplanation(organisation, target, period, info);
 	}
 	
 	public void setValueService(ValueService valueService) {
 		this.valueService = valueService;
 	}
 	
 	public void setOrganisationService(OrganisationService organisationService) {
 		this.organisationService = organisationService;
 	}
 	
 	public void setInfoService(InfoService infoService) {
 		this.infoService = infoService;
 	}
 	
 }
