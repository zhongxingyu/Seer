 package org.springminutes.example.integration;
 
 import org.custommonkey.xmlunit.*;
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.junit.runner.RunWith;
 import org.springframework.integration.Message;
 import org.springframework.integration.message.GenericMessage;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 import java.util.List;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"/META-INF/spring/integration-appContext.xml"})
 public class JsonToFloodRequestXmlTransformerTest {
 
     @Test
     public void test_transformPayload() {
         String jsonString = "{ " +
                   "\"credentials\": { \"username\": \"bobo\", \"password\": \"logmein\" }, " +
                    "\"loanInfo\": { \"caseNumber\": \"LOAN-1234\", " +
                       "\"borrower\": { \"firstName\":\"Jed\", \"lastName\": \"Clampett\" }," +
                       "\"property\": { \"address1\":\"750 Bel Air Rd\", \"city\": \"Los Angeles\", \"state\": \"CA\", \"zip\": \"90077\" }" +
                    "}" +
                 "}";
         Message convertedJson = new JsonToFloodRequestXmlTransformer().transform(new GenericMessage<String>(jsonString));
         String jsonToXmlString = (String) convertedJson.getPayload();
 
         String xmlString = "<REQUEST_GROUP MISMOVersionID='2.4'>" +
                 "  <SUBMITTING_PARTY LoginAccountIdentifier='bobo' LoginAccountPassword='logmein' />" +
                "  <REQUEST InternalAccountIdentifier='bobo' RequestDatetime='2011-11-24T01:44:37.241-05:00'>" +
                 "    <REQUEST_DATA>" +
                 "      <FLOOD_REQUEST MISMOVersionID='2.4' _ActionType='Original'>" +
                 "        <_PRODUCT _CategoryDescription='Flood'>" +
                 "          <_NAME _Identifier='FL' />" +
                 "        </_PRODUCT>" +
                 "        <BORROWER _FirstName='Jed' _LastName='Clampett' />" +
                 "        <MORTGAGE_TERMS LenderCaseIdentifier='LOAN-1234' />" +
                 "        <PROPERTY _StreetAddress='750 Bel Air Rd' _City='Los Angeles' _State='CA' _PostalCode='90077' />" +
                 "      </FLOOD_REQUEST>" +
                 "    </REQUEST_DATA>" +
                 "  </REQUEST>" +
                 "</REQUEST_GROUP>";
 
         try {
             XMLUnit.setIgnoreWhitespace(true);
             DetailedDiff myDiff = new DetailedDiff(new Diff(xmlString, jsonToXmlString));
             List allDifferences = myDiff.getAllDifferences();
             assertEquals(myDiff.toString(), 1, allDifferences.size());
             assertTrue(allDifferences.get(0).toString(), ((Difference) allDifferences.get(0)).toString().contains("/REQUEST_GROUP[1]/REQUEST[1]/@RequestDatetime"));
         } catch (SAXException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
