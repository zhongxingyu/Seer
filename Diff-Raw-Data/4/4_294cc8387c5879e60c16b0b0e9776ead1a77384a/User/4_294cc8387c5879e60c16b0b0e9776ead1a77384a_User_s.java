 /**
  * ESUP-Portail Blank Application - Copyright (c) 2006 ESUP-Portail consortium
  * http://sourcesup.cru.fr/projects/esup-blank
  */
 package org.esupportail.example.domain.beans;
 
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 
 import org.esupportail.commons.utils.strings.StringUtils;
 
 /**
  * The class that represent users.
  */
 @Entity
 public class User implements Serializable {
 	
 	/**
 	 * The serialization id.
 	 */
 	private static final long serialVersionUID = 9108580316214008120L;
 
 	/**
 	 * Id of the user.
 	 */
 	@Id
 	private String id;
 	
     /**
 	 * Display Name of the user.
 	 */
     private String displayName;
     
     /**
 	 * True for administrators.
 	 */
     private boolean admin;
 	
     /**
      * The prefered language.
      */
     private String language;
     
     /**
      * information recorded during database insert 
      * used in esup-example to illustrate open session in view mechanism
      */
    @OneToMany(cascade={CascadeType.ALL})
     private List<Information> informations;
     
 	/**
 	 * Bean constructor.
 	 */
 	public User() {
 		super();
 	}
 
 	/**
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(final Object obj) {
 		if (obj == null) {
 			return false;
 		}
 		if (!(obj instanceof User)) {
 			return false;
 		}
 		return id.equals(((User) obj).getId());
 	}
 
 	/**
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return super.hashCode();
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "User#" + hashCode() + "[id=[" + id + "], displayName=[" + displayName 
 		+ "], admin=[" + admin + "], language=[" + language + "]]";
 	}
 
 	/**
 	 * @return  the id of the user.
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * @param id
 	 */
 	public void setId(final String id) {
 		this.id = StringUtils.nullIfEmpty(id);
 	}
 
     /**
 	 * @return  Returns the displayName.
 	 */
     public String getDisplayName() {
         return this.displayName;
     }
 
     /**
 	 * @param displayName  The displayName to set.
 	 */
     public void setDisplayName(final String displayName) {
         this.displayName = StringUtils.nullIfEmpty(displayName);
     }
     
     /**
 	 * @param admin  The admin to set.
 	 */
     public void setAdmin(final boolean admin) {
         this.admin = admin;
     }
 
     /**
 	 * @return  Returns the admin.
 	 */
     public boolean isAdmin() {
         return this.admin;
     }
 
 	/**
 	 * @return the language
 	 */
 	public String getLanguage() {
 		return language;
 	}
 
 	/**
 	 * @param language the language to set
 	 */
 	public void setLanguage(final String language) {
 		this.language = StringUtils.nullIfEmpty(language);
 	}
 
 	/**
 	 * @return the informations
 	 */
 	public List<Information> getInformations() {
 		return informations;
 	}
 
 	/**
 	 * @param informations the informations to set
 	 */
 	public void setInformations(List<Information> informations) {
 		this.informations = informations;
 	}
     
 
 }
