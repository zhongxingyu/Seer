 package org.fao.fi.vme.domain.model;
 
 import java.net.URL;
 import java.util.Date;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.OneToOne;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.fao.fi.vme.domain.support.MultiLingualStringConverter;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.RSGReferenceReport;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.fields.RSGConverter;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.fields.RSGIdentifier;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.fields.RSGInstructions;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.fields.RSGName;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.fields.RSGSimpleReference;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.fields.RSGWeight;
 import org.gcube.application.rsg.support.compiler.bridge.converters.impl.DateDataConverter;
 import org.gcube.application.rsg.support.compiler.bridge.converters.impl.IntegerDataConverter;
 import org.gcube.application.rsg.support.compiler.bridge.converters.impl.LongDataConverter;
 import org.gcube.application.rsg.support.compiler.bridge.converters.impl.URLDataConverter;
 import org.gcube.application.rsg.support.compiler.bridge.interfaces.ReferenceReport;
 
 /**
  * 
  * The source of information available on the level of an RFMO.
  * 
  * 
  * @author Erik van Ingen
  * 
  */
 @RSGReferenceReport(name = "VME Information Source")
 @Entity(name = "INFORMATION_SOURCE")
 public class InformationSource implements ReferenceReport {
 
 	@RSGIdentifier
 	@RSGConverter(LongDataConverter.class)
 	@Id
 	private Long id;
 
 	/**
 	 * 
 	 */
 
 	@OneToOne(cascade = CascadeType.REFRESH)
 	@RSGName("Authority")
 	@RSGWeight(0)
 	@RSGSimpleReference
 	private Rfmo rfmo;
 
 	/**
 	 * InformationSource has 0,1 SpecificMeasure
 	 */
 	@OneToOne(cascade = CascadeType.ALL)
 	private SpecificMeasure specificMeasure;
 
 	/**
 	 * InformationSource has 0,1 GeneralMeasure
 	 */
 	@OneToOne
 	private GeneralMeasure generalMeasure;
 
 	/**
 	 * Also referred to as issue date of biblio entry
 	 * 
 	 * TODO change into publicationYear, type int
 	 */
 
 	@RSGName("Publication Year")
 	@RSGConverter(IntegerDataConverter.class)
 	@RSGWeight(1)
 	private Integer publicationYear;
 
 	@RSGName("Meeting Start Date")
 	@RSGInstructions("Use the YYYY/MM/DD format")
 	@RSGConverter(DateDataConverter.class)
 	@RSGWeight(1)
 	@Temporal(TemporalType.DATE)
 	private Date meetingStartDate;
 
 	@RSGName("Meeting End Date")
 	@RSGInstructions("Use the YYYY/MM/DD format")
 	@RSGConverter(DateDataConverter.class)
 	@RSGWeight(1)
 	@Temporal(TemporalType.DATE)
 	private Date meetingEndDate;
 
 	/** */
 	@RSGName("Committee")
 	@RSGConverter(MultiLingualStringConverter.class)
 	@RSGWeight(1)
 	@OneToOne(cascade = { CascadeType.ALL })
 	private MultiLingualString committee;
 
 	/** */
 	@RSGName("Report Summary")
 	@RSGConverter(MultiLingualStringConverter.class)
 	@RSGWeight(1)
 	@OneToOne(cascade = { CascadeType.ALL })
 	private MultiLingualString reportSummary;
 
 	/**
 	 * The url where the document is to be found
 	 */
 	@RSGName("URL")
 	@RSGConverter(URLDataConverter.class)
 	@RSGWeight(1)
 	private URL url;
 
 	/**
 	 * The title
 	 */
 	@RSGName("Citation")
 	@RSGConverter(MultiLingualStringConverter.class)
 	@RSGWeight(0)
 	@OneToOne(cascade = { CascadeType.ALL })
 	private MultiLingualString citation;
 
 	/**
 	 * This field maybe used to indicate what type of source this is. One type
 	 * would be link CEM Source.
 	 * 
 	 * <option value="1">Book </option>
 	 * 
 	 * <option value="3">Journal </option>
 	 * 
 	 * <option value="4">Project </option>
 	 * 
 	 * <option value="2">Meeting documents</option>
 	 * 
 	 * <option value="6">CD-ROM/DVD</option> <option VALUE="-1">Other </option>
 	 * 
 	 */
 	private Integer sourceType;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public Rfmo getRfmo() {
 		return rfmo;
 	}
 
 	public void setRfmo(Rfmo rfmo) {
 		this.rfmo = rfmo;
 	}
 
 	public SpecificMeasure getSpecificMeasure() {
 		return specificMeasure;
 	}
 
 	public void setSpecificMeasure(SpecificMeasure specificMeasure) {
 		this.specificMeasure = specificMeasure;
 	}
 
 	public GeneralMeasure getGeneralMeasure() {
 		return generalMeasure;
 	}
 
 	public void setGeneralMeasure(GeneralMeasure generalMeasure) {
 		this.generalMeasure = generalMeasure;
 	}
 
 	public Date getMeetingStartDate() {
 		return meetingStartDate;
 	}
 
 	public void setMeetingStartDate(Date meetingStartDate) {
 		this.meetingStartDate = meetingStartDate;
 	}
 
 	public Date getMeetingEndDate() {
 		return meetingEndDate;
 	}
 
 	public void setMeetingEndDate(Date meetingEndDate) {
 		this.meetingEndDate = meetingEndDate;
 	}
 
 	public MultiLingualString getCommittee() {
 		return committee;
 	}
 
 	public void setCommittee(MultiLingualString committee) {
 		this.committee = committee;
 	}
 
 	public MultiLingualString getReportSummary() {
 		return reportSummary;
 	}
 
 	public void setReportSummary(MultiLingualString reportSummary) {
 		this.reportSummary = reportSummary;
 	}
 
 	public URL getUrl() {
 		return url;
 	}
 
 	public void setUrl(URL url) {
 		this.url = url;
 	}
 
 	public MultiLingualString getCitation() {
 		return citation;
 	}
 
 	public void setCitation(MultiLingualString citation) {
 		this.citation = citation;
 	}
 
 	public Integer getSourceType() {
 		return sourceType;
 	}
 
 	public void setSourceType(int sourceType) {
 		this.sourceType = sourceType;
 	}
 
 	public Integer getPublicationYear() {
 		return publicationYear;
 	}
 
 	public void setPublicationYear(Integer publicationYear) {
 		this.publicationYear = publicationYear;
 	}
 
 	// /* (non-Javadoc)
 	// * @see java.lang.Object#hashCode()
 	// */
 	// @Override
 	// public int hashCode() {
 	// final int prime = 31;
 	// int result = 1;
 	// result = prime * result + ((this.citation == null) ? 0 :
 	// this.citation.hashCode());
 	// result = prime * result + ((this.committee == null) ? 0 :
 	// this.committee.hashCode());
 	// result = prime * result + ((this.generalMeasure == null) ? 0 :
 	// this.generalMeasure.hashCode());
 	// result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
 	// result = prime * result + ((this.meetingEndDate == null) ? 0 :
 	// this.meetingEndDate.hashCode());
 	// result = prime * result + ((this.meetingStartDate == null) ? 0 :
 	// this.meetingStartDate.hashCode());
 	// result = prime * result + ((this.publicationYear == null) ? 0 :
 	// this.publicationYear.hashCode());
 	// result = prime * result + ((this.reportSummary == null) ? 0 :
 	// this.reportSummary.hashCode());
 	// result = prime * result + ((this.rfmoList == null) ? 0 :
 	// this.rfmoList.hashCode());
 	// result = prime * result + ((this.sourceType == null) ? 0 :
 	// this.sourceType.hashCode());
 	// result = prime * result + ((this.specificMeasure == null) ? 0 :
 	// this.specificMeasure.hashCode());
 	// result = prime * result + ((this.url == null) ? 0 : this.url.hashCode());
 	// return result;
 	// }
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		InformationSource other = (InformationSource) obj;
 		if (this.citation == null) {
 			if (other.citation != null)
 				return false;
 		} else if (!this.citation.equals(other.citation))
 			return false;
 		if (this.committee == null) {
 			if (other.committee != null)
 				return false;
 		} else if (!this.committee.equals(other.committee))
 			return false;
 		if (this.generalMeasure == null) {
 			if (other.generalMeasure != null)
 				return false;
 		} else if (!this.generalMeasure.equals(other.generalMeasure))
 			return false;
 		if (this.id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!this.id.equals(other.id))
 			return false;
 		if (this.meetingEndDate == null) {
 			if (other.meetingEndDate != null)
 				return false;
 		} else if (!this.meetingEndDate.equals(other.meetingEndDate))
 			return false;
 		if (this.meetingStartDate == null) {
 			if (other.meetingStartDate != null)
 				return false;
 		} else if (!this.meetingStartDate.equals(other.meetingStartDate))
 			return false;
 		if (this.publicationYear == null) {
 			if (other.publicationYear != null)
 				return false;
 		} else if (!this.publicationYear.equals(other.publicationYear))
 			return false;
 		if (this.reportSummary == null) {
 			if (other.reportSummary != null)
 				return false;
 		} else if (!this.reportSummary.equals(other.reportSummary))
 			return false;
 		if (this.rfmo == null) {
 			if (other.rfmo != null)
 				return false;
 		} else if (!this.rfmo.equals(other.rfmo))
 			return false;
 		if (this.sourceType == null) {
 			if (other.sourceType != null)
 				return false;
 		} else if (!this.sourceType.equals(other.sourceType))
 			return false;
 		if (this.specificMeasure == null) {
 			if (other.specificMeasure != null)
 				return false;
 		} else if (!this.specificMeasure.equals(other.specificMeasure))
 			return false;
 		if (this.url == null) {
 			if (other.url != null)
 				return false;
 		} else if (!this.url.equals(other.url))
 			return false;
 		return true;
 	}
	
	
	equalsObjective1
 }
