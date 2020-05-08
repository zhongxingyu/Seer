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
 
 import javax.swing.*;
 
 import edu.wheaton.simulator.statistics.StatisticsManager;
 
 public class StatisticsScreen extends Screen {
 	//TODO instance variables
 	private JPanel dataPanel;
 
 	private String[] entities;
 
 	private String[] agentFields;
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 714636604315959167L;
 	//TODO fix layout of this screen	
 	public StatisticsScreen(final ScreenManager sm) {
 		super(sm);
 		this.setLayout(new BorderLayout());
 		JLabel label = new JLabel("Statistics");
 		label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		label.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 		label.setPreferredSize(new Dimension(300, 150));
 		JPanel mainPanel = new JPanel();
 		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
 		JPanel graphPanel = new JPanel();
 		dataPanel = new JPanel();
 		dataPanel.setLayout(new CardLayout());
 		JPanel populationCard = new JPanel();
 		String populations = "Card with Populations";
 		JPanel fieldCard = new JPanel();
 		String fields = "Card with Fields";
 		JPanel lifespanCard = new JPanel();
 		String lifespans = "Card with Lifespans";
 		dataPanel.add(populationCard, populations);
 		dataPanel.add(fieldCard, fields);
 		dataPanel.add(lifespanCard, lifespans);
 		JPanel boxPanel = new JPanel();
 		String[] boxItems = {populations, fields, lifespans};
 		JComboBox cardSelector = new JComboBox(boxItems);
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
 
 		//TODO placeholder
 		//String[] entities = {"Fox", "Rabbit", "Clover", "Bear"};
		//entities = new String[0];
 		JComboBox popEntityTypes = new JComboBox(entities);
 		populationCard.add(popEntityTypes);
 
 		JComboBox fieldEntityTypes = new JComboBox(entities);
 		//TODO placeholder
 		//String[] agentFields = {"height", "weight", "speed"};
		//agentFields = new String[0];
 		JComboBox agentFieldsBox = new JComboBox(agentFields);
 		fieldCard.add(fieldEntityTypes);
 		fieldCard.add(agentFieldsBox);
 
 		JComboBox lifeEntityBox = new JComboBox(entities);
 		lifespanCard.add(lifeEntityBox);
 
 		JButton finishButton = new JButton("Finish");
 		finishButton.setPreferredSize(new Dimension(150, 70));
 		finishButton.setAlignmentX(CENTER_ALIGNMENT);
 		finishButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				sm.update(sm.getScreen("Edit Simulation")); 
 			}
 		});
 
 		if(popEntityTypes.getSelectedIndex() >= 0){
 			StatisticsManager statMan = sm.getStatManager();
 			int[] p = statMan.getPopVsTime(sm.getFacade().
 					getPrototype(popEntityTypes.getSelectedItem().toString())
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
 
 
 		this.add(label, BorderLayout.NORTH);
 
 		//TODO MAJOR figure out how to make a graph or something!!
 		graphPanel.add(new JLabel("Graph object goes here"));
 		mainPanel.add(graphPanel);
 		mainPanel.add(boxPanel);
 		mainPanel.add(dataPanel);
 		mainPanel.add(finishButton);
 		this.add(mainPanel);
 
 	}
 
 	//TODO finish this
 	@Override
 	public void load() {
 		/*
 		entities = new String[sm.getFacade().prototypeNames().size()];
 		int i = 0;
 		for (String s : sm.getFacade().prototypeNames()) {
 			entities[i++] = s;
 		}
 
 		 */
 	}
 
 }
