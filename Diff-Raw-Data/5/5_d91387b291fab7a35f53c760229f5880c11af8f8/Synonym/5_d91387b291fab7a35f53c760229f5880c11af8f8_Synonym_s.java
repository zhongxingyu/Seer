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
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNode;
 import com.solertium.lwxml.shared.NativeNodeList;
 
 public class Synonym implements Serializable {
 
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
 	// FIXME -- DELETE TAXON STATUS
 	public final static String ROOT_TAG = "synonym";
 	public final static String ACCEPTED = "ACCEPTED";
 	public final static String ADDED = "ADD";
 	public final static String DELETED = "DELETE";
 	public final static String CHANGED = "CHANGED";
 	public final static String NEW = "NEW";
 	public final static String MERGE = "MERGE";
 	public final static String SPLIT = "SPLIT";
 
 	public Synonym(Taxon taxon) {
 		this();
 		if (taxon.getTaxonLevel().getLevel() < TaxonLevel.SPECIES) {
 			setName(taxon.getName());
 			setAuthor(taxon.getTaxonomicAuthority());
 		} else {
 			setName(taxon.getFootprint()[TaxonLevel.GENUS]);
 			if (taxon.getTaxonLevel().getLevel() == TaxonLevel.SPECIES) {
 				setSpeciesAuthor(taxon.getTaxonomicAuthority());
 				setSpeciesName(taxon.getName());
 			} else if (taxon.getTaxonLevel().getLevel() == TaxonLevel.INFRARANK) {
 				setSpeciesName(taxon.getFootprint()[TaxonLevel.SPECIES]);
 				setInfrarankAuthor(taxon.getTaxonomicAuthority());
 				setInfraName(taxon.getName());
 			} else if (taxon.getTaxonLevel().getLevel() == TaxonLevel.SUBPOPULATION) {
 				setSpeciesName(taxon.getFootprint()[TaxonLevel.SPECIES]);
 				setStockName(taxon.getName());
 			} else if (taxon.getTaxonLevel().getLevel() == TaxonLevel.INFRARANK_SUBPOPULATION) {
 				setSpeciesName(taxon.getFootprint()[TaxonLevel.SPECIES]);
 				setInfraName(taxon.getFootprint()[TaxonLevel.INFRARANK]);
 				setStockName(taxon.getName());
 			}
 		}
 		setFriendlyName(taxon.getFriendlyName());
 		setStatus(Synonym.NEW);
 		setTaxon(taxon);
 		setTaxon_level(taxon.getTaxonLevel());
 	}
 
 	public static Synonym fromXML(NativeElement synTag, Taxon taxon) {
 		Synonym s = new Synonym();
 		fromXML(s, synTag, taxon);
 		return s;
 	}
 	
 	public static void fromXML(Synonym s, NativeElement synTag, Taxon taxon) {
 		s.setId(Integer.valueOf(synTag.getAttribute("id")));
 		s.setStatus(synTag.getAttribute("status"));
 		
 		final Set<Notes> newNotes = new HashSet<Notes>();
 		
 		final NativeNodeList nodes = synTag.getChildNodes();
 		for (int i = 0; i < nodes.getLength(); i++) {
 			final NativeNode node = nodes.item(i);
 			String name = node.getNodeName();
 			if ("name".equals(name))
 				s.setName(node.getTextContent());
 			else if("speciesName".equals(name))
 				s.setSpeciesName(node.getTextContent());
 			else if ("infrarankName".equals(name))
 				s.setInfraName(node.getTextContent());
 			else if ("note".equals(name))
 				newNotes.add(Notes.fromXML((NativeElement)node));
 			else if ("stockName".equals(name))
 				s.setStockName(node.getTextContent());
 			else if ("author".equals(name))
 				s.setAuthor(node.getTextContent());
 			else if ("speciesAuthor".equals(name))
 				s.setSpeciesAuthor(node.getTextContent());
 			else if ("infrarankAuthor".equals(name))
 				s.setInfrarankAuthor(node.getTextContent());
 			else if ("friendlyName".equals(name))
 				s.setFriendlyName(node.getTextContent());
 			else if ("level".equals(name)) {
 				String synLevel = node.getTextContent();
 				if (!(synLevel == null || !synLevel.matches("\\d+")))
 					s.setTaxon_level(TaxonLevel.getTaxonLevel(Integer.parseInt(synLevel)));
 			}
 			else if (Infratype.ROOT_NAME.equals(name)) {
 				s.setInfraType(Infratype.fromXML((NativeElement)node, null).getName());
 			}
 		}
 		
 		if (s.getNotes().isEmpty())
 			s.getNotes().addAll(newNotes);
 		else {
 			final List<Notes> toRemove = new ArrayList<Notes>();
 			for (Notes note : s.getNotes())
 				if (!newNotes.contains(note))
 					toRemove.add(note);
 			
 			for (Notes note : toRemove)
 				s.getNotes().remove(note);
 		}
 		if (taxon != null)
 			taxon.getSynonyms().add(s);
 		s.setTaxon(taxon);
 	}
 
 	public String toXML() {
 		StringBuilder xml = new StringBuilder();
 		xml.append("<" + Synonym.ROOT_TAG + " id=\"" + getId() + "\" status=\"" + getStatus() + "\">");
 		xml.append("<name><![CDATA[" + ((getName() == null)? "" : getName()) + "]]></name>");
 		xml.append("<speciesName><![CDATA[" + ((getSpeciesName() == null)? "" : getSpeciesName()) + "]]></speciesName>");
 		xml.append("<infrarankName><![CDATA[" + ((getInfraName() == null)? "" : getInfraName()) + "]]></infrarankName>");
 		xml.append("<stockName><![CDATA[" + ((getStockName() == null)? "" : getStockName()) + "]]></stockName>");
 		xml.append("<author><![CDATA[" + ((getAuthor() == null)? "" : getAuthor()) + "]]></author>");
 		xml.append("<speciesAuthor><![CDATA[" + ((getSpeciesAuthor() == null)? "" : getSpeciesAuthor()) + "]]></speciesAuthor>");
		xml.append("<infrarankAuthor><![CDATA[" + ((getInfraName() == null)? "" : getInfraName()) + "]]></infrarankAuthor>");
 		xml.append(getTaxon_level() != null ? "<level><![CDATA[" + getTaxon_level().getLevel() + "]]></level>" : ""); 
 		xml.append("<friendlyName><![CDATA[" + ((getFriendlyName() == null)? "" : getFriendlyName()) + "]]></friendlyName>");
 
 
 		for (Notes note : getNotes())
 			xml.append(note.toXML());
 
 		if (getInfraType() != null) {
 			xml.append(Infratype.getInfratype(getInfraType()).toXML());
 		}
 
 		xml.append("</" + Synonym.ROOT_TAG + ">");
 		return xml.toString();
 	}
 
 	public void clearAuthorities() {
 		setAuthor(null);
 		setSpeciesAuthor(null);
 		setInfrarankAuthor(null);
 	}
 
 	public String toDisplayableString() {
 		return getFriendlyName();
 	}
 
 	public void setName(String value) {
 		this.genusName = value;
 	}
 
 	public void setAuthor(String value) {
 		this.genusAuthor = value;
 	}
 
 	public String getAuthor() {
 		return genusAuthor;
 	}
 
 	public String getName() {
 		return genusName;
 	}
 
 	public String getGenusAuthor() {
 		return genusAuthor;
 	}
 
 	public String getGenusName() {
 		return genusName;
 	}
 
 	public void setGenusAuthor(String genusAuthor) {
 		this.genusAuthor = genusAuthor;
 	}
 
 	public void setGenusName(String genusName) {
 		this.genusName = genusName;
 	}
 
 	private String status;
 
 	public String getStatus() {
 		return status;
 	}
 
 	public void setStatus(String status) {
 		this.status = status;
 	}
 
 	private String infraType;
 
 	public void setInfraTypeObject(Infratype value) {
		this.infraType = value.getName();
 	}
 	
 	public void setInfraType(String infratypeName) {
 		infraType = infratypeName;
 	}
 
 	public String getInfraType() {
 		return infraType;
 	}
 
 	public int getTaxaID() {
 		if (taxon != null)
 			return taxon.getId();
 		else
 			return -1;
 	}
 
 	public void setAuthority(String authority, int level) {
 		if (level <= TaxonLevel.GENUS) {
 			setAuthor(authority);
 		} else if (level == TaxonLevel.SPECIES) {
 			setSpeciesAuthor(authority);
 		} else if (level > TaxonLevel.SPECIES) {
 			setInfrarankAuthor(authority);
 		}
 	}
 
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
 
 	public Synonym() {
 		this.generationID = new Date().getTime();
 		this.notes = new HashSet<Notes>();
 	}
 
 	@Override
 	public int hashCode() {
 		return new Long(generationID).hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Synonym other = (Synonym) obj;
 		if (generationID != other.generationID)
 			return false;
 		return true;
 	}
 
 	private int id;
 
 	private String friendlyName;
 
 	private String genusName;
 
 	private String speciesName;
 
 	private String infraName;
 
 	private String stockName;
 
 	private String genusAuthor;
 
 	private String speciesAuthor;
 
 	private String infrarankAuthor;
 
 	private TaxonLevel taxon_level;
 
 	private Taxon taxon;
 
 	private java.util.Set<Notes> notes = new java.util.HashSet<Notes>();
 
 	private java.util.Set<Reference> reference = new java.util.HashSet<Reference>();
 	
 	protected long generationID;
 
 	public void setId(int value) {
 		this.id = value;
 		this.generationID = value;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public int getORMID() {
 		return getId();
 	}
 
 	public void setFriendlyName(String value) {
 		this.friendlyName = value;
 	}
 
 	public String getFriendlyName() {
 		if (friendlyName == null || friendlyName.trim().equals("")) {
 			generateFriendlyName();
 		}
 		return friendlyName;
 	}
 	
 	public String generateFriendlyName() {
 		friendlyName = getName();
 		if (getSpeciesName() != null && !getSpeciesName().trim().equalsIgnoreCase("")) {
 			friendlyName += " " + getSpeciesName();
 			if (getSpeciesAuthor() != null && !getSpeciesAuthor().trim().equalsIgnoreCase("")) 
 				friendlyName += " ("+ getSpeciesAuthor() + ")";
 			if (getInfraName() != null && !getInfraName().trim().equalsIgnoreCase("")) {
 				friendlyName += " " + Infratype.getDisplayString(getInfraType()) + " " + getInfraName();
 				if (getInfrarankAuthor() != null && !getInfrarankAuthor().trim().equalsIgnoreCase("")) 
 					friendlyName += " ("+ getInfrarankAuthor() + ")";
 			}
 			if (getStockName() != null) {
 				friendlyName += " " + getStockName();
 			}
 		}
 		return friendlyName;
 	}
 
 	public void setSpeciesName(String value) {
 		this.speciesName = value;
 	}
 
 	public String getSpeciesName() {
 		return speciesName;
 	}
 
 	public void setInfraName(String value) {
 		this.infraName = value;
 	}
 
 	public String getInfraName() {
 		return infraName;
 	}
 
 	public void setStockName(String value) {
 		this.stockName = value;
 	}
 
 	public String getStockName() {
 		return stockName;
 	}
 
 	public void setSpeciesAuthor(String value) {
 		this.speciesAuthor = value;
 	}
 
 	public String getSpeciesAuthor() {
 		return speciesAuthor;
 	}
 
 	public void setInfrarankAuthor(String value) {
 		this.infrarankAuthor = value;
 	}
 
 	public String getInfrarankAuthor() {
 		return infrarankAuthor;
 	}
 
 	public void setTaxon(Taxon value) {
 		this.taxon = value;
 	}
 
 	public Taxon getTaxon() {
 		return taxon;
 	}
 
 	public void setTaxon_level(TaxonLevel value) {
 		this.taxon_level = value;
 	}
 
 	public TaxonLevel getTaxon_level() {
 		return taxon_level;
 	}
 
 	public void setNotes(java.util.Set<Notes> value) {
 		this.notes = value;
 	}
 
 	public java.util.Set<Notes> getNotes() {
 		return notes;
 	}
 
 	public void setReference(java.util.Set<Reference> value) {
 		this.reference = value;
 	}
 
 	public java.util.Set<Reference> getReference() {
 		return reference;
 	}
 
 	public String toString() {
 		return String.valueOf(getId());
 	}
 
 	private boolean _saved = false;
 
 	public void onSave() {
 		_saved = true;
 	}
 
 	public void onLoad() {
 		_saved = true;
 	}
 
 	public boolean isSaved() {
 		return _saved;
 	}
 
 }
