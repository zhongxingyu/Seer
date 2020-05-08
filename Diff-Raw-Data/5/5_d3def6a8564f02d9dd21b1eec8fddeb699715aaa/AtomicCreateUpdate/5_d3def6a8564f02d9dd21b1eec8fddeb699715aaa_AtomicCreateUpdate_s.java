 package com.okcupidlabs.neo4j.server.plugins;
 
 import java.util.Map;
 import java.net.URI;
 
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.core.MediaType;
 
 import org.neo4j.graphdb.*;
 import org.neo4j.graphdb.index.UniqueFactory;
 import org.neo4j.kernel.GraphDatabaseAPI;
 import org.neo4j.kernel.impl.transaction.xaframework.ForceMode;
 import org.neo4j.server.rest.domain.PropertySettingStrategy;
 import org.neo4j.server.rest.repr.*;
 import org.neo4j.server.rest.web.DatabaseActions;
 import org.neo4j.server.rest.web.PropertyValueException;
 
 
 //An extension to the Neo4j Server for atomically creating or updating nodes/edges if they already exist.
@Path("/atomic")
 public class AtomicCreateUpdate {
 
     private static final String REQUIRED_PARAMETERS = "from, to, relationship_type, properties";
 
     private final UriInfo uriInfo;
     private final InputFormat input;
     private final OutputFormat output;
     private final DatabaseActions actions;
     private final GraphDatabaseService service;
     private final PropertySettingStrategy propertySetter;
 
     public AtomicCreateUpdate(@Context UriInfo uriInfo, @Context InputFormat input,
                               @Context OutputFormat output, @Context DatabaseActions actions,
                               @Context GraphDatabaseService service)
     {
         this.uriInfo = uriInfo;
         this.input = input;
         this.output = output;
         this.actions = actions;
         this.service = service;
         // NOTE: This is ugly as hell.  I don't want to depend on this cast but I do want
         // the PropertySettingStrategy instead of re-implementing that functionality.
         // WHATCHAGONNADO.
         this.propertySetter = new PropertySettingStrategy((GraphDatabaseAPI)service);
 
     }
 
     // Creates or patches a node given a unique index key and value for the node.
     @POST
     @Path("/upsert/{index_name}/{index_key}/{index_value}")
     public Response upsertNode(
                 final @HeaderParam("Transaction") ForceMode force,
                 final @PathParam("index_name") String indexName,
                 final @PathParam("index_key") String indexKey,
                 final @PathParam("index_value") String indexValue,
                 final String body)
     {
         if (!this.service.index().existsForNodes(indexName)) {
            throw new IllegalArgumentException("Index with index_name: " + indexKey + " does not exist.");
         }
 
         UniqueFactory<Node> nodeFactory = new UniqueFactory.UniqueNodeFactory(service, indexName)
         {
             @Override
             protected void initialize( Node created, Map<String, Object> properties )
             {
                 //noop
             }
         };
 
         final Node upsertedNode = nodeFactory.getOrCreate(indexKey, indexValue);
         try {
             this.propertySetter.setProperties(upsertedNode, input.readMap(body));
         } catch (BadInputException e) {
 
             return output.badRequest(e);
         } catch (ArrayStoreException e) {
 
             return badJsonFormat(body);
         }
 
         return output.ok(new NodeRepresentation(upsertedNode));
     }
 
     @POST
     @Path("/upconnect")
     public Response upconnectNodes(
             final @HeaderParam("Transaction") ForceMode force,
             final String body)
     {
         final Map<String, Object> properties;
         try {
             properties = input.readMap(body);
         } catch (BadInputException e) {
             return output.badRequest(e);
         }
 
         if(!ensureRequiredUpconnectParameters(properties)) {
             return missingUpconnectParameters(properties);
         }
 
         long fromId = parseNodeIdFromURI(URI.create((String) properties.get("from")));
         long toId = parseNodeIdFromURI(URI.create((String)properties.get("to")));
         Node fromNode = this.service.getNodeById(fromId);
         Node toNode = this.service.getNodeById(toId);
         RelationshipType relationshipType = DynamicRelationshipType.withName(
                 (String)properties.get("relationship_type"));
         Map<String, Object> relationshipProperties = (Map<String, Object>)properties.get("properties");
 
         Relationship upconnectedRelationship;
         try {
             upconnectedRelationship= createOrUpdateRelationship(fromNode, toNode, relationshipType, relationshipProperties);
         } catch (PropertyValueException e) {
             return output.badRequest(e);
         }
 
         return output.ok(new RelationshipRepresentation(upconnectedRelationship));
     }
 
     private Relationship createOrUpdateRelationship(
             final Node fromNode,
             final Node toNode,
             final RelationshipType type,
             final Map<String, Object> properties) throws PropertyValueException
     {
         // check if relationship exists first, if it does update properties and GTFO
         Relationship relationship = fromNode.getSingleRelationship(type, Direction.OUTGOING);
         if (relationship != null) {
             this.propertySetter.setProperties(relationship, properties);
             return relationship;
         }
 
         // otherwise acquire write lock on from node
         Transaction tx = this.service.beginTx();
         Lock writeLock = tx.acquireWriteLock(fromNode);
 
         // check and see if we were beat to the lock, if so update and GTFO
         relationship = fromNode.getSingleRelationship(type, Direction.OUTGOING);
         if (relationship != null) {
             tx.finish();
             this.propertySetter.setProperties(relationship, properties);
             return relationship;
         }
 
         // alright, let's do the damn thing
         try {
             relationship = fromNode.createRelationshipTo(toNode, type);
             this.propertySetter.setProperties(relationship, properties);
             tx.success();
         } catch (Exception e) {
             System.out.println(e.getMessage());
         } finally {
             tx.finish();
         }
 
         return relationship;
     }
 
     private long parseNodeIdFromURI(URI uri)
     {
         String path = uri.getPath();
         String idStr = path.substring(path.lastIndexOf('/') + 1);
         return Long.parseLong(idStr);
     }
 
     private boolean ensureRequiredUpconnectParameters(Map<String, Object> properties)
     {
         return properties.containsKey("from") &&
                 properties.containsKey("to") &&
                 properties.containsKey("relationship_type") &&
                 properties.containsKey("properties");
     }
 
 
     private Response missingUpconnectParameters(Map<String, Object> properties)
     {
         String[] receivedParams = properties.keySet().toArray(new String[0]);
         String receivedParamString = "";
         if (receivedParams.length > 0) {
             StringBuilder sb = new StringBuilder();
             sb.append(receivedParams[0]);
 
             for (int i = 1; i < receivedParams.length; i++) {
                 sb.append(", ");
                 sb.append(receivedParams[i]);
             }
             receivedParamString = sb.toString();
         }
 
         return Response.status( 400 )
                 .type(MediaType.TEXT_PLAIN)
                 .entity("Required parameters: " + REQUIRED_PARAMETERS + "\n"
                       + "Received parameters: " + receivedParamString)
                 .build();
     }
 
     private Response badJsonFormat(String body) {
         return Response.status( 400 )
                 .type( MediaType.TEXT_PLAIN )
                 .entity( "Invalid JSON array in POST body: " + body )
                 .build();
     }
 
 }
