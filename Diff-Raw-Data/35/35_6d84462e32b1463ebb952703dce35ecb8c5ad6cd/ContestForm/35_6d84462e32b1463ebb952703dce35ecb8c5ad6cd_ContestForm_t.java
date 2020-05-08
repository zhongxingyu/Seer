 package org.icemobile.samples.spring.mediacast;
 
 import javax.validation.constraints.Size;
 
 import org.hibernate.validator.constraints.Email;
 import org.hibernate.validator.constraints.NotEmpty;
 
 public class ContestForm {
 	
	public static final String PAGE_UPLOAD = "upload";
	public static final String PAGE_GALLERY = "gallery";
	public static final String PAGE_VIEWER = "viewer";
	public static final String PAGE_ALL = "all";


	public static final String DESKTOP = "d";
	public static final String MOBILE = "m";
	public static final String TABLET = "t";
 	
 	@NotEmpty @Email 
 	private String email;
 	
 	@Size(max = 164)
 	private String description;
 	
 	private String l;//layout
 	
	private String p = PAGE_UPLOAD;//page
 	
 	private String photoId;
 	
 	private String form;
 	
 	private String voterId;
 	
 	public String getForm() {
 		return form;
 	}
 
 	public void setForm(String form) {
 		this.form = form;
 	}
 
 	public String getPhotoId() {
 		return photoId;
 	}
 
 	public void setPhotoId(String photoId) {
 		this.photoId = cleanParam(photoId);
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = cleanParam(email);
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getL() {
 		return l;
 	}
 
 	public void setL(String layout) {
 		if( notNullOrEmpty(layout)){
 			this.l = cleanParam(layout);
			if( TABLET.equals(l) ){
				p = PAGE_ALL;
			}
			else if( PAGE_ALL.equals(p)){
				p = PAGE_UPLOAD;
			}
 		}
 	}
 	
 	public boolean isEmpty(){
 		return this.email == null || this.email.length() == 0;
 	}
 	
 	public String cleanParam(String param){
 		if( param != null && param.indexOf(",") > 0 ){
 			param = param.substring(0,param.indexOf(","));
 		}
 		return param;
 	}
 	
 	private boolean notNullOrEmpty(String param){
 		if( param != null && param.length() > 0 ){
 			return true;
 		}
 		return false;
 	}
 
 	public String getVoterId() {
 		return voterId;
 	}
 
 	public void setVoterId(String voterId) {
 		if( notNullOrEmpty(voterId)){
 			this.voterId = cleanParam(voterId);
 		}
 	}
 	
 
 	public String getP() {
 		return p;
 	}
 
 	public void setP(String p) {
		this.p = cleanParam(p);
 	}
 
 	@Override
 	public String toString() {
 		return "ContestForm [email=" + email + ", description=" + description
 				+ ", l=" + l + ", p=" + p + ", photoId=" + photoId + ", form="
 				+ form + ", voterId=" + voterId  + "]";
 	}
 
 
 }
