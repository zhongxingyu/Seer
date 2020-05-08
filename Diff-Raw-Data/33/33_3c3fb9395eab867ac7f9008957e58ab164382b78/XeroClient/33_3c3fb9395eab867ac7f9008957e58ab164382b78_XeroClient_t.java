 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package com.softlysoftware.jxero;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Arrays;
 import java.util.Properties;
 import java.io.IOException;
 import java.io.File;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 import java.net.URISyntaxException;
 import net.oauth.OAuth;
 import net.oauth.OAuthException;
 import net.oauth.OAuthAccessor;
 import net.oauth.OAuthConsumer;
 import net.oauth.OAuthMessage;
 import net.oauth.client.OAuthClient;
 import net.oauth.signature.RSA_SHA1;
 import net.oauth.client.httpclient3.HttpClient3;
 import com.softlysoftware.jxero.wrappers.Response;
 import com.softlysoftware.jxero.wrappers.Endpoint;
 import com.softlysoftware.jxero.wrappers.Contacts;
import com.softlysoftware.jxero.wrappers.Invoices;
 import com.softlysoftware.jxero.core.Contact;
import com.softlysoftware.jxero.core.Invoice;
 import com.softlysoftware.jxero.core.Address;
 import com.softlysoftware.jxero.core.Phone;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
 * The main point of interaction with the package. Create one of these with your OAuth credentials and you
 * will be free to use the simple get/post methods to work direct with your Xero business logic objects.
 */
 public class XeroClient {
 
 	private static Log log = LogFactory.getLog("xero.client");
 
 	private static String BASE =  "https://api.xero.com/api.xro/2.0/";
 
 	private OAuthAccessor oAuthAccessor = null;
 
 	public enum Method {GET, POST, PUT}
 
 	private XeroClient() {}
 
 	/**
 	* To create a client this way, you just need the three strings that encapsulate your OAuth
 	* inteactions with the Xero API.
 	* See <a href="http://developer.xero.com/documentation/getting-started/private-applications/">the Xero guide to setting up a private application</a> 
 	* for details on how to get your credentials.
 	*/
 	public XeroClient(String consumerKey, String consumerSecret, String privateKey) {
 		setupOAuth(consumerKey, consumerSecret, privateKey);
 	}
 
 	/**
 	* The first two parameters are simple Strings, but the privateKey is a bigger chunk of text. 
 	* Use this contstuctor to grab a pem file from your filesystem somewhere.
 	*/
 	public XeroClient(String consumerKey, String consumerSecret, File privateKeyFile) {
 		try {
 			String privateKey =  FileUtils.readFileToString(privateKeyFile, "ISO-8859-1");
 			setupOAuth(consumerKey, consumerSecret, privateKey);
 		}
 		catch (IOException e) {
 			throw new RuntimeException("Problem tracking down your private key file.", e);
 		}
 	}
 
 	/**
 	* You can also include your credentials at runtime and use this constructor.
 	* Note, if this can't find both a jxero.aouth.properties and jxero.oauth.pem on the classpath,
 	* this constructor will throw an exception.
 	*/
 	public XeroClient(boolean iPromiseToIncludeTheFilesOnTheClasspath) {
 		if (!iPromiseToIncludeTheFilesOnTheClasspath) throw new RuntimeException("No soup for you.");
 		try {
 			Properties properties = new Properties();
 			InputStream propertiesStream = this.getClass().getClassLoader().getResourceAsStream("jxero.oauth.properties");
 			if (propertiesStream == null) throw new RuntimeException("Couldn't find the jxero.oauth.properties file on classpath.");
 			properties.load(propertiesStream);
 			propertiesStream.close();
 			String consumerKey = properties.getProperty("jxero.oauth.consumer.key");
 			String consumerSecret = properties.getProperty("jxero.oauth.consumer.secret");
 			if (consumerKey == null || consumerKey.equals("YOUR-CONSUMER-KEY") || consumerSecret == null || consumerSecret.equals("YOUR-CONSUMER-SECRET")) {
 				throw new RuntimeException("Found the jxero.oauth.properties file, but didn't spot your key and secret in it.");
 			}
 			String privateKey = "";
 			// two ways to pass in the private key
 			if (properties.getProperty("jxero.oauth.pem.file") != null) {
 				// state the location in the properties file
 				privateKey = FileUtils.readFileToString(new File(properties.getProperty("jxero.oauth.pem.file")), "ISO-8859-1");
 			}
 			else {
 				// or the correctly named file on the classpath
 				InputStream in = this.getClass().getClassLoader().getResourceAsStream("jxero.oauth.pem");
 				if (in == null) throw new RuntimeException("Couldn't find the jxero.oauth.pem file on classpath.");
 				String line = null;
 				BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
 				while ((line = reader.readLine()) != null) privateKey = privateKey + line + "\n";
 				reader.close();
 			}
 			setupOAuth(consumerKey, consumerSecret, privateKey);
 		}
 		catch (IOException e) {
 			throw new RuntimeException("Problem tracking down the OAuth credentials from file from classpath", e);
 		}
 	}
 
 	private void setupOAuth(String consumerKey, String consumerSecret, String privateKey) {
 		OAuthConsumer consumer = new OAuthConsumer(null, consumerKey, null, null);
 		consumer.setProperty(RSA_SHA1.PRIVATE_KEY, privateKey);
 		consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);
 		oAuthAccessor = new OAuthAccessor(consumer);
 		oAuthAccessor.accessToken = consumerKey;
 		oAuthAccessor.tokenSecret = consumerSecret;
 	}
 
 	/**
 	* Advanced users can use this method for constucting their API calls directly.
 	* But hopefully it will be simpler to use the longer get/post methods.
 	*/
 	public String invoke(Endpoint endpoint, Method method, String identifier, List<OAuth.Parameter> params) {
 		try {
 			if (!endpoint.supportsMethod(method)) throw new RuntimeException("Bad method : " + method);
 			OAuthClient oAuthClient = new OAuthClient(new HttpClient3());
 			String url = BASE + endpoint.getRootElementName() + "/";
 			if (identifier != null) url = url + identifier;
 			if (params == null) params = OAuth.newList();
 			log.trace("Invoking : " + method + " on " + url);
 			log.trace("--------------------------------");
 			for (OAuth.Parameter param : params) {
 				log.trace(param.getKey() + " : " + param.getValue());
 			}
 			log.trace("--------------------------------");
 			OAuthMessage message= oAuthClient.invoke(oAuthAccessor, method.toString(), url, params);
 			String response = message.readBodyAsString();
 			log.trace("Reponse:");
 			log.trace("--------------------------------");
 			log.trace(response);
 			log.trace("--------------------------------");
 			return response;
 		}
 		catch (URISyntaxException urise) {
 			throw new RuntimeException("Should not happen?", urise);
 		}
 		catch (IOException ioe) {
 			throw new RuntimeException("Something went wrong connecting to the service.", ioe);
 		}
 		catch (OAuthException oae) {
 			throw new RuntimeException("Something went wrong with the OAuth connection.", oae);
 		}
 	}
 
 	/**
 	* Advanced users can build a GET call this way, adding filters etc. via the params object.
 	* Hopefully though it will be simpler to work direct with the longer get methods.
 	*/
 	public Response get(Endpoint endpoint, String identifier, List<OAuth.Parameter> params) {
 		String xml = invoke(endpoint, Method.GET, identifier, params);
 		return (Response)Xml.fromXml(xml, Response.class);
 	}
 
 	/**
 	* Advanced users can build POST calls here. Again, use the named post methods to avoid having to wrap your
 	* own collections in an Endpoint object. 
 	*/
 	public void post(Endpoint endpoint) {
 		String xml = Xml.toXml(endpoint);
 		invoke(endpoint, Method.POST, null, OAuth.newList("xml", xml));
 	}
 
 	// ------------------- Contacts
 
 	/**
 	* Grab a Contact object by specifiying its identifier. You can use either the main Xero one, or any ContactID you 
 	* may have created via the API.
 	*
 	* @return Either the matching Contact, or null when no match is found.
 	*/
 	public Contact getContact(String identifier) {
 	    List<Contact> build = get(new Contacts(), identifier, null).Contacts.getList();
 		if (build.size() == 0) return null;
 		if (build.size() > 1) throw new RuntimeException("Multiple contacts with the same identifier : " + identifier);
 		return build.get(0);
 	}
 
 	/**
 	* Grab contacts using any "where" filter. For example, getContactsWhere("FullyPaidOnDate >= DateTime(2011, 10, 01) AND FullyPaidOnDate <= DateTime(2011, 10, 30)");
 	* See <a href="http://developer.xero.com/documentation/getting-started/http-requests-and-responses/">the Xero documentation</a> for full details.
 	*/
 	public List<Contact> getContactsWhere(String where) {
 		return get(new Contacts(), null, OAuth.newList("where", where)).Contacts.getList();
 	}
 
 	/**
 	* Like the generalised getContactsWhere, but for convenience when you expect just one object to be returned in the set.
 	* Throws an exception when there's more than one match.
 	* @return If no match is found, returns null.
 	*/
 	public Contact getContactWhere(String where) {
 		List<Contact> contacts = getContactsWhere(where);
 		if (contacts.size() == 0) return null;
 		if (contacts.size() > 1) throw new RuntimeException("Multiple (" + contacts.size() + ") contacts matched : " + where);
 		return contacts.get(0);
 	}
 
 	/**
 	* I'm not sure if the email address is unique on the Xero side. If so, this should never cause a problem.
 	*/
 	public Contact getContactByEmailAddress(String email) {
 		return getContactWhere("EmailAddress =  \"" + email + "\"");
 	}
 
 	/**
 	* Either get a collection of Contact objects, or build from scratch. Then call this method to update/add them to your Xero data.
 	*/
 	public void postContacts(List<Contact> contacts) {
 		Contacts endpoint = new Contacts();
 		endpoint.getList().addAll(contacts);
 		post(endpoint);
 	}
 
 	/**
 	* Grab a contact via a get method, or build one from scratch to use this method to update/add it.
 	*/
 	public void postContact(Contact contact) {
 		Contacts endpoint = new Contacts();
 		endpoint.getList().add(contact);
 		post(endpoint);
 	}
 
	// ------------------- Invoices

	/**
	* Either get a collection of Invoice objects, or build from scratch. Then call this method to update/add them to your Xero data.
	*/
	public void postInvoices(List<Invoice> invoices) {
		Invoices endpoint = new Invoices();
		endpoint.getList().addAll(invoices);
		post(endpoint);
	}

	/**
	* Grab a invoice via a get method, or build one from scratch to use this method to update/add it.
	*/
	public void postInvoice(Invoice invoice) {
		Invoices endpoint = new Invoices();
		endpoint.getList().add(invoice);
		post(endpoint);
	}

 }
