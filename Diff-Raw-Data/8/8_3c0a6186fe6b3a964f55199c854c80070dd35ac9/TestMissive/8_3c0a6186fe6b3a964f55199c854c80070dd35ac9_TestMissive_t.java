 package com.barchart.missive.core;
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 public class TestMissive {
 
 	public static final Tag<String> BACON = Tag.create(String.class);
 	public static final Tag<String> HAM = Tag.create(String.class);
 	public static final Tag<String> EGGS = Tag.create(String.class);
 	public static final Tag<String> HASHBROWNS = Tag.create(String.class);
 	
 	public static final Tag<?>[] SMALL = new Tag<?>[]{BACON, EGGS};
 	public static final Tag<?>[] NORMAL = new Tag<?>[]{HASHBROWNS};
 	public static final Tag<?>[] SUPER = new Tag<?>[]{HAM};
 	
 	public static class SmallBreakfast extends Missive {
 		
 		static {
 			install(SMALL);
 		}
		
 	}
 	
 	public static class SmallBreakfastSafe extends MissiveSafe {
 		
 		static {
 			install(SMALL);
 		}
 		
 	}
 	
 	public static class NormalBreakfast extends SmallBreakfast {
 		
 		static {
 			install(NORMAL);
 		}
 		
 	}
 	
 	public static class NormalBreakfastSafe extends SmallBreakfastSafe {
 		
 		static {
 			install(NORMAL);
 		}
 		
 	}
 	
 	public static class SuperBreakfast extends NormalBreakfast {
 		
 		static {
 			install(SUPER);
 		}
 		
 	}
 	
 	public static class SuperBreakfastSafe extends NormalBreakfastSafe {
 		
 		static {
 			install(SUPER);
 		}
 		
 	}
 	
 	@Test
 	public void testMissive() {
 		
 		SmallBreakfast smallBfast = Missive.build(SmallBreakfast.class);
 		SmallBreakfastSafe smallSafe = Missive.build(SmallBreakfastSafe.class);
 		NormalBreakfast normBfast = Missive.build(NormalBreakfast.class);
 		NormalBreakfastSafe normSafe = Missive.build(NormalBreakfastSafe.class);
 		SuperBreakfast superBfast = Missive.build(SuperBreakfast.class);
 		SuperBreakfastSafe superSafe = Missive.build(SuperBreakfastSafe.class);
 		
 		assertTrue(smallBfast.contains(BACON));
 		assertTrue(smallBfast.contains(EGGS));
 		assertTrue(!smallBfast.contains(HASHBROWNS));
 		assertTrue(!smallBfast.contains(HAM));
 		
 		assertTrue(normBfast.contains(BACON));
 		assertTrue(normBfast.contains(EGGS));
 		assertTrue(normBfast.contains(HASHBROWNS));
 		assertTrue(!normBfast.contains(HAM));
 		
 		assertTrue(superBfast.contains(BACON));
 		assertTrue(superBfast.contains(EGGS));
 		assertTrue(superBfast.contains(HASHBROWNS));
 		assertTrue(superBfast.contains(HAM));
 		
 		assertTrue(smallSafe.contains(BACON));
 		assertTrue(smallSafe.contains(EGGS));
 		assertTrue(!smallSafe.contains(HASHBROWNS));
 		assertTrue(!smallSafe.contains(HAM));
 		
 		assertTrue(normSafe.contains(BACON));
 		assertTrue(normSafe.contains(EGGS));
 		assertTrue(normSafe.contains(HASHBROWNS));
 		assertTrue(!normSafe.contains(HAM));
 		
 		assertTrue(superSafe.contains(BACON));
 		assertTrue(superSafe.contains(EGGS));
 		assertTrue(superSafe.contains(HASHBROWNS));
 		assertTrue(superSafe.contains(HAM));
 		
 		smallSafe.set(BACON, "bacon");
 		smallSafe.set(EGGS, "eggs");
 		
 		assertTrue(smallSafe.get(BACON).equals("bacon"));
 		assertTrue(smallSafe.get(EGGS).equals("eggs"));
 		
 	}
 	
 	
 }
