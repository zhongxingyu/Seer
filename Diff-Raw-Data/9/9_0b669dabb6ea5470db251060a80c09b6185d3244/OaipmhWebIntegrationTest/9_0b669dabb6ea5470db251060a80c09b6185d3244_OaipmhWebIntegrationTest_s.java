 package au.com.miskinhill.web.oaipmh;
 
 import static au.com.miskinhill.MiskinHillMatchers.*;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import static org.junit.matchers.JUnitMatchers.*;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.XPath;
 import org.hamcrest.Description;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.Duration;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.junit.Test;
 import org.junit.internal.matchers.TypeSafeMatcher;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.util.LinkedMultiValueMap;
 import org.springframework.util.MultiValueMap;
 
 import au.com.miskinhill.AbstractWebIntegrationTest;
 import au.com.miskinhill.web.ProperURLCodec;
 
 public class OaipmhWebIntegrationTest extends AbstractWebIntegrationTest {
     
     private static final int MIN_EXPECTED_RECORDS = 52;
     private static final DateTimeFormatter DATE_TIME_FORMAT = ISODateTimeFormat.dateTimeNoMillis().withOffsetParsed();
     
     @Test
     public void shouldServeTextXml() {
         ResponseEntity<Document> response = restTemplate.getForEntity(BASE.resolve("/oaipmh?verb=Identify"), Document.class);
         assertThat(response.getHeaders().getContentType(), equalTo(MediaType.TEXT_XML));
     }
     
     @Test
     public void shouldAcceptPOST() {
         MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
         params.add("verb", "Identify");
         Document doc = restTemplate.postForObject(BASE.resolve("/oaipmh"), params, Document.class);
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("Identify"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
     }
     
     @Test
     public void testNoVerb() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), nullValue());
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badVerb"));
         assertThat(error.getText(), equalTo("verb parameter missing"));
     }
     
     @Test
     public void testNonsenseVerb() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=asdf"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), nullValue());
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badVerb"));
         assertThat(error.getText(), equalTo("Unrecognised verb asdf"));
     }
     
     @Test
     public void testNonsenseParam() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=Identify&test=test"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("Identify"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badArgument"));
         assertThat(error.getText(), equalTo("Unexpected parameter test"));
     }
     
     @Test
     public void testDuplicateParam() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListMetadataFormats&identifier=asdf&identifier=xyz"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badArgument"));
         assertThat(error.getText(), equalTo("identifier parameter has multiple values"));
     }
     
     @Test
     public void testBadResumptionToken() {
         Document doc = restTemplate.getForObject(
                 BASE.resolve("/oaipmh?verb=ListIdentifiers&metadataPrefix=oai_dc&resumptionToken=asdf"),
                 Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListIdentifiers"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badResumptionToken"));
         assertThat(error.getText(), equalTo("Unexpected resumption token"));
     }
     
     @Test
     public void testIdentify() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=Identify"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("Identify"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         assertThat(xpath("/oai:OAI-PMH/oai:Identify/oai:repositoryName").selectSingleNode(doc).getText(), containsString("Miskin Hill"));
         assertThat(xpath("/oai:OAI-PMH/oai:Identify/oai:baseURL").selectSingleNode(doc).getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         assertThat(xpath("/oai:OAI-PMH/oai:Identify/oai:protocolVersion").selectSingleNode(doc).getText(), equalTo("2.0"));
         DateTime earliestDatestamp = DATE_TIME_FORMAT.parseDateTime(
                 xpath("/oai:OAI-PMH/oai:Identify/oai:earliestDatestamp").selectSingleNode(doc).getText());
         assertThat(earliestDatestamp.getZone(), equalTo(DateTimeZone.UTC));
         assertTrue(earliestDatestamp.isBeforeNow());
         assertThat(xpath("/oai:OAI-PMH/oai:Identify/oai:deletedRecord").selectSingleNode(doc).getText(), equalTo("no"));
         assertThat(xpath("/oai:OAI-PMH/oai:Identify/oai:granularity").selectSingleNode(doc).getText(), equalTo("YYYY-MM-DDThh:mm:ssZ"));
     }
     
     @Test
     public void testListMetadataFormatsForAllItems() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListMetadataFormats"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListMetadataFormats"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         @SuppressWarnings("unchecked")
         List<Element> formats = (List<Element>) xpath("/oai:OAI-PMH/oai:ListMetadataFormats/oai:metadataFormat").selectNodes(doc);
         assertThat(formats, hasItems(
                 new MetadataFormatMatcher("oai_dc",
                     "http://www.openarchives.org/OAI/2.0/oai_dc.xsd",
                     "http://www.openarchives.org/OAI/2.0/oai_dc/"),
                 new MetadataFormatMatcher("mods",
                    "http://www.loc.gov/standards/mods/v3/mods-3-3.xsd",
                     "http://www.loc.gov/mods/v3")));
     }
     
     @Test
     public void testListMetadataFormatsForNonExistentId() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListMetadataFormats&identifier=asdf"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListMetadataFormats"));
         assertThat(request.attributeValue("identifier"), equalTo("asdf"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("idDoesNotExist"));
         assertThat(error.getText(), equalTo("Identifier asdf is not known to this repository"));
     }
     
     @Test
     public void testListMetadataFormatsForId() {
         String articleUri = "http://miskinhill.com.au/journals/asees/22:1-2/post-soviet-boevik";
         Document doc = restTemplate.getForObject(
                 BASE.resolve("/oaipmh?verb=ListMetadataFormats&identifier=" + ProperURLCodec.encodeUrl(articleUri)),
                 Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListMetadataFormats"));
         assertThat(request.attributeValue("identifier"), equalTo(articleUri));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         @SuppressWarnings("unchecked")
         List<Element> formats = (List<Element>) xpath("/oai:OAI-PMH/oai:ListMetadataFormats/oai:metadataFormat").selectNodes(doc);
         assertThat(formats, hasItems(
                 new MetadataFormatMatcher("oai_dc",
                         "http://www.openarchives.org/OAI/2.0/oai_dc.xsd",
                 "http://www.openarchives.org/OAI/2.0/oai_dc/")));
     }
     
     @Test
     public void testListIdentifiersWithoutMetadataPrefix() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListIdentifiers"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListIdentifiers"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badArgument"));
         assertThat(error.getText(), equalTo("metadataPrefix parameter missing"));
     }
     
     @Test
     public void testListIdentifiers() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListIdentifiers&metadataPrefix=oai_dc"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListIdentifiers"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         @SuppressWarnings("unchecked")
         List<Element> headers = (List<Element>) xpath("/oai:OAI-PMH/oai:ListIdentifiers/oai:header").selectNodes(doc);
         assertThat(headers.size(), greaterThan(MIN_EXPECTED_RECORDS));
         assertThat(headers, hasItem(new HeaderMatcher("http://miskinhill.com.au/journals/asees/22:1-2/post-soviet-boevik")));
     }
     
     @Test
     public void testListIdentifiersForUnsupportedMetadataPrefix() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListIdentifiers&metadataPrefix=asdf"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListIdentifiers"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("asdf"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("cannotDisseminateFormat"));
         assertThat(error.getText(), equalTo("Metadata prefix asdf is not supported"));
     }
     
     @Test
     public void testListIdentifierFromLongPast() {
         Document doc = restTemplate.getForObject(
                 BASE.resolve("/oaipmh?verb=ListIdentifiers&metadataPrefix=oai_dc&from=2000-01-01T00:00:00Z"),
                 Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListIdentifiers"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.attributeValue("from"), equalTo("2000-01-01T00:00:00Z"));
         assertThat(request.attributeValue("until"), nullValue());
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         @SuppressWarnings("unchecked")
         List<Element> records = (List<Element>) xpath("/oai:OAI-PMH/oai:ListIdentifiers/oai:header").selectNodes(doc);
         assertThat(records.size(), greaterThan(MIN_EXPECTED_RECORDS));
     }
     
     @Test
     public void testListIdentifiersFrom() {
         // find the datestamp for some header, then assert it goes away if we request from after its date
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListIdentifiers&metadataPrefix=oai_dc"), Document.class);
         Element header = (Element) xpath("/oai:OAI-PMH/oai:ListIdentifiers/oai:header[1]").selectSingleNode(doc);
         String headerIdentifier = xpath("./oai:identifier").selectSingleNode(header).getText();
         DateTime headerDatestamp = DATE_TIME_FORMAT.parseDateTime(xpath("./oai:datestamp").selectSingleNode(header).getText());
         
         String from = DATE_TIME_FORMAT.print(headerDatestamp.plusHours(1));
         doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListIdentifiers&metadataPrefix=oai_dc&from=" + from), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListIdentifiers"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.attributeValue("from"), equalTo(from));
         assertThat(request.attributeValue("until"), nullValue());
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         @SuppressWarnings("unchecked")
         List<Element> recordHeaders = (List<Element>) xpath("/oai:OAI-PMH/oai:ListIdentifiers/oai:header").selectNodes(doc);
         assertThat(recordHeaders, not(hasItem(new HeaderMatcher(headerIdentifier))));
     }
     
     @Test
     public void testListIdentifiersWithUnparseableFrom() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListIdentifiers&metadataPrefix=oai_dc&from=asdf"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListIdentifiers"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badArgument"));
         assertThat(error.getText(), equalTo("from parameter could not be parsed"));
     }
     
     @Test
     public void testListIdentifiersWithNonUTCFrom() {
         Document doc = restTemplate.getForObject(
                 BASE.resolve("/oaipmh?verb=ListIdentifiers&metadataPrefix=oai_dc&from=2000-01-01T10:00:00%2B10:00"),
                 Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListIdentifiers"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badArgument"));
         assertThat(error.getText(), equalTo("from parameter was not in UTC"));
     }
     
     @Test
     public void testListIdentifiersFromFuture() {
         String future = DATE_TIME_FORMAT.print(new DateTime().plusYears(1).toDateTime(DateTimeZone.UTC));
         Document doc = restTemplate.getForObject(
                 BASE.resolve("/oaipmh?verb=ListIdentifiers&metadataPrefix=oai_dc&from=" + future),
                 Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListIdentifiers"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.attributeValue("from"), equalTo(future));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("noRecordsMatch"));
         assertThat(error.getText(), equalTo("Filter criteria yielded empty result set"));
     }
     
     @Test
     public void testListIdentifiersUntilFuture() {
         String future = DATE_TIME_FORMAT.print(new DateTime().toDateTime(DateTimeZone.UTC).plusYears(1));
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListIdentifiers&metadataPrefix=oai_dc&until=" + future), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListIdentifiers"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.attributeValue("from"), nullValue());
         assertThat(request.attributeValue("until"), equalTo(future));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         @SuppressWarnings("unchecked")
         List<Element> records = (List<Element>) xpath("/oai:OAI-PMH/oai:ListIdentifiers/oai:header").selectNodes(doc);
         assertThat(records.size(), greaterThan(MIN_EXPECTED_RECORDS));
     }
     
     @Test
     public void testListIdentifiersUntil() {
         // find the datestamp for some record, then assert it goes away if we request until before its date
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListIdentifiers&metadataPrefix=oai_dc"), Document.class);
         Element header = (Element) xpath("/oai:OAI-PMH/oai:ListIdentifiers/oai:header[1]").selectSingleNode(doc);
         String headerIdentifier = xpath("./oai:identifier").selectSingleNode(header).getText();
         DateTime headerDatestamp = DATE_TIME_FORMAT.parseDateTime(xpath("./oai:datestamp").selectSingleNode(header).getText());
         
         String until = DATE_TIME_FORMAT.print(headerDatestamp.minusHours(1));
         doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListIdentifiers&metadataPrefix=oai_dc&until=" + until), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListIdentifiers"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.attributeValue("from"), nullValue());
         assertThat(request.attributeValue("until"), equalTo(until));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         @SuppressWarnings("unchecked")
         List<Element> recordHeaders = (List<Element>) xpath("/oai:OAI-PMH/oai:ListIdentifiers/oai:header").selectNodes(doc);
         assertThat(recordHeaders, not(hasItem(new HeaderMatcher(headerIdentifier))));
     }
     
     @Test
     public void testListIdentifiersWithUnparseableUntil() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListIdentifiers&metadataPrefix=oai_dc&until=asdf"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListIdentifiers"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badArgument"));
         assertThat(error.getText(), equalTo("until parameter could not be parsed"));
     }
     
     @Test
     public void testListIdentifiersWithNonUTCUntil() {
         Document doc = restTemplate.getForObject(
                 BASE.resolve("/oaipmh?verb=ListIdentifiers&metadataPrefix=oai_dc&until=2000-01-01T10:00:00%2B10:00"),
                 Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListIdentifiers"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badArgument"));
         assertThat(error.getText(), equalTo("until parameter was not in UTC"));
     }
     
     @Test
     public void testListIdentifiersUntilPast() {
         Document doc = restTemplate.getForObject(
                 BASE.resolve("/oaipmh?verb=ListIdentifiers&metadataPrefix=oai_dc&until=2000-01-01T00:00:00Z"),
                 Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListIdentifiers"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.attributeValue("until"), equalTo("2000-01-01T00:00:00Z"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("noRecordsMatch"));
         assertThat(error.getText(), equalTo("Filter criteria yielded empty result set"));
     }
     
     @Test
     public void testGetRecordWithoutIdentifierOrMetadataPrefix() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=GetRecord"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("GetRecord"));
         assertThat(request.attributeValue("identifier"), nullValue());
         assertThat(request.attributeValue("metadataPrefix"), nullValue());
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badArgument"));
         assertThat(error.getText(), equalTo("identifier parameter missing"));
     }
     
     @Test
     public void testGetRecordWithoutIdentifier() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=GetRecord&metadataPrefix=oai_dc"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("GetRecord"));
         assertThat(request.attributeValue("identifier"), nullValue());
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badArgument"));
         assertThat(error.getText(), equalTo("identifier parameter missing"));
     }
     
     @Test
     public void testGetRecordWithoutMetadataPrefix() {
         String articleUri = "http://miskinhill.com.au/journals/asees/22:1-2/post-soviet-boevik";
         Document doc = restTemplate.getForObject(
                 BASE.resolve("/oaipmh?verb=GetRecord&identifier=" + ProperURLCodec.encodeUrl(articleUri)),
                 Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("GetRecord"));
         assertThat(request.attributeValue("metadataPrefix"), nullValue());
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badArgument"));
         assertThat(error.getText(), equalTo("metadataPrefix parameter missing"));
     }
     
     @Test
     public void testGetRecordForNonsenseMetadataPrefix() {
         String articleUri = "http://miskinhill.com.au/journals/asees/22:1-2/post-soviet-boevik";
         Document doc = restTemplate.getForObject(
                 BASE.resolve("/oaipmh?verb=GetRecord&metadataPrefix=asdf&identifier=" + ProperURLCodec.encodeUrl(articleUri)),
                 Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("GetRecord"));
         assertThat(request.attributeValue("identifier"), equalTo(articleUri));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("asdf"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("cannotDisseminateFormat"));
         assertThat(error.getText(), equalTo("Metadata prefix asdf is not supported"));
     }
     
     @Test
     public void testGetRecordForNonexistentId() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=GetRecord&metadataPrefix=oai_dc&identifier=asdf"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("GetRecord"));
         assertThat(request.attributeValue("identifier"), equalTo("asdf"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("idDoesNotExist"));
         assertThat(error.getText(), equalTo("Identifier asdf is not known to this repository"));
     }
     
     @Test
     public void testGetRecord() {
         String articleUri = "http://miskinhill.com.au/journals/asees/22:1-2/post-soviet-boevik";
         Document doc = restTemplate.getForObject(
                 BASE.resolve("/oaipmh?verb=GetRecord&metadataPrefix=oai_dc&identifier=" + ProperURLCodec.encodeUrl(articleUri)),
                 Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("GetRecord"));
         assertThat(request.attributeValue("identifier"), equalTo(articleUri));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         @SuppressWarnings("unchecked")
         List<Element> records = (List<Element>) xpath("/oai:OAI-PMH/oai:GetRecord/oai:record").selectNodes(doc);
         assertThat(records.size(), equalTo(1));
         Element record = records.get(0);
         assertThat((Element) xpath("./oai:header").selectSingleNode(record), new HeaderMatcher(articleUri));
         assertThat(xpath("./oai:metadata/oai_dc:dc/*").selectNodes(record).size(), greaterThan(0));
     }
     
     @Test
     public void testListRecordsWithoutMetadataPrefix() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListRecords"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListRecords"));
         assertThat(request.attributeValue("metadataPrefix"), nullValue());
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badArgument"));
         assertThat(error.getText(), equalTo("metadataPrefix parameter missing"));
     }
     
     @Test
     public void testListRecordsForNonsenseMetadataPrefix() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListRecords&metadataPrefix=asdf"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListRecords"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("asdf"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("cannotDisseminateFormat"));
         assertThat(error.getText(), equalTo("Metadata prefix asdf is not supported"));
     }
     
     @Test
     public void testListRecordsForSet() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListRecords&metadataPrefix=asdf&set=blerg"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListRecords"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("asdf"));
         assertThat(request.attributeValue("set"), equalTo("blerg"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("noSetHierarchy"));
         assertThat(error.getText(), equalTo("This repository does not support sets"));
     }
     
     @Test
     public void testListAllRecords() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListRecords&metadataPrefix=oai_dc"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListRecords"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         @SuppressWarnings("unchecked")
         List<Element> records = (List<Element>) xpath("/oai:OAI-PMH/oai:ListRecords/oai:record").selectNodes(doc);
         assertThat(records.size(), greaterThan(MIN_EXPECTED_RECORDS));
         for (Element record: records) {
             assertThat(xpath("./oai:header/oai:identifier").selectSingleNode(record), not(nullValue()));
             assertThat(xpath("./oai:header/oai:datestamp").selectSingleNode(record), not(nullValue()));
             assertThat(xpath("./oai:metadata/oai_dc:dc/*").selectNodes(record).size(), greaterThan(0));
         }
     }
     
     @Test
     public void testListRecordsFromLongPast() {
         Document doc = restTemplate.getForObject(
                 BASE.resolve("/oaipmh?verb=ListRecords&metadataPrefix=oai_dc&from=2000-01-01T00:00:00Z"),
                 Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListRecords"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.attributeValue("from"), equalTo("2000-01-01T00:00:00Z"));
         assertThat(request.attributeValue("until"), nullValue());
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         @SuppressWarnings("unchecked")
         List<Element> records = (List<Element>) xpath("/oai:OAI-PMH/oai:ListRecords/oai:record").selectNodes(doc);
         assertThat(records.size(), greaterThan(MIN_EXPECTED_RECORDS));
     }
     
     @Test
     public void testListRecordsFrom() {
         // find the datestamp for some record, then assert it goes away if we request from after its date
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListRecords&metadataPrefix=oai_dc"), Document.class);
         Element record = (Element) xpath("/oai:OAI-PMH/oai:ListRecords/oai:record[1]").selectSingleNode(doc);
         String recordIdentifier = xpath("./oai:header/oai:identifier").selectSingleNode(record).getText();
         DateTime recordDatestamp = DATE_TIME_FORMAT.parseDateTime(xpath("./oai:header/oai:datestamp").selectSingleNode(record).getText());
         
         String from = DATE_TIME_FORMAT.print(recordDatestamp.plusHours(1));
         doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListRecords&metadataPrefix=oai_dc&from=" + from), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListRecords"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.attributeValue("from"), equalTo(from));
         assertThat(request.attributeValue("until"), nullValue());
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         @SuppressWarnings("unchecked")
         List<Element> recordHeaders = (List<Element>) xpath("/oai:OAI-PMH/oai:ListRecords/oai:record/oai:header").selectNodes(doc);
         assertThat(recordHeaders, not(hasItem(new HeaderMatcher(recordIdentifier))));
     }
     
     @Test
     public void testListRecordsWithUnparseableFrom() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListRecords&metadataPrefix=oai_dc&from=asdf"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListRecords"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badArgument"));
         assertThat(error.getText(), equalTo("from parameter could not be parsed"));
     }
     
     @Test
     public void testListRecordsWithNonUTCFrom() {
         Document doc = restTemplate.getForObject(
                 BASE.resolve("/oaipmh?verb=ListRecords&metadataPrefix=oai_dc&from=2000-01-01T10:00:00%2B10:00"),
                 Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListRecords"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badArgument"));
         assertThat(error.getText(), equalTo("from parameter was not in UTC"));
     }
     
     @Test
     public void testListRecordsFromFuture() {
         String future = DATE_TIME_FORMAT.print(new DateTime().toDateTime(DateTimeZone.UTC).plusYears(1));
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListRecords&metadataPrefix=oai_dc&from=" + future), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListRecords"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.attributeValue("from"), equalTo(future));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("noRecordsMatch"));
         assertThat(error.getText(), equalTo("Filter criteria yielded empty result set"));
     }
     
     @Test
     public void testListRecordsUntilFuture() {
         String future = DATE_TIME_FORMAT.print(new DateTime().toDateTime(DateTimeZone.UTC).plusYears(1));
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListRecords&metadataPrefix=oai_dc&until=" + future), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListRecords"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.attributeValue("from"), nullValue());
         assertThat(request.attributeValue("until"), equalTo(future));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         @SuppressWarnings("unchecked")
         List<Element> records = (List<Element>) xpath("/oai:OAI-PMH/oai:ListRecords/oai:record").selectNodes(doc);
         assertThat(records.size(), greaterThan(MIN_EXPECTED_RECORDS));
     }
     
     @Test
     public void testListRecordsUntil() {
         // find the datestamp for some record, then assert it goes away if we request until before its date
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListRecords&metadataPrefix=oai_dc"), Document.class);
         Element record = (Element) xpath("/oai:OAI-PMH/oai:ListRecords/oai:record[1]").selectSingleNode(doc);
         String recordIdentifier = xpath("./oai:header/oai:identifier").selectSingleNode(record).getText();
         DateTime recordDatestamp = DATE_TIME_FORMAT.parseDateTime(xpath("./oai:header/oai:datestamp").selectSingleNode(record).getText());
         
         String until = DATE_TIME_FORMAT.print(recordDatestamp.minusHours(1));
         doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListRecords&metadataPrefix=oai_dc&until=" + until), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListRecords"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.attributeValue("from"), nullValue());
         assertThat(request.attributeValue("until"), equalTo(until));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         @SuppressWarnings("unchecked")
         List<Element> recordHeaders = (List<Element>) xpath("/oai:OAI-PMH/oai:ListRecords/oai:record/oai:header").selectNodes(doc);
         assertThat(recordHeaders, not(hasItem(new HeaderMatcher(recordIdentifier))));
     }
     
     @Test
     public void testListRecordsWithUnparseableUntil() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListRecords&metadataPrefix=oai_dc&until=asdf"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListRecords"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badArgument"));
         assertThat(error.getText(), equalTo("until parameter could not be parsed"));
     }
     
     @Test
     public void testListRecordsWithNonUTCUntil() {
         Document doc = restTemplate.getForObject(
                 BASE.resolve("/oaipmh?verb=ListRecords&metadataPrefix=oai_dc&until=2000-01-01T10:00:00%2B10:00"),
                 Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListRecords"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("badArgument"));
         assertThat(error.getText(), equalTo("until parameter was not in UTC"));
     }
     
     @Test
     public void testListRecordsUntilPast() {
         Document doc = restTemplate.getForObject(
                 BASE.resolve("/oaipmh?verb=ListRecords&metadataPrefix=oai_dc&until=2000-01-01T00:00:00Z"),
                 Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListRecords"));
         assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
         assertThat(request.attributeValue("until"), equalTo("2000-01-01T00:00:00Z"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("noRecordsMatch"));
         assertThat(error.getText(), equalTo("Filter criteria yielded empty result set"));
     }
     
     @Test
     public void testListSets() {
         Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListSets"), Document.class);
         assertResponseDate(doc);
         
         Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
         assertThat(request.attributeValue("verb"), equalTo("ListSets"));
         assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
         
         Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
         assertThat(error.attributeValue("code"), equalTo("noSetHierarchy"));
         assertThat(error.getText(), equalTo("This repository does not support sets"));
     }
     
     private static XPath xpath(String expression) { // ugh
         XPath xpath = DocumentHelper.createXPath(expression);
         HashMap<String, String> namespaces = new HashMap<String, String>();
         namespaces.put("oai", "http://www.openarchives.org/OAI/2.0/");
         namespaces.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
         xpath.setNamespaceURIs(namespaces);
         return xpath;
     }
 
     private void assertResponseDate(Document doc) {
         DateTime responseDate = DATE_TIME_FORMAT.parseDateTime(xpath("/oai:OAI-PMH/oai:responseDate").selectSingleNode(doc).getText());
         assertThat(responseDate, within(Duration.standardSeconds(5)));
         assertThat(responseDate.getZone(), equalTo(DateTimeZone.UTC));
     }
     
     private static final class MetadataFormatMatcher extends TypeSafeMatcher<Element> {
         
         private final String prefix;
         private final String schema;
         private final String namespace;
         
         public MetadataFormatMatcher(String prefix, String schema, String namespace) {
             this.prefix = prefix;
             this.schema = schema;
             this.namespace = namespace;
         }
 
         @Override
         public boolean matchesSafely(Element element) {
             return (xpath("./oai:metadataPrefix").selectSingleNode(element).getText().equals(prefix) &&
                     xpath("./oai:schema").selectSingleNode(element).getText().equals(schema) &&
                     xpath("./oai:metadataNamespace").selectSingleNode(element).getText().equals(namespace));
         }
         
         @Override
         public void describeTo(Description desc) {
             desc.appendText("<metadataFormat> element with <metadataPrefix>")
                 .appendText(prefix)
                 .appendText("</metadataPrefix> and <schema>")
                 .appendText(schema)
                 .appendText("</schema> and <metadataNamespace>")
                 .appendText(namespace)
                 .appendText("</metadataNamespace>");
         }
         
     }
     
     private static final class HeaderMatcher extends TypeSafeMatcher<Element> {
         
         private final String identifier;
         
         public HeaderMatcher(String identifier) {
             this.identifier = identifier;
         }
         
         @Override
         public boolean matchesSafely(Element element) {
             return (xpath("./oai:identifier").selectSingleNode(element).getText().equals(identifier) &&
                     DATE_TIME_FORMAT.parseDateTime(xpath("./oai:datestamp").selectSingleNode(element).getText())
                         .getZone().equals(DateTimeZone.UTC));
         }
         
         @Override
         public void describeTo(Description desc) {
             desc.appendText("<header> element with <identifier>")
                 .appendText(identifier)
                 .appendText("</identifier> with UTC datestamp");
         }
         
     }
 
 }
