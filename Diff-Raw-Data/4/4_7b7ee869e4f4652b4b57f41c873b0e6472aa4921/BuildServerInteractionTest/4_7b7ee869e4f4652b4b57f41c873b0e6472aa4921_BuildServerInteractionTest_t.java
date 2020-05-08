 package icloude.testing.backend;
 
 import icloude.backend_buildserver.requests.NewBuildAndRunRequest;
 import icloude.backend_buildserver.responses.IDResponse;
 import icloude.cron_services.TaskStartingService;
 import icloude.frontend_backend.request_handlers.NewBuildAndRunTaskRequestHandler;
 import icloude.frontend_backend.requests.NewBuildAndRunTaskRequest;
 import icloude.frontend_backend.requests.NewFileRequest;
 import icloude.frontend_backend.responses.StandartResponse;
 import icloude.helpers.Logger;
 import icloude.helpers.ProjectZipper;
 import icloude.testing.backend.Test.TestResult;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import storage.Database;
 import storage.DatabaseException;
 import storage.StoringType;
 import storage.project.Project;
 import storage.taskqueue.BuildAndRunTask;
 import storage.taskqueue.TaskStatus;
 
 /**
  * @author DimaTWL
  *
  */
 
 public class BuildServerInteractionTest extends Test {
 
 	public BuildServerInteractionTest() {
 		tests.add(new SendProjectToBuildAndGetResponse()); // All tests added here!!!
 	}
 	
 	private class SendProjectToBuildAndGetResponse extends Test {
 		
 		private String failDescription = null;
 		
 		public List<TestResult> launch() {
 			//1. Create project
 			String projectID = createOneFileProject("OneFileTestProject");
 			List<TestResult> result = new ArrayList<TestResult>();
 			if (projectID != null) {
 				//2. Add new task to TaskQueue (NewBuildAndRunTask)
 				boolean newTaskInQueue = putNewTaskToTaskQueue(projectID);
 				if (newTaskInQueue) {
 					//3. Send task to build-server
 					//4. Get response from build-server
 					//5. Check response
 					boolean taskSent = sendTaskToBuildServer();
 					if (taskSent) {
 						result.add(new TestResult(true, getName(), "No issues."));
 					} else {
 						result.add(new TestResult(false, getName(), failDescription));
 					}
 				} else {
 					result.add(new TestResult(false, getName(), failDescription));
 				}
 			} else {
 				result.add(new TestResult(false, getName(), "Project isn't created."));
 			}
 			return result;
 		}
 		
 		private boolean putNewTaskToTaskQueue(String projectID) {
 			NewBuildAndRunTaskRequestHandler nbartrh = new NewBuildAndRunTaskRequestHandler();
 			NewBuildAndRunTaskRequest nbartr = new NewBuildAndRunTaskRequest(PROTOCOL_VERSION, "TestRequestID", "newbuildandruntask", "TestUserID", projectID, "TestCompileParameters", "TestEntryPointID", "TestInputData");
 			String json = nbartrh.post(GSON.toJson(nbartr));
 			StandartResponse sr = GSON.fromJson(json, StandartResponse.class);
 			if (null == sr) {
 				failDescription = "Can't put task to queue. Response is null.";
 				return false;
 			} else if (!sr.getResult()) {
 				failDescription = "Can't put task to queue. 'False' in result field of response.";
 				return false;
			} else if (!"TestRequestID".equals(sr.getRequestID())) {
				failDescription = "Wrong requestID in response. Got: " + sr.getRequestID();
 				return false;
 			} else if (null == sr.getDescription()) {
 				failDescription = "Description field is empty.";
 				return false;
 			} else {
 				return true;
 			}
 		}
 		
 		/**
 		 * Overlap with the following code in the class TaskStartingService!
 		 * It's not very good, but otherwise you'll have to rewrite the entire TaskStartingService.
 		 */
 		private boolean sendTaskToBuildServer() {
 			TaskStartingService tss = new TaskStartingService();
 			TaskStartingService.Tester tester = tss.new Tester();
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
 					IDResponse idResponse = tester.publicSendZippedProject(zippedProject);
 					if (!tester.publicIDResponseCheck(idResponse)) {
 						failDescription = "Some fields in ID response are not presented.";
 						return false;
 					} else if (!idResponse.getResult()) {
 						failDescription = "Got negative result in ID response with description: "
 								+ idResponse.getDescription();
 						return false;
 					}
 				} else {
 					failDescription = "No available tasks.";
 					return false;
 				}
 			} catch (DatabaseException e) {
 				failDescription = "Got DatabaseException with message: "
 						+ e.getMessage();
 				return false;
 			} catch (IOException e) {
 				failDescription = "Got IOException with message: " + e.getMessage();
 				return false;
 			} catch (Exception e) {
 				failDescription = "Got Exception with message: " + e.getMessage();
 				return false;
 			}
 			return true;
 		}
 		
 	}
 
 }
