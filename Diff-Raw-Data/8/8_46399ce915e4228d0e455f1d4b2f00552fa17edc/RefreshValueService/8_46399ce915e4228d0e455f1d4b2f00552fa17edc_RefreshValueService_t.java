 package org.chai.kevin.value;
 
 import grails.plugin.springcache.annotations.CacheFlush;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.Period;
 import org.chai.kevin.data.Calculation;
 import org.chai.kevin.data.DataElement;
 import org.chai.kevin.data.DataService;
 import org.chai.kevin.data.NormalizedDataElement;
 import org.chai.kevin.data.RawDataElement;
 import org.chai.kevin.location.CalculationLocation;
 import org.chai.kevin.location.DataLocation;
 import org.codehaus.groovy.grails.commons.GrailsApplication;
 import org.hibernate.CacheMode;
 import org.hibernate.FlushMode;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 public class RefreshValueService {
 
 	private final static Log log = LogFactory.getLog(RefreshValueService.class);
 	
 	private DataService dataService;
 	private SessionFactory sessionFactory;
 	private ExpressionService expressionService;
 	private ValueService valueService;
 	private GrailsApplication grailsApplication;
 	
 	@Transactional(readOnly = false)
 	public void refreshNormalizedDataElements() {
 		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 		sessionFactory.getCurrentSession().setCacheMode(CacheMode.IGNORE);
 		
 		List<NormalizedDataElement> normalizedDataElements = sessionFactory.getCurrentSession().createCriteria(NormalizedDataElement.class).list();
 		while (!normalizedDataElements.isEmpty()) {
 			NormalizedDataElement normalizedDataElement = normalizedDataElements.get(0);
 			List<NormalizedDataElement> uptodateElements = getMe().refreshNormalizedDataElementInTransaction(normalizedDataElement);
 			normalizedDataElements.removeAll(uptodateElements);
 			sessionFactory.getCurrentSession().clear();
 		}
 	}
 	
 	@Transactional(readOnly = false, propagation=Propagation.REQUIRES_NEW)
 	public List<NormalizedDataElement> refreshNormalizedDataElementInTransaction(NormalizedDataElement normalizedDataElement) {
 		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 		sessionFactory.getCurrentSession().setCacheMode(CacheMode.IGNORE);
 		
 		List<NormalizedDataElement> uptodateElements = new ArrayList<NormalizedDataElement>();
 		refreshDataElement(normalizedDataElement, uptodateElements);
 		return uptodateElements;
 	}
 	
 	@Transactional(readOnly = false)
 	public List<NormalizedDataElement> refreshNormalizedDataElement(NormalizedDataElement normalizedDataElement) {
 		List<NormalizedDataElement> uptodateElements = new ArrayList<NormalizedDataElement>();
 		refreshDataElement(normalizedDataElement, uptodateElements);
 		return uptodateElements;
 	}
 	
 	private Date refreshDataElement(DataElement dataElement, List<NormalizedDataElement> uptodateElements) {
 		if (dataElement instanceof RawDataElement) return dataElement.getLastValueChanged();
 		else {
 			NormalizedDataElement normalizedDataElement = (NormalizedDataElement)dataElement;
 			Date latestDependency = null;
 			
 			List<DataElement> dependencies = new ArrayList<DataElement>();
 			collectOrderedDependencies(normalizedDataElement, dependencies);
 			Collections.reverse(dependencies);
 			
 			uptodateElements.add(normalizedDataElement);
 			for (DataElement dependency : dependencies) {
 				if (!uptodateElements.contains(dependency)) {
 					Date dependencyDate = refreshDataElement(dependency, uptodateElements);
					if (latestDependency == null || (dependencyDate != null && dependencyDate.after(latestDependency))) latestDependency = dependencyDate;
 				}
 			}
 			
 			// we refresh if the data element was changed after the last refresh
 			if (normalizedDataElement.getRefreshed() == null || normalizedDataElement.getTimestamp().after(normalizedDataElement.getRefreshed())) {
 				refreshNormalizedDataElementOnly(normalizedDataElement);
 			}
 			
 			// we refresh if the last value of the dependency is after the last refreshed date of this element
 			// this means some values of the dependency were changed after us
 			else if (latestDependency != null && latestDependency.after(normalizedDataElement.getRefreshed())) {
 				refreshNormalizedDataElementOnly(normalizedDataElement);
 			}
 			
 			return normalizedDataElement.getLastValueChanged();
 		}
 	}
 	
 	@Transactional(readOnly = false)
 	public void refreshNormalizedDataElement(NormalizedDataElement dataElement, DataLocation dataLocation, Period period) {
 		refreshDataElement(dataElement, dataLocation, period, new ArrayList<NormalizedDataElement>());
 	}
 	
 	private Date refreshDataElement(DataElement dataElement, DataLocation dataLocation, Period period, List<NormalizedDataElement> uptodateElements) {
 		DataValue storedValue = valueService.getDataElementValue(dataElement, dataLocation, period);
 		
 		if (dataElement instanceof RawDataElement) {
 			if (storedValue == null) return dataElement.getTimestamp();
 			return storedValue.getTimestamp();
 		}
 		else {
 			NormalizedDataElement normalizedDataElement = (NormalizedDataElement)dataElement;
 			Date latestDependency = null;
 			
 			List<DataElement> dependencies = new ArrayList<DataElement>();
 			collectOrderedDependencies(normalizedDataElement, dependencies);
 			Collections.reverse(dependencies);
 			
 			uptodateElements.add(normalizedDataElement);
 			for (DataElement dependency : dependencies) {
 				if (!uptodateElements.contains(dependency)) {
 					Date dependencyDate = refreshDataElement(dependency, dataLocation, period, uptodateElements);
					if (latestDependency == null || (dependencyDate != null && dependencyDate.after(latestDependency))) latestDependency = dependencyDate;
 				}
 			}
 			
 			// we refresh if the value does not exist
 			if (storedValue == null) {
 				storedValue = refreshNormalizedDataElementOnly(normalizedDataElement, dataLocation, period);
 			}
 			
 			// we refresh if the timestamp of the dataElement is bigger than that of the value
 			else if (dataElement.getTimestamp().after(storedValue.getTimestamp())) {
 				storedValue = refreshNormalizedDataElementOnly(normalizedDataElement, dataLocation, period);
 			}
 			
 			// we refresh if the timestamp of the dependency is after the timestamp of this value
 			else if (latestDependency != null && latestDependency.after(storedValue.getTimestamp())) {
 				storedValue = refreshNormalizedDataElementOnly(normalizedDataElement, dataLocation, period);
 			}
 			
 			return storedValue.getTimestamp();
 		}
 	}
 	
 	private NormalizedDataElementValue refreshNormalizedDataElementOnly(NormalizedDataElement normalizedDataElement, DataLocation dataLocation, Period period) {
 		valueService.deleteValues(normalizedDataElement, dataLocation, period);
 		return valueService.save(expressionService.calculateValue((NormalizedDataElement)normalizedDataElement, dataLocation, period));
 	}
 	
 	private void refreshNormalizedDataElementOnly(NormalizedDataElement normalizedDataElement) {
 		if (log.isDebugEnabled()) log.debug("refreshNormalizedDataElement(normalizedDataElement="+normalizedDataElement+")");
 		
 		valueService.deleteValues(normalizedDataElement, null, null);
 		for (Iterator<Object[]> iterator = getCombinations(DataLocation.class); iterator.hasNext();) {
 			Object[] row = (Object[]) iterator.next();
 			DataLocation dataLocation = (DataLocation)row[0];
 			Period period = (Period)row[1];
 			NormalizedDataElementValue value = expressionService.calculateValue(normalizedDataElement, dataLocation, period);
 			valueService.save(value);
 		}
 		normalizedDataElement.setRefreshed(new Date());
 		dataService.save(normalizedDataElement);
 	}
 	
 	private void collectOrderedDependencies(DataElement dataElement, List<DataElement> dependencies) {
 		dependencies.add(dataElement);
 		if (dataElement instanceof NormalizedDataElement) {
 			NormalizedDataElement normalizedDataElement = (NormalizedDataElement)dataElement;
 			for (String expression : normalizedDataElement.getExpressions()) {
 				Map<String, DataElement> dependenciesMap = expressionService.getDataInExpression(expression, DataElement.class);
 				for (DataElement dependency : dependenciesMap.values()) {
 					if (dependency != null && !dependencies.contains(dependency)) collectOrderedDependencies(dependency, dependencies);
 				}
 			}
 		}
 	}
 	
 	@Transactional(readOnly = false)
 	public void refreshCalculations() {
 		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 		sessionFactory.getCurrentSession().setCacheMode(CacheMode.IGNORE);
 		
 		// TODO get only those who need to be refreshed
 		List<Calculation<?>> calculations = sessionFactory.getCurrentSession().createCriteria(Calculation.class).list();
 		
 		for (Calculation<?> calculation : calculations) {
 			getMe().refreshCalculationInTransaction(calculation);
 			sessionFactory.getCurrentSession().clear();
 		}
 	}
 	
 	@Transactional(readOnly = false, propagation=Propagation.REQUIRES_NEW)
 	public void refreshCalculationInTransaction(Calculation<?> calculation) {
 		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 		sessionFactory.getCurrentSession().setCacheMode(CacheMode.IGNORE);
 		
 		refreshCalculation(calculation);
 	}
 	
 	@Transactional(readOnly = false)
 	public void refreshCalculation(Calculation<?> calculation) {
 		Map<String, NormalizedDataElement> dependenciesMap = expressionService.getDataInExpression(calculation.getExpression(), NormalizedDataElement.class);
 		
 		Date latestDependency = null;
 		for (NormalizedDataElement dependency : dependenciesMap.values()) {
 			Date dependencyDate = refreshDataElement(dependency, new ArrayList<NormalizedDataElement>());
			if (latestDependency == null || (dependencyDate != null && dependencyDate.after(latestDependency))) latestDependency = dependencyDate;
 		}
 		
 		// we refresh if the data element was changed after the last refresh
 		if (calculation.getRefreshed() == null || calculation.getTimestamp().after(calculation.getRefreshed())) {
 			refreshCalculationOnly(calculation);
 		}
 		
 		// we refresh if the last value of the dependency is after the last refreshed date of this element
 		// this means some values of the dependency were changed after us
 		else if (latestDependency != null && latestDependency.after(calculation.getRefreshed())) {
 			refreshCalculationOnly(calculation);
 		}
 	}
 	
 	private void refreshCalculationOnly(Calculation<?> calculation) {
 		if (log.isDebugEnabled()) log.debug("refreshCalculation(calculation="+calculation+")");
 		valueService.deleteValues(calculation, null, null);
 		for (Iterator<Object[]> iterator = getCombinations(CalculationLocation.class); iterator.hasNext();) {
 			Object[] row = (Object[]) iterator.next();
 			CalculationLocation location = (CalculationLocation)row[0];
 			Period period = (Period)row[1];
 			
 			valueService.deleteValues(calculation, location, period);
 			for (CalculationPartialValue partialValue : expressionService.calculatePartialValues(calculation, location, period)) {
 				valueService.save(partialValue);
 			}
 		}
 		calculation.setRefreshed(new Date());
 		dataService.save(calculation);
 	}
 
 	@CacheFlush(caches={"dsrCache", "dashboardCache", "fctCache"})
 	public void flushCaches() { }
 	
 	private <T extends CalculationLocation> Iterator<Object[]> getCombinations(Class<T> clazz) {
 		Query query = sessionFactory.getCurrentSession().createQuery(
 				"select location, period " +
 				"from "+clazz.getSimpleName()+" location, Period period"
 		).setCacheable(true).setReadOnly(true);
 		return query.iterate();
 	}
 	
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 	
 	public void setExpressionService(ExpressionService expressionService) {
 		this.expressionService = expressionService;
 	}
 	
 	public void setValueService(ValueService valueService) {
 		this.valueService = valueService;
 	}
 	
 	public void setDataService(DataService dataService) {
 		this.dataService = dataService;
 	}
 	
 	public GrailsApplication getGrailsApplication() {
 		return grailsApplication;
 	}
 	
 	public void setGrailsApplication(GrailsApplication grailsApplication) {
 		this.grailsApplication = grailsApplication;
 	}
 	
 	public RefreshValueService getMe() {
 		return grailsApplication.getMainContext().getBean(RefreshValueService.class);
 	}
 	
 }
