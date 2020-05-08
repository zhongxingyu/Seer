 package blue.hotel.gui;
 
 import java.util.List;
 
 import blue.hotel.model.Reservation;
 import blue.hotel.model.RoomReservation;
 
 public class CheckListItem {
 
 	private Reservation item;
 	private boolean isSelected = false;
  
 	public CheckListItem(Reservation item) {
 		this.item = item;
 	}
 	
 	public boolean isSelected() {
 		return isSelected;
 	}
  
 	public void setSelected(boolean isSelected) {
 		this.isSelected = isSelected;
 	}
 	
 	//returns the string displayed in the list
 	public String toString() {
 		String returnString = "No. " + Integer.toString(this.item.getId());
 				
 		String roomString = null;
 		List<RoomReservation> custList = item.getRooms();
 		
 		if(custList != null) {
			if(custList.size() == 0){
				roomString = "";
			}else if(custList.size() == 1) {
 				roomString = custList.get(0).getRoom().getName();
 			} else if(custList.size() == 2) {
 				roomString = custList.get(0).getRoom().getName() + ", " + custList.get(1).getRoom().getName();
 			} else {
 				roomString = custList.get(0).getRoom().getName() + ", " + custList.get(1).getRoom().getName() + ",...";
 			}
 			
 			returnString = returnString + " / Rooms: " + roomString;
 		}
 			
 		return returnString;
 	}
 }
