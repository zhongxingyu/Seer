 package com.readytalk.olive.servlet;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Iterator;
 import java.util.List;
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
 import org.jets3t.service.ServiceException;
 
 import com.google.gson.Gson;
 import com.readytalk.olive.json.AddToSelectedRequest;
 import com.readytalk.olive.json.CombineVideosRequest;
 import com.readytalk.olive.json.DeleteAccountRequest;
 import com.readytalk.olive.json.DeleteProjectRequest;
 import com.readytalk.olive.json.DeleteVideoRequest;
 import com.readytalk.olive.json.GeneralRequest;
 import com.readytalk.olive.json.GetAccountInformationResponse;
 import com.readytalk.olive.json.RemoveFromSelectedRequest;
 import com.readytalk.olive.json.RenameProjectRequest;
 import com.readytalk.olive.json.RenameVideoRequest;
 import com.readytalk.olive.json.SplitVideoRequest;
 import com.readytalk.olive.json.UpdateProjectsPositionRequest;
 import com.readytalk.olive.json.UpdateTimelinePositionRequest;
 import com.readytalk.olive.json.UpdateVideosPositionRequest;
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
 			} else if (id.equals("EditUser")) {
 				handleEditUser(request, response, session);
 			} else if (id.equals("AddUser")) {
 				handleAddUser(request, response, session);
 			} else if (id.equals("AddProject")) {
 				handleAddProject(request, response, session);
 			} else if (id.equals("security-question-form")) {
 				handleSecurityQuestionRetrieval(request, response, session);
 			} else if (id.equals("security-question-form-2")) {
 				handleSecurityAnswer(request, response, session);
 			} else if (id.equals("new_password")) {
 				handleNewPassword(request, response, session);
 			} else {
 				log.severe("HTTP POST request coming from unknown form: " + id);
 			}
 		} else if (request.getContentType().contains("multipart/form-data")) { // Full value: "multipart/form-data; boundary=----WebKitFormBoundaryjAGjLWGWeI3ltfBe"
 			// This is a file upload form.
 			log.info("The servlet is responding to an "
 					+ "HTTP POST request from a file upload form");
 			handleUploadVideo(request, response, session);
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
 			if(DatabaseApi.usernameExists(username)){
 				String securityQuestion = DatabaseApi.getAccountSecurityQuestion(DatabaseApi.getAccountId(username));
 				if (securityQuestion!=null) {
 					session.setAttribute(Attribute.SECURITY_QUESTION.toString(), securityQuestion);
 					session.setAttribute(Attribute.USERNAME.toString(), username);
 					session.removeAttribute(Attribute.IS_SAFE.toString()); // Cleared so as to not interfere with any other form.
 					response.sendRedirect("securityQuestion.jsp");
 				} else {
 					session.setAttribute(Attribute.IS_CORRECT.toString(), false);	
 					response.sendRedirect("forgot.jsp");
 				}
 			}
 			else {
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
 		String username = (String)session.getAttribute(Attribute.USERNAME.toString());
 		if (Security.isSafeSecurityAnswer(answer)) {
 			session.setAttribute(Attribute.IS_SAFE.toString(), true);
 			String securityQuestion = DatabaseApi.getAccountSecurityQuestion(DatabaseApi.getAccountId(username));
 			Boolean isCorrect = DatabaseApi.isCorrectSecurityInfo(username, securityQuestion, answer);
 			if (isCorrect) {
 				session.setAttribute(Attribute.IS_CORRECT.toString(), true);
 				session.removeAttribute(Attribute.IS_SAFE.toString()); // Cleared so as to not interfere with any other form.
 				response.sendRedirect("new-password-form.jsp");
 			} 
 			else {
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
 		out.close();
 	}
 
 	private void handleLogin(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws UnsupportedEncodingException, IOException {
 		Boolean isAuthorized;
 		String username = request.getParameter("username");
 		String password = request.getParameter("password");
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
 
 	private void handleEditUser(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws UnsupportedEncodingException, IOException {
 		String username = (String) session.getAttribute(Attribute.USERNAME
 				.toString());
 		String newName = request.getParameter("new-name");
 		String newEmail = request.getParameter("new-email");
 		String newPassword = request.getParameter("new-password");
 		String confirmNewPassword = request
 				.getParameter("confirm-new-password");
		String securityQuestion = request.getParameter("security_question");
		String securityAnswer = request.getParameter("security_answer");
 		if (Security.isSafeName(newName) && Security.isSafeEmail(newEmail)
 				&& Security.isSafePassword(newPassword)
 				&& Security.isSafePassword(confirmNewPassword)
 				&& Security.isSafeSecurityQuestion(securityQuestion)
 				&& Security.isSafeSecurityAnswer(securityAnswer)) {
 			if (newPassword.equals(confirmNewPassword)) {
 				User updateUser = new User(username, newPassword, newName,
 						newEmail, securityQuestion, securityAnswer);
 				Boolean editSuccessfully = DatabaseApi.editAccount(updateUser);
 				session.setAttribute(Attribute.EDIT_SUCCESSFULLY.toString(),
 						editSuccessfully);
 				session.setAttribute(Attribute.PASSWORDS_MATCH.toString(), true);
 				session.setAttribute(Attribute.PASSWORD.toString(), newPassword);
 				session.setAttribute(Attribute.EMAIL.toString(), newEmail);
 				session.setAttribute(Attribute.NAME.toString(), newName);
 				session.setAttribute(Attribute.SECURITY_QUESTION.toString(),
 						securityQuestion);
 				session.setAttribute(Attribute.SECURITY_ANSWER.toString(),
 						securityAnswer);
 			} else {
 				session.setAttribute(Attribute.EDIT_SUCCESSFULLY.toString(),
 						false);
 				session.setAttribute(Attribute.PASSWORDS_MATCH.toString(),
 						false);
 			}
 		} else {
 			session.setAttribute(Attribute.EDIT_SUCCESSFULLY.toString(), false);
 		}
 		response.sendRedirect("account.jsp");
 	}
 
 	private void handleAddUser(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws IOException {
 		// The jQuery regex should catch malicious input, but sanitize just to
 		// be safe.
 		String username = Security.stripOutIllegalCharacters(request
 				.getParameter("name"));
 		String password = Security.stripOutIllegalCharacters(request
 				.getParameter("password"));
 		String email = Security.stripOutIllegalCharacters(request
 				.getParameter("email"));
 		User newUser = new User(username, password, "", email);
 		Boolean addSuccessfully = DatabaseApi.AddAccount(newUser);
 		if (addSuccessfully) {
 			session.setAttribute(Attribute.IS_AUTHORIZED.toString(), true);
 			session.setAttribute(Attribute.USERNAME.toString(), username);
 			session.setAttribute(Attribute.PASSWORD.toString(), password);
 			session.setAttribute(Attribute.EMAIL.toString(), email);
 			session.setAttribute(Attribute.IS_FIRST_SIGN_IN.toString(), true);
 			response.sendRedirect("projects.jsp");
 		} else {
 			response.sendRedirect("index.jsp");
 			// TODO Add error message here
 		}
 	}
 
 	private void handleAddProject(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws UnsupportedEncodingException, IOException {
 		int accountId = getAccountIdFromSessionAttributes(session);
 		String projectName = request.getParameter("new-project-name");
 		if (Security.isSafeProjectName(projectName)
 				&& Security.isUniqueProjectName(projectName, accountId)
 				&& Security.projectFits(DatabaseApi
 						.getNumberOfProjects(accountId))) {
 			session.setAttribute(Attribute.IS_SAFE.toString(), true);
 
 			String icon = ""; // TODO Get this from user input.
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
 			}
 		} else {
 			session.setAttribute(Attribute.IS_SAFE.toString(), false);
 		}
 		response.sendRedirect("projects.jsp");
 	}
 
 	private void handleUploadVideo(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws IOException {
 		PrintWriter out = response.getWriter();
 		out.println("Uploading file...");
 
 		response.setContentType("text/plain");
 
 		DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
 		// Set the size threshold, above which content will be stored on disk.
 		fileItemFactory.setSizeThreshold(1 * 1024 * 1024); // 1 MB
 
 		// Set the temporary directory to store the uploaded files of size above threshold.
 		fileItemFactory.setRepository(tempDir);
 
 		ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
 		File file = null;
 		try {
 			/*
 			 * Parse the request
 			 */
 			List items = uploadHandler.parseRequest(request);
 			Iterator itr = items.iterator();
 
 			/*
 			 * The two items in the form
 			 */
 			FileItem videoNameItem = null;
 			FileItem fileItem = null;
 			while (itr.hasNext()) {
 				FileItem item = (FileItem) itr.next();
 				/*
 				 * Handle Form Fields.
 				 */
 				if (item.isFormField()
 						&& item.getFieldName().equals("new-video-name")) { // Short-circuitry
 					// Handle text fields
 					log.info("Form Name = \"" + item.getFieldName()
 							+ "\", Value = \"" + item.getString() + "\"");
 					videoNameItem = item;
 				} else {
 					// Handle Uploaded files.
 					log.info("Field Name = \"" + item.getFieldName()
 							+ "\", File Name = \"" + item.getName()
 							+ "\", Content type = \""
 							+ item.getContentType() // TODO Save this
 							+ "\", File Size (bytes) = \"" + item.getSize()
 							+ "\"");
 					fileItem = item;
 				}
 			}
 
 			if (videoNameItem == null) {
 				log.severe("Video name field not found in video upload form");
 				return;
 			}
 			if (fileItem == null) {
 				log.severe("File field not found in video upload form");
 				return;
 			}
 
 			/*
 			 * Write file to the ultimate location.
 			 */
 			FileItem i = fileItem;
 			file = new File(destinationDir, fileItem.getName()); // Allocate the space
 			fileItem.write(file); // Save the file to the allocated space
 			int projectId = getProjectIdFromSessionAttributes(session);
 			String videoName = videoNameItem.getString();
 			if (Security.isSafeVideoName(videoName)
 					&& Security.isSafeVideo(i)
 					&& Security.isUniqueVideoName(videoName, projectId)
 					&& Security.videoFits(DatabaseApi
 							.getNumberOfVideos(projectId))) {
 				String[] videoUrlAndIcon = S3Api.uploadFile(file);
 				String videoUrl = videoUrlAndIcon[0];
 				String videoIcon = videoUrlAndIcon[1];
 				if (videoUrl != null) {
 					DatabaseApi.addVideo(new Video(videoName, videoUrl,
 							videoIcon, projectId, -1, -1, false)); // TODO Get icon from Zencoder.
 					// File downloadedFile = S3Api.downloadFile(videoUrl); // TODO Add to /temp/ folder so it can be played in the player.
 					out.println("File uploaded. Please close this window and refresh the editor page.");
 					out.println();
 					response.sendRedirect("editor.jsp"); // Keep the user on the same page.
 				} else {
 					out.println("Upload Failed. Error uploading video to the cloud.");
 					log.warning("Upload Failed. Error uploading video to the cloud.");
 					// response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 					return;
 				}
 			} else if (!Security.isSafeVideoName(videoName)) {
 				out.println("Upload Failed. Video name may consist of a-z, 0-9; and must begin with a letter.");
 				log.warning("Upload Failed. Video name may consist of a-z, 0-9; and must begin with a letter.");
 				// response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
 				return;
 			} else if (!Security.isUniqueVideoName(videoName, projectId)) {
 				out.println("Upload Failed. Video name already exists.");
 				log.warning("Upload Failed. Video name already exists.");
 				return;
 			} else {
 				out.println("Upload Failed. Video type is invalid or maximum number of videos reached.");
 				log.warning("Upload Failed. Video type is invalid or maximum number of videos reached.");
 				// response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Name");
 				return;
 			}
 
 		} catch (FileUploadException e) {
 			log.severe("Error encountered while parsing the request in the upload handler");
 			out.println("Upload failed.");
 			e.printStackTrace();
 		} catch (InvalidFileSizeException e) {
 			log.severe("Invalid file size");
 			out.println("Upload failed (invalid file size)");
 			e.printStackTrace();
 		} catch (Exception e) {
 			log.severe("Unknown error encountered while uploading file");
 			out.println("Upload failed (unknown reason).");
 			e.printStackTrace();
 		} finally {
 			if (out != null) {
 				out.close();
 			}
 			if (file != null) {
 				file.delete();
 			}
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
 
 		if (generalRequest.command.equals("deleteAccount")) {
 			handleDeleteAccount(request, response, session, json);
 		} else if (generalRequest.command.equals("getAccountInformation")) {
 			handleGetAccountInformation(request, response, session, json);
 		} else if (generalRequest.command.equals("getProjects")) {
 			handleGetProjects(request, response, session, json);
 		} else if (generalRequest.command.equals("createProject")) {
 			handleCreateProject(request, response, session, json);
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
 			log.warning("JSON request not recognized.");
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 			return;
 		}
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
 		out.close();
 	}
 
 	private void handleGetAccountInformation(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		response.setContentType("application/json; charset=utf-8");
 		PrintWriter out = response.getWriter();
 
 		String name = (String) session.getAttribute(Attribute.NAME.toString());
 		String email = (String) session
 				.getAttribute(Attribute.EMAIL.toString());
 		String password = (String) session.getAttribute(Attribute.PASSWORD
 				.toString());
 		String securityQuestion = (String) session
 				.getAttribute(Attribute.SECURITY_QUESTION.toString());
 		String securityAnswer = (String) session
 				.getAttribute(Attribute.SECURITY_ANSWER.toString());
 		out.println(new Gson().toJson(new GetAccountInformationResponse(name,
 				email, password, securityQuestion, securityAnswer)));
 		
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
 		log.severe("handleCreateProject has not yet been implemented.");
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
 		DatabaseApi.deleteVideo(videoId);
 
 		S3Api.deleteFileInS3(DatabaseApi.getVideoName(videoId));
 
 		S3Api.deleteFileInS3(DatabaseApi.getVideoName(videoId));
 
 		out.println(deleteVideoRequest.arguments.video
 				+ " deleted successfully.");
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
 			DatabaseApi.addVideo(new Video(videoFragment.getName(),
 					videoFragment.getUrl(), videoFragment.getIcon(), projectId,
 					-1, -1, false)); // projectId not computed by Zencoder
 		}
 
 		out.println(splitVideoRequest.arguments.video + " split at "
 				+ splitVideoRequest.arguments.splitTimeInSeconds
 				+ " seconds successfully.");
 		out.close();
 	}
 
 	private void handleCombineVideos(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 
 	throws IOException, NoSuchAlgorithmException, InvalidFileSizeException,
 			ServiceException, InterruptedException {
 
 		CombineVideosRequest combineVideosRequest = new Gson().fromJson(json,
 				CombineVideosRequest.class);
 		log.info("COMBINING VIDEOS");
 		// response.setContentType("text/plain");
 
 		// PrintWriter out = response.getWriter();
 		int projectId = getProjectIdFromSessionAttributes(session);
 		String[] videos = DatabaseApi.getVideosOnTimeline(projectId);
 		String[] videoURLs = new String[videos.length];
 		for (int i = 0; i < videos.length; i++) {
 			videoURLs[i] = DatabaseApi.getVideoUrl(DatabaseApi.getVideoId(
 					videos[i], projectId));
 		}
 
 		String combinedURL = combineVideos(videoURLs, videos);
 		// My view resource servlet:
 		// Use a ServletOutputStream because we may pass binary information
 		log.info("Combined. Now Dowloading");
 		final ServletOutputStream out = response.getOutputStream();
 		response.setContentType("application/octet-stream");
 
 		File file = new File(combinedURL);
 		BufferedInputStream is = new BufferedInputStream(new FileInputStream(
 				file));
 		byte[] buf = new byte[4 * 1024]; // 4K buffer
 		int bytesRead;
 		log.info("downloading");
 		while ((bytesRead = is.read(buf)) != -1) {
 			log.info("in while");
 			out.write(buf, 0, bytesRead);
 			log.info("...Dowloading in while...");
 		}
 
 		is.close();
 		out.close();
 		log.info("end of handleCombinedVideos");
 	}
 
 	private String combineVideos(String[] videoURLs, String[] videos)
 			throws IOException, NoSuchAlgorithmException,
 			InvalidFileSizeException, ServiceException, InterruptedException {
 		String[] result = new String[2];
 		result[0] = "combined";
 		Runtime r = Runtime.getRuntime();
 		boolean isWindows = isWindows();
 		boolean isLinux = isLinux();
 
 		File combined = new File(videoURLs[0]);
 		S3Api.downloadVideosToTemp(videoURLs[0]);
 		Process p;
 		String videoName = "";
 		for (int i = 0; i < videos.length - 1; i++) { // Use i+1 everywhere
 			videoName = S3Api.downloadVideosToTemp(videoURLs[i + 1]);
 			p = r.exec("ffmpeg -i " + combined.getName() + " -sameq temp.mpg",
 					null, tempDir);
 
 			/*
 			 * BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream())) ;
 			 * String currentLine = null;
 			 * while (( currentLine = in.readLine()) != null )
 			 * System.out.println ( currentLine ) ;
 			 */
 
 			// p.waitFor();
 			// log.info("Process 1...Done");
 			p = r.exec("ffmpeg -i " + videoName + " -sameq temp2.mpg", null,
 					tempDir);
 			// p.waitFor();
 			// log.info("Process 2...Done");
 			if (isWindows) {
 
 				log.info("Windows");
 				p = r.exec(
 						"cmd /c copy /b temp.mpg+temp2.mpg intermediateTemp.mpg",
 						null, tempDir);
 				InputStream is2 = p.getInputStream();
 				log.info("InputStream2");
 				InputStreamReader isr2 = new InputStreamReader(is2);
 				log.info("InputStreamReader2");
 				BufferedReader br2 = new BufferedReader(isr2);
 				String line;
 				for (int j = 0; j < 10; j++) {
 					line = br2.readLine();
 					if (line != null) {
 						log.info("Line " + j + 1 + ": " + line);
 					}
 				}
 				// p.waitFor();
 				log.info("after Windows Process finishes");
 				// log.info("Process 3...Done");
 				// r.exec("cmd /c del temp\\"+videos[i+1]+".mpg");
 			} else if (isLinux) {
 
 				log.info("Linux");
 				String[] arr = { "/bin/sh", "-c",
 						"cat temp.mpg temp2.mpg > intermediateTemp.mpg" };
 				p = r.exec(arr, null, tempDir);
 				// p.waitFor();
 				// log.info("Process 3...Done");
 				// r.exec("rm temp\\"+videos[i+1]+".mpg");
 			} else {
 				return null;
 			}
 			log.info("after IFS");
 
 			p = r.exec("ffmpeg -i intermediateTemp.mpg -sameq combined.ogv",
 					null, tempDir);
 			InputStream is2 = p.getErrorStream();
 			log.info("InputStream");
 			InputStreamReader isr2 = new InputStreamReader(is2);
 			log.info("InputStreamReader");
 			BufferedReader br2 = new BufferedReader(isr2);
 			String line;
 			for (int j = 0; j < 25; j++) {
 				line = br2.readLine();
 				if (line != null) {
 					log.info("FFMPEG Line " + j + 1 + ": " + line);
 				}
 			}
 			log.info("after last ffmpeg process");
 
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					p.getErrorStream()));
 			String currentLine = null;
 			while ((currentLine = in.readLine()) != null)
 				System.out.println(currentLine);
 			p.waitFor();
 			// log.info("Process 4...Done");
 			combined = new File(tempDir.getAbsolutePath() + File.separator
 					+ "combined.ogv");
 			// process.waitFor();
 
 			videos[0] = "combined";
 		}
 
 		// Removing all temp files except for the one combined video
 		// result[1] = videoURLs[0];
 		// if(inFor){
 		// return S3Api.uploadFile(combined);
 		// }
 		// else{
 		return combined.getAbsolutePath();
 		// }
 		// return S3Api.uploadFile(new File(videoName));
 		// return null;
 		// return videoURLs[0];
 
 	}
 
 	// http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
 	private Boolean isWindows() {
 		String os = System.getProperty("os.name").toLowerCase();
 		// windows
 		return (os.indexOf("win") >= 0);
 	}
 
 	private Boolean isLinux() {
 		String os = System.getProperty("os.name").toLowerCase();
 		// linux or unix
 		return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
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
 		out.close();
 	}
 
 	private void handleIsFirstSignIn(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, String json)
 			throws IOException {
 		response.setContentType("application/json; charset=utf-8");
 		PrintWriter out = response.getWriter();
 		out.println(new Gson().toJson((Boolean) session
 				.getAttribute(Attribute.IS_FIRST_SIGN_IN.toString())));
 		out.close();
 	}
 }
