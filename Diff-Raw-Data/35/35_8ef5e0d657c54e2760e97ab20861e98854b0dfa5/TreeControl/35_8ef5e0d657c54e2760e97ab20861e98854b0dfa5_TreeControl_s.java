 package org.concord.waba.extra.ui;
 import waba.ui.*;
 import waba.fx.*;
 import waba.util.*;
 import waba.sys.*;
 import org.concord.waba.extra.event.*;
 
 public class TreeControl extends Container implements TreeModelListener
 {
 	public final static int DOUBLE_CLICK = 1555;
 
     TreeModel tm;
     Vector lines = new Vector();
 
     int indentSize = 10;
     int textHeight = 10;
 
     Font myFont = MainWindow.defaultFont;
     FontMetrics myFM;
 
     TreeLine selected = null;
 
 	public int	firstLine = 0;
 
 	Vector listeners = new Vector();
 	TreeControlEvent tev;
 
     public TreeControl(TreeModel tm)
     {
 		TreeNode curNode;
 		this.tm = tm;
 		tm.addTreeModelListener(this);
 		curNode = tm.getRoot();
 		lines.add(new TreeLine(curNode));	
 		myFM = getFontMetrics(myFont);
     }
 
 	public TreeNode getRootNode()
 	{
 		return tm.getRoot();
 	}
 
 	public int getAllLines(){
 		if(lines == null) return 0;
 		return lines.getCount();
 	}
 	public int maxVisLines(){
 		if(textHeight < 1) return 0;
 		return height / textHeight;
 	}
 	
 	public void addTreeControlListener(TreeControlListener l){
 		if(l == null) return;
 		if(listeners == null) return;
 		int index = listeners.find(l);
 		if(index >= 0) return;
 		listeners.add(l);
 	}
 	public void removeTreeControlListener(TreeControlListener l){
 		if(l == null) return;
 		if(listeners == null) return;
 		int index = listeners.find(l);
 		if(index < 0) return;
 		listeners.del(index);
 	}
 	
 	public void notifyListeners(int typeEvent){
 		if(listeners == null) return;
 		if(tev == null){
 			tev = new TreeControlEvent(this,typeEvent);
 		}else{
 			tev.type = typeEvent;
 		}
 		for(int i = 0; i < listeners.getCount(); i++){
 			TreeControlListener l = (TreeControlListener)listeners.get(i);
 			if(l != null){
 				l.treeControlChanged(tev);
 			}
 		}
 	}
 	
 	
 	
     boolean sRoot = true;
     public void showRoot(boolean sRoot)
     {
 		if(this.sRoot && !sRoot){
 			if(lines.getCount() >= 1){
 				collapse(0, lines);
 				((TreeLine)(lines.get(0))).depth = -1;
 				expand(0, lines);
 				lines.del(0);	    
 			}
 		}
 		this.sRoot = sRoot;
 
     }
 
     public void onPaint(Graphics g)
     {
 		int numLines = lines.getCount();
 		TreeLine line;
 		int curY = 0;
 
 
 		g.setColor(0,0,0);
 		g.drawRect(0,0,width,height-1);
		g.setClip(1,1,width-2,height-3);
 
 		g.setColor(255,255,255);
 		g.fillRect(1,1,width-2,height-3);
 		g.setColor(0,0,0);
 
 		if(numLines == 0) return;
 
 		g.translate(1,1);
 		for(int i = firstLine; i<numLines; i++){
 			line = (TreeLine)lines.get(i);
 			if(line.node == null) {
 				g.drawText("..null..", (line.depth+1)*indentSize, curY);
 			} else {
 				if(!line.node.isLeaf()){
 					drawTwist(g, (line.depth)* indentSize, curY, line.expanded);
 				}
 				if(!line.selected){
 					if(line.node.toString() == null){
 						g.drawText("..no_name..", (line.depth+1)*indentSize, curY);
 					} else {
 						g.drawText(line.node.toString(), (line.depth+1)*indentSize, curY);
 					}
 				} else {
 					int tWidth = myFM.getTextWidth(line.node.toString());
 					g.setColor(0,0,0);
 					g.fillRect((line.depth+1)*indentSize, curY, tWidth, textHeight);
 					g.setColor(255,255,255);
 					if(line.node.toString() == null){
 						g.drawText("..no_name..", (line.depth+1)*indentSize, curY);
 					} else {
 						g.drawText(line.node.toString(), (line.depth+1)*indentSize, curY);
 					}
 					g.setColor(0,0,0);
 				}
 			}
 			curY += textHeight;
 		}
 		g.translate(-1,-1);
 
     }
 
     public void setSelected(TreeNode node)
     {
 		if(selected != null){
 			selected.selected = false;
 			selected = null;
 		}
 
 		if(node == null) return;
 	
 		TreeLine line = null;
 		for(int i=0; i < lines.getCount(); i++){
 			line = (TreeLine)lines.get(i);
 			if(line.node == node){
 				line.selected = true;
 				selected = line;
 				break;
 			}
 		}
 
 		repaint();
     }
 
     public TreeNode getSelected()
     {
 		if(selected == null) return null;
 
 		return selected.node;
     }
 
 	public TreeLine getSelectedLine(){
 		if(selected == null) return null;
 		return selected;
 	}
 
     public TreeNode getSelectedParent()
     {
 		if(selected == null) return null;
 
 		return selected.parent;
     }
     
 
 	Timer timer;
 
     public void onEvent(Event e)
     {
 		if(e instanceof PenEvent && e.type == PenEvent.PEN_DOWN){
 			PenEvent pe = (PenEvent)e;
 			int lineIndex = pe.y / textHeight + firstLine;
 			if(lineIndex >= lines.getCount()){
 				if(selected != null) selected.selected = false;
 				selected = null;
 				repaint();
 				return;
 			}
 
 			TreeLine line = (TreeLine)lines.get(lineIndex);
 			if(line == null) return;
 			int lineOff = line.depth*indentSize;
 			if(pe.x >= lineOff && pe.x < lineOff + indentSize){
 				Graphics g = createGraphics();
 				if(!line.node.isLeaf()){
 					drawDownTwist(g, (line.depth)* indentSize + 1, (lineIndex-firstLine)*textHeight+1, 
 							  line.expanded);
 				}
 				g.free();
 				if(line.expanded) collapse(lineIndex, lines);
 				else expand(lineIndex, lines);
 				repaint();
 			} else if(pe.x >= lineOff + indentSize){
 				if(selected != null && line != selected){
 					selected.selected = false;
 					if(timer != null) {
 						removeTimer(timer);
 						timer = null;
 					}						
 				}
 				if(line.selected == true){
 					if(timer != null){
 						removeTimer(timer);
 						timer = null;
 						// send double click event
 						postEvent(new ControlEvent(DOUBLE_CLICK, this));
 						return;
 					}
 					timer = addTimer(750);
 					return;
 				}
 				line.selected = true;
 				selected = line;
 				timer = addTimer(750);
 				// Send event;
 				repaint();
 			}
 		} else if(e instanceof ControlEvent && e.type == ControlEvent.TIMER){
 			removeTimer(timer);
 			timer = null;
 		}else if (e instanceof KeyEvent){
 			onKeyEvent((KeyEvent)e);
 		}else if(e instanceof PenEvent && e.type == PenEvent.PEN_DRAG){
 			PenEvent pe = (PenEvent)e;
 			if(pe.y > height){
 				int nVisibleLines = height / textHeight;
 				if(firstLine + nVisibleLines < lines.getCount()){
 					firstLine++;
 					repaint();
 				}
 			}else if(pe.y < 0){
 				if(firstLine > 0){
 					firstLine--;
 					repaint();
 				}
 			}
 		}
 
 
     }
 
 
 	public void onKeyEvent(KeyEvent e){
 	}
 
 	
     static int [] coord1 = {1,7,4};
     static int [] coord2 = {1,1,8};
     void drawDownTwist(Graphics g, int x ,int y, boolean expanded)
     {
 		g.translate(x,y);		
 		g.setColor(0,0,0);
 		g.fillRect(0,0,9,9);
 		g.setColor(255,255,255);
 		if(expanded){
 			g.fillPolygon(coord1, coord2, 3);
 		} else {
 			g.fillPolygon(coord2, coord1, 3);
 		}
 		g.setColor(0,0,0);
 		g.translate(-x,-y);
     }
 
     void drawTwist(Graphics g, int x ,int y, boolean expanded)
     {
 		g.translate(x,y);
 		if(expanded){
 			g.fillPolygon(coord1, coord2, 3);
 		} else {
 			g.fillPolygon(coord2, coord1, 3);
 		}
 		g.translate(-x,-y);
     }
 
     void expand(int index, Vector lines)
     {
 		TreeLine line = (TreeLine)lines.get(index);
 		TreeLine child;
 		TreeNode [] children;
 
 		// send event
 
 		if(!line.node.isLeaf() && !line.expanded){
 			line.expanded = true;
 			children = line.node.childArray();
 			for(int i=0; i<children.length; i++){
 				child = new TreeLine(children[i]);
 				child.depth = line.depth+1;
 				child.parent = line.node;
 				child.lineParent = line;
 				lines.insert(index+i+1, child);		
 			}
 			notifyListeners(TreeControlEvent.TC_EXPAND);
 		}
 	
     }
 
     void collapse(int index, Vector lines)
     {
 		TreeLine line = (TreeLine)lines.get(index);
 		int curIndex = index+1;
 		TreeLine next;
 
 		// send event
 
 		if(!line.node.isLeaf() && line.expanded){
 			line.expanded = false;
 			while(curIndex < lines.getCount()){
 				next = (TreeLine)lines.get(curIndex);
 				if(next.depth <= line.depth){
 					break;
 				}
 				lines.del(curIndex);
 			}
 			notifyListeners(TreeControlEvent.TC_COLLAPSE);
 		}
 	
     }
 
     public void treeNodeInserted(TreeNode node, TreeNode parent)
     {
 		TreeLine line;
 		int i;
 
 		if(!sRoot &&
 		   parent == tm.getRoot()){
 			reparse();
 		} else {
 			for(i=0; i < lines.getCount(); i++){
 				line = (TreeLine)lines.get(i);
 				if(line.node == parent){
 					if(line.expanded){
 						reparse();
 						//collapse(i,lines);
 						//expand(i,lines);
 					} else {
 						expand(i,lines);
 					}
 					break;
 				}
 			}
 		}
 
 		// This calls repaint
 		setSelected(node);
     }
 
     public void reparse()
     {
 		Vector newLines = new Vector();
 		int i, j;
 		int numLines = lines.getCount();
 		TreeLine curLine;
 		TreeLine newLine;
 		TreeLine newSelected = null;
 
 		TreeNode curNode = tm.getRoot();
 		newLine = new TreeLine(curNode);
 
 		newLines.add(newLine);
 
 		if(!sRoot){
 			newLine.expanded = false;
 			newLine.depth = -1;
 			expand(0, newLines);
 			newLines.del(0);
 		}
  
 
 		while(true){
 	    
 			for(i=0; i < newLines.getCount(); i++){
 				newLine = (TreeLine)(newLines.get(i));
 				for(j=0; j < numLines; j++){
 					curLine = (TreeLine)(lines.get(j));
 					if(newLine.node.equals(curLine.node)){
 						if(curLine.selected == true) {
 							newLine.selected = true;
 							newSelected = newLine;
 						}
 						if(curLine.expanded == true &&
 						   newLine.expanded != true){
 							expand(i, newLines);
 							break;
 						}
 					}
 				}
 				if(j != numLines) break;
 			}
 			if(i == newLines.getCount()) break;
 		}
 
 		lines = newLines;	    
 		selected = newSelected;
     }
 
     public void treeModelChanged()
     {
 		reparse();
 		repaint();
 		notifyListeners(TreeControlEvent.TC_CHANGED);
     }
 }
