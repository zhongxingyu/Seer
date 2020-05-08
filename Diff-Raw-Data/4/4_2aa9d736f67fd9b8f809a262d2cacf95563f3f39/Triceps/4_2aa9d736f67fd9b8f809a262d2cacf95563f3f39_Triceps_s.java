 import java.lang.*;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 /* Triceps
  */
 public class Triceps {
 	public static final int ERROR = 1;
 	public static final int OK = 2;
 	public static final int AT_END = 3;
 
 	static private final Vector EMPTY_LIST = new Vector();
 
 	public	Schedule nodes = null;
 	public	Evidence evidence = null;	// XXX should be private - made public for Node.prepareChoicesAsHTML(parser,...)
 	public	Parser parser = new Parser();	// XXX should be private - made public for Node.prepareChoicesAsHTML(parser,...)
 
 	private Vector errors = new Vector();
 	private static String fileAccessError = null;
 	private int currentStep=0;
 	private int numQuestions=0;	// so know how many to skip for compount question
 	private Date startTime = null;
 	private Date stopTime = null;
 	private String startTimeStr = null;
 	private String stopTimeStr = null;
 	private boolean isValid = false;
 	private Random random = new Random();
 	private String tempPassword = null;
 
 	public Triceps(String scheduleLoc) {
 		nodes = new Schedule(scheduleLoc);
 		if (!nodes.init()) {
 			setError(nodes.getErrors());
 		}
 		resetEvidence();
 		setDefaultValues();
 		createTempPassword();
 		isValid = true;
 	}
 	
 	public boolean isValid() { return isValid; }
 	
 	public boolean reloadSchedule() { 
 		return loadDatafile(null);
 	}
 	
 	public boolean loadDatafile(String name) {
 		Schedule oldNodes = nodes;
 		Evidence oldEvidence = evidence;
 		
 		boolean ok = false;
 		
 		if (name != null) {
 			nodes = new Schedule(name);
 		}
 		else {
 			nodes = new Schedule(nodes.getScheduleSource());
 		}
 		
 		if (!nodes.init()) {
 			nodes = oldNodes;
 			return false;
 		}
 		
 		resetEvidence();
 		setDefaultValues();
 		
 		try {
 			for (int i=0;i<oldNodes.size();++i) {
 				Node oldNode = oldNodes.getNode(i);
 				Node newNode = evidence.getNode(oldNode);	// get newNode with same name or concept as old ones
 				if (newNode != null) {
 					evidence.set(newNode, oldEvidence.getDatum(oldNode),oldNode.getTimeStampStr());
 				}
 			}
 		}
 		catch (Throwable t) {
 			String msg = "Error loading datafile: " + t.getMessage() + " restoring original schedule";
 			setError(msg);
 			nodes = oldNodes;
 			evidence = oldEvidence;
 			return false;
 		}
 		/* data/evidence is loaded from working file; but the nodes are from the schedule soruce directory */
 		nodes.overloadReserved(oldNodes);
 		return true;
 	}
 
 	public Datum getDatum(Node n) {
 		return evidence.getDatum(n);
 	}
 
 	public Date getStartTime() {
 		return startTime;
 	}
 
 	public String getStartTimeStr() {
 		return startTimeStr;
 	}
 	public String getStopTimeStr() {
 		return stopTimeStr;
 	}
 
 	public Vector getErrors() {
 		/* when there is an error in getting or parsing a node */
 		if (parser.hasErrors()) {
 			setError(parser.getErrors());
 		}
 		Vector tmp = errors;
 		errors = new Vector();
 		return tmp;
 	}
 
 	public Enumeration getQuestions() {
 		Vector e = new Vector();
 		int braceLevel  = 0;
 		int actionType;
 		Node node;
 		int step = currentStep;
 
 		// should loop over available questions
 		do {
 			if (step >= size()) {
 				if (braceLevel > 0) {
 					setError("missing " + braceLevel + " closing brace(s)");
 				}
 				break;
 			}
 			node = nodes.getNode(step++);
 			actionType = node.getQuestionOrEvalType();
 			e.addElement(node);	// add regardless of type
 
 			if (actionType == Node.GROUP_OPEN) {
 				++braceLevel;
 			}
 			else if (actionType == Node.EVAL) {
 				node.setError("Should not have expression evaluations within a query block (brace level " + braceLevel);
 				break;
 			}
 			else if (actionType == Node.GROUP_CLOSE) {
 				--braceLevel;
 				if (braceLevel < 0) {
 					node.setError("Extra closing brace");
 					break;
 				}
 			}
 			else if (actionType == Node.QUESTION) {
 			}
 			else {
 				node.setError("Invalid action type");
 				break;
 			}
 		} while (braceLevel > 0);
 
 		numQuestions = e.size();	// what about error conditions?
 		return e.elements();
 	}
 
 	public String getQuestionStr(Node q) {
 		/* recompute the min and max ranges, if necessary - must be done before premature abort (if invalid entry)*/
 
 		String s;
 
 		s = q.getMinStr();
 		if (s != null)
 			q.setMinDatum(parser.parse(evidence,s));
 		else
 			q.setMinDatum(null);
 
 		s = q.getMaxStr();
 		if (s != null)
 			q.setMaxDatum(parser.parse(evidence,s));
 		else
 			q.setMaxDatum(null);
 
 		q.createParseRangeStr();
 
 		q.setQuestionAsAsked(parser.parseJSP(evidence, q.getQuestionOrEval()) + q.getQuestionMask());
 		return q.getQuestionAsAsked();
 	}
 
 	public int gotoFirst() {
 		currentStep = 0;
 		numQuestions = 0;
 		return gotoNext();
 	}
 
 	public int gotoStarting() {
 		currentStep = Integer.parseInt(nodes.getReserved(Schedule.STARTING_STEP));
 		if (currentStep < 0)
 			currentStep = 0;
 		numQuestions = 0;
 		return gotoNext();
 	}
 
 	public int gotoNext() {
 		Node node;
 		int braceLevel = 0;
 		int actionType;
 		int step = currentStep + numQuestions;
 
 		if (currentStep == size()) {
 			/* then already at end */
 			setError("You are already at the end of interview.  Thanks again.");
 			return ERROR;
 		}
 
 		do {		// loop forward through nodes -- break to query user or to end
 			if (step >= size()) {	// then the schedule is complete
 				if (braceLevel > 0) {
 					setError("Missing " + braceLevel + " closing brace(s)");
 				}
 				currentStep = size();	// put at last node
 				numQuestions = 0;
 				
 				/* The current state should be saved in the background after the next set of questions is retrieved.
 					It is up to the calling program to call toTSV() to save the state */
 
 				return AT_END;
 			}
 			if ((node = nodes.getNode(step)) == null) {
 				setError("Invalid node at step " + step);
 				return ERROR;
 			}
 
 			actionType = node.getQuestionOrEvalType();
 
 			if (actionType == Node.GROUP_OPEN) {
 				if (braceLevel == 0) {
 					if (parser.booleanVal(evidence, node.getDependencies())) {
 						break;	// this is the first node of a block - break out of loop to ask it
 					}
 					else {
 						++braceLevel;	// XXX:  skip this entire section
 						evidence.set(node, Datum.NA_DATUM);	// and set all of this brace's values to NA
 					}
 				}
 				else {
 					++braceLevel;	// skip this inner block
 					evidence.set(node, Datum.NA_DATUM);	// set all of this brace's values to NA
 				}
 			}
 			else if (actionType == Node.GROUP_CLOSE) {
 				--braceLevel;	// close an open block
 				evidence.set(node, Datum.NA_DATUM);	// closing an open block, so set value to NA
 				if (braceLevel < 0) {
 					node.setError("Extra closing brace");
 					return OK;	// otherwise won't be able to progress past the dangling closing brace!
 //					return ERROR;
 				}
 			}
 			else if (actionType == Node.EVAL) {
 				if (braceLevel > 0) {
 					evidence.set(node, Datum.NA_DATUM);	// NA if internal to a brace when going forwards
 				}
 				else {
 					if (parser.booleanVal(evidence, node.getDependencies())) {
 						evidence.set(node, new Datum(parser.stringVal(evidence, node.getQuestionOrEval()),node.getDatumType(),node.getMask()));
 					}
 					else {
 						evidence.set(node, Datum.NA_DATUM);	// if doesn't satisfy dependencies, store NA
 					}
 				}
 			}
 			else if (actionType == Node.QUESTION) {
 				if (braceLevel > 0) {
 					evidence.set(node, Datum.NA_DATUM);	// NA if internal to a brace when going forwards
 				}
 				else {
 					if (parser.booleanVal(evidence, node.getDependencies())) {
 						break;	// ask this question
 					}
 					else {
 						evidence.set(node, Datum.NA_DATUM);	// if doesn't satisfy dependencies, store NA
 					}
 				}
 			}
 			else {
 				node.setError("Invalid action type");
 				evidence.set(node, Datum.NA_DATUM);
 				return ERROR;
 			}
 			++step;
 		} while (true);
 		currentStep = step;
 		numQuestions = 0;
 
 		/* The current state should be saved in the background after the next set of questions is retrieved.
 			It is up to the calling program to call toTSV() to save the state */
 		return OK;
 	}
 
 	public int gotoNode(Object val) {
 		int step = currentStep;
 
 		Node n = evidence.getNode(val);
 		if (n == null) {
 			setError("Unknown node: " + val.toString());
 			return ERROR;
 		}
 		int result = evidence.getStep(n);
 		if (result == -1) {
 			setError("Unable to find index for node: " + n);
 			return ERROR;
 		} else {
 			currentStep = result;
 			return OK;
 		}
 	}
 
 	public int gotoPrevious() {
 		Node node;
 		int braceLevel = 0;
 		int actionType;
 		int step = currentStep;
 
 		while (true) {
 			if (--step < 0) {
 				if (braceLevel < 0)
 					setError("Missing " + braceLevel + " openining braces");
 
 				setError("You are already at the beginning.");
 				return ERROR;
 			}
 			if ((node = nodes.getNode(step)) == null)
 				return ERROR;
 
 			actionType = node.getQuestionOrEvalType();
 
 			if (actionType == Node.EVAL) {
 				;	// skip these going backwards, but don't reset values when going backwards
 			}
 			else if (actionType == Node.GROUP_CLOSE) {
 				--braceLevel;
 			}
 			else if (actionType == Node.GROUP_OPEN) {
 				++braceLevel;
 				if (braceLevel > 0) {
 					setError("extra opening brace");
 					return ERROR;
 				}
 				if (braceLevel == 0 && parser.booleanVal(evidence, node.getDependencies())) {
 					break;	// ask this block of questions
 				}
 				else {
 					// try the next question
 				}
 			}
 			else if (actionType == Node.QUESTION) {
 				if (braceLevel == 0 && parser.booleanVal(evidence, node.getDependencies())) {
 					break;	// ask this block of questions
 				}
 				else {
 					// else within a brace, or not applicable, so skip it.
 				}
 			}
 			else {
 				node.setError("Invalid action type");
 				return ERROR;
 			}
 		}
 		currentStep = step;
 		return OK;
 	}
 
 	private void startTimer(Date time) {
 		startTime = time;
 		startTimeStr = Datum.format(startTime,Datum.DATE,Datum.TIME_MASK);
 		stopTime = null;	// reset stopTime, since re-starting
 		nodes.setReserved(Schedule.START_TIME,startTimeStr);	// so that saved schedule knows when it was started
 	}
 
 	private void stopTimer() {
 		if (stopTime == null) {
 			stopTime = new Date(System.currentTimeMillis());
 			stopTimeStr = Datum.format(stopTime,Datum.DATE,Datum.TIME_MASK);
 		}
 	}
 
 	public void resetEvidence() {
 		evidence = new Evidence(nodes);
 	}
 	
 	private void setDefaultValues() {
 		Node n;
 		Datum d;
 		String init;
 		for (int i=0;i<size();++i) {
 			n = nodes.getNode(i);
 			if (n == null)
 				continue;
 
 			init = n.getAnswerGiven();
 
 			if (init == null || init.length() == 0 || init.equals(Datum.TYPES[Datum.UNASKED])) {
 				d = Datum.UNASKED_DATUM;
 			}
 			else if (init.equals(Datum.TYPES[Datum.UNKNOWN])) {
 				d = Datum.UNKNOWN_DATUM;
 			}
 			else if (init.equals(Datum.TYPES[Datum.NA])) {
 				d = Datum.NA_DATUM;
 			}
 			else if (init.equals(Datum.TYPES[Datum.INVALID])) {
 				d = Datum.INVALID_DATUM;
 			}
 			else if (init.equals(Datum.TYPES[Datum.REFUSED])) {
 				d = Datum.REFUSED_DATUM;
 			}
 			else if (init.equals(Datum.TYPES[Datum.NOT_UNDERSTOOD])) {
 				d = Datum.NOT_UNDERSTOOD_DATUM;
 			}			
 			else {
 				d = new Datum(init,n.getDatumType(),n.getMask());
 			}
 
 			evidence.set(n,d,n.getAnswerTimeStampStr());
 		}
 
 		startTimer(new Date(System.currentTimeMillis()));	// use current time
 	}
 
 	public boolean storeValue(Node q, String answer, String comment, String special, boolean adminMode) {
 		if (q == null) {
 			setError("null node");
 			return false;
 		}
 
 		if (answer == null || answer.trim().equals("")) {
 			if (q.getAnswerType() == Node.CHECK) {
 				answer = "0";	// unchecked defaults to false
 			}
 		}
 		Datum d;
 
 		if (comment != null) {
 			q.setComment(comment);
 		}
 
 		if (special != null && special.trim().length() > 0) {
 			if (adminMode) {
 				if (special.equals(Datum.TYPES[Datum.REFUSED])) {
 					evidence.set(q,Datum.REFUSED_DATUM);
 					return true;
 				}
 				if (special.equals(Datum.TYPES[Datum.UNKNOWN])) {
 					evidence.set(q,Datum.UNKNOWN_DATUM);
 					return true;
 				}
 				if (special.equals(Datum.TYPES[Datum.NOT_UNDERSTOOD])) {
 					evidence.set(q,Datum.NOT_UNDERSTOOD_DATUM);
 					return true;
 				}
 				setError("Unknown special datatype");
 				return false;
 			}
 			else {
 				setError("You do not currently have permission to be in Admin Mode");
 				return false;
 			}
 		}
 
 		if (q.getAnswerType() == Node.NOTHING && q.getQuestionOrEvalType() != Node.EVAL) {
 			evidence.set(q,new Datum("",Datum.STRING));
 			return true;
 		}
 		else {
 			d = new Datum(answer,q.getDatumType(),q.getMask()); // use expected value type
 		}
 
 		/* check for type error */
 		if (!d.exists()) {
 			String s = d.getError();
 			if (s.length() == 0) {
 				q.setError("<- Please answer this question");
 			}
 			else {
 				q.setError("<- " + s);
 			}
 			evidence.set(q,Datum.UNASKED_DATUM);
 			return false;
 		}
 
 		/* check if out of range */
 		if (!q.isWithinRange(d)) {
 			evidence.set(q,Datum.UNASKED_DATUM);
 			return false;	// shouldn't wording of error be done here, not in Node?
 		}
 		else {
 			evidence.set(q, d);
 			return true;
 		}
 	}
 
 	public int size() { return nodes.size(); }
 
 	public Node getNode(int i) {
 		return nodes.getNode(i);	// XXX should not allow direct access to this?
 	}
 
 	public String toString(Node n) {
 		return toString(n,false);
 	}
 
 	public String toString(Node n, boolean showReserved) {
 		Datum d = getDatum(n);
 		if (d == null)
 			return "null";
 		else
 			return d.stringVal(showReserved);
 	}
 
 	public boolean isSet(Node n) {
 		Datum d = getDatum(n);
 		if (d == null || d.isType(Datum.UNASKED))
 			return false;
 		else
 			return true;
 	}
 
 	public String evidenceToXML() {
 		return evidence.toXML();
 	}
 
 	public String toXML() {
 		StringBuffer sb = new StringBuffer("<Evidence>\n");
 		Node n;
 		Datum d;
 
 		for (int i=0;i<size();++i) {
 			n = nodes.getNode(i);
 			if (n == null)
 				continue;
 
 			d = evidence.getDatum(n);
 			if (d == null)
 				continue;
 
 			sb.append(" <datum name='" + n.getLocalName() + "' value='" + d.stringVal(true) + "'/>\n");
 		}
 		sb.append("</Evidence>\n");
 		return sb.toString();
 	}
 
 	public Vector collectParseErrors() {
 		/* Simply cycle through nodes, processing dependencies & actions */
 
 		Node n;
 		Datum d;
 		Vector parseErrors = new Vector();
 		String dependenciesErrors;
 		String actionErrors;
 		Vector nodeErrors;
 		boolean hasErrors;
 
 		for (int i=0;i<size();++i) {
 			n = nodes.getNode(i);
 			if (n == null)
 				continue;
 
 			hasErrors = false;
 			dependenciesErrors = "";
 			actionErrors = "";
 			nodeErrors = EMPTY_LIST;
 
 			parser.booleanVal(evidence, n.getDependencies());
 
 			if (parser.hasErrors()) {
 				hasErrors = true;
 				dependenciesErrors = parser.getErrors();
 			}
 
 			int actionType = n.getQuestionOrEvalType();
 			String s = n.getQuestionOrEval();
 
 			/* Check questionOrEval for syntax errors */
 			if (s != null) {
 				if (actionType == Node.QUESTION) {
 					parser.parseJSP(evidence, s);
 				}
 				else if (actionType == Node.EVAL) {
 					parser.stringVal(evidence, s);
 				}
 			}
 			
 			/* Check min & max range delimiters for syntax errors */
 			s = n.getMinStr();
 			if (s != null) {
 				parser.stringVal(evidence, s);
 			}
 			s = n.getMaxStr();
 			if (s != null) {
 				parser.stringVal(evidence, s);
 			}
 			
 			Vector v = n.getAnswerChoices();
 			if (v != null) {
 				for (int j=0;j<v.size();++j) {
 					AnswerChoice ac = (AnswerChoice) v.elementAt(j);
 					if (ac != null)
 						ac.parse(parser, evidence);	// any errors will be associated with the parser, not the node (although this is misleading)
 				}
 			}
 
 			if (parser.hasErrors()) {
 				hasErrors = true;
 				actionErrors = parser.getErrors();
 			}
 
 			if (n.hasErrors()) {
 				hasErrors = true;
 				nodeErrors = n.getErrors();
 			}
 			else {
 				nodeErrors = EMPTY_LIST;
 			}
 
 			if (hasErrors) {
 				parseErrors.addElement(new ParseError(n, dependenciesErrors, actionErrors, nodeErrors));
 			}
 		}
 		return parseErrors;
 	}
 
 	public boolean toTSV(String dir) {
 		return toTSV(dir,nodes.getReserved(Schedule.FILENAME));
 	}
 
 	public boolean toTSV(String dir, String targetName) {
 		String filename = dir + targetName;
 		FileWriter fw = null;
 		boolean ok = false;
 
 		stopTimer();
 
 		try {
 			File f = new File(filename);
 
 			fw = new FileWriter(filename);
 			ok = writeTSV(fw);
 		}
 		catch (Throwable t) {
 			String msg = "error writing to " + filename + ": " + t.getMessage();
 			setError(msg);
 		}
 		if (fw != null) {
 			try { fw.close(); } catch (Throwable t) {
 				setError("Error closing writer: " + t.getMessage());
 			}
 		}
 		return ok;
 	}
 
 	public boolean writeTSV(Writer out) {
 		Node n;
 		Datum d;
 		String ans;
 
 		if (out == null)
 			return false;
 
 
 		try {
 			/* Write header information */
 			/* set save file so can resume at same position */
 			nodes.setReserved(Schedule.STARTING_STEP,Integer.toString(currentStep));
 			nodes.toTSV(out);
 
 			/* Write comments saying when started and stopped.  If multiply resumed, will list these several times */
 			out.write("COMMENT " + "Schedule: " + nodes.getReserved(Schedule.LOADED_FROM) + "\n");
 			out.write("COMMENT " + "Started: " + getStartTimeStr() + "\n");
 			out.write("COMMENT " + "Stopped: " + getStopTimeStr() + "\n");
 
 			/* Show the names of the output columns */
 			out.write("COMMENT " + "concept\tinternalName\texternalName\tdependencies\tquestionOrEvalType");
 			for (int i=0;i<nodes.getLanguages().size();++i) {
 				out.write("\treadback[" + i + "]" +
 					"\tquestionOrEval[" + i + "]" +
 					"\tanswerChoices[" + i + "]" +
 					"\thelpURL[" + i + "]");
 			}
 			out.write("\tlanguageNum\tquestionAsAsked\tanswerGiven\tcomment\ttimeStamp\n");
 
 			for (int i=0;i<size();++i) {
 				n = nodes.getNode(i);
 				if (n == null)
 					continue;
 
 				d = evidence.getDatum(n);
 				if (d == null) {
 					ans = "";	// NULL
 				}
 				else {
 					ans = d.stringVal(true);
 				}
 				String comment = n.getComment();
 				if (comment == null)
 					comment = "";
 
 				out.write(n.toTSV() +
 					"\t" + n.getAnswerLanguageNum() +
 					"\t" + n.getQuestionAsAsked() +
 					"\t" + ans +
 					"\t" + comment +
 					"\t" + n.getTimeStampStr() + "\n");
 			}
 			out.flush();
 			return true;
 		}
 		catch (Throwable t) {
 			String msg = "Unable to write schedule file: " + t.getMessage();
 			setError(msg);
 			return false;
 		}
 	}
 
 	public String getTitle() {
 		return nodes.getReserved(Schedule.TITLE);
 	}
 
 	public String getPasswordForAdminMode() {
 		String s = nodes.getReserved(Schedule.PASSWORD_FOR_ADMIN_MODE);
 		if (s == null || s.trim().length() == 0)
 			return null;
 		else
 			return s;
 	}
 
 	public boolean isShowAdminModeIcons() {
 		return Boolean.valueOf(nodes.getReserved(Schedule.SHOW_ADMIN_ICONS)).booleanValue();
 	}
 	public boolean isShowQuestionRef() {
 		return Boolean.valueOf(nodes.getReserved(Schedule.SHOW_QUESTION_REF)).booleanValue();
 	}	
 	public boolean isAutoGenOptionNum() {
 		return Boolean.valueOf(nodes.getReserved(Schedule.AUTOGEN_OPTION_NUM)).booleanValue();
 	}
 	public boolean isDebugMode() {
 		return Boolean.valueOf(nodes.getReserved(Schedule.DEBUG_MODE)).booleanValue();		
 	}
 	public boolean isDeveloperMode() {
 		return Boolean.valueOf(nodes.getReserved(Schedule.DEVELOPER_MODE)).booleanValue();		
 	}
 	public boolean isAllowComments() {
 		return Boolean.valueOf(nodes.getReserved(Schedule.ALLOW_COMMENTS)).booleanValue();
 	}
 	public boolean isAllowLanguageSwitching() {
 		return Boolean.valueOf(nodes.getReserved(Schedule.ALLOW_LANGUAGE_SWITCHING)).booleanValue();
 	}	
 	
 	public String getIcon() { return nodes.getReserved(Schedule.ICON); }
 	public String getHeaderMsg() { return nodes.getReserved(Schedule.HEADER_MSG); }
 
 	public void setPasswordForAdminMode(String s) { nodes.setReserved(Schedule.PASSWORD_FOR_ADMIN_MODE,s); }
 
 	public Datum evaluateExpr(String expr) {
 		return parser.parse(evidence,expr);
 	}
 
 	public String getFilename() { return nodes.getReserved(Schedule.FILENAME); }
 	public boolean setFilename(String name) { return nodes.setReserved(Schedule.FILENAME, name); }
 
 	public boolean setLanguage(String language) {
 		return nodes.setReserved(Schedule.CURRENT_LANGUAGE,language);
 	}
 
 	public int getLanguage() { return nodes.getLanguage(); }
 	
 	public String createTempPassword() {
 		tempPassword = Long.toString(random.nextLong());
 		return tempPassword;
 	}
 	
 	public boolean isTempPassword(String s) {
 		String temp = tempPassword;
 		createTempPassword();	// reset it
 		
 		if (s == null)
 			return false;
 		return s.equals(temp);
 	}
 	
 	private void setError(String s) {
 		errors.addElement(s);
System.err.println(s);
 	}
 	
 	private void setError(Vector v) {
 		for (int i=0;i<v.size();++i) {
 			errors.addElement(v.elementAt(i));
 		}
 	}
 }
