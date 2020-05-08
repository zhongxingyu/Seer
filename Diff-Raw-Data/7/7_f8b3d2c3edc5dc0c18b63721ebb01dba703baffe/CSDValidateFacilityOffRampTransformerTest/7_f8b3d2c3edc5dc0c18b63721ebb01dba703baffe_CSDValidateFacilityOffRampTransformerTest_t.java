 /* This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
 package org.jembi.rhea.transformers;
 
 import static org.junit.Assert.*;
 
 import org.jembi.Util;
 import org.junit.Test;
 import org.mule.api.MuleMessage;
 import org.mule.api.transformer.TransformerException;
 
 public class CSDValidateFacilityOffRampTransformerTest {
 	private static final String EXPECTED_REQUEST_BY_ID =
 		"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
 		"<csd:careServicesRequest xmlns:csd='urn:ihe:iti:csd:2013' xmlns='urn:ihe:iti:csd:2013'>\n" +
 		"    <function uuid=\"c7640530-f600-11e2-b778-0800200c9a66\">\n" +
 		"        <requestParams>\n" +
 		"            <id oid=\"%s\"/>\n" +
 		"            <primaryName/>\n" +
 		"            <name/>\n" +
		"            <codedType/>\n" +
 		"            <addressLine/>\n" +
 		"            <record/>\n" +
 		"            <start/>\n" +
 		"            <max/>\n" +
 		"        </requestParams>\n" +
 		"    </function>\n" +
 		"</csd:careServicesRequest>";
 
 	@Test
 	public void testTransformMessage() throws TransformerException {
 		String id = "1234567890";
 		MuleMessage mockMessage = Util.buildMockMuleResponse(true, id);
 		CSDValidateFacilityOffRampTransformer transformer = new CSDValidateFacilityOffRampTransformer();
 
 		Object res = transformer.transform(mockMessage, "");
 		assertEquals(Util.trimXML(String.format(EXPECTED_REQUEST_BY_ID, id)), Util.trimXML((String)res));
 	}
 }
