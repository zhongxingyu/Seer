 package dashboard.servlet;
 
 import java.io.IOException;
 import java.util.Date;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import dashboard.error.AlreadyEndedException;
 import dashboard.error.InvalidAmountException;
 import dashboard.error.InvalidEndDateException;
 import dashboard.error.InvalidStudyMomentException;
 import dashboard.error.NoSuchCourseException;
 import dashboard.error.NotStudyingException;
 import dashboard.model.Course;
 import dashboard.model.Location;
 import dashboard.model.Student;
 import dashboard.model.StudyMoment;
 import dashboard.registry.AchievementRegistry;
 import dashboard.registry.CourseRegistry;
 import dashboard.util.RegistryInitializer;
 
 public class TrackingServlet extends HttpServlet{
 
 	private static final long serialVersionUID = -786837324508180891L;
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		HttpSession session = req.getSession();
 		if(!RegistryInitializer.initialized()){
 			RegistryInitializer.initialize(session.getServletContext());
 		}
 		Student student = (Student)session.getAttribute("student");
 		
 		if(student!=null){
 			if(student.getCurrentStudyMoment() == null){
 				session.setAttribute("startTracking", null);
 				session.setAttribute("course", null);
 				session.setAttribute("adres", null);
 				session.setAttribute("alias", null);
 				resp.sendRedirect("/jsp/track/start.jsp");
 			} else {
 				session.setAttribute("startTracking", student.getCurrentStudyMoment().getStart());
 				session.setAttribute("course", student.getCurrentStudyMoment().getCourse());
 				Location loc = student.getCurrentStudyMoment().getLocation();
 				if(loc!=null){
 					session.setAttribute("adres", loc.getAdres());
 					Location matchedLocation = student.matchStarredLocation(loc, 1000);
 					if(matchedLocation!=null)
 						session.setAttribute("alias", matchedLocation.getAlias());
 				}	
 				resp.sendRedirect("/jsp/track/stop.jsp");
 			}
 		} else {
 			resp.sendRedirect("/jsp/login/login.jsp");
 		}
 	}
 	/**
 	 * Called when a user starts or stops tracking a study moment
 	 * @param req
 	 * @param resp
 	 * @throws IOException
 	 */
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		HttpSession session = req.getSession();
 		if(!RegistryInitializer.initialized()){
 			RegistryInitializer.initialize(session.getServletContext());
 		}
 		Student student = (Student)session.getAttribute("student");//get the current user
 		
 		if(req.getParameter("submit").equals("Start") && student.getCurrentStudyMoment() == null){//if the student is not studying yet
 			Course course = null;
 			try {
 				course = CourseRegistry.getCourse((String) req.getParameter("courseinput"));
 			} catch (NoSuchCourseException e) {
 				e.printStackTrace();
 			}
 			Date start = new Date();
 			if(req.getParameter("longitude")!=null){
 				double longitude = Double.parseDouble(req.getParameter("longitude"));
 				double latitude = Double.parseDouble(req.getParameter("latitude"));
 				int accuracy = Integer.parseInt(req.getParameter("accuracy"));
 				String adres = req.getParameter("adres");
 				Location loc = new Location(longitude,latitude,accuracy,adres,null);
 				student.setCurrentStudyMoment(new StudyMoment(start,course,loc));//create a new study moment with location
 				session.setAttribute("adres", adres);
 			}else{
 				student.setCurrentStudyMoment(new StudyMoment(start,course));
 			}
 			if(req.getParameter("alias")!=null){
 				session.setAttribute("alias", req.getParameter("alias"));
 			}
 			session.setAttribute("startTracking", start);
 			session.setAttribute("course", course);
 			resp.sendRedirect("/jsp/track/stop.jsp");
 		} else {//if the student was already studying
 			if(req.getParameter("submit").equals("Stop")){
 				try {
 					Student oldStudent = (Student)student.clone();
 					student.endStudying(new Date(), Integer.parseInt(req.getParameter("amount")),req.getParameter("kind"));
 					session.setAttribute("changedAchievements", AchievementRegistry.getChangedAchievements(student, oldStudent));
 					session.setAttribute("startTracking", null);
 					session.setAttribute("course", null);
 					resp.sendRedirect("/jsp/track/end.jsp");//end the current studymoment
 				} catch (NumberFormatException e) {
					resp.sendRedirect("/jsp/util/error.jsp?msg=Ongeldig aantal!");
 				} catch (AlreadyEndedException e) {
					resp.sendRedirect("/jsp/util/error.jsp?msg=Dit studiemoment werd reeds gestopt!");
 				} catch (InvalidEndDateException e) {
					resp.sendRedirect("/jsp/util/error.jsp?msg=Ongeldige einddatum?");
 				} catch (InvalidAmountException e) {
					resp.sendRedirect("/jsp/util/error.jsp?msg=Ongeldig aantal!");
 				} catch (InvalidStudyMomentException e) {
					resp.sendRedirect("/jsp/util/error.jsp?msg=Overlap");
 					e.printStackTrace();
 				} catch (CloneNotSupportedException e) {
 					e.printStackTrace();
 				}
 			} else if(req.getParameter("submit").equals("Cancel")){//cancel the study moment
 				try {
 					student.cancelCurrentStudyMoment();
 					session.setAttribute("startTracking", null);
 					session.setAttribute("course", null);
 					resp.sendRedirect("/track");
 				} catch (NotStudyingException e) {
					resp.sendRedirect("/jsp/util/error.jsp?msg=U was niet aan het studeren!");
 				}
 			} else {
				resp.sendRedirect("/jsp/util/error.jsp");
 			}
 		}
 	} 
 	
 }
