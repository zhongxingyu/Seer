 package com.readytalk.olive.servlet;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Scanner;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.IOUtils;
 import org.jets3t.service.ServiceException;
 
 import com.google.gson.Gson;
 import com.readytalk.olive.json.AddToSelectedRequest;
 import com.readytalk.olive.json.CombineVideosRequest;
 import com.readytalk.olive.json.CreateAccountRequest;
 import com.readytalk.olive.json.CreateProjectRequest;
 import com.readytalk.olive.json.DeleteAccountRequest;
 import com.readytalk.olive.json.DeleteProjectRequest;
 import com.readytalk.olive.json.DeleteVideoRequest;
 import com.readytalk.olive.json.GeneralRequest;
 import com.readytalk.olive.json.GetAccountInformationResponse;
 import com.readytalk.olive.json.IsDuplicateProjectNameRequest;
 import com.readytalk.olive.json.IsDuplicateProjectNameResponse;
 import com.readytalk.olive.json.IsDuplicateUsernameRequest;
 import com.readytalk.olive.json.IsDuplicateUsernameResponse;
 import com.readytalk.olive.json.RemoveFromSelectedRequest;
 import com.readytalk.olive.json.RenameProjectRequest;
 import com.readytalk.olive.json.RenameVideoRequest;
 import com.readytalk.olive.json.SplitVideoRequest;
 import com.readytalk.olive.json.UpdateProjectsPositionRequest;
 import com.readytalk.olive.json.UpdateTimelinePositionRequest;
 import com.readytalk.olive.json.UpdateVideosPositionRequest;
 import com.readytalk.olive.logic.Combiner;
 import com.readytalk.olive.logic.ZencoderApi;
 import com.readytalk.olive.logic.DatabaseApi;
 import com.readytalk.olive.logic.S3Api;
 import com.readytalk.olive.logic.Security;
 import com.readytalk.olive.model.Project;
 import com.readytalk.olive.model.User;
 import com.readytalk.olive.model.Video;
 import com.readytalk.olive.util.Attribute;
 import com.readytalk.olive.util.InvalidFileSizeException;
 
 /**
  * class OliveServlet
  * 
  * @author Team Olive
  * 
  */
 public class OliveServlet extends HttpServlet {
 	// Don't store anything as a member variable in the Servlet.
 	// private Object dontDoThis;
 
 	// Generated using Eclipse's "Add generated serial version ID" refactoring.
 	private static final long serialVersionUID = -6820792513104430238L;
 	// Static variables are okay, though, because they don't change across instances.
 	private static Logger log = Logger.getLogger(OliveServlet.class.getName());
 	public static final String TEMP_DIR_PATH = "/temp/"; // TODO Make a getter for this.
 	public static File tempDir; // TODO Make a getter for this.
 	public static final String DESTINATION_DIR_PATH = "/temp/"; // TODO Make a getter for this.
 	public static File destinationDir; // TODO Make a getter for this.
 
 	// Modified from: http://www.jsptube.com/servlet-tutorials/servlet-file-upload-example.html
 	// Also see: http://stackoverflow.com/questions/4101960/storing-image-using-htm-input-type-file
 	/**
 	 * 
 	 */
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		createTempDirectories();
 	}
 
 	private void createTempDirectories() throws ServletException {
 		String realPathTemp = getServletContext().getRealPath(TEMP_DIR_PATH);
 		tempDir = new File(realPathTemp);
 		if (!tempDir.isDirectory()) {
 			throw new ServletException(TEMP_DIR_PATH + " is not a directory");
 		}
 		String realPathDest = getServletContext().getRealPath(
 				DESTINATION_DIR_PATH);
 		destinationDir = new File(realPathDest);
 		if (!destinationDir.isDirectory()) {
 			throw new ServletException(DESTINATION_DIR_PATH
 					+ " is not a directory");
 		}
 	}
 
 	private int getAccountIdFromSessionAttributes(HttpSession session) {
 		String sessionUsername = (String) session
 				.getAttribute(Attribute.USERNAME.toString());
 		return DatabaseApi.getAccountId(sessionUsername);
 	}
 
 	private int getProjectIdFromSessionAttributes(HttpSession session) {
 		String sessionUsername = (String) session
 				.getAttribute(Attribute.USERNAME.toString());
 		int accountId = DatabaseApi.getAccountId(sessionUsername);
 		String sessionProjectName = (String) session
 				.getAttribute(Attribute.PROJECT_NAME.toString());
 		int projectId = DatabaseApi.getProjectId(sessionProjectName, accountId);
 		return projectId;
 	}
 
 	private int getProjectIdFromSessionAttributes(HttpSession session,
 			String projectName) {
 		return DatabaseApi.getProjectId(projectName,
 				getAccountIdFromSessionAttributes(session));
 	}
 
 	private int getVideoIdFromSessionAttributes(HttpSession session,
 			String videoName) {
 		return DatabaseApi.getVideoId(videoName,
 				getProjectIdFromSessionAttributes(session));
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession();
 		if (request.getContentType().contains(
 				"application/x-www-form-urlencoded")) { // Full value: "application/x-www-form-urlencoded"
 			// This is a regular text form.
 			String id = request.getParameter("FormName");
 			log.info("The servlet is responding to an "
 					+ "HTTP POST request from form: " + id);
 			if (id.equals("LoginUser")) {
 				handleLogin(request, response, session);
 			} else if (id.equals("EditUser-NameEmail")) {
 				handleEditUserNameEmail(request, response, session);
 			} else if (id.equals("EditUserPassword")) {
 				handleEditUserPassword(request, response, session);
 			} else if (id.equals("EditUserSecurity")) {
 				handleEditUserSecurity(request, response, session);
 			} else if (id.equals("security-question-form")) {
 				handleSecurityQuestionRetrieval(request, response, session);
 			} else if (id.equals("security-question-form-2")) {
 				handleSecurityAnswer(request, response, session);
 			} else if (id.equals("new_password")) {
 				handleNewPassword(request, response, session);
 			} else if (id.equals("combine-form")) {
 				try {
 					handleCombineVideos(request, response, session, "");
 				} catch (NoSuchAlgorithmException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (InvalidFileSizeException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (ServiceException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			} else {
 				log.severe("HTTP POST request coming from unknown form: " + id);
 			}
 		} else if (request.getContentType()
				.contains("application/octet-stream")) { // Full value: "application/octet-stream"
 			// This is a fancy file upload form.
 			log.info("The servlet is responding to an "
 					+ "HTTP POST request from a fancy file upload form");
 			try {
 				handleFancyUploadVideo(request, response, session);
 			} catch (NoSuchAlgorithmException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InvalidFileSizeException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ServiceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else if (request.getContentType().contains("application/json")) {
 			// This is not a form, but a custom POST request with JSON in it.
 			log.info("The servlet is responding to an "
 					+ "HTTP POST request in JSON format");
 			try {
 				handleJsonPostRequest(request, response, session);
 			} catch (NoSuchAlgorithmException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InvalidFileSizeException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ServiceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			log.severe("Unknown content type");
 		}
 	}
 
 	private void handleNewPassword(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws UnsupportedEncodingException, IOException {
 		// TODO Auto-generated method stub
 		String newPassword = request.getParameter("password");
 		String confirmNewPassword = request.getParameter("confirm_password");
 		Boolean newPasswordSet;
 		if (Security.isSafePassword(newPassword)
 				&& Security.isSafePassword(confirmNewPassword)) {
 			session.setAttribute(Attribute.IS_SAFE.toString(), true);
 			if (newPassword.equals(confirmNewPassword)) {
 				session.setAttribute(Attribute.PASSWORDS_MATCH.toString(), true);
 				String username = (String) session
 						.getAttribute(Attribute.USERNAME.toString());
 				newPasswordSet = DatabaseApi
 						.editPassword(username, newPassword);
 				session.setAttribute(Attribute.EDIT_SUCCESSFULLY.toString(),
 						newPasswordSet);
 			} else {
 				session.setAttribute(Attribute.PASSWORDS_MATCH.toString(),
 						false);
 				session.setAttribute(Attribute.EDIT_SUCCESSFULLY.toString(),
 						false);
 			}
 		} else {
 			session.setAttribute(Attribute.IS_SAFE.toString(), false);
 			session.setAttribute(Attribute.EDIT_SUCCESSFULLY.toString(), false);
 		}
 		response.sendRedirect("new-password-form.jsp");
 		session.removeAttribute(Attribute.USERNAME.toString());
 	}
 
 	private void handleSecurityQuestionRetrieval(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws UnsupportedEncodingException, IOException {
 		// TODO Auto-generated method stub
 		String username = request.getParameter("username");
 		if (Security.isSafeUsername(username)) {
 			session.setAttribute(Attribute.IS_SAFE.toString(), true);
 			if (DatabaseApi.usernameExists(username)) {
 				String securityQuestion = DatabaseApi
 						.getAccountSecurityQuestion(DatabaseApi
 								.getAccountId(username));
 				if (securityQuestion != null) {
 					session.setAttribute(
 							Attribute.SECURITY_QUESTION.toString(),
 							securityQuestion);
 					session.setAttribute(Attribute.USERNAME.toString(),
 							username);
 					session.removeAttribute(Attribute.IS_SAFE.toString()); // Cleared so as to not interfere with any other form.
 					response.sendRedirect("securityQuestion.jsp");
 				} else {
 					session.setAttribute(Attribute.IS_CORRECT.toString(), false);
 					response.sendRedirect("forgot.jsp");
 				}
 			} else {
 				session.setAttribute(Attribute.IS_CORRECT.toString(), false);
 				response.sendRedirect("forgot.jsp");
 			}
 		} else {
 			session.setAttribute(Attribute.IS_SAFE.toString(), false);
 			session.setAttribute(Attribute.IS_CORRECT.toString(), false);
 			response.sendRedirect("forgot.jsp");
 		}
 	}
 
 	private void handleSecurityAnswer(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws UnsupportedEncodingException, IOException {
 		// TODO Auto-generated method stub
 		String answer = request.getParameter("security_answer");
 		String username = (String) session.getAttribute(Attribute.USERNAME
 				.toString());
 		if (Security.isSafeSecurityAnswer(answer)) {
 			session.setAttribute(Attribute.IS_SAFE.toString(), true);
 			String securityQuestion = DatabaseApi
 					.getAccountSecurityQuestion(DatabaseApi
 							.getAccountId(username));
 			Boolean isCorrect = DatabaseApi.isCorrectSecurityInfo(username,
 					securityQuestion, answer);
 			if (isCorrect) {
 				session.setAttribute(Attribute.IS_CORRECT.toString(), true);
 				session.removeAttribute(Attribute.IS_SAFE.toString()); // Cleared so as to not interfere with any other form.
 				response.sendRedirect("new-password-form.jsp");
 			} else {
 				session.setAttribute(Attribute.IS_CORRECT.toString(), false);
 				response.sendRedirect("securityQuestion.jsp");
 			}
 		} else {
 			session.setAttribute(Attribute.IS_SAFE.toString(), false);
 			session.setAttribute(Attribute.IS_CORRECT.toString(), false);
 			response.sendRedirect("securityQuestion.jsp");
 		}
 	}
 
 	// http://www.apl.jhu.edu/~hall/java/Servlet-Tutorial/Servlet-Tutorial-Form-Data.html
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		log.info("The servlet is responding to an HTTP GET request");
 		response.setContentType("text/html");
 		HttpSession session = request.getSession();
 		String projectName = request.getParameter("projectName");
 		int accountId = DatabaseApi.getAccountId((String) session
 				.getAttribute(Attribute.USERNAME.toString()));
 		if (projectName != null && Security.isSafeProjectName(projectName)
 				&& DatabaseApi.projectExists(projectName, accountId)) { // Short-circuiting
 			session.setAttribute(Attribute.PROJECT_NAME.toString(), projectName);
 			response.sendRedirect("editor.jsp");
 		} else {
 			response.sendRedirect("projects.jsp");
 		}
 		PrintWriter out = response.getWriter();
 		out.println("File uploaded. Please close this window and refresh the editor page.");
 		out.flush();
 		out.close();
 	}
 
 	private void handleLogin(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws UnsupportedEncodingException, IOException {
 		Boolean isAuthorized;
 		String username = request.getParameter("login-username");
 		String password = request.getParameter("login-password");
 		if (Security.isSafeUsername(username)
 				&& Security.isSafePassword(password)) {
 			session.setAttribute(Attribute.IS_SAFE.toString(), true);
 			isAuthorized = DatabaseApi.isAuthorized(username, password);
 			session.setAttribute(Attribute.IS_AUTHORIZED.toString(),
 					isAuthorized);
 			if (isAuthorized) { // Take the user to the projects page.
 				int accountId = DatabaseApi.getAccountId(username);
 				session.setAttribute(Attribute.USERNAME.toString(),
 						DatabaseApi.getAccountUsername(accountId));
 				session.setAttribute(Attribute.PASSWORD.toString(), password);
 				session.setAttribute(Attribute.EMAIL.toString(),
 						DatabaseApi.getAccountEmail(accountId));
 				session.setAttribute(Attribute.NAME.toString(),
 						DatabaseApi.getAccountName(accountId));
 				session.setAttribute(Attribute.IS_FIRST_SIGN_IN.toString(),
 						false);
 				session.removeAttribute(Attribute.IS_SAFE.toString()); // Cleared so as to not interfere with any other form.
 				response.sendRedirect("projects.jsp");
 			} else {
 				response.sendRedirect("index.jsp"); // Keep the user on the same page.
 			}
 		} else {
 			session.setAttribute(Attribute.IS_SAFE.toString(), false);
 			session.setAttribute(Attribute.IS_AUTHORIZED.toString(), false);
 			response.sendRedirect("index.jsp");
 		}
 	}
 	private void handleEditUserNameEmail(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 	throws UnsupportedEncodingException, IOException {
 		String username = (String) session.getAttribute(Attribute.USERNAME
 				.toString());
 		String newName = request.getParameter("new-name");
 		String newEmail = request.getParameter("new-email");
 		if (Security.isSafeName(newName) && Security.isSafeEmail(newEmail)){
 			User updateUser = new User(username, "" , newName,
 					newEmail, "", "");
 			Boolean editSuccessfully = DatabaseApi.editAccount(updateUser);
 			session.setAttribute(Attribute.EDIT_NAME_SUCCESSFULLY.toString(),
 					editSuccessfully);
 			session.setAttribute(Attribute.EMAIL.toString(), newEmail);
 			session.setAttribute(Attribute.NAME.toString(), newName);
 			
 		}
 		else {
 			session.setAttribute(Attribute.EDIT_NAME_SUCCESSFULLY.toString(), false);
 		}
 		response.sendRedirect("account.jsp");
 				
 	}
 	private void handleEditUserPassword(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 	throws UnsupportedEncodingException, IOException {
 		String username = (String) session.getAttribute(Attribute.USERNAME
 				.toString());
 		String newPassword = request.getParameter("new-password");
 		String confirmNewPassword = request
 				.getParameter("confirm-new-password");
 		if (Security.isSafePassword(newPassword)
 				&& Security.isSafePassword(confirmNewPassword)){
 			if (newPassword.equals(confirmNewPassword)) {
 				User updateUser = new User(username, newPassword, "",
 						"", "", "");
 				Boolean editSuccessfully = DatabaseApi.editAccount(updateUser);
 				session.setAttribute(Attribute.EDIT_PWD_SUCCESSFULLY.toString(),
 						editSuccessfully);
 				session.setAttribute(Attribute.PASSWORDS_MATCH.toString(), true);
 			}
 			else{
 				session.setAttribute(Attribute.EDIT_PWD_SUCCESSFULLY.toString(),
 						false);
 				session.setAttribute(Attribute.PASSWORDS_MATCH.toString(),
 						false);
 			}
 			
 		}
 		else {
 			session.setAttribute(Attribute.EDIT_PWD_SUCCESSFULLY.toString(), false);
 		}
 		response.sendRedirect("account.jsp");
 				
 	}
 	private void handleEditUserSecurity(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 	throws UnsupportedEncodingException, IOException {
 		String username = (String) session.getAttribute(Attribute.USERNAME
 				.toString());
 		String securityQuestion = request.getParameter("new-security-question");
 		String securityAnswer = request.getParameter("new-security-answer");
 		if (Security.isSafeSecurityQuestion(securityQuestion)
 				&& Security.isSafeSecurityAnswer(securityAnswer)){
 			User updateUser = new User(username, "", "",
 					"", securityQuestion, securityAnswer);
 			Boolean editSuccessfully = DatabaseApi.editAccount(updateUser);
 			session.setAttribute(Attribute.EDIT_QA_SUCCESSFULLY.toString(),
 					editSuccessfully);
 			session.setAttribute(Attribute.SECURITY_QUESTION.toString(),
 					securityQuestion);
 			session.setAttribute(Attribute.SECURITY_ANSWER.toString(),
 					securityAnswer);
 				
 		}
 		else {
 			session.setAttribute(Attribute.EDIT_QA_SUCCESSFULLY.toString(), false);
 		}
 		response.sendRedirect("account.jsp");
 				
 	}
 
 	/**
 	 * 
 	 * Modified from OctetStreamReader.java at: http://valums.com/ajax-upload/
 	 * 
 	 * @param request
 	 * @param response
 	 * @throws ServiceException
 	 * @throws IOException
 	 * @throws InvalidFileSizeException
 	 * @throws NoSuchAlgorithmException
 	 */
 	public void handleFancyUploadVideo(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws NoSuchAlgorithmException, InvalidFileSizeException,
 			IOException, ServiceException {
 		PrintWriter out = null;
 		try {
 			out = response.getWriter();
 		} catch (IOException ex) {
 			log.severe(OliveServlet.class.getName()
 					+ " has thrown an exception: " + ex.getMessage());
 		}
 
 		log.info("Uploading file...");
 		File video = streamVideoToTemp(request, response, out);
 		addVideoEverywhere(out, getProjectIdFromSessionAttributes(session),
 				video);
 		video.delete(); // Delete it from the temp folder
 		log.info("File uploaded");
 	}
 
 	private File streamVideoToTemp(HttpServletRequest request,
 			HttpServletResponse response, PrintWriter out) {
 		InputStream inputStream = null;
 		FileOutputStream fileOutputStream = null;
 
 		String filename = request.getHeader("X-File-Name");
 		File video = new File(destinationDir, filename);
 		try {
 			inputStream = request.getInputStream();
 			fileOutputStream = new FileOutputStream(video);
 			IOUtils.copy(inputStream, fileOutputStream);
 			response.setStatus(HttpServletResponse.SC_OK);
 			out.print("{\"success\": true, \"videoPath\": \"" + "/olive/temp/"
 					+ video.getName() + "\"}");
 		} catch (FileNotFoundException ex) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			out.print("{success: false}");
 			log.severe(OliveServlet.class.getName()
 					+ " has thrown an exception: " + ex.getMessage());
 		} catch (IOException ex) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			out.print("{success: false}");
 			log.severe(OliveServlet.class.getName()
 					+ " has thrown an exception: " + ex.getMessage());
 		} finally {
 			try {
 				fileOutputStream.close();
 				inputStream.close();
 			} catch (IOException ignored) {
 			}
 		}
 
 		out.flush();
 		out.close();
 
 		return video;
 	}
 
 	private void addVideoEverywhere(PrintWriter out, int projectId, File video)
 			throws InvalidFileSizeException, IOException, ServiceException,
 			NoSuchAlgorithmException {
 		if (Security.isSafeVideo(video)
 				&& Security.videoFits(DatabaseApi.getNumberOfVideos(projectId))) {
 			String[] videoUrlAndIcon = S3Api.uploadFile(video);
 			String videoUrl = videoUrlAndIcon[0];
 			String videoIcon = videoUrlAndIcon[1];
 			if (videoUrl != null) {
 				// Give the video a name only at the last moment to prevent duplicates.
 				String videoName = Security.convertToSafeAndUniqueVideoName(
 						video.getName(), projectId);
 				DatabaseApi.addVideo(new Video(videoName, videoUrl, videoIcon,
 						projectId, -1, -1, false));
 				// File downloadedFile = S3Api.downloadFile(videoUrl); // TODO Add to /temp/ folder so it can be played in the player.
 				out.println("File uploaded. Please close this window and refresh the editor page.");
 				out.println();
 
 				return;
 			}
 			out.println("Upload Failed. Error uploading video to the cloud.");
 			log.warning("Upload Failed. Error uploading video to the cloud.");
 			// response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 			return;
 		} else if (!Security.isSafeVideo(video)) {
 			out.println("Upload Failed. Video is invalid.");
 			log.warning("Upload Failed. Video is invalid.");
 			return;
 		} else if (!Security
 				.videoFits(DatabaseApi.getNumberOfVideos(projectId))) {
 			out.println("Upload Failed. Maximum number of videos reached.");
 			log.warning("Upload Failed. Maximum number of videos reached.");
 			return;
 		} else {
 			out.println("Upload Failed. Unknown reason.");
 			log.warning("Upload Failed. Unknown reason.");
 			// response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Name");
 			return;
 		}
 	}
 
 	// Gson help: http://code.google.com/p/google-gson/
 	// http://stackoverflow.com/questions/338586/a-better-java-json-library
 	// http://stackoverflow.com/questions/1688099/converting-json-to-java/1688182#1688182
 	private void handleJsonPostRequest(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws IOException, NoSuchAlgorithmException,
 			InvalidFileSizeException, ServiceException, InterruptedException {
 		String line;
 		String json = "";
 		while ((line = request.getReader().readLine()) != null) {
 			json += line;
 		}
 		request.getReader().close();
 
 		GeneralRequest generalRequest = new Gson().fromJson(json,
 				GeneralRequest.class);
 
 		if (generalRequest.command.equals("createAccount")) {
 			handleCreateAccount(request, response, session, json);
 		} else if (generalRequest.command.equals("isDuplicateUsername")) {
 			handleIsDuplicateUsername(request, response, session, json);
 		} else if (generalRequest.command.equals("deleteAccount")) {
 			handleDeleteAccount(request, response, session, json);
 		} else if (generalRequest.command.equals("getAccountInformation")) {
 			handleGetAccountInformation(request, response, session, json);
 		} else if (generalRequest.command.equals("getProjects")) {
 			handleGetProjects(request, response, session, json);
 		} else if (generalRequest.command.equals("createProject")) {
 			handleCreateProject(request, response, session, json);
 		} else if (generalRequest.command.equals("isDuplicateProjectName")) {
 			handleIsDuplicateProjectName(request, response, session, json);
 		} else if (generalRequest.command.equals("deleteProject")) {
 			handleDeleteProject(request, response, session, json);
 		} else if (generalRequest.command.equals("renameProject")) {
 			handleRenameProject(request, response, session, json);
 		} else if (generalRequest.command.equals("updateProjectsPosition")) {
 			handleUpdateProjectsPosition(request, response, session, json);
 		} else if (generalRequest.command.equals("getProjectInformation")) {
 			handleGetProjectInformation(request, response, session, json);
 		} else if (generalRequest.command.equals("getVideos")) {
 			handleGetVideos(request, response, session, json);
 		} else if (generalRequest.command.equals("createVideo")) {
 			handleCreateVideo(request, response, session, json);
 		} else if (generalRequest.command.equals("deleteVideo")) {
 			handleDeleteVideo(request, response, session, json);
 		} else if (generalRequest.command.equals("renameVideo")) {
 			handleRenameVideo(request, response, session, json);
 		} else if (generalRequest.command.equals("addToTimeline")) {
 			handleAddToTimeline(request, response, session, json);
 		} else if (generalRequest.command.equals("removeFromTimeline")) {
 			handleRemoveFromTimeline(request, response, session, json);
 		} else if (generalRequest.command.equals("addToSelected")) {
 			handleAddToSelected(request, response, session, json);
 		} else if (generalRequest.command.equals("removeFromSelected")) {
 			handleRemoveFromSelected(request, response, session, json);
 		} else if (generalRequest.command.equals("splitVideo")) {
 			handleSplitVideo(request, response, session, json);
 		} else if (generalRequest.command.equals("combineVideos")) {
 			handleCombineVideos(request, response, session, json);
 		} else if (generalRequest.command.equals("updateVideosPosition")) {
 			handleUpdateVideosPosition(request, response, session, json);
 		} else if (generalRequest.command.equals("updateTimelinePosition")) {
 			handleUpdateTimelinePosition(request, response, session, json);
 		} else if (generalRequest.command.equals("getVideoInformation")) {
 			handleGetVideoInformation(request, response, session, json);
 		} else if (generalRequest.command.equals("isFirstSignIn")) {
 			handleIsFirstSignIn(request, response, session, json);
 		} else {
 			log.warning("JSON request not recognized.");
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 			return;
 		}
 	}
 
 	private void handleCreateAccount(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		CreateAccountRequest createAccountRequest = new Gson().fromJson(json,
 				CreateAccountRequest.class);
 
 		response.setContentType("text/plain");
 		PrintWriter out = response.getWriter();
 
 		String username = createAccountRequest.arguments.username;
 		String email = createAccountRequest.arguments.email;
 		String password = createAccountRequest.arguments.password;
 		String confirmPassword = createAccountRequest.arguments.confirmPassword;
 		String name = "Enter your name";
 
 		if (Security.isSafeUsername(username) && Security.isSafeEmail(email)
 				&& Security.isSafePassword(password)
 				&& Security.isSafePassword(confirmPassword)
 				&& password.equals(confirmPassword)
 				&& Security.isSafeName(name)) { // Short-circuitry
 			User newUser = new User(username, password, name, email);
 			boolean addedSuccessfully = DatabaseApi.AddAccount(newUser);
 			if (addedSuccessfully) {
 				session.setAttribute(Attribute.IS_AUTHORIZED.toString(), true);
 				session.setAttribute(Attribute.USERNAME.toString(), username);
 				session.setAttribute(Attribute.EMAIL.toString(), email);
 				session.setAttribute(Attribute.PASSWORD.toString(), password);
 				session.setAttribute(Attribute.IS_FIRST_SIGN_IN.toString(),
 						true);
 				out.println(username + " created successfully.");
 			} else {
 				// TODO Add error message here
 			}
 		} else {
 			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 		}
 		out.flush();
 		out.close();
 	}
 
 	private void handleIsDuplicateUsername(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		response.setContentType("application/json; charset=utf-8");
 		PrintWriter out = response.getWriter();
 
 		IsDuplicateUsernameRequest isDuplicateUsernameRequest = new Gson()
 				.fromJson(json, IsDuplicateUsernameRequest.class);
 		out.println(new Gson().toJson(new IsDuplicateUsernameResponse(
 				DatabaseApi
 						.usernameExists(isDuplicateUsernameRequest.arguments.username))));
 
 		out.flush();
 		out.close();
 	}
 
 	private void handleDeleteAccount(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		DeleteAccountRequest deleteAccountRequest = new Gson().fromJson(json,
 				DeleteAccountRequest.class);
 
 		response.setContentType("text/plain");
 		// response.setStatus(HttpServletResponse.SC_OK); // Unnecessary
 
 		PrintWriter out = response.getWriter();
 
 		String sessionUsername = (String) session
 				.getAttribute(Attribute.USERNAME.toString());
 		int accountId = DatabaseApi.getAccountId(sessionUsername);
 		DatabaseApi.deleteAccount(accountId);
 
 		out.println(deleteAccountRequest.arguments.account
 				+ " deleted successfully.");
 		out.flush();
 		out.close();
 	}
 
 	private void handleGetAccountInformation(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		response.setContentType("application/json; charset=utf-8");
 		PrintWriter out = response.getWriter();
 
 		int accountId = getAccountIdFromSessionAttributes(session);
 		String name = DatabaseApi.getAccountName(accountId);
 		String email = DatabaseApi.getAccountEmail(accountId);
 		String password = ""; // The encryption function is one-way (and it's a security issue to redisplay this).
 		String securityQuestion = DatabaseApi
 				.getAccountSecurityQuestion(accountId);
 		String securityAnswer = DatabaseApi.getAccountSecurityAnswer(accountId);
 
 		out.println(new Gson().toJson(new GetAccountInformationResponse(name,
 				email, password, securityQuestion, securityAnswer)));
 		out.flush();
 		out.close();
 	}
 
 	private void handleGetProjects(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		log.severe("handleGetProjects has not yet been implemented.");
 	}
 
 	private void handleCreateProject(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		response.setContentType("text/plain");
 		PrintWriter out = response.getWriter();
 
 		int accountId = getAccountIdFromSessionAttributes(session);
 		CreateProjectRequest createProjectRequest = new Gson().fromJson(json,
 				CreateProjectRequest.class);
 		String projectName = createProjectRequest.arguments.project;
 
 		if (Security.isSafeProjectName(projectName)
 				&& Security.isUniqueProjectName(projectName, accountId)
 				&& Security.projectFits(DatabaseApi
 						.getNumberOfProjects(accountId))) {
 			session.setAttribute(Attribute.IS_SAFE.toString(), true);
 
 			String icon = "/olive/images/Ponkan_folder_opened_64.png";
 			Project project = new Project(projectName, accountId, icon, -1);
 			Boolean added = DatabaseApi.addProject(project);
 			if (!added) {
 				session.setAttribute(Attribute.ADD_SUCCESSFULLY.toString(),
 						false);
 			} else {
 				session.setAttribute(Attribute.ADD_SUCCESSFULLY.toString(),
 						true);
 				session.setAttribute(Attribute.IS_FIRST_SIGN_IN.toString(),
 						false);
 				out.println(createProjectRequest.arguments.project
 						+ " created successfully.");
 			}
 		} else {
 			session.setAttribute(Attribute.IS_SAFE.toString(), false);
 		}
 
 		out.flush();
 		out.close();
 	}
 
 	private void handleIsDuplicateProjectName(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		response.setContentType("application/json; charset=utf-8");
 		PrintWriter out = response.getWriter();
 
 		int accountId = getAccountIdFromSessionAttributes(session);
 		IsDuplicateProjectNameRequest isDuplicateProjectNameRequest = new Gson()
 				.fromJson(json, IsDuplicateProjectNameRequest.class);
 		out.println(new Gson().toJson(new IsDuplicateProjectNameResponse(
 				DatabaseApi.projectExists(
 						isDuplicateProjectNameRequest.arguments.project,
 						accountId))));
 
 		out.flush();
 		out.close();
 	}
 
 	private void handleDeleteProject(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		DeleteProjectRequest deleteProjectRequest = new Gson().fromJson(json,
 				DeleteProjectRequest.class);
 
 		response.setContentType("text/plain");
 		// response.setStatus(HttpServletResponse.SC_OK); // Unnecessary
 
 		PrintWriter out = response.getWriter();
 
 		String sessionUsername = (String) session
 				.getAttribute(Attribute.USERNAME.toString());
 		int accountId = DatabaseApi.getAccountId(sessionUsername);
 		String projectToDelete = deleteProjectRequest.arguments.project;
 		int projectId = DatabaseApi.getProjectId(projectToDelete, accountId);
 		DatabaseApi.deleteProject(projectId);
 
 		out.println(deleteProjectRequest.arguments.project
 				+ " deleted successfully.");
 		out.flush();
 		out.close();
 	}
 
 	private void handleRenameProject(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		RenameProjectRequest renameProjectRequest = new Gson().fromJson(json,
 				RenameProjectRequest.class);
 
 		String newProjectName = renameProjectRequest.arguments.newProjectName;
 		String oldProjectName = renameProjectRequest.arguments.oldProjectName;
 		int projectId = getProjectIdFromSessionAttributes(session,
 				oldProjectName);
 		int accountId = getAccountIdFromSessionAttributes(session);
 		response.setContentType("text/plain");
 		PrintWriter out = response.getWriter();
 
 		if (Security.isSafeProjectName(newProjectName)
 				&& Security.isUniqueProjectName(newProjectName, accountId)) {
 			DatabaseApi.renameProject(projectId, newProjectName);
 			out.println(newProjectName);
 		} else {
 			out.println(oldProjectName);
 		}
 		out.flush();
 		out.close();
 	}
 
 	private void handleUpdateProjectsPosition(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		UpdateProjectsPositionRequest updateProjectsPositionRequest = new Gson()
 				.fromJson(json, UpdateProjectsPositionRequest.class);
 
 		int accountId = getAccountIdFromSessionAttributes(session);
 		DatabaseApi.setAllProjectPoolPositionsToNull(accountId);
 
 		int numberOfProjects = updateProjectsPositionRequest.arguments.projects.length;
 		for (int projectIndex = 0; projectIndex < numberOfProjects; ++projectIndex) {
 			String projectName = updateProjectsPositionRequest.arguments.projects[projectIndex].project;
 			int projectId = getProjectIdFromSessionAttributes(session,
 					projectName);
 			int position = updateProjectsPositionRequest.arguments.projects[projectIndex].position;
 			DatabaseApi.setProjectPoolPosition(projectId, position);
 		}
 	}
 
 	private void handleGetProjectInformation(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		int accountId = getAccountIdFromSessionAttributes(session);
 		String projectString = S3Api.getProjectInformation(accountId);
 		response.setContentType("application/json; charset=utf-8");
 		PrintWriter out = response.getWriter();
 		out.println(projectString);
 		out.flush();
 		out.close();
 	}
 
 	private void handleGetVideos(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		log.severe("handleGetVideos has not yet been implemented.");
 	}
 
 	private void handleCreateVideo(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		log.severe("handleCreateVideo has not yet been implemented.");
 	}
 
 	private void handleDeleteVideo(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		DeleteVideoRequest deleteVideoRequest = new Gson().fromJson(json,
 				DeleteVideoRequest.class);
 
 		response.setContentType("text/plain");
 
 		PrintWriter out = response.getWriter();
 
 		int projectId = getProjectIdFromSessionAttributes(session);
 		int videoId = DatabaseApi.getVideoId(
 				deleteVideoRequest.arguments.video, projectId);
 
 		S3Api.deleteFileInS3(S3Api.getNameFromUrl(DatabaseApi
 				.getVideoUrl(videoId))); // Delete video
 		S3Api.deleteFileInS3(S3Api.getNameFromUrl(DatabaseApi
 				.getVideoIcon(videoId))); // Delete icon
 
 		DatabaseApi.deleteVideo(videoId);
 
 		out.println(deleteVideoRequest.arguments.video
 				+ " deleted successfully.");
 		out.flush();
 		out.close();
 	}
 
 	private void handleRenameVideo(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		RenameVideoRequest renameVideoRequest = new Gson().fromJson(json,
 				RenameVideoRequest.class);
 
 		String newVideoName = renameVideoRequest.arguments.newVideoName;
 		String oldVideoName = renameVideoRequest.arguments.oldVideoName;
 		int videoId = getVideoIdFromSessionAttributes(session, oldVideoName);
 		int projectId = getProjectIdFromSessionAttributes(session);
 		response.setContentType("text/plain");
 		PrintWriter out = response.getWriter();
 
 		if (Security.isSafeVideoName(newVideoName)
 				&& Security.isUniqueVideoName(newVideoName, projectId)) {
 			DatabaseApi.renameVideo(videoId, newVideoName);
 			out.println(newVideoName);
 		} else {
 			out.println(oldVideoName);
 		}
 		out.flush();
 		out.close();
 	}
 
 	private void handleAddToTimeline(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		log.severe("handleAddToTimeline has not yet been implemented.");
 	}
 
 	private void handleRemoveFromTimeline(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		log.severe("handleRemoveFromTimeline has not yet been implemented.");
 	}
 
 	private void handleAddToSelected(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		AddToSelectedRequest addToSelectedRequest = new Gson().fromJson(json,
 				AddToSelectedRequest.class);
 
 		int videoId = getVideoIdFromSessionAttributes(session,
 				addToSelectedRequest.arguments.video);
 
 		if (!DatabaseApi.setVideoAsSelected(videoId)) {
 			log.severe("Error marking video " + videoId + " as selected");
 		}
 	}
 
 	private void handleRemoveFromSelected(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		RemoveFromSelectedRequest removeFromSelectedRequest = new Gson()
 				.fromJson(json, RemoveFromSelectedRequest.class);
 
 		int videoId = getVideoIdFromSessionAttributes(session,
 				removeFromSelectedRequest.arguments.video);
 
 		if (!DatabaseApi.setVideoAsUnselected(videoId)) {
 			log.severe("Error marking video " + videoId + " as unselected");
 		}
 	}
 
 	private void handleSplitVideo(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		SplitVideoRequest splitVideoRequest = new Gson().fromJson(json,
 				SplitVideoRequest.class);
 
 		response.setContentType("text/plain");
 
 		PrintWriter out = response.getWriter();
 
 		if (!Security.isSafeVideoName(splitVideoRequest.arguments.video)) {
 			out.println("Name of video to split is invalid.");
 			log.warning("Name of video to split is invalid.");
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 			return;
 		}
 
 		if (!Security
 				.isSafeSplitTimeInSeconds(splitVideoRequest.arguments.splitTimeInSeconds)) {
 			out.println("Split time (in seconds) is invalid.");
 			log.warning("Split time (in seconds) is invalid.");
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 			return;
 		}
 
 		int projectId = getProjectIdFromSessionAttributes(session);
 		int videoId = DatabaseApi.getVideoId(splitVideoRequest.arguments.video,
 				projectId);
 		Video[] videoFragments = ZencoderApi.split(videoId,
 				splitVideoRequest.arguments.splitTimeInSeconds);
 
 		for (Video videoFragment : videoFragments) { // foreach-loop
 			// Give the video a name only at the last moment to prevent duplicates.
 			String newVideoName = Security.convertToSafeAndUniqueVideoName(
 					videoFragment.getName(), projectId);	// .getName() returns the original video name at this point.
 			videoFragment.setName(newVideoName);	// Now, change .getName() to a unique name.
 			
 			DatabaseApi.addVideo(new Video(videoFragment.getName(),
 					videoFragment.getUrl(), videoFragment.getIcon(), projectId,
 					-1, -1, false)); // projectId not computed by Zencoder
 		}
 
 		out.println(splitVideoRequest.arguments.video + " split at "
 				+ splitVideoRequest.arguments.splitTimeInSeconds
 				+ " seconds successfully.");
 		out.flush();
 		out.close();
 	}
 
 	private void handleCombineVideos(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 
 	throws IOException, NoSuchAlgorithmException, InvalidFileSizeException,
 			ServiceException, InterruptedException {
 		int projectId = getProjectIdFromSessionAttributes(session);
 		String[] videos = DatabaseApi.getVideosOnTimeline(projectId);
 		String format = request.getParameter("output-extension");
 		log.info("Combining Videos, if necessary");
 		if (videos.length == 0){
 			response.sendRedirect("editor.jsp");
 		} else {
 			String[] videoURLs = new String[videos.length];
 			for (int i = 0; i < videos.length; i++) {
 				videoURLs[i] = DatabaseApi.getVideoUrl(DatabaseApi.getVideoId(
 						videos[i], projectId));
 			}
 			String combinedURL = "";
 			if (videoURLs.length == 1) {
 				combinedURL = S3Api.downloadVideosToTemp(videoURLs[0]);
 			} else if (videoURLs.length > 1) {
 				combinedURL = Combiner.combineVideos(videoURLs, videos, tempDir);
 			}
 			if (combinedURL == null) {
 				response.sendRedirect("editor.jsp");
 				return;
 			}
 			log.info("Now converting, if necessary");
 			File file = new File(combinedURL);
 			String ext = ".ogv";
 			String converted = "";
 			if(!format.equals("ogv")){
 				if(format.equals("avi")){
 					ext=".avi";
 				} else if(format.equals("wmv")){
 					ext=".wmv";
 				} else if(format.equals("mp4")){
 					ext=".mp4";
 				}
 				converted = Combiner.convertTo(format,file,tempDir);
 				file = new File(tempDir+"/"+converted);
 			}
 			log.info("Now Dowloading");
 			final ServletOutputStream out = response.getOutputStream();
 			response.setContentType("application/octet-stream");
 			response.setHeader("Content-Disposition",
 			"attachment;filename=combinedVideo"+ext);
 			BufferedInputStream is = new BufferedInputStream(
 					new FileInputStream(file));
 			byte[] buf = new byte[4 * 1024]; // 4K buffer
 			int bytesRead;
 			while ((bytesRead = is.read(buf)) != -1) {
 				out.write(buf, 0, bytesRead);
 			}
 			is.close();
 			out.flush();
 			out.close();
 			file.delete();
 			log.info("Downloaded. End of handleCombinedVideos");
 		}
 	}
 
 	private void handleUpdateVideosPosition(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		UpdateVideosPositionRequest updateVideosPositionRequest = new Gson()
 				.fromJson(json, UpdateVideosPositionRequest.class);
 
 		int projectId = getProjectIdFromSessionAttributes(session);
 		DatabaseApi.setAllVideoPoolPositionsToNull(projectId);
 
 		int numberOfVideos = updateVideosPositionRequest.arguments.videos.length;
 		for (int videoIndex = 0; videoIndex < numberOfVideos; ++videoIndex) {
 			String videoName = updateVideosPositionRequest.arguments.videos[videoIndex].video;
 			int videoId = getVideoIdFromSessionAttributes(session, videoName);
 			int position = updateVideosPositionRequest.arguments.videos[videoIndex].position;
 			DatabaseApi.setVideoPoolPosition(videoId, position);
 		}
 	}
 
 	private void handleUpdateTimelinePosition(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		UpdateTimelinePositionRequest updateTimelinePositionRequest = new Gson()
 				.fromJson(json, UpdateTimelinePositionRequest.class);
 
 		int projectId = getProjectIdFromSessionAttributes(session);
 		DatabaseApi.setAllVideoTimelinePositionsToNull(projectId);
 
 		int numberOfVideos = updateTimelinePositionRequest.arguments.videos.length;
 		for (int videoIndex = 0; videoIndex < numberOfVideos; ++videoIndex) {
 			String videoName = updateTimelinePositionRequest.arguments.videos[videoIndex].video;
 			int videoId = getVideoIdFromSessionAttributes(session, videoName);
 			int position = updateTimelinePositionRequest.arguments.videos[videoIndex].position;
 			DatabaseApi.setTimelinePosition(videoId, position);
 		}
 	}
 
 	private void handleGetVideoInformation(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		int projectId = getProjectIdFromSessionAttributes(session);
 		String videoString = S3Api.getVideoInformation(projectId);
 		response.setContentType("application/json; charset=utf-8");
 		PrintWriter out = response.getWriter();
 		out.println(videoString);
 		out.flush();
 		out.close();
 	}
 
 	private void handleIsFirstSignIn(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		response.setContentType("application/json; charset=utf-8");
 		PrintWriter out = response.getWriter();
 		out.println(new Gson().toJson((Boolean) session
 				.getAttribute(Attribute.IS_FIRST_SIGN_IN.toString())));
 		out.flush();
 		out.close();
 	}
 }
