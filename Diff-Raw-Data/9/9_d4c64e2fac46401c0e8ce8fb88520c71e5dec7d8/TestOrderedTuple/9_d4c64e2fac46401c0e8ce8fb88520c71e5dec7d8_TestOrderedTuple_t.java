 /*
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package org.gengar.util.tuple.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.gengar.util.tuple.IOrderedTuple;
 import org.gengar.util.tuple.impl.OrderedTuple;
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestOrderedTuple {
 
 	private List<IOrderedTuple<String, Integer>> $animal_list;
 	
 	private List<IOrderedTuple<String, Integer>> $ordered_animal_list;
 	
 	@Before
 	public void populate_lists() {
 		
 		$animal_list = new ArrayList<IOrderedTuple<String, Integer>>();
 		$ordered_animal_list = new ArrayList<IOrderedTuple<String, Integer>>();
 		
 		// Populate the animal list
 		$animal_list.add( new OrderedTuple<String, Integer>( "Dog", 3 ) );
 		$animal_list.add( new OrderedTuple<String, Integer>( "Dog", 5 ) );
 		$animal_list.add( new OrderedTuple<String, Integer>( "Dog", -12 ) );
 		$animal_list.add( new OrderedTuple<String, Integer>( null, -13 ) );
 		
 		$animal_list.add( new OrderedTuple<String, Integer>( "Cat", 78 ) );
 		$animal_list.add( new OrderedTuple<String, Integer>( null, 0 ) );
 		
 		$animal_list.add( new OrderedTuple<String, Integer>( "cat", 77 ) );
 		$animal_list.add( new OrderedTuple<String, Integer>( "cat", -12 ) );
 		$animal_list.add( new OrderedTuple<String, Integer>( "cat", -11 ) );
 		$animal_list.add( new OrderedTuple<String, Integer>( "cat", null ) );
 		$animal_list.add( new OrderedTuple<String, Integer>( null, null ) );
 		
 		$animal_list.add( new OrderedTuple<String, Integer>( "Bunny", 12 ) );
 		$animal_list.add( new OrderedTuple<String, Integer>( "Bunny", -2 ) );
 		$animal_list.add( new OrderedTuple<String, Integer>( "Bunny", 100 ) );
 		$animal_list.add( new OrderedTuple<String, Integer>( "Bunny", null ) );
 		$animal_list.add( new OrderedTuple<String, Integer>( null, -13 ) );
 		
 		// Now, manually sort it.
 		$ordered_animal_list.add( new OrderedTuple<String, Integer>( null, null ) );
 		$ordered_animal_list.add( new OrderedTuple<String, Integer>( null, -13 ) );
 		$ordered_animal_list.add( new OrderedTuple<String, Integer>( null, -13 ) );
 		$ordered_animal_list.add( new OrderedTuple<String, Integer>( null, 0 ) );
 		
 		$ordered_animal_list.add( new OrderedTuple<String, Integer>( "Bunny", null ) );
 		$ordered_animal_list.add( new OrderedTuple<String, Integer>( "Bunny", -2 ) );
 		$ordered_animal_list.add( new OrderedTuple<String, Integer>( "Bunny", 12 ) );
 		$ordered_animal_list.add( new OrderedTuple<String, Integer>( "Bunny", 100 ) );
 		
 		$ordered_animal_list.add( new OrderedTuple<String, Integer>( "Cat", 78 ) );
 		
 		$ordered_animal_list.add( new OrderedTuple<String, Integer>( "Dog", -12 ) );
 		$ordered_animal_list.add( new OrderedTuple<String, Integer>( "Dog", 3 ) );
 		$ordered_animal_list.add( new OrderedTuple<String, Integer>( "Dog", 5 ) );
 		
 		$ordered_animal_list.add( new OrderedTuple<String, Integer>( "cat", null ) );
 		$ordered_animal_list.add( new OrderedTuple<String, Integer>( "cat", -12 ) );
 		$ordered_animal_list.add( new OrderedTuple<String, Integer>( "cat", -11 ) );
 		$ordered_animal_list.add( new OrderedTuple<String, Integer>( "cat", 77 ) );
 		
 		
 	}
 	
 	@Test
 	public void test_comparisons() {
 		
 		IOrderedTuple<String, Integer> cat1a = new OrderedTuple<String, Integer>( "Cat", 5 );
 		IOrderedTuple<String, Integer> cat1b = new OrderedTuple<String, Integer>( "Cat", 5 );
 		
 		IOrderedTuple<String, Integer> cat2 = new OrderedTuple<String, Integer>( "Cat", 4 );
 		IOrderedTuple<String, Integer> cat3 = new OrderedTuple<String, Integer>( "Dog", 5 );
 		IOrderedTuple<String, Integer> cat4 = new OrderedTuple<String, Integer>( "Dog", 7 );
 		
 		IOrderedTuple<String, Integer> cat5 = new OrderedTuple<String, Integer>( null, 5 );
 		IOrderedTuple<String, Integer> cat6 = new OrderedTuple<String, Integer>( "Cat", null );
 		IOrderedTuple<String, Integer> cat7 = new OrderedTuple<String, Integer>( null, null );
 		
 		// Comparison should be 0 on these two equal tuples.
 		assertEquals( 0, cat1a.compareTo( cat1b ) );
 		assertEquals( 0, cat1b.compareTo( cat1a ) );
 		
 		// Now try various tuples that are NOT equal.
 		
 		// (Cat, 5) comes after (Cat, 4)
 		assertTrue( cat1a.compareTo( cat2 ) > 0 );
 		assertTrue( cat2.compareTo( cat1a ) < 0 );
 		
 		// (Cat, 5) comes before (Dog, 5)
 		assertTrue( cat1a.compareTo( cat3 ) < 0 );
 		assertTrue( cat3.compareTo( cat1a ) > 0 );
 		
 		// (Cat, 5) comes before (Dog, 7)
 		assertTrue( cat1a.compareTo( cat4 ) < 0 );
 		assertTrue( cat4.compareTo( cat1a ) > 0 );
 		
 		// (Cat, 5) comes AFTER (null, 5)
 		assertTrue( cat1a.compareTo( cat5 ) > 0 );
 		assertTrue( cat5.compareTo( cat1a ) < 0 );
 		
 		// (Cat, 5) comes AFTER (Cat, null)
 		assertTrue( cat1a.compareTo( cat6 ) > 0 );
 		assertTrue( cat6.compareTo( cat1a ) < 0 );
 		
 		// (Cat, 5) comes AFTER (null, null)
 		assertTrue( cat1a.compareTo( cat7 ) > 0 );
 		assertTrue( cat7.compareTo( cat1a ) < 0 );
 		
 		// null < everything
 		assertTrue( cat1a.compareTo( null ) > 0 );
 	}
 	
 	@Test
 	public void test_sorting() {
 		
 		Collections.sort( $animal_list );
 		
 		// Ensure that everything matches up.
 		for( int animal_index = 0; animal_index < $animal_list.size(); animal_index++ ) {
 			
 			IOrderedTuple<String, Integer> sorted_list_item = $animal_list.get( animal_index );
 			IOrderedTuple<String, Integer> reference_list_item = $ordered_animal_list.get( animal_index );
 			assertEquals( reference_list_item, sorted_list_item );
 		}
 	}
 
 }
