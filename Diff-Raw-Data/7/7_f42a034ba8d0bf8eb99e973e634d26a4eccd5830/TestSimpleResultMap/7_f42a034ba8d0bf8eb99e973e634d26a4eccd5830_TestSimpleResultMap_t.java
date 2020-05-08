 /**
  * Copyright (C) 2008 Ivan S. Dubrov
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *         http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.google.code.nanorm.test.resultmap;
 
 import static com.google.code.nanorm.test.common.Utils.assertContains;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import com.google.code.nanorm.DataSink;
 import com.google.code.nanorm.TypeHandler;
 import com.google.code.nanorm.annotations.Property;
 import com.google.code.nanorm.annotations.ResultMap;
 import com.google.code.nanorm.annotations.Scalar;
 import com.google.code.nanorm.annotations.Select;
 import com.google.code.nanorm.exceptions.DataException;
 import com.google.code.nanorm.test.beans.Publication;
 import com.google.code.nanorm.test.common.MapperTestBase;
 
 /**
  * 
  * @author Ivan Dubrov
  * @version 1.0 27.05.2008
  */
 @SuppressWarnings("all")
 public class TestSimpleResultMap extends MapperTestBase {
 	public interface Mapper1 {
 		// No result map -- automatic mapping (no default map as well)
 		@Select("SELECT id, subject as title, year FROM articles WHERE ID = ${1}")
 		Publication getPublicationById1(int id);
 
 		// Test automatic is off by default
 		@ResultMap(mappings = { @Property(value = "article.subject", column = "subject") })
 		@Select("SELECT id, subject, body, year FROM articles WHERE ID = ${1}")
 		Publication getPublicationById2(int id);
 
 		// Turning on automatic mapping
 		@ResultMap(auto = true, mappings = { @Property(value = "article.subject", column = "subject") })
 		@Select("SELECT id, subject, subject as title FROM articles WHERE ID = ${1}")
 		Publication getPublicationById3(int id);
 
 		// Test list
 		@ResultMap(auto = true, mappings = { @Property(value = "article.subject", column = "subject") })
 		@Select("SELECT id, subject, year FROM articles ORDER BY id ASC")
 		List<Publication> listPublications();
 
 		// Test data sink
 		@ResultMap(auto = true, mappings = { @Property(value = "article.subject", column = "subject") })
 		@Select("SELECT id, subject, year FROM articles ORDER BY id ASC")
 		void listPublications2(DataSink<Publication> callback);
 
 		// Test null return value
 		@Select("SELECT id FROM articles WHERE 1 = 0")
 		int selectN();
 
 		// Test null return value
 		@Select("SELECT id FROM articles")
 		int selectSM();
 
 		// Test primitive array
 		@Select("SELECT id FROM articles")
 		int[] selectArticleIds();
 
 		// Scalar mapping
 		@Select("SELECT '2009-06-07 15:23:34'")
 		@Scalar
 		Timestamp selectTimestamp();
 		
 		// Scalar mapping
 		@Select("SELECT '2009-06-07 15:23:34' UNION SELECT '2010-01-03 06:17:54'")
 		@Scalar
 		Timestamp[] selectTimestampArray();
 	}
 
 	@Test
 	/*
 	 * Test automatic result mapping
 	 */
 	public void testResultMap1() throws Exception {
 		Mapper1 mapper = factory.createMapper(Mapper1.class);
 		Publication pub = mapper.getPublicationById1(2);
 		Assert.assertEquals(2, pub.getId());
 		Assert.assertEquals(2008, pub.getYear());
 		Assert.assertEquals(null, pub.getArticle().getSubject());
 		Assert.assertEquals(null, pub.getArticle().getBody());
 	}
 
 	@Test
 	/*
 	 * Test no automatic result mapping by default
 	 */
 	public void testResultMap2() throws Exception {
 		Mapper1 mapper = factory.createMapper(Mapper1.class);
 		Publication pub = mapper.getPublicationById2(2);
 		Assert.assertEquals(0, pub.getId());
 		Assert.assertEquals(0, pub.getYear());
 		Assert.assertEquals("Saving the Earth", pub.getArticle().getSubject());
 		Assert.assertEquals(null, pub.getArticle().getBody());
 	}
 
 	@Test
 	/*
 	 * Test automatic result mapping
 	 */
 	public void testResultMap3() throws Exception {
 		Mapper1 mapper = factory.createMapper(Mapper1.class);
 		Publication pub = mapper.getPublicationById3(2);
 		Assert.assertEquals(2, pub.getId());
 		Assert.assertEquals(0, pub.getYear());
 		Assert.assertEquals("Saving the Earth", pub.getArticle().getSubject());
 		Assert.assertEquals("Saving the Earth", pub.getTitle());
 		Assert.assertEquals(null, pub.getArticle().getBody());
 	}
 
 	@Test
 	public void testSelectList() throws Exception {
 		Mapper1 mapper = factory.createMapper(Mapper1.class);
 
 		List<Publication> articles = mapper.listPublications();
 		Assert.assertEquals(2, articles.size());
 
 		Publication pub = articles.get(0);
 		Assert.assertEquals(1, pub.getId());
 		Assert.assertEquals("World Domination", pub.getArticle().getSubject());
 		Assert.assertEquals(2007, pub.getYear());
 
 		pub = articles.get(1);
 		Assert.assertEquals(2, pub.getId());
 		Assert.assertEquals("Saving the Earth", pub.getArticle().getSubject());
 		Assert.assertEquals(2008, pub.getYear());
 	}
 
 	@Test
 	public void testSelectList2() throws Exception {
 		Mapper1 mapper = factory.createMapper(Mapper1.class);
 
 		final List<Publication> articles = new ArrayList<Publication>();
 		DataSink<Publication> rc = new DataSink<Publication>() {
 			public void pushData(Publication pub) {
 				articles.add(pub);
 			}
 
 			public void commitData() {
 				// Nothning...
 			}
 		};
 		mapper.listPublications2(rc);
 		Assert.assertEquals(2, articles.size());
 
 		Publication pub = articles.get(0);
 		Assert.assertEquals(1, pub.getId());
 		Assert.assertEquals("World Domination", pub.getArticle().getSubject());
 		Assert.assertEquals(2007, pub.getYear());
 
 		pub = articles.get(1);
 		Assert.assertEquals(2, pub.getId());
 		Assert.assertEquals("Saving the Earth", pub.getArticle().getSubject());
 		Assert.assertEquals(2008, pub.getYear());
 	}
 
 	@Test
 	public void testSelectNull() throws Exception {
 		Mapper1 mapper = factory.createMapper(Mapper1.class);
 		try {
 			mapper.selectN();
 			Assert.fail();
 		} catch (DataException e) {
 			assertContains(e, "selectN", "Mapper1", "empty", "primitive", "int");
 		}
 	}
 
 	@Test
 	public void testSelectSingleMultiple() throws Exception {
 		Mapper1 mapper = factory.createMapper(Mapper1.class);
 
 		try {
 			mapper.selectSM();
 			Assert.fail();
 		} catch (IllegalStateException e) {
 			assertContains(e, "selectSM", "Mapper1", "single");
 		}
 	}
 
 	@Test
 	public void testPrimitiveArray() throws Exception {
 		Mapper1 mapper = factory.createMapper(Mapper1.class);
 		int[] arr = mapper.selectArticleIds();
 		Assert.assertEquals(2, arr.length);
 		Assert.assertEquals(1, arr[0]);
 		Assert.assertEquals(2, arr[1]);
 	}
 
 	/**
 	 * TEST: Invoke mapper method with result mapped as scalar (via appropriate
 	 * {@link TypeHandler}, in this case {@link Timestamp}) or array of scalars.
 	 * 
 	 * EXPECT: Data is mapped successfully and return value matches the expected.
 	 * 
 	 * @throws Exception any exception
 	 */
 	@Test
 	public void testScalarMapping() throws Exception {
 		Mapper1 mapper = factory.createMapper(Mapper1.class);
 		Timestamp stamp = mapper.selectTimestamp();
		Assert.assertEquals(new Timestamp(1244388214000L).getTime(), stamp.getTime());
 		
 		Timestamp[] arr = mapper.selectTimestampArray();
 		Assert.assertEquals(2, arr.length);
		Assert.assertEquals(new Timestamp(1244388214000L).getTime(), arr[0].getTime());
		Assert.assertEquals(new Timestamp(1262499474000L).getTime(), arr[1].getTime());
 	}
 }
