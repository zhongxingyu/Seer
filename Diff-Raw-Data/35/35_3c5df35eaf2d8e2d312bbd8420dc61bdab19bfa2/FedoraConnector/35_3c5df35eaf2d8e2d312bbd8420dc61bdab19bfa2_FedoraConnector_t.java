 package com.github.cwilper.fcrepo.cloudsync.service.backend;
 
 import com.github.cwilper.fcrepo.cloudsync.api.ObjectInfo;
 import com.github.cwilper.fcrepo.cloudsync.api.ObjectStore;
 import com.github.cwilper.fcrepo.cloudsync.service.util.JSON;
 import com.github.cwilper.fcrepo.cloudsync.service.util.StringUtil;
 import com.github.cwilper.fcrepo.dto.core.Datastream;
 import com.github.cwilper.fcrepo.dto.core.DatastreamVersion;
 import com.github.cwilper.fcrepo.dto.core.FedoraObject;
 import com.github.cwilper.fcrepo.httpclient.FedoraHttpClient;
 import com.github.cwilper.fcrepo.httpclient.HttpClientConfig;
 import com.github.cwilper.fcrepo.riclient.RIClient;
 import com.github.cwilper.fcrepo.riclient.RIQueryResult;
 import org.openrdf.model.Value;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.List;
 import java.util.Map;
 
 public class FedoraConnector extends StoreConnector {
 
     private final FedoraHttpClient httpClient;
     private final RIClient riClient;
 
     public FedoraConnector(ObjectStore store) {
         Map<String, String> map = JSON.getMap(JSON.parse(store.getData()));
         String url = StringUtil.validate("url", map.get("url"));
         String username = StringUtil.validate("username", map.get("username"));
         String password = StringUtil.validate("password", map.get("password"));
         httpClient = new FedoraHttpClient(new HttpClientConfig(),
                 URI.create(url), username, password);
         riClient = new RIClient(httpClient);
     }
 
     @Override
     public void countObjects(ObjectQuery query, ObjectCountHandler handler) {
     }
 
     @Override
     public void listObjects(ObjectQuery query, ObjectListHandler handler) {
         String type = query.getType();
         if (type.equals("pidPattern")) {
             RIQueryResult result = riClient.itql("select $o from <#ri> where $o <fedora-model:hasModel> <info:fedora/fedora-system:FedoraObject-3.0>", false);
             listObjects(result, new PIDPatternFilter(query.getPidPattern()), handler);
         } else if (type.equals("pidList")) {
             // TODO: Check if each objects exists
             throw new UnsupportedOperationException("pidList not supported yet.");
         } else if (type.equals("query")) {
             RIQueryResult result;
             if (query.getQueryType().equals("iTQL")) {
                 result = riClient.itql(query.getQueryText(), false);
             } else if (query.getQueryType().equals("SPARQL")) {
                 result = riClient.sparql(query.getQueryText(), false);
             } else {
                 throw new IllegalArgumentException("Query type '" + query.getQueryType() + "' unrecognized.");
             }
             listObjects(result, null, handler);
         }
     }
 
     @Override
     public FedoraObject getObject(String pid) {
         return null;
     }
 
     @Override
     public InputStream getContent(FedoraObject o, Datastream ds, DatastreamVersion dsv) {
         return null;
     }
 
     private void listObjects(RIQueryResult result, PIDPatternFilter filter, ObjectListHandler handler) {
         try {
             boolean keepGoing = true;
             while (result.hasNext() && keepGoing) {
                 List<Value> row = result.next();
                 // info:fedora/ = 12
                 String pid = row.get(0).toString().substring(12);
                 if (filter == null || filter.accept(pid) != null) {
                     ObjectInfo o = new ObjectInfo();
                     o.setPid(pid);
                     keepGoing = handler.handleObject(o);
                 }
             }
         } catch (IOException e) {
             throw new RuntimeException("Error iterating query results", e);
         } finally {
             result.close();
         }
     }
 }
