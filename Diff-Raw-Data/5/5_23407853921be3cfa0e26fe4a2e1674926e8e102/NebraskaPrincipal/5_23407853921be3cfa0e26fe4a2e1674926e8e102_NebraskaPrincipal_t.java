 package org.thera_pi.nebraska.crypto;
 
 import java.util.regex.Pattern;
 
 /**
  * This class represents a principal (subject or issuer of a certificate).
  * It is used to split the distinguished name string into the usual fields
  * and provides a comparison function.
  *    
  * @author bodo
  *
  */
 public class NebraskaPrincipal {
 	private String country;
 	private String organization;
 	private String institutionName;
 	private String institutionID;
 	private String personName;
 	private String distinguishedName;
 	
 	// getters and setters
 	public String getCountry() {
 		return country;
 	}
 
 	public void setCountry(String country) {
 		this.country = country;
 		// force re-generation
 		this.distinguishedName = null;
 	}
 
 	public String getOrganization() {
 		return organization;
 	}
 
 	public void setOrganization(String organization) {
 		this.organization = organization;
 		// force re-generation
 		this.distinguishedName = null;
 	}
 
 	public String getInstitutionName() {
 		return institutionName;
 	}
 
 	public void setInstitutionName(String institutionName) {
 		this.institutionName = institutionName;
 		// force re-generation
 		this.distinguishedName = null;
 	}
 
 	public String getInstitutionID() {
 		return institutionID;
 	}
 
 	public void setInstitutionID(String institutionID) {
 		this.institutionID = institutionID;
 		// force re-generation
 		this.distinguishedName = null;
 	}
 
 	public String getPersonName() {
 		return personName;
 	}
 
 	public void setPersonName(String personName) {
 		this.personName = personName;
 		// force re-generation
 		this.distinguishedName = null;
 	}
 
 	/**
 	 * Create an alias from the name fields.
 	 * Uses the institution ID (IK) field if present, otherwise the 
 	 * full distinguished name without spaces.
 	 * 
 	 * @return the alias
 	 */
 	public String getAlias() {
 		if(institutionID != null && institutionID.length() > 0)
 		{
 			return institutionID;
 		}
 		return distinguishedName.replace(" ", "");
 	}
 	
 	/**
 	 * Create a principal object from individual name fields.
 	 * 
 	 * @param country
 	 * @param organization
 	 * @param institutionName
 	 * @param institutionID
 	 * @param personName
 	 */
 	public NebraskaPrincipal(String country, String organization,
 			String institutionName, String institutionID, String personName) {
 		this.country = country;
 		this.organization = organization;
 		this.institutionName = institutionName;
 		this.institutionID = institutionID;
 		this.personName = personName;
 	}
 	
 	/**
 	 * create a principal object from a distinguished name string.
 	 * 
 	 * @param distinguishedName
 	 */
 	public NebraskaPrincipal(String distinguishedName) {
 		this.distinguishedName = distinguishedName;
 		String[] dnParts = distinguishedName.split(",");
 		for(int i = 0; i < dnParts.length; i++) {
 			String[] keyVal = dnParts[i].trim().split(" *= *", 2);
 			if(keyVal.length == 2) {
 				if("CN".equals(keyVal[0])) {
 					personName = keyVal[1];
 				} else if("OU".equals(keyVal[0])) {
 					if(Pattern.matches("^IK[0-9]+$", keyVal[1])) {
 						institutionID = NebraskaUtil.normalizeIK(keyVal[1]);
 					} else {
 						institutionName = keyVal[1];
 					}
 				} else if("C".equals(keyVal[0])) {
 					country = keyVal[1];
 				} else if("O".equals(keyVal[0])) {
 					organization = keyVal[1];
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Helper class to append only existing name parts to a distinguished name string.
 	 * 
 	 * @author bodo
 	 *
 	 */
 	private class DistinguishedNameBuffer {
 		StringBuffer buf = new StringBuffer();
 
 		/**
 		 * Append key=value if value is not null.
 		 * 
 		 * @param key key string
 		 * @param value value string
 		 */
 		public void appendPart(String key, String value)
 		{
 			appendPart(key, null, value);
 		}
 		/**
 		 * Append key=prefixvalue if value is not null.
 		 * Intended for institution ID (IK).
 		 * 
 		 * @param key key string
 		 * @param prefix prefix string
 		 * @param value value string
 		 */
 		public void appendPart(String key, String prefix, String value)
 		{
 			if(value != null)
 			{
 				if(buf.length() > 0) buf.append(", ");
 				buf.append(key);
 				if(!key.endsWith("=")) buf.append("=");
				if(prefix != null)
				{
					buf.append(prefix);
				}
 				buf.append(value);
 			}
 		}
 		public String toString()
 		{
 			return buf.toString();
 		}
 	}
 	
 	/**
 	 * Reconstruct distinguished name from name parts if necessary and 
 	 * return distinguished name.
 	 * 
 	 * @return distinguishedName
 	 */
 	public String getDistinguishedName()
 	{
 		if(distinguishedName == null)
 		{
 			DistinguishedNameBuffer buf = new DistinguishedNameBuffer();
 			buf.appendPart("CN", personName);
 			buf.appendPart("OU", "IK", institutionID);
 			buf.appendPart("OU=",institutionName);
 			buf.appendPart("O", organization);
 			buf.appendPart("C", country);
 			distinguishedName = buf.toString();
 		}
 		return distinguishedName;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	public boolean equals(Object o)
 	{
 		// other object must exist and must be of this object's class.
 		if(o == null) return false;
 		if(!(o instanceof NebraskaPrincipal)) return false;
 
 		NebraskaPrincipal other = (NebraskaPrincipal) o;
 		/* If a field exists in one object but not in the other,
 		 * the objects are not equal.
 		 */
 		if((this.country == null) != (other.country == null)) return false;
 		if((this.organization == null) != (other.organization == null)) return false;
 		if((this.institutionID == null) != (other.institutionID == null)) return false;
 		if((this.institutionName == null) != (other.institutionName == null)) return false;
 		if((this.personName == null) != (other.personName == null)) return false;
 		
 		/* if any of the existing fields is not equal to the corresponding field
 		 * of the other object they are not equal. 
 		 */
 		if(this.country != null && !this.country.equals(other.country)) return false;
 		if(this.organization != null && !this.organization.equals(other.organization)) return false;
 		if(this.institutionID != null && !this.institutionID.equals(other.institutionID)) return false;
 		if(this.institutionName != null && !this.institutionName.equals(other.institutionName)) return false;
 		if(this.personName != null && !this.personName.equals(other.personName)) return false;
 
 		// Objects must be equal if we reached this.
 		return true;
 	}
 }
