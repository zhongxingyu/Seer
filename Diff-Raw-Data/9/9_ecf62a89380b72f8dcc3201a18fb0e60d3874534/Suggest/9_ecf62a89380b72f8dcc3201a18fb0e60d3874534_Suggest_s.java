 package com.jcertif.web.model;
 
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 /**
  * Bo {@link Suggest}.
  * 
  * @author rossi.oddet
  * 
  */
 @XmlRootElement
 public class Suggest extends AbstractBO {
 
 	private static final long serialVersionUID = 1L;
 
 	private Long id;
 	private String title;
 	private String description;
 	private String summary;
 	private String needs;
 	private String keyword;
 	private Long conferenceId;
 	private String codeStatut;
 	private List<Long> sujetIds;
 	private List<Long> participantIds;
 
 	/**
 	 * @return the id
 	 */
 	public Long getId() {
 		return id;
 	}
 
 	/**
 	 * @param id
 	 *            the id to set
 	 */
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * @param title
 	 *            the title to set
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @param description
 	 *            the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * @return the summary
 	 */
 	public String getSummary() {
 		return summary;
 	}
 
 	/**
 	 * @param summary
 	 *            the summary to set
 	 */
 	public void setSummary(String summary) {
 		this.summary = summary;
 	}
 
 	/**
 	 * @return the needs
 	 */
 	public String getNeeds() {
 		return needs;
 	}
 
 	/**
 	 * @param needs
 	 *            the needs to set
 	 */
 	public void setNeeds(String needs) {
 		this.needs = needs;
 	}
 
 	/**
 	 * @return the keyword
 	 */
 	public String getKeyword() {
 		return keyword;
 	}
 
 	/**
 	 * @param keyword
 	 *            the keyword to set
 	 */
 	public void setKeyword(String keyword) {
 		this.keyword = keyword;
 	}
 
 	/**
 	 * @return the conferenceId
 	 */
 	public Long getConferenceId() {
 		return conferenceId;
 	}
 
 	/**
 	 * @param conferenceId
 	 *            the conferenceId to set
 	 */
 	public void setConferenceId(Long conferenceId) {
 		this.conferenceId = conferenceId;
 	}
 
 	/**
 	 * @return the codeStatut
 	 */
 	public String getCodeStatut() {
 		return codeStatut;
 	}
 
 	/**
 	 * @param codeStatut
 	 *            the codeStatut to set
 	 */
 	public void setCodeStatut(String codeStatut) {
 		this.codeStatut = codeStatut;
 	}
 
 	/**
 	 * @return the sujetIds
 	 */
 	public List<Long> getSujetIds() {
 		return sujetIds;
 	}
 
 	/**
 	 * @param sujetIds
 	 *            the sujetIds to set
 	 */
 	public void setSujetIds(List<Long> sujetIds) {
 		this.sujetIds = sujetIds;
 	}
 
 	/**
 	 * @return the participantIds
 	 */
 	public List<Long> getParticipantIds() {
 		return participantIds;
 	}
 
 	/**
 	 * @param participantIds
 	 *            the participantIds to set
 	 */
 	public void setParticipantIds(List<Long> participantIds) {
 		this.participantIds = participantIds;
 	}
 
 	/**
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return new HashCodeBuilder().append(title).toHashCode();
 	}
 
 	/**
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 
 		if (this == obj) {
 			return true;
 		}
 
 		if (!(obj instanceof Suggest)) {
 			return false;
 		}
 
 		final Suggest other = (Suggest) obj;
 
 		return new EqualsBuilder().append(title, other.getTitle())
 				.append(description, other.getDescription()).isEquals();
 	}
 
 }
