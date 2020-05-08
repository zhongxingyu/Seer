 package unit_tests;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import ai_ex5_pkg.MinConflictAlgo;
 
 public class TestMinConflictAlgo {
 
 	@Test
 	public void test_dimension_1_one_time_assert_false() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(1);
 		boolean actual = mca.start();
 		assertFalse(actual);
 	}
 	
 	@Test
 	public void test_dimension_1_fifty_times_assert_false() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(1);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertFalse(actual);
 		}
 	}
 	@Test
 	public void test_dimension_2_one_time_assert_false() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(2);
 		boolean actual = mca.start();
 		assertFalse(actual);
 	}
 	
 	@Test
 	public void test_dimension_2_fifty_times_assert_false() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(2);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertFalse(actual);
 		}
 	}
 	@Test
 	public void test_dimension_3_one_time_assert_false() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(3);
 		boolean actual = mca.start();
 		assertFalse(actual);
 	}
 	
 	@Test
 	public void test_dimension_3_fifty_times_assert_false() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(3);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertFalse(actual);
 		}
 	}
 	@Test
 	public void test_dimension_4_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(4);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_4_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(4);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_5_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(5);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_5_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(5);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_6_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(6);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_6_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(6);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_7_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(7);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_7_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(7);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_8_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(8);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_8_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(8);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_9_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(9);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_9_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(9);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_10_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(10);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_10_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(10);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_11_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(11);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_11_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(11);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_12_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(12);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_12_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(12);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_13_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(13);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_13_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(13);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_14_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(14);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_14_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(14);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_15_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(15);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_15_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(15);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_16_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(16);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_16_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(16);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_17_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(17);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_17_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(17);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_18_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(18);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_18_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(18);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_19_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(19);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_19_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(19);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_20_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(20);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_20_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(20);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_21_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(21);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_21_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(21);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_22_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(22);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_22_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(22);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_23_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(23);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_23_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(23);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_24_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(24);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_24_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(24);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_25_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(25);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_25_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(25);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_26_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(26);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_26_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(26);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_27_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(27);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_27_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(27);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_28_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(28);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_28_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(28);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_29_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(29);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_29_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(29);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_30_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(30);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_30_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(30);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_31_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(31);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_31_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(31);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 	@Test
 	public void test_dimension_32_one_time_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(32);
 		boolean actual = mca.start();
 		assertTrue(actual);
 	}
 	
 	@Test
 	public void test_dimension_32_fifty_times_assert_true() {
 		ai_ex5_pkg.MinConflictAlgo mca = new MinConflictAlgo(32);
 		for (int i = 0; i < 50; i++) {
 			boolean actual = mca.start();
 			assertTrue(actual);
 		}
 	}
 
 }
