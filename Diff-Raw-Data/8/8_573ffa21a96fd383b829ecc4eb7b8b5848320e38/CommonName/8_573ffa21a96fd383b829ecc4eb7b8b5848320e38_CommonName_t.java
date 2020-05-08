 package org.iucn.sis.shared.api.models;
 /**
  * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
  * 
  * This is an automatic generated file. It will be regenerated every time 
  * you generate persistence class.
  * 
  * Modifying its content may cause the program not work, or your work may lost.
  */
 
 /**
  * Licensee: 
  * License Type: Evaluation
  */
 import java.io.Serializable;
 import java.util.HashSet;
 
 import org.iucn.sis.shared.api.utils.XMLUtils;
 
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNodeList;
 public class CommonName implements Serializable {
 	
 	public static CommonName createCommonName(String name, String language,
 			String isoCode, boolean primary) {
 		return CommonName.createCommonName(name, language, isoCode, false, primary);
 	}
 	
 	public static CommonName createCommonName(String name, String language,
 			String isoCode, boolean validated, boolean isPrimary) {
 		return new CommonName(name, language, isoCode, validated, isPrimary);
 	}	
 	
 	public static CommonName fromXML(NativeElement commonNameTag) {
 		String id = commonNameTag.getAttribute("id");
 		String iso = commonNameTag.getAttribute("iso");
 		String language = commonNameTag.getAttribute("language");
 		boolean validated = commonNameTag.getAttribute("validated")
 				.equalsIgnoreCase("true");
 		String name = commonNameTag.getAttribute("name");
 		boolean primary = commonNameTag.getAttribute("primary")
 				.equalsIgnoreCase("true");
 		String reason = commonNameTag.getAttribute("reason");
 		
		
 		CommonName curName = new CommonName();
 		curName.setId(Integer.valueOf(id).intValue());
 		curName.setName(name);
 		if( iso != null || language != null )
 			curName.setIso(new IsoLanguage(language, iso));
 		curName.setValidated(validated);
 		curName.setPrincipal(primary);
 		if (!validated)
 			curName.setChangeReason(Integer.valueOf(reason).intValue());
 		NativeNodeList refs = commonNameTag.getElementsByTagName("reference");
 	
 		for (int i = 0; i < refs.getLength(); i++) {
 			curName.getReference().add(
 					Reference.fromXML((NativeElement) refs.item(i)));
 		}
 	
 		NativeNodeList notes = commonNameTag.getElementsByTagName("note");
 		for (int i = 0; i < notes.getLength(); i++) {
 			NativeElement current = (NativeElement) notes.item(i);
 			curName.getNotes().add(Notes.fromXML(current));
 		}
 		return curName;
 	}
 	
 	
 	
 	public static final String ROOT_TAG = "commonName";
 	public static final int UNSET = 0;
 	public static final int ADDED = 1;
 	public static final int DELETED = 2;
 	public static final int ISO_CHANGED = 3;
 	public static final int NAME_CHANGED = 4;
 	public static final String[] reasons = { "", "ADDED", "DELETED", "ISO CHANGED", "NAME CHANGED" };
 	
 	
 	private int id;
 	private String name;
 	private boolean principal;
 	private int changeReason;
 	private boolean validated;	
 	private IsoLanguage iso;
 	private Taxon taxon;
 	private java.util.Set<Reference> reference;
 	private java.util.Set<Notes> notes;
 	
 	public CommonName() {
 		this(null, null, null);
 	}
 	
 	public CommonName(String name, String language, String isoCode) {
 		this(name, language, isoCode, false, false);
 	}
 
 	public CommonName(String name, String language, String isoCode, boolean validated, boolean isPrimary) {
 		super();
 		this.name = name;
 		if (language != null && isoCode != null)
 			this.iso = new IsoLanguage(language, isoCode);
 		this.validated = validated;
 		this.principal = isPrimary;
 		this.reference = new HashSet<Reference>();
 		this.notes = new HashSet<Notes>();
 	}
 	
 	
 	public CommonName deepCopy() {
 		CommonName cn =  new CommonName(name, iso == null ? null : iso.getName(), iso == null ? null : iso.getCode(), validated, principal);
 		cn.setNotes(new HashSet<Notes>());
		cn.setChangeReason(getChangeReason());
 		for (Notes note : getNotes()) {
 			Notes newNote = note.deepCopy();
 			newNote.setCommonName(cn);
 			cn.getNotes().add(newNote);
 		}
 		return cn;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null)
 			return false;
 		if (obj instanceof CommonName) {
 			CommonName other = (CommonName) obj;
 			if ((other.name == null && getName() != null) || (other.name != null && getName() == null))
 				return false;
 			if ((other.iso == null && getIso() != null) || (other.iso != null && getIso() == null))
 				return false;
 			if (other.name == null && getName() == null && getIso() == null && other.getIso() == null)
 				return true;
 			if (other.name != null && getName() != null && getIso() == null && other.getIso() == null)
 				return false;
 			return (other.name.equalsIgnoreCase(getName()) && other.getIso().equals(getIso()));
 		}
 		return super.equals(obj);
 	}
 	
 	public int getChangeReason() {
 		return changeReason;
 	}
 	
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
 	
 	public int getId() {
 		return id;
 	}
 	
 	public IsoLanguage getIso() {
 		return iso;
 	}
 	
 	public String getIsoCode() {
 		return iso.getCode();
 	}
 	
 	public String getLanguage() {
 		return iso.getName();
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public java.util.Set<Notes> getNotes() {
 		return notes;
 	}
 	
 	public int getORMID() {
 		return getId();
 	}
 	
 	public boolean getPrincipal() {
 		return principal;
 	}
 	
 	public java.util.Set<Reference> getReference() {
 		return reference;
 	}
 	
 	
 	
 	public Taxon getTaxon() {
 		return taxon;
 	}
 	
 	public boolean getValidated() {
 		return validated;
 	}
 	
 	@Override
 	public int hashCode() {
 		return getName() == null? "".hashCode() : getName().toLowerCase().hashCode();
 	}
 	
 	public boolean isPrimary() {
 		return getPrincipal();
 	}
 	
 	
 	public void setChangeReason(int reason) {
 		validated = false;
 		changeReason = reason;
 	}
 	
 	public void setId(int value) {
 		this.id = value;
 	}
 	
 	public void setIso(IsoLanguage value) {
 		this.iso = value;
 	}
 	
 	public void setIsoCode(String isoCode) {
 		validated = false;
 		changeReason = ISO_CHANGED;
 		iso = IsoLanguage.getByIso(isoCode);
 	}
 	
 	public void setLanguage(String language) {
 		validated = false;
 		changeReason = ISO_CHANGED;
 		iso = IsoLanguage.getByLanguage(language);
 	}
 	
 	public void setName(String value) {
 		this.name = value;
 	}
 	
 	
 	public void setNotes(java.util.Set<Notes> value) {
 		this.notes = value;
 	}
 	
 	public void setPrincipal(boolean value) {
 		this.principal = value;
 	}
 	
 	
 	public void setReference(java.util.Set<Reference> value) {
 		this.reference = value;
 	}
 
 	public void setTaxon(Taxon value) {
 		this.taxon = value;
 	}
 
 	public void setValidated(boolean value) {
 		this.validated = value;
 	}
 
 	public String toString() {
 		return String.valueOf(getId());
 	}
 
 	public String toXML() {
 		String xml = "<" + ROOT_TAG + " ";
 		xml += (getIso() == null ? "" : (" iso=\""
 				+ XMLUtils.clean(getIso().getCode())
 				+ "\" language=\""
 				+ XMLUtils.clean(getIso().getName()) + "\""));
 		xml += " validated=\"" + getValidated() + "\" name=\""
 				+ XMLUtils.clean(getName()) + "\" primary=\""
 				+ getPrincipal() + "\" id=\"" + getId() + "\"";
 		if (!getValidated())
			xml += " reason=\"" + getChangeReason() + "\"";
		xml += ">";
 	
 		xml += "<sources>";
 		for (Reference ref : getReference()) {
 			xml += ref.toXML();
 		}
 		xml += "</sources>";
 	
 		xml += "<notes>";
 		for (Notes note : getNotes()) {
 			xml += note.toXML();
 		}
 		xml += "</notes>";
 	
 		xml += "</commonName>\r\n";
 	
 		return xml;
 	}
 	
 }
