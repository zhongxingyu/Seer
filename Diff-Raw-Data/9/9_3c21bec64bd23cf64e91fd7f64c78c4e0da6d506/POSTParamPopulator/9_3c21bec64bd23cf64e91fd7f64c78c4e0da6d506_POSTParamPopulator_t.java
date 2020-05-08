 package com.lonepulse.zombielink.core.request;
 
 /*
  * #%L
  * ZombieLink
  * %%
  * Copyright (C) 2013 Lonepulse
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.http.HttpHeaders;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.entity.ContentType;
 import org.apache.http.message.BasicNameValuePair;
 
 import com.lonepulse.zombielink.core.annotation.Param;
 import com.lonepulse.zombielink.core.annotation.Request;
 import com.lonepulse.zombielink.core.processor.ProxyInvocationConfiguration;
 import com.lonepulse.zombielink.rest.annotation.Rest;
 
 /**
  * <p>This is a concrete implementation of {@link RequestPopulator} which serves {@link Request}s and 
  * {@link Rest}ful requests that employ {@link RequestMethod#POST}.</p>
  * 
 * <p>It acts on @{@link Param} annotations on the arguments to an endpoint interface method and constructs 
 * a string of <a href="http://en.wikipedia.org/wiki/POST_(HTTP)#Use_for_submitting_web_forms"> application/
 * x-www-form-urlencoded</a> key-value pairs to be included in the request body.</p> 
  * 
  * @version 1.1.0
  * <br><br>
  * @since 1.2.4
  * <br><br>
  * @author <a href="mailto:sahan@lonepulse.com">Lahiru Sahan Jayasinghe</a>
  */
 class POSTParamPopulator implements RequestPopulator {
 
 	
 	/**
 	 * <p>Accepts the {@link ProxyInvocationConfiguration} of a request which uses {@link RequestMethod#POST} 
 	 * and creates a string of <a href="http://en.wikipedia.org/wiki/POST_(HTTP)#Use_for_submitting_web_forms">
	 * application/x-www-form-urlencoded</a> key-value pairs using any arguments which were annotated with 
 	 * @{@link Param} and @{@link Request.Param}. This is then inserted into the body of a freshly constructed 
 	 * {@link HttpPost} instance, which is subsequently returned.</p>
 	 * 
 	 * <p>See {@link RequestPopulator#populate(ProxyInvocationConfiguration)}.</p>
 	 * 
 	 * @param config
 	 * 			an immutable instance of {@link ProxyInvocationConfiguration} which is used to create the 
 	 * 			form-urlencoded string and the {@link HttpPost} request
 	 * 
 	 * @return an instance of {@link HttpPost} with a body that includes a form-urlencoded string string (if any 
 	 * 		   request parameters were specified)
 	 * 
 	 * @throws RequestPopulatorException
 	 * 			if an {@link HttpPost} instance failed to be created or if a query parameter failed to be inserted
 	 * 
 	 * @since 1.2.4
 	 */
 	@Override
 	public HttpRequestBase populate(ProxyInvocationConfiguration config) throws RequestPopulatorException {
 
 		try {
 			
 			List<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();
 			
 			List<Request.Param> constantFormParams = RequestUtils.findConstantRequestParams(config);
 			Map<String, Object> formParams = RequestUtils.findRequestParams(config);
 			
 			for (Request.Param param : constantFormParams) {
 				
 				nameValuePairs.add(new BasicNameValuePair(param.name(), param.value()));
 			}
 			
 			for (Entry<String, Object> entry : formParams.entrySet()) {
 				
 				String name = entry.getKey();
 				Object value = entry.getValue();
 				
 				nameValuePairs.add(new BasicNameValuePair(name, String.valueOf(value)));
 			}
 			
 			UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairs);
 			urlEncodedFormEntity.setContentType(ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
 
 			HttpPost httpPost = new HttpPost(config.getUri());
 			httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
 			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
 			return httpPost;
 		}
 		catch(Exception e) {
 			
 			throw (e instanceof RequestPopulatorException)? 
 					(RequestPopulatorException)e :new RequestPopulatorException(e);
 		}
 	}
 }
