 /*
  * Copyright 2012 Christian Senk
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.csenk.gwt.commons.bean.client.observe.impl;
 
 import org.junit.Test;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.junit.client.GWTTestCase;
 
 import de.csenk.gwt.commons.bean.shared.observe.ObservableBean;
 import de.csenk.gwt.commons.bean.shared.observe.ObservableBeans;
 import de.csenk.gwt.commons.bean.shared.observe.PropertyValueChangeEvent;
 import de.csenk.gwt.commons.bean.shared.observe.PropertyValueChangeHandler;
 
 
 /**
  * @author senk.christian@googlemail.com
  *
  */
 public class ObservableBeanFactoryImplTest extends GWTTestCase {
 
 	public interface Actor {
 		
 		void setName(String name);
 		
 		String getName();
 		
 		void setAge(int age);
 		
 		int getAge();
 		
 	}
 	
 	public interface ObservableBeanFactory extends de.csenk.gwt.commons.bean.shared.observe.ObservableBeanFactory {
 		
 		ObservableBean<Actor> create();
 		
 		ObservableBean<Actor> create(Actor original);
 		
 	}
 	
 	public class ActorBean implements Actor {
 		
 		private String name;
 		private int age;
 		
 		/**
 		 * @param name
 		 * @param age
 		 */
 		public ActorBean(String name, int age) {
 			this.name = name;
 			this.age = age;
 		}
 
 		/**
 		 * @return the name
 		 */
 		public String getName() {
 			return name;
 		}
 		
 		/**
 		 * @param name the name to set
 		 */
 		public void setName(String name) {
 			this.name = name;
 		}
 		
 		/**
 		 * @return the age
 		 */
 		public int getAge() {
 			return age;
 		}
 		
 		/**
 		 * @param age the age to set
 		 */
 		public void setAge(int age) {
 			this.age = age;
 		}
 		
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getModuleName() {
 		return "de.csenk.gwt.commons.Bean";
 	}
 	
 	@Test
 	public void testCreateNotNull() {
 		final ObservableBeanFactory actorObservableBeanFactory = GWT.create(ObservableBeanFactory.class);
 		
 		final ObservableBean<Actor> actorObservableBean = actorObservableBeanFactory.create();
 		assertNotNull(actorObservableBean);
 	}
 	
 	@Test
 	public void testAsNotNull() {
 		final ObservableBeanFactory actorObservableBeanFactory = GWT.create(ObservableBeanFactory.class);
 		
 		final ObservableBean<Actor> actorObservableBean = actorObservableBeanFactory.create();
 		assertNotNull(actorObservableBean.as());
 	}
 	
 	@Test
 	public void testGetPropertyNotNull() {
 		final ObservableBeanFactory actorObservableBeanFactory = GWT.create(ObservableBeanFactory.class);
 		
 		final ObservableBean<Actor> actorObservableBean = actorObservableBeanFactory.create();
 		final Actor actor = actorObservableBean.as();
 		
 		assertNotNull(actorObservableBean.getProperty(actor.getName()));
 	}
 	
 	@Test
 	public void testProperty() {
 		final ObservableBeanFactory actorObservableBeanFactory = GWT.create(ObservableBeanFactory.class);
 		
 		final ObservableBean<Actor> actorObservableBean = actorObservableBeanFactory.create();
 		final Actor actor = actorObservableBean.as();
 		
		actor.setName("Sheldon"); 		//Event -> oldValue=null, newValue="Sheldon"
		actor.setName("Sheldon"); 		//No event
		actor.setName("Wollowitz"); 	//Event -> oldValue="Sheldon", newValue="Wollowitz"
 		
		assertEquals("Wollowitz", actor.getName());
 		
 		assertEquals(0, actor.getAge());
 	}
 	
 	@Test
 	public void testPropertyValueChangeEvent() {
 		final ObservableBeanFactory actorObservableBeanFactory = GWT.create(ObservableBeanFactory.class);
 		
 		final ObservableBean<Actor> actorObservableBean = actorObservableBeanFactory.create();
 		final Actor actor = actorObservableBean.as();
 		
 		actorObservableBean.getProperty(actor.getName()).addPropertyValueChangeHandler(new PropertyValueChangeHandler<String>() {
 			
 			@Override
 			public void onPropertyValueChange(PropertyValueChangeEvent<String> event) {
 				assertEquals(null, event.getOldValue());
 				assertEquals("Sheldon", event.getNewValue());
 			}
 			
 		});
 		
 		actor.setName("Sheldon");
 	}
 	
 	@Test
 	public void testWrapping() {
 		final ObservableBeanFactory actorObservableBeanFactory = GWT.create(ObservableBeanFactory.class);
 		
 		final Actor actor = new ActorBean("Sheldon", 23);
 		final ObservableBean<Actor> actorObservableBean = actorObservableBeanFactory.create(actor);
 		
 		assertEquals(23, actorObservableBean.as().getAge());
 		assertEquals("Sheldon", actorObservableBean.as().getName());
 		
 		actorObservableBean.as().setAge(19);
 		assertEquals(19, actor.getAge());
 		
 		actorObservableBean.as().setName("Wollowitz");
 		assertEquals("Wollowitz", actor.getName());
 		
 		actorObservableBean.getProperty(actor.getName()).addPropertyValueChangeHandler(new PropertyValueChangeHandler<String>() {
 			
 			@Override
 			public void onPropertyValueChange(PropertyValueChangeEvent<String> event) {
 				throw new RuntimeException("Should not occur.");
 			}
 			
 		});
 		
 		actor.setName("Sheldon");
 		actor.setAge(23);
 	}
 	
 	@Test
 	public void testSimpleWeakMapping() {
 		final ObservableBeanFactory actorObservableBeanFactory = GWT.create(ObservableBeanFactory.class);
 		
 		final ObservableBean<Actor> actorObservableBean = actorObservableBeanFactory.create();
 		assertEquals(actorObservableBean, ObservableBeans.get(actorObservableBean.as()));
 	}
 	
 	@Test
 	public void testComplexWeakMapping() {
 		final ObservableBeanFactory actorObservableBeanFactory = GWT.create(ObservableBeanFactory.class);
 		
 		final Actor actor = new ActorBean("Sheldon", 23);
 		final ObservableBean<Actor> actorObservableBean = actorObservableBeanFactory.create(actor);
 		
 		assertEquals(actorObservableBean, ObservableBeans.get(actorObservableBean.as()));
 		assertEquals(actorObservableBean, ObservableBeans.get(actor));
 	}
 	
 }
