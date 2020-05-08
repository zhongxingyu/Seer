 package icloude.frontend_backend.request_handlers;
 
 import icloude.frontend_backend.requests.BaseRequest;
 import icloude.frontend_backend.requests.NewBuildAndRunTaskRequest;
 import icloude.frontend_backend.responses.BaseResponse;
 import icloude.frontend_backend.responses.IDResponse;
 import icloude.frontend_backend.responses.StandartResponse;
 
 import javax.ws.rs.FormParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import storage.Database;
 import storage.DatabaseException;
 import storage.StoringType;
 import storage.taskqueue.TaskType;
 
 import com.google.gson.JsonSyntaxException;
 
 /**
  * @author DimaTWL 
  * Handling all requests on "rest/newbuildandruntask" 
 * URL: rest/newbuildandruntask 
  * Method: POST 
  * Required response: Standart
  */
 @Path("/newbuildandruntask")
 public class NewBuildAndRunTaskRequestHandler extends BaseRequestHandler {
 
 	/**
 	 * This method used to handle all POST request on "rest/newbuildandruntask"
 	 * 
 	 * @return the StandartResponse witch will be sent to client
 	 */
 	@POST
 	@Produces(MediaType.APPLICATION_JSON)
 	public String post(@FormParam("json") String json) {
 		return getResponce(json);
 	}
 
 	/**
 	 * Realization of this method expected to convert JSON representation to
 	 * concrete request object.
 	 * 
 	 * @param json
 	 *            is JSON string from client.
 	 * @return concrete request object.
 	 */
 	@Override
 	protected BaseRequest jsonToRequest(String json) throws JsonSyntaxException {
		return GSON.fromJson(json, NewBuildAndRunTaskRequest.class);
 	}
 
 	/**
 	 * Realization of this method expected to check if 'requestType' in request
 	 * is allowed on some address.
 	 * 
 	 * @param requestType
 	 *            is 'requestType' field from request.
 	 * @return 'true' if request is allowed and 'false' otherwise.
 	 */
 	@Override
 	protected Boolean requestTypeCheck(String requestType) {
 		return "newbuildandruntask".equals(requestType);
 	}
 
 	/**
 	 * Realization of this method expected to do all specific staff (save/read
 	 * DB) and generate some response witch will be sent to client.
 	 * 
 	 * @param request
 	 *            is concrete request object.
 	 * @return response witch will be sent to client.
 	 */
 	@Override
 	protected BaseResponse handleRequest(BaseRequest request) {
 		BaseResponse response;
 		NewBuildAndRunTaskRequest castedRequest = (NewBuildAndRunTaskRequest) request;
 		try {
 			String key = Database.create(StoringType.BUILD_AND_RUN_TASK,
 					castedRequest.getProjectID(),
 					TaskType.BUILD_AND_RUN);
 			response = new IDResponse(request.getRequestID(), true,
 					"New Build&Run task created.",castedRequest.getProjectID(), key);
 		} catch (DatabaseException e) {
 			response = new StandartResponse(request.getRequestID(), false,
 					"DB error. " + e.getMessage());
 		}
 		return response;
 	}
 	
 	/**
 	 * Realization of this method expected to check all specific fields
 	 * in concrete request for not null. Check of BaseRequest field is redundant. 
 	 * 
 	 * @param request
 	 *            is concrete request object.
 	 * @return True if ALL specific fields != null
 	 * 		   False otherwise.
 	 */
 	@Override
 	protected Boolean concreteRequestNullCheck(BaseRequest request){
 		NewBuildAndRunTaskRequest castedRequest = (NewBuildAndRunTaskRequest) request;
 		return (null != castedRequest.getProjectID()) &&
 				(null != castedRequest.getCompileParameters()) &&
 				(null != castedRequest.getEntryPoindID()) &&
 				(null != castedRequest.getInputData());
 	}
 
 }
