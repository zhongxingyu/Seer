 package main;
 
 import global.GlobalSettings;
 import global.MainMenuInterface;
 
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import javax.swing.JFrame;
 
 import core.StateManager;
 
 @SuppressWarnings("serial")
 public class Main extends JFrame {
 	
 	public Main(GlobalSettings gs, final MainMenuInterface mmi) {
 		super();
 		
 		setTitle("The Impossible Clone");
 		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 
 		this.addWindowListener(new WindowListener(){
 
 			@Override
 			public void windowClosed(WindowEvent arg0) {
 			}
 
 			@Override
 			public void windowClosing(WindowEvent arg0) {
 				mmi.returnToMainMenu();
 			    dispose();
 			}
 
 			@Override
 			public void windowDeactivated(WindowEvent arg0) {
 			}
 
 			@Override
 			public void windowDeiconified(WindowEvent arg0) {
 			}
 
 			@Override
 			public void windowIconified(WindowEvent arg0) {
 			}
 
 			@Override
 			public void windowOpened(WindowEvent arg0) {
 			}
 
 			@Override
 			public void windowActivated(WindowEvent arg0) {
 				
 			}
 		});
 		this.pack();
 		setSize(gs.getResolution()[0], gs.getResolution()[1]);
 		//add(new Scene(gs));
 		add(new StateManager(this, gs));
 		
 		this.setLocationRelativeTo(null);
 		setVisible(true);
 		
 		final Main obj = this;
 		Thread t = new Thread() {
 			@Override 
 			public void run() {
 				@SuppressWarnings("unused")
				double cur2, upd2 = System.currentTimeMillis()-16, dif2;
 				while(obj.isDisplayable()) {
					try {
						cur2 = System.currentTimeMillis();
						dif2 = cur2 - upd2;
						upd2 = cur2;
						
						obj.run();
						
						//System.out.println((long)(16-(diff)));
						//long val = (long)dif2-16 > 0 ? (long)dif2-16 : 0;
						//System.out.println(16-val);
						//System.out.println(dif2);
						
						Thread.sleep((long)(16)); // 16 - val
					} catch (InterruptedException e) {
						System.out.println("Hurr Durr MF");
 					}
 				}
 			}
 		};
 		t.start();
 	}
 	
 	private void run() {
 		repaint();
 	}
 }
