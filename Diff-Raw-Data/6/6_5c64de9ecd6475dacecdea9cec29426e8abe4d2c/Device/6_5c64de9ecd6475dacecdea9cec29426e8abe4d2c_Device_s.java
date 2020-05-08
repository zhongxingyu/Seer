 package hid.entity.java;
 
 import java.util.Set;
 
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.OnDelete;
 import org.hibernate.annotations.OnDeleteAction;
 import org.hibernate.validator.constraints.NotEmpty;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 
 import static javax.persistence.GenerationType.IDENTITY;
 
 @Entity
 @Table(name = "DEVICES")
 public class Device {
 
 	@Id
 	@GeneratedValue(strategy = IDENTITY)
 	@Column(name = "ID_DEVICE", nullable = false)
 	private long id;
 
 	@NotNull
 	@Column(name = "VENDOR_ID", nullable = false)
 	private int vendorId;
 
 	@NotNull
 	@Column(name = "PRODUCT_ID", nullable = false)
 	private int productId;
 
	@SuppressWarnings("deprecation")
	@OneToMany(mappedBy = "device", fetch = FetchType.EAGER)
	@Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE,
          org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
 	@OnDelete(action=OnDeleteAction.CASCADE)
 	private Set<Connection> connections;
 
 	public Set<Connection> getConnections() {
 		return connections;
 	}
 
 	public void setConnections(Set<Connection> connections) {
 		this.connections = connections;
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public int getVendorId() {
 		return vendorId;
 	}
 
 	public void setVendorId(int vendorId) {
 		this.vendorId = vendorId;
 	}
 
 	public int getProductId() {
 		return productId;
 	}
 
 	public void setProductId(int productId) {
 		this.productId = productId;
 	}
 }
