 package com.acmetelecom.test;
 
 import java.util.Date;
 import java.util.List;
 
import com.acmetelecom.TimeGetter;

 public class TestTimeGetter implements TimeGetter {
 
 	private List<Date> times;
 	
 	public TestTimeGetter(List<Date> times) {
 		this.times = times;
 	}
	@Override
 	public long getCurrentTime() {
 		return times.remove(0).getTime();
 	}
 
 }
