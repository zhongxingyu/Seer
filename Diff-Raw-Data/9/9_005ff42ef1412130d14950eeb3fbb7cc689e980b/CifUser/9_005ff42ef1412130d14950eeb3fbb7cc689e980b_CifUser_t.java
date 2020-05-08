 package edu.rochester.cif.cifreader;
 
 /**
  * Handy structure for containing a user's information.
  */
 public class CifUser {
 //	public enum UserLevel {
 //		USER_LEVEL_NORMAL,
 //		USER_LEVEL_ADMIN,
 //		USER_LEVEL_DISABLED,
 //		USER_LEVEL_ERROR
 //	}
 	
 	public final String uid;
 	public final String fullName;
 	public final int lcc;
 	public final boolean isAdmin;
 	public final boolean inLabGroup;
 	public final boolean isDisabled;
 	
 	private CifUser(String[] params) {
 		this.uid = params[0];
 		this.fullName = params[1];
 		this.lcc = Integer.parseInt(params[2]);
 		int level = Integer.parseInt(params[3]);
 		this.isAdmin = (level & 0x1) == 0x1;
 		this.inLabGroup = (level & 0x2) == 0x2;
 		this.isDisabled = (level & 0x4) == 0x4;
 	}
 	
 	public static CifUser cifUserFromResponse(String line) {
 		String[] params = new String(line).split("\\|");
 		if(params.length == 4) {
 			return new CifUser(params);
 		} else {
 			return null;
 		}
 	}
 	
 //	public int getUid() { return this.uid; }
 //	public String getName() { return this.fullName; }
 //	public int getLcc() { return this.lcc; }
}
