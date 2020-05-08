 package org.virtualrepository.sdmx;
 
 import static org.virtualrepository.Utils.*;
 
 import java.net.URI;
 
 import org.virtualrepository.Asset;
 import org.virtualrepository.Property;
 import org.virtualrepository.RepositoryService;
 import org.virtualrepository.impl.AbstractAsset;
 import org.virtualrepository.impl.Type;
 
 /**
  * Partial implementation of an {@link Asset} available in the SDMX format.
  * 
  * @author Fabio Simeoni
  * 
  */
 public abstract class SdmxAsset extends AbstractAsset {
 
 	/**
 	 * The generic type of {@link SdmxAsset}s.
 	 */
 	public static final Type<SdmxAsset> type = new SdmxGenericType();
 	
 	private String version;
 	
 	private String remoteId;
 	
 	private String agency;
 	
 	private URI uri;
 	
 	private String status;
 	
 	
 	/**
 	 * Creates an instance with a given type, urn, identifier, version, name, and properties.
 	 * <p>
 	 * Inherit as a plugin-facing constructor for asset discovery and retrieval purposes.
 	 * 
 	 * @param type the type
 	 * @param urn the urn
 	 * @param id the identifier
 	 * @param version the version
 	 * @param name the name
 	 * @param properties the properties
 	 */
 	protected <T extends SdmxAsset> SdmxAsset(Type<T> type,String urn, String id, String version, String name, Property ... properties) {
 		
 		super(type,urn,name,properties);
 		
 		notNull("identifier",id);
 		this.remoteId=id;
 
 		
 		
 	}
 	
 	/**
 	 * Creates an instance with a given type, name, and target service.
 	 * <p>
 	 * Inherit as a client-facing constructor for asset publication with services that do now allow client-defined
 	 * identifiers, or else that force services to generate identifiers.
 	 * 
 	 * 
 	 * @param type the type
 	 * @param name the name
 	 * @param service the service
 	 */
 	protected <T extends SdmxAsset> SdmxAsset(Type<T> type, String name, RepositoryService service, Property ... properties) {
 		super(type,name,service,properties);
 	}
 	
 	
 	/**
 	 * Returns the identifier of this asset's agency.
 	 * 
 	 * @return the agency
 	 */
 	public String agency() {
 
 		return agency;
 
 	}
 
 	/**
 	 * Sets the identifier of this asset's agency.
 	 * 
 	 * @param agency the agency identifier
 	 */
 	public void setAgency(String agency) {
 
 		notNull("agency",agency);
 
 		this.agency=agency;
 	}
 	
 	/**
 	 * Returns the URI of this asset.
 	 * 
 	 * @return the URI
 	 */
 	public URI uri() {
 
 		return uri;
 
 	}
 	
 	/**
 	 * Sets the URI of this asset.
 	 * 
 	 * @param uri the URI
 	 */
 	public void setURI(URI uri) {
 
 		notNull("uri",uri);
 
 		this.uri = uri;
 	}
 	
 	/**
 	 * Returns the version of this asset.
 	 * 
 	 * @return the version
 	 */
 	public String version() {
 
 		return version;
 
 	}
 	
 	/**
 	 * Returns the remote identifier of this asset.
 	 * 
 	 * @return the identifier
 	 */
 	public String remoteId() {
 
 		return remoteId;
 
 	}
 
 	
 	/**
 	 * Returns the status of this asset.
 	 * 
 	 * @return the status
 	 */
 	public String status() {
 
 		return status;
 
 	}
 	
 	
 	/**
 	 * Sets the status of this asset.
 	 * 
 	 * @param status the status
 	 */
 	public void setStatus(String status) {
 
 		notNull("status",status);
 		this.status=status;;
 
 	}
 	
 	
 	@Override
 	public String toString() {
 		return getClass().getSimpleName()+" [agency()=" + agency() + ", uri()=" + uri() + ", version()=" + version() + ", remoteId()="
 				+ remoteId() + ", status()=" + status() + ", id()=" + id() + ", type()=" + type() + ", service()="
 				+ service() + ", name()=" + name() + ", properties()=" + properties() + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + ((agency == null) ? 0 : agency.hashCode());
 		result = prime * result + ((remoteId == null) ? 0 : remoteId.hashCode());
 		result = prime * result + ((status == null) ? 0 : status.hashCode());
 		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
 		result = prime * result + ((version == null) ? 0 : version.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		SdmxAsset other = (SdmxAsset) obj;
 		if (agency == null) {
 			if (other.agency != null)
 				return false;
 		} else if (!agency.equals(other.agency))
 			return false;
 		if (remoteId == null) {
 			if (other.remoteId != null)
 				return false;
 		} else if (!remoteId.equals(other.remoteId))
 			return false;
 		if (status == null) {
 			if (other.status != null)
 				return false;
 		} else if (!status.equals(other.status))
 			return false;
 		if (uri == null) {
 			if (other.uri != null)
 				return false;
 		} else if (!uri.equals(other.uri))
 			return false;
 		if (version == null) {
 			if (other.version != null)
 				return false;
 		} else if (!version.equals(other.version))
 			return false;
 		return true;
 	}
 
 }
