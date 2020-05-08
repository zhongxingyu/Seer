 /**
  * Copyright 2006 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.javacrumbs.springws.test.util;
 
 import java.util.Collection;
 
 import net.javacrumbs.springws.test.MockWebServiceMessageSender;
 
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanCreationException;
 import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 import org.springframework.ws.client.core.WebServiceTemplate;
 import org.springframework.ws.transport.WebServiceMessageSender;
 
 /**
  * Injects mock message senders into {@link WebServiceTemplate}.
  * @author Lukas Krecan
  *
  */
 public class MockMessageSenderInjector implements BeanFactoryPostProcessor {
 
 	@SuppressWarnings("unchecked")
 	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
 		Collection<WebServiceTemplate> templates = beanFactory.getBeansOfType(WebServiceTemplate.class).values();
 		if (templates.size()==0)
 		{
 			throw new BeanCreationException("No WebServiceTemplate found in the servlet context.");
 		}
		WebServiceTemplate template = templates.iterator().next();
 		
 		Collection<MockWebServiceMessageSender> mockSenders = beanFactory.getBeansOfType(MockWebServiceMessageSender.class).values();
				
		template.setMessageSenders(mockSenders.toArray(new WebServiceMessageSender[mockSenders.size()]));		
 	}
 }
