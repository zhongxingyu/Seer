 package org.meteornetwork.meteor.saml;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.junit.Test;
 import org.meteornetwork.meteor.saml.exception.SecurityTokenException;
 import org.opensaml.xml.ConfigurationException;
 
 public class SecurityTokenImplTest extends TestCase {
 
 	private final Date currentDate;
 
 	SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
 
 	public SecurityTokenImplTest() throws ParseException, ConfigurationException {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy Z", Locale.US);
 		currentDate = dateFormat.parse("April 5, 2063 -0000");
 	}
 
 	@Test
 	public void testToXMLStringFAA() throws SecurityException, NoSuchMethodException, SecurityTokenException, ParseException {
 
 		SecurityTokenImpl token = new SecurityTokenMock();
 
 		token.setAssertionId("12345678");
 		token.setIssuer("meteornetwork.org");
 		token.setSubjectName("LTI_AP");
 		token.setSubjectLocalityIpAddress("1.2.3.4");
 		token.setSubjectLocalityDnsAddress("notarealdomain-false.com");
 		token.setProviderType(ProviderType.ACCESS);
 		token.setOrganizationId("12345");
 		token.setOrganizationIdType("OPEID");
 		token.setOrganizationType("School");
 		token.setAuthenticationProcessId("1");
 		token.setLevel(3);
 		token.setUserHandle("theuser");
 		token.setRole(Role.FAA);
 
 		String assertionXml = token.toXMLString();
 		SecurityTokenImpl result = SecurityTokenImpl.fromXML(assertionXml);
 		Assert.assertEquals("12345678", result.getAssertionId());
 		Assert.assertEquals("meteornetwork.org", result.getIssuer());
 		Assert.assertEquals("LTI_AP", result.getSubjectName());
 		Assert.assertEquals("1.2.3.4", result.getSubjectLocalityIpAddress());
 		Assert.assertEquals("notarealdomain-false.com", result.getSubjectLocalityDnsAddress());
 		Assert.assertEquals(parseDate("2063-04-05T00:00:00.000-0000").getTime(), result.getConditionsNotBefore().getMillis());
		Assert.assertEquals(parseDate("2063-04-05T01:00:00.000-0000").getTime(), result.getConditionsNotOnOrAfter().getMillis());
 		Assert.assertEquals(ProviderType.ACCESS, result.getProviderType());
 		Assert.assertEquals("12345", result.getOrganizationId());
 		Assert.assertEquals("OPEID", result.getOrganizationIdType());
 		Assert.assertEquals("School", result.getOrganizationType());
 		Assert.assertEquals("1", result.getAuthenticationProcessId());
 		Assert.assertEquals(Integer.valueOf(3), result.getLevel());
 		Assert.assertEquals("theuser", result.getUserHandle());
 		Assert.assertEquals(Role.FAA, result.getRole());
 	}
 
 	@Test
 	public void testFromXMLFAA() throws IOException, SecurityTokenException, ParseException {
 		String assertionXml = getXmlFromFile("assertion-faa.xml");
 		SecurityTokenImpl result = SecurityTokenImpl.fromXML(assertionXml);
 		Assert.assertEquals("98287E61FB3575AF5713168076864216", result.getAssertionId());
 		Assert.assertEquals("meteornetwork.org", result.getIssuer());
 		Assert.assertEquals("LTI_AP40", result.getSubjectName());
 		Assert.assertEquals("1.2.3.4", result.getSubjectLocalityIpAddress());
 		Assert.assertEquals("not-a-real-domain-false1.com", result.getSubjectLocalityDnsAddress());
 		Assert.assertEquals(parseDate("2011-09-23T19:54:46.421-0000").getTime(), result.getConditionsNotBefore().getMillis());
 		Assert.assertEquals(parseDate("2011-09-23T23:54:46.421-0000").getTime(), result.getConditionsNotOnOrAfter().getMillis());
 		Assert.assertEquals(ProviderType.ACCESS, result.getProviderType());
 		Assert.assertEquals("12324", result.getOrganizationId());
 		Assert.assertEquals("OPEID", result.getOrganizationIdType());
 		Assert.assertEquals("SCHOOL", result.getOrganizationType());
 		Assert.assertEquals("1", result.getAuthenticationProcessId());
 		Assert.assertEquals(Integer.valueOf(3), result.getLevel());
 		Assert.assertEquals("faa", result.getUserHandle());
 		Assert.assertEquals(Role.FAA, result.getRole());
 	}
 
 	@Test
 	public void testToXMLStringBorrower() throws SecurityException, NoSuchMethodException, SecurityTokenException, ParseException {
 
 		SecurityToken token = new SecurityTokenMock();
 
 		token.setAssertionId("12345678");
 		token.setIssuer("meteornetwork.org");
 		token.setSubjectName("LTI_AP");
 		token.setSubjectLocalityIpAddress("1.2.3.4");
 		token.setSubjectLocalityDnsAddress("notarealdomain-false.com");
 		token.setAuthenticationProcessId("1");
 		token.setLevel(3);
 		token.setUserHandle("theuser");
 		token.setRole(Role.BORROWER);
 		token.setSsn("101011010");
 
 		String assertionXml = token.toXMLString();
 		SecurityToken result = SecurityTokenImpl.fromXML(assertionXml);
 		Assert.assertEquals("12345678", result.getAssertionId());
 		Assert.assertEquals("meteornetwork.org", result.getIssuer());
 		Assert.assertEquals("LTI_AP", result.getSubjectName());
 		Assert.assertEquals("1.2.3.4", result.getSubjectLocalityIpAddress());
 		Assert.assertEquals("notarealdomain-false.com", result.getSubjectLocalityDnsAddress());
 		Assert.assertEquals(parseDate("2063-04-05T00:00:00.000-0000").getTime(), result.getConditionsNotBefore().getMillis());
		Assert.assertEquals(parseDate("2063-04-05T01:00:00.000-0000").getTime(), result.getConditionsNotOnOrAfter().getMillis());
 		Assert.assertEquals("1", result.getAuthenticationProcessId());
 		Assert.assertEquals(Integer.valueOf(3), result.getLevel());
 		Assert.assertEquals("theuser", result.getUserHandle());
 		Assert.assertEquals(Role.BORROWER, result.getRole());
 		Assert.assertEquals("101011010", result.getSsn());
 	}
 
 	@Test
 	public void testFromXMLBorrower() throws IOException, SecurityTokenException, ParseException {
 		String assertionXml = getXmlFromFile("assertion-borrower.xml");
 		SecurityToken result = SecurityTokenImpl.fromXML(assertionXml);
 		Assert.assertEquals("98287E61FB3575AF5713168076864216", result.getAssertionId());
 		Assert.assertEquals("meteornetwork.org", result.getIssuer());
 		Assert.assertEquals("LTI_AP40", result.getSubjectName());
 		Assert.assertEquals("1.2.3.4", result.getSubjectLocalityIpAddress());
 		Assert.assertEquals("not-a-real-domain-false1.com", result.getSubjectLocalityDnsAddress());
 		Assert.assertEquals(parseDate("2011-09-23T19:54:46.421-0000").getTime(), result.getConditionsNotBefore().getMillis());
 		Assert.assertEquals(parseDate("2011-09-23T23:54:46.421-0000").getTime(), result.getConditionsNotOnOrAfter().getMillis());
 		Assert.assertEquals("1", result.getAuthenticationProcessId());
 		Assert.assertEquals(Integer.valueOf(3), result.getLevel());
 		Assert.assertEquals("theuser", result.getUserHandle());
 		Assert.assertEquals(Role.BORROWER, result.getRole());
 		Assert.assertEquals("101011010", result.getSsn());
 	}
 
 	@Test
 	public void testToXMLStringLender() throws SecurityException, NoSuchMethodException, SecurityTokenException, ParseException {
 
 		SecurityToken token = new SecurityTokenMock();
 
 		token.setAssertionId("12345678");
 		token.setIssuer("meteornetwork.org");
 		token.setSubjectName("LTI_AP");
 		token.setSubjectLocalityIpAddress("1.2.3.4");
 		token.setSubjectLocalityDnsAddress("notarealdomain-false.com");
 		token.setAuthenticationProcessId("1");
 		token.setLevel(3);
 		token.setUserHandle("theuser");
 		token.setRole(Role.LENDER);
 		token.setLender("12345");
 
 		String assertionXml = token.toXMLString();
 		SecurityToken result = SecurityTokenImpl.fromXML(assertionXml);
 		Assert.assertEquals("12345678", result.getAssertionId());
 		Assert.assertEquals("meteornetwork.org", result.getIssuer());
 		Assert.assertEquals("LTI_AP", result.getSubjectName());
 		Assert.assertEquals("1.2.3.4", result.getSubjectLocalityIpAddress());
 		Assert.assertEquals("notarealdomain-false.com", result.getSubjectLocalityDnsAddress());
 		Assert.assertEquals(parseDate("2063-04-05T00:00:00.000-0000").getTime(), result.getConditionsNotBefore().getMillis());
		Assert.assertEquals(parseDate("2063-04-05T01:00:00.000-0000").getTime(), result.getConditionsNotOnOrAfter().getMillis());
 		Assert.assertEquals("1", result.getAuthenticationProcessId());
 		Assert.assertEquals(Integer.valueOf(3), result.getLevel());
 		Assert.assertEquals("theuser", result.getUserHandle());
 		Assert.assertEquals(Role.LENDER, result.getRole());
 		Assert.assertEquals("12345", result.getLender());
 	}
 
 	@Test
 	public void testFromXMLLender() throws IOException, SecurityTokenException, ParseException {
 		String assertionXml = getXmlFromFile("assertion-lender.xml");
 		SecurityToken result = SecurityTokenImpl.fromXML(assertionXml);
 		Assert.assertEquals("98287E61FB3575AF5713168076864216", result.getAssertionId());
 		Assert.assertEquals("meteornetwork.org", result.getIssuer());
 		Assert.assertEquals("LTI_AP40", result.getSubjectName());
 		Assert.assertEquals("1.2.3.4", result.getSubjectLocalityIpAddress());
 		Assert.assertEquals("not-a-real-domain-false1.com", result.getSubjectLocalityDnsAddress());
 		Assert.assertEquals(parseDate("2011-09-23T19:54:46.421-0000").getTime(), result.getConditionsNotBefore().getMillis());
 		Assert.assertEquals(parseDate("2011-09-23T23:54:46.421-0000").getTime(), result.getConditionsNotOnOrAfter().getMillis());
 		Assert.assertEquals("1", result.getAuthenticationProcessId());
 		Assert.assertEquals(Integer.valueOf(3), result.getLevel());
 		Assert.assertEquals("theuser", result.getUserHandle());
 		Assert.assertEquals(Role.LENDER, result.getRole());
 		Assert.assertEquals("12345", result.getLender());
 	}
 
 	private String getXmlFromFile(String fileName) throws IOException {
 		File file = new File(this.getClass().getResource(fileName).getFile());
 		FileInputStream fileInputStream = new FileInputStream(file);
 		try {
 			FileChannel fc = fileInputStream.getChannel();
 			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
 			/* Instead of using default, pass in a decoder. */
 			return Charset.forName("utf-8").decode(bb).toString();
 		} finally {
 			fileInputStream.close();
 		}
 	}
 
 	private Date parseDate(String dateString) throws ParseException {
 		return DATE_FORMATTER.parse(dateString);
 	}
 
 	private class SecurityTokenMock extends SecurityTokenImpl {
 
 		@Override
 		protected DateTime getCurrentDateTime() {
 			return new DateTime(currentDate, DateTimeZone.UTC);
 		}
 	}
 }
