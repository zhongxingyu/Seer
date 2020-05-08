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
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.gengar.util.tuple.IGenericTuple;
 import org.gengar.util.tuple.impl.GenericTuple;
import org.gengar.util.tuple.impl.MatchedTuple;
 import org.junit.Test;
 
 public class TestGenericTuple {
 
 	private List<IGenericTuple<String,Integer>> $stuples;
 	private List<IGenericTuple<Integer,String>> $intuples;
 	
 	public void populate_with_tuples() {
 		
 		$stuples = new ArrayList<IGenericTuple<String,Integer>>();
 		$intuples = new ArrayList<IGenericTuple<Integer,String>>();
 				
 		// Strings
 		$stuples.add( new GenericTuple<String,Integer>( "Cat", 6 ) );
 		$stuples.add( new GenericTuple<String,Integer>( "Dog", -1 ) );
 		$stuples.add( new GenericTuple<String,Integer>( "Bunny", 55 ) );
 		$stuples.add( new GenericTuple<String,Integer>( "Snake", -0 ) );
 		$stuples.add( new GenericTuple<String,Integer>( "Dodecahedron", 124 ) );
 		
 		$stuples.add( new GenericTuple<String,Integer>( "Bunny", -9 ) );
 		$stuples.add( new GenericTuple<String,Integer>( "Dodecahedron", -11 ) );
 		
 		// Integers
 		$intuples.add( new GenericTuple<Integer,String>( 6, "Cat" ) );
 		$intuples.add( new GenericTuple<Integer,String>( -1, "Dog" ) );
 		$intuples.add( new GenericTuple<Integer,String>( 3, "Bunny" ) );
 		$intuples.add( new GenericTuple<Integer,String>( 10, "Snake" ) );
 		$intuples.add( new GenericTuple<Integer,String>( -25, "Dodecahedron" ) );
 		
 		$intuples.add( new GenericTuple<Integer,String>( 3, "Bunny" ) );
 		$intuples.add( new GenericTuple<Integer,String>( -25, "Dodecahedron" ) );
 		
 	}
 	
 	@Test
 	public void test_creation() {
 		populate_with_tuples();
 	}
 	
 	@Test
 	public void test_accesses() {
 		
 		GenericTuple<String,Integer> test1 = new GenericTuple<String,Integer>( "Doggly-Woggly", 66 );
 		
 		assertEquals( "Doggly-Woggly", test1.first() );
 		assertEquals( new Integer( 66 ), test1.second() );
 	}
 	
 	@Test
 	public void test_equality() {
 		
 		GenericTuple<String,Integer> dog1a = new GenericTuple<String,Integer>( "Dog", 6 );
 		GenericTuple<String,Integer> dog1b = new GenericTuple<String,Integer>( "Dog", 6 );
 		
 		// Equals should say they're equal.
 		assertEquals( dog1a, dog1b );
 		assertEquals( dog1b, dog1a );
 		
 		// They should have the same hash codes.
 		assertEquals( dog1a.hashCode(), dog1b.hashCode() );
 	}
 	
 	@Test
 	public void test_inequality() {
 		
 		GenericTuple<String,Integer> dog1a = new GenericTuple<String,Integer>( "Dog", 6 );
 		GenericTuple<String,Integer> dog2 = new GenericTuple<String,Integer>( "dog", 6 );
 		GenericTuple<String,Integer> dog3 = new GenericTuple<String,Integer>( "Dog", 12 );
 		GenericTuple<String,Integer> dog4 = new GenericTuple<String,Integer>( null, -9 );
 		GenericTuple<String,Integer> dog5 = new GenericTuple<String,Integer>( null, null );
 
 		assertThat( dog1a, is( not( dog2 ) ) );
 		assertThat( dog2, is( not( dog1a ) ) );
 		
 		assertThat( dog1a, is( not( dog3 ) ) );
 		assertThat( dog3, is( not( dog1a ) ) );
 		
 		assertThat( dog1a, is( not( dog4 ) ) );
 		assertThat( dog4, is( not( dog1a ) ) );
 		
 		assertThat( dog1a, is( not( dog5 ) ) );
 		assertThat( dog5, is( not( dog1a ) ) );
 	}
 	
 	@SuppressWarnings("rawtypes")
 	@Test
 	public void test_incomparable() {
 		
 		GenericTuple<String,Integer> dog1a = new GenericTuple<String,Integer>( "Dog", 6 );
 		
 		GenericTuple<Integer,String> dog6 = new GenericTuple<Integer,String>( 1, "Doggles" );
 		GenericTuple<Integer,String> dog7 = new GenericTuple<Integer,String>( null, "Kittens" );
 		GenericTuple<Integer,String> dog8 = new GenericTuple<Integer,String>( 100, null );
 		GenericTuple<String,Boolean> dog9 = new GenericTuple<String,Boolean>( "Dog", null );
 		
 		// If someone ignores generics and tries to compare tuples with different data types...
 		assertThat( (GenericTuple)dog1a, is( not( (GenericTuple)dog6 ) ) );
 		assertThat( (GenericTuple)dog6, is( not( (GenericTuple)dog1a ) ) );
 		
 		assertThat( (GenericTuple)dog1a, is( not( (GenericTuple)dog7 ) ) );
 		assertThat( (GenericTuple)dog7, is( not( (GenericTuple)dog1a ) ) );
 		
 		assertThat( (GenericTuple)dog1a, is( not( (GenericTuple)dog8 ) ) );
 		assertThat( (GenericTuple)dog8, is( not( (GenericTuple)dog1a ) ) );
 		
 		assertThat( (GenericTuple)dog1a, is( not( (GenericTuple)dog9 ) ) );
 		assertThat( (GenericTuple)dog9, is( not( (GenericTuple)dog1a ) ) );
 	}
 
 }
