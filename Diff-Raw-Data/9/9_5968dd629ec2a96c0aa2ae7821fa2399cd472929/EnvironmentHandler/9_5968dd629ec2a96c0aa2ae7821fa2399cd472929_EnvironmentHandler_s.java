 package org.noip.staticattic.world;
 
 import java.io.File;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 
 import org.noip.staticattic.GUI.MainWindow;
 import org.noip.staticattic.entities.Tile;
 import org.noip.staticattic.entities.Tile.Type;
 
 public class EnvironmentHandler implements Runnable {
 
 	private MainWindow main;
 	private Environment environment = null;
 	private Environment MainEnvironment;
 	private ImageIcon playericon = null;
 	private JLabel player;
 	
 	public EnvironmentHandler(MainWindow main) {
 		
 		this.main = main;
 		
 		try {
 			
 			playericon = new ImageIcon(ImageIO.read(main.getClass().getResource("/resources/img/player.png")));
 			
 		} catch (Exception e1) {
 			
 			try {
 				
 				playericon = new ImageIcon(ImageIO.read(new File(System.getProperty("user.home")+"/village/dude.png")));
 				
 			} catch (Exception e2) {
 				
 				e2.printStackTrace();
 				
 			}
 			
 		}
 		
 		player = new JLabel(playericon);
 		
 	}
 	
 	@Override
 	public void run() {
 	
 		if (!main.getPlayer().getCurrentEnvironment().equals(environment)) {
 			
 			environment = main.getPlayer().getCurrentEnvironment();
 			changeEnvironments();
 			
 		}
 		
 	}
 	
 	private void changeEnvironments() {
 		
 		Tile[][] array = main.getPlayer().getCurrentEnvironment().getArray();
 		
 		for (Tile[] row : array) {
 			
 			for (Tile e : row) {
 				
 				if (e.getType().equals(Type.GRASS)) {
 					
 					try {
 						
 						e.setIcon(new ImageIcon(ImageIO.read(main.getClass().getResource("/resources/img/tiles/grass.fw.png"))));
 						
 					} catch (Exception e1) {
 						
 						try {
 							
 							e.setIcon(new ImageIcon(ImageIO.read(new File(System.getProperty("user.home")+"/village/grass.fw.png"))));
 							
 						} catch (Exception e2) {
 							
 							e2.printStackTrace();
 							
 						}
 						
 					}
 					
 				}
 				
 				JLabel lbl = new JLabel(e.getIcon());
 				lbl.setBounds(e.getLocation().getX(), e.getLocation().getY(), 48, 48);
 				
 				main.mainpanel.add(lbl);
 				
 			}
 			
 		}
 		
		player.setBounds(main.getPlayer().getLocation().getX(), main.getPlayer().getLocation().getY(), 68, 88);
		
		main.mainpanel.add(player);
		
 		main.repaint();
 		
 	}
 	
 	public void moveRight() {
 		
 		main.mainpanel.removeAll();
 		main.mainpanel.add(player);
 		
 		Tile[][] array = main.getPlayer().getCurrentEnvironment().getArray();
 		
 		for (Tile[] row : array) {
 			
 			for (Tile e : row) {
 				
 				JLabel lbl = new JLabel(e.getIcon());
 				e.getLocation().setX(e.getLocation().getX()+48);
 				lbl.setBounds(e.getLocation().getX(), e.getLocation().getY(), 48, 48);
 				
 				main.mainpanel.add(lbl);
 				
 			}
 			
 		}
 		
 		main.repaint();
 		
 	}
 	
 	public void moveLeft() {
 			
 		main.mainpanel.removeAll();
 		main.mainpanel.add(player);
 			
 		Tile[][] array = main.getPlayer().getCurrentEnvironment().getArray();
 			
 		for (Tile[] row : array) {
 				
 			for (Tile e : row) {
 					
 				JLabel lbl = new JLabel(e.getIcon());
 				e.getLocation().setX(e.getLocation().getX()-48);
 				lbl.setBounds(e.getLocation().getX(), e.getLocation().getY(), 48, 48);
 					
 				main.mainpanel.add(lbl);
 					
 			}
 				
 		}
 			
 		main.repaint();	
 		
 	}
 	
 	public void moveUp() {
 		
 		main.mainpanel.removeAll();
 		main.mainpanel.add(player);
 			
 		Tile[][] array = main.getPlayer().getCurrentEnvironment().getArray();
 			
 		for (Tile[] row : array) {
 				
 			for (Tile e : row) {
 					
 				JLabel lbl = new JLabel(e.getIcon());
 				e.getLocation().setY(e.getLocation().getY()-48);
 				lbl.setBounds(e.getLocation().getX(), e.getLocation().getY(), 48, 48);
 					
 				main.mainpanel.add(lbl);
 					
 			}
 				
 		}
 			
 		main.repaint();	
 		
 	}
 	
 	public void moveDown() {
 		
 		main.mainpanel.removeAll();
 		main.mainpanel.add(player);
 			
 		Tile[][] array = main.getPlayer().getCurrentEnvironment().getArray();
 			
 		for (Tile[] row : array) {
 				
 			for (Tile e : row) {
 					
 				JLabel lbl = new JLabel(e.getIcon());
 				e.getLocation().setY(e.getLocation().getY()+48);
 				lbl.setBounds(e.getLocation().getX(), e.getLocation().getY(), 48, 48);
 					
 				main.mainpanel.add(lbl);
 					
 			}
 				
 		}
 			
 		main.repaint();	
 		
 	}
 
 	public Environment getMainEnvironment() {
 		
 		return MainEnvironment;
 		
 	}
 
 	public void setMainEnvironment(Environment mainEnvironment) {
 		
 		MainEnvironment = mainEnvironment;
 		
 	}
 
 }
