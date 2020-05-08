 package com.mjeanroy.springhub.models.entities;
 
 import com.mjeanroy.springhub.models.AbstractModel;
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 import org.apache.commons.lang3.builder.ToStringBuilder;
 import org.apache.commons.lang3.builder.ToStringStyle;
 
 import javax.persistence.MappedSuperclass;
 import javax.persistence.Version;
 
 /**
  * Generic JPA entity.
  */
 @MappedSuperclass
 public abstract class AbstractGenericEntity extends AbstractModel {
 
 	/** Version (lock optimistic) */
 	@Version
	protected long version;
 
 	/**
 	 * Get identifier of entity.
 	 *
 	 * @return Identifier of entity.
 	 */
 	public abstract Long entityId();
 
 	@Override
 	public Long modelId() {
 		return entityId();
 	}
 
 	/**
 	 * Get {@link #version}
 	 *
 	 * @return {@link #version}
 	 */
	public long getVersion() {
 		return version;
 	}
 
 	/**
 	 * Set {@link #version}
 	 *
 	 * @param version
 	 */
	public void setVersion(long version) {
 		this.version = version;
 	}
 
 	/**
 	 * Check if entity is new (will be inserted and not updated).
 	 *
 	 * @return True if entity is new, false otherwise.
 	 */
 	public boolean isNew() {
 		return entityId() == null;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (o == this) {
 			return true;
 		}
 
 		if (o instanceof AbstractGenericEntity) {
 			boolean inherits = o.getClass().isAssignableFrom(this.getClass())
 					|| this.getClass().isAssignableFrom(o.getClass());
 
 			if (inherits) {
 				AbstractGenericEntity e = (AbstractGenericEntity) o;
 				if ((entityId() == null) || (e.entityId() == null)) {
 					return false;
 				}
 				return new EqualsBuilder().append(entityId(), e.entityId()).isEquals();
 			}
 		}
 
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
 		return new HashCodeBuilder(17, 31).append(entityId()).toHashCode();
 	}
 
 	@Override
 	public String toString() {
 		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("entityId", entityId()).toString();
 	}
 }
