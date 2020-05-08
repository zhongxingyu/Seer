 package ddddbb.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import ddddbb.game.Level;
 import ddddbb.gen.BoolModel;
 import ddddbb.gen.DiAxisModel;
 import ddddbb.gen.IntStringModel;
 import ddddbb.math.AOP;
 
 public class KeyControl implements KeyListener {
 	public static class LookupTable3int<T> {
 		public int c1,c2,c3;
 		Object[][][] data;
 		public LookupTable3int(int _c1,int _c2, int _c3) {
 			c1=_c1;c2=_c2;c3=_c3;
 		}
 
 		public void put(int i1,int i2, int i3, T val) {
 			assert i1 < c1 && i2 < c2 & i3 < c3;
 			if (data==null) {
 				data=new Object[c1][][];
 			}
 			if (data[i1] == null) {
 				data[i1] = new Object[c2][]; 
 			}
 			if (data[i1][i2] == null) {
 				data[i1][i2] = new Object[c3];
 			}
 			data[i1][i2][i3] = val;
 		}
 		@SuppressWarnings("unchecked")
 		public T get(int i1,int i2, int i3) {
 			if (i1 >= c1) { return null; }
 			if (i2 >= c2) { return null; }
 			if (i3 >= c3) { return null; }
 			if (data==null) { return null; }
 			if (data[i1]==null) { return null; }
 			if (data[i1][i2] == null) { return null;}
 			return (T)data[i1][i2][i3];
 		}
 		public boolean has(int i1, int i2) {
 			if (i1>=c1) return false; 
 			if (i2>=c2) return false; 
 			if (data==null) return false;
 			if (data[i1]==null) return false;
 			if (data[i1][i2]==null) return false;
 			return true;
 		}
 	}
 
 	public class LookupTable2int<T extends Object> {
 		public int c1,c2;
 		T[][] data;
 		public LookupTable2int(int _c1,int _c2) {
 			c1=_c1;c2=_c2;
 		}
 
 		@SuppressWarnings("unchecked")
 		public void put(int i1,int i2, T val) {
 			assert i1 < c1 && i2 < c2;
 			if (data==null) {
 				data=(T[][])new Object[c1][];
 			}
 			if (data[i1] == null) {
 				data[i1] = (T[])new Object[c2]; 
 			}
 			data[i1][i2] = val;
 		}
 		public T get(int i1,int i2) {
 			if (i1 >= c1) { return null; }
 			if (i2 >= c2) { return null; }
 			if (data==null) {
 				return null;
 			}
 			if (data[i1]==null) {
 				return null;
 			}
 			return data[i1][i2];
 		}
 	}
 
 	public KeyControl(final Level scene, BoolModel showGoal, DiAxisModel viewRotXAxis12, IntStringModel d3ViewRotAxis, IntStringModel dim34) {
 		int maxMod = 4;
 		int maxKey = 100;
 		singleKeyAssignment = new LookupTable2int<ActionListener>(maxMod,maxKey);
 		LookupTable2int<ActionListener> s = singleKeyAssignment;
 		int SHIFT = InputEvent.SHIFT_MASK;
 		int CTRL = InputEvent.CTRL_MASK;
 
 		UIAction ua = new UIAction(viewRotXAxis12,d3ViewRotAxis,dim34,scene);
 		
 		s.put(0,37,scene.transSelectedAction(-1)); // <-
 		s.put(0,39,scene.transSelectedAction(+1)); // ->
 		s.put(0,40,scene.transSelectedAction(-2)); // v
 		s.put(0,38,scene.transSelectedAction(+2)); // ^
		s.put(SHIFT,37,new ActionListener() { public void actionPerformed(ActionEvent e) {scene.transCamByHoriz(-0.1);}}); 
		s.put(SHIFT,39,new ActionListener() { public void actionPerformed(ActionEvent e) {scene.transCamByHoriz(+0.1);}}); 
		s.put(SHIFT,40,new ActionListener() { public void actionPerformed(ActionEvent e) {scene.transCamByVertic(-0.1);}});
		s.put(SHIFT,38,new ActionListener() { public void actionPerformed(ActionEvent e) {scene.transCamByVertic(+0.1);}});
 		s.put(CTRL,37,new ActionListener() { public void actionPerformed(ActionEvent e) {scene.rotCamByHoriz(-AOP.deg);}});
 		s.put(CTRL,39,new ActionListener() { public void actionPerformed(ActionEvent e) {scene.rotCamByHoriz(+AOP.deg);}});
 		s.put(CTRL,40,new ActionListener() { public void actionPerformed(ActionEvent e) {scene.rotCamByVertic(-AOP.deg);}});
 		s.put(CTRL,38,new ActionListener() { public void actionPerformed(ActionEvent e) {scene.rotCamByVertic(+AOP.deg);}});
 		
 //		s.put(CTRL,85,ua.transCam(1)); //u
 //		s.put(CTRL,73,ua.transCam(2)); //i
 //		s.put(CTRL,79,ua.transCam(3)); //o
 //		s.put(CTRL,80,ua.transCam(4)); //p
 //
 //		s.put(CTRL|SHIFT,85,ua.transCam(-1)); //u
 //		s.put(CTRL|SHIFT,73,ua.transCam(-2)); //i
 //		s.put(CTRL|SHIFT,79,ua.transCam(-3)); //o
 //		s.put(CTRL|SHIFT,80,ua.transCam(-4)); //p
 //		
 //		s.put(CTRL,77,ua.transCam(-1)); //m
 //		s.put(CTRL,44,ua.transCam(-2)); //,
 //		s.put(CTRL,46,ua.transCam(-3)); //.
 //		s.put(CTRL,47,ua.transCam(-4)); ///
 		
 		s.put(0,85,scene.transSelectedAction(1)); //u
 		s.put(0,73,scene.transSelectedAction(2)); //i
 		s.put(0,79,scene.transSelectedAction(3)); //o
 		s.put(0,80,scene.transSelectedAction(4)); //p
 
 		s.put(0,77,scene.transSelectedAction(-1)); //m
 		s.put(0,44,scene.transSelectedAction(-2)); //,
 		s.put(0,46,scene.transSelectedAction(-3)); //.
 		s.put(0,47,scene.transSelectedAction(-4)); ///
 		
 		s.put(0,49,scene.compounds.setSelectedAction(0)); //1
 		s.put(0,50,scene.compounds.setSelectedAction(1)); //2
 		s.put(0,51,scene.compounds.setSelectedAction(2)); //3
 		s.put(0,52,scene.compounds.setSelectedAction(3)); //4
 		s.put(0,53,scene.compounds.setSelectedAction(4)); //5
 		s.put(0,54,scene.compounds.setSelectedAction(5)); //6
 		s.put(0,55,scene.compounds.setSelectedAction(6)); //7
 		s.put(0,56,scene.compounds.setSelectedAction(7)); //8
 		s.put(0,57,scene.compounds.setSelectedAction(8)); //9
 		s.put(0,71,scene.compounds.nextAction); //g
 		s.put(SHIFT,71,scene.compounds.prevAction); //G
 		s.put(0,32,showGoal.toggleAction); //SPACE
 		s.put(0,66,scene.combineAction); //b
 		s.put(0,20,dim34.cyclicNextAction);//caps lock
 
 		doubleKeyAssignment = new LookupTable3int<ActionListener>(maxMod,maxKey,maxKey);
 		LookupTable3int<ActionListener> d = doubleKeyAssignment;
 		
 		d.put(0,70,68,scene.rotSelectedAction(1,2)); //f,d
 		d.put(0,70,83,scene.rotSelectedAction(1,3)); //f,s
 		d.put(0,70,65,scene.rotSelectedAction(1,4)); //f,a
 		d.put(0,68,83,scene.rotSelectedAction(2,3)); //d,s
 		d.put(0,68,65,scene.rotSelectedAction(2,4)); //d,a
 		d.put(0,83,65,scene.rotSelectedAction(3,4)); //s,a
 		d.put(0,68,70,scene.rotSelectedAction(2,1)); //d,f
 		d.put(0,83,70,scene.rotSelectedAction(3,1)); //s,f
 		d.put(0,65,70,scene.rotSelectedAction(4,1)); //a,f
 		d.put(0,83,68,scene.rotSelectedAction(3,2)); //s,d
 		d.put(0,65,68,scene.rotSelectedAction(4,2)); //a,d
 		d.put(0,65,83,scene.rotSelectedAction(4,3)); //a,s
 		
 		d.put(0,74,75,ua.rotCam(1,2)); //j,k
 		d.put(0,74,76,ua.rotCam(1,3)); //j,l
 		d.put(0,74,59,ua.rotCam(1,4)); //j,;
 		d.put(0,75,76,ua.rotCam(2,3)); //k,l
 		d.put(0,75,59,ua.rotCam(2,4)); //k,;
 		d.put(0,76,59,ua.rotCam(3,4)); //l,;
 		d.put(0,75,74,ua.rotCam(2,1)); //k,j
 		d.put(0,76,74,ua.rotCam(3,1)); //l,j
 		d.put(0,59,74,ua.rotCam(4,1)); //;,j
 		d.put(0,76,75,ua.rotCam(3,2)); //l,k
 		d.put(0,59,75,ua.rotCam(4,2)); //;,k
 		d.put(0,59,76,ua.rotCam(4,3)); //;,l
 
 		d.put(0,82,69,ua.set4dRotAxes(1,2)); //r,e
 		d.put(0,69,82,ua.set4dRotAxes(1,2)); //r,e
 		d.put(0,82,87,ua.set4dRotAxes(1,3)); //r,w
 		d.put(0,87,82,ua.set4dRotAxes(1,3)); //r,w
 		d.put(0,82,81,ua.set4dRotAxes(1,4)); //r,q
 		d.put(0,81,82,ua.set4dRotAxes(1,4)); //r,q
 		d.put(0,69,87,ua.set4dRotAxes(2,3)); //e,w
 		d.put(0,87,69,ua.set4dRotAxes(2,3)); //e,w
 		d.put(0,69,81,ua.set4dRotAxes(2,4)); //e,q
 		d.put(0,81,69,ua.set4dRotAxes(2,4)); //e,q
 		d.put(0,87,81,ua.set4dRotAxes(3,4)); //w,q
 		d.put(0,81,87,ua.set4dRotAxes(3,4)); //w,q
 
 	}
 	
 	public static LookupTable2int<ActionListener> singleKeyAssignment;
 	public static LookupTable3int<ActionListener> doubleKeyAssignment;
 	
 	public int pressedKey = -1;
 	
 	public synchronized void keyPressed(KeyEvent e) {
 		if (doubleKeyAssignment.has(e.getModifiers(),e.getKeyCode()) &&	pressedKey==-1) {
 			pressedKey = e.getKeyCode();
 			return;
 		}
 		
 		ActionListener p = singleKeyAssignment.get(e.getModifiers(),e.getKeyCode());
 		if (p!=null) {
 			p.actionPerformed(null);
 			return;
 		}
 		else if (pressedKey!=-1) {
 			p = doubleKeyAssignment.get(e.getModifiers(),pressedKey,e.getKeyCode());
 			if (p!=null) {
 				p.actionPerformed(null);
 				return;
 			}
 		}
 		
 		switch (e.getKeyCode()) {
 		case 16: //Caps
 		case 17: //shift
 		case 18:  //CRTL
 		case 524: //Win
 		case 525: //Context
 			return; 	
 		}
 		System.out.println("Keycode " + e.getKeyCode() + " not assigned.");
 		//System.out.println(e.getKeyLocation());
 	}
 	
 	public synchronized void keyReleased(KeyEvent e) {
 		if (pressedKey == e.getKeyCode()) {
 			pressedKey = -1;
 		}
 	}
 	
 	public void keyTyped(KeyEvent e) {
 		//adapter
 	}
 }
