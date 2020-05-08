 package kkckkc.jsourcepad.model.bundle.snippet;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import kkckkc.jsourcepad.model.*;
 import kkckkc.jsourcepad.model.Window;
 import kkckkc.jsourcepad.model.bundle.BundleItemSupplier;
 import kkckkc.jsourcepad.model.bundle.EnvironmentProvider;
 import kkckkc.jsourcepad.model.bundle.snippet.SnippetParser.*;
 import kkckkc.jsourcepad.util.io.ScriptExecutor;
 import kkckkc.jsourcepad.util.io.ScriptExecutor.Execution;
 import kkckkc.jsourcepad.util.io.UISupportCallback;
 import kkckkc.syntaxpane.model.Interval;
 import kkckkc.syntaxpane.util.Pair;
 
 import javax.swing.*;
 import javax.swing.event.DocumentEvent;
 import javax.swing.text.Keymap;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 
 public class Snippet {
 	
 	public static final int INCREMENT = 1;
 	public static final int DECREMENT = -1;
 	
 	private Collection<Node> nodes;
 	private BundleItemSupplier bundleItemSupplier;
 
 	// State
 	private Map<String, String> environment;
 	private List<SnippetConstituent> constituents;
 	private Buffer buffer;
 	private int currentTabStop;
 	
 	private boolean changeTrackingEnabled;
 	
 	public Snippet(String snippet, BundleItemSupplier bundleItemSupplier) {
 		this.nodes = new SnippetParser().parse(snippet);
 		this.bundleItemSupplier = bundleItemSupplier;
 	}
 
 	public void insert(final Window window, final Buffer buffer) {
 		this.buffer = buffer;
 		this.constituents = Lists.newArrayList();
 		this.environment = EnvironmentProvider.getEnvironment(window, bundleItemSupplier);
 		
         Map<Integer, Variable> primaryVariables = Maps.newHashMap();
         Map<Integer, String> variableDefaults = Maps.newHashMap();
 
         // We need to compile twice as defaults may not be defined for the first occurence
         // of a certain variable
         
         StringBuilder b = new StringBuilder();
 		for (SnippetParser.Node node : nodes) {
 			node.accept(new CompilingVisitor(window, b, primaryVariables, variableDefaults));
 		}
 
         b = new StringBuilder();
         constituents.clear();
 		for (SnippetParser.Node node : nodes) {
 			node.accept(new CompilingVisitor(window, b, primaryVariables, variableDefaults));
 		}
 
 		String str = b.toString();
 		int firstLineLength = str.indexOf('\n');
 		
 		String currentLine = buffer.getText(buffer.getCurrentLine());
 
         if (buffer.getSelection() != null) {
             buffer.remove(buffer.getSelection());
         }
 
 		int position = buffer.getInsertionPoint().getPosition();
 		buffer.insertText(position, str, getAnchors());
 		
 		if (firstLineLength >= 0) {
 			int endPosition = buffer.getInsertionPoint().getPosition();
 			int tabCount = buffer.getDoc().getTabManager().getTabCount(currentLine);
 			Interval snippetInterval = new Interval(position + firstLineLength + 1, endPosition);
 			buffer.shift(snippetInterval, tabCount);
 		}
 		
 		buffer.beginRestrictedEditing(new SnippetRestrictedEditor());
 		this.changeTrackingEnabled = true;
 
 		currentTabStop = -1;
 		SnippetConstituent nextConstituent = findNextTabStop();
 		if (nextConstituent != null) {
 			currentTabStop = nextConstituent.getTabStopId();
 			buffer.setSelection(new Interval(
 					nextConstituent.getBounds().getFirst().getPosition(),
 					nextConstituent.getBounds().getSecond().getPosition()));
 		} else {
 			buffer.endRestrictedEditing();
 		}
 	}
 
 	private Anchor[] getAnchors() {
 		List<Anchor> anchors = Lists.newArrayList();
 		for (SnippetConstituent c : constituents) {
 			anchors.add(c.getBounds().getFirst());
 			anchors.add(c.getBounds().getSecond());
 		}
 	    return anchors.toArray(new Anchor[anchors.size()]);
     }
 
 	private SnippetConstituent findWithTabStopId(int id, int increment) {
 		int maxTabStopId = findMaxTabStopId();			 
 		
 		while (id > 0 && id <= maxTabStopId) {
 			for (SnippetConstituent c : constituents) {
 				if (c.isCopy() || ! c.isActive()) continue;
 				if (c.getTabStopId() == id) return c;
 			}			
 			id += increment;
 		}
 		
 		// As fallback if next is not found, use index 0
 		if (id > maxTabStopId) {
 			for (SnippetConstituent c : constituents) {
 				if (c.isCopy()) continue;
 				if (c.getTabStopId() == 0)return c;
 			}
 		}
 		
 		return null;
 	}
 
 	private int findMaxTabStopId() {
 	    int maxTabStopId = 0;
 		for (SnippetConstituent c : constituents) {
 			if (c.isCopy() || ! c.isActive()) continue;
 			maxTabStopId = Math.max(maxTabStopId, c.getTabStopId());
 		}
 	    return maxTabStopId;
     }
 	
 	private SnippetConstituent findNextTabStop() {
 		if (currentTabStop == 0) return null;
 		if (currentTabStop == -1) {
 			return findWithTabStopId(1, INCREMENT);
 		} else {
 			return findWithTabStopId(currentTabStop + 1, INCREMENT);
 		}
 	}
 
 	private SnippetConstituent findPreviousTabStop() {
 		if (currentTabStop == 1) return null;
 		if (currentTabStop == 0) {
 			return findWithTabStopId(findMaxTabStopId(), DECREMENT);
 		} else {
 			return findWithTabStopId(currentTabStop - 1, DECREMENT);
 		}
 	}
 
 	class CompilingVisitor implements NodeVisitor {
 	    private final Window window;
 	    private final StringBuilder b;
         private Map<Integer, String> variableDefaults;
         private Map<Integer, Variable> primaryVariables;
 
         private CompilingVisitor(Window window, StringBuilder b, Map<Integer, Variable> primaryVariables, Map<Integer, String> variableDefaults) {
 		    this.window = window;
 		    this.b = b;
 		    this.primaryVariables = primaryVariables;
             this.variableDefaults = variableDefaults;
 	    }
 
 	    @Override
 	    public void visit(Literal literal) {
 	        b.append(literal.getString());
 	    }
 
 	    @Override
 	    public void visit(Variable variable) {
 	        Anchor start = new Anchor(b.length(), Anchor.Bias.LEFT); 
 	    	
 	    	if (! variable.isTabStop()) {
 	    		b.append(variable.evaluate(environment));
 	        }
 
 	    	for (Node n : variable.children()) {
 	    		n.accept(this);
 	    	}
 
 	        if (variable.isTabStop()) {
 	        	int tabStopId = Integer.parseInt(variable.getName());
 
                 // This is a copy variable if it is not defined as the primary for this tab stop
                 boolean isCopyVar = primaryVariables.get(tabStopId) != variable;
 
                 // If we don't have a default yet, try to establish one
 	        	if (! variableDefaults.containsKey(tabStopId)) {
 
                     String value = b.substring(start.getPosition());
 
                     // Do we have a value or not
                     if (! "".equals(value)) {
                         // If we do, record it and assume this is the primary variable
 	        		    variableDefaults.put(tabStopId, value);
                         primaryVariables.put(tabStopId, variable);
                     } else {
 
                         // If we don't but still haven't assigned a primary, assume this is the primary for now
                         if (! primaryVariables.containsKey(tabStopId)) {
                             primaryVariables.put(tabStopId, variable);
                         }
                     }
 
                 // If this is a copy var, just use any defaults recorded for the primary instance
 	        	} else if (isCopyVar) {
                     String s = variableDefaults.get(tabStopId);
 	        		b.append(s == null ? "" : s);
 	        	}
 
     	        Anchor end = new Anchor(b.length(), Anchor.Bias.RIGHT);
 	        	constituents.add(new SnippetConstituent(
 	        			variable,
 	        			new Pair<Anchor, Anchor>(start, end),
 	        			tabStopId,
 	        		    isCopyVar
 	        	));
 	        }
 	    }
 
 	    @Override
 	    public void visit(Script script) {
 	    	WindowManager wm = Application.get().getWindowManager();
 	    	
 	        ScriptExecutor scriptExecutor = new ScriptExecutor(script.getBody(), Application.get().getThreadPool());
 	        try {
 	            Execution ex = scriptExecutor.execute(new UISupportCallback(wm.getContainer(window)), 
 	            		new StringReader(""), 
 	            		environment);
 	            
 	            ex.waitForCompletion();
 	            
 	            b.append(ex.getStdout());
 	            
 	        } catch (IOException e) {
 	            throw new RuntimeException(e);
 	        } catch (InterruptedException e) {
 	            throw new RuntimeException(e);
 	        } catch (ExecutionException e) {
 	            throw new RuntimeException(e);
 	        }
 	    }
     }
 
 	class SnippetConstituent {
 		private Pair<Anchor, Anchor> bounds;
 		private int tabStopId;
 		private boolean copy;
 		private boolean active;
 		private Variable variable;
 
 		public SnippetConstituent(Variable variable, Pair<Anchor, Anchor> bounds, int tabStopId, boolean copy) {
 			this.variable = variable;
 	        this.bounds = bounds;
 	        this.tabStopId = tabStopId;
 	        this.copy = copy;
 	        this.active = true;
         }
 		
 		public boolean isActive() {
 	        return active;
         }
 		
 		public void setActive(boolean active) {
 	        this.active = active;
         }
 		
 		public Pair<Anchor, Anchor> getBounds() {
         	return bounds;
         }
 
 		private Interval getBoundsAsInterval() {
 	        return new Interval(bounds.getFirst().getPosition(), bounds.getSecond().getPosition());
         }
 
 		public int getTabStopId() {
         	return tabStopId;
         }
 
 		public boolean isCopy() {
         	return copy;
         }
 		
 		public String toString() {
 			return tabStopId + ", " + copy + ", " + bounds;
 		}
 
 		public void textChanged(DocumentEvent de) {
 			assert EventQueue.isDispatchThread() : "Only to be called from the EventQueue";
 			
 			if (copy) return;
 			
 			Interval i = getBoundsAsInterval();
 			
 			environment.put(Integer.toString(tabStopId), buffer.getText(i));
 			
 			for (final SnippetConstituent c : constituents) {
 				if (c.isCopy() && c.getTabStopId() == tabStopId) {
 					final String updatedValue = variable.evaluate(environment);
 
 					try {
 						changeTrackingEnabled = false;
 						buffer.replaceText(
 								c.getBoundsAsInterval(), 
 								updatedValue, null);
 					} finally {
 						changeTrackingEnabled = true;
 					}
 				}
 			}
 		}
 	}
 	
 	class SnippetRestrictedEditor implements Buffer.RestrictedEditor {
 		private static final String SHIFT_TAB = "shift TAB";
 		private static final String TAB = "TAB";
 
 		private Keymap keymap;
 
 		private Action tabAction;
 		private Action shiftTabAction;
 
 		@Override
 	    public void init(Keymap keymap) {
 		    this.keymap = keymap;
 
 		    // Save
 		    tabAction = keymap.getAction(KeyStroke.getKeyStroke(TAB));
 		    shiftTabAction = keymap.getAction(KeyStroke.getKeyStroke(SHIFT_TAB));
 
 		    // Remove
 	    	keymap.removeKeyStrokeBinding(KeyStroke.getKeyStroke(TAB));
 	    	keymap.removeKeyStrokeBinding(KeyStroke.getKeyStroke(SHIFT_TAB));
 
 		    // Install new actions
 	    	keymap.addActionForKeyStroke(KeyStroke.getKeyStroke(TAB), new AbstractAction() {
                 public void actionPerformed(ActionEvent e) {
             		SnippetConstituent nextConstituent = findNextTabStop();
             		if (nextConstituent != null) {
             			currentTabStop = nextConstituent.getTabStopId();
             			buffer.setSelection(new Interval(
             					nextConstituent.getBounds().getFirst().getPosition(),
             					nextConstituent.getBounds().getSecond().getPosition()));
             			if (currentTabStop == 0) {
             				destroy();
             			}
             		}
                 }
 	    	});
 	    	keymap.addActionForKeyStroke(KeyStroke.getKeyStroke(SHIFT_TAB), new AbstractAction() {
                 public void actionPerformed(ActionEvent e) {
             		SnippetConstituent nextConstituent = findPreviousTabStop();
             		if (nextConstituent != null) {
             			currentTabStop = nextConstituent.getTabStopId();
             			buffer.setSelection(new Interval(
             					nextConstituent.getBounds().getFirst().getPosition(),
             					nextConstituent.getBounds().getSecond().getPosition()));
             		}
                 }
 	    	});
 	    }
 
 		private void destroy() {
 	    	buffer.endRestrictedEditing();
 
 	    	keymap.removeKeyStrokeBinding(KeyStroke.getKeyStroke(TAB));
 	    	keymap.removeKeyStrokeBinding(KeyStroke.getKeyStroke(SHIFT_TAB));
 
 	    	keymap.addActionForKeyStroke(KeyStroke.getKeyStroke(TAB), tabAction);
 	    	if (shiftTabAction != null) {
 	    		keymap.addActionForKeyStroke(KeyStroke.getKeyStroke(SHIFT_TAB), shiftTabAction);
 	    	}
         }
 
 
 		@Override
 	    public void caretPositionChanged(int position) {
             if (! changeTrackingEnabled) return;
 		    if (isOutsideOfAnchor(position)) {
 		    	destroy();
 		    }
 	    }
 
 		@Override
 	    public void textChanged(final DocumentEvent de) {
 			if (! changeTrackingEnabled) return;
 
             int position = de.getOffset();
 		    if (isOutsideOfAnchor(position)) {
 		    	destroy();
 		    	return;
 		    }
 
 		    Interval changeInterval = new Interval(de.getOffset(), de.getOffset() + de.getLength());
 
 			for (final SnippetConstituent c : constituents) {
 				Pair<Anchor, Anchor> pair = c.getBounds();
 		    	if (c.getBoundsAsInterval().overlaps(changeInterval)) {
 					if (de.getLength() > (pair.getSecond().getPosition() - pair.getFirst().getPosition())) {
 			    		EventQueue.invokeLater(new Runnable() {
 	                        public void run() {
 	                        	c.setActive(false);
 	        		    		c.textChanged(de);
 	                        }
 			    		});
 					} else {
 			    		EventQueue.invokeLater(new Runnable() {
 	                        public void run() {
 	        		    		c.textChanged(de);
 	                        }
 			    		});
 					}
 		    	}
 			}
 		}
 
 		private boolean isOutsideOfAnchor(int position) {
 			for (SnippetConstituent c : constituents) {
 				Pair<Anchor, Anchor> pair = c.getBounds();
 		    	if (pair.getFirst().getPosition() <= position && pair.getSecond().getPosition() >= position) {
 		    		return false;
 		    	}
 		    }
 		    return true;
 	    }
 	}
 }
