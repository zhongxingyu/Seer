 /*
  * jPOS Presentation Manager [http://jpospm.blogspot.com]
  * Copyright (C) 2010 Jeronimo Paoletti [jeronimo.paoletti@gmail.com]
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jpos.ee.pm.struts;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.jpos.ee.DBSupport;
 import org.jpos.ee.pm.core.Entity;
 import org.jpos.ee.pm.core.EntityFilter;
 import org.jpos.ee.pm.core.EntitySupport;
 import org.jpos.ee.pm.core.Field;
 
 public class PMListSupport {
 	public static final int DEFAULT_PAGE_SIZE = 10;
 	private DBSupport dbs;
 	private Integer total;
 	private Integer rpp;
 
 
 	public List<Object> getContentList(Entity entity, EntityFilter filter, PMList pmlist) throws ClassNotFoundException{
 		Criteria list  = createCriteria(entity,filter,pmlist);
 		Criteria count = createCriteria(entity,filter,pmlist);
 		rpp = (pmlist!=null && pmlist.getRowsPerPage()!=null)?pmlist.getRowsPerPage():DEFAULT_PAGE_SIZE;
 		Integer fr= (pmlist!=null && pmlist.getPage()!=null)?(((pmlist.getPage()-1) * pmlist.getRowsPerPage())):0;
 		
 		count.setProjection(Projections.rowCount());
 		list.setMaxResults(getRpp());
 		list.setFirstResult(fr);
 		
 		count.setMaxResults(1);
 		
 		total = (Integer) count.uniqueResult();
 		if(total==null)total = 0;
 		
 		return list.list();
 	}
 	
 	protected Criteria createCriteria(Entity entity, EntityFilter filter, PMList pmlist) throws ClassNotFoundException{
 		Criteria c  = dbs.session().createCriteria(Class.forName(entity.getClazz()));
 		
 		if(pmlist!=null){
			String order = pmlist.getOrder();
			//This is a temporary patch until i found how to sort propertys
			if(order!=null && order.contains(".")) order = order.substring(0, order.indexOf("."));
			if(order!= null && order.compareTo("")!=0){
				if(pmlist.isDesc()) 	c.addOrder(Order.desc(order));
				else			c.addOrder(Order.asc (order));
 			}
 		}
 		if(entity.getListfilter() != null) {
 			c.add(entity.getListfilter().getListCriteria());
 		}
 		
 		if(filter != null){
 			for(Criterion criterion : filter.getFilters()){
 				c.add(criterion);
 			}
 		}
 		return c;
 	}
 	
 	public Object getObject(Entity entity, String property, String value) throws ClassNotFoundException{
 		Criteria c = getDbs().session().createCriteria(Class.forName(entity.getClazz()));
 		c.setMaxResults(1);
 		c.add(Restrictions.sqlRestriction(property+"="+value));
 		return c.uniqueResult();
 	}
 	
 	public PMListSupport(DBSupport dbs) {
 		super();
 		this.dbs = dbs;
 	}
 
 	public DBSupport getDbs() {
 		return dbs;
 	}
 
 	public Integer getTotal() {
 		return total;
 	}
 	
 	public Map<String, Object> buildFilterMap(List<Field> fields, Object filter) {
 		Map<String,Object> r = new HashMap<String, Object>();
 		for(Field field : fields){
 			Object object = EntitySupport.get(filter, field.getId());
 			if(object!=null){
 				if(!(object instanceof String) || ((String)object).compareTo("")!=0){
 					r.put(field.getId(), object);
 				}
 			}
 		}
 		return r;
 	}
 
 	public void setRpp(Integer rpp) {
 		this.rpp = rpp;
 	}
 
 	public Integer getRpp() {
 		return rpp;
 	}
 }
