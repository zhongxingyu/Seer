 package dashboard.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import com.google.appengine.labs.repackaged.org.json.JSONException;
 import com.google.appengine.labs.repackaged.org.json.JSONObject;
 
 import dashboard.error.InvalidAmountException;
 import dashboard.error.InvalidEndDateException;
 import dashboard.error.InvalidStudyMomentException;
 import dashboard.error.NoSuchCourseException;
 import dashboard.model.Course;
 import dashboard.model.Location;
 import dashboard.model.Student;
 import dashboard.model.StudyMoment;
 import dashboard.registry.CourseRegistry;
 
 
 public class MatchLocationServlet extends HttpServlet{
 	
 	private static final long serialVersionUID = 8568509987539754981L;
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		resp.setContentType("application/json");
 		JSONObject root = new JSONObject();
 		HttpSession session = req.getSession();
 		Student student = (Student)session.getAttribute("student");
 		PrintWriter writer = resp.getWriter();
 		boolean invalid = false;
 		double lat = 0;
		double lang = 0;
 		int acc = 0;
 		try{
 			lat = Double.parseDouble(req.getParameter("lat"));
			lang = Double.parseDouble(req.getParameter("long"));
 			acc = Integer.parseInt(req.getParameter("acc"));
 		}catch(NumberFormatException e){
 			invalid = true;
 		}catch(NullPointerException e){
 			invalid = true;
 		}
 		try{
 			if(student == null)  
 	        	root.put("status", "UNAUTHORIZED");
 			else if(invalid){
 	        	root.put("status", "ILLEGAL PARAMETERS");
 			}else{
				Location testLoc = new Location(lat,lang,acc,null,null);
 				Location bestMatch = student.matchStarredLocation(testLoc, 1000);
 				if(bestMatch==null){
 					writer.println("{status:\"NONE FOUND\"}");
 				}else{
 		        	root.put("status", "OK");
 		        	root.put("result", bestMatch.getAlias());
 				}
 			}
 		}catch(JSONException e){}
 		writer.println(root.toString());
 	}
 		
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		}
 }
