 package org.fao.fi.vme.domain.model;
 
 import java.util.Map;
 
import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 /**
  * Not applied yet.
  * 
  * See also
  * http://stackoverflow.com/questions/5964027/localizable-strings-in-jpa
  * 
  * 
  * http://hwellmann.blogspot.it/2010/07/jpa-20-querying-map.html
  * 
  * http://hwellmann.blogspot.it/2010/07/jpa-20-mapping-map.html
  * 
  * @author Erik van Ingen
  * 
  */
 @Entity(name = "MULTILINGUAL_STRING")
 public class MultiLingualString implements ObjectId {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private Long id;
 
 	/**
 	 * map of descriptions per language
 	 * 
 	 */
 
 	@ElementCollection
	@Column(columnDefinition = "CLOB")
 	private Map<Integer, String> stringMap;
 
 	@Override
 	public Long getId() {
 		return id;
 	}
 
 	@Override
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Map<Integer, String> getStringMap() {
 		return stringMap;
 	}
 
 	public void setStringMap(Map<Integer, String> stringMap) {
 		this.stringMap = stringMap;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
 		result = prime * result + ((this.stringMap == null) ? 0 : this.stringMap.hashCode());
 		return result;
 	}
 
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
 		MultiLingualString other = (MultiLingualString) obj;
 		if (this.id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!this.id.equals(other.id))
 			return false;
 		if (this.stringMap == null) {
 			if (other.stringMap != null)
 				return false;
 		} else if (!this.stringMap.equals(other.stringMap))
 			return false;
 		return true;
 	}
 }
