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
 public class HateoasLnk {
 
     /**
      * Protected constructor, use one of the static methods to obtain a
      * {@link com.jayway.jaxrs.hateoas.core.HateoasLnk.HateoasLnkBuilder} instance and obtain a HateoasLnk from that.
      */
     protected HateoasLnk() {
     }
 
     /**
      * Create a new HateoasLnkBuilder for the provided linkable method.
      *
      * @param linkableId the id of the linkable method
      * @return a new HateoasLnkBuilder
      * @throws IllegalArgumentException if linkableId is null or not available in HateoasContext.
      */
     public static HateoasLnkBuilder linkableId(String linkableId) {
         HateoasLnkBuilder b = HateoasLnkBuilder.newInstance();
         b.linkableId(linkableId);
         return b;
     }
 
     /**
      * Create a new HateoasLnkBuilder for the provided URI.
      *
      * @param uri the URI this link will expose as href
      * @return a new HateoasLnkBuilder
      * @throws IllegalArgumentException if uri is null.
      */
     public static HateoasLnkBuilder uri(URI uri) {
         HateoasLnkBuilder b = HateoasLnkBuilder.newInstance();
         b.uri(uri);
         return b;
     }
 
 
     public static abstract class HateoasLnkBuilder {
 
         /**
          * Create a new builder instance.
          *
          * @return a new ResponseBuilder
          */
         protected static HateoasLnkBuilder newInstance() {
             HateoasLnkBuilder b = new HateoasLnkBuilderImpl();
             return b;
         }
 
         /**
          * Set the linkableId on the HateoasLnkBuilder.
          *
          * @param linkableId the id of the linkable method
          * @return the updated HateoasLnkBuilder
          * @throws IllegalArgumentException if linkableId is null or not available in HateoasContext.
          */
         public abstract HateoasLnkBuilder linkableId(String linkableId);
 
         /**
          * Expands the path parameters and query parameters for the method identified by the given linkableId.
          *
          * @param params parameters to populate the linkable URI with
          * @return the updated HateoasLnkBuilder
          * @throws IllegalArgumentException if params is null or empty.
          */
         public abstract HateoasLnkBuilder params(Object... params);
 
         /**
          * Set the rel on the HateoasLnkBuilder.
          *
          * @param rel to be used in the link
          * @return the updated HateoasLnkBuilder
          * @throws IllegalArgumentException if rel is null or empty.
          */
         public abstract HateoasLnkBuilder rel(String rel);
 
         /**
          * Set the HttpMethod on the HateoasLnkBuilder.
          *
          * @param method to be used in the link
          * @return the updated HateoasLnkBuilder
          * @throws IllegalArgumentException if method is null.
          */
         public abstract HateoasLnkBuilder rel(HttpMethod method);
 
         /**
          * Set the URI on the HateoasLnkBuilder.
          *
          * @param uri the URI this link will expose as href
          * @return the updated HateoasLnkBuilder
          * @throws IllegalArgumentException if uri is null.
          */
         public abstract HateoasLnkBuilder uri(URI uri);
 
         /**
          * Set the MediaTypes that the link will consume.
          *
          * @param mediaTypes the MediaTypes this link will consume
          * @return the updated HateoasLnkBuilder
          * @throws IllegalArgumentException if mediaTypes is null or empty.
          */
         public abstract HateoasLnkBuilder consumes(MediaType... mediaTypes);
 
         /**
          * Set the MediaTypes that the link will produce.
          *
          * @param mediaTypes the MediaTypes this link will produce
          * @return the updated HateoasLnkBuilder
          * @throws IllegalArgumentException if mediaTypes is null or empty.
          */
         public abstract HateoasLnkBuilder produces(MediaType... mediaTypes);
 
     }
 }
