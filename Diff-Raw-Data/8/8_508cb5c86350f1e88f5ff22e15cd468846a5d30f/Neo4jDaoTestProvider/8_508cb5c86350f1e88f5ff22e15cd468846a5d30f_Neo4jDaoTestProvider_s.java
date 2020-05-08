 package com.zenika.dorm.core.test.dao.neo4j;
 
 import com.zenika.dorm.core.dao.neo4j.Neo4jDependency;
 import com.zenika.dorm.core.dao.neo4j.Neo4jIndex;
 import com.zenika.dorm.core.dao.neo4j.Neo4jMetadata;
 import com.zenika.dorm.core.dao.neo4j.Neo4jMetadataExtension;
 import com.zenika.dorm.core.dao.neo4j.Neo4jRelationship;
 import com.zenika.dorm.core.dao.neo4j.Neo4jResponse;
 import com.zenika.dorm.core.dao.neo4j.Neo4jTraverse;
 import com.zenika.dorm.core.dao.neo4j.util.ObjectMapperProvider;
 import com.zenika.dorm.core.graph.Dependency;
 import com.zenika.dorm.core.graph.DependencyNode;
 import com.zenika.dorm.core.graph.impl.DefaultDependency;
 import com.zenika.dorm.core.graph.impl.DefaultDependencyNode;
 import com.zenika.dorm.core.graph.impl.Usage;
 import com.zenika.dorm.core.model.DormMetadata;
 import com.zenika.dorm.core.model.DormMetadataExtension;
 import com.zenika.dorm.core.model.impl.DefaultDormMetadata;
 import com.zenika.dorm.core.model.impl.DefaultDormMetadataExtension;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
  */
 public class Neo4jDaoTestProvider {
 
     private static final Logger LOG = LoggerFactory.getLogger(Neo4jDaoTestProvider.class);
 
     private static final String INDEX_RESPONSE_JSON = "/com/zenika/dorm/core/test/resources/index_response.json";
     private static final String INDEX_DEPENDENCY_RESPONSE_JSON = "/com/zenika/dorm/core/test/resources/index_dependency_response.json";
     private static final String DEPENDENCY21_RESPONSE_JSON = "/com/zenika/dorm/core/test/resources/dependency_response.json";
     private static final String METADATA20_RESPONSE_JSON = "/com/zenika/dorm/core/test/resources/metadata_response.json";
     private static final String EXTENSION19_RESPONSE_JSON = "/com/zenika/dorm/core/test/resources/extension_response.json";
     private static final String DEPENDENCY3_RESPONSE_JSON = "/com/zenika/dorm/core/test/resources/dependency_response2.json";
     private static final String METADATA2_RESPONSE_JSON = "/com/zenika/dorm/core/test/resources/metadata_response2.json";
     private static final String EXTENSION1_RESPONSE_JSON = "/com/zenika/dorm/core/test/resources/extension_response2.json";
     private static final String TRAVERSE_RESPONSE_JSON = "/com/zenika/dorm/core/test/resources/traverse_response.json";
     private static final String RELATIONSHIP_DEPENDENCY21_RESPONSE_JSON = "/com/zenika/dorm/core/test/resources/relationship_dependency_response.json";
     private static final String RELATIONSHIP_DEPENDENCY3_RESPONSE_JSON = "/com/zenika/dorm/core/test/resources/relationship_dependency2_response.json";
     private static final String RELATIONSHIP_METADATA20_RESPONSE_JSON = "/com/zenika/dorm/core/test/resources/relationship_metadata_response.json";
     private static final String RELATIONSHIP_METADATA2_RESPONSE_JSON = "/com/zenika/dorm/core/test/resources/relationship_metadata2_response.json";
 
 
     // TODO : Refactor to the true model
    private static final String INDEX_DEPENDENCY_URI = "http://localhost:7474/db/data/index/node/dependency/fullqualifier/dorm/habi-base/0.6";
     private static final String DEPENDENCY21_URI = "http://localhost:7474/db/data/node/21";
     private static final String DEPENDENCY3_URI = "http://localhost:7474/db/data/node/3";
     private static final String METADATA20_URI = "http://localhost:7474/db/data/node/20";
     private static final String METADATA2_URI = "http://localhost:7474/db/data/node/2";
     private static final String EXTENSION19_URI = "http://localhost:7474/db/data/node/19";
     private static final String EXTENSION1_URI = "http://localhost:7474/db/data/node/1";
     private static final String TRAVERSE_URI = "http://localhost:7474/db/data/node/21/traverse/relationship";
     private static final String RELATIONSHIP_DEPENDENCY21_URI = "http://localhost:7474/db/data/relationship/16";
     private static final String RELATIONSHIP_DEPENDENCY3_URI = "http://localhost:7474/db/data/relationship/1";
     private static final String RELATIONSHIP_METADATA20_URI = "http://localhost:7474/db/data/relationship/15";
     private static final String RELATIONSHIP_METADATA2_URI = "http://localhost:7474/db/data/relationship/0";
 
     private static final String SEARCH_RELATIONSHIP_DEPENDENCY21_URI = "http://localhost:7474/db/data/node/21/relationships/out/METADATA";
     private static final String SEARCH_RELATIONSHIP_DEPENDENCY3_URI = "http://localhost:7474/db/data/node/3/relationships/out/METADATA";
     private static final String SEARCH_RELATIONSHIP_METADATA20_URI = "http://localhost:7474/db/data/node/20/relationships/out/EXTENSION";
     private static final String SEARCH_RELATIONSHIP_METADATA2_URI = "http://localhost:7474/db/data/node/2/relationships/out/EXTENSION";
 
     private static final String POST_URI = "http://localhost:7474/db/data/node";
 
     private URL dependency21Url;
     private URL metadata20Url;
     private URL extension19Url;
     private URL dependency3Url;
     private URL metadata2Url;
     private URL extension1Url;
     private URL relationshipDependency21Url;
     private URL relationshipMetadata20Url;
     private URL relationshipDependency3Url;
     private URL relationshipMetadata2Url;
     private URL indexDependencyUrl;
     private URL traverseUrl;
     private URL indexResponse;
 
     private URI indexUri;
     private URI dependency21Uri;
     private URI dependency3Uri;
     private URI metadata20Uri;
     private URI metadata2Uri;
     private URI extension19Uri;
     private URI extension1Uri;
     private URI relationshipDependency21Uri;
     private URI relationshipDependency3Uri;
     private URI relationshipMetadata20Uri;
     private URI relationshipMetadata2Uri;
     private URI traverseUri;
 
     private URI searchRelationshipDependency21Uri;
     private URI searchRelationshipDependency3Uri;
     private URI searchRelationshipMetadata20Uri;
     private URI searchRelationshipMetadata2Uri;
 
     private URI postUri;
 
     private TypeReference<Neo4jResponse<Neo4jDependency>> dependencyResponseType;
     private TypeReference<Neo4jResponse<Neo4jMetadata>> metadataResponseType;
     private TypeReference<Neo4jResponse<Neo4jMetadataExtension>> extensionResponseType;
     private TypeReference<List<Neo4jRelationship>> listRelationshipType;
     private TypeReference<List<Neo4jResponse<Neo4jDependency>>> listDependencyResponseType;
 
     private Neo4jIndex index;
     private Neo4jResponse<Neo4jDependency> dependency21Response;
     private Neo4jResponse<Neo4jMetadata> metadata20Response;
     private Neo4jResponse<Neo4jMetadataExtension> extension19Response;
     private Neo4jResponse<Neo4jDependency> dependency3Response;
     private Neo4jResponse<Neo4jMetadata> metadata2Response;
     private Neo4jResponse<Neo4jMetadataExtension> extension1Response;
     private Neo4jTraverse traverse;
     private List<Neo4jRelationship> relationshipsDependency21;
     private List<Neo4jRelationship> relationshipsDependency3;
     private List<Neo4jRelationship> relationshipsMetadata20;
     private List<Neo4jRelationship> relationshipsMetadata2;
     private List<Neo4jRelationship> relationships;
     private List<Neo4jResponse<Neo4jDependency>> listDependencyResponse;
 
     private ObjectMapper mapper;
 
     private Usage usage;
     private Neo4jDependency dependency;
     private DependencyNode dependencyNode;
     private Neo4jMetadata metadata;
     private Neo4jMetadataExtension extension;
 
     public Neo4jDaoTestProvider() {
         mapper = ObjectMapperProvider.createDefaultMapper();
         setUpDormDependency();
         setUpUrl();
         setUpUri();
         setUpTypeReference();
         setUpNeo4jResponseObject();
     }
 
     private void setUpUrl() {
         dependency21Url = getClass().getResource(DEPENDENCY21_RESPONSE_JSON);
         metadata20Url = getClass().getResource(METADATA20_RESPONSE_JSON);
         extension19Url = getClass().getResource(EXTENSION19_RESPONSE_JSON);
         dependency3Url = getClass().getResource(DEPENDENCY3_RESPONSE_JSON);
         metadata2Url = getClass().getResource(METADATA2_RESPONSE_JSON);
         extension1Url = getClass().getResource(EXTENSION1_RESPONSE_JSON);
         relationshipDependency21Url = getClass().getResource(RELATIONSHIP_DEPENDENCY21_RESPONSE_JSON);
         relationshipMetadata20Url = getClass().getResource(RELATIONSHIP_METADATA20_RESPONSE_JSON);
         relationshipDependency3Url = getClass().getResource(RELATIONSHIP_DEPENDENCY3_RESPONSE_JSON);
         relationshipMetadata2Url = getClass().getResource(RELATIONSHIP_METADATA2_RESPONSE_JSON);
         indexDependencyUrl = getClass().getResource(INDEX_DEPENDENCY_RESPONSE_JSON);
         indexResponse = getClass().getResource(INDEX_RESPONSE_JSON);
         traverseUrl = getClass().getResource(TRAVERSE_RESPONSE_JSON);
     }
 
     private void setUpUri() {
         try {
             indexUri = new URI(INDEX_DEPENDENCY_URI);
             dependency21Uri = new URI(DEPENDENCY21_URI);
             dependency3Uri = new URI(DEPENDENCY3_URI);
             metadata20Uri = new URI(METADATA20_URI);
             metadata2Uri = new URI(METADATA2_URI);
             extension19Uri = new URI(EXTENSION19_URI);
             extension1Uri = new URI(EXTENSION1_URI);
             relationshipDependency21Uri = new URI(RELATIONSHIP_DEPENDENCY21_URI);
             relationshipDependency3Uri = new URI(RELATIONSHIP_DEPENDENCY3_URI);
             relationshipMetadata20Uri = new URI(RELATIONSHIP_METADATA20_URI);
             relationshipMetadata2Uri = new URI(RELATIONSHIP_METADATA2_URI);
             traverseUri = new URI(TRAVERSE_URI);
 
             searchRelationshipDependency21Uri = new URI(SEARCH_RELATIONSHIP_DEPENDENCY21_URI);
             searchRelationshipDependency3Uri = new URI(SEARCH_RELATIONSHIP_DEPENDENCY3_URI);
             searchRelationshipMetadata20Uri = new URI(SEARCH_RELATIONSHIP_METADATA20_URI);
             searchRelationshipMetadata2Uri = new URI(SEARCH_RELATIONSHIP_METADATA2_URI);
 
             postUri = new URI(POST_URI);
         } catch (URISyntaxException e) {
             LOG.error("Bad URI", e);
         }
     }
 
     private void setUpTypeReference() {
         dependencyResponseType = new TypeReference<Neo4jResponse<Neo4jDependency>>() {
         };
         metadataResponseType = new TypeReference<Neo4jResponse<Neo4jMetadata>>() {
         };
         extensionResponseType = new TypeReference<Neo4jResponse<Neo4jMetadataExtension>>() {
         };
         listRelationshipType = new TypeReference<List<Neo4jRelationship>>() {
         };
         listDependencyResponseType = new TypeReference<List<Neo4jResponse<Neo4jDependency>>>() {
         };
     }
 
     private void setUpNeo4jResponseObject() {
         try {
             dependency21Response = mapper.readValue(dependency21Url, dependencyResponseType);
             metadata20Response = mapper.readValue(metadata20Url, metadataResponseType);
             extension19Response = mapper.readValue(extension19Url, extensionResponseType);
             dependency3Response = mapper.readValue(dependency3Url, dependencyResponseType);
             metadata2Response = mapper.readValue(metadata2Url, metadataResponseType);
             extension1Response = mapper.readValue(extension1Url, extensionResponseType);
             relationshipsDependency21 = mapper.readValue(relationshipDependency21Url, listRelationshipType);
             relationshipsDependency3 = mapper.readValue(relationshipDependency3Url, listRelationshipType);
             relationshipsMetadata20 = mapper.readValue(relationshipMetadata20Url, listRelationshipType);
             relationshipsMetadata2 = mapper.readValue(relationshipMetadata2Url, listRelationshipType);
             relationships = mapper.readValue(traverseUrl, listRelationshipType);
             listDependencyResponse = mapper.readValue(indexDependencyUrl, listDependencyResponseType);
             index = mapper.readValue(indexResponse, Neo4jIndex.class);
             traverse = new Neo4jTraverse(new Neo4jRelationship(usage));
 
             dependency21Response.getData().setResponse(dependency21Response);
             metadata20Response.getData().setResponse(metadata20Response);
             extension19Response.getData().setResponse(extension19Response);
             dependency3Response.getData().setResponse(dependency3Response);
             metadata2Response.getData().setResponse(metadata2Response);
             extension1Response.getData().setResponse(extension1Response);
         } catch (IOException e) {
             LOG.error("Jackson mapper error", e);
         }
     }
 
     private void setUpDormDependency(){
         usage = Usage.create("DEFAULT");
         extension = new Neo4jMetadataExtension(new DefaultDormMetadataExtension("habi-base"));
         metadata = new Neo4jMetadata(DefaultDormMetadata.create("0.6", extension));
         dependency = new Neo4jDependency(DefaultDependency.create(metadata, usage));
         dependencyNode = DefaultDependencyNode.create(dependency);
     }
 
     public TypeReference<Neo4jResponse<Neo4jDependency>> getDependencyResponseType() {
         return dependencyResponseType;
     }
 
     public TypeReference<Neo4jResponse<Neo4jMetadata>> getMetadataResponseType() {
         return metadataResponseType;
     }
 
     public TypeReference<Neo4jResponse<Neo4jMetadataExtension>> getExtensionResponseType() {
         return extensionResponseType;
     }
 
     public TypeReference<List<Neo4jRelationship>> getListRelationshipType() {
         return listRelationshipType;
     }
 
     public TypeReference<List<Neo4jResponse<Neo4jDependency>>> getListDependencyResponseType() {
         return listDependencyResponseType;
     }
 
     public Neo4jResponse<Neo4jDependency> getDependency21Response() {
         return dependency21Response;
     }
 
     public Neo4jResponse<Neo4jMetadata> getMetadata20Response() {
         return metadata20Response;
     }
 
     public Neo4jResponse<Neo4jMetadataExtension> getExtension19Response() {
         return extension19Response;
     }
 
     public Neo4jResponse<Neo4jDependency> getDependency3Response() {
         return dependency3Response;
     }
 
     public Neo4jResponse<Neo4jMetadata> getMetadata2Response() {
         return metadata2Response;
     }
 
     public Neo4jResponse<Neo4jMetadataExtension> getExtension1Response() {
         return extension1Response;
     }
 
     public List<Neo4jRelationship> getRelationshipsDependency21() {
         return relationshipsDependency21;
     }
 
     public List<Neo4jRelationship> getRelationshipsDependency3() {
         return relationshipsDependency3;
     }
 
     public List<Neo4jRelationship> getRelationshipsMetadata20() {
         return relationshipsMetadata20;
     }
 
     public List<Neo4jRelationship> getRelationshipsMetadata2() {
         return relationshipsMetadata2;
     }
 
     public List<Neo4jRelationship> getRelationships() {
         return relationships;
     }
 
     public List<Neo4jResponse<Neo4jDependency>> getListDependencyResponse() {
         return listDependencyResponse;
     }
 
     public Neo4jIndex getIndex() {
         return index;
     }
 
     public Neo4jTraverse getTraverse() {
         return traverse;
     }
 
     public URI getDependency21Uri() {
         return dependency21Uri;
     }
 
     public URI getIndexUri() {
         return indexUri;
     }
 
     public URI getDependency3Uri() {
         return dependency3Uri;
     }
 
     public URI getMetadata20Uri() {
         return metadata20Uri;
     }
 
     public URI getMetadata2Uri() {
         return metadata2Uri;
     }
 
     public URI getExtension19Uri() {
         return extension19Uri;
     }
 
     public URI getExtension1Uri() {
         return extension1Uri;
     }
 
     public URI getRelationshipDependency21Uri() {
         return relationshipDependency21Uri;
     }
 
     public URI getRelationshipDependency3Uri() {
         return relationshipDependency3Uri;
     }
 
     public URI getRelationshipMetadata20Uri() {
         return relationshipMetadata20Uri;
     }
 
     public URI getRelationshipMetadata2Uri() {
         return relationshipMetadata2Uri;
     }
 
     public URI getTraverseUri() {
         return traverseUri;
     }
 
     public URI getSearchRelationshipMetadata2Uri() {
         return searchRelationshipMetadata2Uri;
     }
 
     public URI getSearchRelationshipMetadata20Uri() {
         return searchRelationshipMetadata20Uri;
     }
 
     public URI getSearchRelationshipDependency3Uri() {
         return searchRelationshipDependency3Uri;
     }
 
     public URI getSearchRelationshipDependency21Uri() {
         return searchRelationshipDependency21Uri;
     }
 
     public Usage getUsage() {
         return usage;
     }
 
     public Neo4jDependency getDependency() {
         return dependency;
     }
 
     public DependencyNode getDependencyNode() {
         return dependencyNode;
     }
 
     public Neo4jMetadata getMetadata() {
         return metadata;
     }
 
     public Neo4jMetadataExtension getExtension() {
         return extension;
     }
 
     public List getEmptyList(){
         return new ArrayList();
     }
 
     public List getNoEmptyList(){
         List list =  new ArrayList();
         list.add("empty");
         return list;
     }
 }
