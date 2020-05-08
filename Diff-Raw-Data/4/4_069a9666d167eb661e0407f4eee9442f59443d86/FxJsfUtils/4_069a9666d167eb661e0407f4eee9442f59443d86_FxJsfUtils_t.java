 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.faces;
 
 import com.flexive.faces.beans.MessageBean;
 import com.flexive.faces.components.Thumbnail;
 import com.flexive.faces.messages.FxFacesMessage;
 import com.flexive.faces.messages.FxFacesMessages;
 import com.flexive.faces.model.FxJSFSelectItem;
 import com.flexive.shared.*;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.scripting.FxScriptInfo;
 import com.flexive.shared.search.FxPaths;
 import com.flexive.shared.search.FxResultSet;
 import com.flexive.shared.security.Account;
 import com.flexive.shared.structure.FxSelectList;
 import com.flexive.shared.structure.FxSelectListItem;
 import com.flexive.shared.value.BinaryDescriptor;
 import com.flexive.shared.value.FxBinary;
 import com.flexive.shared.value.FxBoolean;
 import com.flexive.shared.value.FxValue;
 import com.flexive.shared.value.renderer.FxValueRendererFactory;
 import com.flexive.war.FxRequest;
 import com.flexive.war.filter.FxResponseWrapper;
 import com.flexive.war.servlet.ThumbnailServlet;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.el.ValueExpression;
 import javax.faces.application.Application;
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.model.SelectItem;
 import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.io.Writer;
 import java.text.Collator;
 import java.util.*;
 
 import static javax.faces.context.FacesContext.getCurrentInstance;
 
 
 /**
  * Utility class for JSF functionality within beans.
  *
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class FxJsfUtils {
     private static final Log LOG = LogFactory.getLog(FxJsfUtils.class);
     private static final String JSON_RPC_SERVLET = "/adm/JSON-RPC";
     private final static String BRACKET_OPEN = "__-";
     private final static String BRACKET_CLOSE = "_-_";
     private final static String SLASH = "-__";
 
     /**
      * Encode identifier to be valid for JSF components
      *
      * @param identifier id to encode
      * @return encoded id
      */
     public static String encodeJSFIdentifier(String identifier) {
         // We may only use '-' and '_' characters to encode JSF identifiers
         identifier = identifier.replaceAll("/", SLASH);
         identifier = identifier.replaceAll("\\[", BRACKET_OPEN);
         identifier = identifier.replaceAll("]", BRACKET_CLOSE);
         return identifier;
     }
 
     /**
      * Decode a JSF identifier created by encodeJSFIdentifier
      *
      * @param identifier id to decode
      * @return decoded id
      */
     public static String decodeJSFIdentifier(String identifier) {
         return identifier.replaceAll(BRACKET_CLOSE, "]").replace(SLASH, "/").replace(BRACKET_OPEN, "[");
     }
 
     /**
      * Renders the value as returned from a flexive search query to the
      * given output writer.
      *
      * @param out   the output writer
      * @param value the value to be formatted
      * @param linkFormatter link formatter
      * @param linkFormat link format
      * @param itemLinkFormat item link format
      * @throws java.io.IOException if the value could not be written
      * @deprecated  use {@link #formatResultValue(String, Object, com.flexive.shared.ContentLinkFormatter, String, String)}
      */
     @Deprecated
     public static void writeResultValue(Writer out, Object value, ContentLinkFormatter linkFormatter, String linkFormat, String itemLinkFormat) throws IOException {
         out.write(formatResultValue(null, value, linkFormatter, linkFormat, itemLinkFormat));
     }
 
     /**
      * Formats the value as returned from a flexive search query.
      *
      * @param value the value to be formatted
      * @param linkFormatter link formatter
      * @param linkFormat link format
      * @param itemLinkFormat item link format
      * @return the formatted string value
      * @deprecated  use {@link #formatResultValue(String, Object, com.flexive.shared.ContentLinkFormatter, String, String)}
      */
     public static String formatResultValue(Object value, ContentLinkFormatter linkFormatter, String linkFormat, String itemLinkFormat) {
         return formatResultValue(null, value, linkFormatter, linkFormat, itemLinkFormat);
     }
 
     /**
      * Formats the value as returned from a flexive search query.
      *
      * @param propertyName the (optional) property name for the given value
      * @param value the value to be formatted
      * @param linkFormatter link formatter
      * @param linkFormat link format
      * @param itemLinkFormat item link format
      * @return the formatted string value
      * @since 3.1
      */
     public static String formatResultValue(String propertyName, Object value, ContentLinkFormatter linkFormatter, String linkFormat, String itemLinkFormat) {
         linkFormatter = linkFormatter != null ? linkFormatter : ContentLinkFormatter.getInstance();
         if (value == null || (value instanceof FxValue && ((FxValue) value).isEmpty())) {
             return "<i>" + FxSharedUtils.getEmptyResultMessage() + "</i>";
         } else if (value instanceof FxBinary) {
             final FxBinary binary = (FxBinary) value;
             return "<img src=\"" + FxContext.get().getContextPath() +
                     ThumbnailServlet.getLink(XPathElement.getPK(binary.getXPath()),
                             BinaryDescriptor.PreviewSizes.PREVIEW2,
                             binary.getXPath(),
                             binary.getBestTranslation().getCreationTime(),
                             FxContext.getUserTicket().getLanguage())
                     + "\" alt=\""
                     + FxFormatUtils.escapeForJavaScript(binary.getBestTranslation().getName())
                     + "\" class=\"" + Thumbnail.CSS_CLASS + "\"/>";
         } else if (value instanceof FxBoolean) {
             return MessageBean.getInstance().getMessage(
                     "shared.result.value." + ((FxBoolean) value).getBestTranslation()
             );
         } else if (value instanceof FxValue) {
             return FxValueRendererFactory.getInstance().format((FxValue) value);
         } else if (value instanceof FxPK) {
             return linkFormatter.format(linkFormat, (FxPK) value);
         } else if (value instanceof FxPaths) {
             return linkFormatter.format(itemLinkFormat, (FxPaths) value);
         } else if (value instanceof FxResultSet.WrappedLock) {
             final FxResultSet.WrappedLock wrapped = (FxResultSet.WrappedLock) value;
             return FxSharedUtils.getMessage(
                     FxSharedUtils.SHARED_BUNDLE,
                     "shared.result.lockInfo.user",
                     wrapped.getUsername(),
                     wrapped.getLock().getLockType().getLabel()).toString();
         } else {
             return value.toString(); // unsupported type
         }
     }
 
     private static String resolveUserId(long accountId) {
         final String contextKey = FxJsfUtils.class.getName() + "_accounts";
 
         // fetch all accounts, cache in context scope
         if (FxContext.get().getAttribute(contextKey) == null) {
             try {
                 FxContext.get().setAttribute(contextKey, EJBLookup.getAccountEngine().loadAll());
             } catch (FxApplicationException e) {
                 throw e.asRuntimeException();
             }
         }
 
         @SuppressWarnings({"unchecked"}) final List<Account> accounts =
                 (List<Account>) FxContext.get().getAttribute(contextKey);
         for (Account account : accounts) {
             if (account.getId() == accountId) {
                 return account.getName();
             }
         }
         // could not resolve user ID - should not happen
         return String.valueOf(accountId);
     }
 
     /**
      * Private constructor to avoid instantiation
      */
     private FxJsfUtils() {
     }
 
     /**
      * Get managed beans based on the beans name.
      *
      * @param beanName the beans name
      * @return the managed beans associated with the beans name
      */
     public static Object getManagedBean(String beanName) {
         return getValueExpression(getJsfEl(beanName)).getValue(getCurrentInstance().getELContext());
     }
 
     /**
      * Returns the managed beans of the given class. Works only for flexive beans that
      * have the full class name and a "fx" prefix by convention. For example,
      * the ContentEditorBean is registered with JSF as "fxContentEditorBean".
      *
      * @param beanClass the beans class
      * @return the managed beans of the given class, or null if none exists
      */
     public static <T> T getManagedBean(Class<T> beanClass) {
         return beanClass.cast(getManagedBean("fx" + beanClass.getSimpleName()));
     }
 
     /**
      * Remove the managed beans based on the beans name.
      *
      * @param beanName the beans name of the managed beans to be removed
      */
     public static void resetManagedBean(String beanName) {
         getValueExpression(getJsfEl(beanName)).setValue(getCurrentInstance().getELContext(), null);
     }
 
     /**
      * Replace the managed beans based on the beans name.
      *
      * @param beanName the beans name of the managed beans to be replaced
      * @param bean     the new beans
      */
     public static void replaceManagedBean(String beanName, Object bean) {
         getValueExpression(getJsfEl(beanName)).setValue(getCurrentInstance().getELContext(), bean);
     }
 
     /**
      * Store the managed beans inside the session scope.
      *
      * @param beanName    the name of the managed beans to be stored
      * @param managedBean the managed beans to be stored
      */
     public static void setManagedBeanInSession(String beanName, Object managedBean) {
         //noinspection unchecked
         Map<String, Object> map = getCurrentInstance().getExternalContext().getSessionMap();
         map.put(beanName, managedBean);
     }
 
     /**
      * Get parameter value from request scope.
      *
      * @param name the name of the parameter
      * @return the parameter value
      */
     public static String getRequestParameter(String name) {
         return getCurrentInstance().getExternalContext().getRequestParameterMap().get(name);
     }
 
     /**
      * Evaluate the value of a JSF expression.
      *
      * @param el the JSF expression
      * @return the integer value associated with the JSF expression
      */
     public static Object evalObject(String el) {
         if (el == null) {
             return null;
         }
         return isValueReference(el) ? getElValue(el) : el;
     }
 
     /**
      * <a href="http://issues.apache.org/struts/browse/SHALE-305">Source: SHALE-305.</a><br/>
      * Return true if the specified string contains an EL expression.
      * <p/>
      * This is taken almost verbatim from javax.faces.webapp.UIComponentTag
      * in order to remove JSP dependencies from the renderers.
      *
      * @param value the expression to be checked
      * @return true if the given value contains an EL expression
      */
     public static boolean isValueReference(String value) {
         if (value == null) return false;
 
         int start = value.indexOf("#{");
         if (start < 0) return false;
 
         int end = value.lastIndexOf('}');
         return (end >= 0 && start < end);
     }
 
 
     /**
      * Evaluate the integer value of a JSF expression.
      *
      * @param el the JSF expression
      * @return the integer value associated with the JSF expression
      */
     public static Integer evalInt(String el) {
         if (el == null) {
             return null;
         }
         Object value = evalObject(el);
 
         if (value instanceof Integer) {
             return (Integer) value;
         } else {
             return new Integer(value.toString());
         }
     }
 
     /**
      * Evaluate the string value of a JSF expression.
      *
      * @param el the JSF expression
      * @return the string value associated with the JSF expression
      */
     public static String evalString(String el) {
         if (el == null) {
             return null;
         }
         Object value = evalObject(el);
 
         if (value instanceof String) {
             return (String) value;
         } else {
             return value.toString();
         }
     }
 
     public static Application getApplication() {
         return getCurrentInstance().getApplication();
     }
 
     private static ValueExpression getValueExpression(String el) {
         return getApplication().getExpressionFactory().createValueExpression(getCurrentInstance().getELContext(),
                 el, Object.class);
     }
 
     private static Object getElValue(String el) {
         return getValueExpression(el).getValue(getCurrentInstance().getELContext());
     }
 
     private static String getJsfEl(String value) {
         return "#{" + value + "}";
     }
 
     /**
      * Gets the id of the given parameter name.
      *
      * @param parameterName the parameter name
      * @return the id
      */
     public static long getId(String parameterName) {
         String valueText = null;
         try {
             valueText = FacesContext.getCurrentInstance().
                     getExternalContext().getRequestParameterMap().get(parameterName);
             return Long.parseLong(valueText);
         } catch (NumberFormatException e) {
             throw new IllegalArgumentException("Couldn't parse '" + parameterName + "'='" + valueText + "' as a long");
         }
     }
 
 
     public static void setSessionAttribute(String key, Object value) {
         //noinspection unchecked
         FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(key, value);
     }
 
     public static Object getSessionAttribute(String key) {
         return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(key);
     }
 
     public static void removeSessionAttribute(String key) {
         FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(key);
     }
 
     public static String getParameter(String name) {
         return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(name);
     }
 
     public static long getLongParameter(String name, long defaultValue) {
         return Long.valueOf(StringUtils.defaultString(getParameter(name), String.valueOf(defaultValue)));
     }
 
     public static boolean hasParameter(String name) {
         String s = getParameter(name);
         return s != null && s.length() > 0;
     }
 
     public static long getLongParameter(String name) {
         return Long.valueOf(getParameter(name));
     }
 
     public static int getIntParameter(String name, int defaultValue) {
         return Integer.valueOf(StringUtils.defaultString(getParameter(name), String.valueOf(defaultValue)));
     }
 
     public static int getIntParameter(String name) {
         return Integer.valueOf(getParameter(name));
     }
 
     public static boolean getBooleanParameter(String name, boolean defaultValue) {
         return Boolean.valueOf(StringUtils.defaultString(getParameter(name), String.valueOf(defaultValue)));
     }
 
     public static boolean getBooleanParameter(String name) {
         return Boolean.valueOf(getParameter(name));
     }
 
     /**
      * Returns the servlet context.
      * <p/>
      * The result is null if no faces context or external context is available.
      *
      * @return the servlet context
      */
     public static ServletContext getServletContext() {
         try {
             return (ServletContext) getCurrentInstance().getExternalContext().getContext();
         } catch (Throwable t) {
             return null;
         }
 
     }
 
     /**
      * Gets the session from the faces context.
      * <p/>
      * The result is null if no faces context is available.
      *
      * @return the session from the faces context
      */
     public static HttpSession getSession() {
         try {
             return (HttpSession) getCurrentInstance().getExternalContext().getSession(false);
         } catch (Throwable t) {
             return null;
         }
     }
 
     /**
      * Gets the request from the faces context.
      * <p/>
      * The result is null if no faces context is available.
      *
      * @return the request from the faces context
      */
     public static FxRequest getRequest() {
         try {
             HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper)
                     getCurrentInstance().getExternalContext().getRequest();
             while (wrapper != null && !(wrapper instanceof FxRequest)) {
                 wrapper = (HttpServletRequestWrapper) wrapper.getRequest();
             }
             return (FxRequest) wrapper;
         } catch (Throwable t) {
             return null;
         }
     }
 
     /**
      * Gets the response from the faces context.
      * <p/>
      * The result is null if no faces context is available.
      *
      * @return the response from the faces context
      */
     public static FxResponseWrapper getResponse() {
         HttpServletResponseWrapper resp = (HttpServletResponseWrapper) getCurrentInstance().
                 getExternalContext().getResponse();
         FxResponseWrapper fresp;
         if (resp == null) {
             fresp = null;
         } else if (resp instanceof FxResponseWrapper) {
             fresp = (FxResponseWrapper) resp;
         } else {
             fresp = (FxResponseWrapper) resp.getResponse();
         }
         return fresp;
     }
 
     /**
      * Returns the URI of the JSON/RPC servlet
      *
      * @return the URI of the JSON/RPC servlet
      */
     public static String getJsonServletUri() {
         return getRequest().getContextPath() + JSON_RPC_SERVLET;
     }
 
     /**
      * Get the server URL like "http://www.flexive.org" without the context path
      *
      * @return server URL
      */
     public static String getServerURL() {
         final FxRequest req = FxJsfUtils.getRequest();
         try {
             return req.getRequestURL().substring(0, req.getRequestURL().indexOf(req.getContextPath()));
         } catch (Exception e) {
             final HttpServletRequest r = req.getRequest();
             return r.getProtocol() + "://" + r.getRemoteHost() + (r.getProtocol().startsWith("http")
                     ? ""
                     : ":" + r.getRemotePort());
         }
     }
 
     /**
      * Return the localized message for the given key and replace all parameters with the given args.
      *
      * @param messageKey key of the message to be translated
      * @param args       optional arguments to be replaced in the message
      * @return the localized message
      */
     public static String getLocalizedMessage(String messageKey, Object... args) {
         return MessageBean.getInstance().getMessage(messageKey, args);
     }
 
 
     public static void addMessage(FacesMessage message) {
         getCurrentInstance().addMessage(null, message);
     }
 
     /**
      * Clears all faces messages (optional: for the given client id).
      *
      * @param clientId the client id to work on, or null to remove all messages.
      */
     @SuppressWarnings("unchecked")
     public static void clearAllMessages(String clientId) {
         //noinspection unchecked
         Iterator<FacesMessage> it = (clientId == null || clientId.length() == 0) ?
                 getCurrentInstance().getMessages() : getCurrentInstance().getMessages(clientId);
         while (it.hasNext()) {
             it.next();
             it.remove();
         }
     }
 
     /**
      * Gets all faces messages (optional: for the given client id).
      *
      * @param clientId the client id to work on, or null to remove all messages.
      * @return a array holding all messages
      */
     @SuppressWarnings("unchecked")
     public static ArrayList<FacesMessage> getMessages(String clientId) {
         ArrayList<FacesMessage> result = new ArrayList<FacesMessage>(25);
         try {
             //noinspection unchecked
             Iterator<FacesMessage> it = (clientId == null || clientId.length() == 0) ?
                     getCurrentInstance().getMessages() : getCurrentInstance().getMessages(clientId);
 
             while (it.hasNext()) {
                 result.add(it.next());
             }
         } catch (Throwable t) {
             result.add(new FacesMessage("Failed to build message list: " + t.getMessage()));
         }
         result.trimToSize();
         return result;
     }
 
     /**
      * Gets all faces messages wrapped as FxFacesMessage (which gives access to the clientId)
      *
      * @return a array holding all messages
      */
     @SuppressWarnings("unchecked")
     public static ArrayList<FxFacesMessage> getFxMessages() {
         final FacesContext ctx = getCurrentInstance();
 
         // Get all messages belonging to an specific client id
         Iterator it = ctx.getClientIdsWithMessages();
         ArrayList<FxFacesMessage> result = new ArrayList<FxFacesMessage>(5);
         while (it.hasNext()) {
             String clientId = String.valueOf(it.next());
             //noinspection unchecked
             Iterator<FacesMessage> msgs = ctx.getMessages(clientId);
             while (msgs.hasNext()) {
                 FacesMessage msg = msgs.next();
                 if (!(msg instanceof FxFacesMessage)) {
                     msg = new FxFacesMessage(msg, clientId);
                 }
                 result.add((FxFacesMessage) msg);
             }
         }
 
         // get all messages that are not linked to an client
         it = ctx.getMessages();
         while (it.hasNext()) {
             FacesMessage msg = (FacesMessage) it.next();
             // Add the message, but only if it doesnt exist already with a filled in form/client id
             if (!messageExists(result, msg)) {
                 if (!(msg instanceof FxFacesMessage)) {
                     msg = new FxFacesMessage(msg, null);
                 }
                 result.add((FxFacesMessage) msg);
             }
         }
 
         // Return the result
         return result;
     }
 
     private static boolean messageExists(ArrayList<FxFacesMessage> list, FacesMessage msg) {
         for (FxFacesMessage comp : list) {
             if (comp.getSeverity() == msg.getSeverity() &&
                     comp.getSummary().equals(msg.getSummary()) &&
                     comp.getDetail().equals(msg.getDetail()))
                 return true;
         }
         return false;
     }
 
     /**
      * Gets all faces messages grouped by a equal summary and wrapped as
      * FxFacesMessage (which gives access to the clientId). In case of a grouped message
      * the details of the original FacesMessage can be retrieved by calling getDetails() of
      * the FxFacesMessage.
      *
      * @return a array holding all (grouped) messages
      */
     public static List<FxFacesMessages> getGroupedFxMessages() {
         ArrayList<FxFacesMessage> ffm = getFxMessages();
         Hashtable<String, FxFacesMessages> grouped = new Hashtable<String, FxFacesMessages>(ffm.size());
         for (FxFacesMessage msg : ffm) {
             String key = msg.getSeverity() + ":" + msg.getSummary();
             FxFacesMessages exists = grouped.get(key);
             if (exists != null) {
                 exists.addMessage(msg);
             } else {
                 grouped.put(key, new FxFacesMessages(msg.getSummary(), msg.getSeverity(), msg));
             }
         }
         return new ArrayList<FxFacesMessages>(grouped.values());
     }
 
     /**
      * Find a parent of the given class. Throws a runtime exception if none is found.
      *
      * @param component the (child) component that searches an ancestor
      * @param cls       the class or interface to be searched for in the component's ancestors
      * @return the parent component
      */
     public static <T> T findAncestor(UIComponent component, Class<T> cls) {
         UIComponent current = component;
         if (current != null) {
             current = current.getParent();
         }
         while (current != null && !(cls.isAssignableFrom(current.getClass()))) {
             current = current.getParent();
         }
         if (current == null) {
             throw new FxNotFoundException(LOG, "ex.jsf.ancestor.notFound").asRuntimeException();
         }
         return cls.cast(current);
     }
 
     /**
      * Create a new component of the given type.
      *
      * @param componentType the component type, e.g. <code>javax.faces.SelectOne</code>
      * @return the created component
      */
     public static UIComponent createComponent(String componentType) {
         return getApplication().createComponent(componentType);
     }
 
     /**
      * Add a child component of the given type to the parent component. The child component
      * is returned to the caller.
      *
      * @param parent        the parent component
      * @param componentType child component type, e.g. <code>javax.faces.SelectOne</code>
      * @return the created child component
      * @deprecated          adding the child without its final ID leads to problems with JSF2 events. Use
      * {@link #addChildComponent(javax.faces.component.UIComponent, java.lang.String, java.lang.String)}
      */
     public static UIComponent addChildComponent(UIComponent parent, String componentType) {
         return addChildComponent(parent, null, componentType);
     }
 
     /**
      * Add a child component of the given type to the parent component. The child component
      * is returned to the caller.
      *
      * @param parent    the parent component
      * @param id        the component ID. If null, no ID will be set - in this case,
      * don't set an ID on the component afterwards, otherwise the wrong ID has already been published
      * in JSF2 events.
      * @param componentType child component type, e.g. <code>javax.faces.SelectOne</code>
      * @return the created child component
      * @since               3.1.3
      */
     public static UIComponent addChildComponent(UIComponent parent, String id, String componentType) {
         return addChildComponent(parent, id, componentType, false);
     }
 
     /**
      * Add a child component of the given type to the parent component. The child component
      * is returned to the caller.
      *
      * @param parent    the parent component
      * @param id        the component ID. If null, no ID will be set - in this case,
      * don't set an ID on the component afterwards, otherwise the wrong ID has already been published
      * in JSF2 events.
      * @param componentType child component type, e.g. <code>javax.faces.SelectOne</code>
      * @param isTransient   if the component should be marked as transient
      * @return the created child component
      * @since               3.1.4
      */
     public static UIComponent addChildComponent(UIComponent parent, String id, String componentType, boolean isTransient) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Adding a component of type " + componentType + " to " + parent.getClientId(FacesContext.getCurrentInstance()));
         }
         final UIComponent child = createComponent(componentType);
         if (StringUtils.isNotBlank(id)) {
             child.setId(id);
         }
         child.setTransient(isTransient);
         parent.getChildren().add(child);
         return child;
     }
 
     /**
      * Return true if the current request has been submitted via Ajax4JSF.
      *
      * @return true if the current request has been submitted via Ajax4JSF.
      */
     public static boolean isAjaxRequest() {
         return getRequest().getParameterMap().containsKey("AJAXREQUEST");   // TODO: JSF2
     }
 
     /**
      * Recursively search for the first child of the given class type for the component.
      *
      * @param component the component to be searched
      * @param childType the required child type
      * @return the first child of the given type, or null if no child was found.
      */
     public static <T extends UIComponent> T findChild(UIComponent component, Class<T> childType) {
         for (Object item : component.getChildren()) {
             final UIComponent child = (UIComponent) item;
             if (childType.isAssignableFrom(child.getClass())) {
                 return childType.cast(child);
             }
             // search in this child's children
             final T nestedChild = findChild(child, childType);
             if (nestedChild != null) {
                 return nestedChild;
             }
         }
         return null;
     }
 
     /**
      * Convert a list of SelectableObjects to JSF SelectItems.
      *
      * @param items the items to be converted
      * @param addEmptyElement if set to true a empty element is added
      * @return the given list converted to JSF SelectItems
      */
     public static List<SelectItem> asSelectListWithName(List<? extends SelectableObjectWithName> items, boolean addEmptyElement) {
         final List<SelectItem> result = new ArrayList<SelectItem>(items.size()+ (addEmptyElement ? 1 : 0));
         if (addEmptyElement)
             result.add(new FxJSFSelectItem());
         for (SelectableObjectWithName item : items)
             result.add(new FxJSFSelectItem(item));
         return result;
     }
 
     /**
      * Convert a list of SelectableObjectWithLabels to JSF SelectItems.
      *
      * @param items the items to be converted
      * @param addEmptyElement if set to true a empty element is added
      * @return the given list converted to JSF SelectItems
      */
     public static List<SelectItem> asSelectListWithLabel(List<? extends SelectableObjectWithLabel> items, boolean addEmptyElement) {
         final List<SelectItem> result = new ArrayList<SelectItem>(items.size()+ (addEmptyElement ? 1 : 0));
         if (addEmptyElement)
             result.add(new FxJSFSelectItem());
         for (SelectableObjectWithLabel item : items) {
             // use the SelectableObject constructor to allow a fallback if the label is not set
             result.add(new FxJSFSelectItem((SelectableObject) item));
         }
         return result;
     }
 
     /**
      * Convert the values of an enum to a select list.
      *
      * @param values the enum values to be converted
      * @return the select list backing the given enumeration values.
      */
 
     public static <T extends Enum> List<SelectItem> enumsAsSelectList(T[] values) {
         final ArrayList<SelectItem> result = new ArrayList<SelectItem>(values.length);
         for (Enum value : values)
             result.add(new FxJSFSelectItem(value));
         return result;
     }
 
     /**
      * Convert a list of SelectableObjectsWithLabel to JSF SelectItems.
      *
      * @param items the items to be converted
      * @return the given list converted to JSF SelectItems
      */
     public static List<SelectItem> asSelectListWithLabel(List<? extends SelectableObjectWithLabel> items) {
         return asSelectListWithLabel(items, false);
     }
 
     /**
      * Convert a list of SelectableObjectsWithName to JSF SelectItems.
      *
      * @param items the items to be converted
      * @return the given list converted to JSF SelectItems
      */
     public static List<SelectItem> asSelectListWithName(List<? extends SelectableObjectWithName> items) {
         return asSelectListWithName(items, false);
     }
 
 
     /**
      * Converts the given flexive select list to a list of JSF SelectItems.
      *
      * @param list the select list to be converted
      * @return a JSF select list corresponding to the given list options
      */
     public static List<SelectItem> asSelectList(FxSelectList list) {
         final List<SelectItem> result = new ArrayList<SelectItem>(list.getItemCount());
         for (FxSelectListItem item : (list.isSortEntries() ? list.getItemsSortedByLabel() : list.getItems()))
             result.add(new FxJSFSelectItem(item));
         return result;
     }
 
     /**
      * Converts a list of String arrays (2 dim, containing value and display) to a list of JSF SelectItems.
      *
      * @param list the list of String arrays (2 dim, containing value and display)
      * @return a JSF select list corresponding to the given list options
      */
     public static List<SelectItem> asSelectList(List<String[]> list) {
         final List<SelectItem> result = new ArrayList<SelectItem>(list.size());
         for (String[] item : list) {
             if (item == null || item.length != 2)
                 continue;
             result.add(new FxJSFSelectItem(item[0], item[1]));
         }
         Collections.sort(result, new SelectItemSorter());
         return result;
     }
 
     /**
      * Comparator for sorting select items by their display label.
      */
     public static class SelectItemSorter implements Comparator<SelectItem> {
         private final Collator collator;
 
         public SelectItemSorter() {
             this.collator = FxSharedUtils.getCollator();
         }
 
         public int compare(SelectItem o1, SelectItem o2) {
             if (o1.getLabel() == null && o2.getLabel() != null) {
                 return -1;
             } else if (o2.getLabel() == null && o1.getLabel() != null) {
                 return 1;
             } else if (o1.getLabel() == null && o2.getLabel() == null) {
                 return 0;
             } else {
                 return this.collator.compare(o1.getLabel(), o2.getLabel());
             }
         }
     }
 
     /**
      * Comparator for sorting FxScriptInfo objects by their name.
      */
     public static class ScriptInfoSorter implements Comparator<FxScriptInfo> {
         private final Collator collator;
 
         public ScriptInfoSorter() {
             this.collator = FxSharedUtils.getCollator();
         }
 
         public int compare(FxScriptInfo o1, FxScriptInfo o2) {
             return this.collator.compare(o1.getName(), o2.getName());
         }
     }
 
     /**
      * Checks if the minimum and maximum values of a muliplicity are in valid ranges
      *
      * @param min minMultiplicity
      * @param max maxMultiplicity
      * @throws FxApplicationException on errors
      */
     public static void checkMultiplicity(int min, int max) throws FxApplicationException {
         if (min < 0)
             throw new FxApplicationException("ex.structure.multiplicity.minimum.invalid", min, max);
         if (max < 1)
             throw new FxApplicationException("ex.structure.multiplicity.maximum.invalid", max, min);
         if (min > max)
             throw new FxApplicationException("ex.structure.multiplicity.minimum.invalid", min, max);
     }
 
     /**
      * Returns the given component in the current view, or null if it does not exist.
      *
      * @param context   the current faces context
      * @param clientId  the component's client ID
      * @return  the given component in the current view, or null if it does not exist.
      */
     public static UIComponent findComponent(FacesContext context, String clientId) {
         return context.getViewRoot().findComponent(clientId);
     }
 
     /**
      * Returns true if the current request is served for the given browser, with at least the given
      * browser version.
      *
      * @param browser   the desired browser
      * @param minVersion    the minimum version. If set to -1, it is not checked.
      * @return  true if the current browser matches the requested parameters
      * @since 3.1
      */
     public static boolean isBrowser(FxRequest.Browser browser, double minVersion) {
         return browser.equals(getRequest().getBrowser()) && getRequest().getBrowserVersion() >= minVersion;
     }
 
     /**
      * Returns true if the current request is served for the given browser, but the version does
      * not meet the given minimum version.
      *
      * @param browser   the desired browser
      * @param minVersion    the minimum version. If set to -1, it is not checked.
      * @return  true if the current browser is served by the given browser, but the version is too old
      * @since 3.1
      */
     public static boolean isOlderBrowserThan(FxRequest.Browser browser, double minVersion) {
         return browser.equals(getRequest().getBrowser()) && getRequest().getBrowserVersion() < minVersion;
     }
 
     /**
      * Strip the context path from the given URL. The input is not validated.
      *
      * @param urlWithContextPath    the URL with context path
      * @return                      the URL without context path
      */
     public static String stripContextPath(String urlWithContextPath) {
         return urlWithContextPath == null ? null : urlWithContextPath.substring(getRequest().getContextPath().length());
     }
 }
