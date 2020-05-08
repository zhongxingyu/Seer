 public class SpecialSchedule {
 
 	private Day specialDay;
 	private CurrentDate today;
 
 	public SpecialSchedule() {
 
 	}
 
 	private void makeSpecial() {
 		Day temp = new Day();
 		Period p = new Period();
 		p.setStartTime(adjustTime(800, today, 1));
 		p.setEndTime(adjustTime(845, today, 1));
 		p.setNumber(1);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(850, today, 1));
 		p.setEndTime(adjustTime(935, today, 1));
 		p.setNumber(2);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(940, today, 1));
 		p.setEndTime(adjustTime(1025, today, 1));
 		p.setNumber(3);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(1030, today, 1));
 		p.setEndTime(adjustTime(1115, today, 1));
 		p.setNumber(4);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(1120, today, 1));
 		p.setEndTime(adjustTime(1150, today, 1));
 		p.setNumber(-13);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(1155, today, 1));
 		p.setEndTime(adjustTime(1235, today, 1));
 		p.setNumber(-7);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(1240, today, 1));
 		p.setEndTime(adjustTime(1325, today, 1));
 		p.setNumber(5);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(1330, today, 1));
 		p.setEndTime(adjustTime(1415, today, 1));
 		p.setNumber(6);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(1420, today, 1));
 		p.setEndTime(adjustTime(1505, today, 1));
 		p.setNumber(7);
 		temp.add(p);
 		p = new Period();
 		specialDay = temp;
 
 	}
 
 	private void makeSpecialWenesday() {
 		Day temp = new Day();
 		Period p = new Period();
 		p.setStartTime(adjustTime(900, today, 1));
 		p.setEndTime(adjustTime(940, today, 1));
 		p.setNumber(1);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(945, today, 1));
 		p.setEndTime(adjustTime(1025, today, 1));
 		p.setNumber(2);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(1030, today, 1));
 		p.setEndTime(adjustTime(1110, today, 1));
 		p.setNumber(3);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(1115, today, 1));
 		p.setEndTime(adjustTime(1130, today, 1));
 		p.setNumber(-13);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(1135, today, 1));
 		p.setEndTime(adjustTime(1215, today, 1));
 		p.setNumber(4);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(1220, today, 1));
 		p.setEndTime(adjustTime(1255, today, 1));
 		p.setNumber(-7);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(1300, today, 1));
 		p.setEndTime(adjustTime(1340, today, 1));
 		p.setNumber(5);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(1345, today, 1));
 		p.setEndTime(adjustTime(1425, today, 1));
 		p.setNumber(6);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(1430, today, 1));
 		p.setEndTime(adjustTime(1510, today, 1));
 		p.setNumber(7);
 		temp.add(p);
 		p = new Period();
 		specialDay = temp;
 
 	}
 
 	public Day getSpecialDay() {
 		return specialDay;
 	}
 
 	public void setSpecialDay(Day specialDay) {
 		this.specialDay = specialDay;
 	}
 
 	public void makeSpecialDay(int dayType, CurrentDate today) {
 		this.today = today;
 		switch (dayType) {
 			case 0:
 
 				makeSpecial();
 				break;
 
 			case 1:
 				makeSpecialWenesday();
 				break;
 
 			case 2:
 				makeSeniorPetDay();
 				break;
 
 			default:
 				break;
 		}
 	}
 
 	private void makeSeniorPetDay() {
 		Day temp = new Day();
 		Period p = new Period();
 		p.setStartTime(adjustTime(800, today, 1));
 		p.setEndTime(adjustTime(835, today, 1));
 		p.setNumber(1);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(840, today, 1));
 		p.setEndTime(adjustTime(915, today, 1));
 		p.setNumber(2);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(920, today, 1));
 		p.setEndTime(adjustTime(955, today, 1));
 		p.setNumber(3);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(1000, today, 1));
 		p.setEndTime(adjustTime(1035, today, 1));
 		p.setNumber(4);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(1040, today, 1));
 		p.setEndTime(adjustTime(1115, today, 1));
 		p.setNumber(7);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(1120, today, 1));
 		p.setEndTime(adjustTime(1155, today, 1));
 		p.setNumber(5);
 		temp.add(p);
 		p = new Period();
 		p.setStartTime(adjustTime(1200, today, 1));
 		p.setEndTime(adjustTime(1235, today, 1));
 		p.setNumber(6);
 		temp.add(p);
 		p = new Period();
 		specialDay = temp;
 
 	}
 
 	public int adjustTime(int time, CurrentDate today, int direction) {
 		int currentTime = time;
 		if (today.isBefore(new CurrentDate(11, 4, 2012)) || today.isAfterOrEqual(new CurrentDate(3, 10, 2013))) {
 			currentTime += 600 * direction;
 		} else {
 			currentTime += 700 * direction;
 		}
 		return currentTime;
 
 	}
 }
