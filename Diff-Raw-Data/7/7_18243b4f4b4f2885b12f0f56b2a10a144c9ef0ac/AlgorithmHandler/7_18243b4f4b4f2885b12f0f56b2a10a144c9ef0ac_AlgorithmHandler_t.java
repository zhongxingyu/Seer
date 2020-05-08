 package server;
 
 import algorithms.AlgorithmFactory;
 import computecore.AlgorithmRegistry;
 import computecore.ComputeCore;
 import computecore.ComputeRequest;
 import computecore.RequestPoints;
 import database.DatabaseManager;
 import database.RequestDataset;
 import database.RequestStatusEnum;
 import database.UserDataset;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonToken;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBufferInputStream;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
 import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
 
 /**
  * User: Niklas Schnelle
  * Date: 12/26/11
  * Time: 11:34 PM
  */
 public class AlgorithmHandler extends RequestHandler {
 
     private static Logger log = Logger.getLogger("server");
 
     private static final class MapType extends TypeReference<Map<String, Object>> {
     }
 
     private static final MapType JSONOBJECT = new MapType();
     private static final ObjectMapper mapper = new ObjectMapper();
     private final boolean isPrivate;
     private final DatabaseManager dbm;
     private final ComputeCore computer;
     private final Authorizer authorizer;
     private final AlgorithmRegistry algReg;
 
 
     protected AlgorithmHandler(Authorizer auth, boolean isPrivate, DatabaseManager dbm, ComputeCore computer) {
         super(null);
         this.isPrivate = isPrivate;
         this.dbm = dbm;
         this.computer = computer;
         this.authorizer = auth;
         this.algReg = computer.getAlgorithmRegistry();
     }
 
 
     /**
      * Reads a JSON encoded compute request from the content field of the given
      * request
      *
      * @param algName
      * @param responder
      * @param request
      * @return
      * @throws IOException
      * @throws JsonParseException
      */
     private ComputeRequest readComputeRequest(final String algName, final Responder responder, final HttpRequest request) throws IOException, JsonParseException {
         // Check whether Client accepts "application/x-jackson-smile"
         boolean acceptsSmile = (request.getHeader("Accept") != null) ? request.getHeader("Accept").contains("application/x-jackson-smile") : false;
 
         Map<String, Object> constraints = null;
         final RequestPoints points = new RequestPoints();
         final ChannelBuffer content = request.getContent();
         if (content.readableBytes() > 0) {
 
             final JsonParser jp = mapper.getJsonFactory().createJsonParser(new ChannelBufferInputStream(content));
             jp.setCodec(mapper);
 
             if (jp.nextToken() != JsonToken.START_OBJECT) {
                 throw new JsonParseException("Request contains no json object", jp.getCurrentLocation());
             }
 
             String fieldname;
             JsonToken token;
             Map<String, Object> pconsts;
             int lat = 0, lon = 0;
             while (jp.nextToken() != JsonToken.END_OBJECT) {
                 fieldname = jp.getCurrentName();
                 token = jp.nextToken(); // move to value, or
                 // START_OBJECT/START_ARRAY
                 if ("points".equals(fieldname)) {
                     // Should be on START_ARRAY
                     if (token != JsonToken.START_ARRAY) {
                         throw new JsonParseException("points is no array", jp.getCurrentLocation());
                     }
                     // Read array elements
                     while (jp.nextToken() != JsonToken.END_ARRAY) {
                         pconsts = new HashMap<String, Object>();
                         while (jp.nextToken() != JsonToken.END_OBJECT) {
                             fieldname = jp.getCurrentName();
                             token = jp.nextToken();
 
                             if ("lt".equals(fieldname)) {
                                 lat = jp.getIntValue();
                             } else if ("ln".equals(fieldname)) {
                                 lon = jp.getIntValue();
                             } else {
                                 pconsts.put(fieldname, jp.readValueAs(Object.class));
                             }
                         }
                         points.addPoint(lat, lon, pconsts);
                     }
 
                 } else if ("constraints".equals(fieldname)) {
                     constraints = jp.readValueAs(JSONOBJECT);
                 } else {
                     // ignore for now TODO: user version string etc.
                     if ((token == JsonToken.START_ARRAY) || (token == JsonToken.START_OBJECT)) {
                         jp.skipChildren();
                     }
                 }
             }
 
         } else {
             // Respond with No Content
             final HttpResponse response = new DefaultHttpResponse(HTTP_1_1, NO_CONTENT);
             // Write the response.
             final ChannelFuture future = responder.getChannel().write(response);
             future.addListener(ChannelFutureListener.CLOSE);
             log.warning("No Content");
             return null;
         }
 
         return new ComputeRequest(responder, algName, points, constraints, acceptsSmile);
     }
 
     /**
      * @param request
      * @param algName
      * @throws java.io.IOException
      * @throws java.sql.SQLException Thrown if auth fails or logging of request fails
      */
     public void handleAlg(HttpRequest request, String algName) throws IOException, SQLException {
         UserDataset userDataset = null;
 
         if (isPrivate) {
             userDataset = authorizer.auth(request);
             if (userDataset == null) {
                 responder.writeUnauthorizedClose();
                 return;
             }
         }
 
         try {
             // Get the AlgorithmFactory for this Alg to check if it's registered and not isHidden
             AlgorithmFactory algFac = algReg.getAlgByURLSuffix(algName);
             if (algFac == null) {
                 log.warning("Unsupported algorithm " + algName + " requested");
                 responder.writeErrorMessage("EUNKNOWNALG", "An unknown algorithm was requested", null, HttpResponseStatus.NOT_FOUND);
                 return;
             }
             // Only now read the request
             final ComputeRequest req = readComputeRequest(algName, responder, request);
 
             if (req != null) {
                 RequestDataset requestDataset = null;
 
                 if (isPrivate && !algFac.isHidden()) {
                     byte[] jsonRequest = request.getContent().array();
                     requestDataset = dbm.addNewRequest(userDataset.id, algName, jsonRequest);
                     req.setRequestID(requestDataset.requestID);
                 }
 
                 final boolean success = computer.submit(req);
 
                 if (!success) {
                     String errorMessage = responder.writeAndReturnErrorMessage("EBUSY", "This server is currently too busy to fullfill the request", null, HttpResponseStatus.SERVICE_UNAVAILABLE);
                     log.warning("Server had to deny algorithm request because of OVERLOAD");
                     if(isPrivate && !algFac.isHidden()){
                         // Log failed requests because of full queue as failed
                         // TODO specify this case clearly, maybe behavior should be another
                         requestDataset.failDescription = errorMessage;
                         // TODO maybe a better method should be used to convert a string to a byte array
                         requestDataset.jsonResponse = errorMessage.getBytes();
                         requestDataset.status = RequestStatusEnum.failed;

                        // already sent error message, throw no exception
                        try {
                            dbm.updateRequest(requestDataset);
                        } catch (SQLException e) {
                        }
                     }
 
                 }
             }
         } catch (JsonParseException e) {
             responder.writeErrorMessage("EBADJSON", "Could not parse supplied JSON", e.getMessage(), HttpResponseStatus.UNAUTHORIZED);
         }
 
     }
 }
