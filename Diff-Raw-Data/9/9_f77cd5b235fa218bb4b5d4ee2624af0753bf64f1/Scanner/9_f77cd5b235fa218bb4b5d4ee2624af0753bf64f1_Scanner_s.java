 package com.redcareditor.mate;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.logging.Logger;
 import java.util.logging.Handler;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Level;
 
 import com.redcareditor.onig.Match;
 import com.redcareditor.onig.Rx;
 
 public class Scanner implements Iterable<Marker> {
 	private Scope currentScope;
 	public int position;
 	public String line;
 	public int lineLength;
 	public int lineIx;
 	public ArrayList<Marker> cachedMarkers;
 	public Logger logger;
 
 	public void setCurrentScope(Scope scope) {
 		this.currentScope = scope;
 		cachedMarkers.clear();
 	}
 	
 	public Scope getCurrentScope() {
 		return this.currentScope;
 	}
 	
 	public Scanner(Scope startScope, String line, int lineIx) {
 		this.currentScope = startScope;
 		this.line = line;
 		this.lineIx = lineIx;
 		this.lineLength = line.getBytes().length;
 		this.position = 0;
 		this.cachedMarkers = new ArrayList<Marker>();
 		logger = Logger.getLogger("JMV.Scanner");
 		logger.setUseParentHandlers(false);
 		for (Handler h : logger.getHandlers()) {
 			logger.removeHandler(h);
 		}
 		logger.addHandler(MateText.consoleHandler());
 	}
 
 	// if we have already scanned this line for this scope then
 	// simply return the next cached marker (choosing the longest
 	// match in case of a tie).
 	public Marker getCachedMarker() {
 		Marker m = null;
 		int newLength;
 		for (Marker newMarker : cachedMarkers) {
 			m = newMarker.bestOf(m);
 		}
 		return m;
 	}
 	
 	public void removePrecedingCachedMarkers(Marker m) {
 		int ix = 0;
 		int len = cachedMarkers.size();
 		for(int i = 0; i < len; i++, ix++) {
 			if (cachedMarkers.get(ix).from < m.match.getCapture(0).end) {
 				cachedMarkers.remove(ix);
 				ix--;
 			}
 		}
 	}
 
 	public Match scanForMatch(int from, Pattern p) {
 		if (p.name != null && (p.name.startsWith("#") || p.name.startsWith("$")))
 			System.out.printf("*** WARNING trying to scan for pattern called %s\n", p.name);
 		
 		Match match = null;
 		if (p instanceof SinglePattern) {
 			SinglePattern sp = (SinglePattern) p;
 			if (sp.match.regex != null) {
 				match = sp.match.search(line, from, this.lineLength);
 			}
 		}
 		else if (p instanceof DoublePattern) {
 			if (((DoublePattern) p).begin.regex != null) {
 				match = ((DoublePattern) p).begin.search(this.line, from, this.lineLength);
 			}
 		}
 		return match;
 	}
 	
 	public Marker findNextMarker() {
 		//logger.info(String.format("scanning: '%s' from %d to %d (current_scope is %s)", this.line.replaceAll("\n", ""), this.position, this.lineLength, currentScope.name));
 		Marker bestMarker = null;
 		int newLength;
 		boolean isCloseMatch = false;
 //		if ((bestMarker = getCachedMarker()) != null) {
 //			logger.info("  got cached marker\n");
 //			this.cachedMarkers.remove(bestMarker);
 //			removePrecedingCachedMarkers(bestMarker);
 //			return bestMarker;
 //		}
 //		assert(cachedMarkers.size() == 0);
 		Rx closingRegex = currentScope.closingRegex;
 		if (closingRegex != null) {
 			//logger.info(String.format("closing regex: '%s'", closingRegex.pattern));
 			Match match = closingRegex.search(this.line, this.position, this.lineLength);
 			if (match != null && 
 			       !(match.getCapture(0).start == currentScope.getStart().getLineOffset() && 
 			         currentScope.getStart().getLine() == this.lineIx)
 			    ) {
 				//logger.info(String.format("closing match: %s (%d-%d)", this.currentScope.name, match.getCapture(0).start, match.getCapture(0).end));
 				Marker newMarker = new Marker();
 				newMarker.pattern = this.currentScope.pattern;
 				newMarker.match = match;
 				newMarker.from = match.getCapture(0).start;
 				newMarker.isCloseScope = true;
 				this.cachedMarkers.add(newMarker);
 				bestMarker = newMarker;
 				isCloseMatch = true;
 			} else {
 				// logger.info(String.format("no close match"));
 			}
 		}
 		//logger.info(String.format("  scanning for %d patterns", ((DoublePattern) currentScope.pattern).patterns.size()));
 		for (Pattern p : ((DoublePattern) currentScope.pattern).patterns) {
 			// System.out.printf("     scanning for %s (%s)\n", p.name, p.disabled);
 			if (p.disabled)
 				continue;
 			int positionNow = position;
 			int positionPrev = position-1;
 			Match match;
 			while ((match = scanForMatch(positionNow, p)) != null &&
 				   positionNow != positionPrev // some regex's have zero width (meta.selector.css)
 				) {
 				positionPrev = positionNow;
 				// logger.info(String.format("  matched: %s (%d-%d)", p.prettyName(), match.getCapture(0).start, match.getCapture(0).end));
 				Marker newMarker = new Marker();
 				newMarker.pattern = p;
 				newMarker.match = match;
				newMarker.from = match.getCapture(0).start;
 				newMarker.isCloseScope = false;
 				this.cachedMarkers.add(newMarker);
 				bestMarker = newMarker.bestOf(bestMarker);
 				positionNow = match.getByteCapture(0).end;
 			}
 		}
 		if (bestMarker != null) {
 			this.cachedMarkers.remove(bestMarker);
 			this.removePrecedingCachedMarkers(bestMarker);
 		}
 		return bestMarker;
 	}
 	
 	public Iterator<Marker> iterator() {
 		return new ScannerIterator(this);
 	}
 	
 	// TODO: implement this class for real
 	public class ScannerIterator implements Iterator<Marker> {
 		private Scanner scanner;
 		private Marker nextMarker;
 		
 		public ScannerIterator(Scanner scanner) {
 			this.scanner = scanner;
 		}
 		
 		public boolean hasNext() {
 			nextMarker = scanner.findNextMarker();
 			return (nextMarker != null);
 		}
 		
 		public Marker next() {
 			return nextMarker;
 		}
 		
 		public void remove() {
 			nextMarker = null;
 		}
 	}
 }
