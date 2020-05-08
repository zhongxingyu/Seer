 package edu.toronto.ece1778.urbaneyes.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.toronto.ece1778.urbaneyes.data.SubmissionManager;
 import edu.toronto.ece1778.urbaneyes.data.SurveyManager;
 import edu.toronto.ece1778.urbaneyes.model.Answer;
 import edu.toronto.ece1778.urbaneyes.model.Question;
 import edu.toronto.ece1778.urbaneyes.model.Submission;
 import edu.toronto.ece1778.urbaneyes.model.Survey;
 
 /**
  * Servlet for conversion of data to CSV format.
  * 
  * @author mcupak
  * 
  */
 @WebServlet(value = "/csv", name = "csvservlet")
 public class CSVServlet extends HttpServlet {
 
 	public static final String SURVEY_PARAM = "sur";
 	public static final String SUBMISSION_PARAM = "sum";
 	private static final long serialVersionUID = 1L;
 	private List<Long> questionList = new ArrayList<Long>();
 
 	@Inject
 	private SubmissionManager subm;
 	@Inject
 	private SurveyManager surm;
 
 	private String generateHeader(Survey s) {
 		StringBuilder output = new StringBuilder();
 		output.append("ID,DATE,SURVEY_ID,LAT,LON,ALT,");
 		for (Question q : s.getQuestions()) {
 			// create ordering of questions
 			questionList.add(q.getId());
 
			output.append("QUESTION_" + q.getId().toString() + ",ANSWER,");
 		}
 		output.append("USER\n");
 
 		return output.toString();
 	}
 
 	private String getAsCSV(Submission s) {
 		StringBuilder output = new StringBuilder();
 		output.append(s.getId() + ",");
 		output.append("\"" + s.getDate() + "\",");
 		output.append(s.getSurvey().getId() + ",");
 		output.append(s.getPoint().getLatitude() + ",");
 		output.append(s.getPoint().getLongitude() + ",");
 		output.append(s.getPoint().getAltitude() + ",");
 
 		// we need ordering of answers to preserve columns in the output
 		Map<Long, Answer> answerMap = new HashMap<Long, Answer>();
 		for (Answer a : s.getAnswers()) {
 			answerMap.put(a.getQuestion().getId(), a);
 		}
 		for (Long l : questionList) {
			output.append(l + ",");
 			output.append("\"" + answerMap.get(l).getName() + "\",");
 		}
 
 		output.append("\"" + s.getUser().getEmail() + "\"\n");
 
 		return output.toString();
 	}
 
 	protected void processRequest(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("text/csv");
 		response.setHeader("Content-Disposition", "attachment;filename=\""
 				+ "data.csv" + "\"");
 		PrintWriter out = response.getWriter();
 
 		Long surveyId = null;
 		Long submissionId = null;
 		try {
 			surveyId = Long.valueOf(request.getParameter(SURVEY_PARAM));
 		} catch (NumberFormatException ex) {
 		}
 		try {
 			submissionId = Long.valueOf(request.getParameter(SUBMISSION_PARAM));
 		} catch (NumberFormatException ex) {
 		}
 
 		try {
 			if (surveyId != null) {
 				// return all survey submissions
 				Survey i = surm.getSurvey(surveyId);
 				if (i != null) {
 					out.write(generateHeader(i));
 					List<Submission> ss = subm.getSubmissionsBySurvey(i);
 					for (Submission j : ss) {
 						out.write(getAsCSV(j));
 					}
 				}
 			} else if (submissionId != null) {
 				Submission i = subm.getSubmission(submissionId);
 				if (i != null) {
 					out.write(getAsCSV(i));
 				}
 			} else {
 				// return empty csv
 				out.write("");
 			}
 		} finally {
 			out.close();
 		}
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		processRequest(request, response);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		processRequest(request, response);
 	}
 
 	@Override
 	public String getServletInfo() {
 		return "CSV servlet";
 	}
 }
