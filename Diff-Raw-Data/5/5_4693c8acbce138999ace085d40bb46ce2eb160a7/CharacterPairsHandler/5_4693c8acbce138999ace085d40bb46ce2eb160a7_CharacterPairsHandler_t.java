 package kkckkc.jsourcepad.model;
 
 import java.awt.Color;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DocumentFilter;
 
 import kkckkc.jsourcepad.model.Anchor.Bias;
 import kkckkc.jsourcepad.model.Buffer.HighlightType;
 import kkckkc.jsourcepad.model.bundle.BundleManager;
 import kkckkc.jsourcepad.model.bundle.PrefKeys;
 import kkckkc.syntaxpane.model.Interval;
 import kkckkc.syntaxpane.style.StyleBean;
 
 import com.google.common.collect.MapMaker;
 
 public class CharacterPairsHandler extends DocumentFilter {
     private final Buffer buffer;
 	private final Map<Anchor, Boolean> anchors;
 	private AnchorManager anchorManager;
 
     public CharacterPairsHandler(Buffer buffer, AnchorManager anchorManager) {
 	    this.buffer = buffer;
 	    this.anchorManager = anchorManager;
 	    
 		this.anchors = new MapMaker().expiration(30, TimeUnit.SECONDS).makeMap();
     }
     
     @Override
     public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
             throws BadLocationException {
     	if (text.length() != 1) {
     		fb.replace(offset, length, text, attrs);
     		return;
     	}
 
 	    InsertionPoint insertionPoint = buffer.getInsertionPoint();
 	    
 	    List<List<String>> pairs = (List) Application.get().getBundleManager().getPreference(
 	    		PrefKeys.PAIRS_SMART_TYPING, insertionPoint.getScope());
 	    
 	    if (pairs == null) {
 	    	fb.replace(offset, length, text, attrs);
 	    	return;
 	    }
 	    
 
 	    boolean handled = false;
     	if (length > 0) {
 	        for (final List<String> pair : pairs) {
 	        	String start = pair.get(0);
 	        	String end = pair.get(1);
 	        	if (text.equals(start)) {
 	        		fb.replace(offset, 0, start, attrs);
 	        		fb.replace(offset + length + 1, 0, end, attrs);
 	        		
 	        		// TODO: Decide what to do with selection
		        	handled = true;
		        	break;
 	        	}
 	        }    		
     	} else {
 	        for (final List<String> pair : pairs) {
 	        	String start = pair.get(0);
 	        	String end = pair.get(1);
 	        	
 	        	if (start.equals(text)) {
 	            	Anchor a = new Anchor(offset + 1, Bias.RIGHT);
 	            	anchors.put(a, Boolean.TRUE);
 	                    	
 	    	    	fb.replace(offset, length, start + end, attrs);
 	                buffer.setSelection(Interval.createEmpty(offset + 1));
 	                
 	                anchorManager.addAnchor(a);
 	                
 	                handled = true;
 	                break;
 	                
 	        	} else if (end.equals(text)) {
 	        		String charToTheRight = buffer.getText(Interval.createWithLength(offset, 1));
 	            	if (charToTheRight.equals(end)) {
 	            		Iterator<Map.Entry<Anchor, Boolean>> it = anchors.entrySet().iterator();
 	            		while (it.hasNext()) {
 	            			Map.Entry<Anchor, Boolean> en = it.next();
 	            			Anchor a = en.getKey();
 	            			if (a.isRemoved()) continue;
 	            			
 	            			if (a.getPosition() == offset) {
 	                    		it.remove();
 	                            buffer.setSelection(Interval.createEmpty(offset + 1));
 	                    		
 	                            handled = true;
 	                            break;
 	            			}
 	            		}
 	            	}
 	        	}
 	        }
     	}
     	
         if (! handled) {
         	fb.replace(offset, length, text, attrs);
         }
     }
 
     
     public void highlight() {
 	    InsertionPoint insertionPoint = buffer.getInsertionPoint();
 		if (insertionPoint.getPosition() == 0) return; 
 
 	    BundleManager bundleManager = Application.get().getBundleManager();
 	    List<List<String>> pairs = (List) bundleManager.getPreference(PrefKeys.PAIRS_HIGHLIGHT, insertionPoint.getScope());
 
 	    if (pairs == null) return;
 	    
 	    for (List<String> p : pairs) {
 	    	char start = p.get(0).charAt(0);
 	    	char end = p.get(1).charAt(0);
 	    	
 	    	char cur = buffer.getText(Interval.createWithLength(insertionPoint.getPosition() - 1, 1)).charAt(0);
 	    	
     		int pos = insertionPoint.getPosition();
     		if (end == cur) {
     			int level = 1, found = -1;
     			char[] s = buffer.getText(Interval.createWithLength(0, pos - 1)).toCharArray();
     			for (int f = 0; f < s.length; f++) {
     				char c = s[s.length - f - 1];
     				if (c == start) level--;
     				if (c == end) level++;
 
     				if (level == 0) {
     					found = f;
     					break;
     				}
     			}
 
     			if (found == -1) {
 	    			Interval i = Interval.createWithLength(pos - 1, 1);
 	    			buffer.highlight(i, HighlightType.Box, new StyleBean(null, null, Color.red), true);
     				return;
     			}
     			
     			Interval i = Interval.createWithLength(pos - found - 2, 1);
     			buffer.highlight(i, HighlightType.Box, new StyleBean(null, null, Color.gray), true);
     		} else if (start == cur) {
     			if (buffer.getLength() <= pos) return;
 
     			int level = 1, found = -1;
     			char s[] = buffer.getText(Interval.createWithLength(pos, buffer.getLength() - pos)).toCharArray();
     			for (int f = 0; f < s.length; f++) {
     				char c = s[f];
     				if (c == start) level++;
     				if (c == end) level--;
     				
     				if (level == 0) {
     					found = f;
     					break;
     				}
     			}
 
     			if (found == -1) {
 	    			Interval i = Interval.createWithLength(pos - 1, 1);
 	    			buffer.highlight(i, HighlightType.Box, new StyleBean(null, null, Color.red), true);
     				return;
     			}
     			
     			Interval i = Interval.createWithLength(pos + found, 1);
     			buffer.highlight(i, HighlightType.Box, new StyleBean(null, null, Color.gray), true);
     		}
 	    }
     }
 }
