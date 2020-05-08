 package com.xaf.form;
 
 import java.io.*;
 import java.util.*;
 import org.w3c.dom.*;
 import com.xaf.value.*;
 import com.xaf.form.field.*;
 
 public class DialogField
 {
     // all these values are also defined in dialog.js (make sure they are always in sync)
 	static public final int FLDFLAG_REQUIRED             = 1;
 	static public final int FLDFLAG_PRIMARYKEY           = FLDFLAG_REQUIRED * 2;
 	static public final int FLDFLAG_INVISIBLE            = FLDFLAG_PRIMARYKEY * 2;
 	static public final int FLDFLAG_READONLY             = FLDFLAG_INVISIBLE * 2;
 	static public final int FLDFLAG_INITIAL_FOCUS        = FLDFLAG_READONLY * 2;
 	static public final int FLDFLAG_PERSIST              = FLDFLAG_INITIAL_FOCUS * 2;
 	static public final int FLDFLAG_CREATEADJACENTAREA   = FLDFLAG_PERSIST * 2;
 	static public final int FLDFLAG_SHOWCAPTIONASCHILD   = FLDFLAG_CREATEADJACENTAREA * 2;
 	static public final int FLDFLAG_INPUT_HIDDEN         = FLDFLAG_SHOWCAPTIONASCHILD * 2;
 	static public final int FLDFLAG_HAS_CONDITIONAL_DATA = FLDFLAG_INPUT_HIDDEN * 2;
 	static public final int FLDFLAG_COLUMN_BREAK_BEFORE  = FLDFLAG_HAS_CONDITIONAL_DATA * 2;
 	static public final int FLDFLAG_COLUMN_BREAK_AFTER   = FLDFLAG_COLUMN_BREAK_BEFORE * 2;
     static public final int FLDFLAG_BROWSER_READONLY     = FLDFLAG_COLUMN_BREAK_AFTER * 2;
     public static final int FLDFLAG_IDENTIFIER           = FLDFLAG_BROWSER_READONLY * 2;
 	static public final int FLDFLAG_STARTCUSTOM          = FLDFLAG_IDENTIFIER * 2; // all DialogField "children" will use this
 
     // flags used to describe what kind of formatting needs to be done to the dialog field
     public static final int DISPLAY_FORMAT = 1;
     public static final int SUBMIT_FORMAT  = 2;
 
 	static public int[] CHILD_CARRY_FLAGS = new int[] { FLDFLAG_REQUIRED, FLDFLAG_INVISIBLE, FLDFLAG_READONLY, FLDFLAG_PERSIST, FLDFLAG_CREATEADJACENTAREA, FLDFLAG_SHOWCAPTIONASCHILD  };
 
 	static public String CUSTOM_CAPTION = new String();
 	static public String GENERATE_CAPTION = "*";
 
 	static public String FIELDTAGPREFIX = "field.";
 
 	static public int fieldCounter = 0;
 
 	private DialogField parent;
     private int arrayIndex = -1;
 	private String id;
 	private String simpleName;
 	private String qualifiedName;
 	private SingleValueSource caption;
 	private String cookieName;
 	private String errorMessage;
 	private SingleValueSource defaultValue;
 	private List errors;
 	private List children;
 	private List conditionalActions;
 	private List dependentConditions;
     private List clientJavascripts;
 	private long flags;
 	private DialogFieldPopup popup;
     private String hint;
 
 	public DialogField()
 	{
 		defaultValue = null;
 		errorMessage = null;
 		flags = 0;
 	}
 
 	public DialogField(String aName, String aCaption)
 	{
 		this();
 		setSimpleName(aName);
 		caption = aCaption != null ? ValueSourceFactory.getSingleOrStaticValueSource(aCaption) : null;
 	}
 
 	public boolean defaultIsListValueSource()
 	{
 		return false;
 	}
 
 	public void importFromXml(Element elem)
 	{
 		simpleName = elem.getAttribute("name");
 		if(simpleName.length() == 0) simpleName = null;
 		setSimpleName(simpleName);
 
 		String captionStr = elem.getAttribute("caption");
 		if(captionStr.length() > 0)
 			caption = ValueSourceFactory.getSingleOrStaticValueSource(captionStr);
 
 		if(! defaultIsListValueSource())
 		{
 			String defaultv = elem.getAttribute("default");
 			if(defaultv.length() > 0)
 			{
 				defaultValue = ValueSourceFactory.getSingleOrStaticValueSource(defaultv);
 			}
 			else
 				defaultValue = null;
 		}
 
         hint = elem.getAttribute("hint");
         if(hint.length() == 0) hint = null;
 
 		if(elem.getAttribute("required").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_REQUIRED);
 
 		if(elem.getAttribute("primary=key").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_PRIMARYKEY);
 
 		if(elem.getAttribute("initial-focus").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_INITIAL_FOCUS);
 
         // 1. read-only flag of 'yes' will display the field value as a static string
         // within SPAN tags and the INPUT will be hidden.
         // 2. read-only flag of 'browser' will display the field value as
         // an INPUT with readonly flag set.
         String readonly = elem.getAttribute("read-only");
 		if(readonly.equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_READONLY);
         else if (readonly.equalsIgnoreCase("browser"))
         	setFlag(DialogField.FLDFLAG_BROWSER_READONLY);
 
 		if(elem.getAttribute("visible").equalsIgnoreCase("no"))
 			setFlag(DialogField.FLDFLAG_INVISIBLE);
 
 		if(elem.getAttribute("hidden").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_INPUT_HIDDEN);
 
 		if(elem.getAttribute("persist").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_PERSIST);
 
 		if(elem.getAttribute("show-child-caption").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_SHOWCAPTIONASCHILD);
 
 		if(elem.getAttribute("create-adjacent-area").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_CREATEADJACENTAREA);
 
         if(elem.getAttribute("identifier").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_IDENTIFIER);
 
 		String colBreak = elem.getAttribute("col-break");
 		if(colBreak.length() > 0)
 		{
 			if(colBreak.equals("before"))
 				setFlag(DialogField.FLDFLAG_COLUMN_BREAK_BEFORE);
 			else if(colBreak.equals("after") || colBreak.equals("yes"))
 				setFlag(DialogField.FLDFLAG_COLUMN_BREAK_AFTER);
 		}
 
 		importChildrenFromXml(elem);
 	}
 
 	public void importChildrenFromXml(Element elem)
 	{
 		NodeList children = elem.getChildNodes();
 		for(int n = 0; n < children.getLength(); n++)
 		{
 			Node node = children.item(n);
 			if(node.getNodeType() != Node.ELEMENT_NODE)
 				continue;
 
 			String childName = node.getNodeName();
 			if(childName.startsWith(FIELDTAGPREFIX))
 			{
 				Element fieldElem = (Element) node;
 				DialogField field = DialogFieldFactory.createField(childName);
 				if(field != null)
 					field.importFromXml(fieldElem);
 				else
 					field = new SelectField("error", "Unable to create field of type '" + childName, SelectField.SELECTSTYLE_COMBO, ValueSourceFactory.getListValueSource("dialog-field-types:"));
 				addChildField(field);
 			}
 			else if(childName.equals("conditional"))
 			{
 				importConditionalFromXml((Element) node);
 			}
 			else if(childName.equals("popup"))
 			{
 				importPopupFromXml((Element) node);
 			}
             else if (childName.equals("client-js"))
             {
                 importCustomJavaScriptFromXml((Element) node);
             }
 		}
 	}
 
     /**
      * Reads the XML for Custom Javascript configuration assigned to a dialog field
      *
      * @param elem client-js node
      * @returns none
      */
     public void importCustomJavaScriptFromXml(Element elem)
     {
         // what time of event should this custom JS respond to
         String event = elem.getAttribute("event");
         if(event == null || event.length() == 0)
         {
             addErrorMessage("No 'event' specified for custom Javascript.");
             return;
         }
         else if (!DialogFieldClientJavascript.isValidEvent(event))
         {
             addErrorMessage("Invalid 'event' specified for custom Javascript.");
             return;
         }
 
         // whether or not if this JS script should overwrite or extend the existing default JS
         // assigned to the event
         String type = elem.getAttribute("type");
         if (type == null || type.length() == 0)
         {
             addErrorMessage("No 'type' specified for custom Javascript.");
             return;
         }
         else if (!type.equals("extends") && !type.equals("override"))
         {
             addErrorMessage("Invalid 'type' specified for custom Javascript.");
             return;
         }
 
         // get the custom script
         String script = elem.getAttribute("js-expr");
         if (script == null || script.length() == 0)
         {
             addErrorMessage("No custom Javascript defined.");
             return;
         }
         DialogFieldClientJavascript customJS = new DialogFieldClientJavascript();
         customJS.setEvent(event);
         customJS.setType(type);
         customJS.setScript(script);
 
         this.addClientJavascript(customJS);
     }
 
 	public void importConditionalFromXml(Element elem)
 	{
         String action = elem.getAttribute("action");
         if(action == null || action.length() == 0)
         {
             addErrorMessage("No 'action' specified for conditional.");
             return;
         }
 
         DialogFieldConditionalAction actionInst = DialogFieldFactory.createConditional(action);
         if(actionInst != null)
         {
             int conditionalItem = (conditionalActions == null ? 0 : conditionalActions.size()) + 1;
             if(actionInst.importFromXml(this, elem, conditionalItem))
                 addConditionalAction(actionInst);
         }
         else
         {
             addErrorMessage("Conditional action '"+ action +"' unknown.");
         }
 	}
 
 	public void importPopupFromXml(Element elem)
 	{
 		String action = elem.getAttribute("action");
 		if(action.length() == 0)
 		{
 			addErrorMessage("Popup has no associated 'action' (URL).");
 			return;
 		}
 
 		String[] fillFields = null;
 		String fill = elem.getAttribute("fill");
 		if(fill.length() == 0)
 		{
 			fillFields = new String[] { getQualifiedName() };
 		}
 		else
 		{
 			if(fill.indexOf(",") > 0)
 			{
 				int fillCount = 0;
 				StringTokenizer st = new StringTokenizer(fill, ",");
 				while(st.hasMoreTokens())
 				{
 					st.nextToken();
 					fillCount++;
 				}
 
 				int fillIndex = 0;
 				fillFields = new String[fillCount];
 				st = new StringTokenizer(fill, ",");
 				while(st.hasMoreTokens())
 				{
 					fillFields[fillIndex] = st.nextToken();
 					fillIndex++;
 				}
 			}
 			else
 				fillFields = new String[] { fill };
 		}
 
 		popup = new DialogFieldPopup(action, fillFields);
 		String imgsrc = elem.getAttribute("image-src");
 		if(imgsrc.length() > 0)
 			popup.setImageUrl(imgsrc);
 	}
 
 	public void invalidate(DialogContext dc, String message)
 	{
 		dc.addErrorMessage(parent != null ? parent : this, message);
 	}
 
 	public final DialogField getParent() { return parent; }
 	public void setParent(DialogField newParent) { parent = newParent; }
 
 	public final String getId() { return id; }
 	public final String getSimpleName() { return simpleName; }
 	public final String getQualifiedName() { return qualifiedName; }
 
 	public void setSimpleName(String newName)
 	{
 		simpleName = newName;
 		if(simpleName != null)
 		{
 			id = Dialog.PARAMNAME_CONTROLPREFIX + simpleName;
 			setQualifiedName(simpleName);
 		}
 	}
 
 	public void setQualifiedName(String newName)
 	{
 		qualifiedName = newName;
 		if(qualifiedName != null)
 			id = Dialog.PARAMNAME_CONTROLPREFIX + qualifiedName;
 	}
 
 	public final String getCookieName() { return "DLG_" + parent.getSimpleName() + "_FLD_" + (cookieName.length() > 0 ? cookieName : simpleName); }
 	public void setCookieName(String name) { cookieName = name; }
 
 	public SingleValueSource getCaptionSource() { return caption; }
 	public String getCaption(DialogContext dc) { return caption != null ? caption.getValue(dc) : null; }
 	public void setCaption(SingleValueSource value) { caption = value; }
     public void setCaption(String value) { setCaption(value != null ? ValueSourceFactory.getSingleOrStaticValueSource(value) : null); }
 
 	public final String getHint() { return hint; }
 	public void setHint(String value) { hint = value; }
 
 	public final String getErrorMessage() { return errorMessage; }
 	public void setErrorMessage(String newMessage) { errorMessage = newMessage; }
 
 	public final SingleValueSource getDefaultValue() { return defaultValue; }
 	public void setDefaultValue(SingleValueSource value) { defaultValue = value; }
 
 	public final DialogFieldPopup getPopup() { return popup; }
 	public void setPopup(DialogFieldPopup value) { popup = value; }
 
 	public final List getChildren() { return children; }
 
 	public void addChildField(DialogField field)
 	{
 		for(int i = 0; i < CHILD_CARRY_FLAGS.length; i++)
 		{
 			int flag = CHILD_CARRY_FLAGS[i];
 			if((flags & flag) != 0)
 				field.setFlag(flag);
 		}
 
 		if(children == null) children = new ArrayList();
 		children.add(field);
 
 		field.setParent(this);
 		if(qualifiedName != null)
 			field.setQualifiedName(qualifiedName + "." + field.getSimpleName());
 	}
 
 	public final List getErrors() { return errors; }
 
 	public final void addErrorMessage(String msg)
 	{
 		if(errors == null) errors = new ArrayList();
 		errors.add(msg);
 	}
 
 	public final List getConditionalActions() { return conditionalActions; }
 
 	public void addConditionalAction(DialogFieldConditionalAction action)
 	{
 		if(conditionalActions == null) conditionalActions = new ArrayList();
 
 		if(action instanceof DialogFieldConditionalData || action instanceof DialogFieldConditionalApplyFlag)
 			setFlag(FLDFLAG_HAS_CONDITIONAL_DATA);
 
 		conditionalActions.add(action);
 	}
 
     /**
      * get all the javascripts defined for this field
      *
      * @returns ArrayList
      */
     public final List getClientJavascripts() { return this.clientJavascripts; }
 
     /**
      * Add a javascript to the list of scripts defined for this field
      *
      * @param script custom js object
      */
     public void addClientJavascript(DialogFieldClientJavascript script)
     {
         if(this.clientJavascripts == null)
             this.clientJavascripts = new ArrayList();
         this.clientJavascripts.add(script);
 
     }
 
 	public DialogField findField(String qualifiedName)
 	{
 		if(this.qualifiedName != null && this.qualifiedName.equals(qualifiedName))
 			return this;
 
 		if(children != null)
 		{
 			Iterator i = children.iterator();
 			while(i.hasNext())
 			{
 				DialogField field = (DialogField) i.next();
 				DialogField found = field.findField(qualifiedName);
 				if(found != null)
 					return found;
 			}
 		}
 
 		return null;
 	}
 
 	public void finalizeQualifiedName(Dialog dialog)
 	{
 		String newQName = simpleName;
 		if(parent != null && simpleName != null)
 			newQName = parent.getQualifiedName() + "." + simpleName;
 		if(newQName != null)
 			setQualifiedName(newQName);
 
 		if(children != null)
 		{
 			Iterator i = children.iterator();
 			while(i.hasNext())
 			{
 				DialogField field = (DialogField) i.next();
 				field.finalizeQualifiedName(dialog);
 			}
 		}
 	}
 
 	public void finalizeContents(Dialog dialog)
 	{
 		if(conditionalActions != null)
 		{
 			Iterator i = conditionalActions.iterator();
 			while(i.hasNext())
 			{
 				DialogFieldConditionalAction action = (DialogFieldConditionalAction) i.next();
 				DialogField partnerField = dialog.findField(action.getPartnerFieldName());
 				if(partnerField != null)
 					action.setPartnerField(partnerField);
 			}
 		}
 
 		if(children != null)
 		{
 			Iterator c = children.iterator();
 			while(c.hasNext())
 			{
 				DialogField field = (DialogField) c.next();
 				field.finalizeContents(dialog);
 			}
 		}
 	}
 
 	public final List getDependentConditions() { return dependentConditions; }
 	public void addDependentCondition(DialogFieldConditionalAction action)
 	{
 		if(dependentConditions == null) dependentConditions = new ArrayList();
 		dependentConditions.add(action);
 	}
 
 	public final long getFlags() { return flags; }
 	public final boolean flagIsSet(long flag) { return (flags & flag) == 0 ? false : true; }
 
 	public final void setFlag(long flag)
 	{
 		flags |= flag;
 		if(children != null)
 		{
 			Iterator i = children.iterator();
 			while(i.hasNext())
 			{
 				((DialogField) i.next()).setFlag(flag);
 			}
 		}
 	}
 
 	public final void clearFlag(long flag)
 	{
 		flags &= ~flag;
 		if(children != null)
 		{
 			Iterator i = children.iterator();
 			while(i.hasNext())
 			{
 				((DialogField) i.next()).clearFlag(flag);
 			}
 		}
 	}
 
 	public final boolean isRequired(DialogContext dc)
 	{
         String qName = getQualifiedName();
 		if(qName != null)
 		{
 			if(dc.flagIsSet(qName, FLDFLAG_REQUIRED)) return true;
 		}
 		else
 		{
 			if(flagIsSet(FLDFLAG_REQUIRED)) return true;
 		}
 
 		if(children != null)
 		{
 			Iterator i = children.iterator();
 			while(i.hasNext())
 			{
 				if(((DialogField) i.next()).isRequired(dc))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	public final boolean isVisible(DialogContext dc)
 	{
 		if(flagIsSet(FLDFLAG_HAS_CONDITIONAL_DATA))
 		{
 			Iterator i = conditionalActions.iterator();
 			while(i.hasNext())
 			{
 				DialogFieldConditionalAction action = (DialogFieldConditionalAction) i.next();
 				if(action instanceof DialogFieldConditionalData)
 				{
 					// if the partner field doesn't have data yet, hide this field
 					if(isRequired(dc))
 					{
 						String value = dc.getValue(action.getPartnerField());
 						if(value == null || value.length() == 0)
 							return false;
 					}
 				}
 			}
 		}
         String qName = getQualifiedName();
 		if(qName != null)
         {
 			return dc.flagIsSet(qName, FLDFLAG_INVISIBLE) ? false : true;
         }
 		else
         {
 			return flagIsSet(FLDFLAG_INVISIBLE) ? false : true;
         }
 	}
 
 	public final boolean isReadOnly(DialogContext dc)
 	{
         String qName = getQualifiedName();
 		if(qName != null)
 			return dc.flagIsSet(qName, FLDFLAG_READONLY);
 		else
 			return flagIsSet(FLDFLAG_READONLY);
 	}
 
     public final boolean isBrowserReadOnly(DialogContext dc)
     {
         String qName = getQualifiedName();
 		if(qName != null)
 		    return dc.flagIsSet(qName, FLDFLAG_BROWSER_READONLY);
 		else
 			return flagIsSet(FLDFLAG_BROWSER_READONLY);
     }
 
 	public final boolean isInputHidden(DialogContext dc)
 	{
 		if(simpleName != null)
 			return dc.flagIsSet(getQualifiedName(), FLDFLAG_INPUT_HIDDEN);
 		else
 			return flagIsSet(FLDFLAG_INPUT_HIDDEN);
 	}
 
 	public final boolean persistValue() { return (flags & FLDFLAG_PERSIST) == 0 ? false : true; }
 	public final boolean showCaptionAsChild() { return (flags & FLDFLAG_SHOWCAPTIONASCHILD) == 0 ? false : true; }
 
 	public String getHiddenControlHtml(DialogContext dc)
 	{
 		String value = dc.getValue(this);
         return "<input type='hidden' name='"+ getId() +"' value=\"" + (value != null ? value : "") + "\">";
 	}
 
 	public String getControlHtml(DialogContext dc)
 	{
 		if(flagIsSet(FLDFLAG_INPUT_HIDDEN))
 			return getHiddenControlHtml(dc);
 
 		if(children == null)
 			return null;
 
 		return dc.getSkin().getCompositeControlsHtml(dc, this);
 	}
 
 	public boolean needsValidation(DialogContext dc)
 	{
 		if(flagIsSet(FLDFLAG_HAS_CONDITIONAL_DATA))
 			return true;
 
 		if(children == null)
 			return isRequired(dc);
 
 		int validateFieldsCount = 0;
 		Iterator i = children.iterator();
 		while(i.hasNext())
 		{
 			DialogField field = (DialogField) i.next();
 			if(field.isVisible(dc) && field.needsValidation(dc))
 				validateFieldsCount++;
 		}
 
 		return validateFieldsCount > 0 ? true : false;
 	}
 
 	public boolean isValid(DialogContext dc)
 	{
 		if(flagIsSet(FLDFLAG_HAS_CONDITIONAL_DATA))
 		{
 			Iterator i = conditionalActions.iterator();
 			while(i.hasNext())
 			{
 				DialogFieldConditionalAction action = (DialogFieldConditionalAction) i.next();
 				if(action instanceof DialogFieldConditionalData)
 				{
                     // if the partner field doesn't have data, then this field is "invalid"
 					if(isRequired(dc) && dc.getValue(action.getPartnerField()) == null)
 						return false;
 				}
 			}
 		}
 		if(children == null)
 			return true;
 
 		int invalidFieldsCount = 0;
 		Iterator i = children.iterator();
 		while(i.hasNext())
 		{
 			DialogField field = (DialogField) i.next();
 			if(field.isVisible(dc) && (! field.isValid(dc)))
 				invalidFieldsCount++;
 		}
 		return invalidFieldsCount == 0 ? true : false;
 	}
 
     /**
      * Format the dialog value after it has been validated and is ready for submission
      *
      * @param value dialog field value
      * @returns String
      */
 	public String formatSubmitValue(String value)
 	{
 		return value;
 	}
 
     /**
      * Format the dialog value for every dialog stage except before submission
      *
      * @param value dialog field value
      * @returns String
      */
 	public String formatDisplayValue(String value)
 	{
 		return value;
 	}
 
 
     public Object getValueAsObject(String value)
     {
         return value;
     }
 
 	public Object getValueForSqlBindParam(String value)
 	{
 		return getValueAsObject(value);
 	}
 
     public void makeStateChanges(DialogContext dc, int stage)
     {
         if(stage == DialogContext.STATECALCSTAGE_INITIAL && conditionalActions != null)
         {
             for(int i = 0; i < conditionalActions.size(); i++)
             {
                 DialogFieldConditionalAction action = (DialogFieldConditionalAction) conditionalActions.get(i);
                 if(action instanceof DialogFieldConditionalApplyFlag)
                     ((DialogFieldConditionalApplyFlag) action).applyFlags(dc);
             }
         }
 
         if (children != null)
         {
             Iterator i = children.iterator();
             while(i.hasNext())
             {
                 DialogField field = (DialogField) i.next();
                 field.makeStateChanges(dc, stage);
             }
         }
     }
 
 	public void populateValue(DialogContext dc, int formatType)
 	{
 		if(id == null) return;
 
         String value = dc.getValue(this);
         if(value == null)
     		value = dc.getRequest().getParameter(id);
 		if(dc.getRunSequence() == 1)
 		{
 			if((value != null && value.length() == 0 && defaultValue != null) ||
 				(value == null && defaultValue != null))
 				value = defaultValue.getValueOrBlank(dc);
 		}
         if (formatType == DialogField.DISPLAY_FORMAT)
 		    dc.setValue(this, this.formatDisplayValue(value));
         else if (formatType == DialogField.SUBMIT_FORMAT)
             dc.setValue(this, this.formatSubmitValue(value));
 
 		if(children == null) return;
 
 		Iterator i = children.iterator();
 		while(i.hasNext())
 		{
 			DialogField field = (DialogField) i.next();
 			if(field.isVisible(dc)) field.populateValue(dc, formatType);
 		}
 	}
 
     /**
      * Produces JavaScript code to handle Client-side events for the dialog field
      *
      */
     public String getJavaScriptDefn(DialogContext dc)
 	{
 		String fieldClassName = this.getClass().getName();
 		String js =
 			"field = new DialogField(\"" + fieldClassName + "\", \""+ this.getId() + "\", \"" + this.getSimpleName() + "\", \"" + this.getQualifiedName() + "\", \"" + this.getCaption(dc) + "\", " + this.getFlags() +");\n" +
 			"dialog.registerField(field);\n";
         String customStr = this.getEventJavaScriptFunctions(dc);
         customStr += this.getCustomJavaScriptDefn(dc);
         if (customStr != null)
             js += customStr;
 
 		List dependentConditions = this.getDependentConditions();
 		if(dependentConditions != null)
 		{
 			StringBuffer dcJs = new StringBuffer();
 			Iterator i = dependentConditions.iterator();
 			while(i.hasNext())
 			{
 				DialogFieldConditionalAction o = (DialogFieldConditionalAction) i.next();
 
 				if(o instanceof DialogFieldConditionalDisplay)
 				{
 					DialogFieldConditionalDisplay action = (DialogFieldConditionalDisplay) o;
					dcJs.append("field.dependentConditions[field.dependentConditions.length] = new DialogFieldConditionalDisplay(\""+ action.getSourceField().getQualifiedName() +"\", \""+ action.getPartnerField().getQualifiedName() + "\", \""+ action.getExpression() + "\");\n");
 				}
 			}
 			js = js + dcJs.toString();
 		}
 
 		List children = this.getChildren();
 		if(children != null)
 		{
 			StringBuffer childJs = new StringBuffer();
 			Iterator i = children.iterator();
 			while(i.hasNext())
 			{
 				DialogField child = (DialogField) i.next();
 				childJs.append(child.getJavaScriptDefn(dc));
 			}
 			js = js + childJs.toString();
 		}
 
 		return js;
 	}
 
     /**
      * Retrieves user defined java script strings for this field and creates JS functions
      * out of them
      */
     public String getEventJavaScriptFunctions(DialogContext dc)
     {
         String ret = "";
 
         List jsList = this.getClientJavascripts();
         if (jsList != null)
         {
             String jsType = "";
             String eventName = "";
 
             StringBuffer jsBuffer = new StringBuffer();
             Iterator i = jsList.iterator();
 			while(i.hasNext())
 			{
 				DialogFieldClientJavascript jsObject = (DialogFieldClientJavascript) i.next();
                 String script = (jsObject.getScript() != null ? jsObject.getScript().getValue(dc) : null);
                 eventName = com.xaf.xml.XmlSource.xmlTextToJavaIdentifier(jsObject.getEvent().getValue(dc), false);
                 // append function signature
                 if (script != null)
                 {
                     jsBuffer.append("field.customHandlers." + eventName + " = new Function(\"field\", \"control\", \"" +
                         jsObject.getScript().getValue(dc) + "\");\n");
                     jsBuffer.append("field.customHandlers." + eventName + "Type = '" + jsObject.getType().getValue(dc) + "';\n");
                 }
             }
             ret = ret + jsBuffer.toString();
         }
         return ret;
     }
 
     /**
      * Empty method. Overwritten by extending classes that define extra Javascript definitions.
      */
     public String getCustomJavaScriptDefn(DialogContext dc)
     {
         return "";
     }
 
     /**
 	 * Produces Java code when a custom DialogContext is created
 	 */
 	public DialogContextMemberInfo createDialogContextMemberInfo(String dataTypeName)
 	{
 		DialogContextMemberInfo mi = new DialogContextMemberInfo(this.getQualifiedName(), dataTypeName);
 
         String memberName = mi.getMemberName();
         String fieldName = mi.getFieldName();
 
         mi.addJavaCode("\tpublic boolean is" + memberName + "ValueSet() { return hasValue(\""+ fieldName +"\"); }\n");
         mi.addJavaCode("\tpublic boolean is" + memberName + "FlagSet(long flag) { return flagIsSet(\""+ fieldName +"\", flag); }\n");
 		mi.addJavaCode("\tpublic void set" + memberName + "Flag(long flag) { setFlag(\""+ fieldName +"\", flag); }\n");
         mi.addJavaCode("\tpublic void clear" + memberName + "Flag(long flag) { clearFlag(\""+ fieldName +"\", flag); }\n");
         mi.addJavaCode("\tpublic String get" + memberName + "RequestParam() { return request.getParameter(\""+ Dialog.PARAMNAME_CONTROLPREFIX + fieldName +"\"); }\n");
         mi.addJavaCode("\tpublic DialogField get" + memberName + "Field() { return getField(\""+ fieldName +"\"); }\n");
         mi.addJavaCode("\tpublic DialogContext.DialogFieldState get" + memberName + "FieldState() { return getFieldState(\""+ fieldName +"\"); }\n");
         mi.addJavaCode("\tpublic void add" + memberName + "ErrorMsg(String msg) { addErrorMessage(\""+ fieldName +"\", msg); }\n");
 
         return mi;
 	}
 
 	/**
 	 * Produces Java code when a custom DialogContext is created
      * The default method produces nothing; all the subclasses must define what they need.
 	 */
 	public DialogContextMemberInfo getDialogContextMemberInfo()
 	{
         if(children == null) return null;
 
         DialogContextMemberInfo mi = createDialogContextMemberInfo("children");
 		Iterator i = children.iterator();
 		while(i.hasNext())
 		{
 			DialogField field = (DialogField) i.next();
             DialogContextMemberInfo childMI = field.getDialogContextMemberInfo();
             if(childMI == null)
                 continue;
 
             String[] childImports = childMI.getImportModules();
             if(childImports != null)
             {
                 for(int m = 0; m < childImports.length; m++)
                     mi.addImportModule(childImports[m]);
             }
 
             mi.addJavaCode(childMI.getCode());
 		}
 
         return mi;
 	}
 }
