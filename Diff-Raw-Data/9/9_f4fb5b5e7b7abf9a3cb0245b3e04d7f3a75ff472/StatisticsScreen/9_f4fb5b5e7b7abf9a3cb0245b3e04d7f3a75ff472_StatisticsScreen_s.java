 /**
  * StatisticsScreen
  * 
  * Class representing the screen that allows users to view statistics.
  * 
  * @author Willy McHie
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.gui.screen;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.*;
 
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.gui.BoxLayoutAxis;
 import edu.wheaton.simulator.gui.Gui;
 import edu.wheaton.simulator.gui.ScreenManager;
 import edu.wheaton.simulator.gui.SimulatorFacade;
 
 public class StatisticsScreen extends Screen {
 	
 	private static final String POPS_STR = "Populations";
 	private static final String FIELDS_STR = "Fields";
 	private static final String LIFESPANS_STR = "Lifespans";
 
 	private JPanel dataPanel;
 
 	private String[] entities;
 	private String[] agentFields;
 	
 	private JComboBox cardSelector;
 	private JComboBox popEntityBox;
 	private JComboBox fieldEntityBox;
 	private JComboBox lifeEntityBox;
 
 	private Map<String, JComboBox> agentFieldsBoxes;
 	private JPanel fieldCard;
 
 	private static final long serialVersionUID = 714636604315959167L;
 	
 	//TODO fix layout of this screen	
 	public StatisticsScreen(final SimulatorFacade gm) {
 		super(gm);
 		this.setLayout(new BorderLayout());
 				
 		JPanel populationCard = makeCard();
 		fieldCard = makeCard();
 		JPanel lifespanCard = makeCard();
 		String[] boxItems = {POPS_STR, FIELDS_STR, LIFESPANS_STR};
 		
 		dataPanel = new JPanel(new CardLayout());
 		dataPanel.add(populationCard, POPS_STR);
 		dataPanel.add(fieldCard, FIELDS_STR);
 		dataPanel.add(lifespanCard, LIFESPANS_STR);
 		
 		entities = new String[0];
 		
 		popEntityBox = new JComboBox(entities);
 		
 		populationCard.add(popEntityBox);
 		
 		agentFields = new String[0];
 		
 		agentFieldsBoxes = new HashMap<String, JComboBox>();
 		agentFieldsBoxes.put("", new JComboBox(agentFields));
 
 		fieldEntityBox = makeFieldEntityBox(entities);
 		
 		fieldCard.add(fieldEntityBox);
 		fieldCard.add(agentFieldsBoxes.get(""));
 
 		lifeEntityBox = new JComboBox(entities);
 		
 		lifespanCard.add(lifeEntityBox);
 		
 		if(popEntityBox.getSelectedIndex() >= 0){
			int[] popVsTime = gm.getStatManager().getPopVsTime(gm.
 					getPrototype(popEntityBox.getSelectedItem().toString())
 					.getName()
 					);
 			
 			Object[][] timePop = new Object[popVsTime.length][2];
 			for(int i = 0; i < popVsTime.length; i++)
 				timePop[i] = new Object[]{i, popVsTime[i]};
 
 			populationCard.add(new JTable(timePop, 
 				new String[]{"Population", "Time"})
 			);
 		}
 
 		//COMING SOON: Average Field Table Statistics
 
 		//		if(fieldEntityTypes.getSelectedIndex() >= 0){
 		//			double[] p = statMan.getAvgFieldValue((gm.getFacade().
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
 		//		JLabel label = new JLabel("Statistics");
 		//		label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		//		label.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 		//		label.setPreferredSize(new Dimension(300, 150));
 		//
 		//		this.add(label, BorderLayout.NORTH);
 		
 		//TODO MAJOR figure out how to make a graph or something!!
 		
 		cardSelector = makeCardSelector(boxItems,dataPanel);
 		
 		this.add(Gui.makePanel(BoxLayoutAxis.Y_AXIS,null,null,
 				Gui.makePanel(new JLabel("Graph object goes here")),
 				Gui.makePanel(cardSelector),dataPanel,Gui.makePanel(
 				makeDisplayButton(), makeFinishButton())));
 	}
 	
 	private JButton makeDisplayButton(){
 		JButton displayButton = Gui.makeButton("Display",null,new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						if (cardSelector.getSelectedItem().equals(POPS_STR)) {
 							int[] pops = gm.getPopVsTime(gm.
 														getPrototype((String)popEntityBox.getSelectedItem())
 														.getName());
 							//TODO output stats
 						}
 						else if (cardSelector.getSelectedItem().equals(FIELDS_STR)) {
 							String s = (String)fieldEntityBox.getSelectedItem();
 							Prototype p = gm.getPrototype(s);
 							double[] vals = gm.getAvgFieldValue(p.getName(),
 									((String)agentFieldsBoxes.get(s).getSelectedItem()));
 							//TODO output stats
 						}
 						else {
 							Prototype p = gm.getPrototype((String)lifeEntityBox.getSelectedItem());
 							//TODO output stats
 						}
 					}
 				}
 				);
 		return displayButton;
 	}
 	
 	private JButton makeFinishButton(){
 		JButton finishButton = Gui.makeButton("Finish",null,
 				new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ScreenManager sm = getScreenManager();
 				sm.update(sm.getScreen("View Simulation")); 
 			}
 		});
 		return finishButton;
 	}
 	
 	private JComboBox makeFieldEntityBox(String[] entities){
 		JComboBox box = new JComboBox(entities);
 		box.addItemListener(
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
 		return box;
 	}
 	
 	private static JComboBox makeCardSelector(final String[] boxItems, final JPanel dataPanel){
 		JComboBox selector = new JComboBox(boxItems);
 		selector.addItemListener(
 				new ItemListener() {
 					@Override
 					public void itemStateChanged(ItemEvent e) {
 						CardLayout cl = (CardLayout)dataPanel.getLayout();
 						cl.show(dataPanel, (String)e.getItem());
 					}
 				}
 				);
 		return selector;
 	}
 
 	private static JPanel makeCard(){
 		JPanel card = new JPanel();
 		card.setLayout(new BoxLayout(card, BoxLayout.X_AXIS));
 		return card;
 	}
 	
 	@Override
 	public void load() {
 		entities = new String[gm.getPrototypeNames().size()];
 		popEntityBox.removeAllItems();
 		fieldEntityBox.removeAllItems();
 		lifeEntityBox.removeAllItems();
 		agentFieldsBoxes.clear();
 		int i = 0;
 		for (String s : gm.getPrototypeNames()) {
 			entities[i++] = s;
 			agentFields = gm.getPrototype(s).getCustomFieldMap().keySet().toArray(agentFields);
 			agentFieldsBoxes.put(s, new JComboBox(agentFields));
 			popEntityBox.addItem(s);
 			fieldEntityBox.addItem(s);
 			lifeEntityBox.addItem(s);
 		}
 	}
 }
