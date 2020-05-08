 package services;
 
 import exceptions.DriverException;
 import exceptions.WorkflowException;
import java.util.ArrayList;
 import javax.servlet.ServletConfig;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  *  Gatekeeper service.
  *  Allows a direct, simple call to a standardized gatekeeper ressource configured 
  *  through the servlet instanciation parameters.
  *  Only accepts one unique parameter, containing a value to check, and return a
  *  two-column data set, where first column indicates the server response (as 0
  *  for KO, 1 for OK) and second column the violation message key to display in
  *  case of failure.
  *      @author kawa
  */
 public class GatekeeperService extends DirectService {
     /**
      *  Starter.
      *  Initiates the gatekeeper service from the servlet "procedure" parameter.
      *  The syntax of the procedure has to respect the syntax : name.p
      *  Where : - "name" is the SQL procedure syntax ;
      *          - "p" is a single parameter, defined as litteral type (string, 
      *            integer, boolean ...) followed by the "*" character indicating 
      *            a mandatory status.
      *  An optionnal "lifetime" parameter can be set to rewrite the base ressource
      *  lifetime.
      *      @param cfg  Servlet configuration.
      */
     @Override
     protected void start(ServletConfig cfg) {
        ArrayList<String> template = new ArrayList<>();
        template.add("Response_Value_KEY");
        template.add("Response_Violation");
        
         super.start(cfg);
         if (super.getBase().getModelSize() != 1) {
             super.manage(new DriverException(this, "Invalid gatekeeper model size"));
         }
        super.getBase().setTemplate(template);
     }
     /**
      *  Invoker.
      *  Submits the base ressource from a single parameter (named "value") and 
      *  returns the resulting XML image.
      *      @param request  Servlet request.   
      */
     @Override
     protected String invoke(HttpServletRequest request) throws DriverException, WorkflowException {
         String params[] = new String[1];
         params[0] = request.getParameter("value");
         return super.getBase().submit(params);
     }
     /**
      *  @return Driver name.
      */
     @Override
     public String getDriverName() {
         if (super.getBase() == null) {
             return "GatekeeperService[Unstarted]";
         }
         return "GatekeeperService[" + this.getBase().getDriverName() + "]";
     }
 }
