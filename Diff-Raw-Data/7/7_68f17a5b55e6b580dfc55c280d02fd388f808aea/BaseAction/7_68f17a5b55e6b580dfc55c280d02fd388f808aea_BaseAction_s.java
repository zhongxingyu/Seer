 package net.indybracket.tourney.servlet;
 
 import static net.indybracket.tourney.common.OfyService.ofy;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import net.indybracket.tourney.common.Bracket;
 
 import org.apache.struts.Globals;
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.common.base.Predicate;
 import com.google.common.collect.FluentIterable;
 
 
 /*
 ********************************************************************************
 * Class: BaseAction
 ********************************************************************************
 */ /**
 *
 */
 public abstract class BaseAction
     extends Action
 {
 	private static UserService userService = UserServiceFactory.getUserService();
 
 	public final String FORWARD_RETURN_SUCCESS = "success";
 	public final String FORWARD_RETURN_FAILURE = "failure";
 	
     public String BRACKET_ROOT = "";
     public String BRACKET_FILE_PATH = "";
     public String BRACKET_COMPLETED_PATH = "";
 	public final int MAX_COMMENTS = 20;
 
     public HttpSession oSession;
     
     /*
     ****************************************************************************
     * Constructor()
     ****************************************************************************
     */ /**
     *
     */
     public BaseAction()
     {
         super();
 
     } // Constructor()
 
     /*
     ****************************************************************************
     * execute()
     ****************************************************************************
     */ /**
     *
     */
     public ActionForward execute(
         ActionMapping oMapping, ActionForm oActionForm,
         HttpServletRequest oRequest, HttpServletResponse oResponse)
         throws Exception
     {
     	ActionForward oReturn = null;
         try
         {
 	    	oSession = oRequest.getSession(false);	     
 	        oReturn = doExecute(oMapping, oActionForm, oRequest, oResponse);
         }
         catch(Exception e)
         {
         	oReturn = oMapping.findForward(FORWARD_RETURN_FAILURE);
         }
         
         return oReturn;
     } // execute()
 
     /*
     ****************************************************************************
     * addError()
     ****************************************************************************
     */ /**
     *
     */
     protected void addError(HttpServletRequest oRequest, String sKey)
     {
         ActionMessages oMessages = new ActionMessages();
         ActionMessage oMessage = new ActionMessage(sKey);
 
         oMessages.add(Globals.ERROR_KEY, oMessage);
 
         saveErrors(oRequest, oMessages);
 
     } // addError()
     
     public Bracket readMaster() {
     	Bracket b = ofy().load().type(Bracket.class).id(Bracket.PERFECT_ID).get();
     	b.init();
     	return b;
     }
 
     /*
      ****************************************************************************
      * readBracket()
      ****************************************************************************
      */ /**
      *
      */
      public Bracket readBracket(final String name, final String email, boolean bComplete)
      {
      	FluentIterable<Bracket> brackets = FluentIterable.from(ofy().load().type(Bracket.class).filter("msEntryName", name));
      	if (email != null) {
      		brackets = brackets.filter(new Predicate<Bracket>() {
 				@Override
 				public boolean apply(Bracket b) {
 					return email.equalsIgnoreCase(b.getUserEmail());
 				}});
      	}
      	
      	if (brackets.size() > 1) {
      		throw new RuntimeException("Too many brackets matching name " + name);
      	}
      	
      	if (brackets.isEmpty()) {
      		return null;
      	}
      	
      	Bracket oEntry = brackets.first().get();
      	oEntry.init();
      	oEntry.validate(bComplete);
      	return oEntry;
      } // readBracket()
      
  	public String getBracketId(String bracketName) {
  		return getEmail() + "/" + bracketName;
  	}
  	
  	public String fullNameToBracketId(String fullBracketName) {
  		return fullBracketName.replace(" - ","/");
  	} 	
  	
  	public String getEmail() {
  		return userService.getCurrentUser().getEmail();
  	}
  	
  	public String getNickname() {
  		return userService.getCurrentUser().getNickname();
  	}
      
     /*
     ****************************************************************************
     * doExecute()
     ****************************************************************************
     */ /**
     *
     */
     public abstract ActionForward doExecute(
         ActionMapping oMapping, ActionForm oActionForm,
         HttpServletRequest oRequest, HttpServletResponse oResponse)
         throws Exception;
 
 } // Class: BaseAction
