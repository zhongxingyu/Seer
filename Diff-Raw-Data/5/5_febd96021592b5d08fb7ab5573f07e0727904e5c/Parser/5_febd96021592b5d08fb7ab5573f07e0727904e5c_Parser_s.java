 package com.redcareditor.mate;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.ScrollBar;
 
 import org.eclipse.jface.text.IViewportListener;
 import org.eclipse.jface.text.JFaceTextUtil;
 
 import com.redcareditor.mate.document.MateDocument;
 import com.redcareditor.onig.Match;
 import com.redcareditor.onig.Range;
 import com.redcareditor.onig.Rx;
 
 public class Parser {
 	int LOOK_AHEAD = 100;
 	public static int linesParsed = 0;
 	public static boolean synchronousParsing = false;
 	
 	public Grammar grammar;
 	public MateText mateText;
 	public StyledText styledText;
 	public MateDocument document;
 	
 	public int parsed_upto;	
 	public int lookAhead;
 	public int lastVisibleLine;
 	public int deactivationLevel;
 	public int parsedUpto;
 	public boolean alwaysParseAll;
 	public boolean enabled;
 	
 	public RangeSet changes;
 	public Scope root;
 	
 	// temporary stores for the modifications to the mateText
 	private int modifyStart, modifyEnd;
 	private String modifyText;
 	public ParseThunk thunk;
 	
 	public Parser(Grammar g, MateText m) {
 		g.initForUse();
 		grammar = g;
 		mateText = m;
 		styledText = m.getTextWidget();
 		lookAhead = LOOK_AHEAD;
 		lastVisibleLine = 0;
 //		tags = new Sequence<TextTag>(null);
 		changes = new RangeSet();
 		deactivationLevel = 0;
 		makeRoot();
 		attachListeners();
 		parsedUpto = 0;
 		alwaysParseAll = false;
 		modifyStart = -1;
 		document = m.getMateDocument();
 		enabled = true;
 	}
 	
 	public void close() {
 		if (thunk != null) {
 			thunk.stop();
 			thunk = null;
 		}
 		removeListeners();
 	}
 	
 	public void setRoot(Scope root) {
 		this.root = root;
 		root.setMateText(mateText);
 	}
 	
 	public void makeRoot() {
 		this.root = new Scope(mateText, this.grammar.scopeName);
 		this.root.isOpen = true;
 //		this.root.setStartPos(0, 0, true);
 //		int lineIx = styledText.getLineCount()-1;
 //		this.root.setEndPos(lineIx, 
 //							styledText.getCharCount() - styledText.getOffsetAtLine(lineIx), false);
 //		System.out.printf("making root: %s\n", this.grammar.scopeName);
 		DoublePattern dp = new DoublePattern();
 		dp.name = this.grammar.name;
 		dp.patterns = this.grammar.patterns;
 		dp.grammar = this.grammar;
 		this.root.pattern = dp;
 	}
 	
 	private VerifyListener verifyListener;
 	private ModifyListener modifyListener;
 	private IViewportListener viewportListener;
 	
 	public void attachListeners() {
 //		System.out.printf("parser attach listeners\n");
 		verifyListener = new VerifyListener() {
 			public void verifyText(VerifyEvent e) {
 				verifyEventCallback(e.start, e.end, e.text);
 			}
 		};
 		modifyListener = new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				modifyEventCallback();
 			}
 		};
 		viewportListener = new IViewportListener() {
 			public void viewportChanged(int verticalOffset) {
 				viewportScrolledCallback();
 			}
 		};
 		
 		mateText.getTextWidget().addVerifyListener(verifyListener);
 		mateText.getTextWidget().addModifyListener(modifyListener);
 		mateText.viewer.addViewportListener(viewportListener);
 	}
 	
 	public void removeListeners() {
 		mateText.getTextWidget().removeVerifyListener(verifyListener);
 		mateText.getTextWidget().removeModifyListener(modifyListener);
 		mateText.viewer.removeViewportListener(viewportListener);
 	}
 
 	public void verifyEventCallback(int start, int end, String text) {
 		// System.out.printf("verifyEventCallback(%s)\n", text);
 		if (enabled) {
 			modifyStart = start;
 			modifyEnd   = end;
 			modifyText  = text;
 		}
 	}
 	
 	public void modifyEventCallback() {
 		// TODO: this isn't quite right...
 		// System.out.printf("modifyEventCallback parser:%p\n", this);
 		// System.out.printf("modifying %d - %d, %d, %s\n", modifyStart, modifyEnd, styledText.getLineAtOffset(modifyStart), modifyText);
 		if (enabled) {
 			changes.add(styledText.getLineAtOffset(modifyStart), 
 					styledText.getLineAtOffset(modifyStart + modifyText.length()));
 			modifyStart = -1;
 			modifyEnd   = -1;
 			modifyText  = null;
 			processChanges();
 		}
 	}
 
 	public boolean shouldColour() {
 		return (modifyStart == -1);
 	}
 	
 	public void viewportScrolledCallback() {
 		if (enabled) {
 			lastVisibleLineChanged(JFaceTextUtil.getBottomIndex(mateText.getTextWidget()));
 		}
 	}
 	
 	// Process all change ranges.
 	public void processChanges() {
 		int thisParsedUpto = -1;
 		// System.out.printf("process_changes (lastVisibleLine: %d) (charCount = %d)\n", lastVisibleLine, styledText.getCharCount());
 		for (Range range : changes) {
 			if (range.end > thisParsedUpto && range.start <= lastVisibleLine + lookAhead) {
 				int rangeEnd = Math.min(lastVisibleLine + lookAhead, range.end);
 				thisParsedUpto = parseRange(range.start, rangeEnd);
 			}
 			int startOffset = styledText.getOffsetAtLine(range.start);
 			int endOffset = styledText.getOffsetAtLine(range.end);
 			styledText.redrawRange(startOffset, endOffset - startOffset, false);
 		}
 		//		System.out.printf("%s\n", root.pretty(0));
 		changes.ranges.clear();
 	}
 
 	// Parse from from_line to *at least* to_line. Will parse
 	// more if necessary. Returns the index of the last line
 	// parsed.
 	public int parseRange(int fromLine, int toLine) {
 		System.out.printf("parse_range(%d, %d)\n", fromLine, toLine);
 
 		int lineIx = fromLine;
 		boolean scopeChanged = false;
 		boolean scopeEverChanged = false;
 		while (lineIx <= toLine) {
 			scopeChanged = parseLine(lineIx);
 			System.out.printf("%d:%b\n", lineIx, scopeChanged);
 			if (scopeChanged) {
 				scopeEverChanged = true;
 				this.parsedUpto = lineIx;
 			}
 			lineIx++;
 		}
 		if (thunk != null) {
 			System.out.printf("already thunk\n");
 			thunk.delayAndUpdate(lineIx);
 		}
 		else {
 			System.out.printf("new thunk? (%b, %d, %d)\n", scopeEverChanged, lineIx, styledText.getLineCount() - 1);
 			if (scopeEverChanged && lineIx <= styledText.getLineCount() - 1) {
 				System.out.printf("new thunk!\n");
 				thunk = new ParseThunk(this, lineIx);
 				if (Parser.synchronousParsing) {
 					thunk.execute();
 				}
 			}
 		}
 
 		return toLine;
 	}
 	
 	public void parseOnwards(int fromLine) {
 		// The widget can be disposed between the Thunk being created and being
 		// executed.
 		if (styledText.isDisposed())
 			return;
 		int lineIx = fromLine;
 		int lineCount = styledText.getLineCount();
 		int lastLine = Math.min(lastVisibleLine + 100, lineCount - 1);
 		while (lineIx <= lastLine) {
 			parseLine(lineIx);
 			this.parsedUpto = lineIx;
 			redrawLine(lineIx);
 			lineIx++;
 		}
 	}
 	
 	public void redrawLine(int lineIx) {
 		// System.out.printf("redrawLine(%d)\n", lineIx);
 		int startOffset = styledText.getOffsetAtLine(lineIx);
 		int endOffset;
 		int lineCount = styledText.getLineCount();
 		if (lineIx + 1 < lineCount) {
 			endOffset = styledText.getOffsetAtLine(lineIx + 1) - 1;
 		}
 		else {
 			endOffset = styledText.getCharCount();
 		}
 		styledText.redrawRange(startOffset, endOffset - startOffset, false);
 	}
 	
 	public void lastVisibleLineChanged(int newLastVisibleLine) {
 		this.lastVisibleLine = newLastVisibleLine;
 		// System.out.printf("lastVisibleLineChanged(%d)\n", lastVisibleLine);
 		// System.out.printf("  parsedUpto: %d\n", parsed_upto);
 		if (lastVisibleLine + lookAhead >= parsedUpto) {
 			int endRange = Math.min(styledText.getLineCount() - 1, lastVisibleLine + lookAhead);
 			parseRange(parsedUpto, endRange);
 		}
 	}
 
 	public void sleep(int ms) {
 		try{
 		  //do what you want to do before sleeping
 		  Thread.currentThread().sleep(ms);//sleep for 1000 ms
 		  //do what you want to do after sleeptig
 		}
 		catch(InterruptedException ie){
 		//If this thread was intrrupted by
 		}
 	}
 	
 	private Scope scopeBeforeStartOfLine(int lineIx) {
 		Scope startScope = this.root.scopeAt(lineIx, 0);
 		if (startScope.getStart().getLine() == lineIx) {
 //			System.out.printf("sbsol: %s\n", startScope.pattern.name);
 			startScope = startScope.containingDoubleScope(lineIx);
 		}
 
 		return startScope;
 	}
 
 	private Scope scopeAfterEndOfLine(int lineIx, int lineLength) {
 		Scope endScope = this.root.scopeAt(lineIx, lineLength - 1);
 		if (endScope.getStart().getLine() == lineIx ) {
 			endScope = endScope.containingDoubleScope(lineIx);
 		}
 
 		return endScope;
 	}
 	
 	private boolean parseLine(int lineIx) {
 		Parser.linesParsed++;
 		String line = styledText.getLine(lineIx) + "\n";
 		int length = line.length();
 		System.out.printf("p%d, ", lineIx);
 		if (lineIx > this.parsedUpto)
 			this.parsedUpto = lineIx;
 		Scope startScope = scopeBeforeStartOfLine(lineIx);
 		Scope endScope1  = scopeAfterEndOfLine(lineIx, length);
 //		System.out.printf("startScope is: %s\n", startScope.name);
 //		System.out.printf("endScope1: %s\n", endScope1.name);
 		Scanner scanner = new Scanner(startScope, line);
 		ArrayList<Scope> allScopes = new ArrayList<Scope>();
 		allScopes.add(startScope);
 		ArrayList<Scope> closedScopes = new ArrayList<Scope>();
 		ArrayList<Scope> removedScopes = new ArrayList<Scope>();
 		allScopes.add(startScope);
 		// System.out.printf("start pretty:\n%s\n", root.pretty(2));
 		for (Marker m : scanner) {
 			Scope expectedScope = getExpectedScope(scanner.getCurrentScope(), lineIx, length, scanner.position);
 			if (expectedScope != null)
 				System.out.printf("expectedScope: %s (%d, %d)\n", expectedScope.name, expectedScope.getStart().getLine(), 
 					           expectedScope.getStart().getLineOffset());
 			else
 				System.out.printf("no expected scope\n");
 			System.out.printf("  scope: %s %d-%d (line length: %d)\n", 
 								m.pattern.name, m.from, m.match.getCapture(0).end, length);
 			if (m.isCloseScope) {
 				System.out.printf("     (closing)\n");
 				closeScope(scanner, expectedScope, lineIx, line, length, m, 
 							allScopes, closedScopes, removedScopes);
 			}
 			else if (m.pattern instanceof DoublePattern) {
 				System.out.printf("     (opening)\n");
 				openScope(scanner, expectedScope, lineIx, line, length, m, 
 						   allScopes, closedScopes, removedScopes);
 			}
 			else {
 				System.out.printf("     (single)\n");
 				singleScope(scanner, expectedScope, lineIx, line, length, m, 
 							 allScopes, closedScopes, removedScopes);
 			}
			// System.out.printf("pretty:\n%s\n", root.pretty(2));
 			scanner.position = m.match.getByteCapture(0).end;
 		}
 		clearLine(lineIx, startScope, allScopes, closedScopes, removedScopes);
 		Scope endScope2 = scopeAfterEndOfLine(lineIx, length);
 //		System.out.printf("end_scope2: %s\n", endScope2.name);
 		// System.out.printf("end pretty: %s\n", this.root.pretty(2));
 //		if (colourer != null) {
 //			// System.out.printf("before_uncolour_scopes\n");
 //			colourer.uncolourScopes(removedScopes);
 //			// System.out.printf("before_colour_line_with_scopes\n");
 //			colourer.colourLineWithScopes(allScopes);
 //			// System.out.printf("after_colour_line_with_scopes\n");
 //		}
 //		else {
 //			// stdout.printf("no colourer");
 //		}
 		return (endScope1 != endScope2);
 	}
 
 	public Scope getExpectedScope(Scope currentScope, int line, int lineLength, int lineOffset) {
 //		System.out.printf("get_expected_scope(%s, %d, %d)\n", currentScope.name, line, lineOffset);
 		if (lineOffset == lineLength)
 			return null;
 		Scope expectedScope = currentScope.firstChildAfter(document.getTextLocation(line, lineOffset));
 //		System.out.printf("first_child_after: %s\n", expectedScope.name);
 		assert(expectedScope != currentScope);
 		if (expectedScope != null) {
 			if (expectedScope.getStart().getLine() != line)
 				expectedScope = null;
 			while (expectedScope != null && expectedScope.isCapture) {
 				expectedScope = expectedScope.parent;
 			}
 		}
 		return expectedScope;
 	}
 
 	public void closeScope(Scanner scanner, Scope expectedScope, int lineIx, String line, 
 			int length, Marker m, ArrayList<Scope> allScopes, 
 			ArrayList<Scope> closedScopes, ArrayList<Scope> removedScopes) {
 		String endMatchString = line.substring(m.from, m.match.getCapture(0).end);
 		
 		// stdout.printf("checking for already closed: %s, (%d,%d)-(%d,%d) (%d,%d)-(%d,%d) '%s' '%s'\n",
 //     scanner.current_scope.name,
 //			  scanner.current_scope.end_line(), scanner.current_scope.end_line_offset(),
 //			  line_ix, m.match.end(0), 
 //			  scanner.current_scope.inner_end_line(), scanner.current_scope.inner_end_line_offset(),
 //				line_ix, m.from,
 //				scanner.current_scope.end_match_string, end_match_string);
 //		  
 		if (//scanner.getCurrentScope().endPos != null &&
 				scanner.getCurrentScope().getEnd().equals(document.getTextLocation(lineIx, m.match.getCapture(0).end)) &&
 				scanner.getCurrentScope().getInnerEnd().equals(document.getTextLocation(lineIx, m.from)) &&
				scanner.getCurrentScope().endMatchString == endMatchString) {
 				// we have already parsed this line and this scope ends here
 
 			// Re-add the captures from the end of the current scope to the 
 			// tracking arrays
 			for (Scope child : scanner.getCurrentScope().children) {
 				if (child.isCapture && 
 						child.getStart().getLine() == lineIx) {
 					if (!closedScopes.contains(child))
 						closedScopes.add(child);
 					if (!allScopes.contains(child))
 						allScopes.add(child);
 				}
 			}
 			// stdout.printf("closing scope matches expected\n");
 		}
 		else {
 			// stdout.printf("closing scope at %d\n", m.from);
 //			if (colourer != null) {
 //				colourer.uncolourScope(scanner.getCurrentScope(), false);
 //			}
 			setInnerEndPosSafely(scanner.getCurrentScope(), m, lineIx, length, 0);
 			setEndPosSafely(scanner.getCurrentScope(), m, lineIx, length, 0);
 			scanner.getCurrentScope().isOpen = false;
 			scanner.getCurrentScope().endMatchString = endMatchString;
 			//stdout.printf("end_match_string: '%s'\n", scanner.current_scope.end_match_string);
 			handleCaptures(lineIx, length, line, scanner.getCurrentScope(), m, allScopes, closedScopes);
 			if (expectedScope != null) {
 				scanner.getCurrentScope().removeChild(expectedScope);
 				removedScopes.add(expectedScope);
 				// @removed_scopes << expected_scope
 			}
 		}
 		removedScopes.add(scanner.getCurrentScope()); // so it gets uncoloured
 		closedScopes.add(scanner.getCurrentScope());
 		scanner.setCurrentScope(scanner.getCurrentScope().parent);
 		allScopes.add(scanner.getCurrentScope());
 	}
 
 
 	public void openScope(Scanner scanner, Scope expectedScope, int lineIx, 
 			String line, int length, Marker m,
 			ArrayList<Scope> allScopes, ArrayList<Scope> closedScopes, ArrayList<Scope> removedScopes ) {
 		// System.out.printf("[opening with %d patterns], \n", ((DoublePattern) m.pattern).patterns.size());
 		Scope s = new Scope(mateText, m.pattern.name);
 		s.pattern = m.pattern;
 		s.openMatch = m.match;
 		setStartPosSafely(s, m, lineIx, length, 0);
 		setInnerStartPosSafely(s, m, lineIx, length, 0);
 		s.beginMatchString = line.substring(m.from, m.match.getCapture(0).end);
 		
 		s.isOpen = true;
 		s.isCapture = false;
 		s.parent = scanner.getCurrentScope();
 		Scope newScope = s;
 		// is this a bug? captures aren't necessarily to be put into all_scopes yet surely?
 		if (expectedScope != null) {
 			// check mod ending scopes as the new one will not have a closing marker
 			// but the expected one will:
 			// stdout.printf("%s == %s and %s == %s and %s == %s and %s == %s and %s == %s",
 			// 			  name, other.name, pattern.name, other.pattern.name, start_loc().to_s(),
 			// 			  other.start_loc().to_s(), inner_start_loc().to_s(), other.inner_start_loc().to_s(),
 			// 			  begin_match_string, other.begin_match_string);
 			// if (!s.name.equals(expectedScope.name)) System.out.printf("different names\n");
 			// if (!s.pattern.name.equals(expectedScope.pattern.name)) System.out.printf("different patterns\n");
 			// if (!s.getStart().equals(expectedScope.getStart())) System.out.printf("different starts\n");
 			// if (!s.getInnerStart().equals(expectedScope.getInnerStart())) System.out.printf("different inner starts\n");
 			// if (!s.beginMatchString.equals(expectedScope.beginMatchString)) {
 			// 	System.out.printf("different beginMatchStrings '%s'/'%s'\n", s.beginMatchString, expectedScope.beginMatchString);
 			// }
 			if (s.surfaceIdenticalToModuloEnding(expectedScope)) {
 				System.out.printf("surface_identical_mod_ending: keep expected\n");
 				// don't need to do anything as we have already found this,
 				// but let's keep the old scope since it will have children and what not.
 				newScope = expectedScope;
 				for (Scope child : expectedScope.children) {
 					// if (!child.isCapture) {
 						closedScopes.add(child);
 						allScopes.add(child);
 					// }
 				}
 				// handleCaptures(lineIx, length, line, s, m, allScopes, closedScopes);
 				scanner.setCurrentScope(expectedScope);
 			}
 			else {
 				System.out.printf("surface_NOT_identical_mod_ending: replace expected\n");
 				if (s.overlapsWith(expectedScope)) {
 					scanner.getCurrentScope().removeChild(expectedScope);
 					// removed_scopes << expected_scope
 					removedScopes.add(expectedScope);
 				}
 				handleCaptures(lineIx, length, line, s, m, allScopes, closedScopes);
 				scanner.getCurrentScope().addChild(s);
 				scanner.setCurrentScope(s);
 			}
 		}
 		else {
 			handleCaptures(lineIx, length, line, s, m, allScopes, closedScopes);
 			scanner.getCurrentScope().addChild(s);
 			scanner.setCurrentScope(s);
 		}
 		allScopes.add(newScope);
 	}
 
 	private void singleScope(Scanner scanner, Scope expectedScope, int lineIx, 
 							String line, int length, Marker m, 
 							ArrayList<Scope> allScopes, ArrayList<Scope> closedScopes, 
 							ArrayList<Scope> removedScopes) {
 		Scope s = new Scope(this.mateText, m.pattern.name);
 		s.pattern = m.pattern;
 		s.openMatch = m.match;
 		setStartPosSafely(s, m, lineIx, length, 0);
 		setEndPosSafely(s, m, lineIx, length, 0);
 		s.isOpen = false;
 		s.isCapture = false;
 //		System.out.printf("beginMatchString '%s' %d - %d\n",  new String(line.getBytes(), m.from, m.match.getCapture(0).end - m.from), m.from, m.match.getCapture(0).end);
 		// s.beginMatchString = new String(line.getBytes(), m.from, m.match.getCapture(0).end - m.from); 
 		s.beginMatchString = line.substring(m.match.getCapture(0).start, m.match.getCapture(0).end);
 		s.parent = scanner.getCurrentScope();
 		Scope newScope = s;
 		if (expectedScope != null) {
 			if (s.surfaceIdenticalTo(expectedScope)) {
 				newScope = expectedScope;
 				for (Scope child : expectedScope.children) {
 					closedScopes.add(child);
 				}
 			}
 			else {
 				handleCaptures(lineIx, length, line, s, m, allScopes, closedScopes);
 				if (s.overlapsWith(expectedScope)) {
 					if (expectedScope == scanner.getCurrentScope()) {
 						// we expected this scope to close, but it doesn't
 					}
 					else {
 						scanner.getCurrentScope().removeChild(expectedScope);
 						// removed_scopes << expectedScope
 						removedScopes.add(expectedScope);
 					}
 				}
 				scanner.getCurrentScope().addChild(s);
 			}
 		}
 		else {
 			handleCaptures(lineIx, length, line, s, m, allScopes, closedScopes);
 			scanner.getCurrentScope().addChild(s);
 		}
 		allScopes.add(newScope);
 		closedScopes.add(newScope);
 	}
 
 	private boolean atEndOfNonFinalLine(int lineIx, int length, int to) {
 		return to == length && styledText.getLineCount() > lineIx+1;
 	}
 
 	public void setStartPosSafely(Scope scope, Marker m, int lineIx, int length, int cap) {
 		int to = m.match.getCapture(cap).start;
 		if (atEndOfNonFinalLine(lineIx, length, to)) 
 			scope.setStartPos(lineIx+1, 0, false);
 		else
 			scope.setStartPos(lineIx, Math.min(to, length), false);
 	}
 
 	public void setInnerStartPosSafely(Scope scope, Marker m, int lineIx, int length, int cap) {
 		int to = m.match.getCapture(cap).start;
 		if (atEndOfNonFinalLine(lineIx, length, to)) 
 			scope.setInnerStartPos(lineIx+1, 0, false);
 		else
 			scope.setInnerStartPos(lineIx, Math.min(to, length), false);
 	}
 
 	public void setInnerEndPosSafely(Scope scope, Marker m, int lineIx, int length, int cap) {
 		int from = m.match.getCapture(cap).start;
 		if (atEndOfNonFinalLine(lineIx, length, from)) {
 			scope.setInnerEndPos(lineIx, length, true);
 		}
 		else {
 			scope.setInnerEndPos(lineIx, Math.min(from, length-1), true);
 		}
 	}
 	
 	public void setEndPosSafely(Scope scope, Marker m, int lineIx, int length, int cap) {
 		int to = m.match.getCapture(cap).end;
 		if (atEndOfNonFinalLine(lineIx, length, to)) {
 			scope.setEndPos(lineIx, length, true);
 		}
 		else {
 			scope.setEndPos(lineIx, Math.min(to, length-1), true);
 		}
 	}
 	
 	// Opens scopes for captures AND creates closing regexp from
 	// captures if necessary.
 	public void handleCaptures(int lineIx, int length, String line, 
 			Scope scope, Marker m, ArrayList<Scope> allScopes, ArrayList<Scope> closedScopes) {
 		makeClosingRegex(line, scope, m);
 		collectChildCaptures(lineIx, length, scope, m, allScopes, closedScopes);
 	}
 
 	public Rx makeClosingRegex(String line, Scope scope, Marker m) {
 		// System.out.printf("make_closing_regex\n");
 		// new_end = pattern.end.gsub(/\\([0-9]+)/) do
 		// 	md.captures.send(:[], $1.to_i-1)
 		// end
 		if (m.pattern instanceof DoublePattern && !m.isCloseScope) {
 			DoublePattern dp = (DoublePattern) m.pattern;
 			//stdout.printf("making closing regex: %s (%d)\n", dp.end_string, (int) dp.end_string.length);
 			Rx rx = Rx.createRx("\\\\(\\d+)");
 			Match match;
 			int pos = 0;
 			StringBuilder src = new StringBuilder("");
 			boolean found = false;
 			while ((match = rx.search(dp.endString, pos, (int) dp.endString.length())) != null) {
 				found = true;
 				src.append(dp.endString.substring(pos, match.getCapture(0).start));
 				String numstr = dp.endString.substring(match.getCapture(1).start, match.getCapture(1).end);
 				int num = Integer.parseInt(numstr);
 				// System.out.printf("capture found: %d\n", num);
 				String capstr = line.substring(m.match.getCapture(num).start, m.match.getCapture(num).end);
 				src.append(capstr);
 				pos = match.getCapture(1).end;
 			}
 			if (found)
 				src.append(dp.endString.substring(pos, dp.endString.length()));
 			else
 				src.append(dp.endString);
 			System.out.printf("close re: '%s'\n", src.toString());
 			scope.closingRegex = Rx.createRx(src.toString());
 		}
 		return null;
 	}
 
 	public void collectChildCaptures(int lineIx, int length, Scope scope, 
 			Marker m, ArrayList<Scope> allScopes, ArrayList<Scope> closedScopes) {
 		Scope s;
 		Map<Integer, String> captures;
 		if (m.pattern instanceof SinglePattern) {
 			captures = ((SinglePattern) m.pattern).captures;
 		}
 		else {
 			if (m.isCloseScope) {
 				captures = ((DoublePattern) m.pattern).endCaptures;
 			}
 			else {
 				captures = ((DoublePattern) m.pattern).beginCaptures;
 			}
 			if (((DoublePattern) m.pattern).bothCaptures != null) {
 				captures = ((DoublePattern) m.pattern).bothCaptures;
 			}
 		}
 		List<Scope> captureScopes = new ArrayList<Scope>();
 		// create capture scopes
 		if (captures != null) {
 			for (Integer cap : captures.keySet()) {
 				if (m.match.numCaptures() - 1 >= cap && m.match.getCapture(cap).start != -1) {
 					s = new Scope(mateText, captures.get(cap));
 					s.pattern = scope.pattern;
 					s.setStartPos(lineIx, Math.min(m.match.getCapture(cap).start, length-1), false);
 					setInnerEndPosSafely(s, m, lineIx, length, cap);
 					setEndPosSafely(s, m, lineIx, length, cap);
 					s.isOpen = false;
 					s.isCapture = true;
 					s.parent = scope;
 					captureScopes.add(s);
 					allScopes.add(s);
 					closedScopes.add(s);
 				}
 			}
 		}
 		// Now we arrange the capture scopes into a tree under the matched
 		// scope. We do this by processing the captures in order of offset and 
 		// length. For each capture, we check to see if it is a child of an already 
 		// placed capture, and if so we add it as a child (we know that the latest 
 		// such capture is the one to add it to by the ordering). If not we
 		// add it as a child of the matched scope.
 
 		List<Scope> placedScopes = new ArrayList<Scope>();
 		Scope parentScope;
 		while (captureScopes.size() > 0) {
 			s = null;
 			// find first and longest remaining scope (put it in 's')
 			for (Scope cs : captureScopes) {
 				if (s == null || 
 						cs.getStart().compareTo(s.getStart()) < 0 || 
 						(cs.getStart().compareTo(s.getStart()) == 0 && cs.getLength() > s.getLength())) {
 					s = cs;
 				}
 			}
 			//System.out.printf("arrange: %s, start: %d, length: %d\n", s.name, s.startOffset(), s.endOffset() - s.startOffset());
 			// look for somewhere to put it from placed_scopes
 			parentScope = null;
 			for (Scope ps : placedScopes) {
 				if (s.getStart().compareTo(ps.getStart()) >= 0 && s.getEnd().compareTo(ps.getEnd()) <= 0) {
 					parentScope = ps;
 				}
 			}
 			if (parentScope != null) {
 				parentScope.addChild(s);
 				s.parent = parentScope;
 			}
 			else {
 				scope.addChild(s);
 			}
 			placedScopes.add(s);
 			captureScopes.remove(s);
 		}
 	}
 
 	private void clearLine(int lineIx, Scope startScope, ArrayList<Scope> allScopes, 
 							ArrayList<Scope> closedScopes, ArrayList<Scope> removedScopes) {
 		// If we are reparsing, we might find that some scopes have disappeared,
 		// delete them:
 		Scope cs = startScope;
 		while (cs != null) {
 			// stdout.printf("  removing_scopes from: %s\n", cs.name);
 			ArrayList<Scope> newRemovedScopes = cs.deleteAnyOnLineNotIn(lineIx, allScopes);
 			removedScopes.addAll(newRemovedScopes);
 			cs = cs.parent;
 		}
 
 		// any that we expected to close on this line that now don't?
 		// first build list of scopes that close on this line (including ones
 		// that did but haven't been removed yet).
 		ArrayList<Scope> scopesThatClosedOnLine = new ArrayList<Scope>();
 		Scope ts = startScope;
 		while (ts.parent != null) {
 			if (ts.getInnerEnd().getLine() == lineIx) {
 				System.out.printf("scopesThatClosedOnLine.add('%s')\n", ts.name);
 				scopesThatClosedOnLine.add(ts);
 			}
 			ts = ts.parent;
 		}
 		for (Scope s : scopesThatClosedOnLine) {
 			if (!closedScopes.contains(s)) {
 				if (s.isCapture) {
 					s.parent.removeChild(s);
 					removedScopes.add(s);
 				}
 				else {
 					int line = styledText.getLineCount() - 1;
 					int lineOffset = styledText.getCharCount() - styledText.getOffsetAtLine(line);
 					System.out.printf("reopening '%s'\n", s.name);
 					// TODO: why are we setting an end position here for the end of the doc,
 					// but using a null to represent open elsewhere?
 					s.setInnerEndPos(line, lineOffset, false);
 					s.setEndPos(line, lineOffset, false);
 					s.isOpen = true;
 				}
 			}
 		}
 	}
 }
