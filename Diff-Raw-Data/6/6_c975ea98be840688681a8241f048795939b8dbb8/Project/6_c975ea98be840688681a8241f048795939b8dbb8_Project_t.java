 package beans;
 
 public class Project 
 {
 	private int    projID;
 	private String Name;
 	private String Description;
 	private int	   userID;
 	private int    orgID;
	
 	//from credentialDB
 		public void setProjID(int projID) {
 			this.projID = projID;
 		}
 		public int getProjID() {
 			return projID;
 		}
 		public void setName(String name) {
 			this.Name = name;
 		}
 		public String getName() {
 			return Name;
 		}
 		public void setDesc(String desc) {
 			this.Description = desc;
 		}
 		public String getDesc() {
 			return Description;
 		}
		public void setUserID(int userID) {
			this.userID = userID;
 		}
 		public int getUserID() {
 			return userID;
 		}
 		public void setOrgID(int orgID) {
 			this.orgID = orgID;
 		}
 		public int getOrgID() {
 			return orgID;
 		}
 }
