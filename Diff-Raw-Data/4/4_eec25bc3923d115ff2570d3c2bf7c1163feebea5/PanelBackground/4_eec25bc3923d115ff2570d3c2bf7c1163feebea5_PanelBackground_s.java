 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 
 public class PanelBackground extends JPanel
 {
 	public static final int MAX_VEHICLES = 2;
 	
 	private Image imgBackground;
 	private PanelRoad pnlRoads[] = new PanelRoad[MAX_VEHICLES];
 	private JButton btnStart;
 	
 	private Timer tmrMover;
 	private Timer tmrSpeed;
 	
 	private Vehicle vehicles[][] = 
 	{
 		{new VehicleGoogle(0), new VehicleTreadwheel(0)},
 		{new VehicleIronman(0), new VehicleBus(0)}
 	};
 	
 	public PanelBackground()
 	{
 		// Load image
 		imgBackground = new ImageIcon("res/background.png").getImage();
 		
 		// Set properties
 		setLayout(null);	// This enable free locating using setBounds or setLocation.
 		
 		// Create components
 		for (int i=0 ; i<pnlRoads.length ; i++)
 		{
 			pnlRoads[i] = new PanelRoad(vehicles[i]);
 			pnlRoads[i].setLocation(0, 32*9 + 32*4*i);
 			pnlRoads[i].setBackground(new Color(0, 0, 0, 0));	// last parameter is to transparent background.
 		}
 		
 		btnStart = new ButtonStart(vehicles);
 		btnStart.setLocation(32*13, 32*18);
 		
 		// Add components
 		for (int i=0 ; i<pnlRoads.length ; i++)
 		{
 			add(pnlRoads[i]);
 		}
 		add(btnStart);
 	}
 	
 	public void paintComponent(Graphics g)
 	{
 		// Draw background image
 		g.drawImage(imgBackground, 0, 0, this);
 	}
 	
 	public int getSelectedVehicleIndex(int idxRoad)
 	{
 		return pnlRoads[idxRoad].getSelectedVehicleIndex();
 	}
 	
 	public void hideControlComponents()
 	{
 		for (int i=0 ; i<pnlRoads.length ; i++)
 		{
 			pnlRoads[i].hideControlComponents();	// This hide combobox
 		}
 		btnStart.setVisible(false);
 	}
 	
 	public void showControlComponents()
 	{
 		for (int i=0 ; i<pnlRoads.length ; i++)
 		{
 			pnlRoads[i].showControlComponents();	// This show combobox
 		}
 		btnStart.setVisible(true);
 	}
 	
 	public void startRacing()
 	{
 		final int delay = 1;
 		final PanelBackground parent = this;
 		final int selectedIndex[] = new int[vehicles.length];
 		final int speed[] = new int[vehicles.length];
 		
 		for (int i=0 ; i<speed.length ; i++)
 		{
 			speed[i] = (int)(Math.random() * 16) + 1;
 		}
 		
 		// Get selected index in combobox
 		for (int i=0 ; i<selectedIndex.length ; i++)
 		{
 			selectedIndex[i] = getSelectedVehicleIndex(i);
 		}
 		
 		// Define class
 		class TimerMoverListener implements ActionListener
 		{
 			private boolean isStop;
 			
 			public void actionPerformed(ActionEvent event)
 			{
 				for (int i=0 ; i<vehicles.length ; i++)
 				{
 					vehicles[i][getSelectedVehicleIndex(i)].move(Vehicle.DIR_EAST);
 					
 					// Check this vehicle reach the finish line.
 					if (parent.getBounds().width <=
 						vehicles[i][selectedIndex[i]].getBounds().x +
 						vehicles[i][selectedIndex[i]].getBounds().width)
 					{
 						parent.stopRacing();
 						isStop = true;
 					}
 				}
 				
 				// If timer is stop(racing ended)...
 				if (isStop)
 				{
 					// Player1 win
 					if ((vehicles[0][selectedIndex[0]].getBounds().x +
 						vehicles[0][selectedIndex[0]].getBounds().width) >
 						(vehicles[1][selectedIndex[1]].getBounds().x +
 						vehicles[1][selectedIndex[1]].getBounds().width))
 					{
 						JOptionPane.showMessageDialog(parent, "Player1 win!");
 					}
 					// Player2 win
 					else if ((vehicles[0][selectedIndex[0]].getBounds().x +
 							vehicles[0][selectedIndex[0]].getBounds().width) <
 							(vehicles[1][selectedIndex[1]].getBounds().x +
 							vehicles[1][selectedIndex[1]].getBounds().width))
 					{
 						JOptionPane.showMessageDialog(parent, "Player2 win!");
 					}
 					// Draw
 					else
 					{
 						JOptionPane.showMessageDialog(parent, "Draw!");
 					}
 					
 					// Init
 					for (int i=0 ; i<vehicles.length ; i++)
 					{
 						for (int j=0 ; j<vehicles[i].length ; j++)
 						{
 							vehicles[i][j].initVehicle();
 						}
 					}
 				}
 			}
 		}
 		class TimerSpeedListener implements ActionListener
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				for (int i=0 ; i<vehicles.length ; i++)
 				{
 					speed[i] = (int)(Math.random() * 2) + 1; 
 					vehicles[i][selectedIndex[i]].setSpeed(speed[i]);
 				}
 			}
 		}
 		tmrMover = new Timer(delay, new TimerMoverListener());
 		tmrMover.start();
 		
		tmrSpeed = new Timer(delay*100, new TimerSpeedListener());
 		tmrSpeed.start();
 		
 		hideControlComponents();
 	}
 	
 	public void stopRacing()
 	{
 		tmrMover.stop();
 		
 		showControlComponents();
 	}
 }
