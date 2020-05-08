 import java.awt.Component;
 import java.awt.event.*;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 
 /**
  *
  */
 
 public class MenuEditor extends JPanel {
 
 
 	private static final long serialVersionUID = 1L;
 	static JFrame frame;
 	    static String wand = new String("Wand");
 	    static String start = new String("Start");
 	    static String ziel = new String("Ziel");
 	    static String boss = new String("Boss");
 	    static String q = new String("Questgegenstand");
 	    static String np = new String("NP");
 	    static String onea = new String("Level 1");
 	    static String twoa = new String("Level 2");
 	    static String threea = new String("Level 3");
 	    static String oneb = new String("Raum 1");
 	    static String twob = new String("Raum 2");
 	    static String threeb = new String("Raum 3");
 	    JButton save, cancel, upload;
 	    static String[][] karte;
 	   
 	    
 	    
 	    public MenuEditor() {
 		super(true);
 		
 
 		save = new JButton("Speichern");
 		cancel = new JButton("Abbrechen");
 		upload = new JButton("Hochladen");
 		
 		// Create the buttons.
 		JRadioButton X = new JRadioButton(wand);
 		X.setActionCommand(wand);
 		X.setSelected(true);
 
 		JRadioButton S = new JRadioButton(start);
 		S.setActionCommand(start);
 		
 		JRadioButton Z = new JRadioButton(ziel);
 		Z.setActionCommand(ziel);
 
 		JRadioButton B = new JRadioButton(boss);
 		B.setActionCommand(boss);
 		
 		JRadioButton Q = new JRadioButton(q);
 		Q.setActionCommand(q);
 		
 		JRadioButton NP = new JRadioButton(np);
 		NP.setActionCommand(np);
 		
 		
 
 		JRadioButton one = new JRadioButton(onea);
 		one.setActionCommand(onea);
 		
 		JRadioButton two = new JRadioButton(twoa);
 		two.setActionCommand(twoa);
 		
 		JRadioButton three = new JRadioButton(threea);
 		three.setActionCommand(threea);
 		
 		JRadioButton one1 = new JRadioButton(oneb);
 		one1.setActionCommand(oneb);
 		
 		JRadioButton two1 = new JRadioButton(twob);
 		two1.setActionCommand(twob);
 		
 		JRadioButton three1 = new JRadioButton(threeb);
 		three1.setActionCommand(threeb);
 
 		// Group the radio buttons.
 		ButtonGroup group = new ButtonGroup();
 		group.add(X);
 		group.add(S);
 		group.add(Z);
 		group.add(B);
 		group.add(Q);
 		group.add(NP);
 		
 		ButtonGroup level= new ButtonGroup();
 		level.add(one);
 		level.add(two);
 		level.add(three);
 
 		ButtonGroup raum = new ButtonGroup();
 		raum.add(one1);
 		raum.add(two1);
 		raum.add(three1);
 	
 
 	        // Register a listener for the radio buttons.
 		RadioListener myListener = new RadioListener();
 		X.addActionListener(myListener);
 		S.addActionListener(myListener);
 		Z.addActionListener(myListener);
 		B.addActionListener(myListener);
 		Q.addActionListener(myListener);
 		NP.addActionListener(myListener);
 
 		add(X);
 		add(S);
 		add(Z);
 		add(B);
 		add(Q);
 		add(NP);
 		add(save);
 		add(cancel);
 		add(upload);
 		add(one);
 		add(two);
 		add(three);
 		add(one1);
 		add(two1);
 		add(three1);
 		
 		
 		
 	    }
 
 
 	    
 	    /** Listens to the radio buttons. */
 	    class RadioListener implements ActionListener { 
 		public void actionPerformed(ActionEvent e) {
 
 			save.addActionListener(
             	    new ActionListener() {
             	        public void actionPerformed(ActionEvent e) {
             	        	
             	        	//System.out.println(action(LevelEditor.check(karte)));
             	        	//save(1,1);
             	        	
             	        	if ((e.getActionCommand() == onea) && (e.getActionCommand() == oneb) )
             			    {
             			    	save(1,1);
             			    } 
             			    else if  ((e.getActionCommand() == onea) && (e.getActionCommand() == twob) )
             			    {
             			    	save(1,2);
 
             			    }
             			    else if  ((e.getActionCommand() == onea) && (e.getActionCommand() == threeb) ) 
             			    {
             			    	save(1,3);
 
             			    }
             			    else if  ((e.getActionCommand() == twoa) && (e.getActionCommand() == oneb) )
             			    {
             			    	save(2,1);
 
             			    } 
             			    else if  ((e.getActionCommand() == twoa) && (e.getActionCommand() == twob) ) 
             			    {
             			    	save(2,2);
 
             			    }
             			    else if  ((e.getActionCommand() == twoa) && (e.getActionCommand() == threeb) )
             			    {
             			    	save(2,3);
 
             			    }
             			    else if  ((e.getActionCommand() == threea) && (e.getActionCommand() == oneb) )
             			    {
             			    	save(3,1);
 
             			    } 
             			    else if  ((e.getActionCommand() == threea) && (e.getActionCommand() == twob) ) 
             			    {
             			    	save(3,2);
 
             			    }
             			    else if  ((e.getActionCommand() == threea) && (e.getActionCommand() == threeb) )
             			    {
             			    	save(3,3);
 
             			    }
    
 
             	        	
             	        }
 
             	    });
 			
 		    cancel.addActionListener(
             	    new ActionListener() {
             	        public void actionPerformed(ActionEvent e) {
             	      
             	        	System.exit(0);
             	        }
 
             	    });
 		    
 		    upload.addActionListener(
             	    new ActionListener() {
             	        public void actionPerformed(ActionEvent e) {
             	      
             	        	frame.setVisible(false);
             	        	try {
 								upload();
 							} catch (IOException e1) {
 								// TODO Auto-generated catch block
 								e1.printStackTrace();
 							}
             	        }
             	        
             	       
 
             	    });
 		    
 		    if (e.getActionCommand() == start) 
 		    {
 			System.out.println(start + " pressed.");
 			LevelEditor.action("s");
 		    } 
 		    else if (e.getActionCommand() == ziel) 
 		    {
 			System.out.println(ziel + " pressed.");
 			LevelEditor.action("z");
 		    }
 		    else if (e.getActionCommand() == boss) 
 		    {
 				System.out.println(boss + " pressed.");
 				LevelEditor.action("B");
 			}
 		    else if (e.getActionCommand() == q) 
 		    {
 				System.out.println(q + " pressed.");
 				LevelEditor.action("q");
 			}
 		    else if (e.getActionCommand() == np) 
 		    {
 				System.out.println(np + " pressed.");
 				LevelEditor.action("NP");
 			}
 		    else if (e.getActionCommand() == wand) 
 		    {
 				System.out.println(wand + " pressed.");
 				LevelEditor.action("x");
 			}
 
 		}
 		}
 		
 
 		public static String[][] action(String [][] kk) {
 			// TODO Auto-generated method stub
 			
 			return karte = kk;
 			
 		}
 		
 
		public void upload() throws IOException, InterruptedException, UnsupportedAudioFileException, LineUnavailableException
 		{
 			Object[] possibilities = {"1.1", "1.2", "1.3","2.1", "2.2", "2.3","3.1", "3.2", "3.3"};
 			String s = (String)JOptionPane.showInputDialog(
 			                    frame,
 			                    "Gespeicherte Karten...",
 			                    "Level.Raum",
 			                    JOptionPane.PLAIN_MESSAGE,
 			                    null,
 			                    possibilities,
 			                    "1.1");
 
 			if (s == "1.1") {
 			    Globals.level=1;
 			    Globals.room=1;
 			}
 			else if (s == "1.2"){
 				Globals.level=1;
 			    Globals.room=2;
 			}
 			else if (s == "1.3"){
 				Globals.level=1;
 			    Globals.room=3;
 			}
 			else if (s == "2.1"){
 				Globals.level=2;
 			    Globals.room=1;
 			}
 			else if (s == "2.2"){
 				Globals.level=2;
 			    Globals.room=2;
 			}
 			else if (s == "2.3"){
 				Globals.level=2;
 			    Globals.room=3;
 			}
 			else if (s == "3.1"){
 				Globals.level=3;
 			    Globals.room=1;
 			}
 			else if (s == "3.2"){
 				Globals.level=3;
 			    Globals.room=2;
 			}
 			else if (s == "3.3"){
 				Globals.level=3;
 			    Globals.room=3;
 			}	    
 				System.out.println(Globals.level);
 				System.out.println(Globals.room);
 				//GetRoom room = new GetRoom();
 				GetRoom.room(1);
				Game.start(1);
 				
 			}
 
 			
 
 
 		public void save(int a, int b ){
 	    	
 	    	try {
 
 	    		
 				File file = new File("src/Raum" + a + "-" + b + ".txt");
 	 
 				// if file doesnt exists, then create it
 				if (!file.exists()) {
 					file.createNewFile();
 				}
 	 
 				
 				
 				FileWriter fw = new FileWriter(file.getAbsoluteFile());
 				BufferedWriter bw = new BufferedWriter(fw);
 		
 				
 				//System.out.println(LevelEditor.check(karte));
 				
 				//karte = action(LevelEditor.check(a, b));
 			
 			    for(int j=0; j<21; j++){
 					bw.write(karte[0][j]);
 					bw.write("\n");
 					bw.write(karte[1][j]);
 					bw.write("\n");
 					bw.write(karte[2][j]);
 					bw.write("\n");
 					bw.write(karte[3][j]);
 					bw.write("\n");
 					bw.write(karte[4][j]);
 					bw.write("\n");
 					bw.write(karte[5][j]);
 					bw.write("\n");
 					bw.write(karte[6][j]);
 					bw.write("\n");
 					bw.write(karte[7][j]);
 					bw.write("\n");
 					bw.write(karte[8][j]);
 					bw.write("\n");
 					bw.write(karte[9][j]);
 					bw.write("\n");
 					bw.write(karte[10][j]);
 					bw.write("\n");
 					bw.write(karte[11][j]);
 					bw.write("\n");
 					bw.write(karte[12][j]);
 					bw.write("\n");
 					bw.write(karte[13][j]);
 					bw.write("\n");
 					bw.write(karte[14][j]);
 					bw.write("\n");
 					bw.write(karte[15][j]);
 					bw.write("\n");
 					bw.write(karte[16][j]);
 					bw.write("\n");
 					bw.write(karte[17][j]);
 					bw.write("\n");
 					bw.write(karte[18][j]);
 					bw.write("\n");
 					bw.write(karte[19][j]);
 					bw.write("\n");
 					bw.write(karte[20][j]);
 					bw.write("\n");				
 					bw.close();
 					}
 				
 	 
 				Component frame = null;
 				JOptionPane.showMessageDialog(frame, "Das Spiel wurde erfolgreich gespeichert");
 	 
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 	    	
 	    }
 	 
 	    
 	    public void main() {
 	         WindowListener l = new WindowAdapter() {
 	             public void windowClosing(WindowEvent e) {System.exit(0);}
 	         };
 	 
 	         frame = new JFrame("Men Editor");
 	         frame.addWindowListener(l);
 	         frame.add("Center", new MenuEditor());
 	         frame.pack();
 	         frame.setVisible(true);
 	         
 	         new LevelEditor();
 	         
 	    }
 
 
 
 }
