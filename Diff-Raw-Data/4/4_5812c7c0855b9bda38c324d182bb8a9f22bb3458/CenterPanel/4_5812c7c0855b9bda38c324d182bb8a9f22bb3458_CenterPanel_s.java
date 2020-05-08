 package de.proglabor.aufgabe3.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.Border;
 
 import de.proglabor.aufgabe3.Welt;
 import de.proglabor.aufgabe3.controllers.Controller;
 
 /**
  * @author id261708
  *
  */
 public class CenterPanel extends JPanel {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7338585042616278139L;
 	private static final int MARGIN = 10;
 	private static final int ICON_RESOLUTION = 32;
 	private static final int PANEL_WIDTH = 984;
 	private static final int PANEL_HEIGHT = 690;
 	private Display display = new Display();
 	private JPanel staticsPanel = new JPanel();
 	private String clearText = "0";
 
 	JLabel animalsAlive = new JLabel(clearText);
 	JLabel plantsAlive = new JLabel(clearText);
 	JLabel animalsDead = new JLabel(clearText);
 	JLabel plantsPlanted = new JLabel(clearText);
 	JLabel plantsEaten = new JLabel(clearText);
 	JLabel animalsBorn = new JLabel(clearText);
 	JLabel statusLabel = new JLabel("");
 
 	Controller controller;
 
 	/**
 	 *  
 	 */
 	public CenterPanel() {
 		setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
 		setLayout(new BorderLayout());
 		setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
 		// Simulation Statics
 		staticsPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
 		staticsPanel.setPreferredSize(new Dimension(this.getWidth(),
 				ICON_RESOLUTION));
 		staticsPanel.setLayout(new BoxLayout(staticsPanel, BoxLayout.X_AXIS));
 		Border empty = BorderFactory.createEmptyBorder(0, 0, 0, MARGIN);
 		animalsAlive.setHorizontalAlignment(SwingConstants.LEFT);
 		animalsAlive.setBorder(empty);
 		animalsAlive.setToolTipText("Current Animal Count");
 		plantsAlive.setHorizontalAlignment(SwingConstants.LEFT);
 		plantsAlive.setBorder(empty);
 		plantsAlive.setToolTipText("Current Plant Count");
 		animalsBorn.setHorizontalAlignment(SwingConstants.LEFT);
 		animalsBorn.setBorder(empty);
 		animalsBorn.setToolTipText("Animals Borned");
 		animalsDead.setHorizontalAlignment(SwingConstants.LEFT);
 		animalsDead.setBorder(empty);
 		animalsDead.setToolTipText("Animal Deaths");
 		plantsPlanted.setHorizontalAlignment(SwingConstants.LEFT);
 		plantsPlanted.setBorder(empty);
 		plantsPlanted.setToolTipText("Plants planted");
 		plantsEaten.setHorizontalAlignment(SwingConstants.LEFT);
 		plantsEaten.setBorder(empty);
 		plantsEaten.setToolTipText("Plants eaten");
 
 		loadImg();
 
 		staticsPanel.add(animalsAlive);
 		staticsPanel.add(plantsAlive);
 		staticsPanel.add(animalsBorn);
 		staticsPanel.add(animalsDead);
 		staticsPanel.add(plantsPlanted);
 		staticsPanel.add(plantsEaten);
 		this.add(staticsPanel, BorderLayout.NORTH);
 		this.add(display, BorderLayout.CENTER);
 		// Status Bar
 		JPanel statusPanel = new JPanel();
 		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
 		statusPanel.setPreferredSize(new Dimension(this.getWidth(),
 				ICON_RESOLUTION / 2));
 		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
 		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
 		statusPanel.add(statusLabel);
 		this.add(statusPanel, BorderLayout.SOUTH);
 
 	}
 
 	/**
 	 * loads all icons
 	 */
 	public void loadImg() {
 		try {
			System.out.println(new File("").getAbsolutePath());
 			Image cowImg = ImageIO.read(getClass().getResource(
					"../resources/cow.png"));
 			ImageIcon cowIcon = new ImageIcon(cowImg.getScaledInstance(
 					ICON_RESOLUTION, ICON_RESOLUTION, Image.SCALE_FAST));
 			animalsAlive.setIcon(cowIcon);
 
 			Image plantImg = ImageIO.read(getClass().getResource(
 					"/resources/plants.png"));
 			ImageIcon plantIcon = new ImageIcon(plantImg.getScaledInstance(
 					ICON_RESOLUTION, ICON_RESOLUTION, Image.SCALE_FAST));
 			plantsAlive.setIcon(plantIcon);
 
 			Image animalsBornImg = ImageIO.read(getClass().getResource(
 					"/resources/cow_add.png"));
 			ImageIcon animalsBornIcon = new ImageIcon(
 					animalsBornImg.getScaledInstance(ICON_RESOLUTION,
 							ICON_RESOLUTION, Image.SCALE_FAST));
 			animalsBorn.setIcon(animalsBornIcon);
 
 			Image animalsDeadImg = ImageIO.read(getClass().getResource(
 					"/resources/death.png"));
 			ImageIcon animalsDeadIcon = new ImageIcon(
 					animalsDeadImg.getScaledInstance(ICON_RESOLUTION,
 							ICON_RESOLUTION, Image.SCALE_FAST));
 			animalsDead.setIcon(animalsDeadIcon);
 
 			Image plantsPlantedImg = ImageIO.read(getClass().getResource(
 					"/resources/planted.png"));
 			ImageIcon plantsPlantedIcon = new ImageIcon(
 					plantsPlantedImg.getScaledInstance(ICON_RESOLUTION,
 							ICON_RESOLUTION, Image.SCALE_FAST));
 			plantsPlanted.setIcon(plantsPlantedIcon);
 
 			Image plantsEatenImg = ImageIO.read(getClass().getResource(
 					"/resources/eaten.png"));
 			ImageIcon plantsEatenIcon = new ImageIcon(
 					plantsEatenImg.getScaledInstance(ICON_RESOLUTION,
 							ICON_RESOLUTION, Image.SCALE_FAST));
 			plantsEaten.setIcon(plantsEatenIcon);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @param model 
 	 */
 	public void updateDisplay(Welt model) {
 		// statusLabel.setText(model.);
 		animalsAlive.setText(model.totalAnimals() + "");
 		plantsAlive.setText(model.totalPlants() + "");
 		animalsBorn.setText(model.getBornCount() + "");
 		animalsDead.setText(model.getDeadCount() + "");
 		plantsPlanted.setText(model.getPlantedCount() + "");
 		plantsEaten.setText(model.getEatenCount() + "");
 		display.refresh(model);
 	}
 
 	/**
 	 * Clears the Display
 	 */
 	public void clear() {
 
 		animalsAlive.setText(clearText);
 		plantsAlive.setText(clearText);
 		animalsBorn.setText(clearText);
 		animalsDead.setText(clearText);
 		plantsPlanted.setText(clearText);
 		plantsEaten.setText(clearText);
 		display.clear();
 	}
 
 	/**
 	 * Prints a Message to the Statusbar
 	 * 
 	 * @param msg 
 	 */
 	public void setStatus(String msg) {
 		this.statusLabel.setText(msg);
 	}
 
 }
