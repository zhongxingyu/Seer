 package net.sf.yal10n.analyzer;
 
 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Unit test for {@link SimpleEncodingDetector}.
  */
 public class SimpleEncodingDetectorTest
 {
 
     SimpleEncodingDetector detector = new SimpleEncodingDetector();
 
     /**
      * Test to detect UTF8_BOM.
      * @throws Exception any error
      */
     @Test
     public void testUTF8WithBOM() throws Exception
     {
         File f = prepareFile( true, "This is a string with umlauts: äöüß encoded as UTF-8 with BOM", "UTF-8" );
         EncodingResult encodingResult = detector.detectEncoding( f );
         Assert.assertEquals( Encoding.UTF8_BOM, encodingResult.getDetected() );
         Assert.assertNull( encodingResult.getError() );
     }
 
     /**
      * Test to detect plain UTF-8.
      * @throws Exception any error
      */
     @Test
     public void testUTF8() throws Exception
     {
         File f = prepareFile( false, "This is a string with umlauts: äöüß encoded as UTF-8 without BOM", "UTF-8" );
         EncodingResult encodingResult = detector.detectEncoding( f );
         Assert.assertEquals( Encoding.UTF8, encodingResult.getDetected() );
         Assert.assertNull( encodingResult.getError() );
     }
 
     /**
      * Test to detect other encoding.
      * @throws Exception any error
      */
     @Test
     public void testOther() throws Exception
     {
         File f = prepareFile( false, "This is a string with umlauts: äöüß encoded as latin-1", "ISO-8859-1" );
         EncodingResult encodingResult = detector.detectEncoding( f );
         Assert.assertEquals( Encoding.OTHER, encodingResult.getDetected() );
         Assert.assertTrue ( encodingResult.getError().isMalformed() );
         final int expectedErrorPosition = 31;
         Assert.assertEquals( expectedErrorPosition, encodingResult.getErrorPosition() );
     }
 
     /**
      * Tests a very short file - the file is shorted than the BOM.
      * @throws Exception any error
      */
     @Test
     public void testVeryShortFile() throws Exception
     {
         File f = prepareFile( false, "a", "ISO-8859-1" );
         EncodingResult encodingResult = detector.detectEncoding( f );
         // note: will detect as UTF-8, as for this 1 byte, it's the same
         Assert.assertEquals( Encoding.UTF8, encodingResult.getDetected() );
         Assert.assertNull( encodingResult.getError() );
     }
 
     /**
      * Tests a file that seems to start with a BOM.
      * @throws Exception any error
      */
     @Test
     public void testMalformedBOM() throws Exception
     {
         File f = prepareFile( new byte[] { SimpleEncodingDetector.UTF8_BOM_BYTES[0], 'a', 'b', 'c' } );
         EncodingResult encodingResult = detector.detectEncoding( f );
         Assert.assertEquals( Encoding.OTHER, encodingResult.getDetected() );
         Assert.assertTrue( encodingResult.getError().isMalformed() );
         Assert.assertEquals( 1, encodingResult.getError().length() );
         Assert.assertEquals( 0, encodingResult.getErrorPosition() );
 
         f = prepareFile( new byte[] { SimpleEncodingDetector.UTF8_BOM_BYTES[0],
                 SimpleEncodingDetector.UTF8_BOM_BYTES[1], 'a', 'b', 'c' } );
         encodingResult = detector.detectEncoding( f );
         Assert.assertEquals( Encoding.OTHER, encodingResult.getDetected() );
         Assert.assertTrue( encodingResult.getError().isMalformed() );
         Assert.assertEquals( 2, encodingResult.getError().length() );
         Assert.assertEquals( 0, encodingResult.getErrorPosition() );
     }
 
     /**
      * Expects an exception if the file doesn't exist.
      * @throws Exception any error
      */
     @Test( expected = RuntimeException.class )
     public void testFileNotFound() throws Exception
     {
         detector.detectEncoding( new File( "some file that does not exist" ) );
     }
 
    /**
     * Empty files should be detected as UTF-8.
     * @throws Exception any error
     */
    @Test
    public void testEmptyFile() throws Exception
    {
        File f = prepareFile( false, "", "UTF-8" );
        EncodingResult result = detector.detectEncoding( f );
        Assert.assertEquals( Encoding.UTF8, result.getDetected() );
    }

     private File prepareFile( boolean withBOM, String text, String encoding ) throws IOException
     {
         File f = File.createTempFile( "yal10n", null );
         f.deleteOnExit();
 
         FileOutputStream out = new FileOutputStream( f );
         if ( withBOM )
         {
             out.write( SimpleEncodingDetector.UTF8_BOM_BYTES );
         }
         out.write( text.getBytes( encoding ) );
         out.close();
         return f;
     }
 
     private File prepareFile( byte[] bytes ) throws IOException
     {
         File f = File.createTempFile( "yal10n", null );
         f.deleteOnExit();
 
         FileOutputStream out = new FileOutputStream( f );
         out.write( bytes );
         out.close();
         return f;
     }
 }
