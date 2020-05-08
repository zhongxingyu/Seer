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
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.data.LongUtils;
 import org.iucn.sis.shared.api.utils.CanonicalNames;
 import org.iucn.sis.shared.api.utils.XMLUtils;
 
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNode;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.portable.XMLWritingUtils;
 
 public class Taxon implements AuthorizableObject, Serializable {
 
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
 	private static final long serialVersionUID = -3917537005947441129L;
 	public static final String ROOT_TAG = "taxon";
 	public static final int DELETED = -1;
 	public static final int ACTIVE = 0;
 
 	public int state;
 
 	public int getState() {
 		return state;
 	}
 
 	public void setState(int state) {
 		this.state = state;
 	}
 
 	public void setId(int value) {
 		this.id = value;
 	}
 
 	public String getFullURI() {
 		return "resource/taxon/" + getTaxonLevel().getLevel() + "/" + getName();
 	}
 	
 	public boolean isScoped() {
 		return true;
 	}
 
 	public String getKingdomName() {
 		return getFootprint()[0];
 	}
 
 	@Override
 	public String getProperty(String key) {
 		return "";
 	}
 
 	public String getDisplayableLevel() {
 		if (this.getTaxonLevel().getLevel() == TaxonLevel.INFRARANK) {
 			Infratype infratype = getInfratype();
 			if (infratype == null)
 				return "Unknown";
 			else
 				return infratype.getFormalName();
 		}
 		return TaxonLevel.displayableLevel[this.getTaxonLevel().getLevel()];
 	}
 
 	public void setParentId(int parentID) {
 		Taxon parent = new Taxon();
 		parent.setId(parentID);
 		setParent(parent);
 	}
 
 	public int getParentId() {
 		if (parent != null)
 			return parent.getId();
 		return 0;
 	}
 
 	public String getParentName() {
 		if (parent == null)
 			return null;
 		return parent.getFriendlyName();
 	}
 
 	public void correctFullName() {
 		String fullName = generateFullName();
 		this.friendlyName = fullName;
 	}
 
 	public String generateFullName() {
 
 		String fullName = null;
 		
 		if (getTaxonLevel().getLevel() <= TaxonLevel.GENUS)
 			fullName = name;
 		else {
 			fullName = getParent().getFriendlyName();
 			if (infratype != null) {
 				fullName += " " + infratype.getCode();
 				
 				for (int code : Infratype.ALL) {
 					Infratype type = Infratype.getInfratype(code);
 					name = name.replace(type.getCode(), "").trim();
 				}
 			}
 			fullName += " " + name;
 			
 		}
 		return fullName.trim();
 	}
 
 	private String[] footprint;
 	private Integer[] footprintID;
 
 	protected Edit lastEdit;
 
 	public Edit getLastEdit() {
 		if (lastEdit == null) {
 			if (getEdits().size() > 1) {
 				List<Edit> edits = new ArrayList<Edit>();
 				edits.addAll(getEdits());
 				Collections.sort(edits);
 				lastEdit = edits.get(0);
 			} else if (getEdits().size() == 1) {
 				lastEdit = getEdits().iterator().next();
 			}
 		}
 		return lastEdit;
 
 	}
 
 	public void setStatus(String status) {
 		setTaxonStatus(TaxonStatus.fromCode(status));
 	}
 
 	public void setFootprint(String[] footprint) {
 		this.footprint = footprint;
 	}
 
 	// FIXME
 	public Integer[] getIDFootprint() {
 		if (this.footprintID == null) {
 			ArrayList<Integer> nodes = new ArrayList<Integer>();
 			nodes.add(this.getId());
 			Taxon node = this;
 			while (node.getParent() != null) {
 				node = node.getParent();
 				nodes.add(node.getId());
 			}
 			Collections.reverse(nodes);
 			footprintID = nodes.toArray(new Integer[nodes.size()]);
 		}
 		return footprintID;
 	}
 
 	public String[] getFootprint() {
 		if (this.footprint == null) {
 			ArrayList<String> nodes = new ArrayList<String>();
 			nodes.add(this.getName());
 			Taxon node = this;
 			while (node.getParent() != null) {
 				node = node.getParent();
 				nodes.add(node.getName());
 			}
 			Collections.reverse(nodes);
 			footprint = nodes.toArray(new String[nodes.size()]);
 		}
 		return footprint;
 	}
 
 	public String getFootprintCSV() {
 		return getFootprintAsString(0, ",");
 	}
 
 	public String getFootprintAsString() {
 		return getFootprintAsString(0);
 	}
 
 	public String getFootprintAsString(int startIndex) {
 		return getFootprintAsString(startIndex, " ");
 	}
 
 	public String getFootprintAsString(int startIndex, String separator) {
 		getFootprint();
 		String ret = "";
 
 		if (footprint.length <= startIndex)
 			return ret;
 
 		for (int i = startIndex; i < footprint.length - 1; i++)
 			ret += footprint[i] + separator;
 		ret += footprint[footprint.length - 1];
 
 		return ret;
 	}
 
 	public String getIDFootprintAsString(int startIndex, String separator) {
 		getIDFootprint();
 		String ret = "";
 
 		if (footprintID.length <= startIndex)
 			return ret;
 
 		for (int i = startIndex; i < footprintID.length - 1; i++)
 			ret += footprintID[i] + separator;
 		ret += footprintID[footprintID.length - 1];
 
 		return ret;
 	}
 
 	public static String getDisplayableLevel(int level) {
 		return TaxonLevel.getDisplayableLevel(level);
 	}
 
 	public static int getDisplayableLevelCount() {
 		return TaxonLevel.displayableLevel.length;
 	}
 
 	public String getSequenceCode() {
 		return "0.0";
 	}
 
 	public boolean isDeprecated() {
 		String code = getTaxonStatus().getCode();
 		
 		return TaxonStatus.STATUS_SYNONYM.equals(code) || 
 			TaxonStatus.STATUS_DISCARDED.equals(code);
 	}
 	
 	public boolean isNotAssigned() {
 		return TaxonStatus.STATUS_NOT_ASSIGNED.equals(getStatusCode());
 	}
 
 	public int getLevel() {
 		return getTaxonLevel().getLevel();
 	}
 
 	public String getFullName() {
 		return getFriendlyName();
 	}
 
 	protected CommonName primaryCommonName;
 
 	public CommonName getPrimaryCommonName() {
 		if (primaryCommonName == null) {
 			for (CommonName name : getCommonNames()) {
 				if (name.isPrimary()) {
 					primaryCommonName = name;
 					break;
 				}
 			}
 			if (primaryCommonName == null && !getCommonNames().isEmpty())
 				primaryCommonName = getCommonNames().iterator().next();
 			
 		}
 		return primaryCommonName;
 	}
 
 
 
 	public String getChildrenCSV() {
 		StringBuilder builder = new StringBuilder();
 		for (Taxon child : getChildren()) {
 			builder.append(child.getId() + ",");
 		}
 		if (builder.length() > 0) {
 			return builder.substring(0, builder.length() - 1);
 		}
 		return "";
 	}
 
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
 
 	public Taxon() {
 		working_set = new java.util.HashSet<WorkingSet>();
 		reference = new java.util.HashSet<Reference>();
 		children = new java.util.HashSet<Taxon>();
 		edits = new java.util.HashSet<Edit>();
 		notes = new java.util.HashSet<Notes>();
 		assessments = new java.util.HashSet<Assessment>();
 		synonyms = new java.util.HashSet<Synonym>();
 		commonNames = new java.util.HashSet<CommonName>();
 		images = new java.util.HashSet<TaxonImage>();
 	}
 
 	private int id;
 	
 	private Integer internalID;
 
 	private TaxonLevel taxonLevel;
 
 	private String name;
 
 	private String friendlyName;
 
 	private boolean hybrid;
 
 	private String taxonomicAuthority;
 
 	private TaxonStatus taxonStatus;
 	
 	private Integer sequence;
 	
 	private boolean invasive;
 	
 	private boolean feral;
 
 	private Taxon parent;
 
 	private java.util.Set<WorkingSet> working_set;
 
 	private java.util.Set<Reference> reference;
 
 	private java.util.Set<Taxon> children;
 
 	private java.util.Set<Edit> edits;
 
 	private java.util.Set<Notes> notes;
 
 	private java.util.Set<Assessment> assessments;
 
 	private java.util.Set<Synonym> synonyms;
 
 	private java.util.Set<CommonName> commonNames;
 	
 	private java.util.Set<TaxonImage> images;
 
 	private Infratype infratype;
 	
 	private Field taxonomicNotes;
 	
 	private String generatedXML;
 
 	public int getId() {
 		return id;
 	}
 
 	public int getORMID() {
 		return getId();
 	}
 	
 	public Integer getInternalID() {
 		return internalID;
 	}
 	
 	public void setInternalID(Integer internalID) {
 		this.internalID = internalID;
 	}
 
 	public void setName(String value) {
 		this.name = value;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setFriendlyName(String value) {
 		this.friendlyName = value;
 	}
 
 	public String getFriendlyName() {
 		return friendlyName;
 	}
 
 	public void setHybrid(boolean value) {
 		this.hybrid = value;
 	}
 
 	public boolean getHybrid() {
 		return hybrid;
 	}
 	
 	public void setSequence(Integer sequence) {
 		this.sequence = sequence;
 	}
 	
 	public Integer getSequence() {
 		return sequence;
 	}
 	
 	public void setInvasive(boolean invasive) {
 		this.invasive = invasive;
 	}
 	
 	public boolean getInvasive() {
 		return invasive;
 	}
 	
 	public void setFeral(boolean feral) {
 		this.feral = feral;
 	}
 	
 	public boolean getFeral() {
 		return feral;
 	}
 
 	public void setTaxonomicAuthority(String value) {
 		this.taxonomicAuthority = value;
 	}
 
 	public String getTaxonomicAuthority() {
 		return taxonomicAuthority;
 	}
 
 	public void setWorking_set(java.util.Set<WorkingSet> value) {
 		this.working_set = value;
 	}
 
 	public java.util.Set<WorkingSet> getWorking_set() {
 		return working_set;
 	}
 
 	public void setReference(java.util.Set<Reference> value) {
 		this.reference = value;
 	}
 
 	public java.util.Set<Reference> getReference() {
 		return reference;
 	}
 
 	public void setTaxonLevel(TaxonLevel value) {
 		this.taxonLevel = value;
 	}
 
 	public TaxonLevel getTaxonLevel() {
 		return taxonLevel;
 	}
 
 	public void setChildren(java.util.Set<Taxon> value) {
 		this.children = value;
 	}
 
 	public java.util.Set<Taxon> getChildren() {
 		return children;
 	}
 
 	public void setTaxonStatus(TaxonStatus value) {
 		this.taxonStatus = value;
 	}
 
 	public String getStatusCode() {
 		return taxonStatus.getCode();
 	}
 
 	public TaxonStatus getTaxonStatus() {
 		return taxonStatus;
 	}
 
 	public void setEdits(java.util.Set<Edit> value) {
 		this.edits = value;
 	}
 
 	public java.util.Set<Edit> getEdits() {
 		return edits;
 	}
 
 	public void setNotes(java.util.Set<Notes> value) {
 		this.notes = value;
 	}
 
 	public java.util.Set<Notes> getNotes() {
 		return notes;
 	}
 
 	public void setParent(Taxon value) {
 		this.parent = value;
 	}
 
 	public Taxon getParent() {
 		return parent;
 	}
 
 	public void setAssessments(java.util.Set<Assessment> value) {
 		this.assessments = value;
 	}
 
 	public java.util.Set<Assessment> getAssessments() {
 		return assessments;
 	}
 
 	public void setSynonyms(java.util.Set<Synonym> value) {
 		this.synonyms = value;
 	}
 
 	public java.util.Set<Synonym> getSynonyms() {
 		return synonyms;
 	}
 
 	public void setCommonNames(java.util.Set<CommonName> value) {
 		this.commonNames = value;
 	}
 
 	public java.util.Set<CommonName> getCommonNames() {
 		return commonNames;
 	}
 	
 	public void setImages(java.util.Set<TaxonImage> images) {
 		this.images = images;
 	}
 	
 	public java.util.Set<TaxonImage> getImages() {
 		return images;
 	}
 
 	public void setInfratype(Infratype value) {
 		this.infratype = value;
 	}
 
 	public Infratype getInfratype() {
 		return infratype;
 	}
 	
 	public void setTaxonomicNotes(Field taxonomicNotes) {
 		this.taxonomicNotes = taxonomicNotes;
 	}
 	
 	public Field getTaxonomicNotes() {
 		return taxonomicNotes;
 	}
 
 	public String toString() {
 		return "Taxon #" + id + " :" + name;
 	}
 
 	public String toXMLMinimal() {
 		return toXMLMinimal(false);
 	}
 	
 	public String toXMLMinimal(boolean showFootprint) {
 		return toRelatedXML(ROOT_TAG, showFootprint);
 	}
 
 	/**
 	 * Parses the parent or child NativeElement
 	 * 
 	 * @param element
 	 *            -- start with either parent, or child, or is basic
 	 * @return
 	 */
 	public static Taxon fromXMLminimal(NativeElement element) {
 		Taxon taxon = new Taxon();
 		taxon.setId(Integer.parseInt(element.getAttribute("id")));
		taxon.setTaxonLevel(TaxonLevel.getTaxonLevel(Integer.parseInt(element.getAttribute("level"))));
 		
 		NativeNodeList children = element.getChildNodes();
 		for (int i = 0; i < children.getLength(); i++) {
 			NativeNode child = children.item(i);
 			if ("name".equals(child.getNodeName()))
 				taxon.setName(child.getTextContent());
 			else if ("fullname".equals(child.getNodeName()))
 				taxon.setFriendlyName(child.getTextContent());
 			else if ("footprint".equals(child.getNodeName()))
 				taxon.setFootprint(child.getTextContent().split(","));
 			else if (TaxonStatus.ROOT_TAG.equals(child.getNodeName()))
 				taxon.setTaxonStatus(TaxonStatus.fromXML((NativeElement)child));
 		}
 		
 		return taxon;
 	}
 
 	public String toXML() {
 		// TRY TO GET THE PARENT NAME FROM THE SOURCE, IF APPLICABLE
 		StringBuilder xml = new StringBuilder();
 		xml.append("<" + ROOT_TAG + " id=\"" + getId() + 
 			"\" level=\"" + getLevel() + 
 			"\" sequence=\"" + getSequence() + 
 			"\" hybrid=\"" + getHybrid() +
 			"\" invasive=\"" + getInvasive() +
 			"\" feral=\"" + getFeral() +
 			"\">");
 		
 		xml.append(XMLWritingUtils.writeCDATATag("name", getName()));
 		xml.append(XMLWritingUtils.writeCDATATag("fullname", getFullName()));
 		
 		if (getInternalID() != null)
 			xml.append(XMLWritingUtils.writeTag("internalID", getInternalID().toString()));
 	
 		if (getTaxonStatus() != null)
 			xml.append(getTaxonStatus().toXML());
 		
 		if (getTaxonomicNotes() != null)
 			xml.append(getTaxonomicNotes().toXML());
 	
 		if (getParent() != null) {
 			xml.append(getParent().toParentXML());
 		}
 		if (getChildren() != null) {
 			for (Taxon child : getChildren())
 				xml.append(child.toChildXML());
 		}
 	
 		xml.append("<footprint>" + getFootprintCSV() + "</footprint>");
 		if (getTaxonomicAuthority() != null)
 			xml.append("<taxonomicAuthority><![CDATA[" + getTaxonomicAuthority() + "]]></taxonomicAuthority>");
 	
 		if (getEdits() != null) {
 			for (Edit edit : getEdits())
 				xml.append(edit.toXML());
 		}
 	
 		if (getNotes() != null) {
 			for (Notes note : getNotes())
 				xml.append(note.toXML());
 		}
 	
 		if (getReference() != null) {
 			for (Reference note : getReference())
 				xml.append("\r\n" + note.toXML());
 		}
 	
 		if (getSynonyms() != null) {
 			for (Synonym note : getSynonyms())
 				xml.append(note.toXML());
 		}
 	
 		if (getCommonNames() != null) {
 			for (CommonName note : getCommonNames())
 				xml.append(note.toXML());
 		}
 	
 		if (getInfratype() != null) {
 			xml.append(getInfratype().toXML());
 		}
 		
 		if (getImages() != null)
 			for (TaxonImage image : getImages())
 				xml.append(image.toXML());
 	
 		xml.append("</" + ROOT_TAG + ">");
 		generatedXML = xml.toString();
 		return xml.toString();
 	}
 	
 	public String getGeneratedXML() {
 		return generatedXML;
 	}
 
 	/**
 	 * Returns a synonym with all of the appropriate fields set, based on the
 	 * taxon argument.
 	 * 
 	 * @param taxon
 	 *            - used to generate the synonym
 	 * @return a synonym
 	 */
 	public static Synonym synonymizeTaxon(Taxon taxon) {
 		return new Synonym(taxon);
 	}
 
 	
 
 	public static Taxon createNode(long id, String name, int level, boolean hybrid) {
 	
 		Taxon taxon = new Taxon();
 		if (id > 0)
 			taxon.setId((int) id);
 		taxon.setName(name);
 		taxon.setTaxonLevel(TaxonLevel.getTaxonLevel(level));
 		taxon.setHybrid(hybrid);
 		return taxon;
 	
 	}
 	
 	/**
 	 * Method should only be called from toParentXML and toChildXML
 	 * 
 	 * @param taxon
 	 *            -- the taxon to be searlized
 	 * @param tagName
 	 *            -- either "parent", "child"
 	 * @return
 	 */
 	String toRelatedXML(String tagName) {
 		return toRelatedXML(tagName, false);
 	}
 	
 	String toRelatedXML(String tagName, boolean showFootprint) {
 		if (tagName.equals("parent") || tagName.equalsIgnoreCase("child") || tagName.equals(Taxon.ROOT_TAG)) {
 			StringBuilder minimal = new StringBuilder();
			minimal.append("<" + tagName + " id=\"" + getId() +
					"\" level=\"" + getLevel() +	
					"\">");
 			minimal.append(XMLWritingUtils.writeCDATATag("name", getName()));
 			minimal.append(XMLWritingUtils.writeCDATATag("fullname", getFriendlyName()));
 			if (showFootprint)
 				minimal.append(XMLWritingUtils.writeCDATATag("footprint", getFootprintCSV()));
 			minimal.append(getTaxonStatus().toXML());
 			minimal.append("</" + tagName + ">");
 			
 			return minimal.toString();
 		}
 		return null;
 	}
 
 	public String toParentXML() {
 		return toRelatedXML("parent");
 	}
 
 	public String toChildXML() {
 		return toRelatedXML("child");
 	}
 	
 	public static Taxon fromXML(NativeDocument ndoc) {
 		return fromXML(ndoc.getDocumentElement());
 	}
 	
 	public static Taxon fromXML(NativeElement nodeElement) {
 		long id = LongUtils.safeParseLong(nodeElement.getAttribute("id"));
 		int level = Integer.parseInt(nodeElement.getAttribute("level"));
 		int sequence;
 		try {
 			sequence = Integer.parseInt(nodeElement.getAttribute("sequence"));
 		} catch (Exception e) {
 			sequence = 0;
 		}
 		boolean hybrid = (nodeElement.getAttribute("hybrid") != null && nodeElement.getAttribute("hybrid").equals(
 				"true"));
 		
 		Taxon taxon = Taxon.createNode(id, "unknown", level, hybrid);
 		taxon.setSequence(sequence);
 		
 		NativeNodeList name = nodeElement.getElementsByTagName("name");
 		if (name.getLength() > 0) {
 			taxon.setName(name.elementAt(0).getTextContent());
 		}
 		
 		NativeNodeList fullname = nodeElement.getElementsByTagName("fullname");
 		if (fullname.getLength() > 0) {
 			taxon.setFriendlyName(fullname.elementAt(0).getTextContent());
 		}
 		
 		taxon.setFeral("true".equals(nodeElement.getAttribute("feral")));
 		taxon.setInvasive("true".equals(nodeElement.getAttribute("invasive")));
 		
 		NativeNodeList status = nodeElement.getElementsByTagName(TaxonStatus.ROOT_TAG);
 		if (status.getLength() > 0)
 			taxon.setTaxonStatus(TaxonStatus.fromXML(status.elementAt(0)));
 
 		NativeNodeList parent = nodeElement.getElementsByTagName("parent");
 		if (parent.getLength() > 0) {
 			taxon.setParent(Taxon.fromXMLminimal(parent.elementAt(0)));
 		}
 
 		NativeNodeList children = nodeElement.getElementsByTagName("child");
 		if (children.getLength() > 0) {
 			taxon.setChildren(new HashSet<Taxon>());
 			for (int i = 0; i < children.getLength(); i++)
 				taxon.getChildren().add(Taxon.fromXMLminimal(children.elementAt(i)));
 		}
 
 		NativeNodeList list = nodeElement.getElementsByTagName(Infratype.ROOT_NAME);
 		if (list.getLength() > 0)
 			taxon.setInfratype(Infratype.fromXML(list.elementAt(0), taxon));
 
 		NativeNodeList footprint = nodeElement.getElementsByTagName("footprint");
 		if (footprint.getLength() > 0) {
 			taxon.setFootprint(footprint.elementAt(0).getTextContent().split(","));
 		}
 		
 		NativeNodeList internalID = nodeElement.getElementsByTagName("internalID");
 		if (internalID.getLength() > 0)
 			taxon.setInternalID(Integer.valueOf(internalID.elementAt(0).getTextContent()));
 
 		NativeNodeList taxAuths = nodeElement.getElementsByTagName("taxonomicAuthority");
 		if (taxAuths.getLength() > 0)
 			taxon.setTaxonomicAuthority(XMLUtils.clean(taxAuths.elementAt(0).getTextContent()));
 		
 		NativeNodeList taxNotes = nodeElement.getElementsByTagName(CanonicalNames.TaxonomicNotes);
 		if (taxNotes.getLength() > 0)
 			taxon.setTaxonomicNotes(Field.fromXML(taxNotes.elementAt(0)));
 
 		NativeNodeList names = nodeElement.getElementsByTagName(CommonName.ROOT_TAG);
 		taxon.setCommonNames(new HashSet<CommonName>());
 		for (int i = 0; i < names.getLength(); i++) {
 			CommonName commonName = CommonName.fromXML(names.elementAt(i));
 			commonName.setTaxon(taxon);
 			taxon.getCommonNames().add(commonName);
 		}
 		
 		NativeNodeList notes = nodeElement.getElementsByTagName(Notes.ROOT_TAG);
 		taxon.setNotes(new HashSet<Notes>());
 		for (int i = 0; i < notes.getLength(); i++) {
 			Notes note = Notes.fromXML(notes.elementAt(i));
 			note.setTaxon(taxon);
 			taxon.getNotes().add(note);
 		}
 
 		NativeNodeList edits = nodeElement.getElementsByTagName(Edit.ROOT_TAG);
 		taxon.setEdits(new HashSet<Edit>());
 		for (int i = 0; i < edits.getLength(); i++) {
 			Edit edit = Edit.fromXML(edits.elementAt(i));
 			edit.getTaxon().add(taxon);
 			taxon.getEdits().add(edit);
 		}
 
 		NativeNodeList synonyms = nodeElement.getElementsByTagName(Synonym.ROOT_TAG);
 		taxon.setSynonyms(new HashSet<Synonym>());
 		for (int i = 0; i < synonyms.getLength(); i++) {
 			
 			taxon.getSynonyms().add(Synonym.fromXML(synonyms.elementAt(i), taxon));
 		}
 
 		NativeNodeList assessments = nodeElement.getElementsByTagName(Assessment.ROOT_TAG);
 		taxon.setAssessments(new HashSet<Assessment>());
 		for (int i = 0; i < assessments.getLength(); i++) {
 			Assessment assessment = Assessment.fromXML(assessments.elementAt(i));
 			taxon.getAssessments().add(assessment);
 			assessment.setTaxon(taxon);
 		}
 
 		NativeNodeList references = nodeElement.getElementsByTagName(Reference.ROOT_TAG);
 		taxon.setReference(new HashSet<Reference>());
 		for (int i = 0; i < references.getLength(); i++) {
 			Reference reference = Reference.fromXML(references.elementAt(i));
 			reference.getTaxon().add(taxon);
 			taxon.getReference().add(reference);
 		}
 		
 		NativeNodeList images = nodeElement.getElementsByTagName(TaxonImage.ROOT_TAG);
 		taxon.setImages(new HashSet<TaxonImage>());
 		for (int i = 0; i < images.getLength(); i++) {
 			TaxonImage image = TaxonImage.fromXML(images.elementAt(i));
 			image.setTaxon(taxon);
 			taxon.getImages().add(image);
 		}
 		
 		return taxon;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + id;
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
 		Taxon other = (Taxon) obj;
 		if (id != other.id)
 			return false;
 		return true;
 	}
 	
 }
