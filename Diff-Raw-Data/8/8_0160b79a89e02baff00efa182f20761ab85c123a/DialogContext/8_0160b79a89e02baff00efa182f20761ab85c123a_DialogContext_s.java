 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only
  *    (as Java .class files or a .jar file containing the .class files) and only
  *    as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software
  *    development kit, other library, or development tool without written consent of
  *    Netspective Corporation. Any modified form of The Software is bound by
  *    these same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective
  *    Corporation and may not be used to endorse products derived from The
  *    Software without without written consent of Netspective Corporation. "Sparx"
  *    and "Netspective" may not appear in the names of products derived from The
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind.
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: DialogContext.java,v 1.36 2004-03-02 07:42:38 aye.thu Exp $
  */
 
 package com.netspective.sparx.form;
 
 import com.netspective.commons.text.TextUtils;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.source.StaticValueSource;
 import com.netspective.sparx.command.AbstractHttpServletCommand;
 import com.netspective.sparx.console.panel.presentation.HttpRequestParametersPanel;
 import com.netspective.sparx.console.panel.presentation.dialogs.DialogContextAttributesPanel;
 import com.netspective.sparx.console.panel.presentation.dialogs.DialogContextFieldStatesClassesPanel;
 import com.netspective.sparx.console.panel.presentation.dialogs.DialogContextFieldStatesPanel;
 import com.netspective.sparx.form.field.DialogFieldStates;
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.sparx.panel.HtmlLayoutPanel;
 import com.netspective.sparx.panel.HtmlPanel;
 import com.netspective.sparx.panel.HtmlPanelActionStates;
 import com.netspective.sparx.panel.HtmlPanelValueContext;
 import com.netspective.sparx.panel.HtmlPanelsStyleEnumeratedAttribute;
 import com.netspective.sparx.value.BasicDbHttpServletValueContext;
 import com.netspective.sparx.value.source.DialogFieldValueSource;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Writer;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * A dialog context functions as the controller of the dialog, tracking and managing field state and field data.
  * A new <code>DialogContext</code> object is created for each HTTP request coming from a JSP even though
  * the dialogs are cached.
  * <p>
  * For most occasions, the default <code>DialogContext</code> object should be sufficient but
  * for special curcumstances when the behavior of a dialog needs to be modified, the <code>DialogContext</code> class
  * can be extended (inherited) to create a customzied dialog context.
  * @see DialogState
  */
 public class DialogContext extends BasicDbHttpServletValueContext implements HtmlPanelValueContext
 {
     private static final Log log = LogFactory.getLog(DialogContext.class);
 
     /**
      * The name of a URL parameter, if present, that will be used as the redirect string after dialog execution
      */
     public static final String DEFAULT_REDIRECT_PARAM_NAME = "redirect";
 
     /* if dialog fields need to be pre-populated (before the context is created)
      * then a java.util.Map can be created and stored in the request attribute
      * called "dialog-field-values". All keys in the map are field names, and values
      * are the field values
      */
     public static final String DIALOG_FIELD_VALUES_ATTR_NAME = "dialog-field-values";
 
     /* after the dialog context is created, it is automatically stored as
      * request parameter with this name so that it is available throughout the
      * request
      */
     public static final String DIALOG_CONTEXT_ATTR_NAME = "dialog-context";
 
     public static final ValueSource dialogFieldStoreValueSource = new DialogFieldValueSource();
 
     /**
      * Used for indicating that the calculation of a dialog's stage is starting
      */
     public static final int STATECALCSTAGE_BEFORE_VALIDATION = 0;
 
     /**
      * Used for indicating that the calculation of a dialog's stage is ending
      */
     public static final int STATECALCSTAGE_AFTER_VALIDATION = 1;
 
     private int panelRenderFlags;
     private DialogFieldStates fieldStates = new DialogFieldStates(this);
     private DialogValidationContext validationContext = new DialogValidationContext(this);
     private boolean resetContext;
     private Dialog dialog;
     private DialogSkin skin;
     private DialogState state;
     private boolean executeHandled;
     private String[] retainReqParams;
     private boolean redirectDisabled;
     private boolean cancelButtonPressed;
     private boolean redirectAfterExecute;
 
     public DialogContext()
     {
     }
 
     public HtmlPanel getPanel()
     {
         return dialog;
     }
 
     public int getPanelRenderFlags()
     {
         return panelRenderFlags;
     }
 
     public boolean isMinimized()
     {
         return false;
     }
 
     public void setPanelRenderFlags(int panelRenderFlags)
     {
         this.panelRenderFlags = panelRenderFlags;
     }
 
     public boolean isRedirectAfterExecute()
     {
         return redirectAfterExecute;
     }
 
     public void setRedirectAfterExecute(boolean redirectAfterExecute)
     {
         this.redirectAfterExecute = redirectAfterExecute;
     }
 
     public HtmlPanelActionStates getPanelActionStates()
     {
         // TODO: currently this does not support states for panel actions
         return null;
     }
 
     /**
      * Initializes the dialog context object. Called by the <code>Dialog</code> after creating the context.
      *
      * @param aDialog the Dialog object which this context is associated with
      * @param aSkin the DialogSkin object of the dialog
      */
     public void initialize(NavigationContext nc, Dialog aDialog, DialogSkin aSkin)
     {
         super.initialize(nc);
 
         HttpServletRequest request = nc.getHttpRequest();
         nc.getRequest().setAttribute(DIALOG_CONTEXT_ATTR_NAME, this);
 
         String overrideSkin = request.getParameter(Dialog.PARAMNAME_OVERRIDE_SKIN);
         if(overrideSkin != null)
             aSkin = nc.getActiveTheme().getDialogSkin(overrideSkin);
 
         dialog = aDialog;
         skin = aSkin == null ? nc.getActiveTheme().getDefaultDialogSkin() : aSkin;
         redirectAfterExecute = aDialog.isRedirectAfterExecute();
 
         state = dialog.getDialogState(this);
         state.incRunSequence();
 
         String resetContext = request.getParameter(dialog.getResetContextParamName());
         if(resetContext != null)
         {
             state.reset(nc);
             this.resetContext = true;
         }
 
         // check to see if the dialog was submitted using the cancel button
         String cancelValue = request.getParameter("cancelButton");
         if (cancelValue != null && cancelValue.length() > 0)
             setCancelButtonPressed(true);
 
         ValueSource ncRetainVS = nc.getActivePage().getRetainParams();
         if(ncRetainVS != null)
         {
             String ncRetain = ncRetainVS.getTextValue(this);
             if(ncRetain != null && ncRetain.length() > 0)
                 addRetainRequestParams(TextUtils.split(ncRetain, ",", true));
         }
 
         nc.setDialogContext(this);
     }
 
     /**
      * Checks to see if the cancel button was pressed for dialog submittal
      * @return true if the dialog was submitted using the cancel cutton
      */
     public boolean isCancelButtonPressed()
     {
         return cancelButtonPressed;
     }
 
     /**
      * Sets the flag for the cancel button press
      * @param cancelButtonPressed
      */
     public void setCancelButtonPressed(boolean cancelButtonPressed)
     {
         this.cancelButtonPressed = cancelButtonPressed;
     }
 
     public DialogValidationContext getValidationContext()
     {
         return validationContext;
     }
 
     public void persistValues()
     {
         fieldStates.persistValues();
     }
 
     public boolean isRedirectDisabled()
     {
         return redirectDisabled;
     }
 
     public void setRedirectDisabled(boolean value)
     {
         redirectDisabled = value;
     }
 
     /**
      * Return the next action url (where to redirect after execute) based on user input or other method.
      */
     public String getNextActionUrl(String defaultUrl)
     {
         return dialog.getNextActionUrl(this, defaultUrl);
     }
 
     public void performDefaultRedirect(Writer writer, String redirect) throws IOException
     {
         if(! redirectAfterExecute)
             return;
 
         ServletRequest request = getRequest();
         String redirectToUrl = redirect != null ? redirect : request.getParameter(DEFAULT_REDIRECT_PARAM_NAME);
         if(redirectToUrl == null)
         {
             redirectToUrl = request.getParameter(dialog.getPostExecuteRedirectUrlParamName());
             if(redirectToUrl == null)
                 redirectToUrl = getNextActionUrl(state.getReferer());
         }
 
         if(redirectDisabled || redirectToUrl == null)
         {
             writer.write("<p><b>Redirect is disabled</b>.");
             writer.write("<br><code>redirect</code> method parameter is <code>"+ redirect +"</code>");
             writer.write("<br><code>redirect</code> URL parameter is <code>"+ request.getParameter(DEFAULT_REDIRECT_PARAM_NAME) +"</code>");
             writer.write("<br><code>redirect</code> form field is <code>"+ request.getParameter(dialog.getPostExecuteRedirectUrlParamName()) +"</code>");
             writer.write("<br><code>getNextActionUrl</code> method result is <code>"+ getNextActionUrl(null) +"</code>");
             writer.write("<br><code>original referer</code> url is <code>"+ state.getReferer() +"</code>");
             writer.write("<p><font color=red>Would have redirected to <code>"+ redirectToUrl +"</code>.</font>");
             return;
         }
 
         HttpServletResponse response = (HttpServletResponse) getResponse();
         if(response.isCommitted())
             skin.renderRedirectHtml(writer, this, response.encodeRedirectURL(redirectToUrl));
         else
             sendRedirect(redirectToUrl);
     }
 
     /**
      * Check the dataCmdCondition against each available data command and see if it's set in the condition; if the
      * data command is set in the condition, then check to see if our data command for that command id is set. If any
      * of the data commands in dataCommandCondition match our current dataCmd, return true.
      *
      * @param perspectives the data command condition
      * @return boolean True if the data commands in the passes in condition matches the current dialog data command
      */
     public boolean matchesPerspective(int perspectives)
     {
         if(perspectives == DialogPerspectives.NONE || state.getPerspectives().getFlags() == DialogPerspectives.NONE)
             return false;
 
         int lastDataCmd = DialogPerspectives.LAST;
         for(int i = 1; i <= lastDataCmd; i *= 2)
         {
             // if the dataCmdCondition's dataCmd i is set, it means we need to check our dataCmd to see if we're set
             if((perspectives & i) != 0 && state.getPerspectives().flagIsSet(i))
                 return true;
         }
 
         // if we get to here, nothing matched
         return false;
     }
 
     /**
      * Returns a string useful for displaying a unique Id for this DialogContext
      * in a log or monitor file.
      *
      * @return String Log id
      */
     public String getLogId()
     {
         return dialog.getHtmlFormName() + " (" + state.getIdentifier() + ")";
     }
 
     /**
      * Using a Document or element that was serialized using the exportToXml method in this class,
      * reconstruct the DialogFieldStates hash map. This is basically a data deserialization method.
      *
      * @param parent dialog context element's parent
      */
     public void importFromXml(Element parent)
     {
         NodeList dcList = parent.getElementsByTagName("dialog-context");
         if(dcList.getLength() > 0)
         {
             Element dcElem = (Element) dcList.item(0);
             fieldStates.importFromXml(dcElem);
         }
     }
 
     static public void exportParamToXml(Element parent, String name, String[] values)
     {
         Document doc = parent.getOwnerDocument();
         Element fieldElem = doc.createElement("request-param");
         fieldElem.setAttribute("name", name);
         if(values != null && values.length > 1)
         {
             fieldElem.setAttribute("value-type", "strings");
             Element valuesElem = doc.createElement("values");
             for(int i = 0; i < values.length; i++)
             {
                 Element valueElem = doc.createElement("value");
                 valueElem.appendChild(doc.createTextNode(values[i]));
                 valuesElem.appendChild(valueElem);
             }
             fieldElem.appendChild(valuesElem);
             parent.appendChild(fieldElem);
         }
         else if(values != null)
         {
             fieldElem.setAttribute("value-type", "string");
             Element valueElem = doc.createElement("value");
             valueElem.appendChild(doc.createTextNode(values[0]));
             fieldElem.appendChild(valueElem);
             parent.appendChild(fieldElem);
         }
     }
 
     public void exportToXml(Element parent)
     {
         ServletRequest request = getRequest();
         Element dcElem = parent.getOwnerDocument().createElement("dialog-context");
         dcElem.setAttribute("name", dialog.getName());
         dcElem.setAttribute("transaction", state.getIdentifier());
         fieldStates.exportToXml(dcElem);
 
         Set retainedParams = null;
         if(retainReqParams != null)
         {
             retainedParams = new HashSet();
             for(int i = 0; i < retainReqParams.length; i++)
             {
                 String paramName = retainReqParams[i];
                 String[] paramValues = request.getParameterValues(paramName);
                 if(paramValues != null)
                     exportParamToXml(dcElem, paramName, paramValues);
                 retainedParams.add(paramName);
             }
         }
         boolean retainedAnyParams = retainedParams != null;
 
         if(dialog.retainRequestParams())
         {
             if(dialog.getDialogFlags().flagIsSet(DialogFlags.RETAIN_ALL_REQUEST_PARAMS))
             {
                 for(Enumeration e = request.getParameterNames(); e.hasMoreElements();)
                 {
                     String paramName = (String) e.nextElement();
                     if(paramName.startsWith(Dialog.PARAMNAME_DIALOGPREFIX) ||
                             paramName.startsWith(Dialog.PARAMNAME_CONTROLPREFIX) ||
                             (retainedAnyParams && retainedParams.contains(paramName)))
                         continue;
 
                     exportParamToXml(dcElem, paramName, request.getParameterValues(paramName));
                 }
             }
             else
             {
                 String[] retainParams = dialog.getRetainParams();
                 int retainParamsCount = retainParams.length;
 
                 for(int i = 0; i < retainParamsCount; i++)
                 {
                     String paramName = retainParams[i];
                     if(retainedAnyParams && retainedParams.contains(paramName))
                         continue;
 
                     exportParamToXml(dcElem, paramName, request.getParameterValues(paramName));
                 }
             }
         }
 
         parent.appendChild(dcElem);
     }
 
     public void setFromXml(String xml) throws ParserConfigurationException, SAXException, IOException
     {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
 
         InputStream is = new java.io.ByteArrayInputStream(xml.getBytes());
         Document doc = builder.parse(is);
         importFromXml(doc.getDocumentElement());
     }
 
     public Document getAsXmlDocument() throws ParserConfigurationException
     {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document doc = builder.newDocument();
         Element root = doc.createElement("sparx");
         doc.appendChild(root);
 
         exportToXml(doc.getDocumentElement());
         return doc;
     }
 
     public String getAsXml() throws ParserConfigurationException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
     {
         Document doc = getAsXmlDocument();
 
         // we use reflection so that org.apache.xml.serialize.* is not a package requirement
         // TODO: when DOM Level 3 is finalized, switch it over to Load/Save methods in that DOM3 spec
 
         Class serializerCls = Class.forName("org.apache.xml.serialize.XMLSerializer");
         Class outputFormatCls = Class.forName("org.apache.xml.serialize.OutputFormat");
 
         Constructor serialCons = serializerCls.getDeclaredConstructor(new Class[]{OutputStream.class, outputFormatCls});
         Constructor outputCons = outputFormatCls.getDeclaredConstructor(new Class[]{Document.class});
 
         OutputStream os = new java.io.ByteArrayOutputStream();
 
         Object outputFormat = outputCons.newInstance(new Object[]{doc});
         Method indenting = outputFormatCls.getMethod("setIndenting", new Class[]{boolean.class});
         indenting.invoke(outputFormat, new Object[]{new Boolean(true)});
         Method omitXmlDecl = outputFormatCls.getMethod("setOmitXMLDeclaration", new Class[]{boolean.class});
         omitXmlDecl.invoke(outputFormat, new Object[]{new Boolean(true)});
 
         Object serializer = serialCons.newInstance(new Object[]{os, outputFormat});
         Method serialize = serializerCls.getMethod("serialize", new Class[]{Document.class});
         serialize.invoke(serializer, new Object[]{doc});
 
         return os.toString();
     }
 
     /**
      * Calculate what the next state or stage of the dialog should be.
      */
     public void calcState()
     {
         dialog.makeStateChanges(this, STATECALCSTAGE_BEFORE_VALIDATION);
 
         ServletRequest request = getRequest();
         DialogFlags dialogFlags = dialog.getDialogFlags();
 
         boolean ignoreValidation = false;
         if(dialog.getDialogFlags().flagIsSet(DialogFlags.ALLOW_PENDING_DATA))
         {
             String ignoreValidationOption = request.getParameter(dialog.getPendDataParamName());
             if(ignoreValidationOption != null && !ignoreValidationOption.equals("no"))
             {
                 ignoreValidation = true;
                 validationContext.setValidationStage(DialogValidationContext.VALSTAGE_IGNORE);
             }
         }
 
         boolean autoExec = dialog.isAutoExecByDefault();
         if(!autoExec && ! dialog.getDialogFlags().flagIsSet(DialogFlags.DISABLE_AUTO_EXECUTE))
         {
             String autoExecOption = request.getParameter(Dialog.PARAMNAME_AUTOEXECUTE);
             if (autoExecOption == null || autoExecOption.length() == 0)
                 // if no autoexec is defined in the request parameter, look for it also in the request attribute
                 autoExecOption = (String) request.getAttribute(Dialog.PARAMNAME_AUTOEXECUTE);
 
             if(dialog.isAutoExec(this, autoExecOption))
                 autoExec = true;
         }
         boolean executeButtonPressed =
             (request.getParameter(dialog.getSubmitDataParamName()) != null) ||
             (request.getParameter(dialog.getCancelDataParamName()) != null && dialog.getDialogFlags().flagIsSet(DialogFlags.ALLOW_EXECUTE_WITH_CANCEL_BUTTON));
         if(autoExec || executeButtonPressed || ignoreValidation)
         {
             if(! dialogFlags.flagIsSet(DialogFlags.ALLOW_MULTIPLE_EXECUTES) && state.isAlreadyExecuted())
             {
                 getValidationContext().addError(dialog.getMultipleExecErrorMessage().getTextValue(this));
                 state.reset(this);
                 return;
             }
 
             if(dialog.isValid(this))
                 state.setExecuteMode();
         }
 
         dialog.makeStateChanges(this, STATECALCSTAGE_AFTER_VALIDATION);
     }
 
     public DialogFieldStates getFieldStates()
     {
         return this.fieldStates;
     }
 
     public DialogState getDialogState()
     {
         return state;
     }
 
     /**
      * Indicates whether or not the context was reset
      *
      * @return boolean True if context was reset
      */
     public boolean contextWasReset()
     {
         return resetContext;
     }
 
     /**
      * Return true if the "pending" button was pressed in the dialog.
      *
      * @return boolean
      */
     public boolean isPending()
     {
         return validationContext.getValidationStage() == DialogValidationContext.VALSTAGE_IGNORE;
     }
 
     /**
      * Returns the <code>Dialog</code> object this context is associated with
      *
      * @return Dialog dialog object
      */
     public Dialog getDialog()
     {
         return dialog;
     }
 
     /**
      * Returns the <code>DialogSkin</code> object the dialog is using for its display
      *
      * @return DialogSkin dialog skin
      */
     public DialogSkin getSkin()
     {
         return skin;
     }
 
     public boolean addingData()
     {
         return state.getPerspectives().flagIsSet(DialogPerspectives.ADD);
     }
 
     public boolean editingData()
     {
         return state.getPerspectives().flagIsSet(DialogPerspectives.EDIT);
     }
 
     public boolean deletingData()
     {
         return state.getPerspectives().flagIsSet(DialogPerspectives.DELETE);
     }
 
     public boolean confirmingData()
     {
         return state.getPerspectives().flagIsSet(DialogPerspectives.CONFIRM);
     }
 
     public boolean printingData()
     {
         return state.getPerspectives().flagIsSet(DialogPerspectives.PRINT);
     }
 
     public boolean executeStageHandled()
     {
         return executeHandled;
     }
 
     public void setExecuteStageHandled(boolean value)
     {
         executeHandled = value;
     }
 
     /**
      * Retrieves the HTTP request parameters that has been retained through the different dialog states
      *
      * @return String[] a string array of request parameters
      */
     public String[] getRetainRequestParams()
     {
         return retainReqParams;
     }
 
     /**
      * Sets the HTTP request parameters to retain
      *
      * @param params HTTP request parameters
      */
     public void addRetainRequestParams(String[] params)
     {
         if(retainReqParams == null)
             retainReqParams = params;
         else
         {
             String[] oldReqParams = retainReqParams;
             retainReqParams = new String[oldReqParams.length + params.length];
             for(int i = 0; i < oldReqParams.length; i++)
                 retainReqParams[i] = oldReqParams[i];
             for(int i = 0; i < params.length; i++)
                 retainReqParams[oldReqParams.length + i] = params[i];
         }
     }
 
     /**
      * Returns a HTML string which contains hidden  form fields representing the dialog's information
      */
     public String getStateHiddens()
     {
         ServletRequest request = getRequest();
 
         StringBuffer hiddens = new StringBuffer();
         hiddens.append("<input type='hidden' name='" + dialog.getDialogStateIdentifierParamName() + "' value='" + state.getIdentifier() + "'>\n");
 
         String pageCmd = request.getParameter(AbstractHttpServletCommand.PAGE_COMMAND_REQUEST_PARAM_NAME);
         if(pageCmd != null)
             hiddens.append("<input type='hidden' name='" + AbstractHttpServletCommand.PAGE_COMMAND_REQUEST_PARAM_NAME + "' value='" + pageCmd + "'>\n");
 
         String redirectUrlParamValue = (state.isInitialEntry() ? request.getParameter(dialog.getPostExecuteRedirectUrlParamName()) : request.getParameter(DialogContext.DEFAULT_REDIRECT_PARAM_NAME));
         if(redirectUrlParamValue != null)
             hiddens.append("<input type='hidden' name='" + dialog.getPostExecuteRedirectUrlParamName() + "' value='" + redirectUrlParamValue + "'>\n");
 
         Set retainedParams = null;
         if(retainReqParams != null)
         {
             retainedParams = new HashSet();
             for(int i = 0; i < retainReqParams.length; i++)
             {
                 String paramName = retainReqParams[i];
                 Object paramValue = request.getParameter(paramName);
                 if(paramValue == null)
                     continue;
 
                 hiddens.append("<input type='hidden' name='");
                 hiddens.append(paramName);
                 hiddens.append("' value='");
                 hiddens.append(paramValue);
                 hiddens.append("'>\n");
                 retainedParams.add(paramName);
             }
         }
         boolean retainedAnyParams = retainedParams != null;
 
         if(dialog.retainRequestParams())
         {
             if(dialog.getDialogFlags().flagIsSet(DialogFlags.RETAIN_ALL_REQUEST_PARAMS))
             {
                 for(Enumeration e = request.getParameterNames(); e.hasMoreElements();)
                 {
                     String paramName = (String) e.nextElement();
                     if(paramName.startsWith(Dialog.PARAMNAME_DIALOGPREFIX) ||
                             paramName.startsWith(Dialog.PARAMNAME_CONTROLPREFIX) ||
                             (retainedAnyParams && retainedParams.contains(paramName)))
                         continue;
 
                     hiddens.append("<input type='hidden' name='");
                     hiddens.append(paramName);
                     hiddens.append("' value='");
                    hiddens.append(request.getParameter(paramName));
                     hiddens.append("'>\n");
                 }
             }
             else
             {
                 String[] retainParams = dialog.getRetainParams();
                 int retainParamsCount = retainParams.length;
 
                 for(int i = 0; i < retainParamsCount; i++)
                 {
                     String paramName = retainParams[i];
                     if(retainedAnyParams && retainedParams.contains(paramName))
                         continue;
 
                     hiddens.append("<input type='hidden' name='");
                     hiddens.append(paramName);
                     hiddens.append("' value='");
                    hiddens.append(request.getParameter(paramName));
                     hiddens.append("'>\n");
                 }
             }
         }
 
         return hiddens.toString();
     }
 
     public void renderDebugPanels(Writer writer) throws IOException
     {
         debugPanels.render(writer, this, this.getActiveTheme(), HtmlPanel.RENDERFLAGS_DEFAULT);
     }
 
     protected static final HtmlLayoutPanel debugPanels = new HtmlLayoutPanel();
     static
     {
         debugPanels.getFrame().setHeading(new StaticValueSource("Dialog Context Debug"));
         debugPanels.setStyle(new HtmlPanelsStyleEnumeratedAttribute(HtmlPanelsStyleEnumeratedAttribute.TABBED));
         debugPanels.getFrame().setFooting(new StaticValueSource("NOTE: You need to add override Dialog.execute(Writer, DialogContext)."));
         debugPanels.setIdentifier("DlgCntxt_Debug");
 
         DialogContextAttributesPanel attrPanel = new DialogContextAttributesPanel();
         attrPanel.setPanelIdentifier("DlgCntxt_Debug_Attributes");
         debugPanels.addPanel(attrPanel);
         DialogContextFieldStatesPanel statesPanel = new DialogContextFieldStatesPanel();
         statesPanel.setPanelIdentifier("DlgCntxt_Debug_States");
         debugPanels.addPanel(statesPanel);
         DialogContextFieldStatesClassesPanel stateClassesPanel = new DialogContextFieldStatesClassesPanel();
         stateClassesPanel.setPanelIdentifier("DlgCntxt_Debug_State_Classes");
         debugPanels.addPanel(stateClassesPanel);
         HttpRequestParametersPanel reqPanel = new HttpRequestParametersPanel();
         reqPanel.setPanelIdentifier("DlgCntxt_Debug_RequestParams");
         debugPanels.addPanel(reqPanel);
     }
 }
