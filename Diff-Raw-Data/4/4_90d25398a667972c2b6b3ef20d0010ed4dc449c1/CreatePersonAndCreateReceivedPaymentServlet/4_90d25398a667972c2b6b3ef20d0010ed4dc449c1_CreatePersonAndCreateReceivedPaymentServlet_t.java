 package userAction;
 
 import java.io.IOException;
 import java.sql.Date;
 import java.text.SimpleDateFormat;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import system.Expenses;
 import system.MyWeddingManager;
 import system.Person;
 import system.TotalExpenses;
 import system.User;
 import system.UserAction;
 
 /**
  * Servlet implementation class CreatePersonAndCreateReceivedPaymentServlet
  */
 @WebServlet("/CreatePersonAndCreateReceivedPayment")
 public class CreatePersonAndCreateReceivedPaymentServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	MyWeddingManager mw = MyWeddingManager.getInstance();
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public CreatePersonAndCreateReceivedPaymentServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
     /**user params**/
 //  public static final String FIRST_NAME_PARAM = "FirstName";
 //	public static final String LAST_NAME_PARAM = "LastName";
 //	public static final String USER_PASSWORD_PARAM = "password";
 //	public static final String RE_ENTER_PASSWORD_PARAM ="Re_enterPassword";
     public static final String USER_EMAIL_PARAM = "email";
     //------------------------------------------------------
     public static final String ERROR_MWSSAGE = "errorMessage";
     public static final String USER_PARAM = "user";
     public static final String USER_NAME_PARAM = "userName";
     public static final String MSG_PARAM ="Msg";
     //--------------------------------------------------------------------
     /**person params**/
     public static final String  PERSON_NAME_PARAM = "personName";
     public static final String  PERSON_LAST_NAME_PARAM = "personLastName";
     public static final String  PERSON_RELATIONSHIP_PARAM = "relationship";
     public static final String  PERSON_ADDRESS_PARAM = "pesronAddress";
     public static final String  PERSON_PHONE_PARAM = "personPhone";
     public static final String  PERSON_EMAIL_PARAM = "personEmail";
     public static final String  PERSON_COMMENT_PARAM = "pesronComment";
     //---------------------------------------------------------------------
     /**Expenses params**/
     public static final String  RECEVED_PAYMENT_PARAM  = "received_payment";
     public static final String  PAYBACK_PARAM  = "payback_payment";
     public static final String  PAYMENT_TYPE_PARAM  ="payment_type";
     public static final String  EVENT_PARAM   = "eventType";
     public static final String  PABACK_PAYMENT_EVENT_PARAM   ="payback_payment_eventType";
     public static final String  EVENT_ADDRESS_PARAM  ="eventAddress";
     public static final String  PAYE_COMMENT_PARAM = "payeComment";
     public static final String  DATE_PARAM ="date";
    
     
     
     
     public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException , IOException{
     	HttpSession session = request.getSession(false);
     	System.out.println("qwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
     	/**user params**/
 //    	String FirstName = (String)request.getParameter(FIRST_NAME_PARAM);
 //    	String LastName = (String)request.getParameter(LAST_NAME_PARAM);
 //    	String password = (String)request.getParameter(USER_PASSWORD_PARAM);
 //    	String Re_enterPassword = (String)request.getParameter(RE_ENTER_PASSWORD_PARAM);
 //    	String email = (String)request.getParameter(USER_EMAIL_PARAM);
     	//----------------------------------------------------------------------
     	/**person params**/
     	String personName = (String)request.getParameter(PERSON_NAME_PARAM);
     	String personLastName = (String)request.getParameter(PERSON_LAST_NAME_PARAM);
     	String relationship = (String)request.getParameter(PERSON_RELATIONSHIP_PARAM);
     	String pesronAddress = (String)request.getParameter(PERSON_ADDRESS_PARAM);
     	String personPhone = (String)request.getParameter(PERSON_PHONE_PARAM);
     	String personEmail = (String)request.getParameter(PERSON_ADDRESS_PARAM);
     	String pesronComment = (String)request.getParameter(PERSON_COMMENT_PARAM);
     	
     	//------------------------------------------------------------------------------
     	/**Expenses params**/	
     	Double received_payment = (Double)Double.parseDouble((String)request.getParameter(RECEVED_PAYMENT_PARAM));
     	Double payback_payment = (Double)Double.parseDouble((String)request.getParameter(PAYBACK_PARAM)); 
     	String payment_type = (String)request.getParameter(PAYMENT_TYPE_PARAM);
     	String eventType = (String)request.getParameter(EVENT_PARAM);
     	String payback_payment_eventType = (String)request.getParameter(PABACK_PAYMENT_EVENT_PARAM);
     	String eventAddress = (String)request.getParameter(EVENT_ADDRESS_PARAM);
     	String  payeComment = (String)request.getParameter(PAYE_COMMENT_PARAM);
     	
     	//String  date = (String)request.getParameter(DATE_PARAM);
     	//1.
     	//String dateString = "2001/03/09";
     	//String dateString = (String)request.getParameter(DATE_PARAM);
     	//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/mm/dd");
     	//Date convertedDate = (Date) dateFormat.parse(dateString); 
 
     	
     	if ((session.getAttribute(USER_EMAIL_PARAM)) == null ){
  
         		request.setAttribute(ERROR_MWSSAGE, "You have to login first");
         
        		this.getServletConfig().getServletContext().getRequestDispatcher("/LogIng.jsp").forward(request, response);
         	return;	
         	}
     	
     	
     	if ((session.getAttribute(USER_EMAIL_PARAM)) != null){
     	 long ts = System.currentTimeMillis();
     	 java.sql.Date sqlDate = new Date(ts);
    		 
     	 User ur = new User((String)(session.getAttribute(USER_EMAIL_PARAM)));
      	UserAction uac = new UserAction(ur);
      	ur = uac.getUser(ur.getEmail());
 
     	
     	Person pr = new Person(0, personName, personLastName, relationship, pesronAddress, personPhone, personEmail, pesronComment, ur.getId());
    	Expenses exp = new Expenses(0, ur.getId(), pr.getFirstName(), pr.getLastName(), pr.getId(), received_payment, payback_payment, payment_type, eventType, payback_payment_eventType, eventAddress, payeComment, sqlDate);
    	
  
     	TotalExpenses toxp = new TotalExpenses(exp.getUser_id());
     	try {
 			uac.CreatePersonAndCreateReceivedPayment(exp, pr, toxp);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			System.out.println("Creating person and expenses filed , pleas tray a gain");
 			e.printStackTrace();
 		}
     	
     	
    
     	session.setAttribute(USER_NAME_PARAM ,ur.getFirstName());
     	session.setAttribute(MSG_PARAM, "Creating person and expenses succeeded");
 
     	
     	this.getServletConfig().getServletContext().getRequestDispatcher("/CreatePersonAndCreateReceivedPayment.jsp").forward(request, response);
     	return;
     	
     	}else {
     		request.setAttribute(ERROR_MWSSAGE, "You have to login first");
     		this.getServletConfig().getServletContext().getRequestDispatcher("/LogIng.jsp").forward(request, response);
         	return;	
     	}
     	
    	
     } 
     
 
 }
