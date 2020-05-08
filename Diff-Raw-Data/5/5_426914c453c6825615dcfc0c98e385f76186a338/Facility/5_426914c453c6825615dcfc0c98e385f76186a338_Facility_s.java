 package booking;
 
 import java.util.ArrayList;
 
 public class Facility {
 	private int id;
 	private String desc;
 	private ArrayList<BookingSlot> slots;
 	private ArrayList<MonitorClient> monitorList;
 	private int confirmId = 0;
 	
 	public Facility(int id, String desc) {
 		this.id = id;
 		this.desc = desc;
 		slots = new ArrayList<BookingSlot>();
 		monitorList = new ArrayList<MonitorClient>();
 	}
 	
 	// toString method
 	public String toString() {
 		return "Id:" + this.id + ":" + this.desc;
 	}
 	
 	// get all bookings slot string
 	public String getBookSchedule() {
 		if(this.slots.size() == 0) 
 			return "There is no booking slots for " + this.desc;
 		String str = "";
 		for(int i = 0;  i < this.slots.size(); i++) {
 			str += "Slot " + i + ": \n" + slots.get(i).toString() + "\n";
 		}
 		return str;
 	}
 	
 	public ArrayList<MonitorClient> getClientList() {
 		return this.monitorList;
 	}
 	
 	public ArrayList<BookingSlot> getBookSlots() {
 		return this.slots;
 	}
 	
 	// query Availability
 	// output = true (mean available at startTime), nextTime = next occupied Time
 	// output = false(mean not available at startTime), nextTime = next available Time
 	public boolean queryAvailibility(TimePoint startTime, TimePoint nextTime) {
 		if(slots.size() == 0) {
 			nextTime = null;
 			return true;
 		}
 		int index = 0;
 		while(index < slots.size()) {
 			BookingSlot currentSlot = slots.get(index);
 			if(currentSlot.compareTime(startTime) > 0) {
 				break;
 			}
 			index++;
 		}
 		if(index > 0 && slots.get(index-1).getEndTime().compareTime(startTime) > 0) {
 			nextTime = slots.get(index - 1).getEndTime();
 			return false;
 		}
 		if(index == 0 || index < slots.size()) {
 			BookingSlot nextSlot = slots.get(index);
 			nextTime = new TimePoint(
 					nextSlot.getStartDate(), 
 					nextSlot.getStartHour(), 
 					nextSlot.getStartMin());
 		} else nextTime = null;
 		return true;
 	}
 	
 	// add method
 	public int addSlot(BookingSlot newSlot) {
 		int index = 0;
 		// find ordering index to add slot
 		while(index < slots.size()) {
 			//System.out.println("index: " + index + ", size = " + slots.size());
 			BookingSlot currentSlot = slots.get(index);
 			if(newSlot.compareTime(currentSlot) <= 0) {
 				break;
 			} else {
 				index++;
 			}
 		}
 		// check violation
 		// before violation: start time of new slot violates end time of previous
 		if(index > 0) {
 			BookingSlot prevSlot = slots.get(index - 1);
 			if(newSlot.compareTime(prevSlot.getEndTime()) < 0) {
 				System.out.println("Time Violation: before");
 				return -1;
 			}
 		}
 		// after violation: end time of new slot violates start time of the next
 		if(index < slots.size()) {
 			BookingSlot currSlot = slots.get(index);
 			if(currSlot.compareTime(newSlot.getEndTime()) < 0) {
 				System.out.println("Time Violation: after");
 				return -1;
 			}
 		}
 		slots.add(index, newSlot);
 		this.confirmId ++;
 		newSlot.setConfirmationId(confirmId);
 		return confirmId;
 	}
 	
 	public BookingSlot removeSlot(int index) {
 		if(this.slots.size() == 0 || this.slots.size() > 0 && index >= this.slots.size())
 			return null;
 		return this.slots.remove(index);
 	}
 	
 	// search booking slot by confirmation id
 	public int searchBookSlot(int confirmId) {
 		int index = -1;
 		if(this.slots.size() > 0) {
 			for(int i = 0; i < this.slots.size(); i++) {
 				BookingSlot slot = this.slots.get(i);
 				if(slot.getConfirmationId() == confirmId) {
 					index = i;
 					break;
 				}
 			}
 		}
 		return index;
 	}
 	
 	public int bookChange(int confirmId, Duration dr) {
 		// return -1 if failed
 		// otherwise return the new confirmation id
 		int index = this.searchBookSlot(confirmId);
 		if(index == 1)
 			return -1;
 		
 		BookingSlot currSlot = this.removeSlot(index);
 		BookingSlot updateSlot = currSlot.getUpdateSlot(dr);
 		int addResult = this.addSlot(updateSlot);
 		if(addResult == -1)
 			this.addSlot(currSlot);
 		return addResult;
 	}
 
 	public static void main(String [] args) {
 		
 		Facility books = new Facility(1, "Books");
 
 		BookingSlot bs1 = new BookingSlot(new TimePoint(TimePoint.MONDAY, 10, 0), new Duration(0, 3, 0));
 		BookingSlot bs3 = new BookingSlot(new TimePoint(TimePoint.SUNDAY, 10, 1), new Duration(0, 3, 0));
 		BookingSlot bs4 = new BookingSlot(new TimePoint(TimePoint.FRIDAY, 10, 1), new Duration(0, 3, 0));
 		BookingSlot bs5 = new BookingSlot(new TimePoint(TimePoint.THURSDAY, 10, 1), new Duration(0, 3, 0));
 		BookingSlot bs2 = new BookingSlot(new TimePoint(TimePoint.THURSDAY, 13, 0), new Duration(0, 3, 0));
 
 		books.addSlot(bs1);
 		books.addSlot(bs2);
 		books.addSlot(bs3);
 		books.addSlot(bs4);
 		books.addSlot(bs5);
 		System.out.println(books.toString());
 		System.out.println(books.getBookSchedule());
 		System.out.println("Terminate"); 
 	}
 }
