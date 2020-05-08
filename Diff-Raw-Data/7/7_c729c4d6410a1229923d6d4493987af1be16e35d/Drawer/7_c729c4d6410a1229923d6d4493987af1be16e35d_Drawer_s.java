 package myDrawer;
 import javax.swing.JFrame;
 
 import myTypeData.Type;
 import myTypeData.TypeHistogram;
 
 public class Drawer {
 
 	JFrame jfrm;
 	TypeHistogram h;
 	private int height = 420;
 	private int width = 420;
 
 	public Drawer(){
 		jfrm = new JFrame();
 		jfrm.setSize(width, height);
 		h = new TypeHistogram();
 		h.addType(new Type("B1",10));
 		h.addType(new Type("B2",15));
 		h.addType(new Type("B3",7));
 		h.addType(new Type("B4",25));
 		h.addType(new Type("B5",11));
 		h.addType(new Type("B6",16));
 		h.addType(new Type("B7",1));
 		h.addType(new Type("B8",19));
 		h.addType(new Type("B9",12));
 		h.addType(new Type("B10",5));
 		jfrm.add(h.draw());
 		jfrm.setVisible(true);
 	}	
 }
