 /**
  * Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.util.values.provider;
 
 import java.lang.reflect.Field;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.barchart.util.bench.size.JavaSize;
 import com.barchart.util.values.api.Value;
 
 public class TestValueConst {
 
 	private static final Logger log = LoggerFactory
 			.getLogger(TestValueConst.class);
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	public static final void sizeReport(final Class<?> klaz) {
 
 		final StringBuilder text = new StringBuilder(1024);
 
 		text.append("##################################");
 		text.append("\n");
 		text.append("Instance Size Report");
 		text.append("\n");
 
 		final Field[] fieldArray = klaz.getFields();
 
 		for (final Field field : fieldArray) {
 			if (Value.class.isAssignableFrom(field.getType())) {
 				try {
 					final String name = field.getName();
 					final Object value = field.get(null);
 					final int size = JavaSize.of(value);
 					text.append(String.format("%-20s %,9d", name, size));
 					text.append("\n");
 				} catch (final Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		text.append("##################################");
 		text.append("\n");
 
 		log.debug("\n{}\n", text);
 
 	}
 
 	@Test
 	public void testSizeReport() {

		// TODO add instances to report on

 	}
 
 }
