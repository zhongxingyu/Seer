 package icloude.cron_services;
 
 import icloude.backend_amazon.requests.NewBuildAndRunRequest;
 import icloude.backend_amazon.responses.AcceptResultResponse;
 import icloude.backend_amazon.responses.IDResponse;
 import icloude.helpers.Logger;
 import icloude.helpers.ProjectZipper;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.ProtocolException;
 import java.net.URL;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import storage.Database;
 import storage.DatabaseException;
 import storage.StoringType;
 import storage.project.Project;
 import storage.taskqueue.BuildAndRunTask;
 import storage.taskqueue.TaskStatus;
 
 
 /**
  * @author DimaTWL 
  * Handling all requests on "rest/taskstartingservice" 
  * URL: rest/taskstartingservice 
  * Method: GET
  */
 @Path("/taskstartingservice")
 public class TaskStartingService extends BaseService {
 
 	/**
 	 * This fields used to determine build server address
 	 */
 	public final static String UPLOAD_ZIP_ADDRESS = BUILD_SERVER_ADDRESS
 			+ "rest/uploadzipfile";
 	public final static String NEW_BUILD_AND_RUN_TASK_ADDRESS = BUILD_SERVER_ADDRESS
 			+ "rest/newbuildandruntask";
 
 	/**
 	 * This method used to handle all GET request on "rest/taskstartingservice"
 	 * 
 	 * @return the StandartResponse witch will be sent to client
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public void get() {
 		BuildAndRunTask task = null;
 		try {
 			task = (BuildAndRunTask) Database.get(
 					StoringType.BUILD_AND_RUN_TASK, TaskStatus.NEW);
 			if (task != null) {
 				// 0.Get project
 				Project project = (Project) Database.get(StoringType.PROJECT,
 						task.getProjectKey());
 				// 1.Get zipped project
 				byte[] zippedProject = ProjectZipper.zipProject(project);
 				// 2.Send zipped project
 
 				IDResponse idResponse = SendZippedProject(zippedProject);
 				if (!IDResponseCheck(idResponse)) {
 					Logger.toLog("Some fields in ID response are not presented.");
 				} else if (!idResponse.getResult()) {
 					Logger.toLog("Got negative result in ID response with description: "
 							+ idResponse.getDescription());
 				} else {
 					// 3.Send build&run info
 					NewBuildAndRunRequest buildAndRunRequest = new NewBuildAndRunRequest(
 							PROTOCOL_VERSION, idResponse.getZipID(),
 							"HARDCODED", "BUILD&RUN", "HARDCODED", "HARDCODED",
 							"HARDCODED", "HARDCODED");
 					AcceptResultResponse acceptResultResponse = newBuildAndRunTask(buildAndRunRequest);
 					if (!AcceptResultResponseCheck(acceptResultResponse)) {
 						Logger.toLog("Some fields in AcceptResult response are not presented.");
 					} else if (!acceptResultResponse.getResult()) {
 						Logger.toLog("Got negative result in AcceptResult response with description: "
 								+ acceptResultResponse.getDescription());
 					} else {
 						// 4.Set TaskStatus.RUNNING
 						task.setStatus(TaskStatus.RUNNING);
 						// 5.Set new taskID
 						task.setTaskID(idResponse.getZipID());
 						// 6.Update task
 						Database.update(StoringType.BUILD_AND_RUN_TASK, task);
 					}
 				}
			} else {
				Logger.toLog("No available tasks.");
 			}
 		} catch (DatabaseException e) {
 			Logger.toLog("Got DatabaseException with message: "
 					+ e.getMessage());
 		} catch (IOException e) {
 			Logger.toLog("Got IOException with message: " + e.getMessage());
 		} catch (Exception e) {
 			Logger.toLog("Got Exception with message: " + e.getMessage());
 		}
 	}
 
 	private IDResponse SendZippedProject(byte[] zippedProject)
 			throws IOException {
 		URL url = new URL(UPLOAD_ZIP_ADDRESS);
 		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 		connection.setDoOutput(true);
 		connection.setRequestMethod("POST");
 		connection.setRequestProperty("content-type",
 				"application/x-zip-compressed");
 
 		OutputStream outputStream = connection.getOutputStream();
 		outputStream.write(zippedProject);
 		outputStream.close();
 
 		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					connection.getInputStream()));
 			StringBuilder jsonBuilder = new StringBuilder();
 			String input;
 			while ((input = in.readLine()) != null) {
 				jsonBuilder.append(input);
 			}
 			return GSON.fromJson(jsonBuilder.toString(), IDResponse.class);
 		} else {
 			throw new ProtocolException("Got HTTP error with code: "
 					+ connection.getResponseCode());
 		}
 	}
 
 	private Boolean IDResponseCheck(IDResponse response) {
 		return (null != response.getResult())
 				&& (null != response.getDescription())
 				&& (null != response.getZipID());
 	}
 
 	private Boolean AcceptResultResponseCheck(AcceptResultResponse response) {
 		return (null != response.getResult())
 				&& (null != response.getDescription());
 	}
 
 	private AcceptResultResponse newBuildAndRunTask(
 			NewBuildAndRunRequest request) throws IOException {
 		URL url = new URL(NEW_BUILD_AND_RUN_TASK_ADDRESS);
 		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 		connection.setDoOutput(true);
 		connection.setRequestMethod("POST");
 		connection.setRequestProperty("content-type", "application/JSON");
 
 		OutputStream outputStream = connection.getOutputStream();
 		outputStream.write(("json=" + GSON.toJson(request)).getBytes());
 		outputStream.close();
 
 		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					connection.getInputStream()));
 			StringBuilder jsonBuilder = new StringBuilder();
 			String input;
 			while ((input = in.readLine()) != null) {
 				jsonBuilder.append(input);
 			}
 			return GSON.fromJson(jsonBuilder.toString(),
 					AcceptResultResponse.class);
 		} else {
 			throw new ProtocolException("Got HTTP error with code: "
 					+ connection.getResponseCode());
 		}
 	}
 
 }
