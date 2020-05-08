 package net.codjo.test.release.task.excel;
 
 import com.googlecode.junit.ext.checkers.Checker;
 
 public class ExcelChecker implements Checker {
 	public boolean satisfy() {
 		boolean result = false;
		
 		try {
			ExcelApplicationManager manager = new ExcelApplicationManager();
 			result = (manager != null) && manager.isStarted();
 		} catch (RuntimeException e) {
 			result = false;
 		} catch (Exception e) {
 			result = false;
		}
 		
 		return result;
 	}
 }
