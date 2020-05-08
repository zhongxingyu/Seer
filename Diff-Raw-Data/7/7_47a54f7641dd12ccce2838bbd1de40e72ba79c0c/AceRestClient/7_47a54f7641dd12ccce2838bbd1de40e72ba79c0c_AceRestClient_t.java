 /*
  * Copyright (c) 2013 Dmytro Pishchukhin
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.ops4j.pax.maven.plugins.ace.rest;
 
 import org.glassfish.jersey.SslConfigurator;
 import org.glassfish.jersey.apache.connector.ApacheClientProperties;
 import org.glassfish.jersey.apache.connector.ApacheConnector;
 import org.glassfish.jersey.client.ClientConfig;
 import org.glassfish.jersey.client.ClientProperties;
 import org.glassfish.jersey.client.filter.HttpBasicAuthFilter;
 import org.ops4j.pax.maven.plugins.ace.rest.model.*;
 import org.ops4j.pax.maven.plugins.ace.rest.utils.AceRequestBuilder;
 import org.ops4j.pax.maven.plugins.ace.rest.utils.AceResponsesParser;
 import org.osgi.framework.Filter;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.InvalidSyntaxException;
 
 import javax.ws.rs.client.Client;
 import javax.ws.rs.client.ClientBuilder;
 import javax.ws.rs.client.Entity;
 import javax.ws.rs.client.WebTarget;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.io.File;
 import java.io.FileInputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author dpishchukhin
  */
 public class AceRestClient {
     private static final String CONTEXT_ARTIFACT = "artifact";
     private static final String CONTEXT_FEATURE = "feature";
     private static final String CONTEXT_DISTRIBUTION = "distribution";
     private static final String CONTEXT_TARGET = "target";
     private static final String CONTEXT_DISTRIBUTION_2_TARGET = "distribution2target";
     private static final String CONTEXT_FEATURE_2_DISTRIBUTION = "feature2distribution";
     private static final String CONTEXT_ARTIFACT_2_FEATURE = "artifact2feature";
 
     private static final String ACTION_REGISTER = "register";
     private static final String ACTION_APPROVE = "approve";
     private static final String ACTION_AUDIT_EVENTS = "auditEvents";
     private static final String ACTION_PARAM_START = "start";
     private static final String ACTION_PARAM_MAX = "max";
 
     private URI workLocation;
     private AceRestClientConfig config;
 
     public AceRestClient(AceRestClientConfig config) {
         this.config = config;
     }
 
     public void open() throws Exception {
         Response response = createClient()
                 .target(this.config.getServerUri())
                 .path(this.config.getClientPath())
                 .request().post(null);
         if (response.getStatus() == 302) {
             workLocation = response.getLocation();
         } else {
             throw createRestException(response);
         }
     }
 
     public void persist() throws Exception {
         Response response = createClient()
                 .target(this.workLocation)
                 .request().post(null);
         if (response.getStatus() != 200) {
             throw createRestException(response);
         }
     }
 
     public void close() throws Exception {
         Response response = createClient()
                 .target(this.workLocation)
                 .request().delete();
         if (response.getStatus() != 200) {
             throw createRestException(response);
         }
     }
 
     public AceStorage loadStorage(RequestFilter requestFilter) throws Exception {
         AceStorage aceStorage = new AceStorage();
 
         aceStorage.addTargets(loadEntries(CONTEXT_TARGET, AceTarget.class, requestFilter.isTargetsInclude(), requestFilter.getTargetsFilter()));
         aceStorage.addDistributions(loadEntries(CONTEXT_DISTRIBUTION, AceDistribution.class, requestFilter.isDistributionsInclude(), requestFilter.getDistributionsFilter()));
         aceStorage.addFeatures(loadEntries(CONTEXT_FEATURE, AceFeature.class, requestFilter.isFeaturesInclude(), requestFilter.getFeaturesFilter()));
         aceStorage.addArtifacts(loadEntries(CONTEXT_ARTIFACT, AceArtifact.class, requestFilter.isArtifactsInclude(), requestFilter.getArtifactsFilter()));
 
         loadLinks(aceStorage);
 
         return aceStorage;
     }
 
     public List<AceDistribution> loadDistributions(String filter) throws Exception {
         return loadEntries(CONTEXT_DISTRIBUTION, AceDistribution.class, true, createFilter(filter));
     }
 
     public List<AceTarget> loadTargets(String filter) throws Exception {
         return loadEntries(CONTEXT_TARGET, AceTarget.class, true, createFilter(filter));
     }
 
     public List<AceFeature> loadFeatures(String filter) throws Exception {
         return loadEntries(CONTEXT_FEATURE, AceFeature.class, true, createFilter(filter));
     }
 
     public List<AceArtifact> loadArtifacts(String filter) throws Exception {
         return loadEntries(CONTEXT_ARTIFACT, AceArtifact.class, true, createFilter(filter));
     }
 
     public List<String> deleteArtifacts(String filter, Action<AceArtifact> postAction) throws Exception {
         return deleteEntries(CONTEXT_ARTIFACT, AceArtifact.class, filter, postAction);
     }
 
     public List<String> deleteTargets(String filter, Action<AceTarget> postAction) throws Exception {
         return deleteEntries(CONTEXT_TARGET, AceTarget.class, filter, postAction);
     }
 
     public List<String> deleteFeatures(String filter, Action<AceFeature> postAction) throws Exception {
         return deleteEntries(CONTEXT_FEATURE, AceFeature.class, filter, postAction);
     }
 
     public List<String> deleteDistributions(String filter, Action<AceDistribution> postAction) throws Exception {
         return deleteEntries(CONTEXT_DISTRIBUTION, AceDistribution.class, filter, postAction);
     }
 
     public void deleteAll() throws Exception {
         deleteTargets(null, new Action<AceTarget>() {
             @Override
             public void execute(AceRestClient client, AceTarget entry) throws Exception {
                 client.deleteDistribution2TargetLinks(client.loadDistributions(null),
                         Collections.singletonList(entry));
             }
         });
         deleteDistributions(null, new Action<AceDistribution>() {
             @Override
             public void execute(AceRestClient client, AceDistribution entry) throws Exception {
                 client.deleteFeature2DistributionLinks(client.loadFeatures(null),
                         Collections.singletonList(entry));
             }
         });
         deleteFeatures(null, new Action<AceFeature>() {
             @Override
             public void execute(AceRestClient client, AceFeature entry) throws Exception {
                 client.deleteArtifactDFeatureLinks(client.loadArtifacts(null),
                         Collections.singletonList(entry));
             }
         });
         deleteArtifacts(null, new Action<AceArtifact>() {
             @Override
             public void execute(AceRestClient client, AceArtifact entry) throws Exception {
                 client.purgeArtifact(entry.getAttributes().get(AceArtifact.URL_ATTR));
             }
         });
     }
 
     public String createFeature(AceFeature feature) throws Exception {
         return createEntry(CONTEXT_FEATURE, feature);
     }
 
     public String createDistribution(AceDistribution distribution) throws Exception {
         return createEntry(CONTEXT_DISTRIBUTION, distribution);
     }
 
     public List<String> createDistribution2TargetLinks(String distributionsFilter, String targetsFilter) throws Exception {
         List<AceTarget> targets = loadEntries(CONTEXT_TARGET, AceTarget.class,
                 true, createFilter(targetsFilter));
         List<AceDistribution> distributions = loadEntries(CONTEXT_DISTRIBUTION, AceDistribution.class,
                 true, createFilter(distributionsFilter));
         return createLinks(CONTEXT_DISTRIBUTION_2_TARGET, distributions, targets);
     }
 
     public List<String> createFeature2DistributionLinks(String featuresFilter, String distributionsFilter) throws Exception {
         List<AceFeature> features = loadEntries(CONTEXT_FEATURE, AceFeature.class,
                 true, createFilter(featuresFilter));
         List<AceDistribution> distributions = loadEntries(CONTEXT_DISTRIBUTION, AceDistribution.class,
                 true, createFilter(distributionsFilter));
         return createLinks(CONTEXT_FEATURE_2_DISTRIBUTION, features, distributions);
     }
 
     public List<String> createArtifactDFeatureLinks(String artifactsFilter, String featuresFilter) throws Exception {
         List<AceFeature> features = loadEntries(CONTEXT_FEATURE, AceFeature.class,
                 true, createFilter(featuresFilter));
         List<AceArtifact> artifacts = loadEntries(CONTEXT_ARTIFACT, AceArtifact.class,
                 true, createFilter(artifactsFilter));
         return createLinks(CONTEXT_ARTIFACT_2_FEATURE, artifacts, features);
     }
 
     public List<String> deleteDistribution2TargetLinks(String distributionsFilter, String targetsFilter) throws Exception {
         List<AceTarget> targets = loadEntries(CONTEXT_TARGET, AceTarget.class,
                 true, createFilter(targetsFilter));
         List<AceDistribution> distributions = loadEntries(CONTEXT_DISTRIBUTION, AceDistribution.class,
                 true, createFilter(distributionsFilter));
         return deleteDistribution2TargetLinks(distributions, targets);
     }
 
     public List<String> deleteDistribution2TargetLinks(List<AceDistribution> distributions, List<AceTarget> targets) throws Exception {
         return deleteLinks(CONTEXT_DISTRIBUTION_2_TARGET, distributions, targets);
     }
 
     public List<String> deleteFeature2DistributionLinks(List<AceFeature> features, List<AceDistribution> distributions) throws Exception {
         return deleteLinks(CONTEXT_FEATURE_2_DISTRIBUTION, features, distributions);
     }
 
     public List<String> deleteArtifactDFeatureLinks(List<AceArtifact> artifacts, List<AceFeature> features) throws Exception {
         return deleteLinks(CONTEXT_ARTIFACT_2_FEATURE, artifacts, features);
     }
 
     public List<String> deleteFeature2DistributionLinks(String featuresFilter, String distributionsFilter) throws Exception {
         List<AceFeature> features = loadEntries(CONTEXT_FEATURE, AceFeature.class,
                 true, createFilter(featuresFilter));
         List<AceDistribution> distributions = loadEntries(CONTEXT_DISTRIBUTION, AceDistribution.class,
                 true, createFilter(distributionsFilter));
         return deleteFeature2DistributionLinks(features, distributions);
     }
 
     public List<String> deleteArtifactDFeatureLinks(String artifactsFilter, String featuresFilter) throws Exception {
         List<AceFeature> features = loadEntries(CONTEXT_FEATURE, AceFeature.class,
                 true, createFilter(featuresFilter));
         List<AceArtifact> artifacts = loadEntries(CONTEXT_ARTIFACT, AceArtifact.class,
                 true, createFilter(artifactsFilter));
         return deleteArtifactDFeatureLinks(artifacts, features);
     }
 
     private <L extends AceEntry, R extends AceEntry> List<String> createLinks(String context, List<L> leftEntries, List<R> rightEntries) throws Exception {
         List<String> createIds = new ArrayList<String>();
         if (leftEntries != null && rightEntries != null) {
             for (L leftEntry : leftEntries) {
                 for (R rightEntry : rightEntries) {
                     createIds.add(createLink(context, leftEntry, rightEntry));
                 }
             }
         }
         return createIds;
     }
 
     private <L extends AceEntry, R extends AceEntry> List<String> deleteLinks(String context, List<L> leftEntries, List<R> rightEntries) throws Exception {
         List<String> deleteIds = new ArrayList<String>();
         if (leftEntries != null && rightEntries != null) {
             List<AceLink> links = loadEntries(context, AceLink.class, true, null);
             for (L leftEntry : leftEntries) {
                 String leftFilter = leftEntry.getIdFilter();
                 for (R rightEntry : rightEntries) {
                     String rightFilter = rightEntry.getIdFilter();
                     for (AceLink link : links) {
                         // todo: compare links better!!!
                         if (leftFilter.equals(link.getLeftEndpoint())
                                 && rightFilter.equals(link.getRightEndpoint())) {
                             deleteIds.add(deleteEntry(context, link.getId()));
                         }
                     }
                 }
             }
         }
         return deleteIds;
     }
 
     private Filter createFilter(String filterString) throws InvalidSyntaxException {
         return filterString == null ? null : FrameworkUtil.createFilter(filterString);
     }
 
     private <L extends AceEntry, R extends AceEntry> String createLink(String context, L leftEntry, R rightEntry) throws Exception {
         AceLink link = new AceLink();
         link.setLeftEndpoint(leftEntry.getIdFilter());
         link.setRightEndpoint(rightEntry.getIdFilter());
 
         return createEntry(context, link);
     }
 
     public String createTarget(AceTarget target) throws Exception {
         return createEntry(CONTEXT_TARGET, target);
     }
 
     public String createArtifact(AceArtifact artifact, File file) throws Exception {
         return createEntry(CONTEXT_ARTIFACT, artifact);
     }
 
     public void registerTargets(String filter) throws Exception {
         List<AceTarget> targets = loadEntries(CONTEXT_TARGET, AceTarget.class, true, createFilter(filter));
         if (targets != null) {
             for (AceTarget target : targets) {
                 executeAction(ACTION_REGISTER, target.getId());
             }
         }
     }
 
     public void approveTargets(String filter) throws Exception {
         List<AceTarget> targets = loadEntries(CONTEXT_TARGET, AceTarget.class, true, createFilter(filter));
         if (targets != null) {
             for (AceTarget target : targets) {
                 executeAction(ACTION_APPROVE, target.getId());
             }
         }
     }
 
     public AuditLogBook getAuditEventsForTargets(String filter, int start, int max) throws Exception {
         List<AceTarget> targets = loadEntries(CONTEXT_TARGET, AceTarget.class, true, createFilter(filter));
         List<AuditLog> result = new ArrayList<AuditLog>();
         if (targets != null) {
             for (AceTarget target : targets) {
                 WebTarget path = createClient()
                         .target(workLocation)
                         .path(CONTEXT_TARGET).path(target.getId()).path(ACTION_AUDIT_EVENTS);
                 if (start >= 0) {
                     path.queryParam(ACTION_PARAM_START, start);
                 }
                 if (max >= 0) {
                     path.queryParam(ACTION_PARAM_MAX, max);
                 }
                 Response response = path
                         .request(MediaType.APPLICATION_JSON_TYPE)
                         .get();
                 if (response.getStatus() != 200) {
                     throw createRestException(response);
                 }
                 if (response.hasEntity()) {
                     AuditLog auditLog = new AuditLog(target);
                     result.add(auditLog);
                     auditLog.setEvents(AceResponsesParser.parseAuditEvents(response.readEntity(String.class)));
                 }
             }
         }
         return new AuditLogBook(result);
     }
 
     private void executeAction(String actionName, String id) throws Exception {
         Response response = createClient()
                 .target(workLocation)
                 .path(CONTEXT_TARGET).path(id).path(actionName)
                 .request(MediaType.APPLICATION_JSON_TYPE)
                 .post(null);
         if (response.getStatus() != 200) {
             throw createRestException(response);
         }
     }
 
     public void purgeArtifact(String url) throws Exception {
         Response response = createClient()
                 .target(url)
                 .request()
                 .delete();
         if (response.getStatus() != 200) {
             throw createRestException(response);
         }
     }
 
     public String uploadArtifact(File file) throws Exception {
         FileInputStream in = new FileInputStream(file);
         Response response;
         try {
             response = createClient()
                     .target(this.config.getServerUri())
                     .path(this.config.getObrPath())
                     .queryParam("filename", file.getName())
                     .request()
                     .post(Entity.entity(in, MediaType.APPLICATION_OCTET_STREAM_TYPE));
         } finally {
             in.close();
         }
         if (response.getStatus() == 201) {
             return response.getLocation().toString();
         } else {
             throw createRestException(response);
         }
     }
 
     private Exception createRestException(Response response) {
         return new Exception(
                 String.format("%s - %s: %s"
                         , response.getStatusInfo().getStatusCode()
                         , response.getStatusInfo().getReasonPhrase()
                         , response.getStringHeaders()
                 )
         );
     }
 
     private <T extends AceEntry> String createEntry(String context, T entry) throws Exception {
         // validate
         entry.validate();
 
         String json = AceRequestBuilder.buildJson(entry);
 
         Response response = createClient()
                 .target(workLocation)
                 .path(context)
                 .request(MediaType.APPLICATION_JSON_TYPE)
                 .post(Entity.json(json));
         if (response.getStatus() == 302) {
             return readIdFromLocation(response.getLocation());
         } else {
             throw createRestException(response);
         }
     }
 
     private static String readIdFromLocation(URI location) {
         String path = location.getPath();
         return path.substring(path.lastIndexOf('/') + 1);
     }
 
     private <T extends AceEntry> List<String> deleteEntries(String context, Class<T> entryClass, String filter, Action<T> postAction) throws Exception {
         List<T> entries = loadEntries(context, entryClass, true, createFilter(filter));
         List<String> result = new ArrayList<String>();
         for (T entry : entries) {
             deleteEntry(context, entry.getId());
             result.add(entry.getId());
             if (postAction != null) {
                 postAction.execute(this, entry);
             }
         }
         return result;
     }
 
     private void loadLinks(AceStorage aceStorage) throws Exception {
         List<AceLink> distributionToTargetLinks = loadEntries(CONTEXT_DISTRIBUTION_2_TARGET, AceLink.class, true, null);
         if (distributionToTargetLinks != null) {
             for (AceLink link : distributionToTargetLinks) {
                 List<AceDistribution> distributions = aceStorage.getDistributions(link.getLeftEndpointFilter());
                 List<AceTarget> targets = aceStorage.getTargets(link.getRightEndpointFilter());
                 for (AceDistribution distribution : distributions) {
                     for (AceTarget target : targets) {
                         distribution.addTarget(target.getId());
                         target.addDistribution(distribution.getId());
                     }
                 }
             }
         }
         List<AceLink> featureToDistributionLinks = loadEntries(CONTEXT_FEATURE_2_DISTRIBUTION, AceLink.class, true, null);
         if (featureToDistributionLinks != null) {
             for (AceLink link : featureToDistributionLinks) {
                 List<AceFeature> features = aceStorage.getFeatures(link.getLeftEndpointFilter());
                 List<AceDistribution> distributions = aceStorage.getDistributions(link.getRightEndpointFilter());
                 for (AceFeature feature : features) {
                     for (AceDistribution distribution : distributions) {
                         distribution.addFeature(feature.getId());
                         feature.addDistribution(distribution.getId());
                     }
                 }
             }
         }
         List<AceLink> artifactToFeatureLinks = loadEntries(CONTEXT_ARTIFACT_2_FEATURE, AceLink.class, true, null);
         if (artifactToFeatureLinks != null) {
             for (AceLink link : artifactToFeatureLinks) {
                 List<AceArtifact> artifacts = aceStorage.getArtifacts(link.getLeftEndpointFilter());
                 List<AceFeature> features = aceStorage.getFeatures(link.getRightEndpointFilter());
                 for (AceArtifact artifact : artifacts) {
                     for (AceFeature feature : features) {
                         feature.addArtifact(artifact.getId());
                         artifact.addFeature(feature.getId());
                     }
                 }
             }
         }
     }
 
     private <T extends AceEntry> List<T> loadEntries(String context, Class<T> entryClass, boolean include, Filter filter) throws Exception {
         List<T> result = new ArrayList<T>();
         if (include) {
             List<String> list = getList(context);
             if (list != null) {
                 for (String id : list) {
                     T entry = loadEntry(id, context, entryClass);
                    if (entry != null && (filter == null || filter.match(entry.getAttributes()))) {
                         result.add(entry);
                     }
                 }
             }
         }
         return result;
     }
 
     private <T extends AceEntry> T loadEntry(String id, String context, Class<T> entryClass) throws Exception {
         Response response = createClient()
                 .target(this.workLocation)
                 .path(context + "/" + id)
                 .request()
                 .get();
        if (response.getStatus() == 404) {
            return null;
        } else if (response.getStatus() != 200) {
             throw createRestException(response);
         }
         if (response.hasEntity()) {
             return AceResponsesParser.parseEntry(response.readEntity(String.class), entryClass);
         }
         return null;
     }
 
     private List<String> getList(String context) throws Exception {
         Response response = createClient()
                 .target(this.workLocation)
                 .path(context)
                 .request()
                 .get();
         if (response.getStatus() != 200) {
             throw createRestException(response);
         }
 
         if (response.hasEntity()) {
             return AceResponsesParser.parseIdsArray(response.readEntity(String.class));
         }
         return null;
     }
 
     private String deleteEntry(String context, String id) throws Exception {
         Response response = createClient()
                 .target(this.workLocation)
                 .path(context + "/" + id)
                 .request()
                 .delete();
         if (response.getStatus() != 200) {
             throw createRestException(response);
         }
         return id;
     }
 
     private Client createClient() throws URISyntaxException {
         return createClientBuilder().build();
     }
 
     private ClientBuilder createClientBuilder() throws URISyntaxException {
         ClientBuilder builder = ClientBuilder.newBuilder();
         ClientConfig clientConfig = new ClientConfig();
         builder.property(ClientProperties.FOLLOW_REDIRECTS, false);
 
         if (config.getUsername() != null) {
             clientConfig.register(new HttpBasicAuthFilter(config.getUsername(), config.getPassword()));
         }
 
         SslConfigurator sslConfigurator = SslConfigurator.newInstance(false);
         if (config.getKeyStore() != null && config.getPrivateKeyPassword() != null) {
             sslConfigurator.keyStore(config.getKeyStore());
             sslConfigurator.keyStorePassword(config.getPrivateKeyPassword());
         }
         if (config.getTrustStore() != null) {
             sslConfigurator.trustStore(config.getTrustStore());
         }
         clientConfig.property(ApacheClientProperties.SSL_CONFIG, sslConfigurator);
 
         if (config.getProxy() != null) {
             clientConfig.property(ApacheClientProperties.PROXY_URI, config.getProxy().toURI());
         }
         clientConfig.connector(new ApacheConnector(clientConfig));
         builder.withConfig(clientConfig);
 
         return builder;
     }
 
 }
