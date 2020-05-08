 package vafusion.gui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 
 import jm.audio.Instrument;
 import jm.music.data.CPhrase;
 import jm.music.data.Note;
 import jm.music.data.Part;
 import jm.music.data.Score;
 import jm.util.Play;
 import vafusion.inst.PluckInst;
 
 @SuppressWarnings("serial")
 public class PianoComponent extends JComponent{
 	
 	private vafusion.data.Piano piano;
 	private vafusion.data.Score score;
 	private Pianel parent;
 	
 	PianoComponent(int x, int y, int height, int width, Pianel parent){
 		this.piano = new vafusion.data.Piano(x, y, height, width, 60);
 		this.parent = parent;
 		
 		for(KeyComponent k : piano.getBlackKeys())
 			this.add(k);
 
 		for(KeyComponent k : piano.getWhiteKeys())
 			this.add(k);
 		
 		//this.addMouseMotionListener(parent.charRecog.createMouseMotionListener());
 		
 		this.addMouseListener(new MouseListener(){
 
 			@Override
			public void mouseClicked(MouseEvent arg0) {
				
				PianoComponent.this.parent.charRecog.dispatchEvent(arg0);
				PianoComponent.this.parent.staff.dispatchEvent(arg0);
				
			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				KeyComponent k = piano.getKeyComponentAt(arg0.getX(), arg0.getY());
 				
 				if(k != null && arg0.getX() < PianoComponent.this.getX() + PianoComponent.this.getWidth()
 						&& arg0.getY() < PianoComponent.this.getY() + PianoComponent.this.getHeight()){
 					System.out.println("mouse press (Piano): x: " + arg0.getX() + " y: " + arg0.getY());
 					k.press();
 					PianoComponent.this.repaint();
 				} else {
 					
 					//not a key press
 					//arg0.consume();
 					PianoComponent.this.parent.charRecog.dispatchEvent(arg0);
 					PianoComponent.this.parent.staff.dispatchEvent(arg0);
 					
 				}
 				
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				
 				KeyComponent k = piano.getKeyComponentAt(arg0.getX(), arg0.getY());
 				
 				if(k != null){
 					System.out.println("mouse release (Piano): x: " + arg0.getX() + " y: " + arg0.getY());
 					k.unpress();
 					PianoComponent.this.repaint();
 					/*
 					 * just unpress ALL the keys. this will fix that stuck key bug. Also, clear the current chord.
 					 * 
 					 */
 					for(KeyComponent kc : piano.getBlackKeys())
 						kc.unpress();
 					for(KeyComponent kc : piano.getWhiteKeys())
 						kc.unpress();
 					
 					PianoComponent.this.repaint();
 				} else {
 					
 					PianoComponent.this.parent.charRecog.dispatchEvent(arg0);
 					PianoComponent.this.parent.staff.dispatchEvent(arg0);
 					
 				}
 				
 				
 			}
 			
 		});
 	}
 	
 	@Override
 	public void paint(Graphics g){
 		Graphics2D g2d = (Graphics2D) g;
 		
 		g2d.setColor(Color.WHITE);
 		g2d.fillRect(piano.getX(), piano.getY(), piano.getWidth(), piano.getHeight());
 		g2d.setColor(Color.black);
 		
 		for(KeyComponent k : piano.getWhiteKeys()){
 			k.paint(g2d);
 		}
 		
 		for(KeyComponent k : piano.getBlackKeys()){
 			k.paint(g2d);
 		}
 		
 		if(!piano.getCurrentChord().isEmpty()) {
 		    CPhrase chord = new CPhrase();
 		    chord.addChord(piano.getCurrentChord().toArray(new Note[0]));
 		    Part p = new Part("Piano", 0, 0);
 		    p.addCPhrase(chord);
 		    Instrument[] insts = new PluckInst[1];
 		    insts[0] = new PluckInst(2000);	            
 		    Score s = new Score(p);
 		    
 		    try {
 		    	Play.midi(s, false);
 		    } catch(IllegalThreadStateException e) {
 		    	
 		    	piano.getCurrentChord().removeAll(piano.getCurrentChord());
 		    	
 		    }
 		}
 	}
 	
 	public vafusion.data.Piano getPiano(){
 		return piano;
 	}
 
 	public vafusion.data.Score getScore() {
 		return score;
 	}
 
 	public void setScore(vafusion.data.Score score) {
 		this.score = score;
 	}
 
 }
