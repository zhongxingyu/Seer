 package org.os.javaee.orm.multitenancy.hibernate;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.hibernate.SessionFactory;
 import org.hibernate.engine.spi.FilterDefinition;
 import org.hibernate.mapping.PersistentClass;
 import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
 import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
 
 /**
  * <p>Title: MultiTenancyLocalSessionFactoryBean</p>
  * <p><b>Description:</b> MultiTenancyLocalSessionFactoryBean</p>
  * <p>Copyright: Copyright (c) 2013</p>
  * <p>Company: Open Source Development.</p>
  * @author Murali Reddy
  * @version 1.0
  */
 public class MultiTenancyLocalSessionFactoryBean extends LocalSessionFactoryBean {
 
 	private static final Logger log = Logger.getLogger(MultiTenancyLocalSessionFactoryBean.class);
 	
 	private List<FilterInfo> filterList = new ArrayList<FilterInfo>();
 	private Map<String,List<String>> filterMappings = new HashMap<String,List<String>>();
 	
 	/* (non-Javadoc)
 	 * @see org.springframework.orm.hibernate4.LocalSessionFactoryBean#buildSessionFactory(org.springframework.orm.hibernate4.LocalSessionFactoryBuilder)
 	 */
 	@Override
 	protected SessionFactory buildSessionFactory(LocalSessionFactoryBuilder sfb) {
 		Map<String,FilterInfo> filterDefinitionMap = new HashMap<String,FilterInfo>();
 		if(this.getFilterList() != null && this.getFilterList().size()>0){
 			for(FilterInfo filterInfo:this.getFilterList()){
 				filterDefinitionMap.put(filterInfo.getName(), filterInfo);
 				super.getConfiguration().addFilterDefinition(new FilterDefinition(filterInfo.getName(), filterInfo.getDefaultCondition(), filterInfo.getParametersType()));
 			}
 		}
 		
 		super.getConfiguration().buildMappings();
 		
 		for(Iterator<? extends PersistentClass> iter = this.getConfiguration().getClassMappings();iter.hasNext();){
 			PersistentClass pClass = iter.next();
 			/*if(pClass.getEntityName().equalsIgnoreCase("org.os.javaee.orm.multitenancy.entity.MultiTenancyEnabledEntity")){
 				pClass.addFilter("tenantFilter", ":tenantParam=TENANT_ID", false, null, null);
 				log.info("Entity Name -->:"+(pClass.getEntityName())+"\t And Filters -->:"+(pClass.getFilters().size()));
 			}
 			*/
 			if(getFilterMappings().containsKey(pClass.getEntityName())){
 				for(String filterName:getFilterMappings().get(pClass.getEntityName())){
 					FilterInfo filterInfo = filterDefinitionMap.get(filterName); 
 					pClass.addFilter(filterInfo.getName(), (filterInfo.getCondition() != null ? filterInfo.getCondition():filterInfo.getDefaultCondition()), false, null, null);
					log.info("Entity Name -->:"+(pClass.getEntityName())+"\t And Filters -->:"+(pClass.getFilters().size()));
 				}
 			}
 		}
 		return super.buildSessionFactory(sfb);
 
 		/**
 		 * TODO Check the How we can implement dynamic filter creation (in case of lazy entity scan/parse) at runtime.
 		 */
 	}
 
 	/**
 	 * @return the filterList
 	 */
 	public List<FilterInfo> getFilterList() {
 		return filterList;
 	}
 	/**
 	 * @param filterList the filterList to set
 	 */
 	public void setFilterList(List<FilterInfo> filterList) {
 		this.filterList = filterList;
 	}
 	/**
 	 * @return the filterMappings
 	 */
 	public Map<String, List<String>> getFilterMappings() {
 		return filterMappings;
 	}
 	/**
 	 * @param filterMappings the filterMappings to set
 	 */
 	public void setFilterMappings(Map<String, List<String>> filterMappings) {
 		this.filterMappings = filterMappings;
 	}
 }
