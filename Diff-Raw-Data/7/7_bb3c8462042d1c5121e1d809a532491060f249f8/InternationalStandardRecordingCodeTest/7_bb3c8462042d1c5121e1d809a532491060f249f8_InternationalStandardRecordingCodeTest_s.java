 package de.soulworks.dam.util;
 
 import de.soulworks.dam.webservice.util.InternationalStandardRecordingCode;
 import org.junit.Assert;
 import org.junit.Test;
 import org.springframework.test.annotation.ExpectedException;
 
 import java.util.Locale;
 
 /**
  * @author Christian Schmitz <csc@soulworks.de>
  */
 public class InternationalStandardRecordingCodeTest {
 
 	@Test
 	public void testGetterAndSetter() {
 		InternationalStandardRecordingCode isrc = new InternationalStandardRecordingCode();
 		
 		isrc.setCountry(Locale.GERMANY.getCountry());
 		Assert.assertEquals(Locale.GERMANY.getCountry(), isrc.getCountry());
 
 		isrc.setRegistrant("AAA");
 		Assert.assertEquals("AAA", isrc.getRegistrant());
 
 		isrc.setRegistrant("Aaa");
 		Assert.assertEquals("AAA", isrc.getRegistrant());
 
 		isrc.setYear(3);
 		Assert.assertEquals(3, isrc.getYear());
 
 		isrc.setYear(3);
 		Assert.assertEquals(3, isrc.getYear());
 
 		isrc.setNumber(1);
 		Assert.assertEquals(1, isrc.getNumber());
 	}
 
 	@Test
 	public void testCreateFromString() {
 		InternationalStandardRecordingCode isrc = null;
 
 		isrc = InternationalStandardRecordingCode.createFromString("BRBMG0300729");
 		Assert.assertEquals("BR", isrc.getCountry());
 		Assert.assertEquals("BMG", isrc.getRegistrant());
 		Assert.assertEquals(3, isrc.getYear());
 		Assert.assertEquals(729, isrc.getNumber());
 		Assert.assertEquals("BRBMG0300729", isrc.toString());
 
 		isrc = InternationalStandardRecordingCode.createFromString("USPR37300012");
 		Assert.assertEquals("US", isrc.getCountry());
 		Assert.assertEquals("PR3", isrc.getRegistrant());
 		Assert.assertEquals(73, isrc.getYear());
 		Assert.assertEquals(12, isrc.getNumber());
 		Assert.assertEquals("USPR37300012", isrc.toString());
 	}
 
	@Test
	@ExpectedException(IllegalArgumentException.class)
 	public void testInvalidIsrcWithHyphens() {
 		InternationalStandardRecordingCode isrc = InternationalStandardRecordingCode.createFromString("US-S1Z-73-00012");
 	}
 
	@Test
	@ExpectedException(IllegalArgumentException.class)
 	public void testInvalidRegistrant() {
 		InternationalStandardRecordingCode isrc = InternationalStandardRecordingCode.createFromString("USS1Z7300012");
 	}
 
 }
