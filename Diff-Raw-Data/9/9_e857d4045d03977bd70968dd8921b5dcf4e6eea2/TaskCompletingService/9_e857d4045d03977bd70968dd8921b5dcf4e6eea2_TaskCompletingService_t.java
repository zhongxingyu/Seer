 package icloude.cron_services;
 
 import icloude.backend_amazon.requests.CheckTaskStatusRequest;
 import icloude.backend_amazon.requests.DownloadBuildLogsRequest;
 import icloude.backend_amazon.requests.DownloadRunResultRequest;
 import icloude.backend_amazon.responses.BuildLogsResponse;
 import icloude.backend_amazon.responses.RunResultResponse;
 import icloude.backend_amazon.responses.StatusResponse;
 import icloude.helpers.Logger;
 
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
 import storage.taskqueue.BuildAndRunTask;
 import storage.taskqueue.TaskStatus;
 
 
 /**
  * @author DimaTWL 
  * Handling all requests on "rest/taskcompletingservice" 
  * URL: rest/taskcompletingservice 
  * Method: GET
  */
 @Path("/taskcompletingservice")
 public class TaskCompletingService extends BaseService {
 
 	/**
 	 * This fields used to determine build server address
 	 */
 	public final static String CHECK_TASK_STATUS_ADDRESS = BUILD_SERVER_ADDRESS
 			+ "rest/checktaskstatus";
 	public final static String DOWNLOAD_BUILD_LOGS_ADDRESS = BUILD_SERVER_ADDRESS
 			+ "rest/downloadbuildlogs";
 	public final static String DOWNLOAD_RUN_RESULTS_ADDRESS = BUILD_SERVER_ADDRESS
 			+ "rest/downloadrunresult";
 
 	/**
 	 * This method used to handle all GET request on
 	 * "rest/taskcompletingservice"
 	 * 
 	 * @return the StandartResponse witch will be sent to client
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public void get() {
 		BuildAndRunTask task = null;
 		try {
 			task = (BuildAndRunTask) Database.get(
 					StoringType.BUILD_AND_RUN_TASK, TaskStatus.RUNNING);
 			if (task != null) {
 				// 1.Check task status
 				CheckTaskStatusRequest statusRequest = new CheckTaskStatusRequest(
 						PROTOCOL_VERSION, task.getTaskID());
 				StatusResponse statusResponse = getTaskStatus(statusRequest);
 				if (!StatusResponseCheck(statusResponse)) {
 					Logger.toLog("Some fields in Status response are not presented.");
 				} else if (!statusResponse.getResult()) {
 					Logger.toLog("Got negative result in Status response with description: "
 							+ statusResponse.getDescription());
 				} else {
 					// 2.Download build logs
 					DownloadBuildLogsRequest buildRequest = new DownloadBuildLogsRequest(
 							PROTOCOL_VERSION, task.getTaskID());
 					BuildLogsResponse buildResponse = getBuildLogs(buildRequest);
 					if (!BuildLogsResponseCheck(buildResponse)) {
 						Logger.toLog("Some fields in Build response are not presented.");
 					} else if (!buildResponse.getResult()) {
 						Logger.toLog("Got negative result in Build response with description: "
 								+ statusResponse.getDescription());
 					} else {
 						task.addToResult(buildResponse.getBuildLogs());
 					}
 					// 3.Download run results
 					DownloadRunResultRequest runRequest = new DownloadRunResultRequest(
 							PROTOCOL_VERSION, task.getTaskID());
 					RunResultResponse runResponse = getRunResults(runRequest);
 					if (!RunResultResponseCheck(runResponse)) {
 						Logger.toLog("Some fields in Run response are not presented.");
 					} else if (!runResponse.getResult()) {
 						Logger.toLog("Got negative result in Run response with description: "
 								+ statusResponse.getDescription());
 					} else {
 						task.addToResult(runResponse.getRunResult());
 					}
 					// 4.Set TaskStatus.FINISHED
 					task.setStatus(TaskStatus.FINISHED);
 					// 6.Update task
 					Database.update(StoringType.BUILD_AND_RUN_TASK, task);
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
 
 	private Boolean StatusResponseCheck(StatusResponse response) {
 		return (null != response.getResult())
 				&& (null != response.getDescription());
 	}
 
 	private Boolean BuildLogsResponseCheck(BuildLogsResponse response) {
 		return (null != response.getResult())
 				&& (null != response.getDescription())
 				&& (null != response.getBuildLogs());
 	}
 
 	private Boolean RunResultResponseCheck(RunResultResponse response) {
 		return (null != response.getResult())
 				&& (null != response.getDescription())
 				&& (null != response.getRunResult());
 	}
 
 	private StatusResponse getTaskStatus(CheckTaskStatusRequest request)
 			throws IOException {
 		URL url = new URL(CHECK_TASK_STATUS_ADDRESS);
 		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 		connection.setDoOutput(true);
 		connection.setRequestMethod("GET");
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
 			return GSON.fromJson(jsonBuilder.toString(), StatusResponse.class);
 		} else {
 			throw new ProtocolException("Got HTTP error with code: "
 					+ connection.getResponseCode());
 		}
 	}
 
 	private RunResultResponse getRunResults(DownloadRunResultRequest request)
 			throws IOException {
 		URL url = new URL(DOWNLOAD_RUN_RESULTS_ADDRESS);
 		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 		connection.setDoOutput(true);
 		connection.setRequestMethod("GET");
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
 					RunResultResponse.class);
 		} else {
 			throw new ProtocolException("Got HTTP error with code: "
 					+ connection.getResponseCode());
 		}
 	}
 
 	private BuildLogsResponse getBuildLogs(DownloadBuildLogsRequest request)
 			throws IOException {
 		URL url = new URL(DOWNLOAD_BUILD_LOGS_ADDRESS);
 		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 		connection.setDoOutput(true);
 		connection.setRequestMethod("GET");
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
 					BuildLogsResponse.class);
 		} else {
 			throw new ProtocolException("Got HTTP error with code: "
 					+ connection.getResponseCode());
 		}
 	}
 
 }
