 /*
  * Copyright 2012 Vincent Demeester<vincent+shortbrain@demeester.fr>.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.shortbrain.vaadin.container.annotation.reader;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.shortbrain.vaadin.container.AbstractContainerUtilsTest;
 import org.shortbrain.vaadin.container.annotation.Container;
 import org.shortbrain.vaadin.container.annotation.ContainerType;
 import org.shortbrain.vaadin.container.annotation.Property;
 import org.shortbrain.vaadin.container.property.PropertyMetadata;
 
 @SuppressWarnings("unused")
 @RunWith(BlockJUnit4ClassRunner.class)
 public class ContainerBeanAnnotationReaderTest extends
 		AbstractContainerUtilsTest {
 
 	@Test
 	public void getMetadataByContainerTypeNulls() {
 		try {
 			ContainerBeanAnnotationReader.getMetadataByContainerType(null);
 			fail("should throw an IllegalArgumentException");
 		} catch (IllegalArgumentException e) {
 			assertTrue(e instanceof IllegalArgumentException);
 			assertEquals("beanClass and annotationClass cannot be null.",
 					e.getMessage());
 		} catch (IllegalAccessException e) {
 			fail("should throw an IllegalArgumentException");
 		} catch (InvocationTargetException e) {
 			fail("should throw an IllegalArgumentException");
 		} catch (NoSuchMethodException e) {
 			fail("should throw an IllegalArgumentException");
 		} catch (InstantiationException e) {
 			fail("should throw an IllegalArgumentException");
 		} catch (SecurityException e) {
 			fail("should throw an IllegalArgumentException");
 		} catch (NoSuchFieldException e) {
 			fail("should throw an IllegalArgumentException");
 		}
 	}
 	
 	@Test
 	public void getMetadataByContainerTypeNonAnnotated() {
 		try {
 			ContainerBeanAnnotationReader.getMetadataByContainerType(NonAnnotatedBean.class);
 			fail("should throw an IllegalArgumentException");
 		} catch (IllegalArgumentException e) {
 			assertTrue(e instanceof IllegalArgumentException);
 			assertEquals("beanClass and its super classes are not annotated with Container.",
 					e.getMessage());
 		} catch (IllegalAccessException e) {
 			fail("should throw an IllegalArgumentException");
 		} catch (InvocationTargetException e) {
 			fail("should throw an IllegalArgumentException");
 		} catch (NoSuchMethodException e) {
 			fail("should throw an IllegalArgumentException");
 		} catch (InstantiationException e) {
 			fail("should throw an IllegalArgumentException");
 		} catch (SecurityException e) {
 			fail("should throw an IllegalArgumentException");
 		} catch (NoSuchFieldException e) {
 			fail("should throw an IllegalArgumentException");
 		}
 	}
 
 	@Test
 	public void getMetadataBycontainerTypeWrongAnnotatedBean() {
 		try {
 			Map<ContainerType, List<PropertyMetadata>> map = ContainerBeanAnnotationReader.getMetadataByContainerType(WrongAnnotatedBean.class);
 			fail("should thrown a NoSuchFieldException");
 		} catch (IllegalAccessException e) {
 			fail("should not throw an IllegalAccessException");
 		} catch (InvocationTargetException e) {
 			fail("should not throw an InvocationTargetException");
 		} catch (NoSuchMethodException e) {
 			fail("should not throw an NoSuchMethodException");
 		} catch (InstantiationException e) {
 			fail("should not throw an InstantiationException");
 		} catch (SecurityException e) {
 			fail("should not throw an SecurityException");
 		} catch (NoSuchFieldException e) {
 			assertTrue(e instanceof NoSuchFieldException);
            assertEquals("No field string for class java.lang.Object.", e.getMessage());
			// FIXME: A better message should be the following
			// assertEquals("No field string for class org.shortbrain.vaadin.container.annotation.reader.ContainerBeanAnnotationReaderTest$WrongAnnotatedBean.", e.getMessage());
 		}
 	}
 	
 	@Test
 	public void getMetadataBycontainerTypeSimpleAnnotatedBean() {
 		try {
 			Map<ContainerType, List<PropertyMetadata>> map = ContainerBeanAnnotationReader.getMetadataByContainerType(SimpleAnnotatedBean.class);
 			assertNotNull(map);
 			assertEquals(2, map.size());
 			assertTrue(map.containsKey(ContainerType.RESUME));
 			assertTrue(map.containsKey(ContainerType.EXTENDED));
 			// Test entries
 			List<PropertyMetadata> resumeMetadata = map.get(ContainerType.RESUME);
 			assertNotNull(resumeMetadata);
 			assertEquals(1, resumeMetadata.size());
 			assertMetadata("string", String.class, null, "string", resumeMetadata.get(0));
 			List<PropertyMetadata> extendedMetadata = map.get(ContainerType.EXTENDED);
 			assertNotNull(extendedMetadata);
 			assertEquals(2, extendedMetadata.size());
 			assertMetadata("string", String.class, null, "string", extendedMetadata.get(0));
 			assertMetadata("subsubstring", String.class, null, "nonAnnotatedBean.subString", extendedMetadata.get(1));
 		} catch (IllegalAccessException e) {
 			fail("should not throw an IllegalAccessException");
 		} catch (InvocationTargetException e) {
 			fail("should not throw an InvocationTargetException");
 		} catch (NoSuchMethodException e) {
 			fail("should not throw an NoSuchMethodException");
 		} catch (InstantiationException e) {
 			fail("should not throw an InstantiationException");
 		} catch (SecurityException e) {
 			fail("should not throw an SecurityException");
 		} catch (NoSuchFieldException e) {
 			fail("should not throw an NoSuchFieldException");
 		}
 	}
 	
 	@Test
 	public void getMetadataBycontainerTypeSubSimpleAnnotatedBean() {
 		try {
 			Map<ContainerType, List<PropertyMetadata>> map = ContainerBeanAnnotationReader.getMetadataByContainerType(SubSimpleAnnotatedBean.class);
 			assertNotNull(map);
 			assertEquals(2, map.size());
 			assertTrue(map.containsKey(ContainerType.RESUME));
 			assertTrue(map.containsKey(ContainerType.EXTENDED));
 			// Test entries
 			List<PropertyMetadata> resumeMetadata = map.get(ContainerType.RESUME);
 			assertNotNull(resumeMetadata);
 			assertEquals(1, resumeMetadata.size());
 			assertMetadata("string", String.class, null, "string", resumeMetadata.get(0));
 			List<PropertyMetadata> extendedMetadata = map.get(ContainerType.EXTENDED);
 			assertNotNull(extendedMetadata);
 			assertEquals(2, extendedMetadata.size());
 			assertMetadata("string", String.class, null, "string", extendedMetadata.get(0));
 			assertMetadata("subsubstring", String.class, null, "nonAnnotatedBean.subString", extendedMetadata.get(1));
 		} catch (IllegalAccessException e) {
 			fail("should not throw an IllegalAccessException");
 		} catch (InvocationTargetException e) {
 			fail("should not throw an InvocationTargetException");
 		} catch (NoSuchMethodException e) {
 			fail("should not throw an NoSuchMethodException");
 		} catch (InstantiationException e) {
 			fail("should not throw an InstantiationException");
 		} catch (SecurityException e) {
 			fail("should not throw an SecurityException");
 		} catch (NoSuchFieldException e) {
 			e.printStackTrace();
 			fail("should not throw an NoSuchFieldException");
 		}
 	}
 	
 	@Container(properties = {
 			@Property(name = "string", types = {ContainerType.EXTENDED, ContainerType.RESUME}),
 			@Property(name = "subsubstring", types = { ContainerType.EXTENDED}, attribute = "nonAnnotatedBean.subString")
 	})
 	private static class SimpleAnnotatedBean {
 		private String string;
 		private NonAnnotatedBean nonAnnotatedBean;
 
 		public String getString() {
 			return string;
 		}
 
 		public NonAnnotatedBean getNonAnnotatedBean() {
 			return nonAnnotatedBean;
 		}
 
 	}
 
 	private static class SubSimpleAnnotatedBean extends SimpleAnnotatedBean {
 	}
 
 	@Container(properties = {
 			@Property(name = "string", types = {ContainerType.EXTENDED, ContainerType.RESUME}),
 	})
 	private static class WrongAnnotatedBean {
 		private Integer integer;
 
 		public Integer getInteger() {
 			return integer;
 		}
 		
 	}
 
 	private static class NonAnnotatedBean {
 		private String subString;
 
 		public String getSubString() {
 			return subString;
 		}
 	}
 }
