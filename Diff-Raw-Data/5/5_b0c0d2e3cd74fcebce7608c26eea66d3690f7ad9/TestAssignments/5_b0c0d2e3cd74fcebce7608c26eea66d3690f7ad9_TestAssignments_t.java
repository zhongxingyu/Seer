 package net.maltera.daranable.edinet;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.awt.Color;
 import java.util.Calendar;
 import java.util.Date;
 
 import net.maltera.daranable.edinet.model.Assignment;
 import net.maltera.daranable.edinet.model.Course;
 import net.maltera.daranable.edinet.model.Database;
 import net.maltera.daranable.edinet.model.Repository;
 import net.maltera.daranable.edinet.model.Term;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TestAssignments {
 	private static DatabaseHelper dbHelper;
 	
 	@BeforeClass
 	public static void setupClass() {
 		dbHelper = new DatabaseHelper();
 	}
 	
 	@Before
 	public void setup() {
 		dbHelper.prepare();
 	}
 	
 	@After
 	public void cleanup() {
 		dbHelper.cleanup();
 	}
 	
 	@Test
 	public void testNewAssignment() 
 	throws Exception {
 		Repository repo = new Repository();
 		Database database = repo.getDatabase();
 		Date startDate = new Date();
 		
 		// Create the new Term to use with my course.
 		Term term = Term.create( repo );
 		
 		// Set it's default values
 		term.setYear( 2012 );
 		term.setName( "Fall" );
 		term.setStartDate( startDate );
 		
 		// Push its values to the db
 		term.commit();
 		
 		Color color = new Color( 0, 255, 0 );
 		
 		// Create a new course to put the assignment for.
 		Course course = Course.create( repo );
 		
 		course.setTermReference( term );
 		course.setTeacher( "Ceto" );
 		course.setName( "Underwater Basket Weaving 101" );
 		course.setAbbreviation( "UBW-101" );
 		course.setColor( color );
 		course.setNotes( "Basic information on making baskets under water." );
 		
 		course.commit();
 		
 		Assignment assign = Assignment.create( repo );
 		
 		Calendar calendar = Calendar.getInstance();
 		calendar.set( 2012, 10, 31 );
 		
 		assign.setCourse( course );
 		assign.setTitle( "Read Chapter 1: Know your Seaweed" );
 		assign.setTimeEstimate( 60 );
 		
 		assign.commit();
 		
 		int assign_id = assign.getId();
 		
 		assign = null;
 		
 		assign = repo.getAssignment( assign_id );
 		
 		assertTrue( "course is not the same", 
 				assign.getCourse().equals( course ) );
 		assertEquals( "title", 
 				"Read Chapter 1: Know your Seaweed", assign.getTitle() );
 		assertTrue( "time estimate is not the same", 
 				assign.getTimeEstimate() == 60 );
 		
 		database.shutdown();
 	}
 	
 	@Test
 	public void testEditAssignment() 
 	throws Exception {
 		Repository repo = new Repository();
 		Database database = repo.getDatabase();
 		Date startDate = new Date();
 		
 		// Create the new Term to use with my course.
 		Term term = Term.create( repo );
 		
 		// Set it's default values
 		term.setYear( 2012 );
 		term.setName( "Fall" );
 		term.setStartDate( startDate );
 		
 		// Push its values to the db
 		term.commit();
 		
 		Color color = new Color( 0, 255, 0 );
 		
 		// Create a new course to put the assignment for.
 		Course course = Course.create( repo );
 		
 		course.setTermReference( term );
 		course.setTeacher( "Ceto" );
 		course.setName( "Underwater Basket Weaving 101" );
 		course.setAbbreviation( "UBW-101" );
 		course.setColor( color );
 		course.setNotes( "Basic information on making baskets under water." );
 		
 		course.commit();
 		
 		Assignment assign = Assignment.create( repo );
 		
 		Calendar calendar = Calendar.getInstance();
 		calendar.set( 2012, 10, 31 );
 		
 		assign.setCourse( course );
 		assign.setTitle( "Read Chapter 1: Know your Seaweed" );
 		assign.setTimeEstimate( 60 );
 		
 		assign.commit();
 		
 		int assign_id = assign.getId();
 		
 		assign.setTitle( "Read Chapter 2: Strengthening your Seaweed" );
 		assign.setDescription( "Chapter covers going over different weaves " +
 				"and much more." );
 		
 		assign.commit();
 		
 		assign = null;
 		
 		assign = repo.getAssignment( assign_id );
 		
 		assertTrue( "course is not the same", 
 				assign.getCourse().equals( course ) );
 		assertEquals( "title", 
 				"Read Chapter 2: Strengthening your Seaweed", 
 				assign.getTitle() );
 		assertTrue( "time estimate is not the same", 
 				assign.getTimeEstimate() == 60 );
 		assertEquals( "Chapter covers going over different weaves " +
 				"and much more.",
 				assign.getDescription() );
 		
 		database.shutdown();
 	}
 }
