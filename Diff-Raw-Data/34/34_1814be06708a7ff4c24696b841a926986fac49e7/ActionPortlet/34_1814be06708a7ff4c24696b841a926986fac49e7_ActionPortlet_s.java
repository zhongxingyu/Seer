 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id: ActionPortlet.java 5032 2006-08-17 18:15:06Z novotny $
  */
 package org.gridsphere.provider.portlet.jsr;
 
 import org.gridsphere.portlet.*;
 import org.gridsphere.portlet.jsrimpl.ActionRequestImpl;
 import org.gridsphere.portlet.impl.SportletLog;
 import org.gridsphere.portlet.impl.SportletProperties;
 import org.gridsphere.portlet.service.PortletService;
 import org.gridsphere.portlet.service.PortletServiceException;
 import org.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridsphere.provider.event.jsr.ActionFormEvent;
 import org.gridsphere.provider.event.jsr.RenderFormEvent;
 import org.gridsphere.provider.event.jsr.impl.ActionFormEventImpl;
 import org.gridsphere.provider.event.jsr.impl.RenderFormEventImpl;
 import org.gridsphere.provider.event.jsr.FormEvent;
import org.gridsphere.provider.portletui.beans.TableBean;
 import org.gridsphere.provider.portletui.beans.MessageBoxBean;
 import org.gridsphere.provider.portletui.beans.MessageStyle;
 import org.gridsphere.portletcontainer.impl.GridSphereEventImpl;
import org.gridsphere.services.core.persistence.QueryFilter;
 
 import javax.portlet.*;
 import javax.portlet.PortletConfig;
 import javax.portlet.PortletException;
 import javax.portlet.PortletRequest;
 import javax.portlet.PortletResponse;
 import javax.servlet.http.HttpServletRequestWrapper;
 import java.io.IOException;
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.*;
 
 /**
  * An <code>ActionPortlet</code> provides an abstraction on top of
  * <code>AbstractPortlet</code> to develop portlets under the action provider model.
  */
 public class ActionPortlet extends GenericPortlet {
 
     public static PortletLog log = SportletLog.getInstance(ActionPortlet.class);
 
     // Default error page
     protected String ERROR_PAGE = "doError";
 
     // Default VIEW mode
     protected String DEFAULT_VIEW_PAGE = "view.jsp";
 
     // Default EDIT mode
     protected String DEFAULT_EDIT_PAGE = "edit.jsp";
 
     // Default HELP mode
     protected String DEFAULT_HELP_PAGE = "help.jsp";
 
     // Default CONFIGURE mode
     protected String DEFAULT_CONFIGURE_PAGE = "configure.jsp";
 
     public void init(PortletConfig config) throws PortletException {
         super.init(config);
     }
 
     protected void setFileDownloadEvent(PortletRequest req, String fileName, String path) {
         setFileDownloadEvent(req, fileName, path, false);
     }
 
     protected void setFileDownloadEvent(PortletRequest req, String fileName, String path, boolean deleteFile) {
         req.setAttribute(SportletProperties.FILE_DOWNLOAD_NAME, fileName);
         req.setAttribute(SportletProperties.FILE_DOWNLOAD_PATH, path);
         req.setAttribute(SportletProperties.FILE_DELETE, Boolean.valueOf(deleteFile));
     }
 
     protected void setFileDownloadEvent(PortletRequest req, File file) {
         req.setAttribute(SportletProperties.FILE_DOWNLOAD_NAME, file.getName());
         req.setAttribute(SportletProperties.FILE_DOWNLOAD_BINARY, file);
     }
 
     /**
      * Sets the next display state. The state specified may be either a JSP or it can
      * be another method name to invoke.
      *
      * @param request the <code>Portletrequest</code>
      * @param state   the next display state
      */
     protected void setNextState(PortletRequest request, String state) {
         String id = getUniqueId();
         // JN request.setAttribute(id + ".state", state);
         request.getPortletSession(true).setAttribute(id + ".state", state);
         //log.debug("in ActionPortlet in setNextState: setting state to " + state);
     }
 
     /**
      * Returns the next display state. The state specified may be either a JSP or it can
      * be another method name to invoke.
      *
      * @param request the <code>PortletRequest</code>
      * @return the next display state, either the JSP to include or a method to invoke
      */
     protected String getNextState(PortletRequest request) {
         String id = getUniqueId();
         return (String)request.getPortletSession(true).getAttribute(id+".state");
     }
 
     protected void removeNextState(PortletRequest request) {
         String id = getUniqueId();
         // JN String state = (String)request.getAttribute(id+".state");
         request.getPortletSession(true).removeAttribute(id+".state");
     }
 
     /**
      * Returns the title to display in the portlet
      * Doesn't work since title rendering occurs before title is set!
      *
      * @param request the <code>PortletRequest</code>
      * @return the title to display in the portlet
      */
     public String getNextTitle(PortletRequest request) {
         String id = getUniqueId();
         //log.debug("ActionPortlet in getNextTitle: setting title attribute " + id + ".title");
         // JN String title = (String) request.getAttribute(id + ".title");
         String title = (String) request.getPortletSession(true).getAttribute(id + ".title");
         if (title == null) {
             Locale locale = request.getLocale();
             ResourceBundle rb = this.getPortletConfig().getResourceBundle(locale);
             title = rb.getString("java.portlet.title");
             log.debug("Printing default title: " + title);
         }
         log.debug("next title= " + title);
         return title;
     }
 
     /**
      * Sets the title to display in the portlet
      * Doesn't work since title rendering occurs before title is set!
      *
      * @param request the <code>PortletRequest</code>
      * @param title   the title display in the portlet
      */
     public void setNextTitle(PortletRequest request, String title) {
         log.debug("Setting title to " + title);
         String id = getUniqueId();
         //System.err.println("in setNextT: in attribute " + id + ".title");
         request.setAttribute(id + ".title", title);
     }
 
     public void removeNextTitle(PortletRequest request) {
         String id = getUniqueId();
         request.removeAttribute(id + ".title");
     }
 
     /**
      * Sets the tag beans obtained from the FormEvent. Used internally and should not
      * normally need to be invoked by portlet developers.
      *
      * @param request  the <code>PortletRequest</code>
      * @param tagBeans a <code>Map</code> containing the portlet UI visual beans
      */
     protected void setTagBeans(PortletRequest request, Map tagBeans) {
         String id = getUniqueId();
         // JN request.setAttribute(id + ".form", tagBeans);
         log.debug("saving tag beans in session " + id + ".beans");
         request.getPortletSession(true).setAttribute(id + ".beans", tagBeans);
     }
 
     protected void removeTagBeans(PortletRequest request) {
         String id = getUniqueId();
         // JN request.setAttribute(id + ".form", tagBeans);
         log.debug("removing tag beans from session " + id + ".beans");
         request.getPortletSession(true).removeAttribute(id + ".beans");
     }
 
     /**
      * Returns the tag beans obtained from the FormEvent. Used internally and should not
      * normally need to be invoked by portlet developers.
      *
      * @param request the <code>PortletRequest</code>
      * @return the visual beans
      */
     protected Map getTagBeans(PortletRequest request) {
         String id = getUniqueId();
         //JN Map tagBeans = (Map) request.getAttribute(id + ".form");
         log.debug("getting tag beans from session " + id + ".beans");
         return (Map) request.getPortletSession(true).getAttribute(id + ".beans");
     }
 
     /**
      * Uses the action name obtained from the <code>ActionEvent</code> to invoke the
      * appropriate portlet action method.
      *
      * @param actionRequest  the <code>ActionRequest</code>
      * @param actionResponse the <code>ActionResponse</code>
      * @throws PortletException if a portlet exception occurs
      */
     public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException {
         log.debug("in ActionPortlet: processAction\t\t\t");
 
         // if cid is null (true in non-GS portlet container) then use the portlet name
         String cid = (String)actionRequest.getAttribute(SportletProperties.COMPONENT_ID);
         if (cid == null) actionRequest.setAttribute(SportletProperties.COMPONENT_ID, getUniqueId());
 
         DefaultPortletAction action = (DefaultPortletAction) actionRequest.getAttribute(SportletProperties.ACTION_EVENT);
         // In non-GS container this will need to be created
         if (!(actionRequest instanceof ActionRequestImpl))  {
             action = GridSphereEventImpl.createAction(actionRequest);
             //System.err.println("action name" + action.getName());
         }
         ActionFormEvent formEvent = new ActionFormEventImpl(action, actionRequest, actionResponse);
 
         Class[] parameterTypes = new Class[]{ActionFormEvent.class};
         Object[] arguments = new Object[]{formEvent};
 
         String methodName = formEvent.getAction().getName();
 
         doAction(actionRequest, actionResponse, methodName, parameterTypes, arguments);
         //System.err.println("in processAction: befoire store cid=" + actionRequest.getAttribute(SportletProperties.COMPONENT_ID));
 
         setTagBeans(actionRequest, formEvent.getTagBeans());
     }
 
     /**
      * Invokes the appropriate portlet action method based on the portlet action received
      *
      * @param request        the portlet request
      * @param response       the portlet response
      * @param methodName     the method name to invoke
      * @param parameterTypes the method parameters
      * @param arguments      the method arguments
      * @throws PortletException if an error occurs during the method invocation
      */
     protected void doAction(PortletRequest request, PortletResponse response,
                             String methodName,
                             Class[] parameterTypes,
                             Object[] arguments) throws PortletException {
 
         // reset next state
         removeNextState(request);
 
         // Get object and class references
         Class thisClass = this.getClass();
         // Call method specified by action name
         try {
             Method method = thisClass.getMethod(methodName, parameterTypes);
             method.invoke(this, arguments);
             StringBuffer sb = new StringBuffer();
             sb.append("Invoking portlet action ").append(thisClass.getName()).append("#").append(methodName);
             if (request.getUserPrincipal() != null) {
                 sb.append(" user=").append(request.getUserPrincipal().getName());
                 sb.append(" session id=").append(request.getPortletSession().getId());
             }
             if (request instanceof HttpServletRequestWrapper) {
                 sb.append(" remote ip=").append(((HttpServletRequestWrapper)request).getRemoteAddr());
                 sb.append(" user agent=").append(((HttpServletRequestWrapper)request).getHeader("user-agent"));
             }
             log.info(sb.toString());
         } catch (NoSuchMethodException e) {
             String error = "No such method: " + methodName + "\n" + e.getMessage();
            log.error(error, e);
             // If action is not illegal do error undefined action
             //doErrorInvalidAction(request, error);
            throw new PortletException(e);
         } catch (IllegalAccessException e) {
             String error = "Error accessing action method: " + methodName + "\n" + e.getMessage();
            log.error(error, e);
             // If action is not illegal do error undefined action
             //doErrorInvalidAction(request, error);
            throw new PortletException(e);
         } catch (InvocationTargetException e) {
             String error = "Error invoking action method: " + methodName;
            log.error(error, e);
 
             // JN request.setAttribute(SportletProperties.PORTLETERROR + request.getAttribute(SportletProperties.PORTLETID), e.getTargetException());
             request.getPortletSession(true).setAttribute(SportletProperties.PORTLETERROR + request.getAttribute(SportletProperties.PORTLETID), e.getTargetException());
 
             // If action is not illegal do error undefined action
             //doErrorInvalidAction(request, error);
            throw new PortletException(e.getTargetException());
         }
 
     }
 
     /**
      * Renders the supplied JSP page.
      *
      * @param request  the portlet request
      * @param response the portlet response
      * @param jsp      the JSP page to include
      */
     public void doViewJSP(RenderRequest request, RenderResponse response, String jsp) throws PortletException {
         log.debug("Including JSP page:" + jsp);
         response.setContentType("text/html; charset=utf-8");
         try {
             if (jsp.startsWith("/")) {
                 getPortletConfig().getPortletContext().getRequestDispatcher(jsp).include(request, response);
             } else {
                 getPortletConfig().getPortletContext().getRequestDispatcher("/jsp/" + jsp).include(request, response);
             }
         } catch (Exception e) {
             // JN request.setAttribute(SportletProperties.PORTLETERROR + request.getAttribute(SportletProperties.PORTLETID), e);
             request.getPortletSession(true).setAttribute(SportletProperties.PORTLETERROR + request.getAttribute(SportletProperties.PORTLETID), e);
 
             //log.error("Unable to include resource : " + e.getMessage());
             //setNextError(request, "Unable to include resource " + jsp);
             throw new PortletException(e);
         }
 
     }
 
     /**
      * Uses #getNextState to either render a JSP or invoke the specified render action method
      *
      * @param request  the portlet request
      * @param response the portlet response
      * @throws PortletException    if a portlet exception occurs
      * @throws java.io.IOException if an I/O error occurs
      */
     public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
         String next = getNextState(request);
         if (next == null) {
             log.debug("in ActionPortlet: state is null-- setting to DEFAULT_VIEW_PAGE");
             setNextState(request, DEFAULT_VIEW_PAGE);
         }
         doMode(request, response);
     }
 
     protected void doMode(RenderRequest request, RenderResponse response) throws PortletException, IOException {
         String next = getNextState(request);
         log.debug("in ActionPortlet: portlet id= " + getUniqueId() + " doView next page is= " + next);
 
         // if cid is null (true in non-GS portlet container) then use the portlet name
         String cid = (String)request.getAttribute(SportletProperties.COMPONENT_ID);
         if (cid == null) request.setAttribute(SportletProperties.COMPONENT_ID, getUniqueId());
 
         if (next.endsWith(".jsp")) {
             // this is necessary in case beans were modified in action method and set next state is a JSP to render which needs the beans
             Map tagBeans = getTagBeans(request);
             RenderFormEvent formEvent = new RenderFormEventImpl(request, response, tagBeans);
             formEvent.store();
             doViewJSP(request, response, next);
         } else {
             Map tagBeans = getTagBeans(request);
             RenderFormEvent formEvent = new RenderFormEventImpl(request, response, tagBeans);
             Class[] paramTypes = new Class[]{RenderFormEvent.class};
             Object[] arguments = new Object[]{formEvent};
 
             doAction(request, response, next, paramTypes, arguments);
 
             //System.err.println("in doMode: befoire store cid=" + request.getAttribute(SportletProperties.COMPONENT_ID));
             formEvent.store();
             next = getNextState(request);
             if ((next != null) && (next.endsWith(".jsp"))) {
                 log.debug("in doView: next page is= " + next);
                 doViewJSP(request, response, next);
                /*
                 if (hasError(request)) {
                     PrintWriter out = response.getWriter();
                     log.debug("hasError = true");
                     String message = getNextError(request);
                     out.println(message);
                 }
                 */
             }
         }
         removeTagBeans(request);
         removeNextTitle(request);
         removeNextState(request);
     }
 
     protected void doDispatch(RenderRequest request,
                               RenderResponse response) throws PortletException, java.io.IOException {
 
         // if cid is null (true in non-GS portlet container) then use the portlet name
         String cid = (String)request.getAttribute(SportletProperties.COMPONENT_ID);
         if (cid == null) request.setAttribute(SportletProperties.COMPONENT_ID, getUniqueId());
 
         WindowState state = request.getWindowState();
         try {
             super.doDispatch(request, response);
         } catch (PortletException e) {
             if (!state.equals(WindowState.MINIMIZED)) {
                 PortletMode mode = request.getPortletMode();
                 if (mode.toString().equalsIgnoreCase("CONFIG")) {
                     doConfigure(request, response);
                     return;
                 }
             }
             throw e;
         }
 
     }
 
     /**
      * Simply forwards to #doView
      *
      * @param request  the portlet request
      * @param response the portlet response
      * @throws PortletException    if a portlet exception occurs
      * @throws java.io.IOException if an I/O error occurs
      */
     public void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException {
         log.debug("ActionPortlet: in doEdit");
         String next = getNextState(request);
         if (next == null) {
             log.debug("in ActionPortlet: state is null-- setting to DEFAULT_EDIT_PAGE");
             setNextState(request, DEFAULT_EDIT_PAGE);
         }
         doMode(request, response);
     }
 
     /**
      * Simply forwards to #doView
      *
      * @param request  the portlet request
      * @param response the portlet response
      * @throws PortletException    if a portlet exception occurs
      * @throws java.io.IOException if an I/O error occurs
      */
     public void doConfigure(RenderRequest request, RenderResponse response) throws PortletException, IOException {
         log.debug("ActionPortlet: in doConfigure");
         String next = getNextState(request);
         if (next == null) {
             log.debug("in ActionPortlet: state is null-- setting to DEFAULT_CONFIGURE_PAGE");
             setNextState(request, DEFAULT_CONFIGURE_PAGE);
         }
         doMode(request, response);
     }
 
     /**
      * Simply forwards to #doView
      *
      * @param request  the portlet request
      * @param response the portlet response
      * @throws PortletException    if a portlet exception occurs
      * @throws java.io.IOException if an I/O error occurs
      */
     public void doHelp(RenderRequest request, RenderResponse response) throws PortletException, IOException {
         log.debug("ActionPortlet: in doHelp");
         setNextState(request, DEFAULT_HELP_PAGE);
         doView(request, response);
     }
 
     protected String getLocalizedText(PortletRequest req, String key) {
         Locale locale = req.getLocale();
         ResourceBundle bundle = ResourceBundle.getBundle("Portlet", locale);
         return bundle.getString(key);
     }
 
     public String getParameter(PortletRequest request, String param) {
         String value = request.getParameter(param);
         if (value == null)
             return "";
         else
             return value;
     }
 
     public String getParameter(PortletRequest request, String param, String defaultValue) {
         String value = request.getParameter(param);
         if (value == null)
             return defaultValue;
         else
             return value;
     }
 
     public String[] getParameterValues(PortletRequest request, String param) {
         String values[] = request.getParameterValues(param);
 
         if (values == null)
             return new String[0];
         else
             return values;
     }
 
     public int getParameterAsInt(PortletRequest request, String param) {
         return getParameterAsInt(request, param, 0);
     }
 
     public int getParameterAsInt(PortletRequest request, String param, int defaultValue) {
         String value = request.getParameter(param);
         if (value == null)
             return defaultValue;
         if (value.equals(""))
             return defaultValue;
         try {
             return (new Integer(value)).intValue();
         } catch (Exception e) {
             return defaultValue;
         }
     }
 
     public int[] getParameterValuesAsInt(PortletRequest request, String param) {
         String values[] = request.getParameterValues(param);
         if (values == null) {
             return new int[0];
         } else {
             int objs[] = new int[values.length];
             for (int ii = 0; ii < values.length; ++ii) {
                 String value = values[ii];
                 try {
                     objs[ii] = (new Integer(value)).intValue();
                 } catch (Exception e) {
                     objs[ii] = 0;
                 }
             }
             return objs;
         }
     }
 
     public long getParameterAsLng(PortletRequest request, String param) {
         return getParameterAsLng(request, param, 0);
     }
 
     public long getParameterAsLng(PortletRequest request, String param, long defaultValue) {
         String value = request.getParameter(param);
         if (value == null)
             return defaultValue;
         if (value.equals(""))
             return defaultValue;
         try {
             return (new Long(value)).longValue();
         } catch (Exception e) {
             return defaultValue;
         }
     }
 
     public long[] getParameterValuesAsLng(PortletRequest request, String param) {
         String values[] = request.getParameterValues(param);
         if (values == null) {
             return new long[0];
         } else {
             long objs[] = new long[values.length];
             for (int ii = 0; ii < values.length; ++ii) {
                 String value = values[ii];
                 try {
                     objs[ii] = (new Long(value)).longValue();
                 } catch (Exception e) {
                     objs[ii] = 0;
                 }
             }
             return objs;
         }
     }
 
     public float getParameterAsFlt(PortletRequest request, String param) {
         return getParameterAsFlt(request, param, (float) 0.0);
     }
 
     public float getParameterAsFlt(PortletRequest request, String param, float defaultValue) {
         String value = request.getParameter(param);
         if (value == null)
             return defaultValue;
         if (value.equals(""))
             return defaultValue;
         try {
             return (new Float(value)).floatValue();
         } catch (Exception e) {
             return defaultValue;
         }
     }
 
     public float[] getParameterValuesAsFlt(PortletRequest request, String param) {
         String values[] = request.getParameterValues(param);
         if (values == null) {
             return new float[0];
         } else {
             float objs[] = new float[values.length];
             for (int ii = 0; ii < values.length; ++ii) {
                 String value = values[ii];
                 try {
                     objs[ii] = (new Float(value)).floatValue();
                 } catch (Exception e) {
                     objs[ii] = 0;
                 }
             }
             return objs;
         }
     }
 
     public double getParameterAsDbl(PortletRequest request, String param) {
         return getParameterAsDbl(request, param, 0.0);
     }
 
     public double getParameterAsDbl(PortletRequest request, String param, double defaultValue) {
         String value = request.getParameter(param);
         if (value == null)
             return defaultValue;
         if (value.equals(""))
             return defaultValue;
         try {
             return (new Double(value)).doubleValue();
         } catch (Exception e) {
             return defaultValue;
         }
     }
 
 
     public double[] getParameterValuesAsDbl(PortletRequest request, String param) {
         String values[] = request.getParameterValues(param);
         if (values == null) {
             return new double[0];
         } else {
             double objs[] = new double[values.length];
             for (int ii = 0; ii < values.length; ++ii) {
                 String value = values[ii];
                 try {
                     objs[ii] = (new Double(value)).doubleValue();
                 } catch (Exception e) {
                     objs[ii] = 0;
                 }
             }
             return objs;
         }
     }
 
     public boolean getParameterAsBool(PortletRequest request, String param) {
         String value = request.getParameter(param);
         if (value == null) return false;
         if (value.equals("")) return false;
         if (value.equals("true")) return true;
         return false;
     }
 
     public boolean[] getParameterValuesAsBool(PortletRequest request, String param) {
         String values[] = request.getParameterValues(param);
         if (values == null) {
             return new boolean[0];
         } else {
             boolean objs[] = new boolean[values.length];
             for (int ii = 0; ii < values.length; ++ii) {
                 String value = values[ii];
                 if (value.equals("")) objs[ii] = false;
                 if (value.equals("true")) objs[ii] = true;
                 objs[ii] = false;
             }
             return objs;
         }
     }
 
     public Integer getParameterAsInteger(PortletRequest request, String param) {
         String value = request.getParameter(param);
         if (value == null)
             return (new Integer(0));
         if (value.equals("")) value = "0";
         try {
             return (new Integer(value));
         } catch (Exception e) {
             return (new Integer(0));
         }
     }
 
     public Integer[] getParameterValuesAsInteger(PortletRequest request, String param) {
         String values[] = request.getParameterValues(param);
         if (values == null) {
             return new Integer[0];
         } else {
             Integer objs[] = new Integer[values.length];
             for (int ii = 0; ii < values.length; ++ii) {
                 String value = values[ii];
                 try {
                     objs[ii] = (new Integer(value));
                 } catch (Exception e) {
                     objs[ii] = (new Integer(0));
                 }
             }
             return objs;
         }
     }
 
     public Float getParameterAsFloat(PortletRequest request, String param) {
         String value = request.getParameter(param);
         if (value == null)
             return (new Float(0.0));
         if (value.equals("")) value = "0";
         try {
             return (new Float(value));
         } catch (Exception e) {
             return (new Float(0.0));
         }
     }
 
     public Float[] getParameterValuesAsFloat(PortletRequest request, String param) {
         String values[] = request.getParameterValues(param);
         if (values == null) {
             return new Float[0];
         } else {
             Float objs[] = new Float[values.length];
             for (int ii = 0; ii < values.length; ++ii) {
                 String value = values[ii];
                 try {
                     objs[ii] = (new Float(value));
                 } catch (Exception e) {
                     objs[ii] = (new Float(0.0));
                 }
             }
             return objs;
         }
     }
 
     public Double getParameterAsDouble(PortletRequest request, String param) {
         String value = request.getParameter(param);
         if (value == null)
             return (new Double(0.0));
         if (value.equals("")) value = "0";
         try {
             return (new Double(value));
         } catch (Exception e) {
             return (new Double(0.0));
         }
     }
 
     public Double[] getParameterValuesAsDouble(PortletRequest request, String param) {
         String values[] = request.getParameterValues(param);
         if (values == null) {
             return new Double[0];
         } else {
             Double objs[] = new Double[values.length];
             for (int ii = 0; ii < values.length; ++ii) {
                 String value = values[ii];
                 try {
                     objs[ii] = (new Double(value));
                 } catch (Exception e) {
                     objs[ii] = (new Double(0.0));
                 }
             }
             return objs;
         }
     }
 
     public Boolean getParameterAsBoolean(PortletRequest request, String param) {
         String value = request.getParameter(param);
         if (value == null) return Boolean.FALSE;
         if (value.equals("")) return Boolean.FALSE;
         if (value.equals("true")) return Boolean.TRUE;
         return Boolean.FALSE;
     }
 
     public Boolean[] getParameterValuesAsBoolean(PortletRequest request, String param) {
         String values[] = request.getParameterValues(param);
         if (values == null) {
             return new Boolean[0];
         } else {
             Boolean objs[] = new Boolean[values.length];
             for (int ii = 0; ii < values.length; ++ii) {
                 String value = values[ii];
                 if (value.equals("")) objs[ii] = Boolean.FALSE;
                 if (value.equals("true")) objs[ii] = Boolean.TRUE;
                 objs[ii] = Boolean.FALSE;
             }
             return objs;
         }
     }
 
     public List getParameterValuesAsList(PortletRequest request, String param) {
         // Create list for values
         List listValues = new Vector();
         // Get parameter values
         String paramValues[] = getParameterValues(request, param);
         for (int ii = 0; ii < paramValues.length; ++ii) {
             listValues.add(paramValues[ii]);
         }
         return listValues;
     }
 
     public Map getParameterValuesAsMap(PortletRequest request, String param) {
         // Create list for values
         HashMap mapValues = new HashMap();
         // Get parameter values
         String paramValues[] = getParameterValues(request, param);
         for (int ii = 0; ii < paramValues.length; ++ii) {
             String paramValue = paramValues[ii];
             int index = paramValue.indexOf(":");
             if (index < 0) {
                 continue;
             }
             String key = paramValue.substring(0, index);
             String value = paramValue.substring(index + 1, paramValue.length());
             mapValues.put(key, value);
         }
         return mapValues;
     }
 
     protected String getUniqueId() {
         //log.debug("setting unique cid: " + this.getPortletConfig().getPortletName());
         return this.getPortletConfig().getPortletName();
     }
 
     public PortletService createPortletService(Class serviceClass) throws PortletServiceException {
         return PortletServiceFactory.createPortletService(serviceClass, true);
     }
 
     protected void createErrorMessage(FormEvent evt, String text) {
         MessageBoxBean msgBox = evt.getMessageBoxBean("msg");
         msgBox.setMessageType(MessageStyle.MSG_ERROR);
         String msgOld = msgBox.getValue();
         msgBox.setValue((msgOld != null ? msgOld : "") + "\n" + text);
     }
 
     protected void createSuccessMessage(FormEvent evt, String text) {
         MessageBoxBean msg = evt.getMessageBoxBean("msg");
         msg.setValue(text);
         msg.setMessageType(MessageStyle.MSG_SUCCESS);
     }
 
 }
