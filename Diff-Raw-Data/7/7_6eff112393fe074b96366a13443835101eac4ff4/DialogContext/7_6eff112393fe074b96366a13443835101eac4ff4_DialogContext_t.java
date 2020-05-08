 package com.xaf.form;
 
 import java.lang.reflect.*;
 import java.util.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.security.*;
 
 import com.xaf.db.*;
 import com.xaf.value.*;
 
 public final class DialogContext extends Hashtable implements ValueContext
 {
     /* if dialog fields need to be pre-populated (before the context is created)
      * then a java.util.Map can be created and stored in the request attribute
      * called "dialog-field-values". All keys in the map are field names, and values
      * are the field values
      */
 	static public final String DIALOG_FIELD_VALUES_ATTR_NAME = "dialog-field-values";
 
     /* after the dialog context is created, it is automatically stored as
      * request parameter with this name so that it is available throughout the
      * request
      */
 	static public final String DIALOG_CONTEXT_ATTR_NAME = "dialog-context";
 
 	public class DialogFieldState
 	{
 		public DialogField field;
 		public String value;
 		public String[] values;
 		public long flags;
 		public ArrayList errorMessages;
 
 		DialogFieldState(DialogField aField)
 		{
 			field = aField;
 			flags = field.getFlags();
 		}
 	}
 
 	static public final char DIALOGMODE_UNKNOWN  = ' ';
 	static public final char DIALOGMODE_INPUT    = 'I';
 	static public final char DIALOGMODE_VALIDATE = 'V';
 	static public final char DIALOGMODE_EXECUTE  = 'E';
 
 	static public final int VALSTAGE_NOT_PERFORMED       = 0;
 	static public final int VALSTAGE_PERFORMED_FAILED    = 1;
 	static public final int VALSTAGE_PERFORMED_SUCCEEDED = 2;
 
 	static public final int STATECALCSTAGE_INITIAL = 0;
 	static public final int STATECALCSTAGE_FINAL   = 1;
 
 	private List listeners = new ArrayList();
 	private String transactionId;
 	private HttpServletRequest request;
 	private HttpServletResponse response;
 	private Servlet servlet;
 	private ServletContext servletContext;
 	private Dialog dialog;
 	private DialogSkin skin;
 	private char activeMode;
 	private char nextMode;
 	private int runSequence;
     private int execSequence;
 	private int validationStage;
 	private String originalReferer;
 	private DatabaseContext dbContext;
 
 	public DialogContext(ServletContext aContext, Servlet aServlet, HttpServletRequest aRequest, HttpServletResponse aResponse, Dialog aDialog, DialogSkin aSkin)
 	{
         aRequest.setAttribute(DIALOG_CONTEXT_ATTR_NAME, this);
 
 		request = aRequest;
 		response = aResponse;
 		servlet = aServlet;
 		servletContext = aContext;
 
 		if(servlet instanceof DialogContextListener)
 			listeners.add(servlet);
 
 		dialog = aDialog;
 		skin = aSkin;
 		activeMode = DIALOGMODE_UNKNOWN;
 		nextMode = DIALOGMODE_UNKNOWN;
 		validationStage = VALSTAGE_NOT_PERFORMED;
 		String runSeqValue = request.getParameter(dialog.getRunSequenceParamName());
 		if(runSeqValue != null)
 			runSequence = new Integer(runSeqValue).intValue();
 		else
 			runSequence = 1;
 
 		String execSeqValue = request.getParameter(dialog.getExecuteSequenceParamName());
 		if(execSeqValue != null)
 			execSequence = new Integer(execSeqValue).intValue();
 		else
 			execSequence = 0; // we have not executed at all yet
 
 		if(runSequence == 1)
 		{
 			originalReferer = aRequest.getHeader("Referer");
 			try
 			{
 				MessageDigest md = MessageDigest.getInstance("MD5");
 	    		md.update((dialog.getName() + new Date().toString()).getBytes());
 		    	transactionId = md.digest().toString();
 			}
 			catch(NoSuchAlgorithmException e)
 			{
 				transactionId = "No MessageDigest Algorithm found!";
 			}
 		}
 		else
 		{
 			originalReferer = aRequest.getParameter(dialog.getOriginalRefererParamName());
 			transactionId = aRequest.getParameter(dialog.getTransactionIdParamName());
 		}
 
 		createStateFields(dialog.getFields());
 
 		DialogDirector director = dialog.getDirector();
 		if(director != null)
 		{
 			String qName = director.getQualifiedName();
 			if(qName != null)
 				put(qName, new DialogFieldState(director));
 		}
 	}
 
 	public List getListeners() { return listeners; }
 	public void addListener(DialogContextListener listener)
 	{
 		listeners.add(listener);
 	}
 
 	public void createStateFields(List fields)
 	{
 		int fieldsCount = fields.size();
 		for(int i = 0; i < fieldsCount; i++)
 		{
 			DialogField field = (DialogField) fields.get(i);
 			String qName = field.getQualifiedName();
 			if(qName != null)
 				put(qName, new DialogFieldState(field));
 			List children = field.getChildren();
 			if(children != null)
 				createStateFields(children);
 		}
 
 		if(runSequence == 1)
 		{
 			Map values = (Map) request.getAttribute(dialog.getValuesRequestAttrName());
 			if(values == null)
 				values = (Map) request.getAttribute(DIALOG_FIELD_VALUES_ATTR_NAME);
 			if(values != null)
 			{
 				for(Iterator i = values.keySet().iterator(); i.hasNext(); )
 				{
 					String keyName = (String) i.next();
 					DialogFieldState state = (DialogFieldState) get(keyName);
 					if(state != null)
 					{
 						Object keyObj = (Object) values.get(keyName);
 						if (keyObj != null)
 							state.value =  values.get(keyName).toString();
 						else
 							state.value = null;
 					}
 				}
 			}
 		}
 	}
 
 	public void calcState()
 	{
 		activeMode = DIALOGMODE_INPUT;
 		dialog.makeStateChanges(this, STATECALCSTAGE_INITIAL);
 
 		String autoExec = request.getParameter(Dialog.PARAMNAME_AUTOEXECUTE);
 		if(autoExec != null && ! autoExec.equals("no"))
 		{
 			activeMode = dialog.isValid(this) ? DIALOGMODE_EXECUTE : DIALOGMODE_VALIDATE;
 		}
 		else
 		{
 			String modeParamValue = request.getParameter(dialog.getActiveModeParamName());
 			if(modeParamValue != null)
 			{
 				char givenMode = modeParamValue.charAt(0);
 				activeMode = (
 					givenMode == DIALOGMODE_VALIDATE ?
 						(dialog.isValid(this) ? DIALOGMODE_EXECUTE : DIALOGMODE_VALIDATE) :
 						givenMode
 					);
 			}
 		}
 
 		nextMode = activeMode;
 		if(activeMode == DIALOGMODE_INPUT)
 		{
 			nextMode = dialog.needsValidation(this) ? DIALOGMODE_VALIDATE : DIALOGMODE_EXECUTE;
 		}
 		else if(activeMode == DIALOGMODE_VALIDATE)
 		{
 			nextMode = dialog.isValid(this) ? DIALOGMODE_EXECUTE : DIALOGMODE_VALIDATE;
 		}
 		else if(activeMode == DIALOGMODE_EXECUTE)
 		{
             execSequence++;
 
 			if(dialog.loopEntries())
 				nextMode = dialog.needsValidation(this) ? DIALOGMODE_VALIDATE : DIALOGMODE_EXECUTE;
 			else
 				nextMode = DIALOGMODE_INPUT;
 		}
 
 		dialog.makeStateChanges(this, STATECALCSTAGE_FINAL);
 	}
 
 	public final String getTransactionId() { return transactionId; }
 	public final int getRunSequence() { return runSequence; }
 	public final int getExecuteSequence() { return execSequence; }
 	public final boolean isInitialEntry() { return runSequence == 1; }
 	public final boolean isInitialExecute() { return execSequence == 1; }
 	public final boolean isDuplicateExecute() { return execSequence > 1; }
 	public final char getActiveMode() { return activeMode; }
 	public final char getNextMode() { return nextMode; }
 	public final boolean inInputMode() { return activeMode == DIALOGMODE_INPUT; }
 	public final boolean inExecuteMode() { return activeMode == DIALOGMODE_EXECUTE; }
 	public final String getOriginalReferer() { return originalReferer; }
 	public final Dialog getDialog() { return dialog; }
 	public final ServletContext getServletContext() { return servletContext; }
 	public final Servlet getServlet() { return servlet; }
 	public final ServletRequest getRequest() { return request; }
 	public final ServletResponse getResponse() { return response; }
 	public final HttpSession getSession() { return request.getSession(true); }
 	public final DialogSkin getSkin() { return skin; }
 
 	public final DatabaseContext getDatabaseContext() { return dbContext; }
 	public final void setDatabaseContext(DatabaseContext value) { dbContext = value; }
 
 	public boolean validationPerformed() { return validationStage != VALSTAGE_NOT_PERFORMED ? true : false; }
 	public int getValidationStage() { return validationStage; }
 	public void setValidationStage(int value) { validationStage = value; }
 
 	public String getStateHiddens()
 	{
 		StringBuffer hiddens = new StringBuffer();
 		hiddens.append("<input type='hidden' name='"+ dialog.getOriginalRefererParamName() +"' value='"+ originalReferer +"'>\n");
 		hiddens.append("<input type='hidden' name='"+ dialog.getTransactionIdParamName() +"' value='"+ transactionId +"'>\n");
 		hiddens.append("<input type='hidden' name='"+ dialog.getRunSequenceParamName() +"' value='"+ (runSequence + 1) +"'>\n");
 		hiddens.append("<input type='hidden' name='"+ dialog.getExecuteSequenceParamName() +"' value='"+ execSequence +"'>\n");
 		hiddens.append("<input type='hidden' name='"+ dialog.getActiveModeParamName() +"' value='"+ nextMode +"'>\n");
 		hiddens.append("<input type='hidden' name='"+ dialog.PARAMNAME_DIALOGQNAME +"' value='"+ (runSequence > 1 ? request.getParameter(Dialog.PARAMNAME_DIALOGQNAME) : request.getParameter(DialogManager.REQPARAMNAME_DIALOG)) +"'>\n");
 
 		if(dialog.retainRequestParams())
 		{
 			if(dialog.retainAllRequestParams())
 			{
 				for(Enumeration e = request.getParameterNames(); e.hasMoreElements(); )
 				{
 					String paramName = (String) e.nextElement();
 					if( paramName.startsWith(Dialog.PARAMNAME_DIALOGPREFIX) ||
 						paramName.startsWith(Dialog.PARAMNAME_CONTROLPREFIX))
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
 				String[] retainParams = dialog.getRetainRequestParams();
 				int retainParamsCount = retainParams.length;
 
 				for(int i = 0; i < retainParamsCount; i++)
 				{
 					String paramName = retainParams[i];
 					hiddens.append("<input type='hidden' name='");
 					hiddens.append(paramName);
 					hiddens.append(" value='");
 					hiddens.append(request.getParameter(paramName));
 					hiddens.append("'>\n");
 				}
 			}
 		}
 
 		return hiddens.toString();
 	}
 
 	public boolean flagIsSet(String fieldQName, long flag)
 	{
 		DialogFieldState state = (DialogFieldState) get(fieldQName);
 		return (state.flags & flag) != 0;
 	}
 
 	public void setFlag(String fieldQName, long flag)
 	{
 		DialogFieldState state = (DialogFieldState) get(fieldQName);
 		if(state != null)
 		{
 			state.flags |= flag;
 			List children = state.field.getChildren();
 			if(children != null)
 			{
 				Iterator i = children.iterator();
 				while(i.hasNext())
 				{
 					setFlag(((DialogField) i.next()).getQualifiedName(), flag);
 				}
 			}
 		}
 		else
 			throw new RuntimeException("Attempting to set flag '"+ flag +"' for non-existant field '"+ fieldQName + "': " + toString());
 	}
 
 	public void clearFlag(String fieldQName, long flag)
 	{
 		DialogFieldState state = (DialogFieldState) get(fieldQName);
 		if(state != null)
 		{
 			state.flags &= ~flag;
 			List children = state.field.getChildren();
 			if(children != null)
 			{
 				Iterator i = children.iterator();
 				while(i.hasNext())
 				{
 					clearFlag(((DialogField) i.next()).getQualifiedName(), flag);
 				}
 			}
 		}
 		else
 			throw new RuntimeException("Attempting to clear flag '"+ flag +"' for non-existant field '"+ fieldQName + "'" + toString());
 	}
 
 	public String getValue(DialogField field)
 	{
 		DialogFieldState state = (DialogFieldState) get(field.getQualifiedName());
 		if(state == null)
 			return null;
 		else
 			return state.value;
 	}
 
 	public String getValue(String qualifiedName)
 	{
 		DialogFieldState state = (DialogFieldState) get(qualifiedName);
 		if(state == null)
 			return null;
 		else
 			return state.value;
 	}
 
 	public Object getValueForSqlBindParam(DialogField field)
 	{
 		DialogFieldState state = (DialogFieldState) get(field.getQualifiedName());
 		if(state == null)
 			return null;
 		else
             return field.getValueForSqlBindParam(state.value);
 	}
 
 	public Object getValueForSqlBindParam(String qualifiedName)
 	{
 		DialogFieldState state = (DialogFieldState) get(qualifiedName);
 		if(state == null)
 			return null;
 		else
             return state.field.getValueForSqlBindParam(state.value);
 	}
 
 	public void setValue(String qualifiedName, String value)
 	{
 		DialogFieldState state = (DialogFieldState) get(qualifiedName);
 		state.value = value;
 	}
 
 	public void setValue(DialogField field, String value)
 	{
 		DialogFieldState state = (DialogFieldState) get(field.getQualifiedName());
 		state.value = value;
 	}
 
 	public String[] getValues(DialogField field)
 	{
 		DialogFieldState state = (DialogFieldState) get(field.getQualifiedName());
 		if(state == null)
 			return null;
 		else
 			return state.values;
 	}
 
 	public String[] getValues(String qualifiedName)
 	{
 		DialogFieldState state = (DialogFieldState) get(qualifiedName);
 		if(state == null)
 			return null;
 		else
 			return state.values;
 	}
 
 	public void setValues(DialogField field, String[] values)
 	{
 		DialogFieldState state = (DialogFieldState) get(field.getQualifiedName());
 		state.values = values;
 	}
 
 	public ArrayList getErrorMessages(DialogField field)
 	{
 		DialogFieldState state = (DialogFieldState) get(field.getQualifiedName());
 		if(state == null)
 			return field.getErrors();
 		else
 		{
 			ArrayList fieldErrors = state.field.getErrors();
 			if(fieldErrors != null)
 				return fieldErrors;
 			return state.errorMessages;
 		}
 	}
 
 	public void addErrorMessage(DialogField field, String message)
 	{
 		DialogFieldState state = (DialogFieldState) get(field.getQualifiedName());
 		if(state.errorMessages == null)
 			state.errorMessages = new ArrayList();

		for(Iterator i = state.errorMessages.iterator(); i.hasNext(); )
		{
			if(((String) i.next()).equals(message))
				return;
		}

 		state.errorMessages.add(message);
 	}
 
 	public String getDebugHtml()
 	{
 		StringBuffer values = new StringBuffer();
 
 		for(Enumeration e = elements(); e.hasMoreElements(); )
 		{
 			DialogFieldState state = (DialogFieldState) e.nextElement();
 			if(state.values != null)
 			{
 				StringBuffer multiValues = new StringBuffer();
 				for(int i = 0; i < state.values.length; i++)
 					multiValues.append(state.values[i] + "<br>");
 
 				values.append("<tr valign=top><td>"+state.field.getQualifiedName()+"</td><td>"+multiValues.toString()+"</td></tr>");
 			}
 			else
 			{
 				values.append("<tr valign=top><td>"+state.field.getQualifiedName()+"</td><td>"+state.value+"</td></tr>");
 			}
 		}
 
 		return "<table>"+
 			    "<tr><td><b>Dialog</b></td><td>"+ dialog.getName() +"</td></tr>" +
 			    "<tr><td><b>Run Sequence</b></td><td>"+ runSequence +"</td></tr>" +
 			    "<tr><td><b>Active/Next Mode</b></td><td>"+ activeMode + " -> " + nextMode +"</td></tr>" +
 			    "<tr><td><b>Validation Stage</b></td><td>"+ validationStage +"</td></tr>" +
 			    values.toString()+
 				"</table>";
 	}
 }
