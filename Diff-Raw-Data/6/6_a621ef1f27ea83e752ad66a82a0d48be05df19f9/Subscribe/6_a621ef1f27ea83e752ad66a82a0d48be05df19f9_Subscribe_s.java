 /*
  * Created on 25.04.2006
  * 
  */
 package eionet.meta.notif;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import com.tee.xmlserver.*;
 
 import eionet.util.*;
 
 import org.apache.xmlrpc.XmlRpcClient;
 
 /**
  * 
  * @author Jaanus Heinlaid
  */
 public class Subscribe extends HttpServlet{
 	
 	/** */
 	public static final String PROP_UNS_EVENTTYPE_PREDICATE = "uns.eventtype.predicate";
 	public static final String PROP_UNS_DATASET_PREDICATE = "uns.dataset.predicate";
 	public static final String PROP_UNS_TABLE_PREDICATE = "uns.table.predicate";
 	public static final String PROP_UNS_COMMONELEM_PREDICATE = "uns.commonelem.predicate";
     public static final String PROP_UNS_REGSTATUS_PREDICATE = "uns.definition-status.predicate";
 	public static final String PROP_UNS_XMLRPC_SERVER_URL = "uns.xml.rpc.server.url";
 	public static final String PROP_UNS_CHANNEL_NAME = "uns.channel.name";
 	public static final String PROP_UNS_SUBSCRIPTIONS_URL = "uns.subscriptions.url";
 	public static final String PROP_UNS_USERNAME = "uns.username";
 	public static final String PROP_UNS_PASSWORD = "uns.password";
 	public static final String PROP_UNS_DONTSENDEVENTS = "uns.dont-send-events";
 	public static final String PROP_UNS_SUBSCRIBE_FUNC = "uns.make.subsription.function";
 	public static final String PROP_UNS_SEND_NOTIFICATION_FUNC = "uns.send.notification.function";
 	
 	/** */
 	public static final String DATASET_CHANGED_EVENT = "Dataset changed";
 	public static final String TABLE_CHANGED_EVENT = "Table changed";
 	public static final String COMMON_ELEMENT_CHANGED_EVENT = "Common element changed";
     public static final String REGSTATUS_CHANGED_EVENT = "";
 	
 	public static final String NEW_DATASET_EVENT = "New dataset";
 	public static final String NEW_TABLE_EVENT = "New table";
 	public static final String NEW_COMMON_ELEMENT_EVENT = "New common element";
 
 	/** */
 	private static String predEventType = null;
 	private static String channelName = null;
 	private static String serverURL = null;
 	
 	private static String unsUsername = null;
 	private static String unsPassword = null;
 
 	private static String unsMakeSubscriptionFunction = null;
 	
     private static Hashtable predsMap = null;
     private static Hashtable eventsMap = null;
     private static boolean initialized = false;
 
 	/*
 	 *  (non-Javadoc)
 	 * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
 	 */
 	public void init() throws ServletException{
 		try{
 			if (initialized==false)
 				initialize();
 		}
 		catch (Exception e) {
             e.printStackTrace(System.out);
             throw new ServletException(e);
         }
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	public void service(HttpServletRequest req, HttpServletResponse res)
                                             throws ServletException, IOException {
         
         try {
         	// make sure SUCCESS flag is cleared from session,
         	// even though it is a responsibility of the one who checks the flag
 	        req.getSession().removeAttribute("SUCCESS"); 
 	        
 	        // get user object from session
 			AppUserIF user = SecurityUtil.getUser(req);
 			// if no user object found, it means the user's session has
 			// expired, so we refresh subscribe.jsp which enables login then
 			if (user==null)
 				req.getRequestDispatcher("subscribe.jsp").forward(req, res);				
 			String username = user.getUserName();
 			if (username==null)
 				throw new Exception("User object did not contain the username");
 			
 			// set up the filters
 			Hashtable filter;
 			Vector filters = new Vector();
 			
 			String newDatasets = req.getParameter("new_datasets");
 			String newTables = req.getParameter("new_tables");
 			String newCommonElems = req.getParameter("new_common_elems");
 			if (newDatasets!=null){
 				filter = new Hashtable();
 				filter.put(predEventType, NEW_DATASET_EVENT);
 				filters.add(filter);
 			}
 			if (newTables!=null){
 				filter = new Hashtable();
 				filter.put(predEventType, NEW_TABLE_EVENT);
 				filters.add(filter);
 			}
 			if (newCommonElems!=null){
 				filter = new Hashtable();
 				filter.put(predEventType, NEW_COMMON_ELEMENT_EVENT);
 				filters.add(filter);
 			}
 			
 			Enumeration parNames = req.getParameterNames();
 			while (parNames!=null && parNames.hasMoreElements()){
 				
 				String parName = (String)parNames.nextElement();
 				String pred = (String)predsMap.get(parName);
 				String event = (String)eventsMap.get(parName);
 				if (pred!=null && event!=null){
 					String parValue = req.getParameter(parName);
 					if (parValue!=null && !parValue.equals("_none_")){
 						filter = new Hashtable();
                         if(parName != null && !parName.equals("reg_status")){
                             filter.put(predEventType, event);
                         }
 						if (!parValue.equals("_all_")){
 							filter.put(pred, parValue);
 						}
 						filters.add(filter);
 					}
 				}
 			}
 			
 			// DEBUG
 			//logFilters(filters);
 			
 			// call RPC method
 	        if (filters.size()>0){
 		        	
 				// set up the xml-rpc server object
 	        	XmlRpcClient server = new XmlRpcClient(serverURL);
 				server.setBasicAuthentication(unsUsername, unsPassword);
 	        	
 	            // make subscription
 				Vector params = new Vector();
 				params = new Vector();
 	            params.add(channelName);
 	            params.add(username);
 	            params.add(filters);            
 	            String makeSubscription =
 	            (String) server.execute(unsMakeSubscriptionFunction, params);
 	
 	        }
 	        
 	        req.getSession().setAttribute("SUCCESS", "");
 	        res.sendRedirect("subscribe.jsp");
 	        
         }
         catch (Throwable t) {
             t.printStackTrace(System.out);
             String msg = t.getMessage();
             if (msg==null) msg = t.toString();
 			req.setAttribute("DD_ERR_MSG", msg);
 			req.setAttribute("DD_ERR_BACK_LINK", "subscribe.jsp");
 			req.getRequestDispatcher("error.jsp").forward(req, res);
         }
     }
 	
 	/**
 	 * 
 	 *
 	 */
 	private static void initialize() throws Exception{
 		
 		try{
             predEventType = Props.getProperty(PROP_UNS_EVENTTYPE_PREDICATE);
             serverURL = Props.getProperty(PROP_UNS_XMLRPC_SERVER_URL);
             channelName = Props.getProperty(PROP_UNS_CHANNEL_NAME);
             
     		predsMap = new Hashtable();
     		predsMap.put("dataset", Props.getProperty(PROP_UNS_DATASET_PREDICATE));
     		predsMap.put("table", Props.getProperty(PROP_UNS_TABLE_PREDICATE));
     		predsMap.put("common_element", Props.getProperty(PROP_UNS_COMMONELEM_PREDICATE));
             predsMap.put("reg_status", Props.getProperty(PROP_UNS_REGSTATUS_PREDICATE));
 
     		eventsMap = new Hashtable();
     		eventsMap.put("dataset", DATASET_CHANGED_EVENT);
     		eventsMap.put("table", TABLE_CHANGED_EVENT);
     		eventsMap.put("common_element", COMMON_ELEMENT_CHANGED_EVENT);
             eventsMap.put("reg_status", REGSTATUS_CHANGED_EVENT);
     		
     		unsUsername = Props.getProperty(PROP_UNS_USERNAME);
 			unsPassword = Props.getProperty(PROP_UNS_PASSWORD);
 			
 			unsMakeSubscriptionFunction = Props.getProperty(PROP_UNS_SUBSCRIBE_FUNC);
 		}
 		catch (Throwable t) {
             t.printStackTrace(System.out);
             throw new Exception(t);
         }
 		
 		initialized = true;
 	}
 	
 	/**
 	 * 
 	 * @param users
 	 * @param filters
 	 * @throws Exception
 	 */
 	public static void subscribe(String user, Vector filters) throws Exception{
 		Vector v = new Vector();
 		v.add(user);
 		subscribe(v, filters);
 	}
	
 	/**
 	 * 
	 * @param channelName
	 * @param userName
 	 * @param filters
 	 * @throws Exception
 	 */
 	public static void subscribe(Vector users, Vector filters) throws Exception{
 		
 		if (users==null || users.size()==0 || filters==null || filters.size()==0)
 			return;
 		
 		if (initialized==false)
 			initialize();
 		
 		// set up the xml-rpc server object
     	XmlRpcClient server = new XmlRpcClient(serverURL);
 		server.setBasicAuthentication(unsUsername, unsPassword);
     	
         // make subscription
 		for (int i=0; i<users.size(); i++){
 			Vector params = new Vector();
 			params = new Vector();
 	        params.add(channelName);
 	        params.add(users.get(i));
 	        params.add(filters);            
 	        String s = (String) server.execute(unsMakeSubscriptionFunction, params);
 		}
 	}
 
 	/**
 	 * 
 	 * @param users
 	 * @param commonElmIdfier
 	 * @throws Exception
 	 */
 	public static void subscribeToCommonElement(Vector users, String commonElmIdfier)
 																			throws Exception{
 		// we don't do this when in development environment (assume it's Win32)
 		if (File.separatorChar == '\\')
 			return;
 			
 		if (users==null || users.size()==0 || commonElmIdfier==null)
 			return;
 
 		if (initialized==false)
 			initialize();
 
 		String predicate = (String)predsMap.get("common_element");
 		if (predicate!=null){
 			Vector filters = new Vector();
 			Hashtable filter = new Hashtable();
 			filter.put(predEventType, COMMON_ELEMENT_CHANGED_EVENT);
 			filter.put(predicate, commonElmIdfier);
 			filters.add(filter);
 			subscribe(users, filters);
 		}
 	}
 	
 	/*
 	 * 
 	 */
 	private void logFilters(Vector filters){
 		
 		for (int i=0; filters!=null && i<filters.size(); i++){
 			Hashtable filter = (Hashtable)filters.get(i);
 			System.out.println("========================= filter " + i);
 			System.out.println(filter);
 		}
 	}
 }
