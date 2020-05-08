 import java.lang.*;
 import java.util.*;
 import java.io.*;
 import java.text.Format;
 
 
 public class Node  {
 	public static final int BADTYPE = 0;
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
 		"*badtype*", "nothing", "radio", "check", "combo", "list",
 		"text", "double", "radio2", "password","memo",
 		"date", "time", "year", "month", "day", "weekday", "hour", "minute", "second", "month_num"};
 	private static final int DATA_TYPES[] = {
 		Datum.STRING, Datum.STRING, Datum.STRING, Datum.STRING, Datum.STRING, Datum.STRING,
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
 
 	private static final int MAX_TEXT_LEN_FOR_COMBO = 60;
 	private static final int MAX_ITEMS_IN_LIST = 20;
 
 
 	/* These are the columns in the flat file database */
 	private String conceptName = "";
 	private String localName = "";
 	private String externalName = ""; // name within DISC
 	private String dependencies = "";
 
 	private Vector readback = new Vector();
 	private Vector questionOrEval = new Vector();
 	private Vector answerChoicesStr = new Vector();
 	private Vector answerChoicesVector = new Vector();	// of Vectors
 	private Vector helpURL = new Vector();
 
 	private int numLanguages = 0;
 	private int answerLanguageNum = 0;
 	private String questionAsAsked = "";
 	private String answerGiven = "";
 	private String answerTimeStampStr = "";
 	private String comment = "";
 
 	private Hashtable answerChoicesHash = new Hashtable();
 
 	/* These are local variables */
 
 	private int sourceLine = 0;
 	private String sourceFile = "";
 	private int questionOrEvalType = BADTYPE;
 	private String questionOrEvalTypeField = "";	// questionOrEvalType;datumType;min;max;mask
 	private int answerType = BADTYPE;
 	private int datumType = Datum.INVALID;
 	private Vector runtimeErrors = new Vector();
 	private Vector parseErrors = new Vector();
 
 	private String questionOrEvalTypeStr = "";
 	private String datumTypeStr = "";
 	private String minStr = null;
 	private String maxStr = null;
 	private String maskStr = null;
 
 	private Datum minDatum = null;
 	private Datum maxDatum = null;
 	private String rangeStr = null;
 
 	private Format mask = null;
 	private String exampleFormatStr = null;
 
 	private Date timeStamp = null;
 	private String timeStampStr = null;
 
 	public Node(int sourceLine, String sourceFile, String tsv, int numLanguages) {
 	try {
 		String token;
 		int field = 0;
 
 		if (numLanguages < 1) {
 			setParseError("Invalid language number (" + numLanguages + "): must be >= 1");
 			numLanguages = 1;	// the default
 		}
 
 		this.sourceLine = sourceLine;
 		this.sourceFile = sourceFile;
 		this.numLanguages = numLanguages;	// needs to be validated?
 
 		int numLanguagesFound = 0;
 		int numAnswersFound = 0;
 
 		StringTokenizer ans = new StringTokenizer(tsv,"\t",true);
 
 		while(ans.hasMoreTokens()) {
 			String s = null;
 			try {
 				s = ans.nextToken();
 			}
 			catch (Throwable t) {
 				setParseError("tokenization error: " + t.getMessage());
 				System.err.println("tokenization error: " + t.getMessage());
 			}
 
 			if (s.equals("\t")) {
 				++field;
 				if (field == 7) {
 					++numLanguagesFound;	// since once that field has been entered, has successfully coded a language as present
 				}
 				if (field == 9 && numLanguagesFound < numLanguages) {
 					field = 5;	// so that next element is readback for the next language
 				}
 				continue;
 			}
 
 			switch(field) {
 				/* there should be one copy of each of these */
 				case 0: conceptName = Node.fixExcelisms(s); break;
 				case 1: localName = Node.fixExcelisms(s); break;
 				case 2: externalName = Node.fixExcelisms(s); break;
 				case 3: dependencies= Node.fixExcelisms(s); break;
 				case 4: questionOrEvalTypeField = Node.fixExcelisms(s); break;
 				/* there are as many copies of each of these are there are languages */
 				case 5: readback.addElement(Node.fixExcelisms(s)); break;
 				case 6: questionOrEval.addElement(Node.fixExcelisms(s)); break;
 				case 7: answerChoicesStr.addElement(Node.fixExcelisms(s)); break;
 				case 8: helpURL.addElement(Node.fixExcelisms(s)); break;
 				/* there are as many copies of each of these are there are answers - rudimentary support for arrays? */
 				case 9: {
 					int i = 0;
 					try {
 						i = Integer.parseInt(Node.fixExcelisms(s));
 					}
 					catch (Throwable t) {
 						setParseError("LanguageNum for answer not an integer: " + t.getMessage());
 						System.err.println("LanguageNum for answer not an integer: " + t.getMessage());
 						i = 0; // default language
 					}
 					if (i < 0 || i >= numLanguages) {
 						setParseError("Invalid language number '" + i + "': must be between 0 and " + (numLanguages - 1));
 						System.err.println("Invalid language number '" + i + "': must be between 0 and " + (numLanguages - 1));
 						i = 0;	// default language
 					}
 					answerLanguageNum = i;
 				}
 					break;
 				case 10: questionAsAsked = Node.fixExcelisms(s); break;
 				case 11: answerGiven = Node.fixExcelisms(s); break;
 				case 12: comment = Node.fixExcelisms(s); break;
 				case 13: answerTimeStampStr = Node.fixExcelisms(s); break;
 				default: break;	// ignore extras
 			}
 		}
 		if (field < 7) {	// help column is optional
 			setParseError("Too few columns: " + field);
 		}
 		if (numLanguagesFound < numLanguages) {
 			setParseError("Missing entries for languages " + (numLanguages - numLanguagesFound) + " to " + (numLanguages - 1));
 		}
 
 		if (conceptName != null && conceptName.trim().length() > 0) {
 			conceptName = conceptName.trim();
 			if (Character.isDigit(conceptName.charAt(0))) {
 				setParseError("Invalid conceptName '" + conceptName + "':  it begins with a digit - prepending '_'");
 				conceptName = "_" + conceptName;
 			}
 		}
 		if (localName != null && localName.trim().length() > 0) {
 			localName = localName.trim();
 			if (Character.isDigit(localName.charAt(0))) {
 				setParseError("Invalid localName '" + localName + "':  it begins with a digit - prepending '_'");
 				localName = "_" + localName;
 			}
 		}
 		else {
 			setParseError("A localName must be specified for this node");
 		}
 		/*
 		if (externalName != null && externalName.trim().length() > 0) {
 			externalName = externalName.trim();
 			if (Character.isDigit(externalName.charAt(0))) {
 				setParseError("Invalid externalName '" + externalName + "':  it begins with a digit - prepending '_'");
 				externalName = "_" + externalName;
 			}
 		}
 		*/
 
 		parseQuestionOrEvalTypeField();
 
 		for (int i=0;i<answerChoicesStr.size();++i) {
 			parseAnswerOptions(i,(String) answerChoicesStr.elementAt(i));
 		}
 
 		processFormattingMask();
 		createParseRangeStr();
 
 		if (datumType == Datum.INVALID) {
 			setParseError("Invalid dataType");
 		}
 	}
 	catch (Throwable t) {
 		setParseError("Syntax error creating node: " + t.getMessage());
 	}
 	}
 
 	public static synchronized String fixExcelisms(String s) {
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
 
 	private void parseQuestionOrEvalTypeField() {
 		StringTokenizer ans;
 		int z;
 
 		if (questionOrEvalTypeField == null) {
 			setParseError("Syntax error - must specify questionOrEvalTypeField");
 			return;
 		}
 
 		ans = new StringTokenizer(questionOrEvalTypeField,";",true);	// return ';' tokens too
 
 		for(int field=0;ans.hasMoreTokens();) {
 			String s = null;
 			try {
 				s = ans.nextToken();
 			}
 			catch (Throwable t) {
 				setParseError("tokenization error: " + t.getMessage());
 				System.err.println("tokenization error: " + t.getMessage());
 			}
 
 			if (";".equals(s)) {
 				++field;
 				continue;
 			}
 			switch(field) {
 				case 0:
 					questionOrEvalTypeStr = s;
 					for (z=0;z<ACTION_TYPES.length;++z) {
 						if (questionOrEvalTypeStr.equalsIgnoreCase(ACTION_TYPES[z])) {
 							questionOrEvalType = z;
 							break;
 						}
 					}
 					if (z == ACTION_TYPES.length) {
 						setParseError("Unknown questionOrEval type <B>" + Node.encodeHTML(questionOrEvalTypeStr) + "</B>");
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
 					minStr = s;
 					break;
 				case 3:
 					maxStr = s;
 					break;
 				case 4:
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
 
 	private boolean parseAnswerOptions(int langNum, String src) {
 		/* Need to make sure that the answer type, order of answers, and internal values of answers are the same across all languages */
 		if (src == null) {
 			setParseError("Syntax error - must specify answerOptions");
 			return false;
 		}
 
 		StringTokenizer ans = new StringTokenizer(src,";",true);	// return ';' tokens too
 		String token = "";
 
 		// Determine the question type (first token)
 		try {
 			token = ans.nextToken();
 		}
 		catch (Throwable t) {
 			setParseError("tokenization error: " + t.getMessage());
 			System.err.println("tokenization error: " + t.getMessage());
 		}
 
 		if (langNum == 0) {
 			for (int z=0;z<QUESTION_TYPES.length;++z) {
 				if (token.equalsIgnoreCase(QUESTION_TYPES[z])) {
 					answerType = z;
 					break;
 				}
 			}
 		}
 		else {
 			if (!QUESTION_TYPES[answerType].equalsIgnoreCase(token)) {
 				setParseError("mismatch across languages in answerType");
 			}
 			// don't change the known value for answerType
 		}
 
 		if (questionOrEvalType == EVAL) {
 			answerType = NOTHING;	// so no further processing
 			datumType = Datum.STRING;
 			return true;	// XXX? - should this really return?
 		}
 		else if (answerType == BADTYPE) {
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
 				Vector ansOptions = new Vector();
 				Vector prevAnsOptions = null;
 
 				if (langNum > 0) {
 					prevAnsOptions = (Vector) answerChoicesVector.elementAt(0);
 					/*
 					if (ans.countTokens() != (1 + 2 * prevAnsOptions.size())) {
 						setParseError("mismatch across languages in number of answerChoices");
 					}
 					*/
 				}
 
 				int ansPos = 0;
 
 				while(ans.hasMoreTokens()) {
 					String s = null;
 					try {
 						s = ans.nextToken();
 					}
 					catch (Throwable t) {
 						setParseError("tokenization error: " + t.getMessage());
 						System.err.println("tokenization error: " + t.getMessage());
 					}
 
 					if (";".equals(s)) {
 						++field;
 						continue;
 					}
 					switch(field) {
 						case 0:
 							break;	// discard the first token - answerType
 						case 1:
 							val = s;
 							if (langNum > 0) {
 								boolean err = false;
 								String s2 = null;	// previous answer
 								try {
 									s2 = ((AnswerChoice) prevAnsOptions.elementAt(ansPos++)).getValue();
 									if (!s2.equals(val)) {
 										err = true;
 									}
 								}
 								catch (Throwable t) { err = true; }	// catches NullPointerException and ArrayIndexOutOfBoundsException
 								if (err) {
 									setParseError("mismatch across languages in return value for answerChoice # " + (ansPos-1));
 									val = s2;	// reset it to the previously known return value for consistency (?)
 								}
 							}
 							break;
 						case 2: msg = s;
 							field = 0;	// so that cycle between val & msg;
 							if (val == null || msg == null) {
 								setParseError("Answer choice has null value or message");
 							}
 							else {
 								AnswerChoice ac = new AnswerChoice(val,msg);
 								ansOptions.addElement(ac);
 
 								/* check for duplicate answer choice values */
 								if (langNum == 0) {	// only for first pass
 									if (answerChoicesHash.put(val, ac) != null) {
 										setParseError("Answer value <B>" + val + "</B> already used");
 									}
 								}
 							}
 							val = null;
 							msg = null;
 							break;
 					}
 				}
 				if (ansOptions.size() == 0) {
 					setParseError("No answer choices specified");
 				}
 				else if (field == 1) {
 					setParseError("Missing message for value " + val);
 				}
 				answerChoicesVector.addElement(ansOptions);
 				break;
 			default:
 				break;
 		}
 
 		return true;
 	}
 
 	public String prepareChoicesAsHTML(Parser parser, Evidence ev, Datum datum, boolean autogen) {
 		return prepareChoicesAsHTML(parser, ev, datum,"",autogen);
 	}
 
 	public String prepareChoicesAsHTML(Parser parser, Evidence ev, Datum datum, String errMsg, boolean autogen) {
 		/* errMsg is a hack - only applies to RADIO_HORIZONTAL */
 		StringBuffer sb = new StringBuffer();
 		String defaultValue = "";
 		AnswerChoice ac;
 		Enumeration ans = null;
 
 		try {
 			switch (answerType) {
 			case RADIO:	// will store integers
 				ans = getAnswerChoices().elements();
 				while (ans.hasMoreElements()) { // for however many radio buttons there are
 					ac = (AnswerChoice) ans.nextElement();
 					ac.parse(parser,ev);
 					sb.append("<input type='radio' name='" + Node.encodeHTML(getLocalName()) + "' " + "value='" + Node.encodeHTML(ac.getValue()) + "'" +
 						(DatumMath.eq(datum,new Datum(ac.getValue(),DATA_TYPES[answerType])).booleanVal() ? " CHECKED" : "") + ">" + Node.encodeHTML(ac.getMessage()) + "<br>");
 				}
 				break;
 			case RADIO_HORIZONTAL: // will store integers
 				/* table underneath questions */
 				Vector v = getAnswerChoices();
 				ans = v.elements();
 				int count = v.size();
 
 				if (count > 0) {
 					Double pct = new Double(100. / (double) count);
 					sb.append("<TD COLSPAN='2' BGCOLOR='lightgrey'>");
 					sb.append("\n<TABLE CELLPADDING='0' CELLSPACING='2' BORDER='1' WIDTH='100%'>");
 					sb.append("\n<TR>");
 					while (ans.hasMoreElements()) { // for however many radio buttons there are
 						ac = (AnswerChoice) ans.nextElement();
 						ac.parse(parser,ev);
 						sb.append("\n<TD VALIGN='top' WIDTH='" + pct.toString() + "%'>");
 						sb.append("<input type='radio' name='" + Node.encodeHTML(getLocalName()) + "' " + "value='" + Node.encodeHTML(ac.getValue()) + "'" +
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
 				ans = getAnswerChoices().elements();
 				while (ans.hasMoreElements()) { // for however many radio buttons there are
 					ac = (AnswerChoice) ans.nextElement();
 					ac.parse(parser,ev);
 					sb.append("<input type='checkbox' name='" + Node.encodeHTML(getLocalName()) + "' " + "value='" + Node.encodeHTML(ac.getValue()) + "'" +
 						(DatumMath.eq(datum, new Datum(ac.getValue(),DATA_TYPES[answerType])).booleanVal() ? " CHECKED" : "") + ">" + Node.encodeHTML(ac.getMessage()) + "<br>");
 				}
 				break;
 			case COMBO:	// stores integers as value
 			case LIST: {
 				StringBuffer choices = new StringBuffer();
 				ans = getAnswerChoices().elements();
 
 				int optionNum=0;
 				int totalLines = 0;
 				boolean nothingSelected = true;
 				while (ans.hasMoreElements()) { // for however many radio buttons there are
 					ac = (AnswerChoice) ans.nextElement();
 					ac.parse(parser,ev);
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
 									((autogen) ? String.valueOf(optionNum) : Node.encodeHTML(ac.getValue())) +
 									")&nbsp;");
 								nothingSelected = false;
 							}
 							else {
 								choices.append(">" +
 									((autogen) ? String.valueOf(optionNum) : Node.encodeHTML(ac.getValue())) +
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
 				sb.append("\n<select name='" + Node.encodeHTML(getLocalName()) + "'" +
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
 				sb.append("<input type='text' onfocus='javascript:select()' name='" + Node.encodeHTML(getLocalName()) + "' value='" + Node.encodeHTML(defaultValue) + "'>");
 				break;
 			case MEMO:
 				if (datum != null && datum.exists())
 					defaultValue = datum.stringVal();
 				sb.append("<TEXTAREA rows='5' onfocus='javascript:select()' name='" + Node.encodeHTML(getLocalName()) + "'>" + Node.encodeHTML(defaultValue) + "</TEXTAREA>");
 				break;
 			case PASSWORD:	// stores Text type
 				if (datum != null && datum.exists())
 					defaultValue = datum.stringVal();
 				sb.append("<input type='password' onfocus='javascript:select()' name='" + Node.encodeHTML(getLocalName()) + "'>");
 				break;
 			case DOUBLE:	// stores Double type
 				if (datum != null && datum.exists())
 					defaultValue = datum.stringVal();
 				sb.append("<input type='text' onfocus='javascript:select()' name='" + Node.encodeHTML(getLocalName()) + "' value='" + Node.encodeHTML(defaultValue) + "'>");
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
 				sb.append("<input type='text' onfocus='javascript:select()' name='" + Node.encodeHTML(getLocalName()) + "' value='" + Node.encodeHTML(defaultValue) + "'>");
 				break;
 			case NOTHING:
 				sb.append("&nbsp;");
 				break;
 			}
 		}
 		catch (Throwable t) {
 			setError("Internal error: " + Node.encodeHTML(t.getMessage()));
 			System.err.println("Internal error: " + t.getMessage());
 			return "";
 		}
 
 		return sb.toString();
 	}
 
 	public boolean isWithinRange(Datum d) {
 		boolean err = false;
 
 /*
 		System.err.println("(" + minStr + "|" + ((minDatum != null) ? Datum.format(minDatum,mask) : "") +
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
 
 	public int getAnswerType() { return answerType; }
 	public int getDatumType() { return datumType; }
 
 
 	public String getQuestionMask() {
 		if (rangeStr != null)
 			return rangeStr;
 		else
 			return exampleFormatStr;
 	 }
 	public int getSourceLine() { return sourceLine; }
 	public String getSourceFile() { return sourceFile; }
 	public int getQuestionOrEvalType() { return questionOrEvalType; }
 	public String getQuestionOrEvalTypeField() { return questionOrEvalTypeField; }
 	public String getMaskStr() { return maskStr; }
 	public Format getMask() { return mask; }
 	public void setMinDatum(Datum d) { minDatum = d; }
 	public void setMaxDatum(Datum d) { maxDatum = d; }
 	public String getMinStr() { return minStr; }
 	public String getMaxStr() { return maxStr; }
 
 	public boolean focusable() { return (answerType != BADTYPE && answerType != NOTHING); }
 	public boolean focusableArray() { return (answerType == RADIO || answerType == RADIO_HORIZONTAL || answerType == CHECK); }
 
 
 	public void setParseError(String error) {
 		if (error != null)
 			parseErrors.addElement(error);
 
 	}
 	public void setError(String error) {
 		if (error != null)
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
 	 /*
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 		return "Node (" + sourceLine + "): <B>" + Node.encodeHTML(localName) + "</B><BR>\n" + "Concept: <B>" + Node.encodeHTML(conceptName) + "</B><BR>\n" +
 			"Description: <B>" + Node.encodeHTML(readback) + "</B><BR>\n" + "Dependencies: <B>" + Node.encodeHTML(dependencies) + "</B><BR>\n" +
 			"Question Reference: <B>" + Node.encodeHTML(externalName) + "</B><BR>\n" + "Action Type: <B>" + Node.encodeHTML(questionOrEvalTypeStr) + "</B><BR>\n" +
 			"Action: <B>" + Node.encodeHTML(questionOrEval) + "</B><BR>\n" + "AnswerType: <B>" + Node.encodeHTML(QUESTION_TYPES[answerType]) + "</B><BR>\n" + "AnswerOptions: <B>" +
 			Node.encodeHTML(answerChoices) + "</B><BR>\n";
 	}
 	*/
 
 	public String toTSV() {
 		StringBuffer sb = new StringBuffer(conceptName + "\t" + localName + "\t" + externalName + "\t" + dependencies + "\t" + questionOrEvalTypeField);
 
 		for (int i = 0;i<numLanguages;++i) {
 			try { sb.append("\t"); sb.append(readback.elementAt(i)); } catch (ArrayIndexOutOfBoundsException e) { }
 			try { sb.append("\t"); sb.append(questionOrEval.elementAt(i)); } catch (ArrayIndexOutOfBoundsException e) { }
 			try { sb.append("\t"); sb.append(answerChoicesStr.elementAt(i)); } catch (ArrayIndexOutOfBoundsException e) { }
 			try { sb.append("\t"); sb.append(helpURL.elementAt(i)); } catch (ArrayIndexOutOfBoundsException e) { }
 		}
 		return sb.toString();
 	}
 	
 	public String getTimeStampStr() { return timeStampStr; }
 	public Date getTimeStamp() { return timeStamp; }
 
 
 	public void setTimeStamp() {
 		timeStamp = new Date(System.currentTimeMillis());
 		timeStampStr = Datum.format(timeStamp,Datum.DATE,Datum.TIME_MASK);
 	}
 
 	public void setTimeStamp(String timeStr) {
 		if (timeStr == null || timeStr.trim().length() == 0) {
 			setTimeStamp();
 			return;
 		}
 
 		Date time = null;
 		try {
 			time = Datum.TIME_MASK.parse(timeStr);
 		}
 		catch (Throwable t) {
 			setParseError("Error parsing time " + t.getMessage());
 			System.err.println("error parsing time " + t.getMessage());
 		}
 		if (time == null) {
 			setTimeStamp();
 		}
 		else {
 			timeStamp = time;
 			timeStampStr = timeStr;
 		}
 	}
 
 	/* These are the get functions for the language specific vectors */
 	// these only occur once
 	public String getConcept() { return conceptName; }
 	public String getLocalName() { return localName; }
 	public String getExternalName() { return externalName; }
 	public String getDependencies() { return dependencies; }
 
 	// these are a Vector of length #languages
 	static private synchronized String elementAt(Vector v, int i) {
 		/* Get the language most furthest to the right (or the exact match) as the value for an element */
 		String s = null;
 		try {
 			for (int j=i;j>=0;--j) {
 				if (j >= v.size()) {
 					continue;
 				}
 				s = (String) v.elementAt(j);
 				if (s == null || s.trim().length() == 0) {
 					continue;
 				}
 				else {
 					break;
 				}
 			}
 			if (s == null) {
 				return "";
 			} else {
 				return s;
 			}
 		}
 		catch (Throwable t) {
 			System.err.println("Internal error in elementAt()");
 			return "";
 		}
 	}
 
 	public String getReadback() { return Node.elementAt(readback,answerLanguageNum); }
 	public String getQuestionOrEval() { return Node.elementAt(questionOrEval, answerLanguageNum); }
 	public Vector getAnswerChoices() {
 		try {
 			return (Vector) answerChoicesVector.elementAt(answerLanguageNum);
 		}
 		catch (Throwable t) {
 //			System.err.println("internal error accessing answerChoices # " + answerLanguageNum);
 			return null;
 		}
 	}
 	public void	setHelpURL(String h) { helpURL.setElementAt(h,answerLanguageNum); }
 	public String getHelpURL() { return Node.elementAt(helpURL,answerLanguageNum); }
 
 	public void setQuestionAsAsked(String s) { questionAsAsked = s; }
 	public String getQuestionAsAsked() { return questionAsAsked; }
 	public String getAnswerGiven() { return answerGiven; }
 	public String getAnswerTimeStampStr() { return answerTimeStampStr; }
 	public void setComment(String c) { comment = c; }
 	public String getComment() { return comment; }
 
 	public void setAnswerLanguageNum(int langNum) {
 		if (langNum < 0 || langNum >= numLanguages) {
 			setParseError("LanguageNum must be between 0 and " + (numLanguages - 1));
 			return;
 		}
 		answerLanguageNum = langNum;
 	}
 	public int getAnswerLanguageNum() { return answerLanguageNum; }
 	
	static public synchronized String encodeHTML(String s, boolean disallowEmpty) { return (new XmlString(s,disallowEmpty)).toString(); }
	static public String encodeHTML(String s) {	return encodeHTML(s,false); }	
 }
