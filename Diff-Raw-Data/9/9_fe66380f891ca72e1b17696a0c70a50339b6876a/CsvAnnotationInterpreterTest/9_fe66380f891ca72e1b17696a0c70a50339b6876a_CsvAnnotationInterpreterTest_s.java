 package org.gesis.zl.marshalling.csv;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.List;
 
 import org.gesis.zl.marshalling.annotations.AnnotationInterpreterFactory;
 import org.junit.Before;
 import org.junit.Test;
 
 public class CsvAnnotationInterpreterTest {
 
 	private CsvAnnotationInterpreter<Row> reader;
 
 	@Before
 	public void init()
 	{
 		reader = AnnotationInterpreterFactory.createCsvAnnotationReader( Row.class );
 		assertNotNull( reader );
 	}
 
 	@Test
 	public void testGetInputFields() {
 		List<String> fields = reader.getInputFieldNames();
 		assertNotNull( fields );
 		assertEquals( 2, fields.size() );
 
 		assertEquals( "name", fields.get( 0 ) );
 		assertEquals( "description", fields.get( 1 ) );
 	}
 
 	@Test
 	public void testGetOutputFields()
 	{
 		List<String> fields = reader.getOutputFieldNames();
 		assertNotNull( fields );
		assertEquals( 2, fields.size() );
 
 		assertEquals( "target_name", fields.get( 0 ) );
 		assertEquals( "target_description", fields.get( 1 ) );
 	}
 
 	@Test
 	public void testGetOutputColumnNames()
 	{
 		List<String> fields = reader.getOutputColumnNames();
 		assertNotNull( fields );
		assertEquals( 2, fields.size() );
 
 		assertEquals( "name", fields.get( 0 ) );
 		assertEquals( "description", fields.get( 1 ) );
 	}
 
 	@Test
 	public void testGetPositionOf()
 	{
 		int pos = reader.getPositionOf( "target_name" );
 		assertEquals( 0, pos );
 
 		pos = reader.getPositionOf( "target_description" );
 		assertEquals( 1, pos );
 
 		pos = reader.getPositionOf( "name" );
 		assertEquals( 2, pos );
 
 		pos = reader.getPositionOf( "description" );
 		assertEquals( 3, pos );
 	}
 
 	@Test
 	public void testGetPositionOfNonExistantField()
 	{
 		int pos = reader.getPositionOf( "arbitrary_column_name" );
 		assertEquals( -1, pos );
 
 	}
 
 }
