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
 
 	public static final String NULL = "not set";	// a default value to represent null in config files
 
 	private String scheduleURL = null;
 	private String scheduleUrlPrefix = null;
 	private Schedule nodes = new Schedule();
 	private Evidence evidence = null;
 	static private Parser parser = new Parser();
 
 	private Vector errors = new Vector();
 	private static String fileAccessError = null;
 	private int currentStep=0;
 	private int numQuestions=0;	// so know how many to skip for compount question
 	private Date startTime = null;
 	private Date stopTime = null;
 	private String startTimeStr = null;
 	private String stopTimeStr = null;
 	
 	private String workingFilesDir = null;
 	private String completedFilesDir = null;
 	private String scheduleSrcDir = null;
 
 	public Triceps(String scheduleSrcDir, String workingFilesDir, String completedFilesDir) {
 		setScheduleSrcDir(scheduleSrcDir);
 		setWorkingFilesDir(workingFilesDir);
 		setCompletedFilesDir(completedFilesDir);
 	}
 
 	public boolean setSchedule(String filename, String optionalFilePrefix) {
 		BufferedReader br = Triceps.getReader(filename, optionalFilePrefix);
 		if (br == null) {
 			scheduleURL = null;
 			errors.addElement("Unable to find or access '" + Triceps.getReaderError() + "'");
 			return false;
 		}
 		else {
 			scheduleURL = filename;
 			scheduleUrlPrefix = optionalFilePrefix;
 			
 			nodes = new Schedule();
 			return (nodes.load(br,scheduleURL) && resetEvidence() && setDefaultEvidence());
 		}
 	}
 
 	public boolean reloadSchedule() {
 		Schedule oldNodes = nodes;
 		Evidence oldEvidence = evidence;
 		
 		nodes = new Schedule();
 		
 		boolean ok=false;
 		
 		ok = nodes.load(Triceps.getReader(scheduleURL,scheduleUrlPrefix),scheduleURL);
 		ok = resetEvidence() && ok;
 		
 		try {
 			for (int i=0;i<oldNodes.size();++i) {
 				Node oldNode = oldNodes.getNode(i);
 				Node newNode = nodes.getNode(i);
 				evidence.set(newNode, oldEvidence.getDatum(oldNode),oldNode.getTimeStampStr());
 			}
 		}
 		catch (Exception e) {
 			errors.addElement("Error reloading schedule - number of nodes probably changed");
 			return false;
 		}
 		
 		return ok;
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
 
 	public Enumeration getErrors() {
 		/* when there is an error in getting or parsing a node */
 		if (parser.hasErrors()) { 
 			Vector v=parser.getErrors(); 
 			for (int c=0;c<v.size();++c) { 
 				errors.addElement(v.elementAt(c)); 
 			}  
 		}
 		Vector tmp = errors;
 		errors = new Vector();
 		return tmp.elements();
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
 					errors.addElement("missing " + braceLevel + " closing brace(s)");
 				}
 				break;
 			}
 			node = nodes.getNode(step++);
 			actionType = node.getActionType();
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
 		int parseRangeType = q.getParseRangeType();
 
 		if (parseRangeType == Node.PARSE_MIN || parseRangeType == Node.PARSE_MIN_AND_MAX) {
 			q.setMinDatum(parser.parse(evidence,q.getMinStr()));
 		}
 		if (parseRangeType == Node.PARSE_MAX || parseRangeType == Node.PARSE_MIN_AND_MAX) {
 			q.setMaxDatum(parser.parse(evidence,q.getMaxStr()));
 		}
 		q.createParseRangeStr();
 
 		q.setQuestionAsAsked(parser.parseJSP(evidence, q.getAction()) + q.getQuestionMask());
 //		if (parser.hasErrors()) { Vector v=parser.getErrors(); for (int c=0;c<v.size();++c) { errors.addElement(v.elementAt(c)); }  }
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
 			errors.addElement("You are already at the end of interview.  Thanks again.");
 			return ERROR;
 		}
 
 		do {		// loop forward through nodes -- break to query user or to end
 			if (step >= size()) {	// then the schedule is complete
 				if (braceLevel > 0) {
 					errors.addElement("Missing " + braceLevel + " closing brace(s)");
 				}
 				currentStep = size();	// put at last node
 				numQuestions = 0;
 				
 				/* Before advancing any further, save to completed files dir */
 //				toTSV(getCompletedFilesDir() + startTimeStr);		// no, have the servlet do this after sending next question
 				
 				return AT_END;
 			}
 			if ((node = nodes.getNode(step)) == null) {
 				errors.addElement("Invalid node at step " + step);
 				return ERROR;
 			}
 
 			actionType = node.getActionType();
 
 			if (actionType == Node.GROUP_OPEN) {
 				if (braceLevel == 0) {
 					if (parser.booleanVal(evidence, node.getDependencies())) {
 						break;	// this is the first node of a block - break out of loop to ask it
 					}
 					else {
 						++braceLevel;	// XXX:  skip this entire section 
 						evidence.set(node, Datum.getInstance(Datum.NA));	// and set all of this brace's values to NA
 					}
 //					if (parser.hasErrors()) { Vector v=parser.getErrors(); for (int c=0;c<v.size();++c) { errors.addElement(v.elementAt(c)); }  }
 				}
 				else {
 					++braceLevel;	// skip this inner block
 					evidence.set(node, Datum.getInstance(Datum.NA));	// set all of this brace's values to NA
 				}
 			}
 			else if (actionType == Node.GROUP_CLOSE) {
 				--braceLevel;	// close an open block
 				evidence.set(node, Datum.getInstance(Datum.NA));	// closing an open block, so set value to NA
 				if (braceLevel < 0) {
 					node.setError("Extra closing brace");
 					return ERROR;
 				}
 			}
 			else if (actionType == Node.EVAL) {
 				if (braceLevel > 0) {
 					evidence.set(node, Datum.getInstance(Datum.NA));	// NA if internal to a brace when going forwards
 				}
 				else {
 					if (parser.booleanVal(evidence, node.getDependencies())) {
 						evidence.set(node, new Datum(parser.stringVal(evidence, node.getAction()),node.getDatumType(),node.getMask()));
 					}
 					else {
 						evidence.set(node, Datum.getInstance(Datum.NA));	// if doesn't satisfy dependencies, store NA
 					}
 //					if (parser.hasErrors()) { Vector v=parser.getErrors(); for (int c=0;c<v.size();++c) { errors.addElement(v.elementAt(c)); }  }
 				}
 			}
 			else if (actionType == Node.QUESTION) {
 				if (braceLevel > 0) {
 					evidence.set(node, Datum.getInstance(Datum.NA));	// NA if internal to a brace when going forwards
 				}
 				else {
 					if (parser.booleanVal(evidence, node.getDependencies())) {
 						break;	// ask this question
 					}
 					else {
 						evidence.set(node, Datum.getInstance(Datum.NA));	// if doesn't satisfy dependencies, store NA
 					}
 //					if (parser.hasErrors()) { Vector v=parser.getErrors(); for (int c=0;c<v.size();++c) { errors.addElement(v.elementAt(c)); }  }
 				}
 			}
 			else {
 				node.setError("Invalid action type");
 				evidence.set(node, Datum.getInstance(Datum.NA));
 				return ERROR;
 			}
 			++step;
 		} while (true);
 		currentStep = step;
 		numQuestions = 0;
 		
 		/* Before advancing any further, save a scratch file */
 //		toTSV(getWorkingFilesDir() + startTimeStr);	// no, have the servlet do this after returning reply?
 		
 		return OK;
 	}
 
 	public int gotoNode(Object val) {
 		int step = currentStep;
 
 		Node n = evidence.getNode(val);
 		if (n == null) {
 			errors.addElement("Unknown node: " + Node.encodeHTML(val.toString()));
 			return ERROR;
 		}
 		int result = evidence.getStep(n);
 		if (result == -1) {
 			errors.addElement("Unable to find index for node: " + n);
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
 					errors.addElement("Missing " + braceLevel + " openining braces");
 
 				errors.addElement("You are already at the beginning.");
 				return ERROR;
 			}
 			if ((node = nodes.getNode(step)) == null)
 				return ERROR;
 
 			actionType = node.getActionType();
 
 			if (actionType == Node.EVAL) {
 				;	// skip these going backwards, but don't reset values when going backwards
 			}
 			else if (actionType == Node.GROUP_CLOSE) {
 				--braceLevel;
 			}
 			else if (actionType == Node.GROUP_OPEN) {
 				++braceLevel;
 				if (braceLevel > 0) {
 					errors.addElement("extra opening brace");
 					return ERROR;
 				}
 				if (braceLevel == 0 && parser.booleanVal(evidence, node.getDependencies())) {
 					break;	// ask this block of questions
 				}
 				else {
 					// try the next question
 				}
 //				if (parser.hasErrors()) { Vector v=parser.getErrors(); for (int c=0;c<v.size();++c) { errors.addElement(v.elementAt(c)); }  }
 			}
 			else if (actionType == Node.QUESTION) {
 				if (braceLevel == 0 && parser.booleanVal(evidence, node.getDependencies())) {
 					break;	// ask this block of questions
 				}
 				else {
 					// else within a brace, or not applicable, so skip it.
 				}
 //				if (parser.hasErrors()) { Vector v=parser.getErrors(); for (int c=0;c<v.size();++c) { errors.addElement(v.elementAt(c)); }  }
 			}
 			else {
 				node.setError("Invalid action type");
 				return ERROR;
 			}
 		}
 		currentStep = step;
 		return OK;
 	}
 
 	public boolean resetEvidence() {
 		evidence = new Evidence(nodes);
 		return true;
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
 
 	private boolean setDefaultEvidence() {
 		Node n;
 		Datum d;
 		String init;
 		for (int i=0;i<size();++i) {
 			n = nodes.getNode(i);
 			if (n == null)
 				continue;
 
 			init = n.getDefaultAnswer();
 
 			if (init == null || init.length() == 0 || init.equals(NULL)) {
 				d = Datum.getInstance(Datum.UNKNOWN);
 			}
 			else if (init.equals(Datum.TYPES[Datum.UNKNOWN])) {
 				d = Datum.getInstance(Datum.UNKNOWN);
 			}
 			else if (init.equals(Datum.TYPES[Datum.NA])) {
 				d = Datum.getInstance(Datum.NA);
 			}
 			else if (init.equals(Datum.TYPES[Datum.INVALID])) {
 				d = Datum.getInstance(Datum.INVALID);
 			}
 			else if (init.equals(Datum.TYPES[Datum.REFUSED])) {
 				d = Datum.getInstance(Datum.REFUSED);
 			}
 			else {
 				d = new Datum(init,n.getDatumType(),n.getMask());
 			}
 			
 			evidence.set(n,d,n.getDefaultAnswerTimeStampStr());
 		}
 		
 		startTimer(new Date(System.currentTimeMillis()));	// use current time
 		
 		return true;
 	}
 
 	public boolean storeValue(Node q, String answer, boolean bypass) {
 		if (q == null) {
 			errors.addElement("null node");
 			return false;
 		}
 
 		if (answer == null || answer.trim().equals("")) {
 			if (bypass) {
 				evidence.set(q,Datum.getInstance(Datum.REFUSED));
 				return true;
 			}
 			if (q.getAnswerType() == Node.CHECK) {
 				answer = "0";	// unchecked defaults to false
 			}
 		}
 		Datum d;
 		
 		if (q.getAnswerType() == Node.NOTHING && q.getActionType() != Node.EVAL) {
 			d = Datum.getInstance(Datum.NA);
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
 			evidence.set(q,Datum.getInstance(Datum.UNKNOWN));
 			return false;
 		}
 
 		/* check if out of range */
 		if (!q.isWithinRange(d)) {
 			evidence.set(q,Datum.getInstance(Datum.UNKNOWN));
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
 		if (d == null || d.isType(Datum.UNKNOWN))
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
 
 			sb.append(" <datum name='" + n.getName() + "' value='" + d.stringVal(true) + "'/>\n");
 		}
 		sb.append("</Evidence>\n");
 		return sb.toString();
 	}
 
 	public Vector collectParseErrors() {
 		/* Simply cycle through nodes, processing dependencies & actions */
 
 		Node n;
 		Datum d;
 		Vector parseErrors = new Vector();
 		Vector dependenciesErrors;
 		Vector actionErrors;
 		Vector nodeErrors;
 		boolean hasErrors;
 //		Evidence ev = new Evidence(nodes);
 
 		for (int i=0;i<size();++i) {
 			n = nodes.getNode(i);
 			if (n == null)
 				continue;
 
 			hasErrors = false;
 			dependenciesErrors = EMPTY_LIST;
 			actionErrors = EMPTY_LIST;
 			nodeErrors = EMPTY_LIST;
 
 			parser.booleanVal(evidence, n.getDependencies());
 
 			if (parser.hasErrors()) {
 				hasErrors = true;
 				dependenciesErrors = parser.getErrors();
 			}
 
 			int actionType = n.getActionType();
 			String action = n.getAction();
 
 			if (action != null) {
 				if (actionType == Node.QUESTION) {
 					parser.parseJSP(evidence, action);
 				}
 				else if (actionType == Node.EVAL) {
 					parser.stringVal(evidence, action);
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
 	
 	public boolean toTSV() {
 		return toTSV(getWorkingFilesDir() + nodes.getReserved(Schedule.FILENAME));
 	}
 
 	public boolean toTSV(String filename) {
 		FileWriter fw = null;
 
 		stopTimer();
 
 		try {
 			File f = new File(filename);
 
 			fw = new FileWriter(filename);
 			boolean ok = writeTSV(fw);
 			return ok;
 		}
 		catch (Exception e) {
 			errors.addElement("error writing to " + Node.encodeHTML(filename) + ": " + e);
 			return false;
 		}
 		finally {
 			if (fw != null) {
 				try { fw.close(); } catch (Exception e) {}
 			}
 		}
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
 			out.write("COMMENT " + "Schedule: " + scheduleURL + "\n");
 			out.write("COMMENT " + "Started: " + getStartTimeStr() + "\n");
 			out.write("COMMENT " + "Stopped: " + getStopTimeStr() + "\n");
 
 			for (int i=0;i<size();++i) {
 				n = nodes.getNode(i);
 				if (n == null)
 					continue;
 
 				d = evidence.getDatum(n);
 				if (d == null) {
 					ans = NULL;
 				}
 				else {
 					ans = d.stringVal(true);
 				}
 
 				out.write(n.toTSV() + "\t" + n.getQuestionAsAsked() + "\t" + ans + "\t" + n.getTimeStampStr() + "\n");
 			}
 			out.flush();
 			return true;
 		}
 		catch (IOException e) {
 			errors.addElement("Unable to write schedule file: " + Node.encodeHTML(e.getMessage()));
 			return false;
 		}
 	}
 
 	static BufferedReader getReader(String filename, String optionalFilePrefix) {
 		boolean ok = false;
 		BufferedReader br = null;
 		URL url = null;
 		File file = null;
 
 //		if (filename == null)
 //			return null;
 
 		/* Is it a URL pointing to a file? If so, try reading from it*/
 		try {
 			url = new URL(filename);
 			br = new BufferedReader(new InputStreamReader(url.openStream()));
 			br.mark(1000);	// allows stream be reset to beginning, rather than closing & re-opening it
 
 			String fileLine;
 			while ((fileLine = br.readLine()) != null) {
 				fileLine = fileLine.trim();
 				if (fileLine.equals(""))
 					continue;
 
 				/* If this is an HTML page instead of a text file, then an error has occurred */
 				if (fileLine.startsWith("<")) {
 					ok = false;
 					break;
 				}
 				else {
 					br.reset();	// so that resume reading from the beginning of the file
 					ok = true;
 					break;
 				}
 			}
 		}
 		catch (Throwable t) {
 			/* not a valid URL, or unable to access it - so try reading from a file */
			System.err.println("probalbly not a url: " + t.getMessage());
 		}
 		finally {
 			if (ok) {
 				return br;
 			}
 			else {
 				if (br != null) {
 					try { br.close(); } catch (Exception e) {}
 				}
 			}
 		}		
 
 		/* If not a URL, then try reading from a file */
 		try {
 			file = new File(((optionalFilePrefix != null) ? optionalFilePrefix : "") + filename);
 			if (!file.exists() || !file.isFile() || !file.canRead()) {
 				ok = false;
 			}
 			else {
 				br = new BufferedReader(new FileReader(file));
 				ok = true;
 			}
 		}
 		catch (Throwable t) {
			System.err.println("error accessing " + file.toString() + ": " + t.getMessage());
 		}
 		finally {
 			if (ok) {
 				return br;
 			}
 			else {
 				if (br != null) {
 					try { br.close(); } catch (Exception e) {}
 				}
 			}
 		}
 		if (!ok) {
 			StringBuffer sb = new StringBuffer();
 			if (url != null) {
 				sb.append(url.toString());
 			}
 			if (file != null) {
 				if (sb.length() > 0) {
 					sb.append(" and ");
 				}
 				sb.append(file.toString());
 			}
 			if (sb.length() == 0) {
 				sb.append("[filename=" + filename + "], [optionalFilePrefix=" + optionalFilePrefix + "]");
 			}
 				
 			fileAccessError = "Error accessing or reading from " + sb.toString();
 		}		
 		
 		return null;
 	}
 	
 	public static String getReaderError() {
 		String tmp = fileAccessError;
 		fileAccessError = null;
 		return tmp;
 	}
 	
 	public String getTitle() {
 		return nodes.getReserved(Schedule.TITLE);
 	}
 	
 	public void setWorkingFilesDir(String s) { 
 		workingFilesDir = ((s == null) ? "" : s); 
 		try {
 			File f = new File(workingFilesDir);
 			if (!f.isDirectory() || !f.canRead()) {
 				System.err.println("unreadable directory " + f.toString());
 			}
 		}
 		catch (Throwable t) {
			System.err.println("error setting working directory: " + t.getMessage());
 		}
 	}
 	public String getWorkingFilesDir() { return workingFilesDir; }
 	public void setCompletedFilesDir(String s) { completedFilesDir = ((s == null) ? "" : s); }
 	public String getCompletedFilesDir() { return completedFilesDir; }
 	public void setScheduleSrcDir(String s) { scheduleSrcDir = ((s == null) ? "" : s); }
 	public String getScheduleSrcDir() { return scheduleSrcDir; }
 	
 	public String getPasswordForRefused() { 
 		String s = nodes.getReserved(Schedule.PASSWORD_FOR_REFUSED);
 		if (s == null || s.trim().length() == 0)
 			return null;
 		else
 			return s;
 	}
 	public String getPasswordForUnknown() { 
 		String s = nodes.getReserved(Schedule.PASSWORD_FOR_UNKNOWN);
 		if (s == null || s.trim().length() == 0)
 			return null;
 		else
 			return s;
 	}
 	public String getIcon() { return nodes.getReserved(Schedule.ICON); }
 	public String getHeaderMsg() { return nodes.getReserved(Schedule.HEADER_MSG); }
 	
 	public void setPasswordForRefused(String s) { nodes.setReserved(Schedule.PASSWORD_FOR_REFUSED,s); }
 	
 	public Datum evaluateExpr(String expr) {
 		return parser.parse(evidence,expr);
 	}
 	
 	public String getFilename() { return nodes.getReserved(Schedule.FILENAME); }
 	public boolean setFilename(String name) { return nodes.setReserved(Schedule.FILENAME, name); }
 }
