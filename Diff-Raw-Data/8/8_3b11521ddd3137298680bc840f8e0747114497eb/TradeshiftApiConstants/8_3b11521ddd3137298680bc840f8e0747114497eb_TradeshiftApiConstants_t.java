 package org.integration.payments.server.ws.tradeshift;
 
 import org.integration.payments.server.ws.ApiConstants;
 
 public abstract class TradeshiftApiConstants extends ApiConstants {
 	public static final String DEFAULT_CHARSET = UTF8_CHARSET;
 
 	// ~ header keys
 	public static final String TENANTID_HEADER_NAME = "X-Tradeshift-TenantId";
 
 	// ~ field keys
 	public static final String SETTINGS_FIELD_NAME = "Settings";
 }
