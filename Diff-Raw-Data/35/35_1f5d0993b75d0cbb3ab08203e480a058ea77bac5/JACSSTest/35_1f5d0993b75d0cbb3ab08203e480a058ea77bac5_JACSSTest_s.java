 package com.wickedspiral.jacss;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.google.common.io.Files;
 import org.testng.Assert;
 import org.testng.annotations.*;
 
 /**
  * @author wasche
  * @since 9/30/13
  */
 public class JACSSTest
 {
     private static final String TEST_DIR = "tests";
     private static final String EXT_RESULT = ".min";
     public static final Charset UTF8 = Charset.forName("utf-8");
     private static final FilenameFilter FILE_CSS = new FilenameFilter()
     {
         @Override
         public boolean accept(File dir, String name)
         {
             return name.endsWith( ".css" );
         }
     };
 
     private static List<Object[]> tests;
 
     @BeforeClass
     public void init() throws IOException
     {
         tests = new LinkedList<>();
 
         File dir = new File( TEST_DIR );
         File resultFile;
         String test, result;
         for (File file : dir.listFiles( FILE_CSS ))
         {
             resultFile = new File( dir, file.getName() + EXT_RESULT );
             if ( !resultFile.exists() )
             {
                 throw new IllegalStateException( "Could not find result for test: " + file.getName() );
             }
             test = Files.toString( file, UTF8 );
             if ( test == null || test.length() == 0 )
             {
                 throw new IllegalStateException( "Test input is empty: " + file.getName() );
             }
             result = Files.toString( resultFile, UTF8 );
             if ( result == null || result.length() == 0 )
             {
                 throw new IllegalStateException( "Test result input is empty: " + resultFile.getName() );
             }
             tests.add( new Object[]{ file.getName(), test, result } );
         }
     }
 
     @DataProvider( name="files" )
     public Iterator<Object[]> getFiles()
     {
         return tests.iterator();
     }
     
     @Test( dataProvider="files" )
     public void testAll( String name, String source, String expected ) throws FileNotFoundException
     {
         ByteArrayInputStream in = new ByteArrayInputStream( source.getBytes( UTF8 ) );
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         Options options = new Options();
         if (name.startsWith("yui-"))
         {
             options.yui242 = true;
             options.imply();
         }
         new JACSS( in, out, options ).run();
         Assert.assertEquals( out.toString(), expected, name );
     }
 }
