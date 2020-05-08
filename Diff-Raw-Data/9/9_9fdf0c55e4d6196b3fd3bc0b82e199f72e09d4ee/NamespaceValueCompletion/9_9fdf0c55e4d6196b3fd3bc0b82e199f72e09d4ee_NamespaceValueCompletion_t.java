 /*
  *  Copyright 2013 OpenBEL Consortium
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.openbel.rest.common;
 
 import static org.openbel.rest.common.Objects.*;
 import static org.openbel.rest.main.*;
 import static org.openbel.rest.Util.*;
 import static java.util.regex.Pattern.*;
 import static java.lang.String.format;
 import static java.net.URLDecoder.*;
 import org.jongo.*;
 import java.util.*;
 import java.util.regex.*;
 import org.restlet.resource.Get;
 import org.restlet.resource.ServerResource;
 import org.restlet.representation.Representation;
 import org.restlet.data.Status;
 import org.openbel.rest.Path;
 import org.bson.types.ObjectId;
 
 @Path("/api/v1/completion/namespace-value/{keyword}/{value}")
 public class NamespaceValueCompletion extends ServerResource {
     private static final String ROOT_FIND;
     private static final String ROOT_PROJECTION;
     private static final Map<ObjectId, String> PREFIX_MAP;
     private static final String ALT_URL;
     private static final String FIND_ONE;
     private static final String FIND_VALS;
     private static final String FIND_SYNS;
     static {
         ROOT_FIND = "{}";
         ROOT_PROJECTION = "{description: 0}";
         ALT_URL = "/api/v1/completion/namespace-value/";
         FIND_ONE = "{keyword: '%s'}";
         FIND_VALS = "{nsmeta-id:#, norm:#}";
         FIND_SYNS = "{nsmeta-id:#, synonyms:#}";
         PREFIX_MAP = new HashMap<>();
         Find find = $namespaces.find(ROOT_FIND);
         for (Map<?, ?> map : find.as(Map.class)) {
             ObjectId oid = (ObjectId) map.get("_id");
             String keyword = (String) map.get("keyword");
             PREFIX_MAP.put(oid, keyword);
         }
     }
 
     @Get("json")
     public Representation _get() {
         String keyword = getAttribute("keyword");
         String value = getAttribute("value");
         try {
             value = decode(value, "UTF-8");
         } catch (Exception e) {}
        Response ret = new Response();
        ret.addLink("self", ALT_URL + keyword + "/" + value);
 
         String query = format(FIND_ONE, keyword);
         @SuppressWarnings("unchecked")
         Map<?, ?> ns = $namespaces.findOne(query).as(Map.class);
         if (ns == null) {
             setStatus(Status.SUCCESS_NO_CONTENT);
             return ret.json();
         }
 
         String input = format("^%s", escapeRE(value));
         Pattern ptrn = compile(input.toLowerCase());
         Find find = $nsvalues.find(FIND_VALS, (ns.get("_id")), ptrn);
 
         List<NSValueCompletion> rslts = new ArrayList<>();
 
         // Autocomplete values first
         for (Map<?, ?> map : find.as(Map.class)) {
             if (rslts.size() == 20) break;
             String prefix = PREFIX_MAP.get((ObjectId) map.get("nsmeta-id"));
             String val = (String) map.get("val");
             @SuppressWarnings("unchecked")
             List<String> syns = (List<String>) map.get("synonyms");
             NSValueCompletion nvc = new NSValueCompletion(prefix, val, syns);
             rslts.add(nvc);
         }
 
         // Autocomplete synonyms second
         find = $nsvalues.find(FIND_SYNS, (ns.get("_id")), ptrn);
         for (Map<?, ?> map : find.as(Map.class)) {
             if (rslts.size() == 20) break;
             String prefix = PREFIX_MAP.get((ObjectId) map.get("nsmeta-id"));
             String val = (String) map.get("val");
             @SuppressWarnings("unchecked")
             List<String> syns = (List<String>) map.get("synonyms");
             NSValueCompletion nvc = new NSValueCompletion(prefix, val, syns);
             nvc.matchedSynonym();
             rslts.add(nvc);
         }
 
         if (rslts.size() == 0) {
             setStatus(Status.SUCCESS_NO_CONTENT);
             return ret.json();
         }
 
         for (NSValueCompletion nvc : rslts) ret.addValue(nvc);
         if (rslts.size() == 1) return ret.json();
 
         for (NSValueCompletion nvc : rslts) {
             ret.addLink("result", ALT_URL + keyword + "/" + nvc.value);
         }
         ret.addLink("self", ALT_URL + keyword + "/" + value);
         setStatus(Status.SUCCESS_ACCEPTED);
         return ret.json();
     }
 
     static class Response extends Objects.Base {
         List<NSValueCompletion> values;
         {
             addDocumentation("namespace-completion");
         }
         void addValue(NSValueCompletion nvc) {
             if (values == null) {
                 values = new ArrayList<>();
                 put("values", values);
             }
             values.add(nvc);
         }
     }
 
 }
