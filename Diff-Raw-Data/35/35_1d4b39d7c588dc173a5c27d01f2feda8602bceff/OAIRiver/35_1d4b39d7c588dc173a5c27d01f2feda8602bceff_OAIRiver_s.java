 /*
  * Licensed to ElasticSearch and Shay Banon under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. ElasticSearch licenses this
  * file to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.elasticsearch.river.oai;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.Date;
 import java.util.Map;
 import org.elasticsearch.ExceptionsHelper;
 import org.elasticsearch.action.get.GetResponse;
 import org.elasticsearch.client.Client;
 import static org.elasticsearch.client.Requests.indexRequest;
 import org.elasticsearch.cluster.block.ClusterBlockException;
 import org.elasticsearch.common.inject.Inject;
 import org.elasticsearch.common.unit.TimeValue;
 import org.elasticsearch.common.util.concurrent.EsExecutors;
 import org.elasticsearch.common.xcontent.XContentBuilder;
 import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
 import org.elasticsearch.common.xcontent.support.XContentMapValues;
 import org.elasticsearch.indices.IndexAlreadyExistsException;
 import org.elasticsearch.river.AbstractRiverComponent;
 import org.elasticsearch.river.River;
 import org.elasticsearch.river.RiverIndexName;
 import org.elasticsearch.river.RiverName;
 import org.elasticsearch.river.RiverSettings;
 import org.xbib.io.EmptyWriter;
 import org.xbib.io.util.DateUtil;
 import org.xbib.oai.ListRecordsRequest;
 import org.xbib.oai.ListRecordsResponse;
 import org.xbib.oai.MetadataPrefixService;
 import org.xbib.oai.MetadataReader;
 import org.xbib.oai.OAIOperation;
 import org.xbib.oai.ResumptionToken;
 import org.xbib.oai.client.OAIClient;
 import org.xbib.oai.client.OAIClientFactory;
 import org.xbib.rdf.Resource;
 import org.xbib.rdf.Statement;
 import org.xbib.rdf.io.StatementListener;
 import org.xbib.rdf.io.XmlHandler;
 import org.xbib.rdf.io.XmlTriplifier;
 import org.xbib.rdf.simple.SimpleResource;
 import org.xbib.xml.transform.StylesheetTransformer;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 public class OAIRiver extends AbstractRiverComponent implements River {
 
     private final Client client;
     private final String riverIndexName;
     private final String indexName;
     private final String typeName;
     private final BulkWrite operation;
     private final int bulkSize;
     private final int maxBulkRequests;
     private final TimeValue bulkTimeout;
     private volatile Thread harvesterThread;
     private volatile boolean closed;
     private volatile boolean failure;
     private final String oaiUrl;
     private final String oaiSet;
     private final String oaiMetadataPrefix;
     private String oaiFrom;
     private String oaiUntil;
     private final TimeValue oaiPoll;
     private final String oaiProxyHost;
     private final int oaiProxyPort;
     private final TimeValue oaiTimeout;
 
     @Inject
     public OAIRiver(RiverName riverName, RiverSettings settings,
             @RiverIndexName String riverIndexName, Client client) {
         super(riverName, settings);
         this.riverIndexName = riverIndexName;
         this.client = client;
         if (settings.settings().containsKey("oai")) {
             Map<String, Object> oaiSettings = (Map<String, Object>) settings.settings().get("oai");            
             oaiPoll = XContentMapValues.nodeTimeValue(oaiSettings.get("poll"), TimeValue.timeValueMinutes(60));
             oaiUrl = XContentMapValues.nodeStringValue(oaiSettings.get("url"), "http://localhost");
             oaiSet = XContentMapValues.nodeStringValue(oaiSettings.get("set"), null);
             oaiMetadataPrefix = XContentMapValues.nodeStringValue(oaiSettings.get("metadataPrefix"), null);
             oaiFrom = XContentMapValues.nodeStringValue(oaiSettings.get("from"), null);
             oaiUntil = XContentMapValues.nodeStringValue(oaiSettings.get("until"), null);
             oaiProxyHost = XContentMapValues.nodeStringValue(oaiSettings.get("proxyhost"), null);
             oaiProxyPort = XContentMapValues.nodeIntegerValue(oaiSettings.get("proxyport"), 0);
             oaiTimeout = XContentMapValues.nodeTimeValue(oaiSettings.get("timeout"), TimeValue.timeValueSeconds(60));
         } else {
             oaiPoll = TimeValue.timeValueMinutes(60);
             oaiUrl = "http://localhost";
             oaiSet = null;
             oaiMetadataPrefix = null;
             oaiFrom = null;
             oaiUntil = null;
             oaiProxyHost = null;
             oaiProxyPort = 0;
             oaiTimeout = TimeValue.timeValueSeconds(60);
         }
         if (settings.settings().containsKey("index")) {
             Map<String, Object> indexSettings = (Map<String, Object>) settings.settings().get("index");
             indexName = XContentMapValues.nodeStringValue(indexSettings.get("index"), "oai");
             typeName = XContentMapValues.nodeStringValue(indexSettings.get("type"), "default");
             bulkSize = XContentMapValues.nodeIntegerValue(indexSettings.get("bulk_size"), 100);
             maxBulkRequests = XContentMapValues.nodeIntegerValue(indexSettings.get("max_bulk_requests"), 30);
             if (indexSettings.containsKey("bulk_timeout")) {
                 bulkTimeout = TimeValue.parseTimeValue(XContentMapValues.nodeStringValue(indexSettings.get("bulk_timeout"), "60s"), TimeValue.timeValueMillis(60000));
             } else {
                 bulkTimeout = TimeValue.timeValueMillis(60000);
             }
         } else {
             indexName = "oai";
             typeName = "default";
             bulkSize = 100;
             maxBulkRequests = 30;
             bulkTimeout = TimeValue.timeValueMillis(60000);
         }
         operation = new BulkWrite(logger, indexName, typeName).setBulkSize(bulkSize).setMaxActiveRequests(maxBulkRequests).setMillisBeforeContinue(bulkTimeout.millis());
     }
 
     @Override
     public void start() {
         logger.info("starting OAI harvester: URL [{}], set [{}], metadataPrefix [{}], from [{}], until [{}], indexing to [{}]/[{}], poll [{}]",
                 oaiUrl, oaiSet, oaiMetadataPrefix, oaiFrom, oaiUntil, indexName, typeName, oaiPoll);
         try {
             client.admin().indices().prepareCreate(indexName).execute().actionGet();
         } catch (Exception e) {
             if (ExceptionsHelper.unwrapCause(e) instanceof IndexAlreadyExistsException) {
                 // that's fine
             } else if (ExceptionsHelper.unwrapCause(e) instanceof ClusterBlockException) {
                 // ok, not recovered yet..., lets start indexing and hope we recover by the first bulk
                 // TODO: a smarter logic can be to register for cluster event listener here, and only start sampling when the block is removed...
             } else {
                 logger.warn("failed to create index [{}], disabling river...", e, indexName);
                 return;
             }
         }
         harvesterThread = EsExecutors.daemonThreadFactory(settings.globalSettings(), "oai_river_harvester").newThread(new Harvester());
         harvesterThread.start();
     }
 
     @Override
     public void close() {
         if (closed) {
             return;
         }
         logger.info("closing");
         harvesterThread.interrupt();
         closed = true;
     }
 
    private ListRecordsRequest createOAIRequest() {
        return new ListRecordsRequest() {
 
             Date from;
             Date until;
             ResumptionToken token;
 
             @Override
             public void setFrom(Date from) {
                 this.from = from;
             }
 
             @Override
             public Date getFrom() {
                 return from;
             }
 
             @Override
             public void setUntil(Date until) {
                 this.until = until;
             }
 
             @Override
             public Date getUntil() {
                 return until;
             }
 
             @Override
             public String getSet() {
                 return oaiSet;
             }
 
             @Override
             public String getMetadataPrefix() {
                 return oaiMetadataPrefix;
             }
 
             @Override
             public void setResumptionToken(ResumptionToken token) {
                 this.token = token;
             }
 
             @Override
             public ResumptionToken getResumptionToken() {
                 return token;
             }
 
             @Override
             public String getPath() {
                 // only used in server context
                 return null;
             }
 
             @Override
             public Map<String, String[]> getParameterMap() {
                 // only used in server context
                 return null;
             }
        };
     }
 
     private class Harvester implements Runnable {
 
         private Resource resource;
 
         public void setResource(Resource resource) {
             this.resource = resource;
         }
 
         public Resource getResource() {
             return resource;
         }
 
         @Override
         public void run() {
             final OAIClient oaiClient = OAIClientFactory.getClient(oaiUrl);
             if (oaiProxyHost != null) {
                 oaiClient.setProxy(oaiProxyHost, oaiProxyPort);
             }
             if (oaiTimeout.millis() > 0) {
                 oaiClient.setTimeout(oaiTimeout.millis());
             }
             while (true) {
                 if (closed) {
                     return;
                 }
                 boolean shouldHarvest = false;
                final ListRecordsRequest request = createOAIRequest();
                 request.setFrom(DateUtil.parseDateISO(oaiFrom));
                 request.setUntil(DateUtil.parseDateISO(oaiUntil));
                 try {
                     client.admin().indices().prepareRefresh(riverIndexName).execute().actionGet();
                     GetResponse get = client.prepareGet(riverIndexName, riverName().name(), "_last").execute().actionGet();
                     if (!get.exists()) {
                         shouldHarvest = true;
                     } else {
                         Map<String, Object> oaiState = (Map<String, Object>) get.sourceAsMap().get("oai");
                         if (oaiState != null) {
                             // resume
                             Object resumptionToken = oaiState.get("resumptionToken");
                             if (resumptionToken != null) {
                                 ResumptionToken token = ResumptionToken.newToken(resumptionToken.toString());
                                 request.setResumptionToken(token);
                                 shouldHarvest = true;
                             } else {
                                 // advance time period
                                 Object lastFrom = oaiState.get("last_from");
                                 Object lastUntil = oaiState.get("last_until");
                                 if (lastFrom != null && lastUntil != null) {
                                     Date d1 = DateUtil.parseDateISO(lastFrom.toString());
                                     Date d2 = DateUtil.parseDateISO(lastUntil.toString());
                                     long delta = d2.getTime() - d1.getTime();
                                     d1.setTime(d1.getTime() + delta);
                                     d2.setTime(d2.getTime() + delta);
                                     // don't harvest into the future (now +/- 15 seconds)
                                     if (d2.getTime() < new Date().getTime() - 15000L) {
                                         request.setFrom(d1);
                                         request.setUntil(d2);
                                         shouldHarvest = true;
                                     }
                                 }
                             }
                         }
                     }
                 } catch (Exception e) {
                     logger.warn("failed to get last_OAI state, throttling", e);
                     try {
                         Thread.sleep(60000);
                     } catch (InterruptedException e1) {
                         if (closed) {
                             return;
                         }
                     }
                 }
                 if (!shouldHarvest) {
                     if (oaiPoll.millis() > 0L) {
                         logger.info("waiting {} for next run of URL [{}] set [{}] metadataPrefix [{}]",
                                 oaiPoll, oaiUrl, oaiSet, oaiMetadataPrefix);
                         try {
                             Thread.sleep(oaiPoll.millis());
                         } catch (InterruptedException e1) {
                             if (closed) {
                                 return;
                             }
                         }
                     }
                     continue;
                 }
                 logger.info("OAI harvest: URL [{}] set [{}] metadataPrefix [{}] from [{}] until [{}] resumptionToken [{}]",
                         oaiUrl, request.getSet(), request.getMetadataPrefix(),
                         request.getFrom(), request.getUntil(), request.getResumptionToken());
                 do {
                     EmptyWriter w = new EmptyWriter();
                     ListRecordsResponse response = new ListRecordsResponse(w);
                     XmlTriplifier reader = MetadataPrefixService.getTriplifier(oaiMetadataPrefix);
                     final StatementListener stmt = new StatementListener() {
 
                         @Override
                         public void newIdentifier(URI uri) {
                             getResource().setIdentifier(uri);
                         }
 
                         @Override
                         public void statement(Statement statement) {
                             getResource().add(statement);
                         }
                     };
                     reader.setListener(stmt);
                     final XmlHandler handler = reader.getHandler();
                     MetadataReader metadataReader = new MetadataReader() {
 
                         @Override
                         public void startDocument() throws SAXException {
                             handler.startDocument();
                             setResource(new SimpleResource());
                         }
 
                         @Override
                         public void endDocument() throws SAXException {
                             handler.endDocument();
                             if (resource.getIdentifier() == null) {
                                 resource.setIdentifier(URI.create(getHeader().getIdentifier()));
                             }
                             try {
                                 operation.write(client, resource);
                             } catch (IOException ex) {
                                 logger.error(ex.getMessage(), ex);
                                 failure = true;
                             }
                         }
 
                         @Override
                         public void startPrefixMapping(String string, String string1) throws SAXException {
                             handler.startPrefixMapping(string, string1);
                         }
 
                         @Override
                         public void endPrefixMapping(String string) throws SAXException {
                             handler.endPrefixMapping(string);
                         }
 
                         @Override
                         public void startElement(String ns, String localname, String string2, Attributes atrbts) throws SAXException {
                             handler.startElement(ns, localname, string2, atrbts);
                         }
 
                         @Override
                         public void endElement(String ns, String localname, String string2) throws SAXException {
                             handler.endElement(ns, localname, string2);
                         }
 
                         @Override
                         public void characters(char[] chars, int i, int i1) throws SAXException {
                             handler.characters(chars, i, i1);
                         }
                     };
                     oaiClient.setMetadataReader(metadataReader);
                     try {
                         StylesheetTransformer transformer = new StylesheetTransformer("xsl");
                         oaiClient.setStylesheetTransformer(transformer);
                        OAIOperation op = oaiClient.listRecords(request, response);
                        oaiClient.waitFor(op);
                        if (op.getStatusCodes().isEmpty()) {
                             logger.error("no response, failure");
                             failure = true;
                         } else {
                            logger.info("OAI response status codes " + op.getStatusCodes());
                        }
                        if (!op.getExceptions().isEmpty()) {
                            logger.error(op.getExceptions().toString());
                            failure = true;
                         }
                     } catch (IOException ex) {
                         logger.error(ex.getMessage(), ex);
                         failure = true;
                     }
                     if (response.getError() != null) {
                         logger.error("OAI response error " + response.getError());
                         failure = true;
                     }
                     // save state
                     try {
                         operation.flush(client);
                         XContentBuilder builder = jsonBuilder();
                         builder.startObject().startObject("oai");
                         if (request.getResumptionToken() != null) {
                             builder.field("resumptionToken", request.getResumptionToken());
                         }
                         if (request.getFrom() != null) {
                             builder.field("last_from", DateUtil.formatDateISO(request.getFrom()));
                         }
                         if (request.getUntil() != null) {
                             builder.field("last_until", DateUtil.formatDateISO(request.getUntil()));
                         }
                         builder.endObject().endObject();                        
                         client.prepareBulk().add(indexRequest(riverIndexName).type(riverName.name()).id("_last").source(builder)).execute().actionGet();                        
                     } catch (Exception ex) {
                         logger.error(ex.getMessage(), ex);
                         failure = true;
                     }
                 } while (request.getResumptionToken() != null && !failure && !closed);
                 // close if failure
                 if (failure) {
                     closed = true;
                 } else {
                     if (oaiPoll.millis() > 0L) {
                         logger.info("waiting {} for next run of URL [{}] set [{}] metadataPrefix [{}]",
                                 oaiPoll, oaiUrl, oaiSet, oaiMetadataPrefix);
                         try {
                             Thread.sleep(oaiPoll.millis());
                         } catch (InterruptedException e1) {
                             if (closed) {
                                 return;
                             }
                         }
                     }
                 }
             }
         }
     }
 }
