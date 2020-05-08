 /**
  * 
  */
 package org.jenkins.plugins.dbaudit.internal.model;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
import javax.persistence.Transient;
 
 import org.jenkins.plugins.dbaudit.model.BuildParameter;
 
 /**
  * @author Marco Scata
  *
  */
 @Entity(name="JENKINS_BUILD_PARAMS")
 public class BuildParameterImpl implements BuildParameter {
 	private Long id;
 	private String name;
 	private String value;
 	private String buildId;
 	
 	public BuildParameterImpl() {
 		super();
 	}
 	
 	public BuildParameterImpl(final Long id, final String name, final String value, final String buildId) {
 		this.id = id;
 		this.name = name;
 		this.value = value;
 		this.buildId = buildId;
 	}
 	
 	/**
 	 * @see org.jenkins.plugins.dbaudit.model.BuildParameter#getId()
 	 */
 	@Id
 	@Column(nullable=false, unique=true)
 	@Override
 	public Long getId() {
 		return id;
 	}
 
 	/**
 	 * @see org.jenkins.plugins.dbaudit.model.BuildParameter#setId(java.lang.Long)
 	 */
 	@Override
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	/**
 	 * @see org.jenkins.plugins.dbaudit.model.BuildParameter#getName()
 	 */
 	@Column(nullable=false, unique=false)
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @see org.jenkins.plugins.dbaudit.model.BuildParameter#setName(java.lang.String)
 	 */
 	@Override
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @see org.jenkins.plugins.dbaudit.model.BuildParameter#getValue()
 	 */
 	@Column(nullable=true, unique=false)
 	@Override
 	public String getValue() {
 		return value;
 	}
 
 	/**
 	 * @see org.jenkins.plugins.dbaudit.model.BuildParameter#setValue(java.lang.String)
 	 */
 	@Override
 	public void setValue(String value) {
 		this.value = value;
 	}
 
 	/**
 	 * @see org.jenkins.plugins.dbaudit.model.BuildParameter#getBuildId()
 	 */
 	@Column(nullable=false, unique=false)
 	@Override
 	public String getBuildId() {
 		return buildId;
 	}
 
 	/**
 	 * @see org.jenkins.plugins.dbaudit.model.BuildParameter#setBuildId(java.lang.String)
 	 */
 	@Override
 	public void setBuildId(String buildId) {
 		this.buildId = buildId;
 	}
 
 	/**
 	 * Used to decide equality.
 	 * 
 	 * @return the internal id.
 	 */
	@Transient
 	private String getInternalId() {
 		return String.format("%s/%s", 
 				this.buildId, this.name);
 	}
 	
 	@Override
 	public String toString() {
 		return String.format("%s=%s [%s]", 
 				getInternalId(), this.value, this.id);
 	}
 	
 	@Override
 	public int hashCode() {
 		return getInternalId().hashCode();
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		// fail-fast logic
 		if (null == obj) { return false; }
 		if (!(obj instanceof BuildParameter)) { return false; }
 		
 		final BuildParameter other = (BuildParameter) obj;
 		
 		return other.hashCode() == this.hashCode();
 	}
 }
