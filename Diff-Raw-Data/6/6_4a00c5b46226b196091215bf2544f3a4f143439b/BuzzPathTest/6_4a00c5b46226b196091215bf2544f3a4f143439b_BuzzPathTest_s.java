 /** ==== BEGIN LICENSE =====
    Copyright 2012 - BeeQueue.org
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  
  *  ===== END LICENSE ====== */
 package org.beequeue.buzz;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.junit.Test;
 
 public class BuzzPathTest {
 
 	@Test
 	public void compareTo() {
 		assertEquals(0, bp("a","b").compareTo( bp("a","b") ));
 		assertEquals(1, bp("a","b").compareTo( bp() ));
 		assertEquals(-1, bp("a","b").compareTo( bp("a","c") ));
 		assertEquals(1, bp("a","c").compareTo( bp("a","b") ));
 		assertEquals(1, bp("a","b").compareTo( bp("a","a") ));
 		assertEquals(1, bp("a","b").compareTo( bp("a") ));
 		assertEquals(-1, bp("a").compareTo( bp("a","b") ));
 	}
 	
 	@Test
 	public void startsWith() {
 		assertTrue( bp("a","b").startsWith(bp("a")) );
 		assertTrue( bp("a","b", "c").startsWith(bp("a")) );
 		assertTrue( bp("a","b", "c").startsWith(bp("a", "b")) );
 		assertTrue( bp("a","b", "c").startsWith(bp("a", "b", "c")) );
 		assertTrue( !bp("a","b", "c").startsWith(bp("a", "b", "c", "d")) );
 		assertTrue( !bp("a","b", "c").startsWith(bp("b", "a", "c")) );
 	}
 
 
 	public BuzzPath bp(String... p) {
 		return new BuzzPath(p);
 	}
 	
 	@Test
 	public void subpath() {
 		assertEquals(bp("a","b","c").subpath(1, 3), bp( "b", "c" ));
 		assertTrue( bp("a","b","a","b").subpath(2, 4).startsWith(bp("a")) );
 		assertTrue( bp("a","b", "c").startsWith(bp("a","a","b").subpath(1, 2)) );
 		assertTrue( bp("a","b", "c","a","b", "c").subpath(0, 3).startsWith(bp("a", "b")) );
 		assertEquals(1, bp("x", "a","b").subpath(1, 3).compareTo( bp() ));
 		assertEquals(-1, bp("a","b", "x").subpath(0, 2).compareTo( bp("a","c") ));
 		assertEquals(0, bp("a","b", "x").subpath(0, 2).compareTo( bp("a","b") ));
 		assertEquals(1, bp("r","a","c","r").subpath(1, 3).compareTo( bp("a","b") ));
 		assertSerialization(bp("q").subpath(1, 1).subpath(0, 0) );
 		assertSerialization(bp("q","a","q").subpath(1, 2));
 		assertSerialization(bp("a", "b", "c", "a", "b", "c").subpath(3, 6));
 	}
 
 	@Test
 	public void findAncestors() {
 		Set<BuzzPath> set = new HashSet<BuzzPath>();
 		set.add(BuzzPath.valueOf("a"));
 		set.add(BuzzPath.valueOf("a/c"));
 		set.add(BuzzPath.valueOf("a/b"));
 		set.add(BuzzPath.valueOf("a/c/q"));
 		set.add(BuzzPath.valueOf("a/q"));
 		set.add(BuzzPath.valueOf(""));
 		assertEquals(BuzzPath.valueOf("a/c/q").findAncestor(set).toString(),"a/c/q");	
 		assertEquals(BuzzPath.valueOf("a/c/m").findAncestor(set).toString(),"a/c");	
 		assertEquals(BuzzPath.valueOf("a/z/m").findAncestor(set).toString(),"a");	
 		assertEquals(BuzzPath.valueOf("z/z/m").findAncestor(set).toString(),"");	
 		assertEquals(BuzzPath.valueOf("a/c/q").findAllAncestors(set).toString(),"[a/c/q, a/c, a, ]");	
 		assertEquals(BuzzPath.valueOf("a/c/m").findAllAncestors(set).toString(),"[a/c, a, ]");	
 		assertEquals(BuzzPath.valueOf("a/z/m").findAllAncestors(set).toString(),"[a, ]");	
 		assertEquals(BuzzPath.valueOf("z/z/m").findAllAncestors(set).toString(),"[]");	
 	}
 	@Test
 	public void add() {
 		assertEquals(BuzzPath.valueOf("a").addElements("c").toString(), "a/c");
 	}
 	@Test
 	public void name() {
 		assertEquals(BuzzPath.valueOf("a").addElements("c").name(), "c");
 		assertTrue(BuzzPath.valueOf("").name()==null);
 	}
 	@Test
 	public void valueOf() {
 		assertSerialization(bp());
 		assertSerialization(bp("a"));
 		assertSerialization(bp("a", "b", "c"));
 		try{
 			assertSerialization(bp("a", null, "c"));
 			fail();
 		}catch (BuzzException e) {
 			assertEquals(e.statusCode, 500);
			assertTrue(e.getMessage().startsWith("path mailformed: empty elements in the middle") );
 		}
 		try{
 			assertSerialization(bp("a", "", "c"));
 			fail();
 		}catch (BuzzException e) {
 			assertEquals(e.statusCode, 500);
			assertTrue(e.getMessage().startsWith("path mailformed: empty elements in the middle") );
 		}
 	}
 
 	public void assertSerialization(BuzzPath bp) {
 		String s1 = bp.toString();
 		BuzzPath bp2 = BuzzPath.valueOf(s1);
 		assertEquals(bp, bp2);
 		String s2 = bp2.toString();
 		assertEquals(s1, s2);
 		
 	}
 
 }
