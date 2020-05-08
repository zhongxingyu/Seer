package csci306;
 
 import java.awt.Component;
 import java.awt.GridLayout;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 
 public class NotesDialog extends JDialog {
 	private static final long serialVersionUID = 3370549787533395603L;
 	Board board;
 	
 	public NotesDialog(Board board){
 		setTitle("Detective Notes");
 		setLayout(new GridLayout(3,2));
 		this.board = board;
 		add(createPeopleCheckbox());
 		add(createPeopleCombo());
 		add(createRoomCheckbox());
 		
 	}
 	
 	private JPanel createRoomCheckbox() {
 		JPanel panel = new JPanel();
 		panel.setBorder(new TitledBorder (new EtchedBorder(), "Rooms"));
 		for(String room: board.rooms.values()){
 			JCheckBox box = new JCheckBox(room);
 			panel.add(box);
 		}
 		return panel;
 	}
 
 	private JPanel createPeopleCombo(){
 		JPanel panel = new JPanel();
 		panel.setBorder(new TitledBorder (new EtchedBorder(), "Person Guess"));
 		JComboBox b = new JComboBox();
 		for(Player p: board.players){
 			b.addItem(p.getName());
 		}
 		panel.add(b);
 		return panel;
 	}
 	
 	private JPanel createPeopleCheckbox(){
 		JPanel boxes = new JPanel();
 		boxes.setBorder(new TitledBorder (new EtchedBorder(), "People"));
 		for(Player p: board.players){
 			JCheckBox box = new JCheckBox(p.getName());
 			boxes.add(box);
 		}
 		return boxes;
 	} 
 
 }
