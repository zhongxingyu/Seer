 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.internal.core.planner;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.swordfish.core.Interceptor;
 import org.eclipse.swordfish.core.planner.strategy.FilterStrategy;
 import org.eclipse.swordfish.core.planner.strategy.Hint;
 import org.eclipse.swordfish.core.util.ReadOnlyRegistry;
 
 /**
  * @author dwolz
  *
  */
 public class FilterStrategyImpl implements FilterStrategy {
 
 	private static final Log LOG = LogFactory.getLog(PlannerImpl.class);
 
 	private List<FilterStrategy> filterStrategies;
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see org.eclipse.swordfish.api.FilterStrategy#filter(java.util.List,
 	 * org.eclipse.swordfish.api.ReadOnlyRegistry, java.util.List)
 	 */
 	public List<Interceptor> filter(List<Interceptor> interceptors,
 			ReadOnlyRegistry<Interceptor> registry, List<Hint<?>> hints) {
 		Set<Interceptor> filtered = new HashSet<Interceptor>();
 
 		if (filterStrategies != null && filterStrategies.size() > 0) {
 			for (FilterStrategy strategy: filterStrategies) {
 				filtered.addAll(strategy.filter(interceptors, registry, hints));
 			}
 		} else {
 			filtered.addAll(interceptors);//
 			LOG.info("No filter strategy defined");
 		}
 		for (Interceptor interceptor : interceptors) {
			if (interceptor.getProperties() != null) {
			Object type = interceptor.getProperties().get("type");
 			   if(type != null && type instanceof QName && type.equals(new QName("http://interceptor.core.internal.swordfish.eclipse.org/","CxfDecoratingInterceptor"))){
 				   filtered.add(interceptor);
 			    break;
 			   }
			}
 		}
 		return new ArrayList<Interceptor>(filtered);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see org.eclipse.swordfish.api.FilterStrategy#getPriority()
 	 */
 	public int getPriority() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public void setFilterStrategies(List<FilterStrategy> filterStrategies) {
 		this.filterStrategies = filterStrategies;
 	}
 
 }
