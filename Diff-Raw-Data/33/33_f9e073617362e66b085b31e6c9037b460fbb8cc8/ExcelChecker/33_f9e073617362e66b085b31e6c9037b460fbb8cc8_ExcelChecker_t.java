 package net.codjo.test.release.task.excel;
 
 import com.googlecode.junit.ext.checkers.Checker;
import jp.ne.so_net.ga2.no_ji.jcom.JComException;
 
 public class ExcelChecker implements Checker {
 	public boolean satisfy() {
 		boolean result = false;

        ExcelApplicationManager manager = null;
 		try {
			manager = new ExcelApplicationManager();
 			result = (manager != null) && manager.isStarted();
 		} catch (RuntimeException e) {
 			result = false;
 		} catch (Exception e) {
 			result = false;
		} finally {
            if (manager != null) {
                try {
                    manager.quit();
                }
                catch (JComException e) {
                    // ignore
                }
            }
        }
 		
 		return result;
 	}
 }
