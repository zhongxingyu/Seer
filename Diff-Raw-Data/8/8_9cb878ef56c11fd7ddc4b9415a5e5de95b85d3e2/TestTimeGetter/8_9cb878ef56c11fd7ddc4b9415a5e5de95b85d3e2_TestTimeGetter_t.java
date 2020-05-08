 package com.acmetelecom.test;
 
import com.acmetelecom.TimeGetter;

 import java.util.Date;
 import java.util.List;
 
 public class TestTimeGetter implements TimeGetter {
 
 	private List<Date> times;
 	
 	public TestTimeGetter(List<Date> times) {
 		this.times = times;
 	}

 	public long getCurrentTime() {
 		return times.remove(0).getTime();
 	}
 
 }
