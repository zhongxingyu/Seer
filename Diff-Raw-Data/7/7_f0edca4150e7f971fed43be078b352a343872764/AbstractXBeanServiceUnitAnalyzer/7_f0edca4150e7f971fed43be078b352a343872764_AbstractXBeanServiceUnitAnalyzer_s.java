 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.common.xbean;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jbi.messaging.MessageExchange;
 
 import org.apache.servicemix.common.Endpoint;
 import org.apache.servicemix.common.packaging.Provides;
 import org.apache.servicemix.common.packaging.ServiceUnitAnalyzer;
 import org.apache.xbean.spring.context.FileSystemXmlApplicationContext;
 
 public abstract class AbstractXBeanServiceUnitAnalyzer implements
 		ServiceUnitAnalyzer {
 
 	List consumes = new ArrayList();
 
 	List provides = new ArrayList();
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.servicemix.common.packaging.ServiceUnitAnalyzer#getConsumes()
 	 */
 	public List getConsumes() {
 		return consumes;
 	}
 
 	protected abstract List getConsumes(Endpoint endpoint);
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.servicemix.common.packaging.ServiceUnitAnalyzer#getProvides()
 	 */
 	public List getProvides() {
 		return provides;
 	}
 
 	protected List getProvides(Endpoint endpoint) {
 		List providesList = new ArrayList();
 		if (endpoint.getRole().equals(MessageExchange.Role.PROVIDER)) {
 			Provides newProvide = new Provides();
 			newProvide.setEndpointName(endpoint.getEndpoint());
 			newProvide.setInterfaceName(endpoint.getInterfaceName());
 			newProvide.setServiceName(endpoint.getService());
 			providesList.add(newProvide);
 		}
 
 		return providesList;
 	}
 
 	protected abstract String getXBeanFile();
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.servicemix.common.packaging.ServiceUnitAnalyzer#init(java.io.File)
 	 */
 	public void init(File explodedServiceUnitRoot) {
 		FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(
				explodedServiceUnitRoot.getAbsolutePath() + "/"
 						+ getXBeanFile());

 		for (int i = 0; i < context.getBeanDefinitionNames().length; i++) {
 			Object bean = context.getBean(context.getBeanDefinitionNames()[i]);
 			if (isValidEndpoint(bean)) {
 				// The provides are generic while the consumes need to
 				// be handled by the implementation
 				Endpoint endpoint = (Endpoint) bean;
 				provides.addAll(getProvides(endpoint));
 				consumes.addAll(getConsumes(endpoint));
 			}
 		}
 	}
 
 	protected abstract boolean isValidEndpoint(Object bean);
 
 }
