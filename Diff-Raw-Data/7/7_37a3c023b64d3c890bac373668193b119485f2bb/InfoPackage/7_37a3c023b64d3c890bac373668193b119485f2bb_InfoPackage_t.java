 package sql;
 
 import etc.Constants;
 
 public class InfoPackage {
 	private static java.util.Date d_start, d_end;
 	private static int [] _actions;
 	
 	public InfoPackage () {
 		_actions = new int [500];
 		for(int i=0; i<500; i++) {
 			_actions[i] = 0;
 		}
 	}
 	
 	public void setDates(java.util.Date s, java.util.Date e) {
 		d_start = s;
 		d_end = e;
 	}
 	
 	public void setActions(int [] a) {
 		for(int i=0; i<500; i++) {
 			if(a[i] != Constants.DIR_LEFT && a[i] != Constants.DIR_RIGHT && a[i] != Constants.DIR_UP && a[i] != Constants.DIR_DOWN && a[i] != 0) {
 				System.out.printf("ERROR (IP): Unexpected action value!\n");
 			} else {
 				_actions[i] = a[i];
 			}
 		}
 	}
 	
 	public int [] getActions() {
 		return _actions;
 	}
 	
 	public java.util.Date getDate(int which) {
 		if(which == 1) {
 			return d_start;
 		} else {
 			return d_end;
 		}
 	}
 }
