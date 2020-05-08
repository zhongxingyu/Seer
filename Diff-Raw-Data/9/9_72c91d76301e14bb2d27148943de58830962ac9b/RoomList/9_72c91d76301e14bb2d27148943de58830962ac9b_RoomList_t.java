 package gui;
 
 import javax.swing.JList;
 import javax.swing.ListSelectionModel;
 
 import model.Room;
 import model.RoomListModel;
 
public class RoomList extends JList{
 
 	public RoomList(RoomListModel model){
 		
 		setModel(model);
 		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		
 	}
 	
 }
