 package warborn.view.map;
 
 import java.awt.Color;
 import java.awt.Image;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import javax.swing.border.BevelBorder;
 
 import warborn.constants.MapData;
 import warborn.constants.MapNotFoundException;
 import warborn.model.Warborn;
 
 @SuppressWarnings("serial")
 public abstract class Map extends JPanel implements Observer {
 
 	private Warborn model;
 	private JButton[] buttons;
 	private double[][] scalingConstants;
 
 	/**
 	 * Create the Map panel.
 	 */
 
 	public Map(Warborn model) {
 		this.model = model;
 
 		try {
 			scalingConstants = MapData.getScalingConstants(toString());
 		} catch (MapNotFoundException mnfe) {
 			mnfe.printStackTrace();
 		}
 		setLayout(null);
 		setSize(model.getWidth(), model.getHeight());
 		buttons = new JButton[model.getTerritories().length];
 
 		JLabel maplbl = new JLabel("");
 		maplbl.setVerticalAlignment(SwingConstants.TOP);
 		maplbl.setBounds(0, 0, model.getWidth(),
 				(int) (model.getHeight() * 0.75));
 
 		// Scaling the Map image to fit the screensize
 		Image I = new ImageIcon("WarbornData/images/" + toString() + ".png")
 				.getImage();
 		I = I.getScaledInstance(model.getWidth(),
 				(int) ((model.getHeight()) * 0.75), 0);
 		maplbl.setIcon(new ImageIcon(I));
 		
 		add(maplbl, 0);
 
 		for (int i = 0; i < model.getTerritories().length; i++) {
 
 			buttons[i] = new JButton();
 
 			buttons[i].setActionCommand(Integer.toString(i));
 
 			buttons[i].setBounds(
 					(int) (model.getWidth() * scalingConstants[i][0]),
 					(int) (model.getHeight() * scalingConstants[i][1]), 50, 45);
 			
 			add(buttons[i], 0);
 		}
 
 		update(null, null);
 	}
 
 	public JButton[] getButtons() {
 		return buttons;
 	}
 
 	@Override
 	public String toString() {
 		return MapData.getMapName(model.getMapIndex());
 	}
 
 	public void update(Observable lol, Object lol1) {
 		if (model.getPhase() == 1) {
 			for (int i=0; i<buttons.length; i++){
 				buttons[i].setEnabled(false);
 			}
 		} else {
 			for (int i=0; i<buttons.length; i++){
 				buttons[i].setEnabled(true);
 			}
 			this.requestFocus();
 			
 			for (int i = 0; i < buttons.length; i++) {
				buttons[i].setText(Integer.toString(model.getTerritory(i).getNbrOfUnits()));
 				buttons[i].setBackground(model.getTerritory(i).getOwner().getColor());
 				if (buttons[i].getBackground().getRed()
 						+ buttons[i].getBackground().getGreen()
 						+ buttons[i].getBackground().getBlue() < 250) {
 					buttons[i].setForeground(Color.WHITE);
 				} else {
 					buttons[i].setForeground(Color.BLACK);
 				}
 				if (model.getTerritory(i).isProtected()) {
					float[] hsbFloats = {0f,0f,0f};
 					Color.RGBtoHSB(buttons[i].getBackground().getRed(),
 							buttons[i].getBackground().getGreen(),
 							buttons[i].getBackground().getBlue(), hsbFloats);
 					buttons[i].setBackground(Color.getHSBColor(hsbFloats[0],hsbFloats[1]/2,hsbFloats[2]+((1-hsbFloats[2])/2)));
 					buttons[i].setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.RAISED, Color.WHITE, Color.gray));
 				}
 			}
 			if (model.getSelectedTerritoryIndex() > -1) {
 				buttons[model.getSelectedTerritoryIndex()].setBackground(Color.lightGray);
 			}			
 		}
 	}
 }
