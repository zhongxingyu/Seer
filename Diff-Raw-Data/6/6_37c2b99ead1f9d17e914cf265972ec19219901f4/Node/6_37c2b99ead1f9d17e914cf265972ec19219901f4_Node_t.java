 import java.lang.*;
 import java.util.*;
 import java.io.*;
 import java.text.Format;
 
 
 public class Node  {
 	public static final int UNKNOWN = 0;
 	public static final int NOTHING=1;	// do nothing
 	public static final int RADIO = 2;
 	public static final int CHECK = 3;
 	public static final int COMBO = 4;
 	public static final int LIST = 5;	// combo of size=(min (6,#lines))
 	public static final int TEXT = 6;
 	public static final int DOUBLE = 7;
 	public static final int RADIO_HORIZONTAL = 8;	// different layout
 	public static final int PASSWORD = 9;
 	public static final int MEMO = 10;
 	public static final int DATE = 11;
 	public static final int TIME = 12;
 	public static final int YEAR = 13;
 	public static final int MONTH = 14;
 	public static final int DAY = 15;
 	public static final int WEEKDAY = 16;
 	public static final int HOUR = 17;
 	public static final int MINUTE = 18;
 	public static final int SECOND = 19;
 	public static final int MONTH_NUM = 20;
 	
 	private static final String QUESTION_TYPES[] = {
 		"*unknown*", "nothing", "radio", "check", "combo", "list",
 		"text", "double", "radio2", "password","memo", 
 		"date", "time", "year", "month", "day", "weekday", "hour", "minute", "second", "month_num"};
 	private static final int DATA_TYPES[] = { 
 		Datum.STRING, Datum.NA, Datum.STRING, Datum.STRING, Datum.STRING, Datum.STRING,
 		Datum.STRING, Datum.NUMBER, Datum.STRING, Datum.STRING, Datum.STRING,
 		Datum.DATE, Datum.TIME, Datum.YEAR, Datum.MONTH, Datum.DAY, Datum.WEEKDAY, Datum.HOUR, Datum.MINUTE, Datum.SECOND, Datum.MONTH_NUM};
 
 	public static final int QUESTION = 1;
 	public static final int EVAL = 2;
 	public static final int GROUP_OPEN = 3;
 	public static final int GROUP_CLOSE = 4;
 	public static final int BRACE_OPEN = 5;
 	public static final int BRACE_CLOSE = 6;
 	public static final int CALL_SCHEDULE = 7;
 	public static final String ACTION_TYPE_NAMES[] = {"*unknown*","question", "expression", "group_open", "group_close", "brace_open", "brace_close", "call_schedule"};
 	public static final String ACTION_TYPES[] = {"?","q","e","[","]", "{", "}", "call" };
 
 	public static final int PARSE_NEITHER = 0;
 	public static final int PARSE_MIN = 1;
 	public static final int PARSE_MAX = 2;
 	public static final int PARSE_MIN_AND_MAX = 3;
 	public static final String PARSE_RANGE_TYPES[] = { "", "m", "M", "B" };
 	public static final String PARSE_RANGE_TYPE_STRS[] = { "", "parse min value", "parse max value", "parse both min and max values" };
 
 	private static final int MAX_TEXT_LEN_FOR_COMBO = 60;
 	private static final int MAX_ITEMS_IN_LIST = 20;
 	private static boolean AUTOGEN_OPTION_ACCELERATOR = true;
 	
 
 	private String concept = "";
 	private String description = "";
 	private int sourceLine = 0;
 	private String sourceFile = "";
 	private String stepName = "";
 	private String dependencies = "";
 	private String questionRef = ""; // name within DISC
 	private int actionType = UNKNOWN;
 	private String actionTypeField = "";	// actionType;datumType;parseRangeType;min;max;mask
 	private String action = "";
 	private int answerType = UNKNOWN;
 	private int datumType = Datum.INVALID;
 	private String answerOptions = "";
 	private Vector answerChoices = new Vector();
 	private Hashtable answerChoicesHash = new Hashtable();
 	private Vector runtimeErrors = new Vector();
 	private Vector parseErrors = new Vector();
 
 	private String actionTypeStr = "";
 	private String datumTypeStr = "";
 	private String minStr = null;
 	private String maxStr = null;
 	private String maskStr = null;
 	private String parseRangeTypeStr = "";
 	private int parseRangeType = PARSE_NEITHER;
 
 	private Datum minDatum = null;
 	private Datum maxDatum = null;
 	private String rangeStr = null;
 
 	private Format mask = null;
 	private String exampleFormatStr = null;
 
 	// loading from extended Schedule with default answers
 	// XXX hack - Node shouldn't know values of evidence - Schedule should know how to load itself.
 	private String defaultAnswer = null;
 	private String defaultAnswerTimeStampStr = null;
 	private String questionAsAsked = "";
 	private Date timeStamp = null;
 	private String timeStampStr = null;
 
 	public Node(int sourceLine, String sourceFile, String tsv) {
 		String token;
 		int field = 0;
 
 		this.sourceLine = sourceLine;
 		this.sourceFile = sourceFile;
 
 		StringTokenizer ans = new StringTokenizer(tsv,"\t",true);
 
 		while(ans.hasMoreTokens()) {
 			String s = null;
 			try {
 				s = ans.nextToken();
 			}
 			catch (Exception e) {
 				setParseError("nextToken: " + e.getMessage());
 			}
 
 			if (s.equals("\t")) {
 				++field;
 				continue;
 			}
 
 			switch(field) {
 				case 0: concept = Node.fixExcelisms(s); break;
 				case 1: description = Node.fixExcelisms(s); break;
 				case 2: stepName = Node.fixExcelisms(s); break;
 				case 3: dependencies= Node.fixExcelisms(s); break;
 				case 4: questionRef = Node.fixExcelisms(s); break;
 				case 5: actionTypeField = Node.fixExcelisms(s); break;
 				case 6: action = Node.fixExcelisms(s); break;
 				case 7: answerOptions = Node.fixExcelisms(s); break;
 				case 8: questionAsAsked = Node.fixExcelisms(s); break;
 				case 9: defaultAnswer = Node.fixExcelisms(s); break;
 				case 10: defaultAnswerTimeStampStr = Node.fixExcelisms(s); break;
 				default:	break;	// discard any extras
 			}
 		}
 		/*
 		if (field < 6 || field > 10) {
 			setParseError("Expected 8-11 tokens; found " + (field + 1));
 		}
 		*/
 
 		/* Fix step names */
 		try {
 			if (Character.isDigit(stepName.charAt(0))) {
 				stepName = "_" + stepName;
 			}
 		}
 		catch (IndexOutOfBoundsException e) {
 			/* Must have a null or zero-length value for stepName */
 		}
 
 		parseActionTypeField();
 		parseAnswerOptions();
 		processFormattingMask();
 		parseRange();
 		createParseRangeStr();
 	}
 
 	public static String fixExcelisms(String s) {
 		/* Fix Excel-isms, in which strings with internal quotes have all quotes replaced with double quotes (\"\"), and
 			whole string surrounded by quotes.
 			XXX - this requires assumption that if a field starts AND stops with a quote, then probably an excel-ism
 		*/
 
 		if (s.startsWith("\"") && s.endsWith("\"")) {
 			StringBuffer sb = new StringBuffer();
 
 			int start=1;
 			int stop=0;
 			while ((stop = s.indexOf("\"\"",start)) != -1) {
 				sb.append(s.substring(start,stop));
 				sb.append("\"");
 				start = stop+2;
 			}
 			sb.append(s.substring(start,s.length()-1));
 			return sb.toString();
 		}
 		else {
 			return s;
 		}
 	}
 
 	private void parseActionTypeField() {
 		StringTokenizer ans;
 		int z;
 
 		ans = new StringTokenizer(actionTypeField,";",true);	// return ';' tokens too
 
 		for(int field=0;ans.hasMoreTokens();) {
 			String s = null;
 			try {
 				s = ans.nextToken();
 			}
 			catch (Exception e) {}
 
 			if (";".equals(s)) {
 				++field;
 				continue;
 			}
 			switch(field) {
 				case 0:	
 					actionTypeStr = s; 
 					for (z=0;z<ACTION_TYPES.length;++z) {
 						if (actionTypeStr.equalsIgnoreCase(ACTION_TYPES[z])) {
 							actionType = z;
 							break;
 						}
 					}
 					if (z == ACTION_TYPES.length) {
 						setParseError("Unknown action type <B>" + Node.encodeHTML(actionTypeStr) + "</B>");
 					}
 					break;
 				case 1: 
 					datumTypeStr = s;
 					for (z=0;z<Datum.TYPES.length;++z) {
 						if (datumTypeStr.equalsIgnoreCase(Datum.TYPES[z])) {
 							datumType = z;
 							break;
 						}
 					}
 					if (z == Datum.TYPES.length) {
 						setParseError("Unknown datum type <B>" + Node.encodeHTML(datumTypeStr) + "</B>");
 					}		
 					break;
 				case 2: 
 					parseRangeTypeStr = s;
 					for (z=0;z<PARSE_RANGE_TYPES.length;++z) {
 						if (parseRangeTypeStr.equals(PARSE_RANGE_TYPES[z])) {
 							parseRangeType = z;
 							break;
 						}
 					}
 					if (z == PARSE_RANGE_TYPES.length) {
 						setParseError("Unknown parse range type <B>" + Node.encodeHTML(parseRangeTypeStr) + "</B>");
 					}		
 					break;
 				case 3: 
 					minStr = s; 
 					break;
 				case 4: 
 					maxStr = s; 
 					break;
 				case 5: 
 					maskStr = s; 
 					break;
 			}
 		}
 	}
 
 	private void processFormattingMask() {
 		if (maskStr == null || maskStr.trim().equals("")) {
 			mask = Datum.getDefaultMask(datumType);
 			/* this is allowed to be null - means nothing is done with it */
 		}
 		else {
 			mask = Datum.buildMask(maskStr, datumType);
 			/* if mask is null here, it means that the maskStr is invalid */
 			if (mask == null) {
 				setParseError("Invalid formatting mask <B>" + maskStr + "</B>");
 				mask = Datum.getDefaultMask(datumType);	// set  to default to avoid NullPointerException
 			}
 		}
 		String s = Datum.getExampleFormatStr(mask, datumType);
 		if (s.equals(""))
 			exampleFormatStr = "";
 		else
 			exampleFormatStr = " (e.g. " + s + ")";
 	}
 
 	private void parseRange() {
 		if (parseRangeType == PARSE_MIN || parseRangeType == PARSE_MIN_AND_MAX) {
 			if (minStr == null) {
 				setParseError("Discrepency:  requested <B>" + PARSE_RANGE_TYPE_STRS[parseRangeType] + "</B>, but no min value specified");
 				// change boundary conditions?
 			}
 			else {
 				/* check to see whether value exists? Too highly intertwined.  */
 			}
 		}
 		else {
 			if (minStr == null) {
 				minDatum = null;	// no minumum bound
 			}
 			else {
 				minDatum = new Datum(minStr,datumType,mask);
 				if (!minDatum.isValid()) {
 					setParseError("Invalid value <B>" + minStr + "</B> for formatter <B>" + mask + "</B>");
 					minDatum = null;	// if unknown, don't use a minimum bound
 				}
 			}
 		}
 		if (parseRangeType == PARSE_MAX || parseRangeType == PARSE_MIN_AND_MAX) {
 			if (maxStr == null) {
 				setParseError("Discrepency:  requested <B>" + PARSE_RANGE_TYPE_STRS[parseRangeType] + "</B>, but no max value specified");
 				// change boundary conditions?
 			}
 			else {
 				/* To check this, need access to Triceps' parser!  Highly intertwined. */
 			}
 		}
 		else {
 			if (maxStr == null) {
 				maxDatum = null;	// no maximum bound
 			}
 			else {
 				maxDatum = new Datum(maxStr,datumType,mask);
 				if (!maxDatum.isValid()) {
 					setParseError("Invalid value <B>" + maxStr + "</B> for formatter <B>" + mask + "</B>");
 					maxDatum = null;	// if unknown, don't use maximum bound
 				}
 			}
 		}
 		if (minDatum != null && maxDatum != null) {
 			if (DatumMath.lt(maxDatum,minDatum).booleanVal()) {
 				setParseError("Max value (" + maxStr + ") less than Min value (" + minStr + ")");
 			}
 		}
 	}
 
 	public void createParseRangeStr() {
 		/* Create the help-string showing allowable range of input values.
 			Can be re-created (e.g. if range dynamically changes */
 
 		String min = null;
 		String max = null;
 
 		if ((minStr == null && maxStr == null) || answerType == PASSWORD) {
 			rangeStr = null;
 			return;
 		}
 
 		if (mask == null) {
 			/* This only applies to non-DATE and non-NUMBER values */
 			min = minStr;
 			max = maxStr;
 		}
 		else {
 			/* Show the range of valid values, in the appropriate format */
 			
 			if (minStr != null) {
 				if (minDatum == null || !minDatum.isValid()) {
 					min = null;
 				}
 				else {
 					min = Datum.format(minDatum,mask);
 				}
 			}
 			if (maxStr != null) {
 				if (maxDatum == null || !maxDatum.isValid()) {
 					max = null;
 				}
 				else {
 					max = Datum.format(maxDatum,mask);
 				}
 			}
 		}
 		if (minDatum != null && maxDatum != null) {
 			if (DatumMath.lt(maxDatum,minDatum).booleanVal()) {
 				setError("Max value (" + max + ") less than Min value (" + min + ")");
 			}
 		}
 
 		rangeStr = " (" +
 			((min != null) ? min : "") +
 			" - " +
 			((max != null) ? max : "") +
 			")";
 	}
 
 	private boolean parseAnswerOptions() {
 		StringTokenizer ans = new StringTokenizer(answerOptions,";",true);	// return ';' tokens too
 		String token = "";
 
 		try {
 			token = ans.nextToken();
 		}
 		catch (Exception e) {}
 
 		for (int z=0;z<QUESTION_TYPES.length;++z) {
 			if (token.equalsIgnoreCase(QUESTION_TYPES[z])) {
 				answerType = z;
 				break;
 			}
 		}
 
 		if (actionType == EVAL) {
 			answerType = NOTHING;	// so no further processing
 			datumType = Datum.STRING;
 			return true;
 		}
 		else if (answerType == UNKNOWN) {
 			setParseError("Unknown data type for answer <B>" + Node.encodeHTML(token) + "</B>");
 			answerType = NOTHING;
 		}
 
 		if (datumType == Datum.INVALID) {
 			/* so only if not set via datumTypeStr */
 			datumType = DATA_TYPES[answerType];
 		}
 
 		switch (answerType) {
 			case CHECK:
 			case COMBO:
 			case LIST:
 			case RADIO:
 			case RADIO_HORIZONTAL:
 				String val=null;
 				String msg=null;
 				int field=0;
 
 				while(ans.hasMoreTokens()) {
 					String s = null;
 					try {
 						s = ans.nextToken();
 					}
 					catch (Exception e) {}
 
 					if (";".equals(s)) {
 						++field;
 						continue;
 					}
 					switch(field) {
 						case 0:
 							break;	// discard the first token - answerType
 						case 1:
 							val = s;
 							break;
 						case 2: msg = s;
 							field = 0;	// so that cycle between val & mag;
 							if (val == null || msg == null) {
 								setParseError("Answer choice has null value or message");
 							}
 							else {
 								AnswerChoice ac = new AnswerChoice(val,msg);
 								answerChoices.addElement(ac);
 								
 								/* check for duplicate answer choice values */
 								if (answerChoicesHash.put(val, ac) != null) {
 									setParseError("Answer value <B>" + val + "</B> already used");
 								}
 							}
 							val = null;
 							msg = null;
 							break;
 					}
 				}
 				if (answerChoices.size() == 0) {
 					setParseError("No answer choices specified");
 				}
 				else if (field == 1) {
 					setParseError("Missing message for value " + val);
 				}
 				break;
 			default:
 				break;
 		}
 
 		return true;
 	}
 	
 	public String prepareChoicesAsHTML(Datum datum) {
 		return prepareChoicesAsHTML(datum,"");
 	}
 
 	public String prepareChoicesAsHTML(Datum datum, String errMsg) {
 		/* errMsg is a hack - only applies to RADIO_HORIZONTAL */
 		StringBuffer sb = new StringBuffer();
 		String defaultValue = "";
 		AnswerChoice ac;
 		Enumeration ans = answerChoices.elements();
 
 		try {
 			switch (answerType) {
 			case RADIO:	// will store integers
 				while (ans.hasMoreElements()) { // for however many radio buttons there are
 					ac = (AnswerChoice) ans.nextElement();
 					sb.append("<input type='radio' name='" + Node.encodeHTML(getName()) + "' " + "value='" + Node.encodeHTML(ac.getValue()) + "'" +
 						(DatumMath.eq(datum,new Datum(ac.getValue(),DATA_TYPES[answerType])).booleanVal() ? " CHECKED" : "") + ">" + Node.encodeHTML(ac.getMessage()) + "<br>");
 				}
 				break;
 			case RADIO_HORIZONTAL: // will store integers
 				/* table underneath questions */
 				int count = answerChoices.size();
 
 				if (count > 0) {
 					Double pct = new Double(100. / (double) count);
 					sb.append("<TD COLSPAN='2' BGCOLOR='lightgrey'>");
 					sb.append("\n<TABLE CELLPADDING='0' CELLSPACING='2' BORDER='1' WIDTH='100%'>");
 					sb.append("\n<TR>");
 					while (ans.hasMoreElements()) { // for however many radio buttons there are
 						ac = (AnswerChoice) ans.nextElement();
 						sb.append("\n<TD VALIGN='top' WIDTH='" + pct.toString() + "%'>");
 						sb.append("<input type='radio' name='" + Node.encodeHTML(getName()) + "' " + "value='" + Node.encodeHTML(ac.getValue()) + "'" +
 							(DatumMath.eq(datum,new Datum(ac.getValue(),DATA_TYPES[answerType])).booleanVal() ? " CHECKED" : "") + ">" + Node.encodeHTML(ac.getMessage()));
 						sb.append("</TD>");
 					}
 				}
 				sb.append("\n</TR>");
 				sb.append("\n</TABLE>\n");
 				/* XXX: add Node errors here - a kludge */
 				sb.append(errMsg);
 				sb.append("</TD>");
 //				sb.append("</TR>");	// closing the outside is reserverd for TricepsServlet
 				break;
 			case CHECK:
 				while (ans.hasMoreElements()) { // for however many radio buttons there are
 					ac = (AnswerChoice) ans.nextElement();
 					sb.append("<input type='checkbox' name='" + Node.encodeHTML(getName()) + "' " + "value='" + Node.encodeHTML(ac.getValue()) + "'" +
 						(DatumMath.eq(datum, new Datum(ac.getValue(),DATA_TYPES[answerType])).booleanVal() ? " CHECKED" : "") + ">" + Node.encodeHTML(ac.getMessage()) + "<br>");
 				}
 				break;
 			case COMBO:	// stores integers as value
 			case LIST: {
 				StringBuffer choices = new StringBuffer();
 				
 				int optionNum=0;
 				int totalLines = 0;
 				boolean nothingSelected = true;
 				while (ans.hasMoreElements()) { // for however many radio buttons there are
 					ac = (AnswerChoice) ans.nextElement();
 					++optionNum;
 
 					String option = ac.getMessage();
 					String prefix = "<option value='" + Node.encodeHTML(ac.getValue()) + "'";
 					boolean selected = DatumMath.eq(datum, new Datum(ac.getValue(),DATA_TYPES[answerType])).booleanVal();
 					int stop;
 					int line=0;
 
 					while (option.length() > 0) {
 						if (option.length() < MAX_TEXT_LEN_FOR_COMBO) {
 							stop = option.length();
 						}
 						else {
 							stop = option.lastIndexOf(' ',MAX_TEXT_LEN_FOR_COMBO);
 							if (stop <= 0) {
 								stop = MAX_TEXT_LEN_FOR_COMBO;	// if no extra space, take entire string
 							}
 						}
 
 						choices.append(prefix);
 						if (line++ == 0) {
 							if (selected) {
 								choices.append(" SELECTED>" + 
 									((AUTOGEN_OPTION_ACCELERATOR) ? String.valueOf(optionNum) : Node.encodeHTML(ac.getValue())) + 
 									")&nbsp;");
 								nothingSelected = false;
 							}
 							else {
 								choices.append(">" + 
 									((AUTOGEN_OPTION_ACCELERATOR) ? String.valueOf(optionNum) : Node.encodeHTML(ac.getValue())) + 
 									")&nbsp;");
 							}
 						}
 						else {
 							choices.append(">&nbsp;&nbsp;&nbsp;");
 						}
 						choices.append(Node.encodeHTML(option.substring(0,stop)));
 
 						if (stop<option.length())
 							option = option.substring(stop+1,option.length());
 						else
 							option = "";
 					}
 					totalLines += line;
 
 					choices.append("</option>");
 				}
 				sb.append("\n<select name='" + Node.encodeHTML(getName()) + "'" +
 					((answerType == LIST) ? " size = '" + Math.min(MAX_ITEMS_IN_LIST,totalLines+1) + "'" : "") +
 					">");
 				sb.append("\n<option value=''" + 
 					((nothingSelected) ? " SELECTED" : "") +	// so that focus is properly shifted on List box
 					">--select one of the following--");	// first choice is empty				
 				sb.append(choices);
 				sb.append("</select>");
 			}
 				break;
 			case TEXT:	// stores Text type
 				if (datum != null && datum.exists())
 					defaultValue = datum.stringVal();
 				sb.append("<input type='text' onfocus='javascript:select()' name='" + Node.encodeHTML(getName()) + "' value='" + Node.encodeHTML(defaultValue) + "'>");
 				break;
 			case MEMO:
 				if (datum != null && datum.exists())
 					defaultValue = datum.stringVal();
 				sb.append("<TEXTAREA rows='5' onfocus='javascript:select()' name='" + Node.encodeHTML(getName()) + "'>" + Node.encodeHTML(defaultValue) + "</TEXTAREA>");
 				break;
 			case PASSWORD:	// stores Text type
 				if (datum != null && datum.exists())
 					defaultValue = datum.stringVal();
 				sb.append("<input type='password' onfocus='javascript:select()' name='" + Node.encodeHTML(getName()) + "'>");
 				break;
 			case DOUBLE:	// stores Double type
 				if (datum != null && datum.exists())
 					defaultValue = datum.stringVal();
 				sb.append("<input type='text' onfocus='javascript:select()' name='" + Node.encodeHTML(getName()) + "' value='" + Node.encodeHTML(defaultValue) + "'>");
 				break;
 			default:
 /*			
 			case DATE:
 			case TIME:
 			case YEAR:
 			case MONTH:
 			case DAY:
 			case WEEKDAY:
 			case HOUR:
 			case MINUTE:
 			case SECOND:
 			case MONTH_NUM:
 */
 				if (datum != null && datum.exists())
 					defaultValue = datum.stringVal();
 				sb.append("<input type='text' onfocus='javascript:select()' name='" + Node.encodeHTML(getName()) + "' value='" + Node.encodeHTML(defaultValue) + "'>");
 				break;
 			case NOTHING:
 				sb.append("&nbsp;");
 				break;
 			}
 		}
 		catch (Throwable t) {
 			setError("Internal error: " + Node.encodeHTML(t.getMessage()));
 			return "";
 		}
 
 		return sb.toString();
 	}
 
 	public boolean isWithinRange(Datum d) {
 		boolean err = false;
 		
 /*
 		System.out.println("(" + minStr + "|" + ((minDatum != null) ? Datum.format(minDatum,mask) : "") +
 			"," + d.stringVal() +
 			"," + maxStr + "|" + ((maxDatum != null) ? Datum.format(maxDatum,mask) : "") + ")");
 */			
 
 		if (minDatum != null) {
 			if (!DatumMath.ge(d,minDatum).booleanVal())
 				err = true;
 		}
 		if (maxDatum != null) {
 			if (!DatumMath.le(d,maxDatum).booleanVal())
 				err = true;
 		}
 		
 		if (err) {
 			if (answerType == PASSWORD) {
 				setError("Incorrect password.  Please try again.");
 			}
 			else {
 				setError("Please enter a " + Datum.TYPES[datumType] + " in the range:" + rangeStr);
 			}
 		}
 		return !(err);
 	}
 
 	public String getAction() { return action; }
 	public int getActionType() { return actionType; }
 	public String getActionTypeField() { return actionTypeField; }
 	public String getAnswerOptions() { return answerOptions; }
 	public int getAnswerType() { return answerType; }
 	public int getDatumType() { return datumType; }
 	public String getConcept() { return concept; }
 	public String getDependencies() { return dependencies; }
 	public String getDescription() { return description; }
 	public String getName() { return stepName; }
 	public String getQuestionRef() { return questionRef; }
 	public String getDefaultAnswer() { return defaultAnswer; }
 	public String getDefaultAnswerTimeStampStr() { return defaultAnswerTimeStampStr; }
 	public String getQuestionMask() {
 		if (rangeStr != null)
 			return rangeStr;
 		else
 			return exampleFormatStr;
 	 }
 	public int getSourceLine() { return sourceLine; }
 	public String getSourceFile() { return sourceFile; }
 	public String getQuestionAsAsked() { return questionAsAsked; }
 	public void setQuestionAsAsked(String s) { questionAsAsked = s; }
 	public String getMaskStr() { return maskStr; }
 	public Format getMask() { return mask; }
 	public int getParseRangeType() { return parseRangeType; }
 	public void setMinDatum(Datum d) { minDatum = d; }
 	public void setMaxDatum(Datum d) { maxDatum = d; }
 	public String getMinStr() { return minStr; }
 	public String getMaxStr() { return maxStr; }
 
 	public boolean focusable() { return (answerType != UNKNOWN && answerType != NOTHING); }
 	public boolean focusableArray() { return (answerType == RADIO || answerType == RADIO_HORIZONTAL || answerType == CHECK); }
 	
 
 	public void setParseError(String error) {
 		parseErrors.addElement(error);
 
 	}
 	public void setError(String error) {
 		runtimeErrors.addElement(error);
 	}
 
 	public Vector getErrors() {
 		Vector errs = new Vector();
 		for (int j=0;j<parseErrors.size();++j) { errs.addElement(parseErrors.elementAt(j)); }
 		for (int j=0;j<runtimeErrors.size();++j) { errs.addElement(runtimeErrors.elementAt(j)); }
 		runtimeErrors = new Vector();	// clear the runtime errors;
 		return errs;
 	}
 
 	public Vector getRuntimeErrors() {
 		Vector errs = runtimeErrors;
 		runtimeErrors = new Vector();	// clear them.
 		return errs;
 	}
 
 
 	public boolean hasErrors() { return ((runtimeErrors.size() + parseErrors.size()) > 0); }
 	public boolean hasRuntimeErrors() { return (runtimeErrors.size() > 0); }
 
 
 	/**
 	 * Prints out the components of a node in the schedule.
 	 */
 	public String toString() {
 		return "Node (" + sourceLine + "): <B>" + Node.encodeHTML(stepName) + "</B><BR>\n" + "Concept: <B>" + Node.encodeHTML(concept) + "</B><BR>\n" +
 			"Description: <B>" + Node.encodeHTML(description) + "</B><BR>\n" + "Dependencies: <B>" + Node.encodeHTML(dependencies) + "</B><BR>\n" +
 			"Question Reference: <B>" + Node.encodeHTML(questionRef) + "</B><BR>\n" + "Action Type: <B>" + Node.encodeHTML(actionTypeStr) + "</B><BR>\n" +
 			"Action: <B>" + Node.encodeHTML(action) + "</B><BR>\n" + "AnswerType: <B>" + Node.encodeHTML(QUESTION_TYPES[answerType]) + "</B><BR>\n" + "AnswerOptions: <B>" +
 			Node.encodeHTML(answerOptions) + "</B><BR>\n";
 	}
 
 	public String toTSV() {
 		return concept + "\t" + description + "\t" + stepName + "\t" + dependencies + "\t" + questionRef +
 			"\t" + actionTypeField + "\t" + action + "\t" + answerOptions;
 	}
 
 
 	static public String encodeHTML(String s, boolean disallowEmpty) {
 		/* Limited HTML tagging supported:
 			<B></B>
 			<I></I>
 			<U></U>
 			<BR>
 		*/
 		StringBuffer dst = new StringBuffer();
 		
 		try {
			char[] src = s.toCharArray();
			
 			for (int i=0;i<src.length;++i) {
 				switch (src[i]) {
 					case '\'': dst.append("&#39;"); break;
 					case '\"': dst.append("&#34;"); break;
 					case '<': 
 						if (((i+2) < src.length) && src[i+2] == '>') {
 							switch(src[i+1]) {
 								case 'B': case 'b': dst.append("<B>"); i+=2; break;
 								case 'I': case 'i': dst.append("<I>"); i+=2; break;
 								case 'U': case 'u': dst.append("<U>"); i+=2; break;
 								default: dst.append("&#60;"); break;
 							}
 						}
 						else if (((i+3) < src.length) && src[i+1] == '/' && src[i+3] == '>') {
 							switch(src[i+2]) {
 								case 'B': case 'b': dst.append("</B>"); i+=3; break;
 								case 'I': case 'i': dst.append("</I>"); i+=3; break;
 								case 'U': case 'u': dst.append("</U>"); i+=3; break;
 								default: dst.append("&#60;"); break;
 							}
 						}						
 						else if (((i+3) < src.length) && src[i+3] == '>') {
 							if ((src[i+1] == 'b' || src[i+1] == 'B') &&
 									(src[i+2] == 'r' || src[i+2] == 'R')) {
 								dst.append("<BR>");
 								i+=3;
 							}
 							else {
 								dst.append("&#60;");
 							}
 						}
 						else {
 							dst.append("&#60;");
 						}
 						break;
 					case '>': dst.append("&#62;"); break;
 					case '&': dst.append("&#38;"); break;
 					default: dst.append(src[i]); break;
 				}
 			}
 		}
 		catch (Throwable t) {
 			/* ArrayIndexOutOfBounds */
 		}
 		finally {
 			String ans = dst.toString();
 			if (disallowEmpty && ans.length() == 0) {
 				return "&nbsp;";
 			}
 			else {
 				return ans;
 			}
 		}
 	}
 
 	static public String encodeHTML(String s) {
 		return encodeHTML(s,false);
 	}
 	
 		
 	public String getTimeStampStr() { return timeStampStr; }
 	public Date getTimeStamp() { return timeStamp; }
 		
 		
 	public void setTimeStamp() {
 		timeStamp = new Date(System.currentTimeMillis());
 		timeStampStr = Datum.format(timeStamp,Datum.DATE,Datum.TIME_MASK);
 	}
 		
 	public void setTimeStamp(String t) {
 		if (t == null) {
 			setTimeStamp();
 			return;
 		}
 			
 		Date time = null;
 		try {
 			time = Datum.TIME_MASK.parse(t);
 		}
 		catch (java.text.ParseException e) {
 			setParseError("Error parsing time " + e.getMessage());
			System.out.println("error parsing time " + e.getMessage());
 		}
 		if (time == null) {
 			setTimeStamp();
 		}
 		else {
 			timeStamp = time;
 			timeStampStr = t;
 		}
 	}
 	
 	public static void setAutoGenOptionAccelerator(boolean t) { AUTOGEN_OPTION_ACCELERATOR = t; }
 }
