 /**
  * StatisticsScreen
  * 
  * Class representing the screen that allows users to view statistics.
  * 
  * @author Willy McHie
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.gui;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.HashMap;
 
 import javax.swing.*;
 
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.statistics.StatisticsManager;
 
 public class StatisticsScreen extends Screen {
 
 	private JPanel dataPanel;
 
 	private String[] entities;
 
 	private String[] agentFields;
 	
 	private JComboBox cardSelector;
 
 	private JComboBox popEntityBox;
 
 	private JComboBox fieldEntityBox;
 
 	private JComboBox lifeEntityBox;
 
 	private HashMap<String, JComboBox> agentFieldsBoxes;
 
 	private JPanel fieldCard;
 
 	private StatisticsManager statMan;
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 714636604315959167L;
 	//TODO fix layout of this screen	
 	//TODO make sure that correct fields box gets put on the panel when an agent is selected.
 	public StatisticsScreen(final ScreenManager sm) {
 		super(sm);
 		statMan = sm.getStatManager();
 		this.setLayout(new BorderLayout());
 		JLabel label = new JLabel("Statistics");
 		label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		label.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 		label.setPreferredSize(new Dimension(300, 150));
 		JPanel buttonPanel = new JPanel();
 		JPanel mainPanel = new JPanel();
 		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
 		JPanel graphPanel = new JPanel();
 		dataPanel = new JPanel();
 		dataPanel.setLayout(new CardLayout());
 		JPanel populationCard = new JPanel();
 		String populations = "Populations";
 		fieldCard = new JPanel();
 		String fields = "Fields";
 		JPanel lifespanCard = new JPanel();
 		String lifespans = "Lifespans";
 		dataPanel.add(populationCard, populations);
 		dataPanel.add(fieldCard, fields);
 		dataPanel.add(lifespanCard, lifespans);
 		JPanel boxPanel = new JPanel();
 		String[] boxItems = {populations, fields, lifespans};
 		cardSelector = new JComboBox(boxItems);
 		cardSelector.addItemListener(
 				new ItemListener() {
 					@Override
 					public void itemStateChanged(ItemEvent e) {
 						CardLayout cl = (CardLayout)dataPanel.getLayout();
 						cl.show(dataPanel, (String)e.getItem());
 					}
 				}
 				);
 		boxPanel.add(cardSelector);
 
 		populationCard.setLayout(new BoxLayout(populationCard, BoxLayout.X_AXIS));
 		fieldCard.setLayout(new BoxLayout(fieldCard, BoxLayout.X_AXIS));
 		lifespanCard.setLayout(new BoxLayout(lifespanCard, BoxLayout.X_AXIS));
 
 		entities = new String[0];
 		popEntityBox = new JComboBox(entities);
 		populationCard.add(popEntityBox);
 
 		fieldEntityBox = new JComboBox(entities);
 		agentFields = new String[0];
 		agentFieldsBoxes = new HashMap<String, JComboBox>();
 		agentFieldsBoxes.put("", new JComboBox(agentFields));
 		fieldEntityBox.addItemListener(
 				new ItemListener() {
 					@Override
 					public void itemStateChanged(ItemEvent e) {
 						fieldCard.remove(1);
 						fieldCard.add(agentFieldsBoxes.get(e.getItem()));
 						validate();
 						repaint();
 					}
 				}
 				);
 		fieldCard.add(fieldEntityBox);
 		fieldCard.add(agentFieldsBoxes.get(""));
 
 		lifeEntityBox = new JComboBox(entities);
 		lifespanCard.add(lifeEntityBox);
 
 		JButton displayButton = new JButton("Display");
 		displayButton.setPreferredSize(new Dimension(150, 70));
 		displayButton.addActionListener(
 				new ActionListener() {
					@Override
 					public void actionPerformed(ActionEvent e) {
 						if (cardSelector.getSelectedItem().equals("Populations")) {
 							int[] pops = statMan.getPopVsTime(sm.getFacade().
 														getPrototype((String)popEntityBox.getSelectedItem())
 														.getPrototypeID());
 							System.out.println("Population over time: ");
 							//TODO temporary solution to demonstrate compatibility
 							//TODO this loop displays nothing for a basic simulation
 							for (int i = 0; i < pops.length; i++) {
 								System.out.println(i + ":  " + pops[i]);
 							}
 						}
 						else if (cardSelector.getSelectedItem().equals("Fields")) {
 							String s = (String)fieldEntityBox.getSelectedItem();
 							Prototype p = sm.getFacade().getPrototype(s);
 							double[] vals = statMan.getAvgFieldValue(p.getPrototypeID(),
 									((String)agentFieldsBoxes.get(s).getSelectedItem()));
 							//TODO temporary solution to demonstrate compatibility
 							//TODO this loop displays nothing for a basic simulation
 							System.out.println("Average field value over time: ");
 							for (int i = 0; i < vals.length; i++) {
 								System.out.println(i + ":  " + vals[i]);
 							}
 						}
 						else {
 							Prototype p = sm.getFacade().getPrototype((String)lifeEntityBox.getSelectedItem());
 							//TODO temporary solution to demonstrate compatibility
 							//getAvgLifespan returns NaN (not a number; 0 / 0 ?)
 							System.out.println("Average lifespan: " + 
 									statMan.getAvgLifespan(p.getPrototypeID()));
 						}
 					}
 				}
 				);
 		
 		JButton finishButton = new JButton("Finish");
 		finishButton.setPreferredSize(new Dimension(150, 70));
 		finishButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				sm.update(sm.getScreen("Edit Simulation")); 
 			}
 		});
 
 		if(popEntityBox.getSelectedIndex() >= 0){
 			StatisticsManager statMan = sm.getStatManager();
 			int[] p = statMan.getPopVsTime(sm.getFacade().
 					getPrototype(popEntityBox.getSelectedItem().toString())
 					.getPrototypeID()
 					);
 			String[] popTime = {"Population", "Time"};
 			Object[][] timePop = new Object[p.length][2];
 			for(int i = 0; i < p.length; i++){
 				Object[] array= {i, p[i]};
 				timePop[i] = array;
 			}
 
 			JTable jt = new JTable(timePop ,popTime);
 			populationCard.add(jt);
 		}
 
 		//COMING SOON: Average Field Table Statistics
 
 		//		if(fieldEntityTypes.getSelectedIndex() >= 0){
 		//			double[] p = statMan.getAvgFieldValue((sm.getFacade().
 		//					getPrototype(popEntityTypes.getSelectedItem().toString())
 		//					.getPrototypeID()), (String) agentFieldsBox.getSelectedItem()
 		//					);
 		//			String[] popTime = {"Population", "Time"};
 		//			Object[][] timePop = new Object[p.length][2];
 		//			for(int i = 0; i < p.length; i++){
 		//				Object[] array= {i, p[i]};
 		//				timePop[i] = array;
 		//			}
 		//
 		//			JTable jt = new JTable(timePop ,popTime);
 		//			populationCard.add(jt);
 		//		}
 		//
 		//		this.add(label, BorderLayout.NORTH);
 
 		//TODO MAJOR figure out how to make a graph or something!!
 		buttonPanel.add(displayButton);
 		buttonPanel.add(finishButton);
 		graphPanel.add(new JLabel("Graph object goes here"));
 		mainPanel.add(graphPanel);
 		mainPanel.add(boxPanel);
 		mainPanel.add(dataPanel);
 		mainPanel.add(buttonPanel);
 		this.add(mainPanel);
 
 	}
 
 	//TODO finish this
 	@Override
 	public void load() {
 		entities = new String[sm.getFacade().prototypeNames().size()];
 		popEntityBox.removeAllItems();
 		fieldEntityBox.removeAllItems();
 		lifeEntityBox.removeAllItems();
 		agentFieldsBoxes.clear();
 		int i = 0;
 		for (String s : sm.getFacade().prototypeNames()) {
 			entities[i++] = s;
 			agentFields = sm.getFacade().getPrototype(s).getCustomFieldMap().keySet().toArray(agentFields);
 			agentFieldsBoxes.put(s, new JComboBox(agentFields));
 			popEntityBox.addItem(s);
 			fieldEntityBox.addItem(s);
 			lifeEntityBox.addItem(s);
 		}
 
 
 	}
 
 }
