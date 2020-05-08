 /* 
   Copyright 2010 Duncan Mac-Vicar P. <dmacvicar@suse.de>
  
   This file is part of the openSUSE Build Service Android Client.
 
   openSUSE Build Service Android Client is free software: you
   can  redistribute it and/or modify it under the terms of the GNU
   General Public License as published by the Free Software Foundation,
   either version 3 of the License, or (at your option) any later
   version.
 
   openSUSE Build Service Android Client is distributed in the hope
   that it will be useful, but WITHOUT ANY WARRANTY; without even the
   implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   See the GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package org.opensuse.android;
 
 import org.opensuse.android.util.Base64;
 import org.opensuse.android.util.IOUtils;
 import org.simpleframework.xml.Serializer;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.*;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.methods.*;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.util.Log;
 
 public class HttpCoreRestClient implements RestClient {
 	private static final String HTTPCLIENT = "HTTPCLIENT";
 	private String protocol, host, username, password;
 	private Serializer serializer;
 		
 	public HttpCoreRestClient(String host, String username, String password) {
 		if (host == null) throw new RuntimeException("Need to specify a host, username, and password");
 		this.protocol = "http://";
 		this.host = host;
 		this.username = username;
 		this.password = password;
 		this.serializer = new XmlPersister();		
 	}
 	
 	public HttpCoreRestClient() {		
 		this.serializer = new XmlPersister();
 	}
 		
 	public <T> T post(String path, String data, Class<T> returnType) {
 		Resource resource = post(path, data);
 		return decode(resource, returnType);		
 	}
 	
 	public <T> T get(String path, Class<T> returnType) {
 		Resource resource = get(path);
 		return decode(resource, returnType);		
 	}
 	
 	public String getPlain(String path) {
 		Resource resource = get(path);
 		return IOUtils.readStream(resource.getContent());
 	}
 	
 	public String postPlain(String path, String data) {
 		Resource resource = post(path, data);
 		return IOUtils.readStream(resource.getContent());
 	}
 	
 	/**
 	 * Decodes the resource converting it to the specified type
 	 */
 	private <T> T decode(Resource resource, Class<T> returnType) {
 		try {
 			T object = serializer.read(returnType, resource.getContent());
 			return object;
 		}
 		catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Can't retrieve resource", e);
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public void setProtocol(String protocol) {
 		this.protocol = protocol;
 	}
 	
 	public void setHost(String host) {
 		this.host = host;
 	}
 	
 	public String getUsername() { return this.username; }
 	
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	
 	public Resource get(String uri) {
 		Log.i(HTTPCLIENT, "GET: " + uri);
         return execute(new HttpGet(buildUri(uri)));
 	}
 	
 	public Resource post(String uri, String xml){
 		Log.i(HTTPCLIENT, "POST: " + uri);
 		return execute(add(new HttpPost(buildUri(uri)), xml));
 	}
 	public Resource put(String uri, String xml) {
 		return execute(add(new HttpPut(buildUri(uri)), xml));
 	}
 	public Resource delete(String uri) {
 		return execute(new HttpDelete(buildUri(uri)));
 	}
 	
 	private Resource execute(HttpUriRequest m) {
 		try {
 			m.addHeader("Authorization", "Basic "+ getEncodedCredentials());
 
 			HttpResponse response = connect().execute(m);
 			int status = response.getStatusLine().getStatusCode();
 			Log.w("HTTP_STATUS", Integer.toString(status));
 			//Log.i("HTTP_CONTENT", response.getEntity().getContent().);
             
 	        return (new Resource(status,
 	        			response.getEntity().getContent()));
 		}	
 		catch(Exception e){
 			throw new RuntimeException("Couldn't execute request:", e);
 		}
 	}
 	private HttpUriRequest add(HttpEntityEnclosingRequestBase m, String xml) {
 		try {
 			m.setEntity(new StringEntity(xml, "ISO-8859-1"));
 			m.setHeader("Content-Type",  "application/xml");
 			return m;
 		}
 		catch(Exception e){
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private String getEncodedCredentials() {
 		return(Base64.encodeBytes((this.username+":"+this.password).getBytes()));
 	}
 	
 	private HttpClient connect() {
 		DefaultHttpClient client = new DefaultHttpClient();
         client.getCredentialsProvider().setCredentials(
             new AuthScope(host, 80),            
             new UsernamePasswordCredentials(username, password)
         );        
 		return client;
 	}
 	
 	private String buildUri(String path) {
 		return protocol + host + "/" + path;
 	}
 }
