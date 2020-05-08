 /*
  * Created on 08/10/2012
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 
 package com.antiaction.common.json;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PushbackInputStream;
import java.nio.charset.Charset;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 @RunWith(JUnit4.class)
 public class TestJSONEncoding {
 
 	@Test
 	public void test_jsonencoding() {
 		Object[][] cases = new Object[][] {
 				// 0
 				{new byte[] {}, JSONEncoding.E_UNKNOWN, 0},
 				// 1
 				{new byte[] {(byte)0x00}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0xFE}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0xFF}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0xEF}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0xBB}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0xBF}, JSONEncoding.E_UNKNOWN, 0},
 				// 2
 				{new byte[] {(byte)0xFE, (byte)0xFF}, JSONEncoding.E_UTF16BE, 2},
 				{new byte[] {(byte)0xFF, (byte)0xFE}, JSONEncoding.E_UTF16LE, 2},
 				{new byte[] {(byte)0xFE, (byte)']'}, JSONEncoding.E_UTF8, 0},
 				{new byte[] {(byte)0xFF, (byte)']'}, JSONEncoding.E_UTF8, 0},
 				{new byte[] {(byte)'[', (byte)0xFF}, JSONEncoding.E_UTF8, 0},
 				{new byte[] {(byte)'[', (byte)0xFE}, JSONEncoding.E_UTF8, 0},
 				{new byte[] {(byte)0xFE, (byte)0x00}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0xFF, (byte)0x00}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0x00, (byte)0xFF}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0x00, (byte)0xFE}, JSONEncoding.E_UNKNOWN, 0},
 				// 3
 				{new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF}, JSONEncoding.E_UTF8, 3},
 				{new byte[] {(byte)0xEF, (byte)0xBF, (byte)0xBB}, JSONEncoding.E_UTF8, 0},
 				{new byte[] {(byte)0xEF, (byte)0xBB, (byte)']'}, JSONEncoding.E_UTF8, 0},
 				{new byte[] {(byte)0xEF, (byte)'[', (byte)']'}, JSONEncoding.E_UTF8, 0},
 				{new byte[] {(byte)0xBB, (byte)0xEF, (byte)0xBF}, JSONEncoding.E_UTF8, 0},
 				{new byte[] {(byte)0x00, (byte)0xBB, (byte)0xBF}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0xEF, (byte)0x00, (byte)0xBF}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0xEF, (byte)0xBB, (byte)0x00}, JSONEncoding.E_UNKNOWN, 0},
 				// 4
 				{new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF, (byte)'['}, JSONEncoding.E_UTF8, 3},
 				{new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF, (byte)0x00}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0xEF, (byte)0xBB, (byte)']', (byte)'{'}, JSONEncoding.E_UTF8, 0},
 				{new byte[] {(byte)0xEF, (byte)'[', (byte)']', (byte)'}'}, JSONEncoding.E_UTF8, 0},
 				{new byte[] {(byte)0xBB, (byte)0xEF, (byte)0xBF, (byte)'['}, JSONEncoding.E_UTF8, 0},
 				{new byte[] {(byte)0x00, (byte)0xBB, (byte)0xBF, (byte)'['}, JSONEncoding.E_UNKNOWN, 0},
 				
 				{new byte[] {(byte)0x00, (byte)0x00, (byte)0xFE, (byte)0xFF}, JSONEncoding.E_UTF32BE, 4},
 				{new byte[] {(byte)0xFF, (byte)0xFE, (byte)0x00, (byte)0x00}, JSONEncoding.E_UTF32LE, 4},
 				{new byte[] {(byte)0x00, (byte)0x00, (byte)0xFE, (byte)0xFE}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0x00, (byte)0x00, (byte)0xFF, (byte)0xFE}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0xFE, (byte)0xFF, (byte)0x00, (byte)0x00}, JSONEncoding.E_UNKNOWN, 0},
 				
 				{new byte[] {(byte)0xFE, (byte)0xFF, (byte)0x00, (byte)'['}, JSONEncoding.E_UTF16BE, 2},
 				{new byte[] {(byte)0xFF, (byte)0xFE, (byte)'[', (byte)0x00}, JSONEncoding.E_UTF16LE, 2},
 				{new byte[] {(byte)0xFE, (byte)0xFE, (byte)0x00, (byte)'['}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0xFF, (byte)0xFF, (byte)'[', (byte)0x00}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0xFF, (byte)0xFF, (byte)0x00, (byte)'['}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0xFE, (byte)0xFE, (byte)'[', (byte)0x00}, JSONEncoding.E_UNKNOWN, 0},
 
 				{new byte[] {(byte)0x00, (byte)'[', (byte)0x00, (byte)']'}, JSONEncoding.E_UTF16BE, 0},
 				{new byte[] {(byte)'[', (byte)0x00, (byte)']', (byte)0x00}, JSONEncoding.E_UTF16LE, 0},
 				{new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)'['}, JSONEncoding.E_UTF32BE, 0},
 				{new byte[] {(byte)']', (byte)0x00, (byte)0x00, (byte)0x00}, JSONEncoding.E_UTF32LE, 0},
 
 				{new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0x00, (byte)'[', (byte)0x00, (byte)0x00}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0x00, (byte)0x00, (byte)']', (byte)0x00}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)0x00, (byte)'[', (byte)']', (byte)0x00}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)'[', (byte)0x00, (byte)0x00, (byte)']'}, JSONEncoding.E_UNKNOWN, 0},
 				{new byte[] {(byte)'[', (byte)0x00, (byte)'[', (byte)']'}, JSONEncoding.E_UNKNOWN, 0},
 		};
 
 		ByteArrayInputStream in;
 		PushbackInputStream pbin;
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		int encoding;
 		int read;
 		byte[] buffer = new byte[ 256 ];
 		try {
 			for ( int i=0; i<cases.length; ++i ) {
 				out.reset();
 				byte[] bytes = (byte[])cases[ i ][ 0 ];
 				int expected_encoding = (Integer)cases[ i ][ 1 ];
 				int expected_bom = (Integer)cases[ i ][ 2 ];
 				in = new ByteArrayInputStream( bytes );
 				pbin = new PushbackInputStream( in, 4 );
 				// debug
				//System.out.println( i + ": " + new String( bytes ) );
 				encoding = JSONEncoding.encoding( pbin );
 				Assert.assertEquals( expected_encoding, encoding );
 				read = 0;
 				while ( read != -1 ) {
 					out.write( buffer, 0, read );
 					read = pbin.read( buffer );
 				}
				Assert.assertEquals( expected_bom, bytes.length - out.toString( "ISO-8859-1" ).length() );
 			}
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 			Assert.fail( "Unexpected exception!" );
 		}
 	}
 
 	@Test
 	public void test_jsonencoding_decoder() {
 		JSONEncoding json_encoding = JSONEncoding.getJSONEncoding();
 		JSONEncoding json_encoding2 = JSONEncoding.getJSONEncoding();
 		Assert.assertEquals( json_encoding, json_encoding2 );
 
 		JSONDecoder json_decoder;
 		JSONDecoder json_decoder2;
 
 		json_decoder = json_encoding.getJSONDecoder( JSONEncoding.E_UTF8 );
 		json_decoder2 = json_encoding.getJSONDecoder( JSONEncoding.E_UTF8 );
 		Assert.assertEquals( json_decoder, json_decoder2 );
 
 		json_decoder = json_encoding.getJSONDecoder( JSONEncoding.E_UTF16BE );
 		json_decoder2 = json_encoding.getJSONDecoder( JSONEncoding.E_UTF16BE );
 		Assert.assertEquals( json_decoder, json_decoder2 );
 
 		json_decoder = json_encoding.getJSONDecoder( JSONEncoding.E_UTF16LE );
 		json_decoder2 = json_encoding.getJSONDecoder( JSONEncoding.E_UTF16LE );
 		Assert.assertEquals( json_decoder, json_decoder2 );
 
 		try {
 			json_encoding.getJSONDecoder( JSONEncoding.E_UTF32BE );
 			Assert.fail( "Exception expected!" );
 		}
 		catch (IllegalArgumentException e) {
 		}
 		try {
 			json_encoding.getJSONDecoder( JSONEncoding.E_UTF32LE );
 			Assert.fail( "Exception expected!" );
 		}
 		catch (IllegalArgumentException e) {
 		}
 		try {
 			json_encoding.getJSONDecoder( JSONEncoding.E_UNKNOWN );
 			Assert.fail( "Exception expected!" );
 		}
 		catch (IllegalArgumentException e) {
 		}
 		try {
 			json_encoding.getJSONDecoder( 42 );
 			Assert.fail( "Exception expected!" );
 		}
 		catch (IllegalArgumentException e) {
 		}
 	}
 
 	@Test
 	public void test_jsondecoding_encoder() {
 		JSONEncoding json_encoding = JSONEncoding.getJSONEncoding();
 		JSONEncoding json_encoding2 = JSONEncoding.getJSONEncoding();
 		Assert.assertEquals( json_encoding, json_encoding2 );
 
 		JSONEncoder json_encoder;
 		JSONEncoder json_encoder2;
 
 		json_encoder = json_encoding.getJSONEncoder( JSONEncoding.E_UTF8 );
 		json_encoder2 = json_encoding.getJSONEncoder( JSONEncoding.E_UTF8 );
 		Assert.assertEquals( json_encoder, json_encoder2 );
 
 		json_encoder = json_encoding.getJSONEncoder( JSONEncoding.E_UTF16BE );
 		json_encoder2 = json_encoding.getJSONEncoder( JSONEncoding.E_UTF16BE );
 		Assert.assertEquals( json_encoder, json_encoder2 );
 
 		json_encoder = json_encoding.getJSONEncoder( JSONEncoding.E_UTF16LE );
 		json_encoder2 = json_encoding.getJSONEncoder( JSONEncoding.E_UTF16LE );
 		Assert.assertEquals( json_encoder, json_encoder2 );
 
 		try {
 			json_encoding.getJSONEncoder( JSONEncoding.E_UTF32BE );
 			Assert.fail( "Exception expected!" );
 		}
 		catch (IllegalArgumentException e) {
 		}
 		try {
 			json_encoding.getJSONEncoder( JSONEncoding.E_UTF32LE );
 			Assert.fail( "Exception expected!" );
 		}
 		catch (IllegalArgumentException e) {
 		}
 		try {
 			json_encoding.getJSONEncoder( JSONEncoding.E_UNKNOWN );
 			Assert.fail( "Exception expected!" );
 		}
 		catch (IllegalArgumentException e) {
 		}
 		try {
 			json_encoding.getJSONEncoder( 42 );
 			Assert.fail( "Exception expected!" );
 		}
 		catch (IllegalArgumentException e) {
 		}
 	}
 
 }
