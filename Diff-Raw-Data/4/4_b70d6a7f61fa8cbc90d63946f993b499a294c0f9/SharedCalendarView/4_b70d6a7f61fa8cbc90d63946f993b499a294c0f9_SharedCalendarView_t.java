 package framePackage;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import data.CalendarModel;
 import data.Person;
 
 public class SharedCalendarView implements PropertyChangeListener{
 	private JPanel sharedCPanel;
 	private CalendarModel calendarModel;
 	private JCheckBox checkBox;
 	private List<Person> personList;
 	private List<JCheckBox> checkBoxList;
 
 	
 	public SharedCalendarView(CalendarModel calendarModel){
 		initialize(calendarModel);
 	}
 	
 	
 	private void initialize(CalendarModel calendarModel){
 		this.calendarModel = calendarModel;
 		calendarModel.addPropertyChangeListener(this);
 		sharedCPanel = new JPanel(new GridBagLayout());
 		sharedCPanel.setPreferredSize(new Dimension(250, 200));
 		sharedCPanel.setVisible(true);
 		sharedCPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
 		checkBoxList = new ArrayList<JCheckBox>();
 		personList = new ArrayList<Person>();
 	}
 	
 	private void setCheckBox(List<Person> list){
 		GridBagConstraints c = new GridBagConstraints();
 		c.anchor = GridBagConstraints.LINE_START;
 		for(int i = 0; i < list.size(); i++){
 			personList.add(list.get(i));
 			checkBox = new JCheckBox(list.get(i).getFirstName() + list.get(i).getLastName());
			checkBox.setForeground(calendarModel.getColorOfPerson(list.get(i)));
 			c.gridx = 0;
 			c.gridy = i;
 			sharedCPanel.add(checkBox,c);
 			checkBoxList.add(checkBox);
 			checkBox.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					calendarModel.setSelected(personList.get(checkBoxList.indexOf(checkBox)),checkBox.isSelected());
 				}
 			});
 		}
		sharedCPanel.validate();
 	}
 	
 	public JPanel getPanel(){
 		return sharedCPanel;
 	}
 
 	public void propertyChange(PropertyChangeEvent evt) {
 		switch (evt.getPropertyName()) {
 		case CalendarModel.PERSONS_ADDED_Property:
 			setCheckBox(calendarModel.getPersons());
 			System.out.println("cookie " + calendarModel.getPersons());
 			break;
 
 		}
 	}	
 }
