 package uk.ac.gla.dcs.tp3.w.league;
 
 public class DateTime extends Date {
 
 	private int hour = 0;
 	private int min = 0;
 
 	public DateTime() {
 		super();
 	}
 
 	public DateTime(int d, int m, int y, int h, int min) {
 		super(d, m, y);
 		if (!validateTime(h, min)) {
 			System.out.println("Erroneous time.");
 			return;
 		}
 		hour = h;
 		this.min = min;
 	}
 
 	public DateTime(int d, String m, int y, int h, int min) {
 		super(d, m, y);
 		if (!validateTime(h, min)) {
 			System.out.println("Erroneous time.");
 			return;
 		}
 		hour = h;
 		this.min = min;
 	}
 
 	public DateTime(Date D, int h, int m) {
 		super(D.getDay(), D.getMonth(), D.getYear());
 		if (!validateTime(h, min)) {
 			System.out.println("Erroneous time.");
 			return;
 		}
 		hour = h;
 		this.min = m;
 	}
 
 	public int getHour() {
 		return hour;
 	}
 
 	public int getMinute() {
 		return min;
 	}
 
 	public Date getDateOnly() {
 		return new Date(this.getDay(), this.getMonth(), this.getYear());
 	}
 
 	public String formatTime() {
 		String strhr;
 		String strmin;
 		if (this.hour < 10)
 			strhr = "0" + this.hour;
 		else
 			strhr = "" + this.hour;
 		if (this.min < 10)
 			strmin = "0" + this.min;
 		else
 			strmin = "" + this.min;
 		return strhr + ":" + strmin;
 	}
 
 	private boolean validateTime(int h, int m) {
 		return (0 <= h && h < 24 && 0 <= m && m < 60);
 	}
 
 	public boolean before(DateTime DT) {
		if (!super.equals(DT))
			return super.before(DT);
 		if (hour < DT.getHour())
 			return true;
 		else if (getHour() == DT.getHour() && getMinute() < DT.getMinute())
 			return true;
 		return false;
 	}
 
 	public String toString() {
 		return String.format("%d/%d/%d %s", this.getDay(), this.getMonth(),
 				this.getYear(), formatTime());
 	}
 }
