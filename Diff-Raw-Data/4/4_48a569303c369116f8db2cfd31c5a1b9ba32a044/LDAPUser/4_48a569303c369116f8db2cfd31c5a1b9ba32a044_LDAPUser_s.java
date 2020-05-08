 package uk.ac.cam.cl.dtg.ldap;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * A class containing all data for a particular LDAP queried user
  */
 public class LDAPUser extends LDAPObject {
 
 	/**
 	 * Fields to cache user data once looked up
 	 */
 	private String crsid;
 	private String regName;
 	private String displayName;
 	private String surname;
 	private String email;
 	private List<String> instID;
 	private List<String> institutions;
 	private List<String> status;
 	private List<String> photos;
 
 	/** Class constructor taking a crsid of the user to lookup **/
 	LDAPUser(String crsid, String regName, String displayName, String surname, String email, List<String> instID,
 			List<String> status, List<String> institutions, List<String> photos) {
 
 		super();
 
 		this.crsid = crsid;
 
 		// set default values
 		this.regName = ifNull(regName, "undefined");
 		this.displayName = ifNull(displayName, regName);
 		this.surname = ifNull(surname, "undefined");
 		this.email = ifNull(email, "undefined");
 		this.instID = ifNull(instID, Arrays.asList("undefined"));
 		this.institutions = ifNull(institutions,
 				Arrays.asList("undefined"));
 		this.status = ifNull(status, Arrays.asList("undefined"));
 		this.photos = ifNull(photos, Arrays.asList("undefined"));
 
		Collections.sort(institutions);
		Collections.sort(instID);
 	}
 
 	/**
 	 * Get users crsid
 	 * 
 	 * @return String crsid
 	 */
 	@Override
 	public String getID() {
 		return crsid;
 	}
 
 	/**
 	 * Get surname for trie matching
 	 * 
 	 * @return String surname
 	 */
 	@Override
 	String getName() {
 		return surname;
 	}
 
 	/**
 	 * Get users display name, defaults to registered name if not set
 	 * 
 	 * @return String registered name
 	 */
 	public String getDisplayName() {
 		return displayName;
 	}
 	
 	/**
 	 * Old method to get display name
 	 * @deprecated user {@link getDisplayName()} instead.
 	 */
 	@Deprecated
 	public String getcName() {
 		return displayName;
 	}
 	
 	/**
 	 * Get users registered name
 	 * 
 	 * @return String registered name
 	 */
 	public String getRegName() {
 		return regName;
 	}
 
 	/**
 	 * Get users surname
 	 * 
 	 * @return String surname
 	 */
 	public String getSurname() {
 		return surname;
 	}
 
 	/**
 	 * Get users email
 	 * 
 	 * @return String email
 	 */
 	public String getEmail() {
 		return email;
 	}
 
 	/**
 	 * Get institution id
 	 * 
 	 * @return String instID
 	 */
 	public List<String> getInstID() {
 		return instID;
 	}
 	
 	/**
 	 * Gets a list of institutions associated with user
 	 * 
 	 * @return String status
 	 */
 	public List<String> getInstitutions() {
 		return institutions;
 	}
 
 	/**
 	 * Gets a list of misAffiliations associated with user If 'staff'
 	 * misAffiliations user is present sets status as staff, otherwise student
 	 * 
 	 * @return String status
 	 */
 	public List<String> getStatus() {
 		return status;
 	}
 
 	/**
 	 * Gets photo as an encoded base 64 jpeg To display in soy template, use
 	 * <img src="data:image/jpeg;base64,{$user.photo}" /> or similar
 	 * 
 	 * @return String photo
 	 */
 	public List<String> getPhotos() {
 		return photos;
 	}
 
 	/**
 	 * Gets cName, surname, email
 	 * 
 	 * @return HashMap
 	 */
 	public HashMap<String, String> getEssentials() {
 
 		HashMap<String, String> data = new HashMap<String, String>();
 
 		data.put("crsid", crsid);
 		data.put("name", regName);
 		data.put("username", displayName);
 		data.put("surname", surname);
 		data.put("email", email);
 
 		return data;
 	}
 
 	/**
 	 * Gets cName, surname, email
 	 * 
 	 * @return HashMap
 	 */
 	public HashMap<String, Object> getAll() {
 
 		HashMap<String, Object> data = new HashMap<String, Object>();
 
 		data.put("crsid", crsid);
 		data.put("name", regName);
 		data.put("username", displayName);
 		data.put("surname", surname);
 		data.put("email", email);
 		data.put("instID", instID);
 		data.put("institution", institutions);
 		data.put("status", status.get(0));
 		data.put("photo", photos.get(0));
 
 		return data;
 	}
 
 }
