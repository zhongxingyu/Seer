 /**
  * Copyright 2011 the original author or authors.
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
 package com.jayway.jaxrs.hateoas.core;
 
 import javax.ws.rs.HttpMethod;
 import javax.ws.rs.core.MediaType;
 import java.net.URI;
 
 
 /**
  * @author Kalle Stenflo
  */
 public class HateoasLnkBuilderImpl extends HateoasLnk.HateoasLnkBuilder {
     @Override
     public HateoasLnk.HateoasLnkBuilder linkableId(String linkableId) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public HateoasLnk.HateoasLnkBuilder params(Object... params) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public HateoasLnk.HateoasLnkBuilder rel(String rel) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public HateoasLnk.HateoasLnkBuilder rel(HttpMethod method) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public HateoasLnk.HateoasLnkBuilder uri(URI uri) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public HateoasLnk.HateoasLnkBuilder consumes(MediaType... mediaTypes) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public HateoasLnk.HateoasLnkBuilder produces(MediaType... mediaTypes) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 }
