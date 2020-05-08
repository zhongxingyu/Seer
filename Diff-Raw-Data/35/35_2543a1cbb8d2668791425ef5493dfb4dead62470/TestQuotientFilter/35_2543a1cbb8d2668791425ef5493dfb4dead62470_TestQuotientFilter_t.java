 package com.eigenvektor.amq;
 
 import static org.junit.Assert.*;
 
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestQuotientFilter
 {
 	@Before
 	public void setUp() throws Exception
 	{
 	}
 
 	@After
 	public void tearDown() throws Exception
 	{
 	}
 
 	/**
 	 * Tests with simple inserts
 	 */
 	@Test
 	public void testSimpleInsert()
 	{
 		ApproxMemQuery<Integer> qf = new QuotientFilter<Integer>(3);
 		qf.add(5);
 		assertTrue(qf.contains(5));
 		
 		int test = (3 << 29) + 63;
 		qf.add(test);
 		assertTrue(qf.contains(test));
 		
 		int test2 = (3 << 29) + 66;
 		assertTrue(!qf.contains(test2));
 	}
 	
 	/**
 	 * Tests with a set of elements that comprise a single run.
 	 */
 	@Test
 	public void testSingleRun()
 	{
 		// Create a single run of integers with the same quotient.
 		ApproxMemQuery<Integer> qf = new QuotientFilter<Integer>(3); 
 		
 		int test1 = (3 << 29) + 63;
 		int test2 = (3 << 29) + 66;
 		int test3 = (3 << 29) + 65;
 		int test4 = (3 << 29) + 61;
 		
 		qf.add(test1);
 		qf.add(test2);
 		qf.add(test3);
 		qf.add(test4);
 		
 		assertTrue(qf.contains(test1));
 		assertTrue(qf.contains(test2));
 		assertTrue(qf.contains(test3));
 		assertTrue(qf.contains(test4));
 	}
 	
 	/**
 	 * Tests with a set of elements that comprise a single run that wraps around
 	 * the end of the BitSet.
 	 */
 	@Test
 	public void testSingleRunWrapped()
 	{
 		// Create a single run of integers with the same quotient.
 		ApproxMemQuery<Integer> qf = new QuotientFilter<Integer>(3); 
 		
 		int test1 = (7 << 29) + 63;
 		int test2 = (7 << 29) + 66;
 		int test3 = (7 << 29) + 65;
 		int test4 = (7 << 29) + 61;
 		
 		qf.add(test1);
 		qf.add(test2);
 		qf.add(test3);
 		qf.add(test4);
 		
 		assertTrue(qf.contains(test1));
 		assertTrue(qf.contains(test2));
 		assertTrue(qf.contains(test3));
 		assertTrue(qf.contains(test4));
 	}
 	
 	/**
 	 * A test that creates a simple cluster and makes sure everything works.
 	 */
 	@Test
 	public void testSimpleCluster()
 	{
 		// Create a single run of integers with the same quotient.
 		ApproxMemQuery<Integer> qf = new QuotientFilter<Integer>(3); 
 		
 		int test1 = (3 << 29) + 63; // Canonically in 3
 		int test2 = (3 << 29) + 66;
 		
 		int test3 = (1 << 29) + 66; // Canonically all in 1, and enough of them that they push 3 out of the way.
 		int test4 = (1 << 29) + 65;
 		int test5 = (1 << 29) + 61;
 		int test6 = (1 << 29) + 3;
 		
 		qf.add(test1);
 		qf.add(test2);
 		qf.add(test3);
 		qf.add(test4);
 		qf.add(test5);
 		qf.add(test6);
 		
 		assertTrue(qf.contains(test1));
 		assertTrue(qf.contains(test2));
 		assertTrue(qf.contains(test3));
 		assertTrue(qf.contains(test4));
 		assertTrue(qf.contains(test5));
 		assertTrue(qf.contains(test6));
 	}
 	
 	/**
 	 * A test that creates a simple cluster and makes sure everything works, this time wrapped
 	 * around the end of the bitset.
 	 */
 	@Test
 	public void testSimpleClusterWrapped()
 	{
 		// Create a single run of integers with the same quotient.
 		ApproxMemQuery<Integer> qf = new QuotientFilter<Integer>(3); 
 		
 		int test1 = (1 << 29) + 63; // Canonically in 1
 		int test2 = (1 << 29) + 66;
 		
 		int test3 = (7 << 29) + 66; // Canonically all in 7, and enough of them that they push 3 out of the way.
 		int test4 = (7 << 29) + 65;
 		int test5 = (7 << 29) + 61;
 		int test6 = (7 << 29) + 3;
 		
 		qf.add(test1);
 		qf.add(test2);
 		qf.add(test3);
 		qf.add(test4);
 		qf.add(test5);
 		qf.add(test6);
 		
 		assertTrue(qf.contains(test1));
 		assertTrue(qf.contains(test2));
 		assertTrue(qf.contains(test3));
 		assertTrue(qf.contains(test4));
 		assertTrue(qf.contains(test5));
 		assertTrue(qf.contains(test6));
 	}
 	
 	/**
 	 * A test that creates a cluster, and then inserts into a slot that is 
 	 * canonically in the middle of the already extant cluster.
 	 */
 	@Test
 	public void testInsertIntoCluster()
 	{
 		ApproxMemQuery<Integer> qf = new QuotientFilter<Integer>(3); 
 		
 		int test1 = (3 << 29) + 63; // Canonically in 3
 		int test2 = (3 << 29) + 66;
 		
 		int test3 = (1 << 29) + 66; // Canonically all in 1, and enough of them that they push 3 out of the way.
 		int test4 = (1 << 29) + 65;
 		int test5 = (1 << 29) + 61;
 		int test6 = (1 << 29) + 3;
 		
 		int test7 = (4 << 29) + 99; // Canonically in 4.
 		
 		// Make the cluster.
 		qf.add(test3);
 		qf.add(test4);
 		qf.add(test5);
 		qf.add(test6);
 		qf.add(test7);
 		
 		// Insert into it.
 		qf.add(test1);
 		qf.add(test2);
 		
 		assertTrue(qf.contains(test1));
 		assertTrue(qf.contains(test2));
 		assertTrue(qf.contains(test3));
 		assertTrue(qf.contains(test4));
 		assertTrue(qf.contains(test5));
 		assertTrue(qf.contains(test6));
 		assertTrue(qf.contains(test7));
 	}
 	
 	
 	/**
 	 * A bigger scale test
 	 */
 	@Test
 	public void bigTest()
 	{
 		// 1024 slots.
 		QuotientFilter<Integer> qf = new QuotientFilter<Integer>(10); 
 		
 		Set<Integer> nums = new HashSet<Integer>();
 		
 		// Insert 1000 random numbers and see how it does.
 		Random rnd = new Random(1337);
 		for (int j = 0 ; j < 1000 ; ++j)
 		{
 			byte[] b = new byte[4];
 			rnd.nextBytes(b);
 			int test = 0;
 			for (int k = 0 ; k < 4 ; ++k)
 			{
 				test = test << 8;
 				test += b[k];
 			}
 			
 			qf.add(test);
 			nums.add(test);
 		}
 		
 		for (int x : nums)
 		{
 			assertTrue(qf.contains(x));
 		}
 		
 		QuotientFilter<Integer>.Stats stats = qf.getStats();
 		assertTrue(stats.getNumOccupied() == qf.getNumOccupied());
 		
 		// Do a bunch of negative tests as well.
 		for (int j = 0 ; j < 1000 ; ++j)
 		{
 			byte[] b = new byte[4];
 			rnd.nextBytes(b);
 			int test = 0;
 			for (int k = 0 ; k < 4 ; ++k)
 			{
 				test = test << 8;
 				test += b[k];
 			}
 			
 			if (nums.contains(test))
 			{
 				assertTrue(qf.contains(test));
 			}
 			else
 			{
 				assertTrue(!qf.contains(test));
 			}
 		}
 	}
 	
 	
 	/**
 	 * A bigger scale test that only uses 10 bits for the remainder.
 	 */
 	@Test
 	public void bigTestSmallRemainder()
 	{
 		// 1024 slots, 10 bit remainder.
 		QuotientingStrategy<Integer> quot = new HashQuotientingStrategy<>(10, 10);
 		ApproxMemQuery<Integer> qf = new QuotientFilter<Integer>(quot); 
 		
 		Set<Integer> nums = new HashSet<Integer>();
 		
 		// Insert 1000 random numbers and see how it does.
 		Random rnd = new Random(1337);
 		for (int j = 0 ; j < 1000 ; ++j)
 		{
 			byte[] b = new byte[4];
 			rnd.nextBytes(b);
 			int test = 0;
 			for (int k = 0 ; k < 4 ; ++k)
 			{
 				test = test << 8;
 				test += b[k];
 			}
 			
 			qf.add(test);
			
			int canonical = test & 0b11111111110000000000001111111111;
			nums.add(canonical);
 		}
 		
 		for (int x : nums)
 		{
 			assertTrue(qf.contains(x));
 		}
 		
		// Do a bunch of negative tests as well.
		for (int j = 0 ; j < 1000 ; ++j)
		{
			byte[] b = new byte[4];
			rnd.nextBytes(b);
			int test = 0;
			for (int k = 0 ; k < 4 ; ++k)
			{
				test = test << 8;
				test += b[k];
			}

			int canonical = test & 0b11111111110000000000001111111111;
			if (nums.contains(canonical))
			{
				assertTrue(qf.contains(canonical));
			}
			else
			{
				assertTrue(!qf.contains(canonical));
			}
		}
		
 	}
 }
