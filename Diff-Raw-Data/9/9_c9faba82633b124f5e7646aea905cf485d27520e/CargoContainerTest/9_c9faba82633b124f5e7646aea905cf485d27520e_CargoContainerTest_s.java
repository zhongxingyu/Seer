 /*
    
 This file is part of opensearch.
 Copyright © 2009, Dansk Bibliotekscenter a/s, 
 Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
 opensearch is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 opensearch is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 /**
  * \file
  * \brief UnitTest for CargoContainer
  **/
 
 package dk.dbc.opensearch.common.types;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.junit.*;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertNull;
 
 
 /**
  *
  */
 public class CargoContainerTest
 {
 
     Logger log = Logger.getLogger("CargoContainerTest");
 
     CargoContainer cargo;
     String format;
     String language;
     String mimetype;
     String submitter;
     byte[] data;
     DataStreamType dsn;
     String teststring;
 
 
     @Before
     public void SetUp() throws UnsupportedEncodingException
     {
         dsn = DataStreamType.getDataStreamTypeFrom( "originalData" );
         format = "forfatterweb";
         language = "DA";
         mimetype = "text/xml";
         submitter = "DBC";
 
         teststring = "æøå";
         data = teststring.getBytes( "UTF-8" );
         //data = new ByteArrayInputStream( teststring.getBytes( "UTF-8" ) );
         cargo = new CargoContainer();
     }
 
 
     /**
      * Tests the basic add functionality of the CargoContainer. This
      * test reflects a rosy scenario and if this fails, all other
      * tests in this suite should also fail.
      * 
      * @throws IOException
      */
     @Test
     public void testAdd() throws IOException
     {
         //CargoContainer cargo = new CargoContainer();
         assertEquals( 0, cargo.getCargoObjectCount() );
         long id = cargo.add( dsn, format, submitter, language, mimetype, IndexingAlias.Article, data );
         assertTrue( !( id == 0 ) );
         assertEquals( 1, cargo.getCargoObjectCount() );
     }
 
 
     /**
      * 
      * This test serves two purposes:
      * 1: test that the length of the byte array that is added matches
      *    the length of the byte array represented in the CargoObject
      * 2: test that utf-8 gets represented correctly in that we are
      *    dependant on the conversion from String to byte array.
      *
      * @throws IOException
      */
     @Test 
     public void testStreamSizeInContainer() throws IOException
     {
 
         cargo.add( dsn, format, submitter, language, mimetype, IndexingAlias.Article, data );
 
         //UTF-8 uses two bytes per Danish letter
         int expectedLength = teststring.getBytes().length;
 
         ArrayList< CargoObject > list = (ArrayList<CargoObject>)cargo.getCargoObjects();
 
         // get the length if the data contained in the CargoObject
         int contentLength = list.get( 0 ).getContentLength();
 
         assertTrue( expectedLength == contentLength );
     }
 
 
     /** 
      * It should not be permitted to add empty data streams to the
      * CargoContainer. This test ensures that it will not be the case
      * 
      * @throws IOException
      */
     @Test(expected = IllegalArgumentException.class)
     public void testStreamCannotBeEmpty() throws IOException
     {
 
         byte[] is = new byte[0];
         CargoContainer cc = new CargoContainer();
 
         //the add method of the CargoContainer should throw a NullPointerException on this call:
         cc.add( dsn, format, submitter, language, mimetype, IndexingAlias.Article, is );
 
         // CargoObject co = cc.getCargoObjects().get( 0 );
         // byte[] list = co.getBytes();
 
         // if( list.length == 0)
         // {
         //     throw new NullPointerException();
         // }
     }
     @Test(expected = IllegalArgumentException.class)
     public void testIllegalArgumentExceptionWithNullDataStreamType() throws IOException
     {
         cargo.add( null, format, submitter, language, mimetype, IndexingAlias.None, data );
 
     }
     @Test(expected = IllegalArgumentException.class)
     public void testIllegalArgumentExceptionWithNullFormat() throws IOException
     {
         cargo.add( dsn, null, submitter, language, mimetype, IndexingAlias.None, data );
 
     }
     @Test(expected = IllegalArgumentException.class)
     public void testIllegalArgumentExceptionWithNullSubmitter() throws IOException
     {
         cargo.add( dsn, format, null, language, mimetype, IndexingAlias.None, data );
 
     }
     @Test(expected = IllegalArgumentException.class)
     public void testIllegalArgumentExceptionWithNullLanguage() throws IOException
     {
         cargo.add( dsn, format, submitter, null, mimetype, IndexingAlias.None, data );
 
     }
     @Test(expected = IllegalArgumentException.class)
     public void testIllegalArgumentExceptionWithNullMimetype() throws IOException
     {
         cargo.add( dsn, format, submitter, language, null, IndexingAlias.None, data );
 
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testIllegalArgumentExceptionWithNullAlias() throws IOException
     {
         cargo.add( dsn, format, submitter, language, mimetype, null, data );
 
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testIllegalArgumentExceptionWithNoData() throws IOException
     {
         cargo.add( dsn, format, submitter, language, mimetype, IndexingAlias.None, "".getBytes() );
 
     }
 
 
     /** 
      * This test will ensure that the conversion between the String
      * representation of utf-8 characters and a byte representation of
      * the same characters will work as expected
      */
     @Test 
     public void testGetByteArrayPreservesUTF8() throws IOException, UnsupportedEncodingException
     {
         cargo.add( dsn, format, submitter, language, mimetype, IndexingAlias.Article, data );
 
         List< CargoObject > aList = cargo.getCargoObjects();
         byte[] listB = aList.get( 0 ).getBytes();
         byte[] sixBytes = new byte[6];
         for( int i = 0; i < listB.length; i++ )
         {
             sixBytes[i] = listB[i];
         }
         assertTrue( teststring.equals( new String( sixBytes, "UTF-8" ) ) );
     }
 
 
     @Test
     public void testItemsCount() throws IOException
     {
         CargoContainer cc = new CargoContainer();
 
         String str1 = "abc";
         byte[] data1 = str1.getBytes();
 
         String str2 = "abc";
         byte[] data2 = str2.getBytes();
 
         String str3 = "abc";
         byte[] data3 = str3.getBytes();
 
         String str4 = "abc";
         byte[] data4 = str4.getBytes();
 
         cc.add( dsn, format, submitter, language, mimetype, IndexingAlias.Article, data1);
         cc.add( dsn, format, submitter, language, mimetype, IndexingAlias.Article, data2);
         cc.add( dsn, format, submitter, language, mimetype, IndexingAlias.Article, data3);
         cc.add( dsn, format, submitter, language, mimetype, IndexingAlias.Article, data4);
 
         int expectedCount = 4;
         int actualCount = cc.getCargoObjectCount();
 
         assertEquals( expectedCount, actualCount );
         
     }
 
 
     @Test 
     public void testIdenticalIdsForIdenticalInput() throws IOException
     {
 
         String str1 = "abc";
         byte[] data1 = str1.getBytes();
 
         // String str2 = "abc";
         // byte[] data2 = str2.getBytes();
 
         String str3 = "cba";
         byte[] data3 = str3.getBytes();
 
         String str4 = "abc";
         byte[] data4 = str4.getBytes();
 
         long id1 = cargo.add( dsn, format, submitter, language, mimetype, IndexingAlias.Article, data1);
         long id2 = cargo.add( dsn, format, submitter, language, mimetype, IndexingAlias.Article, data3);
         long id3 = cargo.add( dsn, format, submitter, language, mimetype, IndexingAlias.Article, data4);
         long id4 = cargo.add( dsn, format, submitter, language, mimetype, IndexingAlias.Article, data1);
 
 
         log.debug( String.format( "ids: %s, %s, %s, %s",id1, id2, id3, id4 ) );
         
         assertTrue( id1 != id2 );
         assertTrue( id1 != id3 );
         assertTrue( id1 == id4 );
         assertTrue( id2 != id3 );
         assertTrue( id2 != id4 );
         assertTrue( id3 != id4 );
       
     }
 
     /** 
      * Tests the getCargoObject( DataStreamType ) function of the
      * CargoContainer. Even though two identical datastreams are laid
      * into the same CargoContainer, the getCargoObject will get the
      * correct one based on the DataStreamType.
      */
     @Test
     public void testLookupCargoObjectWithDataStreamType() throws IOException
     { 
         CargoObject co1;
         CargoObject co2;
 
         DataStreamType dst1 = DataStreamType.getDataStreamTypeFrom( "originalData" );
         DataStreamType dst2 = DataStreamType.getDataStreamTypeFrom( "indexableData" );
 
         byte[] data1 = "abc".getBytes();
 
         cargo.add( dst1, format, submitter, language, mimetype, IndexingAlias.Article, data1);
         cargo.add( dst2, format, submitter, language, mimetype, IndexingAlias.Article, data1);
 
         co1 = cargo.getCargoObject( dst2 );
         co2 = cargo.getCargoObject( dst2 );
         assertTrue( Arrays.equals( co1.getBytes(), co2.getBytes() ) );
     }
 
 
     /** 
      * This test tests the expectations of a null return value
      * if the client tries to lookup a nonexisting datastream
      * from a cargocontainer
      */
     @Test
     public void testNullWithLookupOnNonExistingCargoObject(){
         DataStreamType dst3 = DataStreamType.getDataStreamTypeFrom( "adminData" );
         assertNull( cargo.getCargoObject( dst3 ) );
     }
 
 
     /** 
      * Illustrates the API documented 'feature' that given more than
      * one CargoObject with the same DataStreamType, getCargoObject(
      * DataStreamType ) will return the first one encountered. Which
      * should be the last one added.
      */
     @Test
     public void testLookupCargoObjectWithDataStreamTypeReturnsFirst() throws IOException
     { 
         CargoObject co;
         DataStreamType dst1 = DataStreamType.OriginalData;
         DataStreamType dst2 = DataStreamType.OriginalData;
         String str1 = "abc";
         byte[] data1 = str1.getBytes();
         String str2 = "abc";
         byte[] data2 = str2.getBytes();
 
         long id1 = cargo.add( dst1, format, submitter, language, mimetype, IndexingAlias.Article, data1);
         long id2 = cargo.add( dst2, format, submitter, language, mimetype, IndexingAlias.Article, data2);
 
         co = cargo.getCargoObject( dst2 );
 
         log.debug( String.format( "id1 == %s, id2 == %s, co.getId() == %s", id1, id2, co.getId() ) );
         assertTrue( id2 == co.getId() );
         assertTrue( id1 != co.getId() );
 
     }
 
 
     @Test 
     public void testUniqueIdOfCargoObjects() throws IOException
     {
         long id1 = cargo.add( DataStreamType.OriginalData, format, submitter, language, mimetype, IndexingAlias.Article, "abc".getBytes() );
         long id2 = cargo.add( DataStreamType.OriginalData, format, submitter, language, mimetype, IndexingAlias.Article, "abc".getBytes() );
 
         assertTrue( !( id1 == id2 ) );
     }
 
     @Test
     public void testRemoveCargoObject() throws IOException
     {
         long id = cargo.add( DataStreamType.OriginalData, 
                               format, 
                               submitter, 
                               language, 
                               mimetype, 
                               IndexingAlias.Article, 
                               "abc".getBytes() );
         
         assertTrue( cargo.remove( id ) );
     }
 
     @Test
     public void testTryRemoveNonexistingId() throws IOException
     {
         long id = cargo.add( DataStreamType.OriginalData, 
                               format, 
                               submitter, 
                               language, 
                               mimetype, 
                               IndexingAlias.Article, 
                               "abc".getBytes() );
         id++;
         assertTrue( ! cargo.remove( id ) );
     }
 
     @Test
     public void testTryRemoveNonexistingCargoObject() throws IOException
     {
         long id = cargo.add( DataStreamType.OriginalData, 
                               format, 
                               submitter, 
                               language, 
                               mimetype, 
                               IndexingAlias.Article, 
                               "abc".getBytes() );
         
         assertTrue( cargo.remove( id ) );
         assertTrue( ! cargo.remove( id ) );
     }
 
 }
