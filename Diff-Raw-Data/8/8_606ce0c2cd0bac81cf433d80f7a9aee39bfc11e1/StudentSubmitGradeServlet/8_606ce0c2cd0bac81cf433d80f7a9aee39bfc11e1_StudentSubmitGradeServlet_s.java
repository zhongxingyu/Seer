 package edu.harvard.cs262.grading.client.web;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.harvard.cs262.grading.server.services.Assignment;
 import edu.harvard.cs262.grading.server.services.AssignmentImpl;
 import edu.harvard.cs262.grading.server.services.GradeCompilerService;
 import edu.harvard.cs262.grading.server.services.InvalidGraderForStudentException;
 import edu.harvard.cs262.grading.server.services.NoShardsForAssignmentException;
 import edu.harvard.cs262.grading.server.services.Score;
 import edu.harvard.cs262.grading.server.services.ScoreImpl;
 import edu.harvard.cs262.grading.server.services.ServiceLookupUtility;
 import edu.harvard.cs262.grading.server.services.Student;
 import edu.harvard.cs262.grading.server.services.StudentImpl;
 import edu.harvard.cs262.grading.server.services.Submission;
 import edu.harvard.cs262.grading.server.services.SubmissionStorageService;
 import edu.harvard.cs262.grading.server.web.ServletConfigReader;
 
 public class StudentSubmitGradeServlet extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2228843269260363252L;
 	private SubmissionStorageService submissionStorage;
 	private GradeCompilerService submissionServer;
 
 	public void lookupServices() {
 
 		try {
 			// get reference to database service
 			submissionStorage = (SubmissionStorageService) ServiceLookupUtility
 					.lookupService(
 							new ServletConfigReader(this.getServletContext()),
 							"SubmissionStorageService");
 		} catch (RemoteException e) {
 			e.printStackTrace(); // To change body of catch statement use File |
 									// Settings | File Templates.
 		} catch (NullPointerException e) {
 			e.printStackTrace(); // To change body of catch statement use File |
 									// Settings | File Templates.
 		}
 		if (submissionStorage == null) {
 			System.err.println("Looking up SubmissionStorageService failed.");
 		}
 
 		try {
 			// get reference to database service
 			submissionServer = (GradeCompilerService) ServiceLookupUtility
 					.lookupService(
 							new ServletConfigReader(this.getServletContext()),
 							"GradeCompilerService");
 		} catch (RemoteException e) {
 			e.printStackTrace(); // To change body of catch statement use File |
 									// Settings | File Templates.
 		} catch (NullPointerException e) {
 			e.printStackTrace(); // To change body of catch statement use File |
 									// Settings | File Templates.
 		}
 		if (submissionServer == null) {
 			System.err
 					.println("StudentGetGradesServlet: Looking up submission service failed.");
 		}
 
 	}
 
 	public void init(ServletConfig config) throws ServletException {
 
 		super.init(config);
 
 		lookupServices();
 
 	}
 
 	// passed id for student and actual grade
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 
 		// get posted parameters (may have to update parameter names)
 		String rawScore = request.getParameter("score");
 		String rawGrader = request.getParameter("uid");
 		String rawStudent = request.getParameter("student");
 		String rawAssignment = request.getParameter("assignment");
 		String comments = request.getParameter("comments");
 
 		if (rawScore == null || rawGrader == null || rawStudent == null
 				|| rawAssignment == null) {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 					"parameters not set");
 		} else {
 
 			// try to convert parameters into usable format
 			try {
 				Long studentID = Long.parseLong(rawStudent);
 				Long graderID = Long.parseLong(rawGrader);
 				Integer scoreValue = Integer.parseInt(rawScore);
 				Long assignmentID = Long.parseLong(rawAssignment);
 				Score score = new ScoreImpl(scoreValue, scoreValue);
 				Student grader = new StudentImpl(graderID);
 				Student student = new StudentImpl(studentID);
 				Assignment assignment = new AssignmentImpl(assignmentID,"");
 
 				Submission submission = submissionStorage.getLatestSubmission(
 						student, assignment);
 				if (submission == null) {
 					response.sendError(
 							HttpServletResponse.SC_BAD_REQUEST,
 							"Student "
 									+ studentID
 									+ " does not have a submission for assignment "
 									+ assignmentID);
 				} else {
 					submissionServer.storeGrade(grader, submission, score,
 							comments);
 
 					response.setContentType("text/plain");
 					response.setCharacterEncoding("UTF-8");
 					response.getWriter().write(
 							"Succesfully submitted grade for student "
 									+ studentID + "'s assignment "
 									+ assignmentID + ".");
 				}
 
 			} catch (NumberFormatException e) {
 				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 						"invalid values given");
 			} catch (NullPointerException e) {
 				response.sendError(
 						HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 						"grade upload failed");
 				e.printStackTrace();
 			} catch (InvalidGraderForStudentException e) {
 				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 				"grader not assigned to grade that student");
 			} catch (NoShardsForAssignmentException e) {
 				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 				"assignment has no shards");
 			}
 		}
 	}
 
 }
