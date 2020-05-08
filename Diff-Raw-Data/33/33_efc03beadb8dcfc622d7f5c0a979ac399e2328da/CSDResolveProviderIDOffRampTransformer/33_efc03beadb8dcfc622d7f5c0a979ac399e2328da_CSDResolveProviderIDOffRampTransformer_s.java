 /* This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
 package org.jembi.rhea.transformers;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jembi.rhea.Constants;
 import org.jembi.rhea.transformers.exceptions.IdentifierTypeOIDNotSet;
 import org.jembi.rhea.transformers.exceptions.InvalidPropertyException;
 import org.mule.api.MuleMessage;
 import org.mule.api.transformer.TransformerException;
 import org.mule.api.transport.PropertyScope;
 import org.mule.transformer.AbstractMessageTransformer;
 
 public class CSDResolveProviderIDOffRampTransformer extends AbstractMessageTransformer {
 	
 	private static final String CSD_REQUEST_TEMPLATE =
 		"<?xml version=\"1\" encoding=\"utf-8\"?>\n" +
 		"<csd:careServicesRequest xmlns:csd='urn:ihe:iti:csd:2013' xmlns='urn:ihe:iti:csd:2013'>\n" +
 		"    <function uuid=\"4e8bbeb9-f5f5-11e2-b778-0800200c9a66\">\n" +
 		"        <requestParams>\n" +
 		"            %s\n" +
 		"            <commonName/>\n" +
 		"            <type/>\n" +
 		"            <addressLine/>\n" +
 		"            <record/>\n" +
 		"            <start/>\n" +
 		"            <max/>\n" +
 		"        </requestParams>\n" +
 		"    </function>\n" +
 		"</csd:careServicesRequest>";
 	private static final String CSD_REQUEST_ID_TEMPLATE =
 		"<id oid=\"%s\"/>\n" +
 		"<otherID/>";
 	private static final String CSD_REQUEST_OTHERID_TEMPLATE =
 		"<id/>\n" +
 		"<otherID assigningAuthorityName=\"%s\" code=\"%s\"/>";
 	
 	public static final String SESSIONVAR_PROVIDER_TARGETIDTYPE = "provider-targetIdType";
 	
 	protected static Map<String, String> ASSIGNING_AUTHORITY_OIDS = null;
 	private static final Object lock = new Object();
 
 	private String assigningAuthorityOIDS;
 	
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Object transformMessage(MuleMessage msg, String enc)
 			throws TransformerException {
 
 		try {
 			synchronized (lock) {
 				if (ASSIGNING_AUTHORITY_OIDS==null)
 					buildProviderIdTypeOIDSMap();
 			}
 			
 			Map<String, String> payload = (Map<String, String>) msg.getPayload();
 			String id = payload.get("id");
 			String idType = payload.get("idType");
 	
 			String targetIdType = payload.get("targetIdType");
 			setTargetIdTypeOIDSessionProperty(msg, targetIdType);
 	
 			if (idType==null || idType.equalsIgnoreCase(Constants.EPID_ID_TYPE)) {
 				return buildRequestByIDFromTemplate(id);
 			} else {
 				return buildRequestByOtherIDFromTemplate(idType, id);
 			}
 		} catch (IdentifierTypeOIDNotSet | InvalidPropertyException ex) {
 			throw new TransformerException(this, ex);
 		}
 	}
 
 	private void buildProviderIdTypeOIDSMap() throws InvalidPropertyException {
 		ASSIGNING_AUTHORITY_OIDS = new HashMap<>();
 
 		if (assigningAuthorityOIDS==null)
 			return;
 
 		for (String pair : assigningAuthorityOIDS.split(",")) {
 			String[] components = pair.split(":");
 			if (components.length!=2)
 				throw new InvalidPropertyException("pr.csd.providerIdTypeOIDS", assigningAuthorityOIDS);
 
 			ASSIGNING_AUTHORITY_OIDS.put(components[0], components[1]);
 		}
 	}
 	
 	private void setTargetIdTypeOIDSessionProperty(MuleMessage msg, String targetIdType) throws IdentifierTypeOIDNotSet {
 		if (targetIdType==null || targetIdType.equalsIgnoreCase(Constants.EPID_ID_TYPE)) {
			//Use null to indicate that the universal identifier needs to be retrieved
			msg.setProperty(SESSIONVAR_PROVIDER_TARGETIDTYPE, null, PropertyScope.SESSION);
 		} else {
 			String targetIdOID = ASSIGNING_AUTHORITY_OIDS.get(targetIdType);
 			if (targetIdOID==null)
 				throw new IdentifierTypeOIDNotSet(targetIdType);
 
 			msg.setProperty(SESSIONVAR_PROVIDER_TARGETIDTYPE, targetIdOID, PropertyScope.SESSION);
 		}
 	}
 	
 	private String buildRequestByIDFromTemplate(String id) {
 		return String.format(CSD_REQUEST_TEMPLATE, String.format(CSD_REQUEST_ID_TEMPLATE, id));
 	}
 
 	private String buildRequestByOtherIDFromTemplate(String idType, String id) throws IdentifierTypeOIDNotSet {
 		String idTypeOID = ASSIGNING_AUTHORITY_OIDS.get(idType);
 		if (idTypeOID==null)
 			throw new IdentifierTypeOIDNotSet(idTypeOID);
 
 		return String.format(CSD_REQUEST_TEMPLATE, String.format(CSD_REQUEST_OTHERID_TEMPLATE, idTypeOID, id));
 	}
 
 
 	public String getAssigningAuthorityOIDS() {
 		return assigningAuthorityOIDS;
 	}
 
 	public void setAssigningAuthorityOIDS(String assigningAuthorityOIDS) {
 		this.assigningAuthorityOIDS = assigningAuthorityOIDS;
 	}
 }
