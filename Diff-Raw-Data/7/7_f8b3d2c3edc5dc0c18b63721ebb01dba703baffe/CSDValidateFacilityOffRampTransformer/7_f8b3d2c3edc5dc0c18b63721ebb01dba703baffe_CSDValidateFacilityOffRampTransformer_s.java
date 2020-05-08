 /* This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
 package org.jembi.rhea.transformers;
 
 import org.mule.api.MuleMessage;
 import org.mule.api.transformer.TransformerException;
 import org.mule.transformer.AbstractMessageTransformer;
 
 public class CSDValidateFacilityOffRampTransformer extends AbstractMessageTransformer {
 	
 	private static final String CSD_REQUEST_TEMPLATE =
 		"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
 		"<csd:careServicesRequest xmlns:csd='urn:ihe:iti:csd:2013' xmlns='urn:ihe:iti:csd:2013'>\n" +
 		"    <function uuid=\"c7640530-f600-11e2-b778-0800200c9a66\">\n" +
 		"        <requestParams>\n" +
 		"            <id oid=\"%s\"/>\n" +
 		"            <primaryName/>\n" +
 		"            <name/>\n" +
 		"            <addressLine/>\n" +
 		"            <record/>\n" +
 		"            <start/>\n" +
 		"            <max/>\n" +
 		"        </requestParams>\n" +
 		"    </function>\n" +
 		"</csd:careServicesRequest>";
 	
 
 	@Override
 	public Object transformMessage(MuleMessage msg, String enc)
 			throws TransformerException {
 
 		String payload = (String) msg.getPayload();
 		return String.format(CSD_REQUEST_TEMPLATE, payload);
 	}
 }
