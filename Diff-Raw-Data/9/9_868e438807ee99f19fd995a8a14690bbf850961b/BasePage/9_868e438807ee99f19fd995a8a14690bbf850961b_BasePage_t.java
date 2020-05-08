 package war.webapp.action;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import javax.faces.context.FacesContext;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.beanutils.BeanComparator;
 import org.apache.commons.collections.comparators.NullComparator;
 import org.apache.commons.collections.comparators.ReverseComparator;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.mail.SimpleMailMessage;
 
import org.springframework.security.context.SecurityContextHolder;
 import war.webapp.Constants;
import war.webapp.model.User;
 import war.webapp.service.MailEngine;
 import war.webapp.service.UserManager;
 
 public class BasePage {
     protected final Log log = LogFactory.getLog(getClass());
     protected UserManager userManager;
     protected MailEngine mailEngine;
     protected SimpleMailMessage message;
     protected String templateName;
     protected String sortColumn;
     protected boolean ascending;
     protected boolean nullsAreHigh;
 
     public FacesContext getFacesContext() {
         return FacesContext.getCurrentInstance();
     }
 
     public void setUserManager(UserManager userManager) {
         this.userManager = userManager;
     }
 
     // Convenience methods ====================================================
     public String getParameter(String name) {
         return getRequest().getParameter(name);
     }
 
     public Map<String, String> getCountries() {
         CountryModel model = new CountryModel();
         return model.getCountries(getRequest().getLocale());
     }
 
     public String getBundleName() {
         return getFacesContext().getApplication().getMessageBundle();
     }
 
     public ResourceBundle getBundle() {
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         return ResourceBundle.getBundle(getBundleName(), getRequest().getLocale(), classLoader);
     }
 
     public ResourceBundle getBundle(Locale locale) {
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         return ResourceBundle.getBundle(getBundleName(), locale, classLoader);
     }
     
     public String getText(String key) {
         String message;
 
         try {
             message = getBundle().getString(key);
         } catch (java.util.MissingResourceException mre) {
             log.warn("Missing key for '" + key + "'");
             return "???" + key + "???";
         }
 
         return message;
     }
 
     public String getText(String key, Object arg) {
         if (arg == null) {
             return getText(key);
         }
 
         MessageFormat form = new MessageFormat(getBundle().getString(key));
 
         if (arg instanceof String) {
             return form.format(new Object[] { arg });
         } else if (arg instanceof Object[]) {
             return form.format(arg);
         } else {
             log.error("arg '" + arg + "' not String or Object[]");
 
             return "";
         }
     }
 
     @SuppressWarnings("unchecked")
     protected void addMessage(String key, Object arg) {
         // JSF Success Messages won't live past a redirect, so it's not used
         // FacesUtils.addInfoMessage(formatMessage(key, arg));
         List<String> messages = (List<String>) getSession().getAttribute("messages");
 
         if (messages == null) {
             messages = new ArrayList<String>();
         }
 
         messages.add(getText(key, arg));
         getSession().setAttribute("messages", messages);
     }
 
     protected void addMessage(String key) {
         addMessage(key, null);
     }
 
     @SuppressWarnings("unchecked")
     protected void addError(String key, Object arg) {
         // The "JSF Way" doesn't allow you to put HTML in your error messages,
         // so I don't use it.
         // FacesUtils.addErrorMessage(formatMessage(key, arg));
         List<String> errors = (List<String>) getSession().getAttribute("errors");
 
         if (errors == null) {
             errors = new ArrayList<String>();
         }
 
         // if key contains a space, don't look it up, it's likely a raw message
         if (key.contains(" ") && arg == null) {
             errors.add(key);
         } else {
             errors.add(getText(key, arg));
         }
 
         getSession().setAttribute("errors", errors);
     }
 
     protected void addError(String key) {
         addError(key, null);
     }
 
     /**
      * Convenience method for unit tests.
      * 
      * @return boolean indicator of an "errors" attribute in the session
      */
     public boolean hasErrors() {
         return (getSession().getAttribute("errors") != null);
     }
 
     /**
      * Servlet API Convenience method
      * 
      * @return HttpServletRequest from the FacesContext
      */
     protected HttpServletRequest getRequest() {
         return (HttpServletRequest) getFacesContext().getExternalContext().getRequest();
     }
 
     /**
      * Servlet API Convenience method
      * 
      * @return the current user's session
      */
     protected HttpSession getSession() {
         return getRequest().getSession();
     }
 
     /**
      * Servlet API Convenience method
      * 
      * @return HttpServletResponse from the FacesContext
      */
     protected HttpServletResponse getResponse() {
         return (HttpServletResponse) getFacesContext().getExternalContext().getResponse();
     }
 
     protected void setResponse(HttpServletResponse response) {
         getFacesContext().getExternalContext().setResponse(response);
     }
 
     /**
      * Servlet API Convenience method
      * 
      * @return the ServletContext form the FacesContext
      */
     protected ServletContext getServletContext() {
         return (ServletContext) getFacesContext().getExternalContext().getContext();
     }
 
    public String getCurrentUserName() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return currentUser.getFirstName();
    }

     /**
      * Convenience method to get the Configuration HashMap from the servlet
      * context.
      * 
      * @return the user's populated form from the session
      */
     @SuppressWarnings("rawtypes")
     protected Map getConfiguration() {
         Map config = (HashMap) getServletContext().getAttribute(Constants.CONFIG);
 
         // so unit tests don't puke when nothing's been set
         if (config == null) {
             return new HashMap();
         }
 
         return config;
     }
 
     public void setMailEngine(MailEngine mailEngine) {
         this.mailEngine = mailEngine;
     }
 
     public void setMessage(SimpleMailMessage message) {
         this.message = message;
     }
 
     public void setTemplateName(String templateName) {
         this.templateName = templateName;
     }
 
     // The following methods are used by t:dataTable for sorting.
     public String getSortColumn() {
         return sortColumn;
     }
 
     public void setSortColumn(String sortColumn) {
         this.sortColumn = sortColumn;
     }
 
     public boolean isAscending() {
         return ascending;
     }
 
     public void setAscending(boolean ascending) {
         this.ascending = ascending;
     }
 
     /**
      * Sort list according to which column has been clicked on.
      * 
      * @param list the java.util.List to sort
      * @return ordered list
      */
     @SuppressWarnings("unchecked")
     protected <T> List<T> sort(List<T> list) {
         Comparator<Object> comparator = new BeanComparator(sortColumn, new NullComparator(nullsAreHigh));
         if (!ascending) {
             comparator = new ReverseComparator(comparator);
         }
         Collections.sort(list, comparator);
         return list;
     }
 }
