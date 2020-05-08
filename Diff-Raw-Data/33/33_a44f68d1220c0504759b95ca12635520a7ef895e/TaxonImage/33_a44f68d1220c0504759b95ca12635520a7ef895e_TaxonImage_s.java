 package org.iucn.sis.shared.api.models;
 
 import java.util.Date;
 
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNode;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.portable.XMLWritingUtils;
 
 public class TaxonImage {
 	
 	public static final String ROOT_TAG = "image";
 	public static final String IMG_JPEG = "image/jpeg";
 	public static final String IMG_GIF = "image/gif";
 	public static final String IMG_TIFF = "image/tiff";
 	public static final String IMG_PNG = "image/png";
 	public static final String IMG_BMP = "image/bmp";
 	
 	private int id;
 	
 	private String encoding;
 	
 	private String identifier;
 	
 	private boolean primary;
 	
 	private Float rating;
 	
 	private int weight;
 	
 	private String caption;
 	
 	private String credit;
 	
 	private String source;
 	
 	private boolean showRedList;
 	
 	private boolean showSIS;
 	
 	private Taxon taxon;
 	
 	private String generationID;
 	
 	public TaxonImage() {
 		generationID = new Date().getTime() + "";
 	}
 
 	public String getEncoding() {
 		return encoding;
 	}
 
 	public void setEncoding(String encoding) {
 		this.encoding = encoding;
 	}
 	
 	public int getId() {
 		return id;
 	}
 	
 	public void setId(int id) {
 		this.id = id;
 		if (id > 0)
 			this.generationID = id + "";
 	}
 
 	public String getIdentifier() {
 		return identifier;
 	}
 
 	public void setIdentifier(String identifier) {
 		this.identifier = identifier;
 		this.generationID += identifier;
 	}
 
 	public boolean getPrimary() {
 		return primary;
 	}
 
 	public void setPrimary(boolean primary) {
 		this.primary = primary;
 	}
 
 	public Float getRating() {
 		return rating;
 	}
 	
 	public Float getRating(Float nullPolicy) {
 		return rating == null ? nullPolicy : rating;
 	}
 
 	public void setRating(Float rating) {
 		this.rating = rating;
 	}
 
 	public int getWeight() {
 		return weight;
 	}
 
 	public void setWeight(int weight) {
 		this.weight = weight;
 	}
 
 	public String getCaption() {
 		return caption;
 	}
 
 	public void setCaption(String caption) {
 		this.caption = caption;
 	}
 
 	public String getCredit() {
 		return credit;
 	}
 
 	public void setCredit(String credit) {
 		this.credit = credit;
 	}
 
 	public String getSource() {
 		return source;
 	}
 
 	public void setSource(String source) {
 		this.source = source;
 	}
 
 	public boolean getShowRedList() {
 		return showRedList;
 	}
 
 	public void setShowRedList(boolean showRedList) {
 		this.showRedList = showRedList;
 	}
 
 	public boolean getShowSIS() {
 		return showSIS;
 	}
 
 	public void setShowSIS(boolean showSIS) {
 		this.showSIS = showSIS;
 	}
 	
 	public void setTaxon(Taxon taxon) {
 		this.taxon = taxon;
 	}
 	
 	public Taxon getTaxon() {
 		return taxon;
 	}
 
 	public String getGenerationID() {
 		return generationID;
 	}
 
 	public void setGenerationID(String generationID) {
 		this.generationID = generationID;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((generationID == null) ? 0 : generationID.hashCode());
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
 		TaxonImage other = (TaxonImage) obj;
 		if (generationID == null) {
 			if (other.generationID != null)
 				return false;
 		} else if (!generationID.equals(other.generationID))
 			return false;
 		return true;
 	}
 	
 	public String getFileName() {
 		return identifier + getExtensionFromEncoding(encoding);
 	}
 	
 	public String toXML() {
 		StringBuilder out = new StringBuilder();
 		out.append("<" + ROOT_TAG + " id=\"" + getId() + "\">");
 		out.append(XMLWritingUtils.writeCDATATag("caption", getCaption()));
 		out.append(XMLWritingUtils.writeCDATATag("credit", getCredit()));
 		out.append(XMLWritingUtils.writeCDATATag("source", getSource()));
 		out.append(XMLWritingUtils.writeTag("encoding", getEncoding()));
 		out.append(XMLWritingUtils.writeTag("identifier", getIdentifier()));
 		out.append(XMLWritingUtils.writeTag("primary", Boolean.toString(getPrimary())));
 		out.append(XMLWritingUtils.writeTag("rating", getRating(0.0F) + ""));
 		out.append(XMLWritingUtils.writeTag("weight", getWeight() + ""));
 		out.append(XMLWritingUtils.writeTag("showredlist", Boolean.toString(getShowRedList())));
 		out.append(XMLWritingUtils.writeTag("showsis", Boolean.toString(getShowSIS())));
		out.append(getTaxon().toXMLMinimal());
 		out.append("</" + ROOT_TAG + ">");
 		
 		return out.toString();
 	}
 	
 	public static final TaxonImage fromXML(NativeElement el) {
 		TaxonImage image = new TaxonImage();
 		image.setId(Integer.valueOf(el.getAttribute("id")));
 		
 		NativeNodeList nodes = el.getChildNodes();
 		for (int i = 0; i < nodes.getLength(); i++) {
 			NativeNode current = nodes.item(i);
 			if ("caption".equals(current.getNodeName()))
 				image.setCaption(current.getTextContent());
 			else if ("credit".equals(current.getNodeName()))
 				image.setCredit(current.getTextContent());
 			else if ("source".equals(current.getNodeName()))
 				image.setSource(current.getTextContent());
 			else if("encoding".equals(current.getNodeName()))
 				image.setEncoding(current.getTextContent());
 			else if ("identifier".equals(current.getNodeName()))
 				image.setIdentifier(current.getTextContent());
 			else if ("primary".equals(current.getNodeName()))
 				image.setPrimary("true".equals(current.getTextContent()));
 			else if ("rating".equals(current.getNodeName())) {
 				try {
 					image.setRating(Float.valueOf(current.getTextContent()));
 				} catch (Exception e) {
 					
 				}
 			}
 			else if ("weight".equals(current.getNodeName())) {
 				try {
 					image.setWeight(Integer.valueOf(current.getTextContent()));
 				} catch (Exception e) {
 					
 				}
 			}
 			else if ("showredlist".equals(current.getNodeName()))
 				image.setShowRedList("true".equals(current.getTextContent()));
 			else if ("showsis".equals(current.getNodeName()))
 				image.setShowSIS("true".equals(current.getTextContent()));
			else if (Taxon.ROOT_TAG.equals(current.getNodeName()))
 				image.setTaxon(Taxon.fromXMLminimal((NativeElement)current));
 		}
 		
 		return image;
 	}
 	
 	public static String getExtensionFromEncoding(String encoding) {
 		if (encoding.equals(IMG_JPEG))
 			return ".jpg";
 		if (encoding.equals(IMG_GIF))
 			return ".gif";
 		if (encoding.equals(IMG_TIFF))
 			return ".tiff";
 		if (encoding.equals(IMG_PNG))
 			return ".png";
 		if (encoding.equals(IMG_BMP))
 			return ".bmp";
 		return null;
 	}
 	
 }
