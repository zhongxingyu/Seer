 /**
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.gmail;
 
 import java.util.Properties;
 
 import org.apache.commons.lang.StringUtils;
import org.mule.api.annotations.Module;
 import org.mule.api.annotations.lifecycle.Start;
 
 import com.google.code.javax.mail.MessagingException;
 import com.google.code.javax.mail.NoSuchProviderException;
 import com.google.code.javax.mail.Session;
 import com.google.code.javax.mail.Store;
 
 /**
  *
  * Gmail cloud connector.
  * This connector covers the standard IMAP protocol plus Google's extensions for Gmail.
  * This implementation uses basic authentication
 
  * @author mariano.gonzalez@mulesoft.com
  *
  */
@Module(name="gmail", schemaVersion="1.0", friendlyName="GMail Connector", minMuleVersion="3.3")
 public class BasicAuthGmailConnector extends BaseGmailConnector {
 	
 	private Properties props;
 	
 	@Start
 	public void init() {
 		this.props = System.getProperties();
 		this.props.setProperty("mail.store.protocol", "imaps");
 	}
 	
 	@Override
 	protected Store getStore(String username, String password) throws MessagingException {
 		
 		if (StringUtils.isBlank(password)) {
 			throw new IllegalArgumentException("password cannot be blank");
 		}
 		
 		Session session = Session.getDefaultInstance(props, null);
 		Store store = null;
 		
 		try {
 			store = session.getStore("imaps");
 		} catch (NoSuchProviderException e) {
 			throw new RuntimeException("Could not find imaps provider", e);
 		}
 		
 		store.connect("imap.gmail.com", username, password);
 		return store;
 	}
 
 
 	/**
 	 * Unsupported for this implementation. Do not invoke
 	 * @throws UnsupportedOperationException
 	 */
 	@Override
 	public String getAccessToken() {
 		throw new UnsupportedOperationException();
 	}
 	
 }
