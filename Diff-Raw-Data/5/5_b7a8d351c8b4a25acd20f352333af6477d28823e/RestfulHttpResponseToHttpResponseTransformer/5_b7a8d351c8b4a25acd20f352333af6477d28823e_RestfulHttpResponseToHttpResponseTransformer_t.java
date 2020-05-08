 /* This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
 package org.jembi.rhea.transformers;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import org.jembi.rhea.RestfulHttpResponse;
 import org.jembi.rhea.orchestration.exceptions.ClientValidationException;
 import org.jembi.rhea.orchestration.exceptions.EncounterEnrichmentException;
 import org.jembi.rhea.orchestration.exceptions.LocationValidationException;
 import org.jembi.rhea.orchestration.exceptions.ProviderValidationException;
 import org.mule.api.MuleMessage;
 import org.mule.api.transformer.TransformerException;
 import org.mule.api.transport.PropertyScope;
 import org.mule.transformer.AbstractMessageTransformer;
 
 public class RestfulHttpResponseToHttpResponseTransformer extends
 		AbstractMessageTransformer {
 
 	@Override
 	public Object transformMessage(MuleMessage msg, String enc) throws TransformerException {
 		
 		Object payload = msg.getPayload();
 		
 		if (payload instanceof RestfulHttpResponse) {
 			RestfulHttpResponse restRes = (RestfulHttpResponse) payload;
 			msg.setOutboundProperty("http.status", restRes.getHttpStatus());
 			msg.setPayload(restRes.getBody());
 			
 			return msg;
 			
 		} else {
 			Throwable exception = msg.getExceptionPayload().getException();
 			StringWriter sw = new StringWriter();
 			
 			if (exception != null) {
 				exception.printStackTrace(new PrintWriter(sw));
 				
 				Throwable cause = exception.getCause();
 				if (cause != null) {
 					if (cause instanceof ClientValidationException) {
 						msg.setProperty("http.status", 400, PropertyScope.OUTBOUND);
 						msg.setPayload("Message orchestration failed, the client ID failed validation: " + sw.toString());
 					} else if (cause instanceof EncounterEnrichmentException) {
 						msg.setProperty("http.status", 400, PropertyScope.OUTBOUND);
 						msg.setPayload("Message orchestration failed, encounter enrichment failed: " + sw.toString());
 					} else if (cause instanceof LocationValidationException) {
 						msg.setProperty("http.status", 400, PropertyScope.OUTBOUND);
 						msg.setPayload("Message orchestration failed, the location ID failed validation: " + sw.toString());
 					} else if (cause instanceof ProviderValidationException) {
 						msg.setProperty("http.status", 400, PropertyScope.OUTBOUND);
 						msg.setPayload("Message orchestration failed, the provider ID failed validation: " + sw.toString());
 					} else {
 						msg.setProperty("http.status", 500, PropertyScope.OUTBOUND);
						msg.setPayload("A server error has occured, the response was not of type RestfulHttpResponse, caused by: " + sw.toString());
 					}
 				}
 				
 			} else {
 				msg.setProperty("http.status", 500, PropertyScope.OUTBOUND);
				msg.setPayload("A server error has occured, the response was of type " + payload.getClass().getName() + " instead of the expected type RestfulHttpResponse");
 			}
 			
 			return msg;
 		}
 
 	}
 
 }
