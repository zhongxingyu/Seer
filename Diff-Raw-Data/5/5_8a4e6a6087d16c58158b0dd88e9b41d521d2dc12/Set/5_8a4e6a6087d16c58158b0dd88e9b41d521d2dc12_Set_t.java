 package sim.functions;
 
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JOptionPane;
 
 import sim.gui.elements.GuiElement;
 import sim.gui.elements.GuiFunction;
 import sim.structures.Array;
 import sim.structures.LinkedList;
 import sim.structures.Tree;
 import sim.structures.Variable;
 
 public class Set implements ActionListener{
 	Variable v;
 	Variable i;
 	Object l;
 	boolean singleChar;
 	
 	GuiFunction gui;
 	public GuiElement getGuiElement(){
 		return gui;
 	}
 	public Object getTarget() {
 		return l;
 	}
 	public void setTarget(Object l) {
 		this.l = l;
 	}
 	public Variable getSourceVariable() {
 		return v;
 	}
 	public void setSourceVariable(Variable v) {
 		this.v = v;
 	}
 	public Variable getIndexVariable() {
 		return i;
 	}
 	public void setIndexVariable(Variable i) {
 		this.i = i;
 	}
 	public boolean getSingleChar(){
 		return singleChar;
 	}
 	public void setSingleChar(boolean singleChar){
 		this.singleChar = singleChar;
 	}
 
 	public Set(Rectangle bounds, boolean singleChar){
 		//TODO add direction here
 		gui = new GuiFunction(bounds,"Set");
 		gui.getButton().addActionListener(this);
 		this.v = null;
 		this.l = null;
 		this.singleChar=singleChar;
 	}
 	/**
 	 * Constructor.
 	 */
 	public Set(Rectangle bounds, Array l, Variable v, Variable i, boolean singleChar) {
 		gui = new GuiFunction(bounds,"Set");
 		gui.getButton().addActionListener(this);
 		this.l=l;
 		this.v=v;
 		this.singleChar=singleChar;
 	}
 	public Set(Rectangle bounds, LinkedList l, Variable v, Variable i, boolean singleChar) {
 		gui = new GuiFunction(bounds,"Set");
 		gui.getButton().addActionListener(this);
 		this.l=l;
 		this.v=v;
 		this.singleChar=singleChar;
 	}
 	public Set(Rectangle bounds, Tree l, Variable v, Variable i, boolean singleChar) {
 		gui = new GuiFunction(bounds,"Set");
 		gui.getButton().addActionListener(this);
 		this.l=l;
 		this.i=i;
 		this.v=v;
 		this.singleChar=singleChar;
 	}
 	/**
 	 * Will remove the first char from the input string and append it to the output string. 
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(v != null){
 			if(l instanceof Variable){
 				if(singleChar){
 					String val = v.getValue();
 
 					if(val.length()>0){
 					String ch = val.substring(0, 1);
 					v.setValue(val.substring(1));
 					String tarVal = ((Variable) l).getValue();
 					tarVal += ch;
 					((Variable) l).setValue(tarVal);
 					}
 				}
 				else {
 					v.setValue(((Variable) l).getValue());
 				}
 			}else if(i != null && l instanceof Tree){
 
 				try{
 						int index = Integer.parseInt(i.getValue());
 						((Tree)l).setValueAt(index, v.getValue());
 						
 				}catch(NumberFormatException nfe){
 					JOptionPane.showConfirmDialog(gui, "Illegal character: you can only enter numbers.");
 				}
 			}else if(i != null && l instanceof LinkedList){
 
 				try{
 					int index = Integer.parseInt(i.getValue());
					String s = v.getValue();
 					if(s!=null)
						((LinkedList)l).setValueAt(index,s);
 
 				}catch(NumberFormatException nfe){
 					JOptionPane.showConfirmDialog(gui, "Illegal character: you can only enter numbers.");
 				}
 			}
 			else if(i != null && l instanceof Array){
 					if(i.getValue().indexOf(",") > 0){
 						String[] index = i.getValue().split(",");
 						try{
 							int indexY = Integer.parseInt(index[0]);
 							int indexX = Integer.parseInt(index[1]);
 
 							if(((Array) l).getDimensions() == 2){
 								((Array) l).setValueAt(v.getValue(), indexY, indexX);
 							}else{
 								((Array) l).setValueAt(v.getValue(), indexY);
 							}
 						}catch(Exception nfe){
 							JOptionPane.showMessageDialog(gui, "Illegal character: you can only enter numbers separated by a comma (,)");
 						}
 					}else{
 						try{
 							int indexY = Integer.parseInt(i.getValue());
 							((Array) l).setValueAt(v.getValue(), indexY);
 						}catch(Exception nfe){
 							JOptionPane.showMessageDialog(gui, "Illegal character: you can only enter numbers separated by a comma (,)");
 						}
 					}
 				}
 		}
 	}
 }
