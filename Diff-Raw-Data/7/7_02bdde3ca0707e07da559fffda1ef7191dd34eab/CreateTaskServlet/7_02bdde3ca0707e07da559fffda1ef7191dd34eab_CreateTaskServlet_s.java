 package servlet;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import crowdtrust.AnnotationType;
 import crowdtrust.InputType;
 import crowdtrust.MediaType;
 
 import db.TaskDb;
 
 	public class CreateTaskServlet extends HttpServlet {
 	
 		private static final long serialVersionUID = -2917901106721177733L;
 		private static final String TASKS_DIRECTORY = "/vol/project/2012/362/g1236218/TaskFiles/";
 
 		protected void doPost(HttpServletRequest request,
 	              HttpServletResponse response) throws IOException {
 			
 			response.setContentType("text/html;charset=UTF-8");
 	        PrintWriter out = response.getWriter();
 	        
 	        //validate user credentials
 	        HttpSession session = request.getSession();
 	        if (session == null||session.getAttribute("account_id") == null) {
 	        	response.sendRedirect("/");
 	        	return;
 	        }
 	        int accountID = (Integer) session.getAttribute("account_id");
 	        
 	        //validate user, add task to db, maked task directory
 	        String name = request.getParameter("name");
 	        float accuracy;
 	        int max_labels;
	        int num_answers;
 	        MediaType media_type;
 	        AnnotationType annotation_type;
 	        InputType input_type;
 			try {
 				accuracy = Float.parseFloat(request.getParameter("accuracy"));
 				max_labels = Integer.parseInt(request.getParameter("max_labels"));
				num_answers = Integer.parseInt(request.getParameter("num_answers"));
 				media_type = MediaType.valueOf(request.getParameter("media_type"));
 				annotation_type = AnnotationType.valueOf(request.getParameter("annotation_type"));
 				input_type = InputType.valueOf(request.getParameter("input_type"));
 			} catch (NumberFormatException e) {
 				out.println("accuracy or type not an integer");
 				e.printStackTrace();
 				return;
 			}
 			long expiry;
 			try {
 				expiry = getLongDate(request);
 			} catch (ParseException e) {		
 		        out.println("<html>");
 		        out.println("<head>");			
 		    	out.println("<meta http-equiv=\"Refresh\" content=\"5\"; url=\"addtask.jsp\">");
 		        out.println("</head>");
 		        out.println("<body>");
 		        out.println("bad date given, returning to add task page");
 		        out.println("</body>");
 		        out.println("</html>");
 		        return;
 			}
 			List<String> answers = new LinkedList<String>();
 			String answer = request.getParameter("answer1");
 			for(int i = 2 ; i < num_answers ; i++) {
 				answers.add(answer);
 				answer = request.getParameter("answer" + i);
 			}
 			int tid = TaskDb.addTask(accountID, name, request.getParameter("question"), 
 					accuracy, media_type, annotation_type, input_type, max_labels, expiry, answers);
 	        if( tid > 0) {
 	            File taskFolder = new File(TASKS_DIRECTORY + "/" + tid);
 	        	taskFolder.mkdirs();
 	        	response.sendRedirect("/client/upload.jsp");	        
 	        }
 	}
 		
 
 	private long getLongDate(HttpServletRequest request) throws ParseException{
 		String expiryDateStr = request.getParameter("day") + request.getParameter("month") + request.getParameter("year");
 		Date expiryDate = new SimpleDateFormat("ddMMyyyy").parse(expiryDateStr);
 		return expiryDate.getTime();
 	}
 }
