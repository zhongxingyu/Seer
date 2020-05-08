 package com.xaf.form.field;
 
 import java.io.*;
 import java.util.*;
 
 import org.w3c.dom.*;
 
 import com.xaf.form.*;
 import com.xaf.value.*;
 
 public class SelectField extends DialogField
 {
 	static public final long FLDFLAG_SORTCHOICES  = DialogField.FLDFLAG_STARTCUSTOM;
 	static public final long FLDFLAG_PREPENDBLANK = FLDFLAG_SORTCHOICES * 2;
 	static public final long FLDFLAG_APPENDBLANK  = FLDFLAG_PREPENDBLANK * 2;
 
 	static private final SelectChoicesList EMPTY_CHOICES = new SelectChoicesList();
 
 	static public final int SELECTSTYLE_RADIO      = 0;
 	static public final int SELECTSTYLE_COMBO      = 1;
 	static public final int SELECTSTYLE_LIST       = 2;
 	static public final int SELECTSTYLE_MULTICHECK = 3;
 	static public final int SELECTSTYLE_MULTILIST  = 4;
 	static public final int SELECTSTYLE_MULTIDUAL  = 5;
 
     private ListValueSource listSource;
 	private ListValueSource defaultValue;
 	private int style;
 	private int size = 4;
 	private int multiDualWidth = 125;
 	private String multiDualCaptionLeft = "Available";
 	private String multiDualCaptionRight = "Selected";
 	private boolean splitRadioCheck = true;
 
 	public SelectField()
 	{
 		super();
 		style = SELECTSTYLE_COMBO;
 	}
 
 	public SelectField(String aName, String aCaption, int aStyle)
 	{
 		super(aName, aCaption);
 		style = aStyle;
 	}
 
 	public SelectField(String aName, String aCaption, int aStyle, String choices)
 	{
 		super(aName, aCaption);
 		style = aStyle;
 		setChoices(choices);
 	}
 
 	public SelectField(String aName, String aCaption, int aStyle, ListValueSource ls)
 	{
 		super(aName, aCaption);
 		style = aStyle;
 		setListSource(ls);
 	}
 
 	public final boolean isMulti()
 	{
 		return (
 			style == SELECTSTYLE_MULTICHECK ||
 			style == SELECTSTYLE_MULTILIST ||
 			style == SELECTSTYLE_MULTIDUAL) ? true : false;
 	}
 
 	public final int getStyle() { return style; }
 	public void setStyle(int value) { style = value; }
 
 	public final int getSize() { return size; }
 	public void setSize(int value) { size = value; }
 
 	public final ListValueSource getDefaultListValue() { return defaultValue; }
 	public void setDefaultListValue(ListValueSource value) { defaultValue = value; }
 
 	public final void setMultiDualCaptions(String left, String right)
 	{
 		multiDualCaptionLeft = left;
 		multiDualCaptionRight = right;
 	}
 
 	public final void setMultiDualWidth(int value)
 	{
 		multiDualWidth = value;
 	}
 
 	public void setChoices(String values)
 	{
         ListValueSource vs = null;
         if(values != null)
         {
             vs = ValueSourceFactory.getListValueSource(values);
             if(vs == null)
             {
                 vs = new StringsListValue();
                 vs.initializeSource(values);
             }
         }
 
         setListSource(vs);
 	}
 
 	public final ListValueSource getListSource() { return listSource; }
 	public void setListSource(ListValueSource value) { listSource = value; }
 
 	public boolean defaultIsListValueSource()
 	{
 		return true;
 	}
 
 	public void importFromXml(Element elem)
 	{
 		super.importFromXml(elem);
 
 		String styleValue = elem.getAttribute("style");
 		if(styleValue.length() > 0)
 		{
 			if(styleValue.equalsIgnoreCase("radio"))
 				style = SelectField.SELECTSTYLE_RADIO;
 			else if (styleValue.equalsIgnoreCase("list"))
 				style = SelectField.SELECTSTYLE_LIST;
 			else if (styleValue.equalsIgnoreCase("multicheck"))
 				style = SelectField.SELECTSTYLE_MULTICHECK;
 			else if (styleValue.equalsIgnoreCase("multilist"))
 				style = SelectField.SELECTSTYLE_MULTILIST;
 			else if (styleValue.equalsIgnoreCase("multidual"))
 				style = SelectField.SELECTSTYLE_MULTIDUAL;
 			else
 				style = SelectField.SELECTSTYLE_COMBO;
 		}
 
 		if(style == SelectField.SELECTSTYLE_MULTIDUAL)
 		{
 			String value = elem.getAttribute("caption-left");
 			if(value.length() > 0)
 				multiDualCaptionLeft = value;
 
 			value = elem.getAttribute("caption-right");
 			if(value.length() > 0)
 				multiDualCaptionRight = value;
 
 			value = elem.getAttribute("multi-width");
 			if(value.length() > 0)
 				multiDualWidth = Integer.parseInt(value);
 		}
 
 		String defaultv = elem.getAttribute("default");
 		if(defaultv.length() > 0)
 		{
             if(isMulti())
     			defaultValue = ValueSourceFactory.getListValueSource(defaultv);
             else
 				super.setDefaultValue(ValueSourceFactory.getSingleOrStaticValueSource(defaultv));
 		}
 		else
 			defaultValue = null;
 
 		String choicesValue = elem.getAttribute("choices");
 		if(choicesValue.length() > 0)
 			setChoices(choicesValue);
 
 		String blank = elem.getAttribute("prepend-blank");
 		if(blank.length() > 0 && blank.equals("yes"))
 			setFlag(FLDFLAG_PREPENDBLANK);
 
 		blank = elem.getAttribute("append-blank");
 		if(blank.length() > 0 && blank.equals("yes"))
 			setFlag(FLDFLAG_APPENDBLANK);
 	}
 
 	public boolean isValid(DialogContext dc)
 	{
 		switch(style)
 		{
 			case SELECTSTYLE_COMBO:
 			case SELECTSTYLE_LIST:
 			case SELECTSTYLE_RADIO:
 				String value = dc.getValue(this);
 				if(isRequired(dc) && (value == null || value.length() == 0))
 				{
 					invalidate(dc, getCaption(dc) + " is required.");
 					return false;
 				}
 				break;
 
 			case SELECTSTYLE_MULTILIST:
 			case SELECTSTYLE_MULTICHECK:
 			case SELECTSTYLE_MULTIDUAL:
 				String[] values = dc.getValues(this);
 				if(isRequired(dc) && (values == null || values.length == 0))
 				{
 					invalidate(dc, getCaption(dc) + " is required.");
 					return false;
 				}
 				break;
 
 		}
 		return super.isValid(dc);
 	}
 
 	public void populateValue(DialogContext dc)
 	{
 		if(isMulti())
 		{
 			String[] values = dc.getValues(this);
 			if(values == null)
 				values = dc.getRequest().getParameterValues(getId());
 
 			if(dc.getRunSequence() == 1)
 			{
 				if((values != null && values.length == 0 && defaultValue != null) ||
 					(values == null && defaultValue != null))
 				{
 					SelectChoicesList list = defaultValue.getSelectChoices(dc);
 					dc.setValues(this, list.getValues());
 				}
 			}
 			else
 				dc.setValues(this, values);
 		}
 		else
 			super.populateValue(dc);
 	}
 
 	public String getMultiDualControlHtml(DialogContext dc, SelectChoicesList choices)
 	{
 		String dialogName = dc.getDialog().getName();
 
 		String width = multiDualWidth + " pt";
 		String sorted = flagIsSet(FLDFLAG_SORTCHOICES) ? "true" : "false";
 		String id = getId();
 		String name = getQualifiedName();
 		String fieldAreaFontAttrs = dc.getSkin().getControlAreaFontAttrs();
 
 		StringBuffer selectOptions = new StringBuffer();
 		StringBuffer selectOptionsSelected = new StringBuffer();
 		Iterator i = choices.getIterator();
 		while(i.hasNext())
 		{
 			SelectChoice choice = (SelectChoice) i.next();
 			if(choice.selected)
 				selectOptionsSelected.append("<option value='"+ choice.value +"'>"+ choice.caption +"</option>\n");
 			else
 				selectOptions.append("<option value='"+ choice.value +"'>"+ choice.caption +"</option>\n");
 		}
 
 		return
 			"<TABLE CELLSPACING=0 CELLPADDING=1 ALIGN=left BORDER=0>\n"+
 			"<TR>\n"+
 			"<TD ALIGN=left><FONT "+fieldAreaFontAttrs+">"+ multiDualCaptionLeft + "</FONT></TD><TD></TD>\n"+
 			"<TD ALIGN=left><FONT "+fieldAreaFontAttrs+">"+ multiDualCaptionRight + "</FONT></TD>\n"+
 			"</TR>\n"+
 			"<TR>\n"+
 			"<TD ALIGN=left VALIGN=top>\n"+
 			"	<SELECT class='dialog_control' ondblclick=\"MoveSelectItems('"+ dialogName +"', '"+ name +"_From', '"+ id +"', "+ sorted +")\" NAME='"+ name +"_From' SIZE='"+ size +"' MULTIPLE STYLE=\"width: "+ width +"\">\n"+
 			"	"+ selectOptions +"\n"+
 			"	</SELECT>\n"+
 			"</TD>\n"+
 			"<TD ALIGN=center VALIGN=middle>\n"+
 			"	&nbsp;<INPUT TYPE=button NAME=\""+ name +"_addBtn\" onClick=\"MoveSelectItems('"+ dialogName +"', '"+ name +"_From', '"+ id +"', "+ sorted +")\" VALUE=\" > \">&nbsp;<BR CLEAR=both>\n"+
 			"	&nbsp;<INPUT TYPE=button NAME=\""+ name +"_removeBtn\" onClick=\"MoveSelectItems('"+ dialogName +"', '"+ id +"', '"+ name +"_From', "+ sorted +")\" VALUE=\" < \">&nbsp;\n"+
 			"</TD>\n"+
 			"<TD ALIGN=left VALIGN=top>\n"+
 			"	<SELECT class='dialog_control' ondblclick=\"MoveSelectItems('"+ dialogName +"', '"+ id +"', '"+ name +"_From', "+ sorted +")\" NAME='"+ id +"' SIZE='"+ size +"' MULTIPLE STYLE=\"width: "+ width +"\" " + dc.getSkin().getDefaultControlAttrs() + ">\n"+
 			"	"+ selectOptionsSelected +"\n"+
 			"	</SELECT>\n"+
 			"</TD>\n"+
 			"</TR>\n"+
 			"</TABLE>";
 	}
 
 	public String getHiddenControlHtml(DialogContext dc, boolean showCaptions)
 	{
         SelectChoicesList choices = null;
 		if(listSource != null)
         {
             choices = listSource.getSelectChoices(dc);
     		choices.calcSelections(dc, this);
         }
         else
             choices = EMPTY_CHOICES;
 
 		String id = getId();
 		Iterator i = choices.getIterator();
 		StringBuffer html = new StringBuffer();
 
 		if(showCaptions)
 		{
 			while(i.hasNext())
 			{
 				SelectChoice choice = (SelectChoice) i.next();
 				if(choice.selected)
 				{
 					if(html.length() > 0)
 						html.append("<br>");
 					html.append("<input type='hidden' name='"+ id +"' value='"+ choice.value +"'><span id='"+ getQualifiedName() +"'>" + choice.caption + "</span>");
 				}
 			}
 		}
 		else
 		{
 			while(i.hasNext())
 			{
 				SelectChoice choice = (SelectChoice) i.next();
 				if(choice.selected)
 					html.append("<input type='hidden' name='"+ id +"' value='"+ choice.value +"'>");
 			}
 		}
 
 		return html.toString();
 	}
 
 	public String getControlHtml(DialogContext dc)
 	{
 		if(isInputHidden(dc))
 			return getHiddenControlHtml(dc, false);
 
 		if(isReadOnly(dc))
 			return getHiddenControlHtml(dc, true);
 
         SelectChoicesList choices = null;
 		if(listSource != null)
         {
             choices = listSource.getSelectChoices(dc);
     		choices.calcSelections(dc, this);
         }
         else
             choices = EMPTY_CHOICES;
 
 		boolean readOnly = isReadOnly(dc);
 		String id = getId();
 		String defaultControlAttrs = dc.getSkin().getDefaultControlAttrs();
 
 		StringBuffer options = new StringBuffer();
 		int itemIndex = 0;
 		Iterator i = choices.getIterator();
 		switch(style)
 		{
 			case SELECTSTYLE_RADIO:
 				if(splitRadioCheck)
 				{
 					while(i.hasNext())
 					{
 						SelectChoice choice = (SelectChoice) i.next();
 						if(options.length() > 0)
 							options.append("<br>");
 						options.append("<input type='radio' name='"+ id +"' id='"+ id + itemIndex +"' value='"+ choice.value +"' "+ (choice.selected ? "checked " : "") + defaultControlAttrs + "> <label for='"+ id + itemIndex +"'>" + choice.caption + "</label>");
 						itemIndex++;
 					}
 				}
 				else
 				{
 					while(i.hasNext())
 					{
 						SelectChoice choice = (SelectChoice) i.next();
 						options.append("<nobr><input type='radio' name='"+ id +"' id='"+ id + itemIndex +"' value='"+ choice.value +"' "+ (choice.selected ? "checked " : "") + defaultControlAttrs + "> <label for='"+ id + itemIndex +"'>" + choice.caption + "</label></nobr>&nbsp;&nbsp;");
 						itemIndex++;
 					}
 				}
 				return options.toString();
 
 			case SELECTSTYLE_MULTICHECK:
 				if(splitRadioCheck)
 				{
 					while(i.hasNext())
 					{
 						SelectChoice choice = (SelectChoice) i.next();
 						if(options.length() > 0)
 							options.append("<br>");
 						options.append("<input type='checkbox' name='"+ id +"' id='"+ id + itemIndex +"' value='"+ choice.value +"' "+ (choice.selected ? "checked " : "") + defaultControlAttrs + "> <label for='"+ id + itemIndex +"'>" + choice.caption + "</label>");
 						itemIndex++;
 					}
 				}
 				else
 				{
 					while(i.hasNext())
 					{
 						SelectChoice choice = (SelectChoice) i.next();
 						options.append("<nobr><input type='checkbox' name='"+ id +"' id='"+ id + itemIndex +"' value='"+ choice.value +"' "+ (choice.selected ? "checked " : "") + defaultControlAttrs + "> <label for='"+ id + itemIndex +"'>" + choice.caption + "</label></nobr>&nbsp;&nbsp;");
 						itemIndex++;
 					}
 				}
 				return options.toString();
 
 			case SELECTSTYLE_COMBO:
 			case SELECTSTYLE_LIST:
 			case SELECTSTYLE_MULTILIST:
 				if(readOnly)
 				{
 					while(i.hasNext())
 					{
 						SelectChoice choice = (SelectChoice) i.next();
 						if(choice.selected)
 						{
 							if(options.length() > 0)
 								options.append(", ");
 							options.append("<input type='hidden' name='"+ id +"' value='"+ choice.value +"'>");
 							options.append(choice.caption);
 						}
 					}
 					return options.toString();
 				}
 				else
 				{
 					boolean prependBlank = false;
 					boolean appendBlank = false;
 
 					if(style == SELECTSTYLE_COMBO || style == SELECTSTYLE_LIST)
 					{
 						prependBlank = flagIsSet(FLDFLAG_PREPENDBLANK);
 						appendBlank = flagIsSet(FLDFLAG_APPENDBLANK);
 					}
 
 					if(prependBlank)
 					    options.append("<option value=''></option>");
 
 					while(i.hasNext())
 					{
 						SelectChoice choice = (SelectChoice) i.next();
 						options.append("<option value='"+ choice.value +"' "+ (choice.selected ? "selected" : "") + ">"+ choice.caption +"</option>");
 					}
 
 					if(appendBlank)
 					    options.append("<option value=''></option>");
 
 					switch(style)
 					{
 						case SELECTSTYLE_COMBO:
							return "<select name='"+ id +"'" + defaultControlAttrs + ">" + options + "</select>";
 
 						case SELECTSTYLE_LIST:
							return "<select name='"+ id +"' size='"+ size +"'" + defaultControlAttrs + ">" + options + "</select>";
 
 						case SELECTSTYLE_MULTILIST:
 							return "<select name='"+ id +"' size='"+ size +"' multiple='yes' " + defaultControlAttrs + ">" + options + "</select>";
 					}
 				}
 
 			case SELECTSTYLE_MULTIDUAL:
 				return getMultiDualControlHtml(dc, choices);
 
 			default:
 				return "Unknown style " + style;
 		}
 	}
 }
