 package de.feu.showgo.ui.views;
 
 import info.clearthought.layout.TableLayout;
 
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.plaf.basic.BasicComboBoxRenderer;
 
 import org.apache.log4j.Logger;
 
 import de.feu.showgo.model.Person;
 import de.feu.showgo.model.Role;
 
 public class CastRowPanel extends JPanel {
 
 	private Person emptySelection;
 	private Role role;
 	private List<Person> availablePersons;
 	private List<JComboBox<Person>> castsSelects;
 	private List<JPanel> castPanels;
 	private static final Logger log = Logger.getLogger(CastSelectionPanel.class);
 	private int rowCounter = 2;
 	private boolean mayBeEmpty;
 	
 	public CastRowPanel(Role role, List<Person> availablePersons, boolean mayBeEmpty) {
 		this.role = role;
 		this.availablePersons = availablePersons;
 		this.mayBeEmpty = mayBeEmpty;
 		log.debug("creating cast row panel for " + role);
 		createComponent();
 	}
 	
 	private void createComponent(){	
 		emptySelection = new Person();
 		emptySelection.setName("---");
 		
 		double size[][] = { { TableLayout.PREFERRED}, { 30, 30 } };
 		final TableLayout layout = new TableLayout(size);
 		setLayout(layout);
 		
 		castsSelects = new ArrayList<JComboBox<Person>>();
 		castPanels = new ArrayList<JPanel>();
 		
 		add(new JLabel(role.getName()+":"), "0,0");
 		
 		int counter = 1;
 		if(role.getCast() != null && !role.getCast().isEmpty()){
 
 			for(Person selected : role.getCast()){
 				log.debug("adding cast " + selected);
 				layout.insertRow(counter, TableLayout.PREFERRED);
 				add(createCastSelectPanel(counter), "0,"+counter+",f,c");
 				castsSelects.get(counter - 1).setSelectedItem(selected);
 				counter++;
 			}
 			
 		}else{
 			layout.insertRow(counter, TableLayout.PREFERRED);
 			add(createCastSelectPanel(1), "0,1,f,c");
 			counter++;
 		}
 		
 		JPanel controlPanel = new JPanel();
 		controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		
 		JButton addCast = new JButton("Weitere Besetzung hinzufügen");
 		
 		rowCounter = counter;
 		addCast.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				log.debug("adding cast for " + role.getName());
 				int castNum = castsSelects.size() + 1;
 				layout.insertRow(rowCounter, TableLayout.PREFERRED);
 				add(createCastSelectPanel(castNum),"0,"+rowCounter+",f,c");
 				rowCounter++;
 				revalidate();
 				repaint();
 			}
 		});
 		
 		JButton removeCast = new JButton("Besetzung entfernen");
 		removeCast.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(castsSelects.size() > 1){
 					log.debug("removing cast for " + role.getName());
 					log.debug("num rows: " + layout.getNumRow());
 					castsSelects.remove(castsSelects.size()-1);
 					JPanel castPanel = castPanels.get(castPanels.size() - 1);
 					castPanel.removeAll();
 					castPanels.remove(castPanel);
 					log.debug("castSelects: " + castsSelects);
 					revalidate();
 					repaint();
 				}
 			}
 		});
 		
 		controlPanel.add(addCast);
 		controlPanel.add(removeCast);
 		add(controlPanel, "0,"+counter+",l,c");
 	}
 	
 	private JPanel createCastSelectPanel(int rankNum){
 		JPanel castPanel = new JPanel();
 		double size[][] = { { 150, 400 }, { TableLayout.PREFERRED } };
 		TableLayout layout = new TableLayout(size);
 		castPanel.setLayout(layout);
 	
 		castPanel.add(new JLabel(rankNum + ". Besetzung:"),"0,0");
 		
 		JComboBox<Person> personSelect = new JComboBox<Person>();
 		personSelect.setRenderer(new PersonComboRederer());
 		
 		if(mayBeEmpty){
 			personSelect.addItem(emptySelection);
 		}
 		for(Person person : availablePersons){
 			personSelect.addItem(person);
 		}
 		castsSelects.add(personSelect);
 		
 		castPanel.add(personSelect,"1,0");
 		
 		castPanels.add(castPanel);
 		return castPanel;
 	}
 	
 	private class PersonComboRederer extends BasicComboBoxRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
 			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 
 			if (value != null) {
 				Person person = (Person) value;
 				setText(person.getName());
 			}
 
 			return this;
 		}
 	}
 
 	public Role getRole() {
 		return role;
 	}
 	
 	public List<Person> getSelectedPersons(){
 		List<Person> assignedPersons = new LinkedList<Person>();
 		
 		for(JComboBox<Person> select : castsSelects){
 			if(select.getSelectedItem() != emptySelection){
 				assignedPersons.add((Person) select.getSelectedItem());
 			}
 		}
 		
 		return assignedPersons;
 	}
 
 }
