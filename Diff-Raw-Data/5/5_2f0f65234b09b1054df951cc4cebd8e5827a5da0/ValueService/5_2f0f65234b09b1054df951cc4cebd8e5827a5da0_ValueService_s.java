 package org.chai.kevin.value;
 
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
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.Entity;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.LanguageService;
 import org.chai.kevin.Period;
 import org.chai.kevin.data.Calculation;
 import org.chai.kevin.data.Data;
 import org.chai.kevin.data.DataElement;
 import org.chai.kevin.data.NormalizedDataElement;
 import org.chai.kevin.location.CalculationLocation;
 import org.chai.kevin.location.DataLocation;
 import org.chai.kevin.location.DataLocationType;
 import org.chai.kevin.util.Utils;
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Conjunction;
 import org.hibernate.criterion.Disjunction;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.transaction.annotation.Transactional;
 
 public class ValueService {
 
 	private static final Log log = LogFactory.getLog(ValueService.class);
 	
 	private SessionFactory sessionFactory;
 	private LanguageService languageService;
 	
 	@Transactional(readOnly=false)
 	public <T extends StoredValue> T save(T value) {
 		log.debug("save(value="+value+")");
 		
 		value.setTimestamp(new Date());
 		sessionFactory.getCurrentSession().saveOrUpdate(value);
 		
 		setLastValueChanged(value.getData());
 		return value;
 	}
 	
 	@Transactional(readOnly=true)
 	public <T extends DataValue> T getDataElementValue(DataElement<T> data, DataLocation dataLocation, Period period) {
 		if (log.isDebugEnabled()) log.debug("getDataElementValue(data="+data+", period="+period+", dataLocation="+dataLocation+")");
 		List<T> values = listDataElementValues(data, dataLocation, period, new HashMap<String, Object>());
 		T result = null;
 		if (values.size() > 0) result = values.get(0);
 		if (log.isDebugEnabled()) log.debug("getDataElementValue(...)="+result);
 		
 		return result;
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Transactional(readOnly=true)
 	public <T extends DataValue> List<T> searchDataElementValues(String text, DataElement<T> data, DataLocation dataLocation, Period period, Map<String, Object> params) {
 		if (log.isDebugEnabled()) log.debug("searchDataElementValues(text="+text+", data="+data+", period="+period+", dataLocation="+dataLocation+")");
 		Criteria criteria = getCriteria(data, dataLocation, period);
 		addSortAndLimitCriteria(criteria, params);
 		addSearchCriteria(criteria, text);
 		
 		List<T> result = criteria.list();
 		filterList(result, text);
 		
 		if (log.isDebugEnabled()) log.debug("searchDataElementValues(...)=");
 		return result;
 	}
 	
 	private <T extends DataValue> void filterList(List<T> list, String text) {
 		for (String chunk : StringUtils.split(text)) {
 			for (DataValue element : new ArrayList<T>(list)) {
				if (!Utils.matches(chunk, element.getLocation().getNames().get(languageService.getCurrentLanguage()))) list.remove(element);
				if (!Utils.matches(chunk, element.getLocation().getCode())) list.remove(element);
 			}
 		}
 	}
 	
 	private void addSearchCriteria(Criteria criteria, String text) {
 		Conjunction textRestrictions = Restrictions.conjunction();
 		for (String chunk : StringUtils.split(text)) {
 			Disjunction disjunction = Restrictions.disjunction();		
 			disjunction.add(Restrictions.ilike("location.code", chunk, MatchMode.ANYWHERE));
 			disjunction.add(Restrictions.ilike("location.names.jsonText", chunk, MatchMode.ANYWHERE));
 			textRestrictions.add(disjunction);
 		}
 		criteria.add(textRestrictions);
 	}
 	
 	private void addSortAndLimitCriteria(Criteria criteria, Map<String, Object> params) {
 		if (params.containsKey("sort")) {
 			criteria.addOrder(params.get("order").equals("asc")?Order.asc(params.get("sort")+""):Order.desc(params.get("sort")+""));
 		}
 		else {
 			criteria.addOrder(Order.asc("location.code"));
 		}
 		
 		if (params.get("offset") != null) criteria.setFirstResult((Integer)params.get("offset"));
 		if (params.get("max") != null) criteria.setMaxResults((Integer)params.get("max"));
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Transactional(readOnly=true)
 	public <T extends DataValue> List<T> listDataElementValues(DataElement<T> data, DataLocation dataLocation, Period period, Map<String, Object> params) {
 		if (log.isDebugEnabled()) log.debug("listDataElementValues(data="+data+", period="+period+", dataLocation="+dataLocation+")");
 		Criteria criteria = getCriteria(data, dataLocation, period);
 		addSortAndLimitCriteria(criteria, params);
 		
 		List<T> result = criteria.list();
 		if (log.isDebugEnabled()) log.debug("listDataElementValues(...)=");
 		return result;
 	}
 	
 	public <T extends DataValue> Long countDataElementValues(String text, DataElement<T> data, DataLocation dataLocation, Period period) {
 		if (log.isDebugEnabled()) log.debug("listDataElementValues(data="+data+", period="+period+", dataLocation="+dataLocation+")");
 		Criteria criteria = getCriteria(data, dataLocation, period);
 		if (text != null) addSearchCriteria(criteria, text);
 		
 		Long result = (Long)criteria.setProjection(Projections.count("id")).uniqueResult();
 		if (log.isDebugEnabled()) log.debug("listDataElementValues(...)=");
 		return result;
 	}
 	
 	private <T extends DataValue> Criteria getCriteria(DataElement<T> data, DataLocation dataLocation, Period period) {
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(data.getValueClass());
 		criteria.add(Restrictions.eq("data", data));
 		if (period != null) criteria.add(Restrictions.eq("period", period));
 		if (dataLocation != null) criteria.add(Restrictions.eq("location", dataLocation));
 		criteria.createAlias("location", "location");
 		return criteria;
 	}
 	
 	@Transactional(readOnly=true)
 	public <T extends CalculationPartialValue> CalculationValue<T> getCalculationValue(Calculation<T> calculation, CalculationLocation location, Period period, Set<DataLocationType> types) {
 		if (log.isDebugEnabled()) log.debug("getCalculationValue(calculation="+calculation+", period="+period+", location="+location+", types="+types+")");
 		List<T> partialValues = getPartialValues(calculation, location, period, types);
 		CalculationValue<T> result = calculation.getCalculationValue(partialValues, period, location);
 		if (log.isDebugEnabled()) log.debug("getCalculationValue(...)="+result);
 		return result;
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Transactional(readOnly=true)
 	public <T extends CalculationPartialValue> List<T> getPartialValues(Calculation<T> calculation, CalculationLocation location, Period period) {
 		return (List<T>)sessionFactory.getCurrentSession().createCriteria(calculation.getValueClass())
 		.add(Restrictions.eq("period", period))
 		.add(Restrictions.eq("location", location))
 		.add(Restrictions.eq("data", calculation)).list();
 	}
 	
 	@SuppressWarnings("unchecked")
 	private <T extends CalculationPartialValue> List<T> getPartialValues(Calculation<T> calculation, CalculationLocation location, Period period, Set<DataLocationType> types) {
 		return (List<T>)sessionFactory.getCurrentSession().createCriteria(calculation.getValueClass())
 		.add(Restrictions.eq("period", period))
 		.add(Restrictions.eq("location", location))
 		.add(Restrictions.eq("data", calculation))
 		.add(Restrictions.in("type", types)).list();
 	}
 	
 	@Transactional(readOnly=true)
 	public Long getNumberOfValues(Data<?> data, Period period) {
 		return (Long)sessionFactory.getCurrentSession().createCriteria(data.getValueClass())
 		.add(Restrictions.eq("data", data))
 		.add(Restrictions.eq("period", period))
 		.setProjection(Projections.count("id"))
 		.uniqueResult();
 	}
 	
 	// if this is set readonly, it triggers an error when deleting a
 	// data element through DataElementController.deleteEntity
 	@Transactional(readOnly=true)
 	public Long getNumberOfValues(Data<?> data) {
 		return (Long)sessionFactory.getCurrentSession().createCriteria(data.getValueClass())
 		.add(Restrictions.eq("data", data))
 		.setProjection(Projections.count("id"))
 		.uniqueResult();
 	}
 	
 	@Transactional(readOnly=true)
 	public Long getNumberOfValues(Data<?> data, Status status, Period period) {
 		// TODO allow Calculation here
 		if (!(data instanceof NormalizedDataElement)) {
 			throw new IllegalArgumentException("wrong data type");
 		}
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(data.getValueClass())
 			.add(Restrictions.eq("data", data))
 			.add(Restrictions.eq("status", status));
 		if (period != null) criteria.add(Restrictions.eq("period", period));
 		return (Long)criteria	
 			.setProjection(Projections.count("id"))
 			.uniqueResult();
 	}
 	
 	@Transactional(readOnly=false)
 	public void deleteValues(Data<?> data, CalculationLocation location, Period period) {
 		String queryString = "delete from "+data.getValueClass().getAnnotation(Entity.class).name()+" where data = :data";
 		if (location != null) queryString += " and location = :location";
 		if (period != null) queryString += " and period = :period";
 		Query query = sessionFactory.getCurrentSession()
 		.createQuery(queryString)
 		.setParameter("data", data);
 		if (location != null) query.setParameter("location", location);
 		if (period != null) query.setParameter("period", period);
 		query.executeUpdate();
 		
 		setLastValueChanged(data);
 	}
 	
 	private void setLastValueChanged(Data<?> data) {
 		data.setLastValueChanged(new Date());
 		sessionFactory.getCurrentSession().save(data);
 	}
 	
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 	
 	public void setLanguageService(LanguageService languageService) {
 		this.languageService = languageService;
 	}
 	
 }
