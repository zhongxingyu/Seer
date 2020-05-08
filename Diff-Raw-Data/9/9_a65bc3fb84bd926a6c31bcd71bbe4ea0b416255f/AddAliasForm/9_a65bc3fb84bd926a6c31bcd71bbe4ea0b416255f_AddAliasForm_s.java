 package jamm.webapp;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 
 public class AddAliasForm extends ActionForm
 {
     public AddAliasForm()
     {
         mAddresses = new ArrayList();
     }
 
     public void setDomain(String domain)
     {
         mDomain = domain;
     }
 
     public String getDomain()
     {
         return mDomain;
     }
     
     public void setName(String name)
     {
         mName = name;
     }
 
     public String getName()
     {
         return mName;
     }
 
     public void setDestinations(String destinations)
     {
         mDestinations = destinations;
         StringTokenizer tokenizer = new StringTokenizer(mDestinations,
                                                         " \t\n\r\f,");
         mAddresses.clear();
         while (tokenizer.hasMoreTokens())
         {
             mAddresses.add(tokenizer.nextToken());
         }
     }
 
     public String getDestinations()
     {
         return mDestinations;
     }
     
     public List getDestinationAddresses()
     {
         return mAddresses;
     }
 
     public void setPassword(String password)
     {
         mPassword = password;
     }
 
     public String getPassword()
     {
         return mPassword;
     }
 
     public void setRetypedPassword(String retypedPassword)
     {
         mRetypedPassword = retypedPassword;
     }
 
     public String getRetypedPassword()
     {
         return mRetypedPassword;
     }
 
     /**
      * The password is empty if both passwords are null or the empty
      * string.
      */
     public boolean isPasswordEmpty()
     {
         return (((mPassword == null) && (mRetypedPassword == null)) ||
                 ((mPassword.equals("") && mRetypedPassword.equals(""))));
     }
 
     public void reset(ActionMapping mapping, HttpServletRequest request)
     {
         mDomain = request.getParameter("domain");
         mName = null;
         mDestinations = null;
         mAddresses.clear();
         clearPasswords();
     }
 
     /**
      * Validates the destination addresses and password.
      *
      * @param mapping The action mapping.
      * @param request The servlet request.
      * @return Any errors.
      */
     public ActionErrors validate(ActionMapping mapping,
                                  HttpServletRequest request)
     {
         ActionErrors errors = new ActionErrors();
 
         if (mAddresses.size() == 0)
         {
             errors.add("destinations",
                       new ActionError("add_alias.error.non_zero_aliases"));
         }
 
         if (!isPasswordEmpty())
         {
             if (!PasswordValidator.validatePassword(mPassword,
                                                     mRetypedPassword, errors))
             {
                 clearPasswords();
             }
         }
 
         return errors;
     }
 
     private void clearPasswords()
     {
         mPassword = null;
         mRetypedPassword = null;
     }
     
     private String mDomain;
     private String mName;
     private String mDestinations;
     private List mAddresses;
     private String mPassword;
     private String mRetypedPassword;
 }
