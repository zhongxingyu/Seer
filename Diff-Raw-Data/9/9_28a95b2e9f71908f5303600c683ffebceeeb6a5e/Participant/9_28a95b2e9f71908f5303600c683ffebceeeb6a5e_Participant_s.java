 package is.idega.idegaweb.pheidippides.data;
 
 import java.io.Serializable;
 import java.util.Date;
 
 public class Participant implements Serializable {
 
 	private static final long serialVersionUID = -7894754146844154272L;
 
 	private String firstName;
 	private String middleName;
 	private String lastName;
 	private String fullName;
 	private String personalId;
 	private Date dateOfBirth;
 	private String uuid;
 	private String address;
 	private String postalAddress;
 	private String postalCode;
 	private String city;
 	private String country;
 	private String nationality;
 	private String email;
 	private String phoneHome;
 	private String phoneMobile;
 	private String gender;
 	private boolean foreigner;
 	private String imageURL;
 	private String login;
 	
 	private String relayLeg;
 	private ShirtSize shirtSize;
 
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	public String getMiddleName() {
 		return middleName;
 	}
 
 	public void setMiddleName(String middleName) {
 		this.middleName = middleName;
 	}
 
 	public String getLastName() {
 		return lastName;
 	}
 
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 
 	public String getFullName() {
 		return fullName;
 	}
 
 	public void setFullName(String fullName) {
 		this.fullName = fullName;
 	}
 
 	public String getPersonalId() {
 		return personalId;
 	}
 
 	public void setPersonalId(String personalId) {
 		this.personalId = personalId;
 	}
 
 	public String getUuid() {
 		return uuid;
 	}
 
 	public void setUuid(String uuid) {
 		this.uuid = uuid;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	public String getPostalAddress() {
 		return postalAddress;
 	}
 
 	public void setPostalAddress(String postalAddress) {
 		this.postalAddress = postalAddress;
 	}
 
 	public String getNationality() {
 		return nationality;
 	}
 
 	public void setNationality(String nationality) {
 		this.nationality = nationality;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getPhoneHome() {
 		return phoneHome;
 	}
 
 	public void setPhoneHome(String phoneHome) {
 		this.phoneHome = phoneHome;
 	}
 
 	public String getPhoneMobile() {
 		return phoneMobile;
 	}
 
 	public void setPhoneMobile(String phoneMobile) {
 		this.phoneMobile = phoneMobile;
 	}
 
 	public Date getDateOfBirth() {
 		return dateOfBirth;
 	}
 
 	public void setDateOfBirth(Date dateOfBirth) {
 		this.dateOfBirth = dateOfBirth;
 	}
 
 	public String getGender() {
 		return gender;
 	}
 
 	public void setGender(String gender) {
 		this.gender = gender;
 	}
 
 	public String getPostalCode() {
 		return postalCode;
 	}
 
 	public void setPostalCode(String postalCode) {
 		this.postalCode = postalCode;
 	}
 
 	public String getCity() {
 		return city;
 	}
 
 	public void setCity(String city) {
 		this.city = city;
 	}
 
 	public String getCountry() {
 		return country;
 	}
 
 	public void setCountry(String country) {
 		this.country = country;
 	}
 
 	public String getRelayLeg() {
 		return relayLeg;
 	}
 
 	public void setRelayLeg(String relayLeg) {
 		this.relayLeg = relayLeg;
 	}
 
 	public ShirtSize getShirtSize() {
 		return shirtSize;
 	}
 
 	public void setShirtSize(ShirtSize shirtSize) {
 		this.shirtSize = shirtSize;
 	}
 	
 	public boolean isForeigner() {
 		return foreigner;
 	}
 
 	public void setForeigner(boolean foreigner) {
 		this.foreigner = foreigner;
 	}
 
 	public String getImageURL() {
 		return imageURL;
 	}
 
 	public void setImageURL(String imageURL) {
 		this.imageURL = imageURL;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((dateOfBirth == null) ? 0 : dateOfBirth.hashCode());
 		result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
 		result = prime * result + ((personalId == null) ? 0 : personalId.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Participant other = (Participant) obj;
 		if (dateOfBirth == null) {
 			if (other.dateOfBirth != null)
 				return false;
 		} else if (!dateOfBirth.equals(other.dateOfBirth))
 			return false;
 		if (fullName == null) {
 			if (other.fullName != null)
 				return false;
 		} else if (!fullName.equals(other.fullName))
 			return false;
 		if (personalId == null) {
 			if (other.personalId != null)
 				return false;
 		} else if (!personalId.equals(other.personalId))
 			return false;
 		return true;
 	}
 
 	public String getLogin() {
 		return login;
 	}
 
 	public void setLogin(String login) {
 		this.login = login;
 	}
 }
