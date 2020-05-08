 /*
  * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     Olivier Grisel
  */
 package org.nuxeo.ecm.platform.semanticentities.sources;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.ws.rs.core.UriBuilder;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.nuxeo.ecm.core.api.Blob;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;
 import org.nuxeo.ecm.core.api.model.Property;
 import org.nuxeo.ecm.core.api.model.PropertyException;
 import org.nuxeo.ecm.core.schema.types.Type;
 import org.nuxeo.ecm.core.schema.types.primitives.StringType;
 import org.nuxeo.ecm.platform.semanticentities.DereferencingException;
 import org.nuxeo.ecm.platform.semanticentities.RemoteEntity;
 import org.nuxeo.ecm.platform.semanticentities.service.ParameterizedHTTPEntitySource;
 import org.nuxeo.ecm.platform.semanticentities.service.RemoteEntitySourceDescriptor;
 
 /**
  * Implementation of the RemoteEntitySource interface from the HTTP endpoint of
  * the EntityHub of a Stanbol instance.
  */
 public class StanbolEntityHubSource extends ParameterizedHTTPEntitySource {
 
     public static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
 
     private static final Log log = LogFactory.getLog(StanbolEntityHubSource.class);
 
     protected final ObjectMapper mapper = new ObjectMapper();
 
     protected String endpointURL;
 
     public StanbolEntityHubSource() {
         initHttpClient();
     }
 
     @Override
     public synchronized void setDescriptor(
             RemoteEntitySourceDescriptor descriptor) {
         this.descriptor = descriptor;
         endpointURL = descriptor.getParameters().get("stanbolURL");
         if ("${org.nuxeo.ecm.platform.semanticentities.stanbolUrl}".equals(endpointURL)) {
             // no property defined, use some default value instead
             endpointURL = "https://stanbol.demo.nuxeo.com";
         }
         if (endpointURL == null || endpointURL.isEmpty()) {
             throw new RuntimeException(
                     "stanbolURL parameter is missing for the"
                             + " StanbolEntityHubSource ");
         }
         if (!endpointURL.endsWith("/")) {
             endpointURL += "/";
         }
         endpointURL += "entityhub/";
         String site = descriptor.getParameters().get("site");
         if (site != null) {
             endpointURL += "site/" + site + "/";
         }
         log.info("Configured StanbolEntityHubSource to endpoint: "
                 + endpointURL);
     }
 
     @SuppressWarnings("unchecked")
     protected Map<String, Object> fetchJSONDescription(URI remoteEntity)
             throws JsonParseException, JsonMappingException, IOException {
         // TODO: make the format configurable and upgrade to JSON-LD once
         // the spec is stabilizing a bit
        URI resourceUri = UriBuilder.fromPath(endpointURL).queryParam("id",
                remoteEntity.toString()).build();
         return mapper.readValue(doHttpGet(resourceUri, "application/json"),
                 Map.class);
     }
 
     @Override
     public Set<String> getAdmissibleTypes(URI remoteEntity)
             throws DereferencingException {
         Map<String, Object> jsonDescription;
         try {
             jsonDescription = fetchJSONDescription(remoteEntity);
         } catch (JsonParseException e) {
             throw new DereferencingException(e);
         } catch (JsonMappingException e) {
             throw new DereferencingException(e);
         } catch (IOException e) {
             throw new DereferencingException(e);
         }
         return getAdmissibleTypes(jsonDescription);
     }
 
     @SuppressWarnings("unchecked")
     protected Set<String> getAdmissibleTypes(Map<String, Object> jsonDescription)
             throws DereferencingException {
         try {
             Map<String, Object> attributes = (Map<String, Object>) jsonDescription.get("representation");
             List<Map<String, String>> typeInfos = (List<Map<String, String>>) attributes.get(RDF_TYPE);
             Set<String> admissibleTypes = new TreeSet<String>();
             Map<String, String> reverseTypeMapping = descriptor.getReverseMappedTypes();
             for (Map<String, String> typeInfo : typeInfos) {
                 String localType = reverseTypeMapping.get(typeInfo.get("value"));
                 if (localType != null) {
                     admissibleTypes.add(localType);
                 }
             }
             return admissibleTypes;
         } catch (Exception e) {
             throw new DereferencingException(e);
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void dereferenceInto(DocumentModel localEntity, URI remoteEntity,
             boolean override) throws DereferencingException {
         try {
             Map<String, Object> jsonDescription = fetchJSONDescription(remoteEntity);
             Map<String, Object> representation = (Map<String, Object>) jsonDescription.get("representation");
             if (representation == null) {
                 throw new DereferencingException(
                         "Invalid JSON response from Stanbol server:"
                                 + " missing 'representation' key: "
                                 + mapper.writeValueAsString(jsonDescription));
             }
             Set<String> possibleTypes = getAdmissibleTypes(jsonDescription);
             if (!possibleTypes.contains(localEntity.getType())) {
                 throw new DereferencingException(String.format(
                         "Remote entity '%s' can be mapped to types:"
                                 + " ('%s') but not to '%s'", remoteEntity,
                         StringUtils.join(possibleTypes, "', '"),
                         localEntity.getType()));
             }
             // special handling for the entity:sameas property
             // XXX: the following code should be factorized somewhere
             List<String> samesas = new ArrayList<String>();
             List<String> sameasDisplayLabel = new ArrayList<String>();
             Property sameasProp = localEntity.getProperty("entity:sameas");
             if (sameasProp.getValue() != null) {
                 samesas.addAll(sameasProp.getValue(List.class));
             }
             Property sameasDisplayLabelProp = localEntity.getProperty("entity:sameasDisplayLabel");
             if (sameasDisplayLabelProp.getValue() != null) {
                 sameasDisplayLabel.addAll(sameasDisplayLabelProp.getValue(List.class));
             }
             if (!samesas.contains(remoteEntity.toString())) {
                 samesas.add(remoteEntity.toString());
                 localEntity.setPropertyValue("entity:sameas",
                         (Serializable) samesas);
 
                 String titlePropUri = descriptor.getMappedProperties().get(
                         "dc:title");
                 String label = localEntity.getTitle();
                 label = label != null ? label : "Missing label";
                 if (titlePropUri != null) {
                     String labelFromRDF = readDecodedLiteral(representation,
                             titlePropUri, StringType.INSTANCE, "en").toString();
                     label = labelFromRDF != null ? labelFromRDF : label;
                 }
                 sameasDisplayLabel.add(label);
                 localEntity.setPropertyValue("entity:sameasDisplayLabel",
                         (Serializable) sameasDisplayLabel);
             }
             HashMap<String, String> mapping = new HashMap<String, String>(
                     descriptor.getMappedProperties());
             // as sameas has a special handling, remove it from the list of
             // properties to synchronize the generic way
             mapping.remove("entity:sameas");
 
             // generic handling of mapped properties
             for (Entry<String, String> mappedProperty : mapping.entrySet()) {
                 String localPropertyName = mappedProperty.getKey();
                 String remotePropertyUri = mappedProperty.getValue();
                 try {
                     Property localProperty = localEntity.getProperty(localPropertyName);
                     Type type = localProperty.getType();
                     if (type.isListType()) {
                         // only synchronize string lists right now
                         List<String> newValues = new ArrayList<String>();
                         if (localProperty.getValue() != null) {
                             newValues.addAll(localProperty.getValue(List.class));
                         }
                         if (override) {
                             newValues.clear();
                         }
                         for (String value : readStringList(representation,
                                 remotePropertyUri)) {
                             if (!newValues.contains(value)) {
                                 newValues.add(value);
                             }
                         }
                         localEntity.setPropertyValue(localPropertyName,
                                 (Serializable) newValues);
                     } else {
                         if (localProperty.getValue() == null
                                 || "".equals(localProperty.getValue())
                                 || override) {
                             if (type.isComplexType()
                                     && "content".equals(type.getName())) {
                                 Serializable linkedResource = (Serializable) readLinkedResource(
                                         representation, remotePropertyUri);
                                 if (linkedResource != null) {
                                     localEntity.setPropertyValue(
                                             localPropertyName, linkedResource);
                                 }
                             } else {
                                 Serializable literal = readDecodedLiteral(
                                         representation, remotePropertyUri,
                                         type, "en");
                                 if (literal != null) {
                                     localEntity.setPropertyValue(
                                             localPropertyName, literal);
                                 }
                             }
                         }
                     }
                 } catch (PropertyException e) {
                     // ignore missing properties
                 }
             }
         } catch (DereferencingException e) {
             throw e;
         } catch (Exception e) {
             throw new DereferencingException(e);
         }
     }
 
     @SuppressWarnings("unchecked")
     protected Serializable readLinkedResource(
             Map<String, Object> jsonRepresentation, String propertyUri) {
         // download depictions or other kind of linked resources
         List<Map<String, String>> propInfos = (List<Map<String, String>>) jsonRepresentation.get(propertyUri);
         if (propInfos == null) {
             return null;
         }
         for (Map<String, String> propInfo : propInfos) {
             String contentURI = propInfo.get("value");
             InputStream is = null;
             try {
                 is = doHttpGet(URI.create(contentURI), null);
                 if (is == null) {
                     log.warn("failed to fetch resource: " + contentURI);
                     return null;
                 }
                 Blob blob = StreamingBlob.createFromStream(is).persist();
                 int lastSlashIndex = contentURI.lastIndexOf('/');
                 if (lastSlashIndex != -1) {
                     blob.setFilename(contentURI.substring(lastSlashIndex + 1));
                 }
                 return (Serializable) blob;
             } catch (IOException e) {
                 log.warn(e.getMessage());
                 return null;
             } finally {
                 if (is != null) {
                     try {
                         is.close();
                     } catch (IOException e) {
                         log.error(e, e);
                     }
                 }
             }
         }
         return null;
     }
 
     @SuppressWarnings("unchecked")
     protected List<String> readStringList(
             Map<String, Object> jsonRepresentation, String propertyUri) {
         Set<String> values = new LinkedHashSet<String>();
         List<Map<String, String>> propInfos = (List<Map<String, String>>) jsonRepresentation.get(propertyUri);
         for (Map<String, String> propInfo : propInfos) {
             String value = propInfo.get("value");
             if (value != null) {
                 values.add(value);
             }
         }
         return new ArrayList<String>(values);
     }
 
     @SuppressWarnings("unchecked")
     protected Serializable readDecodedLiteral(
             Map<String, Object> jsonRepresentation, String propertyUri,
             Type type, String filterLang) {
         List<Map<String, String>> propInfos = (List<Map<String, String>>) jsonRepresentation.get(propertyUri);
         if (propInfos == null) {
             return null;
         }
         for (Map<String, String> propInfo : propInfos) {
             String lang = propInfo.get("xml:lang");
             if (lang != null && !filterLang.equals(lang)) {
                 continue;
             }
             String value = propInfo.get("value");
             Serializable decoded = (Serializable) type.decode(value);
             if (decoded instanceof String) {
                 decoded = StringEscapeUtils.unescapeHtml((String) decoded);
             }
             return decoded;
         }
         return null;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<RemoteEntity> suggestRemoteEntity(String keywords, String type,
             int maxSuggestions) throws IOException {
         // build a field query on the entity hub
         Map<String, Object> query = new LinkedHashMap<String, Object>();
         List<Map<String, String>> constraints = new ArrayList<Map<String, String>>();
         String namePropertyUri = descriptor.getMappedProperties().get(
                 "dc:title");
         Map<String, String> nameTextConstraint = new LinkedHashMap<String, String>();
         nameTextConstraint.put("type", "text");
         nameTextConstraint.put("field", namePropertyUri);
         nameTextConstraint.put("text", keywords);
         constraints.add(nameTextConstraint);
         if (type != null) {
             String remoteType = descriptor.getMappedTypes().get(type);
             if (remoteType == null) {
                 return Collections.emptyList();
             }
             Map<String, String> typeReferenceConstraint = new LinkedHashMap<String, String>();
             typeReferenceConstraint.put("type", "reference");
             typeReferenceConstraint.put("field",
                     "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
             typeReferenceConstraint.put("value", remoteType);
             constraints.add(typeReferenceConstraint);
         }
         List<String> selected = Arrays.asList(namePropertyUri);
         query.put("selected", selected);
         query.put("limit", maxSuggestions);
         query.put("constraints", constraints);
         String queryPayload = mapper.writeValueAsString(query);
         Map<String, Object> response = mapper.readValue(
                 doHttpPost(URI.create(endpointURL + "query"),
                         "application/json", "application/json", queryPayload),
                 Map.class);
         List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
         List<RemoteEntity> suggestions = new ArrayList<RemoteEntity>();
         for (Map<String, Object> result : results) {
             String name = readDecodedLiteral(result, namePropertyUri,
                     StringType.INSTANCE, "en").toString();
             suggestions.add(new RemoteEntity(name, result.get("id").toString()));
         }
         return suggestions;
     }
 
 }
