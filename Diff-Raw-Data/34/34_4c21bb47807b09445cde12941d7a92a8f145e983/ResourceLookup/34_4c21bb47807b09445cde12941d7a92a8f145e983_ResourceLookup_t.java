 /**
  * Copyright 2009-2010 the original author or authors.
  *
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
  */
 package net.javacrumbs.springws.test.lookup;
 
 import java.io.IOException;
 import java.net.URI;
 
 import org.springframework.core.io.Resource;
 import org.springframework.ws.WebServiceMessage;
 
 /**
  * Searches for resources and loads it. If the resource is a template, it might be processed when looked-up. 
  * 
  * @author Lukas Krecan
  * 
  */
 public interface ResourceLookup {
 	/**
 	 * Looks for appropriate resource.
 	 * 
 	 * @param uri
	 * @param message Message to be used as input for the template or as a source of data for lookup.
 	 * @return null if not found.
 	 * @throws IOException 
 	 */
 	public Resource lookupResource(URI uri, WebServiceMessage message) throws IOException;
 }
