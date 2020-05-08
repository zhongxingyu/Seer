 package com.ems.controller.priv;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 
 import com.ems.dao.EventDao;
 import com.ems.dao.UserDao;
 import com.ems.model.Event;
 import com.ems.model.User;
 
 
 /**
  * Servlet implementation class UserController
  */
 @WebServlet(urlPatterns = {
 		"/private/eventList.html", 
 		"/private/event.jsp", 
 		"/private/eventAdd",
 		"/private/eventDelete"
 		})
 public class EventController extends HttpServlet {
 	
 	// commons logging references
 	static Logger log = Logger.getLogger(EventController.class.getName());
 	
 	private static final long serialVersionUID = 1L;
     private static String INSERT_OR_EDIT = "/event.jsp";
     private static String LIST_USER = "/eventList.jsp";
     private static String UNAUTHORIZED_PAGE = "/WEB-INF/jsp/private/errors/unauthorized.jsp";
 
     private EventDao dao;   
 	
     /**
      * @see HttpServlet#HttpServlet()
      */
     public EventController() {
         super();
         log.debug("###################################");
     	log.trace("START");
 		dao = new EventDao();
         log.debug("Dao object instantiated");
         log.trace("END");
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     	log.trace("START");
     	String forward="";
         String action = request.getParameter("action");
         
 		UserDao ud = new UserDao();
 		User  systemUser = ud.getUserByEmail(request.getUserPrincipal().getName());
 		
 		HttpSession session = request.getSession(true);
 		session.removeAttribute("systemUser");
 		session.setAttribute("systemUser",systemUser);
 		
         if (action == null){
         	log.debug("action is NULL");
         	action="";
         }
 // #########################################################################################        
         if (action.equalsIgnoreCase("delete")){
             log.debug("action: DELETE - " + action);
             int id = Integer.parseInt(request.getParameter("id"));
             //check if systemUser is authorized to delete the record
             if (dao.canBeChangedBy(id).contains(systemUser.getId())){
             	log.debug("Event is getting deleted");
             	dao.deleteRecord(id);
             	forward = LIST_USER;
                 //check if systemUser is admin
             	if (systemUser.getRole().equals("admin")){
             		request.setAttribute("records", dao.getAllRecords());
             	}
                 //check if systemUser is event_mng
                 else if (systemUser.getRole().equals("event_mng")){
             	   	dao.deleteRecord(id);
                 	forward = LIST_USER;
                 	request.setAttribute("records", dao.getRecordsById_event_mng(systemUser.getId()));
             	}
             }            
         } 
 // #########################################################################################
         else if (action.equalsIgnoreCase("edit")){
             log.debug("action: EDIT - " + action);
             forward = INSERT_OR_EDIT;
             int id = Integer.parseInt(request.getParameter("id"));
             //check if systemUser is authorized to delete the record
             if (dao.canBeChangedBy(id).contains(systemUser.getId())){
                 Event record = dao.getRecordById(id);
                 request.setAttribute("record", record);
                 List<User> listOfEvent_mng = ud.getAllRecordWithRole("event_mng");
                 session.setAttribute("listOfEvent_mng", listOfEvent_mng);
             }            
         }
 // #########################################################################################
         else if (action.equalsIgnoreCase("insert")){
             log.debug("action: INSERT - " + action);
             if (systemUser.getRole().equals("admin") || systemUser.getRole().equals("event_mng")){
                 request.removeAttribute("record");
                 forward = INSERT_OR_EDIT;
                 List<User> listOfEvent_mng = ud.getAllRecordWithRole("event_mng");
                 log.debug("listOfEvent_mng: " + listOfEvent_mng);
                 session.setAttribute("listOfEvent_mng", listOfEvent_mng);
             }
         }
 // #########################################################################################        
         else if (action.equalsIgnoreCase("listRecord")){
             log.debug("action: listRecord - " + action);
             if (systemUser.getRole().equals("admin")){
                 forward = LIST_USER;
                 request.setAttribute("records", dao.getAllRecords());
             }
             else if (systemUser.getRole().equals("event_mng")){
                forward = LIST_USER;
                request.setAttribute("records", dao.getRecordsById_event_mng(systemUser.getId()));
             }
 
         } else {
             log.debug("action: ELSE - " + action);
             if (systemUser.getRole().equals("admin")){
                 forward = LIST_USER;
                 request.setAttribute("records", dao.getAllRecords());
             }
             else if (systemUser.getRole().equals("event_mng")){
                forward = LIST_USER;
                request.setAttribute("records", dao.getRecordsById_event_mng(systemUser.getId()));
             }
         }
 // #########################################################################################        
         log.debug("forward: " + forward);
         log.debug("action: " + action);
     	
         log.debug("######################");
         log.debug("systemUser.getRole(): " + systemUser.getRole().toString());
         
 		if (systemUser.getRole().equals("admin") || systemUser.getRole().equals("event_mng")){
 	        log.debug("systemUser is an admin or event_mng");
 		    forward = "/WEB-INF/jsp/private" + forward;
 		}
 		else {
 			forward = UNAUTHORIZED_PAGE;
 		}
         
 
 
 		try {
 			getServletConfig().getServletContext().getRequestDispatcher(forward).forward(request, response);
 			} 
 		catch (Exception ex) {
 				ex.printStackTrace();
 			}
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	log.trace("START");
     	Event record = new Event();

 		record.setId_manager(Integer.parseInt(request.getParameter("id_manager")));
     	record.setName(request.getParameter("name"));
     	record.setDescription(request.getParameter("description"));
     	record.setStart(request.getParameter("start"));
     	record.setEnd(request.getParameter("end"));
         record.setEnrollment_start(request.getParameter("enrollment_start"));
         record.setEnrollment_end(request.getParameter("enrollment_end"));
         String id = request.getParameter("id");
         
     	log.debug("id: " + id);
     	
         if(id == null || id.isEmpty()) {
         	log.debug("INSERT");
             dao.addRecord(record);
         }
         else
         {
         	log.debug("UPDATE");
         	record.setId(Integer.parseInt(id));
             dao.updateRecord(record);
         }
         
         
         String forward = "/WEB-INF/jsp/private" + LIST_USER;
         log.debug("forward: " + forward);
         request.setAttribute("records", dao.getAllRecords());
 		try {
 			getServletConfig().getServletContext().getRequestDispatcher(forward).forward(request, response);
 			} 
 		catch (Exception ex) {
 				ex.printStackTrace();
 			}
     	log.trace("END");
 	}
 
 }
