 package com.khs.sherpa.spring;
 
 /*
  * Copyright 2012 the original author or authors.
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
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletContext;
 
 import org.reflections.Reflections;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.NoSuchBeanDefinitionException;
 import org.springframework.beans.factory.support.BeanDefinitionRegistry;
 import org.springframework.beans.factory.support.GenericBeanDefinition;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.security.core.token.TokenService;
 
 import com.khs.sherpa.SherpaSettings;
 import com.khs.sherpa.annotation.Endpoint;
 import com.khs.sherpa.context.factory.InitManageBeanFactory;
 import com.khs.sherpa.context.factory.ManagedBeanFactory;
 import com.khs.sherpa.exception.NoSuchManagedBeanExcpetion;
 import com.khs.sherpa.json.service.ActivityService;
 import com.khs.sherpa.json.service.JsonProvider;
 import com.khs.sherpa.json.service.UserService;
 import com.khs.sherpa.parser.BooleanParamParser;
 import com.khs.sherpa.parser.CalendarParamParser;
 import com.khs.sherpa.parser.DateParamParser;
 import com.khs.sherpa.parser.DoubleParamPaser;
 import com.khs.sherpa.parser.FloatParamParser;
 import com.khs.sherpa.parser.IntegerParamParser;
 import com.khs.sherpa.parser.JsonParamParser;
 import com.khs.sherpa.parser.StringParamParser;
 import com.khs.sherpa.util.Util;
 
 
 public class SpringManagedBeanFactory implements ManagedBeanFactory, InitManageBeanFactory, ApplicationContextAware {
 
 	private ApplicationContext springApplicationContext;
 	
 	public boolean containsManagedBean(Class<?> type) {
 		return springApplicationContext.containsBean(type.getSimpleName());
 	}
 
 	public boolean containsManagedBean(String name) {
 		return springApplicationContext.containsBean(name);
 	}
 
 	public <T> T getManagedBean(Class<T> type) throws NoSuchManagedBeanExcpetion {
 		return springApplicationContext.getBean(type);
 	}
 
 	public <T> Collection<T> getManagedBeans(Class<T> type) {
 		return springApplicationContext.getBeansOfType(type).values();
 	}
 	
 	public Object getManagedBean(String name) throws NoSuchManagedBeanExcpetion {
 		return springApplicationContext.getBean(name);
 	}
 
 	public <T> T getManagedBean(String name, Class<T> type) throws NoSuchManagedBeanExcpetion {
 		return springApplicationContext.getBean(name, type);
 	}
 
 	public boolean isTypeMatch(String name, Class<?> type) throws NoSuchManagedBeanExcpetion {
 		return springApplicationContext.isTypeMatch(name, type);
 	}
 
 	public Class<?> getType(String name) throws NoSuchManagedBeanExcpetion {
 		return springApplicationContext.getType(name);
 	}
 
 	public Map<String, Object> getEndpointTypes() {
 		return springApplicationContext.getBeansWithAnnotation(Endpoint.class);
 	}
 
 	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
 		this.springApplicationContext = applicationContext;
 	}
 
 	public void loadManagedBeans(String path) {
 		Reflections reflections = new Reflections(path);
 		this.loadManagedBeans(reflections.getTypesAnnotatedWith(com.khs.sherpa.annotation.Endpoint.class));
 	}
 	
 	public void loadManagedBeans(Set<Class<?>> types) {
 		for(Class<?> type: types) {
 			this.loadManagedBean(Util.getObjectName(type), type);
 		}
 	}
 	
 	public void loadManagedBean(String name, Class<?> type) {
 		BeanDefinitionRegistry registry = ((BeanDefinitionRegistry) springApplicationContext.getAutowireCapableBeanFactory());
 		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
 		beanDefinition.setBeanClass(type);
 		registry.registerBeanDefinition(name, beanDefinition);
 	}
 	
 	public void init(SherpaSettings settings, ServletContext context) {
 		try {
 			springApplicationContext.getBean(UserService.class);
 		} catch (NoSuchBeanDefinitionException e) {
 			this.loadManagedBean("userService", settings.userService());
 		}
 		
 		try {
			springApplicationContext.getBean(TokenService.class);
 		} catch (NoSuchBeanDefinitionException e) {
 			this.loadManagedBean("tokenService", settings.tokenService());
 		}
 
 		try {
 			springApplicationContext.getBean(ActivityService.class);
 		} catch (NoSuchBeanDefinitionException e) {
 			this.loadManagedBean("activityService", settings.activityService());
 		}
 		
 		try {
 			springApplicationContext.getBean(JsonProvider.class);
 		} catch (NoSuchBeanDefinitionException e) {
 			this.loadManagedBean("jsonProvider", settings.jsonProvider());
 		}
 		
 		this.loadManagedBean("StringParamParser", StringParamParser.class);
 		this.loadManagedBean("IntegerParamParser", IntegerParamParser.class);
 		this.loadManagedBean("DoubleParamPaser", DoubleParamPaser.class);
 		this.loadManagedBean("FloatParamParser", FloatParamParser.class);
 		this.loadManagedBean("BooleanParamParser", BooleanParamParser.class);
 		this.loadManagedBean("DateParamParser", DateParamParser.class);
 		this.loadManagedBean("CalendarParamParser", CalendarParamParser.class);
 		this.loadManagedBean("JsonParamParser", JsonParamParser.class);
 		
 		// load the root domain
 		this.loadManagedBeans("com.khs.sherpa.endpoint");		
 	}
 
 }
